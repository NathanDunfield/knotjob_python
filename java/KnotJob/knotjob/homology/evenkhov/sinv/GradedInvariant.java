/*

Copyright (C) 2021-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class GradedInvariant<R extends Ring<R>> {
    
    private final Link theLink;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final int[] girth;
    private final R unit;
    private final boolean highDetail;
    private String grInv;
    private String sInv;
    
    public GradedInvariant(LinkData thLnk, R unt, DialogWrap frm, Options optns) {
        Link lnk = Reidemeister.freeOfOne(thLnk.chosenLink().breakUp().girthMinimize());
        if (lnk.crossingLength() == 1) theLink = lnk.breakUp();
        else theLink = lnk;
        girth = theLink.totalGirthArray();
        unit = unt;
        frame = frm;
        abInf = frm.getAbortInfo();
        highDetail = optns.getGirthInfo() == 2;
    }

    public void calculate() {
        if (theLink.crossingNumber() == 0) { // it's an obvious unknot
            sInv = "0";
            grInv = "0. 0";
            return;
        }
        int[] wrt = theLink.crossingSigns();
        int hstart = -wrt[1];
        int qstart = wrt[0]+2*hstart;
        calculateGradedInvariant(hstart, qstart);
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
    
    private void calculateGradedInvariant(int hstart, int qstart) {
        GradedComplex<R> theComplex = getComplex(hstart,qstart);
        if (abInf.isAborted()) return;
        theComplex.smithNormalizeZero();
        if (abInf.isAborted()) return;
        GradedComplex<R> mirrorComplex = theComplex.mirror();
        grInv = theComplex.getGrading();
        if (grInv == null) return;
        sInv = getSInvariant(grInv);
        String mir = mirrorComplex.getGrading();
        if (mir == null) {
            grInv = null;
            return;
        }
        grInv = grInv+". "+mir;
    }
    
    private String getSInvariant(String gInfo) {
        int v = gInfo.indexOf(" ");
        if (v > 0) return gInfo.substring(0, v);
        return gInfo;
    }
    
    public int getSInvariant() {
        return Integer.parseInt(sInv);
    }
    
    private GradedComplex<R> getComplex(int hstart, int qstart) {
        int tsum = totalSum(theLink.getCrossings());
        int ign = 1;
        GradedComplex<R> theComplex = new GradedComplex<R>(theLink.getCross(0), theLink.getPath(0),
                hstart, qstart, false, true, true, false, unit, frame, abInf);
        int u = 1;
        while (u < theLink.crossingLength()-1) {
            boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
                theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
            GradedComplex<R> nextComplex = new GradedComplex<R>(theLink.getCross(u), theLink.getPath(u),
                    0, 0, orient, true, true, false, unit, null, null);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            theComplex.modifyComplex(nextComplex,0,girthInfo(u), highDetail);
            theComplex.throwAway(tsum-ign+2,ign);
            if (theLink.getCross(u) < 0) tsum = tsum - theLink.getCross(u);
            else tsum = tsum + theLink.getCross(u);
            u++;
        }
        theComplex = lastComplex(theComplex, u);
        //theComplex.boundaryCheck();
        return theComplex;
    }
    
    private String girthInfo(int u) {
        String info = String.valueOf(girth[u]);
        if (!highDetail) return info;
        if (u < girth.length - 1) info = info+" ("+girth[u+1];
        else return info;
        for (int i = 1; i < 3; i++) {
            if (u < girth.length - i - 1) info = info+", "+girth[u+1+i];
        }
        info = info+")";
        return info;
    }
    
    private GradedComplex<R> lastComplex(GradedComplex<R> theComplex, int u) {
        boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
            theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
        GradedComplex<R> nextComplex = new GradedComplex<R>(theLink.getCross(u), theLink.getPath(u),
                0, 0, orient, false, true, false, unit, null, null);
        frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
        int c = 1+theLink.getPath(u, theLink.basepoint());
        theComplex.modifyComplex(nextComplex, c, "0", highDetail);
        return theComplex;
    }

    public String getGInvariant() {
        return grInv;
    }
    
}
