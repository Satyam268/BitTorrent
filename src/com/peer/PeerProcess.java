package com.peer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

//import org.apache.log4j.Logger;

public class PeerProcess {

	PeerMeta myInfo;
    List<PeerMeta> peerMetaCfg;
    int NumberOfPreferredNeighbors;
    int UnchokingInterval;
    int OptimisticUnchokingInterval;
    String FileName;
    int FileSize;
    int PieceSize;

	//final static Logger logger = Logger.getLogger(PeerProcess.class);

    public PeerProcess(int peerId) {
        myInfo = new PeerMeta(peerId);
    }

    void establishTCPConnection() {
        for(int i=0;i<peerMetaCfg.size();i++) {
            if(peerMetaCfg.get(i).peerId!=myInfo.peerId) {
                //establish TCP connection
                System.out.println("Establishes connection with peerId "+ peerMetaCfg.get(i).peerId);
            }
            else {
                break;
            }
        }
    }

    void readPeerInfoFile() {
        String fileName = "src/com/peer/peerInfo.cfg";
        peerMetaCfg = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(x -> peerMetaCfg.add(new PeerMeta(x)));
            System.out.println(peerMetaCfg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void readCommonCFGFile() {
        String fileName = "src/com/peer/common.cfg";
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

            Iterator<String> it = stream.iterator();
            NumberOfPreferredNeighbors = Integer.parseInt(it.next().split(" ")[1]);
            UnchokingInterval = Integer.parseInt(it.next().split(" ")[1]);
            OptimisticUnchokingInterval = Integer.parseInt(it.next().split(" ")[1]);
            FileName = it.next().split(" ")[1];
            FileSize = Integer.parseInt(it.next().split(" ")[1]);
            PieceSize = Integer.parseInt(it.next().split(" ")[1]);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(){

    	 PeerProcess me = new PeerProcess(1004);
         me.readPeerInfoFile();
         me.establishTCPConnection();
         me.readCommonCFGFile();
    }
}
