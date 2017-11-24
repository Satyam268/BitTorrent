package com.peer.messages.types;

import java.io.DataInputStream;

import com.peer.messages.ActualMsg;
import com.peer.utilities.MessageType;

public class Unchoke extends ActualMsg  {

	public Unchoke(DataInputStream in) {
		super(in);
	}
	
	public Unchoke() {
		setLength(1);
		setType(MessageType.UNCHOKE);
		setPayload(null); //make sure null is ok
	}
}
