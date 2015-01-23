/**
 * InBuffer.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4;



// ===== interface SSF.OS.BGP4.InBuffer ==================================== //
/**
 * Buffers incoming and local BGP messages and events.
 */
interface InBuffer {

  // ......................... constants ........................... //


  // ........................ member data .......................... //



  // ----- InBuffer.size --------------------------------------------------- //
  /**
   * Returns the number of events and/or messages in the buffer.
   */
  public abstract int size();

  // ----- InBuffer.next --------------------------------------------------- //
  /**
   * Removes the next event/message in the buffer, along with its associated
   * protocol session, and returns them.
   */
  public abstract Object next();

  // ----- InBuffer.add ---------------------------------------------------- //
  /**
   * Adds an event/message, with its associated protocol session, to the
   * buffer.
   */
    /*
  public abstract void add(ProtocolMessage message,
                           ProtocolSession fromSession);
    */


} // end interface InBuffer
