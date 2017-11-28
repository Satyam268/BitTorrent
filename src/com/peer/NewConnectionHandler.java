package com.peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.BitSet;
import java.util.Map;

import org.apache.log4j.Logger;

import com.peer.messages.ActualMsg;
import com.peer.messages.types.BitField;

public class NewConnectionHandler implements Runnable {

	Socket socket;
	PeerInfo peerClient;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Map<Integer, PeerInfo> peerMap;
	private int neighborId;
	private PeerInfo myInfo;
	private FileHandler fileHandler;
	final static Logger logger = Logger.getLogger(NewConnectionHandler.class);

	public NewConnectionHandler(Socket clientSocket, PeerInfo peerInfo) {
		socket = clientSocket;
		peerClient = peerInfo;
	}

	public NewConnectionHandler(Socket clientSocket, ObjectInputStream in2, ObjectOutputStream out2, PeerInfo myInfo,
			Map<Integer, PeerInfo> peerMap, int neighborId, FileHandler fileHandler) {
		socket = clientSocket;
		this.in = in2;
		this.out = out2;
		this.myInfo = myInfo;
		this.peerMap = peerMap;
		this.neighborId = neighborId;
		this.fileHandler = fileHandler;
		logger.info("-----------new connection handler object of new connection hadler created with id: " + neighborId
				+ "--------");
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
				} catch (Exception e) {
					logger.warn("problem with mesage/connection  with peerID:" + neighborId + " " + e);
					break;
				}
			}
		} catch (Exception e1) {
			logger.warn("Problem Connecting to peer: " + neighborId + " " + e1);
		}
	}

	private void sendBitFieldMessage(ObjectOutputStream out2) {
		try {
			ActualMsg bitFieldMessage = new BitField();
			bitFieldMessage.setLength(myInfo.getBitfield().length() + 1);
			bitFieldMessage.setPayload(myInfo.getBitfield().toByteArray());
			bitFieldMessage.write(out2);
			logger.info("------ Sent bitField Message with details: " + bitFieldMessage + "to peerID" + neighborId
					+ "-------");
		} catch (IOException e) {
			logger.warn("Unable to write bitField Message to Peer: " + neighborId + " " + e);
		}
	}

}
