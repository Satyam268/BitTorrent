package com.peer.messages;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import com.peer.utilities.CommonUtils;

public class HandshakeMsg extends Message {

	private final String handshakeHeader = "P2PFILESHARINGPROJ";
	private final byte[] zeroBits = new byte[10];
	byte[] peerId = new byte[4];

	public HandshakeMsg(int peerID) {
		peerId = CommonUtils.intToByteArray(peerID);
	}

	public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
		HandshakeMsg message = (HandshakeMsg) in.readObject();
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
		}

		// if (in.read(protocolId, 0, handshakeHeader.length()) <
		// handshakeHeader.length()) {
		// throw new ProtocolException(
		// "protocol id is " + Arrays.toString(protocolId) + " instead of " +
		// handshakeHeader);
		// }
		//
		// if (!handshakeHeader.equals(new String(protocolId, "US-ASCII"))) {
		// throw new ProtocolException(
		// "protocol id is " + Arrays.toString(protocolId) + " instead of " +
		// handshakeHeader);
		// }
		//
		// if (in.read(zeroBits, 0, zeroBits.length) < zeroBits.length) {
		// throw new ProtocolException("zero bit bytes read are less than " +
		// zeroBits.length);
		// }
		//
		// if (in.read(peerId, 0, peerId.length) < peerId.length) {
		// throw new ProtocolException("peer id bytes read are less than " +
		// peerId.length);
		// }
	}

	public String getHandshakeHeader() {
		return handshakeHeader;
	}

	public void write(ObjectOutputStream out) throws IOException {

		if (peerId.length > handshakeHeader.length()) {
			throw new IOException("protocol id length is " + peerId.length + " instead of " + handshakeHeader.length());
		}
		out.writeObject(this);
		/*
		 * out.write(handshakeHeader.getBytes(), 0, handshakeHeader.length());
		 * out.write(zeroBits, 0, zeroBits.length); out.write(myPeerID, 0,
		 * myPeerID.length);
		 */
	}

	public static boolean validateHeader(byte[] handShakeData) {
		// TODO Auto-generated method stub
		return true;
	}

	public int getPeerID() {
		// TODO Auto-generated method stub
		return ByteBuffer.wrap(peerId).order(ByteOrder.BIG_ENDIAN).getInt();
	}

}
