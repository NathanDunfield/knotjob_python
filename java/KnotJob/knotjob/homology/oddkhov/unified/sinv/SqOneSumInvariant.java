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

import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.oddkhov.unified.Homomorphism;
import knotjob.homology.oddkhov.unified.UnifiedChainComplex;
import knotjob.homology.oddkhov.unified.UnifiedSES;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.rings.ModNXi;

/**
 *
 * @author Dirk
 */
public class SqOneSumInvariant {
    
    private Link theLink;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final ModNXi unit;
    private final ModNXi xi;
    private final int[] girth;
    private final boolean highDetail;
    private final ArrayList<Integer> invs;
    private Homomorphism<ModNXi> beta;
    private String sqinv;
    private Integer sinv;
    
    public SqOneSumInvariant(LinkData linkData, DialogWrap frm, Options optns) {
        theLink = Reidemeister.freeOfOne(linkData.chosenLink()).breakUp().girthDiscMinimize();
        girth = theLink.totalGirthArray();
        frame = frm;
        abInf = frame.getAbortInfo();
        unit = new ModNXi(1, 2);
        xi = new ModNXi(0, 1, 2);
        highDetail = optns.getGirthInfo() == 2;
        invs = new ArrayList<Integer>();
        sinv = linkData.sInvariant(2);
    }

    public void calculate() {
        int[] wrt = theLink.crossingSigns();
        int hstart = -wrt[1];
        int qstart = wrt[0]+2*hstart;
        calculateSqOneSum(hstart, qstart);
        if (abInf.isAborted()) return;
        theLink = theLink.mirror();
        sinv = -sinv;
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, true);
        wrt = theLink.crossingSigns();
        hstart = -wrt[1];
        qstart = wrt[0]+2*hstart;
        calculateSqOneSum(hstart, qstart);
        sinv = -sinv;
        if (abInf.isAborted()) return;
        sqinv  = "("+invs.get(0)+", "+invs.get(1)+", "+(-invs.get(2))+", "+(-invs.get(3))+")";
    }

    public int getSInvariant() {
        return sinv;
    }

    public String getInvariant() {
        return sqinv;
    }

    private void calculateSqOneSum(int hstart, int qstart) {
        SUniComplex<ModNXi> theComplex = getComplex(hstart, qstart);
        if (!abInf.isAborted()) lipSarkize(theComplex);
    }
    
    private SUniComplex<ModNXi> getComplex(int hstart, int qstart) {
        if (theLink.crossingLength() == 0) return new SUniComplex<ModNXi>(theLink.unComponents(), 
                unit, abInf, null);
        //if (theLink.crossingLength() == 1) return oneCrossingComplex(hstart, qstart);
        int tsum = totalSum(theLink.getCrossings());
        int ign = 2;
        SUniComplex<ModNXi> theComplex = 
                new SUniComplex<ModNXi>(theLink.getCross(0), theLink.getPath(0), hstart, qstart,
                        false, unit, xi, frame, abInf);
        theComplex.setClosure(theLink);
        int u = 1;
        while (u < theLink.crossingLength()) {
            boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
                theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
            SUniComplex<ModNXi> nextComplex = new SUniComplex<ModNXi>(theLink.getCross(u),
                    theLink.getPath(u), 0, 0, orient, unit, xi, null, null);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            theComplex.modifyComplex(nextComplex, girthInfo(u), highDetail);
            theComplex.throwAway(tsum-ign+2,ign);
            if (theLink.getCross(u) < 0) tsum = tsum - theLink.getCross(u);
            else tsum = tsum + theLink.getCross(u);
            u++;
        }
        return theComplex;
    }
    
    private void lipSarkize(SUniComplex<ModNXi> theComplex) {
        if (sinv == null) {
            SUniComplex<ModNXi> modTwoComplex = new SUniComplex<ModNXi>(theComplex, unit, xi, false);
            sinv = modTwoComplex.barnatize(1);
        }
        UnifiedSES<ModNXi> ses = new UnifiedSES<ModNXi>(theComplex, unit, xi, frame, abInf);
        beta = ses.getConnectingHom(true, true, -1, sinv+1);
        if (beta == null) invs.add(sinv);
        else {
            ArrayList<Integer> theQs = theComplex.getQs();
            int qmax = theQs.get(theQs.size()-1);
            int qmin = theQs.get(0);
            UnifiedChainComplex<ModNXi> cloneComplex = new UnifiedChainComplex<ModNXi>(
                    ses.getCComplex(), unit, false, false);
            cloneComplex.setBetaGens(beta);
            int rplus = cloneComplex.getrPlus(sinv, qmax, qmin, unit);
            invs.add(rplus);
        }
        beta = ses.getConnectingHom(true, true, -1, sinv-1);
        if (beta == null) invs.add(sinv);
        else {
            ArrayList<Integer> theQs = theComplex.getQs();
            int qmax = theQs.get(theQs.size()-1);
            int qmin = theQs.get(0);
            UnifiedChainComplex<ModNXi> cloneComplex = new UnifiedChainComplex<ModNXi>(
                    ses.getCComplex(), unit, false, false);
            cloneComplex.setBetaGens(beta);
            int splus = cloneComplex.getsPlus(sinv, qmax, qmin, unit);
            invs.add(splus);
        }
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
    
}
