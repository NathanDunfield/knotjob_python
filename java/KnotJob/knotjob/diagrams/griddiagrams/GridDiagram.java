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

import java.util.ArrayList;

/**
 *
 * @author Dirk
 */
public class GridDiagram {
    
    private final ArrayList<Integer> xPos;
    private final ArrayList<Integer> oPos;
    
    public GridDiagram(ArrayList<Integer> xp, ArrayList<Integer> op) {
        xPos = new ArrayList<Integer>(xp.size());
        oPos = new ArrayList<Integer>(op.size());
        for (Integer x : xp) xPos.add(x);
        for (Integer o : op) oPos.add(o);
    }
    
    public int getXofColumn(int i) {
        return xPos.get(i);
    }
    
    public int getOofColumn(int i) {
        return oPos.get(i);
    }
    
    public int getXofRow(int i) {
        return xPos.indexOf(i);
    }
    
    public int getOofRow(int i) {
        return oPos.indexOf(i);
    }
    
    public int size() {
        return xPos.size();
    }
    
    @Override
    public String toString() {
        String theString = "";
        for (int i = 0; i < xPos.size(); i++) {
            for (int j = 0; j < xPos.size(); j++) {
                if (j == getXofRow(i)) theString = theString+"X";
                else {
                    if (j == getOofRow(i)) theString = theString+"O";
                    else theString = theString+".";
                }
            }
            theString = theString+"\n";
        }
        return theString;
    }

    public boolean commutes(int fline, int sline, boolean hor) {
        int xf = getXofColumn(fline);
        int of = getOofColumn(fline);
        int xs = getXofColumn(sline);
        int os = getOofColumn(sline);
        if (hor) {
            xf = getXofRow(fline);
            of = getOofRow(fline);
            xs = getXofRow(sline);
            os = getOofRow(sline);
        }
        if (xf > of) {
            int h = xf;
            xf = of;
            of = h;
        } // now xf < of
        if (xs > os) {
            int h = xs;
            xs = os;
            os = h;
        } // now xs < os
        if (xf < xs) {
            return (xs >= of || os <= of);
        }
        return (xf >= os || of <= os);
    }

    public void cancel(int[] coord) {
        if (isX(coord)) {
            int a = getOofColumn(coord[0]);
            int b = getOofRow(coord[1]);
            if (Math.abs(coord[1]-a) == 1) {
                int c = getXofRow(a);
                if ((c < coord[0] && b > coord[0]) || (c > coord[0] && b < coord[0])) {
                    remove(coord, new int[] {coord[0], a});
                    return;
                }  
            }
            if (Math.abs(coord[0]-b) == 1) {
                int d = getXofColumn(b);
                if ((d < coord[1] && a > coord[1]) || (d > coord[1] && a < coord[1])) {
                    remove(coord, new int[] {b, coord[1]});
                    return;
                } 
            }
        }
        if (isO(coord)) {
            int a = getXofColumn(coord[0]);
            int b = getXofRow(coord[1]);
            if (Math.abs(coord[1]-a) == 1) {
                int c = getOofRow(a);
                if ((c < coord[0] && b > coord[0]) || (c > coord[0] && b < coord[0])) {
                    remove(new int[] {coord[0], a}, coord);
                    return;
                }
            }
            if (Math.abs(coord[0]-b) == 1) {
                int d = getOofColumn(b);
                if ((d < coord[1] && a > coord[1]) || (d > coord[1] && a < coord[1]))
                    remove(new int[] {b, coord[1]}, coord);
            }
        }
    }
    
    private void remove(int[] xcoord, int[] ocoord) {
        oPos.remove(ocoord[0]);
        xPos.remove(xcoord[0]);
        if (xcoord[1] == ocoord[1]) {
            for (int k = 0; k < size(); k++) {
                if (xPos.get(k) > xcoord[1]) xPos.set(k, xPos.get(k)-1);
                if (oPos.get(k) > xcoord[1]) oPos.set(k, oPos.get(k)-1);
            }
        }
        else {
            int min = xcoord[1];
            if (ocoord[1] < min) min = ocoord[1];
            for (int k = 0; k < size(); k++) {
                if (xPos.get(k) > min) xPos.set(k, xPos.get(k)-1);
                if (oPos.get(k) > min) oPos.set(k, oPos.get(k)-1);
            }
        }
    }
    
