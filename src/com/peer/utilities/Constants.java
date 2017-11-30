package com.peer.utilities;

import java.nio.file.Paths;

public class Constants {
	public final static String commonConfigFile = "src/com/peer/configFiles/Common.cfg";
	public final static String peerInfoFile = "src/com/peer/configFiles/PeerInfo.cfg";
	public final static String log4jConfPath = "log4j.properties";
	public final static int oun_count = 1;
	public final static String pieceStore = "pieceStore";
	public final static String outputFileLocation = Paths.get("com", "output", "ThData.dat").toString();
}
