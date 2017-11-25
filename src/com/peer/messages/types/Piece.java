package com.peer.messages.types;

import com.peer.messages.ActualMsg;
import com.peer.utilities.MessageType;

public class Piece extends ActualMsg {

	public Piece() {
		setType(MessageType.PIECE);
	}
}
