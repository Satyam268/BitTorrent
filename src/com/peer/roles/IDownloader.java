package com.peer.roles;

public interface IDownloader {

	void markPeiceReceived(int peiceIndex);

	void measureDownloadSpeed();

	void mergePieces();
	
	void writePieceToFile();

}
