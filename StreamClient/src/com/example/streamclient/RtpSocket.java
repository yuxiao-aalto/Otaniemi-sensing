//refer to RtpSocket.java and SipdroidSocket.java in The Sipdroid Open Source Project

package com.example.streamclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.util.Log;


public class RtpSocket extends DatagramSocket {
	DatagramPacket datagramPacket;
	InetAddress remoteAddress;
	int remotePort;
	
	public RtpSocket() throws SocketException{
		super();
		datagramPacket = new DatagramPacket(new byte[1], 1);
	}
	
	//binds it to aPort on the local host machine
	public RtpSocket(int aPort) throws SocketException {
		super(aPort);
		// TODO Auto-generated constructor stub
		datagramPacket = new DatagramPacket(new byte[1], 1);
	}
	
	public RtpSocket(int port, InetAddress laddr)throws SocketException{
		super(port, laddr);
		datagramPacket = new DatagramPacket(new byte[1], 1);
	}
	
	public void setRemoteAddress(InetAddress laddr){
		remoteAddress = laddr;
	}
	
	public void setRemotePort(int port){
		remotePort = port;
	}
	
	public InetAddress getRemoteAddress(){
		return remoteAddress;
	}
	
	public int getRemotePort(){
		return remotePort;
	}
	
	/** Receives a RTP packet from this socket */
	public void receive(RtpPacket rtpp) throws IOException {
		datagramPacket.setData(rtpp.packet);
		datagramPacket.setLength(rtpp.packet.length);
		this.receive(datagramPacket);
		if (!this.isConnected())
			this.connect(datagramPacket.getAddress(), datagramPacket.getPort());
		rtpp.packet_len = datagramPacket.getLength();
	}

	/** Sends a RTP packet from this socket */
	public void send(RtpPacket rtpp) throws IOException {
		datagramPacket.setData(rtpp.packet);
		datagramPacket.setLength(rtpp.packet_len);
		datagramPacket.setAddress(remoteAddress);
		datagramPacket.setPort(remotePort);

		if (!this.isConnected()) {
			Log.v("S",
					"not connected, connecting... to "
							+ datagramPacket.getAddress().toString() + ":"
							+ Integer.toString(datagramPacket.getPort()));
			this.connect(datagramPacket.getAddress(), datagramPacket.getPort());
		}
		this.send(datagramPacket);
	}


}
