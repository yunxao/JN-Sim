package tid.graphic;

import infonet.javasim.util.IPaddress;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import tid.utils.Config;

import com.renesys.raceway.DML.Configuration;

public class GraphicInterpreter {
	private GraphicNode activeNode = null;
	private GraphicEvent eventShowed = null;
	private TreeSet<GraphicEvent> events;
	private boolean fullScreen = false;
	private static final long serialVersionUID = 1L;

	public GraphicInterpreter self = this;
	// private final Interpreter_ self = this;
	private int width = GraphicConstants.WIDTH;
	private int height = GraphicConstants.HEIGHT;
	private double visualRatio = 1.0;
	public String activeLayer = null;

	public GraphicEnviroment enviroment;
	private Config configEnviroment;

	protected JFrame principalFrame = null;
	protected JPanel principalPanel = null;
	protected JPanel centralPanel = null;
	private JPanel jPanelBetweenCentralAndLeftPanel = null;
	protected JPanel rightPanel = null;
	protected JMenuBar upperBar = null;
	private JMenu programMenu = null;
	private JMenu viewMenu;
	private JMenu optionMenu = null;
	private JScrollPane jScrollPanelEventList = null;
	private JPanel jPanelBetweenEventListAndEventDetail = null;
	private JScrollPane jPanelEventDetail = null;
	private JPanel jPanelBetweenEventDetailAndNodeStatus = null;
	private JScrollPane jPanelNodeStatus = null;
//	private JPanel jPanelEventDetail = null;
	private JTextArea jTextAreaEventDescription = null;
	private JTextArea jTextAreaNodeStatusDescription = null;
	protected JList jEventList = null;
	protected Dimension sizeOfListDetails = new Dimension (GraphicConstants.LIST_OF_EVENTS_WIDHT,new Double(GraphicConstants.HEIGHT*GraphicConstants.LIST_OF_EVENTS_HEIGHT_PERCENT).intValue());
	protected Dimension sizeOfDetailsEvents = new Dimension (GraphicConstants.LIST_OF_EVENTS_WIDHT,new Double(GraphicConstants.HEIGHT*GraphicConstants.EVENT_DETAIL_HEIGHT_PERCENT).intValue());
	protected int widthOfLeftPanel = GraphicConstants.LIST_OF_EVENTS_WIDHT;
	protected JTextField zoom;

	private String TITLE = "J-Sim Log Interpreter";

//	private String imagesDirectory = "images/";

	
	private Dimension centralArea = new Dimension(0,0);
	public int CIDR = 32;
	private String fileNetworkConfiguration;
	private JScrollPane jScrollPaneCentral;
	public int normalFontSize = GraphicConstants.FONT_SIZE;
//	public String filePhysicalEvents;
	// private Hashtable<IPaddress, GraphicNetwork> nodesPerNetwork;


	public static void main(String[] args) {
		new GraphicInterpreter(null, GraphicConstants.WIDTH,GraphicConstants.HEIGHT);
		// principalWindow.setVisible(true);

	}

	// private static String id;
	/**
	 * @param owner
	 */

