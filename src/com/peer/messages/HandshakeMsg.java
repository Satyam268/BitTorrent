package com.peer.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import com.peer.utilities.CommonUtils;

public class HandshakeMsg extends Message {

	private final static String handshakeHeader = "P2PFILESHARINGPROJ";
	private final byte[] zeroBits = new byte[10];
	byte[] peerId = new byte[4];

	byte[] myPeerID;

	public HandshakeMsg(int peerID) {
		myPeerID = CommonUtils.intToByteArray(peerID);
	}

	public void read(DataInputStream in) throws IOException {
		byte[] protocolId = new byte[handshakeHeader.length()];

		if (in.read(protocolId, 0, handshakeHeader.length()) < handshakeHeader.length()) {
			throw new ProtocolException(
					"protocol id is " + Arrays.toString(protocolId) + " instead of " + handshakeHeader);
		}

		if (!handshakeHeader.equals(new String(protocolId, "US-ASCII"))) {
			throw new ProtocolException(
					"protocol id is " + Arrays.toString(protocolId) + " instead of " + handshakeHeader);
		}

		if (in.read(zeroBits, 0, zeroBits.length) < zeroBits.length) {
			throw new ProtocolException("zero bit bytes read are less than " + zeroBits.length);
		}

		if (in.read(peerId, 0, peerId.length) < peerId.length) {
			throw new ProtocolException("peer id bytes read are less than " + peerId.length);
		}
	}

	public void write(DataOutputStream out) throws IOException {

		if (peerId.length > handshakeHeader.length()) {
			throw new IOException("protocol id length is " + peerId.length + " instead of " + handshakeHeader.length());
		}
		out.write(handshakeHeader.getBytes(), 0, handshakeHeader.length());
		out.write(zeroBits, 0, zeroBits.length);
		out.write(myPeerID, 0, myPeerID.length);
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
