package com.peer.roles;

public interface IUploader {

	
	void selectKPeers();//do we care if less than k peers?

	void selectOptimisticUnchokedNeighbour();

}