	public GraphicInterpreter(Frame owner, int width, int height) {

		this.height = height;
		this.width = width;
		enviroment = new GraphicEnviroment();
		initialize();

	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */

	private void initialize() {
		// Iniciating componentes
		this.events = new TreeSet<GraphicEvent>();
		this.principalFrame = configPrincipalFrame();

		this.principalFrame.setJMenuBar(upperBar);
		
		principalFrame.setVisible(true);

	}

	private JFrame configPrincipalFrame() {
		JFrame jFrame = new JFrame(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 3010139866725041862L;


			public void paint(Graphics g){

				setDimensions();
				if (GraphicConstants.debugEnable){
//					System.out.println("Number of elements paint in principalFrame "+this.getComponentCount());
				}
				principalPanel.repaint();
				
			}
		};
		jFrame.setSize(this.width, this.height);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setTitle(this.TITLE);
		jFrame.addKeyListener(new generalKeysPressed());
		this.upperBar = configUpperMenuBar();
		jFrame.setJMenuBar(this.upperBar);
		
		
		
		principalPanel = new JPanel(){

			/**
			 * 
			 */
			private static final long serialVersionUID = -2225063739230146933L;

			public void paint(Graphics g){
				jScrollPaneCentral.setLocation(0,GraphicConstants.BORDER);
				jScrollPaneCentral.setSize(principalPanel.getWidth()-widthOfLeftPanel-GraphicConstants.BORDER, 
						principalPanel.getHeight()-GraphicConstants.BORDER);
				// XXX no se porque hay que poner un tamaño
				centralPanel.setSize(0,0);
				centralPanel.setPreferredSize(calculateCentralArea());
				
				jPanelBetweenCentralAndLeftPanel.setBounds(jScrollPaneCentral.getX() + jScrollPaneCentral.getWidth(),
						GraphicConstants.BORDER,
						GraphicConstants.BORDER,
						principalPanel.getHeight());
				rightPanel.setBounds(jPanelBetweenCentralAndLeftPanel.getX()+jPanelBetweenCentralAndLeftPanel.getWidth(), 
									GraphicConstants.BORDER, 
									principalPanel.getWidth() - jPanelBetweenCentralAndLeftPanel.getX()+jPanelBetweenCentralAndLeftPanel.getWidth()-GraphicConstants.BORDER*2,
									principalPanel.getHeight()-GraphicConstants.BORDER);
				jScrollPaneCentral.repaint();
				jPanelBetweenCentralAndLeftPanel.repaint();
				rightPanel.repaint();
				
			}
		};
//		panel.setSize(this.height,this.width);
		this.centralPanel = configCentralPanel();
		
		jScrollPaneCentral = new JScrollPane();
		jScrollPaneCentral.getViewport().add(this.centralPanel);
		this.rightPanel  = configRightPanel();
		this.jPanelBetweenCentralAndLeftPanel = new JPanel();
		jPanelBetweenCentralAndLeftPanel.addMouseMotionListener(new MoveCentralPanels());
		principalPanel.add(jScrollPaneCentral);
		principalPanel.add(jPanelBetweenCentralAndLeftPanel);
		principalPanel.add(this.rightPanel );
		
		jFrame.add(principalPanel);
//		jFrame.add(centralPanel);
		
		
		
		
		
//		this.leftPanel.setBounds(this.width-GraphicConstants.LIST_OF_EVENTS_WIDHT, 0, GraphicConstants.LIST_OF_EVENTS_WIDHT,this.height);
		
		return jFrame;
	}

	private JPanel configRightPanel() {
		JPanel panel = new JPanel(){
			/**
			 * 
			 */
			private static final long serialVersionUID = -3961396907416802443L;
			public void paint(Graphics g){
				
				
				jScrollPanelEventList.setBounds(0,
												0,
												rightPanel.getWidth(),
												sizeOfListDetails.height-GraphicConstants.BORDER);
				
				jEventList.setSize(jScrollPanelEventList.getSize());
				jEventList.setFont(normalFont());

				
				jPanelBetweenEventListAndEventDetail.setBounds(0,
																jScrollPanelEventList.getY()+jScrollPanelEventList.getHeight(),
																rightPanel.getWidth(), 
																GraphicConstants.BORDER);
				
				
				
				jPanelEventDetail.setBounds(0, 
												jPanelBetweenEventListAndEventDetail.getY()+jPanelBetweenEventListAndEventDetail.getHeight(), 
												rightPanel.getWidth(), 
												sizeOfDetailsEvents.height-GraphicConstants.BORDER);
				
				jTextAreaEventDescription.setSize(jPanelEventDetail.getSize());
				jTextAreaEventDescription.setFont(normalFont());

				jPanelBetweenEventDetailAndNodeStatus.setBounds(0, 
																jPanelEventDetail.getY()+jPanelEventDetail.getHeight(),
																rightPanel.getWidth(), 
																GraphicConstants.BORDER);
				jPanelNodeStatus.setBounds(0, 
											jPanelBetweenEventDetailAndNodeStatus.getY()+jPanelBetweenEventDetailAndNodeStatus.getHeight(),
											rightPanel.getWidth(), 
											rightPanel.getHeight()-(jPanelBetweenEventDetailAndNodeStatus.getY()+jPanelBetweenEventDetailAndNodeStatus.getHeight()));
				jTextAreaNodeStatusDescription.setSize(jPanelNodeStatus.getSize().width-GraphicConstants.BORDER,jPanelNodeStatus.getSize().height);
				jTextAreaNodeStatusDescription.setFont(normalFont());

				jScrollPanelEventList.repaint();
				jPanelBetweenEventListAndEventDetail.repaint();
				jPanelEventDetail.repaint();
				jPanelBetweenEventDetailAndNodeStatus.repaint();
				jPanelNodeStatus.repaint();
				if (GraphicConstants.debugEnable){
					System.out.println(""+jScrollPanelEventList.getY()+"|"+jPanelBetweenEventListAndEventDetail.getY()+"|"+jPanelEventDetail.getY()+"|"
						+jPanelBetweenEventDetailAndNodeStatus.getY()+"|"+jPanelNodeStatus.getY()+"|"+jPanelNodeStatus.getHeight());
				}
				
				
				
			}
		};
//		panel.setBackground(GraphicConstants.LIST_OF_EVENTS_COLOR);
		panel.setBounds(this.width - GraphicConstants.LIST_OF_EVENTS_WIDHT, 0, GraphicConstants.LIST_OF_EVENTS_WIDHT-GraphicConstants.BORDER, this.height);
		
		jEventList = new JList();
		jEventList.addKeyListener(new generalKeysPressed());
		jEventList.addMouseListener(new ListMouseAction());

		jScrollPanelEventList = new JScrollPane();
		jScrollPanelEventList.getViewport().add(jEventList);
	
		
		
		jPanelBetweenEventListAndEventDetail = new JPanel();
		jPanelBetweenEventListAndEventDetail.addMouseMotionListener(new MoveHorizontalDimension(sizeOfListDetails));

		
		jTextAreaEventDescription = new JTextArea();
		

		jPanelEventDetail = new JScrollPane();
		jPanelEventDetail.getViewport().add(jTextAreaEventDescription);
		jTextAreaEventDescription.addKeyListener(new generalKeysPressed());
		
		
		jPanelBetweenEventDetailAndNodeStatus = new JPanel();
		jPanelBetweenEventDetailAndNodeStatus.addMouseMotionListener(new MoveHorizontalDimension(sizeOfDetailsEvents));
		
		jPanelNodeStatus = new JScrollPane();
		jTextAreaNodeStatusDescription = new JTextArea();
		jTextAreaNodeStatusDescription.addKeyListener(new generalKeysPressed());
		jPanelNodeStatus.getViewport().add(jTextAreaNodeStatusDescription);
		
		panel.add(jScrollPanelEventList);
		panel.add(jPanelBetweenEventListAndEventDetail);
		panel.add(jPanelEventDetail);
		panel.add(jPanelBetweenEventDetailAndNodeStatus);
		panel.add(jPanelNodeStatus);
		
		
		return panel;
	}

	private JMenuBar configUpperMenuBar() {
		JMenuBar jMenu = new JMenuBar();

		this.programMenu = configProgramMenu();
		this.viewMenu = configViewMenu();
		this.optionMenu = configOptionMenu();
		// Allways the first layer is physical
//		if (GraphicConstants.fast) {
			this.visualRatio = 1.0;
			this.activeLayer = "physical";
	//	}
		
		jMenu.add(this.programMenu);
		jMenu.add(this.viewMenu);
		jMenu.add(this.optionMenu);
		//
		this.zoom = new JTextField();
		zoom.setText("" + (new Double(visualRatio * 100)).intValue());
		zoom.setSize(50, 18);
		zoom.addKeyListener(new zoomKeyPressed());
		Container c = new Container();

		jMenu.add(new JLabel("Zoom:"));
		c.add(zoom);
		//
		jMenu.add(c);

		return jMenu;
	}
	private JMenu configOptionMenu(){
		JMenu jMenu = new JMenu();
		jMenu.setText("Options");
		JMenuItem generalOpinions = new JMenuItem();
		generalOpinions.setText("General Opinion");
		generalOpinions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK));
		generalOpinions.addActionListener(new ActionOptionPanelGeneral());
		jMenu.add(generalOpinions);
		
		
		JMenuItem layersOptions = new JMenuItem();
		layersOptions.setText("Layer Options");
		layersOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK));
		layersOptions.addActionListener(new ActionOptionPanelLayer());
		jMenu.add(layersOptions);
		
		
		return jMenu;
	}
	private JMenu configProgramMenu() {
		JMenu jMenu = new JMenu();

		jMenu.setText("File");
		jMenu.setMnemonic(KeyEvent.VK_F);
		// LOAD element
		JMenuItem loadItem = new JMenuItem();
		loadItem.setText("Load structure");
		loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				InputEvent.CTRL_DOWN_MASK));
		loadItem.addActionListener(new ActionLoad());
		jMenu.add(loadItem);
		
		JMenuItem loadEventsItem = new JMenuItem();
		loadEventsItem.setText("Load Protocol Events");
		loadEventsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K,InputEvent.CTRL_DOWN_MASK));
		loadEventsItem.addActionListener(new ActionLoadProtocolEvents());
		jMenu.add(loadEventsItem);
		
		JMenuItem loadPhysicalEventItem = new JMenuItem();
		loadPhysicalEventItem.setText("Load Physical Event");
		loadPhysicalEventItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J,InputEvent.CTRL_DOWN_MASK));
		loadPhysicalEventItem.addActionListener(new ActionLoadPhysicalEvents());
		jMenu.add(loadPhysicalEventItem);

		
		JMenuItem resetItem = new JMenuItem();
		resetItem.setText("Reset Enviroment");
		resetItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,InputEvent.CTRL_DOWN_MASK));
		resetItem.addActionListener(new ActionReset());
		jMenu.add(resetItem);
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
		
		
		JMenuItem showNodesNames = new JMenuItem("Show names");
		showNodesNames.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3,
				InputEvent.SHIFT_DOWN_MASK));
		showNodesNames.addActionListener(new ShowNodeNames());
		jMenu.add(showNodesNames);
		
		
		JMenuItem showLinksNames = new JMenuItem("Show names");
		showLinksNames.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
				InputEvent.SHIFT_DOWN_MASK));
		showLinksNames.addActionListener(new ShowLinkNames());
		jMenu.add(showLinksNames);
		
		
		// TODO hacer el lanzador
		// 24 by default
