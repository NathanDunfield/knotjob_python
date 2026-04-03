/*

Copyright (C) 2019-20 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.filters;

import java.math.BigInteger;
import java.util.ArrayList;
import knotjob.homology.HomologyInfo;
import knotjob.homology.Homology;
import knotjob.homology.QuantumCohomology;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class KhovFilter implements Filter {

    private String name;
    private final int style;
    private final int homStyle; // 1 = odd, 2 = sl_3
    private final boolean reduced;
    private final boolean rational;
    private final boolean bddAbove;
    private HomologyInfo hInfo;
    private int hdeg;
    private final BigInteger lowerBound;
    private final BigInteger upperBound;
    private final ArrayList<Integer> primes;
    
    public KhovFilter(String nme, int hs, boolean red, boolean ba, int lb, int ub, ArrayList<Integer> prms) {// Torsion filter
        name = nme;
        homStyle = hs;
        reduced = red;
        rational = false;
        bddAbove = ba;
        lowerBound = BigInteger.valueOf(lb);
        upperBound = BigInteger.valueOf(ub);
        primes = prms;
        hInfo = null;
        style = 0;
    }
    
    public KhovFilter(String nme, ArrayList<Integer> prms, int hs, boolean red, boolean ba, int lb, int ub) {// Width Filter
        name = nme;
        homStyle = hs;
        reduced = red;
        rational = false;
        bddAbove = ba;
        lowerBound = BigInteger.valueOf(lb);
        upperBound = BigInteger.valueOf(ub);
        primes = prms;
        hInfo = null;
        style = 1;
    }
    
    public KhovFilter(String nme, int hs, boolean rat, boolean red, LinkData link, ArrayList<Integer> prms) {// Comparison Filter;
        name = nme;
        homStyle = hs;
        reduced = red;
        rational = rat;
        primes = prms;
        ArrayList<String> theStrings = link.unredKhovHom;
        ArrayList<String> theInfo = link.khovInfo;
        if (reduced) theStrings = link.redKhovHom;
        char reduz = 'u';
        if (reduced) reduz = 'r';
        if (homStyle == 1) {
            reduz = 'o';
            theStrings = link.oddKhovHom;
            theInfo = link.okhovInfo;
        }
        if (homStyle == 2) {
            theStrings = link.unredSlT;
            if (reduced) theStrings = link.redSlT;
            theInfo = link.sltInfo;
        }
        HomologyInfo info = link.integralHomology(reduz, theStrings, theInfo);
        if (info == null) info = link.approximateHomology(reduz, primes, theInfo, theStrings);
        hInfo = info;
        style = 2;
        bddAbove = false;
        lowerBound = BigInteger.ONE;
        upperBound = lowerBound;
    }
    
    public KhovFilter(String nme, int hs, boolean rat, boolean red, ArrayList<Integer> prms) {
        name = nme;
        homStyle = hs;
        reduced = red;
        rational = rat;
        primes = prms;
        hInfo = null;
        bddAbove = false;
        lowerBound = BigInteger.ONE;
        upperBound = lowerBound;
        style = 3;
    }

    public KhovFilter(String nme, ArrayList<Integer> prms, int hs, boolean red, boolean ba, int lb, int ub, int hd) 
    { // Betti Filter
        name = nme;
        homStyle = hs;
        reduced = red;
        rational = true;
        bddAbove = ba;
        lowerBound = BigInteger.valueOf(lb);
        upperBound = BigInteger.valueOf(ub);
        primes = prms;
        style = 4;
        hdeg = hd;
    }
    
    @Override
    public boolean linkIsFiltered(LinkData link) {
        ArrayList<String> theStrings = link.unredKhovHom;
        ArrayList<String> theInfo = link.khovInfo;
        if (reduced) theStrings = link.redKhovHom;
        char reduz = 'u';
        if (reduced) reduz = 'r';
        if (homStyle == 1) {
            reduz = 'o';
            theStrings = link.oddKhovHom;
            theInfo = link.okhovInfo;
        }
        if (homStyle == 2) {
            theStrings = link.unredSlT;
            if (reduced) theStrings = link.redSlT;
            theInfo = link.sltInfo;
        }
        HomologyInfo homInfo = link.integralHomology(reduz, theStrings, theInfo);
        if (homInfo == null) {
            homInfo = link.approximateHomology(reduz, primes, theInfo, theStrings);
            if (homInfo == null) return false;
        }
        if (style == 0) return torsionOfSize(homInfo);
        if (style == 1) return widthOfSize(homInfo);
        if (style == 2) return sameAs(homInfo);
        if (style == 3) return mirrorInvariant(homInfo);
        if (style == 4) return bettiOfSize(homInfo);
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    private boolean bettiOfSize(HomologyInfo homInfo) {
        int betti = 0;
        for (QuantumCohomology coh : homInfo.getHomologies()) {
            for (Homology hom : coh.getHomGroups()) {
                if (hdeg == hom.hdeg()) betti = betti+hom.getBetti();
            }
        }
        int lb = lowerBound.intValue();
        int ub = upperBound.intValue();
        return (lb <= betti & betti <= ub);
    }
    
    private boolean torsionOfSize(HomologyInfo homInfo) {
        boolean found = false;
        int i = 0;
        while (i < homInfo.getHomologies().size() && !found) {
            QuantumCohomology qCoh = homInfo.getHomologies().get(i);
            int j = 0;
            while (j < qCoh.getHomGroups().size() && !found) {
                Homology hom = qCoh.getHomGroups().get(j);
                for (BigInteger big : hom.getTorsion()) {
                    if (big.compareTo(lowerBound)>=0) {
                        if (!bddAbove || big.compareTo(upperBound) <= 0) found = true;
                    }
                }
                j++;
            }
            i++;
        }
        return found;
    }

    private boolean widthOfSize(HomologyInfo homInfo) {
        int width = homInfo.width();
        int lb = lowerBound.intValue();
        int ub = upperBound.intValue();
        boolean okay = false;
        if (width >= lb) {
            if (!bddAbove || width <= ub) okay = true;
        }
        return okay;
    }

    private boolean sameAs(HomologyInfo homInfo) {
        if (hInfo.getHomologies().size() != homInfo.getHomologies().size()) return false;
        boolean same = true;
        int i = 0;
        while (same && i < hInfo.getHomologies().size()) {
            QuantumCohomology qOrg = hInfo.getHomologies().get(i);
            QuantumCohomology qSec = homInfo.getHomologies().get(i);
            if ((qOrg.qdeg() != qSec.qdeg()) || (qOrg.getHomGroups().size() != qSec.getHomGroups().size()) ) same = false;
            else {
                int j = 0;
                while (same && j < qOrg.getHomGroups().size()) {
                    Homology hOrg = qOrg.getHomGroups().get(j);
                    Homology hSec = qSec.getHomGroups().get(j);
                    if ((hOrg.hdeg() != hSec.hdeg()) || (hOrg.getBetti() != hSec.getBetti())) same = false;
                    else if (!rational) {
                        if (!sameTorsion(hOrg.getTorsion(),hSec.getTorsion())) same = false;
                    }
                    j++;
                }
            }
            i++;
        }
        return same;
    }
    
    private boolean sameTorsion(ArrayList<BigInteger> listOne, ArrayList<BigInteger> listTwo) {
        if (listOne.size() != listTwo.size()) return false;
        boolean same = true;
        int i = 0;
        while (same && i < listOne.size()) {
            if (!listOne.get(i).equals(listTwo.get(i))) same = false;
            i++;
        }
        return same;
    }
    
    private boolean mirrorInvariant(HomologyInfo homInfo) {
        hInfo = homInfo.mirror();
        return sameAs(homInfo);
    }
    
    @Override
    public void setName(String nm) {
        name = nm;
    }
    
    @Override
    public String toString() {
        String title = "2."+style+".";
        if (style < 2 || style == 4) {
            title = title+homStyle+".";
            //if (homStyle == 1) title = title+"1.";
            //else title = title+"0.";
            if (reduced) title = title+"1.";
            else title = title+"0.";
            if (bddAbove) title = title+"1.";
            else title = title+"0.";
            title = title+lowerBound+"."+upperBound+"."+hdeg+".";
        }
        if (style == 3) {
            title = title+homStyle+".";
            //if (odd) title = title+"1.";
            //else title = title+"0.";
            if (rational) title = title+"1.";
            else title = title+"0.";
            if (reduced) title = title+"1.";
            else title = title+"0.";
            title = title+"0.0.0.";
        }
        return title;
    }
    
}
