package com.peer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;

import com.peer.utilities.MessageType;

public class ActualMsg extends Message { /// rename methods !!! // throw exceptions if packets do not conform their protocol

	protected int length;//length includes type + payload
	protected MessageType type;//1 byte
	protected byte[] payload;

	public ActualMsg() {}

	public ActualMsg(DataInputStream in) {
		try {
			read(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void read(DataInputStream in) throws ProtocolException, IOException {
		try {
			setPacketLength(in);
			setType(in);
			setPayload(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void write(DataOutputStream out) throws IOException {
		out.writeInt(this.getLength());
		out.writeByte(this.getType().getValue());
		out.write(this.getPayload(), 0, this.getPayload().length);
	}


	
	
	
	
	
	private void setPayload(DataInputStream in) throws IOException {
		// based on type set payload
		payload = new byte[this.length-1];
		in.readFully(payload, 0, this.length-1);
		
	}

	private void setType(DataInputStream in) throws IOException {
		byte type = in.readByte();
		setType((MessageType.getMessageType(type)));
	}

	public void setPacketLength(DataInputStream in) throws IOException {
		setLength(in.readInt());
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

}
