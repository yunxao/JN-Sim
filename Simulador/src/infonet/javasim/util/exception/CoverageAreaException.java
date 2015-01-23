/* 
 * @(#)CoverageAreaException.java   0.1   2002/04
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
 * CoverageAreaException is the main class for exceptions thow by the map classes in the
 * infonet package.
 *
 * @see infonet.javasim.util.CoverageArea
 *
 * @author Pierre Reinbold
 * @version 0.1 2002
 */
public class CoverageAreaException extends RuntimeException
{
	public CoverageAreaException()
	{ 
        super(); 
    }

	public CoverageAreaException(String msg_)
	{ 
        super(msg_); 
    }
}
