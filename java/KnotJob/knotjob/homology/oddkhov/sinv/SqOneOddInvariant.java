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

package knotjob.homology.oddkhov.sinv;

import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.rings.ModN;

/**
 *
 * @author Dirk
 */
public class SqOneOddInvariant {
    
    private final Link theLink;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final ModN unit;
    private final int[] girth;
    private final boolean highDetail;
    private final int mod;
    private String sqinv;
    private int sinv;
    
    public SqOneOddInvariant(LinkData linkData, DialogWrap frm, Options optns) {
        theLink = Reidemeister.freeOfOne(linkData.chosenLink()).breakUp().girthDiscMinimize();
        girth = theLink.totalGirthArray();
        frame = frm;
        abInf = frame.getAbortInfo();
        mod = 4;
        unit = new ModN(1, mod);
        highDetail = optns.getGirthInfo() == 2;
    }
    
    public SqOneOddInvariant(LinkData linkData, DialogWrap frm, Options optns, int md) {
        theLink = Reidemeister.freeOfOne(linkData.chosenLink()).breakUp().girthDiscMinimize();
        girth = theLink.totalGirthArray();
        frame = frm;
        abInf = frame.getAbortInfo();
        mod = md;
        unit = new ModN(1, mod);
        highDetail = optns.getGirthInfo() == 2;
    }
    
    void calculate() {
        int[] wrt = theLink.crossingSigns();
        int hstart = -wrt[1];
        int qstart = wrt[0]+2*hstart;
        calculateSqOneOdd(hstart,qstart);
    }

    private void calculateSqOneOdd(int hstart, int qstart) {
        SOddComplex<ModN> theComplex = getComplex(hstart,qstart);
        if (!abInf.isAborted()) lipSarkize(theComplex);
    }

    private SOddComplex<ModN> getComplex(int hstart, int qstart) {
        if (theLink.crossingLength() == 0) return new SOddComplex<ModN>(theLink.unComponents(), unit, 
                abInf, null);
        if (theLink.crossingLength() == 1) return oneCrossingComplex(hstart, qstart);
        int tsum = totalSum(theLink.getCrossings());
        int ign = 2;
        SOddComplex<ModN> theComplex = 
                new SOddComplex<ModN>(theLink.getCross(0), theLink.getPath(0), hstart, qstart,
                        false, unit, frame, abInf);
        theComplex.setClosure(theLink);
        int u = 1;
        while (u < theLink.crossingLength()) {
            boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
                theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
            SOddComplex<ModN> nextComplex = new SOddComplex<ModN>(theLink.getCross(u),
                    theLink.getPath(u), 0, 0, orient, unit, null, null);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            theComplex.modifyComplex(nextComplex, girthInfo(u), highDetail);
            theComplex.throwAway(tsum-ign+2,ign);
            if (theLink.getCross(u) < 0) tsum = tsum - theLink.getCross(u);
            else tsum = tsum + theLink.getCross(u);
            u++;
        }
        return theComplex;
    }

    private void lipSarkize(SOddComplex<ModN> theComplex) {
        /* //Here we can check whether the complex still gets the right Khovanov homology mod 4
        
        theComplex.forgetBarNatan();
        ArrayList<Integer> relevantQs = theComplex.getQs();
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        for (int q : relevantQs) {
            frame.setLabelRight("" + q, 0);
            ChainComplex<ModN> qComplex = theComplex.getQComplex(q);
            //if (q == 5) qComplex.output();
            if (!qComplex.boundaryCheck()) {
                System.out.println("Boundary Error "+q);
            }
            QuantumCohomology qCoh = new QuantumCohomology(q+1, qComplex.modNormalize(new ModN(2, 4)));
            cohoms.add(qCoh);//System.out.println(qCoh);
        }// */
        
        if (mod == 4) theComplex.slideTwoTorsion();
        else theComplex.slideTwoPowerTorsion();
        
        //theComplex.output();
        
        ModN twoUnit = new ModN(1,2);
        SOddComplex<ModN> modTwoComplex = new SOddComplex<ModN>(theComplex, twoUnit, false);
        
        /* //Here we can check whether the complex mod 2 has two generators in hdeg 0 only.
        //System.out.println("Here");
        ChainComplex<ModN> chainComplex = modTwoComplex.getComplex();
        if (!chainComplex.boundaryCheck()) System.out.println("Error");
        ArrayList<Homology> homs = chainComplex.smithNormalize(new int[0]);
        for (Homology hom : homs) {
            if (hom.hdeg() == 0 && hom.getBetti() != 2) 
        System.out.println(hom.getBetti()+" in "+hom.hdeg());
            if (hom.hdeg() != 0 && hom.getBetti()>0)
                    System.out.println(hom.getBetti()+" in "+hom.hdeg());
        }// */
        
        sinv = modTwoComplex.barnatize(1);
        ArrayList<Integer> theQs = theComplex.getQs();
        int qmax = theQs.get(theQs.size()-1);
        int qmin = theQs.get(0);
        SOddComplex<ModN> cloneComplex = new SOddComplex<ModN>(theComplex, unit, false);
        int rplus = cloneComplex.getrPlus(sinv, qmax, qmin, twoUnit);
        //cloneComplex = new SOddComplex<ModN>(theComplex, unit, false);
        //int splus = cloneComplex.getsPlus(sinv, qmax, qmin, twoUnit);
        cloneComplex = new SOddComplex<ModN>(theComplex, unit, true);
        int rminus = -cloneComplex.getrPlus(-sinv, -qmin, -qmax, twoUnit);
        //cloneComplex = new SOddComplex<ModN>(theComplex, unit, true);
        //int sminus = -cloneComplex.getsPlus(-sinv, -qmin, -qmax, twoUnit);
        sqinv = "("+rplus+", "+rminus+")";
        //sqinv = "("+rplus+", "+splus+", "+rminus+", "+sminus+")"; // */
    }
    
    String getInvariant() {
        return sqinv;
    }
    
    int getSInvariant() {
        return sinv;
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
    
    private SOddComplex<ModN> oneCrossingComplex(int hs, int qs) {
        SOddComplex<ModN> theComplex = new SOddComplex<ModN>(theLink.getCross(0), theLink.getPath(0),
                hs, qs, false, unit, frame, abInf);
        SOddComplex<ModN> unComp = new SOddComplex<ModN>(1, unit,abInf, frame);
        unComp.modifyComplex(theComplex, "0", highDetail);
        return unComp;
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
