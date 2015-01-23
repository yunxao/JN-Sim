package tid.graphic;

import infonet.javasim.bgp4.util.Pair;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.Vector;

import org.jdom.input.SAXBuilder;


public class GraphicPhysicalEventManager extends GraphicEventManager {
	private final int NUM_TYPES_OF_MESSAGE = 2;
	private final int INITIAL_STATE = 0;
	private final int EVENT = 1; 
	private final String[] TYPES = {
			"Initial State", 
			"Phisical event"
			};
	private boolean[] statusMessage = { 
		true,
		true
		};
	@Override
	public String getMessageTypeName(int type) {
		if (NUM_TYPES_OF_MESSAGE > type)
			return TYPES[type];
		return null;
	}

	@Override
	public int getTypesOfMessage() {
		
		return NUM_TYPES_OF_MESSAGE;
	}

	@Override
	public TreeSet<GraphicEvent> readEvents(String filename, GraphicEnviroment enviroment) {
		TreeSet<GraphicEvent> events = new TreeSet<GraphicEvent>();
		
		Hashtable<String,Hashtable<String,Integer>> relationBetweenNodes = new Hashtable<String, Hashtable<String,Integer>>();;
		Vector <Pair<Double,tid.events.Event>> eventsReaded;
		boolean existAEventNotCompatible = false;
		// TODO event w initial state
		Enumeration<GraphicNode> nodes_0 = enviroment.nodes.elements();
		while (nodes_0.hasMoreElements()){
			GraphicNode node = nodes_0.nextElement();
			relationBetweenNodes.put(node.id, new Hashtable<String, Integer>());
//			GraphicLayer physicalLayer = enviroment.layers.get("physical");
//			physicalLayer.
			GraphicEvent event = new GraphicEvent();
			event.time = 0.0;
			event.layer = enviroment.layers.get("physical");
			event.type = INITIAL_STATE;
			event.origin = node;
			event.destiny = null;
			event.message = "Initial state of node "+node.id;
//			event.manager = this;
			event.descriptionState = node.toString(); 
			events.add(event);
		}
		

		try {
			eventsReaded = tid.events.EventBuilder.doEventsFromXML(filename);
			
			for (int i = 0; i < eventsReaded.size();i++){
				tid.events.Event event = eventsReaded.get(i).item2();
				
				if (GraphicConstants.debugEnable)
					System.out.println("Physical event. Time="+eventsReaded.get(i).item1()+", Event="+eventsReaded.get(i).item2());
				if (event instanceof tid.events.UP){
					 tid.events.UP eventUP = (tid.events.UP) event;
					 // if there are more of one component in the simulation event, they are separated in individuals events
					 for (int j = 0; j < eventUP.components.size();j++){
							GraphicEvent graphicalEvent = new GraphicEvent();
							// Time of the event
							graphicalEvent.time = eventsReaded.get(i).item1();
							graphicalEvent.layer = enviroment.layers.get("physical");
							graphicalEvent.type = EVENT;
							graphicalEvent.origin = enviroment.nodes.get(eventUP.components.get(j));
//							graphicalEvent.manager = this;

							// If it's not a node, the component is another component
							if (graphicalEvent.origin == null){
								// testing if the node is a Link
								Vector <GraphicNode>nodes = graphicalEvent.layer.links.get(eventUP.components.get(j));
								if (nodes != null){
									// origin and destiny are selected arbitrarily
									graphicalEvent.origin = nodes.get(0);
									graphicalEvent.destiny = nodes.get(1);
//									graphicalEvent.descriptionState = nodes.get(0).toString();
									graphicalEvent.message = "Down a link between  "+graphicalEvent.origin.id+" and "+graphicalEvent.destiny.id;
								}
								// Unsupported component
								else{
									existAEventNotCompatible = true;
									if (GraphicConstants.debugEnable)
										System.out.println("The event not compatible is: "+event);
								}

							}
							// It's a node 
							else{
								graphicalEvent.descriptionState = graphicalEvent.origin.toString();
								graphicalEvent.destiny = null;
								graphicalEvent.message = "Up the node "+graphicalEvent.origin.id;
							}

							
							
							events.add(graphicalEvent);
					 }
				}
				else if (event instanceof tid.events.DOWN){
					 tid.events.DOWN eventDOWN = (tid.events.DOWN) event;
					 // if there are more of one component in the simulation event, they are separated in individuals events
					 for (int j = 0; j < eventDOWN.components.size();j++){
							GraphicEvent graphicalEvent = new GraphicEvent();
							// Time of the event
							graphicalEvent.time = eventsReaded.get(i).item1();
							graphicalEvent.layer = enviroment.layers.get("physical");
							graphicalEvent.type = EVENT;
							graphicalEvent.origin = enviroment.nodes.get(eventDOWN.components.get(j));
//							graphicalEvent.manager = this;

							// If it's not a node, the component is another component
							if (graphicalEvent.origin == null){
								// testing if the node is a Link
								Vector <GraphicNode>nodes = graphicalEvent.layer.links.get(eventDOWN.components.get(j));
								if (nodes != null){
									// origin and destiny are selected arbitrarily
									graphicalEvent.origin = nodes.get(0);
									graphicalEvent.destiny = nodes.get(1);
//									graphicalEvent.descriptionState = nodes.get(0).toString();
									graphicalEvent.message = "Down a link between  "+graphicalEvent.origin.id+" and "+graphicalEvent.destiny.id;
								}
								// Unsupported component
								else{
									existAEventNotCompatible = true;
									if (GraphicConstants.debugEnable)
										System.out.println("The event not compatible is: "+event);
								}

							}
							// It's a node 
							else{
								graphicalEvent.descriptionState = graphicalEvent.origin.toString();
								graphicalEvent.destiny = null;
								graphicalEvent.message = "Down the node "+graphicalEvent.origin.id;
							}

							
							
							events.add(graphicalEvent);
					 }
				}
				else  {
					existAEventNotCompatible = true;
					if (GraphicConstants.debugEnable)
						System.out.println("The event not compatible is: "+event);
				}
			}
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if (existAEventNotCompatible)
			return null;
		
		return events;
	}

	@Override
	public String getProtocolName() {
		return "physical";
	}
	@Override
	public String getProtocolName(String fname) {
		return "physical";
	}

	@Override
	public String getManagerClassName() {
		return GraphicPhysicalEventManager.class.getName();
	}



	@Override
	public String getProtocolClass() {
		if (GraphicConstants.debugEnable)
			System.out.println("physical layer don't have ProtocolClass");
		return null;
	}	
	@Override
	public String getProtocolClass(String fName) {
		if (GraphicConstants.debugEnable)
			System.out.println("physical layer don't have ProtocolClass");
		return null;
	}

	@Override
	public void setEnableMessageType(int type,boolean enable) {
		if (type < NUM_TYPES_OF_MESSAGE)
			this.statusMessage[type] = enable;
		
	}

	@Override
	public boolean getEnableMessageType(int type) {
		if (type < NUM_TYPES_OF_MESSAGE){
			return this.statusMessage[type];
		}
		return false;
	}




}
