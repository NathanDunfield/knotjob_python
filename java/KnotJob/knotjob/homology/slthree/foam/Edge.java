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

package knotjob.homology.slthree.foam;

import java.util.ArrayList;

/**
 *
 * @author Dirk
 */
public class Edge {
    
    protected final Vertex stVert;
    protected final Vertex enVert;
    protected final ArrayList<Vertex> mdVert;
    
    public Edge(Vertex st, Vertex en) {
        stVert = st;
        enVert = en;
        mdVert = new ArrayList<Vertex>();
    }
    
    public Edge(Vertex st, Vertex en, Vertex mdOne, Vertex mdTwo) {
        stVert = st;
        enVert = en;
        mdVert = new ArrayList<Vertex>();
        mdVert.add(mdOne);
        mdVert.add(mdTwo);
    }

    public int getStVertexName() {
        return stVert.getName();
    }

    public int getEnVertexName() {
        return enVert.getName();
    }
    
    public Vertex getStVertex() {
        return stVert;
    }
    
    public Vertex getEnVertex() {
        return enVert;
    }
    
    public ArrayList<Integer> getVertexNames() {
        ArrayList<Integer> names = new ArrayList<Integer>();
        names.add(stVert.getName());
        for (Vertex m : mdVert) names.add(m.getName());
        names.add(enVert.getName());
        return names;
    }

    public void addMiddle(ArrayList<Vertex> verts) {
        for (Vertex v : verts) mdVert.add(v);
    }
    
    public void addMiddle(Vertex v) {
        mdVert.add(v);
    }
    
    public boolean isCircle() {
        return stVert.getName() == enVert.getName();
    }
    
    public boolean isSame(Edge ed) {
        if (ed.stVert.getName() != stVert.getName()) return false;
        if (ed.enVert.getName() != enVert.getName()) return false;
        if (ed.mdVert.size() != mdVert.size()) return false;
        for (int i = 0; i < mdVert.size(); i++) 
            if (mdVert.get(i).getName() != ed.mdVert.get(i).getName()) return false;
        return true;
    }
    
    @Override
    public String toString() {
        String name = stVert+" - "+enVert;
        if (!mdVert.isEmpty()) {
            name = name+" via ";
            for (Vertex v : mdVert) name = name+v.toString();
        }
        return name;
    }

    public boolean isSingular() {
        return false;
    }

    public int getEuler() {
        if (isCircle()) return 0;
        return 1;
    }
    
    public boolean isContained(ArrayList<Edge> edges) {
        for (Edge ed : edges) if (this.isSame(ed)) return true;
        return false;
    }
    
}
