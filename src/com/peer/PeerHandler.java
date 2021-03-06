package com.peer;
//Reference : dalton/P2P-Project - Link - https://github.com/dalton/P2P-Project
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.peer.messages.Message;
import com.peer.messages.types.Choke;
import com.peer.messages.types.Unchoke;
import com.peer.utilities.Constants;
import com.peer.utilities.MessageType;

public class PeerHandler implements Runnable {

	final static Logger logger = Logger.getLogger(PeerHandler.class);
	Map<Integer, PeerInfo> peerMap;
	Collection<PeerInfo> kPreferredNeighbors = new HashSet<PeerInfo>();
	PeerProperties peerProperties;
	int peerID;

	class OptimisticUnchoker extends Thread {
		private final int numberOfOptimisticallyUnchokedNeighbors;
		private final int optimisticUnchokingInterval;

		private final List<PeerInfo> chokedNeighbors = new ArrayList<>();

		final Collection<PeerInfo> optimisticallyUnchokedPeers = Collections
				.newSetFromMap(new ConcurrentHashMap<PeerInfo, Boolean>());

		OptimisticUnchoker(PeerProperties properties) {
			super("OptimisticUnchoker");
			numberOfOptimisticallyUnchokedNeighbors = Constants.oun_count;
			optimisticUnchokingInterval = properties.getOptimisticUnchokingInterval();
		}

		synchronized void setChokedNeighbors(Collection<PeerInfo> chokedNeighbors) {
			this.chokedNeighbors.clear();
			this.chokedNeighbors.addAll(chokedNeighbors);
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(optimisticUnchokingInterval);
				} catch (InterruptedException ex) {
				}

				synchronized (this) { 
						Collections.shuffle(chokedNeighbors);
						optimisticallyUnchokedPeers.clear();
						optimisticallyUnchokedPeers.addAll(chokedNeighbors.subList(0,
								Math.min(numberOfOptimisticallyUnchokedNeighbors, chokedNeighbors.size())));
				}

