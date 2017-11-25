package com.peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.peer.utilities.CommonUtils;

//peer has a file handler
//manages the 2 bitsets, depicting requestedParts and receivedParts
public class FileHandler {
	final static Logger logger = Logger.getLogger(NewConnectionHandler.class);
	private final Collection<FileHandlerListner> listeners = new LinkedList<>();
	private Destination destination;

	private BitSet receivedParts;//piece I have
	private RequestedParts partsBeingRequested;//pieces I have requested for
	//private ConcurrentHashMap<Integer, Integer> clientRequestMap = new ConcurrentHashMap<>();

	int pieceSize;
	int bitsetSize;
	int peerID;

	FileHandler(int peerId, String fileName, int fileSize, int pieceSize, int unchokingInterval) {
		this.pieceSize = pieceSize;
		bitsetSize = (int) Math.ceil(fileSize / pieceSize);
		this.peerID = peerId;

		logger.debug("File size set to " + fileSize + "\tPart size set to " + pieceSize + "\tBitset size set to "+ bitsetSize);
		receivedParts = new BitSet(bitsetSize);
		partsBeingRequested = new RequestedParts(bitsetSize, unchokingInterval);
		//destination = new Destination(peerId, fileName);
	}

	/**
	 * got a new piece message; add it to receivedParts
	 *
	 * @param partIdx
	 * @param part
	 */
	public synchronized void addPart(int pieceID, byte[] part) {
		final boolean isNewPiece = !receivedParts.get(pieceID);
		//write into a file
		receivedParts.set(pieceID);

		if (isNewPiece) {
			destination.writeByteArrayAsFilePart(part, pieceID);
			for (FileHandlerListner listener : listeners) {
				listener.pieceArrived(pieceID);
			}
		}
		if (isFileCompleted()) {
			destination.mergeFile(receivedParts.cardinality());
			for (FileHandlerListner listener : listeners) {
				listener.fileCompleted();
			}
		}
	}

	/**
	 * @param availableParts
	 *            parts that are available at the remote peer
	 * @return the ID of the part to request, if any, or a negative number in
	 *         case all the missing parts are already being requested or the
	 *         file is complete.
	 */
	synchronized int getPartToRequest(BitSet availableParts) {
		availableParts.andNot(getReceivedParts());
		return partsBeingRequested.getPartToRequest(availableParts);
	}

	public synchronized BitSet getReceivedParts() {
		return (BitSet) receivedParts.clone();
	}

	synchronized public boolean hasPart(int pieceIndex) {
		return receivedParts.get(pieceIndex);
	}

	/**
	 * Set all parts as received.
	 */
	public synchronized void setAllParts() {
		for (int i = 0; i < bitsetSize; i++) {
			receivedParts.set(i, true);
		}
		logger.debug("Received parts set to: " + receivedParts.toString());
	}

	public synchronized int getNumberOfReceivedParts() {
		return receivedParts.cardinality();
	}

	byte[] getPiece(int partId) {
		byte[] piece = destination.getPartAsByteArray(partId);
		return piece;
	}

	public void registerListener(FileHandlerListner listener) {
		listeners.add(listener);
	}

	public void splitFile() {
		destination.splitFile((int) pieceSize);
	}

	public byte[][] getAllPieces() {
		return destination.getAllPartsAsByteArrays();
	}

	public int getBitmapSize() {
		return bitsetSize;
	}

	private boolean isFileCompleted() {
		for (int i = 0; i < bitsetSize; i++) {
			if (!receivedParts.get(i)) {
				return false;
			}
		}
		return true;
	}
}

class Destination {
	/*public Destination(int peerId, String fileName) {
		// TODO Auto-generated constructor stub
	}*/

	final static Logger logger = Logger.getLogger(Destination.class);

	private final File _file=null;
    private final File  _partsDir=null;
    private static final String partsLocation = "files/parts/";

