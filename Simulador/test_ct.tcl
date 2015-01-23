
#puts "Deleting nodes n* y las trazes BGP_DEBUG_FILE BGP_FSM_FILE BGP_TRACE_FILE"


java::call tid.Enviroment resetNetwork [! .]


set fileConfig "test_ct_config.xml"
set fileEvents "test_ct_events.xml"
set NETWORK [! .]
set LINK [java::new drcl.inet.Link]
java::call tid.Enviroment setNetwork [! .]
puts "Init Config..."
puts "General parameters"
set config [java::call tid.utils.XMLUtils doConfig $fileConfig]
puts "Reading events"
set events [java::call tid.events.EventBuilder doEventsFromXML $fileEvents]
set nodes [java::cast {Integer} [$config findSingle "NUM"]]
#check read
set nodes [$nodes intValue]
#java::call infonet.javasim.bgp4.XMLUtils print $config
# es necesario hacer el cast para que sepa que es un array doble de enteros
puts "Reading of configuration ended"

puts "Start configuration of TCP..."
#set topology_ [java::cast {int[][]} [$config findSingle "TOPOLOGY"]]
#set PRINCIALADDR [java::cast {long[]} [$config findSingle "PRINCIALADDR"]]
set IDS [java::cast {String[]} [$config findSingle "IDS"]]
java::call tid.inet.InetUtil createTopology [! .] $LINK $config
#java::call drcl.inet.InetUtil createTopology [! .] "n" "n" [java::cast {int[][]} $topology_] $IDS $LINK



puts "Build node..."
set NODE_BUILDER [java::new drcl.inet.NodeBuilder]

	
#set id_longg [java::call infonet.javasim.bgp4.XMLUtils getElement $IDS 0]
for {set i 0} {$i < $nodes} {incr i} {
	set id [[java::cast {String} [java::call tid.utils.Utils getElement $IDS $i]] toString]
	#set id_str [java::call infonet.javasim.bgp4.XMLUtils addrLongToString $id_str]
	set cfg_route [java::cast {com.renesys.raceway.DML.Configuration} [$config findSingle $id]]
	set stringMap [java::call tid.inet.InetUtil createMap $cfg_route]
#	puts $stringMap
#	puts $id 
	$NODE_BUILDER build [! $id] $stringMap
}




java::call drcl.inet.InetUtil connectNeighbors [! .]
java::call tid.inet.InetUtil addAddresses [! .] $config 


puts "Config Protocols..."


set r [java::new java.util.Random ]


java::call infonet.javasim.bgp4.BGPSession setSeed [$r nextLong]
set traceDir "trace"
for {set i 0} {$i < $nodes} {incr i} {
	puts "Config Router $i ..."
	set routerID [[java::cast {String} [java::call tid.utils.Utils getElement $IDS $i]] toString]
	set routerCfg [java::cast {com.renesys.raceway.DML.Configuration} [$config findSingle $routerID]]
	set protocols [java::cast {java.util.Enumeration} [$routerCfg find "SESSIONS"]]
	
	while {[$protocols hasMoreElements]} {
		set protocolCfg [java::cast {com.renesys.raceway.DML.Configuration} [$protocols nextElement]]
		set protocolID [[java::cast {String} [$protocolCfg findSingle "ID"]] toString]
		puts "Config the session: $protocolID" 
		! $routerID/$protocolID config $protocolCfg
#		setflag debug true $routerID/$protocolID

		java::call tid.inet.protocols.logsFactory factory $protocolCfg [! $routerID/$protocolID]
		

	}
}

#! n*/bgp print
puts "Config of Protocols ended..."
java::call tid.Enviroment prepareNetwork [! .]
# corriendo la simulacion
puts "Start simulation..."
set simulacion [attach_simulator event .]
$simulacion stop
#$NETWORK setDebugEnabled false false
#$NETWORK setErrorNoticeEnabled false true
java::call tid.inet.protocols.Util initProtocols [! *]

#! n*/bgp2 init
#! n167772417/bgp init
#! n167772929/bgp init
set time [java::call tid.Enviroment time]
puts "Time of simulation: $time"

set eventNumber 0

for {set i 0} {$i < [$events size]} {incr i} {
	set event [java::cast {tid.events.Event} [$events get $i]]
	set at [$event time]
	script {
		set event [java::cast {tid.events.Event} [$events get $eventNumber]]
#		
		$event executeAction
		incr eventNumber
		puts [$event toString]
	} -at $at -on $simulacion
}





script {puts "End simulation..."} -at $time -on $simulacion
$simulacion resumeTo $time
