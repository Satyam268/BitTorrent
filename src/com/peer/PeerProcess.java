/*package com.peer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
	int peerID;
	Peer peer = null;
	List<ConfigFileParams> peerInfoFileParams;
	List<Peer> interestedNeighbors = new ArrayList<>();
	final static Logger logger = Logger.getLogger(PeerProcess.class);

	public PeerProcess() {
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
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// configures log4j
		int peerid = 1001;
		String log4jConfPath = "log4j.properties";
		System.setProperty("file.name", "log_peer_" +  peerid+ ".log");
		PropertyConfigurator.configure(log4jConfPath);

		PeerProcess me = new PeerProcess();
		me.readCommonCFGFile();
		me.readPeerInfoFile();
		me.setupPeerInformation(peerid);
		logger.info("Initial config files read\n");
		me.establishTCPConnection();
		logger.info("TCP connections to already connected peeers completed.\n");
		me.startServer();
	}

	public int getPeerID() {
		return peerID;
	}

	public void setPeerID(int peerID) {
		this.peerID = peerID;
	}

	private void setupPeerInformation(int peerId) {
		setPeerProperties(peerId);
		setupOtherPeerInfo();
	}

	private void setupOtherPeerInfo() {
		boolean selfPeerIdNotRead = true;

		for (ConfigFileParams it : peerInfoFileParams) {
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
		peer.splitFileIfNeeded();
	}

	public int getNumberOfPieces() {
		return peer.properties.getNumberOfPieces();
	}

	public void splitFileIntoPieceFiles(File file, int pieceSize) {
		FileOperations.processFileIntoPieceFiles(file, pieceSize);
	}

	private void setPeerProperties(int peerId) {
		this.peer=new Peer(peerId, new PeerProperties(this.fileName, fileSize, optimisticUnchokingInterval,
				numberOfPreferredNeighbors, pieceSize, unchokingInterval));
	}

}
*/

package com.peer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.log4j.PropertyConfigurator;

import com.peer.utilities.Constants;
import com.peer.utilities.PeerProperties;

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
		ArrayList peerInfoFileParams = new ArrayList<>();
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

	public static void main(String[] args) {
		int peerId = 1001;
		System.setProperty("file.name", "log_peer_" + peerId + ".log");
		PropertyConfigurator.configure(Constants.log4jConfPath);
		PeerProcess process = new PeerProcess();
		process.readCommonCFGFile();
		process.readPeerInfoFile(peerId);
		process.setupPeer(peerId);
	}

	private void setupPeer(int peerId) {
		PeerInfo myInfo = neighborMap.remove(peerId);
		this.peer = new Peer(peerId, peerProperties, neighborMap, myInfo);

		peer.startPeerHandler();
		peer.startServer();
		peer.connectToPeers(activePeerIds);
	}

}
