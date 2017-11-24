package com.peer.utilities;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class CommonUtils {

	public static byte[] intToByteArray(int num){
		return ByteBuffer.allocate(4).putInt(num).array();
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
