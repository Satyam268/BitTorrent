package com.peer;

import java.net.Socket;

public class PeerInfo {
	Peer peer;
	boolean choked;
	int peerId;
    public int getPeerId() {
		return peerId;
	}
	String hostName;
    public String getHostName() {
		return hostName;
	}
	int listeningPort;
    int hasFile;
    Socket clientSocket;
    
    public Socket getClientSocket() {
		return clientSocket;
	}

	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public PeerInfo(String line){
        String[] metaInfo = line.split(" ");
        if(metaInfo.length!=4) {
        }
        peerId = Integer.parseInt(metaInfo[0]);
        hostName = metaInfo[1];
        listeningPort = Integer.parseInt(metaInfo[2]);
        hasFile = Integer.parseInt(metaInfo[3]);
    }

    public int getListeningPort() {
		return listeningPort;
	}

	public PeerInfo(int peerId) {
        this.peerId = peerId;
    }
    @Override
    public String toString() {
        return peerId+ " "+hostName+" "+listeningPort+" "+hasFile;
    }
	public PeerInfo(Peer peer) {
		this.peer=peer;
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
	
	
}
