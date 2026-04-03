/*

Copyright (C) 2023-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.slthree;

import java.util.ArrayList;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.homology.slthree.foam.Edge;
import knotjob.homology.slthree.foam.Facet;
import knotjob.homology.slthree.foam.Foam;
import knotjob.homology.slthree.foam.SingEdge;
import knotjob.homology.slthree.foam.Web;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class SlTArrow<R extends Ring<R>> extends Arrow<R> {
    
    private final ArrayList<Foam<R>> foams;
    
    public SlTArrow(SlTGenerator<R> bo, SlTGenerator<R> to) {
        super(bo, to, null);
        foams = new ArrayList<Foam<R>>();
    }
    
    public SlTArrow(SlTGenerator<R> bo, SlTGenerator<R> to, ArrayList<Foam<R>> fms) {
        super(bo, to, null);
        foams = fms;
    }
    
    public SlTArrow(SlTGenerator<R> bo, SlTGenerator<R> to, Foam<R> fm) {
        super(bo, to, null);
        foams = new ArrayList<Foam<R>>();
        foams.add(fm);
    }
    
    public void addFoam(Foam<R> foam) {
        foams.add(foam);
    }
    
    public ArrayList<Foam<R>> getFoams() {
        return foams;
    }
    
    public Web getBotWeb() {
        return ((SlTGenerator<R>) bObj).getWeb();
    }
    
    public Web getTopWeb() {
        return ((SlTGenerator<R>) tObj).getWeb();
    }
    
    @Override
    public void addValue(R ad) {
        if (foams.isEmpty()) foams.add(new Foam<R>(null, null, ad));
        else foams.get(0).addValue(ad);
    }
    
    @Override
    public R getValue() {
        return foams.get(0).getValue();
    }
    
    @Override
    public void output(ArrayList<Generator<R>> nextLevel) {
        System.out.println("To : "+nextLevel.indexOf(tObj));
        for (Foam<R> foam : foams) foam.output();
    }

    public boolean noFoams() {
        return foams.isEmpty();
    }

    public void checkCylinders(int sType, R unit) {
        // will check whether some facets have cylinders that can be simplified
        int i = foams.size()-1;
        while (i >= 0) {
            Foam<R> foam = foams.get(i);
            int[] f = foam.checkCylinder();
            if (f != null) {
                ArrayList<Foam<R>> newFoams = removeCylinder(f, foam, sType);
                foams.remove(i);
                for (Foam<R> nFoam : newFoams) foams.add(i, nFoam);
                i = i + newFoams.size();
            }
            i--;
        } // */
    }
    
    private ArrayList<Foam<R>> removeCylinder(int[] f, Foam<R> foam, int sType) {
        ArrayList<Foam<R>> nFoams = new ArrayList<Foam<R>>();
        Facet fac = foam.getFacets().get(f[0]);
        Edge ed = fac.getDomainEdges().get(f[1]);
        Foam<R> nFoam = splitOffFoam(foam, fac, ed, foam.getValue().negate(), 2, 0, sType);
        if (nFoam != null) nFoams.add(nFoam);
        nFoam = splitOffFoam(foam, fac, ed, foam.getValue().negate(), 1, 1, sType);
        if (nFoam != null) nFoams.add(nFoam);
        nFoam = splitOffFoam(foam, fac, ed, foam.getValue().negate(), 0, 2, sType);
        if (nFoam != null) nFoams.add(nFoam);
        if (sType == 1) {
            nFoams.add(splitOffFoam(foam, fac, ed, foam.getValue(), 1, 0, sType));
            nFoams.add(splitOffFoam(foam, fac, ed, foam.getValue(), 0, 1, sType));
        }
        if (sType == 2) nFoams.add(splitOffFoam(foam, fac, ed, foam.getValue(), 0, 0, sType));
        return nFoams;
    }
    
    private Foam<R> splitOffFoam(Foam<R> foam, Facet fac, Edge ed, R val, int dts, 
            int sdt, int sType) {
        if (sType == 0 && fac.getDots()+sdt >= 3) return null;
        Foam<R> nFoam = new Foam<R>(foam.getDoWeb(), foam.getCoWeb(), val);
        for (Facet oFac : foam.getFacets()) {
            if (oFac != fac) nFoam.addFacets(new Facet(oFac));
        }
        Facet sFac = new Facet(ed, dts);
        nFoam.addFacets(sFac);
        Facet rFac = new Facet(fac.getDots()+sdt, fac.getEuler() + 2 - ed.getEuler());
        for (Edge de : fac.getDomainEdges()) {
            if (de != ed) rFac.getDomainEdges().add(de);
        }
        for (Edge ce : fac.getCodomainEdges()) {
            if (ce != ed) rFac.getCodomainEdges().add(ce);
        }
        nFoam.addFacets(rFac);
        int p = foam.getFacets().indexOf(fac);
        int t = foam.facetSize();
        for (int i = 0; i < foam.getSingEdges().size(); i++) {
            SingEdge sEd = foam.getSingEdges().get(i);
            int[] pos = foam.getSingPositions(sEd);
            int[] npos = getNewPositions(pos, p, t, sEd, ed);
            nFoam.addSingEdge(sEd);
            nFoam.addSingFacets(npos);
        }
        return nFoam;
    }
    
    private int[] getNewPositions(int[] pos, int p, int t, SingEdge sEd, Edge ed) {
        int[] npos = new int[3];
        for (int j = 0; j < 3; j++) {
            if (pos[j] < p) npos[j] = pos[j];
            if (pos[j] > p) npos[j] = pos[j] - 1;
            if (pos[j] == p) {
                if (sEd.getStVertexName() == ed.getStVertexName() || 
                        sEd.getStVertexName() == ed.getEnVertexName()) npos[j] = t-1;
                else npos[j] = t;
            }
        }
        return npos;
    }
    
    public void dotChecker(int sType, R unit) {
        // will look if any facets have >= 3 dots
        int i = foams.size()-1;
        while (i >= 0) {
            Foam<R> foam = foams.get(i);
            if (foam.threeDotsOnFacet()) {
                if (sType == 0) {
                    foams.remove(foam);
                    i--;
                    continue;
                }
                reduceDots(foam, sType);
            }
            i--;
        }
    }
    
    public void checkCombineFoams() {
        int i = foams.size()-1;
        while (i > 0) {
            Foam<R> foam = foams.get(i);
            int j = i - 1;
            while (j >= 0) {
                Foam<R> nFoam = foams.get(j);
                if (foam.sameAs(nFoam)) {
                    foams.remove(i);
                    nFoam.addValue(foam.getValue());
                    if (nFoam.getValue().isZero()) {
                        foams.remove(j);
                        i--;
                    }
                    break;
                }
                j--;
            }
            i--;
        }
    }
    
    public void simplifyFoams(int sType, R unit) {
        int i = foams.size()-1;
        //R mThree = unit.add(unit.add(unit)).negate();
        while (i >= 0) {
            Foam<R> foam = foams.get(i);
            /*if (sType == 0) {//
                foam.genusCheck(mThree);
                if (foam.getValue().isZero()) {
                    foams.remove(foam);
                    i--;
                    continue;
                }
            }// */
            if (foam.threeDotsOnFacet()) {
                if (sType == 0) {
                    foams.remove(foam);
                    i--;
                    continue;
                }
                reduceDots(foam, sType);
            }// */
            if (foam.dotsOnThreeFacets()) {
                if (sType == 3) {
                    foam.reduceOneDotEach();
                }
                else {
                    foams.remove(foam);
                    i--;
                    continue;
                }
            }// */
            if (sType == 0 && foam.twoDotsOnTwo()) {
                foams.remove(foam);
                i--;
                continue;
            }// */
            ArrayList<Foam<R>> nFoams = foam.simplifyFoam(sType, unit);
            if (nFoams == null) {
                foams.remove(foam);
                i--;
                continue;
            }
            if (!nFoams.isEmpty()) {
                int k = nFoams.size();
                foams.remove(i);
                for (Foam<R> nFoam : nFoams) foams.add(i, nFoam);
                i = i + k;
            }
            i--;
        }
    }
    
    private void reduceDots(Foam<R> foam, int sType) {
        foam.reduceDots(sType);
        /*if (sType == 1) { // three dots reduce to two dots
            foam.reduceDots(1);
        }
        if (sType == 2) { // three dots reduce to one dot
            foam.reduceDots(2);
        } 
        if (sType == 3) { // three dots reduce to zero dots
            foam.reduceDots(3);
        } // */
    }
    
    public SlTArrow<R> composeWith(SlTArrow<R> arr) {
        SlTArrow<R> nArr = new SlTArrow<R>((SlTGenerator<R>) this.bObj, (SlTGenerator<R>) arr.tObj);
        for (Foam<R> fFoam : this.foams) {
            for (Foam<R> sFoam : arr.foams) {
                nArr.addFoam(fFoam.composeWith(sFoam));
            }
        }
        return nArr;
    }
    
    // for GenSlTComplex
    
    public void dotChecker(int sType, R unit, R t) {
        // will look if any facets have >= 3 dots
        int i = foams.size()-1;
        while (i >= 0) {
            Foam<R> foam = foams.get(i);
            while (foam.threeDotsOnFacet()) {
                reduceDots(foam, t, sType);
                i++;
            }
            i--;
        }
    }

    public void simplifyFoams(int sType, R unit, R t) {
        int i = foams.size()-1;
        while (i >= 0) {
            Foam<R> foam = foams.get(i);
            while (foam.threeDotsOnFacet()) {
                reduceDots(foam, t, sType);
                i++; // an extra foam is added at the front
            }
            if (foam.dotsOnThreeFacets()) {
                foam.reduceOneDotEach(t);
            }
            ArrayList<Foam<R>> nFoams = foam.simplifyFoam(sType, unit, t);
            if (nFoams == null) {
                foams.remove(foam);
                i--;
                continue;
            }
            if (!nFoams.isEmpty()) {
                int k = nFoams.size();
                foams.remove(i);
                for (Foam<R> nFoam : nFoams) foams.add(i, nFoam);
                i = i + k;
            }
            i--;
        }
    }
    
    private void reduceDots(Foam<R> foam, R t, int sType) { // assumes X^3 = X^{3-sType} - t 
        Foam<R> clone = new Foam<R>(foam.getValue().multiply(t), foam);
        clone.reduceFirstDotFacet(3);
        foam.reduceFirstDotFacet(sType);
        foams.add(0, clone);
    }
    
}
