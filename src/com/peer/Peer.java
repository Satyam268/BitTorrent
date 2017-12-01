package com.peer;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.peer.file.FileHandler;
import com.peer.file.FileOperations;
import com.peer.messages.HandshakeMsg;

public class Peer {

	private int peerID;
	private PeerInfo myInfo;
	//private BitSet bitfield;//receivedParts

	// has a
	PeerProperties properties;

	// Connection Variables
	private Map<Integer, PeerInfo> peerMap;
	private FileHandler fileHandler;
	final static Logger logger = Logger.getLogger(Peer.class);

	public Peer(int peerId) {
		peerID = peerId;
		fileHandler = new FileHandler(peerId);
	}

	public Peer(PeerInfo peerInfo) {
		setMyInfo(peerInfo);
	}

	public Peer(int peerId, PeerProperties peerProperties, Map<Integer, PeerInfo> neighborMap, PeerInfo info) {
		peerID = peerId;
		properties = peerProperties;
		peerMap = neighborMap;
		myInfo = info;
		fileHandler = new FileHandler(peerId, properties, peerMap, myInfo);
	}

	// listening on port for new connections
	public void startServer() {
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		try {
			System.out.println("my peer id:" + peerID);
			ServerSocket serverSocket = new ServerSocket(myInfo.getListeningPort());
			Socket clientSocket;
			while (true) {
				clientSocket = serverSocket.accept();
				in = new ObjectInputStream(clientSocket.getInputStream());
				out = new ObjectOutputStream(clientSocket.getOutputStream());

				int neighborId = handleHandshakeMessage(in, out);
				if (neighborId != -1) {
					setSocketDetailsToPeerMap(neighborId, clientSocket, in, out);
					Thread t = new Thread(
							new SocketHandler(clientSocket, in, out, myInfo, peerMap, neighborId, fileHandler));
					t.start();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int handleHandshakeMessage(ObjectInputStream in2, ObjectOutputStream out2) {
		HandshakeMsg handshakeMessage = new HandshakeMsg(peerID);
		try {
			handshakeMessage.read(in2);
			int neighbourID = handshakeMessage.getPeerID();
			peerMap.put(neighbourID, new PeerInfo(neighbourID));
			handshakeMessage.write(out2);
			return neighbourID;
		} catch (Exception e) {
			logger.debug("Unable to perform handshake.\n" + e);
		}
		return -1;
	}

	public void connectToPeers(List<Integer> activePeerIds) {

		for (int neighborId : activePeerIds) {
			doHandShake(neighborId);
			PeerInfo neighborInfo = peerMap.get(neighborId);
			Thread t = new Thread(new SocketHandler(neighborInfo.clientSocket, neighborInfo.getSocketReader(),
					neighborInfo.getSocketWriter(), myInfo, peerMap, neighborId, fileHandler));
			t.start();
		}
	}

	public void doHandShake(int neighborId) {
		PeerInfo neighborInfo = peerMap.get(neighborId);
		try {
			Socket neighborSocket = new Socket(neighborInfo.getHostName(), neighborInfo.getListeningPort());
			HandshakeMsg handshakeMessage = new HandshakeMsg(myInfo.getPeerId());
			ObjectOutputStream out = new ObjectOutputStream(neighborSocket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(neighborSocket.getInputStream());
			handshakeMessage.write(out);
			handshakeMessage.read(in);
			setSocketDetailsToPeerMap(neighborId, neighborSocket, in, out);
		} catch (UnknownHostException e) {
			logger.warn("Unable to make TCP connection with TCP host: " + neighborInfo.getHostName() + e);
		} catch (Exception e) {
			logger.warn("Unable to make TCP connection with TCP host: " + neighborInfo.getHostName() + e);
		}

	}

	private void setSocketDetailsToPeerMap(int neighborId, Socket neighborSocket, ObjectInputStream in,
			ObjectOutputStream out) {
		peerMap.get(neighborId).setClientSocket(neighborSocket);
		peerMap.get(neighborId).setSocketReader(in);
		peerMap.get(neighborId).setSocketWriter(out);
	}

	public void setPeerMap(Map<Integer, PeerInfo> neighborMap) {
		peerMap = neighborMap;
	}

	public PeerInfo getMyInfo() {
		return myInfo;
	}

	public void setMyInfo(PeerInfo myInfo) {
		this.myInfo = myInfo;
	}

	public int getPeerID() {
		return peerID;
	}

	public void setPeerID(int peerID) {
		this.peerID = peerID;
	}

	/*public BitSet getBitfield() {
		return bitfield;
	}

	public void setBitfield(BitSet bitfield) {
		this.bitfield = bitfield;
	}
*/
	public void startPeerHandler() {
		PeerHandler peerHandler = new PeerHandler(peerID, peerMap, properties);
		Thread peerHandlerThread = new Thread(peerHandler);
		peerHandlerThread.start();
	}

	public void setProperties(PeerProperties peerProperties) {
		this.properties = peerProperties;
	}

}
