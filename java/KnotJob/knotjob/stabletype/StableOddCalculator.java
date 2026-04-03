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
import knotjob.homology.oddkhov.OddKhovHomology;
import knotjob.links.LinkData;
import knotjob.rings.BigInt;
import knotjob.rings.ModN;

/**
 *
 * @author Dirk
 */
public class StableOddCalculator extends Calculator {

    private final int deleps;
    
    public StableOddCalculator(ArrayList<LinkData> lnkLst, Options optns, 
            DialogWrap wrap, int epsdel) {
        super(lnkLst, optns, wrap);
        deleps = epsdel;
    }

    @Override
    protected boolean calculationRequired(LinkData theLink) {
        String rel = theLink.stOdd;
        if (deleps == 2) rel = theLink.stOde;
        if (rel == null) return true;
        if ("trivial".equals(rel)) return false;
        return rel.contains("aborted"); 
    }

    @Override
    protected void setUpRestFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, true);
    }

    @Override
    protected void performCalculation(LinkData theLink) {
        ArrayList<Integer> relQs = theLink.getStableQs(true, false);
        if (relQs == null) {
            OddKhovHomology<BigInt> hom = new OddKhovHomology<BigInt>(theLink, 0, frame, 
                    options, new BigInt(1), null);
            hom.calculate();
            if (!abInf.isAborted()) theLink.wrapUpOddKhov(hom.getOddHomology(), 0, options);
            relQs = theLink.getStableQs(true, false);
        }
        if (relQs.size()>1) {
            int mpower = relQs.get(0);
            if (mpower == 1) mpower = 2;
            relQs.remove(0);
            frame.setTitleLabel(theLink.name, false);
            frame.setLabelLeft("Crossing : ", 0, false);
            frame.setLabelLeft("Girth : ", 1, false);
            frame.setLabelLeft("Objects : ", 2, false);
            StableOddInvariant<ModN> invariant = new StableOddInvariant<ModN>(theLink, 
                    new ModN(1, 2*mpower), new ModN(1,2), new ModN(1,4), frame, options, 
                    relQs, 2*(deleps-1));
            invariant.calculate();
            if (!abInf.isAborted()) {
                if (deleps == 1) {
                    theLink.stOdd = invariant.getInfo();
                    theLink.stOddInfo = invariant.getInfoStrings();
                }
                else {
                    theLink.stOde = invariant.getInfo();
                    theLink.stOdeInfo = invariant.getInfoStrings();
                }
            }
        }
        else {
            theLink.stOdd = "trivial";
            theLink.stOde = "trivial";
        }
    }

    @Override
    protected void adaptFrame() {
        
    }
    
}
