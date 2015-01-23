package tid.inet.protocols.trafficInspectionTool;

import java.net.UnknownHostException;
import java.util.Vector;

import drcl.inet.InetPacket;
import drcl.inet.TraceRTPkt;

public class TraceRoutePacket extends TraceRTPkt {

//	private 
	
	private boolean finished;
	private Long origin;
	private Long destiny;
	private Double timeStart;
	private Double timeFinish = null;
	Vector <Pair<Double,Long>> addressesVisited;
    public TraceRoutePacket(int iType,long origin, long dest, int size,double start)
    {
		super(iType,dest,size);
    	this.origin = origin;
    	this.destiny = dest;
		timeStart = start; 
		finished = false;
		addressesVisited = new Vector<Pair<Double,Long>>();
    }
    public void addHop(double now_, long ip, int incomingIf_)
    {
    	super.addHop(now_, ip, incomingIf_);
    	addressesVisited.add(new Pair<Double,Long>(now_,ip));
    	finished = false;
    }
    public Vector<Pair<Double,Long>> getAddressVisited(){
    	return addressesVisited;
    }
    public boolean isFinished(){
    	return finished;
    }
    public void setfinished(Double time){
    	timeFinish = time;
    	finished = true;
    }
    public String toString(){
    	
    	try {
    		String cad = "Start=";
			cad += timeStart;
			if (timeFinish != null)
				cad+= ",Finish="+timeFinish;
			cad += ",Origin="+tid.utils.Utils.addrLongToString(this.origin)+"("+this.origin+")";
			cad += ",Destiny="+tid.utils.Utils.addrLongToString(this.destiny)+"("+this.destiny+")";
			cad += ",packet Destiny="+tid.utils.Utils.addrLongToString(this.getDestination())+"("+getDestination()+")";
			cad += "Hops: ";
			for (int i = 0; i < addressesVisited.size();i++){
				long addressVisited = addressesVisited.get(i).item2();
				double time = addressesVisited.get(i).item1();
				cad+= "("+(i+1)+"º "+time+", "+tid.utils.Utils.addrLongToString(addressVisited)+"{"+addressVisited+"}) ";
			}
			return cad;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Error\n";
		}
    }
    public Long getOrigin(){
    	return this.origin;
    }
    public Long getDestiny(){
    	return this.destiny;
    }
    
}
