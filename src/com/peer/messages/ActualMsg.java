package com.peer.messages;

public abstract class ActualMsg extends Message {

	protected int length;//length includes type + payload
	protected byte type;
	protected byte[] payload;

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

}
