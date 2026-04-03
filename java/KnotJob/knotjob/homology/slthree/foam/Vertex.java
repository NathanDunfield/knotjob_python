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

package knotjob.homology.slthree.foam;

/**
 *
 * @author Dirk
 */
public class Vertex {

    private final int vName;
    private final int vType; // 0 : boundary pos, 1 : boundary neg, 2 : interior, 3 : source, 4 : sink
    
    public Vertex(int u, int i) {
        vName = u;
        vType = i;
    }
    
    public int getName() {
        return vName;
    }
    
    @Override
    public String toString() {
        return "V("+vName+")";// +", "+vType
    }

    public int getType() {
        return vType;
    }
    
}
