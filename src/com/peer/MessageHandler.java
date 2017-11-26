package com.peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

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

public class MessageHandler implements Runnable {
	final static Logger logger = Logger.getLogger(MessageHandler.class);
	private DataInputStream in;
	private DataOutputStream out;
	private PeerInfo myInfo;
	private Map<Integer, PeerInfo> peerMap;
	private int clientPeerID;
	private FileHandler fileHandler;

	public MessageHandler(DataInputStream in, DataOutputStream out, PeerInfo myInfo, Map<Integer, PeerInfo> peerMap,
			int clientPeerID, FileHandler fileHandler) {
		logger.info("creating new object");
		this.in = in;
		this.out = out;
		this.myInfo = myInfo;
		this.peerMap = peerMap;
		this.clientPeerID = clientPeerID;
		this.fileHandler = fileHandler;
	}

	public void run() {
		ActualMsg message = new ActualMsg(in);
		MessageType msgType = message.getType();

		logger.debug("\nMsg-Type " + msgType +" received from " + clientPeerID);
		try {

			switch (msgType) {
			case BITFIELD:
				handleBitfield(message);
				break;
			case CHOKE:
				handleChoke(message);
				break;
			case UNCHOKE:
				handleUnchoke(message);
				break;
			case INTERESTED:
				handleInterested(message);
				break;
			case NOTINTERESTED:
				handleNotInterested(message);
				break;
			case HAVE:
				handleHave(message);
				break;
			case REQUEST:
				handleRequest(message);
				break;
			case PIECE:
				handlePiece(message);
				break;
			}
		} catch (Exception e) {
			logger.warn("Message type not found exception "+e);
		} 
	}

	private void handleBitfield(ActualMsg message) throws ClassNotFoundException, IOException {
		BitField bitFieldMessage = (BitField) message;
		peerMap.get(clientPeerID).setBitfield(bitFieldMessage.getPayloadInBitSet());
		if (CommonUtils.hasAnyThingInteresting(bitFieldMessage.getPayloadInBitSet(), myInfo.getBitfield())) {
			sendInterestedMessage(out);
		}
		else {
			sendNotInterestedMessage(out);	
		}
	}

	
	private void sendNotInterestedMessage(DataOutputStream out) throws IOException, ClassNotFoundException {
		NotInterested notInterestedMessage = (NotInterested) Message.getInstance(MessageType.NOTINTERESTED);
		notInterestedMessage.write(out);

	}

	private void sendInterestedMessage(DataOutputStream out) throws ClassNotFoundException, IOException {
		Interested interestedMessage = (Interested) Message.getInstance(MessageType.INTERESTED);
		interestedMessage.write(out);

	}

	private void handlePiece(ActualMsg message) throws ClassNotFoundException, IOException {
		PeerInfo peerInfo = peerMap.get(clientPeerID);
		fileHandler.addPiece(peerInfo.getRequestedPieceIndex(), message.getPayload());
		peerInfo.setRequestedPieceIndex(-1);
		// after you receive a piece send another request message....
		sendRequestMessage(out);
	}

	private void sendRequestMessage(DataOutputStream out) throws ClassNotFoundException, IOException {
		PeerInfo clientPeerInfo = peerMap.get(clientPeerID);
		Request requestMessage = (Request) Message.getInstance(MessageType.REQUEST);
		int interestedPieceId = getInterestedPieceId(clientPeerInfo);
		if(interestedPieceId!=-1) {
			clientPeerInfo.setRequestedPieceIndex(interestedPieceId); // set the requested piece in Neighbor's PeerInfo
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
		Have haveMessage = (Have)Message.getInstance(MessageType.HAVE);
		haveMessage.setPayload(message.getPayload());
		int pieceIndex = CommonUtils.byteArrayToInt(haveMessage.getPayload());
		peerMap.get(clientPeerID).setBitfieldAtIndex(pieceIndex);
		if(!fileHandler.hasPiece(pieceIndex))
			sendInterestedMessage(out);
		if(fileHandler.isEverythingComplete()){
			System.exit(0);
		}
	}

	private void handleNotInterested(ActualMsg message) {
		PeerInfo peerInfo =  peerMap.get(clientPeerID);
		peerInfo.setInterested(false);
	}

	
	private void handleUnchoke(ActualMsg message) throws ClassNotFoundException, IOException {
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
