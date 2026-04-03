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

import java.util.Objects;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class SInvFilter implements Filter {

    private final int nonConstS;
    private final boolean nonConstSqOne;
    private final boolean nonConstSqTwo;
    private final boolean nonStdtGrd;
    private final boolean even;
    private final boolean bddBelow;
    private final boolean bddAbove;
    private final int sqTwoCase; // 0 : odd/eps = 0; 1 : odd/eps = 1; 2 : even
    private final int lowerBound;
    private final int upperBound;
    private final int boundType;
    private final int boundChar;
    private String name;
    
    public SInvFilter(String nm, int ncS, boolean ncSq, boolean ev, boolean bddb, boolean bdda, 
            int lb, int ub) {
        nonConstS = ncS;
        nonConstSqOne = ncSq;
        nonStdtGrd = false;
        even = ev;
        bddBelow = bddb;
        bddAbove = bdda;
        lowerBound = lb;
        upperBound = ub;//upperBound == 1 means we're looking for BLSOdd
        name = nm;
        sqTwoCase = 0;
        nonConstSqTwo = false;
        boundType = -1;
        boundChar = -1;
    }
    
    public SInvFilter(String nm, boolean bddb, boolean bdda, 
            int lb, int ub, int tp, int ch) {
        nonConstS = 0;
        nonConstSqOne = false;
        nonStdtGrd = false;
        even = false;
        bddBelow = bddb;
        bddAbove = bdda;
        lowerBound = lb;
        upperBound = ub;
        name = nm;
        sqTwoCase = 0;
        nonConstSqTwo = false;
        boundType = tp;
        boundChar = ch;
    }
    
    public SInvFilter(String nm, int stcase) {
        nonStdtGrd = false;
        nonConstS = 0;
        nonConstSqOne = false;
        nonConstSqTwo = true;
        even = false;
        bddBelow = false;
        bddAbove = false;
        sqTwoCase = stcase;
        lowerBound = 0;
        upperBound = 0;
        name = nm;
        boundType = -1;
        boundChar = -1;
    }
    
    public SInvFilter(String nm, boolean nstd) {
        nonStdtGrd = nstd;
        nonConstS = 0;
        nonConstSqOne = false;
        nonConstSqTwo = false;
        even = false;
        bddBelow = false;
        bddAbove = false;
        sqTwoCase = 0;
        lowerBound = 0;
        upperBound = 0;
        name = nm;
        boundType = -1;
        boundChar = -1;
    }
    
    @Override
    public boolean linkIsFiltered(LinkData link) {
        boolean okay = false;
        if (nonStdtGrd) return linkHasNonStdtGrd(link);
        if (nonConstS > 7) return linkHasDiffsltred(link);
        if (nonConstS > 0) okay = linkHasNonConstS(link);
        if (nonConstSqOne) okay = linkHasNonConstSqOne(link);
        if (nonConstSqTwo) return linkHasNonConstSqTwo(link);
        if (nonConstS == 0 && !nonConstSqOne) okay = sInvInBounds(link);
        return okay;
    }
    
    private boolean sInvInBounds(LinkData link) {
        if (boundChar >= 0) return specBounds(link);
        int[][] sinvs = switch (boundType) {
            case 0 -> link.sInvariants();
            case 1 -> link.sSlTInvariants(false);
            default -> link.sSlTInvariants(true);
        };
        int i = 0;
        while (i < sinvs.length) {
            int s = sinvs[i][1];
            if (bddBelow && s < lowerBound) return false;
            if (bddAbove && s > upperBound) return false;
            i++;
        }
        return sinvs.length > 0;
    }
    
    private boolean specBounds(LinkData link) {
        Integer s = switch(boundType) {
            case 0 -> link.sInvariant(boundChar);
            case 1 -> link.sSlTInvariant(boundChar, false);
            default -> link.sSlTInvariant(boundChar, true);
        };
        if (s == null) return false;
        if (bddBelow && s < lowerBound) return false;
        return !(bddAbove && s > upperBound);// return false;
    }
    
    private boolean linkHasNonStdtGrd(LinkData link) {
        if (link.grsinv == null) return false;
        return link.grsinv.contains("(");
    }
    
    private boolean linkHasNonConstSqOne(LinkData link) {
        boolean okay = false;
        int[] sqOne = link.getSqOne(even);
        if (upperBound == 1) sqOne = link.getBLSOdd();
        if (upperBound == 2) sqOne = link.getSqOneSum();
        if (upperBound == 4) sqOne = link.getCmif();
        if (sqOne != null) {
            int c = sqOne[0];
            okay = false;
            if (sqOne[1] != c | sqOne[2] != c | sqOne[3] != c) okay = true;
        }
        return okay;
    }
    
    private boolean linkHasNonConstSqTwo(LinkData link) {
        boolean okay = false;
        int[] sqTwo = link.getSqTwo(sqTwoCase);
        if (sqTwo != null) {
            int c = sqTwo[0];
            okay = false;
            if (sqTwo[1] != c | sqTwo[2] != c | sqTwo[3] != c) okay = true;
        }
        return okay;
    }
    
    private boolean linkHasNonConstS(LinkData link) {
        boolean okay = false;
        int[][] sinv = new int[0][0];
        if ((nonConstS & 1) != 0) sinv = link.sInvariants();
        int[][] stnv = new int[0][0];
        if ((nonConstS & 2) != 0) stnv = link.sSlTInvariants(false);
        int[][] srnv = new int[0][0];
        if ((nonConstS & 4) != 0) srnv = link.sSlTInvariants(true);
        int[][] allinv = new int[sinv.length+stnv.length+srnv.length][2];
        for (int k = 0; k < sinv.length; k++) allinv[k][1] = sinv[k][1];
        for (int k = 0; k < stnv.length; k++) allinv[sinv.length+k][1] = stnv[k][1];
        for (int k = 0; k < srnv.length; k++) allinv[sinv.length+stnv.length+k][1] = srnv[k][1];
        if (allinv.length > 0) {
            int i = 1;
            okay = false;
            int c = allinv[0][1];
            while (i < allinv.length && !okay) {
                if (allinv[i][1] != c) okay = true;
                else i++;
            }
        }
        return okay;
    }
    
    private boolean linkHasDiffsltred(LinkData link) {
        Integer slt = link.sSlTInvariant(nonConstS - 8, false);
        Integer rlt = link.sSlTInvariant(nonConstS - 8, true);
        if (slt == null || rlt == null) return false;
        return !Objects.equals(slt, rlt);
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String nm) {
        name = nm;
    }
    
}
