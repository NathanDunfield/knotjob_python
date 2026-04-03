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
public class UnivSlTHomCalculator extends Calculator {

    private final int chr;
    private final int sType;
    private final boolean red;
    
    public UnivSlTHomCalculator(ArrayList<LinkData> lnkLst, int val, int s, boolean r,
            Options optns, DialogWrap frm) {
        super(lnkLst, optns, frm);
        chr = val;
        sType = s;
        red = r;
    }
    
    @Override
    protected void setUpRestFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, true);
    }

    @Override
    protected boolean calculationRequired(LinkData theLink) {
        int typ = 2;
        if (sType == 2) typ = 3;
        if (sType == 1 && !red) typ = 4;
        if (sType == 1 && red) typ = 5;
        return theLink.bltInfo(String.valueOf(chr), typ) == null;
    }

    @Override
    protected void performCalculation(LinkData theLink) {
        ArrayList<String> theInfo;// = null;
        ArrayList<Integer> theBlocks;
        boolean csi = !(sType == 1 && !red) && theLink.chosenLink().components() == 1;
        int sInv;
        if (chr == 0) {
            if (sType != 3) csi = false;
            UnivSlThreeHom<BigRat> hom = new UnivSlThreeHom<BigRat>(theLink, sType, frame,
                    new BigRat(BigInteger.ONE), options, red, csi); 
            hom.calculate();
            theInfo = hom.getInfo();
            theBlocks = hom.getBlocks();
            sInv = hom.sInvariant();
        }
        else {
            if (chr == 2 && sType != 3) csi = false;
            if (chr == 3 && sType != 2) csi = false;
            UnivSlThreeHom<ModN> hom = new UnivSlThreeHom<ModN>(theLink, sType, frame,
                    new ModN(1, chr), options, red, csi); 
            hom.calculate();
            theInfo = hom.getInfo();
            theBlocks = hom.getBlocks();
            sInv = hom.sInvariant();
        }
        if (!abInf.isAborted()) {
            theLink.reLock.lock();
            try {
                theLink.wrapUpSlTSS(theInfo, theBlocks, red, chr, sType);
                if (csi) theLink.setSlTSInvariant(chr, sInv, false);
            }
            finally {
                theLink.reLock.unlock();
            }
        }
    }

    @Override
    protected void adaptFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, false);
        if (options.getGirthInfo() == 2) frame.setLabelLeft("h-Level : ", 3, false);
    }
    
}
