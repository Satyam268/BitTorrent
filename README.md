# BitTorrent #

A peer to peer, java based file transfer application.

### What is this repository for? ###

BitTorrent is a popular P2P protocol for file distribution. We have focused on
implementing the choking-unchoking mechanism which is one of the most important features of BitTorrent. 
The protocol implemented has been modified a little bit from the original BitTorrent protocol.
Details of the protocol can be found in the project details folder of the project.

### How do I get set up? ###
1. Clone the project in your ide.
2. Setup Peerinfo and config files (in src/peer/configFiles folder)
	a. If you have a group of peers, write their details in the peerInfo file (We are statically 	 	   setting information about peers' IP address and their respective port numbers in advance).
	   Ex: Space separated are:
		  1. PeerId(can be any integer you select) 
		  2. IP address on which this peerId will be running.
		  3. The port number on which this peer is listening.(other peers use this IP address and 	         port numbers for handshaking and packet exchange)
		  4. This is an integer which conveys if the peer has the entire file (Atleast one peer 				 			 should have an entire file).
	
	Example Config file looks something this:--
	
	```	 
	1001 192.168.0.6 6008 1\n
	1002 192.168.0.13 6008 0\n
	1003 192.168.0.13 6009 0\n
	```
	
	b. Common.cfg file where general information about the system can be changed.
	
	```
	NumberOfPreferredNeighbors 0
	UnchokingInterval 10
	OptimisticUnchokingInterval 15
	FileName src\com\peer\configFiles\TheData.dat
	FileSize 1188528
	PieceSize 100000
	```  
### Owners ###
Satyam Sinha
Yash Jain

### References ###

