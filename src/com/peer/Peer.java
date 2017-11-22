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
	// private List<Peer> peerDetails;
	private int pieces;

	// records the pieces i have/don't have
	private BitSet bitfield = null;
	
	//Connection Variables
	private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Uploader uploader;
    private Downloader downloader;
	public Peer() {
		uploader = new Uploader();
		downloader = new Downloader();
	}
	
	Map<Integer,PeerInfo> peerMap = new HashMap<>();
	
	private void calculatePeices() {
		
	}
	
	public byte doHandShake() {
		return 0;
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
	public void startTCPServer(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String greeting = in.readLine();
        System.out.println(greeting);
        if ("hello server".equals(greeting)) {
            out.println("hello client");
        } else {
            out.println("unrecognised greeting");
        }
    }
	
	public void stopTCPServer() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
	
	public void startConnection(String ip, int port) throws UnknownHostException, IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }
	
	
	public void stopConnection() throws IOException {
	        in.close();
	        out.close();
	        clientSocket.close();
	}
	 
}