    public boolean canCancel(int[] coord) {
        if (isX(coord)) {
            int a = getOofColumn(coord[0]);
            int b = getOofRow(coord[1]);
            if (Math.abs(coord[1]-a) == 1) {
                int c = getXofRow(a);
                if ((c < coord[0] && b > coord[0]) || (c > coord[0] && b < coord[0])) return true;
            }
            if (Math.abs(coord[0]-b) == 1) {
                int d = getXofColumn(b);
                return ((d < coord[1] && a > coord[1]) || (d > coord[1] && a < coord[1]));
            }
        }
        if (isO(coord)) {
            int a = getXofColumn(coord[0]);
            int b = getXofRow(coord[1]);
            if (Math.abs(coord[1]-a) == 1) {
                int c = getOofRow(a);
                if ((c < coord[0] && b > coord[0]) || (c > coord[0] && b < coord[0])) return true;
            }
            if (Math.abs(coord[0]-b) == 1) {
                int d = getOofColumn(b);
                return ((d < coord[1] && a > coord[1]) || (d > coord[1] && a < coord[1]));
            }
        }
        return false;
    }
    
    public void commuteVert(int i, int j) { // moves the i-th column to the j-th column
        int hx = xPos.get(i);        // doesn't check whether this changes the link
        int ho = oPos.get(i);
        xPos.remove(i);
        oPos.remove(i);
        xPos.add(j, hx);
        oPos.add(j, ho);
    }

    public void commuteHor(int i, int j) { // moves the i-th row to the j-th row
        int ch = 1;
        if (j < i) ch = -1;
        ArrayList<Integer> posX = new ArrayList<Integer>();
        ArrayList<Integer> posO = new ArrayList<Integer>();
        for (int k = 0; k <= (j-i)*ch; k++) {
            posX.add(getXofRow(i+k*ch));
            posO.add(getOofRow(i+k*ch));
        }
        for (int k = 1; k <= (j-i)*ch; k++) {
            xPos.set(posX.get(k), i+(k-1)*ch);
            oPos.set(posO.get(k), i+(k-1)*ch);
        }
        xPos.set(posX.get(0), j);
        oPos.set(posO.get(0), j);// */
    }

    boolean isX(int[] coord) {
        return xPos.get(coord[0]) == coord[1];
    }

    boolean isO(int[] coord) {
        return oPos.get(coord[0]) == coord[1];
    }

    void stabilize(int[] coord) {
        if (isX(coord)) {
            int a = getOofColumn(coord[0]);
            int b = getOofRow(coord[1]);
            int m = 0;
            if (a < coord[1]) m = 1;
            for (int j = 0; j < size(); j++) {
                if (xPos.get(j) > coord[1]) xPos.set(j, xPos.get(j)+1);
                if (oPos.get(j) > coord[1]-m) oPos.set(j, oPos.get(j)+1);
            }
            if ((a > coord[1] && b > coord[0]) || (a < coord[1] && b < coord[0]) ) {
                xPos.add(coord[0], coord[1]+1);
                if (a > coord[1]) oPos.add(coord[0]+1, coord[1]+1);
                else oPos.add(coord[0], coord[1]);
            }
            if ((a < coord[1] && b > coord[0]) || (a > coord[1] && b < coord[0])) {
                xPos.add(coord[0]+1, coord[1]+1);
                if (a < coord[1]) oPos.add(coord[0]+1, coord[1]);
                else oPos.add(coord[0], coord[1]+1);
            }
            return;
        }
        if (isO(coord)) {
            int a = getXofColumn(coord[0]);
            int b = getXofRow(coord[1]);
            int m = 0;
            if (a < coord[1]) m = 1;
            for (int j = 0; j < size(); j++) {
                if (oPos.get(j) > coord[1]) oPos.set(j, oPos.get(j)+1);
                if (xPos.get(j) > coord[1]-m) xPos.set(j, xPos.get(j)+1);
            }
            if ((a > coord[1] && b > coord[0]) || (a < coord[1] && b < coord[0]) ) {
                oPos.add(coord[0], coord[1]+1);
                if (a > coord[1]) xPos.add(coord[0]+1, coord[1]+1);
                else xPos.add(coord[0], coord[1]);
            }
            if ((a < coord[1] && b > coord[0]) || (a > coord[1] && b < coord[0])) {
                oPos.add(coord[0]+1, coord[1]+1);
                if (a < coord[1]) xPos.add(coord[0]+1, coord[1]);
                else xPos.add(coord[0], coord[1]+1);
            }
        }
    }

