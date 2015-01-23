package tid.inet;

import java.util.ArrayList;

import drcl.comp.Component;
import drcl.inet.Link;

public class PhysicalNetworkInterface extends NetworkInterface{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6059945237362740427L;
	private ArrayList<Link> links = null;
	private long address;
	
	public PhysicalNetworkInterface(String id){
		super(id);
		links = new ArrayList<Link>();
	}
	public ArrayList<Link> getLinks() {
		return links;
	}
	@Override
	public void setAddress(long address) {
		this.address = address;
	}
	@Override
	public long getAddress() {
		return address;
	}
	public void addLink(Link link){
		links.add(link);
	}
	public boolean removeLink (Link link){
		return links.remove(link);
	}
	
	
}
