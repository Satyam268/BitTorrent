package com.peer.messages.types;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.peer.messages.ActualMsg;
import com.peer.utilities.CommonUtils;

public class BitField extends ActualMsg {
	BitSet pieceField;
	public BitField() {
		
	}
	public BitSet getPieceField() {
		return pieceField;
	}
	public void setPieceField(BitSet pieceField) {
		this.pieceField = pieceField;
	}
	
	public void setPieceFieldIndex(int index) {
		this.pieceField.set(index);
	}
	
	public void unSetPieceFieldIndex(int index) {
		this.pieceField.set(index, false);
	}
	
	
	
	public List<Integer> findSetBitIndexes(BitSet bitSet){
		List<Integer> setBits = new ArrayList<>();
		for(int i=0;i<bitSet.length();i++) {
			if(bitSet.get(i)) setBits.add(i);
		}
		return setBits;
	}
	public List<Integer> getInterestedPieceNumbers(BitSet bitSet){
		return findSetBitIndexes(CommonUtils.getRequiredPieces(pieceField, bitSet));
	}
	
//	public boolean hasAnyThingInteresting(BitSet bitSet) {
//		BitSet requiredPieces = CommonUtils.getRequiredPieces(pieceField, bitSet);
//		for(int i=0;i<requiredPieces.length();i++) {
//			if(requiredPieces.get(i)) return true;
//		}
//		return false;
//	}
	
	public void write(DataOutputStream out) throws IOException {
		
	}	
	
}