//		if (GraphicConstants.fast) {
			this.CIDR = GraphicConstants.DEFAULT_CIDR;

//		}
		return jMenu;
	}
	private JPanel configCentralPanel(){
		JPanel jPanel = new JPanel(){


			/**
			 * 
			 */
			private static final long serialVersionUID = 3010139866725041862L;
//			public void update (Graphics g)
//			{
//				paint (g);
//			}
			public void paint(Graphics g) {
				g.clearRect(0, 0, this.getWidth(),this.getHeight());
				setDimensions();
				if (GraphicConstants.debugEnable){
//					System.out.println("Number of elements paint in centralFrame "+this.getComponentCount());
				}
				GraphicLayer layer = enviroment.layers.get(activeLayer);
				if (layer != null){
//					setComponentZOrder(layer.panel, pos++);
					layer.panel.repaint();
					
				}

				Enumeration<GraphicNetwork> nets = enviroment.networks
						.elements();
				while (nets.hasMoreElements()) {
					GraphicNetwork net = nets.nextElement();
//					net.panel.setOpaque(true);
					
//					setComponentZOrder(net.panel, pos++);
					net.panel.repaint();

				}
				
			}
			

		};
//		jPanel.setDoubleBuffered(true);
		jPanel.addMouseListener(new MousePopupMenuListener());
		// XXX
		
		jPanel.setBackground(new Color (0,0,0));
		return jPanel;
	}




	private class ActionExit implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			principalFrame.dispose();
			Runtime.getRuntime().exit(0);
		}

	}

	private class generalKeysPressed extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_F2) {
				
				if (GraphicConstants.debugEnable)
					System.out.println("F2 Pressed");

				toFullScreen(principalFrame);
			}
			// PAINT
			else if (e.getKeyCode() == KeyEvent.VK_F5) {
				if (GraphicConstants.debugEnable)
					System.out.println("F5 Pressed");
				
				
				centralPanel.removeAll();
				calculatePositions();
				setDimensions();
				centralPanel.repaint();
			}
		}
	}

	private class zoomKeyPressed extends generalKeysPressed {
		public void keyPressed(KeyEvent e) {
			
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				visualRatio = Double.valueOf(zoom.getText()) / 100;
				principalFrame.repaint();
				return;
			}
			super.keyPressed(e);
		}
	}

	private class ActionFullScreen implements ActionListener {
		private Window window;

		public ActionFullScreen(Window window) {
			this.window = window;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			toFullScreen(window);
		}

	}
	private class ActionLoadPhysicalEvents implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			GraphicPhysicalEventManager gem = new GraphicPhysicalEventManager();
			String filePhysicalEvents = null;//"figura5-events.xml";
			int result = 0;
			File file = null;
			if (GraphicConstants.fast){
				filePhysicalEvents = "figura4-events.xml";
				file = new File(filePhysicalEvents );
				result = JFileChooser.APPROVE_OPTION;
			}else {
				JFileChooser fileChoser = new JFileChooser(".");
				fileChoser.setLocation(GraphicUtil.locationToBeCenter(
						principalFrame, fileChoser, true));
				result = fileChoser.showOpenDialog(principalFrame);
				if (result == JFileChooser.APPROVE_OPTION) {
					file = fileChoser.getSelectedFile();
					filePhysicalEvents = file.getAbsolutePath();
				}
			}
			if (result == JFileChooser.APPROVE_OPTION) {
				// TODO cuando se carga una fisica se reiniciaran los fisicos, posteriormente no se reiniciaran x xml cargado
				TreeSet<GraphicEvent> ts = gem.readEvents(filePhysicalEvents,enviroment);
				GraphicLayer layer = enviroment.layers.get("physical");
				layer.setGraphicEventManager(gem);
				
				if (ts == null){
					JOptionPane.showConfirmDialog(principalFrame,
							"Error in the xml file",
							"Error", JOptionPane.CLOSED_OPTION,
							JOptionPane.WARNING_MESSAGE);
				}
				// TODO quitar el clear
//				events.clear();
				
//				events.addAll(ts);
				insertElements(ts,layer);
				reloadEventsList();
			}
			
		}
		
	}

	private class ActionLoadProtocolEvents implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				String protocolEventsFileName = null;
				int result = 0;
				File file = null;
				if (GraphicConstants.fast){
					protocolEventsFileName = "bgp-5000.xml";
					file = new File(protocolEventsFileName );
					result = JFileChooser.APPROVE_OPTION;
				}
				else {
					JFileChooser fileChoser = new JFileChooser(".");
					fileChoser.setLocation(GraphicUtil.locationToBeCenter(
							principalFrame, fileChoser, true));
					result = fileChoser.showOpenDialog(principalFrame);
					if (result == JFileChooser.APPROVE_OPTION) {
						file = fileChoser.getSelectedFile();
						protocolEventsFileName = file.getAbsolutePath();
					}
				}
				if (result == JFileChooser.APPROVE_OPTION){
					GraphicEventManager gem = GraphicEventManager.getManagerClass(protocolEventsFileName);
					TreeSet <GraphicEvent> eventsReaded = gem.readEvents(protocolEventsFileName, enviroment);
					GraphicLayer layer = enviroment.layers.get(gem.getProtocolName());
					layer.setGraphicEventManager(gem);
					if (layer == null){
						throw new Error ("layer doesn't exist");
					}
					if (GraphicConstants.debugEnable)
						System.out.println("Load of Events acepted="+protocolEventsFileName);
					

					insertElements(eventsReaded,layer);
					reloadEventsList();
				}
			} catch (Exception e2) {
				JOptionPane.showConfirmDialog(principalFrame,
						"The protocol is incompatible or it hasn't been readed the configuration",
						"Protocol Error", JOptionPane.CLOSED_OPTION,
						JOptionPane.ERROR_MESSAGE);	
				e2.printStackTrace();
			}
		}
	}
	private void insertElements(Collection<GraphicEvent> elements, GraphicLayer layer){
		TreeSet<GraphicEvent> newTreeSet = new TreeSet<GraphicEvent>();
		while (events.size() > 0){
			GraphicEvent event;
			event = events.pollFirst();
			if (!layer.id.equals(event.layer.id)){
				newTreeSet.add(event);
			}
		}
		newTreeSet.addAll(elements);
		events = newTreeSet;
	}
	private void resetEnviroment(){
		enviroment.reset();
		events.clear();
		jTextAreaEventDescription.setText("");
		jTextAreaNodeStatusDescription.setText("");
		reloadEventsList();
		

	}
	private class ActionReset implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			reset();
		}
		
	}
	private void reset(){
		resetEnviroment();
		principalFrame.repaint();
	}
	private void loadStructure (){
		loadStructure(null);
	}
	private void loadStructure (String fileName){
		if (GraphicConstants.debugEnable) {
			System.out.println("Load Action");
		}

		int result = 0;
		File file = null;

		if (fileName != null) {
			fileNetworkConfiguration = fileName;
			file = new File(fileNetworkConfiguration);
			result = JFileChooser.APPROVE_OPTION;
		}

		else {
			JFileChooser fileChoser = new JFileChooser(".");
			fileChoser.setLocation(GraphicUtil.locationToBeCenter(
					principalFrame, fileChoser, true));
			result = fileChoser.showOpenDialog(principalFrame);
			if (result == JFileChooser.APPROVE_OPTION) {
				file = fileChoser.getSelectedFile();
				fileNetworkConfiguration = file.getAbsolutePath();
			}
		}
		if (result == JFileChooser.APPROVE_OPTION) {
			if (GraphicConstants.debugEnable) {
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
					resetEnviroment();
					// TODO estoy hay que arreglarlo y ponerlo de nuevo
					// TODO
					// TODO
					// TODO
					// TODO
					// TODO
//					configEnviroment = (Config) XMLUtils.doConfig(file
//							.getAbsolutePath());
					configEnviroment = new Config();
					String[] names = (String[]) configEnviroment
							.findSingle("IDS");
					for (String name : names) {
						if (GraphicConstants.debugEnable)
							System.out.println("Reading a node: "+ name);
						GraphicNode n = new GraphicNode(self);
						n.panel.addMouseMotionListener(new MoveItem(n));
						n.panel.addMouseListener(new MouseActionsOnNode(n));
						n.id = name;
						n.setState(0);
						// To paint all nodes by default;
						n.setPainted(true);
						n.config = (Config) configEnviroment
								.findSingle(name);
						@SuppressWarnings("unchecked")
						Hashtable<String, IPaddress> ht = (Hashtable<String, IPaddress>)n.config.findSingle("INTERFACESIPADDRESS");
						n.addresses = ht;
						n.principalAddress = new IPaddress((Long)n.config.findSingle("PRINCIPALADDR"));
						enviroment.nodes.put(n.id, n);
						IPaddress mask = n.principalAddress.getMaskedIPaddress(CIDR);
						if (enviroment.networks.containsKey(mask))
							enviroment.networks.get(mask).addNode(n);
						else {
							if (GraphicConstants.debugEnable){
								System.out.println("Creating a new network");
							}
							GraphicNetwork net = new GraphicNetwork(mask, self);
							// XXX
							net.panel.addMouseMotionListener(new MoveItem(net));
							enviroment.networks.put(mask,net);
							enviroment.networks.get(mask).addNode(n);
						}
						// Reading not physical layer
						@SuppressWarnings("unchecked")
						Enumeration sessions =  n.config.find("SESSIONS");
						if (sessions != null){
							while (sessions.hasMoreElements()){
								Config session = (Config)sessions.nextElement();
								// exist the protocol?
								String idProtocol = (String)session.findSingle("ID");
								GraphicLayer layer;
								if ((layer = enviroment.layers.get(idProtocol))== null){
									layer = new GraphicLayer(idProtocol, self);
									enviroment.layers.put(idProtocol, layer);
									if (GraphicConstants.debugEnable)
										System.out.println("Layer: \""+idProtocol+"\" added");
								}
								IPaddress addressOfTheProtocol = n.addresses.get(session.findSingle("INTERFACENAME"));
								layer.addNode(n, addressOfTheProtocol);
								@SuppressWarnings("unchecked")
								Enumeration neighbors = session.find("NEIGHBOURS");
								while (neighbors.hasMoreElements()){
									Configuration neighbor = (Config)neighbors.nextElement();
									IPaddress neighborAddress = (IPaddress)neighbor.findSingle("ADDRIPADDRESS");
									String neighborStringAddress = neighborAddress.toString();
									String myStringAddress = addressOfTheProtocol.toString();
									String link = null;
									if (myStringAddress.compareTo(neighborStringAddress) < 0){
										link = myStringAddress +"-"+ neighborStringAddress;
									}
									else
										link = neighborStringAddress +"-"+ myStringAddress;
									layer.addLink(link);
									layer.setNodeToLink(n, link);
									
								}
							}
						}
						// END READ OF OTHER LAYERS


					}

					// physical layer
					enviroment.layers.put("physical", new GraphicLayer("physical",self));
					if (GraphicConstants.debugEnable)
						System.out.println("Layer: \"physical\" added");

					@SuppressWarnings("unchecked")
					Enumeration<String> links_ = (Enumeration) configEnviroment
							.find("LINKS");
					Configuration configLink = (Configuration) configEnviroment
							.findSingle("TOPOLOGY");
					// Vector<String> links = new Vector<String>();
					while (links_.hasMoreElements()) {
						String linkName = links_.nextElement();
						@SuppressWarnings("unchecked")
						Enumeration<String> nodesOfALink = configLink.find(linkName);
						while (nodesOfALink.hasMoreElements()) {
							GraphicNode node = enviroment.nodes.get(nodesOfALink
									.nextElement());
							IPaddress principalAddress = new IPaddress(node.principalAddress);
							enviroment.layers.get("physical").addLink(linkName);
							enviroment.layers.get("physical").addNode(node,principalAddress);
							enviroment.layers.get("physical").setNodeToLink(node, linkName);
							
						}
						
						
//						Enumeration<Configuration>
					}

					// ENd of phisical layer
					// Start read phisical events
					
				} catch (Exception e) {
					JOptionPane.showConfirmDialog(principalFrame,
							"The file is incorrect or can't be readed!",
							"File error", JOptionPane.CLOSED_OPTION,
							JOptionPane.ERROR_MESSAGE);
					if (GraphicConstants.debugEnable)
						e.printStackTrace();
				}
			}
		}
		calculatePositions();
		activateLayer("physical");
		
		
		principalFrame.repaint();
	}
	private class ActionLoad implements ActionListener {
		

		// private Vector<GraphicNode> nodes;

		@Override
		public void actionPerformed(ActionEvent a) {
			if (GraphicConstants.fast)
				loadStructure("figura4-config.xml");
			else 
				loadStructure();

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

	public void activateLayer(String string) {
		this.centralPanel.remove(enviroment.layers.get(this.activeLayer).panel);
		String title = TITLE + ". Active Layer: " +string;
		principalFrame.setTitle(title);
		this.activeLayer = string;
		


		GraphicLayer layer = enviroment.layers.get(string);
		Enumeration <GraphicNode> allNodes = enviroment.nodes.elements();
		while (allNodes.hasMoreElements()){
			GraphicNode node = allNodes.nextElement();
			node.setPainted(false);
			node.setState(GraphicConstants.NO_ACTIVE_STATUS);
//			allNodes.nextElement().painted = false;
			
		}
		Enumeration<IPaddress> addressesOfTheNodes = layer.nodes.keys();
		
		while (addressesOfTheNodes.hasMoreElements()){
			IPaddress address = addressesOfTheNodes.nextElement();
			
			GraphicNode node = layer.nodes.get(address);
			node.setPainted(true);
			node.setState(GraphicConstants.NORMAL_STATUS);
			node.principalAddress = address;
			
		}
		this.centralPanel.add(enviroment.layers.get(this.activeLayer).panel);
		// Resize the networks
		renewNetsAreas();
		changeNodeStatus();
//		setDimensions();
		
	}

	private void calculatePositions() {
		GraphicLayer layer = enviroment.layers.get(activeLayer);
		this.centralPanel.add(layer.panel);
		layer.calculatePosition();
		int preferredSizeWidth = 0;
		int preferredSizeHeight = 0;
//		principalFrame.add(layer.panel);
		if (layer == null) {
			// TODO algo pasa
			return;
		}

		Dimension area = invReescalar(this.jScrollPaneCentral.getSize());
		// area = reescalar(area);
		// MORE EFICIENT if reescalar is called 1 less times

		int acumulatedY = 0;

		int actualX = GraphicConstants.START_POSITION_X;
		int actualY = GraphicConstants.START_POSITION_Y;
		// I think that the first network alocate is the first in the vector.
		// and the first network always be
		Enumeration<GraphicNetwork> nets = enviroment.networks.elements();
		while (nets.hasMoreElements()) {
			GraphicNetwork net = nets.nextElement();
			Vector<GraphicNode> nodesOfTheNet = net.nodesOfTheNetwork();
			// this network haven't nodes in this layer
			if (nodesOfTheNet == null || nodesOfTheNet.size() == 0) {
				net.setPainted(false);
				continue;
			}
			net.setPainted(true);
			Dimension netArea = net.calculateDimension();
			net.setStandarPanelArea(netArea);
			if (acumulatedY < actualY + netArea.height) {
				acumulatedY = actualY + netArea.height;
				
			}
			if (actualX + netArea.width + GraphicConstants.BORDER > area.width) {
				// The net is too large
				if (actualX == GraphicConstants.START_POSITION_X) {
					area.width = actualX + netArea.width + GraphicConstants.BORDER;
				}
				actualX = GraphicConstants.START_POSITION_X;
				actualY = acumulatedY + GraphicConstants.BORDER;
				acumulatedY = actualY + netArea.width;
			}
			net.setStandarPosition(new Point(actualX, actualY));
			if (GraphicConstants.debugEnable) {
//				System.out.println("Net: Position X=" + actualX + ",Y="
//						+ actualY + ",Width=" + netArea.getWidth() + ",Heigth="
//						+ netArea.getHeight());
			}
			actualX += netArea.width + GraphicConstants.BORDER;
			centralPanel.add(net.panel);
			net.calculatePositions();

		}
		
	}
	private void setDimensions() {
		
		Enumeration<GraphicLayer> layers = enviroment.layers.elements();
		while (layers.hasMoreElements()){
			layers.nextElement().setRatio(visualRatio);
		}
		Enumeration<GraphicNetwork> nets = enviroment.networks.elements();
		while(nets.hasMoreElements()){
			nets.nextElement().setRatio(visualRatio);
		}
		Enumeration <GraphicNode> nodes = enviroment.nodes.elements();
		while(nodes.hasMoreElements()){
			nodes.nextElement().setRatio(visualRatio);
		}
	}

	public int reescalar(double original) {
		return (new Double(original * visualRatio).intValue());
	}

	public Point reescalar(Point p) {
		if (p == null)
			return null;
		return new Point(reescalar(p.x), reescalar(p.y));

	}

	public Point invReescalar(Point p) {
		return new Point(invReescalar(p.x), invReescalar(p.y));
	}

	public Dimension reescalar(Dimension d) {
		return new Dimension(reescalar(d.width), reescalar(d.height));
	}

	public Dimension invReescalar(Dimension d) {
		return new Dimension(invReescalar(d.width), invReescalar(d.height));
	}

	public int invReescalar(double original) {
		return new Double(original / visualRatio).intValue();
	}
	
	public class ShowNodeNames implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			GraphicConstants.printNodesNames = !GraphicConstants.printNodesNames;
			centralPanel.repaint();
			
		}
		
	}
	public class ShowLinkNames implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			GraphicConstants.printLinksNames = !GraphicConstants.printLinksNames;
			centralPanel.repaint();
			
		}
		
	}
	public class MoveCentralPanels implements MouseMotionListener{
		Point mousePosition;
		@Override
		public void mouseDragged(MouseEvent e) {
			Point newMousePosition = e.getLocationOnScreen();
			int difX = newMousePosition.x - mousePosition.x;
			widthOfLeftPanel -= difX;
			principalPanel.repaint();
			mousePosition = newMousePosition;
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			mousePosition = e.getLocationOnScreen();
			
		}
	}
	public class MoveHorizontalDimension implements MouseMotionListener{
		Point mousePosition;
		Dimension d;
		
		public MoveHorizontalDimension(Dimension d){
			this.d = d;
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			Point newMousePosition = e.getLocationOnScreen();
//			Point oldPosition = object.getStandarPosition();
			int difY = newMousePosition.y - mousePosition.y;
			// The size must be > 0
			if (d.height + difY <= 0){
				return;
			}
			d.height += difY; 
			rightPanel.repaint();
//			}
			mousePosition = newMousePosition;
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			mousePosition = e.getLocationOnScreen();
			
		}
		
		
	}
	/**
	 * Action when click in a Node of the picture
	 * @author Francisco Huertas Ferrer
	 */
	public class MouseActionsOnNode extends MouseAdapter{
		GraphicNode node;
		public MouseActionsOnNode(GraphicNode object) {
			node = object;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (activeNode != node)
				activeNode = node;
			else
				activeNode = null;
			reloadEventsList();
			changeNodeStatus();
			
		}
	}
	
	public class MoveItem implements MouseMotionListener{
		GraphicElements object;
		Point mousePosition;
		public MoveItem(GraphicElements object_){
			this.object = object_;
		}
		@Override
		public void mouseDragged(MouseEvent e) {

			Point newMousePosition = e.getLocationOnScreen();
			Point oldPosition = object.getStandarPosition();
			int difX = invReescalar(newMousePosition.x - mousePosition.x);
			int difY = invReescalar(newMousePosition.y - mousePosition.y);
			object.setStandarPosition(new Point(oldPosition.x+difX, oldPosition.y+difY));
			centralPanel.repaint();
			if (GraphicConstants.debugEnable) {
//				System.out.println("(" + oldPosition.getX() + ","
//						+ oldPosition.getY() + ")" + "(" + e.getXOnScreen() + ","
//						+ e.getYOnScreen() + ")" + "(" + difX + "," + difY
//						+ ")");
			}
			mousePosition = newMousePosition;
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			mousePosition = e.getLocationOnScreen();
			// TODO Auto-generated method stub
			
		}

		
	}
	public class MousePopupMenuListener extends MouseAdapter{
		@Override
		public void mouseClicked(MouseEvent e) {
			
			if (e.getButton() == MouseEvent.BUTTON3){
//				panel.add
				if (GraphicConstants.debugEnable)
					System.out.println("Showing popup menu");
				PopupMenu p = new PopupMenu("Menu");
				Enumeration <GraphicLayer> layers = enviroment.layers.elements();
				// ACTION SHOW ALL NODES
				MenuItem menuItem = new MenuItem("Show all nodes");
				ActionListener action = new ShowAllNodes();
				menuItem.addActionListener(action);
				p.add(menuItem);
				// ACTION HIDDEN NO ACTIVE NODES
				menuItem = new MenuItem("Hidden no active nodes");
				action = new HiddenNoActiveNodes();
				menuItem.addActionListener(action);
				p.add(menuItem);

				
				
				p.add("-");
				p.add("Layers");
				p.add("-");
				while (layers.hasMoreElements()){
					GraphicLayer layer = layers.nextElement();
					menuItem = new MenuItem(layer.id);
					action = new ChangeToLayerAction(layer.id);
					menuItem.addActionListener(action);
					p.add(menuItem);
				}
				centralPanel.add(p);
				p.show(centralPanel, e.getX(), e.getY());
			}
		}
	}
	class ChangeToLayerAction implements ActionListener{
		String layerToChange;
		
		public ChangeToLayerAction(String layer){
			layerToChange = layer;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			if (GraphicConstants.debugEnable)
				System.out.println("Change to \""+layerToChange+"\" layer");
			activeLayer = layerToChange;
			activateLayer(layerToChange);
			centralPanel.repaint();
		}
		
	}
	class ShowAllNodes implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			Enumeration <GraphicNode>nodes = enviroment.nodes.elements();
			while (nodes.hasMoreElements()){
				GraphicNode node = nodes.nextElement();
				node.setPainted(true);
			}
			renewNetsAreas();
			centralPanel.repaint();
		}
		
	}
	class HiddenNoActiveNodes implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			Enumeration <GraphicNode>nodes = enviroment.nodes.elements();
			while (nodes.hasMoreElements()){
				GraphicNode node = nodes.nextElement();
				if (node.getState() == GraphicConstants.NO_ACTIVE_STATUS)
					node.setPainted(false);
			}
			renewNetsAreas();
