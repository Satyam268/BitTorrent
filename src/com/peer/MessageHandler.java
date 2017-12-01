package com.peer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.log4j.Logger;

import com.peer.file.FileHandler;
import com.peer.messages.ActualMsg;
import com.peer.messages.Message;
import com.peer.messages.types.BitField;
import com.peer.messages.types.Interested;
import com.peer.messages.types.NotInterested;
import com.peer.messages.types.Piece;
import com.peer.messages.types.Request;
import com.peer.utilities.CommonUtils;
import com.peer.utilities.MessageType;

public class MessageHandler {
	final static Logger logger = Logger.getLogger(MessageHandler.class);
	private InputStream in;
	private OutputStream out;
	private PeerInfo myInfo;
	private Map<Integer, PeerInfo> peerMap;
	private int clientPeerID;
	private FileHandler fileHandler;

	public MessageHandler(InputStream in, OutputStream out, PeerInfo myInfo, Map<Integer, PeerInfo> peerMap,
			int clientPeerID, FileHandler fileHandler) {
		this.in = in;
		this.out = out;
		this.myInfo = myInfo;
		this.peerMap = peerMap;
		this.clientPeerID = clientPeerID;
		this.fileHandler = fileHandler;
	}

	public void handleMessage() throws ClassNotFoundException, IOException {
		try {
			ActualMsg message = null;
			message = readIncomingMessage(in);
			/*
			 * logger.info(" ------ incoming message " + message +
			 * " received from " + clientPeerID + " -----------------------");
			 */
			MessageType msgType = message.getType();
			switch (msgType) {
			case BITFIELD:
				handleBitfield(message);
				break;
			case CHOKE:
				handleChoke(message);
				logger.debug("Peer [peer_ID" + myInfo.peerId + "] is choked by [peer_ID " + clientPeerID + "]");
				break;
			case UNCHOKE:
				handleUnchoke(message);
				logger.debug("Peer [peer_ID " + myInfo.peerId + "] is unchoked by [peer_ID " + clientPeerID + "]");
				break;
			case INTERESTED:
				handleInterested(message);
				logger.debug("Peer [peer_ID " + myInfo.peerId + "] received the ‘interested’ message from [peer_ID "
						+ clientPeerID + "]");
				break;
			case NOTINTERESTED:
				handleNotInterested(message);
				logger.debug("Peer [peer_ID " + myInfo.peerId + "] received the ‘not interested’ message from [peer_ID "
						+ clientPeerID + "]");
				break;
			case HAVE:
				handleHave(message);
				logger.debug(
						"Peer [peer_ID " + myInfo.peerId + "] received the ‘have’ message from [peer_ID " + clientPeerID
								+ "] for the piece [" + (CommonUtils.byteArrayToInt(message.getPayload())) + "]");
				break;
			case REQUEST:
				handleRequest(message);
				break;
			case PIECE:
				handlePiece(message);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn("---- Bloody exception ---" + e);
			throw e;
		}
	}

	private ActualMsg readIncomingMessage(InputStream in) throws IOException {
		ActualMsg msg = new ActualMsg();
		byte[] bitFieldMsgLengthArray = new byte[4];
		in.read(bitFieldMsgLengthArray, 0, 4);
		msg.setLength(ByteBuffer.wrap(bitFieldMsgLengthArray, 0, 4).getInt());

		byte[] msgType = new byte[1];
		in.read(msgType, 0, 1);
		msg.setType(MessageType.getMessageType(msgType[0]));

		// messageType = ByteBuffer.wrap(msgType, 0, 1).get();
		byte[] payload = null;
		if (msg.getLength() > 1) {
			payload = new byte[msg.getLength() - 1];
			in.read(payload, 0, msg.getLength() - 1);
		}
		return msg;
	}

	private void handleBitfield(ActualMsg message) throws ClassNotFoundException, IOException {
		BitField bitFieldMessage = (BitField) message;
		peerMap.get(clientPeerID).setBitfield(bitFieldMessage.getPayloadInBitSet());
		if (CommonUtils.hasAnyThingInteresting(bitFieldMessage.getPayloadInBitSet(), myInfo.getBitfield())) {
			sendInterestedMessage(out);
		} else {
			sendNotInterestedMessage(out);
		}
	}

	private void sendNotInterestedMessage(OutputStream out) throws IOException, ClassNotFoundException {
		NotInterested notInterestedMessage = (NotInterested) Message.getInstance(MessageType.NOTINTERESTED);
		notInterestedMessage.write(out);
	}

	private void sendInterestedMessage(OutputStream out) throws ClassNotFoundException, IOException {
		Interested interestedMessage = (Interested) Message.getInstance(MessageType.INTERESTED);
		interestedMessage.write(out);
	}

	synchronized private void handlePiece(ActualMsg message) throws ClassNotFoundException, IOException {
		PeerInfo peerInfo = peerMap.get(clientPeerID);
		if (peerInfo.getRequestedPieceIndex() == -1)
			return;

		logger.debug("In handlePiece requested piece was- " + peerInfo.getRequestedPieceIndex());

		fileHandler.addPiece(peerInfo.getRequestedPieceIndex(), message.getPayload(), clientPeerID);
		logger.debug("Peer [peer_ID " + myInfo.peerId + "] has downloaded the piece ["
				+ peerInfo.getRequestedPieceIndex() + "] from [peer_ID " + clientPeerID + "]");
		peerInfo.setRequestedPieceIndex(-1);
		// after you receive a piece send another request message....
		sendRequestMessage(out);
	}

	synchronized private void sendRequestMessage(OutputStream out) throws ClassNotFoundException, IOException {
		PeerInfo clientPeerInfo = peerMap.get(clientPeerID);
		Request requestMessage = (Request) Message.getInstance(MessageType.REQUEST);
		int interestedPieceId = getInterestedPieceId(clientPeerInfo);

		if (interestedPieceId != -1) {
			clientPeerInfo.setRequestedPieceIndex(interestedPieceId);
			requestMessage.setPayload(CommonUtils.intToByteArray(interestedPieceId));
			requestMessage.write(out);
		}
	}

	private void handleRequest(ActualMsg message) throws ClassNotFoundException, IOException {
		int pieceIndex = CommonUtils.byteArrayToInt(message.getPayload());
		logger.debug("In handle request requested piece- " + pieceIndex);
		byte[] piece = fileHandler.getPiece(pieceIndex);
		Piece pieceMessage = (Piece) Message.getInstance(MessageType.PIECE);
		pieceMessage.setLength(piece.length);
		pieceMessage.setPayload(piece);
		pieceMessage.write(out);
	}

	private synchronized void handleHave(ActualMsg message) throws ClassNotFoundException, IOException {
		int pieceIndex = CommonUtils.byteArrayToInt(message.getPayload());
		peerMap.get(clientPeerID).setBitfieldAtIndex(pieceIndex);
		if (!fileHandler.hasPiece(pieceIndex))
			sendInterestedMessage(out);
		logger.info("PeerId" + clientPeerID + " cardinal: " + peerMap.get(clientPeerID).getBitfield().cardinality()
				+ " filehandler bitmap size: " + fileHandler.getBitmapSize());
		if (peerMap.get(clientPeerID).getBitfield().cardinality() == fileHandler.getBitmapSize()) {
			if (fileHandler.isEverythingComplete()) {
				logger.info("-----------System.exit()-----------");
				System.exit(0);
			}
		}
	}

	private void handleNotInterested(ActualMsg message) {
		PeerInfo peerInfo = peerMap.get(clientPeerID);
		peerInfo.setInterested(false);
	}

	synchronized private void handleUnchoke(ActualMsg message) throws ClassNotFoundException, IOException {
		sendRequestMessage(out);
	}

	private int getInterestedPieceId(PeerInfo clientPeerInfo) {
		return fileHandler.getPartToRequest(clientPeerInfo.getBitfield());
	}

	private void handleChoke(ActualMsg message) {
		PeerInfo peerInfo = peerMap.get(clientPeerID);
		peerInfo.setRequestedPieceIndex(-1);
	}

	private void handleInterested(ActualMsg message) throws ClassNotFoundException, IOException {
		peerMap.get(clientPeerID).setInterested(true);
	}

}
