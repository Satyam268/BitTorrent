package com.peer.file;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Map;

import org.apache.log4j.Logger;

import com.peer.PeerInfo;
import com.peer.PeerProperties;
import com.peer.RequestedPieces;
import com.peer.messages.Message;
import com.peer.messages.types.Have;
import com.peer.utilities.CommonUtils;
import com.peer.utilities.MessageType;

//peer has a file handler
//manages the 2 BitSets, depicting requestedParts and receivedParts
/**
 * @author mypc
 * contructor for the fileHandler class responsible for
 */
public class FileHandler {
	final static Logger logger = Logger.getLogger(FileHandler.class);
	private FileOperations fileOps;

	private BitSet receivedPieces;// piece I have
	private RequestedPieces piecesBeingRequested; // pieces I have requested for
	private Map<Integer, PeerInfo> peerMap;
	int pieceSize;
	int bitsetSize;
	int peerID;
	int hasFile;
	PeerProperties properties;


	public FileHandler(int peerId, PeerProperties properties, Map<Integer, PeerInfo> peerMap, PeerInfo peerInfo) {
		this.peerMap = peerMap;
		this.pieceSize = properties.getPieceSize();
		this.bitsetSize = properties.getNumberOfPieces();
		this.peerID = peerId;
		this.hasFile = peerInfo.getHasFile();
		this.receivedPieces = peerInfo.bitfield;

		this.fileOps = new FileOperations(peerId, properties.getFileName());
		if (hasFile == 1) {
			// split file
			fileOps.processFileIntoPieceFiles(new File(properties.getFileName()), properties.getPieceSize());
			receivedPieces.set(0, bitsetSize);
			properties.randomlySelectPreferredNeighbors.set(true);
		}
		this.piecesBeingRequested = new RequestedPieces(bitsetSize, properties.getUnchokingInterval(), peerInfo.bitfield);
		this.properties = properties;

	}

	public FileHandler(int peerId) {
		this.peerID = peerId;
	}


	/**
	 * @param pieceID
	 * @param piece
	 * @param clientPeerId
	 */
	public synchronized void addPiece(int pieceID, byte[] piece, int clientPeerId) {
		if (pieceID == -1)
			return;

		final boolean isNewPiece = !receivedPieces.get(pieceID);
		receivedPieces.set(pieceID);

		if (isNewPiece) {
			fileOps.writePieceToFile(piece, pieceID, clientPeerId);
			broadcastHaveMessageToAllPeers(pieceID);
			int bytesDownloaded = peerMap.get(clientPeerId).bytesDownloaded.get() + piece.length;
			peerMap.get(clientPeerId).bytesDownloaded.set(bytesDownloaded);
		}
		if (isFileCompleted()) {
			properties.randomlySelectPreferredNeighbors.set(true);
			fileOps.mergeFile(receivedPieces.cardinality());
			if (isEverythingComplete()) {
				logger.info("No.of active threads were: " + Thread.activeCount());
				System.exit(0);
			}
		}
	}

	public synchronized void broadcastHaveMessageToAllPeers(int pieceId) {
		peerMap.values().forEach(peerInfo -> {
			try {
				Have haveMessage = (Have) Message.getInstance(MessageType.HAVE);
				haveMessage.setPayload(CommonUtils.intToByteArray(pieceId));
				if (peerInfo.getSocketWriter() != null)
					haveMessage.write(peerInfo.getSocketWriter());

			} catch (Exception e) {
				logger.warn("Could not broadcast \'Have\' to peer_" + peerInfo.getPeerId() + " " + e);
			}
		});
	}

	public synchronized boolean isEverythingComplete() {
		for (PeerInfo peerInfo : peerMap.values()) {
			if (peerInfo.getBitfield().cardinality() != getBitmapSize()) {
				return false;
			}
		}
		closeAllSockets();
		return true;
	}

	private void closeAllSockets() {
		peerMap.values().forEach(peerInfo -> {
			try {
				peerInfo.getClientSocket().close();
			} catch (IOException e) {
				logger.warn("Problem closing Socket: " + e);
			}
		});
	}

	public synchronized int getPartToRequest(BitSet availableParts) {
		logger.info("Available parts: "+availableParts+" ReceivedParts: " + getReceivedParts());
		availableParts.andNot(getReceivedParts());
		return piecesBeingRequested.getPartToRequest(availableParts);
	}

	public synchronized BitSet getReceivedParts() {
		return (BitSet) receivedPieces.clone();
	}

	synchronized public boolean hasPiece(int pieceIndex) {
		return receivedPieces.get(pieceIndex);
	}


	public synchronized int getNumberOfReceivedParts() {
		return receivedPieces.cardinality();
	}

	public byte[] getPiece(int pieceId) {
		byte[] piece = fileOps.getPieceFromFile(pieceId);
		return piece;
	}

	public byte[][] getAllPieces() {
		return fileOps.getAllpiecesAsByteArrays();
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
		logger.debug("Peer [" + peerID + "] has downloaded the complete file");
		return true;
	}

}
