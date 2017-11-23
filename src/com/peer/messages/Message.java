package com.peer.messages;

import java.io.IOException;

import com.peer.messages.types.*;
import com.peer.utilities.MessageType;

public abstract class Message {

	void send() {

	}

	void receive() {

	}

	public static Message getInstance(int length, MessageType type) throws ClassNotFoundException, IOException {
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
			//return new Have(new byte[length]);

		case BITFIELD:
			return new BitField();
			//return new Bitfield(new byte[length]);

		case REQUEST:
			return new Request();
			//return new Request(new byte[length]);

		case PIECE:
			return new Piece();
			//return new Piece(new byte[length]);

		default:
			throw new ClassNotFoundException("message type not handled: " + type.toString());
		}
	}

}
