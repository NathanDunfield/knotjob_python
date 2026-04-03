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
public class PolFunc<S extends Ring<S>> implements Ring<PolFunc<S>> {

    private final Polynomial<S> poly;
    private final S unit;
    
    public PolFunc(S val, S unt) {
        if (val.isZero()) poly = new Polynomial<S>();
        else poly = new Polynomial<S>(new Coeff<S>(val, BigInteger.ZERO));
        unit = unt;
    }
    
    public PolFunc(S val, BigInteger n, S unt) {
        if (val.isZero()) poly = new Polynomial<S>();
        else poly = new Polynomial<S>(new Coeff<S>(val, n));
        unit = unt;
    }
    
    public PolFunc(Polynomial<S> pol, S unt) {
        poly = pol;
        unit = unt;
    }
    
    @Override
    public String toString() {
        if (this.isZero()) return "0";
        return poly.toString();
    }
    
    @Override
    public PolFunc<S> add(PolFunc<S> r) {
        return new PolFunc<S>(poly.add(r.poly), unit);
    }

    @Override
    public PolFunc<S> div(PolFunc<S> r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public PolFunc<S> getZero() {
        return new PolFunc<S>(unit.getZero(), unit);
    }

    @Override
    public PolFunc<S> invert() { // assumes S field and poly invertible
        return new PolFunc<S>(new Polynomial<S>(poly.leadCoeff().invert()), unit);
    }

    @Override
    public boolean divides(PolFunc<S> r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isBigger(PolFunc<S> r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isInvertible() { // assumes S a field, and treats as Laurent polynomial
        return poly.stretch().equals(BigInteger.ZERO);
    }

    @Override
    public boolean isZero() {
        return poly.isZero();
    }

    @Override
    public PolFunc<S> multiply(PolFunc<S> r) {
        return new PolFunc<S>(poly.multiply(r.poly), unit);
    }

    @Override
    public PolFunc<S> negate() {
        return new PolFunc<S>(poly.negate(), unit);
    }

    @Override
    public PolFunc<S> abs(int i) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
