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

package knotjob.homology.oddkhov;

import java.util.ArrayList;

/**
 *
 * @author dirk
 */
public class SurgeryDiagram {
    
    private ArrayList<ArrayList<Integer>> circles;
    private final OddSurgery arc;
    
    public SurgeryDiagram(ArrayList<ArrayList<Integer>> crcs, OddSurgery first) {
        circles = crcs;
        arc = first;
    }
    
    public boolean split() {
        int i = circle(arc.getFPath());
        int j = circle(arc.getSPath());
        return (i == j);
    }
    
    private boolean split(OddSurgery surg) {
        int i = circle(surg.getFPath());
        int j = circle(surg.getSPath());
        return (i == j);
    }
    
    private int circle(int[] ind) {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (circles.get(i).contains(ind[0])) found = true;
            else i++;
        }
        return i;
    }

    public void setCircles(ArrayList<ArrayList<Integer>> crcs) {
        circles = crcs;
    }
    
    public boolean alternate(ArrayList<Integer> dottings) {
        //System.out.println(circles+" "+Arrays.toString(arc.getFPath())+" "+Arrays.toString(arc.getSPath())+" "+split()+" "+dottings.size());
        if (dottings.size() % 2 == 0) return false; // with an even number of dottings we don't alternate
        return (split());
        //if (!split()) return true; // if it's a merge, we have to alternate
        //return (dottings.size()%2 != 0); // if it's a split, it depends on the number of dots
    }
    
    public boolean alternate(OddSurgery surg) { // type A, X get false; type C, Y get true
        int fCircle = circle(arc.getFPath());
        int sCircle = circle(arc.getSPath());
        int tCircle = circle(surg.getFPath());
        int vCircle = circle(surg.getSPath());
        int circleNumber = numberOf(new int[] { fCircle, sCircle, tCircle, vCircle });
        //System.out.println(circleNumber+" "+circles+" "+fCircle+" "+sCircle+" "+tCircle+" "+vCircle);
        //System.out.println(Arrays.toString(arc.getFPath())+" "+Arrays.toString(arc.getSPath())+" "+Arrays.toString(surg.getFPath())
        //        +" "+Arrays.toString(surg.getSPath())+" "+arc.getTurn()+" "+surg.getTurn());
        if (circleNumber >= 3) return true; // this handles C.1, C.4, C.5
        if (circleNumber == 2) return alternateTwo(surg, fCircle, tCircle);
        return alternateOne(surg, fCircle);
    }

    private int numberOf(int[] collection) {
        ArrayList<Integer> set = new ArrayList<Integer>(4);
        for (int i : collection) if (!set.contains(i)) set.add(i);
        return set.size();
    }

    private boolean alternateTwo(OddSurgery surg, int circOne, int circTwo) { // two circles are involved in the surgery diagram with surg
        if (split() || split(surg)) return !(split() & split(surg)); // the cases A.1 and C.2
        return (circOne != circTwo); // the cases A.3 and C.3
    }

    private boolean alternateOne(OddSurgery surg, int circ) { // need to distinguish type X, Y, A.2
        int[] positions = positionsIn(surg, circles.get(circ));
        int[] order = orderOf(positions);
        if (order[1] == 1) return isTypeY(order); // checks if type X or type Y
        return false; // type A.2 
    }

    private int[] positionsIn(OddSurgery surg, ArrayList<Integer> circle) {
        int[] pos = new int[4];
        int s = circle.size();
        pos[0] = circle.indexOf(arc.getFPath(0));
        pos[1] = circle.indexOf(arc.getSPath(0));
        pos[2] = circle.indexOf(surg.getFPath(0));
        pos[3] = circle.indexOf(surg.getSPath(0));
        for (int i = 1; i < 4; i++) if (pos[i] < pos[0]) pos[i] = pos[i]+s; // after this, pos[0] is the smallest value
        return pos;
    }

    private int[] orderOf(int[] pos) {
        if (pos[1] < pos[2] && pos[1] < pos[3]) {
            if (pos[2] < pos[3]) return new int[] {1, -2, 2};
            else return new int[] {1, 2, -2};
        }
        if (pos[1] > pos[2] && pos[1] > pos[3]) {
            if (pos[2] < pos[3]) return new int[] {-2, 2, 1};
            else return new int[] {2, -2, 1};
        }
        if (pos[2] < pos[3]) return new int[] {-2, 1, 2};
        return new int[] {2, 1, -2};
    }

    private boolean isTypeY(int[] order) {
        return (arc.getTurn() == (order[0] > 0));
    }
    
}
