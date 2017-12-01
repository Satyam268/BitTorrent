package com.peer.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.BitSet;

import org.apache.log4j.Logger;

import com.peer.utilities.MessageType;

public class ActualMsg extends Message {

	protected int length;
	protected MessageType type;
	protected byte[] payload;
	final static Logger logger = Logger.getLogger(ActualMsg.class);

	public ActualMsg() {
	}

	public ActualMsg(DataOutputStream in) {

	}

	public void write(ObjectOutputStream out) throws IOException {
		//logger.info(">>>>>>>>>>>>>>>>>>>>> Sent Message details " + this+">>>>>>>>>>>>>>>>>>>>>");
		out.writeObject(this);
	}

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

	public BitSet getPayloadInBitSet() {
		System.out.println(this);
		return BitSet.valueOf(this.payload);
	}

	public String toString() {
		return "Message Details -- length: " + length + " type: " + type + " payload: " + ((this.getType()==MessageType.PIECE)?"":(new String(payload)));
	}
}
