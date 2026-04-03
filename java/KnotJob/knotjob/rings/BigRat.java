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

package knotjob.rings;

import java.math.BigInteger;

/**
 *
 * @author Dirk
 */
public class BigRat implements Ring<BigRat> {
    
    private final BigInteger numerator, denominator;

    public BigRat(BigInteger n) {
        numerator = n;
        denominator = BigInteger.ONE;
    }
    
    public BigRat(BigInteger num, BigInteger den) {
        BigInteger gcd = num.gcd(den);
        if (den.compareTo(BigInteger.ZERO) < 0) {
            numerator = num.divide(gcd).negate();
            denominator = den.divide(gcd).negate();
        }
        else {
            numerator = num.divide(gcd);
            denominator = den.divide(gcd);
        }
    }
    
    @Override
    public BigRat add(BigRat r) {
        return new BigRat(this.numerator.multiply(r.denominator).add(this.denominator.multiply(r.numerator)),
                this.denominator.multiply(r.denominator));
    }

    @Override
    public BigRat multiply(BigRat r) {
        return new BigRat(this.numerator.multiply(r.numerator),this.denominator.multiply(r.denominator));
    }

    @Override
    public String toString() {
        if (this.denominator.equals(BigInteger.ONE)) return this.numerator.toString();
        else return this.numerator.toString()+"/"+this.denominator.toString();
    }

    @Override
    public BigRat negate() {
        return new BigRat(numerator.negate(),denominator);
    }

    @Override
    public boolean isZero() {
        return (numerator.equals(BigInteger.ZERO));
    }

    @Override
    public boolean isInvertible() {
        return (!isZero());
    }

    @Override
    public BigRat invert() {
        return new BigRat(denominator, numerator);
    }

    @Override
    public BigRat getZero() {
        return new BigRat(BigInteger.ZERO);
    }

    @Override
    public boolean isBigger(BigRat r) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BigRat div(BigRat r) {
        return this.multiply(r.invert());
    }

    @Override
    public boolean divides(BigRat r) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BigRat abs(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
