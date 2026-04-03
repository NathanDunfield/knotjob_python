/*

Copyright (C) 2024 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
public class SlTSInvariant<R extends Ring<R>> extends SpecSeqCalculation<R> {

    private final int sType;
    private int sInv;
    
    public SlTSInvariant(LinkData link, R unt, DialogWrap frm, Options opts, int c, boolean rd) {
        super(Reidemeister.freeOfOne(link.chosenLink()).breakUp().girthDiscMinimize(), frm, unt, 
                opts, rd);
        if (reduced) sType = 1;
        else if (c == 3) sType = 2;
        else sType = 3;
    }

    @Override
    public void calculate() {
        if (theLink.crossingNumber() == 0) {
            sInv = 0;
            return;
        }
        UnivSlTComplex<R> theComplex = getComplex();
        if (abInf.isAborted()) return;
        int jump = (-2) * sType;
        calcSpecSeq(theComplex, jump);
        sInv = theComplex.lastQDegree();
    }

    public int getSInvariant() {
        return sInv/2;
    }
    
    private UnivSlTComplex<R> getComplex() {
        UnivSlTComplex<R> theComplex = crossingComplex(0, null, false, 0, sType);
        int u = 1;
        int[] wrt = theLink.crossingSigns();
        int max = 1 + wrt[0];
        int min = -1 - wrt[1];
        if (theComplex.lowestHom() == -1) max--;
        else min++;
        int pos = 0;
        while (u < theLink.crossingLength() - 1 && !abInf.isAborted()) {
            boolean replace = this.girthDifference(u-1) == 4;
            if (replace) pos = getDiscPosition(u);
            UnivSlTComplex<R> nextComplex = crossingComplex(u, theComplex, replace, 
                    theLink.getPath(u, pos), sType);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            modifyComplex(theComplex, nextComplex, replace, u, theLink.getPath(u, pos), sType);
            theComplex.throwAway(min, max);
            if (nextComplex.lowestHom() == -1) max--;
            else min++;
            u++;
        }
        lastComplex(theComplex, u);
        return theComplex;
    }
    
    private void lastComplex(UnivSlTComplex<R> theComplex, int u) {
        UnivSlTComplex<R> nextComplex = crossingComplex(u, theComplex, true, theLink.getPath(u, theLink.basepoint()), sType);
        frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
        theComplex.modifyComplex(nextComplex, "2", highDetail);
        theComplex.throwAway(-1, 1);
        if (reduced) theComplex.finishOffRed("0", highDetail);
        else theComplex.finishOff("0", highDetail);
    }
    
}
