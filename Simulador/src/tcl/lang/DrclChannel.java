// @(#)DrclChannel.java   5/2002
// Copyright (c) 1998-2002, Distributed Real-time Computing Lab (DRCL) 
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// 
// 1. Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer. 
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution. 
// 3. Neither the name of "DRCL" nor the names of its contributors may be used
//    to endorse or promote products derived from this software without specific
//    prior written permission. 
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// 

package tcl.lang;

import java.awt.TextArea;
import java.io.*;
import drcl.comp.Port;

/**
 * Bridge between Tcl Channel and drcl OutChannel.
 * Let drcl OutChannel be used in Tcl as a Tcl Channel.
 */
public class DrclChannel extends Channel 
{
	public DrclChannel(Interp interp_, String chanName_, Port outport_)
	{
		interp = interp_;
		setChanName(chanName_);
		TclIO.registerChannel(interp, this);
		outport = outport_;
	}
	
	Interp interp;
	Port outport;
	
    /**
     * Perform a read on the sub-classed channel.  
     * 
     * @param interp is used for TclExceptions.  
     * @param type is used to specify the type of read (line, all, etc).
     * @param numBytes the number of byte to read (if applicable).
     */

    String read(Interp interp, int type, int numBytes) 
            throws IOException, TclException
	{ return ""; }


    /** 
     * Interface to write data to the Channel
     * 
     * @param interp is used for TclExceptions.  
     * @param outStr the string to write to the sub-classed channel.
     */

    protected void write(Interp interp, String outStr) 
            throws IOException, TclException
	{
		if (outport != null) outport.doSending(outStr);
	}


    /** 
     * Interface to close the Channel.  The channel is only closed, it is 
     * the responsibility of the "closer" to remove the channel from 
     * the channel table.
     */

    void close() throws IOException
	{}


    /** 
     * Interface to flush the Channel.
     *
     * @exception TclException is thrown when attempting to flush a 
     *            read only channel.
     * @exception IOEcception is thrown for all other flush errors.
     */

    void flush(Interp interp) 
            throws IOException, TclException 
	{}


    /** 
     * Interface move the current Channel pointer.
     * Used in file channels to move the file pointer.
     * 
     * @param offset The number of bytes to move the file pointer.
     * @param mode where to begin incrementing the file pointer; beginning,
     *             current, end.
     */

    void seek(long offset, int mode) throws IOException
	{}


    /** 
     * Interface to tell the value for the Channel pointer.
     * Used in file channels to return the current file pointer.
     */

	long tell()  throws IOException { return 0; }


    /**
     * Interface that returns true if the last read reached the EOF.

    boolean eof()
	{ return true; }
     */

    protected InputStream getInputStream() throws IOException
	{ throw new RuntimeException("not implemented"); }
	
    protected OutputStream getOutputStream() throws IOException
	{ throw new RuntimeException("not implemented"); }
	
	String getChanType()
	{ return "DRCL OutChannel"; }
}
