package com.example.streamclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import android.util.Log;

public class ControlChannel {
	private InetAddress cloudletAddr;
	private String cloudletIPAddr;
	private int remoteTCPPort;
	private Socket tcpSocket;
	private InputStreamReader ctlMsgInStream;
	private BufferedReader reader;
	private OutputStream ctlMsgOutStream;
	private OutputStreamWriter outputWriter;
	private static String rtn = "\r\n";

	
	public ControlChannel(){
		tcpSocket = null;
		ctlMsgInStream = null;
		ctlMsgOutStream = null;
		remoteTCPPort = 5000;//set a default value
		cloudletIPAddr = "128.2.213.18";//set a default IP
		
		try {	
			cloudletAddr = InetAddress.getByName(cloudletIPAddr);
			//cloudletAddr = InetAddress.getByAddress(new byte[]{(byte) 128, 2, (byte) 213, 18});
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			Log.v("ctl", "unknown host");
			e.printStackTrace();
		} 
	}
	
	//url:remote hostname or IP address. port: remote TCP port to connect to
	public ControlChannel(String url, int port){
		
		tcpSocket = null;
		ctlMsgInStream = null;
		ctlMsgOutStream = null;
		cloudletIPAddr = url;
		remoteTCPPort = port;
		
		try {	
			
			cloudletAddr = InetAddress.getByName(cloudletIPAddr);
			//cloudletAddr = InetAddress.getByAddress(new byte[]{(byte) 128, 2, (byte) 213, 18});
		
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			Log.v("ctl", "unknown host");
			e.printStackTrace();
		} 
		
		
	}
	
	public void setTCPPort(int port){
		remoteTCPPort = port;
	}
	
	public void setIPAddr(String url){
		
		cloudletIPAddr = url;
		
	}
	
	public boolean isConnected(){
		if (null == tcpSocket)
			return false;
		
		return tcpSocket.isConnected();
	}
	
	public boolean initCtlChannel(){
		
		if (cloudletAddr == null){
			Log.v("socket","cloudletAddr is null");
			return false;
		}
		
		if (null == tcpSocket){
			
			try {
				
				tcpSocket = new Socket(cloudletAddr, remoteTCPPort);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v("socket", "new socket fails");
			}
			
			if (null == tcpSocket)
				return false;
			

			try {
				tcpSocket.setKeepAlive(true);
				
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				Log.v("socket", "failed to set keep alive");
				e.printStackTrace();
				return false;
			}
			
			
			try {
				ctlMsgInStream = new InputStreamReader(tcpSocket.getInputStream());
				ctlMsgOutStream = tcpSocket.getOutputStream();
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return false;
			}
			
			reader = new BufferedReader(ctlMsgInStream);
			outputWriter = new OutputStreamWriter(ctlMsgOutStream);
			
		}
		
		
		return tcpSocket.isConnected();
		
	}
	
	public void closeCtlChannel() throws IOException{
		System.out.println("close ctl channel");
		
		if (outputWriter != null)
			outputWriter.close();
		
		outputWriter = null;
		
		try {
			if (reader != null)
				reader.close();
			
			reader = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (ctlMsgInStream != null)
			ctlMsgInStream.close();
		
		if (ctlMsgOutStream != null)
			ctlMsgOutStream.close();
		
		if (tcpSocket != null)
			tcpSocket.close();
		
		
		if (tcpSocket != null)
			System.out.println("tcpsocket close");
		
		tcpSocket = null;
		ctlMsgInStream = null;
		ctlMsgOutStream = null;
		
	}
	
	
	//The service running on remote server is listening on port X. 
	//This method returns the port X. The video streams will be then sent to port X.
	public int getRemoteServicePort(String serviceName, String resourceName){
		
		int port = -1;
		
		if (outputWriter == null)
			return port;
		
		try {
			outputWriter.write("QUERY "+ serviceName + " "+ resourceName + rtn);
			outputWriter.flush();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		System.out.println("QUERY "+ serviceName + " "+ resourceName+rtn);
		
		//PrintWriter does not work
		/*PrintWriter writer = new PrintWriter(ctlMsgOutStream);
		
		if (writer != null){
			writer.println("QUERY "+ serviceName + " "+ resourceName + rtn);
			ctlMsgOutStream.flush();
			System.out.println("QUERY "+ serviceName + " "+ resourceName+rtn);
		}*/
		
		
		try {
			String newLine = reader.readLine();
			System.out.println("receive: "+newLine);
			
			if (newLine != null){
				StringTokenizer tokens = new StringTokenizer(newLine);
				
				if ((tokens != null) && tokens.countTokens() == 4){
					if (tokens.nextToken().compareToIgnoreCase(serviceName) == 0){
						if (tokens.nextToken().compareToIgnoreCase(resourceName) == 0){
							tokens.nextToken();
							port = Integer.parseInt(tokens.nextToken());
						}
					}
						
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return port;

	}
	
}
