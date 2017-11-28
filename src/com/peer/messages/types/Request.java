package com.peer.messages.types;

import com.peer.messages.ActualMsg;
import com.peer.utilities.MessageType;

public class Request extends ActualMsg {
	public Request() {
		setLength(5);
		setType(MessageType.REQUEST);
	}
}
