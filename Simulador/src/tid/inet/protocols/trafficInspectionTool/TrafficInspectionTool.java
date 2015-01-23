package tid.inet.protocols.trafficInspectionTool;



import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jdom.Element;

import tid.Enviroment;
import tid.inet.protocols.Protocol;
import drcl.comp.Component;
import drcl.comp.Port;
import drcl.comp.io.FileComponent;
import drcl.inet.InetConstants;
import drcl.inet.Node;


public class TrafficInspectionTool extends Component implements Protocol{
	
	protected Port downPort = addPort("down");
	protected Port rtPort = addPort(drcl.inet.InetConstants.SERVICE_RT_PORT_ID);
	private Hashtable<Port, TITOperation> operations;
	private Long address;
	private String nodeID;
	private Vector <Long> destiniesOfTraceRoute;
	private TraceRoute traceRouteComponent;
	private double periodicityTraceRoute;
	private Port traceRouterTimerPort;
	private boolean traceRouteEnable;
	

	/** 
	 * A reference to the timer manager on the local router. 
	 */

	
	
//	private TraceRT manager;
	/**
	 * 
	 */
	private static final long serialVersionUID = 6356895379507453661L;
	
	
	
	public TrafficInspectionTool(){
		super ();
		constructor();
		// TODO quitar
		
	}	
	public TrafficInspectionTool(String id){
		super (id);
		constructor();
		// TODO quitar
		
	}
	private void constructor(){
		this.traceRouterTimerPort = addPort(TrafficInspectionToolConstants.PORT_TRACE_ROUTER);
		destiniesOfTraceRoute = new Vector<Long>();
		operations = new Hashtable<Port, TITOperation>();
	}
	@Override
	protected void process(Object data_, Port inPort_) {
		TITOperation op = operations.get(inPort_);
		switch (op.getOperation()) {
		case TITOperation.TRACEROUTE:
			if (traceRouteEnable && op.isEnable())
				traceRouteComponent.traceRoute(this.address,op.getAddress(),op);
			break;
		default:
			System.out.println("TIT: Operation not superted. Code="+op.getOperation());
			break;
		}
		fork(inPort_,null,op.getPeridicity());
	}


	@Override
	public void endProtocol() {
		if (traceRouteEnable){
			traceRouteComponent.exportUnfishedPackets();
			Enumeration <TITOperation>ops = operations.elements();
			// White stadistics in the file
			if (ops != null){
				while (ops.hasMoreElements()){
					TITOperation op = ops.nextElement();
					if (op.getLogPort() != null){ 
						switch (op.getOperation()) {
						case TITOperation.TRACEROUTE:
							op.getLogPort().exportEvent("Event in the Traffic Inspection Tool", traceRouteComponent.stadisticByOperation(op), "Finish Trace Route");
							break;
						default:
							op.getLogPort().exportEvent("Event in the Traffic Inspection Tool", "TIT: Operation not superted. Code="+op.getOperation(), "Finish");
							break;
						}
					}
					
				}
			}
		}
		
	}



	@Override
	public Object id() {
		return nodeID;
	}



	@Override
	public void init() {
		Enumeration<Port> ports = operations.keys();
		while (ports.hasMoreElements()){
			Port p = ports.nextElement();
			fork(p,null,operations.get(p).getPeridicity());
		}
	}



