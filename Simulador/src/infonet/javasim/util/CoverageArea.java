/* 
 * @(#)CoverageArea.java   0.1   2002/04
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

package infonet.javasim.util;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import infonet.javasim.util.exception.CoverageAreaException;

/**
 * <p><code>CoverageArea</code> is a simple 2D representation for the covarage Area of
 * each Base Station : this class is <strong>not</strong> meant to be an exact picture of this
 * area but an approximation allowing to reduce computation at runtime.
 *
 * @author Pierre Reinbold
 * @version 0.1 2002/04
 */

public abstract class CoverageArea
{
    /**
    * The default type
    */
    public static int DEFAULT_AREA = 0;

   /**
    * Type for the PolygonalCoverageArea class
    */
    public static int POLYGONAL_2D = 1;
    
   /**
    * The area type : 0 represents obviously an error =)
    */
    protected int type = DEFAULT_AREA;
    
    public static String defaultUnsupportedTypeErrorMessage = "Unsupported area type";
    public static String defaultUnsupportedCoordinate = "Unsupported coordinate type";
    public int getType()
    {
        return type;
    }
    
    public void setType(int type_)
    {
        type = type_;
    }

   /**
    * Return true if this area intersects with area_!
    *
    * @param area_     The area to check for intersection
    *
    * @throws CoverageAreaException if the type of area_ is unsupported
    */
    public abstract boolean intersects(CoverageArea area_) throws CoverageAreaException;

   /**
    * Return true if this area contains the point <code>pos_</code>
    *
    * @param pos_     The position to check
    *
    * @throws CoverageAreaException if the type of position provided is unsupported
    */
    public abstract boolean contains(double[] pos_) throws CoverageAreaException;

   /**
    * Return the CoverageArea data : these data is dependent of the type of the area
    */
    public abstract Vector getData();

   /**
    * Apply <code>trsl</code> to the <code>CoverageArea</code>
    */
    public abstract CoverageArea translate(double[] trsl) throws CoverageAreaException;
}
