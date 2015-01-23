package tid.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JOptionPane;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import tid.inet.protocols.Protocol;

public class XMLGenerator {

	public static final String PREFIJO_LINK = ".link_";
	public static final String PREFIJO_NODO = "nodo";

	/**
	 * @param args
	 */
	// @SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fileName = JOptionPane.showInputDialog("Introduce la base del nombre de fichero", "simulacion");
		Element root = new Element("JSimConfig");
		Document documento = new Document(root);
		String cad;
		// Topology
		{
			Element topology = new Element("Topology");
			root.addContent(topology);
			topology.setAttribute("nodesPerLink", "2");

		}

		// Debug
		{
			Element debug = new Element("debug");
			root.addContent(debug);
			debug.setAttribute("debugFlag", "enable");
			debug.setAttribute("errorFlag", "enable");
			debug.setAttribute("trace_dir", "trace");
		}

		// Protocol BGP
		boolean bgp = JOptionPane.showOptionDialog(null, "Quieres que haya protocolo BGP", "Configuración", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) == 0;
		if (bgp) {
			Element configProtocol = new Element("protocol");
			root.addContent(configProtocol);
			configProtocol.setAttribute("type", tid.inet.protocols.Protocol.BGP4);
			Element parameters = new Element("parameters");
			configProtocol.addContent(parameters);

			// Parameters
			{
				parameters.setAttribute("rtlog", "enable");
				parameters.setAttribute("dbglog", "enable");
				parameters.setAttribute("tracelog", "enable");
				parameters.setAttribute("fsmlog", "enable");
				// parameters.setAttribute("tie_breaking","default");
				// // Comprobar si es "med" o "med_type"
				// parameters.setAttribute("med_type","default");
				// parameters.setAttribute("default_port","179");
				// parameters.setAttribute("routes_compare_level","level 5");
				// parameters.setAttribute("default_local_pref","100");
			}
		}

		boolean gp_bgp = JOptionPane.showOptionDialog(null, "Quieres que haya protocolo GP BGP", "Configuración", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) == 0;
		if (gp_bgp) {
			Element configProtocol = new Element("protocol");
			root.addContent(configProtocol);
			configProtocol.setAttribute("type", tid.inet.protocols.Protocol.GP_BGP);
			Element parameters = new Element("parameters");
			configProtocol.addContent(parameters);
			{
				parameters.setAttribute("rtlog", "enable");
				parameters.setAttribute("dbglog", "enable");
				parameters.setAttribute("tracelog", "enable");
				parameters.setAttribute("fsmlog", "enable");
				// parameters.setAttribute("tie_breaking","default");
				// // Comprobar si es "med" o "med_type"
				// parameters.setAttribute("med_type","default");
				parameters.setAttribute("default_port", "800");
				// parameters.setAttribute("routes_compare_level","level 5");
				// parameters.setAttribute("default_local_pref","100");
			}
		}

