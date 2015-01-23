package tid.events;

import org.jdom.Element;

import tid.Enviroment;

public class PAUSE extends Event {

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		Enviroment.getNetwork().stop();
	}

	@Override
	public void fromXML(Element xml) {
		String value;
		value = xml.getAttributeValue("type");
		if (value == null || !value.equals("PAUSE")){
			throw new Error("UP (fromXML): Incorrect type event");
		}

	}
	public String toString(){
		String cadena;
		cadena = "Event: PAUSE. write resume ! to resume the simulation. ";
		if (message != null)
			cadena += "Message: "+this.message;
		return cadena;
	}

}
