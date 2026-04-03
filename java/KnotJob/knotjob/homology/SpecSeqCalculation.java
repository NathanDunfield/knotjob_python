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

package knotjob.homology;

import java.util.ArrayList;
import knotjob.Calculation;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.slthree.univ.GenSlTComplex;
import knotjob.homology.slthree.univ.UnivSlTComplex;
import knotjob.links.Link;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public abstract class SpecSeqCalculation<R extends Ring<R>> extends Calculation<R> {

    private final ArrayList<String> allInfo;
    private final ArrayList<Integer> blocks;
    protected final boolean reduced;
    protected final ArrayList<ArrayList<Integer>> comps;
    
    
    public SpecSeqCalculation(Link lnk, DialogWrap frm, R unt, Options opts,
            boolean r) {
        super(lnk, frm, unt, opts);
        allInfo = new ArrayList<String>();
        blocks = new ArrayList<Integer>();
        reduced = r;
        comps = theLink.getComponents();
    }
    
    public ArrayList<String> getInfo() {
        return allInfo;
    }
    
    public ArrayList<Integer> getBlocks() {
        return blocks;
    }
    
    protected void calcSpecSeq(ChainComplex<R> theComplex, int qJump) {
        int bn = qJump;
        blocks.add(1);
        ArrayList<String> qCohs = theComplex.homologyInfo();
        copyOver(qCohs);
        blocks.add(qCohs.size());
        int eCounter = 2;
        while (theComplex.containsBoundaries()) {
            theComplex.cancelGenerators(qJump);
            if (abInf.isAborted()) return;
            /*if (eCounter == 4) {
                System.out.println("Page "+eCounter);
                theComplex.output();
            }// */
            qCohs = theComplex.homologyInfo();
            blocks.add(allInfo.size()+1);
            copyOver(qCohs);
            blocks.add(allInfo.size());
            qJump = qJump + bn;
            eCounter++;
        }
    }
    
    private void copyOver(ArrayList<String> strings) {
        for (String dtr : strings) allInfo.add(dtr);
    }
    
    protected void modifyComplex(UnivSlTComplex<R> theComplex, UnivSlTComplex<R> nextComplex,
            boolean replace, int u, int old, int sType) {
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
            UnivSlTComplex<R> finComplex = new UnivSlTComplex<R>(unit, frame, abInf, 
                    sType, old, replacement);
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
    
    protected UnivSlTComplex<R> crossingComplex(int c, UnivSlTComplex<R> complex, 
            boolean replace, int rep, int sType) {
        int[] data = getData(c, complex, replace, rep);
        int[] pts = new int[] {data[0], data[1], data[2], data[3]};
        int factor = data[4];
        UnivSlTComplex<R> theComplex = new UnivSlTComplex<R>(pts, c, factor, unit, 
                sType, frame, abInf);
        return theComplex;
    }
    
    protected GenSlTComplex<R> crossingComplex(int c, GenSlTComplex<R> complex,
            boolean replace, int rep, int sType, R t) {
        int[] data = getData(c, complex, replace, rep);
        int[] pts = new int[] {data[0], data[1], data[2], data[3]};
        int factor = data[4];
        GenSlTComplex<R> theComplex = new GenSlTComplex<R>(pts, c, factor, unit, t,
                sType, frame, abInf);
        return theComplex;
    }

    private int[] getData(int c, UnivSlTComplex<R> complex, 
            boolean replace, int rep) {
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
        if (replace) pts = changePoints(pts, c, rep);
        return new int[] {pts[0], pts[1], pts[2], pts[3], factor};
    }
    
    private int[] changePoints(int[] pts, int u, int rep) {
        int[] npts = new int[4];
        //int a = theLink.getPath(u, theLink.basepoint());
        int nv = 100000000;//otherValue(pts); //just a large value that should never be reached otherwise
        for (int i = 0; i < 4; i++) {
            if (pts[i] != rep) npts[i] = pts[i];
            else npts[i] = nv;
        }
        return npts;
    }
    
    private int otherValue(int[] pts) { // this doesn't work, as the number should also not
        for (int i = 1; i <= 5; i++) { // be used by the old complex, and this information is not
            if (i != pts[0] && i != pts[1] && i != pts[2] && i != pts[3]) return i; // present here
        }
        return -1; // can't get here. */
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
    
}
