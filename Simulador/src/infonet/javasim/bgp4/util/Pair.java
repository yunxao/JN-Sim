/**
 * Pair.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4.util;




// ===== class SSF.OS.BGP4.Util.Pair ======================================= //
/**
 * A pair of objects.
 */
public class Pair <Obj1,Obj2>{

  // ......................... constants ........................... //

  // ........................ member data .......................... //

  /** The first item in the pair. */
  public Obj1 item1;

  /** The second item in the pair. */
  public Obj2 item2;


  // ----- constructor Pair ------------------------------------------------ //
  /**
   * Builds a pair given two objects.
   */
  public Pair(Obj1 obj1, Obj2 obj2) {
    item1 = obj1;
    item2 = obj2;
  }
  public Obj1 item1(){return item1;}
  public Obj2 item2(){return item2;}
  

} // end class Pair
