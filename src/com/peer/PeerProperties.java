package com.peer;

import java.util.concurrent.atomic.AtomicBoolean;

public class PeerProperties {

	int numberOfPreferredNeighbors;
	int unchokingInterval;
	int optimisticUnchokingInterval;
	String fileName;
	int fileSize;
	int pieceSize;
	int hasFile;
	public AtomicBoolean randomlySelectPreferredNeighbors = new AtomicBoolean(false); // Whether a peer selects neighbors randomly or not!!!

	public int getHasFile() {
		return hasFile;
	}

	public void setHasFile(int hasFile) {
		this.hasFile = hasFile;
	}

	public PeerProperties(String fileName2, int fileSize2, int optimisticUnchokingInterval2,
			int numberOfPreferredNeighbors2, int pieceSize2, int unchokingInterval2) {
		numberOfPreferredNeighbors = numberOfPreferredNeighbors2;
		unchokingInterval = unchokingInterval2 * 1000;
		optimisticUnchokingInterval = optimisticUnchokingInterval2 * 1000;
		fileSize = fileSize2;
		fileName = fileName2;
		pieceSize = pieceSize2;
	}

	public int getNumberOfPreferredNeighbors() {
		return numberOfPreferredNeighbors;
	}

	public int getUnchokingInterval() {
		return unchokingInterval;
	}

	public int getOptimisticUnchokingInterval() {
		return optimisticUnchokingInterval;
	}

	public String getFileName() {
		return fileName;
	}

	public int getFileSize() {
		return fileSize;
	}

	public int getPieceSize() {
		return pieceSize;
	}

	public int getNumberOfPieces() {
		return (int) Math.ceil((1.0 * fileSize) / pieceSize);
	}

}
