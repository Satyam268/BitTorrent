package com.peer.messages.types;

import com.peer.messages.ActualMsg;
import com.peer.utilities.MessageType;

public class Have extends ActualMsg {
	public Have() {
		setLength(5);
		setType(MessageType.HAVE);
	}

}
