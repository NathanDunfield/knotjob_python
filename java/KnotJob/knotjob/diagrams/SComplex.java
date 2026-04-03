/*

Copyright (C) 2019-20 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.diagrams;

import java.util.ArrayList;

/**
 *
 * @author Dirk
 */
public class SComplex {
    
    public ArrayList<Vertex> vertices;
    public ArrayList<Edge> edges;
    ArrayList<Triangle> triangles;
    double maxx;
    double maxy;
    ArrayList<int[]> unlinkComp;

    public SComplex() {
        vertices = new ArrayList<Vertex>();
        edges = new ArrayList<Edge>();
        triangles = new ArrayList<Triangle>();
        unlinkComp = new ArrayList<int[]>();
    }

    public void addVertex(Vertex vert) {
        vertices.add(vert);
    }

    public Vertex vertexAt(double a, double b) {
        boolean found = false;
        int i = 0;
        while (!found && i < vertices.size()) {
            Vertex vert = vertices.get(i);
            if (vert.x == a && vert.y == b) found = true;
            else i++;
        }
        if (!found) return null;
        return vertices.get(i);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }
}
