/*

Copyright (C) 2019-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.links;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;
import knotjob.Comparer;
import knotjob.Options;
import knotjob.homology.HomologyInfo;
import knotjob.homology.Homology;
import knotjob.homology.QuantumCohomology;
import knotjob.homology.evenkhov.EvenKhovCalculator;

/**
 *
 * @author Dirk
 */
public class LinkData  extends StringData implements Comparable<LinkData> {
    
    public ArrayList<Link> links;
    private final Comparer comparer;
    public final ReentrantLock reLock;
    int chosen;

    public LinkData(String lname, Link theLink, Comparer cmp) {
        super(lname);
        name = lname;
        links = new ArrayList<Link>();
        links.add(theLink);
        comparer = cmp;
        reLock = new ReentrantLock();
    }
    
    public LinkData(String fname, Comparer cmp) {
        super(fname);
        links = new ArrayList<Link>();
        //stuffToNull();
        name = fname;
        comparer = cmp;
        reLock = new ReentrantLock();
    }
    
    public Link chosenLink() {
        return links.get(chosen);
    }
    
    public void setChosen(int i) {
        chosen = i;
    }
    
    public int chosen() {
        return chosen;
    }
    
    public void choseGirthMinimized() {
        if (this.links.size()> 1) {
            int mingirth = this.links.get(0).maxGirth();
            int mintgirth = this.links.get(0).totalGirth();
            int choose = 0;
            for (int j = 1; j < this.links.size(); j++) {
                Link link = this.links.get(j);
                int girth = link.maxGirth();
                int tgirth = link.totalGirth();
                if (girth < mingirth | (girth == mingirth & tgirth < mintgirth)) {
                    mingirth = girth;
                    mintgirth = tgirth;
                    choose = j;
                }
            }
            this.setChosen(choose);
        }
    }
    
    public void choseCrossingMinimized() {
        if (this.links.size()> 1) {
            int mincross = this.links.get(0).crossingNumber();
            int choose = 0;
            for (int j = 1; j < this.links.size(); j++) {
                Link link = this.links.get(j);
                int cros = link.crossingNumber();
                if (cros < mincross) {
                    mincross = cros;
                    choose = j;
                }
            }
            this.setChosen(choose);
        }
    }
    
    public void choseCombinedMinimized() {
        if (this.links.size()> 1) {
            int mincomb = this.links.get(0).crossingLength();
            int choose = 0;
            for (int j = 1; j < this.links.size(); j++) {
                Link link = this.links.get(j);
                int comb = link.crossingLength();
                if (comb < mincomb) {
                    mincomb = comb;
                    choose = j;
                }
            }
            this.setChosen(choose);
        }
    }
    
    public boolean showBLTButton(boolean red) {
        if (red) return (redBLT != null);
        return unredBLT != null;
    }
    
    public boolean showStEvenButton() {
        return stEven != null;
    }
    
    public boolean showStOddButton(int u) {
        if (u == 0) return stOdd != null;
        return stOde != null;
    }
    
    public boolean showOddKhovButton() {
        if (okhovInfo == null) return false;
        boolean found = false;
        int i = 0;
        while (!found && i < okhovInfo.size()) {
            String inf = okhovInfo.get(i);
            if (inf.charAt(0) == 'o' && inf.indexOf('a') == -1) found = true;
            else i++;
        }
        return found;
    }
    
    public boolean showSlTHomButton(boolean reduced) {
        if (sltInfo == null) return false;
        boolean found = false;
        char start = 'u';
        if (reduced) start = 'r';
        int i = 0;
        while (!found && i < sltInfo.size()) {
            String inf = sltInfo.get(i);
            if (inf.charAt(0) == start && inf.indexOf('a') == -1) found = true;
            else i++;
        }
        return found;
    }
    
    public boolean showKhovHomButton(boolean reduced) {
        if (khovInfo == null) return false;
        boolean found = false;
        char start = 'u';
        if (reduced) start = 'r';
        int i = 0;
        while (!found && i < khovInfo.size()) {
            String inf = khovInfo.get(i);
            if (inf.charAt(0) == start && inf.indexOf('a') == -1) found = true;
            else i++;
        }
        return found;
    }

    public boolean integralOddHomologyExists() {
        return (getStartEnd('o', 0, okhovInfo) != null);
    }

    public boolean rationalOddHomologyExists() {
        int[] rel = getStartEnd('o', 1, okhovInfo);
        if (rel == null) rel = getStartEnd('o', -1, okhovInfo);
        return (rel != null);
    }
    
    public boolean integralSlTExists() {
        ArrayList<String> homStrings = sltInfo;
        return (getStartEnd('u', 0, homStrings) != null);
    }
    
    public boolean rationalSlTExists() {
        ArrayList<String> homStrings = sltInfo;
        int[] rel = getStartEnd('u', 1, homStrings);
        if (rel == null) rel = getStartEnd('u', -1, homStrings);
        return (rel != null);
    }
    
    public boolean integralHomologyExists(boolean reduced) {
        char red = 'u';
        if (reduced) red = 'r';
        ArrayList<String> homStrings = khovInfo;
        return (getStartEnd(red, 0, homStrings)!= null);
    }
    
    public boolean rationalHomologyExists(boolean reduced) {
        char red = 'u';
        if (reduced) red = 'r';
        ArrayList<String> homStrings = khovInfo;
        int[] rel = getStartEnd(red, 1, homStrings);
        if (rel == null) rel = getStartEnd(red, -1, homStrings);
        return (rel != null);
    }
    
