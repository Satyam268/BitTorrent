package com.peer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.peer.file.FileHandler;
import com.peer.messages.HandshakeMsg;

public class Peer {

	private int peerID;
	private PeerInfo myInfo;
	private BitSet bitfield;
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
		fileHandler = new FileHandler(peerId, properties, peerMap, myInfo.getHasFile());
	}

	// listening on port for new connections
	public void startServer() {
		OutputStream out = null;
		InputStream in = null;
		try {
			logger.info("Server started on port : " + myInfo.getListeningPort());
			ServerSocket serverSocket = new ServerSocket(myInfo.getListeningPort());
			Socket clientSocket;
			while (true) {
				clientSocket = serverSocket.accept();
				in = clientSocket.getInputStream();
				out = clientSocket.getOutputStream();
				int neighborId = handleHandshakeServer(in, out);
				if (neighborId != -1) {
					setSocketDetailsToPeerMap(neighborId, clientSocket, in, out);
					Thread t = new Thread(
							new SocketHandler(clientSocket, in, out, myInfo, peerMap, neighborId, fileHandler));
					t.start();
				}
			}
		} catch (IOException e) {
			logger.warn("Listening server stopped. " + e);
			e.printStackTrace();
		}
	}

	private int handleHandshakeServer(InputStream in2, OutputStream out2) {
		try {
			HandshakeMsg msg = readIncomingMessage(in2);
			if (isValidHandshake(msg)) {
				HandshakeMsg handshakeResponse = new HandshakeMsg(peerID);
				handshakeResponse.write(out2);
				return msg.getPeerID();
			}
		} catch (Exception e) {
			logger.warn("Unable to perform handshake.\n" + e);
		}
		return -1;
	}

	public void connectToPeers(List<Integer> activePeerIds) {
		logger.info("active peerIDs: "+ activePeerIds);
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
		logger.info("Connecting to peerIDs: "+ neighborId);
		try {
			Socket neighborSocket = new Socket(neighborInfo.getHostName(), neighborInfo.getListeningPort());
			HandshakeMsg handshakeMessage = new HandshakeMsg(peerID);
			OutputStream out = neighborSocket.getOutputStream();
			InputStream in = neighborSocket.getInputStream();
			logger.info("Before writing to: "+ neighborId);
			handshakeMessage.write(out);
			logger.info("After writing to : "+ neighborId);
			handleHandshakeClient(in, out, neighborSocket, neighborId);
			logger.info("Done Reading handshake from : "+ neighborId);
			
		} catch (UnknownHostException e) {
			logger.warn("Unable to make TCP connection with TCP host: " + neighborInfo.getHostName() + e);
		} catch (Exception e) {
			logger.warn("Unable to make TCP connection with TCP host: " + neighborInfo.getHostName() + e);
		}

	}

	private void handleHandshakeClient(InputStream in, OutputStream out, Socket neighbourSocket, int neighbourID)
			throws IOException {
		HandshakeMsg incomingHandShake = readIncomingMessage(in);
		if (isValidHandshake(incomingHandShake)) {
			setSocketDetailsToPeerMap(neighbourID, neighbourSocket, in, out);
		}
	}

	private boolean isValidHandshake(HandshakeMsg incomingHandShake) {
		if (!incomingHandShake.handshakeHeader.equals("P2PFILESHARINGPROJ")) {
			logger.info("Header mismatch in handshake");
			return false;
		}
		return true;
	}

	private HandshakeMsg readIncomingMessage(InputStream in) throws IOException {
		HandshakeMsg msg = new HandshakeMsg();
		byte[] handshakeHead = new byte[18];
		in.read(handshakeHead, 0, handshakeHead.length);
		msg.handshakeHeader = new String(handshakeHead);
		byte[] zeroBits = new byte[10];
		in.read(zeroBits, 0, 10);
		msg.zeroBits = zeroBits;

		byte[] peerId = new byte[4];
		in.read(peerId, 0, 4);
		msg.peerId = peerId;
		return msg;
	}

	private void setSocketDetailsToPeerMap(int neighborId, Socket neighborSocket, InputStream in, OutputStream out) {
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

	public BitSet getBitfield() {
		return bitfield;
	}

	public void setBitfield(BitSet bitfield) {
		this.bitfield = bitfield;
	}

	public void startPeerHandler() {
		PeerHandler peerHandler = new PeerHandler(peerID, peerMap, properties);
		Thread peerHandlerThread = new Thread(peerHandler);
		peerHandlerThread.start();
	}

	public void setProperties(PeerProperties peerProperties) {
		this.properties = peerProperties;
	}

}
