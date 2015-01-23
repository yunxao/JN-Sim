package tid.graphic;

import infonet.javasim.util.IPaddress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public abstract class GraphicEventManager {
	private TreeSet<GraphicEvent> events = null;
	private String protocolClass = null;
	private String protocolName = null;
	private String managerClass = null;
//	private Document xmlDocument;
	
	public Document initFile(String name, String managerClass, String protocolClass){
		Document xmlDocument = new Document(new Element("procotol"));
		xmlDocument.getRootElement().setAttribute("name", name);
		xmlDocument.getRootElement().setAttribute("managerClass",managerClass);
		xmlDocument.getRootElement().setAttribute("protocolClass",protocolClass);
		return xmlDocument;
	}
	public void addClause (Double time, String origin, String destiny,int type, String message, String state, Document xmlFile){
		Element element = new Element("event");
		element.setAttribute("time", ""+time);
		element.setAttribute("origin", origin);
		if (destiny != null)
			element.setAttribute("destiny", destiny);
		element.setAttribute("type", ""+type);
		element.setAttribute("message", message);
		if (state != null)
			element.setAttribute("state", state);
		xmlFile.getRootElement().addContent(element);
	}
	public void writeDocuemnt(String path, Document xmlDocument){
		
		
		try {
			XMLOutputter writter = new XMLOutputter(Format.getPrettyFormat());
			FileOutputStream file = new FileOutputStream(path);
			writter.output(xmlDocument,file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	public TreeSet<GraphicEvent> readEvents(String filename, GraphicEnviroment enviroment){
		events = new TreeSet<GraphicEvent>();
		File xmlFile = new File(filename);
		SAXBuilder builder = new SAXBuilder ();
		try {
			org.jdom.Document doc = builder.build (xmlFile);
			org.jdom.Element root = doc.getRootElement ();
			protocolName = root.getAttributeValue("name");
			protocolClass = root.getAttributeValue("protocolClass"); 
			managerClass = root.getAttributeValue("managerClass");
			GraphicLayer gl = enviroment.layers.get(protocolName);
			if (gl == null){
				if (GraphicConstants.debugEnable)
					System.out.println("Layer don't exist");
				throw new Error("Layer don't exist");
			}
			
			@SuppressWarnings("unchecked")
			List<org.jdom.Element> xmlEvents = root.getChildren("event");
			for (org.jdom.Element xmlEvent : xmlEvents){
				String auxCad;
				GraphicEvent event = new GraphicEvent();
				event.time = new Double (xmlEvent.getAttributeValue("time"));
				event.layer = gl;
				// if the origin is null maybe the origin is a address 
				auxCad = xmlEvent.getAttributeValue("origin");
				if (auxCad != null){
					
					event.origin = enviroment.nodes.get(auxCad);
					if (event.origin == null){
						event.origin = gl.nodes.get(new IPaddress(auxCad));
					}
					if (event.origin == null){
						throw new Error ("Origin" + auxCad + "Can't be recognized");
					}
				}
				else
					throw new Error("There can be a origin");
				
				auxCad = xmlEvent.getAttributeValue("destiny");
				if (auxCad != null){
					
					event.destiny = enviroment.nodes.get(auxCad);
					if (event.destiny == null){
						event.destiny = gl.nodes.get(new IPaddress(auxCad));
					}
					if (event.destiny == null){
						throw new Error ("Destiny " + auxCad + " Can't be recognized");
					}
				}
				
				event.type = Integer.valueOf(xmlEvent.getAttributeValue("type"));
//				event.manager = (GraphicEventManager) Class.forName(managerClass).newInstance();
				event.message = xmlEvent.getAttributeValue("message");
				event.descriptionState = xmlEvent.getAttributeValue("state");
				if (event.origin == null)
					System.out.println("");
				events.add(event);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			events = null;
			return null;
		} 

		return events;
	}
	

	public String getProtocolName(){
		if (protocolName == null && GraphicConstants.debugEnable)
			System.out.println("'readEvents' must be called to use this function");
		return protocolName;
	}
	public String getProtocolName(String fname){
		File xmlFile = new File(fname);
		SAXBuilder builder = new SAXBuilder ();
		try {
			org.jdom.Document doc = builder.build (xmlFile);
			org.jdom.Element root = doc.getRootElement ();
			protocolName = root.getAttributeValue("name");
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
		return protocolName;
	}	
	public String getProtocolClass(){
		if (protocolClass == null && GraphicConstants.debugEnable)
			System.out.println("'readEvents' must be called to use this function");
		return protocolClass;
	}
	public String getProtocolClass(String fname){
		File xmlFile = new File(fname);
		SAXBuilder builder = new SAXBuilder ();
		try {
			org.jdom.Document doc = builder.build (xmlFile);
			org.jdom.Element root = doc.getRootElement ();
			return  root.getAttributeValue("protocolClass");
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
	public static String getManagerClassName(String fName){

		File xmlFile = new File(fName);
		SAXBuilder builder = new SAXBuilder ();
		try {
			org.jdom.Document doc = builder.build (xmlFile);
			org.jdom.Element root = doc.getRootElement ();
			return root.getAttributeValue("managerClass");
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
	public static GraphicEventManager getManagerClass(String fName) throws Exception{
		
		if (GraphicConstants.debugEnable)
				System.out.println(getManagerClassName(fName));
		return (GraphicEventManager) Class.forName(getManagerClassName(fName)).newInstance();
		
		
	}
	public abstract String getManagerClassName();
	public abstract String getMessageTypeName(int type);
	/**
	 * Indicates the number of message types of a GraphicEventManager concrete 
	 * @return the number of message types
	 */
	public abstract int getTypesOfMessage();
	public abstract void setEnableMessageType(int type,boolean enable);
	public abstract boolean getEnableMessageType(int type);


}
