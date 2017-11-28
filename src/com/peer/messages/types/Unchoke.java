package com.peer.messages.types;

import com.peer.messages.ActualMsg;
import com.peer.utilities.MessageType;

public class Unchoke extends ActualMsg  {
	public Unchoke() {
		setLength(1);
		setType(MessageType.UNCHOKE);
		setPayload(new byte[0]);
	}
}
