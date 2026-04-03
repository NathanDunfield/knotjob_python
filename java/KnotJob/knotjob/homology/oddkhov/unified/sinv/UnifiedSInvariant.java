/*

Copyright (C) 2023 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.oddkhov.unified.sinv;

import java.math.BigInteger;
import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.rings.BigIntXi;
import knotjob.rings.ModN;

/**
 *
 * @author Dirk
 */
public class UnifiedSInvariant {
    
    private Link theLink;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final BigIntXi unit;
    private final BigIntXi xi;
    private final int[] girth;
    private final boolean highDetail;
    private final boolean reduce;
    private final ArrayList<Integer> theInv;
    private Integer sinv;
    private int stage;
    
    public UnifiedSInvariant(LinkData linkData, DialogWrap frm, Options optns) {
        theLink = Reidemeister.freeOfOne(linkData.chosenLink()).breakUp().girthDiscMinimize();
        girth = theLink.totalGirthArray();
        frame = frm;
        abInf = frame.getAbortInfo();
        highDetail = optns.getGirthInfo() == 2;
        sinv = linkData.sInvariant(2);
        unit = new BigIntXi(BigInteger.ONE);
        xi = new BigIntXi(BigInteger.ZERO, BigInteger.ONE);
        theInv = new ArrayList<Integer>();
        reduce = true;
        stage = 0;
    }

    public void calculate() {
        int[] wrt = theLink.crossingSigns();
        int hstart = -wrt[1];
        int qstart = wrt[0]+2*hstart;
        calculateUnifiedS(hstart, qstart);
        if (abInf.isAborted()) return;
        theLink = theLink.mirror();
        sinv = -sinv;
        stage = 2;
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, true);
        if (highDetail) frame.setLabelLeft("h-Level : ", 3, false);
        wrt = theLink.crossingSigns();
        hstart = -wrt[1];
        qstart = wrt[0]+2*hstart;
        calculateUnifiedS(hstart, qstart);
        sinv = -sinv;
    }

    public String getUInvariant() {
        if (theInv.size() != 2) return null;
        return "("+theInv.get(0)+", "+(-theInv.get(1))+")";
    }
    
    private void calculateUnifiedS(int hstart, int qstart) {
        SUniComplex<BigIntXi> theComplex = getComplex(hstart, qstart);
        if (!abInf.isAborted()) lipSarkize(theComplex);
    }
    
    private SUniComplex<BigIntXi> getComplex(int hstart, int qstart) {
        if (theLink.crossingLength() == 0) {
            int a = theLink.unComponents();
            if (reduce) a--;
            return new SUniComplex<BigIntXi>(a, unit, abInf, null);
        }
        int tsum = totalSum(theLink.getCrossings());
        int ign = 2;
        SUniComplex<BigIntXi> theComplex = 
                new SUniComplex<BigIntXi>(theLink.getCross(0), theLink.getPath(0), hstart, qstart,
                        false, unit, xi, frame, abInf);
        theComplex.setClosure(theLink);
        int u = 1;
        while (u < theLink.crossingLength()) {
            boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
                theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
            SUniComplex<BigIntXi> nextComplex = new SUniComplex<BigIntXi>(theLink.getCross(u),
                    theLink.getPath(u), 0, 0, orient, unit, xi, null, null);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            if (reduce && u == theLink.crossingLength() - 1) theComplex.setReduced(theLink.getPath(u, theLink.basepoint()));
            theComplex.modifyComplex(nextComplex, girthInfo(u), highDetail);
            theComplex.throwAway(tsum-ign+2,ign);
            if (theLink.getCross(u) < 0) tsum = tsum - theLink.getCross(u);
            else tsum = tsum + theLink.getCross(u);
            u++;
        }
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
    
    private void lipSarkize(SUniComplex<BigIntXi> theComplex) {
        UnifiedChanger<BigIntXi, ModN> changer = new UnifiedChanger<BigIntXi, ModN>(theComplex,
                unit, new ModN(1, 2), frame, abInf, sinv, reduce, stage);
        changer.setupComplexes();
        if (abInf.isAborted()) return;
        int x = 1;
        if (reduce) x = 0;
        changer.simplifyComplexes(x);
        if (abInf.isAborted()) return;
        if (sinv == null) sinv = changer.getSInvariant();
        theInv.add(changer.getUInvariant(x));
    }

    public int getSInvariant() {
        return sinv;
    }
    
}
