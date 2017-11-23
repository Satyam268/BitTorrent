package com.peer.messages;

import com.peer.utilities.MessageType;

public abstract class ActualMsg extends Message {

	protected int length;//length includes type + payload
	protected MessageType type;
	protected byte[] payload;

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

}
