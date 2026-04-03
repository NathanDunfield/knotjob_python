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
 * @param <S>
 */
public class RatFunc<S extends Ring<S>> implements Ring<RatFunc<S>> {

    private final Polynomial<S> numerator, denominator;
    private final S unit;
    
    public RatFunc(S val, S unt) {
        if (val.isZero()) numerator = new Polynomial<S>();
        else numerator = new Polynomial<S>(new Coeff<S>(val, BigInteger.ZERO));
        denominator = new Polynomial<S>(new Coeff<S>(unt, BigInteger.ZERO));
        unit = unt;
    }
    
    public RatFunc(S val, BigInteger n, S unt) {
        if (val.isZero()) numerator = new Polynomial<S>();
        else numerator = new Polynomial<S>(new Coeff<S>(val, n));
        denominator = new Polynomial<S>(new Coeff<S>(unt, BigInteger.ZERO));
        unit = unt;
    }
    
    public RatFunc(Polynomial<S> num, Polynomial<S> den, S unt) {
        Polynomial<S> gcd = num.gcd(den);
        den = den.div(gcd);
        Polynomial<S> fac = new Polynomial<S>(den.leadCoeff().normalize());
        numerator = num.div(gcd).multiply(fac);
        denominator = den.multiply(fac);
        unit = unt;
    }
    
    @Override
    public String toString() {
        if (this.isZero()) return "0";
        return numerator.toString() + " / "+ denominator.toString();
    }
    
    @Override
    public RatFunc<S> add(RatFunc<S> r) {
        return new RatFunc<S>(this.numerator.multiply(r.denominator).add(this.denominator.multiply(r.numerator)),
                this.denominator.multiply(r.denominator), unit);
    }

    @Override
    public RatFunc<S> div(RatFunc<S> r) {
        return this.multiply(r.invert());
    }

    @Override
    public RatFunc<S> getZero() {
        return new RatFunc<S>(unit.getZero(), unit);
    }

    @Override
    public RatFunc<S> invert() {
        return new RatFunc<S>(denominator, numerator, unit);
    }

    @Override
    public boolean divides(RatFunc<S> r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isBigger(RatFunc<S> r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isInvertible() {
        return !this.isZero();
    }

    @Override
    public boolean isZero() {
        return numerator.isZero();
    }

    @Override
    public RatFunc<S> multiply(RatFunc<S> r) {
        return new RatFunc<S>(this.numerator.multiply(r.numerator),
                this.denominator.multiply(r.denominator), unit);
    }

    @Override
    public RatFunc<S> negate() {
        return new RatFunc<S>(this.numerator.negate(), denominator, unit);
    }

    @Override
    public RatFunc<S> abs(int i) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    

    
    
    
    
}
