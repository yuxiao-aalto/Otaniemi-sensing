package com.example.streamclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.SystemClock;
import android.util.Log;

import com.example.streamclient.RtpSocket;

public class StreamingThread extends Thread {
	static final int BUFFER_SIZE = 10240;
	
	private InetAddress remoteAddr;
	private int remotePort;
	private InputStream videoStream;
	private int protocolIndex;
	private LocalSocket localReceiver;
	private LocalServerSocket localLoop;
	
	
	//initialize streamingThread. data will be sent to address:port
	public StreamingThread(int protocol, String address, int port){
		setProtocolIndex(protocol);
		remotePort = port;
		
		try {
			remoteAddr = InetAddress.getByName(address);
			//InetAddress.getByAddress does not work well
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println("connect to:"+remoteAddr.toString()+":"+remotePort);
		
		try {
			localLoop = new LocalServerSocket("videoserver");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void startUDPStreaming() throws IOException{
		
		DatagramSocket udpSocket;
		DatagramPacket packet;
		byte[] buffer = new byte[BUFFER_SIZE];
		int bytes_read = 0;
		int bytes_count = 0;
		int failCount = 0;
		
		udpSocket = new DatagramSocket();
		udpSocket.setReceiveBufferSize(10240);
		udpSocket.setSendBufferSize(10240);
		//all the packets sent through udpSocket will be sent to remoteAddr, remotePort
		udpSocket.connect(remoteAddr, remotePort);
		
		packet = new DatagramPacket(buffer, bytes_read);
		
		while (videoStream != null){
			
		    try {
		    	bytes_read = videoStream.read(buffer, 0, BUFFER_SIZE);
				System.out.println(bytes_read);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    if (bytes_read > 0){
		    	  
		    	packet.setLength(bytes_read);
			            
			    try {
					udpSocket.send(packet);		
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			   
			    
			} else {
			    //in case CameraRecording is stopped first, then bytes_read will be -1.
			    //buflength cannot be -1 when initializing datagram packet
			    System.out.println("bytes_read is -1");
			    failCount++;	
				if (failCount > 10){
					stopStreaming();
					udpSocket.close();	
				}
				
				try {
					sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
					
			}
		    
		    bytes_count += bytes_read;
		    Log.v("streaming", bytes_count+"bytes have been sent");
		}
		
		
	}
	
	//refer to The Sipdroid Open Source Project
	public void startRTPStreaming(){
		RtpSocket rtpSocket = null;
		int frame_size = 1400;
		byte[] buffer = new byte[frame_size + 14];
		buffer[12] = 4;
		RtpPacket rtp_packet = new RtpPacket(buffer, 0);
		int seqn = 0;
		int num, number = 0, src, dest, len = 0, head = 0, lasthead = 0, lasthead2 = 0, cnt = 0, stable = 0;
		long now, lasttime = 0;
		double avgrate = 45000;
		double avglen = avgrate / 20;
		int fps = -1;
		boolean change = false;
		
		try {
			rtpSocket = new RtpSocket();
			rtpSocket.setReceiveBufferSize(10240);
			rtpSocket.setSendBufferSize(10240);
			rtpSocket.connect(remoteAddr, remotePort);
		} catch (SocketException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		rtp_packet.setPayloadType(34);
		
		
		while ((videoStream != null) && (rtpSocket != null)) {
			num = -1;
			try {
				num = videoStream.read(buffer, 14 + number, frame_size - number);
			} catch (IOException e) {
				e.printStackTrace();
				break; 
			}
			
			if (num < 0) {
				try {
					sleep(20);
				} catch (InterruptedException e) {
					break;
				}
				continue;
			}

			number += num;
			head += num;

			try {
				
				now = SystemClock.elapsedRealtime();
				
				if (lasthead != head + videoStream.available() && ++stable >= 5
						&& now - lasttime > 700) {
					
					System.out.println("video available:"+videoStream.available());
					
					if (cnt != 0 && len != 0)
						avglen = len / cnt;
					
					if (lasttime != 0) {
						fps = (int) ((double) cnt * 1000 / (now - lasttime));
						Log.v("fps", Integer.toString(fps));
						avgrate = (double) ((head + videoStream.available()) - lasthead2)
								* 1000 / (now - lasttime);
					}
					
					lasttime = now;
					lasthead = head + videoStream.available();
					lasthead2 = head;
					len = cnt = stable = 0;
				}
				
			} catch (IOException e1) {
				e1.printStackTrace();
				break;
			}
			
			// in h264 search for NAL units here
			for (num = 14; num <= 14 + number - 2; num++)
				if (buffer[num] == 0 && buffer[num + 1] == 0)
					break;
			if (num > 14 + number - 2) {
				num = 0;
				rtp_packet.setMarker(false);
			} else {
				num = 14 + number - num;
				rtp_packet.setMarker(true);
			}
			rtp_packet.setSequenceNumber(seqn++);
			rtp_packet.setPayloadLength(number - num + 2);
			
			if (seqn > 10)
				try {
					//Log.v("S", Integer.toString(rtp_packet.getLength()));
					rtpSocket.send(rtp_packet);
					len += number - num;
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}

			if (num > 0) {
				num -= 2;
				dest = 14;
				src = 14 + number - num;
				if (num > 0 && buffer[src] == 0) {
					src++;
					num--;
				}
				number = num;
				while (num-- > 0)
					buffer[dest++] = buffer[src++];
				buffer[12] = 4;

				cnt++;
				try {
					if (avgrate != 0)
						Thread.sleep((int) (avglen / avgrate * 1000));
				} catch (Exception e) {
					break;
				}
				rtp_packet.setTimestamp(SystemClock.elapsedRealtime() * 90);
			} else {
				number = 0;
				buffer[12] = 0;
			}
			
			if (change) {
				change = false;
				long time = SystemClock.elapsedRealtime();

				try {
					while (videoStream.read(buffer, 14, frame_size) > 0
							&& SystemClock.elapsedRealtime() - time < 3000)
						;
				} catch (Exception e) {
				}
				number = 0;
				buffer[12] = 0;
			}
		}//while
		
		if (rtpSocket != null)
			rtpSocket.close();
		
		if (videoStream != null){
			try {
				while (videoStream.read(buffer, 0, frame_size) > 0)
					;
			} catch (IOException e) {
			} 
			videoStream = null;
		}
		
	}
	
	
	public void run(){
		
		try {
			//wait for CameraRecording to connect
			localReceiver = localLoop.accept();
			
			if (localReceiver != null)
				videoStream = localReceiver.getInputStream();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (protocolIndex == 0)
			try {
				startUDPStreaming();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else if (protocolIndex == 2)
			startRTPStreaming();
		
		
	}
	
	public boolean stopStreaming() throws IOException{
		System.out.println("stop streaming");
		
		if (videoStream != null)
			videoStream.close();
		
		if (localReceiver != null)
			localReceiver.close();
		
		if (localLoop != null)
			localLoop.close();
		
		videoStream = null;
		localReceiver = null;
		localLoop = null;
		
		return true;
	}

	public int getProtocolIndex() {
		return protocolIndex;
	}

	public void setProtocolIndex(int protocolIndex) {
		this.protocolIndex = protocolIndex;
	}
}