    public HomologyInfo theHomology(long[] special, ArrayList<String> homStrings) {
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        for (int j = (int)special[2]; j < (int)special[3]; j++) {
            String qinfo = homStrings.get(j);
            QuantumCohomology coh = new QuantumCohomology(qinfo);
            cohoms.add(coh);
        }
        HomologyInfo hinfo = new HomologyInfo(special[0],(int)special[1]);
        for (QuantumCohomology coh : cohoms) hinfo.addCohomology(coh);
        return hinfo;
    }
    
    public HomologyInfo approximateHomology(char reduced, ArrayList<Integer> primes, ArrayList<String> theInfo, 
            ArrayList<String> homStrings) {
        HomologyInfo approxInfo = new HomologyInfo(0l,1);
        approxInfo.setPrime(0l);
        ArrayList<HomologyInfo> minusInfos = new ArrayList<HomologyInfo>();
        ArrayList<HomologyInfo> plusInfos = new ArrayList<HomologyInfo>();
        ArrayList<Integer> powers = new ArrayList<Integer>();
        for (int p : primes) powers.add(0);
        ArrayList<String> relInfo = getRelevantInfo(reduced, theInfo);
        long[][] startInfo = getStartInfo(relInfo, primes);
        for (int i = 0; i < relInfo.size(); i++) {
            String info = relInfo.get(i);
            if (info.charAt(1)!='-') plusInfos.add(theHomology(startInfo[i], homStrings));
            else minusInfos.add(theHomology(startInfo[i], homStrings));
        }
        if (minusInfos.isEmpty() && plusInfos.isEmpty()) return null;
        for (HomologyInfo hInfo : minusInfos) {
            for (QuantumCohomology coh : hInfo.getHomologies()) {
                approxInfo.addTorsion(coh,onlyAvailable(primes,powers));
            }
            ArrayList<Integer> prmes = EvenKhovCalculator.getPrimes(hInfo.getPrime(), primes);
            for (int i = 0; i < primes.size(); i++) {
                if (!prmes.contains(primes.get(i))) powers.set(i, 1);
            }
        }
        boolean setBetti = !minusInfos.isEmpty();
        for (HomologyInfo hInfo : plusInfos) {
            int prime = (int) hInfo.getPrime();
            if (powers.get(primes.indexOf(prime)) == 0) {
                ArrayList<Integer> prmes = new ArrayList<Integer>(1);
                prmes.add(prime);
                if (!setBetti) approxInfo.adjustBetti(hInfo);
                for (QuantumCohomology coh : hInfo.getHomologies()) {
                    approxInfo.addTorsion(coh,prmes);
                    if (setBetti) approxInfo.setBetti(coh);
                }
                setBetti = false;
            }
        }
        return approxInfo;
    }
    
    private ArrayList<Integer> onlyAvailable(ArrayList<Integer> primes, ArrayList<Integer> powers) {
        ArrayList<Integer> availables = new ArrayList<Integer>();
        for (int i = 0; i < primes.size(); i++) {
            if (powers.get(i) == 0) availables.add(primes.get(i));
        }
        return availables;
    }
    
    private ArrayList<String> getRelevantInfo(char check, ArrayList<String> theInfo) {
        if (theInfo == null) return new ArrayList<String>();
        ArrayList<String> rels = new ArrayList<String>();
        for (String checker : theInfo) {
            if (checker.charAt(0)==check && !"1.".equals(checker.substring(1,3))) rels.add(checker);
        }
        return rels;
    }
    
    public long[][] getStartInfo(ArrayList<String> relInfo, ArrayList<Integer> primes) {
        if (relInfo == null) return new long[0][0];
        long[][] theInfo = new long[relInfo.size()][4];
        for (int i = 0; i < relInfo.size(); i++) {
            long[] prime = primeAndPower(relInfo.get(i),primes);
            int[] start = startAndEnd(relInfo.get(i));
            theInfo[i][0] = prime[0];
            theInfo[i][1] = prime[1];
            theInfo[i][2] = start[0];
            theInfo[i][3] = start[1];
        }
        return theInfo;
    }
    
    private int[] startAndEnd(String info) {
        int[] sae = new int[2];
        int sp = info.lastIndexOf('.');
        int mp = info.lastIndexOf('-');
        sae[0] = Integer.parseInt(info.substring(sp+1, mp));
        sae[1] = Integer.parseInt(info.substring(mp+1));
        return sae;
    }
    
    private long[] primeAndPower(String info, ArrayList<Integer> primesOfInterest) {
        long[] pap = new long[2];
        boolean found = false;
        int i = 0;
        int end = info.indexOf(".");
        long ring = Long.parseLong(info.substring(1, end));
        if (ring < 2) {
            pap[0] = ring;
            pap[1] = 1;
            return pap;
        }
        int prime = 2;
        while (!found) {
            prime = primesOfInterest.get(i);
            if (ring%prime == 0) found = true;
            else i++;
        }
        int power = 0;
        while (ring%prime == 0) {
            ring = ring/prime;
            power++;
        }
        pap[0] = prime;
        pap[1] = power;
        return pap;
    }
    
    private HomologyInfo modularHomology(char reduced, ArrayList<String> homStrings, 
            ArrayList<String> theInfo, int p) {
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        int[] rel = getStartEnd(reduced, p, theInfo);
        if (rel == null) return null;
        for (int j = rel[0]; j < rel[1]; j++) {
            String qinfo = homStrings.get(j);
            QuantumCohomology coh = new QuantumCohomology(qinfo);
            cohoms.add(coh);
        }
        int[] data = primeAndPower(p);
        HomologyInfo hinfo = new HomologyInfo((long) data[0], data[1]);
        for (QuantumCohomology coh : cohoms) hinfo.addCohomology(coh);
        return hinfo;
    }
    
