package tid.graphic;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.jdom.Document;

import infonet.javasim.bgp4.BGPSession;
/**
 * This class is a instance of {@link GraphicEventManager} to {@link infonet.javasim.bgp4.BGPSession BGPSession} protocol 
 * @author Francisco Huertas Ferrer
 *
 */
public class GraphicBGPEventManager extends GraphicEventManager {
	
	private Document xmlDocument = null;
	private String name = null;
	
//	private final int NUM_TYPES_OF_MESSAGE = 12;
	public static final int DEBUG_MESSAGE_LOW = 0;
	public static final int DEBUG_MESSAGE_MEDIUM = 1;
	public static final int DEBUG_MESSAGE_HIGH = 2;
	public static final int FSM_MESSAGE = 3;
	public static final int RT_MESSAGE = 4;
	public static final int MESSAGE_TRAFIC_UNKNOW = 5;

	public static final int MESSAGE_TRAFIC_KAI = 6;
	public static final int MESSAGE_TRAFIC_NOTIFICATION = 7;
	public static final int MESSAGE_TRAFIC_OPEN = 8;
	public static final int MESSAGE_TRAFIC_STAR_STOP = 9;
	public static final int MESSAGE_TRAFIC_TRANSPORT = 10;
	public static final int MESSAGE_TRAFIC_UPDATE = 11;
	public static final int APPLIED_IN_POLICIES = 12;
	public static final int APPLIED_OUT_POLICIES= 13;
	
	
	
	
	private final String[] TYPES = {
			"Debug message low priority", 
			"Debug message medium priority",
			"Debug message high priority",
			"Finite State Machine message",
			"Route Table Message",
			"Message trafic type: unknown", 
			"Message trafic type: Keep Alive", 
			"Message trafic type: Notification", 
			"Message trafic type: Open", 
			"Message trafic type: Start-Stop", 
			"Message trafic type: Transport", 
			"Message trafic type: Update",
			"Applied inbound policies",
			"Applied outbound policies",
			
			};
	
	private boolean[] statusMessage;
	
	/**
	 * Create a new instance of GraphicBGPEventManager without the initialization of xmlFile
	 */
	public GraphicBGPEventManager(){
		statusMessage = new boolean[getTypesOfMessage()];
		for (int i = 0; i < statusMessage.length;i++){
			statusMessage[i] = true;
		}
		statusMessage[DEBUG_MESSAGE_LOW] = false;
		statusMessage[DEBUG_MESSAGE_MEDIUM] = false;
		statusMessage[DEBUG_MESSAGE_HIGH] = false;
//		statusMessage[FSM_MESSAGE] = false;
//		statusMessage[RT_MESSAGE] = false;
		statusMessage[MESSAGE_TRAFIC_UNKNOW] = false;
		statusMessage[MESSAGE_TRAFIC_KAI] = false;
//		statusMessage[MESSAGE_TRAFIC_NOTIFICATION] = false;
//		statusMessage[MESSAGE_TRAFIC_OPEN] = false;
//		statusMessage[MESSAGE_TRAFIC_STAR_STOP] = false;
//		statusMessage[MESSAGE_TRAFIC_TRANSPORT] = false;
//		statusMessage[MESSAGE_TRAFIC_UPDATE] = false;
//		statusMessage[APPLIED_POLICIES] = false;

	}
	/**
	 * Create a new instance of GraphicBGPEventManager with the initialization of xmlFile and requesting file name
	 * @param protocolName name of the protocol
	 */
	public GraphicBGPEventManager(String protocolName){
		super();
		initFile(protocolName);
	}
	/**
	 * Create a new instance of GraphicBGPEventManager with the initialization of xmlFile 
	 * @param protocolName name of protocol
	 * @param fileName name of the file where it's saved 
	 */
	public GraphicBGPEventManager(String protocolName, String fileName){
		super();
		initFile(protocolName,fileName);
	}
	public void initFile(String protocolName){
		initFile(protocolName,null);
	}
	public void initFile(String protocolName, String fileName){
		xmlDocument = super.initFile(protocolName,"tid.graphic.GraphicBGPEventManager", "infonet.javasim.bgp4.BGPSession");
		if (fileName != null){
			name = fileName;
		}
		else if (GraphicConstants.fast){
			name = protocolName+".xml";
		}
		else{
			JFileChooser fileChooser = new JFileChooser(".");
			fileChooser.setSelectedFile(new File(protocolName+".xml"));
			FileFilter filter = new FileFilter() {
				
				@Override
				public String getDescription() {
					return "xml Files";
				}
				
				@Override
				public boolean accept(File f) {
					if (f.isDirectory())
						return true;
					return f.getName().toLowerCase().endsWith(".xml");
				}
			};
			fileChooser.setFileFilter(filter);
			
			int result = fileChooser.showSaveDialog(null);
			if (result == JFileChooser.APPROVE_OPTION){
				name = fileChooser.getSelectedFile().getAbsolutePath();
			}
			
		}
	}
	/**
	 * add Clause to a xmlDocument of this instance, initialization of xml file is needed
	 * @param time Time of the event 
	 * @param origin Origin of the event. It's can be name of the node or address
	 * @param destiny Destiny of the event. 
	 * @param type integer value of type message. 
	 * @param message message of the event
	 * @param state state of the node 
	 * @see GraphicBGPEventManager#getTypesOfMessage()
	 * @see GraphicBGPEventManager#getMessageTypeName(int)
	 */
	public void addClause (Double time, String origin, String destiny,int type, String message, String state){
		if (xmlDocument != null)
			super.addClause(time, origin, destiny, type, message, state, xmlDocument);
	}
	
	public void writeDocuemnt(){
		if (name != null && xmlDocument != null)
			super.writeDocuemnt(name, xmlDocument);
	}
	
	@Override
	public String getMessageTypeName(int type) {
		if (type >= getTypesOfMessage())
			return null;
		return TYPES[type];
	}

	@Override
	public int getTypesOfMessage() {
		return TYPES.length;
	}

	@Override
	public String getProtocolClass() {
		return BGPSession.class.getName();
	}
	@Override
	public String getManagerClassName() {
		return GraphicBGPEventManager.class.getName();
	}
	@Override
	public void setEnableMessageType(int type,boolean enable) {
		if (type < getTypesOfMessage())
			this.statusMessage[type] = enable;
		
	}

	@Override
	public boolean getEnableMessageType(int type) {
		if (type < getTypesOfMessage()){
			return this.statusMessage[type];
		}
		return false;
	}

	
}
