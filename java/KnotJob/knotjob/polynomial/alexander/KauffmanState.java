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
import knotjob.polynomial.HalfPolynomial;

/**
 *
 * @author Dirk
 */
public class KauffmanState {
    
    private final ArrayList<Integer> chosenDiscs;
    private HalfPolynomial poly;
    
    public KauffmanState(HalfPolynomial pol, int cd) {
        chosenDiscs = new ArrayList<Integer>();
        chosenDiscs.add(cd);
        poly = pol;
    }
    
    public KauffmanState(KauffmanState state, HalfPolynomial pol, int cd) {
        chosenDiscs = new ArrayList<Integer>();
        for (int d : state.chosenDiscs) chosenDiscs.add(d);
        chosenDiscs.add(cd);
        poly = state.poly.multiply(pol);
    }
    
    public boolean contains(int dn) {
        return chosenDiscs.contains(dn);
    }
    
    @Override
    public String toString() {
        return chosenDiscs.toString()+" "+poly.toString();
    }

    public boolean zeroPoly() {
        return poly.isZero();
    }

    public boolean sameDots(KauffmanState nState) {
        boolean same = true;
        int i = 0;
        while (same && i < chosenDiscs.size()) {
            if (!nState.contains(chosenDiscs.get(i))) same = false;
            else i++;
        }
        return same;
    }

    public void add(KauffmanState nState) {
        poly = poly.add(nState.poly);
    }
    
    public String thePolynomial() {
        String str = poly.toReducedString();
        //if (poly.foxMilnor()) str = str+"F";
        return str;
    }
    
    public BigInteger theDeterminant() {
        return poly.evaluateAtMinusOne().abs();
    }
    
}
