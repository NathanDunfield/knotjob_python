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

package knotjob.invariants;

import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.rings.BigInt;
import knotjob.rings.DoubleSymMatrix;
import knotjob.rings.SymMatrix;

/**
 *
 * @author Dirk
 */
public class SignatureKnot {

    private final Link theLink;
    private final String name;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final Options options;
    private String detString;
    private String sigString;
    
    public SignatureKnot(LinkData link, DialogWrap frm, Options optns) {
        theLink = Reidemeister.freeOfOne(link.chosenLink()).breakUp().girthMinimize();
        name = link.name;
        frame = frm;
        options = optns;
        abInf = frame.getAbortInfo();
    }

    public void calculate() {
        if (theLink.crossingNumber() == 0) {
            sigString = "0";
            detString = "1";
            if (theLink.unComponents() != 1) detString = "0";
            return;
        }
        ArrayList<ArrayList<Integer[]>> theDiscs = theLink.getDiscs();
        int[] blackAndWhite = new int[theDiscs.size()];
        blackAndWhite[0] = 1;
        while (containsZero(blackAndWhite)) {
            for (int i = 0; i < theDiscs.size(); i++) {
                if (!containsZero(blackAndWhite)) break;
                int bow = blackAndWhite[i];
                if (bow != 0) {
                    for (Integer[] inf : theDiscs.get(i)) {
                        setDisc(theDiscs, inf[0], (inf[1]+2)%4, blackAndWhite, bow);
                        setDisc(theDiscs, inf[0], (inf[1]+1)%4, blackAndWhite, -bow);
                        setDisc(theDiscs, inf[0], (inf[1]+3)%4, blackAndWhite, -bow);
                    }
                }
            }
        }
        int num = countOnesIn(blackAndWhite);
        if (num > blackAndWhite.length/2) num = num - blackAndWhite.length;
        int white = num/Math.abs(num);
        removeDiscs(theDiscs, blackAndWhite, -white);
        int[] crossingTypes = crossingTypes(theDiscs);
        SymMatrix<BigInt> matrix = goeritzMatrix(crossingTypes, theDiscs);
        ArrayList<Integer> directions = theLink.getDirections();
        boolean[] typeIICrossings = type2Crossings(theDiscs, directions);
        int muOfDiagram = theMu(typeIICrossings, crossingTypes);
        long[] detSign = matrix.detSignature();
        int signature = (int) detSign[1] + muOfDiagram;
        detString = String.valueOf(detSign[0]);
        sigString = String.valueOf(signature);
    }

    public String getDeterminant() {
        return detString;
    }
    
    public String getSignature() {
        return sigString;
    }
    
    private boolean containsZero(int[] baw) {
        boolean contains = false;
        int i = 0;
        while (!contains && i < baw.length) {
            if (baw[i] == 0) contains = true;
            else i++;
        }
        return contains;
    }

