package tid.inet.protocols.trafficInspectionTool;

import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;

import drcl.comp.Component;
import drcl.comp.Port;
import drcl.inet.TraceRT;
import drcl.inet.TraceRTPkt;

public class TraceRoute extends Component {
	protected Port downPort = null;
//	Port outputPort = addPort("output");
	private ArrayList<TraceRoutePacket> packetsSended;
	private Hashtable<TraceRoutePacket,TITOperation> operationsByPackets;
	private TrafficInspectionTool owner;
	public TraceRoute(TrafficInspectionTool owner){ 
		super();
		this.owner = owner;
		downPort = addPort("down");
		packetsSended = new ArrayList<TraceRoutePacket>();
		operationsByPackets = new Hashtable<TraceRoutePacket, TITOperation>();
	}	
	public TraceRoute(String id, TrafficInspectionTool owner){ 
		super(id);
		this.owner = owner;
		downPort = addPort("down");
		packetsSended = new ArrayList<TraceRoutePacket>();
		operationsByPackets = new Hashtable<TraceRoutePacket, TITOperation>();
	}

	public ArrayList<TraceRoutePacket> getPackets(long destiny){
		ArrayList<TraceRoutePacket> packets = new ArrayList<TraceRoutePacket>();
		
		for (int i = 0;i< packetsSended.size();i++){
			if (packetsSended.get(i).getDestination() == destiny){
				packets.add(packetsSended.get(i));
			}
		}
		return packets;
	}
	public ArrayList<TraceRoutePacket> getPackets(){
		return packetsSended;
	}
	public ArrayList<TraceRoutePacket> getPacketsFinished(){
		ArrayList<TraceRoutePacket> packets = new ArrayList<TraceRoutePacket>();
		for (int i = 0; i<packetsSended.size();i++){
			TraceRoutePacket packet = packetsSended.get(i);
			if (!packet.isFinished()){
				packets.add(packet);
			}
		}
		return packets;
	}
	public ArrayList<TraceRoutePacket> getPacketsUnfinished(){
		ArrayList<TraceRoutePacket> packets = new ArrayList<TraceRoutePacket>();
		for (int i = 0; i<packetsSended.size();i++){
			TraceRoutePacket packet = packetsSended.get(i);
			if (packet.isFinished()){
				packets.add(packet);
			}
		}
		return packets;
	}
	public int getNumUnfinishedPackets(){
		int num = 0;
		for (int i = 0; i<packetsSended.size();i++){
			TraceRoutePacket packet = packetsSended.get(i);
			if (!packet.isFinished()){
				num++;
			}
		}
		return num;
	}
	
