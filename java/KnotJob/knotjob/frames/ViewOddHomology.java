/*

Copyright (C) 2020 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.frames;

import knotjob.homology.HomologyInfo;
import java.util.ArrayList;
import knotjob.Options;
import knotjob.homology.QuantumCohomology;
import knotjob.homology.evenkhov.EvenKhovCalculator;
import knotjob.links.LinkData;

/**
 *
 * @author dirk
 */
public class ViewOddHomology extends ViewCohomology {
    
    private boolean redder;
    
    public ViewOddHomology(LinkData theLink, boolean red, Options options) {
        super(theLink, red, options);
        redder = reduced;
    }
    
    @Override
    protected HomologyInfo getIntegralHomology() {
        HomologyInfo theInfo = link.integralHomology('o', link.oddKhovHom, link.okhovInfo);
        if (theInfo == null) return null;
        if (reduced) return theInfo;
        return theInfo.doubleHom();
    }
    
    @Override
    protected HomologyInfo getRationalHomology() {
        HomologyInfo theInfo = link.rationalHomology('o', link.oddKhovHom, link.okhovInfo);
        if (theInfo == null) return null;
        if (redder) return theInfo;
        return theInfo.doubleHom();
    }
    
    @Override
    protected void getApproximation() {
        ArrayList<String> theStrings = link.oddKhovHom;
        HomologyInfo approxInfo;
        if (ratGood) {
            boolean red = redder;
            redder = true;
            approxInfo = getRationalHomology();
            redder = red;
        }
        else approxInfo = new HomologyInfo(0l,1);
        approxInfo.setPrime(0l);
        ArrayList<HomologyInfo> minusInfos = new ArrayList<HomologyInfo>();
        plusInfos = new ArrayList<HomologyInfo>();
        for (int p : availPrimes) availPowers.add(0);
        ArrayList<String> relInfo = getRelevantInfo(link.okhovInfo, 'o');
        long[][] startInfo = getStartInfo(relInfo);
        for (int i = 0; i < relInfo.size(); i++) {
            String info = relInfo.get(i);
            if (positiveInfo(info)) plusInfos.add(link.theHomology(startInfo[i], theStrings));
            else minusInfos.add(link.theHomology(startInfo[i], theStrings));
        }
        for (HomologyInfo hInfo : minusInfos) {
            for (QuantumCohomology coh : hInfo.getHomologies()) {
                approxInfo.addTorsion(coh, onlyAvailable());
            }
            ArrayList<Integer> primes = EvenKhovCalculator.getPrimes(hInfo.getPrime(), availPrimes);
            for (int i = 0; i < availPrimes.size(); i++) {
                if (!primes.contains(availPrimes.get(i))) availPowers.set(i, opts.getPowers().get(i));
            }
        }
        
        boolean setBetti = !ratGood;
        for (HomologyInfo hInfo : plusInfos) {
            int prime = (int) hInfo.getPrime();
            if (availPowers.get(availPrimes.indexOf(prime)) == 0) {
                ArrayList<Integer> primes = new ArrayList<Integer>(1);
                primes.add(prime);
                if (!setBetti) approxInfo.adjustBetti(hInfo);
                for (QuantumCohomology coh : hInfo.getHomologies()) {
                    approxInfo.addTorsion(coh,primes);
                    if (setBetti) approxInfo.setBetti(coh);
                }
                setBetti = false;
            }
        }
        for (HomologyInfo hInfo : plusInfos) {
            int prime = (int) hInfo.getPrime();
            if (availPowers.get(availPrimes.indexOf(prime)) == 0) {
                if (approxInfo.compareBetti(hInfo)) imprPrimes.add(prime);
            }
            availPowers.set(availPrimes.indexOf(prime),hInfo.getMaxpower());
        }
        for (int i = availPrimes.size()-1; i >= 0; i--) {
            if (availPowers.get(i) == 0) {
                availPowers.remove(i);
                availPrimes.remove(i);
            }
        }
        if (availPowers.size() == opts.getPowers().size() && imprPrimes.isEmpty() && !minusInfos.isEmpty()) {
            for (int i = 0; i < availPowers.size(); i++) availPowers.set(i, opts.getPowers().get(i));
            intGood = true;
        }
        if (!redder) approxInfo = approxInfo.doubleHom();
        integralHom = approxInfo;
        rationalHom = approxInfo.rationalHomFrom();
    }
    
    @Override
    protected String theTitleForHom() {
        return "OKh_"+link.name;
    }
    
}