				optimisticallyUnchokedPeers.forEach(peerInfo -> {
					try {
						sendUnchoke(peerInfo.getSocketWriter());
						logger.info("Peer [" + peerID + "] has the optimistically unchoked neighbour [" + peerInfo.peerId
								+ "]");
					} catch (Exception e) {
						logger.debug("Unable to choke peer: " + peerInfo.getPeerId() + " " + e);
					}
				});

			}
		}
	}

	private final OptimisticUnchoker optUnchoker;

	public PeerHandler(int peerID, Map<Integer, PeerInfo> peerMap2, PeerProperties peerProperties) {
		this.peerMap = peerMap2;
		this.peerProperties = peerProperties;
		this.peerID = peerID;
		this.optUnchoker = new OptimisticUnchoker(peerProperties);
	}

	public void sendUnchoke(ObjectOutputStream socketWriter) throws ClassNotFoundException, IOException {
		try {
			// is closed thn dont send
			Unchoke unchokeMessage = (Unchoke) Message.getInstance(MessageType.UNCHOKE);
			unchokeMessage.write(socketWriter);
		} catch (Exception e) {
			logger.debug("Socket connection explicitly closed| can't send unchoke from oum " + e);
			// possible hack place
		}
	}

	public void sendChoke(ObjectOutputStream socketWriter) throws ClassNotFoundException, IOException {
		try {
			Choke chokeMessage = (Choke) Message.getInstance(MessageType.CHOKE);
			chokeMessage.write(socketWriter);
		} catch (Exception e) {
			logger.debug("Socket connection explicitly closed| can't send choke from oum " + e);
		}
	}

	@Override
	public void run() {
		optUnchoker.start();

		while (true) {
			try {
				Thread.sleep(peerProperties.getUnchokingInterval());
			} catch (InterruptedException ex) {
				logger.debug("Thread interrupted: "+ex);
			}

			// Get Peers by preference or randomly
			List<PeerInfo> interestedPeers = getInterestedPeers();
			if (peerProperties.randomlySelectPreferredNeighbors.get()) {
				logger.debug("Selecting preferred peers randomly");
				Collections.shuffle(interestedPeers);
			} 
			else {
				Collections.sort(interestedPeers, new Comparator<PeerInfo>() {
					@Override
					public int compare(PeerInfo p1, PeerInfo p2) {
						return (p2.bytesDownloaded.get() - p1.bytesDownloaded.get());
					}
				});
			}

			Collection<PeerInfo> optUnchokablePeers = null;

			Collection<Integer> chokedPeersIDs = new HashSet<>();
			Collection<Integer> preferredNeighborsIDs = new HashSet<>();
			Map<Integer, Long> downloadedBytes = new HashMap<>();

			synchronized (this) {
				// Reset downloaded bytes
				for (PeerInfo peerInfo : peerMap.values()) {
					downloadedBytes.put(peerInfo.getPeerId(), peerInfo.bytesDownloaded.longValue());
					peerInfo.bytesDownloaded.set(0);
				}

				// select preferred by ranking ..here based on download rates
				// and
				// randomly in case of conflict or if sender has complete file
				kPreferredNeighbors.clear();
				kPreferredNeighbors.addAll(interestedPeers.subList(0,
						Math.min(peerProperties.getNumberOfPreferredNeighbors(), interestedPeers.size())));

				if (kPreferredNeighbors.size() > 0) {
					logger.info("Peer [" + peerID + "] has the preferred neighbours " + kPreferredNeighbors);
				}

				// From all peers remove K-preferred
				
				Collection<PeerInfo> chokedPeers = new LinkedList<>(getAllConnectedNeighbours());
				chokedPeers.removeAll(kPreferredNeighbors);
				chokedPeersIDs.addAll(getPeerIds(chokedPeers));

				// List of Choked peers which are optimistically unchokable
				if (peerProperties.getNumberOfPreferredNeighbors() >= interestedPeers.size()) {
					optUnchokablePeers = new ArrayList<>();
				} else {
					// list of peers which are not going to be in the list of k
					// preferred
					// are considered for Optimistic Unchoking
					optUnchokablePeers = interestedPeers.subList(peerProperties.getNumberOfPreferredNeighbors(),
							interestedPeers.size());
				}

				preferredNeighborsIDs.addAll(getPeerIds(kPreferredNeighbors));
			}



			// Send choke and unchoke messages to respective peers !
			chokedPeersIDs.forEach(id -> {
				try {

					sendChoke(peerMap.get(id).getSocketWriter());
				} catch (Exception e) {
					logger.debug(e);
				}
			});

			preferredNeighborsIDs.forEach(id -> {
				try {
						sendUnchoke(peerMap.get(id).getSocketWriter());
				} catch (Exception e) {
					logger.debug(e);
				}
			});

			// Refresh the set of peers for choosing unchokable peers.
			if (optUnchokablePeers != null) {
				optUnchoker.setChokedNeighbors(optUnchokablePeers);
			}
		}

	}

	private List<PeerInfo> getAllConnectedNeighbours() {
		List<PeerInfo> connectedNieghbors = new ArrayList<>();
		peerMap.values().forEach(peerInfo -> {
			if (peerInfo.getClientSocket() != null) {
				connectedNieghbors.add(peerInfo);
			}
		});
		return connectedNieghbors;
	}

	private Collection<Integer> getPeerIds(Collection<PeerInfo> chokedPeers) {
		Collection<Integer> peerIds = new ArrayList<>();
		chokedPeers.forEach(x -> peerIds.add(x.getPeerId()));
		return peerIds;
	}

	private List<PeerInfo> getInterestedPeers() {
		List<PeerInfo> interestedPeers = new ArrayList<>();
		for (PeerInfo peerInfo : peerMap.values()) {
			if (peerInfo.isInterested()) {
				interestedPeers.add(peerInfo);
			}
		}
		return interestedPeers;
	}
}
