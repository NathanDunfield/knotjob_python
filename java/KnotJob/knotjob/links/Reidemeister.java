/*

Copyright (C) 2021-24 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.links;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Dirk
 */
public class Reidemeister {
    
    public static boolean allFour(Link link) {
        boolean allfour = true;
        int i = 0;
        while (allfour && i < link.crossingLength()) {
            ArrayList<Integer> helper = allOfCrossing(link.getPath(i));
            allfour = (helper.size() == 4);
            i++;
        }
        return allfour;
    }
    
    public static Link freeOfOne(Link link) {
        if (allFour(link)) return link;
        int i = firstNotFour(link);
        while (i >= 0) {
            int cr = link.getCross(i);
            if (Math.abs(cr) != 1) link = splitUp(link, i);
            else {
                ArrayList<Integer> helper = allOfCrossing(link.getPath(i));
                if (helper.isEmpty()) { // it is an unknot component
                    link = removeUnknot(link, i);
                }
                else {
                    link = removeROne(link, i);
                }
            }
            i = firstNotFour(link);
        }
        return link;
    }

    private static int firstNotFour(Link link) {
        int i = 0;
        boolean found = false;
        while (!found && i < link.crossingLength()) {
            if (allOfCrossing(link.getPath(i)).size() < 4) found = true;
            else i++;
        }
        if (found) return i;
        return -1;
    }

    private static ArrayList<Integer> allOfCrossing(int[] path) {
        ArrayList<Integer> helper = new ArrayList<Integer>(4);
        for (int u : path) {
            if (!helper.contains(u)) helper.add(u);
            else helper.remove((Integer) u);
        }
        return helper;
    }

    private static Link splitUp(Link link, int i) {
        int n = link.getCross(i);
        int m = Math.abs(n);
        int[] crossings = new int[link.crossingLength()+m-1];
        int[][] paths = new int[link.crossingLength()+m-1][4];
        for (int j = 0; j < link.crossingLength(); j++) {
            int k = j;
            if (k > i) k = k + m - 1;
            crossings[k] = link.getCross(j);
            for (int l = 0; l < 4; l++) paths[k][l] = link.getPath(j, l);
        }
        int om = 2 * link.crossingLength();
        for (int j = 0; j < m - 1; j++) {
            paths[i+j][1] = om + 2 * j + 1;
            paths[i+j][2] = om + 2 * j + 2;
            paths[i+j+1][0] = om + 2 * j + 1;
            paths[i+j+1][3] = om + 2 * j + 2;
            crossings[i+j] = n/m;
        }
        crossings[i+m-1] = n/m;
        paths[i+m-1][1] = link.getPath(i, 1);
        paths[i+m-1][2] = link.getPath(i, 2);
        ArrayList<int[]> ors = new ArrayList<int[]>();
        for (int j = 0; j < link.relComponents(); j++) {
            int[] or = link.orientation(j);
            if (or[0] > i) {
                int[] nor = new int[2];
                if (or[0] > i) nor[0] = or[0]+m-1;
                else nor[0] = or[0];
                nor[1] = or[1];
                ors.add(nor);
            }
            else ors.add(or);
        }
        return new Link(crossings, paths, ors, link.unComponents());
    }

    private static Link removeUnknot(Link link, int i) {
        int a = link.getPath(i, 0);
        int b = link.getPath(i, 2);
        int[][] paths = new int[link.crossingLength()-1][4];
        int[] crossings = new int[link.crossingLength()-1];
        for (int j = 0; j < link.crossingLength(); j++) {
            if (j != i) {
                int k = j;
                if (j > i) k--;
                crossings[k] = link.getCross(j);
                for (int l = 0; l < 4; l++) {
                    int path = link.getPath(j, l);
                    if (path >= a) path--;
                    if (path >= b) path--;
                    paths[k][l] = path;
                }
            }
            
        }
        ArrayList<int[]> ors = new ArrayList<int[]>();
        for (int j = 0; j < link.relComponents(); j++) {
            int[] or = link.orientation(j);
            if (or[0] != i) {
                int[] nor = new int[2];
                if (or[0] > i) nor[0] = or[0]-1;
                else nor[0] = or[0];
                nor[1] = or[1];
                ors.add(nor);
            }
        }
        return new Link(crossings, paths, ors, link.unComponents()+1);
    }

