package tid.graphic;

import infonet.javasim.util.IPaddress;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JPanel;



public class GraphicNetwork implements GraphicElements{
	static int colorin= 0;
	static Color []colores = {new Color(0,0,0),new Color(255,0,0),new Color (0,255,0),new Color (0,0,255),new Color (255,255,0),new Color (255,0,255),new Color (0,255,255)}; 
		public IPaddress netAddress;
		private JPanel toolTip;
		private Object self = this;
		
		public JPanel panel;
		private GraphicInterpreter owner;
//		private Dimension dimension = null;
		private Vector<GraphicNode> nodesOfTheNetwork;
		private boolean painted = false;

		private Dimension standarPanelArea;
		private Point standarPosition;
		private double visualRatio;
		/**
		 * Contructor for the class
		 * @param ipa Address of this network (i.e: 192.168.1.0/24)
		 */
		public GraphicNetwork(IPaddress ipa, GraphicInterpreter owner_){
			netAddress = ipa;
			nodesOfTheNetwork = new Vector<GraphicNode>();
			this.owner = owner_;
			panel = new JPanel(){
			
				/**
				 * 
				 */
				
				private static final long serialVersionUID = 7940108662354539637L;

				public void paint(Graphics g){
					

					
					int x = owner.reescalar(GraphicConstants.BORDER);
					int diameter = owner.reescalar(externalRadio()*2);

					if (GraphicConstants.debugEnable){

					}
					
					g.drawOval(x, x, diameter, diameter);

					for (GraphicNode node:nodesOfTheNetwork){
						if (node.getPainted()){

							if (GraphicConstants.printNodesNames){
								Color c = g.getColor();
								g.setColor(GraphicConstants.NORMAL_COLOR_TEXT);
								Point position = node.getStandarPosition();
								Font f = g.getFont();
								g.setFont(new Font(f.getName(),f.getStyle(),owner.reescalar(f.getSize())));
								g.drawChars(node.id.toCharArray(), 0, node.id.length(),owner.reescalar(position.x-GraphicConstants.BORDER),owner.reescalar(position.y));
								g.setFont(f);
								g.setColor(c);
							}
							node.panel.setLocation(owner.reescalar(node.getStandarPosition()));
							node.panel.repaint();
						}
					}
					
					
				}
			};
			
		}
		

		public boolean addNode(GraphicNode g){
			
			// FIXME algo aqui!!!!
			if (netAddress.same_prefix(g.principalAddress.getMaskedIPaddress(owner.CIDR))){
				if (!nodesOfTheNetwork.contains(g)){
					nodesOfTheNetwork.add(g);
//					panel.add(g.panel);
				}
				return true;
			}
			return false;
		}
		public boolean rmNode(GraphicNode g){
			if (nodesOfTheNetwork.remove(g)){
				panel.remove(g.panel);
				return true;
			}
			return false;
		}
		public int numNodesPainted () {
			int num = 0;
			for (GraphicNode node:nodesOfTheNetwork){
				if (node.getPainted())
					num++;
			}
			return num;
		}
		
		public Point center(){

			Point relativCenter = GraphicUtil.centerPosition(getStandarPanelArea());
			return new Point(relativCenter.x+position().x,relativCenter.y+position().y);
		}

		public Point position(){
			return new Point(standarPosition.x,standarPosition.y);
		}
		public boolean getPainted (){
			return painted;
		}
		public void setPainted(boolean p){
			painted = p;
		}
		

		
		private double factor(){
			double factor = Math.sqrt(numNodesPainted())+1; 

			return factor;
		}
		private int baseRadio(){
			// TODO mirar si es este el que deberia poner
			ImageIcon i = new ImageIcon(GraphicConstants.ROUTE_IMAGES_NODE[GraphicConstants.NORMAL_STATUS]);
			int radio = (i.getIconWidth()>i.getIconHeight())?i.getIconWidth():i.getIconHeight();
			radio = radio / 2;

			return radio; 
		}
		public int externalRadio(){
			int radio = (new Double(baseRadio()*factor()).intValue())+GraphicConstants.BORDER;
			return radio; 
		}
		public int internalRadio(){
			int radio = new Double(baseRadio()*(factor()-1)).intValue();//-GraphicConstants.BORDER;
			return radio; 
			
		}