    public HomologyInfo integralHomology(char reduced, ArrayList<String> homStrings, ArrayList<String> theInfo) {
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        int[] rel = getStartEnd(reduced, 0, theInfo);
        if (rel == null) return null;
        for (int j = rel[0]; j < rel[1]; j++) {
            String qinfo = homStrings.get(j);
            QuantumCohomology coh = new QuantumCohomology(qinfo);
            cohoms.add(coh);
        }
        HomologyInfo hinfo = new HomologyInfo(0l,1);
        for (QuantumCohomology coh : cohoms) hinfo.addCohomology(coh);
        return hinfo;
    }
    
    public HomologyInfo rationalHomology(char reduced, ArrayList<String> homStrings, ArrayList<String> theInfo) {
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        int[] rel = getStartEnd(reduced,1, theInfo);
        if (rel == null) rel = getStartEnd(reduced,-1, theInfo);
        if (rel == null) return null;
        for (int j = rel[0]; j < rel[1]; j++) {
            String qinfo = homStrings.get(j);
            QuantumCohomology coh = new QuantumCohomology(qinfo);
            for (Homology hom : coh.getHomGroups()) hom.removeTorsion();
            cohoms.add(coh);
        }
        HomologyInfo hinfo = new HomologyInfo(1l,1);
        for (QuantumCohomology coh : cohoms) hinfo.addCohomology(coh);
        return hinfo;
    }
    
    private int[] getStartEnd(char red, int cse, ArrayList<String> theInfo) {
        if (theInfo == null) return null;
        boolean found = false;
        int i = 0;
        while (!found && i < theInfo.size()) {
            String khoInf = theInfo.get(i);
            long theC = Long.parseLong(khoInf.substring(1, khoInf.indexOf('.')));
            if (theC < 0) theC = -1;
            if (theC == cse && khoInf.charAt(0) == red && khoInf.indexOf('a') == -1) found = true;
            else i++;
        }
        if (!found) return null;
        String info = theInfo.get(i);
        int[] rel = new int[2];
        int u = info.lastIndexOf(".");
        int v = info.lastIndexOf("-");
        rel[0] = Integer.parseInt(info.substring(u+1, v));
        rel[1] = Integer.parseInt(info.substring(v+1));
        return rel;
    }
    
    public void setSqOne(int[] data) {
        sqEven = "("+data[0]+","+data[1]+","+data[2]+","+data[3]+")";
    }
    
    public void setSInvariant(String sinv) {
        int[][] sinvs = sInvariants(sinv);
        for (int[] sinv1 : sinvs) {
            setSInvariant(sinv1[0], sinv1[1]);
        }
    }
    
    public void setSLtSInvariant(String sinv, boolean red) {
        int[][] sinvs = sInvariants(sinv);
        for (int[] sinv1 : sinvs) {
            setSlTSInvariant(sinv1[0], sinv1[1], red);
        }
    }
    
    public void setSInvariant(int chi, int s) {
        reLock.lock();
        try {
            if (sinvariant == null) sinvariant = chi+":"+s;
            else if (sInvariant(chi) == null) sinvariant = sinvariant+","+chi+":"+s;
        }
        finally {
            reLock.unlock();
        }// */
    }
    
    public void setSlTSInvariant(int chi, int s, boolean red) {
        reLock.lock();
        try {
            if (red) {
                if (sltrinvariant == null) sltrinvariant = chi+":"+s;
                else if (sSlTInvariant(chi, red) == null) {
                    sltrinvariant = sltrinvariant+","+chi+":"+s;
                }
            }
            else {
                if (sltsinvariant == null) sltsinvariant = chi+":"+s;
                else if (sSlTInvariant(chi, red) == null) {
                    sltsinvariant = sltsinvariant+","+chi+":"+s;
                }
            }
        }
        finally {
            reLock.unlock();
        }// */
    }
    
    /*public void setTotalKhov(int chi, int t, boolean red) {
        if (t == 0) return;
        reLock.lock();
        try {
            if (totalKhov == null) totalKhov = String.valueOf(chi);
            else totalKhov = totalKhov+chi;
            if (red) totalKhov = totalKhov+"r"+t+".";
            else totalKhov = totalKhov+"u"+t+".";
        }
        finally {
            reLock.unlock();
        }
    }
    
    public int getTotalKhov(int chi, boolean red) {
        String helper = totalKhov;
        String redd = "u";
        if (red) redd = "r";
        boolean found = false;
        int total = 0;
        while (!found) {
            int a = helper.indexOf(".");
            if (a < 0) break;
            String help = helper.substring(0, a);
            helper = helper.substring(a+1);
            int b = help.indexOf(redd);
            if (b > 0) {
                int chr = Integer.parseInt(help.substring(0, b));
                if (chr == chi) {
                    found = true;
                    total = Integer.parseInt(help.substring(b+1));
                }
            }
        }
        return total;
    }// */
    
    public int getBettiKhov(int chi, int hdeg, boolean red) {
        HomologyInfo info;
        if (chi == 0) info = integralKhovHomology(red);
        else info = modKhovHomology(red, chi);
        int total = 0;
        for (QuantumCohomology coh : info.getHomologies()) {
            for (Homology hom : coh.getHomGroups()) {
                if (hom.hdeg() == hdeg) total = total+hom.getBetti();
            }
        }
        return total;
    }
    
