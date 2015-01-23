package tid.inet;



import drcl.comp.Component;


public class VirtualStaticP2PNetworkInterface extends NetworkInterface {
	private static final long serialVersionUID = 6278545007277079287L;
	private long address;
	private NetworkInterface baseInterface;
	private long remoteHost;
	private Long remoteAddress;
	 
	public VirtualStaticP2PNetworkInterface(String id, NetworkInterface baseInterface, Long address, Long remoteHost, Long remoteAddress){
		super(id);
		this.baseInterface = baseInterface;
		this.address = address;
		this.remoteHost = remoteHost;
		this.remoteAddress = remoteAddress;
	}
	@Override
	public long getAddress() {
		return address;
	}
	@Override
	public void setAddress(long address) {
		this.address = address;

	}
	public NetworkInterface getBaseInterface(){
		return baseInterface;
	}
	public long getRemoteHost(){
		return this.remoteHost;
	}
	public Long getRemoteAddress(){
		return this.remoteAddress;
	}

}
