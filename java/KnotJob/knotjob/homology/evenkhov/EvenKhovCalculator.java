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

package knotjob.homology.evenkhov;

import java.math.BigInteger;
import java.util.ArrayList;
import knotjob.Calculator;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.LinkData;
import knotjob.rings.BigInt;
import knotjob.rings.BigRat;
import knotjob.rings.LocalP;
import knotjob.rings.ModN;

/**
 *
 * @author Dirk
 */
public class EvenKhovCalculator extends Calculator {
    
    private final long coeff;
    private final boolean unred;
    private final boolean red;
    
    public EvenKhovCalculator(ArrayList<LinkData> lnkLst, long val, Options optns, DialogWrap frm) {
        super(lnkLst, optns, frm);
        coeff = val;
        unred = options.getKhovUnred();
        red = options.getKhovRed();
    }
    
    @Override
    protected boolean calculationRequired(LinkData theLink) {
        if ((!unred | theLink.integralHomologyExists(false)) & (!red | theLink.integralHomologyExists(true))) return false;
        if (coeff == 0) return true;
        if (coeff < 0) {
            ArrayList<Integer> primes = getPrimes(coeff,options.getPrimes());
            return ((unred & noBetterCalculation(primes,theLink,false)) | (red & noBetterCalculation(primes,theLink,true)));
        }
        if (coeff == 1 && (!unred | theLink.rationalHomologyExists(false)) & (!red | theLink.rationalHomologyExists(true))) 
            return false;
        if (coeff == 1) return true;
        int[] pap = primeAndPower((int) coeff);
        return ((unred & noBetterCalculation(pap,theLink,false)) | (red & noBetterCalculation(pap,theLink,true)));
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
    
    private boolean noBetterCalculation(int[] pap, LinkData theLink, boolean reduced) {
        if (theLink.khovInfo == null) return true;
        char redChar = 'u';
        if (reduced) redChar = 'r';
        for (String info : theLink.khovInfo) {
            long rng = Long.parseLong(info.substring(1, info.indexOf('.')));
            if (info.charAt(0) == redChar && rng < 0 && info.indexOf('a') == -1) {
                if (!getPrimes(rng,options.getPrimes()).contains(pap[0])) return false;
            }
            if (info.charAt(0) == redChar && rng > 1 && info.indexOf('a') == -1) {
                if (rng % pap[0] == 0 && rng / pap[0] >= pap[1]) return false;
            }
        }
        return true;
    }
    
    private boolean noBetterCalculation(ArrayList<Integer> primes, LinkData theLink, boolean reduced) {
        if (theLink.khovInfo == null) return true;
        ArrayList<ArrayList<Integer>> altPrimes = new ArrayList<ArrayList<Integer>>();
        char redChar = 'u';
        if (reduced) redChar = 'r';
        for (String info : theLink.khovInfo) {
            if (info.charAt(0) == redChar && info.charAt(1)=='-' && info.indexOf('a') == -1) {
                long rng = Long.parseLong(info.substring(1, info.indexOf('.')));
                altPrimes.add(getPrimes(rng,options.getPrimes()));
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
    
    private boolean betterThan(ArrayList<Integer> primes, ArrayList<Integer> subprimes) {
        boolean better = true;
        int i = 0;
        while (i < subprimes.size() && better) {
            if (!primes.contains(subprimes.get(i))) better = false;
            i++;
        }
        return better;
    }
    
    private void wrapUp(LinkData theLink, ArrayList<String> unredInfo, ArrayList<String> redInfo, 
            int tbu, int tbr, int tmu, int tmr) {
        theLink.wrapUpEvenKhov(unredInfo, redInfo, coeff, options);
        /*if (coeff > 1) {
            theLink.setTotalKhov(primeOf((int) coeff), tmu, false);
            theLink.setTotalKhov(primeOf((int) coeff), tmr, true);
        }
        if (coeff == 1) {
            theLink.setTotalKhov(0, tbu, false);
            theLink.setTotalKhov(0, tbr, true);
        }
        if (coeff == 0) {
            theLink.setTotalKhov(0, tbu, false);
            theLink.setTotalKhov(0, tbr, true);
            theLink.setTotalKhov(2, tmu, false);
            theLink.setTotalKhov(2, tmr, true);
        }
        if (coeff < 0) {
            theLink.setTotalKhov(0, tbu, false);
            theLink.setTotalKhov(0, tbr, true);
            if (coeff % 2 == 0) {
                theLink.setTotalKhov(2, tmu, false);
                theLink.setTotalKhov(2, tmr, true);
            }
        }// */
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
    
    public static ArrayList<Integer> getPrimes(long rng, ArrayList<Integer> primes) {
        ArrayList<Integer> prms = new ArrayList<Integer>();
        long pwr = 1;
        for (int p = 0; p < primes.size(); p++) {
            if (rng % (2*pwr) != 0) {
                prms.add(primes.get(p));
                rng = rng + pwr;
            }
            pwr = 2 * pwr;
        }
        return prms;
    }

    @Override
    protected void setUpRestFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, true);
    }

    @Override
    protected void adaptFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, false);
        if (options.getGirthInfo() == 2) frame.setLabelLeft("h-Level : ", 3, false);
    }
    
    @Override
    protected void performCalculation(LinkData theLink) {
        theLink.reLock.lock();
        try {
            if (coeff == 0) {
                EvenKhovHomology<BigInt> hom = new EvenKhovHomology<BigInt>(theLink, coeff, frame,
                        unred, red, options, new BigInt(1), null);
                hom.calculate();
                if (!abInf.isAborted()) wrapUp(theLink, hom.getUnreduced(), hom.getReduced(),
                        hom.totunr, hom.totred, hom.towunr, hom.towred);
            }
            if (coeff == 1) {// rational calculation
                EvenKhovHomology<BigRat> hom = new EvenKhovHomology<BigRat>(theLink, coeff, frame,
                        unred, red, options, new BigRat(BigInteger.ONE), null);
                hom.calculate();
                if (!abInf.isAborted()) wrapUp(theLink, hom.getUnreduced(), hom.getReduced(),
                        hom.totunr, hom.totred, hom.towunr, hom.towred);
            }
            if (coeff >= 2) {// modular calculation
                int coff = (int) coeff;
                EvenKhovHomology<ModN> hom = new EvenKhovHomology<ModN>(theLink, coeff, frame, 
                        unred, red, options, new ModN(1, coff), new ModN(primeOf(coff), coff));
                hom.calculate();
                if (!abInf.isAborted()) wrapUp(theLink, hom.getUnreduced(), hom.getReduced(),
                        hom.totunr, hom.totred, hom.towunr, hom.towred);
            }
            if (coeff < 0) {// local calculation
                ArrayList<Integer> prms = getPrimes(coeff, options.getPrimes());
                int[] primes = new int[prms.size()];
                for (int i = 0; i < prms.size(); i++) primes[i] = prms.get(i);
                EvenKhovHomology<LocalP> hom = new EvenKhovHomology<LocalP>(theLink, coeff, frame, 
                        unred, red, options, new LocalP(BigInteger.ONE,primes), null);
                hom.calculate();
                if (!abInf.isAborted()) wrapUp(theLink, hom.getUnreduced(), hom.getReduced(),
                        hom.totunr, hom.totred, hom.towunr, hom.towred);
            }
        }
        finally {
            theLink.reLock.unlock();
        }
    }
}