    /*public Destination(int peerId, String fileName){
        _partsDir = new File("./peer_" + peerId + "/" + partsLocation + fileName);
        _partsDir.mkdirs();
        _file = new File(_partsDir.getParent() + "/../" + fileName);
    }*/

    public byte[][] getAllPartsAsByteArrays(){
        File[] files = _partsDir.listFiles (new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
        });
        byte[][] ba = new byte[files.length][getPartAsByteArray(1).length];
        for (File file : files) {
            ba[Integer.parseInt(file.getName())] = getByteArrayFromFile(file);
        }
        return ba;
    }

    public byte[] getPartAsByteArray(int partId) {
        File file = new File(_partsDir.getAbsolutePath() + "/" + partId);
        return getByteArrayFromFile(file);
    }

    public void writeByteArrayAsFilePart(byte[] part, int partId){
        FileOutputStream fos;
        File ofile = new File(_partsDir.getAbsolutePath() + "/" + partId);
        try {
            fos = new FileOutputStream(ofile);
            fos.write(part);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            logger.warn(e);
        } catch (IOException e) {
            logger.warn(e);
        }
    }

    private byte[] getByteArrayFromFile(File file){
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] fileBytes = new byte[(int) file.length()];
            int bytesRead = fis.read(fileBytes, 0, (int) file.length());
            fis.close();
            assert (bytesRead == fileBytes.length);
            assert (bytesRead == (int) file.length());
            return fileBytes;
        } catch (FileNotFoundException e) {
        	logger.warn(e);
        } catch (IOException e) {
        	logger.warn(e);
        }
        finally {
            if (fis != null) {
                try {
                    fis.close();
                }
                catch (IOException ex) {}
            }
        }
        return null;
    }

    public void splitFile(int partSize){
        //SplitFile sf = new SplitFile();
        //sf.process(_file, partSize);
        logger.debug("File has been split");
    }

    public void mergeFile(int numParts) {
        File ofile = _file;
        FileOutputStream fos;
        FileInputStream fis;
        byte[] fileBytes;
        int bytesRead = 0;
        List<File> list = new ArrayList<>();
        for (int i = 0; i < numParts; i++) {
            list.add(new File(_partsDir.getPath() + "/" + i));
        }
        try {
            fos = new FileOutputStream(ofile);
            for (File file : list) {
                fis = new FileInputStream(file);
                fileBytes = new byte[(int) file.length()];
                bytesRead = fis.read(fileBytes, 0, (int) file.length());
                assert (bytesRead == fileBytes.length);
                assert (bytesRead == (int) file.length());
                fos.write(fileBytes);
                fos.flush();
                fileBytes = null;
                fis.close();
                fis = null;
            }
            fos.close();
            fos = null;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}


class RequestedParts {
	private final BitSet _requestedParts;
	private final long _timeoutInMillis;
	final static Logger logger = Logger.getLogger(RequestedParts.class);

	RequestedParts(int nParts, long unchokingInterval) {
		_requestedParts = new BitSet(nParts);
		_timeoutInMillis = unchokingInterval * 2;//why?
	}

	/**
	 * @param requestabableParts
	 * @return the ID of the part to request, if any, or a negative number in
	 *         case all the missing parts are already being requested or the
	 *         file is complete.
	 */
	synchronized int getPartToRequest(BitSet requestabableParts) {
		requestabableParts.andNot(_requestedParts);

		if (!requestabableParts.isEmpty()) {
			final int partId = CommonUtils.pickRandomSetIndexFromBitSet(requestabableParts);
			_requestedParts.set(partId);

			// Make the part requestable again in _timeoutInMillis
			new java.util.Timer().schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					synchronized (_requestedParts) {
						_requestedParts.clear(partId);
						logger.debug("clearing requested parts for pert " + partId);
					}
				}
			}, _timeoutInMillis);
			return partId;
		}
		return -1;
	}
}
