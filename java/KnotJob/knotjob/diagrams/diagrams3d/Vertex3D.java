/*

Copyright (C) 2021 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

This file is part of KnotJob.

KnotJob is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

KnotJob is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTIBILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org.licenses/>.

 */

package knotjob.diagrams.diagrams3d;

/**
 *
 * @author Dirk
 */
public class Vertex3D {
    
    double x;
    double y;
    double z;
    Edge3D inEdge;
    Edge3D outEdge;
    
    public Vertex3D(double xx, double yy, double zz) {
        x = xx;
        y = yy;
        z = zz;
        inEdge = null;
        outEdge = null;
    }
    
    @Override
    public String toString() {
        return "("+x+", "+y+", "+z+")";
    }
    
}
