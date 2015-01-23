package tid.inet.protocols;

import java.util.List;

import infonet.javasim.bgp4.BGPSession;
import infonet.javasim.bgp4.Global;
import infonet.javasim.bgp4.policy.Action;
import infonet.javasim.bgp4.policy.AtomicAction;
import infonet.javasim.bgp4.policy.AtomicPredicate;
import infonet.javasim.bgp4.policy.Clause;
import infonet.javasim.bgp4.policy.Predicate;
import infonet.javasim.bgp4.policy.Rule;

import org.jdom.Element;

import tid.inet.protocols.bgp_ct.BGPCTSession;
import tid.inet.protocols.gp_bgp.GP_BGPSession;
import tid.inet.protocols.trafficInspectionTool.EnviromentTIT;
import tid.inet.protocols.trafficInspectionTool.TITOperation;
import tid.inet.protocols.trafficInspectionTool.TrafficInspectionTool;
import tid.utils.Config;
/**
 * This class implements a factory that is used to set General Parameters of Protocols from a {@link org.jdom.Element xml} element.
 * To create for a new protocol.<br>
 * - There must exist a constant with the String id. I.e: 
 * {@link tid.inet.protocols.Protocol#BGP4 BGP4}, 
 * {@link tid.inet.protocols.Protocol#MP_BGP4 MP_BGP}  <br>
 * - Add in {@link GeneralParametersFactory#factory(Element) factory} the new case <br>
 * - Implemented a new function to do that you want. I.e: {@link GeneralParametersFactory#generalBGP(Element)}
 * @author Francisco Huertas
 *
 */
