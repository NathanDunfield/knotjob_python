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

package knotjob.rings;

import java.util.ArrayList;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class SymMatrix<R extends Ring<R>> extends Matrix<R> {
    
    public SymMatrix(int n, R unt) {
        super(n, unt);
    }
    
    public SymMatrix(SymMatrix<R> mat, int i, int j) {
        super(mat, i, j);
    }
    
    public void remove(int i) {
        super.remove(i, i);
        
    }
    
    @Override
    public void set(int i, int j, R ent) {
        if (i != j) super.set(j, i, ent);
        super.set(i, j, ent);
    }
    
    public long[] detSignature() {
        long[] detSign = new long[2];
        ArrayList<Long> determinants = new ArrayList<Long>();
        long det = Long.parseLong(this.determinant().toString());
        detSign[0] = Math.abs(det);
        determinants.add(det);
        if (det == 0) {
            detSign[1] = this.signature();
            return detSign;
        }
        int j = rowNumber()-1;
        SymMatrix<R> firstMatrix = this;
        while (j > 0) {
            SymMatrix<R> nextMatrix = new SymMatrix<R>(firstMatrix, j, j);
            long ndet = Long.parseLong(nextMatrix.determinant().toString());
            if (ndet == 0) {
                detSign[1] = firstMatrix.signature()+changes(determinants);
                return detSign;
            }
            determinants.add(0, ndet);
            firstMatrix = nextMatrix;
            j--;
        }
        if (determinants.get(0)>0) detSign[1]++;
        if (determinants.get(0)<0) detSign[1]--;
        detSign[1] = detSign[1]+changes(determinants);
        return detSign;
    }
    
    private int changes(ArrayList<Long> dets) {
        int val = 0;
        long fi = dets.get(0);
        for (int i = 1; i < dets.size(); i++) {
            long nx = dets.get(i);
            if (fi * nx > 0) val++;
            else val--;
            fi = nx;
        }
        return val;
    }
    
    public int signature() {
        int signature = 0;
        ArrayList<Vector<R>> basis = this.standardBasis();
        while (!basis.isEmpty()) {
            Vector<R> vec = smallestVector(basis);
            if (vec == null) return signature;
            R val = evaluate(vec);
            if (val.add(val.abs(0)).isZero()) signature--;
            else signature++; // val equal to zero should have led to null
            basis.remove(vec);
            for (int i = 0; i < basis.size(); i++) { // now make remaining elements orthogonal to vec
                Vector<R> bi = basis.get(i);
                R valvi = evaluate(vec, bi);
                if (!valvi.isZero()) basis.set(i, bi.multiply(val).add(vec.multiply(valvi.negate())));
            }
        }
        return signature;
    }
    
    private Vector<R> smallestVector(ArrayList<Vector<R>> basis) {
        boolean keepGoing = true;
        int i = 0;
        int sm = -1;
        R small = unit.getZero();
        while (keepGoing && i < basis.size()) {
            Vector<R> vec = basis.get(i);
            R next = evaluate(vec);
            if (!next.isZero()) {
                if (small.isZero() || small.isBigger(next)) {
                    sm = i;
                    small = next;
                }
            }
            if (small.isInvertible()) keepGoing = false;
            i++;
        }
        if (!small.isZero()) return basis.get(sm); // else we need to find some offdiagonal nonzero
        boolean found = false;
        i = 0;
        int j = 1;
        while (!found && i < basis.size()) {
            j = i+1;
            while (!found && j < basis.size()) {
                if (!evaluate(basis.get(i), basis.get(j)).isZero()) found = true;
                else j++;
            }
            if (!found) i++;
        }
        if (!found) return null; // remaining basiselements all in the kernel
        basis.set(i, basis.get(i).add(basis.get(j)));
        return basis.get(i);
    }
    
    private R evaluate(Vector<R> vecOne, Vector<R> vecTwo) {
        R eval = unit.getZero();
        for (int i = 0; i < rowNumber(); i++) {
            for (int j = 0; j < rowNumber(); j++) {
                R vi = vecOne.get(i);
                R vj = vecTwo.get(j);
                R mij = get(i, j);
                if (!(vi.isZero() || vj.isZero() || mij.isZero())) 
                    eval = eval.add(vi.multiply(vj.multiply(mij)));
            }
        }
        return eval;
    }
    
    private R evaluate(Vector<R> vec) {
        return evaluate(vec, vec);
    }
    
    private ArrayList<Vector<R>> standardBasis() {
        ArrayList<Vector<R>> basis = new ArrayList<Vector<R>>(this.rowNumber());
        for (int i = 0; i < this.rowNumber(); i++) {
            Vector<R> ei = new Vector<R>(this.rowNumber(), unit);
            ei.set(i, unit);
            basis.add(ei);
        }
        return basis;
    }
    
}
