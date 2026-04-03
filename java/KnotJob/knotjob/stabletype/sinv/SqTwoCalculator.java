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

package knotjob.stabletype.sinv;

import java.util.ArrayList;
import knotjob.Calculator;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.evenkhov.EvenKhovHomology;
import knotjob.homology.evenkhov.sinv.SInvariant;
import knotjob.links.CombMinimizer;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.rings.BigInt;
import knotjob.rings.ModN;

/**
 *
 * @author Dirk
 */
public class SqTwoCalculator extends Calculator {
    
    private final int botherBound;
    private final int counterMax;
    private final boolean improve;
    
    public SqTwoCalculator(ArrayList<LinkData> lnkLst, Options optns, DialogWrap frm, 
            int bound, int max, boolean impr) {
        super(lnkLst, optns, frm);
        botherBound = bound;
        counterMax = max;
        improve = impr;
    }
    
    @Override
    protected boolean calculationRequired(LinkData theLink) {
        if (theLink.chosenLink().components()>1) return false;
        return (theLink.sqtEven == null);
    }

    @Override
    protected void setUpRestFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, true);
        if (options.getGirthInfo() == 2) frame.setLabelLeft("h-Level : ", 3, false);
    }

    @Override
    protected void performCalculation(LinkData theLink) {
        theLink.reLock.lock();
        try {
            if (!theLink.integralHomologyExists(false)) {
                EvenKhovHomology<BigInt> hom = new EvenKhovHomology<BigInt>(theLink, 0, frame,
                    true, true, options, new BigInt(1), null);
                hom.calculate();
                if (!abInf.isAborted()) theLink.wrapUpEvenKhov(hom.getUnreduced(), 
                    hom.getReduced(), 0, options);
            }
            if (theLink.sInvariant(2) == null) {
                SInvariant<ModN> sInv = new SInvariant<ModN>(theLink, new ModN(1, 2), 
                            frame, options);
                sInv.calculate();
                if (abInf.isAborted()) return;
                theLink.setSInvariant(2, sInv.getSInvariant());
            }
        }
        finally {
            theLink.reLock.unlock();
        }
        int cases = theLink.getRelevant(theLink.sInvariant(2));
        if (abInf.isAborted()) return;
        CombMinimizer comi = new CombMinimizer(theLink, counterMax, true);
        if (improve) {
            Link minLink = comi.getMinimized();
            if (minLink.crossingLength() < theLink.chosenLink().crossingLength()) {
                theLink.links.add(minLink);
                theLink.setChosen(theLink.links.size()-1);
            }
        }
        if (cases > 0 && botherBound < theLink.chosenLink().crossingLength()) return;
        SqTwoInvariant sqInv = new SqTwoInvariant(theLink.chosenLink(), 
                theLink.sInvariant(2), cases, frame, options);
        sqInv.calculate();
        if (!abInf.isAborted()) theLink.sqtEven = sqInv.getInvariant();
    }

    @Override
    protected void adaptFrame() {
        
    }
}
