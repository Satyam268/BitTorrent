package com.peer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.peer.messages.Message;
import com.peer.messages.types.Choke;
import com.peer.messages.types.Unchoke;
import com.peer.utilities.MessageType;
import com.peer.utilities.PeerProperties;

public class PeerHandler implements Runnable {

	final static Logger logger = Logger.getLogger(PeerHandler.class);
	Map<Integer, PeerInfo> peerMap;
	Collection<PeerInfo> kPreferredNeighbors = new HashSet<PeerInfo>();
	AtomicBoolean randomlySelectPreferredNeighbors = new AtomicBoolean(false);
	PeerProperties peerProperties;
	int peerID;

	public PeerHandler(int peerID, Map<Integer, PeerInfo> peerMap2, PeerProperties peerProperties) {
		this.peerMap = peerMap2;
		this.peerProperties = peerProperties;
		this.peerID = peerID;
	}

	@Override
	public void run() {
		// optUnchoker.start();

		while (true) {
			try {
				System.out.println("Time unchoking "+peerProperties.getUnchokingInterval());
				Thread.sleep(peerProperties.getUnchokingInterval()); // Sleep for k time !!!
			} catch (InterruptedException ex) {
			}

			// 1) GET INTERESTED PEERS AND SORT THEM BY PREFERENCE

			List<PeerInfo> interestedPeers = getInterestedPeers();
			if (randomlySelectPreferredNeighbors.get()) {
				logger.debug("Selecting preferred peers randomly");
				Collections.shuffle(interestedPeers);
			} else {
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

				// 2) SELECT THE PREFERRED PEERS BY SELECTING THE HIGHEST RANKED

				kPreferredNeighbors.clear();
				kPreferredNeighbors.addAll(interestedPeers.subList(0,
						Math.min(peerProperties.getNumberOfPreferredNeighbors(), interestedPeers.size())));

				if (kPreferredNeighbors.size() > 0) {
					logger.debug("Peer ["+peerID+"] has the preferred neighbours "+kPreferredNeighbors);
				}

				// 3) SELECT ALLE THE INTERESTED AND UNINTERESTED PEERS, REMOVE THE PREFERRED.
				// THE RESULTS ARE THE CHOKED PEERS

				Collection<PeerInfo> chokedPeers = new LinkedList<>(getAllConnectedNeighbours());
				chokedPeers.removeAll(kPreferredNeighbors);
				chokedPeersIDs.addAll(getPeerIds(chokedPeers));

				// 4) SELECT ALLE THE INTERESTED PEERS, REMOVE THE PREFERRED. THE RESULTS ARE
				// THE CHOKED PEERS THAT ARE "OPTIMISTICALLY-UNCHOKABLE"
				if (peerProperties.getNumberOfPreferredNeighbors() >= interestedPeers.size()) {
					optUnchokablePeers = new ArrayList<>();
				} else {
					// list of peers which are not going to be in the list of k preferred
					optUnchokablePeers = interestedPeers.subList(peerProperties.getNumberOfPreferredNeighbors(),
							interestedPeers.size());
				}

				preferredNeighborsIDs.addAll(getPeerIds(kPreferredNeighbors));
			}

			// debug
			logger.info("STATE: INTERESTED: " + interestedPeers);
			logger.info("STATE: UNCHOKED (" + peerProperties.getNumberOfPreferredNeighbors() + "): " + preferredNeighborsIDs);
			logger.info("STATE: CHOKED:" + chokedPeersIDs);

			for (Entry<Integer, Long> entry : downloadedBytes.entrySet()) {
				String PREFERRED = preferredNeighborsIDs.contains(entry.getKey()) ? " *" : "";
				logger.debug("BYTES DOWNLOADED FROM  PEER " + entry.getKey() + ": " + entry.getValue()
						+ " (INTERESTED PEERS: " + interestedPeers.size() + ": " + interestedPeers + ")\t" + PREFERRED);
			}

			// 5) NOTIFY PROCESS, IT WILL TAKE CARE OF SENDING CHOKE AND UNCHOKE MESSAGES

			// for (PeerManagerListener listener : _listeners) {
			// listener.chockedPeers(chokedPeersIDs);
			// listener.unchockedPeers(preferredNeighborsIDs);
			// }
			chokedPeersIDs.forEach(id -> {
				try {
					Choke chokeMessage = (Choke) Message.getInstance(MessageType.CHOKE);
					chokeMessage.write(peerMap.get(id).getSocketWriter());
				} catch (Exception e) {
					logger.warn(e);
				}
			});

			preferredNeighborsIDs.forEach(id -> {
				try {
					Unchoke unchokeMessage = (Unchoke) Message.getInstance(MessageType.UNCHOKE);
					unchokeMessage.write(peerMap.get(id).getSocketWriter());
				} catch (Exception e) {
					logger.warn(e);
				}
			});

		}

		// 6) NOTIFY THE OPTIMISTICALLY UNCHOKER THREAD WITH THE NEW SET OF UNCHOKABLE
		// PEERS

		// if (optUnchokablePeers != null) {
		// _optUnchoker.setChokedNeighbors(optUnchokablePeers);
		// }
	}

	private List<PeerInfo> getAllConnectedNeighbours() {
		List<PeerInfo> connectedNieghbors=new ArrayList<>();
		peerMap.values().forEach(peerInfo ->{
			if(peerInfo.getClientSocket()!=null){
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
