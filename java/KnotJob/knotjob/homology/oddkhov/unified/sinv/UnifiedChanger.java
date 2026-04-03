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
import knotjob.homology.oddkhov.OddArrow;
import knotjob.homology.oddkhov.OddGenerator;
import knotjob.homology.oddkhov.unified.UnifiedChainComplex;
import knotjob.homology.oddkhov.unified.UnifiedGenerator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 * @param <S>
 */
public class UnifiedChanger<R extends Ring<R>, S extends Ring<S>> {
    
    private final SUniComplex<R> theComplex;
    private final R unit;
    private final S modUnit;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final boolean getSInv;
    private final boolean reduced;
    private final int stage;
    private UnifiedChainComplex<R> dComplex;
    private UnifiedChainComplex<R> eComplex;
    private UnifiedChainComplex<S> bComplex;
    private UnifiedChainComplex<S> mComplex;
    private SUniComplex<S> modComplex;
    private Integer sInv;
    
    public UnifiedChanger(SUniComplex<R> complex, R unt, S munt, DialogWrap frm, AbortInfo ab, 
            Integer snv, boolean red, int stg) {
        theComplex = complex;
        unit = unt;
        modUnit = munt;
        frame = frm;
        abInf = ab;
        getSInv = snv == null;
        reduced = red;
        sInv = snv;
        stage = stg;
    }

    public void setupComplexes() {
        dComplex = new UnifiedChainComplex<R>(unit, frame, abInf, reduced);
        eComplex = new UnifiedChainComplex<R>(unit, frame, abInf, reduced);
        modComplex = new SUniComplex<S>(modUnit, modUnit, frame, abInf);
        int tot = theComplex.generatorSize();
        for (int j = 0 ; j < tot; j++) {
            ArrayList<Generator<R>> gens = theComplex.getGenerators(j);
            for (int i = 0; i < gens.size(); i++) {
                OddGenerator<R> aGen = (OddGenerator<R>) gens.get(i);
                UnifiedGenerator<R> oGen = new UnifiedGenerator<R>(aGen.hdeg(), aGen.qdeg());
                UnifiedGenerator<R> xGen = new UnifiedGenerator<R>(aGen.hdeg(), aGen.qdeg());
                UnifiedGenerator<R> eGen = new UnifiedGenerator<R>(aGen.hdeg(), aGen.qdeg());
                UnifiedGenerator<S> mGen = new UnifiedGenerator<S>(aGen.hdeg(), aGen.qdeg());
                dComplex.addGenerator(oGen, j);
                dComplex.addGenerator(xGen, j);
                eComplex.addGenerator(eGen, j);
                if (getSInv) modComplex.addGenerator(mGen, j);
                Arrow<R> oAr = new Arrow<R>(oGen, eGen, unit);
                Arrow<R> xAr = new Arrow<R>(xGen, eGen, unit);
                oGen.addOutArrow(oAr);
                xGen.addOutArrow(xAr);
                eGen.addInArrow(oAr);
                eGen.addInArrow(xAr);
                for (int k = 0; k < aGen.getTopArrowSize(); k++) {
                    OddArrow<R> arr = aGen.getTopArrow(k);
                    ArrayList<Generator<R>> lowGens = theComplex.getGenerators(j-1);
                    int p = lowGens.indexOf(arr.getBotGenerator());
                    eComplex.addArrow(eGen, j-1, p, arr.getValue().abs(1));
                    R o = arr.getValue().abs(3);
                    R x = arr.getValue().abs(4);
                    if (!o.isZero()) {
                        dComplex.addArrow(oGen, j-1, 2*p, o);
                        dComplex.addArrow(xGen, j-1, 2*p+1, o);
                    }
                    if (!x.isZero()) {
                        dComplex.addArrow(oGen, j-1, 2*p+1, x);
                        dComplex.addArrow(xGen, j-1, 2*p, x);
                    }
                    if (getSInv) {
                        R v = arr.getValue().abs(5);
                        if (!v.isZero()) {
                            modComplex.addArrow(mGen, j-1, p, modUnit);
                        }
                    }
                }
            }
        }
    }

    public Integer getSInvariant() {
        return sInv;
    }

    public void simplifyComplexes(int x) {
        if (getSInv) sInv = modComplex.barnatize(x);
        eComplex.smithNormalize(true, stage+1);
        if (abInf.isAborted()) return;
        dComplex.throwAway(sInv+x);
        dComplex.smithNormalize(true, stage+2);
        if (abInf.isAborted()) return;
        ArrayList<UnifiedGenerator<R>> cocycles = dComplex.getCocycles(0, sInv+x);
        bComplex = new UnifiedChainComplex<S>(modUnit, frame, abInf, reduced);
        mComplex = new UnifiedChainComplex<S>(modUnit, frame, abInf, reduced);
        int tot = eComplex.generatorSize();
        for (int j = 0; j < tot; j++) {
            ArrayList<Generator<R>> gens = eComplex.getGenerators(j);
            for (int i = 0; i < gens.size(); i++) {
                UnifiedGenerator<R> aGen = (UnifiedGenerator<R>) gens.get(i);
                UnifiedGenerator<S> mGen = new UnifiedGenerator<S>(aGen.hdeg(), aGen.qdeg());
                mComplex.addGenerator(mGen, j);
                for (int k = 0; k < aGen.getTopArrowSize(); k++) {
                    Arrow<R> arr = aGen.getTopArrow(k);
                    if (arr.getValue().abs(5).isInvertible()) {
                        ArrayList<Generator<R>> lowGens = eComplex.getGenerators(j-1);
                        int p = lowGens.indexOf(arr.getBotGenerator());
                        mComplex.addArrow(mGen, j-1, p, modUnit);
                    }
                }
            }
        }
        int p = dComplex.getLevel(0);
        for (UnifiedGenerator<R> coc : cocycles) {
            R order = orderOf(coc).abs(5);
            if (order.isZero()) {
                UnifiedGenerator<S> cc = new UnifiedGenerator<S>(0, sInv+x);
                bComplex.addGenerator(cc, 0);
                for (Arrow<R> arr : coc.getOutArrows()) {
                    if (arr.getValue().abs(5).isInvertible()) {
                        Generator<R> tGen = arr.getTopGenerator();
                        int i = eComplex.getGenerators(p).indexOf(tGen);
                        UnifiedGenerator<S> mGen = (UnifiedGenerator<S>) mComplex.getGenerators(p).get(i);
                        Arrow<S> ar = new Arrow<S>(cc, mGen, modUnit);
                        cc.addOutArrow(ar);
                        mGen.addInArrow(ar);
                    }
                }
            }
        }
        mComplex.smithNormalize(true, stage+2);
        mComplex.cancelBoundaries(true);
        if (bComplex.totalObjects() > 0) mComplex.setPossibleCocycles(bComplex.getGenerators(0));
        else mComplex.setPossibleCocycles(new ArrayList<Generator<S>>());
    }
    
    public int getUInvariant(int x) {
        ArrayList<Integer> theQs = mComplex.getQs();
        int qmax = theQs.get(theQs.size()-1);
        int qmin = theQs.get(0);
        int s = mComplex.getrPlus(sInv, qmax, qmin, modUnit) - 2;
        return s;
    }

    private R orderOf(UnifiedGenerator<R> coc) {
        for (Arrow<R> arr : coc.getTopArrows()) {
            UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) arr.getBotGenerator();
            if (bGen.qdeg() == coc.qdeg()) return arr.getValue();
        }
        return unit.getZero();
    }
    
}
