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

package knotjob.polynomial;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Dirk
 */
public class Polynomial {
    
    final protected ArrayList<Coefficient> coefficients;
    final protected String[] labels;
    protected boolean latex;
    
    public Polynomial(String[] lbls, BigInteger val, int[] coef) {
        coefficients = new ArrayList<Coefficient>();
        labels = lbls;
        coefficients.add(new Coefficient(coef, val));
    }
    
    public Polynomial(String[] lbls, ArrayList<Coefficient> coeffs) {
        coefficients = coeffs;
        labels = lbls;
        Collections.sort(coefficients);
    }
    
    public int getLength() {
        return coefficients.size();
    }
    
    public Coefficient getCoefficient(int i) {
        return coefficients.get(i);
    }
    
    public boolean isZero() {
        boolean zero = true;
        int i = 0;
        while (zero && i < coefficients.size()) {
            if (!coefficients.get(i).isZero()) zero = false;
            else i++;
        }
        return zero;
    }
    
    public Polynomial multiply(Polynomial pol) {
        ArrayList<Coefficient> mult = new ArrayList<Coefficient>();
        for (Coefficient coe : coefficients) {
            for (Coefficient col : pol.coefficients) {
                mult.add(coe.multiply(col));
            }
        }
        Collections.sort(mult);
        combineCoefficients(mult);
        return new Polynomial(labels, mult);
    }

    public Polynomial add(Polynomial pol) {
        ArrayList<Coefficient> add = new ArrayList<Coefficient>();
        for (Coefficient coe : coefficients) add.add(coe);
        for (Coefficient coe : pol.coefficients) add.add(coe);
        Collections.sort(add);
        combineCoefficients(add);
        return new Polynomial(labels, add);
    }
    
    protected void combineCoefficients(ArrayList<Coefficient> mult) {
        int i = mult.size()-1;
        while (i > 0) {
            Coefficient coOne = mult.get(i-1);
            Coefficient coTwo = mult.get(i);
            if (coOne.compareTo(coTwo) == 0) {
                Coefficient comb = coOne.add(coTwo);
                mult.remove(i);
                mult.remove(i-1);
                if (comb.isZero()) i--;
                else mult.add(i-1,comb);
            }
            i--;
        }
    }
    
    public void setLatex(boolean lat) {
        latex = lat;
    }
    
    @Override
    public String toString() {
        String info = "";
        if (coefficients.isEmpty()) return " 0";
        int i = 0;
        while (i < coefficients.size()-1) {
            info = info + coefficients.get(i).toString(latex, labels);
            if (coefficients.get(i+1).getValue().compareTo(BigInteger.ZERO) >= 0) info = info+" +"; 
            i++;
        }
        info = info + coefficients.get(coefficients.size()-1).toString(latex, labels);
        return info;
    }
    
}
