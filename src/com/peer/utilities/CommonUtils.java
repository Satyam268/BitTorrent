package com.peer.utilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

public class CommonUtils {

	public static byte[] intToByteArray(int num){
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
		
		for(int i=0;i<requiredPieces.length();i++) {
			if(requiredPieces.get(i)) return true;
		}
		return false;
	}
	
}
