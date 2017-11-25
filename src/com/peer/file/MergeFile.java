package com.peer.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.peer.PeerHandler;

public class MergeFile {
	final static Logger logger = Logger.getLogger(PeerHandler.class);
	private final String FILE_NAME = "ImageFile.jpg";

    public void mergeFiles(){
        File outputFile = new File("ImageFile2.jpg");
        FileOutputStream fos;
        FileInputStream fis;
        byte[] fileBytes;
        int bytesRead = 0;
        List<File> list = new ArrayList<>();
        for (int i = 0; i < 211; i++) {
            list.add(new File("pieces/" + FILE_NAME + ".piece" + i));
        }
        try {
            fos = new FileOutputStream(outputFile, true);
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
        } catch (Exception e) {
            logger.warn(e);
        }
    }
}
