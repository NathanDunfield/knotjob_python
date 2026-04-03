/*

Copyright (C) 2021 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class SignFilter implements Filter {

    private final boolean diffFromRas;
    private final boolean det;
    private final boolean bddBelow;
    private final boolean bddAbove;
    private final int lowerBound;
    private final int upperBound;
    private String name;
    
    public SignFilter(String nm, boolean dfR, boolean dt, boolean bddb, boolean bdda, 
            int lb, int ub) {
        diffFromRas = dfR;
        det = dt;
        bddBelow = bddb;
        bddAbove = bdda;
        lowerBound = lb;
        upperBound = ub;
        name = nm;
    }
    
    @Override
    public boolean linkIsFiltered(LinkData link) {
        if (diffFromRas) return differentFromRasmussen(link);
        if (det) {
            if (bddBelow && bddAbove && lowerBound > upperBound) return determinantSquare(link);
            return determinantInBounds(link);
        }
        if (bddBelow && bddAbove && lowerBound > upperBound) return foxMilnor(link);
        return signatureInBounds(link);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String nm) {
        name = nm;
    }

    private boolean differentFromRasmussen(LinkData link) {
        if (link.signature == null) return false;
        int[][] sinv = link.sInvariants();
        int sgn = Integer.parseInt(link.signature);
        boolean diff = false;
        int i = 0;
        while (!diff && i < sinv.length) {
            if (sgn != sinv[i][1]) diff = true;
            i++;
        }
        return diff;
    }

    private boolean determinantInBounds(LinkData link) {
        boolean okay = true;
        if (link.determinant == null) return false;
        int dt = Integer.parseInt(link.determinant);
        if (bddBelow && dt < lowerBound) okay = false;
        if (bddAbove && dt > upperBound) okay = false;
        return okay;
    }

    private boolean signatureInBounds(LinkData link) {
        boolean okay = true;
        if (link.signature == null) return false;
        int sgn = Integer.parseInt(link.signature);
        if (bddBelow && sgn < lowerBound) okay = false;
        if (bddAbove && sgn > upperBound) okay = false;
        return okay;
    }

    private boolean determinantSquare(LinkData link) {
        if (link.determinant == null) return false;
        int dt = Integer.parseInt(link.determinant);
        int root = (int) Math.sqrt(dt);
        return dt == root * root;
    }

    private boolean foxMilnor(LinkData link) {
        if (link.alex == null) return false;
        return link.alex.contains("F");
    }
    
}