		public Dimension calculateDimension (){
			
			int radio = externalRadio()+GraphicConstants.BORDER;
			return new Dimension(radio*2,radio*2); 
		}
		
		public Point[] pointsOfTheNodes(){
			int numNodes = numNodesPainted();
			Point[] points = new Point[numNodes];
			Point center = GraphicUtil.centerPosition(calculateDimension());
			int radio = internalRadio();
			double anguleBase = Math.PI*2 / numNodes;
//			double anguleBase = 360 / numNodesPainted();
			if (GraphicConstants.debugEnable){
//				System.out.println("RadioBase="+baseRadio()+",InternalRadio="+internalRadio()+",ExtenralRadio="+externalRadio());
			}
			for (int i = 0;i<numNodes;i++){
				double angulo = anguleBase *i;
				int X = new Double(Math.sin(angulo)*radio).intValue();
				int Y = new Double(Math.cos(angulo)*radio).intValue();
				points[i] = new Point(center.x+X,center.y-Y);
			}
			if (GraphicConstants.debugEnable){
//				System.out.print("Points:");
//				int i = 0;
//				
//				for (Point p: points){
//					i++;
//					System.out.print(i+":"+p+"||");
//				}
//				System.out.println();
			}
			

			return points;
		}
		public void calculatePositions(){
			Point[] nodesPoints = pointsOfTheNodes();
			if (GraphicConstants.debugEnable){
//				System.out.print("Points:");
//				int i = 0;
//				
//				for (Point p: nodesPoints){
//					i++;
//					System.out.print(i+":"+p+"||");
//				}
//				System.out.println();
			}
			int pos = 0;
			panel.removeAll();
			for (GraphicNode node : nodesOfTheNetwork)
				if (node.getPainted()){

					node.setStandarCenterPosition(nodesPoints[pos]);
					panel.add(node.panel);
					node.panel.setOpaque(true);
					pos++;
				}
		}
		
		public Vector<GraphicNode> nodesOfTheNetwork(){
			return nodesOfTheNetwork;
		}
		private Polygon newPoligonResized (Point[] points){
			int [] x = new int[points.length];
			int [] y = new int[points.length];
			for (int i = 0; i< points.length;i++){
				x[i] = owner.reescalar(points[i].x);
				y[i] = owner.reescalar(points[i].y);
			}
			return new Polygon(x,y,points.length);
		}
		public void setCornerLocation(int x, int y){
			this.panel.setLocation(x, y);
		}


		public void setStandarPanelArea(Dimension netArea) {
			standarPanelArea = netArea;
		}
		public Dimension getStandarPanelArea() {
			return standarPanelArea;
		}
		public void setStandarPosition(Point position){
			standarPosition = position;
		}

		public Point getStandarPosition(){
			return standarPosition;
		}
		public String toString(){
			String cadena = "";
			cadena += "net address=" + netAddress + ",StandarPosition="+standarPosition+",StandarSize="+standarPanelArea;
			return cadena;
		}


		public void setRatio(double visualRatio) {
			if (this.painted){
				this.panel.setSize(owner.reescalar(getStandarPanelArea()));
				this.panel.setLocation(owner.reescalar(getStandarPosition()));
				this.visualRatio = visualRatio;
				for (GraphicNode node : nodesOfTheNetwork){
					node.setRatio(visualRatio);
				}
			}

			
		}
		public void painting() {
			owner.enviroment.layers.get(owner.activeLayer);
			this.panel.repaint();
			
		}


		@Override
		public String toHtmlString() {
			// TODO Auto-generated method stub
			return null;
		}





		
}