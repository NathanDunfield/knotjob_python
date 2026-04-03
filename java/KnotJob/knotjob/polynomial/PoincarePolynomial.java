/*

Copyright (C) 2021 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
public class PoincarePolynomial extends Polynomial implements Comparable<PoincarePolynomial> {

    private final BigInteger torsionGroup;
    
    public PoincarePolynomial(String[] lbls, BigInteger val, int[] coef, BigInteger tor) {
        super(lbls, val, coef);
        torsionGroup = tor;
    }
    
    public PoincarePolynomial(String[] lbls, ArrayList<Coefficient> coeffs, BigInteger tor) {
        super(lbls, coeffs);
        torsionGroup = tor;
    }
    
    public BigInteger torsion() {
        return torsionGroup;
    }
    
    public PoincarePolynomial add(PoincarePolynomial pol) {
        ArrayList<Coefficient> add = new ArrayList<Coefficient>();
        for (Coefficient coe : coefficients) add.add(coe);
        for (Coefficient coe : pol.coefficients) add.add(coe);
        Collections.sort(add);
        combineCoefficients(add);
        return new PoincarePolynomial(labels, add, torsionGroup);
    }
    
    @Override
    public int compareTo(PoincarePolynomial po) {
        return torsionGroup.compareTo(po.torsionGroup);
    }
    
}
