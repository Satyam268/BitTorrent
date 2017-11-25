package com.peer.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.peer.PeerHandler;

public class SplitFile {
	final static Logger logger = Logger.getLogger(SplitFile.class);
	
	public void process(File inputFile, int pieceSize){

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

    public static void main(String[] args) {
    	String filePath = args[0];
    	int pieceSize = Integer.parseInt(args[1]);   	
        SplitFile sf = new SplitFile();
        sf.process(new File(filePath), pieceSize);
    }

}