    void tryToCancel() {
        int i = xPos.size()-1;
        while (i >= 0) {
            cancel(new int[] {i, xPos.get(i)});
            i--;
        }
        i = oPos.size()-1;
        while (i >= 0) {
            cancel(new int[] {i, oPos.get(i)});
            i--;
        } 
    }

    void shiftColumns() {
        int i = xPos.size()-1;
        while (i >= 0) {
            int u = shiftColumn(i, -1);
            boolean canRemove = canRemoveColumn(i, u, -1);
            if (!canRemove) {
                u = shiftColumn(i, +1);
                canRemoveColumn(i, u, +1);
            }
            i--;
            if (i >= size()) i = size()-1;
        }
    }

    private int shiftColumn(int i, int y) {
        int shift = i;
        int a = xPos.get(i);
        int b = oPos.get(i);
        int n = xPos.size();
        boolean keepShifting = true;
        while (keepShifting) {
            keepShifting = false;
            int c = xPos.get((n+shift+y)%n);
            int d = oPos.get((n+shift+y)%n);
            if (canShift(a, b, c, d)) {
                keepShifting = true;
                shift = (n+shift+y)%n;
            }
        }
        return shift;
    }
    
    private boolean canShift(int a, int b, int c, int d) { 
        if (b < a) {
            int h = b;
            b = a;
            a = h;
        }
        if (d < c) {
            int h = d;
            d = c;
            c = h;
        } // we now have a < b and c < d
        if (b < c || d < a) return true; // intervals disjoint
        if (a < c && d < b) return true;
        return c < a && b < d;
    }
    
    private boolean canRemoveColumn(int i, int u, int y) {
        int n = xPos.size();
        int a = xPos.get(i);
        int b = oPos.get(i);
        int c = xPos.get((n+u+y)%n);
        int d = oPos.get((n+u+y)%n);
        if (c == b || a == d) { //now shift and then destabilize
            int j = i;
            while (j != u) {
                xPos.set(j, xPos.get((n+j+y)%n));
                oPos.set(j, oPos.get((n+j+y)%n));
                j = (n+j+y)%n;
            }
            xPos.set(u, a);
            oPos.set(u, b);
            tryToCancel();
            return true;
        }
        return false;
    }
    
    void shiftRows() {
        int i = xPos.size()-1;
        while (i >= 0) {
            int u = shiftRow(i, -1);
            boolean canRemove = canRemoveRow(i, u, -1);
            if (!canRemove) {
                u = shiftRow(i, +1);
                canRemoveRow(i, u, +1);
            }
            i--;
            if (i >= size()) i = size()-1;
        }
    }

    private int shiftRow(int i, int y) {
        int shift = i;
        int a = xPos.indexOf(i);
        int b = oPos.indexOf(i);
        int n = xPos.size();
        boolean keepShifting = true;
        while (keepShifting) {
            keepShifting = false;
            int c = xPos.indexOf((n+shift+y)%n);
            int d = oPos.indexOf((n+shift+y)%n);
            if (canShift(a, b, c, d)) {
                keepShifting = true;
                shift = (n+shift+y)%n;
            }
        }
        return shift;
    }
    
    private boolean canRemoveRow(int i, int u, int y) {
        int n = xPos.size();
        int a = xPos.indexOf(i);
        int b = oPos.indexOf(i);
        int c = xPos.indexOf((n+u+y)%n);
        int d = oPos.indexOf((n+u+y)%n);
        if (c == b || a == d) { //now shift and then destabilize
            int j = i;
            while (j != u) {
                xPos.set(xPos.indexOf((n+j+y)%n), j);
                oPos.set(oPos.indexOf((n+j+y)%n), j);
                j = (n+j+y)%n;
            }
            xPos.set(a, u);
            oPos.set(b, u);
            tryToCancel();
            return true;
        }
        return false;
    }
    
    void searchStaircase() {
        int i = xPos.size()-1;
        while (i >= 0) { // here we are looking for empty vertical edges
            int a = xPos.get(i);
            int b = oPos.get(i);
            int c = oPos.indexOf(a);
            int d = xPos.indexOf(b);
            if ((c < i) & (i < d) || (d < i) & (i < c)) {
                if (emptyVerticalEdge(a, b, i)) {
                    pushAlongVerticalEdge(a, b, i);
                    tryToCancel();
                }
                
            }
            i--;
        }
        i = xPos.size()-1;
        while (i >= 0) { // here we are looking for empty horizontal edges
            int a = xPos.indexOf(i);
            int b = oPos.indexOf(i);
            int c = oPos.get(a);
            int d = xPos.get(b);
            if ((c < i) & (i < d) || (d < i) & (i < c)) {
                if (emptyHorizontalEdge(a, b, i)) {
                    pushAlongHorizontalEdge(a, b, i);
                    tryToCancel();
                }
            }
            i--;
        }
    }

