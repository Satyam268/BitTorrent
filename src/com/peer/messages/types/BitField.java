package com.peer.messages.types;
import com.peer.messages.ActualMsg;
import com.peer.utilities.MessageType;

public class BitField extends ActualMsg {
	public BitField() {
		setType(MessageType.BITFIELD);
	}
}
