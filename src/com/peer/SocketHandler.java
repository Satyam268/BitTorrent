package com.peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.peer.file.FileHandler;
import com.peer.messages.ActualMsg;
import com.peer.messages.types.BitField;

public class SocketHandler implements Runnable {

	Socket socket;
	PeerInfo peerClient;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Map<Integer, PeerInfo> peerMap;
	private int neighborId;
	private PeerInfo myInfo;
	private FileHandler fileHandler;
	final static Logger logger = Logger.getLogger(SocketHandler.class);

	public SocketHandler(Socket clientSocket, PeerInfo peerInfo) {
		socket = clientSocket;
		peerClient = peerInfo;
	}

	public SocketHandler(Socket clientSocket, ObjectInputStream in2, ObjectOutputStream out2, PeerInfo myInfo,
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
	public void run() {
		// listen to the port and write to the port continuously
		// message handler will be called here
		try {
			MessageHandler messageHandler = new MessageHandler(in, out, myInfo, peerMap, neighborId, fileHandler);
			sendBitFieldMessage(out);

			while (true) {
				try {
					messageHandler.handleMessage();
				} catch (SocketException s) {
					logger.debug("Socket Exception with peerID:" + neighborId + " .. Aborting now ... " + s);
					// s.printStackTrace();
					break;
				} catch (Exception e) {
					logger.debug("problem with mesage/connection  with peerID:" + neighborId + " " + e);
					//e.printStackTrace();
				}
			}
		} catch (Exception e1) {
			logger.debug("Problem Connecting to peer: " + neighborId + " " + e1);
		}
	}

	private void sendBitFieldMessage(ObjectOutputStream out2) {
		try {
			ActualMsg bitFieldMessage = new BitField();
			bitFieldMessage.setLength(myInfo.getBitfield().length() + 1);
			bitFieldMessage.setPayload(myInfo.getBitfield().toByteArray());
			bitFieldMessage.write(out2);
			logger.debug("------ Sent bitField Message with details: " + bitFieldMessage + "to peerID" + neighborId+ "-------");
		} catch (IOException e) {
			logger.debug("Unable to write bitField Message to Peer: " + neighborId + " " + e);
		}
	}

}
