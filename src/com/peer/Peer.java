package com.peer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.peer.messages.HandshakeMsg;
import com.peer.messages.Message;
import com.peer.utilities.CommonUtils;

public class Peer {

	private int peerID;
	private int pieces;
	private PeerInfo myInfo;
	private BitSet bitfield;

	// Connection Variables
	private ServerSocket serverSocket;
	private DataOutputStream out;
	private DataInputStream in;
	private Map<Integer, PeerInfo> peerMap = new HashMap<>();
	private int fileSize;
	private int pieceSize;

	final static Logger logger = Logger.getLogger(Peer.class);

	public Peer(int peerId) {
		peerID = peerId;
	}

	public Peer(PeerInfo peerInfo) {
		setMyInfo(peerInfo);
	}

	private void calculatePeices() {
		pieces = fileSize/pieceSize;
	}

	public void startServer() {
		try {
			ServerSocket serverSocket = new ServerSocket(myInfo.getListeningPort());
			Socket clientSocket;
			logger.info("\nStarting listening to my port\n");
			while (true) {
				logger.info("\nWaiting for socket");
				clientSocket = serverSocket.accept();

				in = new DataInputStream(clientSocket.getInputStream());
				out = new DataOutputStream(clientSocket.getOutputStream());

				// handshake message
				int neighborId = handleHandshakeMessage(in, out);
				logger.info("\n Accepted conection from " + neighborId);

				// get which client it is...check in hashmap whether there's
				// already a connection
				peerMap.get(neighborId).setClientSocket(clientSocket);

				//starting new thread for this neighbourID n me
				Thread t = new Thread(new NewConnectionHandler(clientSocket, in, out, myInfo, peerMap, neighborId));
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
		return 0;
	}

	public void connectToPeers(List<Integer> activePeerIds) {
		for (int neighborId : activePeerIds) {
			doHandShake(neighborId);
			logger.info("\nHandshake completed with " + neighborId);
			PeerInfo neighborInfo = peerMap.get(neighborId);
			Thread t = new Thread(new NewConnectionHandler(neighborInfo.clientSocket, in, out, myInfo, peerMap, neighborId));
			t.start();
		}
	}

	public void doHandShake(int neighborId) {
		logger.info("Starting handshake with neighbourID:" + neighborId);
		PeerInfo neighborInfo = peerMap.get(neighborId);
		try {
			Socket neighborSocket = new Socket(neighborInfo.getHostName(), neighborInfo.getListeningPort());
			HandshakeMsg handshakeMessage = new HandshakeMsg(myInfo.getPeerId());
			logger.info("\nSent handshake msg to neighbourID:" + neighborId);
			handshakeMessage.write(new DataOutputStream(neighborSocket.getOutputStream()));
			handshakeMessage.read(new DataInputStream(neighborSocket.getInputStream()));
			logger.info("\nReceived handshake msg from:" + neighborId);
			peerMap.get(neighborId).setClientSocket(neighborSocket);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

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

	public int getPeices() {
		return pieces;
	}

	public void setPeices(int pieces) {
		this.pieces = pieces;
	}

	public BitSet getBitfield() {
		return bitfield;
	}

	public void setBitfield(BitSet bitfield) {
		this.bitfield = bitfield;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public void setPieceSize(int pieceSize) {
		this.pieceSize = pieceSize;
	}


}
