/*

Copyright (C) 2025 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

import java.util.ArrayList;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class SlTFilter implements Filter {

    private String name;
    private final int typ;
    private final String wString;
    private final boolean bddBelow;
    private final int lowerBound;
    private final boolean bddAbove;
    private final int upperBound;
    private final boolean pageCheck;
    private final boolean relPages;
    private final boolean diffCheck;
    private final boolean niceCheck;
    
    public SlTFilter(String nm, String w, int tp, int lb, int ub, boolean bddb, 
            boolean bdda, boolean pc, boolean rl) {
        name = nm;
        wString = w;
        typ = tp;
        lowerBound = lb;
        upperBound = ub;
        bddBelow = bddb;
        bddAbove = bdda;
        pageCheck = pc;
        relPages = rl;
        diffCheck = false;
        niceCheck = false;
    }
    
    public SlTFilter(String nm, String w, int tp, boolean dc, boolean nc) {
        name = nm;
        wString = w;
        typ = tp;
        bddBelow = false;
        bddAbove = false;
        lowerBound = 0;
        upperBound = 0;
        pageCheck = false;
        relPages = false;
        diffCheck = dc;
        niceCheck = nc;
    }
    
    @Override
    public boolean linkIsFiltered(LinkData link) {
        if (diffCheck) return hasDifferent(link);
        if (pageCheck) return checkPages(link);
        if (niceCheck) return checkNiceness(link);
        return boundCheck(link);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String nm) {
        name = nm;
    }

    private boolean hasDifferent(LinkData link) {
        ArrayList<String> symbols = link.bltCharacteristics(typ);
        if (typ == 6) return dealWithTypeSix(link, symbols);
        if (symbols.size() <= 1) return false;
        if (typ == 3) return comparisons(link, link.sltTypeTwo, new ArrayList<String>(),
                symbols);
        return comparisons(link, link.sltTypeXSq, new ArrayList<String>(), symbols);
    }
    
    private boolean dealWithTypeSix(LinkData data, ArrayList<String> symbols) {
        int[] points = data.bltInfo("0", 3);
        int types = symbols.size();
        ArrayList<String> zeroData = new ArrayList<String>();
        if (points != null) {
            types++;
            for (int u = 0; u < (points.length-2)/2; u++) {
                for (int j = points[2*u]; j <= points[2*u+1]; j++) 
                    zeroData.add(data.sltTypeTwo.get(j));
                for (int j = points[2*u]; j <= points[2*u+1]; j++) 
                    zeroData.add(data.sltTypeTwo.get(j));
            }
            for (int j = points[points.length-2]; j <= points[points.length-1]; j++) 
                zeroData.add(data.sltTypeTwo.get(j));
        }
        if (types <= 1) return false;
        return comparisons(data, data.sltTypeX, zeroData, symbols);
    }
    
    private boolean comparisons(LinkData data, ArrayList<String> seqData, 
            ArrayList<String> zeroData, ArrayList<String> chars) {
        ArrayList<ArrayList<String>> allData = new ArrayList<ArrayList<String>>();
        for (String chr : chars) {
            int[] points = data.bltInfo(chr, typ);
            ArrayList<String> chrData = new ArrayList<String>();
            for (int u = points[0]; u <= points[points.length-1]; u++) 
                chrData.add(seqData.get(u));
            allData.add(chrData);
        }
        int size = allData.get(0).size();
        for (int u = 1; u < allData.size(); u++) 
            if (allData.get(u).size() != size) return true;
        for (int i = 0; i < size; i++) {
            String dude = allData.get(0).get(i);
            for (int u = 1; u < allData.size(); u++) {
                if (!dude.equals(allData.get(u).get(i))) return true;
            }
        }
        if (zeroData.isEmpty()) return false;
        if (size != zeroData.size()) return true;
        for (int i = 0; i < size; i++) {
            String dude = allData.get(0).get(i);
            if (!dude.equals(zeroData.get(i))) return true;
        }
        return false;
    }

    private boolean checkPages(LinkData link) {
        Integer pages = link.bltEPages(wString, typ).size();
        if (relPages) pages = link.relBltPages(wString, typ);
        if (pages == null || pages < lowerBound) return false;
        if (!bddAbove) return true;
        return (pages <= upperBound);
    }

    private boolean boundCheck(LinkData link) {
        Integer sInv = link.nonStandardS(wString, typ);
        if (sInv == null) return false;
        if (bddAbove && sInv > upperBound) return false;
        return !(bddBelow && sInv < lowerBound);
    }

    private boolean checkNiceness(LinkData link) {
        String poly = link.speSeqHomZeroPolynomial(wString, typ);
        if (poly == null) return false;
        ArrayList<Integer> theQs = new ArrayList<Integer>();
        while (poly.contains("q")) {
            if (!poly.startsWith("q")) return false;
            theQs.add(getFirstQ(poly));
            int a = poly.indexOf("+");
            if (a < 0) break;
            poly = poly.substring(a+2);
        }
        if (theQs.size() != 3) return false;
        int q = theQs.get(0)+2;
        for (int i : theQs) {
            if (i != q - 2) return false;
            q = i;
        }
        return true;
    }

    private int getFirstQ(String poly) {
        int a = poly.indexOf("^")+1;
        int b = poly.indexOf(" +");
        if (b < 0) return Integer.parseInt(poly.substring(a));
        return Integer.parseInt(poly.substring(a, b));
    }
    
}
