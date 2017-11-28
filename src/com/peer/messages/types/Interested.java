package com.peer.messages.types;

import java.io.DataInputStream;

import com.peer.messages.ActualMsg;
import com.peer.utilities.MessageType;

public class Interested extends ActualMsg  {

	public Interested() {
		setLength(1);
		setType(MessageType.INTERESTED);
		setPayload(new byte[0]); 
	}	

}