//			setDimensions();
			centralPanel.repaint();
		}
		
	}
	private void activateEvent(GraphicEvent event){
		// si no son el mismo habra que repintarlo
		GraphicEvent oldEvent = eventShowed;
		// Si habia un evento hay que quitarlo si o si
		if (eventShowed != null){
			if (eventShowed.layer != null && eventShowed.destiny != null){
				String link = eventShowed.layer.getLinkBetweenNodes(eventShowed.origin, eventShowed.destiny);
				if (link != null){
					eventShowed.layer.linkStatus.remove(link);
					eventShowed.layer.linkStatus.put(link,GraphicConstants.LINK_NORMAL_STATUS);
				}
			}
			// Pongo el origen y el destino a a normal
			eventShowed.origin.setState(GraphicConstants.NORMAL_STATUS);
			if (eventShowed.destiny != null)
				eventShowed.destiny.setState(GraphicConstants.NORMAL_STATUS);
			jTextAreaEventDescription.setText("");
			jTextAreaNodeStatusDescription.setText("");
			centralPanel.repaint();
			eventShowed = null;
			activeNode = null;

		}
		if (event != null && (oldEvent == null || event != oldEvent)){
			if (event.layer != null){
				activateLayer(event.layer.id);
				if (event.destiny != null){
					String link = event.layer.getLinkBetweenNodes(event.origin, event.destiny);
					if (link != null){
						event.layer.linkStatus.remove(link);
						event.layer.linkStatus.put(link,GraphicConstants.LINK_ACTIVE_STATUS);
					}
				}
			}
			event.origin.setState(GraphicConstants.SENDED_STATUS);
			if (event.destiny != null)
				event.destiny.setState(GraphicConstants.RECIVED_STATUS);
			jTextAreaEventDescription.setText(event.layer.getGraphicEventManager().getMessageTypeName(event.type)+":\n"+event.message);
			activeNode = event.origin;
			
			eventShowed = event;
		}
		centralPanel.repaint();
		changeNodeStatus();
	}

		
		

	/**
	 * This class active/desactive a event when it's clicked
	 */
	private class ListMouseAction extends MouseAdapter {
		public void mouseClicked(MouseEvent e){
			Object objectList = jEventList.getSelectedValue();
			if (!(objectList instanceof GraphicEvent)){
				return;
			}
			GraphicEvent event = (GraphicEvent) objectList;
			activateEvent(event);
			
		}
		
	} // end of ListMouseAction
	private void changeNodeStatus(){
		if (activeNode == null)
		{
			
			return;
		}
		Iterator<GraphicEvent> eventsSet = events.descendingIterator();
		GraphicEvent lastEventThatReferenceANode = null;
		double time = (eventShowed != null)?eventShowed.time:0.0;
		while (eventsSet.hasNext()){
			GraphicEvent event = eventsSet.next();
			if (event.time > time )
				break;
			if (activeNode.id.equals(event.origin.id) && event.layer.id.equals(activeLayer)){
				System.out.println("selected event: "+event);
				if (event.descriptionState != null)
					lastEventThatReferenceANode = event;
				
			}
		}
		
		if (lastEventThatReferenceANode!= null){
			jTextAreaNodeStatusDescription.setText(lastEventThatReferenceANode.descriptionState);
		}
		else {
			jTextAreaNodeStatusDescription.setText("");
		}
	}	
	private void renewNetsAreas(){
		Enumeration<GraphicNetwork> nets = enviroment.networks.elements();
		if (nets != null){
			while (nets.hasMoreElements()){
				GraphicNetwork net = nets.nextElement();
				Dimension netArea = net.calculateDimension();
				net.setStandarPanelArea(netArea);
			}
		}
	}
	private void reloadEventsList(){
		DefaultListModel modelo = new DefaultListModel();
		jEventList.setBackground(GraphicConstants.LIST_OF_EVENTS_COLOR);
//		jEventList.setFont
		Iterator<GraphicEvent> orderedEvents = events.descendingIterator();
		if (activeNode != null){
			GraphicEvent firstElement = null;
			while (orderedEvents.hasNext()){
				GraphicEvent event = orderedEvents.next();
				
				if (event.time >= 0 && activeNode == event.origin){
					if (firstElement == null){
						firstElement = event;
					}
					GraphicEventManager gem = event.layer.getGraphicEventManager();
					if (gem.getEnableMessageType(event.type)){
						DecimalFormat df = new DecimalFormat("0.000");

//						String cadena = "Time:"+df.format(event.time)+" |Origin: "+event.origin.id+" |Type: "+event.type+" |Layer:"+event.layer.id;
						modelo.addElement(event);
					}
				}
			}
			// Si el evento activo no es del nodo lo quito
			if (eventShowed != null && eventShowed.origin != activeNode ){
				activateEvent(null);
				activeNode.setState(GraphicConstants.SENDED_STATUS);
			}
		}
		else{
			while (orderedEvents.hasNext()){
	
				GraphicEvent event = orderedEvents.next();
				if (event.time >= 0){
					GraphicEventManager gem = event.layer.getGraphicEventManager();
					if (gem.getEnableMessageType(event.type))
						modelo.addElement(event);
				}
			}
		}
		
		jEventList.setModel(modelo);
	}
	
	
	private class ActionOptionPanelGeneral implements ActionListener {
		JFrameOptionGeneral form;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			form = new JFrameOptionGeneral();
			form.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			form.setAlwaysOnTop(true);
			form.setTitle("General Options");
			
			

			
			GridBagLayout gridbag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			Container container = form.getContentPane();
			container.setLayout(gridbag);
			int acomulatedRow = 0;
			
			
			
			JLabel titleLabel = new JLabel();
			titleLabel.setText("General Opition");
			titleLabel.setFont(titleFont());
			c.gridx = 0;
			c.gridy = acomulatedRow++;
			c.gridwidth = 2;
			c.gridheight = 1;
			form.add(titleLabel,c);
			
			// CIDR Option
			JLabel CIDROption = new JLabel();
			CIDROption.setText("CIDR");
			CIDROption.setFont(normalFont());
			c.gridx = 0;
			c.anchor = GridBagConstraints.WEST;
			c.gridy = acomulatedRow;
			c.gridwidth = 1;
			c.gridheight = 1;
			form.add(CIDROption,c);
			
			form.jTextFieldCIDR  = new JTextField();
			form.jTextFieldCIDR.setText(""+CIDR);
			form.jTextFieldCIDR.setColumns(2);
			c.gridx = 1;
			c.gridy = acomulatedRow++;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.anchor = GridBagConstraints.EAST;
			form.add(form.jTextFieldCIDR,c);
			// END of CIDR Option
			
			// FontSizeOption Option
			JLabel FontSizeOption = new JLabel();
			FontSizeOption.setText("Font Size");
			FontSizeOption.setFont(normalFont());
			c.gridx = 0;
			c.anchor = GridBagConstraints.WEST;
			c.gridy = acomulatedRow;
			c.gridwidth = 1;
			c.gridheight = 1;
			form.add(FontSizeOption,c);
			
			form.jTextFieldFontSize  = new JTextField();
			form.jTextFieldFontSize.setText(""+normalFontSize);
			form.jTextFieldFontSize.setColumns(2);
			c.gridx = 1;
			c.gridy = acomulatedRow++;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.anchor = GridBagConstraints.EAST;
			form.add(form.jTextFieldFontSize,c);
			// END of FontSizeOption Option
			
			
			
			JButton acceptButton = new JButton("Accept");
			acceptButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					form.exitAccept = true;
					form.dispose();
					
				}
				
			});

			c.anchor = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.NONE;
			c.insets = new Insets(5, 5, 5, 5);
			c.gridy = acomulatedRow;
			c.gridx = 0;
			c.gridheight = 1;
			c.gridwidth = 1;
			container.add(acceptButton,c);
			JButton canceltButton = new JButton("Cancel");
			canceltButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					form.exitAccept = false;
					form.dispose();
					
				}
				
			});
			c.anchor = GridBagConstraints.CENTER;
			c.gridy = acomulatedRow;
			c.insets = new Insets(5, 5, 5, 5);
			c.gridx = 1;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.fill = GridBagConstraints.NONE;
			container.add(canceltButton ,c);
			
			
			
			form.pack();
