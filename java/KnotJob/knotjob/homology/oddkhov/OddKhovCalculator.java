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

package knotjob.homology.oddkhov;

import java.math.BigInteger;
import java.util.ArrayList;
import knotjob.Calculator;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.evenkhov.EvenKhovCalculator;
import knotjob.links.LinkData;
import knotjob.rings.BigInt;
import knotjob.rings.BigRat;
import knotjob.rings.LocalP;
import knotjob.rings.ModN;

/**
 *
 * @author dirk
 */
public class OddKhovCalculator extends Calculator {
    
    private final long coeff;
    
    public OddKhovCalculator(ArrayList<LinkData> lnkLst, long val, Options optns, DialogWrap frm) {
        super(lnkLst, optns, frm);
        coeff = val;
    }
    
    @Override
    protected boolean calculationRequired(LinkData theLink) {//will be a check similar to with even Khov 
        if (theLink.integralOddHomologyExists()) return false;
        if (coeff == 0) return true;
        if (coeff < 0) {
            ArrayList<Integer> primes = EvenKhovCalculator.getPrimes(coeff,options.getPrimes());
            return noBetterCalculation(primes,theLink);
        }
        if (coeff == 1 && theLink.rationalOddHomologyExists()) return false;
        if (coeff == 1) return true;
        int[] pap = primeAndPower((int) coeff);
        return noBetterCalculation(pap,theLink);
    }

    private boolean noBetterCalculation(int[] pap, LinkData theLink) {
        if (theLink.okhovInfo == null) return true;
        for (String info : theLink.okhovInfo) {
            long rng = Long.parseLong(info.substring(1, info.indexOf('.')));
            if (rng < 0 && info.indexOf('a') == -1) {
                if (!EvenKhovCalculator.getPrimes(rng,options.getPrimes()).contains(pap[0])) return false;
            }
            if (rng > 1 && info.indexOf('a') == -1) {
                if (rng % pap[0] == 0 && rng / pap[0] >= pap[1]) return false;
            }
        }
        return true;
    }
    
    private boolean noBetterCalculation(ArrayList<Integer> primes, LinkData theLink) {
        if (theLink.okhovInfo == null) return true;
        ArrayList<ArrayList<Integer>> altPrimes = new ArrayList<ArrayList<Integer>>();
        for (String info : theLink.okhovInfo) {
            if (info.charAt(1)=='-' && info.indexOf('a') == -1) {
                long rng = Long.parseLong(info.substring(1, info.indexOf('.')));
                altPrimes.add(EvenKhovCalculator.getPrimes(rng,options.getPrimes()));
            }
        }
        boolean noBetter = true;
        int i = 0;
        while (i < altPrimes.size() && noBetter) {
            if (betterThan(primes,altPrimes.get(i))) noBetter = false;
            i++;
        }
        return noBetter;
    }
    
    private void wrapUp(LinkData theLink, ArrayList<String> endHom) {
        theLink.wrapUpOddKhov(endHom, coeff, options);
    }
    
    private int[] primeAndPower(int rng) {
        int[] pap = new int[2];
        boolean found = false;
        int i = 0;
        while (!found) {
            int p = options.getPrimes().get(i);
            if (rng % p == 0) found = true;
            else i++;
        }
        pap[0] = options.getPrimes().get(i);
        pap[1] = rng / pap[0];
        return pap;
    }
    
    private boolean betterThan(ArrayList<Integer> primes, ArrayList<Integer> subprimes) {
        boolean better = true;
        int i = 0;
        while (i < subprimes.size() && better) {
            if (!primes.contains(subprimes.get(i))) better = false;
            i++;
        }
        return better;
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

    @Override
    protected void setUpRestFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, true);
        if (options.getGirthInfo() == 2) frame.setLabelLeft("h-Level : ", 3, false);
    }

    @Override
    protected void performCalculation(LinkData theLink) {
        if (coeff == 0) {
            OddKhovHomology<BigInt> hom = new OddKhovHomology<BigInt>(theLink, coeff, frame, options, new BigInt(1), null);
            hom.calculate();
            if (!abInf.isAborted()) wrapUp(theLink, hom.getOddHomology());
        }
        if (coeff == 1) {// rational calculation
            OddKhovHomology<BigRat> hom = new OddKhovHomology<BigRat>(theLink, coeff, frame, options, 
                    new BigRat(BigInteger.ONE), null);
            hom.calculate();
            if (!abInf.isAborted()) wrapUp(theLink, hom.getOddHomology());
        }
        if (coeff >= 2) {// modular calculation
            int coff = (int) coeff;
            OddKhovHomology<ModN> hom = new OddKhovHomology<ModN>(theLink, coeff, frame, options, new ModN(1, coff), 
            new ModN(primeOf(coff), coff));
            hom.calculate();
            if (!abInf.isAborted()) wrapUp(theLink, hom.getOddHomology());
        }
        if (coeff < 0) {// local calculation
            ArrayList<Integer> prms = EvenKhovCalculator.getPrimes(coeff, options.getPrimes());
            int[] primes = new int[prms.size()];
            for (int i = 0; i < prms.size(); i++) primes[i] = prms.get(i);
            OddKhovHomology<LocalP> hom = new OddKhovHomology<LocalP>(theLink, coeff, frame, options, 
                    new LocalP(BigInteger.ONE,primes), null);
            hom.calculate();
            if (!abInf.isAborted()) wrapUp(theLink, hom.getOddHomology());
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