    public int[] getBettiKhovSgn(int chi, boolean red) {
        HomologyInfo info;
        if (chi == 0) info = integralKhovHomology(red);
        else info = modKhovHomology(red, chi);
        int[] total = new int[2];
        for (QuantumCohomology coh : info.getHomologies()) {
            for (Homology hom : coh.getHomGroups()) {
                if (hom.hdeg() % 2 == 0) total[0] = total[0]+hom.getBetti();
                else total[1] = total[1]+hom.getBetti();
            }
        }
        return total;
    }
    
    public void setAlexander(String theString) {
        alex = theString;
    }
    
    public void setJones(String theString) {
        jones = theString;
    }
    
    public void setSignature(int sig) {
        signature = String.valueOf(sig);
    }
    
    public boolean otherSInvariants() {
        int[][] invs = sInvariants();
        int[][] sltinv = sSlTInvariants(false);
        int[][] rltinv = sSlTInvariants(true);
        if (sltinv.length >= 1 || rltinv.length >= 1) return true;
        boolean exist = false;
        int i = 0;
        while (!exist && i < invs.length) {
            if (invs[i][0] >= 3) exist = true;
            i++;
        }
        return exist;
    }
    
    public int[] getSqOne(boolean even) {
        if (even && sqEven == null) return null;
        if (!even && sqOdd == null) return null;
        String relString = sqOdd;
        if (even) relString = sqEven;
        return arrayFromString(relString);
    }
    
    private int[] arrayFromString(String relString) {
        int[] array = new int[4];
        int a = relString.indexOf(',');
        int b = a+1+relString.substring(a+1).indexOf(',');
        if (b == a) {
            int s = sInvariant(2);
            relString = relString.substring(0, relString.length()-1)+", "+s+", "+s+")";
            b = a+1+relString.substring(a+1).indexOf(',');
        }
        int c = b+1+relString.substring(b+1).indexOf(',');
        int d = relString.indexOf(')');
        array[0] = Integer.parseInt(relString.substring(1, a));
        array[1] = Integer.parseInt(relString.substring(a+2, b));
        array[2] = Integer.parseInt(relString.substring(b+2, c));
        array[3] = Integer.parseInt(relString.substring(c+2, d));
        return array;
    }
    
    public int[] getSqTwo(int stcase) {
        if (stcase == 2 && sqtEven == null) return null;
        if (stcase == 0 && sqtOdd == null) return null;
        if (stcase == 1 && sqtOde == null) return null;
        String relString = sqtEven;
        if (stcase == 0) relString = sqtOdd;
        if (stcase == 1) relString = sqtOde;
        return arrayFromString(relString);
    }
    
    public int[] getBLSOdd() {
        if (bsOdd == null) return null;
        return arrayFromString(bsOdd);
    }
    
    public int[] getSqOneSum() {
        if (beta == null) return null;
        return arrayFromString(beta);
    }
    
    public int[][] sInvariants() {
        return sInvariants(sinvariant);
    }
    
    public int[][] sSlTInvariants(boolean red) {
        if (red) return sInvariants(sltrinvariant);
        return sInvariants(sltsinvariant);
    }
    
    private int[][] sInvariants(String sinv) {
        if (sinv == null) return new int[0][0];
        ArrayList<String> infos = new ArrayList<String>();
        String helpString = sinv.substring(0);
        int i = helpString.indexOf(',');
        while (i >= 0) {
            infos.add(helpString.substring(0, i));
            helpString = helpString.substring(i+1);
            i = helpString.indexOf(',');
        }
        infos.add(helpString);
        int[][] info = new int[infos.size()][2];
        i = 0;
        for (String inf : infos) {
            int k = inf.indexOf(':');
            info[i][0] = Integer.parseInt(inf.substring(0, k));
            info[i][1] = Integer.parseInt(inf.substring(k+1));
            i++;
        }
        return info;
    }

    public Integer sInvariant(int chr) {
        int[][] sinvs = sInvariants();
        boolean found = false;
        int i = 0;
        while (!found && i < sinvs.length) {
            if (sinvs[i][0] == chr) found = true;
            else i++;
        }
        if (found) return sinvs[i][1];// */
        return null;
    }
    
    public Integer sSlTInvariant(int chr, boolean red) {
        int[][] sinvs = sSlTInvariants(red);
        boolean found = false;
        int i = 0;
        while (!found && i < sinvs.length) {
            if (sinvs[i][0] == chr) found = true;
            else i++;
        }
        if (found) return sinvs[i][1];// */
        return null;
    }
    
    @Override
    public int compareTo(LinkData o) {
        return comparer.compare(this, o, comparer.getType());
    }

    public HomologyInfo integralKhovHomology(boolean reduced) {
        ArrayList<String> theStrings = unredKhovHom;
        ArrayList<String> theInfo = khovInfo;
        if (reduced) theStrings = redKhovHom;
        char reduz = 'u';
        if (reduced) reduz = 'r';
        return integralHomology(reduz, theStrings, theInfo);
    }
    
    public HomologyInfo integralSlTHomology(boolean reduced) {
        ArrayList<String> theStrings = unredSlT;
        ArrayList<String> theInfo = sltInfo;
        if (reduced) theStrings = redSlT;
        char reduz = 'u';
        if (reduced) reduz = 'r';
        return integralHomology(reduz, theStrings, theInfo);
    }
    
    public HomologyInfo integralOddKhHomology() {
        ArrayList<String> theInfo = okhovInfo;
        ArrayList<String> theStrings = oddKhovHom;
        char reduz = 'o';
        return integralHomology(reduz, theStrings, theInfo);
    }
    