    private void setDisc(ArrayList<ArrayList<Integer[]>> theDiscs, int crs, int pth, 
            int[] blackAndWhite, int bow) {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (contains(theDiscs.get(i), crs, pth)) found = true;
            else i++;
        }
        blackAndWhite[i] = bow;
    }
    
    private boolean contains(ArrayList<Integer[]> disc, int crs, int pth) {
        boolean cont = false;
        int j = 0;
        while (!cont && j < disc.size()) {
            if (disc.get(j)[0] == crs && disc.get(j)[1] == pth) cont = true;
            else j++;
        }
        return cont;
    }

    private int countOnesIn(int[] blackAndWhite) {
        int num = 0;
        for (int k : blackAndWhite) {
            if (k == 1) num++;
        }
        return num;
    }

    private int[] crossingTypes(ArrayList<ArrayList<Integer[]>> theDiscs) {
        int[] theTypes = new int[theLink.crossingNumber()];
        for (int k = 0; k < theDiscs.size(); k++) {
            ArrayList<Integer[]> inf = theDiscs.get(k);
            for (Integer[] pos : inf) {
                if (pos[1]%2 == 0) theTypes[pos[0]] = 2 * theLink.getCross(pos[0]);
                else theTypes[pos[0]] = -theLink.getCross(pos[0]);
            }
        }
        return theTypes;
    }
    
    private void removeDiscs(ArrayList<ArrayList<Integer[]>> theDiscs, 
            int[] blackAndWhite, int black) {
        int j = theDiscs.size()-1;
        while (j >= 0) {
            if (blackAndWhite[j] == black) theDiscs.remove(j);
            j--;
        }
    }

    private SymMatrix<BigInt> goeritzMatrix(int[] crossingTypes, 
            ArrayList<ArrayList<Integer[]>> theDiscs) {
        BigInt unit = new BigInt(1);
        int n = theDiscs.size();
        SymMatrix<BigInt> matrix = new SymMatrix<BigInt>(n, unit);
        int max = 0;
        int maxn = 0;
        for (int i = 0; i < n; i++) {
            if (theDiscs.get(i).size() > maxn) {
                maxn = theDiscs.get(i).size();
                max = i;
            }
            for (int j = i+1; j < n; j++) {
                BigInt val = unit.getZero();
                ArrayList<Integer> overlap = overlappingCrossings(theDiscs.get(i), theDiscs.get(j));
                for (Integer cros : overlap) {
                    if (crossingTypes[cros] < 0) val = val.add(unit.negate());
                    else val = val.add(unit);
                }
                matrix.set(i, j, val);
            }
        }
        for (int i = 0; i < n; i++) {
            BigInt val = unit.getZero();
            for (int j = 0; j < n; j++) val = val.add(matrix.get(i, j));
            matrix.set(i, i, val.negate());
        }
        matrix.remove(max);
        return matrix;
    }

    private ArrayList<Integer> overlappingCrossings(ArrayList<Integer[]> discOne, 
            ArrayList<Integer[]> discTwo) {
        ArrayList<Integer> crossings = new ArrayList<Integer>();
        for (int i = 0; i < theLink.crossingNumber(); i++) {
            if (containsCrossing(discOne, i) && containsCrossing(discTwo, i)) crossings.add(i);
        }
        return crossings;
    }

    private boolean containsCrossing(ArrayList<Integer[]> discOne, int i) {
        boolean found = false;
        int j = 0;
        while (!found && j < discOne.size()) {
            if (discOne.get(j)[0] == i) found = true;
            j++;
        } 
        return found;
    }
    
    private boolean[] type2Crossings(ArrayList<ArrayList<Integer[]>> theDiscs, 
            ArrayList<Integer> directions) {
        boolean[] types = new boolean[directions.size()];
        for (int i = 0; i < directions.size(); i++) {
            int k = blackStuff(theDiscs, i);
            if (directions.get(i) == 0 || directions.get(i) == 3) {
                if (k == 0) types[i] = true;
            }
            else {
                if (k == 1) types[i] = true;
            }
        }
        return types;
    }
    
    private int blackStuff(ArrayList<ArrayList<Integer[]>> theDiscs, int i) {
        boolean found = false;
        int j = 0;
        int k = 0;
        while (!found && j < theDiscs.size()) {
            ArrayList<Integer[]> discOne = theDiscs.get(j);
            k = 0;
            while (!found && k < discOne.size()) {
                if (discOne.get(k)[0] == i) found = true;
                else k++;
            }
            if (!found) j++;
        }
        int dir = theDiscs.get(j).get(k)[1];
        if (dir % 2 == 0) return 0;
        return 1;
    }

    private int theMu(boolean[] typeIICrossings, int[] crossingTypes) {
        //System.out.println(Arrays.toString(typeIICrossings));
        //System.out.println(Arrays.toString(crossingTypes));
        int val = 0;
        for (int i = 0; i < typeIICrossings.length; i++) {
            if (typeIICrossings[i]) {
                if (crossingTypes[i] < 0) val--;
                else val++;
            }
        }
        return val;
    }
    
    private DoubleSymMatrix goeritzDoubleMatrix(int[] crossingTypes, 
            ArrayList<ArrayList<Integer[]>> theDiscs) {
        int n = theDiscs.size();
        DoubleSymMatrix matrix = new DoubleSymMatrix(n);
        int max = 0;
        int maxn = 0;
        for (int i = 0; i < n; i++) {
            if (theDiscs.get(i).size() > maxn) {
                maxn = theDiscs.get(i).size();
                max = i;
            }
            for (int j = i+1; j < n; j++) {
                Double val = 0.0;
                ArrayList<Integer> overlap = overlappingCrossings(theDiscs.get(i), theDiscs.get(j));
                for (Integer cros : overlap) {
                    if (crossingTypes[cros] < 0) val = val-1.0;
                    else val = val+1.0;
                }
                matrix.set(i, j, val);
            }
        }
        for (int i = 0; i < n; i++) {
            Double val = 0.0;
            for (int j = 0; j < n; j++) val = val+(matrix.get(i, j));
            matrix.set(i, i, -val);
        }
        matrix.remove(max);
        return matrix;
    }
    
}
