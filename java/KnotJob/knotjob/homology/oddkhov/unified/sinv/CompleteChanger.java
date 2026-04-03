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

package knotjob.homology.oddkhov.unified.sinv;

import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.homology.evenkhov.sinv.GradedComplex;
import knotjob.homology.oddkhov.unified.UnifiedChainComplex;
import knotjob.homology.oddkhov.unified.UnifiedGenerator;
import knotjob.rings.Matrix;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 * @param <S>
 */
public class CompleteChanger<R extends Ring<R>, S extends Ring<S>>  {

    private final R unit;
    private final S modUnit;
    private final boolean reduce;
    private final boolean highDetail;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final GradedComplex<R> grComplex;
    private final UnifiedChainComplex<R> bnComplex;
    private final UnifiedChainComplex<R> odComplex;
    private final UnifiedChainComplex<R> btComplex;
    private UnifiedChainComplex<R> fiComplex;
    private UnifiedChainComplex<R> fcComplex;
    private UnifiedChainComplex<S> mdComplex;
    private UnifiedChainComplex<S> bmComplex;
    private UnifiedChainComplex<S> omComplex;
    private final int stage;
    
    CompleteChanger(int stg, GradedComplex<R> grx, UnifiedChainComplex<R> bnx, 
            UnifiedChainComplex<R> odx, UnifiedChainComplex<R> btx, R unt, S munt,
            boolean red, boolean hd, DialogWrap fram, AbortInfo abf) {
        unit = unt;
        modUnit = munt;
        reduce = red;
        highDetail = hd;
        frame = fram;
        abInf = abf;
        grComplex = grx;
        bnComplex = bnx;
        odComplex = odx;
        btComplex = btx;
        stage = stg;
    }
    
    public String gradedInvariant() {
        grComplex.smithNormalizeZero();
        return grComplex.getGrading();
    }

    private void normalizeFiltered(Integer sinv) {
        setFilteredComplex(sinv);
        fiComplex.smithNormalize(false, stage+1);
        if (abInf.isAborted()) return;
        fcComplex.smithNormalize(false, stage+2);
        if (abInf.isAborted()) return;
        bnComplex.smithNormalize(false, stage+3);
        if (abInf.isAborted()) return;
        bnComplex.cancelBoundaries(false);
        fiComplex.cancelBoundaries(false);
        fcComplex.cancelBoundaries(false);
        fiComplex.prepareComplex(fcComplex, bnComplex);
        fiComplex.smithNormalize(false, stage+3);
    }

