package com.peer.utilities;

import java.nio.ByteBuffer;

public class CommonUtils {

	public static byte[] intToByteArray(int num){
		return ByteBuffer.allocate(4).putInt(num).array();
	}

}
