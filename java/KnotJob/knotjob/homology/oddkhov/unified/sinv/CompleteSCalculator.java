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

package knotjob.homology.oddkhov.unified.sinv;

import java.util.ArrayList;
import knotjob.Calculator;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.evenkhov.sinv.GradedInvariant;
import knotjob.links.LinkData;
import knotjob.rings.BigInt;

/**
 *
 * @author Dirk
 */
public class CompleteSCalculator extends Calculator {

    private final boolean reduced;
    
    public CompleteSCalculator(ArrayList<LinkData> lnkLst, Options optns, DialogWrap frm, boolean red) {
        super(lnkLst, optns, frm);
        reduced = red;
    }
    
    @Override
    protected void setUpRestFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, true);
        if (options.getGirthInfo() == 2) frame.setLabelLeft("h-Level : ", 3, false);
    }

    @Override
    protected boolean calculationRequired(LinkData theLink) {
        if (theLink.chosenLink().components()>1) return false;
        if (reduced) return (theLink.cmpinv == null);
        return false;
    }

    @Override
    protected void performCalculation(LinkData theLink) {
        if (!reduced) {
            if (theLink.grsinv == null) {
                GradedInvariant<BigInt> gInv = new GradedInvariant<BigInt>(theLink, new BigInt(1), 
                        frame, options);
                gInv.calculate();
                if (!abInf.isAborted()) {
                    theLink.grsinv = gInv.getGInvariant();
                    if (theLink.sInvariant(0) == null) theLink.setSInvariant(0, gInv.getSInvariant());
                }
                else return;
            }
            if (theLink.grsinv.contains("(")) {
                //theLink.cuninv = "non-trivial";
                return;
            }
        }
        CompleteSInvariant inv = new CompleteSInvariant(theLink, frame, options, reduced);
        inv.calculate();
        if (abInf.isAborted()) return;
        if (reduced) theLink.cmpinv = inv.getCInvariant();
        //else theLink.cuninv = inv.getCInvariant();
        if (theLink.grsinv == null) {
            String str = inv.getGradedInvariant();
            theLink.grsinv = str;
            int p = str.indexOf(".");
            str = str.substring(0, p);
            p = str.indexOf(" (");
            if (p > 0) str = str.substring(0, p);
            theLink.setSInvariant(0, Integer.parseInt(str));
        }
    }

    @Override
    protected void adaptFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, true);
        if (options.getGirthInfo() == 2) frame.setLabelLeft("h-Level : ", 3, false);
    }
    
}