    private void setFilteredComplex(Integer sinv) {
        ArrayList<ArrayList<Generator<R>>> fiGens = new ArrayList<ArrayList<Generator<R>>>();
        ArrayList<ArrayList<Generator<R>>> fcGens = new ArrayList<ArrayList<Generator<R>>>();
        for (int k = 0; k < bnComplex.generatorSize(); k++) {
            fiGens.add(new ArrayList<Generator<R>>());
            fcGens.add(new ArrayList<Generator<R>>());
            ArrayList<Generator<R>> gens = bnComplex.getGenerators(k);
            for (int i = 0; i < gens.size(); i++) {
                UnifiedGenerator<R> gen = (UnifiedGenerator<R>) gens.get(i);
                UnifiedGenerator<R> cGen = (UnifiedGenerator<R>) btComplex.getGenerators(k).get(i);
                if (gen.qdeg() >= sinv) {
                    UnifiedGenerator<R> fiGen = new UnifiedGenerator<R>(gen.hdeg(), gen.qdeg());
                    UnifiedGenerator<R> fcGen = new UnifiedGenerator<R>(gen.hdeg(), gen.qdeg());
                    fiGens.get(k).add(fiGen);
                    fcGens.get(k).add(fcGen);
                    for (int j = 0; j < gen.getTopArrowSize(); j++) {
                        Arrow<R> arrow = gen.getTopArrow(j);
                        Arrow<R> cArrow = cGen.getTopArrow(j);
                        UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) arrow.getBotGenerator();
                        UnifiedGenerator<R> bcGen = (UnifiedGenerator<R>) cArrow.getBotGenerator();
                        if (bGen.qdeg() >= sinv) {
                            UnifiedGenerator<R> bfGen = (UnifiedGenerator<R>) bGen.getInArrows().get(0).getBotGenerator();
                            UnifiedGenerator<R> bfcGen = (UnifiedGenerator<R>) bcGen.getInArrows().get(0).getBotGenerator();
                            Arrow<R> fiArr = new Arrow<R>(bfGen, fiGen, arrow.getValue());
                            bfGen.addBotArrow(fiArr);
                            fiGen.addTopArrow(fiArr);
                            Arrow<R> fcArr = new Arrow<R>(bfcGen, fcGen, cArrow.getValue());
                            bfcGen.addBotArrow(fcArr);
                            fcGen.addTopArrow(fcArr);
                        }
                    }
                    Arrow<R> trArr = new Arrow<R>(fiGen, gen, unit);
                    fiGen.addOutArrow(trArr);
                    gen.addInArrow(trArr);
                    Arrow<R> tcArr = new Arrow<R>(fcGen, cGen, unit);
                    fcGen.addOutArrow(tcArr);
                    cGen.getInArrows().add(0, tcArr);
                }
            }
        }
        fiComplex = new UnifiedChainComplex<R>(fiGens, unit, reduce, highDetail, frame, abInf);
        fcComplex = new UnifiedChainComplex<R>(fcGens, unit, reduce, highDetail, frame, abInf);
    }

    private void prepareOdd(Integer bts) {
        odComplex.throwAway(bts);
        odComplex.smithNormalize(false, stage+4);
        if (abInf.isAborted()) return;
        odComplex.cancelBoundaries(false);
    }

    private void modularize(int bts) {
        btComplex.throwAway(bts);
        int lv = btComplex.getLevel(0);
        ArrayList<ArrayList<Generator<S>>> mdGens = new ArrayList<ArrayList<Generator<S>>>();
        ArrayList<ArrayList<Generator<S>>> bmGens = new ArrayList<ArrayList<Generator<S>>>();
        ArrayList<ArrayList<Generator<S>>> omGens = new ArrayList<ArrayList<Generator<S>>>();
        for (int i = 0; i < 3; i++) {
            mdGens.add(new ArrayList<Generator<S>>());
            bmGens.add(new ArrayList<Generator<S>>());
            omGens.add(new ArrayList<Generator<S>>());
        }
        if (lv > 0) fillBtGenerators(lv, -1, bts, mdGens);
        fillBtGenerators(lv, 0, bts, mdGens);
        if (lv < btComplex.generatorSize()-1) fillBtGenerators(lv, 1, bts, mdGens);
        mdComplex = new UnifiedChainComplex<S>(mdGens, modUnit, reduce, highDetail, frame, abInf);
        fillOdGenerators(lv, bts, omGens);
        omComplex = new UnifiedChainComplex<S>(omGens, modUnit, reduce, highDetail, frame, abInf);
        fillBmGenerators(lv, bts, bmGens);
        bmComplex = new UnifiedChainComplex<S>(bmGens, modUnit, reduce, highDetail, frame, abInf);
        mdComplex.smithNormalize(false, stage+4);
        mdComplex.cancelBoundaries(false);
    }
    
    private void fillBtGenerators(int lv, int hs, int qd, ArrayList<ArrayList<Generator<S>>> gens) {
        for (int j = 0; j < btComplex.getGenerators(lv+hs).size(); j++) {
            UnifiedGenerator<R> gen = (UnifiedGenerator<R>) btComplex.getGenerators(lv+hs).get(j);
            UnifiedGenerator<S> mGen = new UnifiedGenerator<S>(hs, qd);
            gens.get(1+hs).add(mGen);
            if (hs >= 0) {
                for (Arrow<R> arr : gen.getTopArrows()) {
                    R value = arr.getValue().abs(2);
                    if (value.isInvertible()) {
                        UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) arr.getBotGenerator();
                        if (bGen.qdeg() == qd) {
                            int pos = btComplex.getGenerators(lv+hs-1).indexOf(bGen);
                            Arrow<S> nArr = new Arrow<S>(gens.get(hs).get(pos), mGen, modUnit);
                            gens.get(hs).get(pos).addBotArrow(nArr);
                            mGen.addTopArrow(nArr);
                        }
                    }
                }
            }
        }
    }
    
    private void fillOdGenerators(int lv, int qd, ArrayList<ArrayList<Generator<S>>> gens) {
        for (int j = 0; j < odComplex.getGenerators(lv).size(); j++) {
            UnifiedGenerator<R> gen = (UnifiedGenerator<R>) odComplex.getGenerators(lv).get(j);
            if (gen.getBotArrows().isEmpty()) {
                if (gen.getTopArrows().isEmpty() || !gen.getTopArrow(0).getValue().abs(2).isInvertible()) {
                    UnifiedGenerator<S> mGen = new UnifiedGenerator<S>(0, qd);
                    gens.get(1).add(mGen);
                    for (Arrow<R> arr : gen.getOutArrows()) {
                        if (arr.getValue().abs(2).isInvertible()) {
                            UnifiedGenerator<R> btGen = (UnifiedGenerator<R>) arr.getTopGenerator();
                            if (btGen.qdeg() == qd) {
                                int pos = btComplex.getGenerators(lv).indexOf(btGen);
                                UnifiedGenerator<S> tGen = (UnifiedGenerator<S>) mdComplex.getGenerators(1).get(pos);
                                Arrow<S> nArr = new Arrow<S>(mGen, tGen, modUnit);
                                mGen.addOutArrow(nArr);
                                tGen.addInArrow(nArr);
                            }
                        }
                    }
                }
            }
        }
    }

    private void fillBmGenerators(int lv, int qd, ArrayList<ArrayList<Generator<S>>> gens) {
        UnifiedGenerator<S> tGen = new UnifiedGenerator<S>(1, qd);
        gens.get(2).add(tGen);
        for (int j = 0; j < fiComplex.getGenerators(lv).size(); j++) {
            UnifiedGenerator<R> gen = (UnifiedGenerator<R>) fiComplex.getGenerators(lv).get(j);
            if ((gen.getBotArrows().isEmpty() || gen.getBotArrows().get(0).getValue().abs(2).isInvertible()) && 
                    (gen.getTopArrows().isEmpty() || !gen.getTopArrow(0).getValue().abs(2).isInvertible())) {
                UnifiedGenerator<S> mGen = new UnifiedGenerator<S>(0, qd);
                addArrows(gen, mGen, lv);
                if (!gen.getBotArrows().isEmpty()) {
                    Arrow<S> arr = new Arrow<S>(mGen, tGen, modUnit);
                    mGen.addBotArrow(arr);
                    tGen.addTopArrow(arr);
                    gens.get(1).add(0, mGen);
                }
                else gens.get(1).add(mGen);
            }
        }
    }

    private void addArrows(UnifiedGenerator<R> gen, UnifiedGenerator<S> mGen, int lv) {
        ArrayList<R> values = new ArrayList<R>();
        for (int i = 0; i < btComplex.getGenerators(lv).size(); i++) values.add(unit.getZero());
        for (Arrow<R> arr : gen.getOutArrows()) {
            UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
            for (Arrow<R> xArr : tGen.getOutArrows()) {
                UnifiedGenerator<R> btGen = (UnifiedGenerator<R>) xArr.getTopGenerator();
                int pos = btComplex.getGenerators(lv).indexOf(btGen);
                if (pos >= 0) {
                    values.set(pos, values.get(pos).add(arr.getValue().multiply(xArr.getValue())));
                }
            }
        }
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).abs(2).isInvertible()) {
                UnifiedGenerator<S> tGen = (UnifiedGenerator<S>) mdComplex.getGenerators(1).get(i);
                Arrow<S> nArr = new Arrow<S>(mGen, tGen, modUnit);
                mGen.addOutArrow(nArr);
                tGen.addInArrow(nArr);
            }
        }
    }

    public Integer completeInvariant(int bts) {
        normalizeFiltered(bts);
        if (abInf.isAborted()) return null;
        prepareOdd(bts);
        if (abInf.isAborted()) return null;
        modularize(bts);
        boolean exists = handleMatrix();
        if (!exists) return bts - 2;
        return bts;
    }

    private boolean handleMatrix() {
        int k = mdComplex.getGenerators(1).size();
        int m = bmComplex.getGenerators(1).size();
        int n = omComplex.getGenerators(1).size();
        Matrix<S> matrix = new Matrix<S>(k, m+n-1, modUnit);
        Matrix<S> line = new Matrix<S>(k, 1, modUnit);
        for (int i = 1; i < m; i++) {
            UnifiedGenerator<S> gen = (UnifiedGenerator<S>) bmComplex.getGenerators(1).get(i);
            enterColumn(matrix, i-1, gen);
        }
        for (int i = 0; i < n; i++) {
            UnifiedGenerator<S> gen = (UnifiedGenerator<S>) omComplex.getGenerators(1).get(i);
            enterColumn(matrix, m-1+i, gen);
        }
        UnifiedGenerator<S> gen = (UnifiedGenerator<S>) bmComplex.getGenerators(1).get(0);
        enterColumn(line, 0, gen);
        return solutionExists(matrix, line);
    }
    
    private void enterColumn(Matrix<S> mat, int col, UnifiedGenerator<S> gen) {
        for (int i = 0; i < gen.getOutArrows().size(); i++) {
            Arrow<S> arr = gen.getOutArrows().get(i);
            int p = mdComplex.getGenerators(1).indexOf(arr.getTopGenerator());
            mat.set(p, col, modUnit);
        }
    }

    private void gaussJordanify(Matrix<S> matrix, Matrix<S> line) {
        for (int i = 0; i < matrix.rowNumber(); i++) {
            int max = matrix.columnNumber()+1;
            int pos = i;
            for (int j = i; j < matrix.rowNumber(); j++) {
                int first = firstNonZero(matrix, j);
                if (first < max) {
                    max = first;
                    pos = j;
                }
            }
            if (pos != i) {
                matrix.flipRow(i, pos);
                line.flipRow(i, pos);
            }
            if (max > matrix.columnNumber()) break;
            for (int j = i+1; j < matrix.rowNumber(); j++) {
                if (matrix.get(j, max).isInvertible()) {
                    matrix.addRow(j, i, modUnit);
                    line.addRow(j, i, modUnit);
                }
            }
        }
    }

    private int firstNonZero(Matrix<S> matrix, int j) {
        for (int i = 0; i < matrix.columnNumber(); i++) {
            if (matrix.get(j, i).isInvertible()) return i;
        }
        return matrix.columnNumber()+2;
    }
    
    private boolean solutionExists(Matrix<S> matrix, Matrix<S> line) {
        gaussJordanify(matrix, line);
        int k = matrix.rowNumber()-1;
        while (k >= 0) {
            if (firstNonZero(matrix, k) > matrix.columnNumber() && line.get(k, 0).isInvertible()) 
                return false;
            k--;
        }
        return true;
    }
    
}
