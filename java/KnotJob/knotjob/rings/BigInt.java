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
public class BigInt implements Ring<BigInt> {
    
    private final BigInteger value;

    public BigInt(BigInteger i) {
        value = i;
    }
    
    public BigInt(int v) {
        value = BigInteger.valueOf(v);
    }
    
    @Override
    public BigInt add(BigInt r) {
        return new BigInt(value.add(r.value));
    }
    
    @Override
    public BigInt multiply(BigInt r) {
        return new BigInt(value.multiply(r.value));
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public BigInt negate() {
        return new BigInt(value.negate());
    }

    @Override
    public boolean isZero() {
        return value.equals(BigInteger.ZERO);
    }

    @Override
    public boolean isInvertible() {
        return value.abs().equals(BigInteger.ONE);
    }

    @Override
    public BigInt invert() {
        return new BigInt(value); // Need to check it's really invertible first
    }

    @Override
    public BigInt getZero() {
        return new BigInt(0);
    }

    @Override
    public boolean isBigger(BigInt r) {
        return (value.abs().compareTo(r.value.abs()) > 0);
    }

    @Override
    public BigInt div(BigInt r) {
        return new BigInt(value.divide(r.value));
    }

    @Override
    public boolean divides(BigInt r) {
        if (r.isZero()) return !isZero();
        if (isZero()) return false;
        return r.value.mod(value.abs()).equals(BigInteger.ZERO);
    }
    
    @Override
    public BigInt abs(int i) {
        if (i == 0) return new BigInt(value.abs());
        return new BigInt(value.abs().mod(new BigInteger("2")));
    }
}