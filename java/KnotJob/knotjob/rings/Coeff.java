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
 * @param <R>
 */
public class Coeff<R extends Ring<R>> implements Comparable<Coeff<R>>{
    
    private final R value;
    private final BigInteger power;
        
    public Coeff(R val, BigInteger p) {
        value = val;
        power = p;
    }

    @Override
    public String toString() {
        String val = value.toString();
        if (power.equals(BigInteger.ZERO)) return val;
        if (power.equals(BigInteger.ONE)) {
            if ("1".equals(val)) return "t";
            if ("-1".equals(val)) return "-t";
            return val +" t";
        }
        if ("1".equals(val)) return "t^"+power.toString();
        if ("-1".equals(val)) return "-t^"+power.toString();
        return val+" t^"+power.toString();
    }

    public boolean isOne(R unit) {
        if (!power.equals(BigInteger.ZERO)) return false;
        R comp = value.add(unit.negate());
        return comp.isZero();
    }

    public Coeff<R> add(Coeff<R> summand) {
        if (this.power.compareTo(summand.power) != 0) return null;
        return new Coeff<R>(value.add(summand.value), power);
    }

    public Coeff<R> multiply(Coeff<R> coe) {
        return new Coeff<R>(value.multiply(coe.value), power.add(coe.power));
    }
    
    public Coeff<R> negate() {
        return new Coeff<R>(value.negate(), power);
    }
    
    public boolean isZero() {
        return value.isZero();
    }
    
    @Override
    public int compareTo(Coeff<R> o) {
        return o.power.compareTo(power);
    }

    public Coeff<R> div(Coeff<R> coe) {
        return new Coeff<R>(value.div(coe.value), power.add(coe.power.negate()));
    }
    
    public Coeff<R> normalize() {
        return new Coeff<R>(value.invert(), BigInteger.ZERO);
    }
    
    public Coeff<R> invert() {
        return new Coeff<R>(value.invert(), power.negate());
    }
    
    public BigInteger degree() {
        return power;
    }

}
