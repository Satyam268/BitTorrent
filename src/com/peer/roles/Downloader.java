package com.peer.roles;

public interface Downloader {

	void markPeiceReceived(int peiceIndex);

	void measureDownloadSpeed();

	void mergePieces();
	
	void writePieceToFile();

}
