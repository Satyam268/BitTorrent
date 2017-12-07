package com.peer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.log4j.PropertyConfigurator;

import com.peer.utilities.Constants;

public class PeerProcess {

	private ArrayList<Integer> activePeerIds;
	private Map<Integer, PeerInfo> neighborMap;
	private PeerProperties peerProperties;
	private Peer peer;

	void readCommonCFGFile() {
		try (Stream<String> stream = Files.lines(Paths.get(Constants.commonConfigFile))) {
			Iterator<String> it = stream.iterator();
			int numberOfPreferredNeighbors = Integer.parseInt(it.next().split(" ")[1]);
			int unchokingInterval = Integer.parseInt(it.next().split(" ")[1]);
			int optimisticUnchokingInterval = Integer.parseInt(it.next().split(" ")[1]);
			String fileName = it.next().split(" ")[1];
			int fileSize = Integer.parseInt(it.next().split(" ")[1]);
			int pieceSize = Integer.parseInt(it.next().split(" ")[1]);
			peerProperties = new PeerProperties(fileName, fileSize, optimisticUnchokingInterval,
					numberOfPreferredNeighbors, pieceSize, unchokingInterval);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void readPeerInfoFile(int peerId) {
		neighborMap = new ConcurrentHashMap<Integer, PeerInfo>();
		activePeerIds = new ArrayList<>();
		try (Stream<String> stream = Files.lines(Paths.get(Constants.peerInfoFile))) {
			Iterator<String> it = stream.iterator();
			boolean myInfoProcessed = false;
			while (it.hasNext()) {
				ConfigFileParams configFileParams = new ConfigFileParams(it.next());
				if (!myInfoProcessed) {
					if (configFileParams.getPeerId() != peerId)
						activePeerIds.add(configFileParams.getPeerId());
					else
						myInfoProcessed = true;
				}
				neighborMap.put(configFileParams.getPeerId(),
						new PeerInfo(configFileParams, peerProperties.getNumberOfPieces()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteFiles(String dir) {
		Path dirPath = Paths.get(dir);
		try {
			Files.walk(dirPath).map(Path::toFile).sorted(Comparator.comparing(File::isDirectory)).forEach(File::delete);
			// System.out.println(x);
		} catch (IOException e) {
			System.out.println("exception " + e);
		}
	}

	public static void main(String[] args) {
		int peerId = Integer.parseInt("1004");
		System.setProperty("file.name", "log_peer_" + peerId + ".log");
		PropertyConfigurator.configure(Constants.log4jConfPath);
		deleteFiles(Paths.get("com").toString());
		deleteFiles(Paths.get("pieceStore").toString());
		PeerProcess process = new PeerProcess();
		process.readCommonCFGFile();
		process.readPeerInfoFile(peerId);
		process.setupPeer(peerId);
	}

	private void setupPeer(int peerId) {
		PeerInfo myInfo = neighborMap.remove(peerId);
		this.peer = new Peer(peerId, peerProperties, neighborMap, myInfo);
		peer.connectToPeers(activePeerIds);
		peer.startPeerHandler();
		peer.startServer();
	}

}
