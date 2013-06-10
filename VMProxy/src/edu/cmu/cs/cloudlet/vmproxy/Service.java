package edu.cmu.cs.cloudlet.vmproxy;

import java.util.ArrayList;
import java.util.List;
import edu.cmu.cs.cloudlet.vmproxy.Resource;

public class Service {
	String name;
	String hostname;//IP address or hostname
	int listeningPort;
	int protocolIndex;
	boolean bPrivate;
	ArrayList<Resource> resourceList;
	
	public Service(String str0, String str1, int int0, int int1){
		name = str0;
		hostname = str1;
		listeningPort = int0;
		protocolIndex = int1;
		bPrivate = false;
		resourceList = new ArrayList<Resource>();
	}
	
	public void addToResourceList(Resource resource){
		resourceList.add(resource);
	}
}

