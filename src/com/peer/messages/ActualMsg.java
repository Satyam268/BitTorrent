package com.peer.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.BitSet;

import org.apache.log4j.Logger;

import com.peer.utilities.MessageType;

public class ActualMsg extends Message {
	protected int length;//length includes type + payload
	protected MessageType type;//1 byte
	protected byte[] payload;
	final static Logger logger = Logger.getLogger(ActualMsg.class);


	public ActualMsg() {}

	public ActualMsg(DataOutputStream in) {
			logger.info("actual message constructor");
	}

	/*public void read(DataOutputStream in) throws ProtocolException, IOException {
			logger.info("reading length: "+ this.getLength());
			readPacketType(in);
			logger.info("reading packet type: "+ this.getType().getValue());
			readPacketPayload(in);
			logger.info("reading payload: "+ this.getPayload());
	}*/
	
/*	private void readPacketPayload(ObjectInputStream in) throws IOException {
		byte[] payload = new byte[this.length];//-1 or not?
		in.readFully(payload, 0, this.length);
		setPayload(payload);
	}

	private void readPacketType(ObjectInputStream in) throws IOException {
		byte type = in.readByte();
		setType((MessageType.getMessageType(type)));
	}

	private void readPacketLength(ObjectInputStream in) throws IOException {
		setLength(in.readInt());
	}*/


	public void write(ObjectOutputStream out) throws IOException {
		out.writeObject(this);
	}

	private void writePacketPayload(ObjectOutputStream out) throws IOException {
		out.write(this.getPayload(), 0, this.getPayload().length);
	}

	private void writePacketType(ObjectOutputStream out) throws IOException {
		out.writeByte(this.getType().getValue());
	}

	private void writePacketLength(ObjectOutputStream out) throws IOException {
		out.writeInt(this.getLength());
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
		return BitSet.valueOf(this.payload);
	}

}
