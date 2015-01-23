package tid.graphic;

import infonet.javasim.util.IPaddress;

import java.util.Hashtable;

public class GraphicEnviroment {
	/**
	 * Hash table with all networks
	 * Key = mask of the net in {@link infonet.javasim.util.IPaddress IPaddres} format
	 */
	protected Hashtable <IPaddress,GraphicNetwork> networks;
	/**
	 * Hash table with all nodes the key is the name of the router
	 */
	protected Hashtable<String, GraphicNode> nodes;
	/**
	 * Hash table with all layers, the key is the id of the layer 
	 */
	protected Hashtable <String, GraphicLayer> layers;
	public GraphicEnviroment (){
		networks = new Hashtable<IPaddress, GraphicNetwork>();
		nodes = new Hashtable<String, GraphicNode>();
		layers = new Hashtable<String, GraphicLayer>();
	}
	public void reset(){
		networks.clear();
		nodes.clear();
		layers.clear();
	}

}
