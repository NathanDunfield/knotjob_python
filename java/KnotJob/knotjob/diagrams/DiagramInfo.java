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

import java.util.ArrayList;

/**
 *
 * @author Dirk
 */
public class DiagramInfo {
    
    private final ArrayList<ArrayList<Integer[]>> discs;
    int ignore;
    
    public DiagramInfo() {
        discs = new ArrayList<ArrayList<Integer[]>>();
        ignore = 0;
    }
    
    public void add(ArrayList<Integer[]> disc) {
        discs.add(disc);
    }
    
    public void setIgnore() {
        for (int i = 1; i < discs.size(); i++) if (discs.get(i).size()> discs.get(ignore).size()) ignore = i;
    }
    
    public int discNumber() {
        return discs.size();
    }
    
    public ArrayList<Integer[]> getDisc(int i) {
        return discs.get(i);
    }
    
    public int getIgnore() {
        return ignore;
    }
}
