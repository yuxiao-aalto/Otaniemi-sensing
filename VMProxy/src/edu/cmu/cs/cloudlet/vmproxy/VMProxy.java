package edu.cmu.cs.cloudlet.vmproxy;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Hashtable;

import edu.cmu.cs.cloudlet.vmproxy.ControlChannel;
import edu.cmu.cs.cloudlet.vmproxy.ForwardingThread;

public class VMProxy {
	private static ArrayList<Resource> resourceReceivers;
	private static ArrayList<Resource> videoReceivers;
	private static Hashtable<String, ArrayList<Resource>> ForwardingList;
	private static Hashtable<String, Thread> resourceToThreadMapping;
	
	//for test only
	public static void createTestCases(int port0, int port1, int port2){
		Resource r= new Resource("video", 0, "128.2.213.18", port0, 0);//proxy receives video at UDP videoport
		resourceReceivers.add(r);
		
		videoReceivers = new ArrayList<Resource>();
		Resource r1 = new Resource("video", 0, "128.2.213.18", port1, 0);
		Resource r2 = new Resource("video", 0, "128.2.213.18", port2, 0);
		videoReceivers.add(r1);
		videoReceivers.add(r2);
		ForwardingList.put("video", videoReceivers);
		
	}
	
	public static void main(String args[]){
		
		int videoPort = 8080;//port for receiving video stream
		
		if (args.length == 2){
			videoPort = Integer.parseInt(args[0]);
			System.out.println("listening for video stream at port: "+videoPort);
		} else if (args.length > 2){
			
			for (int i = 1; i < args.length -1; i++){
				System.out.println("forward video to port "+ args[i]);
				videoReceivers = new ArrayList<Resource>();
				videoReceivers.add(new Resource("video", 0, "localhost", Integer.parseInt(args[i]), 0));	
			}
			
		}
		
	
		ForwardingList = new Hashtable<String, ArrayList<Resource>>();
		
		ForwardingList.put("video", videoReceivers);
		//receive video at videoPort, and forward it to other VMs
		//createTestCases(videoPort, videoPort+1, videoPort+2);
		
		resourceReceivers = new ArrayList<Resource>();
		resourceToThreadMapping = new Hashtable<String, Thread>();
		
		try {
			
			ForwardingThread forward0;
			forward0 = new ForwardingThread(videoReceivers);
			videoPort = forward0.initUDPSocket();
			Resource r= new Resource("video", 0, "localhost", videoPort, 0);//proxy receives video at UDP videoport
			resourceReceivers.add(r);
			
			forward0.start();

			resourceToThreadMapping.put("video", forward0);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ControlChannel ctlMsgChannel = new ControlChannel(resourceReceivers);
		ctlMsgChannel.start();
		
		
			
	}
}
