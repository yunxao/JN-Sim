package tid.inet;

import drcl.comp.Component;

@SuppressWarnings("serial")
public abstract class NetworkInterface extends Component{
	public static final String PHYSICAL_TYPE="physical";
	public static final String VIRTUAL_STATIC_P2P_TYPE="virtual static point to point";
	public static final String VIRTUAL_MULTIPOINT_TYPE="virtual multipoint";
	public NetworkInterface(String id){
		super(id);
	}
	public abstract void setAddress(long address);
	public abstract long getAddress();
}
