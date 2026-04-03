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
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.homology.oddkhov.OddComplex;
import knotjob.homology.oddkhov.OddGenerator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class UnifiedAction<R extends Ring<R>> {
    
    private final OddComplex<R> theComplex;
    private final R unit;
    private final R xi;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final boolean reduced;
    private UnifiedChainComplex<R> complex;
    
    public UnifiedAction(OddComplex<R> complex, R unt, R x, DialogWrap frm, AbortInfo ab) {
        theComplex = complex;
        unit = unt;
        xi = x;
        frame = frm;
        abInf = ab;
        reduced = false;
    }

    void setupComplex(boolean withXi) {
        complex = new UnifiedChainComplex<R>(unit, frame, abInf, reduced);
        int tot = theComplex.generatorSize();
        for (int j = tot-1; j >= 0; j--) {
            ArrayList<Generator<R>> gens = theComplex.getGenerators(j);
            for (int i = 0; i < gens.size(); i++) {
                OddGenerator<R> aGen = (OddGenerator<R>) gens.get(i);
                UnifiedGenerator<R> oGen = new UnifiedGenerator<R>(aGen.hdeg(), aGen.qdeg());
                UnifiedGenerator<R> xGen = new UnifiedGenerator<R>(aGen.hdeg(), aGen.qdeg());
                complex.addGenerator(oGen, j);
                complex.addGenerator(xGen, j);
                if (withXi) {
                    Arrow<R> multo = new Arrow<R>(oGen, xGen, unit);
                    Arrow<R> multx = new Arrow<R>(xGen, oGen, unit);
                    oGen.addOutArrow(multo);
                    oGen.addInArrow(multx);
                    xGen.addInArrow(multo);
                    xGen.addOutArrow(multx);
                }
                for (Arrow<R> arr : aGen.getBotArrows()) {
                    OddGenerator<R> tGen = (OddGenerator<R>) arr.getTopGenerator();
                    UnifiedGenerator<R> toGen = (UnifiedGenerator<R>) tGen.getBotArrows().get(0).getTopGenerator();
                    UnifiedGenerator<R> txGen = (UnifiedGenerator<R>) tGen.getBotArrows().get(1).getTopGenerator();
                    R o = arr.getValue().abs(3);
                    R x = arr.getValue().abs(4);
                    if (!o.isZero()) {
                        Arrow<R> oo = new Arrow<R>(oGen, toGen, o);
                        Arrow<R> xx = new Arrow<R>(xGen, txGen, o);
                        oGen.addBotArrow(oo);
                        toGen.addTopArrow(oo);
                        xGen.addBotArrow(xx);
                        txGen.addTopArrow(xx);
                    }
                    if (!x.isZero()) {
                        Arrow<R> ox = new Arrow<R>(oGen, txGen, x);
                        Arrow<R> xo = new Arrow<R>(xGen, toGen, x);
                        oGen.addBotArrow(ox);
                        txGen.addTopArrow(ox);
                        xGen.addBotArrow(xo);
                        toGen.addTopArrow(xo);
                    }
                }
                aGen.getBotArrows().clear();
                aGen.getBotArrows().add(new Arrow<R>(aGen, oGen, unit));
                aGen.getBotArrows().add(new Arrow<R>(aGen, xGen, unit));
            }
        }
        System.out.println("Sheck "+complex.boundaryCheck());
    }
    
    public void smithNormalize(boolean throwAway) {
        complex.smithNormalize(true, 1);
        if (throwAway) complex.removeBoundaries();
    }
    
    public void outputComplex() {
        complex.output(complex);
    }
    
}
