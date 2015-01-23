set tiempo [java::new java.util.Date]
set t0 [$tiempo getTime]
#puts "Deleting nodes n* y las trazes BGP_DEBUG_FILE BGP_FSM_FILE BGP_TRACE_FILE"


java::call tid.Enviroment resetNetwork [! .]


set fileConfig "tresNodosTopologia.xml"
#set OpenDialog [java::new javax.swing.JFileChooser "."]
#$OpenDialog setDialogTitle "Open topology file"
#$OpenDialog setFileFilter [java::new tid.utils.ExtFilter "XML Files" "^*.xml$"]
#$OpenDialog showOpenDialog [java::new javax.swing.JFrame]
#set fileConfig [[$OpenDialog getSelectedFile] getAbsolutePath]

set fileEvents "tresNodosEventos.xml"
#set OpenDialog [java::new javax.swing.JFileChooser "."]
#$OpenDialog setDialogTitle "Open events file"
#$OpenDialog setFileFilter [java::new tid.utils.ExtFilter "XML Files" "^*.xml$"]
#$OpenDialog showOpenDialog [java::new javax.swing.JFrame]
#set fileEvents [[$OpenDialog getSelectedFile] getAbsolutePath]


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
set tiempo [java::new java.util.Date]
set t1 [$tiempo getTime]
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
	set tiempo [java::new java.util.Date]
	set t2 [$tiempo getTime]
	set tiempoA [java::call tid.utils.Utils resta $t2 $t0] 
	set tiempoB [java::call tid.utils.Utils resta $t2 $t1]
	puts "Tiempo de ejecición:  $tiempoA ms"
	puts "Tiempo de simulación: $tiempoB ms"		
} -at $time -on $simulacion
$simulacion resumeTo $time
