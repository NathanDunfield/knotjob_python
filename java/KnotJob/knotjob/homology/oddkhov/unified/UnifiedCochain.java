/*

Copyright (C) 2023 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.oddkhov.unified;

import java.util.ArrayList;
import knotjob.homology.Arrow;
import knotjob.rings.Matrix;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class UnifiedCochain<R extends Ring<R>> {

    private final ArrayList<UnifiedGenerator<R>> generators;
    private final ArrayList<R> values;
    private final R unit;

    public UnifiedCochain(UnifiedGenerator<R> cocycle, R unt) {
        generators = new ArrayList<UnifiedGenerator<R>>();
        values = new ArrayList<R>();
        unit = unt;
        fillCochains(cocycle);
    }
    
    public UnifiedCochain(R unt) {
        generators = new ArrayList<UnifiedGenerator<R>>();
        values = new ArrayList<R>();
        unit = unt;
    }
    
    public UnifiedCochain<R> boundary() {
        UnifiedCochain<R> bound = new UnifiedCochain<R>(unit);
        for (int i = 0; i < generators.size(); i++) {
            UnifiedGenerator<R> gen = generators.get(i);
            for (Arrow<R> arr : gen.getBotArrows()) {
                UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
                if (tGen.qdeg() == gen.qdeg()) {
                    if (bound.generators.contains(tGen)) {
                        int p = bound.generators.indexOf(tGen);
                        bound.values.set(p, bound.values.get(p).add(values.get(i).multiply(arr.getValue())));
                    }
                    else {
                        bound.generators.add(tGen);
                        bound.values.add(values.get(i).multiply(arr.getValue()));
                    }
                }
            }
        }
        int k = bound.values.size()-1;
        while (k >= 0) {
            if (bound.values.get(k).isZero()) {
                bound.values.remove(k);
                bound.generators.remove(k);
            }
            k--;
        }
        return bound;
    }
    
    public boolean checkDivision(R diver) {
        boolean okay = true;
        int i = 0;
        while (okay && i < values.size()) {
            if (!values.get(i).divides(diver)) okay = false;
            else i++;
        }
        return okay;
    }
    
    public void addArrowsFrom(UnifiedGenerator<R> gen) {
        for (int i = 0; i < generators.size(); i++) {
            UnifiedGenerator<R> dGen = generators.get(i);
            Arrow<R> arr = dGen.getInArrows().get(0);
            UnifiedGenerator<R> cGen = (UnifiedGenerator<R>) arr.getBotGenerator();
            R diver = arr.getValue();
            Arrow<R> nArr = new Arrow<R>(gen, cGen, values.get(i).div(diver));
            gen.addOutArrow(nArr);
            cGen.addInArrow(nArr);
        }
    }
    
    public void output(UnifiedChainComplex<R> complex) {
        System.out.println("Cochain");
        for (int i = 0; i < generators.size(); i++) {
            UnifiedGenerator<R> gen = generators.get(i);
            int g = complex.getLevel(gen);
            System.out.println(values.get(i)+" times Generator "+complex.getGenerators(g).indexOf(gen));
            System.out.println("hdeg = "+gen.hdeg());
            System.out.println("qdeg = "+gen.qdeg());
            System.out.println();
        }
        System.out.println();
    }
    
    private void fillCochains(UnifiedGenerator<R> cocycle) {
        ArrayList<UnifiedGenerator<R>> indexUp = new ArrayList<UnifiedGenerator<R>>();
        ArrayList<UnifiedGenerator<R>> indexDo = new ArrayList<UnifiedGenerator<R>>();
        UnifiedChainComplex<R> complex = getTheComplex(cocycle, indexUp, indexDo);
        Matrix<R> matrix = identityMatrix(indexUp.size());
        //System.out.println(matrix);
        Matrix<R> mat = complex.boundaryMatrix(0);
        //System.out.println(mat);
        
        /*matrix = identityMatrix(3);
        mat = new Matrix<R>(2, 3, unit);
        mat.set(0, 0, unit.add(unit.add(unit.add(unit.add(unit)))).add(unit.add(unit)));
        mat.set(0, 1, unit.add(unit.add(unit)));
        mat.set(1, 0, unit.add(unit));
        mat.set(1, 1, unit.add(unit).add(unit));
        mat.set(1, 2, unit);
        System.out.println(matrix);
        System.out.println(mat);// */
        
        
        int col = getGoodColumn(mat, matrix);
        
        
        /*System.out.println("After "+col);
        System.out.println(matrix);
        System.out.println(mat);// */
        
        
        isolateRow(col, mat, matrix);
        
        
        /*System.out.println("After isolation");
        System.out.println(matrix);
        System.out.println(mat);// */
        
        toZeroRest(col, mat, matrix);
        
        /*System.out.println("After zeroing "+col);
        System.out.println(matrix);
        System.out.println(mat);// */
        
        for (int i = 0; i < matrix.rowNumber(); i++) {
            if (!matrix.get(i, col).isZero()) {
                generators.add(indexUp.get(i));
                values.add(matrix.get(i, col));
            }
        }
        if (!checkCochain(indexDo)) {
            System.out.println("Shout "+col);
            System.out.println(matrix);
            System.out.println(mat);
            complex.output();
        }
    }
    
    private boolean checkCochain(ArrayList<UnifiedGenerator<R>> index) {
        Matrix<R> vals = new Matrix<R>(1, index.size(), unit);
        for (int i = 0; i < generators.size(); i++) {
            UnifiedGenerator<R> gen = generators.get(i);
            for (Arrow<R> arr : gen.getOutArrows()) {
                UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
                R val = values.get(i).multiply(arr.getValue());
                int k = index.indexOf(tGen);
                vals.set(0, k, vals.get(0, k).add(val));
            }
        }
        boolean okay = vals.get(0, 0).add(unit.negate()).isZero();
        int i = 1;
        while (okay && i < vals.columnNumber()) {
            okay = vals.get(0, i).isZero();
            i++;
        }
        if (!okay) System.out.println("Check : "+vals+" "+index.get(0).hdeg()+" "+index.get(0).qdeg());
        return okay;
    }
    
    private void toZeroRest(int col, Matrix<R> mat, Matrix<R> matrix) {
        ArrayList<Integer> avoid = new ArrayList<Integer>();
        avoid.add(col);
        Matrix<R> id = identityMatrix(matrix.rowNumber());
        Matrix<R> clmat = mat.multiply(id);
        Matrix<R> clmatrix = matrix.multiply(id);
        for (int i = 1; i < mat.rowNumber(); i++) {
            boolean keepgoing = zeroCount(clmat, i) > 1;
            while (keepgoing) {
                int column = smallestCol(clmat, avoid, i);
                //avoid.add(column);System.out.println("XXX "+avoid);
                for (int k = 0; k < clmat.columnNumber(); k++) {
                    //System.out.println(i+" "+k+" "+column+" "+avoid);
                    //System.out.println(clmat);
                    if (k != column) {
                        R val = clmat.get(i, k).div(clmat.get(i, column)).negate();
                        Matrix<R> change = addColumnMatrix(matrix.rowNumber(), column, k, val);
                        clmat = clmat.multiply(change);
                        clmatrix = clmatrix.multiply(change);
                    }
                }
                keepgoing = zeroCount(clmat, i) > 1;
            }
            avoid.add(nonzeroOfRow(clmat, i));
        }
        mat.cloneMatrix(clmat);
        matrix.cloneMatrix(clmatrix);
    }
    
    private int nonzeroOfRow(Matrix<R> mat, int row) {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (!mat.get(row, i).isZero()) found = true;
            else i++;
        }
        return i;
    }
    
    private int smallestCol(Matrix<R> mat, ArrayList<Integer> avoid, int row) {
        int column = -1;
        R val = null;
        for (int j = 0; j < mat.columnNumber(); j++) {
            if (!avoid.contains(j) && !mat.get(row, j).isZero()) {
                if (val == null) {
                    val = mat.get(row, j);
                    column = j;
                }
                else {
                    if (val.isBigger(mat.get(row, j))) {
                        val = mat.get(row, j);
                        column = j;
                    }
                }
            }
        }
        return column;
    }
    
    private int zeroCount(Matrix<R> mat, int row) { // it's really the non-zero count
        int count = 0;
        for (int j = 0; j < mat.columnNumber(); j++) 
            if (!mat.get(row, j).isZero()) count++;
        return count;
    }
    
    private void isolateRow(int col, Matrix<R> mat, Matrix<R> matrix) {
        Matrix<R> id = identityMatrix(matrix.rowNumber());
        if (mat.get(0, col).add(unit).isZero()) id.set(col, col, unit.negate());
        Matrix<R> clmat = mat.multiply(id);
        Matrix<R> clmatrix = matrix.multiply(id);
        for (int i = 0; i < mat.columnNumber(); i++) {
            if (i != col && !clmat.get(0, i).isZero()) {
                Matrix<R> change = addColumnMatrix(matrix.rowNumber(), col, i, clmat.get(0, i).negate());
                clmat = clmat.multiply(change);
                clmatrix = clmatrix.multiply(change);
            }
        }
        mat.cloneMatrix(clmat);
        matrix.cloneMatrix(clmatrix);
    }
    
    private int getGoodColumn(Matrix<R> mat, Matrix<R> matrix) {
        boolean found = false;
        int goodCol = -1;
        while (!found) {
            int j = 0;
            while (!found && j < mat.columnNumber()) {
                R ent = mat.get(0, j);
                if (ent.isInvertible()) {
                    found = true;
                    goodCol = j;
                }
                else j++;
            }
            if (!found) modifyMatrices(mat, matrix);
        }
        return goodCol;
    }
    
    private void modifyMatrices(Matrix<R> mat, Matrix<R> matrix) {
        //System.out.println(mat);
        Matrix<R> id = identityMatrix(matrix.rowNumber());
        Matrix<R> clmat = mat.multiply(id);
        Matrix<R> clmatrix = matrix.multiply(id);
        
        int goodColumn = smallestCol(clmat, new ArrayList<Integer>(), 0);
        //System.out.println("Good "+goodColumn);
        for (int j = 0; j < clmat.columnNumber(); j++) {
            if (j!= goodColumn) {
                if (!clmat.get(0, j).isZero()) {
                    R k = clmat.get(0, j).div(clmat.get(0, goodColumn));
                    //System.out.println(clmat.get(0, j)+" "+k);
                    Matrix<R> change = addColumnMatrix(matrix.rowNumber(), goodColumn, j, k.negate());
                    clmat = clmat.multiply(change);
                    clmatrix = clmatrix.multiply(change);
                    //System.out.println(clmat);
                }
            }
        }
        //System.out.println("Get out "+clmat.get(0, goodColumn));
        
        /*while (!found) {
            R ent = mat.get(0, i);System.out.println("Ent "+ent);
            int j = 0;
            while (!found && j < mat.columnNumber()) {
                if (!ent.isZero() && !ent.divides(mat.get(i, j))) {
                    found = true;
                    goodColumn = j;
                }
                else j++;
            }
            if (!found) i++;
        }
        System.out.println(i+" "+goodColumn);
        R entOne = mat.get(0, i);
        R entTwo = mat.get(0, goodColumn);
        Matrix<R> change;
        if (entTwo.isBigger(entOne)) {
            R k = entTwo.div(entOne).negate();
            change = addColumnMatrix(matrix.rowNumber(), 0, goodColumn, k);
        }
        else {
            R k = entOne.div(entTwo).negate();
            change = addColumnMatrix(matrix.rowNumber(), goodColumn, 0, k);
        }
        Matrix<R> clmat = mat.multiply(change);
        Matrix<R> clmatrix = matrix.multiply(change);// */
        mat.cloneMatrix(clmat);
        matrix.cloneMatrix(clmatrix);
    }
           
    private Matrix<R> addColumnMatrix(int n, int i, int j, R k) { // a matrix that adds j-th column to i-th column
        Matrix<R> matrix = identityMatrix(n);
        matrix.set(i, j, k);
        return matrix;
    }
    
    private Matrix<R> identityMatrix(int n) {
        Matrix<R> matrix = new Matrix<R>(n, unit);
        for (int i = 0; i < n; i++) matrix.set(i, i, unit);
        return matrix;
    }
    
    private UnifiedChainComplex<R> getTheComplex(UnifiedGenerator<R> cocycle, 
            ArrayList<UnifiedGenerator<R>> indexUp, ArrayList<UnifiedGenerator<R>> indexDo) {            
        UnifiedChainComplex<R> complex = new UnifiedChainComplex<R>(unit, null, null, false);
        indexDo.add(cocycle);
        UnifiedGenerator<R> first = new UnifiedGenerator<R>(cocycle.hdeg(), cocycle.qdeg());
        complex.addGenerator(first, 0);
        for (int i = 0; i < cocycle.getInArrows().size(); i++) {
            UnifiedGenerator<R> next = (UnifiedGenerator<R>) cocycle.getInArrows().get(i).getBotGenerator();
            indexUp.add(next);
            complex.addGenerator(new UnifiedGenerator<R>(next.hdeg(), next.qdeg()), 1);
        }
        boolean keepgoing = true;
        while (keepgoing) {
            keepgoing = false;
            for (int i = 0; i < indexUp.size(); i++) {
                UnifiedGenerator<R> top = indexUp.get(i);
                for (int j = 0; j < top.getOutArrows().size(); j++) {
                    Arrow<R> arr = top.getOutArrows().get(j);
                    UnifiedGenerator<R> bot = (UnifiedGenerator<R>) arr.getTopGenerator();
                    if (!indexDo.contains(bot)) {
                        indexDo.add(bot);
                        UnifiedGenerator<R> an = new UnifiedGenerator<R>(bot.hdeg(), bot.qdeg());
                        complex.addGenerator(an, 0);
                        keepgoing = true;
                    }
                }
            }
            for (int i = 0; i < indexDo.size(); i++) {
                UnifiedGenerator<R> bot = indexDo.get(i);
                for (int j = 0; j < bot.getInArrows().size(); j++) {
                    Arrow<R> arr = bot.getInArrows().get(j);
                    UnifiedGenerator<R> top = (UnifiedGenerator<R>) arr.getBotGenerator();
                    if (!indexUp.contains(top)) {
                        indexUp.add(top);
                        UnifiedGenerator<R> an = new UnifiedGenerator<R>(top.hdeg(), top.qdeg());
                        complex.addGenerator(an, 1);
                        keepgoing = true;
                    }
                }
            }
        }
        for (int i = 0; i < indexUp.size(); i++) {
            UnifiedGenerator<R> top = indexUp.get(i);
            for (int j = 0; j < top.getOutArrows().size(); j++) {
                Arrow<R> arr = top.getOutArrows().get(j);
                UnifiedGenerator<R> bot = (UnifiedGenerator<R>) arr.getTopGenerator();
                complex.addArrow(1, i, indexDo.indexOf(bot), arr.getValue());
            }
        }
        return complex;
    }
}
