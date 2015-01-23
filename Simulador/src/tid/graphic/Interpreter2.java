package tid.graphic;

import infonet.javasim.util.IPaddress;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import tid.utils.Config;

public class Interpreter2 {
	private final String ROUTE_NODE_1 ="images/node1.png";
	private final int BORDER = 10;
	
	private boolean fullScreen = false;
	private static final long serialVersionUID = 1L;

	private boolean debugEnable = true;
	private boolean fast = true;
	// private final Interpreter_ self = this;
	private int width = 800;
	private int height = 600;
	private int minButtonSizeWidth = 20;
	private int minButtonSizeHeight = 20;
	private double visuaRatio = 1.0;

	private Config enviroment;
	
	private JFrame principalFrame = null;
	private JMenuBar upperBar = null;
	private JMenu programMenu = null;
	private JLabel bClose;

	private String TITLE = "J-Sim Log Interpreter";

	private String imagesDirectory = "images/";
	private String sCloseButton = imagesDirectory + "iClose.png";
	private JMenu viewMenu;

	private int CIDR = 32;
	private String name;
	private Hashtable<IPaddress, Vector<Node>> nodesPerNetwork;

	private Container centralPanel;

	public static void main(String[] args) {
		GraphicInterpreter principalWindow = new GraphicInterpreter(null, 800, 600);
		// principalWindow.setVisible(true);

	}

	// private static String id;
	/**
	 * @param owner
	 */

	public Interpreter2(Frame owner, int width, int height) {

		this.height = height;
		this.width = width;

		initialize();

	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */

	private void initialize() {
		// Iniciating componentes
		this.principalFrame = configPrincipalFrame();
		this.upperBar = configUpperMenuBar();
		// this.setSize(width, height);
		// Adding components
		this.principalFrame.setJMenuBar(upperBar);

		principalFrame.setVisible(true);
		// this.upperBar = new Container();
		//		
		// this.principalFrame.add(upperBar);
		// this.upperBar.addMouseMotionListener(new MoveWindowEvent(this));
		// this.upperBar.setBackground(new Color(0,255,0));
		// this.bClose = new JLabel();
		// this.bClose.addMouseListener(new CloseMouseEvent(this));
		// this.add(this.upperBar);
		// upperBar.add(bClose);
		// this.paintAll();
	}

	private JFrame configPrincipalFrame() {
		JFrame jFrame = new JFrame();
//		{/**
//			 * 
//			 */
//			private static final long serialVersionUID = 1L;
//
//		/*	public void paint(Graphics g){
//				myPaint(g);
//			}*/
//		};
		jFrame.setSize(this.width, this.height);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setTitle(this.TITLE);
		jFrame.addKeyListener(new generalKeysPressed());
		this.upperBar = configUpperMenuBar();
		jFrame.setJMenuBar(this.upperBar);
		return jFrame;
	}

	private JMenuBar configUpperMenuBar() {
		JMenuBar jMenu = new JMenuBar();

		this.programMenu = configProgramMenu();
		this.viewMenu = configViewMenu();
		
		if (fast){
			this.visuaRatio = 1.0;
		}
		jMenu.add(this.programMenu);
		jMenu.add(this.viewMenu);

		JComboBox zoom = new JComboBox();
		zoom.addItem("uno");
		if (debugEnable){
			System.out.println("Height of jMenu="+programMenu.getFont().getSize());
		}
		zoom.setSize(50,18);

		Container c = new Container();
//		c.setSize(200,jMenu.getHeight()-2)
		
		jMenu.add(new JLabel("Zoom:"));
		c.add(zoom);

		jMenu.add(c);

		return jMenu;
	}

	private JMenu configProgramMenu() {
		JMenu jMenu = new JMenu();

		jMenu.setText("File");
		jMenu.setMnemonic(KeyEvent.VK_F);
		// LOAD element
		JMenuItem loadItem = new JMenuItem();
		loadItem.setText("load");
		loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				InputEvent.CTRL_DOWN_MASK));
		loadItem.addActionListener(new ActionLoad());

