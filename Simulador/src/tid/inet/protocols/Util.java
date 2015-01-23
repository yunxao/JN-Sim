package tid.inet.protocols;

import java.util.ArrayList;
import java.util.Vector;

import tid.Enviroment;
import drcl.comp.Component;
import drcl.inet.Link;
import drcl.inet.Node;
/**
 * This class content the auxiliar methods to the protocols 
 * @author Francisco Huertas
 *
 */
public class Util {
	/**
	 * @param components
	 * @see #initProtocols(Object)
	 */
	public static void initProtocols(Object[] components){
		for (int i = 0; i < components.length;i++)
			initProtocols(components[i]);
	}
	/**
	 * Start all protocols in a Component. the method inspect all object of a component and if it's a Protocol, this is started 
	 * @param component 
	 */
	public static void initProtocols(Object component){
		if (component instanceof Node){
			Node node = (Node)component;
			Component []components = node.getAllComponents();
			for (int i = 0; i < components.length;i++){
				if (components[i] instanceof Protocol){
					((Protocol)components[i]).init();
				}
			}
		}
	}
	
	public static void endProtocols(Object[] components){
		for (int i = 0; i < components.length;i++)
			endProtocols(components[i]);
	}
	public static void endProtocols(Object component){
		if (component instanceof Node){
			Node node = (Node)component;
			Component []components = node.getAllComponents();
			for (int i = 0; i < components.length;i++){
				if (components[i] instanceof Protocol){
					((Protocol)components[i]).endProtocol();
				}
			}
		}
	}
}