    private static Link removeROne(Link link, int i) {
        int a = link.getPath(i, 0);
        int b = link.getPath(i, 2);
        int c = link.getPath(i, 1);
        int d = link.getPath(i, 3);
        int[][] paths = new int[link.crossingLength()-1][4];
        int[] crossings = new int[link.crossingLength()-1];
        for (int k = 0; k < link.crossingLength()-1; k++) {
            int l = k;
            if (l >= i) l++;
            crossings[k] = link.getCross(l);
            for (int m = 0; m < 4; m++) {
                paths[k][m] = link.getPath(l, m);
            }
        }
        if (a == c || a == d) {
            if (a == c) {
                c = d;
            }
        }
        else {
            if (b == c) {
                b = a;
                a = c;
                c = d;
            }
            else { // b == d
                b = a;
                a = d;
            }
        } // we now have that a = d, while b and c are different, and are part of the rest of the link
        ArrayList<int[]> ors = new ArrayList<int[]>();
        for (int j = 0; j < link.relComponents(); j++) {
            int[] or = link.orientation(j);
            if (or[0] != i) {
                int[] nor = new int[2];
                if (or[0] > i) nor[0] = or[0]-1;
                else nor[0] = or[0];
                nor[1] = or[1];
                ors.add(nor);
            }
            else {
                ArrayList<ArrayList<Integer>> comps = link.getComponents();
                int k = componentWith(a, comps);
                boolean btoc = getOrder(comps.get(k), a, b, c);
                int[] nor;
                if (btoc) nor = posOf(c, paths);
                else nor = posOf(b, paths);
                ors.add(nor);
            }
        }
        if (b > c) {
            d = c;
            c = b;
            b = d;
        } // we now have b < c
        for (int m = 0; m < paths.length; m++) {
            for (int n = 0; n < 4; n++) {
                int p = paths[m][n];
                if (p == c) p = b;
                if (p > a) p--;
                if (p >= c) p--;
                paths[m][n] = p;
            }
        }
        return new Link(crossings, paths, ors, link.unComponents());
    }

