package com.example.streamVM;

import com.example.streamVM.StreamReceiver;

public class StreamTest {
	public static void main(String args[]){
			int port = 8081;
			int protocolIndex = 0;
			int maxReceivedSize = -1;//unit: KByte. -1 means no limit
			
			if (args.length == 2)
				port = Integer.parseInt(args[0]);
			else if (args.length == 3){
				port = Integer.parseInt(args[0]);
				protocolIndex = Integer.parseInt(args[1]);//0:UDP, 1:TCP, 2:RTPUDP
			}
			else if (args.length == 4){
				port = Integer.parseInt(args[0]);
				protocolIndex = Integer.parseInt(args[1]);//0:UDP, 1:TCP, 2:RTPUDP
				maxReceivedSize = Integer.parseInt(args[2]);//the thread will exit after receiving enough data
			}
			
			System.out.println("port:"+port);
			System.out.println("protocolIndex:"+protocolIndex);
			System.out.println("maxReceivedSize:"+maxReceivedSize);
			
			StreamReceiver receiver = new StreamReceiver();
			receiver.init(port, protocolIndex, maxReceivedSize);
			receiver.start();
	}
}
