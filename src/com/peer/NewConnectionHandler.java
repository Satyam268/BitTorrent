package com.peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import org.apache.log4j.Logger;

import com.peer.messages.ActualMsg;
import com.peer.messages.types.BitField;

public class NewConnectionHandler implements Runnable {

	Socket socket;
	PeerInfo peerClient;
	private DataInputStream in;
	private DataOutputStream out;
	private Map<Integer, PeerInfo> peerMap;
	private int neighborId;
	private PeerInfo myInfo;
	private FileHandler fileHandler;
	final static Logger logger = Logger.getLogger(NewConnectionHandler.class);

	public NewConnectionHandler(Socket clientSocket, PeerInfo peerInfo) {
		socket = clientSocket;
		peerClient = peerInfo;
	}

	public NewConnectionHandler(Socket clientSocket, DataInputStream in2, DataOutputStream out2, PeerInfo myInfo,
			Map<Integer, PeerInfo> peerMap, int neighborId, FileHandler fileHandler) {
		socket = clientSocket;
		this.in = in2;
		this.out = out2;
		this.myInfo = myInfo;
		this.peerMap = peerMap;
		this.neighborId = neighborId;
		this.fileHandler = fileHandler;
	}

	@Override
	public void run() { // validate
		// listen to the port and write to the port continuously
		// message handler will be called here

		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			MessageHandler messageHandler = new MessageHandler(in, out, myInfo, peerMap, neighborId, fileHandler);

			// send bitset
			sendBitFieldMessage(out);
			logger.info("should send bitset message here.");

			while (true) {
				try {
					// System.out.println("got response ");
					// call message handler and pass out to handler
					if (in.available() > 0)
						messageHandler.handleMessage();

				} catch (Exception e) {
					logger.warn("Invalid Message sent from peer: " + neighborId + " " + e);
				}
			}
		} catch (Exception e1) {
			logger.warn("Problem Connecting to peer: " + neighborId + " " + e1);
		}

	}

	private void sendBitFieldMessage(DataOutputStream out2) {
		try {
			ActualMsg bitFieldMessage = new BitField();
			bitFieldMessage.setPayload(myInfo.getBitfield().toByteArray());
			bitFieldMessage.write(out2);
			logger.info("Sent bitField Message to Peer "+neighborId);
		} catch (IOException e) {
			logger.warn("Unable to write bitField Message to Peer: "+ neighborId + " "+ e);
		}
	}

}