//			form.setSize(300, 400);
			int posX = principalFrame.getX() + principalFrame.getWidth() / 2 - form.getWidth()/2;
			
			int posY = principalFrame.getY() + principalFrame.getHeight() /2 - form.getHeight()/2;
			form.setLocation(posX,posY);
			
			form.setVisible(true);
			
			
			
		}
		private class JFrameOptionGeneral extends JFrame {
			JTextField jTextFieldCIDR = null;
			JTextField jTextFieldFontSize = null;
			public void dispose(){
				if (exitAccept){
					boolean confirmReload = false;
					boolean confirmReloadNeeded = false;
					normalFontSize = new Integer(jTextFieldFontSize.getText());
					int newCIDR = new Integer(jTextFieldCIDR.getText());
					if (newCIDR >= 0 && newCIDR <= 32 && newCIDR != CIDR){
						confirmReloadNeeded = true;
						int result = JOptionPane.showConfirmDialog(form,"To apply the new configuration is necesary reload the structure. Are you sure?","Alert",JOptionPane.YES_NO_OPTION);
						if (result == JOptionPane.YES_OPTION){
							confirmReload = true;
							CIDR = newCIDR;
						}
					}
					if (confirmReload && confirmReloadNeeded){
						reset();
						loadStructure(fileNetworkConfiguration);
					}else{
						principalFrame.repaint();
					}
					
				}
				
				super.dispose();
			}
			protected boolean exitAccept;
		}
		
	} // END of ActionOptionPanelGeneral
	private class ActionOptionPanelLayer  implements ActionListener {
		private Vector <JCheckBoxLayerVisibilty> checkBoxs = new Vector<JCheckBoxLayerVisibilty>();
		private JFrameOption optionFrame;
		@Override
		public void actionPerformed(ActionEvent e) {
			optionFrame = new JFrameOption("Layer Options") ;
			
			optionFrame.setAlwaysOnTop(true);
			optionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			GridBagLayout gridbag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			Container container = optionFrame.getContentPane();
			container.setLayout(gridbag);
			
			
			
			int acomulatedRow = 0;
			Enumeration <GraphicLayer> gLayers = enviroment.layers.elements(); 
			while (gLayers.hasMoreElements())
			{
				GraphicLayer gl = gLayers.nextElement();
				
			
				GraphicEventManager gem = gl.getGraphicEventManager();
				if (gem != null) {
					// Field: visible layers
					JLabel titlelabel = new JLabel(gl.id);
					titlelabel.setFont(titleFont());
					c.gridx = 0;
					c.gridy = acomulatedRow;
					c.gridheight = 1;
					c.gridwidth = 3;
					acomulatedRow += 1;
					container.add(titlelabel,c);

//					c.gridy = acomulatedRow;
//					acomulatedRow++;
//					JLabel subTitle = new JLabel("visible layers");
//					subTitle.setFont(subTitleFont());
//					container.add(subTitle,c);
					
					for (int i = 0; i < gem.getTypesOfMessage();i++){
						JLabel messageType = new JLabel("Type "+i+": "+gem.getMessageTypeName(i));
						messageType.setFont(normalFont());
						c.ipadx = 10;
						c.gridy = acomulatedRow;
						c.gridx = 0;
						c.gridheight = 1;
						c.gridwidth = 2;
						c.anchor = GridBagConstraints.NORTHWEST;
						container.add(messageType,c);
						JCheckBoxLayerVisibilty checkBox = new JCheckBoxLayerVisibilty(gem,i);
						checkBoxs.add(checkBox);
//						checkBox.setSelected(gem.getEnableMessageType(i));
						
						
						c.anchor = GridBagConstraints.CENTER;
						c.gridx = 2;
						c.ipadx = 0;
						container.add(checkBox,c);

						
						acomulatedRow++;
					}
				}
				// Accept Exit button
				
			}

			JButton acceptButton = new JButton("Accept");
			acceptButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					optionFrame.exitAccept = true;
					optionFrame.dispose();
				}
			});

			c.anchor = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.NONE;
			c.insets = new Insets(5, 5, 5, 5);
			c.gridy = acomulatedRow;
			c.gridx = 0;
			c.gridheight = 1;
			c.gridwidth = 1;
			container.add(acceptButton,c);
			JButton canceltButton = new JButton("Cancel");
			canceltButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					optionFrame.exitAccept = false;
					optionFrame.dispose();
				}
			});
			c.anchor = GridBagConstraints.CENTER;
			c.gridy = acomulatedRow;
			c.insets = new Insets(5, 5, 5, 5);

			c.gridx = 1;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.fill = GridBagConstraints.NONE;
			container.add(canceltButton ,c);
			// TODO Auto-generated method stub
			optionFrame.pack();
			int posX = principalFrame.getX() + principalFrame.getWidth() / 2 - optionFrame.getWidth()/2;
			
			int posY = principalFrame.getY() + principalFrame.getHeight() /2 - optionFrame.getHeight()/2;
			optionFrame.setLocation(posX,posY);
			optionFrame.setVisible(true);

			
			
		}
		@SuppressWarnings("serial")
		private class JFrameOption extends JFrame {
			/**
			 * 
			 */
			public boolean exitAccept = false;
			public JFrameOption(String string) {
				super(string);
			}
			@Override
			public void dispose(){
				for (JCheckBoxLayerVisibilty checkBox : checkBoxs){
					if (exitAccept)
						checkBox.setVisibilityMessage();
				}
				reloadEventsList();
				this.setAlwaysOnTop(false);
				this.setVisible(false);
				
				principalFrame.setAlwaysOnTop(true);
				principalFrame.setAlwaysOnTop(false);
				
				super.dispose();
				
			}


		} // END JFrameOption
			
		
	} // END ActionOptionPanelLayer
	private class JCheckBoxLayerVisibilty extends JCheckBox {
		private GraphicEventManager asociatedGem = null;
		private int asociatedTypeMessage = -1;
		/**
		 * 
		 */
		private static final long serialVersionUID = 1561359020200489443L;
		public JCheckBoxLayerVisibilty(GraphicEventManager gem, int messageType) {
			// TODO Auto-generated constructor stub
			super();
			asociatedGem = gem;
			asociatedTypeMessage = messageType;
			this.setSelected(gem.getEnableMessageType(messageType));
			
		}
		public void setVisibilityMessage(){
			asociatedGem.setEnableMessageType(asociatedTypeMessage, this.isSelected());
		}
		
	} // end class JCheckBoxLayerVisibilty
	
	protected Font normalFont (){
//		Font font = principalFrame.getFont();
		
		return new Font("Monospace",Font.PLAIN,normalFontSize);
	}
	protected Font subTitleFont (){
		Font normalFont = normalFont();
		return  new Font(normalFont.getFontName(),Font.BOLD,normalFont.getSize());
	}
	protected Font titleFont(){
		Font normalFont = normalFont();
		return new Font(normalFont.getFontName(),Font.BOLD,normalFont.getSize()+2);
	}
	private Dimension calculateCentralArea(){
		Dimension dimension = new Dimension(0,0);
		Enumeration <GraphicNetwork> nets = enviroment.networks.elements();
		while (nets.hasMoreElements()){
			GraphicNetwork net = nets.nextElement();
			if (net.getStandarPosition().x + net.getStandarPanelArea().width > dimension.width)
				dimension.width = net.getStandarPosition().x + net.getStandarPanelArea().width;
			if (net.getStandarPosition().y + net.getStandarPanelArea().height > dimension.height)
				dimension.height = net.getStandarPosition().y + net.getStandarPanelArea().height;
		}
		return dimension;
	}
}
