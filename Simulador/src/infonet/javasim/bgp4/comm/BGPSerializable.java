// ========================================================================= //
// @(#) BGPSerializable.java
//
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 13/04/2002
// ========================================================================= //

package infonet.javasim.bgp4.comm;

public interface BGPSerializable
{

    // ---- BGPSerializable.toBytes ---------------------------------------- //
    /**
     *
     */
    public abstract byte [] toBytes();

    // ---- BGPSerializable.fromBytes -------------------------------------- //
    /**
     *
     */
    public abstract void fromBytes(byte [] bytes);

}
