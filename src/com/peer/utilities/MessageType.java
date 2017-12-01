package com.peer.utilities;

import org.apache.log4j.Logger;

import com.peer.messages.ActualMsg;

public enum MessageType {

	CHOKE(0),
	UNCHOKE(1),
	INTERESTED(2),
	NOTINTERESTED(3),
	HAVE(4),
	BITFIELD(5),
	REQUEST(6),
	PIECE(7);
	private final byte value;
	private MessageType(int val) {
		this.value = (byte)val;
	}
	public byte getValue() {
		return value;
	}

	public static MessageType getMessageType(byte val) {
		for(MessageType type: MessageType.values()) {
			if(type.getValue()==val) {
				return type;
			}
		}
		System.out.println("byte value: "+val);
		throw new IllegalArgumentException();
	}
}
