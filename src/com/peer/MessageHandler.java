package com.peer;

import java.io.PrintWriter;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.peer.messages.*;
import com.peer.messages.types.*;
import com.peer.utilities.MessageType;

public class MessageHandler {
	final static Logger logger = Logger.getLogger(MessageHandler.class);
	// out stream of the socket
//	PrintWriter out;
//	HashMap<Integer, PeerInfo> peerMap;
	public void handleMessage(ActualMsg message, PrintWriter out, PeerInfo myInfo, HashMap<Integer, PeerInfo> map, int clientPeerID) {
		MessageType msgType = message.getType();
		logger.debug("Handler received: Msg-Type "+ msgType);
		switch (msgType) {
		case BITFIELD:
			handleBitfield(message, out, myInfo, map, clientPeerID);
			break;
		case CHOKE:
			handleChoke(message, out, myInfo, map,clientPeerID);
			break;
		case UNCHOKE:
			handleUnchoke(message, out, myInfo, map, clientPeerID);
			break;
		case INTERESTED:
			//handleInterested();
			break;
		case NOTINTERESTED:
			//handleNotInterested();
			break;
		case HAVE:
			//handleHave();
			break;
		case REQUEST:
			//handleRequest();
			break;
		case PIECE:
			//handlePiece();
			break;

		}

	}



	private void handleBitfield(ActualMsg message, PrintWriter out, PeerInfo myInfo, HashMap<Integer, PeerInfo> map, int clientPeerID) {
		BitField bitFieldMessage = (BitField)message;
		map.get(clientPeerID).setBitfield(bitFieldMessage.getPieceField());
		if(bitFieldMessage.hasAnyThingInteresting(myInfo.getBitfield())) {
			// Send new Interested message
		}	
	}



	private void handleUnchoke(ActualMsg message, PrintWriter out, PeerInfo myInfo, HashMap<Integer, PeerInfo> map, int clientPeerID) {
		Unchoke unchokeMessage = (Unchoke)message;
		map.get(clientPeerID).getBitfield()
	}

	private void handleChoke(ActualMsg message, PrintWriter out, PeerInfo myInfo, HashMap<Integer, PeerInfo> map, int clientPeerID) {
		// update in map choked is true
	}

}
