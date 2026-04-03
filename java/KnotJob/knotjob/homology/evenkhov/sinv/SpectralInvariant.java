/*

Copyright (C) 2021-24 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.evenkhov.sinv;

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
public class SpectralInvariant<R extends Ring<R>> extends SpecSeqCalculation<R> {
    
    private final boolean lee;
    
    public SpectralInvariant(LinkData tlnk, R unt, DialogWrap frm, Options optns, boolean red,
            boolean le) {
        super(Reidemeister.freeOfOne(tlnk.chosenLink().breakUp().girthMinimize()),
                frm, unt, optns, red);
        lee = le;
    }
    
    @Override
    public void calculate() {
        int[] wrt = theLink.crossingSigns();
        int hstart = -wrt[1];
        int qstart = wrt[0]+2*hstart;
        calculateSpecSeq(hstart, qstart);
    }
    
    private void calculateSpecSeq(int hs, int qs) {
        SInvComplex<R> theComplex = getComplex(hs, qs);
        if (abInf.isAborted()) return;
        int bn = 2;
        if (lee) bn = 4;
        calcSpecSeq(theComplex, bn);
    }
    
    private SInvComplex<R> getComplex(int hs, int qs) {
        if (theLink.crossingLength() == 0) return new SInvComplex<R>(theLink.unComponents()-1, unit,
                !reduced, reduced, abInf, null);
        int tsum = totalSum(theLink.getCrossings());
        SInvComplex<R> theComplex = new SInvComplex<R>(theLink.getCross(0), theLink.getPath(0),
                hs, qs, false, true, true, false, unit, frame, abInf);
        int u = 1;
        while (u < theLink.crossingLength()-1) {
            boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
                theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
            SInvComplex<R> nextComplex = new SInvComplex<R>(theLink.getCross(u), theLink.getPath(u),
                    0, 0, orient, true, true, false, unit, null, null);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            theComplex.modifyComplex(nextComplex,0,girthInfo(u), highDetail);
            if (theLink.getCross(u) < 0) tsum = tsum - theLink.getCross(u);
            else tsum = tsum + theLink.getCross(u);
            u++;
        }
        theComplex = lastComplex(theComplex, u);
        //theComplex.boundaryCheck();
        return theComplex;
    }
    
    private SInvComplex<R> lastComplex(SInvComplex<R> theComplex, int u) {
        boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
            theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
        GradedComplex<R> nextComplex = new GradedComplex<R>(theLink.getCross(u), theLink.getPath(u),
                0, 0, orient, false, true, false, unit, null, null);
        frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
        int c = 0;
        if (reduced) c = 1+theLink.getPath(u, theLink.basepoint());
        theComplex.modifyComplex(nextComplex, c, "0", highDetail);
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
    
}
