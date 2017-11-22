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

	PeerMeta myInfo;
	List<PeerMeta> peerMetaCfg;
	int NumberOfPreferredNeighbors;
	int UnchokingInterval;
	int OptimisticUnchokingInterval;
	String FileName;
	int FileSize;
	int PieceSize;

	List<Peer> interestedNeighbors = new ArrayList<>();

	final static Logger logger = Logger.getLogger(PeerProcess.class);
	Peer taskHandler = null;

	public PeerProcess(int peerId) {
		myInfo = new PeerMeta(peerId);
		taskHandler = new Peer();
	}

	/*
	 * void establishTCPConnection() { for(int i=0;i<peerMetaCfg.size();i++) {
	 * if(peerMetaCfg.get(i).peerId!=myInfo.peerId) { //establish TCP connection
	 * logger.info("Establishes connection with peerId "+
	 * peerMetaCfg.get(i).peerId); } else { break; } } }
	 */
	void readPeerInfoFile() {
		String fileName = "src/com/peer/configFiles/PeerInfo.cfg";
		peerMetaCfg = new ArrayList<>();

		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			stream.forEach(x -> peerMetaCfg.add(new PeerMeta(x)));
			taskHandler.processNeighbours(peerMetaCfg);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	void readCommonCFGFile() {
		String fileName = "src/com/peer/configFiles/Common.cfg";
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			Iterator<String> it = stream.iterator();
			NumberOfPreferredNeighbors = Integer.parseInt(it.next().split(" ")[1]);
			UnchokingInterval = Integer.parseInt(it.next().split(" ")[1]);
			OptimisticUnchokingInterval = Integer.parseInt(it.next().split(" ")[1]);
			FileName = it.next().split(" ")[1];
			FileSize = Integer.parseInt(it.next().split(" ")[1]);
			PieceSize = Integer.parseInt(it.next().split(" ")[1]);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		String log4jConfPath = "log4j.properties";
		PropertyConfigurator.configure(log4jConfPath);

		PeerProcess me = new PeerProcess(1004);
		me.readPeerInfoFile();
		// me.establishTCPConnection();
		me.readCommonCFGFile();
	}
}
