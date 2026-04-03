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

package knotjob.homology.evenkhov.sinv;

import java.util.ArrayList;
import knotjob.Calculator;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.LinkData;
import knotjob.rings.BigInt;

/**
 *
 * @author Dirk
 */
public class GradedCalculator extends Calculator {
    
    public GradedCalculator(ArrayList<LinkData> lnkLst, Options optns, 
            DialogWrap frm) {
        super(lnkLst, optns, frm);
    }

    @Override
    protected boolean calculationRequired(LinkData theLink) {
        if (theLink.chosenLink().components()>1) return false;
        return theLink.grsinv == null;
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
        GradedInvariant<BigInt> gInv = new GradedInvariant<BigInt>(theLink, new BigInt(1), 
                frame, options);
        gInv.calculate();
        if (!abInf.isAborted()) {
            theLink.grsinv = gInv.getGInvariant();
            if (theLink.sInvariant(0) == null) theLink.setSInvariant(0, gInv.getSInvariant());
        }
    }

    @Override
    protected void adaptFrame() {
        
    }
    
    
}
