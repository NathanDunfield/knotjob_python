/*

Copyright (C) 2020 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.evenkhov;

import java.util.ArrayList;
import knotjob.homology.Cache;
import knotjob.homology.Diagram;

/**
 *
 * @author Dirk
 */
public class EvenCache extends Cache {
    
    private final ArrayList<Integer> dotPwrs;
    private final ArrayList<ArrayList<int[]>> surgeries;
    private final long ttt;
    
    public EvenCache(int[] ends, int change) {
        super(ends, change);
        dotPwrs = new ArrayList<Integer>(2);
        surgeries = new ArrayList<ArrayList<int[]>>(0);
        dotPwrs.add(1);
        dotPwrs.add(2);
        ttt = ((long) 10000) * 10000000;
    }
    
    public EvenCache(ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts) {
        super(pEndpts);
        dotPwrs = new ArrayList<Integer>(pEndpts.size());
        surgeries = new ArrayList<ArrayList<int[]>>(3);
        int j = 1;
        for (int p : pEndpts) {
            dotPwrs.add(j);
            j = 2 * j;
        }
        ttt = ((long) 10000) * 10000000;
    }
    
    public EvenCache(ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, ArrayList<Integer> pDots) {
        super(pEndpts);
        dotPwrs = new ArrayList<Integer>(pEndpts.size());
        dotPts.clear();
        surgeries = new ArrayList<ArrayList<int[]>>(3);
        int j = 1;
        for (int p : pDots) {
            dotPts.add(p);
            dotPwrs.add(j);
            j = 2 * j;
        }
        ttt = ((long) 10000) * 10000000;
    }

    public ArrayList<Diagram> getDiagrams() {
        return diagrams;
    }
    
    @Override
    public Diagram getDiagram(int i) {
        return diagrams.get(i);
    }
    
    public ArrayList<ArrayList<Integer>> getPaths() {
        return paths;
    }
    
    public ArrayList<Integer> getPowrs() {
        return dotPwrs;
    }
    
    public void setPaths(ArrayList<ArrayList<Integer>> newPaths) {
        paths = newPaths;
    }
    
    void addDiagram(Diagram empty) {
        diagrams.add(empty);
    }
    
    ArrayList<Integer> getSurgeries(long surgery, int lastDiag) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        if (surgery == 0) return result;
        if (surgery == 1) {
            result.add(lastDiag);
            return result;
        }
        if (surgery % 10000 == 100) {
            result.add((int) surgery / 10000);
            result.add(lastDiag);
            return result;
        }
        int end = (int) (surgery % 10000);
        int po1 = (int) ((surgery%ttt)/10000);
        int po2 = (int) (surgery/ttt);
        if (end == 101) {
            result.add(po1);
            result.add(po2);
            result.add(lastDiag);
            return result;
        }
        if (end == 201) {
            int[] sur = surgeries.get(0).get(po1);
            result.add(sur[0]);
            result.add(po2);
            result.add(sur[1]);
            result.add(lastDiag);
            return result;
        }
        int e1 = end%100;
        int e2 = end/100;
        if (end >= 6000) System.out.println(end+" "+surgery+" "+surgeries.get(e1-2).size()+" "+surgeries.get(e2-2).size());
        int[] sur1 = surgeries.get(e1-2).get(po2);
        int[] sur2 = surgeries.get(e2-2).get(po1);
        for (int j = 0; j < e2; j++) {
            result.add(sur1[j]);
            result.add(sur2[j]);
        }
        if (e1>e2) result.add(sur1[e2]);
        result.add(lastDiag);
        return result;
    }
    
    long getSurgeries(ArrayList<Integer> surgery) {
        int size = surgery.size();
        if (size <= 1) return size;
        if (size == 2) return (surgery.get(0)*10000)+100;
        if (size == 3) return (surgery.get(0)*10000)+((long) surgery.get(1))* ttt + 101;
        if (size == 4) {
            int[] sur = new int[2];
            sur[0] = surgery.get(0);
            sur[1] = surgery.get(2);
            long pos = positionOf(sur,0);
            return pos * 10000 + ((long) surgery.get(1))* ttt + 201;
        }
        int hs2 = (size-1)/2;
        int hs1 = hs2 + (size-1)%2;
        int[] sur1 = new int[hs1];
        int[] sur2 = new int[hs2];
        for (int j = 0; j < hs1; j++) sur1[j] = surgery.get(2*j);
        for (int j = 0; j < hs2; j++) sur2[j] = surgery.get(2*j+1);
        if (hs1 == hs2) {
            long[] poss = positionsOf(sur1,sur2,hs1-2);
            return poss[1] * 10000 + poss[0] * ttt + hs2 * 100 + hs1;
        }
        long pos1 = positionOf(sur1,hs1-2);
        long pos2 = positionOf(sur2,hs2-2);
        return pos2 * 10000 + pos1 * ttt + hs2 * 100 + hs1;
    }
    
    private long positionOf(int[] sur, int i) {
        boolean found = false;
        int j = 0;
        while (surgeries.size() <= i) surgeries.add(new ArrayList<int[]>());
        while (!found && j < surgeries.get(i).size()) {
            if (agree(sur,surgeries.get(i).get(j))) found = true;
            else j++;
        }
        if (!found) surgeries.get(i).add(sur);
        return j;
    }
    
    private long[] positionsOf(int[] sur1, int[] sur2, int i) {
        boolean found1 = false;
        boolean found2 = false;
        int j = 0;
        while (surgeries.size() <= i) surgeries.add(new ArrayList<int[]>());
        long[] poss = new long[2];
        while (!(found1&found2) && j < surgeries.get(i).size() ) {
            if (!found1 && agree(sur1,surgeries.get(i).get(j))) {
                found1 = true;
                poss[0] = j;
            }
            if (!found2 && agree(sur2,surgeries.get(i).get(j))) {
                found2 = true;
                poss[1] = j;
            }
            j++;
        }
        if (!found1) {
            poss[0] = surgeries.get(i).size();
            surgeries.get(i).add(sur1);
        }
        if (!found2) {
            poss[1] = surgeries.get(i).size();
            surgeries.get(i).add(sur2);
        }
        return poss;
    }
    
    private boolean agree(int[] sur, int[] get) {
        boolean agree = true;
        int j = 0;
        while (agree && j < sur.length) {
            if (sur[j] != get[j]) agree = false;
            else j++;
        }
        return agree;
    }
    
}
