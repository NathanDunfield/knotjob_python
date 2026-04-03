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

import java.util.ArrayList;
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
public class LinkSlTSInvariant<R extends Ring<R>> extends SpecSeqCalculation<R> {

    private final int sType;
    private int sInv;
    
    public LinkSlTSInvariant(LinkData link, R unt, DialogWrap frm, Options opts, int c, boolean rd) {
        super(Reidemeister.freeOfOne(link.chosenLink().mirror()).breakUp().girthDiscMinimize(), frm, unt, 
                opts, rd);
        if (reduced) sType = 1;
        else if (c == 3) sType = 2;
        else sType = 3;
    }

    @Override
    public void calculate() {
        LinkSlTComplex<R> theComplex = getComplex();
        if (abInf.isAborted()) return;
        if (!theComplex.cocycleCheck()) throw new UnsupportedOperationException("Not a cocycle (LinkSlTSInvariant.java calculate).");
        theComplex.specSeq(-2 * sType);
        sInv = -(theComplex.maximalImageQs() - 2)/2 - theLink.unComponents();
    }

    public int getSInvariant() {
        return sInv;
    }

    private LinkSlTComplex<R> getComplex() {
        LinkSlTComplex<R> theComplex = crossingComplex(0, null, 0, sType, false);
        int u = 1;
        int[] wrt = theLink.crossingSigns();
        int max = 0 + wrt[0];
        int min = -1 - wrt[1];
        if (theComplex.lowestHom() == -1) max--;
        else min++;
        int pos = 0;
        while (u < theLink.crossingLength() - 1 && !abInf.isAborted()) {
            boolean replace = this.girthDifference(u-1) == 4;
            if (replace) pos = getDiscPosition(u);
            LinkSlTComplex<R> nextComplex = crossingComplex(u, theComplex, 
                    theLink.getPath(u, pos), sType, replace);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            modifyComplex(theComplex, nextComplex, replace, u, theLink.getPath(u, pos), sType);
            theComplex.throwAway(min, max);
            if (nextComplex.lowestHom() == -1) max--;
            else min++;
            u++;
        }
        if (!abInf.isAborted()) lastComplex(theComplex, u);
        return theComplex;
    }
    
    private void modifyComplex(LinkSlTComplex<R> theComplex, LinkSlTComplex<R> nextComplex,
            boolean replace, int u, int old, int sType) {
        theComplex.modifyComplex(nextComplex, girthInfo(u), highDetail);
        if (replace) {
            ArrayList<Integer> lEndpts = theComplex.getLastEndpoints();
            ArrayList<Integer> nEndpts = nextComplex.getLastEndpoints();
            int replacement = missingValue(nEndpts, theLink.getPath(u));
            if (lEndpts.indexOf(old) < lEndpts.indexOf(replacement)) {
                int help = old;
                old = replacement;
                replacement = help;
            }
            LinkSlTComplex<R> finComplex = new LinkSlTComplex<R>(unit, frame, abInf, 
                    sType, old, replacement);
            theComplex.modifyComplex(finComplex, girthInfo(u), highDetail);
        }
    }
    
    private void lastComplex(LinkSlTComplex<R> theComplex, int u) {
        LinkSlTComplex<R> nextComplex = crossingComplex(u, theComplex, theLink.getPath(u, theLink.basepoint()), sType, true);
        frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
        theComplex.modifyComplex(nextComplex, "2", highDetail);
        theComplex.throwAway(-1, 1);
        if (reduced) theComplex.finishOffRed("0", highDetail);
        else theComplex.finishOff("0", highDetail);
    }
    
}
