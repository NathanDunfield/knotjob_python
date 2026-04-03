/*

Copyright (C) 2023-24 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.slthree;

import java.math.BigInteger;
import java.util.ArrayList;
import knotjob.Calculator;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import static knotjob.homology.evenkhov.EvenKhovCalculator.getPrimes;
import knotjob.links.LinkData;
import knotjob.rings.BigInt;
import knotjob.rings.BigRat;
import knotjob.rings.LocalP;
import knotjob.rings.ModN;

/**
 *
 * @author Dirk
 */
public class SlThreeHomCalculator extends Calculator {

    private final long coeff;
    
    public SlThreeHomCalculator(ArrayList<LinkData> lnkLst, long val, Options optns, DialogWrap frm) {
        super(lnkLst, optns, frm);
        coeff = val;
    }
    
    @Override
    protected void setUpRestFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, true);
    }

    @Override
    protected boolean calculationRequired(LinkData theLink) {
        if (theLink.integralSlTExists()) return false;
        if (coeff == 0) return true;
        return !(coeff == 1 && theLink.rationalSlTExists());
    }

    @Override
    protected void performCalculation(LinkData theLink) {
        theLink.reLock.lock();
        try {
            if (coeff == 0) {
                SlThreeHomology<BigInt> hom = new SlThreeHomology<BigInt>(theLink, coeff, frame,
                        false, options, new BigInt(1), null); 
                hom.calculate();
                if (!abInf.isAborted()) wrapUp(theLink, hom.getUnreduced(), hom.getReduced());
            }
            if (coeff == 1) {
                SlThreeHomology<BigRat> hom = new SlThreeHomology<BigRat>(theLink, coeff, frame,
                        false, options, new BigRat(BigInteger.ONE), null); 
                hom.calculate();
                if (!abInf.isAborted()) wrapUp(theLink, hom.getUnreduced(), hom.getReduced());
            }
            if (coeff >= 2) {
                int coff = (int) coeff;
                SlThreeHomology<ModN> hom = new SlThreeHomology<ModN>(theLink, coeff, frame,
                        false, options, new ModN(1, coff), new ModN(primeOf(coff), coff)); 
                hom.calculate();
                if (!abInf.isAborted()) wrapUp(theLink, hom.getUnreduced(), hom.getReduced());
            }
            if (coeff < 0) {
                ArrayList<Integer> prms = getPrimes(coeff, options.getPrimes());
                int[] primes = new int[prms.size()];
                for (int i = 0; i < prms.size(); i++) primes[i] = prms.get(i);
                SlThreeHomology<LocalP> hom = new SlThreeHomology<LocalP>(theLink, coeff, frame,
                        false, options, new LocalP(BigInteger.ONE, primes), null); 
                hom.calculate();
                if (!abInf.isAborted()) wrapUp(theLink, hom.getUnreduced(), hom.getReduced());
            }
        }
        finally {
            theLink.reLock.unlock();
        }
    }

    @Override
    protected void adaptFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, false);
        if (options.getGirthInfo() == 2) frame.setLabelLeft("h-Level : ", 3, false);
    }

    private void wrapUp(LinkData theLink, ArrayList<String> unred, ArrayList<String> red) {
        theLink.wrapUpSlT(unred, red, coeff, options);
        
    }
    
    private int primeOf(int ring) {
        int prime = 2;
        boolean found = false;
        while (!found) {
            if (ring % prime == 0) found = true;
            else prime++;
        }
        return prime;
    }
    
}
