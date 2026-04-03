/*

Copyright (C) 2024 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.polynomial.moy;

/**
 *
 * @author Dirk
 */
public class Vertex {
    
    private final int label;
    private final int typ;
    
    // typ is type of vertex, with 0 = inward pointing vertex, 1 = outward pointing vertex
    // 2 = interior Vertex (should then be an IntVertex).
    
    public Vertex(int lab, int ty) {
        label = lab;
        typ = ty;
    }
    
    public boolean equals(Vertex vt) {
        return label == vt.label;
    }
    
    @Override
    public String toString() {
        return "("+label+", "+typ+")";
    }
    
    public int theType() {
        return typ;
    }
    
    public int theLabel() {
        return label;
    }
    
}
