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

package knotjob.polynomial.alexander;

import java.math.BigInteger;
import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.polynomial.HalfPolynomial;

/**
 *
 * @author Dirk
 */
public class AlexanderPolynomial {
    
    private final Link theLink;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final Options options;
    private final int[] girth;
    private String alexPoly;
    private BigInteger determinant;
    
    public AlexanderPolynomial(LinkData link, DialogWrap frm, Options optns) {
        theLink = Reidemeister.freeOfOne(link.chosenLink()).breakUp().girthMinimize();
        frame = frm;
        options = optns;
        abInf = frame.getAbortInfo();
        girth = theLink.totalGirthArray();
        alexPoly = null;
    }
    
    public void calculate() {
        if (obviousUnknot()) {
            alexPoly = "0,1";
            determinant = BigInteger.ONE;
            return;
        }
        if (obviousSplitLink()) {
            alexPoly = "";
            determinant = BigInteger.ZERO;
            return;
        }
        ArrayList<ArrayList<Integer>> theDiscs = getDiscs(theLink.getDiscs());
        ArrayList<Integer> directions = theLink.getDirections();
        StateList theList = new StateList(theDiscs, directions);
        calculatePolynomial(theList);
        alexPoly = theList.finalPolynomial();
        determinant = theList.determinant();
    }
    
    public String alexanderInfo() {
        return alexPoly;
    }
    
    public String detInfo() {
        return determinant.toString();
    }
    
    public boolean foxMilnor() {
        BigInteger root = determinant.sqrt();
        if (root.multiply(root).compareTo(determinant) < 0) return false;
        HalfPolynomial thePoly = new HalfPolynomial(new String[] {"t"}, alexPoly);
        return thePoly.foxMilnor();
    }

    private ArrayList<ArrayList<Integer>> getDiscs(ArrayList<ArrayList<Integer[]>> discs) {
        ArrayList<ArrayList<Integer>> newDiscs = new ArrayList<ArrayList<Integer>>();
        for (ArrayList<Integer[]> disc : discs) {
            ArrayList<Integer> newDisc = new ArrayList<Integer>(disc.size());
            for (Integer[] inf : disc) {
                newDisc.add(theLink.getPath(inf[0], inf[1]));
            }
            newDiscs.add(newDisc);
        }
        return newDiscs;
    }

    /*private ArrayList<Integer> getDirections() {
        ArrayList<Integer> dirs = new ArrayList<Integer>();
        ArrayList<ArrayList<Integer>> comps = theLink.getComponents();
        for (int i = 0; i < theLink.crossingLength(); i++) {
            int[] path = theLink.getPath(i);
            ArrayList<Integer> comp = compContaining(comps, path[0]);
            int dirOne = getDirection(comp, path[0], path[2], i);
            comp = compContaining(comps, path[3]);
            int dirTwo = getDirection(comp, path[3], path[1], i);
            dirs.add(dirOne + 2 * dirTwo); // 0 means north, 1 means east, 2 means west, 3 means south
        }
        return dirs;
    }

    private ArrayList<Integer> compContaining(ArrayList<ArrayList<Integer>> comps, int i) {
        boolean found = false;
        int j = 0;
        while (!found) {
            if (comps.get(j).contains(i)) found = true;
            else j++;
        }
        return comps.get(j);
    }
    
    private int getDirection(ArrayList<Integer> comp, int l, int u, int pi) {
        if (comp.size() == 2) {
            int[] or = theLink.orientation(theLink.getComponents().indexOf(comp));
            int os = theLink.getPath(or[0], or[1]);
            if (pi == or[0]) {
                if (os == l) return 0; // 0 means going up
                return 1;              // 1 means going down
            }
            if (os == l) return 1;
            return 0;
        }
        int i = comp.indexOf(l);
        int j = comp.indexOf(u);
        if (i < j) {
            if (j - i == 1) return 0;
            return 1;
        }
        if (i - j == 1) return 1;
        return 0;
    }// */

    private void calculatePolynomial(StateList theList) {
        theList.getStarted(theLink.getCross(0), theLink.getPath(0));
        for (int i = 1; i < theLink.crossingNumber(); i++) {
            frame.setLabelRight(String.valueOf(i+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            frame.setLabelRight(girthInfo(i), 1, false);
            theList.combineWith(theLink.getCross(i), theLink.getPath(i));
        }
    }
    
    private String girthInfo(int u) {
        String info = String.valueOf(girth[u]);
        if (options.getGirthInfo()!= 2) return info;
        if (u < girth.length - 1) info = info+" ("+girth[u+1];
        else return info;
        for (int i = 1; i < 3; i++) {
            if (u < girth.length - i - 1) info = info+", "+girth[u+1+i];
        }
        info = info+")";
        return info;
    }

    private boolean obviousUnknot() {
        return theLink.components() == 1 && theLink.crossingLength() == 0;
    }

    private boolean obviousSplitLink() {
        if (theLink.unComponents() == 1 && theLink.crossingLength() > 0) return true;
        return theLink.splitComponents().size() > 1 || theLink.unComponents() > 1;
    }
    
}
