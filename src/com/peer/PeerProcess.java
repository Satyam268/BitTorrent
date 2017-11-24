package com.peer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class PeerProcess {

	PeerInfo myInfo;
	Map<Integer,PeerInfo> neighborMap;
	List<Integer> activePeerIds;
	int numberOfPreferredNeighbors;
	int unchokingInterval;
	int optimisticUnchokingInterval;
	String fileName;
	int fileSize;
	int pieceSize;

	List<Peer> interestedNeighbors = new ArrayList<>();

	final static Logger logger = Logger.getLogger(PeerProcess.class);
	Peer peer = null;

	public PeerProcess(int peerId) {
		peer = new Peer(peerId);
		neighborMap = new HashMap<>();
		activePeerIds = new ArrayList<>();
	}


	void establishTCPConnection() {
		peer.connectToPeers(activePeerIds);
	}

	private void startServer() {
		peer.startServer();
	}


	void readPeerInfoFile() {
		String fileName = "src/com/peer/configFiles/PeerInfo.cfg";
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			Iterator<String> it= stream.iterator();
			boolean selfPeerIdNotRead=true;

			while(it.hasNext()) {
				PeerInfo peerInfo = new PeerInfo(it.next());
				neighborMap.put(peerInfo.getPeerId(),peerInfo);
				if(selfPeerIdNotRead && peerInfo.getPeerId()!=peer.getPeerID()) {
					activePeerIds.add(peerInfo.getPeerId());
				}
				else {
					selfPeerIdNotRead=false;
				}
			}
			myInfo = neighborMap.remove(peer.getPeerID());
			peer.setPeerMap(neighborMap);
			peer.setMyInfo(myInfo);
			peer.setFileSize(fileSize);
			peer.setPieceSize(pieceSize);
		}
		catch (IOException e) {
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

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	//establishTCPConnection and startServer both run by main thread?
	//what if handshake fails? deadlock?
	public static void main(String[] args) {
		String log4jConfPath = "log4j.properties";
		PropertyConfigurator.configure(log4jConfPath);

		PeerProcess me = new PeerProcess(1001);
		me.readPeerInfoFile();
		me.readCommonCFGFile();

		logger.info("Initial config files read\n");
		me.establishTCPConnection();
		logger.info("TCP connections to already connected peeers completed.\n");
		me.startServer();
	}


	public int getNumberOfPieces() {
		return (fileSize/pieceSize);
	}
}
