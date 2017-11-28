package com.peer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.peer.file.FileOperations;
import com.peer.utilities.PeerProperties;

public class PeerProcess {

	Map<Integer, PeerInfo> neighborMap;
	List<Integer> activePeerIds;
	int numberOfPreferredNeighbors;
	int unchokingInterval;
	int optimisticUnchokingInterval;
	String fileName;
	int fileSize;
	int pieceSize;
	Peer peer = null;
	List<ConfigFileParams> peerInfoFileParams;
	List<Peer> interestedNeighbors = new ArrayList<>();
	final static Logger logger = Logger.getLogger(PeerProcess.class);


	public PeerProcess(int peerId) {
		peer = new Peer(peerId);
		neighborMap = new ConcurrentHashMap<>();
		activePeerIds = new ArrayList<>();
	}

	void establishTCPConnection() {
		peer.connectToPeers(activePeerIds);
	}

	private void startServer() {
		peer.startPeerHandler();
		peer.startServer();
	}
	
	void readPeerInfoFile() {
		String fileName = "src/com/peer/configFiles/PeerInfo.cfg";
		PeerInfo myInfo;
		peerInfoFileParams = new ArrayList<>();
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			Iterator<String> it = stream.iterator();
			it.forEachRemaining(line -> peerInfoFileParams.add(new ConfigFileParams(line)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void readCommonCFGFile() {
		String _fileName = "src/com/peer/configFiles/Common.cfg";
		try (Stream<String> stream = Files.lines(Paths.get(_fileName))) {
			Iterator<String> it = stream.iterator();
			numberOfPreferredNeighbors = Integer.parseInt(it.next().split(" ")[1]);
			unchokingInterval = Integer.parseInt(it.next().split(" ")[1]);
			optimisticUnchokingInterval = Integer.parseInt(it.next().split(" ")[1]);
			fileName = it.next().split(" ")[1];
			fileSize = Integer.parseInt(it.next().split(" ")[1]);
			pieceSize = Integer.parseInt(it.next().split(" ")[1]);
			System.out.println("PeerProcess Config: " + numberOfPreferredNeighbors + " " + unchokingInterval + " "
					+ optimisticUnchokingInterval);

			/*if (myInfo.hasFile == 1) {
				try {
					File file = new File(fileName);
					splitFileIntoPieceFiles(file, pieceSize);
				} catch (Exception e) {
					logger.warn("Cannot split file: "+e);
				}
			}*/
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		int peerID = 1002;
		String log4jConfPath = "log4j.properties";
		System.setProperty("file.name", "log_peer_" + peerID + ".log");
		PropertyConfigurator.configure(log4jConfPath);
		PeerProcess me = new PeerProcess(peerID);
		me.readCommonCFGFile();
		me.readPeerInfoFile();
		me.setupPeerInformation();
		logger.info("Initial config files read\n");
		me.establishTCPConnection();
		logger.info("TCP connections to already connected peeers completed.\n");
		System.out.println(me.peer.properties.getUnchokingInterval());
		me.startServer();
	}

	private void setupPeerInformation() {
		setPeerProperties();
		setupOtherPeerInfo();
	}

	private void setupOtherPeerInfo() {
		boolean selfPeerIdNotRead = true;
		
		for(ConfigFileParams it:peerInfoFileParams) {
			PeerInfo peerInfo = new PeerInfo(it, this.peer.properties.getNumberOfPieces());
			neighborMap.put(peerInfo.getPeerId(), peerInfo);
			if (selfPeerIdNotRead && peerInfo.getPeerId() != peer.getPeerID()) {
				activePeerIds.add(peerInfo.getPeerId());
			} else {
				selfPeerIdNotRead = false;
			}
		}
		PeerInfo myInfo = neighborMap.remove(peer.getPeerID());
		peer.setPeerMap(neighborMap);
		peer.setMyInfo(myInfo);
	}

	public int getNumberOfPieces() {
		return peer.properties.getNumberOfPieces();
	}

	public void splitFileIntoPieceFiles(File file, int pieceSize) {
		FileOperations.processFileIntoPieceFiles(file, pieceSize);
	}

	private void setPeerProperties() {
		this.peer.setProperties(new PeerProperties(this.fileName, fileSize, optimisticUnchokingInterval,
				numberOfPreferredNeighbors, pieceSize, unchokingInterval));
	}

}