    public HomologyInfo rationalKhovHomology(boolean reduced) {
        ArrayList<String> theStrings = unredKhovHom;
        ArrayList<String> theInfo = khovInfo;
        if (reduced) theStrings = redKhovHom;
        char reduz = 'u';
        if (reduced) reduz = 'r';
        return rationalHomology(reduz, theStrings, theInfo);
    }
    
    public HomologyInfo rationalOddKhHomology() {
        ArrayList<String> theInfo = okhovInfo;//System.out.println(okhovInfo);
        ArrayList<String> theStrings = oddKhovHom;
        char reduz = 'o';
        return rationalHomology(reduz, theStrings, theInfo);
    }
    
    public HomologyInfo modKhovHomology(boolean reduced, int p) {
        ArrayList<String> theStrings = unredKhovHom;
        ArrayList<String> theInfo = khovInfo;
        if (reduced) theStrings = redKhovHom;
        char reduz = 'u';
        if (reduced) reduz = 'r';
        return modularHomology(reduz, theStrings, theInfo, p);
    }
    
    public HomologyInfo modOddKhHomology(int p) {
        ArrayList<String> theInfo = okhovInfo;
        ArrayList<String> theStrings = oddKhovHom;
        char reduz = 'o';
        return modularHomology(reduz, theStrings, theInfo, p);
    }
    
    public void  wrapUpSlT(ArrayList<String> unredInfo, ArrayList<String> redInfo,
            long coeff, Options options) {
        reLock.lock();
        try {
            if (sltInfo == null) sltInfo = new ArrayList<String>();
            if (!unredInfo.isEmpty()) {
                if (unredSlT == null) unredSlT = new ArrayList<String>();
                wrapUpNewInfo('u', unredInfo, unredSlT, sltInfo, coeff, options);
            }
            if (!redInfo.isEmpty()) {
                if (redSlT == null) redSlT = new ArrayList<String>();
                wrapUpNewInfo('r', redInfo, redSlT, sltInfo, coeff, options);
            }
        }
        finally {
            reLock.unlock();
        }
    }
    
    public void wrapUpEvenKhov(ArrayList<String> unredInfo, ArrayList<String> redInfo,
            long coeff, Options options) {
        reLock.lock();
        try {
            if (khovInfo == null) khovInfo = new ArrayList<String>();
            if (!unredInfo.isEmpty()) {
                if (unredKhovHom == null) unredKhovHom = new ArrayList<String>();
                wrapUpNewInfo('u', unredInfo, unredKhovHom, khovInfo, coeff, options);
            }
            if (!redInfo.isEmpty()) {
                if (redKhovHom == null) redKhovHom = new ArrayList<String>();
                wrapUpNewInfo('r', redInfo, redKhovHom, khovInfo, coeff, options);
            }
        }
        finally {
            reLock.unlock();
        }
    }

    public void wrapUpOddKhov(ArrayList<String> homInfo,
            long coeff, Options options) {
        reLock.lock();
        try {
            if (okhovInfo == null) okhovInfo = new ArrayList<String>();
            if (oddKhovHom == null) oddKhovHom = new ArrayList<String>();
            wrapUpNewInfo('o', homInfo, oddKhovHom, okhovInfo, coeff, options);
        }
        finally {
            reLock.unlock();
        }
    }
    
    public void wrapUpBLT(ArrayList<String> theInfo, ArrayList<Integer> blocks, 
            boolean reduced, int characteristic) {
        reLock.lock();
        try {
            if (reduced) {
                if (this.redBLT == null) this.redBLT = new ArrayList<String>();
                addFirstLine(this.redBLT, blocks, characteristic+":r:");
                for (String str : theInfo) this.redBLT.add(str);
            }
            else {
                if (this.unredBLT == null) this.unredBLT = new ArrayList<String>();
                addFirstLine(this.unredBLT, blocks, characteristic+":u:");
                for (String str : theInfo) this.unredBLT.add(str);
            }
        }
        finally {
            reLock.unlock();
        }
    }
    
    public void wrapUpSlTSX(ArrayList<String> theInfo, ArrayList<Integer> blocks, String symbol, 
            int typ) {
        reLock.lock();
        try {
            if (typ != 3) {
                if (this.sltTypeX == null) this.sltTypeX = new ArrayList<String>();
                addFirstLine(this.sltTypeX, blocks, symbol+":u:");
                for (String str : theInfo) this.sltTypeX.add(str);
            }
            else {
                if (this.sltTypeXSq == null) this.sltTypeXSq = new ArrayList<String>();
                addFirstLine(this.sltTypeXSq, blocks, symbol+":u:");
                for (String str : theInfo) this.sltTypeXSq.add(str);
            }
        }
        finally {
            reLock.unlock();
        }
    }
    
    public void wrapUpSlTSS(ArrayList<String> theInfo, ArrayList<Integer> blocks,
            boolean reduced, int characteristic, int sType) {
        reLock.lock();
        try {
            switch (sType) {
                case 3 : if (this.sltTypeThree == null) this.sltTypeThree = new ArrayList<String>();
                         addFirstLine(this.sltTypeThree, blocks, characteristic+":u:");
                         for (String str : theInfo) this.sltTypeThree.add(str);break;
                case 2 : if (this.sltTypeTwo == null) this.sltTypeTwo = new ArrayList<String>();
                         addFirstLine(this.sltTypeTwo, blocks, characteristic+":u:");
                         for (String str : theInfo) this.sltTypeTwo.add(str);break;
                case 1 : if (!reduced) {
                             if (this.sltTypeOne == null) this.sltTypeOne = new ArrayList<String>();
                             addFirstLine(this.sltTypeOne, blocks, characteristic+":u:");
                             for (String str : theInfo) this.sltTypeOne.add(str);
                         }
                         else {
                             if (this.sltTypeOneRed == null) this.sltTypeOneRed = new ArrayList<String>();
                                addFirstLine(this.sltTypeOneRed, blocks, characteristic+":r:");
                                for (String str : theInfo) this.sltTypeOneRed.add(str);
                }
            }
        }
        finally {
            reLock.unlock();
        }
    }
    
