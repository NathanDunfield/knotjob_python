/*

Copyright (C) 2021-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.polynomial.alexander;

import java.math.BigInteger;
import java.util.ArrayList;
import knotjob.polynomial.HalfPolynomial;

/**
 *
 * @author Dirk
 */
public class StateList {
    
    private final ArrayList<ArrayList<Integer>> theDiscs;
    private final ArrayList<Integer> directions;
    private final ArrayList<Integer> fullDiscs;
    private final ArrayList<Integer> firstPaths;
    private final ArrayList<Integer> seconPaths;
    private final ArrayList<KauffmanState> theStates;
    
    public StateList(ArrayList<ArrayList<Integer>> discs, ArrayList<Integer> dirs) {
        theDiscs = discs;
        theDiscs.remove(0);
        theDiscs.remove(0);
        directions = dirs;
        fullDiscs = new ArrayList<Integer>();
        firstPaths = new ArrayList<Integer>();
        seconPaths = new ArrayList<Integer>();
        theStates = new ArrayList<KauffmanState>();
    }
    
    public void output() {
        System.out.println("Discs : "+theDiscs);
        System.out.println("Directions : "+directions);
        System.out.println("Full discs : "+fullDiscs);
        System.out.println("Paths used once : "+firstPaths);
        System.out.println("Paths used twice : "+seconPaths);
        System.out.println("Kauffman States : ");
        for (KauffmanState state : theStates) System.out.println(state);
    }
    
    public void getStarted(int c, int[] path) {
        int dir = directions.get(0);
        for (int i = 0; i < 4; i++) {
            int first = path[(i+3)%4];
            int secon = path[i];
            int dn = discWith(first, secon);
            addToPaths(secon);
            if (dn >= 0) {
                HalfPolynomial poly = getPoly(i, dir, c);
                theStates.add(new KauffmanState(poly, dn));
            }
        }
        directions.remove(0);
    }
    
    public BigInteger determinant() {
        return theStates.get(0).theDeterminant();
    }
    
    public String finalPolynomial() {
        return theStates.get(0).thePolynomial();
    }
    
    private int discWith(int f, int s) {
        boolean found = false;
        int i = 0;
        while (!found && i < theDiscs.size()) {
            ArrayList<Integer> disc = theDiscs.get(i);
            if (disc.contains(f) && disc.contains(s)) found = true;
            else i++;
        }
        if (found) return i;
        return -1;
    }
    
    private void addToPaths(int s) {
        int ind = firstPaths.indexOf(s);
        if (ind == -1) firstPaths.add(s);
        else {
            firstPaths.remove(ind);
            seconPaths.add(s);
        }
    }
    
    private HalfPolynomial getPoly(int i, int dir, int cross) {
        int deg = 0;
        int fac = 1;
        if (i == 0) {
            if (dir == 0) deg = cross;
            if (dir == 3) {
                deg = -cross;
                fac = -1;
            }
        }
        if (i == 1) {
            if (dir == 1) {
                deg = cross;
                fac = -1;
            }
            if (dir == 2) {
                deg = -cross;
            }
        }
        if (i == 2) {
            if (dir == 0) {
                deg = -cross;
                fac = -1;
            }
            if (dir == 3) deg = cross;
        }
        if (i == 3) {
            if (dir == 1) deg = -cross;
            if (dir == 2) {
                deg = cross;
                fac = -1;
            }
        }
        return new HalfPolynomial(new String[] {"t"}, BigInteger.valueOf(fac), new int[] {deg});
    }

    void combineWith(int cross, int[] path) {
        for (int i = 0; i < 4; i++) {
            addToPaths(path[i]);
        }
        checkForFullDiscs();
        int dir = directions.get(0);
        directions.remove(0);
        int i = theStates.size()-1;
        while (i >= 0) {
            KauffmanState state = theStates.get(i);
            for (int k = 0; k < 4; k++) {
                int first = path[(k+3)%4];
                int secon = path[k];
                int dn = discWith(first, secon);
                if (dn >= 0 && !state.contains(dn)) {
                    HalfPolynomial poly = getPoly(k, dir, cross);
                    KauffmanState newState = new KauffmanState(state, poly, dn);
                    if (goodDiscs(newState)) theStates.add(newState);
                }
            }
            theStates.remove(i);
            i--;
        }
        combinetheStates();
    }

    private void checkForFullDiscs() {
        for (int i = 0; i < theDiscs.size(); i++) {
            if (!fullDiscs.contains(i)) {
                ArrayList<Integer> disc = theDiscs.get(i);
                if (allInDisc(disc)) fullDiscs.add(i);
            }
        }
    }

    private boolean allInDisc(ArrayList<Integer> disc) {
        boolean allIn = true;
        int i = 0;
        while (allIn && i < disc.size()) {
            if (!seconPaths.contains(disc.get(i))) allIn = false;
            else i++;
        }
        return allIn;
    }

    private boolean goodDiscs(KauffmanState newState) {
        boolean allThere = true;
        int i = 0;
        while (allThere && i < fullDiscs.size()) {
            if (!newState.contains(fullDiscs.get(i))) allThere = false;
            else i++;
        }
        return allThere;
    }

    private void combinetheStates() {
        int i = theStates.size()-1;
        while (i >= 0) {
            KauffmanState state = theStates.get(i);
            if (state.zeroPoly()) {
                theStates.remove(i);
                i--;
            }
            else {
                int j = i-1;
                boolean cont = true;
                while (cont && j >= 0) {
                    KauffmanState nState = theStates.get(j);
                    if (state.sameDots(nState)) {
                        cont = false;
                        theStates.remove(j);
                        state.add(nState);
                    }
                    else j--;
                }
                i--;
            }
        }
    }
    
}
