/**
 * Pair.java
 *
 * @author Francisco Huertas
 */


package tid.inet.protocols.trafficInspectionTool;




	// ===== class SSF.OS.BGP4.Util.Pair ======================================= //
/**
 * A pair of objects.
 */
public class Pair <Obj1,Obj2>{
	/** 
	 * The first item in the pair. 
	 */
	private Obj1 item1;
	
	/** 
	 * The second item in the pair. 
	 */
	private Obj2 item2;
	
	
	// ----- constructor Pair ------------------------------------------------ //
	/**
	 * Builds a pair given two objects.
	 */
	public Pair(Obj1 obj1, Obj2 obj2) {
		item1 = obj1;
		item2 = obj2;
	}
	public Obj1 item1(){
		return item1;
	}
	public Obj2 item2(){
		return item2;
	}
	public void item1(Obj1 obj1){
		
	}
	public void item2(Obj2 obj1){
		
	}
  

} // end class Pair