	@Override
	public void kill() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void restart() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public String type() {
		return Protocol.TIT;
	}
	@Override
	public String info(){
		String cad ="-----Traceroutes made----\n";
		cad += traceRouteComponent.allPacketsToString();
		return "no esta hecho";
		
	}
	public String toString(){
		if (this.getParent() != null)
			return this.getParent()+"/"+this.getID();
		return this.getID();
	}
	public Long getAddress(){
		return this.address;
	}
	@Override
	public void config(Element config, Node node) {
		String value;
		Element parameters = config.getChild("parameters");
		if (parameters == null)
			throw new Error("TrafficInspectionTool.config: parameters has not definend");
		nodeID = node.getID();
		value = parameters.getAttributeValue("id");
		if (value == null){
			if (Enviroment.debugFlag)
				System.out.println("TrafficInspectionTool.config: Warning, there is not id for Traffic Inspection Tool. Value by default: tit");
			value = "tit";
		}
		this.setID(value);

		
		value = parameters.getAttributeValue("periodicity");
		if (value == null){
			throw new Error("TrafficInspectionTool.config: peridicity value is not setup");
		}
		long periodicityByDefault = Long.valueOf(value);
		value = parameters.getAttributeValue("interface");
		if (value == null)
			throw new Error("TrafficInspectionTool.config: BGPSession need a interface parameter");
		Component iface = node.getComponent(value);
		if (iface == null || !(iface instanceof tid.inet.NetworkInterface)){
			throw new Error("TrafficInspectionTool.config: The interface don't exist or is invalid");
		}
		
		address = ((tid.inet.NetworkInterface)iface).getAddress();
		// TODO arreglar varias interfaces
		if (address == null){
			throw new Error("TTrafficInspectionTool.config: raffic inspection tools need a address");
		}
		String fileLogNameByDefault =  parameters.getAttributeValue("fileDebug");
		value = parameters.getAttributeValue("consoleEnable");
		boolean consoleEnableByDefault;
		if ((value != null) && (value.equals("enable")))
			consoleEnableByDefault = true;
		else
			consoleEnableByDefault = false;
		// END OF PARAMETERS
		
		int acomulatedPort = 0;
		
		@SuppressWarnings("unchecked")
		List<Element> listTraceRoute = config.getChildren("traceRoute");
		
		if (listTraceRoute != null && listTraceRoute.size() > 0){
			traceRouteEnable = true;
			// Configure a TraceRoute Component
			Component parent = getParent();
			Component csl_ = parent.getComponent(drcl.inet.InetConstants.ID_CSL);
			
			if (csl_ == null){
				if (tid.Enviroment.errorFlag){
					traceRouteEnable = false;
					throw new Error("TrafficInspectionTool.config: The component is not in a router with a csl component");
				}
			}else{
				traceRouteComponent = new TraceRoute(InetConstants.ID_TRACE_RT,this);
				csl_.addComponent(traceRouteComponent);
				Port cslPort_ = csl_.addPort("up",""+InetConstants.PID_TRACE_RT);
				traceRouteComponent.getPort("down").connect(cslPort_);
			}
			
			Iterator<Element> iteratorListTraceRoute = listTraceRoute.iterator();
			// COnfigure all traces routes
			while (iteratorListTraceRoute.hasNext()){
				Element elementTraceRoute = iteratorListTraceRoute.next();
				value = elementTraceRoute.getAttributeValue("destiny");
				if (value == null)
					throw new Error("TrafficInspectionTool.config: trace route need a destiny");
				long address = 0;
				try {
					address = tid.utils.Utils.stringAddressToLong(value);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					throw new Error("TrafficInpectionTool (config): The address has not a correct value");
				}
				value = elementTraceRoute.getAttributeValue("periodicity");
				long peridicity;
				if (value == null)
					peridicity = periodicityByDefault;
				else
					peridicity = Long.valueOf(value);
				value = elementTraceRoute.getAttributeValue("consoleEnable");
				boolean consoleEnable;
				if (value == null)
					consoleEnable = consoleEnableByDefault;
				else
					consoleEnable = value.equals("enable");
				String fileLog =  elementTraceRoute.getAttributeValue("fileDebug");
				TITOperation op;
				if (fileLog != null){
					op = new TITOperation(TITOperation.TRACEROUTE, address, peridicity, consoleEnable, fileLog);
				}else if (fileLogNameByDefault != null){
					op = new TITOperation(TITOperation.TRACEROUTE, address, peridicity, consoleEnable, fileLogNameByDefault);
				}else
					op = new TITOperation(TITOperation.TRACEROUTE, address, peridicity, consoleEnable);
				Port p = addPort(""+acomulatedPort++);
				operations.put(p, op);
			}
		}
		else 
			traceRouteEnable = false;
		this.createLogs(config);
	}
	@Override
	public void configGeneralParameters(Element xml) {
		Element parameters = (Element) xml.getChild("parameters");
		if ((parameters == null) && Enviroment.debugFlag){
			System.out.println("infonet.javasim.bgp4.BGPSession.configGeneralParameters: " +
					"There are not general parameters defined to the protocol");
		}
		String sConsoleEnableByDefault = parameters.getAttributeValue("consoleEnable");
		
		if (sConsoleEnableByDefault != null){
			EnviromentTIT.consoleEnable = sConsoleEnableByDefault.compareToIgnoreCase("enable") == 0;
		}
		else {
			System.out.println("Warning: there is not defined \"colsoleEnable\" (enable/disable). Output by default will be disable");
			EnviromentTIT.consoleEnable = false;
		}		
	}
	@Override
	public void createLogs(Element sessionElement) {
		Component cNode = this.getParent();
		if ((cNode == null) || !(cNode instanceof Node))
			throw new Error("TrafficInspectionTool.createLogs: the parent of the protocol is not a protocol or not exist");
		Node node = (Node)cNode;
		Enumeration<TITOperation> ops = this.operations.elements();
		if (ops == null)
			return;
		while (ops.hasMoreElements()){
			TITOperation op = ops.nextElement();
			String fileName = op.getLogFileName();
			if (fileName != null){
				
				Port p = EnviromentTIT.files.get(fileName);
				// ¿exist the port and filename?
				if (p== null){
					p = node.findAvailable();
					
					String sTraceDir = Enviroment.tracedir.replace("/", "");
					Component traceDir = Enviroment.getNetwork().getComponent(sTraceDir);
					if (traceDir == null){
						traceDir = new Component(sTraceDir);
						Enviroment.getNetwork().addComponent(traceDir);
					}
					String id_ = "."+Protocol.TIT+fileName;
					FileComponent file;
					if ((file = (FileComponent)traceDir.getComponent(id_)) == null){
						file = new FileComponent("."+Protocol.TIT+fileName);
						traceDir.addComponent(file);
					}
					file.open(Enviroment.tracedir+"TIT"+fileName+".txt");
					file.setEventFilteringEnabled(true);
					p.connect(file.findAvailable());
				}
				op.setLogPort(p);
			}
			else
				op.setLogPort(null);
		}
		
	}
}
