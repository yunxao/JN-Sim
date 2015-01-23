package tid.inet.protocols.trafficInspectionTool;

import drcl.comp.Port;

public class TITOperation {
	public static final int TRACEROUTE = 0;
	public static final int PING = 1;
	private int operation;
	private long address;
	private double peridicity;
	private boolean enable;
	private String logFileName;
	private boolean consoleEnable;
	private Port logPort;

	public TITOperation(int operation, long address, double peridicity,
			boolean consoleEnable, String logFileName) {
		super();
		this.logFileName = logFileName;
		this.enable = true;
		this.operation = operation;
		this.address = address;
		this.peridicity = peridicity;
		this.consoleEnable = consoleEnable;
	}
	public TITOperation(int operation, long address, double peridicity,
			boolean consoleEnable) {
		super();
		this.logFileName = null;

		this.enable = true;
		this.operation = operation;
		this.address = address;
		this.peridicity = peridicity;
		this.consoleEnable = consoleEnable;
	}
	public void setLogPort(Port logPort){
		this.logPort = logPort;
	}
	public Port getLogPort(){
		return logPort;
	}
	public int getOperation() {
		return operation;
	}
	public long getAddress() {
		return address;
	}
	public double getPeridicity() {
		return peridicity;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public boolean isEnable() {
		return enable;
	}
	public boolean isConsoleEnable(){
		return consoleEnable;
	}
	public void setConsoleEnable(boolean value){
		consoleEnable = value;
	}
	public String getLogFileName() {
		return logFileName;
	}
}
