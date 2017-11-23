package com.peer;

import java.io.PrintWriter;
import java.util.HashMap;

import com.peer.messages.ActualMsg;
import com.peer.utilities.MessageType;

public class MessageHandler {

	// out stream of the socket
	public void handleMessage(ActualMsg message, PrintWriter out, int peerID, HashMap<Integer, PeerInfo> map) {
		MessageType msgType = message.getType();

		switch (msgType) {
		case CHOKE:
			handleChoke(peerID);
			break;
		case UNCHOKE:
		//	handleUnchoke();
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
		case BITFIELD:
			//handleBitfield();
			break;
		case REQUEST:
			//handleRequest();
			break;
		case PIECE:
			//handlePiece();
			break;

		}

	}

	private void handleUnchoke(ActualMsg message, PrintWriter out) {
		// send request
	}

	private void handleChoke(int peerID) {
		// update in map choked is true
	}

}
