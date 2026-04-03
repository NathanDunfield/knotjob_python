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
 */
public class DoubleSymMatrix extends DoubleMatrix {
    
    public DoubleSymMatrix(int n) {
        super(n);
    }
    
    public void remove(int i) {
        super.remove(i, i);
        
    }
    
    @Override
    public void set(int i, int j, Double ent) {
        if (i != j) super.set(j, i, ent);
        super.set(i, j, ent);
    }
    
    public int signature() {
        int signature = 0;
        ArrayList<DoubleVector> basis = standardBasis();
        while (basis.size() > 0) {
            DoubleVector vec = smallestVector(basis);
            if (vec == null) return signature;
            Double val = evaluate(vec); //System.out.println(basis.size()+" "+val);
            if (val < 0.0) signature--;
            else signature++; // val equal to zero would have led to null
            basis.remove(vec);
            for (int i = 0; i < basis.size(); i++) { // now make remaining elements orthogonal to vec
                DoubleVector bi = basis.get(i);
                Double valvi = evaluate(vec, bi);
                if (valvi != 0) basis.set(i, bi.add(vec.multiply(-valvi/val)));
            }
        }
        return signature;
    }
    
    private DoubleVector smallestVector(ArrayList<DoubleVector> basis) {
        boolean keepGoing = true;
        int i = 0;
        int sm = -1;
        Double small = 0.0;
        while (keepGoing && i < basis.size()) {
            DoubleVector vec = basis.get(i);
            Double next = evaluate(vec);
            if (next != 0.0) {
                if (small == 0.0 || Math.abs(small)> Math.abs(next)) {
                    sm = i;
                    small = next;
                }
            }
            if (small!= 0) keepGoing = false;
            i++;
        }
        if (small!= 0) return basis.get(sm); // else we need to find some offdiagonal nonzero
        boolean found = false;
        i = 0;
        int j = 1;
        while (!found && i < basis.size()) {
            j = i+1;
            while (!found && j < basis.size()) {
                if (evaluate(basis.get(i), basis.get(j))!= 0.0) found = true;
                else j++;
            }
            if (!found) i++;
        }
        if (!found) return null; // remaining basiselements all in the kernel
        basis.set(i, basis.get(i).add(basis.get(j)));
        return basis.get(i);
    }
    
    private Double evaluate(DoubleVector vecOne, DoubleVector vecTwo) {
        Double eval = 0.0;
        for (int i = 0; i < rowNumber(); i++) {
            for (int j = 0; j < rowNumber(); j++) {
                Double vi = vecOne.get(i);
                Double vj = vecTwo.get(j);
                Double mij = get(i, j);
                if (!(vi == 0.0 || vj==0.0 || mij==0.0)) 
                    eval = eval+(vi*(vj*(mij)));
            }
        }
        return eval;
    }
    
    private Double evaluate(DoubleVector vec) {
        return evaluate(vec, vec);
    }
    
    private ArrayList<DoubleVector> standardBasis() {
        ArrayList<DoubleVector> basis = new ArrayList<DoubleVector>(rowNumber());
        for (int i = 0; i < rowNumber(); i++) {
            DoubleVector ei = new DoubleVector(rowNumber());
            ei.set(i, 1.0);
            basis.add(ei);
        }
        return basis;
    }
    
}
