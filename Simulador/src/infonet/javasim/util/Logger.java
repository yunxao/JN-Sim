// =============================================================== //
// @(#)Logger.java
//
// Logger class
// Infonet Group, University of Namur
//
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
// @date 09/04/2002
// @lastdate 11/04/2002
// =============================================================== //

package infonet.javasim.util;

import java.awt.*;
import java.util.*;
import javax.swing.*;

public class Logger extends JFrame
{

    /**
     * Static instance of the logger. This instance is used by the static
     * methods 'setupArea' and 'addMessage'. The private static method
     * 'checkLogger' allocates the instance if required.
     */
    private static Logger logger;

    /**
     * Table of all text-areas keyed by 'long' identifiers.
     */
    private HashMap textAreas= new HashMap();

    /**
     * Tabbed pane containing every text-areas.
     */
    private JTabbedPane tabbedPane;

    // ----- Logger -------------------------------------------------------- //
    /**
     * Constructor for the Logger class.
     */
    public Logger()
    {
	super();
	setTitle("Logger");
	setSize(300, 200);
	tabbedPane= new JTabbedPane();
	getContentPane().add(tabbedPane, "Center");
    }

    // ----- Logger.getTextArea -------------------------------------------- //
    /**
     * Return the text-area associated to the given identifier. This method
     * is thread safe (the JTabbedPane.addTab call is deferred and
     * synchronized with the main thread).
     *
     * @param id Identifier of the text-area. If the specified text-area does
     *           not exist, it is allocated.
     */
    public JTextArea getTextArea(final long id)
    {
	JTextArea textArea= null;
	Object obj= textAreas.get(new Long(id));
	if ((obj != null) && (obj instanceof JTextArea)) {
	    textArea= (JTextArea) obj;
	} else {
	    textArea= new JTextArea();
	    final JScrollPane scrollPane= new JScrollPane(textArea);
	    textAreas.put(new Long(id), textArea);
	    EventQueue.invokeLater(new Runnable() {
		    private final long id_= id;
		    public void run() {
			tabbedPane.addTab(String.valueOf(id_), scrollPane);
		    }
		});
	}
	return textArea;
    }

    // ----- Logger.getLogger ---------------------------------------------- //
    /**
     * Check if the static instance of the class is allocated. If this is
     * not the case, the method allocates it.
     */
    private static void checkLogger()
    {
	if (logger == null) {
	    logger= new Logger();
	    logger.show();
	}
    }

    // ----- Logger.setupArea ---------------------------------------------- //
    /**
     * Change the title of the pane associated with the given identifier.
     *
     * @param id Identifier of the text-area. If the specified text-area does
     *           not exist, it is allocated.
     * @param title Title of the text area.
     */
    public static void setupArea(long id, String title)
    {
	checkLogger();
	logger.getTextArea(id).append("set_title("+title+")\n");	
    }

    // ----- Logger.addMessage --------------------------------------------- //
    /**
     * Add a message to the text-area associated with the given identifier.
     *
     * @param id Identifier of the text-area. If the specified text-area does
     *           not exist, it is allocated.
     * @param msg Message to be added.
     */
    public static void addMessage(long id, String msg)
    {
	/*
	checkLogger();
	logger.getTextArea(id).append(msg+"\n");
	*/
    }

}













