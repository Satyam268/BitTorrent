package com.peer;

public interface FileHandlerListner {
	   public void fileCompleted();
	   public void pieceArrived(int partIdx);
}