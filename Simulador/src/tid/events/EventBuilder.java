package tid.events;

import infonet.javasim.bgp4.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;

import tid.Enviroment;

import com.sun.xml.internal.ws.org.objectweb.asm.Type;

import drcl.comp.Component;
import drcl.inet.Network;

/**
 * Build a Vector with all events of a file. And set Enviroment vars (time, rtlog...)<br>
 * The format of XML file can be see in test_1.xml
 * @author Francisco Huertas Ferrer
 *
 */
public class EventBuilder {
	/**
	 * Build a Vector from a xmlfile.
	 * @param fname route of file
	 * @return Vector of events
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static Vector<Pair<Double,Event>> doEventsFromXML(String fname) throws JDOMException, IOException{
		HashMap<String,Event>events = new HashMap<String,Event>();
		Vector <Pair<Double,Event>> execution = new Vector<Pair<Double,Event>>();
		File xmlFile = new File(fname);
		SAXBuilder builder = new SAXBuilder ();
		org.jdom.Document doc = builder.build (xmlFile);
		org.jdom.Element root = doc.getRootElement ();
		org.jdom.Element simulation = root.getChild("simulation");
		if (simulation == null)
			throw new Error("Configutation don't exist");
		// Parameters
		tid.Enviroment.time = Long.valueOf(simulation.getAttributeValue("time"));
		
		//Events
		org.jdom.Element listOfEvents = root.getChild("events");
		if (listOfEvents != null){
			@SuppressWarnings("unchecked")
			List<Element> listEvents =(List<Element>)listOfEvents.getChildren("event");
			for(Element event:listEvents){
				try {
					Event newEvent;
					newEvent = (Event) Class.forName("tid.events."+event.getAttributeValue("type")).newInstance();
					newEvent.fromXML(event);
					events.put(newEvent.id,newEvent);
				} catch (Exception e){
					System.err.println("EventBuilder(doEventsXML): A event hasn't been added");
					e.printStackTrace();
				}
				
			}
		}
		//Executions
		
		@SuppressWarnings("unchecked")
		List <Element> listOfExecution = simulation.getChildren("execute");
		for (Element elementExec:listOfExecution){
			String value = elementExec.getAttributeValue("time");
			if (value == null)
				throw new Error("EventBuilder(doEventsXML): There is't time in a event");
			Double time = Double.valueOf(value);
			value = elementExec.getAttributeValue("id");
			if (value == null)
				throw new Error("EventBuilder(doEventsXML): There is not id for the event");
			Event e = events.get(value);
			if (e == null)
				throw new Error("EventBuilder(doEventsXML): "+value +"is not a event id valid");
			execution.add(new Pair<Double, Event>(time,e));
		}
		return execution; 
		
	};
	


}