    private void addFirstLine(ArrayList<String> theBLTs, ArrayList<Integer> blocks, 
            String first) {
        int shift = theBLTs.size();
        for (int i = 1; i <= blocks.size()/2; i++) {
            first = first+"e"+i+"."+(blocks.get(2*(i-1))+shift)+"."
                    +(blocks.get(2*i-1)+shift);
            if (i < blocks.size()/2) first = first+":";
        }
        theBLTs.add(first);
    }
    
    private void wrapUpNewInfo(char redChar, ArrayList<String> endHom, ArrayList<String> oldInfo,
            ArrayList<String> origInfo, long coeff, Options options) {
        String newInfo = endHom.get(0)+"0-"+(endHom.size()-1);
        endHom.remove(0);
        boolean aborted = (newInfo.indexOf('a')>=0);
        int i = origInfo.size()-1;
        while (i>=0) {
            if (origInfo.get(i).charAt(0) == redChar) {
                String infoString = origInfo.get(i);
                if (aborted | keepTheInfo(infoString, coeff, options)) {
                    int st = Integer.parseInt(infoString.substring(infoString.lastIndexOf('.')+1, infoString.lastIndexOf('-')));
                    int en = Integer.parseInt(infoString.substring(infoString.lastIndexOf('-')+1));
                    int be = endHom.size();
                    for (int j = st; j < en; j++) endHom.add(oldInfo.get(j));
                    origInfo.set(i, infoString.substring(0, infoString.lastIndexOf('.')+1)+be+"-"+endHom.size());
                }
                else origInfo.remove(i);
            }
            i--;
        }
        origInfo.add(newInfo);
        oldInfo.clear();
        for (String info : endHom) oldInfo.add(info);
    }
    
    private boolean keepTheInfo(String info, long coeff, Options options) {
        if (coeff == 0) return false;
        long rng = Long.parseLong(info.substring(1,info.indexOf('.')));
        if (coeff == 1) {
            return rng != 1;
        }
        if (coeff < 0) {
            if (rng == 1) return false;
            ArrayList<Integer> primes = getPrimes(coeff, options.getPrimes());
            if (rng > 1) {
                int[] pap = primeAndPower((int) rng, options);
                return primes.contains(pap[0]);
            }
            if (rng < 0) {
                ArrayList<Integer> altPrimes = getPrimes(rng, options.getPrimes());
                return !betterThan(altPrimes,primes);
            }
        }// now coeff >1
        if (rng <= 1) return true;
        int[] pap = primeAndPower((int) coeff, options);
        int[] pop = primeAndPower((int) rng, options);
        if (pap[0] != pop[0]) return true;
        return pap[1] < pop[1];
    }
    
    private int[] primeAndPower(int rng, Options options) {
        int[] pap = new int[2];
        boolean found = false;
        int i = 0;
        while (!found) {
            int p = options.getPrimes().get(i);
            if (rng % p == 0) found = true;
            else i++;
        }
        pap[0] = options.getPrimes().get(i);
        pap[1] = powerOf(rng, pap[0]);
        return pap;
    }
    
    private int[] primeAndPower(int rng) {
        int[] pap = new int[2];
        boolean found = false;
        int p = 2;
        while (!found) {
            if (rng % p == 0) found = true;
            else p++;
        }
        pap[0] = p;
        pap[1] = powerOf(rng, p);
        return pap;
    }
    
