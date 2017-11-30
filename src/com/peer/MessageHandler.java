package com.peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import org.apache.log4j.Logger;

import com.peer.file.FileHandler;
import com.peer.messages.ActualMsg;
import com.peer.messages.Message;
import com.peer.messages.types.BitField;
import com.peer.messages.types.Have;
import com.peer.messages.types.Interested;
import com.peer.messages.types.NotInterested;
import com.peer.messages.types.Piece;
import com.peer.messages.types.Request;
import com.peer.utilities.CommonUtils;
import com.peer.utilities.MessageType;

public class MessageHandler {
	final static Logger logger = Logger.getLogger(MessageHandler.class);
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private PeerInfo myInfo;
	private Map<Integer, PeerInfo> peerMap;
	private int clientPeerID;
	private FileHandler fileHandler;

	public MessageHandler(ObjectInputStream in, ObjectOutputStream out, PeerInfo myInfo, Map<Integer, PeerInfo> peerMap,
			int clientPeerID, FileHandler fileHandler) {
		this.in = in;
		this.out = out;
		this.myInfo = myInfo;
		this.peerMap = peerMap;
		this.clientPeerID = clientPeerID;
		this.fileHandler = fileHandler;
	}

	public void handleMessage() throws ClassNotFoundException, IOException {
		ActualMsg message = null;
		message = (ActualMsg) in.readObject();
		 logger.info(" ------ incoming message " + message + " received from " +
		 clientPeerID + " -----------------------");
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
			logger.debug("Peer [peer_ID " + myInfo.peerId + "] received the ‘have’ message from [peer_ID "
					+ clientPeerID + "] for the piece [" + (CommonUtils.byteArrayToInt(message.getPayload())) + "]");
			break;
		case REQUEST:
			handleRequest(message);
			break;
		case PIECE:
			handlePiece(message);
			break;
		}
	}

	private void handleBitfield(ActualMsg message) throws ClassNotFoundException, IOException {
		BitField bitFieldMessage = (BitField) message;
		peerMap.get(clientPeerID).setBitfield(bitFieldMessage.getPayloadInBitSet());
		System.out.println("bitFiled payload:- " + bitFieldMessage.getPayloadInBitSet());
		if (CommonUtils.hasAnyThingInteresting(bitFieldMessage.getPayloadInBitSet(), myInfo.getBitfield())) {
			sendInterestedMessage(out);
		} else {
			sendNotInterestedMessage(out);
		}
	}

	private void sendNotInterestedMessage(ObjectOutputStream out) throws IOException, ClassNotFoundException {
		NotInterested notInterestedMessage = (NotInterested) Message.getInstance(MessageType.NOTINTERESTED);
		notInterestedMessage.write(out);

	}

	private void sendInterestedMessage(ObjectOutputStream out) throws ClassNotFoundException, IOException {
		Interested interestedMessage = (Interested) Message.getInstance(MessageType.INTERESTED);
		interestedMessage.write(out);
	}

	synchronized private void handlePiece(ActualMsg message) throws ClassNotFoundException, IOException {
		PeerInfo peerInfo = peerMap.get(clientPeerID);
		fileHandler.addPiece(peerInfo.getRequestedPieceIndex(), message.getPayload(), clientPeerID);
		// fileHandler.broadcastHaveMessageToAllPeers(peerInfo.getRequestedPieceIndex());
		logger.debug("Peer [peer_ID " + myInfo.peerId + "] has downloaded the piece ["
				+ peerInfo.getRequestedPieceIndex() + "] from [peer_ID " + clientPeerID + "]");
		peerInfo.setRequestedPieceIndex(-1);
		// after you receive a piece send another request message....
		sendRequestMessage(out);
	}

	synchronized private void sendRequestMessage(ObjectOutputStream out) throws ClassNotFoundException, IOException {
		PeerInfo clientPeerInfo = peerMap.get(clientPeerID);
		Request requestMessage = (Request) Message.getInstance(MessageType.REQUEST);
		int interestedPieceId = getInterestedPieceId(clientPeerInfo);
		logger.info("Intereted piece ID:- " + interestedPieceId);
		

		if (interestedPieceId != -1) {
			clientPeerInfo.setRequestedPieceIndex(interestedPieceId);
			logger.info("Common utils method check" + CommonUtils.intToByteArray(interestedPieceId));
			requestMessage.setPayload(CommonUtils.intToByteArray(interestedPieceId));
			requestMessage.write(out);
		}
	}

	private void handleRequest(ActualMsg message) throws ClassNotFoundException, IOException {
		int pieceIndex = CommonUtils.byteArrayToInt(message.getPayload());
		byte[] piece = fileHandler.getPiece(pieceIndex);
		Piece pieceMessage = (Piece) Message.getInstance(MessageType.PIECE);
		pieceMessage.setLength(piece.length);
		pieceMessage.setPayload(piece);
		pieceMessage.write(out);

	}

	private void handleHave(ActualMsg message) throws ClassNotFoundException, IOException {
		int pieceIndex = CommonUtils.byteArrayToInt(message.getPayload());
		peerMap.get(clientPeerID).setBitfieldAtIndex(pieceIndex);
		if (!fileHandler.hasPiece(pieceIndex))
			sendInterestedMessage(out);
		if (fileHandler.isEverythingComplete()) {
			logger.info("-----------System.exit()-----------");
			System.exit(0);
		}
	}

	private void handleNotInterested(ActualMsg message) {
		PeerInfo peerInfo = peerMap.get(clientPeerID);
		peerInfo.setInterested(false);
	}

	synchronized private void handleUnchoke(ActualMsg message) throws ClassNotFoundException, IOException {
		logger.info("In unchoke messasge handler :-- ");
		sendRequestMessage(out);
	}

	private int getInterestedPieceId(PeerInfo clientPeerInfo) {
		return fileHandler.getPartToRequest(clientPeerInfo.getBitfield());
	}

	private void handleChoke(ActualMsg message) {
		logger.info("In ><choke>< messasge handler :-- ");
		PeerInfo peerInfo = peerMap.get(clientPeerID);
		peerInfo.setRequestedPieceIndex(-1);
	}

	private void handleInterested(ActualMsg message) throws ClassNotFoundException, IOException {
		peerMap.get(clientPeerID).setInterested(true);
	}

}
