/*

Copyright (C) 2020-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import knotjob.Calculator;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class SqOneOddCalculator extends Calculator {
    
    private final int mod;
    
    public SqOneOddCalculator(ArrayList<LinkData> lnkLst, Options optns, DialogWrap frm, int md) {
        super(lnkLst, optns, frm);
        mod = md;
    }
    
    public SqOneOddCalculator(ArrayList<LinkData> lnkLst, Options optns, DialogWrap frm) {
        super(lnkLst, optns, frm);
        mod = 4;
    }
    
    @Override
    protected boolean calculationRequired(LinkData theLink) {
        if (theLink.chosenLink().components()>1) return false;
        if (mod == 4) return (theLink.sqOdd == null);
        return (theLink.bsOdd == null);
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
        SqOneOddInvariant sqInv = new SqOneOddInvariant(theLink,
                frame, options, mod);
        sqInv.calculate();
        if (!abInf.isAborted()) {
            theLink.setSInvariant(2, sqInv.getSInvariant());
            if (mod == 4) theLink.sqOdd = sqInv.getInvariant();
            else theLink.bsOdd = sqInv.getInvariant();
        }
    }

    @Override
    protected void adaptFrame() {
        
    }
    
}
