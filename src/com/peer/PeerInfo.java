package com.peer;

public class PeerInfo {
	Peer peer;
	boolean choked;
	
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