public class GeneralParametersFactory {
	/**
	 * Factory to set General Parameters of Protocols from a {@link org.jdom.Element xml} element.<br>
	 * @param xml {@link org.jdom.Element xml} elementwith the configuration
	 */
	public static void factory (Element xml){
		String type = xml.getAttributeValue("type");
		if (type.equals(Protocol.BGP4)) {
			BGPSession temp = new BGPSession();
			temp.configGeneralParameters(xml);
		}
		else if (type.equals(Protocol.MP_BGP)){
			BGPCTSession temp = new BGPCTSession();
			temp.configGeneralParameters(xml);
		}
		else if (type.equals(Protocol.TIT)){
			TrafficInspectionTool tit = new TrafficInspectionTool();
			tit.configGeneralParameters(xml);
		}else if (type.equals(Protocol.GP_BGP)){
			GP_BGPSession temp = new GP_BGPSession();
			temp.configGeneralParameters(xml);
		}
		else if (type.equals(Protocol.TCP_FULL)); // nothing to do
			
		else
			throw new Error ("GeneralParametersFactory: "+type+ " session not supported");
		
	}
	/**
	 * Method for BGP
	 * @param xml
	 */
//	private static void generalBGP(Element xml){
//		Element bgpParams = (Element) xml.getChild("parameters");
//		
//		// Initialize variables of BGP   
//		Global.tbid_tiebreaking = false;
//		Global.random_tiebreaking = false;
//		// MP-BGP extension
//		Global.mp_bfd = false;
//		Global.mp_bfd_poll_interval = Global.MP_DEFAULT_BFD_POLLING_INTERVAL;
//		
//		String value;
//		if ((value=bgpParams.getAttributeValue("rtlog") )!= null)
//			Global.logRTEnable = value.equals("enable");
//		if ((value=bgpParams.getAttributeValue("dbglog") )!= null)
//			Global.logDebugEnable = value.equals("enable");
//		if ((value=bgpParams.getAttributeValue("fsmlog") )!= null)
//			Global.logFSMEnable = value.equals("enable");
//		if ((value=bgpParams.getAttributeValue("tracelog") )!= null)
//			Global.logMsgEnable = value.equals("enable");
//		if ((value = bgpParams.getAttributeValue("med"))!= null)
//			Global.always_compare_med = value.equals(Global.STRING_ALWAYS_COPMARE_MED);
//		if ((value = bgpParams.getAttributeValue("tie_breaking")) != null)
//			Global.random_tiebreaking = value.equals(Global.STRING_RANDOM_TIE_BREAKING);
//		if ((value = bgpParams.getAttributeValue("default_port"))!= null)
//			BGPSession.PORT_NUM = Integer.valueOf(value);
//
//
//		if ((value = bgpParams.getAttributeValue("routes_compare_level")) != null){
//			if (Global.ROUTES_COMPARE_LEVELS.containsKey(value)){
//				Global.routes_compare_level = Global.ROUTES_COMPARE_LEVELS.get(value);
//			}
//		}
//		if ((value = bgpParams.getAttributeValue("default_local_pref")) != null)
//			Global.default_local_pref = Integer.parseInt(value);
//		
//		
//		/*-----------------POLICIES------------------*/
//		Element bgpPolicy = xml.getChild("policyList");
//		if (bgpPolicy != null){
//			@SuppressWarnings("unchecked")
//			List <Element> policies = bgpPolicy.getChildren("policy");
//			for (int i = 0; i <policies.size();i++){
//				boolean permit = true;
//				if ((value = policies.get(i).getAttributeValue("permit")) != null){
//					permit = !value.equals("false");
//				}
//				Rule policy = new Rule();
//				String ruleId = policies.get(i).getAttributeValue("id");
//				Global.rules.put(ruleId, policy);
//				@SuppressWarnings("unchecked")
//				List <Element> clauses = policies.get(i).getChildren("clause");
//				
//				// List of clauses for a Policy
//				for (int j = 0; j < clauses.size();j++){
//					Predicate p = new Predicate(null);
//					// XXX Por defecto hay que ponerlo en true o false???
//					Action a = null;
//					Element element;
//					// IF exist predicates && atomic predicates
//					if ((element = clauses.get(j).getChild("predicate")) != null){
//						@SuppressWarnings("unchecked")
//						List <Element> atomicPredicates = element.getChildren("atomicPredicate");
//						if (atomicPredicates != null){
//							// List of atomic predicates for a predicates
//							for (int k = 0; k < atomicPredicates.size();k++){
//								String attribute = atomicPredicates.get(k).getAttributeValue("attribute");
//								if (attribute == null)
//									throw new Error("There isn't attribute in a atomicPredicate. Policy ID="+ruleId);
//								String matchString = atomicPredicates.get(k).getAttributeValue("match_string");
//								if (matchString == null)
//									throw new Error("There isn't match_String in a atomicPredicate. Policy ID="+ruleId);
//								AtomicPredicate ap = new AtomicPredicate(attribute,matchString);
//								p.add_atom(ap);
//							}
//						}
//					}
//
//					if ((element = clauses.get(j).getChild("action")) != null){
//						permit = true;
//						if ((value = element.getAttributeValue("permit")) != null){
//							permit = !value.equals("false");
//						}
//						a = new Action (permit,null);
//						@SuppressWarnings("unchecked")
//						List <Element> atomicActions = element.getChildren("atomicAction");
//						if (atomicActions != null){
//							for (int k = 0; k < atomicActions.size();k++){
//								// see Values in AtomicActions
//								String attribute = atomicActions.get(k).getAttributeValue("attribute");
//								if (attribute == null)
//									throw new Error("There isn't attribute in a atomicAction. Policy ID="+ruleId);
//								// See values in AtomicActions 
//								String action = atomicActions.get(k).getAttributeValue("action");
//								if (action == null)
//									throw new Error("There isn't action in a atomicAction. Policy ID="+ruleId);
//								// Multi value string must be separated with a space
//								String val = atomicActions.get(k).getAttributeValue("value");
//								String []values;
//								if (val != null){
//									values = new String[1];
//									values[0] = val;
//								}
//								else{
//									val = atomicActions.get(k).getAttributeValue("values");
//									if (val == null)
//										throw new Error("There isn't \"value\" or \"values\" in a atomicAction. Policy ID="+ruleId);
//									values = val.split(" ");
//
//								}
//								AtomicAction aa = new AtomicAction(attribute, action, values);
//								a.add_atom(aa);
//							}
//						}
//						
//					}
//					else
//						// XXX cual es el valor por defecto
//						a = new Action (true,null);
//					Clause clause = new Clause(p,a);
//					policy.add_clause(clause);
//
//				}
//
//			}
//		}
//	}
//	private static void generalBGP_CT(Element xml){
//		//generalBGP(xml);
//		Element bgpParams = (Element) xml.getChild("parameters");
//		String value;
//		if ((value=bgpParams.getAttributeValue("rtlog") )!= null)
//			Global.mp_logRTEnable = value.equals("enable");
//		if ((value=bgpParams.getAttributeValue("debuglog") )!= null)
//			Global.mp_logDebugEnable = value.equals("enable");
//		if ((value=bgpParams.getAttributeValue("fsmlog") )!= null)
//			Global.mp_logFSMEnable = value.equals("enable");
//		if ((value=bgpParams.getAttributeValue("tracelog") )!= null)
//			Global.mp_logMsgEnable = value.equals("enable");
//
//		if ((value = bgpParams.getAttributeValue("mp_bfd")) != null)
//			Global.mp_bfd = value.equals("enable");
//		
//		if (Global.mp_bfd) {
//			if ((value = bgpParams.getAttributeValue("mp_bfd_poll_interval"))!= null)
//				if (!value.equals("default"))
//					try {
//						Global.mp_bfd_poll_interval = Integer.parseInt(value);
//					} 
//					catch (NumberFormatException e){
//						System.err.println("Number format error. Default value: "+ Global.mp_bfd_poll_interval);
//					}
//		}
//													
//		if ((value = bgpParams.getAttributeValue("mp_type")) != null){
//			if (!value.equals("AF_INET"))
//			{
//			// Only AF_INET support is implemented
//				System.err.println("Only AF_INET MP-BGP protocol extension is supported.");
//				System.err.println("MP-BGP protocol extension disabled.");
//			}
//			else
//				Global.mp_bgp=true;
//		}
//
//		
//	}
//	
//	private static void generalTIT(Element xml){
//		Element parameters = (Element) xml.getChild("parameters");
//		String sConsoleEnableByDefault = parameters.getAttributeValue("consoleEnable");
//		
//		if (sConsoleEnableByDefault != null){
//			EnviromentTIT.consoleEnable = sConsoleEnableByDefault.compareToIgnoreCase("enable") == 0;
//		}
//		else {
//			System.out.println("Warning: there is not defined \"colsoleEnable\" (enable/disable). Output by default will be disable");
//			EnviromentTIT.consoleEnable = false;
//		}
//	}

}
