/*

Copyright (C) 2023-24 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.HomologyCalculation;
import knotjob.homology.evenkhov.EvenKhovCalculator;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class SlThreeHomology<R extends Ring<R>> extends HomologyCalculation<R> {
    
    private final ArrayList<ArrayList<Integer>> comps;
    
    public SlThreeHomology(LinkData link, long cff, DialogWrap frm, boolean rd,
            Options optns, R unt, R prime) {
        super(Reidemeister.freeOfOne(link.chosenLink()).breakUp().girthDiscMinimize(), 
                cff, frm, unt, optns, prime, !rd, rd);
        comps = theLink.getComponents();//System.out.println(link.name);
    }

    @Override
    public void calculate() {
        SlThreeComplex<R> theComplex = getComplex();
        SlThreeComplex<R> redComplex = theComplex.reducify();
        theComplex.finishOff("0", highDetail);
        if (abInf.isAborted()) return;
        int[] primes = new int[0];
        if (coeff < 0) primes = primesFrom();
        if (coeff <= 0) calculateEuclidean(theComplex, redComplex, primes);
        if (coeff == 1) calculateRational(theComplex, redComplex);
        if (coeff >= 2) calculateModular(theComplex, redComplex);
    }

    private void calculateEuclidean(SlThreeComplex<R> theComplex, SlThreeComplex<R> redComplex, 
            int[] primes) {
        ArrayList<String> finalRedInf = smithNormalize(redComplex, primes, false);
        ArrayList<String> finalInfo = smithNormalize(theComplex, primes, false);
        if (finalInfo != null) finishOff(finalInfo, false);
        if (finalRedInf != null) finishOff(finalRedInf, true);
    }
    
    private void calculateRational(SlThreeComplex<R> theComplex, SlThreeComplex<R> redComplex) {
        ArrayList<String> finalRedInf = finishUp(redComplex, false);
        ArrayList<String> finalInfo = finishUp(theComplex, false);
        if (finalInfo != null) finishOff(finalInfo, false);
        if (finalRedInf != null) finishOff(finalRedInf, true);
    }
    
    private void calculateModular(SlThreeComplex<R> theComplex, SlThreeComplex<R> redComplex) {
        ArrayList<String> finalRedInf = modNormalize(redComplex, false);
        ArrayList<String> finalInfo = modNormalize(theComplex, false);
        if (finalInfo != null) finishOff(finalInfo, false);
        if (finalRedInf != null) finishOff(finalRedInf, true);
    }
    
    private SlThreeComplex<R> getComplex() {
        if (theLink.crossingLength() == 0) return unlinkComplex();
        SlThreeComplex<R> theComplex = crossingComplex(0, null, false, 0);
        int u = 1;
        int pos = 0;
        while (u < theLink.crossingLength() - 1 && !abInf.isAborted()) {
            boolean replace = this.girthDifference(u-1) == 4;
            if (replace) pos = getDiscPosition(u);
            SlThreeComplex<R> nextComplex = crossingComplex(u, theComplex, replace,
                    theLink.getPath(u, pos));
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            modifyComplex(theComplex, nextComplex, replace, u, theLink.getPath(u, pos));
            u++;
        }
        lastComplex(theComplex, u);
        return theComplex;
    }

    private void lastComplex(SlThreeComplex<R> theComplex, int u) {
        SlThreeComplex<R> nextComplex = crossingComplex(u, theComplex, true, theLink.getPath(u, theLink.basepoint()));
        frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
        theComplex.modifyComplex(nextComplex, "2", highDetail);
    }
    
    private SlThreeComplex<R> unlinkComplex() {
        SlThreeComplex<R> complex = new SlThreeComplex<R>(unit, frame, abInf, 0, 1, 2);
        int c = theLink.unComponents();
        if ( c == 1) return complex;
        SlThreeComplex<R> nextComplex = new SlThreeComplex<R>(false, unit, null, null);
        while (c > 1) {
            complex.modifyComplex(nextComplex, "", false);
            c--;
        }
        return complex;
    }

    private SlThreeComplex<R> crossingComplex(int c, SlThreeComplex<R> complex, 
            boolean last, int a) {
        int[] pos = positionsOf(c);
        int[] pts = new int[4];
        boolean ne = biggerPos(pos[0], pos[2]);
        boolean nw = biggerPos(pos[3], pos[1]);
        boolean right = ne == nw;
        if (complex != null) {
            int[] path = theLink.getPath(c);
            ArrayList<Integer> overlap = complex.overlap(path);
            if (overlap.contains(0) && overlap.contains(2)) ne = complex.direction(path[0]);
            if (overlap.contains(1) && overlap.contains(3)) nw = complex.direction(path[3]);
            right = ne == nw;
        }
        int start = 1;
        if (right) start = 0;
        if (ne) {
            pts[0] = theLink.getPath(c, start);
            pts[1] = theLink.getPath(c, (3+start)%4);
            pts[2] = theLink.getPath(c, start+1);
            pts[3] = theLink.getPath(c, start+2);
        }
        else {
            pts[3] = theLink.getPath(c, start);
            pts[2] = theLink.getPath(c, (3+start)%4);
            pts[1] = theLink.getPath(c, start+1);
            pts[0] = theLink.getPath(c, start+2);
        }
        if (theLink.getCross(c) < 0) right = !right;
        int factor = 1;
        if (!right) factor = -1;
        if (last) pts = changePoints(pts, c, a);
        SlThreeComplex<R> theComplex = new SlThreeComplex<R>(pts, c, factor, unit, 0, frame, abInf);
        return theComplex;
    }
    
    private int[] changePoints(int[] pts, int u, int a) {
        int[] npts = new int[4];
        int nv = 100000000;//otherValue(pts);
        for (int i = 0; i < 4; i++) {
            if (pts[i] != a) npts[i] = pts[i];
            else npts[i] = nv;
        }
        return npts;
    }
    
    private int otherValue(int[] pts) {
        for (int i = 1; i <= 5; i++) {
            if (i != pts[0] && i != pts[1] && i != pts[2] && i != pts[3]) return i;
        }
        return -1; // can't get here.
    }
    
    private boolean biggerPos(int a, int b) { // is a < b ? cyclically?
        if (a < b) return b-a == 1;
        return a-b > 1;
    }

    private int[] positionsOf(int c) {
        int[] path = theLink.getPath(c);
        int[] pos = new int[4];
        for (int k = 0; k < 4; k++) {
            int j = theLink.compOf(path[k]);
            pos[k] = comps.get(j).indexOf(path[k]);
        }
        return pos;
    }

    private void finishOff(ArrayList<String> finalInfo, boolean red) {
        ArrayList<String> theStrings = endunredHom;
        if (red) theStrings = endredHom;
        for (String inf : finalInfo) {
            if (!inf.contains("x")) theStrings.add(inf);
        }
        if (!theStrings.isEmpty()) lastLine(red, theStrings);
    }
    
    private void lastLine(boolean reduced, ArrayList<String> endHom) {
        String last = "u"+coeff+".";
        if (reduced) last = "r"+coeff+".";
        for (String info : endHom) {
            if (info.contains("aborted")) last = last+"a"+quantum(info)+".";
        }
        if (reduced) last = last+theLink.basecomponent()+"c.";
        endHom.add(0,last);
    }
    
    private int quantum(String info) {
        int e = info.indexOf('h');
        if (e == -1) e = info.indexOf('a');
        return Integer.parseInt(info.substring(1, e));
    }
    
    private int[] primesFrom() {
        ArrayList<Integer> prms = EvenKhovCalculator.getPrimes(coeff, options.getPrimes());
        int[] primes = new int[prms.size()];
        for (int i = 0; i < prms.size(); i++) primes[i] = prms.get(i);
        return primes;
    }
    
    private void modifyComplex(SlThreeComplex<R> theComplex, SlThreeComplex<R> nextComplex,
            boolean replace, int u, int old) {
        theComplex.modifyComplex(nextComplex, girthInfo(u), highDetail);
        if (replace) {
            ArrayList<Integer> lEndpts = theComplex.getLastEndpoints();
            ArrayList<Integer> nEndpts = nextComplex.getLastEndpoints();
            int replacement = missingValue(nEndpts, theLink.getPath(u));
            if (lEndpts.indexOf(old) < lEndpts.indexOf(replacement)) {
                int help = old;
                old = replacement;
                replacement = help;
            }
            SlThreeComplex<R> finComplex = new SlThreeComplex<R>(unit, frame, abInf, 
                    0, old, replacement);
            theComplex.modifyComplex(finComplex, girthInfo(u), highDetail);
        }
    }
    
    private int missingValue(ArrayList<Integer> nEndpts, int[] lEndpts) {
        ArrayList<Integer> conv = new ArrayList<Integer>();
        for (int i = 0; i < 4; i++) conv.add(lEndpts[i]);
        for (int i : nEndpts)
            if (!conv.contains(i)) return i;
        return -1; // shouldn't get here
    }
    
}
