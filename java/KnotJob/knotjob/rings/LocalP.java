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
public class LocalP implements Ring<LocalP> {
    
    private final BigInteger numerator, denominator;
    private final int[] inverted;
    
    public LocalP(BigInteger n, int[] inv) {
        numerator = n;
        denominator = BigInteger.ONE;
        inverted = inv;
    }
    
    public LocalP(BigInteger num, BigInteger den, int[] inv) {
        BigInteger gcd = num.gcd(den);
        if (den.compareTo(BigInteger.ZERO) < 0) {
            numerator = num.divide(gcd).negate();
            denominator = den.divide(gcd).negate();
        }
        else {
            numerator = num.divide(gcd);
            denominator = den.divide(gcd);
        }
        inverted = inv;
    }
    
    private BigInteger reducer() {
        BigInteger reduce = numerator.abs();
        for (int p : inverted) {
            BigInteger prime = new BigInteger(String.valueOf(p));
            while (reduce.mod(prime).equals(BigInteger.ZERO)) {
                reduce = reduce.divide(prime);
            }
        }
        return reduce;
    }
    
    @Override
    public LocalP add(LocalP r) {
        return new LocalP(this.numerator.multiply(r.denominator).add(this.denominator.multiply(r.numerator)),
                this.denominator.multiply(r.denominator),this.inverted);
    }

    @Override
    public LocalP div(LocalP r) {
        BigInteger red = r.reducer();
        BigInteger fac = r.numerator.divide(red);
        return new LocalP(this.numerator.divide(red).multiply(r.denominator),this.denominator.multiply(fac),this.inverted);
    }
    
    @Override
    public LocalP getZero() {
        return new LocalP(BigInteger.ZERO,this.inverted);
    }

    @Override
    public LocalP invert() {
        return new LocalP(denominator,numerator,inverted);
    }

    @Override
    public boolean isBigger(LocalP r) {
        return (this.reducer().compareTo(r.reducer()) > 0);
    }

    @Override
    public boolean isInvertible() {
        return (this.reducer().equals(BigInteger.ONE));
    }

    @Override
    public boolean isZero() {
        return (numerator.equals(BigInteger.ZERO));
    }

    @Override
    public LocalP multiply(LocalP r) {
            return new LocalP(this.numerator.multiply(r.numerator),this.denominator.multiply(r.denominator),this.inverted);
    }

    @Override
    public LocalP negate() {
        return new LocalP(this.numerator.negate(),this.denominator,this.inverted);
    }
    
    @Override
    public String toString() {
        if (this.denominator.equals(BigInteger.ONE)) return this.numerator.toString();
        else return this.numerator.toString()+"/"+this.denominator.toString();
    }

    @Override
    public boolean divides(LocalP r) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LocalP abs(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
