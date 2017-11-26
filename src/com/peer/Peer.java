package com.peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.peer.messages.HandshakeMsg;
import com.peer.utilities.PeerProperties;

public class Peer {

	private int peerID;
	private PeerInfo myInfo;
	private BitSet bitfield;

	PeerProperties properties;
	// Connection Variables
	private ServerSocket serverSocket;

	private Map<Integer, PeerInfo> peerMap = new ConcurrentHashMap<>();
	private FileHandler fileHandler;
	final static Logger logger = Logger.getLogger(Peer.class);

	public Peer(int peerId) {
		peerID = peerId;
		fileHandler = new FileHandler(peerId);
	}

	public Peer(PeerInfo peerInfo) {
		setMyInfo(peerInfo);

	}

	public void startServer() {

		DataOutputStream out = null;
		DataInputStream in = null;
		try {
			ServerSocket serverSocket = new ServerSocket(myInfo.getListeningPort());
			Socket clientSocket;
			logger.info(" Starting listening to my port\n");
			while (true) {
				logger.info(" Waiting for socket");
				clientSocket = serverSocket.accept();
				in = new DataInputStream(clientSocket.getInputStream());
				out = new DataOutputStream(clientSocket.getOutputStream());

				// handshake message
				int neighborId = handleHandshakeMessage(in, out);
				logger.info(" Accepted conection from " + neighborId);

				setSocketDetails(neighborId, clientSocket, in, out);
				Thread t = new Thread(
						new NewConnectionHandler(clientSocket, in, out, myInfo, peerMap, neighborId, fileHandler));
				t.start();
				// client peerId to Socket hashMap -- so that one can delete the
				// thread once done
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int handleHandshakeMessage(DataInputStream in2, DataOutputStream out2) {
		HandshakeMsg handshakeMessage = new HandshakeMsg(peerID);
		try {
			handshakeMessage.read(in2);
			int neighbourID = handshakeMessage.getPeerID();
			peerMap.put(neighbourID, new PeerInfo(neighbourID));
			handshakeMessage.write(out2);
			return neighbourID;
		} catch (IOException e) {
			logger.debug("Unable to perform handshake.\n" + e);
		}
		return -1;
	}

	public void connectToPeers(List<Integer> activePeerIds) {
		for (int neighborId : activePeerIds) {

			if (doHandShake(neighborId)) {
				logger.info("\nHandshake completed with " + neighborId);
				PeerInfo neighborInfo = peerMap.get(neighborId);
				Thread t = new Thread(
						new NewConnectionHandler(neighborInfo.clientSocket, neighborInfo.getSocketReader(),
								neighborInfo.getSocketWriter(), myInfo, peerMap, neighborId, fileHandler));
				t.start();
			}
		}
	}

	public boolean doHandShake(int neighborId) {
		logger.info("Starting handshake with neighbourID:" + neighborId);
		PeerInfo neighborInfo = peerMap.get(neighborId);
		try {
			Socket neighborSocket = new Socket(neighborInfo.getHostName(), neighborInfo.getListeningPort());
			HandshakeMsg handshakeMessage = new HandshakeMsg(myInfo.getPeerId());

			logger.info(" Sent handshake msg to neighbourID:" + neighborId);

			DataOutputStream out = new DataOutputStream(neighborSocket.getOutputStream());
			DataInputStream in = new DataInputStream(neighborSocket.getInputStream());

			handshakeMessage.write(out);
			handshakeMessage.read(in);
			logger.info(" Received handshake msg from:" + neighborId);
			setSocketDetails(neighborId, neighborSocket, in, out);

		} catch (UnknownHostException e) {
			logger.warn("Unable to make TCP connection with TCP host: " + neighborInfo.getHostName() + e);
			return false;
		} catch (IOException e) {
			logger.warn("Unable to make TCP connection with TCP host: " + neighborInfo.getHostName() + e);
			return false;
		}
		return true;
	}

	private void setSocketDetails(int neighborId, Socket neighborSocket, DataInputStream in, DataOutputStream out) {
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
		// TODO Auto-generated method stub
		PeerHandler peerHandler = new PeerHandler(peerMap, properties);
		Thread peerHandlerThread = new Thread(peerHandler);
		peerHandlerThread.start();
	}

	public void setProperties(PeerProperties peerProperties) {
		this.properties = peerProperties;
	}

}
