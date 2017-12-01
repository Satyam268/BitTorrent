package com.peer;

import java.util.BitSet;

import org.apache.log4j.Logger;

import com.peer.utilities.CommonUtils;

public class RequestedPieces {

		private final BitSet _requestedParts;
		private final long _timeoutInMillis;
		final static Logger logger = Logger.getLogger(RequestedPieces.class);

		public RequestedPieces(int nParts, long unchokingInterval) {
			_requestedParts = new BitSet(nParts);
			_timeoutInMillis = unchokingInterval * 2;//why?
		}

		public synchronized int getPartToRequest(BitSet requestabableParts) {
			requestabableParts.andNot(_requestedParts);
			
			if (!requestabableParts.isEmpty()) {
				final int partId = CommonUtils.pickRandomSetIndexFromBitSet(requestabableParts);
				_requestedParts.set(partId);

				// Make the part requestable again in _timeoutInMillis
				new java.util.Timer().schedule(new java.util.TimerTask() {
					@Override
					public void run() {
						synchronized (_requestedParts) {
							_requestedParts.clear(partId);
							logger.debug("clearing requested parts for pert " + partId);
						}
					}
				}, _timeoutInMillis);
				return partId;
			}
			return -1;
		}
}