    private int powerOf(int rng, int p) {
        int i = 1;
        while (rng /p != 0) {
            rng = rng / p;
            i++;
        }
        return i;
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
    
    public ArrayList<Integer> getPrimes(long rng, ArrayList<Integer> primes) {
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

    public int bltShift(boolean reduced) {
        if (reduced) {
            if (redBLT == null) return 0;
            return redBLT.size();
        }
        if (unredBLT == null) return 0;
        return unredBLT.size();
    }
    
    private ArrayList<String> relevantStrings(int typ) {
        ArrayList<String> rel = unredBLT;
        if (typ == 1) rel = redBLT;
        if (typ == 2) rel = sltTypeThree;
        if (typ == 3) rel = sltTypeTwo;
        if (typ == 4) rel = sltTypeOne;
        if (typ == 5) rel = sltTypeOneRed;
        if (typ == 6) rel = sltTypeX;
        if (typ == 7) rel = sltTypeXSq;
        return rel;
    }
    
    public Integer nonStandardS(String chr, int typ) {
        ArrayList<String> rel = relevantStrings(typ);
        int[] se = relevantLine(chr, rel);
        if (se == null) return null;
        Integer sval = 0;
        for (int i = se[0]; i <= se[1]; i++) {
            Integer[] bq = getZeroBetti(rel.get(i));
            if (bq != null) sval = sval + bq[0] * bq[1];
        }
        return sval;
    }
    
    public String speSeqHomZeroPolynomial(String chr, int typ) {
        ArrayList<String> rel = relevantStrings(typ);
        int[] se = relevantLine(chr, rel);
        if (se == null) return null;
        ArrayList<String> homZeros = new ArrayList<String>();
        for (int i = se[0]; i <= se[1]; i++) {
            String ln = zeroBetti(rel.get(i));
            if (ln != null) homZeros.add(ln);
        }
        String poly = homZeros.get(0);
        for (int i = 1; i < homZeros.size(); i++) poly = poly + " + "+homZeros.get(i);
        return poly;
    }
    
    private int[] relevantLine(String chr, ArrayList<String> rels) {
        String line = theLine(chr, rels);
        if (line == null) return null;
        int lc = line.lastIndexOf(":");
        line = line.substring(lc+1);
        int c = line.indexOf(".");
        lc = line.lastIndexOf(".");
        int start = Integer.parseInt(line.substring(c+1, lc));
        int end = Integer.parseInt(line.substring(lc+1));
        return new int[] {start, end};
    }
    
    private Integer[] getZeroBetti(String line) {
        int pos = line.indexOf("h0b");
        if (pos < 0) return null;
        int h = line.indexOf("h");
        int q = Integer.parseInt(line.substring(1, h));
        line = line.substring(pos+3);
        pos = line.indexOf("h");
        int b;
        if (pos < 0) b = Integer.parseInt(line);
        else b = Integer.parseInt(line.substring(0, pos));
        return new Integer[] {b, q};
    }
    
    private String zeroBetti(String line) {
        Integer[] bq = getZeroBetti(line);
        if (bq == null) return null;
        if (bq[0] > 1) return bq[0]+" q^"+bq[1];
        return "q^"+bq[1];
    }
    
    public int[] bltInfo(String chr, int typ) {
        ArrayList<String> rel = relevantStrings(typ);
        String line = theLine(chr, rel);
        if (line == null) return null;
        line = line.substring(line.indexOf(".")+1);
        ArrayList<Integer> theNumbers = new ArrayList<Integer>();
        while (line != null) {
            int dot = line.indexOf(".");
            if (dot == -1) break;
            int col = line.indexOf(":");
            if (col == -1) col = line.length();
            theNumbers.add(Integer.valueOf(line.substring(0, dot)));
            theNumbers.add(Integer.valueOf(line.substring(dot+1, col)));
            line = line.substring(col);
            line = line.substring(line.indexOf(".")+1);
        }
        int[] theArray = new int[theNumbers.size()];
        for (int i = 0; i < theArray.length; i++) theArray[i] = theNumbers.get(i);
        return theArray;
    }
    
    private String theLine(String chr, ArrayList<String> rel) {
        if (rel == null) return null;
        int i = 0;
        boolean found = false;
        while (!found && i < rel.size()) {
            String line = rel.get(i);
            if (chr.equals(bltCharacteristic(line))) found = true;
            else i = bltNextLine(line);
        }
        if (found) return rel.get(i);
        return null;
    }
    
    private String bltCharacteristic(String line) {
        int pos = line.indexOf(":");
        return line.substring(0, pos);
    }
    
    private int bltNextLine(String line) {
        int pos = line.lastIndexOf(".");
        return 1+Integer.parseInt(line.substring(pos+1));
    }
    
    public ArrayList<String> bltCharacteristics(int typ) {
        ArrayList<String> theChars = new ArrayList<String>();
        ArrayList<String> theStrings = relevantStrings(typ);
        if (theStrings != null) {
            int i = 0;
            while (i < theStrings.size()) {
                String line = theStrings.get(i);
                theChars.add(bltCharacteristic(line));
                i = bltNextLine(line);
            }
        }
        Collections.sort(theChars);
        return theChars;
    }
    
    public Integer relBltPages(String chr, int typ) {
        int[] points = bltInfo(chr, typ);
        if (points == null) return null;
        ArrayList<String> theStrings = relevantStrings(typ);
        Integer count = 1;
        int i = 1;
        int fst = points[0];
        int fen = points[1];
        while (i < points.length/2) {
            int nst = points[2*i];
            int nen = points[2*i+1];
            if (fen - fst != nen - nst) count++;
            else if (notSamePage(theStrings, fst, fen, nst)) count++;
            fst = nst;
            fen = nen;
            i++;
        }
        return count;
    }
    
    private boolean notSamePage(ArrayList<String> theStrings, int fa, int fb, int sa) {
        for (int i = 0; i <= fb - fa; i++) {
            if (!theStrings.get(fa+i).equals(theStrings.get(sa+i))) return true;
        }
        return false;
    }
    
    public ArrayList<Integer> bltEPages(String chr, int typ) {
        ArrayList<Integer> theEs = new ArrayList<Integer>();
        ArrayList<String> theStrings = relevantStrings(typ);
        String line = theLine(chr, theStrings);
        if (line != null) {
            int ePos = line.lastIndexOf("e");
            int dot = ePos+line.substring(ePos).indexOf(".");
            int k = Integer.parseInt(line.substring(ePos+1, dot));
            for (int i = 1; i <= k; i++) theEs.add(i);
        }
        return theEs;
    }
    
    public ArrayList<Integer> torsionInGraded() {
        ArrayList<Integer> tors = new ArrayList<Integer>();
        if (grsinv == null) return tors;
        String from = grsinv;
        while (from.contains("(")) {
            ArrayList<Integer> trs = getTorsionFrom(from);
            for (Integer t : trs) tors.add(t);
            from = from.substring(from.indexOf(")"));
        }
        return tors;
    }
    
    private ArrayList<Integer> getTorsionFrom(String from) {
        ArrayList<Integer> tors = new ArrayList<Integer>();
        String stuff = from.substring(from.indexOf("(")+1, from.indexOf(")"))+",";
        while (stuff.contains(",")) {
            int a = stuff.indexOf(",");
            int t = Integer.parseInt(stuff.substring(0, a));
            tors.add(t);
            stuff = stuff.substring(a+1);
        }
        return tors;
    }

    public ArrayList<Integer> getStableQs(boolean odd, boolean red) {
        ArrayList<Integer> info = khovInfoNumbers(odd, red);
        if (info == null) return null;
        int p = getRelPower(info);
        if (p == -1) return null;
        HomologyInfo hom = new HomologyInfo(2l, 1);
        if (p == 0 && odd) hom = integralOddKhHomology();
        if (p == 0 && !odd) hom = integralKhovHomology(red);
        if (p > 0 && odd) hom = modOddKhHomology(p);
        if (p > 0 && !odd) hom = modKhovHomology(red, p);
        if (odd && !red) hom = hom.doubleHom();
        HomologyInfo mHom = hom.modHomFrom(2, 1);
        ArrayList<Integer> qs = new ArrayList<Integer>();
        for (QuantumCohomology qcoh : mHom.getHomologies()) {
            int[] wmx = qcoh.widthAndMax();
            if (wmx[0] > 2) {
                qs.add(qcoh.qdeg());
                qs.add(wmx[1]);
                qs.add(wmx[2]);
            }
        }
        ArrayList<Integer> jqs = justTheQs(qs);
        int mpower = this.maxTorsionPower(jqs, 2, odd, red);
        qs.add(0, mpower);
        return qs;
    }
    
    private ArrayList<Integer> khovInfoNumbers(boolean odd, boolean red) {
        ArrayList<String> strings = khovInfo;
        char start = 'u';
        if (red) start = 'r';
        if (odd) {
            strings = okhovInfo;
            start = 'o';
        }
        if (strings == null || strings.isEmpty()) return null;
        ArrayList<Integer> numbers = new ArrayList<Integer>();
        for (String str : strings) {
            if (str.charAt(0) == start) {
                int dot = str.indexOf('.');
                int val = Integer.parseInt(str.substring(1, dot));
                numbers.add(val);
            }
        }
        return numbers;
    }

    private int getRelPower(ArrayList<Integer> info) {
        if (info.contains(0)) return 0;
        /*int i = 0;
        int p = info.get(0);
        while (i < info.size()) {
            if (p % 2 == 0) return p;
            i++;
            if (i < info.size()) p = info.get(i);
        }// */
        return -1;
    }
    
    public int maxTorsionPower(ArrayList<Integer> qs, int prime, boolean odd, boolean red) {
        ArrayList<Integer> info = khovInfoNumbers(odd, red);
        if (info == null) return -1;
        //int p = getRelPower(info);
        HomologyInfo hom;
        if (!odd) hom = integralKhovHomology(red);
        else hom = integralOddKhHomology();
        if (odd && !red) hom = hom.doubleHom();
        return hom.maxTorsion(qs, prime);
    }
    
    private ArrayList<Integer> justTheQs(ArrayList<Integer> relQs) {
        ArrayList<Integer> qs = new ArrayList<Integer>();
        for (int i = 0; i < relQs.size()/3; i++) {
            qs.add(relQs.get(3*i));
        }
        return qs;
    }
    
    public int getRelevant(int s) {
        ArrayList<Integer> info = khovInfoNumbers(false, false);
        if (info == null) return -1;
        int p = getRelPower(info);
        if (p == -1) return -1;
        HomologyInfo hom = integralKhovHomology(false);
        HomologyInfo mHom = hom.modHomFrom(2, 1);
        QuantumCohomology sPlusOne = mHom.getHomology(s+1);
        QuantumCohomology sMinusOne = mHom.getHomology(s-1);
        int[] widthPlus = sPlusOne.widthAndMax();
        int[] widthMinus = sMinusOne.widthAndMax();
        int rel = 0;
        if (widthPlus[1] <= -2) rel = 1;
        if (widthPlus[2] >= 2) rel = rel + 8;
        if (widthMinus[1] <= -2) rel = rel + 2;
        if (widthMinus[2] >= 2) rel = rel + 4;
        return rel;
    }
    
    public Integer reducedSlTSS(int chr) {
        if (sltTypeOneRed == null) return null;
        int[] info = bltInfo(String.valueOf(chr), 5);
        String qdeg = sltTypeOneRed.get(info[info.length-1]);
        int st = qdeg.indexOf("h");
        return Integer.parseInt(qdeg.substring(1, st))/2;
    }
    
    public String xtortion(int chr) {
        if (redBLT == null) return null;
        int k = bltEPages(String.valueOf(chr), 1).size();
        if (k > 0) {
            return String.valueOf(k-1);
        }
        return null;
    }

    public int[] getCmif() {
        if (cmpinv == null) return null;
        int s = sInvariant(0);
        String data = cmpinv.substring(0, cmpinv.length()-1)+", "+s+", "+s+")";
        return arrayFromString(data);
    }

    public boolean showSlThreeButton() {
        return sltTypeThree != null;
    }

    public boolean showSlTwoButton() {
        return sltTypeTwo != null;
    }
    
    public boolean showSlTwButton(boolean sq) {
        if (sq) return sltTypeXSq != null;
        return sltTypeX != null;
    }

    public boolean showSlOneButton(boolean red) {
        if (!red) return sltTypeOne != null;
        return sltTypeOneRed != null;
    }
    
    public boolean containsLink(Link link) {
        for (Link oLink : links) if (oLink.agreesWith(link)) return true;
        return false;
    }
}
