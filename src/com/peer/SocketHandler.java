package com.peer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

import org.apache.log4j.Logger;

import com.peer.file.FileHandler;
import com.peer.messages.ActualMsg;
import com.peer.messages.types.BitField;

public class SocketHandler implements Runnable {

	Socket socket;
	PeerInfo peerClient;
	private InputStream in;
	private OutputStream out;
	private Map<Integer, PeerInfo> peerMap;
	private int neighborId;
	private PeerInfo myInfo;
	private FileHandler fileHandler;
	final static Logger logger = Logger.getLogger(SocketHandler.class);

	public SocketHandler(Socket clientSocket, PeerInfo peerInfo) {
		socket = clientSocket;
		peerClient = peerInfo;
	}

	public SocketHandler(Socket clientSocket, InputStream in2, OutputStream out2, PeerInfo myInfo,
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
				} catch (Exception e) {
					e.printStackTrace();
					logger.warn("some issue inside of message handler" + e);
					// break; because we don't want to stop listening if a odd
					// packet comes in
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.warn("some issue in socket");
		}
	}

	private void sendBitFieldMessage(OutputStream out2) {
		try {
			ActualMsg bitFieldMessage = new BitField();
			bitFieldMessage.setLength(myInfo.getBitfield().length() + 1);
			bitFieldMessage.setPayload(myInfo.getBitfield().toByteArray());
			bitFieldMessage.write(out2);
		} catch (IOException e) {
			logger.warn("Unable to write bitField Message to Peer: " + neighborId + " " + e);
		}
	}

}
