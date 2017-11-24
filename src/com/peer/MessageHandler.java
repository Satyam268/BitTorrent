package com.peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.peer.messages.ActualMsg;
import com.peer.messages.Message;
import com.peer.messages.types.BitField;
import com.peer.messages.types.Interested;
import com.peer.messages.types.Piece;
import com.peer.messages.types.Request;
import com.peer.messages.types.Unchoke;
import com.peer.utilities.CommonUtils;
import com.peer.utilities.MessageType;

public class MessageHandler implements Runnable {
	final static Logger logger = Logger.getLogger(MessageHandler.class);
	DataInputStream in;
	DataOutputStream out;
	PeerInfo myInfo;
	Map<Integer, PeerInfo> map;
	int clientPeerID;

	public MessageHandler(DataInputStream in, DataOutputStream out, PeerInfo myInfo, Map<Integer, PeerInfo> map,
			int clientPeerID) {
		this.in = in;
		this.out = out;
		this.myInfo = myInfo;
		this.map = map;
		this.clientPeerID = clientPeerID;
	}

	@Override
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
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void handlePiece(ActualMsg message) {
		// write to file !! file manager
	}

	private void handleRequest(ActualMsg message) throws ClassNotFoundException, IOException {
		// send piece message
		Piece pieceMessage = (Piece) Message.getInstance(MessageType.PIECE);
	}

	private void handleHave(ActualMsg message) {
		// update bit field of peerInfo and set it to 1
		// TODO now
	}

	private void handleNotInterested(ActualMsg message) {
		// remove from list of interested messages
	}

	private void handleBitfield(ActualMsg message) throws ClassNotFoundException, IOException {
		BitField bitFieldMessage = (BitField) message;
		map.get(clientPeerID).setBitfield(bitFieldMessage.getPieceField());

		if (CommonUtils.hasAnyThingInteresting(bitFieldMessage.getPieceField(), myInfo.getBitfield())) {
			// Send new Interested message
			Interested interestedMessage = (Interested) Message.getInstance(MessageType.INTERESTED);
			interestedMessage.write(out);
		}
	}

	private void handleUnchoke(ActualMsg message) throws ClassNotFoundException, IOException {
		Unchoke unchokeMessage = (Unchoke) message;
		// select a piece you want to request based on what you want and what
		// you
		// haven't requested already
		PeerInfo clientPeerInfo = map.get(clientPeerID);
		Request requestMessage = (Request) Message.getInstance(MessageType.REQUEST);
		int interestedPieceId = getInterestedPieceId(myInfo, clientPeerInfo);
		requestMessage.setPayload(CommonUtils.intToByteArray(interestedPieceId));
		requestMessage.write(out);

	}

	private int getInterestedPieceId(PeerInfo myInfo2, PeerInfo clientPeerInfo) {
		return 10;
	}

	private void handleChoke(ActualMsg message) {
		// update in map choked is true
		// cannot make request to this uploader and update requested for a
		// particular
		// piece
		// if it was receiving one from another peer who choked it
	}

	private void handleInterested(ActualMsg message) {
		// add to the list of interested peers !!
	}

}
