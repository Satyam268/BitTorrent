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
import java.util.BitSet;

import com.peer.roles.Downloader;
import com.peer.roles.Uploader;

public class Peer implements Uploader, Downloader {

	private int peerID;
	// private List<PeerInfo> peerDetails;
	private int peices;

	// records the pieces i have, don't have
	private BitSet bitfield = null;

	// to do
	// currently processing list
	// hashset ? or Enum array sort?
	
	
	//Connection Variables
	private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

	public Peer() {

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
		return peices;
	}

	public void setPeices(int peices) {
		this.peices = peices;
	}

	public BitSet getBitfield() {
		return bitfield;
	}

	public void setBitfield(BitSet bitfield) {
		this.bitfield = bitfield;
	}

	@Override
	public byte doHandshake() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void selectKPeers() {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectOptimisticUnchokedNeighbour() {
		// TODO Auto-generated method stub

	}

	@Override
	public void markPeiceReceived(int peiceIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void measureDownloadSpeed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void mergePieces() {
		// TODO Auto-generated method stub

	}

	@Override
	public void writePieceToFile() {
		Path path = Paths.get("output.txt");

		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			writer.write("Hello World !!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