		// Components
		Element components = new Element("components");
		root.addContent(components);
		if (bgp) {
			boolean nodosBGP = JOptionPane.showOptionDialog(null, "Quieres inicializar los nodos con BGP", "Configuración", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) == 0;
			if (nodosBGP) {
				Integer numAS = Integer.valueOf(JOptionPane.showInputDialog("Cuantos sistemas autonomos quieres tener", 5));
				Integer asNum = 0;
				Integer nodosPorAS = 4;
				Hashtable<String, Element> routers = new Hashtable<String, Element>();
				for (int i = 1; i <= numAS; i++) {
					asNum++;
					asNum = Integer.valueOf(JOptionPane.showInputDialog("Numero de sistema Autonomo", asNum));
					nodosPorAS = Integer.valueOf(JOptionPane.showInputDialog("Cuantos nodos quieres que tenga el Sistema autonomo " + asNum + "?", nodosPorAS));
					boolean rr = JOptionPane.showOptionDialog(null, "¿Quieres que exista un Router Reflector?", "Configuración", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null,
							null) == 0;
					// Network
					cad = JOptionPane.showInputDialog("Introduce la red", "10.0." + i + ".0/24");
					Integer mascara = Integer.valueOf(cad.split("/")[1]);
					String[] sAddress = (cad.split("/")[0]).split("\\.");
					String baseAddress = "";
					for (int h = 0, pos = 0; h < mascara; h = h + 8, pos++) {
						baseAddress += sAddress[pos] + ".";
					}

					for (int j = 1; j <= nodosPorAS; j++) {
						Element router = new Element("router");
						components.addContent(router);
						String name = PREFIJO_NODO + asNum + "_" + j;
						router.setAttribute("id", name);
						routers.put(name, router);

						// interfaz
						Element iface = new Element("interface");
						router.addContent(iface);
						iface.setAttribute("type", "physical");
						iface.setAttribute("name", "principal");
						iface.setAttribute("address", baseAddress + j);

						// topology
						Element topology = new Element("topology");
						router.addContent(topology);
						for (int k = 1; k <= nodosPorAS; k++) {
							if (k != j) {
								// añadiendo un link
								Element link = new Element("link");
								topology.addContent(link);
								String namelink1 = PREFIJO_LINK + PREFIJO_NODO + asNum + "_" + k + "-" + PREFIJO_NODO + asNum + "_" + j;
								String namelink2 = PREFIJO_LINK + PREFIJO_NODO + asNum + "_" + j + "-" + PREFIJO_NODO + asNum + "_" + k;
								if (namelink1.compareTo(namelink2) < 0)
									link.setAttribute("id", namelink1);
								else
									link.setAttribute("id", namelink2);
							}
						}
						// Protocol tcp
						Element sessionTcp = new Element("session");
						router.addContent(sessionTcp);
						sessionTcp.setAttribute("type", Protocol.TCP_FULL);

						// Protocol BGP
						Element sessionBGP = new Element("session");
						router.addContent(sessionBGP);
						sessionBGP.setAttribute("type", Protocol.BGP4);
						Element parameters = new Element("parameters");
						sessionBGP.addContent(parameters);
						parameters.setAttribute("as", "" + asNum);
						parameters.setAttribute("interface", "principal");
						if (rr) {
							// Router Reflector
							if (j == 1) {
								for (int k = 2; k <= nodosPorAS; k++) {
									Element neighbor = new Element("neighbour");
									sessionBGP.addContent(neighbor);
									neighbor.setAttribute("IP", baseAddress + k);
									neighbor.setAttribute("remote-as", "" + asNum);
									neighbor.setAttribute("rrc", "yes");
								}
							} else {
								Element neighbor = new Element("neighbour");
								sessionBGP.addContent(neighbor);
								neighbor.setAttribute("IP", baseAddress + "1");
								neighbor.setAttribute("remote-as", "" + asNum);
							}

						} else {
							for (int k = 1; k <= nodosPorAS; k++) {
								if (k != j) {
									Element neighbor = new Element("neighbour");
									sessionBGP.addContent(neighbor);
									neighbor.setAttribute("IP", baseAddress + k);
									neighbor.setAttribute("remote-as", "" + asNum);
								}
							}
						}

					}
					// Enumeration<String> keys = routers.keys();
					// cad ="";
					// while(keys.hasMoreElements()){
					// String key = keys.nextElement();
					// cad += ", "+key;
					// }
					// JOptionPane.showMessageDialog(null,
					// "Se ha configurado el sistema autonomo "+asNum+". Los nodos configurados son:\n"+cad);

				}
				while (!(cad = JOptionPane.showInputDialog("Introduce el los nodos que quieres que tengan conexión eBGP separados por una coma, deja en blanco para salir")).equals("")) {
					String[] names = cad.split(",");
					if (names.length != 2) {
						JOptionPane.showMessageDialog(null, "Las conexiones tienen que ser entre 2 nodos");
						continue;

					}
					names[0] = names[0].replace(" ", "");
					names[1] = names[1].replace(" ", "");
					Element node1 = routers.get(names[0]);
					Element node2 = routers.get(names[1]);
					if (node1 == null || node2 == null) {
						JOptionPane.showMessageDialog(null, "Alguno de los nombres no era correcto");
						continue;
					}
					String namelink1 = PREFIJO_LINK + names[0] + "-" + names[1];
					String namelink2 = PREFIJO_LINK + names[1] + "-" + names[0];

					if (namelink1.compareTo(namelink2) < 0) {
						Element topology = node1.getChild("topology");
						Element link = new Element("link");
						topology.addContent(link);
						link.setAttribute("id", namelink1);
						topology = node2.getChild("topology");
						link = new Element("link");
						topology.addContent(link);
						link.setAttribute("id", namelink1);
					} else {
						Element topology = node1.getChild("topology");
						Element link = new Element("link");
						topology.addContent(link);
						link.setAttribute("id", namelink2);
						topology = node2.getChild("topology");
						link = new Element("link");
						topology.addContent(link);
						link.setAttribute("id", namelink2);

					}
					List<Element> sessions = node1.getChildren("session");
					String as1 = "";
					String addr1 = node1.getChild("interface").getAttributeValue("address");
					for (Element session : sessions) {
						if (session.getAttributeValue("type").equals(Protocol.BGP4)) {
							as1 = session.getChild("parameters").getAttributeValue("as");
						}
					}
					sessions = (List<Element>) node2.getChildren("session");
					String as2 = "";
					String addr2 = node2.getChild("interface").getAttributeValue("address");
					for (Element session : sessions) {
						if (session.getAttributeValue("type").equals(Protocol.BGP4)) {
							as2 = session.getChild("parameters").getAttributeValue("as");
						}
					}
					sessions = node1.getChildren("session");
					for (Element session : sessions) {
						if (session.getAttributeValue("type").equals(Protocol.BGP4)) {
							Element neighbour = new Element("neighbour");
							session.addContent(neighbour);
							neighbour.setAttribute("IP", addr2);
							neighbour.setAttribute("remote-as", as2);

						}
					}
					sessions = node2.getChildren("session");
					for (Element session : sessions) {
						if (session.getAttributeValue("type").equals(Protocol.BGP4)) {
							Element neighbour = new Element("neighbour");
							session.addContent(neighbour);
							neighbour.setAttribute("IP", addr1);
							neighbour.setAttribute("remote-as", as1);

						}
					}
				}
			}

		}

