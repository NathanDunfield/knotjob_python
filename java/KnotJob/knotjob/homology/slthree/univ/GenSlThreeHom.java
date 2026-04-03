/*

Copyright (C) 2025 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.slthree.univ;

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
public class GenSlThreeHom<R extends Ring<R>> extends SpecSeqCalculation<R> {

    private final R t;
    private final int sType;
    //private final String name;
    
    public GenSlThreeHom(LinkData link, DialogWrap frm, R unt, R tt, int st, Options opts) {
        super(Reidemeister.freeOfOne(link.chosenLink()).breakUp().girthDiscMinimize(), 
                frm, unt, opts, false);
        t = tt;
        sType = st;
        //name = link.name;
    }

    @Override
    public void calculate() {
        GenSlTComplex<R> theComplex = getComplex();
        if (abInf.isAborted()) return;
        theComplex.finishOff("0", highDetail);
        //if (!abInf.isAborted() && !theComplex.boundaryCheck()) System.out.println(name);
        calcSpecSeq(theComplex, -2);
    }
    
    public void polyCalc() { //somewhat experimental
        GenSlTComplex<R> theComplex = getComplex();
        if (abInf.isAborted()) return;
        theComplex.finishOff("0", highDetail);
        //if (!abInf.isAborted() && !theComplex.boundaryCheck()) System.out.println(name);
        calcSpecSeq(theComplex, -2);
    }
    
    private GenSlTComplex<R> getComplex() {
        if (theLink.crossingLength() == 0) return unlinkComplex();
        GenSlTComplex<R> theComplex = crossingComplex(0, null, false, 0, sType, t);
        int u = 1;
        int pos = 0;
        while (u < theLink.crossingLength() - 1 && !abInf.isAborted()) {
            boolean replace = this.girthDifference(u-1) == 4;
            if (replace) pos = getDiscPosition(u);
            GenSlTComplex<R> nextComplex = crossingComplex(u, theComplex, replace, 
                    theLink.getPath(u, pos), sType, t);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            modifyComplex(theComplex, nextComplex, replace, u, theLink.getPath(u, pos), sType);
            u++;
        }
        lastComplex(theComplex, u);
        return theComplex;
    }
    
    private GenSlTComplex<R> unlinkComplex() {
        GenSlTComplex<R> complex = new GenSlTComplex<R>(unit, t, frame, abInf, 0, 1, 2);
        int c = theLink.unComponents();
        if (c == 1) return complex;
        GenSlTComplex<R> nextComplex = new GenSlTComplex<R>(false, unit, t, null, null);
        while (c > 1) {
            complex.modifyComplex(nextComplex, "", false);
            c--;
        }
        return complex;
    }
    
    private void lastComplex(GenSlTComplex<R> theComplex, int u) {
        GenSlTComplex<R> nextComplex = crossingComplex(u, theComplex, true, 
                theLink.getPath(u, theLink.basepoint()), sType, t);
        frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
        theComplex.modifyComplex(nextComplex, "2", highDetail);
    }
    
}
