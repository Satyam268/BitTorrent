package com.peer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NewConnectionHandler implements Runnable{

	Socket socket;
	PeerInfo peerClient;
	private BufferedReader in;
	private PrintWriter out;
	public NewConnectionHandler(Socket clientSocket, PeerInfo peerInfo) {
		socket = clientSocket;
		peerClient = peerInfo;
	}
	@Override
	public void run() { // validate
		// listen to the port and write to the port continuously 
		// message handler will be called here
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			while(true) {
				try {
					in.readLine();
					// call message handler and pass out to handler
		    	}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}
	
}
