/* 
 * @(#)PolygonalCoverageArea.java   0.1   2002/04
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

import infonet.javasim.util.exception.*;
import infonet.javasim.util.*;
import java.awt.geom.*;
import java.util.*;
import java.awt.*;

/**
 * <p><code>PolygonalCoverageArea</code> is a simple 2D polygonal representation for the covarage Area of
 * each Base Station : this class is <strong>not</strong> meant to be an exact picture of this
 * area but an approximation allowing to reduce computation at runtime.
 *
 * @author Pierre Reinbold
 * @version 0.1 2002/04
 */

public class PolygonalCoverageArea extends CoverageArea
{
    private double[] coordx;
    private double[] coordy;
    private GeneralPath shape;
    private int dimension = 2;

    private String defaultBadInitException = 
        "Bad initialization data for a PolygonalCoverageArea\n";

   /**
    * Simplest constructor for the Coverage Area
    *
    * @throws <code>CoverageAreaException</code> if not enough points are provided (min = 3)
    */
    public PolygonalCoverageArea(double[] coordx_, double[] coordy_)
    {
        if ( (coordx_.length <= 2) || (coordy_.length <= 2) ||  (coordx_.length != coordy_.length) )
        {
            throw new CoverageAreaException(defaultBadInitException);
        }
        else
        {
            coordx = new double[(coordx_.length + 1)];
            coordy = new double[(coordy_.length + 1)];
            System.arraycopy(coordx_, 0, coordx, 0, coordx_.length);
            System.arraycopy(coordy_, 0, coordy, 0, coordy_.length);
            coordx[coordx_.length] = coordx_[0];
            coordy[coordy_.length] = coordy_[0];    // close the polygon
            
            type = CoverageArea.POLYGONAL_2D;

            // Constructs the shape General Path
            shape = new GeneralPath();
            for (int i = 0; i < (coordx.length - 1); i++)
            {
                shape.append(new Line2D.Double(coordx[i], coordy[i], coordx[i+1], coordy[i+1]), true);
            }
        }
    }

    public boolean contains(double[] pos) throws CoverageAreaException
    {
        if (pos.length == dimension)
        {
            return shape.contains(pos[0], pos[1]);
        }
        else
        {
            throw new CoverageAreaException(CoverageArea.defaultUnsupportedCoordinate);
        }
    }

   /**
    * Return the coordinates for the differents points of this area : <strong>the last of these points is
    * the same as the first one in order to close the path</strong>!!!
    */
    public Vector getData()
    {
        Vector data = new Vector();

        data.add(coordx);
        data.add(coordy);
        
        return data;
    }

   /**
    * Return <code>true</code> if this area intersects with <code>area_</code>!
    *
    * @param <code>area_</code>     The area to check for intersection
    *
    * @throws <code>CoverageAreaException</code> if the type of <code>area_</code> is unsupported
    */
    public boolean intersects(CoverageArea area_) throws CoverageAreaException
    {
        if ( area_.getType() != CoverageArea.POLYGONAL_2D )
        {
            throw new CoverageAreaException(CoverageArea.defaultUnsupportedTypeErrorMessage);
        }
        else
        {
            Vector dataArea = area_.getData();
            double [] coordxArea = ((double[]) dataArea.get(0));
            double[] coordyArea = ((double[]) dataArea.get(1));

            for (int i = 0; i < (coordx.length - 1); i++)
            {
                for (int j = 0; j < (coordxArea.length - 1); j++)
                {        
                    // Simplest method ever: if it exist an intersection between two vertice,
                    // the polygons have an intersection!
                    //
                    // Check intesection between 
                    // [(coordx(i), coordy(i)); (coordx(i+1), coordy(i+1))] 
                    // and
                    // [(coordxArea(j), coordyArea(j)); (coordxArea(j+1), coordyArea(j+1))]

                    if (Line2D.linesIntersect(coordx[i], coordy[i], coordx[i+1], coordy[i+1],
                        coordxArea[j], coordyArea[j], coordxArea[j+1], coordyArea[j+1]))
                    {
                        return true;
                    }
                }
            }
            // Other possibility : a shape is entirely contained in the other!
            // Hence : all point of this shape are contained in the other
            return (allContained(area_, coordx, coordy) || allContained(this, coordxArea,
            coordyArea));
        }
    }

    private boolean allContained(CoverageArea area, double[] x, double[] y)
    {
        for (int i = 0; i < (x.length - 1); i++)
        {
                if (!area.contains(new double[] {x[i], y[i]}))
                {
                    return false;
                }
        }
        return true;
    }
    
   /**
    * Apply <code>trsl</code> to the <code>CoverageArea</code>
    */
    public CoverageArea translate(double[] trsl) throws CoverageAreaException
    {
        if (trsl.length == 2)
        {
            // apply to coord
            for (int i = 0; i < coordx.length; i++)
            {
                coordx[i] = coordx[i] + trsl[0];
                coordy[i] = coordy[i] + trsl[1];
            }
            // apply to shape
            AffineTransform at = new AffineTransform(1.0, 0.0, 0.0, 1.0, trsl[0], trsl[1]);
            shape.transform(at);
            return this;
        }
        else
        {
            throw new CoverageAreaException(CoverageArea.defaultUnsupportedCoordinate);            
        }
    }
}
