package infonet.javasim.bgp4.comm;

import infonet.javasim.bgp4.PeerConnection;
import infonet.javasim.bgp4.Route;
import infonet.javasim.bgp4.path.ASpath;
import infonet.javasim.bgp4.path.Attribute;
import infonet.javasim.util.IPaddress;

public class IncorrectUpdateMessage extends UpdateMessage {

	public IncorrectUpdateMessage(PeerConnection peerConnection) {
		super(peerConnection);
	}
	@Override
	public void fromBytes(byte[] bytes){
		throw new Error("");
	}
	@Override
    public byte [] toBytes()
    {
		// HEADER (19 bytes) || nº of withdrawRoutes (2bytes) | withdrawRoutes (variable size) | size of annuncedRoutes | AnouncedROutes
    	byte[] bytes = super.toBytes();
    	bytes[17]--;
    	return bytes;

    }
	


}
