/*

Copyright (C) 2021-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
public class HalfPolynomial extends Polynomial {
    
    public HalfPolynomial(String[] lbls, BigInteger val, int[] coef) {
        super(lbls, new ArrayList<Coefficient>());
        coefficients.add(new HalfCoefficient(coef, val));
    }
    
    public HalfPolynomial(String[] lbls, ArrayList<Coefficient> coeffs) {
        super(lbls, coeffs);
    }
    
    public HalfPolynomial(String[] lbls, String reducedInfo) {
        super(lbls, new ArrayList<Coefficient>());
        if ("".equals(reducedInfo)) return;
        int firstComma = reducedInfo.indexOf(",");
        int q = Integer.parseInt(reducedInfo.substring(0, firstComma));
        BigInteger val;
        reducedInfo = reducedInfo.substring(firstComma+1);
        if (reducedInfo.contains("F")) reducedInfo = reducedInfo.replace("F", "");
        if (reducedInfo.contains("N")) reducedInfo = reducedInfo.replace("N", "");
        do {
            firstComma = reducedInfo.indexOf(",");
            if (firstComma >= 0) val = new BigInteger(reducedInfo.substring(0, firstComma));
            else val = new BigInteger(reducedInfo);
            if (!val.equals(BigInteger.ZERO)) coefficients.add(new HalfCoefficient(new int[] {q}, val));
            q = q + 2;
            reducedInfo = reducedInfo.substring(firstComma+1);
        } while (firstComma >= 0);
    }
    
    public HalfPolynomial(Polynomial poly, String[] lbls, int[] div) {
        super(lbls, new ArrayList<Coefficient>());
        for (int i = 0; i < poly.getLength(); i++) {
            Coefficient coff = poly.getCoefficient(i);
            int[] pwrs = new int[div.length];
            for (int j = 0; j < div.length; j++) {
                pwrs[j] = coff.getPower(j)/div[j];
            }
            coefficients.add(new HalfCoefficient(pwrs, coff.getValue()));
        }
        Collections.sort(coefficients);
    }
    
    public HalfPolynomial multiply(HalfPolynomial pol) {
        ArrayList<Coefficient> mult = new ArrayList<Coefficient>();
        for (Coefficient coe : coefficients) {
            for (Coefficient col : pol.coefficients) {
                mult.add(((HalfCoefficient) coe).multiply((HalfCoefficient) col));
            }
        }
        Collections.sort(mult);
        combineCoefficients(mult);
        return new HalfPolynomial(labels, mult);
    }
    
    public HalfPolynomial add(HalfPolynomial pol) {
        ArrayList<Coefficient> add = new ArrayList<Coefficient>();
        for (Coefficient coe : coefficients) add.add((HalfCoefficient) coe);
        for (Coefficient coe : pol.coefficients) add.add((HalfCoefficient) coe);
        Collections.sort(add);
        combineCoefficients(add);
        return new HalfPolynomial(labels, add);
    }
    
    public BigInteger evaluateAtMinusOne() {
        BigInteger res = BigInteger.ZERO;
        for (Coefficient coef : coefficients) {
            if (coef.getPower(0)%4 != 0) res = res.add(coef.getValue().negate());
            else res = res.add(coef.getValue());
        }
        return res;
    }
    
    @Override
    protected void combineCoefficients(ArrayList<Coefficient> mult) {
        int i = mult.size()-1;
        while (i > 0) {
            HalfCoefficient coOne = (HalfCoefficient) mult.get(i-1);
            HalfCoefficient coTwo = (HalfCoefficient) mult.get(i);
            if (coOne.compareTo(coTwo) == 0) {
                HalfCoefficient comb = coOne.add(coTwo);
                mult.remove(i);
                mult.remove(i-1);
                if (comb.isZero()) i--;
                else mult.add(i-1,comb);
            }
            i--;
        }
    }
    
    public HalfPolynomial negate() {
        ArrayList<Coefficient> ncoeffs = new ArrayList<Coefficient>();
        for (Coefficient coeff : coefficients) {
            ncoeffs.add(new HalfCoefficient(coeff.powers, coeff.getValue().negate()));
        }
        return new HalfPolynomial(this.labels, ncoeffs);
    }
    
    public HalfPolynomial mirror() {
        ArrayList<Coefficient> mcoeffs = new ArrayList<Coefficient>();
        for (Coefficient coeff : coefficients) {
            int[] npowers = new int[coeff.powers.length];
            for (int i = 0; i < coeff.powers.length; i++) 
                npowers[i] = coeff.powers[i] * (-1);
            mcoeffs.add(new HalfCoefficient(npowers, coeff.getValue()));
        }
        Collections.sort(mcoeffs);
        return new HalfPolynomial(this.labels, mcoeffs);
    }
    
    public boolean foxMilnor() {
        if (labels.length > 1) return false; // sorry, no multi-var check
        if (!sameAsMirror()) return false;
        int k = -coefficients.get(0).getPower(0)/2;
        if (k == 0) return true;
        int[] theAs = new int[k+1]; // make the coefficients just integers, not BigIntegers.
        for (int i = 0; i <= k; i++) theAs[i] = getCoeff(2*(k-i));
        int root = (int) Math.sqrt(Math.abs(theAs[k]));
        for (int i = 1; i <= root; i++) {
            int bZero = i;
            if (theAs[0]% bZero == 0) {
                int bKay = theAs[0]/bZero;
                if (bZero * bZero + bKay * bKay <= theAs[k]) {
                    int max = theAs[k]-bZero*bZero-bKay*bKay;
                    int[] theBs = new int[k+1];
                    theBs[0] = bZero;
                    theBs[k] = bKay;
                    if (checkFoxMilnor(k, 0, theBs, max)) return true;
                    theBs[0] = -bZero;
                    theBs[k] = -bKay;
                    if (checkFoxMilnor(k, 0, theBs, max)) return true;
                }
            } 
        }
        return false;
    }

    private boolean checkFoxMilnor(int k, int i, int[] theBs, int max) {
        if (max < 0) return false;
        if (k-i == 1) {
            if (max != 0) return false;
            HalfPolynomial bPoly = polynomialFrom(theBs, k);
            return bPoly.multiply(bPoly.mirror()).add(this.negate()).isZero();
        }
        int root = (int) Math.sqrt(max);
        int[] theCs = new int[theBs.length];
        if (k - i == 2) {
            System.arraycopy(theBs, 0, theCs, 0, theBs.length);
            theCs[k/2] = root;
            HalfPolynomial bPoly = polynomialFrom(theCs, k);
            if (bPoly.multiply(bPoly.mirror()).add(this.negate()).isZero()) return true;
            theCs[k/2] = -root;
            bPoly = polynomialFrom(theCs, k);
            return bPoly.multiply(bPoly.mirror()).add(this.negate()).isZero();
        }
        for (int bNext = 0; bNext <= root; bNext++) {
            System.arraycopy(theBs, 0, theCs, 0, theBs.length);
            theCs[i/2+1] = bNext;
            HalfPolynomial bPoly = polynomialFrom(theCs, k);
            HalfPolynomial mPoly = bPoly.multiply(bPoly.mirror()).add(this.negate());
            int dif = mPoly.coefficientOf(2*k-i-2);
            if (dif%theBs[0] == 0) {
                theCs[k-1-i/2] = -dif/theBs[0];
                if (checkFoxMilnor(k, i+2, theCs, max - bNext * bNext - theCs[k-1-i/2] * theCs[k-1-i/2]))
                    return true;
            }
            if (bNext != 0) {
                System.arraycopy(theBs, 0, theCs, 0, theBs.length);
                theCs[i/2+1] = -bNext;
                bPoly = polynomialFrom(theCs, k);
                mPoly = bPoly.multiply(bPoly.mirror()).add(this.negate());
                dif = mPoly.coefficientOf(2*k-i-2);
                if (dif%theBs[0] == 0) {
                    theCs[k-1-i/2] = -dif/theBs[0];
                    if (checkFoxMilnor(k, i+2, theCs, max - bNext * bNext - theCs[k-1-i/2] * theCs[k-1-i/2]))
                        return true;
                }
            }
        }
        return false;
    }
    
    private int coefficientOf(int p) {
        for (int i = 0; i < coefficients.size(); i++) {
            if (coefficients.get(i).getPower(0) == p) 
                return coefficients.get(i).getValue().intValue();
        }
        return 0;
    }
    
    private HalfPolynomial polynomialFrom(int[] theBs, int k) {
        ArrayList<Coefficient> coeffs = new ArrayList<Coefficient>();
        for (int i = 0; i <= k; i++) {
            coeffs.add(new HalfCoefficient(new int[] {2*i}, BigInteger.valueOf(theBs[i])));
        }
        return new HalfPolynomial(labels, coeffs);
    }
    
    private int getCoeff(int p) {
        int k = coefficients.size()-1;
        while (coefficients.get(k).getPower(0)> p) k--;
        if (coefficients.get(k).getPower(0) == p) return coefficients.get(k).value.intValue();
        return 0;
    }
    
    private boolean sameAsMirror() {
        int k = coefficients.size()-1;
        for (int i = 0; i <= k; i++) {
            if (!coefficients.get(i).value.equals(coefficients.get(k-i).value) || 
                    coefficients.get(i).getPower(0) != -coefficients.get(k-i).getPower(0))
                return false;
        }
        return true;
    }
    
    public String toReducedString() {
        if (coefficients.isEmpty()) return "";
        int firstq = coefficients.get(0).getPower(0);
        String info = firstq+","+coefficients.get(0).value;
        for (int i = 1; i < coefficients.size(); i++) {
            firstq = firstq + 2;
            while (firstq != coefficients.get(i).getPower(0)) {
                firstq = firstq + 2;
                info = info + ",0";
            }
            info = info + ","+coefficients.get(i).value;
        }
        return info;
    }
    
    @Override
    public String toString() {
        String info = "";
        if (coefficients.isEmpty()) return "0";
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
