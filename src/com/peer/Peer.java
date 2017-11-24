package com.peer;

import java.io.BufferedReader;
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

public class Peer {

	private int peerID;
	private int pieces;
	private PeerInfo myInfo;

	//Connection Variables
	private ServerSocket serverSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Map<Integer,PeerInfo> peerMap = new HashMap<>();
    public Peer(int peerId){
    	peerID = peerId;
    }

    public Peer(PeerInfo peerInfo) {
    	setMyInfo(peerInfo);
    }


	private void calculatePeices() {

	}

	public void startServer() {
		try {
			ServerSocket serverSocket = new ServerSocket(myInfo.getListeningPort());
			Socket clientSocket;
			while(true) {
				System.out.println("Waiting for socket");
				clientSocket = serverSocket.accept();
				System.out.println("accepted");
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				
				if()
				
				
				//get peerId from data packet mostly handshake message....
				//new connection first message
				//call message handler -
				System.out.println(data);
		        int neighborId = 1002;
				//get which client it is...check in hashmap whether there's already a connection
				//pass clientSocket and the peerInfo of relevant peer based on what you get from
				// message handler
				// if new connection from same peerID then check old is deprecated or not
				//socket to peerId mapping
		        peerMap.get(neighborId).setClientSocket(clientSocket);
		        Thread t = new Thread(new NewConnectionHandler(clientSocket, null));
				t.start();
				// client peerId to Socket hashMap -- so that one can delete the thread once done

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void connectToPeers(List<Integer> activePeerIds) {
		for(int neighborId:activePeerIds) {
			doHandShake(neighborId);
			PeerInfo neighborInfo = peerMap.get(neighborId);
			Thread t = new Thread(new NewConnectionHandler(neighborInfo.getClientSocket(), neighborInfo));
			t.start();
		}
	}


	public void doHandShake(int neighborId) {
		PeerInfo neighborInfo = peerMap.get(neighborId);

		try {
			Socket neighborSocket = new Socket(neighborInfo.getHostName(), neighborInfo.getListeningPort());
			PrintWriter outer = new PrintWriter(neighborSocket.getOutputStream(), true);
			outer.println("Hi from Yash");
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


}
