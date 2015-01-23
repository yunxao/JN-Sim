package tid.graphic;

import infonet.javasim.util.IPaddress;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import tid.utils.Config;

public class GraphicNode implements GraphicElements,Comparable<GraphicNode>{
	public String id;
	public Config config;
	private int state = 0; 
	public IPaddress principalAddress;
	private Vector<String> layers;
	
	private JPanel toolTip;
	public JPanel panel;
	
	private GraphicNode self = this;
	private GraphicInterpreter owner;
	
	private boolean painted = false;
	public Hashtable<String, IPaddress> addresses;
	private double visualRatio;
	private Point standarPosition;
	private int standarWidth = getStandarImagenIcon().getIconWidth();
	private int standarHeight = getStandarImagenIcon().getIconHeight();
	// doble buffer
	private Image myIcon;
	private Graphics bufferImage;
	
	public ImageIcon getStandarImagenIcon() {
		if (GraphicConstants.jar)
			return new ImageIcon(getClass().getResource("/images/node0.png"));
		return new ImageIcon(GraphicConstants.ROUTE_IMAGES_NODE[GraphicConstants.NORMAL_STATUS]);
//		return new ImageIcon(getClass().getResource(GraphicConstants.ROUTE_IMAGES_NODE[GraphicConstants.NORMAL_STATUS]));
		
	}
	public GraphicNode(GraphicInterpreter owner_){
		layers = new Vector<String>();
		this.owner = owner_;
		this.toolTip = new JPanel();
		this.toolTip.setSize(300,300);


		setState(state);
//		ImageIcon i = new ImageIcon(GraphicConstants.ROUTE_IMAGES_NODE[state]);
//		this.myIcon = i.getImage();
		this.standarPosition = new Point();
		this.standarWidth = getStandarImagenIcon().getIconWidth();
		this.standarHeight = getStandarImagenIcon().getIconHeight();
		panel = new JPanel(){
				
			/**
			 * 
			 */
			private static final long serialVersionUID = -5489341966658014444L;
//			public void update (Graphics g)
//			{
//				g.clearRect(0, 0, this.getWidth(),this.getHeight());
//				paint (g);
//			}
			public void paint(Graphics g){
//				super.paint(g);
//				g.clearRect(0,0, this.getWidth(), this.getHeight());
				if (painted){
					panel.setSize(owner.reescalar(standarWidth),owner.reescalar(standarHeight));
					if (GraphicConstants.jar){
//						BufferedImage i = tid.utils.CargadorDeImagenes.getImagen("node0.png");
//						g.drawImage(i,0,0, owner.reescalar(standarWidth),owner.reescalar(standarHeight),panel);
						ImageIcon i = new ImageIcon(getClass().getResource("/images/node0.png"));
//						ImageIcon i = new ImageIcon(getClass().getResource(GraphicConstants.ROUTE_IMAGES_NODE[state]));
						g.drawImage(i.getImage(),0,0, owner.reescalar(standarWidth),owner.reescalar(standarHeight),panel);
					}else{
						ImageIcon i = new ImageIcon(GraphicConstants.ROUTE_IMAGES_NODE[state]);
						g.drawImage(i.getImage(),0,0, owner.reescalar(standarWidth),owner.reescalar(standarHeight),panel);
					}
//					panel.setBackground(null);
//					panel.setOpaque(false);
					

//					g.drawImage(myIcon, 0,0, owner.reescalar(standarWidth),owner.reescalar(standarHeight),panel);
					panel.setToolTipText(self.toHtmlString());
//					panel.getTo
					
				}
			}
			
			
		};
//		panel.setBackgrdfalse);
		this.panel.setOpaque(false);

	
	}
	public Point getStandarCenterPosition(){
		Point p = new Point();
		p.setLocation(this.standarPosition.getX()+(this.standarWidth/2), this.standarPosition.getY()+(this.standarHeight/2));
		return p;
	}
	public Point getStandarPosition(){
		return this.standarPosition;
	}
	
