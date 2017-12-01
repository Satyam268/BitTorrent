package com.peer.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import com.peer.utilities.CommonUtils;

public class HandshakeMsg extends Message {

	public String handshakeHeader = "P2PFILESHARINGPROJ";
	public byte[] zeroBits = new byte[10];
	public byte[] peerId = new byte[4];

	public HandshakeMsg(int peerID) {
		peerId = CommonUtils.intToByteArray(peerID);
	}

	public HandshakeMsg() {
		// TODO Auto-generated constructor stub
	}

	//public void read(InputStream in) throws IOException, ClassNotFoundException {
	/*	HandshakeMsg message = readIncomingMessage(in);
		this.peerId = message.peerId;
		byte[] protocolId = new byte[handshakeHeader.length()];

		if (message.getHandshakeHeader().length() < this.handshakeHeader.length()) {
			throw new ProtocolException(
					"protocol id is " + Arrays.toString(protocolId) + " instead of " + handshakeHeader);
		}

		if (!handshakeHeader.equals(new String(message.handshakeHeader.getBytes(), "US-ASCII"))) {
			throw new ProtocolException(
					"protocol id is " + Arrays.toString(protocolId) + " instead of " + handshakeHeader);
		}

		if (message.zeroBits.length < zeroBits.length) {
			throw new ProtocolException("zero bit bytes read are less than " + zeroBits.length);
		}

		if (message.peerId.length < peerId.length) {
			throw new ProtocolException("peer id bytes read are less than " + peerId.length);
		}*/

	//}

	public String getHandshakeHeader() {
		return handshakeHeader;
	}

	public void write(OutputStream out) throws IOException {
		if (peerId.length > handshakeHeader.length()) {
			throw new IOException("protocol id length is " + peerId.length + " instead of " + handshakeHeader.length());
		}
		out.write(makePacket().toByteArray());
	}

	public int getPeerID() {
		return ByteBuffer.wrap(peerId).order(ByteOrder.BIG_ENDIAN).getInt();
	}


	public ByteArrayOutputStream makePacket() throws IOException {
		ByteArrayOutputStream packetStream = new ByteArrayOutputStream();

		byte[] packetHeader = handshakeHeader.getBytes();

		packetStream.write(packetHeader);
		packetStream.write(zeroBits);
		packetStream.write(CommonUtils.intToByteArray(getPeerID()));
		return packetStream;
	}

}
