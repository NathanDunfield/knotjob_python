/*

Copyright (C) 2023-24 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.polynomial.slthree;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Dirk
 */
public class Grid {
    
    private final ArrayList<Integer> paths;
    
    public Grid() {
        //super();
        paths = new ArrayList<Integer>();
    }
    
    public void addPath(int p) {
        paths.add(p);
    }
    
    public ArrayList<Integer> getPaths() {
        return paths;
    }
    
    public void sortPaths() {
        Collections.sort(paths);
    }
    
    public void output() {
        System.out.println("Paths : "+paths);
    }
    
}
