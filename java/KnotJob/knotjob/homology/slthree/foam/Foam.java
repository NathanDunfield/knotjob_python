/*

Copyright (C) 2024-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class Foam<R extends Ring<R>> {
    
    private R value;
    private final Web domain;
    private final Web cdmain;
    private final ArrayList<SingEdge> singEdges;
    private final ArrayList<Facet[]> singFacets;
    private final ArrayList<Facet> facets;
    
    public Foam(Web dweb, Web cweb, R val) {
        value = val;
        domain = dweb;
        cdmain = cweb;
        singEdges = new ArrayList<SingEdge>();
        singFacets = new ArrayList<Facet[]>();
        facets = new ArrayList<Facet>();
    }
    
    public Foam(Web dweb, Web cweb) {
        value = null;
        domain = dweb;
        cdmain = cweb;
        singEdges = new ArrayList<SingEdge>();
        singFacets = new ArrayList<Facet[]>();
        facets = new ArrayList<Facet>();
    }
    
    public Foam(Foam fm, R val) { // warning, does not clone facets
        value = val;
        domain = fm.domain;
        cdmain = fm.cdmain;
        singEdges = new ArrayList<SingEdge>();
        singFacets = new ArrayList<Facet[]>();
        facets = new ArrayList<Facet>();
        for (int i = 0; i < fm.singEdges.size(); i++) singEdges.add((SingEdge) fm.singEdges.get(i));
        for (int i = 0; i < fm.singFacets.size(); i++) singFacets.add((Facet[]) fm.singFacets.get(i));
        for (int i = 0; i < fm.facets.size(); i++) facets.add((Facet) fm.facets.get(i));
    }
    
    public Foam(R val, Foam fm) { // supposed to clone the facets
        value = val;
        domain = fm.domain;
        cdmain = fm.cdmain;
        singEdges = new ArrayList<SingEdge>();
        singFacets = new ArrayList<Facet[]>();
        facets = new ArrayList<Facet>();
        for (int i = 0; i < fm.facets.size(); i++) facets.add(new Facet((Facet) fm.facets.get(i)));
        for (int j = 0; j < fm.singEdges.size(); j++) {
            singEdges.add((SingEdge) fm.singEdges.get(j));
            int[] pos = fm.getSingPositions((SingEdge) fm.singEdges.get(j));
            this.addSingFacets(pos);
        }
    }

    public void output() {
        System.out.println("Value : "+value);
        System.out.println("Facets:");
        for (Facet fac : facets) System.out.println(fac);
        System.out.println("Singular Edges:");
        for (int i = 0; i < singEdges.size(); i++) {
            System.out.print(singEdges.get(i)+" with ");
            for (int j = 0; j < 3; j++) System.out.print(facets.indexOf(singFacets.get(i)[j])+" ");
            System.out.println();
        }
    }
    
    public void genusCheck(R mthree) {
        for (Facet fac : facets) {
            int factor = fac.genusCheck(numberOfSingCircles(fac));
            if (factor == 0) {
                this.value = value.getZero();
                return;
            }
            while (factor != 1) {
                factor = factor / (-3);
                this.value = this.value.multiply(mthree);
            }
        }
    }
    
    private int numberOfSingCircles(Facet fac) {
        int count = 0;
        for (int i = 0; i < singEdges.size(); i++) {
            Facet[] facs = singFacets.get(i);
            SingEdge edge = singEdges.get(i);
            if (!edge.isCircle()) continue;
            if (facs[0] == fac) count++;
            if (facs[1] == fac) count++;
            if (facs[2] == fac) count++;
        }
        return count;
    }
    
    public boolean hasStrangeFacets() {
        for (SingEdge edge : singEdges) {
            int[] abc = this.getSingPositions(edge);
            if (abc[0] == abc[1] || abc[0] == abc[2] || abc[1] == abc[2]) return true;
        }
        return false;
    }
    
    public boolean checkSingularEdges() {
        for (int i = 0; i < singEdges.size(); i++) {
            SingEdge edge = singEdges.get(i);
            Facet[] facs = singFacets.get(i);
            if (!edge.isCircle()) {
                for (int k = 0; k < 3; k++) {
                    if (edge.getStLevel() == 0) {
                        boolean exists = false;
                        for (Edge ed : facs[k].getDomainEdges()) 
                            if (ed.getVertexNames().contains(edge.getStVertexName())) exists = true;
                        if (!exists) return false;
                    }
                    else {
                        boolean exists = false;
                        for (Edge ed : facs[k].getCodomainEdges()) 
                            if (ed.getVertexNames().contains(edge.getStVertexName())) exists = true;
                        if (!exists) return false;
                    }
                }
            }
        }
        return true;
    }// */
    
    public Foam<R> reverse() {
        Foam<R> rev = new Foam<R>(cdmain, domain, value);
        for (Facet fac : facets) rev.facets.add(fac.reverse());
        for (SingEdge se : singEdges) {
            rev.singEdges.add(se.reverse());
            rev.addSingFacets(getSingPositions(se));
        }
        return rev;
    }
    
    public void addSingEdge(SingEdge sEdge) {
        singEdges.add(sEdge);
    }
    
    public void addSingFacets(Facet ff, Facet sf, Facet tf) {
        singFacets.add(new Facet[] {ff, sf, tf});
    }
    
    public final void addSingFacets(int[] pos) {
        singFacets.add(new Facet[] {facets.get(pos[0]), facets.get(pos[1]), facets.get(pos[2])});
    }
    
    public void addFacets(Facet fc) {
        facets.add(fc);
    }
    
    public void addFacets(ArrayList<Facet> facs) {
        for (Facet fac : facs) facets.add(fac);
    }
    
    public void addValue(R add) {
        value = value.add(add);
    }
    
    public int degree() {
        int degree = domain.getEuler()+cdmain.getEuler();
        for (Facet fac : facets) {
            degree = degree + 2 * fac.getDots() - 2 * fac.getEuler();
        }
        for (SingEdge edge : singEdges) degree = degree + 4 * edge.getEuler();
        return degree;
    }
    
    public R getValue() {
        return value;
    }

    public void setValue(R val) {
        value = val;
    }
    
    public ArrayList<Facet> getFacets() {
        return facets;
    }
    
    public ArrayList<SingEdge> getSingEdges() {
        return singEdges;
    }

    public int[] getSingPositions(SingEdge aThis) {
        int p = singEdges.indexOf(aThis);
        if (p < 0) return null;
        Facet[] fcs = singFacets.get(p);
        return new int[] {facets.indexOf(fcs[0]), facets.indexOf(fcs[1]), facets.indexOf(fcs[2])};
    }
    
    public boolean hasClosedSurfaces() {
        for (Facet fac : facets) {
            if (fac.isClosed() && notSingular(fac)) return true;
        }
        return false;
    }
    
    public boolean threeDotsOnFacet() {
        for (Facet fac : facets) if (fac.getDots() >= 3) return true;
        return false;
    }
    
    public void reduceDots(int red) {
        for (Facet fac : facets) fac.reduceDots(red);
    }
    
    // checking whether the foam is a product foam without dots ///////////
    
    public boolean isProduct() {
        for (SingEdge edge : singEdges) if (!edge.isVertical()) return false;
        for (Facet fac : facets) if (!fac.isProduct()) return false;
        return true;
    }
    
    // checking whether two foams have the same facets //////////////////////
    
    public boolean sameAs(Foam<R> aFoam) {
        if (facets.size() != aFoam.facets.size()) return false;
        if (singEdges.size() != aFoam.singEdges.size()) return false;
        int[] facetMatch = matchFacets(aFoam);
        if (facetMatch == null) return false; // otherwise the facets match
        int[] edgeMatch = matchEdges(aFoam);
        if (edgeMatch == null) return false; // otherwise the singular edges match
        for (int k = 0; k < singEdges.size(); k++) {
            int[] pos = this.getSingPositions(singEdges.get(k));
            int[] aPos = aFoam.getSingPositions(aFoam.singEdges.get(edgeMatch[k]));
            if (!positionsMatch(pos, aPos, facetMatch)) return false;
        }
        return true;
    }
    
    private boolean positionsMatch(int[] pos, int[] aPos, int[] matches) {
        if (matches[pos[0]] == aPos[0] && matches[pos[1]] == aPos[1] &&
                matches[pos[2]] == aPos[2]) return true;
        if (matches[pos[0]] == aPos[1] && matches[pos[1]] == aPos[2] &&
                matches[pos[2]] == aPos[0]) return true;
        return matches[pos[0]] == aPos[2] && matches[pos[1]] == aPos[0] &&
                matches[pos[2]] == aPos[1];
    }
    
    private int[] matchEdges(Foam<R> aFoam) {
        int[] match = new int[singEdges.size()];
        for (int i = 0; i < singEdges.size(); i++) {
            SingEdge sEd = singEdges.get(i);
            match[i] = sEd.indexIn(aFoam.singEdges);
            if (match[i] == -1) return null;
        }
        return match;
    }
    
    private int[] matchFacets(Foam<R> aFoam) {
        int[] match = new int[facets.size()];
        for (int i = 0; i < facets.size(); i++) {
            Facet fac = facets.get(i);
            match[i] = fac.indexIn(aFoam.facets);
            if (match[i] == -1) return null;
        }
        return match;
    }
    
    // start of composition //////////////////////////////
    
    public Foam<R> composeWith(Foam<R> aFoam) { // aFoam will be put on top of this
        Foam<R> nFoam = new Foam<R>(domain, aFoam.cdmain, value.multiply(aFoam.value));
        EFCombo combo = combineFacets(aFoam.facets);
        nFoam.addFacets(combo.newFacets());
        combo.combineSingEdges(aFoam);
        combo.glueSingEdges(aFoam, nFoam);
        return nFoam;
    }
    
    private EFCombo combineFacets(ArrayList<Facet> aFacets) {
        EFCombo efCombo = new EFCombo();
        for (Edge ed : cdmain.getEdges()) {
            Facet[] tfacs = facetsWith(ed, aFacets);
            efCombo.combineFacets(tfacs, ed);
        }
        efCombo.addMissingFacets(aFacets);
        return efCombo;
    }
    
    private Facet[] facetsWith(Edge ed, ArrayList<Facet> aFacets) {
        Facet[] fw = new Facet[2];
        for (Facet fac : facets) {
            if (fac.codomainEdgesContain(ed)) {
                fw[0] = fac;
                break;
            }
        }
        for (Facet fac : aFacets) {
            if (fac.domainEdgesContain(ed)) {
                fw[1] = fac;
                return fw;
            }
        }
        return null;
    }
    
    // used for gluing another foam to this /////////////////////////////
    
    public void addHorizontalFacets(Foam<R> oFoam, Web oWeb, SlTCache cache) {
        ArrayList<GlueFacet> newFacets = new ArrayList<GlueFacet>();
        ArrayList<Facet> prodFacets = new ArrayList<Facet>();
        for (Facet fac : oFoam.getFacets()) newFacets.add(new GlueFacet(fac));
        for (Edge ed : oWeb.getEdges()) {
            Facet pFac = new Facet(ed, 0);
            prodFacets.add(pFac);
            newFacets.add(new GlueFacet(pFac));
        }
        combineGlueFacets(newFacets);
        addFacetsFrom(newFacets);
        addSingEdges(oFoam, cache, newFacets);
        addSingEdges(oWeb, prodFacets, cache, newFacets);
    }

    private void addSingEdges(Web oWeb, ArrayList<Facet> pFacets, SlTCache cache, 
            ArrayList<GlueFacet> nFacets) {
        for (int i = 0; i < oWeb.singNumber(); i++) {
            Vertex v = oWeb.getSingVertex(i);
            int sl = 0;
            int el = 1;
            if (v.getType() == 3) {
                sl = 1;
                el = 0;
            }
            SingEdge nEdge = cache.getSingularEdge(new SingEdge(v, v, sl, el));
            this.addSingEdge(nEdge);
            int[] pos = oWeb.getSingPositions(v);
            Facet[] sFac = new Facet[] {pFacets.get(pos[0]), pFacets.get(pos[1]), 
                pFacets.get(pos[2])};
            int[] nPos = newPositions(sFac, nFacets);
            this.addSingFacets(facets.get(nPos[0]), facets.get(nPos[1]), facets.get(nPos[2]));
        }
    }
    
    private void addSingEdges(Foam<R> oFoam, SlTCache cache, ArrayList<GlueFacet> newFacets) {
        for (int i = 0; i < oFoam.getSingEdges().size(); i++) {
            SingEdge edge = (SingEdge) oFoam.getSingEdges().get(i);
            SingEdge nEdge = cache.getSingularEdge(edge);
            this.addSingEdge(nEdge);
            Facet[] pos = oFoam.singFacets.get(i);//..getSingPositions(edge);
            int[] nPos = newPositions(pos, newFacets);
            this.addSingFacets(facets.get(nPos[0]), facets.get(nPos[1]), facets.get(nPos[2]));
        }
    }
    
    private int[] newPositions(Facet[] pos, ArrayList<GlueFacet> newFacets) {
        int[] nPos = new int[3];
        for (int i = 0; i < 3; i++) {
            nPos[i] = newFacets.indexOf(getGlueFacetWith(newFacets, pos[i]));
        }
        return nPos;
    }
    
    private GlueFacet getGlueFacetWith(ArrayList<GlueFacet> nFacets, Facet fac) {
        for (GlueFacet gFac : nFacets) {
            if (gFac.contains(fac)) return gFac;
        }
        System.out.println("Not good");
        return null;
    }
    
    private void addFacetsFrom(ArrayList<GlueFacet> newFacets) {
        for (GlueFacet gFacet : newFacets) {
            Facet facet = gFacet.getFacet(domain, cdmain);
            facets.add(facet);
        }
    }
    
    private void combineGlueFacets(ArrayList<GlueFacet> gFacets) {
        int i = gFacets.size()-1;
        while (i >= 0) {
            int j = gFacets.size()-1;
            GlueFacet giFacet = gFacets.get(i);
            while (j >= 0) {
                if (i != j) {
                    GlueFacet gjFacet = gFacets.get(j);
                    if (giFacet.combinesWith(gjFacet)) {
                        GlueFacet cFacet = new GlueFacet(giFacet, gjFacet);
                        gFacets.remove(giFacet);
                        gFacets.add(i, cFacet);
                        gFacets.remove(gjFacet);
                        if (j > i) i++;
                        break;
                    }
                }
                j--;
            }
            i--;
        }
    }
    
    // end of horizontal gluing of foams ///////////////////////
    
    public int facetSize() {
        return facets.size();
    }
    
    public Web getCoWeb() {
        return cdmain;
    }
    
    public Web getDoWeb() {
        return domain;
    }

    public boolean dotsOnThreeFacets() {
        for (Facet[] triple : this.singFacets) {
            if (triple[0] != triple[1] && triple[0] != triple[2] && 
                    triple[1] != triple[2]) {
                if (triple[0].getDots() >= 1 && triple[1].getDots() >= 1 &&
                        triple[2].getDots() >= 1) return true;
            }
        }
        return false;
    }

    public void reduceOneDotEach() {
        for (Facet[] triple : this.singFacets) {
            if (triple[0].getDots() >= 1 && triple[1].getDots() >= 1 &&
                    triple[2].getDots() >= 1) {
                triple[0].decreaseDots(1);
                triple[1].decreaseDots(1);
                triple[2].decreaseDots(1);
            }
        }
    }

    public boolean twoDotsOnTwo() {
        for (Facet[] triple : this.singFacets) {
            if (triple[0].getDots() >= 2 && triple[1].getDots() >= 2 && 
                    triple[0] != triple[1]) return true;
            if (triple[0].getDots() >= 2 && triple[2].getDots() >= 2 &&
                    triple[0] != triple[2]) return true;
            if (triple[1].getDots() >= 2 && triple[2].getDots() >= 2 &&
                    triple[1] != triple[2]) return true;
        }
        return false;
    }
    
    public int[] checkCylinder() {
        for (int j = 0; j < facets.size(); j++) {
            Facet fac = facets.get(j);
            if (fac.getDomainEdges().size() <= 1 || fac.getCodomainEdges().size() <= 1) continue;
            if (fac.getEuler()>0) continue;
            ArrayList<Integer> sEdges = new ArrayList<Integer>();
            for (int i = 0; i < singEdges.size(); i++) {
                Facet[] triple = singFacets.get(i);
                if (triple[0] == fac || triple[1] == fac || triple[2] == fac) sEdges.add(i);
            }
            boolean good = true;
            int k = sEdges.size()-1;
            while (good && k >= 0) {
                if (!singEdges.get(sEdges.get(k)).isVertical()) good = false;
                k--;
            }
            if (good) {
                int ov = fac.overlappingEdge();
                if (ov >= 0) return new int[] {j, ov};
            }
        }
        return null;
    }
    
    public ArrayList<Foam<R>> simplifyFoam(int sType, R unit) {
        if (value.isZero()) return null;
        removeSurfaces(sType, unit);
        if (value.isZero()) return null;
        // now check for singular circles
        ArrayList<Foam<R>> newFoams = checkForSingularCircles(unit);
        if (!newFoams.isEmpty()) {
            removeSurfaces(newFoams, sType, unit);
            if (newFoams.isEmpty()) return null;
        }
        return newFoams;
    }
    
    /*public void surfaceChecker(R unit, int sType) {
        for (int g = 0; g < 10; g++) {
            switch (sType) {
                case 3: System.out.println(cSurfaceValue(0, g, unit)+" "+cSurfaceValue(1, g, unit)+" "+cSurfaceValue(2, g, unit));
                case 2: System.out.println(bSurfaceValue(0, g, unit)+" "+bSurfaceValue(2, g, unit));
                case 1: System.out.println(aSurfaceValue(0, g, unit)+" "+aSurfaceValue(1, g, unit)+" "+aSurfaceValue(2, g, unit));
            }
        }
    }// */
    
    // this looks for singular circles within the foam ///////////////
    
    private ArrayList<Foam<R>> checkForSingularCircles(R unit) {
        ArrayList<Foam<R>> newFoams = new ArrayList<Foam<R>>();
        int i = indexOfFirstSingCirc();
        if (i == -1) return newFoams;
        newFoams.add(foamSingCircRemoved(i, new int[] {0, 1, 2}, unit));
        newFoams.add(foamSingCircRemoved(i, new int[] {1, 2, 0}, unit));
        newFoams.add(foamSingCircRemoved(i, new int[] {2, 0, 1}, unit));
        newFoams.add(foamSingCircRemoved(i, new int[] {0, 2, 1}, unit.negate()));
        newFoams.add(foamSingCircRemoved(i, new int[] {1, 0, 2}, unit.negate()));
        newFoams.add(foamSingCircRemoved(i, new int[] {2, 1, 0}, unit.negate()));
        return newFoams;
    }
    
    private Foam<R> foamSingCircRemoved(int i, int[] dots, R factor) {
        Foam<R> nFoam = new Foam<R>(domain, cdmain, value.multiply(factor));
        for (Facet fac : facets) nFoam.addFacets(new Facet(fac));
        for (int j = 0; j < singEdges.size(); j++) {
            if (j != i) {
                nFoam.addSingEdge(singEdges.get(j));
                int[] pos = getSingPositions(singEdges.get(j));
                nFoam.addSingFacets(pos);
            }
        }
        Facet[] triple = singFacets.get(i);
        for (int k = 0; k < 3; k++) {
            i = facets.indexOf(triple[k]);
            nFoam.facets.get(i).increaseDots(dots[k]);
            nFoam.facets.get(i).increaseEuler(1);
        }
        return nFoam;
    }
    
    private int indexOfFirstSingCirc() {
        for (int i = 0; i < singEdges.size(); i++) 
            if (singEdges.get(i).isCircle()) return i;
        return -1;
    }
    
    // this looks for closed surfaces among facets ///////////////
    
    private void removeSurfaces(int sType, R unit) {
        ArrayList<Facet> surfaces = getSurfaces(this.facets);
        if (!surfaces.isEmpty()) {
            R factor = substituteFoams(surfaces, sType, unit);
            this.value = this.value.multiply(factor);
            this.removeFacets(surfaces);
        }
    }
    
    private void removeSurfaces(ArrayList<Foam<R>> foams, int sType, R unit) {
        int i = foams.size()-1;
        while (i >= 0) {
            Foam<R> foam = foams.get(i);
            foam.removeSurfaces(sType, unit);
            if (foam.value.isZero()) foams.remove(i);
            i--;
        }
    }
    
    private void removeFacets(ArrayList<Facet> facs) {
        int i = facets.size()-1;
        while (i >= 0) {
            if (facs.contains(facets.get(i))) facets.remove(i);
            i--;
        }
    }
    
    private R substituteFoams(ArrayList<Facet> surfaces,
            int sType, R unit) {
        R factor = unit;
        for (Facet surf : surfaces) {
            int dots = surf.getDots();
            int genus = 1-surf.getEuler()/2;
            factor = factor.multiply(surfaceValue(dots, genus, sType, unit));
            if (factor.isZero()) return factor;
        }
        return factor;
    }
    
    private R surfaceValue(int d, int g, int t, R unit) {
        switch (t) {
            case 0 -> {return standardSurfaceValue(d, g, unit);}
            case 1 -> {return aSurfaceValue(d, g, unit);}
            case 2 -> {return bSurfaceValue(d, g, unit);}
            case 3 -> {return cSurfaceValue(d, g, unit);}
        }
        return unit;
    }
    
    private R cSurfaceValue(int d, int g, R unit) {
        while (d >= 3) d = d - 3;
        if (g == 0 && d == 2) return unit.negate();
        if (g >= 1) {
            if ((g+2*d)%3 != 1) return unit.getZero();
            R base = unit.add(unit).add(unit).negate(); // base = -3
            R factor = unit.multiply(base);
            while (g > 1) {
                g--;
                factor = factor.multiply(base);
            }
            return factor.negate();
        }
        return unit.getZero();
    }
    
    private R bSurfaceValue(int d, int g, R unit) {
        while (d >= 3) d = d - 2;
        if (g == 0 && d == 2) return unit.negate();
        if (g >= 1) {
            if (d == 1) return unit.getZero();
            R tw = unit.add(unit);
            R ftw = tw;
            while (g > 1) {
                ftw = ftw.multiply(tw).negate();
                g--;
            }
            if (d == 0) return ftw.add(unit); // return (-1)^{g-1} 2^g + 1
            if (d == 2) return ftw; // return (-1)^{g-1} 2^g
        }
        return unit.getZero();
    }
    
    private R aSurfaceValue(int d, int g, R unit) {
        while (d >= 3) d--;
        if (g == 0 && d == 2) return unit.negate();
        if (g == 1) {
            if (d == 0) return unit.add(unit).add(unit); // return 3
            return unit;
        }
        if (g >= 2) {
            if (g % 2 == 0) return unit.negate();
            return unit;
        }
        return unit.getZero();
    }
    
    private R standardSurfaceValue(int d, int g, R unit) {
        if (g == 0 && d == 2) return unit.negate();
        if (g == 1 && d == 0) return unit.add(unit).add(unit);
        return unit.getZero();
    }
    
    private ArrayList<Facet> getSurfaces(ArrayList<Facet> theFacets) {
        ArrayList<Facet> surfaces = new ArrayList<Facet>();
        for (Facet fac : theFacets) if (fac.isClosed() && notSingular(fac)) surfaces.add(fac);
        return surfaces;
    }
    
    private boolean notSingular(Facet fac) {
        for (Facet[] triple : singFacets) {
            if (triple[0] == fac || triple[1] == fac || triple[2] == fac) return false;
        }
        return true;
    }
    
    // For GenSlTComplex
    
    public void reduceFirstDotFacet(int d) {
        for (Facet fac : facets) {
            if (fac.getDots() >= 3) {
                fac.reduceDotsBy(d);
                return;
            }
        }
    }
    
    public void reduceOneDotEach(R t) {
        for (Facet[] triple : this.singFacets) {
            if (triple[0].getDots() >= 1 && triple[1].getDots() >= 1 &&
                    triple[2].getDots() >= 1) {
                triple[0].decreaseDots(1);
                triple[1].decreaseDots(1);
                triple[2].decreaseDots(1);
                value = value.multiply(t);
            }
        }
    }
    
    public ArrayList<Foam<R>> simplifyFoam(int sType, R unit, R t) {
        if (value.isZero()) return null;
        //removeSurfaces(sType, unit);
        removeSurfaces(unit, t, sType);
        if (value.isZero()) return null;
        // now check for singular circles
        ArrayList<Foam<R>> newFoams = checkForSingularCircles(unit);
        if (!newFoams.isEmpty()) {
            //removeSurfaces(newFoams, sType, unit);
            removeSurfaces(newFoams, unit, t, sType);
            if (newFoams.isEmpty()) return null;
        }
        return newFoams;
    }
    
    private void removeSurfaces(R unit, R t, int sType) {
        ArrayList<Facet> surfaces = getSurfaces(this.facets);
        if (!surfaces.isEmpty()) {
            R factor = substituteFoams(surfaces, unit, t, sType);
            this.value = this.value.multiply(factor);
            this.removeFacets(surfaces);
        }
    }
    
    private R substituteFoams(ArrayList<Facet> surfaces,
            R unit, R t, int sType) {
        R factor = unit;
        for (Facet surf : surfaces) {
            int dots = surf.getDots();
            int genus = 1-surf.getEuler()/2;
            factor = factor.multiply(surfaceValue(dots, genus, unit, t, sType));
            if (factor.isZero()) return factor;
        }
        return factor;
    }
    
    private R surfaceValue(int d, int g, R unit, R t,int sType) {
        if (g == 0) {
            if (d >= 3) return surfaceValue(d-sType, 0, unit, t, sType).add(surfaceValue(d-3, 0, 
                    unit, t, sType).multiply(t));
            if (d == 2) return unit.negate();
            return unit.getZero();
        }
        if (sType == 1) return surfaceValue(d+1, g-1, unit, t, sType).multiply(unit.add(unit));
        R minusTwo = unit.add(unit).negate();
        R minusThr = minusTwo.add(unit.negate());
        if (d == 0) return surfaceValue(2, g-1, unit, t, sType).multiply(minusThr).add(
                surfaceValue(2-sType, g-1, unit, t, sType));
        return surfaceValue(d, g-1, unit, t, sType).multiply(minusTwo).add(
                surfaceValue(d-1, g-1, unit, t, sType).multiply(minusThr.multiply(t)));
    }
    
    /*private R surfaceValue(int d, int g, R unit, R t) { // assumes X^3 = X + t
        if (g == 0) {
            if (d >= 3) return surfaceValue(d-2, 0, unit, t).add(surfaceValue(d-3, 0, unit, t).multiply(t));
            if (d == 2) return unit.negate();
            return unit.getZero();
        }
        R minusTwo = unit.add(unit).negate();
        R minusThr = minusTwo.add(unit.negate());
        if (d == 0) return surfaceValue(2, g-1, unit, t).multiply(minusThr).add(
                surfaceValue(0, g-1, unit, t));
        return surfaceValue(d, g-1, unit, t).multiply(minusTwo).add(
                surfaceValue(d - 1, g-1, unit, t).multiply(minusThr.multiply(t)));
    }// */
    
    private void removeSurfaces(ArrayList<Foam<R>> foams, R unit, R t, int sType) {
        int i = foams.size()-1;
        while (i >= 0) {
            Foam<R> foam = foams.get(i);
            foam.removeSurfaces(unit, t, sType);
            if (foam.value.isZero()) foams.remove(i);
            i--;
        }
    }
    
    
    
    
    // EFCombo is used for composing foams
    
    private class EFCombo {
        
        private final ArrayList<ArrayList<Facet>> cFacets;
        private final ArrayList<ArrayList<Edge>> cEdges;
        private final ArrayList<Facet> otFacets;
        private final ArrayList<Facet> obFacets;
        private final ArrayList<ArrayList<SingEdge>> sEdges;
        private final ArrayList<ArrayList<Vertex>> sVertices;
        private final ArrayList<SingEdge> otEdges;
        private final ArrayList<SingEdge> obEdges;
        
        private EFCombo() {
            cFacets = new ArrayList<ArrayList<Facet>>();
            cEdges = new ArrayList<ArrayList<Edge>>();
            otFacets = new ArrayList<Facet>();
            obFacets = new ArrayList<Facet>();
            sEdges = new ArrayList<ArrayList<SingEdge>>();
            sVertices = new ArrayList<ArrayList<Vertex>>();
            otEdges = new ArrayList<SingEdge>();
            obEdges = new ArrayList<SingEdge>();
        }
        
        private void combineFacets(Facet[] fs, Edge ed) {
            int p = combinedFacetsIndex(fs);
            if (p == -1) {
                ArrayList<Facet> nFacets = new ArrayList<Facet>();
                nFacets.add(fs[0]);
                nFacets.add(fs[1]);
                cFacets.add(nFacets);
                ArrayList<Edge> nEdgs = new ArrayList<Edge>();
                nEdgs.add(ed);
                cEdges.add(nEdgs);
            }
            else {
                ArrayList<Facet> oFacets = cFacets.get(p);
                if (!oFacets.contains(fs[0])) oFacets.add(fs[0]);
                if (!oFacets.contains(fs[1])) oFacets.add(fs[1]);
                ArrayList<Edge> oEdgs = cEdges.get(p);
                oEdgs.add(ed);
            }
            combineFurtherFacets();
        }
        
        private void combineFurtherFacets() {
            int i = cFacets.size()-1;
            while (i > 0) {
                int j = i-1;
                ArrayList<Facet> fFacets = cFacets.get(i);
                while (j >= 0) {
                    ArrayList<Facet> nFacets = cFacets.get(j);
                    if (overlapFacets(fFacets, nFacets)) {
                        for (Facet fc : fFacets) {
                            if (!nFacets.contains(fc)) nFacets.add(fc);
                        }
                        ArrayList<Edge> fEdges = cEdges.get(i);
                        ArrayList<Edge> nEdges = cEdges.get(j);
                        for (Edge e : fEdges) if (!nEdges.contains(e)) nEdges.add(e);
                        cFacets.remove(fFacets);
                        cEdges.remove(fEdges);
                        break;
                    }
                    j--;
                }
                i--;
            }
        }
        
        private boolean overlapFacets(ArrayList<Facet> fFacets, ArrayList<Facet> nFacets) {
            for (Facet fc : fFacets) if (nFacets.contains(fc)) return true;
            return false;
        }
        
        private int combinedFacetsIndex(Facet[] fs) {
            for (int i = 0; i < cFacets.size(); i++) {
                if (cFacets.get(i).contains(fs[0]) || cFacets.get(i).contains(fs[1])) return i;
            }
            return -1;
        }

        private ArrayList<Facet> newFacets() {
            ArrayList<Facet> newFacets = new ArrayList<Facet>();
            for (int i = 0; i < cFacets.size(); i++) { // this is combining all the facets in each element of cFacets
                ArrayList<Facet> oFac = cFacets.get(i);
                ArrayList<Edge> edgs = cEdges.get(i);
                int dots = 0;
                int euler = 0;
                ArrayList<Edge> tEdges = new ArrayList<Edge>();
                ArrayList<Edge> bEdges = new ArrayList<Edge>();
                for (Facet fac : oFac) {
                    dots = dots + fac.getDots();
                    euler = euler + fac.getEuler();
                    if (facets.contains(fac)) {
                        for (Edge ed : fac.getDomainEdges()) bEdges.add(ed);
                    }
                    else for (Edge ed : fac.getCodomainEdges()) tEdges.add(ed);
                }
                Facet nFacet = new Facet(dots, euler - eulerOf(edgs));
                nFacet.addEdges(bEdges, tEdges);
                newFacets.add(nFacet);
            }
            for (Facet fac : obFacets) newFacets.add(new Facet(fac)); // adds the bottom only facets before
            for (Facet fac : otFacets) newFacets.add(new Facet(fac)); // the top only facets
            return newFacets;
        }
        
        private int eulerOf(ArrayList<Edge> dEdges) {
            int eul = 0;
            for (Edge edge : dEdges) if (!edge.isCircle()) eul++;
            return eul;
        }

        private void addMissingFacets(ArrayList<Facet> aFacets) {
            for (Facet fac : aFacets) {
                if (fac.domainEmpty()) otFacets.add(fac);
            }
            for (Facet fac : facets) {
                if (fac.codomainEmpty()) obFacets.add(fac);
            }
        }

        private void combineSingEdges(Foam<R> aFoam) {
            for (int i = 0; i < cdmain.singNumber(); i++) {
                Vertex v = cdmain.getSingVertex(i);
                SingEdge[] sEdgs = getSingEdges(v, aFoam);
                int p = combinedEdgeIndex(sEdgs);
                if (p == -1) {
                    ArrayList<SingEdge> sEd = new ArrayList<SingEdge>();
                    sEd.add(sEdgs[0]);
                    sEd.add(sEdgs[1]);
                    sEdges.add(sEd);
                    ArrayList<Vertex> vrts = new ArrayList<Vertex>();
                    vrts.add(v);
                    sVertices.add(vrts);
                }
                else {
                    ArrayList<SingEdge> sEd = sEdges.get(p);
                    if (!sEd.contains(sEdgs[0])) sEd.add(sEdgs[0]);
                    if (!sEd.contains(sEdgs[1])) sEd.add(sEdgs[1]);
                    sVertices.get(p).add(v);
                }
            }
            combineFurtherEdges();
            addMissingEdges(aFoam);
        }
        
        private void combineFurtherEdges() {
            int i = sEdges.size()-1;
            while (i > 0) {
                int j = i-1;
                ArrayList<SingEdge> fEdges = sEdges.get(i);
                while (j >= 0) {
                    ArrayList<SingEdge> nEdges = sEdges.get(j);
                    if (overlapEdges(fEdges, nEdges)) {
                        for (SingEdge ed : fEdges) {
                            if (!nEdges.contains(ed)) nEdges.add(ed);
                        }
                        ArrayList<Vertex> fVerts = sVertices.get(i);
                        ArrayList<Vertex> nVerts = sVertices.get(j);
                        for (Vertex v : fVerts) if (!nVerts.contains(v)) nVerts.add(v);
                        sEdges.remove(fEdges);
                        sVertices.remove(fVerts);
                        break;
                    }
                    j--;
                }
                i--;
            }
        }
        
        private boolean overlapEdges(ArrayList<SingEdge> fEdges, ArrayList<SingEdge> nEdges) {
            for (SingEdge ed : fEdges) if (nEdges.contains(ed)) return true;
            return false;
        }
        
        private void addMissingEdges(Foam<R> aFoam) {
            for (SingEdge ed : singEdges) {
                if (ed.isBottomOnly()) obEdges.add(ed);
            }
            for (SingEdge ed : aFoam.singEdges) {
                if (ed.isTopOnly()) otEdges.add(ed);
            }
        }
        
        private int combinedEdgeIndex(SingEdge[] se) {
            for (int i = 0; i < sEdges.size(); i++) {
                if (sEdges.get(i).contains(se[0]) || sEdges.get(i).contains(se[1])) return i;
            }
            return -1;
        }
        
        private SingEdge[] getSingEdges(Vertex v, Foam<R> aFoam) {
            SingEdge[] se = new SingEdge[2];
            for (SingEdge edge : singEdges) {
                if (edge.hasLevel(v, 1)) {
                    se[0] = edge;
                    break;
                }
            }
            for (SingEdge edge : aFoam.singEdges) {
                if (edge.hasLevel(v, 0)) {
                    se[1] = edge;
                    return se;
                }
            }
            System.out.println("Bad "+se[0]+" "+se[1]);
            return null; // shouldn't happen
        }
        
        private void glueSingEdges(Foam<R> aFoam, Foam<R> nFoam) {
            int a = cFacets.size();
            int b = obFacets.size();
            for (int i = 0; i < sEdges.size(); i++) { // adding combined singular edges
                ArrayList<SingEdge> sEd = sEdges.get(i);
                ArrayList<Vertex> mVts = sVertices.get(i);
                /*if (!checkSingFacets(sEd, aFoam, a, b)) {
                    System.out.println("Problem!");
                    throw new UnsupportedOperationException("Not supported yet.");
                }// */
                //else System.out.println("All good");
                int[] nFcs = getFacetsFrom(sEd.get(0), aFoam, a, b);
                SingEdge nEd = combineIntoOne(sEd, mVts);
                nFoam.addSingEdge(nEd);
                nFoam.addSingFacets(nFcs);
            }
            for (int i = 0; i < obEdges.size(); i++) { // adding the only bottom singular edges
                int[] nFcs = getFacetsFrom(obEdges.get(i), aFoam, a, b);
                nFoam.addSingEdge(obEdges.get(i));
                nFoam.addSingFacets(nFcs);
            }
            for (int i = 0; i < otEdges.size(); i++) { // adding the only top singular edges
                int[] nFcs = getFacetsFrom(otEdges.get(i), aFoam, a, b);
                nFoam.addSingEdge(otEdges.get(i));
                nFoam.addSingFacets(nFcs);
            }
        }

        /*private boolean checkSingFacets(ArrayList<SingEdge> seds, Foam<R> aFoam, int a, int b) {
            int[] ref = getFacetsFrom(seds.get(0), aFoam, a, b);
            for (int i = 1; i < seds.size(); i++) {
                int[] next = getFacetsFrom(seds.get(i), aFoam, a, b);
                if (!allGood(ref, next)) {
                    System.out.println(Arrays.toString(ref));
                    System.out.println(Arrays.toString(next));
                    return false;
                }
            }
            return true;
        }
        
        private boolean allGood(int[] f, int[] s) {
            if (f[0] == s[0] && f[1] == s[1] && f[2] == s[2]) return true;
            if (f[0] == s[1] && f[1] == s[2] && f[2] == s[0]) return true;
            return f[0] == s[2] && f[1] == s[0] && f[2] == s[1];
        }// for debugging */ 
        
        private int[] getFacetsFrom(SingEdge et, Foam<R> aFoam, int a, int b) {
            int[] nFac = new int[3];
            if (singEdges.contains(et)) {
                Facet[] oFacs = singFacets.get(singEdges.indexOf(et));
                for (int i = 0; i < 3; i++) nFac[i] = positionOfFacet(oFacs[i], a, b);
                return nFac;
            }
            Facet[] oFacs = aFoam.singFacets.get(aFoam.singEdges.indexOf(et));
            for (int i = 0; i < 3; i++) nFac[i] = positionOfFacet(oFacs[i], a, b);
            return nFac;
        }

        private int positionOfFacet(Facet fac, int a, int b) {
            for (int i = 0; i < a; i++) {
                if (cFacets.get(i).contains(fac)) return i;
            }
            if (obFacets.contains(fac)) return a+obFacets.indexOf(fac);
            return a+b+otFacets.indexOf(fac);
        }
        
        private SingEdge combineIntoOne(ArrayList<SingEdge> sEd, ArrayList<Vertex> mVts) {
            if (sEd.size() == mVts.size()) // it's a circle
                return new SingEdge(sEd.get(0).stVert, sEd.get(0).stVert, 0, 0);
            // if not, then it is an edge
            ArrayList<Vertex> tVerts = new ArrayList<Vertex>();
            ArrayList<Vertex> bVerts = new ArrayList<Vertex>();
            for (SingEdge ed : sEd) {
                if (singEdges.contains(ed)) { // edge from the original foam
                    if (ed.getStLevel() == 0) bVerts.add(ed.stVert);
                    if (ed.getEnLevel() == 0) bVerts.add(ed.enVert);
                }
                else { // edge from aFoam
                    if (ed.getStLevel() == 1) tVerts.add(ed.stVert);
                    if (ed.getEnLevel() == 1) tVerts.add(ed.enVert);
                }
            }
            if (tVerts.size()+bVerts.size() != 2) {
                System.out.println("Gluing problem");
                for (SingEdge sed : sEd) System.out.println(sed);
                for (Vertex v : mVts) System.out.println(v);
            }
            if (tVerts.size() == 2) { // it's a top edge only
                if (tVerts.get(0).getType() == 3) 
                    return new SingEdge(tVerts.get(0), tVerts.get(1), 1, 1);
                return new SingEdge(tVerts.get(1), tVerts.get(0), 1, 1);
            }
            if (bVerts.size() == 2) { // it's a bot edge only
                if (bVerts.get(0).getType() == 4) 
                    return new SingEdge(bVerts.get(0), bVerts.get(1), 0, 0);
                return new SingEdge(bVerts.get(1), bVerts.get(0), 0, 0);
            }
            if (tVerts.get(0).getType() == 3) 
                return new SingEdge(tVerts.get(0), bVerts.get(0), 1, 0);
            return new SingEdge(bVerts.get(0), tVerts.get(0), 0, 1);
        }
    }
    
}