	public int getNumFinishedPackets(){
		int num = 0;
		for (int i = 0; i<packetsSended.size();i++){
			TraceRoutePacket packet = packetsSended.get(i);
			if (packet.isFinished()){
				num++;
			}
		}
		return num;
	}
	public int getNumPackets(){
		return packetsSended.size();
	}
	public static String packetsToString (ArrayList<TraceRoutePacket> packets){
		String cad = "";
		for (int i = 0; i < packets.size();i++){
			if (!packets.get(i).isFinished())
				cad += "*"; 
			cad += packets.get(i).toString();
			cad += "\n";
		}
		return cad;
	}
	public String stadisticOfAll(){
		int operations = getNumPackets();
		int operationsFinished = getNumFinishedPackets();
		if (operations == 0){
			return "Not messages Sended";
		}

		DecimalFormat formato = new DecimalFormat("#.##%");
		Double ratio = new Double(operationsFinished/(1.0*operations));
		String cad = "Total packets ="+operations+" , Packet Finished="+operationsFinished;
		cad += ", Packets Unfinished="+(operations-operationsFinished)+", Ratio="+formato.format(ratio);
		return cad;
		
	}
	public String stadisticByOperation(TITOperation op){
		int totalPackets = 0;
		int numPacketsSended = 0;
		for (int i = 0;i < packetsSended.size();i++){
			TraceRoutePacket p = packetsSended.get(i);
			if (operationsByPackets.get(p) == op){
				totalPackets++;
				if (p.isFinished()){
					numPacketsSended++;
				}
			}
		}
		if (totalPackets == 0){
			return "Not messages Sended";
		}
		DecimalFormat formato = new DecimalFormat("#.##%");
		Double ratio = new Double(numPacketsSended/(1.0*totalPackets));
		String cad = "TraceRoute to ";
		try {
			cad = tid.utils.Utils.addrLongToString(op.getAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}
		cad += "("+op.getAddress()+")";
		cad +="Total packets ="+totalPackets+" , Packet Finished="+numPacketsSended;
		cad += ", Packets Unfinished="+(totalPackets-numPacketsSended)+", Ratio="+formato.format(ratio);
		return cad;
		
	}
	public String info(){
		String cad = "- Packets sended finished: \n\t";
		cad += finishedPacketstoString().replaceAll("\n", "\n\t");
		cad += "\n- Packets sended unfinished: \n\t";
		cad += unfinishedPacketstoString().replaceAll("\n", "\n\t");
		cad += "\n"+stadisticOfAll();

		return cad;
	}
	public String finishedPacketstoString(){
		String cad = "";
		for (int i = 0; i < packetsSended.size();i++){
			if (packetsSended.get(i).isFinished()){
				cad += packetsSended.get(i).toString();
				cad += "\n";
			}
		}
		return cad;
	}
	public String unfinishedPacketstoString(){
		String cad = "";
		for (int i = 0; i < packetsSended.size();i++){
			if (!packetsSended.get(i).isFinished()){
				cad += packetsSended.get(i).toString();
				cad += "\n";
			}
		}
		return cad;
	}
	public String allPacketsToString(){
		String cad = "";
		for (int i = 0; i < packetsSended.size();i++){
			if (!packetsSended.get(i).isFinished())
				cad += "*";
			cad += packetsSended.get(i).toString();
			cad += "\n";
		}
		return cad;
		
	}
	
	public String toString(){
		return "tid.inet.protocols.trafficInspectionTool.TraceRoute";
	}
	public void traceRoute(final long destAddress_){
		traceRoute(drcl.net.Address.NULL_ADDR,destAddress_,0);
	}
	public void traceRoute(final long destAddress_, final int pktSize_){
		traceRoute(drcl.net.Address.NULL_ADDR,destAddress_,0);	
	}
	
	public void traceRoute(final long originAddress_,final long destAddress_){
		traceRoute(originAddress_,destAddress_,0);
	}
	
	public void traceRoute(final long originAddress_, final long destAddress_, final int pktSize_)
	{
		TraceRoutePacket p = new TraceRoutePacket(TraceRTPkt.RT_REQUEST,originAddress_, destAddress_,
						pktSize_,this.getTime());
//		hsRequest.put(p, new Double(getTime()));
		packetsSended.add(p);
		downPort.doSending(p);
	}
	public void traceRoute(final long originAddress_, final long destAddress_, TITOperation op){
		traceRoute(originAddress_, destAddress_, 0,op);
	}
	public void traceRoute(final long originAddress_, final long destAddress_, final int pktSize_,TITOperation op){
		TraceRoutePacket p = new TraceRoutePacket(TraceRTPkt.RT_REQUEST,originAddress_, destAddress_,
				pktSize_,this.getTime());
//		hsRequest.put(p, new Double(getTime()));
		packetsSended.add(p);
		operationsByPackets.put(p, op);
		downPort.doSending(p);		
	}
	// TODO explicar que tiene liberacion de memoria si esta la opcion de optimizar
	public void exportUnfishedPackets(){
		
		for (int i = 0; i < packetsSended.size();i++){
			TraceRoutePacket packet_ = packetsSended.get(i); 
			if (!packet_.isFinished()){
				TITOperation op = operationsByPackets.get(packet_);
				if (op != null){
					if (op.isConsoleEnable()){
						System.out.println(packet_);
					}
					if (op.getLogPort()!= null){
						op.getLogPort().exportEvent("Event in the Traffic Inspection Tool", "Unfished: "+packet_.toString(), "Trace Route");
						
					}
				}
				else
					System.out.println(packet_);
				if (tid.Enviroment.MEMORY_OPTIMIZATION){
					freePacket(packet_);
					
				}
			}
			
		}
	}
	/**
	 * TODO explain explicar que tiene optimizazacion de memoria
	 */
	protected void process(Object data_, Port inPort_) {
		// if the packets has a operation, it has the method to print the message. else print into console
		if (data_ instanceof TraceRoutePacket){
			TraceRoutePacket packet_ = (TraceRoutePacket)data_;
			if (packet_.getOrigin().equals(owner.getAddress())){
				packet_.setfinished(this.getTime());
				TITOperation op = operationsByPackets.get(packet_);
				if (op != null){
					if (op.isConsoleEnable()){
						System.out.println(packet_);
					}
					if (op.getLogPort()!= null){
						op.getLogPort().exportEvent("Event in the Traffic Inspection Tool", packet_, "Trace Route");
						
					}
				}
				else
					System.out.println(packet_);
				if (tid.Enviroment.MEMORY_OPTIMIZATION){
					freePacket(packet_);
					
				}
			}
			else{
				// TODO trazas intermedias aquí
			}
		}
		
	}
	private void freePacket(TraceRoutePacket packet){
		packetsSended.remove(packet);
		operationsByPackets.remove(packet);
		packet = null;
	}
}
