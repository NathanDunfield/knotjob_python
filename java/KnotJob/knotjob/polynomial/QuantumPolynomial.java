/*

Copyright (C) 2024 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

/**
 *
 * @author Dirk
 */
public class QuantumPolynomial {
    
    public static Polynomial nThQuantumPol(int n) {
        ArrayList<Coefficient> coeffs = new ArrayList<Coefficient>();
        for (int i = -n+1; i < n; i++) {
            coeffs.add(new Coefficient(new int[] {i}, BigInteger.ONE));
        }
        return new Polynomial(new String[] {"q"}, coeffs);
    }
    
    public static Polynomial nChoosekQuantumPol(int n, int k) {
        if (k < 0 || k > n) return nThQuantumPol(0);
        if (k == 0) return nThQuantumPol(1);
        if (k > n/2) return nChoosekQuantumPol(n, n-k);
        Polynomial qTok = new Polynomial(new String[] {"q"}, BigInteger.ONE, new int[] {k});
        Polynomial qTokminus = new Polynomial(new String[] {"q"}, BigInteger.ONE, new int[] {k-n});
        return qTok.multiply(nChoosekQuantumPol(n-1, k)).add(qTokminus.multiply(nChoosekQuantumPol(n-1, k-1)));
    }
    
}
