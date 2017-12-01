package com.peer;

//Reference : dalton/P2P-Project - Link - https://github.com/dalton/P2P-Project


import java.util.BitSet;

import org.apache.log4j.Logger;

import com.peer.utilities.CommonUtils;

public class RequestedPieces {

	private final BitSet reqPieces;
	private final long timeOut;
	final static Logger logger = Logger.getLogger(RequestedPieces.class);

	public synchronized int getPartToRequest(BitSet requestabableParts) {
		requestabableParts.andNot(reqPieces);

		if (!requestabableParts.isEmpty()) {
			final int partId = CommonUtils.pickRandomSetIndexFromBitSet(requestabableParts);
			reqPieces.set(partId);

			new java.util.Timer().schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					synchronized (reqPieces) {
						reqPieces.clear(partId);
						logger.debug("clearing requested parts for part " + partId);
					}
				}
			}, timeOut);
			return partId;
		}
		return -1;
	}

	public RequestedPieces(int nParts, long unchokingInterval) {
		reqPieces = new BitSet(nParts);
		timeOut = unchokingInterval * 2;
	}
}
