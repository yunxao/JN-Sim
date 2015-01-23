package tid.inet.protocols;

import java.util.Enumeration;
import java.util.Hashtable;

import org.jdom.Element;

import infonet.javasim.bgp4.BGPSession;
import tid.Enviroment;
import tid.inet.protocols.trafficInspectionTool.EnviromentTIT;
import tid.inet.protocols.trafficInspectionTool.TITOperation;

import com.renesys.raceway.DML.Configuration;
import com.renesys.raceway.DML.configException;

import drcl.comp.Component;
import drcl.comp.Port;
import drcl.comp.io.FileComponent;
/**
 * This class implements a factory that is used to configure logs of the Protocols from a {@link Configuration}. 
 * This is optional and it don't generated Exception if not exist for a specific protocol.<br>
 * To configure a new Protocol:<br>
 * - There must exist a constant with the String id. I.e: 
 * {@link tid.inet.protocols.Protocol#BGP4 BGP4}, 
 * {@link tid.inet.protocols.Protocol#MP_BGP4 MP_BGP}  <br>
 * - Add in {@link logsFactory#factory(Configuration, Component) factory} the new case.<br>
 * - Implemented a new function to do that you want. I.e: {@link logsFactory#logsBGP(Configuration, Component)}
 * @author Francisco Huertas
 *
 */
public class logsFactory {
//	/**
//	 * Factory to configure logs of the Protocols from a {@link Configuration} in a node.<br> 
//	 * @param protocol {@link Configuration} where are the parameters
//	 * @param node Where is configured
//	 * @throws configException
//	 */
//	public static void factory(Configuration protocol, Component node) throws configException{
//		String value = (String) protocol.findSingle("PROTOCOL");
//		if (value == Protocol.BGP4){
//			logsBGP (protocol, node);
//		}
//		if (value == Protocol.MP_BGP){
//			logsBGP_CT(protocol, node);
//		}
//		if (value == Protocol.TIT){
//			logsTIT(protocol, node);
//		}
//	}
//	/**
//	 * Implementation for a bgp 
//	 * @param protocol
//	 * @param node
//	 * @throws configException
//	 */
//	private static void logsBGP(Configuration protocol, Component node) throws configException {
//		drcl.comp.io.FileComponent file;
//		String filename = (String)protocol.findSingle("LOG_FILE_NAME");
//		BGPSession bgpSession;
//		if (node instanceof BGPSession){
//			bgpSession = (BGPSession)node;
//			// DEBUG LOG
//			if (infonet.javasim.bgp4.Global.logDebugEnable){
////				file = new drcl.comp.io.FileComponent(filename+"-dbg");
//				file = new drcl.comp.io.FileComponent(filename+"-dbg");
//				bgpSession.addComponent(file);
//				file.open(Enviroment.tracedir+filename+".dbg");
//				file.setEventFilteringEnabled(true);
//				bgpSession.getPort("dbg").connect(file.findAvailable());
//			}
//			// FSM LOG
//			if (infonet.javasim.bgp4.Global.logFSMEnable){
//				file = new drcl.comp.io.FileComponent(filename+"-fsm");
//				bgpSession.addComponent(file);
//				file.open(Enviroment.tracedir+filename+".fsm");
//				file.setEventFilteringEnabled(true);
//				bgpSession.getPort("fsm").connect(file.findAvailable());
//			}
//			//ROUTE TABLE LOG
//			if (infonet.javasim.bgp4.Global.logRTEnable){
//				file = new drcl.comp.io.FileComponent(filename+"-rt");
//				bgpSession.addComponent(file);
//				file.open(Enviroment.tracedir+filename+".rt");
//				file.setEventFilteringEnabled(true);
//				bgpSession.getPort("rt").connect(file.findAvailable());
//
//			}
//			// MSG LOG
//			if (infonet.javasim.bgp4.Global.logMsgEnable){
//				file = new drcl.comp.io.FileComponent(filename+"-msg");
//				bgpSession.addComponent(file);
//				file.open(Enviroment.tracedir+filename+".msg");
//				file.setEventFilteringEnabled(true);
//				bgpSession.getPort("msg").connect(file.findAvailable());
//
//			}
//		
//		}
//	}
//	private static void logsBGP_CT(Configuration protocol, Component node) throws configException{
//		logsBGP(protocol, node);
//	}
//	private static void logsTIT(Configuration protocol, Component node) throws configException{
//		// if the file exist, hat a port in the Hashtable
//		
//		Enumeration<TITOperation> ops = protocol.find("OPERATIONS");
//		if (ops == null)
//			return;
//		while (ops.hasMoreElements()){
//			TITOperation op = ops.nextElement();
//			String fileName = op.getLogFileName();
//			if (fileName != null){
//				
//				Port p = EnviromentTIT.files.get(fileName);
//				// ¿exist the port and filename?
//				if (p== null){
//					p = node.findAvailable();
//					
//					String sTraceDir = Enviroment.tracedir.replace("/", "");
//					Component traceDir = Enviroment.getNetwork().getComponent(sTraceDir);
//					if (traceDir == null){
//						traceDir = new Component(sTraceDir);
//						Enviroment.getNetwork().addComponent(traceDir);
//					}
//					String id_ = "."+Protocol.TIT+fileName;
//					FileComponent file;
//					if ((file = (FileComponent)traceDir.getComponent(id_)) == null){
//						file = new FileComponent("."+Protocol.TIT+fileName);
//						traceDir.addComponent(file);
//					}
//					file.open(Enviroment.tracedir+"TIT"+fileName+".txt");
//					file.setEventFilteringEnabled(true);
//					p.connect(file.findAvailable());
//				}
//				op.setLogPort(p);
//			}
//			else
//				op.setLogPort(null);
//		}
//	}
}
