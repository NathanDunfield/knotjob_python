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

import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class CompFilter implements Filter {

    private String name;
    private final boolean bddAbove;
    private final int lowerBound;
    private final int upperBound;
    
    public CompFilter(String nm, boolean bba, int lb, int ub) {
        name = nm;
        bddAbove = bba;
        lowerBound = lb;
        upperBound = ub;
    }
    
    @Override
    public boolean linkIsFiltered(LinkData link) {
        int c = link.chosenLink().components();
        boolean okay = true;
        if (c < lowerBound) okay = false;
        if (bddAbove) {
            if (c > upperBound) okay = false;
        }
        return okay;
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
