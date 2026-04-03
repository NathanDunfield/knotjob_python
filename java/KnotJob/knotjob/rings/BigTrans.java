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

/**
 *
 * @author Dirk
 */
public class BigTrans implements Ring<BigTrans> {
    
    private final Polynomial<BigRat> numerator, denominator;
    
    public BigTrans(BigInteger n) {
        if (n.equals(BigInteger.ZERO)) numerator = new Polynomial<BigRat>();
        else numerator = new Polynomial<BigRat>(new Coeff<BigRat>(new BigRat(n), BigInteger.ZERO));
        denominator = new Polynomial<BigRat>(new Coeff<BigRat>(new BigRat(BigInteger.ONE), BigInteger.ZERO));
    }
    
    public BigTrans() { // this creates the element 't'
        numerator = new Polynomial<BigRat>(new Coeff<BigRat>(new BigRat(BigInteger.ONE), BigInteger.ONE));
        denominator = new Polynomial<BigRat>(new Coeff<BigRat>(new BigRat(BigInteger.ONE), BigInteger.ZERO));
    }
    
    public BigTrans(Polynomial<BigRat> num, Polynomial<BigRat> den) {
        Polynomial<BigRat> gcd = num.gcd(den);
        den = den.div(gcd);
        Polynomial<BigRat> fac = new Polynomial<BigRat>(den.leadCoeff().normalize());
        numerator = num.div(gcd).multiply(fac);
        denominator = den.multiply(fac);
    }

    @Override
    public String toString() {
        if (this.isZero()) return "0";
        String val = numerator.toString();
        if (!denominator.isOne(new BigRat(BigInteger.ONE))) val = val + " / "+ denominator.toString();
        return val;
    }
    
    @Override
    public BigTrans add(BigTrans r) {
        return new BigTrans(this.numerator.multiply(r.denominator).add(this.denominator.multiply(r.numerator)),
                this.denominator.multiply(r.denominator));
    }

    @Override
    public BigTrans div(BigTrans r) {
        return this.multiply(r.invert());
    }

    @Override
    public BigTrans getZero() {
        return new BigTrans(BigInteger.ZERO);
    }

    @Override
    public BigTrans invert() {
        return new BigTrans(denominator, numerator);
    }

    @Override
    public boolean divides(BigTrans r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isBigger(BigTrans r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isInvertible() {
        return (!isZero());
    }

    @Override
    public boolean isZero() {
        return numerator.isZero();
    }

    @Override
    public BigTrans multiply(BigTrans r) {
        return new BigTrans(this.numerator.multiply(r.numerator),
                this.denominator.multiply(r.denominator));
    }

    @Override
    public BigTrans negate() {
        return new BigTrans(this.numerator.negate(), denominator);
    }

    @Override
    public BigTrans abs(int i) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
