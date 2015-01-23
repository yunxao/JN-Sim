package tid.graphic;

import java.text.DecimalFormat;


public class GraphicEvent implements Comparable<GraphicEvent>{
	protected Double time = null;
	protected GraphicLayer layer = null;
	protected GraphicNode origin = null;
	protected GraphicNode destiny = null;
	protected Integer type = null;
	protected String message = null;
	protected String descriptionState = null;
	@Override
	public int compareTo(GraphicEvent o) {
		int result = o.time.compareTo(this.time); 
		if (result != 0)
			return result;
		result = this.layer.compareTo(o.layer);
		if (result != 0)
			return result;
		result = this.origin.compareTo(o.origin);
		if (result != 0)
			return result;
		if (destiny != null && o.destiny != null){
			result = this.destiny.compareTo(o.destiny);
			if (result != 0)
				return result;
		}
		result = this.type.compareTo(o.type);
		if (result != 0)
			return result;
		return new Integer(this.hashCode()).compareTo(o.hashCode());
	}
	
	public String toString(){
		String cadena = "";
		DecimalFormat df = new DecimalFormat("0.000");
		cadena = "Time:"+df.format(time)+" |Origin: "+origin.id+" |Type: "+layer.getGraphicEventManager().getMessageTypeName(type)+" |Layer:"+layer.id;
		return cadena;
	}
}
