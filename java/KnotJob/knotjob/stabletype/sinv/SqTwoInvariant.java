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
import knotjob.homology.evenkhov.EvenStableComplex;
import knotjob.links.Link;
import knotjob.links.Reidemeister;
import knotjob.rings.ModN;

/**
 *
 * @author Dirk
 */
public class SqTwoInvariant {
    
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
    private int rplus;
    private int rminus;
    private int splus;
    private int sminus;
    
    public SqTwoInvariant(Link link, int s, int css, DialogWrap frm, Options optns) {
        theLink = Reidemeister.freeOfOne(link);
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
        EvenStableComplex<ModN> theComplex = getComplex(link, hs, qs, qdeg);
        if (!abInf.isAborted()) return getSqTwo(qdeg, link, theComplex);
        return 0;
    }
    
    private EvenStableComplex<ModN> getComplex(Link link, int hs, int qs, int qdeg) {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        EvenStableComplex<ModN> theComplex = firstComplex(link, hs, qs);
        theComplex.noCancelq = qdeg;
        int u = 1;
        while (u < link.crossingLength() && !abInf.isAborted()) {
            boolean orient = (theComplex.negContains(link.getPath(u, 0))| theComplex.negContains(link.getPath(u, 2))|
                theComplex.posContains(link.getPath(u,1)) | theComplex.posContains(link.getPath(u,3)));
            EvenStableComplex<ModN> nextComplex = new EvenStableComplex<ModN>(link.getCross(u), link.getPath(u), 0, 0,
                    orient, true, true, false, unit, null, null, relQs);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(link.crossingLength()), 0, false);
            theComplex.modifyStableComplex(nextComplex, 0, girthInfo(u), false);
            u++;
            if (u == link.crossingLength()-1) theComplex.lastCancel = true;
        }
        return theComplex;
    }
    
    private EvenStableComplex<ModN> firstComplex(Link link, int hs, int qs) {
        EvenStableComplex<ModN> theComplex = new EvenStableComplex<ModN>(link.getCross(0), 
                link.getPath(0), hs, qs, false, true, true, false, unit, frame, abInf, relQs);
        return theComplex;
    }
    
    public String getInvariant() {
        return "("+rplus+", "+splus+", "+rminus+", "+sminus+")";
    }
    
    private String girthInfo(int u) {
        String info = String.valueOf(girth[u]);
        return info;
    }
    
    private int getSqTwo(int qdeg, Link link, EvenStableComplex<ModN> theComplex) {
        frame.setLabelLeft("h-degree : ", 1, false);
        OneFlowSCategory<ModN> cat = new OneFlowSCategory<ModN>(frame, abInf, unit, unitTwo, unit, qdeg);
        boolean sock = sockComplex();
        EvenSCatFiller<ModN> filler = new EvenSCatFiller<ModN>(cat, qdeg, -10000,
                10000, frame, abInf, sock, link);
        filler.fill(theComplex);
        if (sock) cat.whitneyfy();
        cat.normalize();
        if (abInf.isAborted()) return -1;
        cat.simplify();
        cat.changify(0);
        return cat.obtainRefinement();
    }
    
    private boolean sockComplex() {
        boolean cube = true;
        int i = 0;
        while (i < theLink.crossingLength() && cube) {
            if (theLink.getCross(i) != 1 && theLink.getCross(i) != -1) cube = false;
            else i++;
        }
        return !cube;
    }
    
}
