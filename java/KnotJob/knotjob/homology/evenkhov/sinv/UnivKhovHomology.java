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

package knotjob.homology.evenkhov.sinv;

import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.ChainComplex;
import knotjob.homology.Homology;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class UnivKhovHomology <R extends Ring<R>> {
    
    private final Link theLink;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final Options options;
    private final int[] girth;
    private final R unit;
    private final R theH;
    private final boolean highDetail;
    
    public UnivKhovHomology(LinkData thLnk, R unt, R h, DialogWrap frm, Options optns) {
        theLink = Reidemeister.freeOfOne(thLnk.chosenLink()).breakUp().girthDiscMinimize();
        girth = theLink.totalGirthArray();
        frame = frm;
        abInf = frame.getAbortInfo();
        options = optns;
        unit = unt;
        theH = h;
        highDetail = options.getGirthInfo() == 2;
    }
    
    public void calculate() {
        int[] wrt = theLink.crossingSigns();
        int hstart = -wrt[1];
        int qstart = wrt[0]+2*hstart;
        calculateUniversal(hstart, qstart);
    }

    private void calculateUniversal(int hstart, int qstart) {
        UnivComplex<R> theComplex = getComplex(hstart, qstart);
        //theComplex.output();
        ArrayList<String> theInfo = smithNormalize(theComplex);
        //theComplex.output();
    }
    
    private UnivComplex<R> getComplex(int hs, int qs) {
        if (theLink.crossingLength() == 0) return new UnivComplex<R>(0, unit, true, false, 
                abInf, null, theH);
        UnivComplex<R> theComplex = firstComplex(hs, qs);
        int u = 1;
        while (u < theLink.crossingLength() && !abInf.isAborted()) {
            boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| 
                    theComplex.negContains(theLink.getPath(u, 2))|
                    theComplex.posContains(theLink.getPath(u, 1)) | 
                    theComplex.posContains(theLink.getPath(u, 3)));
                UnivComplex<R> nextComplex = new UnivComplex<R>(theLink.getCross(u),
                        theLink.getPath(u), 0, 0, theH, orient, false, true,
                        false, unit, null, null);
                frame.setLabelRight(String.valueOf(u+1)+"/"+
                        String.valueOf(theLink.crossingLength()), 0, false);
                if (u < theLink.crossingLength() - 1) 
                    theComplex.modifyComplex(nextComplex, 0, girthInfo(u), highDetail);
                else theComplex.lastModification(nextComplex, girthInfo(u), highDetail);
                u++;
        }
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
    
    private UnivComplex<R> firstComplex(int hs, int qs) {
        UnivComplex<R> theComplex = 
                new UnivComplex<R>(theLink.getCross(0), theLink.getPath(0), hs, qs,
                        theH, false, true, true, false, unit, frame, abInf);
        return theComplex;
    }
    
    private ArrayList<String> smithNormalize(UnivComplex<R> theComplex) {
        if (abInf.isAborted()) return null;
        if (highDetail) {
            frame.setLabelLeft(" ", 3, false);
            frame.setLabelRight(" ", 3, false);
        }
        ChainComplex<R> complex = theComplex.getComplex();
        ArrayList<Homology> homologies = complex.smithNormalize(null);
        for (Homology hom : homologies) {
            System.out.println("H^"+hom.hdeg()+" = "+hom.getBetti()+" "+hom.getTorsion());
        }
        return null;
    }
    
}
