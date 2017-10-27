package com.peer;

import java.util.BitSet;

import com.peer.roles.Downloader;
import com.peer.roles.Uploader;

public class Peer implements Uploader,Downloader{

	private int peerID;
	//private List<PeerInfo> peerDetails;
	private int peices;

	//records the pieces i have, don't have
	private BitSet bitfield = null;

	//to do
	//currently processing list
	//hashset ? or Enum array sort?


	public Peer(){

	}

	private void calculatePeices(){

	}




	public int getPeerID() {
		return peerID;
	}

	public void setPeerID(int peerID) {
		this.peerID = peerID;
	}

	public int getPeices() {
		return peices;
	}

	public void setPeices(int peices) {
		this.peices = peices;
	}

	public BitSet getBitfield() {
		return bitfield;
	}

	public void setBitfield(BitSet bitfield) {
		this.bitfield = bitfield;
	}

	@Override
	public byte doHandshake() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void selectKPeers() {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectOptimisticUnchokedNeighbour() {
		// TODO Auto-generated method stub

	}

	@Override
	public void markPeiceReceived(int peiceIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void measureDownloadSpeed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void mergePieces() {
		// TODO Auto-generated method stub

	}




}
