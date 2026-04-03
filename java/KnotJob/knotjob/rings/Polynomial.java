/*

Copyright (C) 2025 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.rings;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class Polynomial<R extends Ring<R>> {
    
    private final ArrayList<Coeff<R>> coefficients;
    
    public Polynomial(Coeff<R> coeff) {
        coefficients = new ArrayList<Coeff<R>>();
        coefficients.add(coeff);
    }
    
    public Polynomial() { // represents the 0 polynomial
        coefficients = new ArrayList<Coeff<R>>();
    }
    
    public Polynomial(ArrayList<Coeff<R>> coeffs) {
        coefficients = coeffs;
    }

    public boolean isOne(R unit) {
        if (coefficients.size() != 1) return false;
        return coefficients.get(0).isOne(unit);
    }
    
    @Override
    public String toString() {
        if (coefficients.isEmpty()) return "0";
        String value = coefficients.get(0).toString();
        for (int i = 1; i < coefficients.size(); i++) {
            String nextSum = coefficients.get(i).toString();
            String adder = " + ";
            if (nextSum.startsWith("-")) {
                adder = " - ";
                nextSum = nextSum.substring(1);
            }
            value = value + adder + nextSum;
        }
        return value;
    }

    public boolean isZero() {
        return coefficients.isEmpty();
    }
    
    public Polynomial<R> multiply(Polynomial<R> pol) {
        ArrayList<Coeff<R>> mult = new ArrayList<Coeff<R>>();
        for (Coeff<R> coe : coefficients) {
            for (Coeff<R> col : pol.coefficients) {
                mult.add(coe.multiply(col));
            }
        }
        Collections.sort(mult);
        combineCoefficients(mult);
        return new Polynomial<R>(mult);
    }
    
    public Polynomial<R> negate() {
        ArrayList<Coeff<R>> neg = new ArrayList<Coeff<R>>();
        for (Coeff<R> coe : coefficients) neg.add(coe.negate());
        return new Polynomial<R>(neg);
    }
    
    public Polynomial<R> add(Polynomial<R> pol) {
        ArrayList<Coeff<R>> add = new ArrayList<Coeff<R>>();
        for (Coeff<R> coe : coefficients) add.add(coe);
        for (Coeff<R> coe : pol.coefficients) add.add(coe);
        Collections.sort(add);
        combineCoefficients(add);
        return new Polynomial<R>(add);
    }
    
    private void combineCoefficients(ArrayList<Coeff<R>> mult) {
        int i = mult.size()-1;
        while (i > 0) {
            Coeff<R> coOne = mult.get(i-1);
            Coeff<R> coTwo = mult.get(i);
            if (coOne.compareTo(coTwo) == 0) {
                Coeff<R> comb = coOne.add(coTwo);
                mult.remove(i);
                mult.remove(i-1);
                if (comb.isZero()) i--;
                else mult.add(i-1, comb);
            }
            i--;
        }
    }
    
    public BigInteger degree() {
        if (this.isZero()) return BigInteger.valueOf(-1);
        return coefficients.get(0).degree();
    }
    
    public Polynomial<R> normalized() {
        if (this.isZero()) return this;
        Polynomial<R> factor = new Polynomial<R>(coefficients.get(0).normalize());
        return this.multiply(factor);
    }
    
    public Polynomial<R> gcd(Polynomial<R> pol) { // better make sure that R is a field
        if (this.isZero()) return pol.normalized();
        if (pol.isZero()) return this.normalized();
        Polynomial<R> mod = this.mod(pol);
        if (mod.isZero()) return pol.normalized();
        return pol.gcd(mod);
    }

    public Polynomial<R> div(Polynomial<R> pol) {
        if (pol.degree().compareTo(this.degree()) > 0) return new Polynomial<R>(); // returns 0
        Polynomial<R> quot = new Polynomial<R>(this.coefficients.get(0).div(pol.coefficients.get(0)));
        Polynomial<R> next = pol.multiply(quot);
        Polynomial<R> div = this.add(next.negate()).div(pol);
        return div.add(quot);
    }
    
    public Polynomial<R> mod(Polynomial<R> pol) {
        if (pol.degree().compareTo(this.degree()) > 0) return this;
        Polynomial<R> quot = new Polynomial<R>(this.coefficients.get(0).div(pol.coefficients.get(0)));
        Polynomial<R> next = pol.multiply(quot);
        return this.add(next.negate()).mod(pol);
    }
    
    public Coeff<R> leadCoeff() {
        return coefficients.get(0);
    }
    
    public BigInteger stretch() {
        if (coefficients.isEmpty()) return BigInteger.ONE.negate();
        Coeff<R> fCoff = coefficients.get(0);
        Coeff<R> lCoff = coefficients.get(coefficients.size()-1);
        return fCoff.degree().add(lCoff.degree().negate());
    }
}
