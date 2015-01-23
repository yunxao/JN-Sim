/////////////////////////////////////////////////////////////////////
// @(#)NetUtil.java
//
// (c) 2002, Infonet Group, University of Namur, Belgium
//
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
// @lastdate 15/07/2002
/////////////////////////////////////////////////////////////////////

package infonet.javasim.util;

import drcl.comp.Port;
import drcl.comp.Wire;
import drcl.data.BitSet;
import drcl.inet.Link;
import drcl.inet.Node;
import drcl.inet.data.RTEntry;
import drcl.inet.data.RTKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class NetUtil
{

    // ---- NetUtil.setupRoute ----------------------------------- //
    /** Setup a route between two directly connected nodes.
     */
    static public void setupRoute(Node sourceNode, Node destNode)
    {
	// List of links connected to the source node
	HashMap sourceLinks= new HashMap();
	// Interface index to find
	int interfaceIndex= -1;

	//System.out.println("setupRoute: "+sourceNode+" -> "+destNode);

	// ---------------------------------------------------------
	// Find links connected to the source node and save the port
	// connected to each link.
	// ---------------------------------------------------------
	//System.out.println("Wires:");
	Wire [] sourceWires= sourceNode.getAllWiresInsideOut();
	for (int wireIndex= 0; wireIndex < sourceWires.length;
	     wireIndex++) {
	    Wire wire= sourceWires[wireIndex];
	    /*System.out.println("\twire("+wireIndex+") "+
	      wire);*/
	    Port [] wirePorts= wire.getOutPorts();
	    for (int portIndex= 0; portIndex < wirePorts.length;
		 portIndex++) {
		Port port= wirePorts[portIndex];
		//System.out.println("\t\tport("+portIndex+") "+port);
		if ((port.host instanceof Link) &&
		    (!sourceLinks.containsKey(port.host))) {
		    //System.out.println("assoc "+port.host+" <-> "+wire);
		    sourceLinks.put(port.host, wire);
		}
	    }
	}

	// --------------------------------------------------------
	// For each link connected to the source node, find the one
	// connected to the destination node. The ID of the port
	// connected to the link on the source node.
	// --------------------------------------------------------
	//System.out.println("Links:");
	Iterator linkIterator= sourceLinks.keySet().iterator();
	for (;linkIterator.hasNext();) {
	    Link link= (Link) linkIterator.next();
	    //System.out.println("\tlink: "+link);
	    Wire [] linkWires= link.getAllWiresInsideOut();
	    for (int wireIndex= 0; wireIndex < linkWires.length; wireIndex++) {
		Wire wire= linkWires[wireIndex];
		//System.out.println("\t\twire("+wireIndex+") "+wire);
		Port [] wirePorts= wire.getPorts();
		for (int portIndex= 0; portIndex < wirePorts.length;
		     portIndex++) {
		    Port port= wirePorts[portIndex];
		    /*System.out.println("\t\t\tport("+portIndex+") "+
		      port+", "+port.host);*/
		    if (port.host == destNode) {
			Port [] inPorts=
			    ((Wire) sourceLinks.get(link)).getInPorts();
			for (int i= 0; i < inPorts.length; i++) {
			    if (inPorts[i].host == sourceNode) {
				interfaceIndex=
				    Integer.parseInt(inPorts[i].getID());
				break;
			    }
			}
		    }
		}
	    }
	}

	// --------------------------------------------------------
	// If the interface index has been found, set up the route.
	// --------------------------------------------------------
	//System.out.println("Interface:");
	if (interfaceIndex >= 0) {
	    //System.out.println("\tindex: "+interfaceIndex);
	    // Add entry in routing-table
	    RTKey key= new RTKey(0, 0,
				 destNode.getDefaultAddress(), -1,
				 0, 0);

	    /*
	    RTKey key= new RTKey(sourceNode.getDefaultAddress(), -1,
				 destNode.getDefaultAddress(), -1,
				 0, 0);
	    */

	    BitSet bitset= new BitSet(sourceLinks.size());
	    bitset.set(interfaceIndex);
	    //System.out.println("bitset: "+bitset);
	    //System.out.println("rt-entry: "+new RTEntry(bitset, null));
	    sourceNode.addRTEntry(key, new RTEntry(bitset, null), -1.0);
	} else {
	    System.out.println("Could not set up route from "+sourceNode+
			       " to "+destNode);
	}

    }

    // ---- NetUtil.setupBRoute ---------------------------------- //
    /** Setup a bidiretional route between two directly connected
	nodes.
    */
    static public void setupBRoute(Node sourceNode, Node destNode)
    {
	setupRoute(sourceNode, destNode);
	setupRoute(destNode, sourceNode);
    }

}
