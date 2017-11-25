package com.peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.Socket;
import java.util.BitSet;

public class PeerInfo {

	boolean choked;
	int peerId;
	int listeningPort;
    int hasFile;
    Socket clientSocket;
    String hostName;
    boolean handShaked=false;
    boolean interested=false;
    AtomicInteger bytesDownloaded = new AtomicInteger(0);
    DataInputStream socketReader;
    DataOutputStream socketWriter;
    int requestedPieceIndex=-1; // after every p time && whenever making a request and whenever piece
    // comes reset it to -1
	
    // records the pieces i have/don't have
	private BitSet bitfield = null;

	public PeerInfo(String line){
        String[] metaInfo = line.split(" ");
        if(metaInfo.length!=4) {
        }

        peerId = Integer.parseInt(metaInfo[0]);
        hostName = metaInfo[1];
        listeningPort = Integer.parseInt(metaInfo[2]);
        hasFile = Integer.parseInt(metaInfo[3]);

        //handle this AFTER PIECES
        setBitfield(new BitSet());


        if(hasFile==1) {
        	bitfield.set(0, bitfield.size(), true);
        }
        else {
        	bitfield.set(0, bitfield.size(), false);
        }
    }

	public PeerInfo(int peerId) {
        this.peerId = peerId;
    }

    public int getListeningPort() {
		return listeningPort;
	}


    @Override
    public String toString() {
        return peerId+ " "+hostName+" "+listeningPort+" "+hasFile;
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
		return bitfield;
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

	public DataInputStream getSocketReader() {
		return socketReader;
	}

	public void setSocketReader(DataInputStream socketReader) {
		this.socketReader = socketReader;
	}

	public DataOutputStream getSocketWriter() {
		return socketWriter;
	}

	public void setSocketWriter(DataOutputStream socketWriter) {
		this.socketWriter = socketWriter;
	}

	public int getRequestedPieceIndex() {
		return requestedPieceIndex;
	}

	public void setRequestedPieceIndex(int requestedPieceIndex) {
		this.requestedPieceIndex = requestedPieceIndex;
	}


}