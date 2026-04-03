/*

Copyright (C) 2019-20 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.Link;
import knotjob.links.Reidemeister;
import knotjob.rings.ModN;

/**
 *
 * @author Dirk
 */
public class SqOneInvariant {
    
    private final Link theLink;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final Options options;
    private final ModN unit;
    private final int[] girth;
    private final boolean highDetail;
    private String sqinv;
    private int sinv;
    
    SqOneInvariant(Link link, DialogWrap frm, Options optns) {
        theLink = Reidemeister.freeOfOne(link);
        options = optns;
        girth = theLink.totalGirthArray();
        frame = frm;
        abInf = frame.getAbortInfo();
        unit = new ModN(1,4);
        highDetail = optns.getGirthInfo() == 2;
    }
    
    void calculate() {
        int[] wrt = theLink.crossingSigns();
        int hstart = -wrt[1];
        int qstart = wrt[0]+2*hstart;
        calculateSqOne(hstart,qstart);
    }

    private void calculateSqOne(int hstart, int qstart) {
        SInvComplex<ModN> theComplex = getComplex(hstart,qstart);
        if (!abInf.isAborted()) lipSarkize(theComplex);
    }
    
    private SInvComplex<ModN> getComplex(int hstart, int qstart) {
        if (theLink.crossingLength() == 0) return new SInvComplex<ModN>(theLink.unComponents(), unit, 
                true, false, abInf, null);
        if (theLink.crossingLength() == 1) return oneCrossingComplex(hstart, qstart);
        int tsum = totalSum(theLink.getCrossings());
        int ign = 2;
        SInvComplex<ModN> theComplex = 
                new SInvComplex<ModN>(theLink.getCross(0), theLink.getPath(0), hstart, qstart,
                        false, true, true, false, unit, frame, abInf);
        int u = 1;
        while (u < theLink.crossingLength()) {
            boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
                theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
            SInvComplex<ModN> nextComplex = new SInvComplex<ModN>(theLink.getCross(u),
                    theLink.getPath(u), 0, 0, orient, true, true, false, unit, null, null);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            theComplex.modifyComplex(nextComplex,0, girthInfo(u), highDetail);
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
    
    private SInvComplex<ModN> oneCrossingComplex(int hs, int qs) {
        SInvComplex<ModN> theComplex = new SInvComplex<ModN>(theLink.getCross(0), theLink.getPath(0),
                hs, qs, false, true, true, false, unit, frame, abInf);
        SInvComplex<ModN> unComp = new SInvComplex<ModN>(1, unit, false, true,abInf, frame);
        unComp.modifyComplex(theComplex,0,"0", highDetail);
        return unComp;
    }
    
    private void lipSarkize(SInvComplex<ModN> theComplex) {
        theComplex.slideTwoTorsion();
        ModN twoUnit = new ModN(1,2);
        SInvComplex<ModN> modTwoComplex = new SInvComplex<ModN>(theComplex, twoUnit, false);
        sinv = modTwoComplex.barnatize(1);
        ArrayList<Integer> theQs = theComplex.getQs();
        int qmax = theQs.get(theQs.size()-1);
        int qmin = theQs.get(0);
        SInvComplex<ModN> cloneComplex = new SInvComplex<ModN>(theComplex, unit, false);
        int rplus = cloneComplex.getrPlus(sinv, qmax, qmin, twoUnit);
        cloneComplex = new SInvComplex<ModN>(theComplex,unit,false);
        int splus = cloneComplex.getsPlus(sinv, qmax, qmin, twoUnit);
        cloneComplex = new SInvComplex<ModN>(theComplex,unit,true);
        int rminus = -cloneComplex.getrPlus(-sinv, -qmin, -qmax, twoUnit);
        cloneComplex = new SInvComplex<ModN>(theComplex,unit,true);
        int sminus = -cloneComplex.getsPlus(-sinv, -qmin, -qmax, twoUnit);
        sqinv = "("+rplus+", "+splus+", "+rminus+", "+sminus+")";// */
    }

    public String getInvariant() {
        return sqinv;
    }
    
    public int getSInvariant() {
        return sinv;
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
