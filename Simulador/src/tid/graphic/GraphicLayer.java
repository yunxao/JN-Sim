package tid.graphic;

import infonet.javasim.util.IPaddress;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.Popup;

public class GraphicLayer implements GraphicElements, Comparable<GraphicLayer>{
	/**
	 * Hashtable with the relation between a node and key is the id of the link 
	 */
	protected Hashtable<String,Vector<GraphicNode>> links;
	/**
	 * Hast table with status of the links, key is a id of the link, status can be: 
	 * {@link GraphicConstants#LINK_ACTIVE_STATUS}, 
	 * {@link GraphicConstants#LINK_DOWN_STATUS}, 
	 * {@link GraphicConstants#LINK_NORMAL_STATUS}, 
	 */
	protected Hashtable<String,Integer> linkStatus;
	/**
	 * Relation between a node and all nodes that's connected
	 */
	protected Hashtable<GraphicNode, Vector<String>> linksConnectedToANode;
	/**
	 * Hash table with the relation between the principal address for a node and this node. 
	 * Key is the principal address in {@link infonet.javasim.util.IPaddress IPAddres}  
	 */
	protected Hashtable <IPaddress,GraphicNode> nodes;
	public JPanel panel = null;
	String id;
//	private int state; 
	private GraphicInterpreter owner;
	private GraphicLayer self = this;
	private double visualRatio;
	private GraphicEventManager graphicEventManager;
	public GraphicLayer(String id,GraphicInterpreter owner_){
		this.id = id;
		this.owner = owner_;
		this.linksConnectedToANode = new Hashtable<GraphicNode, Vector<String>>();
		linkStatus = new Hashtable<String, Integer>();
//		this.state = GraphicConstants.LINK_NORMAL_COLOR;
		panel = new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -1480380783767421463L;
//			public void update (Graphics g)
//			{
//				paint (g);
//			}
			public void paint(Graphics g){
				
				Graphics2D g2 = (Graphics2D)g;
//				this.setSize(owner.principalFrame.getSize());
				if (GraphicConstants.debugEnable){
//					System.out.println("Printing the layer. Position="+panel.getLocation()+".Size="+panel.getSize());
				}
				
//				Enumeration<Vector<GraphicNode>> eLinks = links.elements();
				Enumeration <String> nameLinks = links.keys();
				while (nameLinks.hasMoreElements()){
					String nameLink = nameLinks.nextElement();
					Vector<GraphicNode> link = links.get(nameLink);
					
					if (link.size() == 2){
						Color c = g2.getColor();
						Stroke s = g2.getStroke();
						GraphicNode node0 = link.get(0);
						GraphicNode node1 = link.get(1);
						IPaddress net0 = node0.principalAddress.getMaskedIPaddress(owner.CIDR);
						IPaddress net1 = node1.principalAddress.getMaskedIPaddress(owner.CIDR);
						if (node0.getPainted() && node1.getPainted()){
							float stroke = (net0.masked_val()== net1.masked_val())?owner.reescalar(GraphicConstants.FINE_STROKE):owner.reescalar(GraphicConstants.GROSS_STROKE);
							Point pos0 = owner.reescalar(node0.getStandarCenterAbsolutePosition());
							Point pos1 = owner.reescalar(node1.getStandarCenterAbsolutePosition());
							g2.setStroke(new BasicStroke(stroke));
							g2.setColor(GraphicConstants.LINK_COLORS[linkStatus.get(nameLink)]);
							g2.drawLine(pos0.x, pos0.y, pos1.x, pos1.y);
							if (GraphicConstants.printLinksNames){
								Font f = g2.getFont();
								g2.setFont(new Font(f.getName(),f.getStyle(),owner.reescalar(f.getSize())));
	
								g2.setColor(GraphicConstants.NORMAL_COLOR_TEXT);
								g2.drawChars(nameLink.toCharArray(), 0, nameLink.length(), owner.reescalar((pos0.x+pos1.x)/2), owner.reescalar( (pos0.y+pos1.y)/2 ));
								g2.setFont(f);
							}
							g2.setColor(c);
							g2.setStroke(s);
						}
					}
				}

//				GraphicNode node = null;
//				Enumeration <GraphicNode> nodes = owner.enviroment.nodes.elements();
//				while (nodes.hasMoreElements()){
//					
//					GraphicNode antNode = node;
//					node = nodes.nextElement();
//					
//					
//					
//					
//					if (antNode != null){
//						Point position = node.getStandarCenterAbsolutePosition();
//						Point position2 = antNode.getStandarCenterAbsolutePosition();
//						System.out.println("Line from="+position+" To="+position2);
//						g.drawLine(owner.reescalar(position.x), owner.reescalar(position.y), owner.reescalar(position2.x), owner.reescalar(position2.y));
//						
//					}
////					http://spejman.blogspot.com/2006/11/aadir-usuarios-sudo.html
//				}
				
			}
			
		};
		links = new Hashtable<String, Vector<GraphicNode>>();
		nodes = new Hashtable<IPaddress, GraphicNode>();
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setNodeToLink(GraphicNode node, String linkName){
		if (!nodes.contains(node)){
			return;
		}
		if (!links.containsKey(linkName))
			links.put(linkName, new Vector<GraphicNode>());
		if (links.get(linkName).contains(node)){
			return;
		}
		linksConnectedToANode.get(node).add(linkName);
		links.get(linkName).add(node);
	}
	public String getLinkBetweenNodes(GraphicNode origin, GraphicNode destiny){
		Vector<String> linksOrigin = linksConnectedToANode.get(origin);
		Vector<String> linksDestiny = linksConnectedToANode.get(destiny);
		if (linksOrigin == null || linksDestiny == null){
			return null;
		}
		for (String linkOrigin: linksOrigin){
			for (String linkDestiny: linksDestiny)
				if (linkOrigin.equals(linkDestiny))
					return linkOrigin;
		}
		return null;
	}
	public boolean addNode(GraphicNode n,IPaddress address){
		
		if (!nodes.contains(n)){
			nodes.put(address,n);
			linksConnectedToANode.put(n, new Vector<String>());
		}
		n.addLayer(this.id);
		return true;
	}
	public boolean nodeIsInThisLayer(GraphicNode n){
		return nodes.contains(n);
	}
	/**
	 * Add a link name to the structure. If the link exist, it's not create 
	 * @param linkName
	 * @return true if the link has been created. False if exist the linkName in the structure 
	 */
	public boolean addLink(String linkName){
		if (!links.containsKey(linkName)){
			links.put(linkName, new Vector<GraphicNode>());
			linkStatus.put(linkName,GraphicConstants.LINK_NORMAL_STATUS);
			return true;
		}
		return false;
	}

	public Enumeration<GraphicNode> nodesOfTheLayer(){
		return this.nodes.elements(); 
	}
	public void setRatio(double visualRatio) {
		this.visualRatio = visualRatio; 
		this.panel.setSize(owner.centralPanel.getSize());
		this.panel.setLocation(0, 0);
		
	}
	
	public void calculatePosition() {
		this.panel.setSize(owner.centralPanel.getSize());
		this.panel.setLocation(0, 0);
	}
	@Override
	public Point getStandarPosition() {
		return new Point(0,0);
	}
	@Override
	public void painting() {
		this.panel.repaint();
	}
	@Override
	public void setStandarPosition(Point position) {
		if (GraphicConstants.debugEnable){
			System.out.println("Position of layer can be changed. Must be (0,0");
		}
		
	}
	@Override
	public String toHtmlString() {
		// TODO hacer
		return null;
	}
	@Override
	public int compareTo(GraphicLayer o) {
		return this.id.compareTo(o.id);
	}
	public void setGraphicEventManager(GraphicEventManager gem) {
		this.graphicEventManager = gem;
	}
	public GraphicEventManager getGraphicEventManager(){
		return this.graphicEventManager;
	}

	

}