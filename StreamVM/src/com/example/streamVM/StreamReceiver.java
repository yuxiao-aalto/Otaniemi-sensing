package com.example.streamVM;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class StreamReceiver extends Thread {
		int listeningPort;
		int protocolIndex;
		DatagramSocket udpSocket;
		int maxReceivedSize;//maxReceivedSize is in Byte
		boolean bNoLimit;
		
		public int init(int port,int protocol, int maxSize){
			boolean bPortAvailable = false;
			maxReceivedSize = maxSize*1024;
			
			if (maxReceivedSize < 0){
				bNoLimit = true;
				System.out.println("no size limit");
			}
			else
			{	
				bNoLimit = false;
			}
			
			if (protocol == 0){
				while (bPortAvailable == false){
					try {
						if (udpSocket == null)
							udpSocket = new DatagramSocket(port);
						
						udpSocket.setReuseAddress(true);
						bPortAvailable = true;
						listeningPort = port;
							
						System.out.println("listening on UDP port "+ port);
						return listeningPort;
						
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						if (udpSocket != null)
							udpSocket.close();
						
						udpSocket = null;
						
						port += 1;
						bPortAvailable = false;
						
						e.printStackTrace();
					}
				}
			}
			
			
			
			return listeningPort;
		}
	
		public void run(){
			 byte[] buffer = new byte[10240];
			 int receivedSize = 0;
			 
			 // Create a packet to receive data into the buffer
			 DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

			 // Now loop forever, waiting to receive packets
			 while (bNoLimit || (receivedSize < maxReceivedSize)) {
			        // Wait to receive a datagram
			    	try {
						udpSocket.receive(packet);
						receivedSize += packet.getLength();
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			        System.out.println("received in total"+receivedSize);
			}
			 
			System.out.println("received in total"+receivedSize);
			
			
		}
}
