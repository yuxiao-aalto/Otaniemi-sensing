package edu.cmu.cs.cloudlet.vmproxy;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ControlChannel extends Thread {
	private ServerSocket ctlChannelSocket;
	private Socket listeningSocket;
	private int ctlChannelPort;
	private static final int TCP_LISTENING_PORT = 5000;
	//private ArrayList<Service> serviceList;
	private ArrayList<Resource> resourceList;
	
	
	public ControlChannel(ArrayList<Resource> receiver){
		ctlChannelSocket = null;
		listeningSocket = null;
		ctlChannelPort = TCP_LISTENING_PORT;
		//serviceList = null;
		//serviceList = new ArrayList<Service>();
		resourceList = receiver;
		
		
	}
	
	private int initControlChannel() throws SocketException{
		boolean bPortAvailable = false;
		
		while (bPortAvailable == false){
			try {
				if (ctlChannelSocket == null)
					ctlChannelSocket = new ServerSocket(ctlChannelPort);	
				
				bPortAvailable = true;
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println(ctlChannelPort+"is occupied");
				bPortAvailable = false;
				
				ctlChannelPort += 1;//try the next port
				
				if (ctlChannelSocket != null){
					try {
						ctlChannelSocket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
				ctlChannelSocket = null;
				e.printStackTrace();
			}
			
			if (ctlChannelSocket != null)
				ctlChannelSocket.setReuseAddress(true);
			
		}
		
		
		return ctlChannelPort;
	}
	
	/*private void createTestCase(){
		//for testing
		Service streamingService = new Service("streaming","128.2.213.18", getCtlChannelPort()+1, 0);
		Resource e = new Resource("video", 0, "localhost", 9002, 0);
		streamingService.addToResourceList(e);
		serviceList.add(streamingService);
	}*/
	
	public void run(){
		
		try {
			initControlChannel();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("listening on tcp port"+ getCtlChannelPort());
		
		//createTestCase();
		
		if (ctlChannelSocket != null){
			
			try{
				listeningSocket = ctlChannelSocket.accept();
				
				BufferedReader receivedData = new BufferedReader(new InputStreamReader(listeningSocket.getInputStream()));
			    DataOutputStream toSendData = new DataOutputStream(listeningSocket.getOutputStream());
			    
			    String receivedLine;
			    
				while ((listeningSocket != null) && (listeningSocket.isConnected())){
					
					receivedLine = receivedData.readLine();
				    
					if (receivedLine != null){
						System.out.println("Received: " + receivedLine);
				    
						toSendData.writeBytes(handleRequest(receivedLine));
						toSendData.flush();
					}
				}
				
				listeningSocket.close();
				ctlChannelSocket.close();
			}catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
		
		}//if
		
		
	}



	public int getCtlChannelPort() {
		return ctlChannelPort;
	}



	public void setCtlChannelPort(int ctlChannelPort) {
		this.ctlChannelPort = ctlChannelPort;
	}
	
	
	public String handleRequest(String input){
		String serviceName = "";
		String resourceName = "";
		String output="";
		
		System.out.println("handling "+input);
		
		if (input != null){
			StringTokenizer tokens = new StringTokenizer(input);
			
			if ((tokens != null) && tokens.hasMoreTokens()){
				if ((tokens.nextToken().compareToIgnoreCase("QUERY") == 0) && tokens.hasMoreTokens()){
					
					serviceName = tokens.nextToken();
					
					if (tokens.hasMoreTokens())
						resourceName = tokens.nextToken();
					
					System.out.println("resourceName "+resourceName);
					
					//for (Service service : serviceList){
						//if (service.name.compareTo(serviceName) == 0){
							//for (Resource resource : service.resourceList){
							for (Resource resource : resourceList){
								if (resource.description.compareToIgnoreCase(resourceName) == 0){
									output = serviceName+" "+ resourceName+ " " + resource.hostname +" " + Integer.toString(resource.port)+"\r\n";
									System.out.println("output:"+output);
									return output;
								}
							}
						//}
					//}//for service
					
				}
					
			}
		}
		
		
		return output;
	}
	
	//register new service
	private void addToServiceList(){
		
	}
	
}
