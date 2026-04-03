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
import java.util.Arrays;

/**
 *
 * @author Dirk
 */
public class Facet {
    
    private final ArrayList<Edge> doBoundaries;
    private final ArrayList<Edge> coBoundaries;
    private int dots;
    private int euler;
    
    public Facet(int d, int e) {
        doBoundaries = new ArrayList<Edge>();
        coBoundaries = new ArrayList<Edge>();
        dots = d;
        euler = e;
    }

    public Facet(Edge edge, int d) { // this is a product facet over the edge, possibly with dots
        doBoundaries = new ArrayList<Edge>();
        coBoundaries = new ArrayList<Edge>();
        doBoundaries.add(edge);
        coBoundaries.add(edge);
        dots = d;
        euler = edge.getEuler();
    }
    
    public Facet(Edge dEdge, Edge cEdge, int d) {// again product, assumed edges are equiv
        doBoundaries = new ArrayList<Edge>();
        coBoundaries = new ArrayList<Edge>();
        doBoundaries.add(dEdge);
        coBoundaries.add(cEdge);
        dots = d;
        euler = dEdge.getEuler();
    }
    
    public Facet(Facet fac) {
        doBoundaries = new ArrayList<Edge>();
        coBoundaries = new ArrayList<Edge>();
        dots = fac.dots;
        euler = fac.euler;
        for (Edge ed : fac.coBoundaries) coBoundaries.add(ed);
        for (Edge ed : fac.doBoundaries) doBoundaries.add(ed);
    }
    
    public void addEdges(Edge[] fbEdges, Edge[] ftEdges) {
        doBoundaries.addAll(Arrays.asList(fbEdges));
        coBoundaries.addAll(Arrays.asList(ftEdges));
    }
    
    public void addEdges(ArrayList<Edge> fbEdges, ArrayList<Edge> ftEdges) {
        for (Edge ed : fbEdges) doBoundaries.add(ed);
        for (Edge ed : ftEdges) coBoundaries.add(ed);
    }
    
    public int getDots() {
        return dots;
    }
    
    public int getEuler() {
        return euler;
    }

    public void decreaseDots(int dec) { // make sure beforehand dots stays >= 0 
        dots = dots - dec;
    }
    
    public ArrayList<Integer> endPoints() {
        ArrayList<Integer> points = new ArrayList<Integer>();
        for (Edge ed : doBoundaries) {
            int st = ed.getStVertexName();
            int en = ed.getEnVertexName();
            if (st > 0) points.add(st);
            if (en > 0) points.add(en);
        }
        return points;
    }
    
    public void increaseDots(int inc) {
        dots = dots + inc;
    }
    
    public void increaseEuler(int inc) {
        euler = euler + inc;
    }
    
    public int overlappingEdge() {
        for (int i = 0; i < doBoundaries.size(); i++) {
            if (coBoundaries.contains(doBoundaries.get(i))) {
                Edge ed = doBoundaries.get(i);
                if (ed.getStVertexName() > 0 && ed.getEnVertexName() > 0) return i;
            }
        }
        return -1;
    }
    
    public int overlap() {
        int ov = 0;
        for (Edge ed : doBoundaries) if (coBoundaries.contains(ed)) ov++;
        return ov;
    }
    
    public boolean isProduct() {
        if (dots != 0) return false;
        if (coBoundaries.size() != 1) return false;
        if (doBoundaries.size() != 1) return false;
        return euler == doBoundaries.get(0).getEuler();
    }
    
    public void reduceDots(int red) {
        while (dots >= 3) dots = dots - red;
    }
    
    public boolean codomainEdgesContain(Edge ed) {
        return coBoundaries.contains(ed);
    }
    
    public boolean domainEdgesContain(Edge ed) {
        return doBoundaries.contains(ed);
    }
    
    public boolean codomainEmpty() {
        return coBoundaries.isEmpty();
    }
    
    public boolean domainEmpty() {
        return doBoundaries.isEmpty();
    }
    
    public ArrayList<Edge> getDomainEdges() {
        return doBoundaries;
    }

    public ArrayList<Edge> getCodomainEdges() {
        return coBoundaries;
    }
    
    public Facet reverse() {
        Facet rev = new Facet(dots, euler);
        for (Edge ed : doBoundaries) rev.coBoundaries.add(ed);
        for (Edge ed : coBoundaries) rev.doBoundaries.add(ed);
        return rev;
    }
    
    public int indexIn(ArrayList<Facet> facList) {
        for (int i = 0; i < facList.size(); i++) {
            if (this.sameAs(facList.get(i))) return i;
        }
        return -1;
    }
    
    private boolean sameAs(Facet fac) {
        if (euler != fac.euler) return false;
        if (dots != fac.dots) return false;
        if (coBoundaries.size() != fac.coBoundaries.size()) return false;
        if (doBoundaries.size() != fac.doBoundaries.size()) return false;
        for (Edge ed : coBoundaries) if (!ed.isContained(fac.coBoundaries)) return false;
        for (Edge ed : doBoundaries) if (!ed.isContained(fac.doBoundaries)) return false;
        return true;
    }
    
    @Override
    public String toString() {
        return dots+" "+euler+" "+doBoundaries+" -- "+coBoundaries;
    }

    public boolean isClosed() { // doesn't check whether singular
        return domainEmpty() && codomainEmpty();
    }

    void reduceDotsBy(int d) {
        dots = dots - d; // check first that dots does not get negative
    }
    
}
