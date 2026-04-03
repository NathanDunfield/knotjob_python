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

package knotjob.filters;

import java.util.ArrayList;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class BLTFilter implements Filter {
    
    private String name;
    private final int typ;
    private final String characteristic;
    private final int lowerBound;
    private final boolean bddAbove;
    private final int upperBound;
    private final boolean constCheck;
    
    public BLTFilter(String nm, int tp, String chr, int lb, int ub, boolean bdd) {
        name = nm;
        typ = tp;
        characteristic = chr;
        lowerBound = lb;
        upperBound = ub;
        bddAbove = bdd;
        constCheck = false;
    }
    
    public BLTFilter(String nm, int tp) {
        name = nm;
        typ = tp;
        characteristic = "0";
        lowerBound = 0;
        upperBound = 0;
        bddAbove = true;
        constCheck = true;
    }

    @Override
    public boolean linkIsFiltered(LinkData link) {
        if (constCheck) return isNonConstant(link);
        int pages = link.bltEPages(String.valueOf(characteristic), typ).size();
        if (pages < lowerBound) return false;
        if (!bddAbove) return true;
        return (pages <= upperBound);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String nm) {
        name = nm;
    }
    
    private boolean isNonConstant(LinkData link) {
        ArrayList<String> chars = link.bltCharacteristics(typ);
        if (chars.size() <= 1) return false;
        int xn = Integer.parseInt(link.xtortion(Integer.parseInt(chars.get(0))));
        for (int i = 1; i < chars.size(); i++) {
            int x = Integer.parseInt(link.xtortion(Integer.parseInt(chars.get(i))));
            if (x != xn) return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        String title = "1.";
        if (typ == 1) title = title+"1.";
        else title = title+"0.";
        if (!constCheck) {
            title = title+characteristic+"."+lowerBound+"."+upperBound+".";
            if (bddAbove) title = title+"1.";
            else title = title+"0.";
        }
        return title;
    }
    
}
