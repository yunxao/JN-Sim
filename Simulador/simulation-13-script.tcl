
#puts "Deleting nodes n* y las trazes BGP_DEBUG_FILE BGP_FSM_FILE BGP_TRACE_FILE"


java::call tid.Enviroment resetNetwork [! .]


set fileConfig "simulation-13-topology.xml"
set fileEvents "simulation-13-events.xml"
set NETWORK [! .]
set LINK [java::new drcl.inet.Link]
java::call tid.Enviroment setNetwork [! .]
puts "Init Config..."
puts "General parameters"
java::call tid.inet.InetUtil doConfigFromXML $fileConfig
puts "Reading events"
set events [java::call tid.events.EventBuilder doEventsFromXML $fileEvents]
puts "Reading of configuration ended"
puts "Start configuration of TCP..."
java::call tid.Enviroment prepareNetwork [! .]
puts "Start simulation..."
set simulacion [attach_simulator event .]
$simulacion stop

java::call tid.inet.protocols.Util initProtocols [! *]

set time [java::call tid.Enviroment time]
puts "Time of simulation: $time"

set eventNumber 0

for {set i 0} {$i < [$events size]} {incr i} {
	set event [java::cast {infonet.javasim.bgp4.util.Pair} [$events get $i]]
	set eventTime [$event item1]
	
	script {
		set event [java::cast {infonet.javasim.bgp4.util.Pair} [$events get $eventNumber]]
		set action [java::cast {tid.events.Event} [$event item2]]	
		set eventTime [[$event item1] toString]
		incr eventNumber
		set msg ""
		append msg $eventNumber ")\ Time:" $eventTime ",\ " [$action toString]
		puts $msg
		
		$action execute
		
		
} -at $eventTime -on $simulacion
}





script {
	puts "End simulation..."
	java::call tid.inet.protocols.Util endProtocols [! *]
		
} -at $time -on $simulacion
$simulacion resumeTo $time
