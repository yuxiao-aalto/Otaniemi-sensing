package edu.cmu.cs.cloudlet.vmproxy;

public class Resource {
	String description;
	int id;
	String hostname;
	int port;
	int protocolIndex;
	
	public Resource(String description0, int resourceID, String url, int portNumber, int protocol){
		description = description0;
		id = resourceID;
		hostname = url;
		port = portNumber;
		protocolIndex = protocol;
	}
}
