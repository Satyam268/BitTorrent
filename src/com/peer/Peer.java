package com.peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.peer.messages.HandshakeMsg;
import com.peer.utilities.CommonUtils;

public class Peer {

	private int peerID;
	private int pieces;
	private PeerInfo myInfo;
	private BitSet bitfield;

	// Connection Variables
	private ServerSocket serverSocket;
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
		pieces = CommonUtils.getNumberOfPieces();
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
			Thread t = new Thread(new NewConnectionHandler(neighborInfo.clientSocket, neighborInfo.getSocketReader(), neighborInfo.getSocketWriter(), myInfo, peerMap, neighborId));
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
			
			DataOutputStream out = new DataOutputStream(neighborSocket.getOutputStream());
			DataInputStream in = new DataInputStream(neighborSocket.getInputStream());
			
			handshakeMessage.write(out);
			handshakeMessage.read(in);
			
			logger.info(" Received handshake msg from:" + neighborId);
			
			setSocketDetails(neighborId, neighborSocket, in, out);
		
		} catch (UnknownHostException e) {
			logger.warn("Unable to make TCP connection with TCP host: "+neighborInfo.getHostName()+e);
		} catch (IOException e) {
			logger.warn("Unable to make TCP connection with TCP host: "+neighborInfo.getHostName()+e);
		}

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

	public void startPeerHandler() {
		// TODO Auto-generated method stub
		PeerHandler peerHandler = new PeerHandler(peerMap);
		Thread peerHandlerThread = new Thread(peerHandler);
		peerHandlerThread.start();
	}

}
