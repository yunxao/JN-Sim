package tid.inet;

public class VirtualMultipointInterface extends NetworkInterface {
	private Long address;
	private NetworkInterface baseInterface;
	public VirtualMultipointInterface (String id, Long address, NetworkInterface baseInterface){
		super(id);
		this.address = address;
		this.baseInterface = baseInterface;
	}
	@Override
	public long getAddress() {
		return this.address;
	}

	@Override
	public void setAddress(long address) {
		this.address = address;

	}
	public NetworkInterface getBaseInterface(){
		return this.baseInterface;
	}

}
