package com.peer.messages.types;

import java.io.DataInputStream;

import com.peer.messages.ActualMsg;
import com.peer.utilities.MessageType;

public class NotInterested extends ActualMsg {

	public NotInterested(DataInputStream in) {
		super(in);
	}
	
	public NotInterested() {
		setLength(1);
		setType(MessageType.NOTINTERESTED);
		setPayload(null); //make sure null is ok
	}

}

