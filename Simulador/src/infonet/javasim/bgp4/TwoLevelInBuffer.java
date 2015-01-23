/**
 * TwoLevelInBuffer.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4;

import java.util.ArrayList;
import infonet.javasim.bgp4.comm.*;

// ===== class SSF.OS.BGP4.TwoLevelInBuffer ================================ //
/**
 * Buffers incoming and local BGP messages and events using two levels of
 * priority.  Update messages are low priority, and all other events and
 * messages are high priority.
 */
class TwoLevelInBuffer implements InBuffer {

  // ......................... constants ........................... //


  // ........................ member data .......................... //

  /** A FIFO queue for all high priority BGP events/messages. */
  private ArrayList hiq = new ArrayList();

  /** A FIFO queue for all low priority BGP events/messages. */
  private ArrayList loq = new ArrayList();

  /** The BGP instance associated with this debugging manager. */
  private BGPSession bgp;


  // ----- constructor TwoLevelInBuffer ------------------------------------ //
  /**
   * Constructs a new buffer given a BGP instance.
   */
  public TwoLevelInBuffer(BGPSession b) {
    bgp = b;
  }

  // ----- TwoLevelInBuffer.size ------------------------------------------- //
  /**
   * Returns the total number of events and/or messages in the buffer.
   * Includes events/messages of either priority.
   */
  public int size() {
    return hiq.size() + loq.size();
  }

  // ----- TwoLevelInBuffer.next ------------------------------------------- //
  /**
   * Removes the next event/message in the buffer and returns it.
   */
  public Object next() {
    if (hiq.size() > 0) {
      return hiq.remove(0);
    } else if (loq.size() > 0) {
      return loq.remove(0);
    } else {
      return null;
    }
  }

  // ----- TwoLevelInBuffer.add -------------------------------------------- //
  /**
   * Adds an event/message, with its associated protocol session, to the
   * appropriate queue.
   */
  public void add(BGPMessage message) {
    Object[] tuple = { message };
    if (message instanceof UpdateMessage && Global.low_update_priority) {
      loq.add(tuple);
      if (Global.notice_update_arrival) {
         // Add it to the high priority queue as well as an "update arrival
         // notice" only.
        ((UpdateMessage)message).treat_as_update = false;
        hiq.add(tuple);
      }
    } else {
      hiq.add(tuple);
    }
  }

} // end interface TwoLevelInBuffer.java
