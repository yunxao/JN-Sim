// ===========================================================================
// @(#)PeerConnection.java
//
// Class that manages the connection to a peer.
//
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 14/05/2002
// ===========================================================================

package infonet.javasim.bgp4;

import infonet.javasim.bgp4.comm.BGPMessage;
import infonet.javasim.bgp4.comm.BGPSerializable;
import infonet.javasim.bgp4.comm.TransportMessage;
import infonet.javasim.bgp4.timing.EventTimer;
import infonet.javasim.util.TimerMaster;

import java.io.IOException;
import java.util.ArrayList;

import tid.Enviroment;
import tid.graphic.GraphicBGPEventManager;
import drcl.inet.socket.InetSocket;
import drcl.inet.socket.NonblockingSocketHandler;
import drcl.inet.socket.SocketListener;
import drcl.inet.socket.SocketMaster;

public class PeerConnection implements PeerConnectionConstants, SocketListener,
		NonblockingSocketHandler {

	/**
	 * Origins of the connection (used in the tie-breaking part of the
	 * connection collision detection algorithm. LOCAL = initiated localy
	 * (connect) REMOTE = initiated by peer (listen)
	 */
	public static final byte ORIGIN_LOCAL = 0;
	public static final byte ORIGIN_REMOTE = 1;

	/**
	 * State of the connection
	 */
	private int state;

	/**
	 * Socket of the connection
	 */
	private InetSocket socket;

	/* PeerEntry associated to this connection */
	protected PeerEntry peerEntry;

	/* SocketMaster associated to the node */
	private SocketMaster socketMaster;

	/* */
	private boolean closed;

	/* Origin of the connection */
	private byte origin;

	protected final int MAX_BGP_MESSAGE_SIZE = 4096;
	protected byte[] currentMessage = new byte[MAX_BGP_MESSAGE_SIZE];
	protected int currentMessageSize = 0;
	protected int currentMessageLength = -1;
	protected Object currentMessageLock = new Object();

	/** The Hold Timer, for timing out connections with peers. */
	public EventTimer holdTimer;

	/**
	 * The KeepAlive Timer, for helping to ensure this peer doesn't time out its
	 * connection with us.
	 */
	public EventTimer keepAliveTimer;

	/**
	 * A queue of writes waiting to be performed on the write socket. The queue
	 * is necessary since a write may be issued before the previous write has
	 * successfully completed. (For a write to successfully complete, the
	 * underlying TCP session must receive an acknowledgement for the bytes that
	 * were sent).
	 */
	public ArrayList writeQueue = new ArrayList(2);

	// ----- PeerConnection ------------------------------------------------ //
	/**
	 * Constructor for a PeerConnection class. This constructor is intended for
	 * remotely initiated connections (listen).
	 */
	public PeerConnection(PeerEntry peerEntry, InetSocket socket) {
		super();
		this.peerEntry = peerEntry;
		this.socket = socket;
		state = CONNECT; // already in CONNECT state since it was
		// initiated by the peer.
		// peerEntry.setConnection(this);
		socketMaster = peerEntry.bgp.socketMaster;
		holdTimer = null;
		keepAliveTimer = null;
		closed = false;
		origin = ORIGIN_REMOTE;
	}

	// ----- PeerConnection ------------------------------------------------ //
	/**
	 * Constructor for a PeerConnection class. This constructor is intended for
	 * localy initiated connections (connect).
	 */
	public PeerConnection(PeerEntry peerEntry) {
		this(peerEntry, null);
		state = IDLE;
		origin = ORIGIN_LOCAL;
	}

	// ----- PeerConnection.manageReceive ---------------------------------- //
	/**
	 * Method used to spawn the thread responsible for the reception of messages
	 * addressed to this peer.
	 */
	public void manageReceive() {
		socket.registerListener(this);
	}

	// ----- PeerEntry.receiveThread -------------------------------------- //
	/**
	 * Main method of the thread responsible for the reception of messages
	 * addressed to this peer.
	 */
	/*
	 * public void receiveThread() { // Number of bytes to read int rlength; //
	 * Number of bytes that were effectively read int erlength; // Position in
	 * the array int pos;
	 * 
	 * 
	 * 
	 * 
	 * 
	 * peerEntry.bgp.logDebug("receiveThread started for peer "+peerEntry.ip_addr
	 * .val()+".");
	 * 
	 * BufferedReader is= new BufferedReader(new
	 * InputStreamReader(socket.getInputStream())); try { // Note: 4096 bytes is
	 * the maximum size of a BGP message char [] charHeader= new char[4096];
	 * byte [] header= new byte[4096];
	 * 
	 * // ---------------------------------------------- // While the session is
	 * alive, read messages from // connection ... //
	 * ---------------------------------------------- while
	 * (peerEntry.bgp.alive) {
	 * 
	 * // ----------------------------- // First, read header of message //
	 * ----------------------------- rlength= BGPMessage.OCTETS_IN_HEADER;
	 * 
	 * pos= 0; // Start writing bytes at beginning of buffer
	 * 
	 * while (rlength > 0) {
	 * 
	 * 
	 * 
	 * 
	 * 
	 * peerEntry.bgp.logDebug("read-message-header-begin("+peerEntry.ip_addr.val(
	 * )+"): "+rlength+"/"+pos); peerEntry.bgp.logDebug(socket.toString());
	 * erlength= is.read(charHeader, pos, rlength);
	 * peerEntry.bgp.logDebug("read-message-header-end("
	 * +peerEntry.ip_addr.val()+"): "+erlength);
	 * 
	 * if (erlength <= 0) { // Fire TransConnClose event to indicate that the //
	 * transport-layer connection has been closed.
	 * System.out.println("ERROR: receiveThread");
	 * peerEntry.bgp.logDebug("ERROR: receiveThread(is.read[1]:"
	 * +erlength+"/"+BGPMessage.OCTETS_IN_HEADER+")"); peerEntry.bgp.push(new
	 * TransportMessage(BGPSession.TransConnClose, this));
	 * 
	 * return; }
	 * 
	 * rlength-= erlength; pos+= erlength;
	 * 
	 * }
	 * 
	 * for (int i= 0; i < 19; i++) header[i]= (byte) charHeader[i];
	 * 
	 * // Extract message length from header int length= (header[16] <<
	 * 8)+header[17]; //System.out.println("LENGTH: "+length);
	 * 
	 * // --------------------------------------------------- // Second, if the
	 * message is not empty, read remaining // bytes ... //
	 * --------------------------------------------------- if (length > 0) {
	 * 
	 * // Number of bytes to read rlength= length-BGPMessage.OCTETS_IN_HEADER;
	 * 
	 * // Position of new bytes in array pos= BGPMessage.OCTETS_IN_HEADER;
	 * 
	 * // While there are bytes to read and we are // not at the end of the
	 * stream ... while (rlength > 0) {
	 * 
	 * // Read as many bytes as possible from the // connection.
	 * peerEntry.bgp.logDebug
	 * ("read-message-begin("+peerEntry.ip_addr.val()+"): "+rlength+"/"+pos);
	 * erlength= is.read(charHeader, pos, rlength);
	 * peerEntry.bgp.logDebug("read-message-end("
	 * +peerEntry.ip_addr.val()+"): "+erlength);
	 * 
	 * // Check result of read if (erlength <= 0) { // Fire TransConnClose event
	 * to indicate that // the transport-layer connection has been // closed.
	 * System.out.println("ERROR: receiveThread");
	 * peerEntry.bgp.logDebug("ERROR: receiveThread(is.read[3]:"
	 * +rlength+"/"+(length-BGPMessage.OCTETS_IN_HEADER)+")");
	 * peerEntry.bgp.push(new TransportMessage(BGPSession.TransConnClose,
	 * this)); return; // error }
	 * 
	 * // Update number of bytes read and position rlength-= erlength; pos+=
	 * erlength;
	 * peerEntry.bgp.logDebug("receiveThread=>read-end("+rlength+","+pos+")");
	 * 
	 * }
	 * 
	 * for (int i= 0; i < length; i++) header[i]= (byte) charHeader[i];
	 * 
	 * // Build new message and send it ... BGPMessage msg=
	 * BGPMessage.buildNewMessage(header); msg.peerConnection= this;
	 * peerEntry.bgp.push(msg);
	 * 
	 * } else { // *** (length == 0) ***
	 * System.out.println("ERROR: zero-length message received");
	 * peerEntry.bgp.logDebug("ERROR: receiveThread(length==0)"); return; //
	 * error }
	 * 
	 * } peerEntry.bgp.logDebug("receiveThread exited."); } catch (IOException
	 * e) { System.out.println("ERROR: receiveThread");
	 * peerEntry.bgp.logDebug("ERROR: receiveThread(IOException)"); // Fire
	 * TransConnClose event to indicate that the // transport-layer connection
	 * has been closed. peerEntry.bgp.push(new
	 * TransportMessage(BGPSession.TransConnClose, this)); e.printStackTrace();
	 * 
	 * return; // error } }
	 */

	// ----- PeerEntry.connect -------------------------------------------- //
	/**
     *
     */
	public void manageConnect() {
		if (socket == null) {
			(new ConnectTimer(peerEntry.bgp.timerMaster, this)).set();
			return;
		}

		// Fire TransConnOpen event to indicate that the transport-layer
		// connection has been opened.

		peerEntry.bgp.push(new TransportMessage(peerEntry.bgp.TransConnOpen,
				this));
	}

	// ----- PeerEntry.connectThread -------------------------------------- //
	/**
	 * Establish a TCP connection to the peer. We must be in the CONNECT state
	 * and wish to go to OPENSENT state.
	 */
	public void connectThread() {
		socket = socketMaster.newSocket();
		socket.registerListener(this);
		socketMaster.bind(socket, peerEntry.return_ip.val(), 0);

		try {
			socketMaster.aConnect(socket, peerEntry.ip_addr.val(),
					peerEntry.bgp.getPort(), this);

			// Manage the connection
			/* manageConnect(); */

			// bqu: bgp.mon.msg(Monitor.SOCKET_EVENT, 7, pe);
		} catch (IOException e) {
			// Fire TransConnOpenFail event to indicate that the
			// transport-layer connection could not be opened.
			System.out.println("ERROR: connectThread");
			peerEntry.bgp.push(new TransportMessage(
					peerEntry.bgp.TransConnOpenFail, this));
		}
	}

	// ----- PeerConnection.getState --------------------------------------- //
	/**
	 * Return the state of this connection.
	 */
	public int getState() {
		return state;
	}

	// ----- PeerConnection.getOrigin -------------------------------------- //
	/**
	 * Return the origin of the connection (ORIGIN_LOCAL or ORIGIN_REMOTE).
	 */
	public byte getOrigin() {
		return origin;
	}

	// ----- PeerConnection.send ------------------------------------------- //
	/**
	 * Attempt to send a message to this peer. If the socket is busy, the data
	 * to be written will be enqueued until the socket is free.
	 * 
	 * @param msg
	 *            A BGP message to be sent to the peer.
	 */
	public final void send(BGPMessage msg) {
		write(msg);
	}

	// ----- PeerConnection.write ------------------------------------------ //
	/**
	 * Attempt to write to a socket that either is or was connected to this
	 * peer. Since it is possible that a new socket is opened before all packets
	 * are done being written to an old one (pending closing), we need to give
	 * the option of specifying which write socket to write to.
	 * 
	 * @param ws
	 *            A socket to write to.
	 * @param msg
	 *            A BGP message to be written to the socket.
	 */
	private synchronized final void write(BGPMessage msg) {
		peerEntry.bgp.logDebug("write-message{to: " + peerEntry.ip_addr.val()
				+ ", msg: " + msg.toString() + "}");
		(new WriteTimer(peerEntry.bgp.timerMaster, this, msg)).set();
	}

	// ----- PeerEntry.writeThread ----------------------------------------- //
	/**
     *
     */
	public synchronized void writeThread(BGPMessage msg) {
		if (!(msg instanceof BGPSerializable)) {
			System.out
					.println("Internal error: can not send a message not serializable !");
			return;
		}

		// OutputStream os= socket.getOutputStream();
		try {
			msg.peerConnection = this;
			byte[] outBytes = ((BGPSerializable) msg).toBytes();
			socket.send(outBytes, 0, outBytes.length);
		} catch (IOException e) {
			System.out.println("ERROR: writeThread");
			peerEntry.bgp.logDebug("ERROR: writeThread(IOException)");
			e.printStackTrace();
			peerEntry.bgp.push(new TransportMessage(BGPSession.TransConnClose,
					this));
		}
	}

	// ----- PeerConnection.setState --------------------------------------- //
	/**
	 * Change the state of this connection. This method should be called by
	 * BGPSession.receive only (heart of the state machine).
	 */
	public void setState(int state) {
		this.state = state;
	}

	// ----- PeerConnection.getEntry --------------------------------------- //
	/**
     *
     */
	public PeerEntry getEntry() {
		return peerEntry;
	}

	// ----- PeerConnection.incOutUpdates ---------------------------------- //
	/**
     *
     */
	public void incOutUpdates() {
		peerEntry.outUpdates++;
	}

	// ----- PeerConnection.clearWriteQueue -------------------------------- //
	/**
     *
     */
	public void clearWriteQueue() {
		writeQueue.clear();
	}

	// ----- PeerConnection.close ------------------------------------------ //
	/**
     *
     */
	public void close() {
		if (!closed) {
			closed = true;
			cancelHoldTimer();
			cancelKeepAliveTimer();
			peerEntry.clearConnection(this);
			/*
			 * if (socket != null) { try {
			 * System.out.println("closing connection"); socket.close();
			 * System.out.println("closing connection: done"); } catch
			 * (IOException e) { throw new
			 * Error("ERROR: PeerConnection.close() ["+ e.getMessage()+"]"); }
			 * socket= null; }
			 */
		}
	}

	public boolean isClosed() {
		return closed;
	}

	// ----- PeerConnection.cancelHoldTimer -------------------------------- //
	/**
	 * Cancel the HOLD timer. Note that the timer is not freed.
	 */
	public void cancelHoldTimer() {
		if (holdTimer != null)
			holdTimer.cancel();
	}

	// ----- PeerConnection.cancelKeepAliveTimer --------------------------- //
	/**
	 * Cancel the KEEP-ALIVE timer. Note that the timer is not freed.
	 */
	public void cancelKeepAliveTimer() {
		if (keepAliveTimer != null)
			keepAliveTimer.cancel();
	}

	// ----- PeerConnection.toString --------------------------------------- //
	/**
	 * Return a textual representation of the object.
	 */
	public String toString() {
		if (peerEntry != null) {
			return "(" + peerEntry.ASNum + ";" + peerEntry.ip_addr.toString()
					+ ")";
		}
		return "???";
	}

	// ----- Connect ------------------------------------------------------- //
	/**
	 * Inner class to manage the connection to the peer.
	 */
	public class ConnectTimer extends infonet.javasim.util.Timer {
		private PeerConnection connection;

		public ConnectTimer(TimerMaster master, PeerConnection connection) {
			super(master, 0);
			this.connection = connection;
		}

		public void callback() {
			connection.connectThread();
		}
	}

	// ----- Write --------------------------------------------------------- //
	/**
	 * Inner class to handle asynchronous writing to the peer.
	 */
	public class WriteTimer extends infonet.javasim.util.Timer {
		private BGPMessage msg;
		private PeerConnection connection;

		public WriteTimer(TimerMaster master, PeerConnection connection,
				BGPMessage msg) {
			super(master, 5);
			this.connection = connection;
			this.msg = msg;
		}

		public void callback() {
			connection.writeThread(msg);
		}
	}

	// ----- dataAvailable ------------------------------------------- //
	public synchronized void dataAvailable(InetSocket socket, int available) {
		// System.out.println("hSM: "+available+
		// " byte(s) available from "+socket);
		if (available > 0) {
			try {
				byte[] dummy = new byte[available];
				socket.receive(dummy, 0, dummy.length);
				(new ReadTimer(peerEntry.bgp.timerMaster, dummy, socket, this))
						.set();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// ----- SocketThread -------------------------------------------------- //
	public class ReadTimer extends infonet.javasim.util.Timer {

		protected byte[] msg;
		protected InetSocket socket;
		protected PeerConnection peerConnection;

		public ReadTimer(TimerMaster master, byte[] msg, InetSocket socket,
				PeerConnection peerConnection) {
			super(master, 0);
			this.msg = msg;
			this.socket = socket;
			this.peerConnection = peerConnection;
		}

		public void callback() {
			synchronized (currentMessageLock) {
				try {
					// -------------------------------------------------------
					// (1) Put data in receive buffer until message header is
					// complete.
					// (2) Once message header is complete. Figure out the
					// size of the message. Continue to fill the receive
					// buffer until the complete message has been read.
					// (3) Deserialize the received message and push it to the
					// BGP session ...
					// -------------------------------------------------------
					int mvSize;
					int msgPos = 0;

					while (msgPos < msg.length) {

						// currentMessageLength indicates if the header of the
						// current message has already been read and thus the
						// total length of the current message is known.
						if (currentMessageLength < 0) {

							if (currentMessageSize < BGPMessage.OCTETS_IN_HEADER) {
								mvSize = Math.min(msg.length - msgPos
										+ currentMessageSize,
										BGPMessage.OCTETS_IN_HEADER)
										- currentMessageSize;
								System.arraycopy(msg, msgPos, currentMessage,
										currentMessageSize, mvSize);
								currentMessageSize += mvSize;
								msgPos += mvSize;
							}

							// Note: this test can not be the 'else' part of the
							// previous test since the size of the current
							// message
							// has
							// changed !
							if (currentMessageSize >= BGPMessage.OCTETS_IN_HEADER) {
								// Extract message length from header
								int length = ((((int) currentMessage[16]) + 128) << 8)
										+ ((int) currentMessage[17]) + 128;
								currentMessageLength = length;
							}

						}

						// Note: this test can not be the 'else' part of the
						// previous test since the size of the current message
						// has
						// changed !
						if (currentMessageLength >= 0) {

							if (currentMessageSize < currentMessageLength) {
								mvSize = Math.min(msg.length - msgPos
										+ currentMessageSize,
										currentMessageLength)
										- currentMessageSize;
								System.arraycopy(msg, msgPos, currentMessage,
										currentMessageSize, mvSize);
								currentMessageSize += mvSize;
								msgPos += mvSize;

							}

							// Note: this test can not be the 'else' part of the
							// previous test since the size of the current
							// message
							// has
							// changed !
							if (currentMessageSize >= currentMessageLength) {

								// Build new message and send it ...
								BGPMessage msgBGP = BGPMessage
										.buildNewMessage(currentMessage);
								// System.out.println("");

								msgBGP.peerConnection = peerConnection;
								if (peerEntry == null)
									System.out
											.println("ERROR: peerEntry == null");
								if (peerEntry.bgp == null)
									System.out
											.println("ERROR: peerEntry.bgp == null");
								peerEntry.bgp.push(msgBGP);

								// Message has been read => reset current
								// message
								// length.
								currentMessageLength = -1;
								currentMessageSize = 0;
							}

						}

					}

				} catch (Exception e) {
					peerEntry.bgp.logDebug("A incorrect Update Message has been received");
					if (peerEntry.bgp.printDebug) {
						// e.printStackTrace();
						System.out.println("A incorrect Update Message has been received");

					}
					PeerEntry peer = peerConnection.peerEntry;
					peerEntry.bgp.logFSM(PeerConnectionConstants.statestr[peerConnection.getState()],
							"UpdateMsgErr", "IDLE", peerConnection);
					peer.bgp.disconnectPeer(peer,(byte)3, (byte)0);
					if (peerEntry.bgp.auto_reconnect(peer, peerConnection )) {
						if (peerEntry.bgp.printDebug)
							System.out.println("WARNING: Automatic reconnection...");
						peerEntry.bgp.startConnection(peer.addr,50.0);
					}
				}
			}
		}
	}

	// ///////////////////////////////////////////////////////////////
	// The following methods handle asynchronous calls to this socket
	// (currently, only connectFinished is used)
	// ///////////////////////////////////////////////////////////////

	// ----- PeerConnection.acceptFinished ----------------------- //
	public void acceptFinished(InetSocket serverSocket, InetSocket clientSocket) {
		throw new Error(
				"Error: PeerConnection.acceptFinished should never be called");
	}

	// ----- PeerConnection.connectFinished ---------------------- //
	public void connectFinished(InetSocket socket) {
		closed = false;
		this.socket = socket;
		manageConnect();
	}

	// ----- PeerConnection.closeFinished ------------------------ //
	public void closeFinished(InetSocket socket) {
		System.out.println("socket-close-finished");
	}

	// ----- PeerConnection.error -------------------------------- //
	public void error(InetSocket socket, IOException error) {
		if (Enviroment.errorFlag)
			System.out.println("socket-error [" + error.getMessage() + "]");
		peerEntry.bgp.logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
				"socket-error [" + error.getMessage() + "]");
		peerEntry.bgp.push(new TransportMessage(BGPSession.TransConnOpenFail,
				this));
		this.socket = null;
	}

	public InetSocket getSocket() {
		return socket;
	}

	public void setSocket(InetSocket socket) {
		this.socket = socket;
	}

}
