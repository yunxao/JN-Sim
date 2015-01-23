package tid.events;

import java.util.List;
import java.util.Vector;

import org.jdom.Element;

import tid.Enviroment;
/**
 * Up a node or link and all protocols related with it
 * @author Francisco Huertas
 *
 */
public class UP extends Event{
	public Vector<String> components;
	public UP(){
		super();
		components = new Vector<String>();
	}
	public void fromXML(Element xml){
		String value;
		value = xml.getAttributeValue("type");
		if (value == null || !value.equals("UP")){
			throw new Error("UP (fromXML): Incorrect type event");
		}
		value = xml.getAttributeValue("id");
		if (value == null)
			throw new Error("UP (fromXML): Event must have a id");
		id = value;
		this.message = xml.getAttributeValue("message");
		@SuppressWarnings("unchecked")
		List<Element> components = xml.getChildren("component");
		
		for (Element component:components){
			this.components.add(component.getAttributeValue("id"));
		}
		
		
		
		this.configured = true;
	}
	@Override
	public void execute() {
		for (int i = 0; i < components.size();i++){
			tid.inet.InetUtil.reConnect(components.get(i));	
		}
		
	}
	
	public String toString(){
		String cadena;
		cadena = "Event: UP. Components restarted: ";
		for (int i = 0;i<components.size();i++){
			cadena += components.get(i)+", ";
		}
		if (message != null)
			cadena += "Message: "+this.message;
		return cadena;
	}


}
