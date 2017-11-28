package com.peer;

import java.io.IOException;
import java.util.BitSet;
import java.util.Map;

import org.apache.log4j.Logger;

import com.peer.file.FileOperations;
import com.peer.messages.Message;
import com.peer.messages.types.Have;
import com.peer.utilities.CommonUtils;
import com.peer.utilities.MessageType;
import com.peer.utilities.PeerProperties;

//peer has a file handler
//manages the 2 BitSets, depicting requestedParts and receivedParts
public class FileHandler {
	final static Logger logger = Logger.getLogger(NewConnectionHandler.class);
	private FileOperations fileOps;

	private BitSet receivedPieces;// piece I have
	private RequestedPieces piecesBeingRequested; // pieces I have requested for
	private Map<Integer, PeerInfo> peerMap;
	int pieceSize;
	int bitsetSize;
	int peerID;
	
	public FileHandler(int peerId, PeerProperties properties,
			Map<Integer, PeerInfo> peerMap) {
		this.peerMap = peerMap;
		this.pieceSize = properties.getPieceSize();
		bitsetSize = properties.getNumberOfPieces();
		this.peerID = peerId;
		receivedPieces = new BitSet(bitsetSize);
		piecesBeingRequested = new RequestedPieces(bitsetSize, properties.getUnchokingInterval());
		fileOps = new FileOperations(peerId, properties.getFileName());
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
	public synchronized void addPiece(int pieceID, byte[] piece, int clientPeerId) {
		final boolean isNewPiece = !receivedPieces.get(pieceID);

		receivedPieces.set(pieceID);

		if (isNewPiece) {
			fileOps.writePieceToFile(piece, pieceID, clientPeerId);
			broadcastHaveMessageToAllPeers(pieceID);
		}
		if (isFileCompleted()) {
			fileOps.mergeFile(receivedPieces.cardinality());
			if (isEverythingComplete()) {
				logger.info("No.of active threads were: " + Thread.activeCount());
				System.exit(0);
			}
		}
	}

	private synchronized void broadcastHaveMessageToAllPeers(int pieceId) {
		peerMap.values().forEach(peerInfo -> {
			try {
				Have haveMessage = (Have) Message.getInstance(MessageType.HAVE);
				haveMessage.setPayload(CommonUtils.intToByteArray(pieceId));
				haveMessage.write(peerInfo.getSocketWriter());
			} catch (Exception e) {
				logger.warn("Could not broadcast \'Have\' to peer_" + peerInfo.getPeerId() + " " + e);
			}
		});
	}

	public synchronized boolean isEverythingComplete() {
		for (PeerInfo peerInfo : peerMap.values()) {
			if (peerInfo.getBitfield().cardinality() != peerInfo.getBitfield().size()) {
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
				logger.warn("Problem closing Socket: "+ e);
			}
		});
	}

	/**
	 * @param availableParts
	 *            parts that are available at the remote peer
	 * @return the ID of the part to request, if any, or a negative number in case
	 *         all the missing parts are already being requested or the file is
	 *         complete.
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
	public synchronized void setAllPieces() {
		for (int i = 0; i < bitsetSize; i++) {
			receivedPieces.set(i, true);
		}
		logger.debug("Received parts set to: " + receivedPieces.toString());
	}

	public synchronized int getNumberOfReceivedParts() {
		return receivedPieces.cardinality();
	}

	byte[] getPiece(int pieceId) {
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
		logger.debug("Peer ["+peerID+"]has downloaded the complete file");
		return true;
	}

}
