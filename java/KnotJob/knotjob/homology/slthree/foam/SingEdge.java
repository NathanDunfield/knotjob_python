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
public class SingEdge extends Edge {
    
    private final int stLev;
    private final int enLev;
    
    public SingEdge(Vertex st, Vertex en, int sl, int el) {
        super(st, en);
        stLev = sl;
        enLev = el;
    }
    
    public int getStLevel() {
        return stLev;
    }
    
    public int getEnLevel() {
        return enLev;
    }
    
    public SingEdge reverse() {
        if (stLev == enLev) return new SingEdge(enVert, stVert, 1-stLev, 1-enLev);
        return new SingEdge(enVert, stVert, stLev, enLev);
    }
    
    public boolean hasLevel(Vertex v, int l) {
        if (stVert.getName() == v.getName() && stLev == l) return true;
        return enVert.getName() == v.getName() && enLev == l;
    }
    
    public int levelOf(Vertex v) {
        if (stVert.getName() == v.getName()) return stLev;
        if (enVert.getName() == v.getName()) return enLev;
        return -1;
    }
    
    public boolean isBottomOnly() {
        return (stLev == 0 && enLev == 0);
    }
    
    public boolean isTopOnly() {
        return (stLev == 1 && enLev == 1);
    }
    
    public int indexIn(ArrayList<SingEdge> edges) {
        for (int i = 0; i < edges.size(); i++) {
            if (this.sameAs(edges.get(i))) return i;
        }
        return -1;
    }
    
    private boolean sameAs(SingEdge ed) {
        if (ed.stVert.getName() != stVert.getName()) return false;
        if (ed.enVert.getName() != enVert.getName()) return false;
        if (ed.stLev != stLev) return false;
        return ed.enLev == enLev;
    }
    
    public boolean isVertical() {
        return (stLev != enLev);
    }
    
    @Override
    public boolean isCircle() {
        return ((stVert.getName() == enVert.getName()) && (stLev == enLev));
    }
    
    @Override
    public int getEuler() {
        if (isCircle()) return 0;
        return 1;
    }
    
    @Override
    public String toString() {
        String name = stVert.toString()+" "+stLev+" - "+enVert.toString()+" "+enLev;
        if (!mdVert.isEmpty()) {
            name = name+" via ";
            for (Vertex v : mdVert) name = name+v.toString();
        }
        return name;
    }

    @Override
    public boolean isSingular() {
        return true;
    }
    
}
