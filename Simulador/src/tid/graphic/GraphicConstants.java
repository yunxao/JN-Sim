package tid.graphic;

import java.awt.Color;

public class GraphicConstants {

	// finals
	

	
	public final static int NORMAL_STATUS 			= 0;
	public final static int SELECT_STATUS 			= 1;
	public final static int RECIVED_STATUS			= 2;
	public final static int SENDED_STATUS			= 3;
	public final static int NO_ACTIVE_STATUS 		= 4;
	public final static String []ROUTE_IMAGES_NODE ={"images/node0.png","images/node1.png","images/node2.png","images/node3.png","images/node4.png"};


	
	public final static int LINK_NORMAL_STATUS	= 0;
	public final static int LINK_ACTIVE_STATUS	= 1;
	public final static int LINK_DOWN_STATUS	= 2;
	public final static Color[] LINK_COLORS = {new Color(0,0,0),new Color(255,0,0), new Color(180,180,180)};
	
	public final static int START_POSITION_X = 10;
	public final static int START_POSITION_Y = 10;
	public final static int BORDER = 10;
	public final static int FINE_STROKE = 2; 
	public final static int GROSS_STROKE = 5;
	
	public final static Color NORMAL_COLOR = new Color(0,0,0);
	public final static Color NORMAL_COLOR_LINE = NORMAL_COLOR;
	public final static Color NORMAL_COLOR_TEXT = new Color(0,0,255);
	
	protected final static Color BACKGROUND_COLOR = new Color(255,255,255);
	

	protected final static int LIST_OF_EVENTS_WIDHT = 330;
	protected final static double LIST_OF_EVENTS_HEIGHT_PERCENT = 0.5;
	protected final static double EVENT_DETAIL_HEIGHT_PERCENT= 0.25;
	protected final static int WIDTH = 800;
	protected final static int HEIGHT = 600;

	protected final static Color LIST_OF_EVENTS_COLOR = new Color(255, 255, 255);
	public static final int DEFAULT_CIDR = 24;
	public static final int FONT_SIZE = 10;
	
	//enviroment
	protected static Boolean debugEnable = false;
	protected static Boolean fast = false;
	protected static Boolean printLinksNames = false;
	protected static Boolean printNodesNames = true;
	protected static Boolean jar = false;
	
	

}
