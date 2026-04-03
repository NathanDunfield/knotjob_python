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

import java.util.ArrayList;

/**
 *
 * @author Dirk
 */
public class MoyWeb {
    
    private final ArrayList<Triple<Integer>> triples;
    private final ArrayList<Integer> edges;
    
    public MoyWeb(Integer[][] trps, int[] eds) {
        triples = new ArrayList<Triple<Integer>>();
        edges = new ArrayList<Integer>();
        for (Integer[] t : trps) triples.add(new Triple<Integer>(t));
        for (int e : eds) edges.add(e);
    }
    
    public String output(MoyCache cache) {
        String str = "[";
        for (int ed : edges) str = str+cache.getEdge(ed).toString()+", ";
        str = str.substring(0, str.length()-2)+"]";
        if (triples.isEmpty()) return str;
        str = str +"\n Triples: ";
        for (Triple<Integer> tr : triples) str = str + tr.toString();
        return str;
    }
    
    ArrayList<Integer> getEdges() {
        return edges;
    }
    
    ArrayList<Triple<Integer>> getTriples() {
        return triples;
    }
    
}
