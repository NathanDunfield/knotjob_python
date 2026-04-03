/*

Copyright (C) 2024 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.polynomial.moy;

/**
 *
 * @author Dirk
 */
public class Edge {
    
    private final Vertex startVertex;
    private final Vertex endVertex;
    
    public Edge(Vertex st, Vertex en) {
        startVertex = st;
        endVertex = en;
    }
    
    public boolean equals(Edge ed) {
        return startVertex.equals(ed.startVertex) && endVertex.equals(endVertex);
    }
    
    @Override
    public String toString() {
        return startVertex.toString()+" --> "+endVertex.toString();
    }

    public Vertex getStartVertex() {
        return startVertex;
    }
    
    public Vertex getEndVertex() {
        return endVertex;
    }
    
    public int[] vertexTypes() {
        return new int[] {startVertex.theType(), endVertex.theType()};
    }
    
    public int[] vertexLabels() {
        return new int[] {startVertex.theLabel(), endVertex.theLabel()};
    }
    
}
