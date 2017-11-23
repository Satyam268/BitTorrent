package com.peer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.peer.messages.types.BitField;
import com.peer.roles.IDownloader;
import com.peer.roles.IUploader;

public class Peer {

	private int peerID;
	private int pieces;
	private PeerInfo myInfo;
	// records the pieces i have/don't have
	private BitSet bitfield = null;

	//Connection Variables
	private ServerSocket serverSocket;
    //private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Uploader uploader;
    private Downloader downloader;
    private Map<Integer,PeerInfo> peerMap = new HashMap<>();
    public Peer(int peerId){
    	peerID = peerId; 
    }
    
    
    public Peer(PeerInfo peerInfo) {
    	setMyInfo(peerInfo);
    }


	private void calculatePeices() {

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


	//to do below this
//	public void startTCPServer(int port) throws IOException {
//        ServerSocket serverSocket = new ServerSocket(port);
//        clientSocket = serverSocket.accept();
//        out = new PrintWriter(clientSocket.getOutputStream(), true);
//        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//        String greeting = in.readLine();
//        System.out.println(greeting);
//        if ("hello server".equals(greeting)) {
//            out.println("hello client");
//        } else {
//            out.println("unrecognized greeting");
//        }
//    }

	public void stopTCPServer() throws IOException {
        in.close();
        out.close();
       // clientSocket.close();
        serverSocket.close();
    }

	public void startConnection(String ip, int port) throws UnknownHostException, IOException {
//        clientSocket = new Socket(ip, port);
//        out = new PrintWriter(clientSocket.getOutputStream(), true);
//        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }


	public void stopConnection() throws IOException {
	        in.close();
	        out.close();
	   //     clientSocket.close();
	}

	public void startServer() {
		try {
			ServerSocket serverSocket = new ServerSocket(myInfo.getListeningPort());
			Socket clientSocket;
			while(true) {
				clientSocket = serverSocket.accept();
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String data = in.readLine();
				//get peerId from data packet mostly handshake message....
				//new connection first message 
				//call message handler - 
				System.out.println(data);
		        int neighborId = 1001;
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

}