		FileOutputStream file;
		try {
			XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
			// String fileName =
			// JOptionPane.showInputDialog("Introduce el nombre del fichero");
			// file = new FileOutputStream(fileName);
			file = new FileOutputStream(fileName + "-topologia.xml");
			out.output(documento, file);
			file.flush();
			file.close();
			out.output(documento, System.out);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		root = new Element("JSimConfig");
		documento = new Document(root);
		Element eventos = new Element("events");
		root.addContent(eventos);
		Comment comentarios = new Comment("Format: \nLabel: event; Atributes: id, type, message.");
		eventos.addContent(comentarios);
		Element simulation = new Element("simulation");
		root.addContent(simulation);
		Integer tiempo = null;
		while (tiempo == null) {
			try {
				tiempo = Integer.valueOf(JOptionPane.showInputDialog("Introduce el tiempo de simulación","10000"));
				simulation.setAttribute("time", ""+tiempo);
			} catch (Exception e) {
				e.printStackTrace();
				tiempo = null;
				JOptionPane.showMessageDialog(null, "El tiempo introducido no es correcto");
			}
		}
		comentarios = new Comment("Format\nLabel: execute; Atributes: id, time.");
		simulation.addContent(comentarios);
		
		
		try {
			XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
			// String fileName =
			// JOptionPane.showInputDialog("Introduce el nombre del fichero");
			// file = new FileOutputStream(fileName);
			file = new FileOutputStream(fileName + "-eventos.xml");
			out.output(documento, file);
			file.flush();
			file.close();
			out.output(documento, System.out);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		JOptionPane.showMessageDialog(null, "Los ficheros se han creado correctamente: \n" + fileName + "-topologia.xml\n" + fileName + "-eventos.xml");

	}
}
