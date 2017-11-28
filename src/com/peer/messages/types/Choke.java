package com.peer.messages.types;

import com.peer.messages.ActualMsg;
import com.peer.utilities.MessageType;

public class Choke extends ActualMsg {

	public Choke() {
		setLength(1);
		setType(MessageType.CHOKE);
		setPayload(new byte[0]); 
	}

}
