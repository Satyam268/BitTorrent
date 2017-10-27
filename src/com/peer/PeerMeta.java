package com.peer;


public class PeerMeta {
    int peerId;
    String hostName;
    String listeningPort;
    int hasFile;

    public PeerMeta(String line){
        String[] metaInfo = line.split(" ");
        if(metaInfo.length!=4) {
        }

        peerId = Integer.parseInt(metaInfo[0]);
        hostName = metaInfo[1];
        listeningPort = metaInfo[2];
        hasFile = Integer.parseInt(metaInfo[3]);
    }

    public PeerMeta(int peerId) {
        this.peerId = peerId;
    }
    @Override
    public String toString() {
        return peerId+ " "+hostName+" "+listeningPort+" "+hasFile;
    }
}
