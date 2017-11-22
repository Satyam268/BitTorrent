package com.peer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import com.peer.roles.IUploader;

public class Uploader implements IUploader, Runnable {

	Socket requestSocket; // socket connect to the server

	ObjectOutputStream out; // stream write to the socket
	ObjectInputStream in; // stream read from the socket

	String message; // message send to the server
	String MESSAGE; // capitalized message read from the server

	public Uploader(List<PeerMeta> neighbours) {

	}

	public void run() {
		try {
			// create a socket to connect to the server
			requestSocket = new Socket("localhost", 8000);
			System.out.println("Connected to localhost in port 8000");
			// initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			// get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				System.out.print("Hello, please input a sentence: ");
				// read a sentence from the standard input
				message = bufferedReader.readLine();
				// Send the sentence to the server

				sendMessage(message);
				// Receive the upperCase sentence from the server

				MESSAGE = (String) in.readObject();
				// show the message to the user

				System.out.println("Receive message: " + MESSAGE);
			}
		} catch (ConnectException e) {
			System.err.println("Connection refused. You need to initiate a server first.");
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found");
		} catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			// Close connections
			try {
				in.close();
				out.close();
				requestSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

	}

	// send a message to the output stream
	void sendMessage(String msg) {
		try {
			// stream write the message
			out.writeObject(msg);
			out.flush();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	/*
	 * // main method // Init Uploader public static void main(String args[]) {
	 * Uploader client = new Uploader(new Lis); client.run(); }
	 */
	@Override
	public void selectKPeers() {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectOptimisticUnchokedNeighbour() {
		// TODO Auto-generated method stub

	}

}
