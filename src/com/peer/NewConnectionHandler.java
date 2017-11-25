package com.peer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

import org.apache.log4j.Logger;

import com.peer.messages.ActualMsg;
import com.peer.messages.types.BitField;
import com.peer.utilities.CommonUtils;
import com.peer.utilities.MessageType;

public class NewConnectionHandler implements Runnable{

	Socket socket;
	PeerInfo peerClient;
	private DataInputStream in;
	private DataOutputStream out;
	private Map<Integer,PeerInfo> peerMap;
	private int neighborId;
	private PeerInfo myInfo;
	final static Logger logger = Logger.getLogger(NewConnectionHandler.class);

	public NewConnectionHandler(Socket clientSocket, PeerInfo peerInfo) {
		socket = clientSocket;
		peerClient = peerInfo;
	}

	public NewConnectionHandler(Socket clientSocket, DataInputStream in2, DataOutputStream out2, PeerInfo myInfo,
			Map<Integer, PeerInfo> peerMap, int neighborId) {
		socket = clientSocket;
		this.in=in2;
		this.out=out2;
		this.myInfo = myInfo;
		this.peerMap= peerMap;
		this.neighborId = neighborId;
	}

	@Override
	public void run() { // validate
		// listen to the port and write to the port continuously
		// message handler will be called here

		try {
			MessageHandler messageHandler = new MessageHandler(in, out, myInfo, peerMap, neighborId);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			//send bitset
			//ActualMsg bitFieldMessage = new BitField();
			logger.info("should send bitset message here.");
			ActualMsg tempPacket = new ActualMsg();
			tempPacket.setLength(5);
	 		tempPacket.setType(MessageType.HAVE);
			tempPacket.setPayload(CommonUtils.intToByteArray(10));
			tempPacket.write(out);
			tempPacket.write(out);
			while(true) {
				try {
					//System.out.println("got response ");
					// call message handler and pass out to handler
					if(in.available()>0)
						messageHandler.handleMessage();
		    	}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


	}

}
