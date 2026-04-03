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
public class Edge3D implements Comparable<Edge3D> {
    
    final Vertex3D fVertex;
    final Vertex3D sVertex;
    
    public Edge3D(Vertex3D fVert, Vertex3D sVert) {
        fVertex = fVert;
        sVertex = sVert;
    }
    
    @Override
    public int compareTo(Edge3D o) {
        double z = fVertex.z + sVertex.z - o.fVertex.z - o.sVertex.z;
        if (z < 0) return -1;
        if (z > 0) return 1;
        return 0;
    }

    public double length() {
        return Math.sqrt((fVertex.x - sVertex.x) * (fVertex.x - sVertex.x) +
                (fVertex.y - sVertex.y) * (fVertex.y - sVertex.y) +
                (fVertex.z - sVertex.z) * (fVertex.z - sVertex.z));
    }
    
}
