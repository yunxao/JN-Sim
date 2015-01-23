package tid.events;

import org.jdom.Element;

import com.sun.xml.internal.ws.streaming.TidyXMLStreamReader;

import drcl.comp.Component;



/**
 * Implmentation of a event.<br>
 * To create a new type of event. <br>
 * - Set the constant. I.e: {@link tid.events.Event#UP UP} {@link tid.events.Event#DOWN DOWN}<br>
 * - Set description in "DESCRIPTION_ARRAY" in the position of the constant. See {@link tid.events.Event#DESCRIPTIONS Descriptions}. <br>
 * - Set a string to indentificate the type and put in STRING_TYPES array in the position of the constant. See {@link tid.events.Event#STRING_TYPES STRING_TYPES}<br>
 * @author Francisco Huertas Ferrer
 */
public abstract class Event {
	/**
	 * Descriptions of the events
	 */
	public final static String[] DESCRIPTIONS = {
		"Up a component",
		"Down a component" 
		};
	/**
	 * String for types inconfiguration
	 */
	public final static String[] STRING_TYPES = {
		"UP",
		"DOWN"};
	/**
	 * Type UP: UP a node or link
	 */
	public final static int UP= 0;
	/**
	 * Type DOWN: disconnect a link or a node
	 */
	public final static int DOWN= 1;
	/**
	 * Number of types
	 */
	public final static int NUM_TYPES = STRING_TYPES.length;
	
	protected boolean configured;
	int type;
	String id;
	String message;
	public Event(){
		configured = false;
	}
	public abstract void fromXML(Element xml);
	public abstract void execute();
	public String toString(){
		String cadena = "";
		cadena = "Type="+DESCRIPTIONS[type]+", ID="+id+", Message"+ message;
		return cadena;
	}

	
	
}
