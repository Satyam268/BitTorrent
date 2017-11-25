package com.peer.utilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class CommonUtils {

	static int numberOfPreferredNeighbors;
	static int unchokingInterval;
	static int optimisticUnchokingInterval;
	static String fileName;
	static int fileSize;
	static int pieceSize;	
	
	public static byte[] intToByteArray(int num) {
		return ByteBuffer.allocate(4).putInt(num).array();
	}

	public static int byteArrayToInt(byte[] numArray) {
		if(numArray.length>4) {
			throw new NumberFormatException(" Byte array not less than size of int");
		}
		return ByteBuffer.wrap(numArray).order(ByteOrder.BIG_ENDIAN).getInt();
	}

	public static BitSet getRequiredPieces(BitSet pieceField, BitSet bitSet) {
		BitSet ans = pieceField;
		ans.flip(0, ans.length());
		ans.and(bitSet);
		return ans;
	}

	public static boolean hasAnyThingInteresting(BitSet pieceField, BitSet bitSet) {
		BitSet requiredPieces = getRequiredPieces(pieceField, bitSet);

		for (int i = 0; i < requiredPieces.length(); i++) {
			if (requiredPieces.get(i))
				return true;
		}
		return false;
	}

	public static void setBitFieldAtIndex(BitSet bitSet, int index) {
		bitSet.set(index);
	}

	public static void UnsetBitFieldAtIndex(BitSet bitSet, int index) {
		bitSet.set(index, false);
	}

	public static List<Integer> findSetBitIndexes(BitSet bitSet){
		List<Integer> setBits = new ArrayList<>();
		for(int i=0;i<bitSet.length();i++) {
			if(bitSet.get(i)) setBits.add(i);
		}
		return setBits;
	}

	public List<Integer> getInterestedPieceNumbers(BitSet pieceField, BitSet bitSet){
		return findSetBitIndexes(CommonUtils.getRequiredPieces(pieceField, bitSet));
	}

	public static int pickRandomSetIndexFromBitSet(BitSet bitset) {
		if (bitset.isEmpty()) {
			throw new RuntimeException("The bitset is empty, cannot find a set element");
		}
		// Generate list of set elements in the format that follows: { 2, 4, 5,
		// ...}
		String set = bitset.toString();
		// Separate the elements, and pick one randomly
		String[] indexes = set.substring(1, set.length() - 1).split(",");
		return Integer.parseInt(indexes[(int) (Math.random() * (indexes.length - 1))].trim());
	}

	public static int getNumberOfPreferredNeighbors() {
		return numberOfPreferredNeighbors;
	}

	public static void setNumberOfPreferredNeighbors(int numberOfPreferredNeighbors) {
		CommonUtils.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
	}

	public static int getUnchokingInterval() {
		return unchokingInterval;
	}

	public static void setUnchokingInterval(int unchokingInterval) {
		CommonUtils.unchokingInterval = 10000;
	}

	public static int getOptimisticUnchokingInterval() {
		return optimisticUnchokingInterval;
	}

	public static void setOptimisticUnchokingInterval(int optimisticUnchokingInterval) {
		CommonUtils.optimisticUnchokingInterval = optimisticUnchokingInterval*1000;
	}

	public static String getFileName() {
		return fileName;
	}

	public static void setFileName(String fileName) {
		CommonUtils.fileName = fileName;
	}

	public static int getFileSize() {
		return fileSize;
	}

	public static void setFileSize(int fileSize) {
		CommonUtils.fileSize = fileSize;
	}

	public static int getPieceSize() {
		return pieceSize;
	}

	public static void setPieceSize(int pieceSize) {
		CommonUtils.pieceSize = pieceSize;
	}

	public static int getNumberOfPieces() {
		return fileSize/pieceSize;
	}

}
