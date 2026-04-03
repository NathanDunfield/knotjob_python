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

/**
 *
 * @author Dirk
 * @param <S>
 */
public class QuadExt<S extends Ring<S>> implements Ring<QuadExt<S>> {

    private final S square;
    private final S value, valueRoot;
    
    public QuadExt(S val, S squ) {
        square = squ;
        value = val;
        valueRoot = val.getZero();
    }
    
    public QuadExt(S val, S valr, S squ) {
        square = squ;
        value = val;
        valueRoot = valr;
    }
    
     @Override
    public String toString() {
        if (valueRoot.isZero()) return value.toString();
        String squ = "\u221A"+square.toString();
        if (value.isZero()) return valueRoot.toString()+squ;
        String add = "+";
        String sec = valueRoot.toString()+squ;
        if (sec.startsWith("-")) add = "";
        return value.toString()+add+sec;
    }
    
    @Override
    public QuadExt<S> add(QuadExt<S> r) {
        return new QuadExt<S>(value.add(r.value), valueRoot.add(r.valueRoot), square);
    }

    @Override
    public QuadExt<S> div(QuadExt<S> r) { // assuming we have a field
        return this.multiply(r.invert());
    }

    @Override
    public QuadExt<S> getZero() {
        return new QuadExt<S>(square.getZero(), square);
    }

    @Override
    public QuadExt<S> invert() {
        S fac = value.multiply(value).add(square.multiply(valueRoot.multiply(valueRoot)).negate());
        if (fac.isZero()) throw new ArithmeticException("Division by 0");
        return new QuadExt<S>(value.multiply(fac.invert()), valueRoot.multiply(fac.invert()).negate(), square);
    }

    @Override
    public boolean divides(QuadExt<S> r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isBigger(QuadExt<S> r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isInvertible() {
        S fac = value.multiply(value).add(square.multiply(valueRoot.multiply(valueRoot)));
        return fac.isInvertible();
    }

    @Override
    public boolean isZero() {
        return value.isZero() && valueRoot.isZero();
    }

    @Override
    public QuadExt<S> multiply(QuadExt<S> r) {
        return new QuadExt<S>(value.multiply(r.value).add(square.multiply(valueRoot.multiply(r.valueRoot))),
                value.multiply(r.valueRoot).add(valueRoot.multiply(r.value)), square);
    }

    @Override
    public QuadExt<S> negate() {
        return new QuadExt<S>(value.negate(), valueRoot.negate(), square);
    }

    @Override
    public QuadExt<S> abs(int i) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
