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

package knotjob.homology;

import java.util.ArrayList;

/**
 *
 * @author Dirk
 */
public class Diagram {
    
    public ArrayList<Integer> paths;
    public ArrayList<Integer> circles;

    public Diagram(int i, int j) {
        paths = new ArrayList<Integer>(2);
        paths.add(i);
        paths.add(j);
        circles = new ArrayList<Integer>(0);
    }

    public Diagram() {
        paths = new ArrayList<Integer>();
        circles = new ArrayList<Integer>(2);
    }
    
    public void output() {
        System.out.println("Paths : "+paths+"  Circles : "+circles);
    }
}
