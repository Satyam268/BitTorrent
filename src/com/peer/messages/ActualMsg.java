package com.peer.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;

import org.apache.log4j.Logger;

import com.peer.utilities.CommonUtils;
import com.peer.utilities.MessageType;

public class ActualMsg extends Message {

	protected int length;
	protected MessageType type;
	protected byte[] payload = new byte[0];
	final static Logger logger = Logger.getLogger(ActualMsg.class);

	public ActualMsg() {
	}

	public ByteArrayOutputStream makePacket() throws IOException{
		ByteArrayOutputStream packetStream = new ByteArrayOutputStream();
		byte[] packetLength = CommonUtils.intToByteArray(this.getLength());
		packetStream.write(packetLength);
		packetStream.write(type.getValue());
		if(this.getLength()>1){
			packetStream.write(payload);
		}
		return packetStream;
	}

	/*public void read(InputStream inputStream){

	}*/

	public void write(OutputStream outputStream) throws IOException{
		ByteArrayOutputStream packet = makePacket();
		outputStream.write(packet.toByteArray());
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

	public String toString() {
		return "Message Details -- length: " + length + " type: " + type + " payload: "
				+ ((this.getType() == MessageType.PIECE) ? "" : (new String(payload)));
	}
}
