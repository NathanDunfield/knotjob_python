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

package knotjob.diagrams;

/**
 *
 * @author Dirk
 */
public class Triangle {
    
    Vertex fvert,svert,tvert;
    boolean rot;

    public Triangle(Vertex get, Vertex get0, Vertex get1,boolean ro) {
        fvert = get;
        svert = get0;
        tvert = get1;
        rot = ro;
    }
}
