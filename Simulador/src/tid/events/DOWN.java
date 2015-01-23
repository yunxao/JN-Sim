package tid.events;

import java.util.Vector;
import java.util.List;

import org.jdom.Element;


public class DOWN extends Event{
	public Vector<String> components;
	public DOWN(){
		super();
		components = new Vector<String>();
	}
	public void fromXML(Element xml){
		String value;
		value = xml.getAttributeValue("type");
		if (value == null || !value.equals("DOWN")){
			throw new Error("DownEvent (fromXML): Incorrect type event");
		}
		value = xml.getAttributeValue("id");
		if (value == null)
			throw new Error("Event must have a id");
		id = value;
		this.message = xml.getAttributeValue("message");
		@SuppressWarnings("unchecked")
		List<Element> components = xml.getChildren("component");
		
		for (Element component:components){
			this.components.add(component.getAttributeValue("id"));
		}
		
		
		
		this.configured = true;

	}
	public void execute (){
		if (configured){
			for (int i = 0; i < components.size(); i++)
				tid.inet.InetUtil.disconnect(components.get(i));
		}
		else
			System.err.println("Event isn't configurated");
	}
	public String toString(){
		String cadena;
		cadena = "Event: DOWN. Components downed: ";
		for (int i = 0;i<components.size();i++){
			cadena += components.get(i)+", ";
		}
		if (message != null)
			cadena += "Message: "+this.message;
		return cadena;
	}

}
