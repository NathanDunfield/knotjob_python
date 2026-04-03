/*

Copyright (C) 2025 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.evenkhov.sinv.link;

import java.util.ArrayList;
import knotjob.Calculation;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class LinkSInvariant<R extends Ring<R>> extends Calculation<R> {
    
    private final ArrayList<int[]> orientations;
    private final ArrayList<Integer> equivCircles;
    private final int jump;
    private final String name;
    private int sinv;
    
    public LinkSInvariant(LinkData thLnk, R unt, DialogWrap frm, Options optns, int jmp) {
        super(Reidemeister.freeOfOne(thLnk.chosenLink()).breakUp().girthDiscMinimize(), frm, unt, optns);
        jump = jmp;
        name = thLnk.name;
        orientations = theLink.getOrientationsOfCrossings();
        equivCircles = getEquivalenceClass();
    }

    private ArrayList<Integer> getEquivalenceClass() {
        ArrayList<Integer> firstClass = new ArrayList<Integer>();
        ArrayList<Integer> seconClass = new ArrayList<Integer>();
        int[][] paths = theLink.getPaths();
        for (int i = 0; i < orientations.size(); i++) {
            int[] or = orientations.get(i);
            int[] path = paths[i];
            int[] orPaths = getOrRes(or, path);
            boolean firstCase = checkCase(orPaths, firstClass, seconClass);
            addPaths(firstClass, seconClass, firstCase, orPaths);
        }
        return firstClass;
    }

    private int[] getOrRes(int[] or, int[] path) {
        int[] orPaths = new int[4];
        orPaths[0] = path[or[0]];
        orPaths[2] = path[or[1]];
        if ((or[1]+1)%4 == or[0]) {
            orPaths[1] = path[(or[0]+1)%4];
            orPaths[3] = path[(or[1]+3)%4];
        }
        else {
            orPaths[1] = path[(or[0]+3)%4];
            orPaths[3] = path[(or[1]+1)%4];
        }
        return orPaths;
    }

    private boolean checkCase(int[] orPaths, ArrayList<Integer> firstClass, ArrayList<Integer> seconClass) {
        if (firstClass.contains(orPaths[0]) || firstClass.contains(orPaths[1])) return true;
        if (seconClass.contains(orPaths[0]) || seconClass.contains(orPaths[1])) return false;
        return !(firstClass.contains(orPaths[2]) || firstClass.contains(orPaths[3]));
    }

    private void addPaths(ArrayList<Integer> firstClass, ArrayList<Integer> seconClass, 
            boolean firstCase, int[] orp) {
        int adder = 0;
        if (!firstCase) adder = 2;
        if (!firstClass.contains(orp[adder])) firstClass.add(orp[adder]);
        if (!firstClass.contains(orp[adder+1])) firstClass.add(orp[adder+1]);
        if (!seconClass.contains(orp[(adder+2)%4])) seconClass.add(orp[(adder+2)%4]);
        if (!seconClass.contains(orp[(adder+3)%4])) seconClass.add(orp[(adder+3)%4]);
    }
    
    @Override
    public void calculate() {
        int[] wrt = theLink.crossingSigns();
        int hstart = -wrt[1];
        int qstart = wrt[0]+2*hstart;
        calculateSInvariant(hstart, qstart);
    }

    public int getSInvariant() {
        return sinv;
    }
    
    private void calculateSInvariant(int hstart, int qstart) {
        LinkSComplex<R> theComplex = getComplex(hstart, qstart);
        if (abInf.isAborted()) return;
        theComplex.specSeq(jump);
        sinv = theComplex.minimalImageQs() + 1 - theLink.unComponents();
        /*ArrayList<Integer> theQs = theComplex.theQs();
        int[] minmax = theMinAndMax(theQs);
        if (minmax[0] != sinv-1) System.out.println(name+" "+minmax[0]+" "+(sinv-1)+" "+minmax[1]);
        if (minmax[1] - minmax[0] > 2 && theLink.components() > 2) System.out.println("X"+name+" "+theQs+" "+minmax[0]+" "+(sinv-1)+" "+minmax[1]);
        // */
    }

    private LinkSComplex<R> getComplex(int hs, int qs) {
        int tsum = totalSum(theLink.getCrossings());
        int ign = 1;
        LinkSComplex<R> theComplex = nextComplex(0, hs, qs, false);
        int u = 1;
        while (u < theLink.crossingLength()) {
            boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
                theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
            LinkSComplex<R> nextComplex = nextComplex(u, 0, 0, orient);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            theComplex.modifyComplex(nextComplex, 0, girthInfo(u), highDetail);
            theComplex.throwAway(tsum-ign+2, 1);
            if (theLink.getCross(u) < 0) tsum = tsum - theLink.getCross(u);
            else tsum = tsum + theLink.getCross(u);
            u++;// */
        }
        if (!abInf.isAborted() && !theComplex.cocycleCheck()) throw new UnsupportedOperationException("Not a cocycle (LinkSInvariant.java getComplex).");
        return theComplex;
    }

    private int totalSum(int[] crossings) {
        int tsum = 0;
        for (int r = 1; r < crossings.length; r++) {
            if (crossings[r] < 0) tsum = tsum + crossings[r];
            else tsum = tsum - crossings[r];
        }
        tsum--;
        return tsum;
    }

    private LinkSComplex<R> nextComplex(int c, int hs, int qs, boolean orient) {
        int sgn = theLink.getCross(c);
        int[] path = theLink.getPath(c);
        int[] or = orientations.get(c);
        int a = path[or[0]];
        int b = path[or[1]];
        if (equivCircles.contains(b)) {
            a = b;
            b = path[or[0]];
        }
        return new LinkSComplex<R>(sgn, path, hs, qs, a, b, orient, true, unit, frame, abInf);
    }

    private int[] theMinAndMax(ArrayList<Integer> theQs) {
        int[] minmax = new int[] {theQs.get(0), theQs.get(0)};
        for (int q : theQs) {
            if (minmax[0] > q) minmax[0] = q;
            if (minmax[1] < q) minmax[1] = q;
        }
        return minmax;
    }// */
    
    
    
}
