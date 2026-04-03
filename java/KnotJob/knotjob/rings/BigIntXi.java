/*

Copyright (C) 2023 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
public class BigIntXi implements Ring<BigIntXi> {

    private final BigInteger value;
    private final BigInteger xiValue;
    
    public BigIntXi(BigInteger val) {
        value = val;
        xiValue = BigInteger.ZERO;
    }
    
    public BigIntXi(BigInteger val, BigInteger xval) {
        value = val;
        xiValue = xval;
    } 
    
    @Override
    public BigIntXi add(BigIntXi r) {
        return new BigIntXi(value.add(r.value), xiValue.add(r.xiValue));
    }

    @Override
    public BigIntXi div(BigIntXi r) { // should only be used on subring Z or dividing by 1-+xi
        if (xiValue.equals(BigInteger.ZERO)) return new BigIntXi(value.divide(r.value));
        if (r.value.equals(BigInteger.ONE)) {
            if (r.xiValue.abs().equals(BigInteger.ONE)) return new BigIntXi(value);
        }  // it is assuming that ZetXi = a + a xi or a - a xi (which can be checked with divides) 
        return null;
    }

    @Override
    public BigIntXi getZero() {
        return new BigIntXi(BigInteger.ZERO);
    }

    @Override
    public BigIntXi invert() {
        if (this.isInvertible()) return this;
        return null;
    }

    @Override
    public boolean divides(BigIntXi r) { // should only be used on subring Z or 1-xi, 1+xi
        if (r.isZero()) return !isZero();
        if (isZero()) return false;
        if (xiValue.equals(BigInteger.ZERO)) return r.value.mod(value.abs()).equals(BigInteger.ZERO);
        if (xiValue.add(value).equals(BigInteger.ZERO)) return value.equals(xiValue.negate());
        // this was the case 1-xi (or xi-1)
        if (xiValue.add(value.negate()).equals(BigInteger.ZERO)) return value.equals(xiValue);
        // this was the case 1+xi (or -1-xi)
        return false; // can't handle other cases for now.
    }

    @Override
    public boolean isBigger(BigIntXi r) { // should only be used on subring Z
        return (value.abs().compareTo(r.value.abs()) > 0);
    }

    @Override
    public boolean isInvertible() {
        return (value.multiply(value).add(xiValue.multiply(xiValue)).equals(BigInteger.ONE));
    }

    @Override
    public boolean isZero() {
        return (value.equals(BigInteger.ZERO) && xiValue.equals(BigInteger.ZERO));
    }

    @Override
    public BigIntXi multiply(BigIntXi r) {
        return new BigIntXi(value.multiply(r.value).add(xiValue.multiply(r.xiValue)),
                value.multiply(r.xiValue).add(xiValue.multiply(r.value)));
    }

    @Override
    public BigIntXi negate() {
        return new BigIntXi(value.negate(), xiValue.negate());
    }

    @Override
    public BigIntXi abs(int i) {
        if (i > 4) {
            BigInteger two = new BigInteger("2");
            return new BigIntXi(value.add(xiValue).mod(two)); 
        }
        if (i > 2) {
            if (i == 4) return new BigIntXi(xiValue);
            return new BigIntXi(value);
        }
        if (i > 0) {
            BigInteger factor = BigInteger.ONE;
            if (i == 2) factor = factor.negate();
            return new BigIntXi(value.add(xiValue.multiply(factor)));
        }
        return new BigIntXi(value.abs());
    }
    
    public BigInt reduction(boolean plus) {
        if (plus) return new BigInt(value.add(xiValue));
        return new BigInt(value.add(xiValue.negate()));
    }
    
    @Override
    public String toString() {
        if (this.isZero()) return "0";
        if (xiValue.equals(BigInteger.ZERO)) return value.toString();
        String str = "";
        String xi = ""+(char) 958;
        String add = "+ ";
        if (xiValue.compareTo(BigInteger.ZERO) < 0) add = "- ";
        if (!value.equals(BigInteger.ZERO)) str = value.toString()+" "+add;
        else if ("- ".equals(add)) str = add;
        if (xiValue.abs().equals(BigInteger.ONE)) return str+xi;
        return str+xiValue.abs()+" "+xi;
    }
}