		jMenu.add(loadItem);
		// EXIT element
		JMenuItem exitItem = new JMenuItem();
		exitItem.setText("exit");
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				InputEvent.CTRL_DOWN_MASK));
		ActionListener exit = new ActionExit();
		exitItem.addActionListener(exit);
		jMenu.add(exitItem);
		return jMenu;
	}

	private JMenu configViewMenu() {
		JMenu jMenu = new JMenu();
		jMenu.setText("View");
		jMenu.setMnemonic(KeyEvent.VK_V);
		ActionListener fullScreenAction = new ActionFullScreen(principalFrame);
		JMenuItem fullScreenItem = new JMenuItem();
		fullScreenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
				InputEvent.ALT_DOWN_MASK));
		fullScreenItem.setText("Full Screen");
		fullScreenItem.addActionListener(fullScreenAction);
		jMenu.add(fullScreenItem);
		JMenuItem optionItem = new JMenuItem();
		optionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				InputEvent.CTRL_DOWN_MASK));
		optionItem.setText("Options");
		// TODO hacer el lanzador
		if (fast) {
			this.CIDR = 24;
			
		}
		return jMenu;
	}

	private void paintAll() {
		// CLOSE BUTTON
		Image iClose = GraphicUtil.loadImage(sCloseButton).getScaledInstance(
				minButtonSizeWidth, minButtonSizeHeight, 0);
		this.upperBar.setSize(width, minButtonSizeHeight);
		this.bClose.setSize(minButtonSizeWidth, minButtonSizeHeight);
		this.bClose.setIcon(new ImageIcon(iClose.getScaledInstance(
				minButtonSizeWidth, minButtonSizeHeight, 0)));

		this.bClose.setLocation(width - minButtonSizeWidth, 0);
		principalFrame.repaint();

	}

	private class CloseMouseEvent extends MouseAdapter {
		Window owner;

		public CloseMouseEvent(Window owner) {
			super();
			this.owner = owner;

		}

		public void mouseClicked(MouseEvent e) {
			owner.dispose();
		}

	}

	private class MoveWindowEvent extends MouseMotionAdapter {
		Component component;
		int posX, posY;

		public MoveWindowEvent(Component componentToMove) {
			super();
			this.component = componentToMove;

		}

		public void mouseMoved(MouseEvent e) {
			posX = e.getXOnScreen();
			posY = e.getYOnScreen();
		}

		public void mouseDragged(MouseEvent e) {
			// e.
			int difX = e.getXOnScreen() - posX;
			int difY = e.getYOnScreen() - posY;
			if (debugEnable) {
				System.out.println("(" + component.getX() + ","
						+ component.getY() + ")" + "(" + e.getXOnScreen() + ","
						+ e.getYOnScreen() + ")" + "(" + difX + "," + difY
						+ ")");
			}
			this.component.setLocation(component.getX() + difX, component
					.getY()
					+ difY);
			posX = e.getXOnScreen();
			posY = e.getYOnScreen();

		}

	}

	private class ActionExit implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			principalFrame.dispose();
			Runtime.getRuntime().exit(0);
		}

	}

	private class generalKeysPressed extends KeyAdapter {
		public void keyPressed(KeyEvent e){
			if (e.getKeyCode() == KeyEvent.VK_F11){
				toFullScreen(principalFrame);
			}
			// PAINT
			if (e.getKeyCode() == KeyEvent.VK_F2){
				if (debugEnable)
					System.out.println("F2 Pressed");
				principalFrame.repaint();
			}
//			this.keyPressed(e);
		}
	}

	private class ActionFullScreen implements ActionListener {
		// private boolean fullScreen = false;
		private Window window;

		public ActionFullScreen(Window window) {
			this.window = window;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			toFullScreen(window);
		}

	}

	private class ActionLoad implements ActionListener {
		private Vector<Node> nodes;

		@Override
		public void actionPerformed(ActionEvent a) {
			if (debugEnable) {
				System.out.println("Load Action");
			}

			int result = 0;
			File file = null;
			// XXX quitar el fast

			if (fast) {
				name = "figura4-config.xml";
				file = new File(name);
				result = JFileChooser.APPROVE_OPTION;
			}

			else {
				JFileChooser fileChoser = new JFileChooser(".");
				fileChoser.setLocation(locationToBeCenter(principalFrame,
						fileChoser, true));
				result = fileChoser.showOpenDialog(principalFrame);
				if (result == JFileChooser.APPROVE_OPTION) {
					file = fileChoser.getSelectedFile();
					name = file.getAbsolutePath();
				}
			}
			if (result == JFileChooser.APPROVE_OPTION) {
				if (debugEnable) {
					System.out.println("Load Acepted. FileRoute="
							+ file.getAbsolutePath());
				}
				if (!file.exists())
					JOptionPane.showConfirmDialog(principalFrame,
							"The file does not exist!", "File error",
							JOptionPane.CLOSED_OPTION,
							JOptionPane.ERROR_MESSAGE);
				else {
					try {
						// TODO no se si esto vale pero si vale  hay que ver que el do Config ya no existe
//						Config configuration = (Config) XMLUtils.doConfig(file
//								.getAbsolutePath());
						Config configuration = new Config();
						String[] names = (String[]) configuration
								.findSingle("IDS");
						nodes = new Vector<Node>();
						// Mask, Vector of nodes
						nodesPerNetwork = new Hashtable<IPaddress, Vector<Node>>();
						for (String name : names) {
							Node n = new Node();
							n.id = name;
							n.config = (Config) configuration.findSingle(name);
							// @SuppressWarnings("unchecked")
							n.address = new Hashtable<String, Long>(
									(HashMap<String, Long>) n.config
											.findSingle("INTERFACES"));
							n.principalAddress = (Long) n.config
									.findSingle("PRINCIPALADDR");
							IPaddress mask = new IPaddress(n.principalAddress,
									CIDR).getMaskedIPaddress();
							if (nodesPerNetwork.containsKey(mask))
								nodesPerNetwork.get(mask).add(n);
							else {
								Vector<Node> v = new Vector<Node>();
								v.add(n);
								nodesPerNetwork.put(mask, v);
							}

							nodes.add(n);

						}
						@SuppressWarnings("unchecked")
						Enumeration<String> links_ = (Enumeration) configuration
								.find("LINKS");
						Vector<String> links = new Vector<String>();
						while (links_.hasMoreElements())
							links.add(links_.nextElement());

						if (debugEnable) {
							String cadena = "Nodes: ";
							for (int i = 0; i < nodes.size() - 1; i++)
								cadena += nodes.get(i).id + ", ";
							cadena += nodes.get(nodes.size() - 1);
							System.out.println(cadena);
							cadena = "Links: ";
							for (int i = 0; i < links.size() - 1; i++)
								cadena += links.get(i) + ", ";
							cadena += links.get(links.size() - 1);
							System.out.println(cadena);
							// Networks
							Enumeration<IPaddress> keys = nodesPerNetwork
									.keys();

							while (keys.hasMoreElements()) {
								IPaddress key = keys.nextElement();
								cadena = "Network=" + key + ".";
								Vector<Node> nodesOfANetwork = nodesPerNetwork
										.get(key);
								for (int i = 0; i < nodesOfANetwork.size(); i++) {
									cadena += " Address"
											+ i
											+ "="
											+ nodesOfANetwork.get(i).principalAddress
											+ ",";
								}
								System.out.println(cadena);
							}
						}

					} catch (Exception e) {
						JOptionPane.showConfirmDialog(principalFrame,
								"The file is incorrect or can't be readed!",
								"File error", JOptionPane.CLOSED_OPTION,
								JOptionPane.ERROR_MESSAGE);
						if (debugEnable)
							e.printStackTrace();
					}
				}
			}
			// String caca = fileChoser.getApproveButtonText();

		}

	}

	private void toFullScreen(Window window) {
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		if (gd.isFullScreenSupported()) {

			if (!fullScreen) {
				gd.setFullScreenWindow(window);
				fullScreen = true;
			} else {
				gd.setFullScreenWindow(null);
				fullScreen = false;
			}

		} else {
			fullScreen = false;
			System.err.println("FullScreen is not supported");
		}
	}

	private Point centerPosition(Component component) {
		Point center = new Point();

		center.x = (component.getWidth() / 2);
		center.y = (component.getHeight() / 2);
		if (debugEnable) {
			System.out.println("Dimension=" + component.getSize()
					+ ",CenterPosition=" + center);
		}
		return center;
	}

	private Point locationToBeCenter(Component base, Component element,
			boolean absolute) {
		Point position = new Point();
		Point baseCenter = centerPosition(base);
		position.x = baseCenter.x - element.getWidth() / 2;
		position.y = baseCenter.y - element.getHeight() / 2;
		if (absolute) {
			position.x += base.getLocation().x;
			position.y += base.getLocation().y;
		}
		if (debugEnable) {
			System.out.println("locationToBeCenter=" + position
					+ ",ElementSize=" + element.getSize());
		}
		return position;
	}

	private void myPaint(Graphics g){
		
		Graphics2D g2 = (Graphics2D)g;
		/*if (nodesPerNetwork != null){
			Enumeration<IPaddress> keys = nodesPerNetwork.keys();
			ImageIcon node = new ImageIcon(ROUTE_NODE_1);
			int alturaBase = node.getIconHeight() +BORDER;
			int anchuraBase= node.getIconWidth()+BORDER;
			int networks = nodesPerNetwork.size();
			// DRAWING AREAS
			int posX = BORDER;
			int posY = BORDER;
			while (keys.hasMoreElements()){
				IPaddress key = keys.nextElement();
				Vector<Node> nodesOfANetwork= nodesPerNetwork.get(key);
				Double factorArea = Math.sqrt(nodesOfANetwork.size());
				double alturaFinal = alturaBase * factorArea;
				double anchuraFinal = anchuraBase * factorArea;
				g.fillRect(reescalar(posX), reescalar(posY), reescalar(anchuraFinal), reescalar(alturaFinal));
				
			}
		}*/
	}
	private int reescalar(double original){
		return (new Double(original * visuaRatio).intValue());
	}
	private class Node {
		public String id;
		public Config config;
		public Long principalAddress;
		public Hashtable<String, Long> address;
		

	}


} // @jve:decl-index=0:visual-constraint="10,10"
