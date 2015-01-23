package tid.utils;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.renesys.raceway.DML.*;


/**
 * This class implements a simple {@link Configuration}. This is used for save BGP configuration
 * @author Francisco Huertas Ferrer
 * @see Configuration
 *
 */
public class Config implements Configuration{
	
	private Hashtable<String, Vector<Object>> elementos;
	
	/**
	 * Constructor of de class
	 */
	public Config () {
		this.elementos = new Hashtable<String, Vector<Object>>();
	}
	/**
	 * it Finds the elemets associated with the key.<br>
	 * This is a implementation of superclass 
	 * @param arg0 Key assciated
	 * @return List (a VectorEnumeration) of the elements or null if not exist
	 */
	public Enumeration<Object> find(String arg0) throws configException {
		if (this.elementos.containsKey(arg0))
			return this.elementos.get(arg0).elements();
		return null;
	}

	/**
	 * if finds the first element of the list associated with the key. and put the element at the end of list<br>
	 * This is a implementation of superclass
	 * @param arg0 it's the key
	 * @return Element or null if not exist
	 */
	public Object findSingle(String arg0) throws configException {
		Vector<Object> contenido;
		if (!this.elementos.containsKey(arg0))
			return null;
		contenido = this.elementos.get(arg0);
		Object o = contenido.remove(0);
		contenido.add(o);
		return o;
	}
	/**
	 * add to the structure. If key don't exist in the
	 * structure, this will be added.
	 * @param key 
	 * @param value
	 */
	public void addElement (String key, Object value) {
		if (this.elementos.get(key) == null) {
			this.elementos.put(key, new Vector<Object>());
		}
		this.elementos.get(key).add(value);
	}
	
	/**
	 * Delete a Key if de structure
	 * @param key
	 */
	public void delElement (String key) {
		
		this.elementos.put(key, null);
	}
	/*
	/**
	 * Print the content of the variable
	 */
	public void printAll(){
		Enumeration<String> keys = this.elementos.keys();
		String key = "";
		while (keys.hasMoreElements()){
			key = (String)keys.nextElement();
			System.out.println("Start of key "+key);
			Object o = this.elementos.get(key);
			System.out.println(o.toString());
			System.out.println("End of key "+key);
			System.out.println();

		}
	}
	@Override
	public Object clone (){
		Config other = new Config();
		Enumeration <String> list = this.elementos.keys();
		while(list.hasMoreElements()){
			String key = list.nextElement();
			Vector<Object> elements = this.elementos.get(key);
			for (Object element: elements)
				this.addElement(key, element);
			
		}
		return other;
	}

}
