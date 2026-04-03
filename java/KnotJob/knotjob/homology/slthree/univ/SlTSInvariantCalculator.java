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

import java.math.BigInteger;
import java.util.ArrayList;
import knotjob.Calculator;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.LinkData;
import knotjob.rings.BigRat;
import knotjob.rings.ModN;

/**
 *
 * @author Dirk
 */
public class SlTSInvariantCalculator  extends Calculator {

    private final int field;
    private final boolean red;
    
    public SlTSInvariantCalculator(ArrayList<LinkData> lnkLst, int val, boolean rd, Options optns, 
            DialogWrap frm) {
        super(lnkLst, optns, frm);
        field = val;
        red = rd;
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
        //if (theLink.chosenLink().components()>1) return false;
        int[][] sinvs = theLink.sSlTInvariants(red);
        boolean found = false;
        int i = 0;
        while (!found && i < sinvs.length) {
            if (sinvs[i][0] == field) found = true;
            else i++;
        }
        return !found;
    }

    @Override
    protected void performCalculation(LinkData theLink) {
        if (/*field <= 5 ||*/ theLink.chosenLink().components()-theLink.chosenLink().unComponents() > 1) 
            performLinkCalculation(theLink);
        else performKnotCalculation(theLink);
    }

    private void performLinkCalculation(LinkData theLink) {
        if (field > 1) {
            LinkSlTSInvariant<ModN> sInv = new LinkSlTSInvariant<ModN>(theLink, new ModN(1, field), 
                    frame, options, field, red);
            sInv.calculate();
            if (!abInf.isAborted()) theLink.setSlTSInvariant(field, sInv.getSInvariant(), red);
        }
        else {
            LinkSlTSInvariant<BigRat> sInv = new LinkSlTSInvariant<BigRat>(theLink, 
                    new BigRat(BigInteger.ONE), frame, options, field, red);
            sInv.calculate();
            if (!abInf.isAborted()) theLink.setSlTSInvariant(field, sInv.getSInvariant(), red);
        }
    }
    
    private void performKnotCalculation(LinkData theLink) {
        if (field > 1) {
            SlTSInvariant<ModN> sInv = new SlTSInvariant<ModN>(theLink, new ModN(1, field), 
                    frame, options, field, red);
            sInv.calculate();
            if (!abInf.isAborted()) theLink.setSlTSInvariant(field, sInv.getSInvariant(), red);
        }
        else {
            SlTSInvariant<BigRat> sInv = new SlTSInvariant<BigRat>(theLink, 
                    new BigRat(BigInteger.ONE), frame, options, field, true);
            sInv.calculate();
            if (!abInf.isAborted()) theLink.setSlTSInvariant(field, sInv.getSInvariant(), red);
        }
    }

    @Override
    protected void adaptFrame() {
        
    }
    
}
