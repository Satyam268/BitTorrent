package com.peer.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class FileOperations {

	private final File file;
	private final File pieceDir;
	private int peerId;
	private static final String piecesLocation = "files/pieces/";
	final static Logger logger = Logger.getLogger(FileOperations.class);

	public FileOperations(int peerId, String fileName) {
		this.peerId = peerId;
		pieceDir = new File("./peer_" + peerId + "/" + piecesLocation + fileName);
		pieceDir.mkdirs();
		file = new File(pieceDir.getParent() + "/../" + fileName);
	}

	public byte[][] getAllpiecesAsByteArrays() {
		File[] files = pieceDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
		});
		byte[][] ba = new byte[files.length][getPieceFromFile(1).length];
		for (File file : files) {
			ba[Integer.parseInt(file.getName())] = getByteArrayFromFile(file);
		}
		return ba;
	}

	public byte[] getPieceFromFile(int pieceId) {
		File file = new File(pieceDir.getAbsolutePath() + "/" + pieceId);
		return getByteArrayFromFile(file);
	}

	public void writePieceToFile(byte[] piece, int pieceId) {
		FileOutputStream fos;
		File ofile = new File(pieceDir.getAbsolutePath() + "/" + pieceId);
		try {
			fos = new FileOutputStream(ofile);
			fos.write(piece);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			logger.warn("Unable to write piece_ " + pieceId + " from peer_" + peerId + " " + e);
		}
	}

	private byte[] getByteArrayFromFile(File file) {
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
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ex) {
				}
			}
		}
		return null;
	}

	// Common Methods for Splitting and merging file
	public static void processFileIntoPieceFiles(File inputFile, int pieceSize){
        FileInputStream inputStream;
        String newFileName;
        FileOutputStream filePart;
        int fileSize = (int) inputFile.length();
        int nChunks = 0, read = 0, readLength = pieceSize;
        byte[] byteChunkPart;
        try {
            inputStream = new FileInputStream(inputFile);
            while (fileSize > 0) {
                if (fileSize <= 5) {
                    readLength = fileSize;
                }
                byteChunkPart = new byte[readLength];
                read = inputStream.read(byteChunkPart, 0, readLength);
                fileSize -= read;
                assert (read == byteChunkPart.length);
                nChunks++;
                newFileName = inputFile.getParent() + "/pieces/" +
                        inputFile.getName() + "/" + Integer.toString(nChunks - 1);
                filePart = new FileOutputStream(new File(newFileName));
                filePart.write(byteChunkPart);
                filePart.flush();
                filePart.close();
                byteChunkPart = null;
                filePart = null;
            }
            inputStream.close();
        } catch (IOException e) {
            logger.warn(e);
        }
    }
	

	public void mergeFile(int numpieces) {
		File ofile = file;
		FileOutputStream fos;
		FileInputStream fis;
		byte[] fileBytes;
		int bytesRead = 0;
		List<File> list = new ArrayList<>();
		for (int i = 0; i < numpieces; i++) {
			list.add(new File(pieceDir.getPath() + "/" + i));
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