    private static int componentWith(int a, ArrayList<ArrayList<Integer>> comps) {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (comps.get(i).contains(a)) found = true;
            else i++;
        }
        return i;
    }

    private static boolean getOrder(ArrayList<Integer> comp, int a, int b, int c) {
        int u = comp.indexOf(a);
        int m = comp.size();
        if (u == 0) {
            comp.add(0, comp.get(m-1));
            comp.remove(m);
        }
        if (u == m-1) {
            comp.add(comp.get(0));
            comp.remove(0);
        } // we now have the order b a c or c a b with no cyclic overlap
        int v = comp.indexOf(b);
        u = comp.indexOf(c);
        return (v < u); // return (pos b is less than pos c)
    }

    private static int[] posOf(int c, int[][] paths) {
        boolean found = false;
        int i = 0;
        int j = 0;
        while (!found) {
            j = 0;
            while (!found && j < 4) {
                if (paths[i][j] == c) found = true;
                else j++;
            }
            if (!found) i++;
        }
        return new int[] {i, j};
    }
    
    public static Link combineCrossings(Link link) {
        boolean keepgoing = true;
        Link currentLink = link;
        while (keepgoing) {
            int[] combineable = combinableCrossings(currentLink);
            if (combineable[0] == -1) keepgoing = false;
            else {
                currentLink = combineCrossings(currentLink, combineable);
            }
        }
        return currentLink;
    }
    
    private static int[] combinableCrossings(Link link) {
        for (int i = 0; i < link.crossingLength()-1; i++) {
            for (int j = i+1; j < link.crossingLength(); j++) {
                int jump = 1;
                if (Math.abs(link.getCross(i)) > 1) jump = 2;
                for (int k = 0; k < 4; k = k+jump) {
                    int p = positionOf(link.getPath(i, k), link.getPath(i, (k+3)%4), link.getPath(j), link.getCross(j));
                    if (p >= 0 && goodCrossings(link.getCross(i), k, link.getCross(j), p))
                        return new int[] {i, k, j, p};
                }
            }
        }
        return new int[] {-1};
    }
    
    private static boolean goodCrossings(int ci, int k, int cj, int l) {
        if (k % 2 == 1) ci = -ci;
        if (l % 2 == 1) cj = -cj;
        return ci * cj > 0;
    }
    
    private static int positionOf(int a, int b, int[] path, int c) {
        int jump = 1;
        if (Math.abs(c) > 1) jump = 2;
        for (int k = 0; k < 4; k = k + jump) {
            if (path[k] == b && path[(k+3)%4] == a) return k;
        }
        return -1;
    }
    
    private static Link combineCrossings(Link link, int[] crs) {
        int[] newCrossings = new int[link.crossingLength()-1];
        int[][] newPaths = new int[link.crossingLength()-1][4];
        ArrayList<int[]> newOrients = new ArrayList<int[]>();
        int a = link.getPath(crs[0], crs[1]);
        int b = link.getPath(crs[2], crs[3]);
        int cOne = link.getCross(crs[0]);
        int cTwo = link.getCross(crs[2]);
        if (crs[1]%2 == 1) cOne = -cOne;
        if (crs[3]%2 == 1) cTwo = -cTwo;
        int shift = 0;
        for (int k = 0; k < newCrossings.length; k++) {
            if (k == crs[2]) shift++;
            if (k == crs[0]) {
                newCrossings[k] = cOne+cTwo;
                int[] nPath = newPathFrom(link.getPath(k), link.getPath(crs[2]), crs[1], crs[3]);
                for (int l = 0; l < 4; l++) newPaths[k][l] = adjusted(nPath[l], a, b);
            }
            else {
                newCrossings[k] = link.getCross(k+shift);
                for (int l = 0; l < 4; l++) newPaths[k][l] = adjusted(link.getPath(k+shift, l), a, b);
            }
        }
        for (int k = 0; k < link.relComponents(); k++) {
            int[] or = link.orientation(k);
            int[] nor = new int[2];
            int drop = 0;
            if (or[0] != crs[0] && or[0] != crs[2]) {
                if (or[0] > crs[2]) drop++;
                nor[0] = or[0]-drop;
                nor[1] = or[1];
            }
            else {
                nor[0] = crs[0];
                if (or[0] == crs[0]) {
                    nor[1] = firstFewCases(or[1], crs[1], link.getCross(crs[2]));
                }
                else nor[1] = secondFewCases(or[1], crs[3], link.getCross(crs[0]));
            }
            newOrients.add(nor);
        }
        return new Link(newCrossings, newPaths, newOrients, link.unComponents());
    }
    
    private static int secondFewCases(int dir, int bPos, int n) {
        if (dir == bPos) {
            if (n % 2 == 0) return 0;
            return 3;
        }
        if (dir == (bPos+3)%4) {
            if (n % 2 == 0) return 3;
            return 0;
        }
        if (dir == (bPos+1)%4) return 1;
        return 2;
    }
    
    private static int firstFewCases(int dir, int aPos, int n) {
        if (dir == aPos) {
            if (n % 2 == 0) return 2;
            return 1;
        }
        if (dir == (aPos+3)%4) {
            if (n % 2 == 0) return 1;
            return 2;
        }
        if (dir == (aPos+1)%4) return 3;
        return 0;
    }
    
    private static int adjusted(int p, int a, int b) {
        if (p > a) p--;
        if (p >= b) p--;
        return p;
    }
    
    private static int[] newPathFrom(int[] pOne, int[] pTwo, int k, int l) {
        int a = pOne[k];
        int b = pTwo[l];
        int c = pOne[(k+1)%4];
        int d = pOne[(k+2)%4];
        int e = pTwo[(l+1)%4];
        int f = pTwo[(l+2)%4];
        return new int[] {d, e, f, c};
    }
    
    public static int[] findReidemeisterI(Link link) {
        for (int i = 0; i < link.crossingLength(); i++) {
            int jump = 1;
            if (Math.abs(link.getCross(i)) > 1) jump = 2;
            for (int j = 0; j < 4; j = j + jump) {
                int[] oc = meisterI(i, j, link);
                if (oc[0] != -1) return oc;
            }
        }
        return new int[] {-1};
    }
    
    private static int[] meisterI(int i, int j, Link link) {
        int p = (j+3)%4;
        int pa = link.getPath(i, p);
        int pb = link.getPath(i, j);
        if (pa == pb) return new int[] {i, p};
        return new int[] {-1};
    }
    
    public static int[] findReidemeisterII(Link link) {
        for (int i = 0; i < link.crossingLength(); i++) {
            if (Math.abs(link.getCross(i)) == 1) {
                for (int j = 0; j < 4; j++) {
                    int[] oc = meisterII(i, j, link);
                    if (oc[0] != -1) return oc;
                }
            }
        }
        return new int[] {-1};
    }
    
    /*
    Reidemeister II consists of four numbers, the positions of the crossings involved as
    well as the first positions on each crossing.
    */
    
    private static int[] meisterII(int i, int j, Link link) {
        int pa = link.getPath(i, j);
        int pb = link.getPath(i, (j+1)%4);
        int[] k = otherCrossingWith(pa, i, link);
        int[] l = otherCrossingWith(pb, i, link);
        if (k[0] == l[0] && k[0] != i) {
            if (k[1] == (l[1]+1)%4) {
                if (overPath(new int[] {i, j}, link) && overPath(k, link)) 
                    return new int[] {i, k[0], j, k[1]};
            }
        }
        return new int[] {-1};
    }
    
    public static ArrayList<int[]> findReidemeisterIII(Link link) {
        ArrayList<int[]> allMoves = new ArrayList<int[]>();
        for (int i = 0; i < link.crossingLength(); i++) {
            if (Math.abs(link.getCross(i)) == 1) {
                for (int j = 0; j < 4; j++) {
                    int[] oc = meisterIII(i, j, link);
                    if (oc[0] != -1) {
                        allMoves.add(oc);
                    }
                }
            }
        }
        return allMoves;
    }
    
    /* 
    Reidemeister III consists of six numbers. The first refers to the crossing so that
    a path can be pushed under it. The third refers to the crossing so that a path can be
    pushed over it. The second is the other crossing in the move.
    The remaining three numbers give the position in these three crossings which form the
    triangle in RIII.
    */

    private static int[] meisterIII(int i, int j, Link link) {
        boolean clockwise = (link.getCross(i) > 0 && j%2 == 0) || 
                            (link.getCross(i) < 0 && j%2 != 0);
        int pa = link.getPath(i, j);
        int pb = link.getPath(i, (j+1)%4);
        int[] k = otherCrossingWith(pa, i, link);
        int[] l = otherCrossingWith(pb, i, link);
        if (k[0] == l[0]) return new int[] {-1};
        if (possibleMeisterIII(k, l, link)) {
            if (overPath(k, link) && overPath(l, link)) {
                if (clockwise) return new int[] {i, l[0], k[0], (j+1)%4, (l[1]+1)%4, k[1]};
                return new int[] {i, k[0], l[0], j, (k[1]+3)%4, l[1]};
            }
        }
        return new int[] {-1};
    }

    private static int[] otherCrossingWith(int pa, int i, Link link) {
        for (int j = 0; j < link.crossingLength(); j++) {
            if (j != i) {
                int pos = arrayPosition(link.getPath(j), pa);
                if (pos != -1) return new int[] {j, pos};
            }
        }
        return new int[] {i, -1};
    }

    private static int arrayPosition(int[] path, int pa) {
        for (int u = 0; u < 4; u++) if (path[u] == pa) return u;
        return -1;
    }

    private static boolean possibleMeisterIII(int[] k, int[] l, Link link) {
        int pa = link.getPath(k[0], (k[1]+3)%4);
        int pb = link.getPath(l[0], (l[1]+1)%4);
        return pa == pb;
    }

    private static boolean overPath(int[] k, Link link) {
        int c = link.getCross(k[0]);
        if (c > 0) return (k[1] % 2 != 0);
        return (k[1] % 2 == 0);
    }
    
    public static Link performMove(Link link, int[] move) {
        if (move.length == 4) return performMoveII(link, move);
        return performMoveIII(link, move);
    }

    private static Link performMoveII(Link link, int[] move) {
        // it is implicitly assumed that no RI moves are present.
        int extraUnknot = 0;
        int[] pathOne = link.getPath(move[0]);
        int[] pathTwo = link.getPath(move[1]);
        int posOne = move[2];
        int posTwo = move[3];
        ArrayList<Integer> dropNumbers = new ArrayList<Integer>();
        ArrayList<Integer> identifyNumbers = new ArrayList<Integer>();
        dropNumbers.add(pathOne[posOne]);
        dropNumbers.add(pathOne[(posOne+1)%4]);
        if (pathOne[(posOne+2)%4] == pathTwo[(posTwo+2)%4]) extraUnknot++;
        else {
            identifyNumbers.add(pathOne[(posOne+2)%4]);
            identifyNumbers.add(pathTwo[(posTwo+2)%4]);
        }
        dropNumbers.add(pathTwo[(posTwo+2)%4]);
        if (pathOne[(posOne+3)%4] == pathTwo[(posTwo+1)%4]) extraUnknot++;
        else {
            identifyNumbers.add(pathOne[(posOne+3)%4]);
            identifyNumbers.add(pathTwo[(posTwo+1)%4]);
        }
        dropNumbers.add(pathTwo[(posTwo+1)%4]);
        return moveIILink(link, dropNumbers, identifyNumbers, extraUnknot, move[0], move[1]);
    }

    private static Link moveIILink(Link link, ArrayList<Integer> dropNumbers, 
            ArrayList<Integer> identifyNumbers, int extraUnknot, int cOne, int cTwo) {
        int[] newCross = new int[link.crossingLength()-2];
        int[][] newPaths = new int[link.crossingLength()-2][4];
        for (int i = 0; i < link.crossingLength(); i++) {
            int ii = crossNumber(i, cOne, cTwo);
            if (ii != -1) {
                newCross[ii] = link.getCross(i);
                int[] nPath = newPathFrom(link.getPath(i), dropNumbers, identifyNumbers);
                System.arraycopy(nPath, 0, newPaths[ii], 0, 4);
            }
        }
        ArrayList<int[]> newOrs = new ArrayList<int[]>();
        for (int i = 0; i < link.relComponents(); i++) {
            ArrayList<int[]> ors = link.getOrientationsOfComponent(i);
            int[] nOr = newOrientationOf(ors, cOne, cTwo);
            if (nOr[0] != -1) newOrs.add(nOr);
        }
        Link nLink = new Link(newCross, newPaths, newOrs, link.unComponents()+extraUnknot);
        return Reidemeister.freeOfOne(nLink).girthMinimize();
    }

    private static int[] newOrientationOf(ArrayList<int[]> ors, int cOne, int cTwo) {
        for (int[] or : ors) {
            if (or[0] != cOne && or[0] != cTwo) {
                int i = crossNumber(or[0], cOne, cTwo);
                return new int[] {i, or[1]};
            }
        }
        return new int[] {-1};
    }
    
    private static int[] newPathFrom(int[] oPath, ArrayList<Integer> dropNumbers, 
            ArrayList<Integer> identifyNumbers) {
        int[] nPath = new int[4];
        for (int i = 0; i < 4; i++) {
            nPath[i] = valueFrom(oPath[i], dropNumbers, identifyNumbers);
        }
        return nPath;
    }
    
    private static int valueFrom(int p, ArrayList<Integer> dropNumbers, 
            ArrayList<Integer> identifyNumbers) {
        if (identifyNumbers.contains(p)) p = identify(p, identifyNumbers);
        int drop = 0;
        for (int dr : dropNumbers) if (dr < p) drop++;
        return p - drop;
    }
    
    private static int identify(int p, ArrayList<Integer> identifyNumbers) {
        int ind = identifyNumbers.indexOf(p);
        if (ind % 2 == 0) return p;
        return identifyNumbers.get(ind-1);
    }
    
    private static int crossNumber(int i, int cOne, int cTwo) {
        if (i == cOne || i == cTwo) return -1;
        int k = i;
        if (i > cOne) k--;
        if (i > cTwo) k--;
        return k;
    }
    
    private static Link performMoveIII(Link link, int[] move) {
        int[] newCross = new int[link.crossingLength()];
        int[][] newPaths = new int[link.crossingLength()][4];
        int[] innerCircle = innerCircleOf(move, link);
        int[] outerCircle = outerCircleOf(move, link, innerCircle);
        for (int i = 0; i < link.crossingLength(); i++) {
            newCross[i] = link.getCross(i);
            int[] newPath;
            if (i == move[0]) newPath = theNewPath(link.getPath(move[0]), innerCircle, outerCircle, 0);
            else if (i == move[1]) newPath = theNewPath(link.getPath(move[1]), innerCircle, outerCircle, 1);
            else if (i == move[2]) newPath = theNewPath(link.getPath(move[2]), innerCircle, outerCircle, 2);
            else newPath = link.getPath(i);
            System.arraycopy(newPath, 0, newPaths[i], 0, 4);
        }
        ArrayList<int[]> newOrs = new ArrayList<int[]>();
        for (int i = 0; i < link.relComponents(); i++) {
            int[] or = link.orientation(i);
            if (or[0] == move[0] || or[0] == move[1] || or[0] == move[2]) {
                newOrs.add(new int[] {or[0], (or[1]+2)%4});
            }
            else newOrs.add(new int[] {or[0], or[1]});
        }
        Link nLink = new Link(newCross, newPaths, newOrs, link.unComponents());
        return Reidemeister.freeOfOne(nLink).girthMinimize();
    }
    
    private static int[] theNewPath(int[] oPath, int[] ic, int[] oc, int pos) {
        int[] nPath = new int[4];
        int[] overlap = positionsOf(ic, oPath);
        int adder = 0;
        if (overlap[0] == 0) { // third crossing
            boolean clockwise = (overlap[1]+1)%4 == overlap[2]%4;
            if (clockwise) adder = 1;
            nPath[overlap[1]%4] = oPath[overlap[1]%4];
            nPath[overlap[2]%4] = oPath[overlap[2]%4];
            nPath[(overlap[1]+2)%4] = oc[2];
            nPath[(overlap[1]+1+2*adder)%4] = oc[1];
        }
        if (overlap[1] == 0) { // first crossing
            boolean clockwise = (overlap[2]+1)%4 == overlap[0]%4;
            if (clockwise) adder = 1;
            nPath[overlap[2]%4] = oPath[overlap[2]%4];
            nPath[overlap[0]%4] = oPath[overlap[0]%4];
            nPath[(overlap[2]+2)%4] = oc[4];
            nPath[(overlap[2]+1+2*adder)%4] = oc[3];
        }
        if (overlap[2] == 0) { // second crossing
            boolean clockwise = (overlap[0]+1)%4 == overlap[1]%4;
            if (clockwise) adder = 1;
            nPath[overlap[0]%4] = oPath[overlap[0]%4];
            nPath[overlap[1]%4] = oPath[overlap[1]%4];
            nPath[(overlap[0]+2)%4] = oc[0];
            nPath[(overlap[0]+1+2*adder)%4] = oc[5];
        }
        return nPath;
    }
    
    private static int[] positionsOf(int[] ic, int[] path) {
        int[] ov = new int[3];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                if (path[i] == ic[j]) ov[j] = i+4;
            }
        }
        return ov;
    }
    
    private static int[] outerCircleOf(int[] move, Link link, int[] ic) {
        int[] circle = new int[6];
        boolean clockWise = true;
        if (link.getPath(move[0], (move[3]+1)%4) == ic[2]) clockWise = false;
        int shift = 3;
        if (clockWise) shift = 1;
        for (int i = 0; i < 3; i++) {
            circle[2*i] = link.getPath(move[i], (move[i+3]+2)%4);
            circle[2*i+1] = link.getPath(move[i], (move[i+3]+shift)%4);
        }
        return circle;
    }
    
    private static int[] innerCircleOf(int[] move, Link link) {
        int[] circle = new int[3];
        for (int i = 0; i < 3; i++) circle[i] = link.getPath(move[i], move[i+3]);
        return circle;
    }
    
    public static void outputMove(int[] move, Link link) {
        int length = move.length / 2;
        System.out.println(Arrays.toString(move));
        for (int i = 0; i < length; i++) 
            System.out.println(link.getCross(move[i])+" "+Arrays.toString(link.getPath(move[i])));
        System.out.println("------");
    }
    
}
