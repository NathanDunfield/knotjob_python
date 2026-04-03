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
public class DoubleMatrix {
    
    private final ArrayList<DoubleVector> rows;
    
    public DoubleMatrix(int n) {
        rows = new ArrayList<DoubleVector>(n);
        for (int i = 0; i < n; i++) rows.add(new DoubleVector(n));
    }
    
    public DoubleMatrix(int n, int m) {
        rows = new ArrayList<DoubleVector>(n);
        for (int i = 0; i < n; i++) rows.add(new DoubleVector(m));
    }
    
    public DoubleMatrix(DoubleMatrix mat, int i, int j) {
        rows = new ArrayList<DoubleVector>(mat.rowNumber()-1);
        for (int k = 0; k < mat.rowNumber()-1; k++) 
            rows.add(new DoubleVector(mat.columnNumber()-1));
        for (int k = 0; k < mat.rowNumber()-1; k++) {
            int kk = k;
            if (kk >= i) kk++;
            for (int l = 0; l < mat.columnNumber()-1; l++) {
                int ll = l;
                if (ll >= j) ll++;
                rows.get(k).set(l, mat.get(kk, ll));
            }
        }
    }
    
    public int rowNumber() {
        return rows.size();
    }
    
    public int columnNumber() {
        return rows.get(0).size();
    }
    
    public Double get(int i, int j) {
        return rows.get(i).get(j);
    }
    
    public void set(int i, int j, Double ent) {
        rows.get(i).set(j, ent);
    }
    
    public Double determinant() {
        return clonedMatrix().determinant(-1, -1);
    }
    
    private DoubleMatrix clonedMatrix() {
        DoubleMatrix clone = new DoubleMatrix(rowNumber(), columnNumber());
        for (int i = 0; i < rowNumber(); i++) {
            for (int j = 0; j < columnNumber(); j++) clone.set(i, j, get(i, j));
        }
        return clone;
    }
    
    private Double determinant(int i, int j) {
        if (rowNumber() == 1) return get(0, 0);
        if (i == -1) {
            int[] ind = smallestEntry();
            i = ind[0];
            j = ind[1];
        }
        Double smallest = get(i, j);
        if (smallest==0.0) return smallest; // it's a zero matrix
        if (restZeroColumn(i, j) || restZeroRow(i, j)) {
            Double factor = get(i, j);
            if ((i+j)%2 != 0) factor = -factor;
            return factor * (removeMatrix(i, j).determinant(-1, -1));
        }
        int k = divideEntriesColumn(i, j);
        int l = divideEntriesRow(i, j);
        if (k < l){ // shuffle rows
            for (int m = 0; m < rowNumber(); m++) {
                if (m != i) {
                    if (get(m, j)!=0) addRow(m, i, (get(m, j)/(-get(i, j))));
                }
            }
        }
        else { // shuffle columns
            for (int m = 0; m < columnNumber(); m++) {
                if (m != j) {
                    if (get(i, m)!=0.0) addColumn(m, j, (get(i, m)/(-get(i, j))));
                }
            }
        }
        return determinant(-1, -1);
    }
    
    private void addRow(int m, int i, Double fac) { // adds the i-th row fac times to m th row
        for (int j = 0; j < columnNumber(); j++) {
            if (get(i, j)!=0.0) {
                set(m, j, get(m, j)+(fac*(get(i, j))));
            }
        }
    }
    
    private void addColumn(int m, int j, Double fac) {
        for (int i = 0; i < rowNumber(); i++) {
            if (get(i, j)!=0.0) {
                set(i, m, get(i, m)+(fac*(get(i, j))));
            }
        }
    }
    
    public void remove(int i, int j) {
        rows.remove(i);
        for (DoubleVector vec : rows) vec.remove(j);
    }
    
    private DoubleMatrix removeMatrix(int i, int j) {
        return new DoubleMatrix(this, i, j);
    }
    
    private boolean restZeroColumn(int i, int j) {
        int k = 0;
        boolean allZero = true;
        while (allZero && k < rowNumber()) {
            if (k != i) {
                if (get(k, j)!=0.0) allZero = false;
            }
            k++;
        }
        return allZero;
    }
    
    private boolean restZeroRow(int i, int j) {
        int k = 0;
        boolean allZero = true;
        while (allZero && k < columnNumber()) {
            if (k != j) {
                if (get(i, k)!=0) allZero = false;
            }
            k++;
        }
        return allZero;
    }
    
    private int divideEntriesColumn(int i, int j) {
        int divs = 0;
        for (int k = 0; k < rowNumber(); k++) {
            if (k != i) divs++;
        }
        return divs;
    }
    
    private int divideEntriesRow(int i, int j) {
        int divs = 0;
        for (int k = 0; k < columnNumber(); k++) {
            if (k != j) divs++;
        }
        return divs;
    }
    
    public int[] smallestEntry() {
        int kx = 0;
        int ky = 0;
        Double smallest = 0.0;
        for (int i = 0; i < rows.size(); i++) {
            int smI = rows.get(i).smallestEntry();
            if (smallest == 0.0) {
                kx = i;
                ky = smI;
                smallest = get(kx, ky);
            }
            else if (Math.abs(get(i, smI)) != 0.0 && Math.abs(smallest) > Math.abs(get(i, smI))) {
                kx = i;
                ky = smI;
                smallest = get(kx, ky);
            }
            if (smallest!=0.0) return new int[] {kx, ky};
        }
        return new int[] {kx, ky};
    }
    
    @Override
    public String toString() {
        String info = "";
        int longest = 0;
        for (int i = 0; i < rowNumber(); i++) {
            for (int j = 0; j < columnNumber(); j++) {
                String test = get(i, j).toString();
                if (longest < test.length()) longest = test.length();
            }
        }
        for (DoubleVector row : rows) info = info+row.toString(longest)+"\n";
        return info;
    }
    
}
