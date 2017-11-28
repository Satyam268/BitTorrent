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

import org.apache.log4j.Logger;

import com.peer.file.FileOperations;
import com.peer.messages.HandshakeMsg;
import com.peer.utilities.PeerProperties;

public class Peer {

	private int peerID;
	private PeerInfo myInfo;
	private BitSet bitfield;

	PeerProperties properties;

	// Connection Variables
	private ServerSocket serverSocket;
	private Map<Integer, PeerInfo> peerMap = new HashMap<>();
	private FileHandler fileHandler;
	final static Logger logger = Logger.getLogger(Peer.class);

	public Peer(int peerId) {
		peerID = peerId;
		fileHandler = new FileHandler(peerId);
	}

	public Peer(PeerInfo peerInfo) {
		setMyInfo(peerInfo);
	}

	// listening on port for new connections
	public void startServer() {
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		try {
			System.out.println("MY peer id:" + peerID);
			ServerSocket serverSocket = new ServerSocket(myInfo.getListeningPort());
			Socket clientSocket;
			logger.info(" Starting listening to my port\n");

			while (true) {
				logger.info(" Waiting for socket");
				clientSocket = serverSocket.accept();
				in = new ObjectInputStream(clientSocket.getInputStream());
				out = new ObjectOutputStream(clientSocket.getOutputStream());

				// handshake message
				int neighborId = handleHandshakeMessage(in, out);
				if (neighborId != -1) {
					logger.info(" Accepted conection from " + neighborId);
					setSocketDetailsToPeerMap(neighborId, clientSocket, in, out);
					Thread t = new Thread(new NewConnectionHandler(clientSocket, in, out, myInfo, peerMap, neighborId, fileHandler));
					t.start();
				}
				// client peerId to Socket hashMap -- so that one can delete the
				// thread once done
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void splitFileIfNeeded() {
		if(myInfo.hasFile==1) {
			File file=new File(properties.getFileName());
			FileOperations.processFileIntoPieceFiles(file, properties.getPieceSize());
		}
	}

	private int handleHandshakeMessage(ObjectInputStream in2, ObjectOutputStream out2) {
		HandshakeMsg handshakeMessage = new HandshakeMsg(peerID);
		try {
			handshakeMessage.read(in2);
			int neighbourID = handshakeMessage.getPeerID();
			System.out.println(peerMap.entrySet());
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
			logger.info("\nHandshake completed with " + neighborId);
			PeerInfo neighborInfo = peerMap.get(neighborId);
			
			Thread t = new Thread(new NewConnectionHandler(neighborInfo.clientSocket, neighborInfo.getSocketReader(),
					neighborInfo.getSocketWriter(), myInfo, peerMap, neighborId, fileHandler));
			t.start();
		}
	}

	public void doHandShake(int neighborId) {
		logger.info("Starting handshake with neighbourID:" + neighborId);
		PeerInfo neighborInfo = peerMap.get(neighborId);
		try {
			Socket neighborSocket = new Socket(neighborInfo.getHostName(), neighborInfo.getListeningPort());
			HandshakeMsg handshakeMessage = new HandshakeMsg(myInfo.getPeerId());
			
			logger.info(" Sent handshake msg to neighbourID:" + neighborId);

			ObjectOutputStream out = new ObjectOutputStream(neighborSocket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(neighborSocket.getInputStream());

			handshakeMessage.write(out);
			handshakeMessage.read(in);

			logger.info(" Received handshake msg from:" + neighborId);

			setSocketDetailsToPeerMap(neighborId, neighborSocket, in, out);

		} catch (UnknownHostException e) {
			logger.warn("Unable to make TCP connection with TCP host: " + neighborInfo.getHostName() + e);
		} catch (Exception e) {
			logger.warn("Unable to make TCP connection with TCP host: " + neighborInfo.getHostName() + e);
		}

	}

	private void setSocketDetailsToPeerMap(int neighborId, Socket neighborSocket, ObjectInputStream in, ObjectOutputStream out) {
		logger.info("Object being set in the peerMap with: neighbourID = " + neighborId);
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
		//this.myInfo.setBitfield(new BitSet(peerProperties.getNumberOfPieces()));
	}

}
