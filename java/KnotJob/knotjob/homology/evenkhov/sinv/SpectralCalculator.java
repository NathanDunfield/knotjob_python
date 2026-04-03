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
public class SpectralCalculator extends Calculator {
    
    private final int characteristic;
    private final boolean reduced;
    
    public SpectralCalculator(ArrayList<LinkData> lnkLst, Options optns, 
            DialogWrap frm, int chr, boolean red) {
        super(lnkLst, optns, frm);
        characteristic = chr;
        reduced = red;
    }

    @Override
    protected boolean calculationRequired(LinkData theLink) {
        int typ = 0;
        if (reduced) typ = 1;
        return theLink.bltInfo(String.valueOf(characteristic), typ) == null;
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
            ArrayList<String> theInfo;// = null;
            ArrayList<Integer> theBlocks;
            if (characteristic == 0) {
                SpectralInvariant<BigRat> spInv = new SpectralInvariant<BigRat>(theLink, 
                        new BigRat(BigInteger.ONE), frame, options, reduced, !reduced);
                spInv.calculate();
                theInfo = spInv.getInfo();
                theBlocks = spInv.getBlocks();
            }
            else {
                SpectralInvariant<ModN> spInv = new SpectralInvariant<ModN>(theLink,
                        new ModN(1, characteristic), frame, options, reduced, 
                        !reduced & characteristic != 2);
                spInv.calculate();
                theInfo = spInv.getInfo();
                theBlocks = spInv.getBlocks();
            }
            if (!abInf.isAborted()) {
                theLink.wrapUpBLT(theInfo, theBlocks, reduced, characteristic);
            }
        }
        finally {
            theLink.reLock.unlock();
        }
    }

    @Override
    protected void adaptFrame() {
        
    }
    
}