    void randomMoves(int i) {
        while (i >= 0) {
            boolean moves = false;
            int fline = 0;
            int sline = 0;
            boolean hor = true;
            while(!moves) {
                fline = (int) Math.round(Math.random() * (size()-1));
                int dir = (int) Math.round(Math.random()) * 2-1;
                hor = ((int) Math.round(Math.random()) == 1);
                sline = (size() + fline + dir)%size();
                if (Math.abs(fline-sline) > 1) moves = true;
                else moves = commutes(fline, sline, hor);
            }
            if (hor) commuteHor(fline, sline);
            else commuteVert(fline, sline);
            i--;
        }
    }

    private boolean emptyVerticalEdge(int i, int j, int level) {
        if (j < i) {
            int h = i;
            i = j;
            j = h;
        } // we now have an interval [i,j]
        boolean okay = true;
        int k = i+1;
        while (okay && k < j) {
            int a = xPos.indexOf(k);
            int b = oPos.indexOf(k);
            if ((a < level & level < b) || (b < level & level < a)) okay = false;
            else k++;
        }
        return okay;
    }

    private boolean emptyHorizontalEdge(int i, int j, int level) {
        if (j < i) {
            int h = i;
            i = j;
            j = h;
        } // we now have an interval [i,j]
        boolean okay = true;
        int k = i+1;
        while (okay && k < j) {
            int a = xPos.get(k);
            int b = oPos.get(k);
            if ((a < level & level < b) || (b < level & level < a)) okay = false;
            else k++;
        }
        return okay;
    }

    private void pushAlongVerticalEdge(int a, int b, int i) {
        int change = 1;
        if (a > b) change = -1;
        int d = oPos.indexOf(a);
        boolean drightOfi = true;
        if (d < i) drightOfi = false;
        int j = a + change;
        while (change * j < change * b) {
            int e = xPos.indexOf(j);
            if ( (e < i) == drightOfi ) {
                commuteRows(a, j);
                a = a + change;
            }
            j = j + change;
        }
        j = b - change;
        while (change * a < change * j) {
            int e = xPos.indexOf(j);
            if ( (e > i) == drightOfi ) {
                commuteRows(b, j);
                b = b - change;
            }
            j = j - change;
        }
    }

    private void pushAlongHorizontalEdge(int a, int b, int i) {
        int change = 1;
        if (a > b) change = -1;
        int d = oPos.get(a);
        boolean dtopOfi = true;
        if (i < d) dtopOfi = false;
        int j = a + change;
        while (change * j < change * b) {
            int e = xPos.get(j);
            if ( (e > i) == dtopOfi ) {
                commuteColumns(a, j);
                a = a + change;
            }
            j = j + change;
        } // */
        j = b - change;
        while (change * a < change * j) {
            int e = xPos.get(j);
            if ( (e < i) == dtopOfi ) {
                commuteColumns(b, j);
                b = b - change;
            }
            j = j - change;
        } // */
    }
    
    private void commuteRows(int xlevel, int j) {
        int change = 1;
        if (xlevel > j) change = -1;
        int i = j - change;
        int a = xPos.indexOf(j);
        int b = oPos.indexOf(j);
        while (change * i >= change * xlevel) {
            int c = xPos.indexOf(i);
            int d = oPos.indexOf(i);
            xPos.set(c,i+change);
            oPos.set(d,i+change);
            i = i - change;
        }
        xPos.set(a,xlevel);
        oPos.set(b,xlevel);
    }

    private void commuteColumns(int xlevel, int j) {
        int change = 1;
        if (xlevel > j) change = -1;
        int i = j - change;
        int a = xPos.get(j);
        int b = oPos.get(j);
        while (change * i >= change * xlevel) {
            int c = xPos.get(i);
            int d = oPos.get(i);
            xPos.set(i+change,c);
            oPos.set(i+change,d);
            i = i - change;
        }
        xPos.set(xlevel,a);
        oPos.set(xlevel,b);
    }
    
}
