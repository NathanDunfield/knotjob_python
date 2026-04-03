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

import java.util.ArrayList;
import knotjob.Options;
import knotjob.homology.HomologyInfo;
import knotjob.homology.QuantumCohomology;
import knotjob.homology.evenkhov.EvenKhovCalculator;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class ViewSlTHomology extends ViewCohomology {
    
    public ViewSlTHomology(LinkData theLink, boolean red, Options options) {
        super(theLink, red, options);
    }
    
    @Override
    protected HomologyInfo getIntegralHomology() {
        ArrayList<String> theStrings = link.unredSlT;
        ArrayList<String> theInfo = link.sltInfo;
        if (reduced) theStrings = link.redSlT;
        char reduz = 'u';
        if (reduced) reduz = 'r';
        return link.integralHomology(reduz, theStrings, theInfo);
    }
    
    @Override
    protected HomologyInfo getRationalHomology() {
        ArrayList<String> theStrings = link.unredSlT;
        ArrayList<String> theInfo = link.sltInfo;
        if (reduced) theStrings = link.redSlT;
        char reduz = 'u';
        if (reduced) reduz = 'r';
        return link.rationalHomology(reduz, theStrings, theInfo);
    }
    
    @Override
    protected void getApproximation() {
        ArrayList<String> theStrings = link.unredSlT;
        if (reduced) theStrings = link.redSlT;
        HomologyInfo approxInfo;
        if (ratGood) approxInfo = rationalHom.rationalHomFrom();
        else approxInfo = new HomologyInfo(0l,1);
        approxInfo.setPrime(0l);
        ArrayList<HomologyInfo> minusInfos = new ArrayList<HomologyInfo>();
        plusInfos = new ArrayList<HomologyInfo>();
        for (int p : availPrimes) availPowers.add(0);
        char check = 'u';
        if (reduced) check = 'r';
        ArrayList<String> relInfo = getRelevantInfo(link.sltInfo, check);
        long[][] startInfo = getStartInfo(relInfo);
        for (int i = 0; i < relInfo.size(); i++) {
            String info = relInfo.get(i);
            if (positiveInfo(info)) plusInfos.add(link.theHomology(startInfo[i], theStrings));
            else minusInfos.add(link.theHomology(startInfo[i], theStrings));
        }
        for (HomologyInfo hInfo : minusInfos) { 
            for (QuantumCohomology coh : hInfo.getHomologies()) {
                approxInfo.addTorsion(coh,onlyAvailable());
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
        integralHom = approxInfo;
        rationalHom = approxInfo.rationalHomFrom();
    }
    
    @Override
    protected String theTitleForHom() {
        return "sl3_"+link.name;
    }
    
}
