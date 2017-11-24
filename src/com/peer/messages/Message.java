package com.peer.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ProtocolException;

import com.peer.messages.types.BitField;
import com.peer.messages.types.Choke;
import com.peer.messages.types.Have;
import com.peer.messages.types.Interested;
import com.peer.messages.types.NotInterested;
import com.peer.messages.types.Piece;
import com.peer.messages.types.Request;
import com.peer.messages.types.Unchoke;
import com.peer.utilities.MessageType;

public class Message {

	public void send() {
		try {
			Message msg = null;
			byte[] data = serialize(msg);
		} catch (Exception e) {
			System.out.println("serialization error");
		}

	}

	private byte[] serialize(Object obj) throws IOException {
		// TODO Auto-generated method stub
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray();
	}

	public void receive(byte[] readBytes) {
		try {
			Object o = deserialize(readBytes);
			Message msg = (Message) o;

			if (msg instanceof HandshakeMsg) {

			} else if (msg instanceof ActualMsg) {

			} else {
				// bad msg
			}
		} catch (Exception e) {
			System.out.println("cant deserialize");
		}
	}

	private Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		return is.readObject();
	}

	public void read(DataInputStream in) throws ProtocolException, IOException {
		int len = 0;
		try {
			len = in.readInt();
			System.out.println("Length :" + len);

			byte[] payload = new byte[len];

			in.readFully(payload, 0, len);
			for (byte b : payload)
				System.out.print(" " + (int) b + " ");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void write(DataOutputStream out) throws IOException {
		ActualMsg s = new ActualMsg();
		out.writeInt(s.getLength());
		out.writeByte((byte)s.getType().ordinal());
		out.write(s.getPayload(), 0, s.getPayload().length);
	}

	public static Message getInstance(int length, MessageType type) throws ClassNotFoundException, IOException {
		switch (type) {
		case CHOKE:
			return new Choke();

		case UNCHOKE:
			return new Unchoke();

		case INTERESTED:
			return new Interested();

		case NOTINTERESTED:
			return new NotInterested();

		case HAVE:
			return new Have();
		// return new Have(new byte[length]);

		case BITFIELD:
			return new BitField();
		// return new Bitfield(new byte[length]);

		case REQUEST:
			return new Request();
		// return new Request(new byte[length]);

		case PIECE:
			return new Piece();
		// return new Piece(new byte[length]);

		default:
			throw new ClassNotFoundException("message type not handled: " + type.toString());
		}
	}

}
