package com.peer.roles;

public interface Uploader {

	byte doHandshake();

	void selectKPeers();//do we care if less than k peers?

	void selectOptimisticUnchokedNeighbour();

}
