/* 
 * @(#)MobilityException.java   0.1   2002/01
 *
 * (C) Copyright Infonet Group 2002 - FUNDP
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package infonet.javasim.util.exception;

/**
 * MobilityException is the main class for exceptions thow by the mobility management classes in the
 * infonet package.
 *
 * @see infonet.javasim.mobility.MobilityTriggerServer
 *
 * @author Pierre Reinbold
 * @version 0.1 2002
 */
public class MobilityException extends RuntimeException
{
	public MobilityException()
	{ 
        super(); 
    }

	public MobilityException(String msg_)
	{ 
        super(msg_); 
    }
}
