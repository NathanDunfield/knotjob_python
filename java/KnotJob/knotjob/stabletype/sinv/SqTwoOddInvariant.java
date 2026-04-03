/*

Copyright (C) 2022 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.stabletype.sinv;

import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.oddkhov.OddStableComplex;
import knotjob.links.Link;
import knotjob.links.Reidemeister;
import knotjob.rings.ModN;

/**
 *
 * @author Dirk
 */
public class SqTwoOddInvariant {
    
    private final Link theLink;
    private final Link theMirror;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final Options options;
    private final int sInv;
    private final int cases;
    private final ModN unit;
    private final ModN unitTwo;
    private final ArrayList<Integer> relQs;
    private final int[] girth;
    private final ModN epsilon;
    private int rplus;
    private int rminus;
    private int splus;
    private int sminus;
    
    public SqTwoOddInvariant(Link link, int s, int css, int eps, DialogWrap frm, Options optns) {
        theLink = Reidemeister.freeOfOne(link).breakUp().girthDiscMinimize();
        theMirror = theLink.mirror();
        options = optns;
        frame = frm;
        abInf = frame.getAbortInfo();
        unit = new ModN(1, 4);
        unitTwo = new ModN(1, 2);
        relQs = new ArrayList<Integer>();
        girth = theLink.totalGirthArray();
        sInv = s;
        cases = css;
        if (eps == 0) epsilon = unitTwo.getZero();
        else epsilon = unitTwo;
        rplus = s;
        rminus = s;
        splus = s;
        sminus = s;
    }
    
    public void calculate() {
        if ((cases & 1) != 0) rplus = sInv + theSqTwoInvariant(sInv+1, theLink);
        if ((cases & 2) != 0) splus = sInv + theSqTwoInvariant(sInv-1, theLink);
        if ((cases & 4) != 0) rminus = sInv - theSqTwoInvariant(-sInv+1, theMirror);
        if ((cases & 8) != 0) sminus = sInv - theSqTwoInvariant(-sInv-1, theMirror);
        
    }
    
    private int theSqTwoInvariant(int qdeg, Link link) {
        int[] wrt = link.crossingSigns();
        int hstart = -wrt[1];
        int qstart = wrt[0]+2*hstart;
        return calculateSqTwo(link, qdeg, hstart, qstart);
    }
    
    private int calculateSqTwo(Link link, int qdeg, int hs, int qs) {
        OddStableComplex<ModN> theComplex = getComplex(link, hs, qs, qdeg);
        if (!abInf.isAborted()) return getSqTwo(qdeg, link, theComplex);
        return 0;
    }
    
    private OddStableComplex<ModN> getComplex(Link link, int hs, int qs, int qdeg) {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        OddStableComplex<ModN> theComplex = firstComplex(link, hs, qs);
        theComplex.setClosure(link);
        theComplex.noCancelq = qdeg;
        int u = 1;
        while (u < link.crossingLength() && !abInf.isAborted()) {
            boolean orient = (theComplex.negContains(link.getPath(u, 0))| theComplex.negContains(link.getPath(u, 2))|
                theComplex.posContains(link.getPath(u,1)) | theComplex.posContains(link.getPath(u,3)));
            OddStableComplex<ModN> nextComplex = new OddStableComplex<ModN>(link.getCross(u), 
                    link.getPath(u), 0, 0, orient, unit, null, null, true, relQs);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(link.crossingLength()), 0, false);
            theComplex.modifyComplex(nextComplex, girthInfo(u), false);
            u++;
            if (u == link.crossingLength()-1) theComplex.lastCancel = true;
        }
        return theComplex;
    }
    
    private OddStableComplex<ModN> firstComplex(Link link, int hs, int qs) {
        OddStableComplex<ModN> theComplex = new OddStableComplex<ModN>(link.getCross(0), 
                link.getPath(0), hs, qs, false, unit, frame, abInf, true, relQs);
        return theComplex;
    }

    public String getInvariant() {
        return "("+rplus+", "+splus+", "+rminus+", "+sminus+")";
    }
    
    private String girthInfo(int u) {
        String info = String.valueOf(girth[u]);
        return info;
    }
    
    private int getSqTwo(int qdeg, Link link, OddStableComplex<ModN> theComplex) {
        frame.setLabelLeft("h-degree : ", 1, false);
        OneFlowSCategory<ModN> cat = new OneFlowSCategory<ModN>(frame, abInf, unit, unitTwo, unit, qdeg);
        OddSCatFiller<ModN> filler = new OddSCatFiller<ModN>(cat, qdeg, -10000,
                10000, frame, abInf, epsilon);
        filler.fill(theComplex);
        cat.normalize();
        if (abInf.isAborted()) return -1;
        cat.simplify();
        cat.changify(0);
        return cat.obtainRefinement();
    }
}
