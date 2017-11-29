package com.peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

public class PeerInfo {

	boolean choked;
	int peerId;
	int listeningPort;
	int hasFile;

	public int getHasFile() {
		return hasFile;
	}

	public void setHasFile(int hasFile) {
		this.hasFile = hasFile;
	}

	Socket clientSocket;
	String hostName;

	boolean handShaked = false;
	boolean interested = false;

	AtomicInteger bytesDownloaded = new AtomicInteger(0);
	ObjectInputStream socketReader;
	ObjectOutputStream socketWriter;
	AtomicInteger requestedPieceIndex = new AtomicInteger(-1); // after every p time && whenever making a request and
																// whenever piece

	// records the pieces i have/don't have
	private BitSet bitfield = null;

	public PeerInfo(ConfigFileParams fileParams, int numberOfPieces) {
		peerId = fileParams.getPeerId();
		hostName = fileParams.getHostName();
		listeningPort = fileParams.getListeningPort();
		hasFile = fileParams.getHasFile();
		BitSet b = new BitSet();
		b.set(0, numberOfPieces, false);
		setBitfield(b);
		if (hasFile == 1) {
			bitfield.set(0, numberOfPieces, true);
		}
	}

	public PeerInfo(int peerId) {
		this.peerId = peerId;
	}

	public int getListeningPort() {
		return listeningPort;
	}

	// overridden for log generation purpose
	@Override
	public String toString() {
		return "PerInfo: " + "peerId: " + peerId + "" + " hasFile: " + hasFile + " bitField: " + bitfield;// + "
																											// "+hostName+"
																											// "+listeningPort+"
																											// "+hasFile;
	}

	public boolean isChoked() {
		return choked;
	}

	public void choke() {
		this.choked = true;
	}

	public void unChoke() {
		this.choked = false;
	}

	public BitSet getBitfield() {
		return (BitSet) bitfield.clone();
	}

	public void setBitfield(BitSet bitfield) {
		this.bitfield = bitfield;
	}

	public void setBitfieldAtIndex(int index) {
		this.bitfield.set(index);
	}

	public int getPeerId() {
		return peerId;
	}

	public String getHostName() {
		return hostName;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public boolean isHandShaked() {
		return handShaked;
	}

	public void setHandShaked(boolean handShaked) {
		this.handShaked = handShaked;
	}

	public boolean isInterested() {
		return interested;
	}

	public void setInterested(boolean interested) {
		this.interested = interested;
	}

	public ObjectInputStream getSocketReader() {
		return socketReader;
	}

	public void setSocketReader(ObjectInputStream socketReader) {
		this.socketReader = socketReader;
	}

	public ObjectOutputStream getSocketWriter() {
		return socketWriter;
	}

	public void setSocketWriter(ObjectOutputStream socketWriter) {
		this.socketWriter = socketWriter;
	}

	public int getRequestedPieceIndex() {
		return requestedPieceIndex.get();
	}

	public void setRequestedPieceIndex(int requestedPieceIndex) {
		this.requestedPieceIndex.set(requestedPieceIndex);
	}

}