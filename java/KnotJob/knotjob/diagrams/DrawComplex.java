/*

Copyright (C) 2020 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import java.util.Collections;

/**
 *
 * @author Dirk
 */
public class DrawComplex {
    
    final ArrayList<Vertex> vertices;
    private final double subdivFactor;
    
    public DrawComplex(SComplex complex, double factor) {
        vertices = new ArrayList<Vertex>();
        for (Vertex vert : complex.vertices) {
            if (vert.type <= 1) {
                vertices.add(vert);
                if (vert.type == 0) vert.comb.clear();
            }
        }
        subdivFactor = factor;
        addVertices();
        subdivide();
    }
    
    public DrawComplex(SComplex complex) {
        vertices = new ArrayList<Vertex>();
        for (Vertex vert : complex.vertices) {
            vertices.add(vert);
        }
        Collections.sort(vertices);
        subdivFactor = 5;
    }
    
    public DrawComplex(int i) {
        vertices = new ArrayList<Vertex>(1);
        vertices.add(new Vertex(10.0, 10.0, 3, i, true));
        vertices.add(new Vertex(2.0, 2.0, 5, 0, true));
        vertices.add(new Vertex(18.0, 18.0, 5, 0, true));
        subdivFactor = 4.0;
    }
    
    public ArrayList<Vertex> getVertices() {
        return vertices;
    }
    
    private void subdivide() {
        ArrayList<Vertex> newVertices = new ArrayList<Vertex>();
        boolean keepgoing = true;
        while (keepgoing) {
            newVertices.clear();
            double r = minimalDist();
            for (Vertex vert : vertices) {
                if (vert.type == 1) subdivide(vert, newVertices, r);
            }
            if (newVertices.isEmpty()) keepgoing = false;
            else {
                for (Vertex vert : newVertices) vertices.add(vert);
            }
        }
    }
    
    private void subdivide(Vertex vert, ArrayList<Vertex> newVertices, double r) {
        int j = vert.comb.size()-1;
        while (j >= 0) {
            Edge edge = vert.comb.get(j);
            double d = distance(edge);
            if (d > subdivFactor * r) {
                Vertex fvert = edge.fvert;
                Vertex svert = edge.svert;
                Vertex mvert = new Vertex(0.333333 * fvert.x + 0.666667 * svert.x, 
                        0.333333 * fvert.y + 0.666667 * svert.y, 1, vert.label, false);
                Vertex nvert = new Vertex(0.666667 * fvert.x + 0.333333 * svert.x, 
                        0.666667 * fvert.y + 0.333333 * svert.y, 4, vert.label, false);
                newVertices.add(nvert);
                newVertices.add(mvert);
                Edge nedge = new Edge(fvert, nvert);
                Edge medge = new Edge(mvert, nvert);
                Edge redge = new Edge(mvert, svert);
                nvert.comb.add(nedge);
                nvert.comb.add(medge);
                mvert.comb.add(medge);
                mvert.comb.add(redge);
                svert.comb.add(redge);
                svert.comb.remove(edge);
                vert.comb.remove(edge);
                fvert.comb.add(j, nedge);
            }
            j--;
        }
    }
    
    private double minimalDist() {
        double min = 100;
        for (Vertex vert : vertices) {
            double mn = minDist(vert);
            if (min > mn) min = mn;
        }
        return min;
    }
    
    private double minDist(Vertex vert) {
        double min = 100;
        for (Edge edge : vert.comb) {
            double mn = distance(edge);
            if (min > mn) min = mn;
        }
        return min;
    }
    
    private double distance(Edge edge) {
        double x = edge.fvert.x - edge.svert.x;
        double y = edge.fvert.y - edge.svert.y;
        return Math.sqrt(x*x + y*y);
    }
    
    private void addVertices() {
        int i = vertices.size()-1;
        while (i >= 0) {
            Vertex vert = vertices.get(i);
            if (vert.type == 1) {
                Vertex[] nVert = getVertex(vert.comb.get(0).fvert, vert.comb.get(1).fvert, vert);
                vertices.add(nVert[0]);
                vertices.add(nVert[1]);
            }
            i--;
        }
    }
    
    private Vertex[] getVertex(Vertex fvert, Vertex svert, Vertex cvert) {
        double tau0 = fvert.r/(fvert.r+cvert.r);
        double tau1 = svert.r/(svert.r+cvert.r);
        double x0 = (1-tau0) * fvert.x + tau0 * cvert.x;
        double y0 = (1-tau0) * fvert.y + tau0 * cvert.y;
        double x1 = (1-tau1) * svert.x + tau1 * cvert.x;
        double y1 = (1-tau1) * svert.y + tau1 * cvert.y;
        Vertex[] verts = new Vertex[2];
        verts[0] = new Vertex(x0, y0, 4, cvert.label, cvert.fixed);
        verts[1] = new Vertex(x1, y1, 4, cvert.label, cvert.fixed);
        Edge fEdgeZer = new Edge(fvert, verts[0]);
        Edge fEdgeOne = new Edge(cvert, verts[0]); 
        Edge sEdgeZer = new Edge(svert, verts[1]);
        Edge sEdgeOne = new Edge(cvert, verts[1]);
        verts[0].comb.add(fEdgeZer);
        verts[0].comb.add(fEdgeOne);
        verts[1].comb.add(sEdgeZer);
        verts[1].comb.add(sEdgeOne);
        cvert.comb.clear();
        cvert.comb.add(fEdgeOne);
        cvert.comb.add(sEdgeOne);
        fvert.comb.add(fEdgeZer);
        svert.comb.add(sEdgeZer);
        return verts;
    }
    
}