	public Dimension getStandarDimension() {
		return new Dimension(this.standarWidth,this.standarHeight);
	}
//	public Dimension getStandarDimension(){
//		return getStandarDimension().getSize();
//	}
	public boolean addLayer(String layer) {
		if (layers.contains(layer))
			return false;
		layers.add(layer);
		return true;
	}
	public boolean  rmLayer(String layer) {
		return layers.remove(layer);
	}
	public void setCenterLocation(int x, int y){
		panel.setLocation(x-standarWidth/2, y-standarHeight/2);
	}
	public void setStandarCenterPosition(Point standarCenterPosition) {
		this.standarPosition.setLocation(standarCenterPosition.x-this.standarWidth/2,standarCenterPosition.y-this.standarHeight/2);
	}
	public void setRatio(double visualRatio) {
		
			this.panel.setSize(owner.reescalar(getStandarDimension()));
			this.visualRatio = visualRatio;
		
	}
	public Point getStandarCenterAbsolutePosition(){
		if (painted){
			IPaddress netAddress = principalAddress.getMaskedIPaddress(owner.CIDR);
			Point netPosition = owner.enviroment.networks.get(netAddress).getStandarPosition();
			// the net haven't position
			if (netPosition == null)
				return null;
			Point nodePosition = getStandarCenterPosition();
			return new Point(netPosition.x+nodePosition.x,netPosition.y+nodePosition.y);
		}
		return null;

	}
	@Override
	public void setStandarPosition(Point position) {
		this.standarPosition = position;
	}
	@Override
	public void painting() {
		owner.enviroment.layers.get(owner.activeLayer);
		this.panel.repaint();
		
		
	}
	public String toString(){
		String cadena = "";
		cadena  ="Id            : "+this.id+"\n";
		cadena +="Principal addr: "+this.principalAddress+"\n";
		cadena +="Layers:\n";
		for (int i = 0;i < layers.size();i++){
			String layer = layers.get(i);
			cadena +="  layer "+i+"     : "+layer+"\n";
		}
		cadena +="Addresses:\n";
		Enumeration<String> keyAddresses = addresses.keys();
		while (keyAddresses.hasMoreElements()){
			String keyAddress = keyAddresses.nextElement();
			IPaddress longAddress = addresses.get(keyAddress);
			cadena +="address       : "+keyAddress+"-"+longAddress+"\n";
		}
		if (GraphicConstants.debugEnable){
//			cadena += "Debug vars\n";
//			cadena += "  standarPos    : ("+this.standarPosition.x+","+this.standarPosition.y+")\n";
//			cadena += "  Painted       : "+this.painted+"\n"; 
//			cadena += "  Visual Ratio  : "+this.visualRatio+"\n"; 
//			cadena += "  Standar Width : "+this.standarWidth+"\n"; 
//			cadena += "  Standar Height: "+this.standarHeight+"\n"; 
//			cadena += "  Status        : "+this.state+"\n"; 

		}
		return cadena;
	}
	public String toHtmlString(){
		String cadena = "<html><table>";
		cadena +="<tr><td>Id</td><td>: "+this.id+"</td></tr>";
		cadena +="<tr><td>Principal addr</td><td>: "+this.principalAddress+"</td></tr>";
		cadena +="<tr><td>Layers:</td><td></td></tr>";
		for (int i = 0;i < layers.size();i++){
			String layer = layers.get(i);
			cadena +="<tr><td>&nbsp&nbsp layer "+i+"</td><td>: "+layer+"</td></tr>";
		}
		cadena +="<tr><td>Addresses:</td><td></td></tr>";
		Enumeration<String> keyAddresses = addresses.keys();
		while (keyAddresses.hasMoreElements()){
			String keyAddress = keyAddresses.nextElement();
			IPaddress longAddress = addresses.get(keyAddress);
			cadena +="<tr><td>&nbsp&nbsp address</td><td>: "+keyAddress+"-"+longAddress+"</td></tr>";
		}
		if (GraphicConstants.debugEnable){
			cadena += "<tr><td>Debug vars</td><td></td></tr>";
			cadena += "<tr><td>&nbsp&nbsp standarPos</td><td>: ("+this.standarPosition.x+","+this.standarPosition.y+")</td></tr>";
			cadena += "<tr><td>&nbsp&nbsp Painted</td><td>: "+this.painted+"</td></tr>"; 
			cadena += "<tr><td>&nbsp&nbsp Visual Ratio</td><td>: "+this.visualRatio+"</td></tr>"; 
			cadena += "<tr><td>&nbsp&nbsp Standar Width</td><td>: "+this.standarWidth+"</td></tr>"; 
			cadena += "<tr><td>&nbsp&nbsp Standar Height</td><td>: "+this.standarHeight+"</td></tr>"; 
			cadena += "<tr><td>&nbsp&nbsp Status</td><td>: "+this.state+"</td></tr>"; 

		}
		cadena += "</table></html>";
		return cadena;
	}
	public void setState(int state_){
		
		this.state = state_;
		if (panel != null){
			this.myIcon = panel.createImage(standarWidth,standarHeight);
			if (myIcon != null){
				this.bufferImage = this.myIcon.getGraphics();
				if (GraphicConstants.jar)
					this.bufferImage.drawImage(new ImageIcon(getClass().getResource("/images/node0.png")).getImage(), 0,0,panel);
				else
					this.bufferImage.drawImage(new ImageIcon(GraphicConstants.ROUTE_IMAGES_NODE[state]).getImage(), 0,0,panel);
					
				
			}
		}
		
	}
	public int getState(){
		return this.state;
	}
//	@Override
//	public JPanel toolTip() {
//		// TODO Auto-generated method stub
//		JLabel text = new JLabel(toHtmlString());
//		text.setSize(this.toolTip.getSize());
//		this.toolTip.add(text);
//		return null;
//	}
	@Override
	public int compareTo(GraphicNode o) {
		return this.id.compareTo(o.id);
	}
	protected void setPainted(boolean painted){
		this.painted = painted;
	}
	protected boolean getPainted(){
		return painted  && myNetwork().getPainted();
	}
	private GraphicNetwork myNetwork (){
		return owner.enviroment.networks.get(this.principalAddress.getMaskedIPaddress(owner.CIDR));
	}
}