/*

Copyright (C) 2022-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.stabletype;

import java.util.ArrayList;
import knotjob.Calculator;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.evenkhov.EvenKhovHomology;
import knotjob.links.LinkData;
import knotjob.rings.BigInt;
import knotjob.rings.ModN;

/**
 *
 * @author Dirk
 */
public class StableEvenCalculator extends Calculator {
    
    public StableEvenCalculator(ArrayList<LinkData> lnkLst, Options optns, 
            DialogWrap wrap) {
        super(lnkLst, optns, wrap);
    }
    
    /*@Override
    public void run() {
        long start = System.nanoTime();
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, false);
        for (LinkData theLink : linkList) {
            frame.setTitleLabel(theLink.name, true);
            if (calculationRequired(theLink)) {
                ArrayList<Integer> relQs = theLink.getStableQs(false, false);
                if (relQs == null) {
                    theLink.reLock.lock();
                    try {
                        EvenKhovHomology<BigInt> hom = new EvenKhovHomology<BigInt>(theLink, 0, frame,
                                true, true, options, new BigInt(1), null);
                        hom.calculate();
                        if (!abInf.isAborted()) theLink.wrapUpEvenKhov(hom.getUnreduced(), 
                                hom.getReduced(), 0, options);
                    }
                    finally {
                        theLink.reLock.unlock();
                        relQs = theLink.getStableQs(false, false);
                    }
                }
                if (relQs.size()>1) {
                    int mpower = relQs.get(0);
                    if (mpower == 1) mpower = 2;
                    relQs.remove(0);
                    frame.setTitleLabel(theLink.name, false);
                    frame.setLabelLeft("Crossing : ", 0, false);
                    frame.setLabelLeft("Girth : ", 1, false);
                    frame.setLabelLeft("Objects : ", 2, false);
                    StableEvenInvariant<ModN> invariant = new StableEvenInvariant<ModN>(theLink, 
                            new ModN(1, 2*mpower), new ModN(1,2), new ModN(1,4), frame, options, relQs);
                    invariant.calculate();
                    if (!abInf.isAborted()) {
                        theLink.stEven = invariant.getInfo();
                        theLink.stEvenInfo = invariant.getInfoStrings();
                    }
                }
                else theLink.stEven = "trivial";
            }
            if (abInf.isCancelled()) break;
            if (abInf.isAborted()) abInf.deAbort();
        }
        long end = System.nanoTime();
        frame.dispose();
        if (options.getTimeInfo()) {
            TimerDialog dialog = new TimerDialog(frame.getFrame(), "Calculation Time", true, end - start);
            dialog.setup();
        }
    }// */

    @Override
    protected boolean calculationRequired(LinkData theLink) {
        if (theLink.stEven == null) return true;
        if ("trivial".equals(theLink.stEven)) return false;
        return theLink.stEven.contains("aborted"); 
    }

    @Override
    protected void setUpRestFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, true);
    }

    @Override
    protected void performCalculation(LinkData theLink) {
        ArrayList<Integer> relQs = theLink.getStableQs(false, false);
        if (relQs == null) {
            theLink.reLock.lock();
            try {
                EvenKhovHomology<BigInt> hom = new EvenKhovHomology<BigInt>(theLink, 0, frame,
                        true, true, options, new BigInt(1), null);
                hom.calculate();
                if (!abInf.isAborted()) theLink.wrapUpEvenKhov(hom.getUnreduced(), 
                        hom.getReduced(), 0, options);
            }
            finally {
                theLink.reLock.unlock();
                relQs = theLink.getStableQs(false, false);
            }
        }
        if (relQs.size()>1) {
            int mpower = relQs.get(0);
            if (mpower == 1) mpower = 2;
            relQs.remove(0);
            frame.setTitleLabel(theLink.name, false);
            frame.setLabelLeft("Crossing : ", 0, false);
            frame.setLabelLeft("Girth : ", 1, false);
            frame.setLabelLeft("Objects : ", 2, false);
            StableEvenInvariant<ModN> invariant = new StableEvenInvariant<ModN>(theLink, 
                    new ModN(1, 2*mpower), new ModN(1,2), new ModN(1,4), frame, options, relQs);
            invariant.calculate();
            if (!abInf.isAborted()) {
                theLink.stEven = invariant.getInfo();
                theLink.stEvenInfo = invariant.getInfoStrings();
            }
        }
        else theLink.stEven = "trivial";
    }

    @Override
    protected void adaptFrame() {
        
    }
    
}
