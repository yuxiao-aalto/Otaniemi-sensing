package edu.cmu.cs.cloudlet.vmproxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class ForwardingThread extends Thread {
	DatagramSocket udpSocket;
	int port;//local listening port
	String resourceName;
	int protocolIndex;
	private ArrayList<Resource>  subscriberList;//should use key-pair
	//private ArrayList<DatagramSocket> udpSocketList;
	private DatagramSocket sendSocket;
	
	public ForwardingThread(ArrayList<Resource> forwardingList) throws SocketException{
		port = 8080;
		
		protocolIndex = 0;//set to UDP first
		
		subscriberList = forwardingList;
		
		resourceName = forwardingList.get(0).description;
		
		//udpSocketList = new ArrayList<DatagramSocket>();
		
		sendSocket = new DatagramSocket();
		/*for (Resource subscriber : subscriberList){
			
			udpSocketList.add(new DatagramSocket());
		}*/
		
		
		
	}
	
	public int initUDPSocket(){
		boolean bPortAvailable = false;
		
		while (bPortAvailable == false){
			try {
				if (udpSocket == null)
					udpSocket = new DatagramSocket(port);
				udpSocket.setReuseAddress(true);
				bPortAvailable = true;
				
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
		
		return port;
		
	}
	
	public int getListeningPort(){
		return port;
	}
	
	public int getProtocolIndex(){
		return protocolIndex;
	}
	
	public void setListeningPort(int i){
		port = i;
	}
	
	
	public void run(){
		
		//initUDPSocket();
		System.out.println("proxy is listening for video on UDP port"+port);
		int packetCount = 0;
		
	    byte[] buffer = new byte[10240];

	    // Create a packet to receive data into the buffer
	    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	    
	    
	    // Now loop forever, waiting to receive packets
	    while (true) {
	        // Wait to receive a datagram
	    	try {
				udpSocket.receive(packet);
				
				
				for (Resource subscriber : subscriberList){
					packet.setPort(subscriber.port);
					packet.setAddress(InetAddress.getByName(subscriber.hostname));
					System.out.println(subscriber.hostname+":"+subscriber.port);
					sendSocket.send(packet);
				}
				
				packetCount++;
				System.out.println("count:"+packetCount);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    }
	}
	
	public void addToSubscriberList(){
		
	}
	
}
