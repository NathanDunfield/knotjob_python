/*

Copyright (C) 2021-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
public class Matrix<R extends Ring<R>> {
    
    protected final R unit;
    private final ArrayList<Vector<R>> rows;
    
    public Matrix(int n, R unt) {
        unit = unt;
        rows = new ArrayList<Vector<R>>(n);
        for (int i = 0; i < n; i++) rows.add(new Vector<R>(n, unit));
    }
    
    public Matrix(int n, int m, R unt) {
        unit = unt;
        rows = new ArrayList<Vector<R>>(n);
        for (int i = 0; i < n; i++) rows.add(new Vector<R>(m, unit));
    }
    
    public Matrix(Matrix<R> mat, int i, int j) {
        unit = mat.unit;
        rows = new ArrayList<Vector<R>>(mat.rowNumber()-1);
        for (int k = 0; k < mat.rowNumber()-1; k++) 
            rows.add(new Vector<R>(mat.columnNumber()-1, unit));
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
    
    public R get(int i, int j) {
        return rows.get(i).get(j);
    }
    
    public void set(int i, int j, R ent) {
        rows.get(i).set(j, ent);
    }
    
    public R determinant() {
        return clonedMatrix().determinant(-1, -1);
    }
    
    public Matrix<R> multiply(Matrix<R> mat) {
        if (this.columnNumber() != mat.rowNumber()) return null;
        Matrix<R> nMat = new Matrix<R>(this.rowNumber(), mat.columnNumber(), unit);
        for (int i = 0; i < this.rowNumber(); i++) {
            for (int j = 0; j < mat.columnNumber(); j++) {
                R ent = unit.getZero();
                for (int k = 0; k < this.columnNumber(); k++) {
                    ent = ent.add(this.get(i, k).multiply(mat.get(k, j)));
                }
                nMat.set(i, j, ent);
            }
        }
        return nMat;
    }
    
    public void cloneMatrix(Matrix<R> mat) {
        for (int i = 0; i < this.rowNumber(); i++) {
            for (int j = 0; j < this.columnNumber(); j++) {
                this.set(i, j, mat.get(i, j));
            }
        }
    }
    
    public int rank() {
        if (rows.isEmpty()) return 0;
        Matrix<R> clone;
        if (rows.size() < rows.get(0).size()) clone = this.clonedMatrix();
        else clone = this.transpose();
        int rank = 0;
        for (int i = 0; i < clone.rowNumber(); i++) {
            int z = clone.nonzeroNumber(i);
            while (z > 1) {
                int col = clone.smallest(i);
                clone.shiftColumns(i, col);
                z = clone.nonzeroNumber(i);
            }
            if (z != 0) {
                int col = clone.smallest(i);
                for (int j = i+1; j < clone.rowNumber(); j++) clone.set(j, col, unit.getZero());
                rank++;
            }
            
        }
        return rank;
    }
    
    public Matrix<R> transpose() {
        Matrix<R> trans = new Matrix<R>(rows.get(0).size(), rows.size(), unit);
        for (int i = 0; i < rows.size(); i++) {
            for (int j = 0; j < rows.get(0).size(); j++) trans.set(j, i, this.get(i, j));
        }
        return trans;
    }
    
    private int nonzeroNumber(int i) {
        int z = 0;
        for (int j = 0; j < columnNumber(); j++) if (!get(i, j).isZero()) z++;
        return z;
    }
    
    private void shiftColumns(int i, int j) {
        for (int k = 0; k < columnNumber(); k++) {
            if (k != j) {
                R val = this.get(i, k).div(this.get(i, j)).negate();
                for (int l = 0; l < rowNumber(); l++) {
                    this.set(l, k, this.get(l, k).add(val.multiply(this.get(l, j))));
                }
            }
        }
    }
    
    private Matrix<R> clonedMatrix() {
        Matrix<R> clone = new Matrix<R>(rowNumber(), columnNumber(), unit);
        for (int i = 0; i < rowNumber(); i++) {
            for (int j = 0; j < columnNumber(); j++) clone.set(i, j, unit.multiply(get(i, j)));
        }
        return clone;
    }
    
    private R determinant(int i, int j) {
        if (rowNumber() == 1) return get(0, 0);
        if (i == -1) {
            int[] ind = smallestEntry();
            i = ind[0];
            j = ind[1];
        }
        R smallest = get(i, j);
        if (smallest.isZero()) return smallest; // it's a zero matrix
        if (restZeroColumn(i, j) || restZeroRow(i, j)) {
            R factor = get(i, j);
            if ((i+j)%2 != 0) factor = factor.negate();
            return factor.multiply(removeMatrix(i, j).determinant(-1, -1));
        }
        int k = divideEntriesColumn(i, j);
        int l = divideEntriesRow(i, j);
        if (k < l){ // shuffle rows
            for (int m = 0; m < rowNumber(); m++) {
                if (m != i) {
                    if (!get(m, j).isZero()) addRow(m, i, (get(m, j).div(get(i, j)).negate()));
                }
            }
        }
        else { // shuffle columns
            for (int m = 0; m < columnNumber(); m++) {
                if (m != j) {
                    if (!get(i, m).isZero()) addColumn(m, j, (get(i, m).div(get(i, j)).negate()));
                }
            }
        }
        return determinant(-1, -1);
    }
    
    public void addRow(int m, int i, R fac) { // adds the i-th row fac times to m th row
        for (int j = 0; j < columnNumber(); j++) {
            if (!get(i, j).isZero()) {
                set(m, j, get(m, j).add(fac.multiply(get(i, j))));
            }
        }
    }
    
    private void addColumn(int m, int j, R fac) {
        for (int i = 0; i < rowNumber(); i++) {
            if (!get(i, j).isZero()) {
                set(i, m, get(i, m).add(fac.multiply(get(i, j))));
            }
        }
    }
    
    public void remove(int i, int j) {
        rows.remove(i);
        for (Vector<R> vec : rows) vec.remove(j);
    }
    
    private Matrix<R> removeMatrix(int i, int j) {
        return new Matrix<R>(this, i, j);
    }
    
    private boolean restZeroColumn(int i, int j) {
        int k = 0;
        boolean allZero = true;
        while (allZero && k < rowNumber()) {
            if (k != i) {
                if (!get(k, j).isZero()) allZero = false;
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
                if (!get(i, k).isZero()) allZero = false;
            }
            k++;
        }
        return allZero;
    }
    
    private int divideEntriesColumn(int i, int j) {
        int divs = 0;
        for (int k = 0; k < rowNumber(); k++) {
            if (k != i) if (get(i, j).divides(get(k, i))) divs++;
        }
        return divs;
    }
    
    private int divideEntriesRow(int i, int j) {
        int divs = 0;
        for (int k = 0; k < columnNumber(); k++) {
            if (k != j) if (get(i, j).divides(get(i, k))) divs++;
        }
        return divs;
    }
    
    public int[] smallestEntry() {
        int kx = 0;
        int ky = 0;
        R smallest = unit.getZero();
        for (int i = 0; i < rows.size(); i++) {
            int smI = rows.get(i).smallestEntry();
            if (smallest.isZero()) {
                kx = i;
                ky = smI;
                smallest = get(kx, ky).abs(0);
            }
            else if (!get(i, smI).isZero() && smallest.isBigger(get(i, smI).abs(0))) {
                kx = i;
                ky = smI;
                smallest = get(kx, ky).abs(0);
            }
            if (smallest.isInvertible()) return new int[] {kx, ky};
        }
        return new int[] {kx, ky};
    }
    
    private int smallest(int i) {
        return rows.get(i).smallestEntry();
    }
    
    public void flipRow(int i, int j) {
        Vector<R> help = rows.get(i);
        rows.set(i, rows.get(j));
        rows.set(j, help);
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
        for (Vector<R> row : rows) info = info+row.toString(longest)+"\n";
        return info;
    }
}
