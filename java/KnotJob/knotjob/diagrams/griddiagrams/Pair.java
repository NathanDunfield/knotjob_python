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

package knotjob.diagrams.griddiagrams;

/**
 *
 * @author Dirk
 */
public class Pair implements Comparable<Pair> {
    
    int n;
    int dir;
    int dist;
    boolean samepos;

    public Pair(int a,int di, int d) {
        n = a;
        dir = di;
        dist = d;
        samepos = false;
    }

    @Override
    public int compareTo(Pair o) {
        return dist - o.dist;
    }
}
