package com.peer.messages;

import java.io.IOException;
import java.io.Serializable;

import com.peer.Peer;
import com.peer.messages.types.BitField;
import com.peer.messages.types.Choke;
import com.peer.messages.types.Have;
import com.peer.messages.types.Interested;
import com.peer.messages.types.NotInterested;
import com.peer.messages.types.Piece;
import com.peer.messages.types.Request;
import com.peer.messages.types.Unchoke;
import com.peer.utilities.MessageType;

public class Message implements Serializable{

	Peer peer;
	Message(){}

	Message(Peer peer){
		this.peer=peer;
	}


	public static Message getInstance(MessageType type) throws ClassNotFoundException, IOException {
		switch (type) {
		case CHOKE:
			return new Choke();

		case UNCHOKE:
			return new Unchoke();

		case INTERESTED:
			return new Interested();

		case NOTINTERESTED:
			return new NotInterested();

		case HAVE:
			return new Have();

		case BITFIELD:
			return new BitField();

		case REQUEST:
			return new Request();

		case PIECE:
			return new Piece();

		default:
			throw new ClassNotFoundException("message type not handled: " + type.toString());
		}
	}

}
