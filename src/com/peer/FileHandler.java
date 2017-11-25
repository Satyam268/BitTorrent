package com.peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.peer.file.FileOperations;

//peer has a file handler
//manages the 2 BitSets, depicting requestedParts and receivedParts
public class FileHandler {
	final static Logger logger = Logger.getLogger(NewConnectionHandler.class);
	private final Collection<FileHandlerListner> listeners = new LinkedList<>();
	private FileOperations fileOps;

	private BitSet receivedPieces;//piece I have
	private RequestedPieces piecesBeingRequested; //pieces I have requested for
	
	int pieceSize;
	int bitsetSize;
	int peerID;

	FileHandler(int peerId, String fileName, int fileSize, int pieceSize, int unchokingInterval) {
		this.pieceSize = pieceSize;
		bitsetSize = (int) Math.ceil(fileSize / pieceSize);
		this.peerID = peerId;

		logger.debug("File size set to " + fileSize + "\tPart size set to " + pieceSize + "\tBitset size set to "+ bitsetSize);
		receivedPieces = new BitSet(bitsetSize);
		piecesBeingRequested = new RequestedPieces(bitsetSize, unchokingInterval);
		fileOps = new FileOperations(peerId, fileName);
	}

	public FileHandler(int peerId) {
		this.peerID = peerId;
	}

	/**
	 * got a new piece message; add it to receivedParts
	 *
	 * @param pieceIndex
	 * @param piece
	 */
	public synchronized void addPiece(int pieceID, byte[] piece) {
		final boolean isNewPiece = !receivedPieces.get(pieceID);
		//write into a file
		receivedPieces.set(pieceID);

		if (isNewPiece) {
			fileOps.writePieceToFile(piece, pieceID);
			
			
			for (FileHandlerListner listener : listeners) {
				listener.pieceArrived(pieceID);
			}
		}
		if (isFileCompleted()) {
			fileOps.mergeFile(receivedPieces.cardinality());
			for (FileHandlerListner listener : listeners) {
				listener.fileCompleted();
			}
		}
	}

	/**
	 * @param availableParts
	 *            parts that are available at the remote peer
	 * @return the ID of the part to request, if any, or a negative number in
	 *         case all the missing parts are already being requested or the
	 *         file is complete.
	 */
	synchronized int getPartToRequest(BitSet availableParts) {
		availableParts.andNot(getReceivedParts());
		return piecesBeingRequested.getPartToRequest(availableParts);
	}

	public synchronized BitSet getReceivedParts() {
		return (BitSet) receivedPieces.clone();
	}

	synchronized public boolean hasPiece(int pieceIndex) {
		return receivedPieces.get(pieceIndex);
	}

	/**
	 * Set all parts as received.
	 */
	public synchronized void setAllParts() {
		for (int i = 0; i < bitsetSize; i++) {
			receivedPieces.set(i, true);
		}
		logger.debug("Received parts set to: " + receivedPieces.toString());
	}

	public synchronized int getNumberOfReceivedParts() {
		return receivedPieces.cardinality();
	}

	byte[] getPiece(int partId) {
		byte[] piece = fileOps.getPartAsByteArray(partId);
		return piece;
	}

	public void registerListener(FileHandlerListner listener) {
		listeners.add(listener);
	}

	public void splitFile() {
		fileOps.splitFile((int) pieceSize);
	}

	public byte[][] getAllPieces() {
		return fileOps.getAllPartsAsByteArrays();
	}

	public int getBitmapSize() {
		return bitsetSize;
	}

	private boolean isFileCompleted() {
		for (int i = 0; i < bitsetSize; i++) {
			if (!receivedPieces.get(i)) {
				return false;
			}
		}
		return true;
	}
}



