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

package knotjob.homology.slthree.univ;

import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.SpecSeqCalculation;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class UnivSlThreeHom<R extends Ring<R>> extends SpecSeqCalculation<R> {

    private final int sType;
    private final boolean checkSI;
    private int sInv;
    
    
    public UnivSlThreeHom(LinkData link, int ty, DialogWrap frm, R unt, Options opts, 
            boolean r, boolean si) {
        super(Reidemeister.freeOfOne(link.chosenLink()).breakUp().girthDiscMinimize(), 
                frm, unt, opts, r);
        sType = ty;
        checkSI = si;
    }

    @Override
    public void calculate() {
        UnivSlTComplex<R> theComplex = getComplex();
        if (abInf.isAborted()) return;
        if (reduced) theComplex.finishOffRed("0", highDetail);
        else theComplex.finishOff("0", highDetail);
        //if (!abInf.isAborted()) theComplex.boundaryCheck();
        int jump = (-2) * sType;
        calcSpecSeq(theComplex, jump);
        if (checkSI) sInv = theComplex.lastQDegree();
    }
    
    public int sInvariant() {
        return sInv / 2;
    }
    
    private UnivSlTComplex<R> getComplex() {
        if (theLink.crossingLength() == 0) return unlinkComplex();
        UnivSlTComplex<R> theComplex = crossingComplex(0, null, false, 0, sType);
        int u = 1;
        int pos = 0;
        while (u < theLink.crossingLength() - 1 && !abInf.isAborted()) {
            boolean replace = this.girthDifference(u-1) == 4;
            if (replace) pos = getDiscPosition(u);
            UnivSlTComplex<R> nextComplex = crossingComplex(u, theComplex, replace, 
                    theLink.getPath(u, pos), sType);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            modifyComplex(theComplex, nextComplex, replace, u, theLink.getPath(u, pos), sType);
            u++;
        }
        lastComplex(theComplex, u);
        return theComplex;
    }

    private void lastComplex(UnivSlTComplex<R> theComplex, int u) {
        UnivSlTComplex<R> nextComplex = crossingComplex(u, theComplex, true, 
                theLink.getPath(u, theLink.basepoint()), sType);
        frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
        theComplex.modifyComplex(nextComplex, "2", highDetail);
    }
    
    private UnivSlTComplex<R> unlinkComplex() {
        UnivSlTComplex<R> complex = new UnivSlTComplex<R>(unit, frame, abInf, 0, 1, 2);
        int c = theLink.unComponents();
        if (c == 1) return complex;
        UnivSlTComplex<R> nextComplex = new UnivSlTComplex<R>(false, unit, null, null);
        while (c > 1) {
            complex.modifyComplex(nextComplex, "", false);
            c--;
        }
        return complex;
    }

    /*private UnivSlTComplex<R> crossingComplex(int c, UnivSlTComplex<R> complex, boolean last) {
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
        if (last) pts = changePoints(pts, c);
        UnivSlTComplex<R> theComplex = new UnivSlTComplex<R>(pts, c, factor, unit, 
                sType, frame, abInf);
        return theComplex;
    }
    
    private int[] changePoints(int[] pts, int u) {
        int[] npts = new int[4];
        int a = theLink.getPath(u, theLink.basepoint());
        int nv = otherValue(pts);
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
    } // */
    
}
