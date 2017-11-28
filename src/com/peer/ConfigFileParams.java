package com.peer;

public class ConfigFileParams {
	int peerId;
	String hostName;
    int listeningPort;
    int hasFile;
    
    public ConfigFileParams(String line) {
    	String[] metaInfo = line.split(" ");
        if(metaInfo.length!=4) {
        }
    	peerId = Integer.parseInt(metaInfo[0]);
        hostName = metaInfo[1];
        listeningPort = Integer.parseInt(metaInfo[2]);
        hasFile = Integer.parseInt(metaInfo[3]);
    }

	public int getPeerId() {
		return peerId;
	}

	public void setPeerId(int peerId) {
		this.peerId = peerId;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getListeningPort() {
		return listeningPort;
	}

	public void setListeningPort(int listeningPort) {
		this.listeningPort = listeningPort;
	}

	public int getHasFile() {
		return hasFile;
	}

	public void setHasFile(int hasFile) {
		this.hasFile = hasFile;
	}
}
