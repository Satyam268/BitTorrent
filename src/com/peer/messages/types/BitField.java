package com.peer.messages.types;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.peer.messages.ActualMsg;

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
	
	public BitSet compareBitFields(BitSet bitSet) {
		BitSet ans = pieceField;
		ans.flip(0, ans.length());
		ans.and(bitSet);
		return ans;
	}
	
	public List<Integer> findSetBitIndexes(BitSet bitSet){
		List<Integer> setBits = new ArrayList<>();
		for(int i=0;i<bitSet.length();i++) {
			if(bitSet.get(i)) setBits.add(i);
		}
		return setBits;
	}
	public List<Integer> getInterestedPieceNumbers(BitSet bitSet){
		return findSetBitIndexes(this.compareBitFields(bitSet));
	}
	
	public boolean hasAnyThingInteresting(BitSet bitSet) {
		BitSet requiredPieces = this.compareBitFields(bitSet);
		for(int i=0;i<requiredPieces.length();i++) {
			if(requiredPieces.get(i)) return true;
		}
		return false;
	}
	
}
