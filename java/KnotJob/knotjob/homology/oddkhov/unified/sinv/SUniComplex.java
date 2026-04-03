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
import java.util.Iterator;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.homology.oddkhov.OddArrow;
import knotjob.homology.oddkhov.OddGenerator;
import knotjob.homology.oddkhov.unified.UnifiedComplex;
import knotjob.homology.oddkhov.unified.UnifiedGenerator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class SUniComplex<R extends Ring<R>> extends UnifiedComplex<R>  {
    
    public SUniComplex(int comp, R unt, AbortInfo ab, DialogWrap frm) {
        super(comp, unt, ab, frm, true);
    }
    
    public SUniComplex(int crs, int[] ends, int hstart, int qstart, boolean rev, 
            R unt, R x, DialogWrap frm, AbortInfo abt) {
        super(crs, ends, hstart, qstart, rev, unt, x, frm, abt, true);
    }
    
    public SUniComplex(R unt, R x, DialogWrap frm, AbortInfo abt) {
        super(unt, x, true, abt, frm);
    }
    
    public SUniComplex(SUniComplex<R> complex, R unt, R x, boolean negate) { // only meant to work for R = ModNXi
        super(unt, x, true, complex.abInf, complex.frame);
        int factor = 1;
        if (negate) factor = -1;
        for (ArrayList<Generator<R>> objs : complex.generators) {
            int i = complex.generators.indexOf(objs);
            ArrayList<Generator<R>> objsc = new ArrayList<Generator<R>>();
            for (Iterator<Generator<R>> it = objs.iterator(); it.hasNext();) {
                OddGenerator<R> obj = (OddGenerator<R>) it.next(); 
                OddGenerator<R> cObj = new OddGenerator<R>(0, factor * obj.hdeg(),
                        factor * obj.qdeg());
                for (Iterator<Arrow<R>> itt = obj.getTopArrows().iterator(); itt.hasNext();) {
                    OddArrow<R> mor = (OddArrow<R>) itt.next();
                    R fac = mor.getValue().abs(1);
                    if (!fac.isZero()) {
                        int pos = i-1;
                        if (negate) pos = 0;
                        OddGenerator<R> bObj = (OddGenerator<R>) generators.get(pos).get(
                                complex.generators.get(i-1).indexOf(mor.getBotGenerator()));
                        OddArrow<R> cmor;
                        if (negate) cmor = new OddArrow<R>(cObj, bObj);
                        else cmor = new OddArrow<R>(bObj, cObj);
                        cmor.setValue(fac);
                        if (negate) {
                            cObj.addBotArrow(cmor);
                            bObj.addTopArrow(cmor);
                        }
                        else {
                            bObj.addBotArrow(cmor);
                            cObj.addTopArrow(cmor);
                        }
                    }
                }
                objsc.add(cObj);
            }
            if (negate) generators.add(0, objsc);
            else generators.add(objsc);
        }
    }
    
    public void addGenerator(UnifiedGenerator<R> gen, int i) {
        while (i >= generators.size()) generators.add(new ArrayList<Generator<R>>());
        generators.get(i).add(gen);
    }
    
    public void addArrow(UnifiedGenerator<R> tGen, int i, int j, R val) {
        if (val.isZero()) return;
        UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) generators.get(i).get(j);
        Arrow<R> arr = new Arrow<R>(bGen, tGen, val);
        bGen.addBotArrow(arr);
        tGen.addTopArrow(arr);
    }
    
    @Override
    public void output() {
        this.output(0,generators.size());
    }
    
    @Override
    public void output(int fh, int lh) {
        System.out.println("Positive Endpts "+posEndpts);
        System.out.println("Negative Endpts "+negEndpts);
        if (cache != null) cache.output();
        for (int i = fh; i < lh; i++) {
            System.out.println();
            System.out.println("Level "+i);
            for (int j = 0; j < ((ArrayList<Generator<R>>) generators.get(i)).size(); j++) {
                System.out.println();
                System.out.println("Generator "+j);
                ArrayList<Generator<R>> nextLev = null;
                if (i < generators.size()-1) nextLev = generators.get(i+1);
                ((OddGenerator<R>) ((ArrayList<Generator<R>>) generators.get(i)).get(j)).output(nextLev);
            }
        }
        System.out.println();
        System.out.println();
    }
    
    /*public int barnatize() {
        int t = relevantLine();
        int qmax = ((QGenerator<R>) generators.get(t).get(0)).qdeg();
        int qmin = qmax;
        for (Iterator<Generator<R>> it = generators.get(t).iterator(); it.hasNext();) {
            QGenerator<R> obj = (QGenerator<R>) it.next();
            if (qmax < obj.qdeg()) qmax = obj.qdeg();
            if (qmin > obj.qdeg()) qmin = obj.qdeg();
        }
        cancelFromTop(generators.get(t),qmax,qmin);
        cancelFromBot(generators.get(t),qmax,qmin);
        if (generators.get(t).size()!= 2) System.out.println("Wrong "+generators.get(t).get(0).hdeg());
        QGenerator<R> genOne = (QGenerator<R>) generators.get(t).get(0);
        QGenerator<R> genTwo = (QGenerator<R>) generators.get(t).get(1);
        int sinvariant = (genOne.qdeg() + genTwo.qdeg())/2;
        return sinvariant;
    }
    
    private void cancelFromTop(ArrayList<Generator<R>> objects0, int qmax, int qmin) {
        int qrun = qmax;
        while (qrun >= qmin) {
            boolean found = false;
            int t = 0;
            while (!found & t < objects0.size()) {
                QGenerator<R> bObj = (QGenerator<R>) objects0.get(t);
                if (bObj.qdeg() == qrun & bObj.hdeg() == 0) {
                    if (!bObj.getBotArrows().isEmpty()) found = true;
                }
                t++;
            }
            if (found) cancelMorObj(objects0, objects0.get(t-1).getBotArrows().get(0));
            else qrun = qrun - 2;
        }
    }
    
    private void cancelFromBot(ArrayList<Generator<R>> objects0, int qmax, int qmin) {
        int qrun = qmin;
        while (qrun <= qmax) {
            boolean found = false;
            int t = 0;
            while (!found & t < objects0.size()) {
                QGenerator<R> tObj = (QGenerator<R>) objects0.get(t);
                if (tObj.qdeg() == qrun & tObj.hdeg() == 0) {
                    if (!tObj.getTopArrows().isEmpty()) found = true;
                }
                t++;
            }
            if (found) cancelMorObj(objects0,objects0.get(t-1).getTopArrows().get(0));
            else qrun = qrun + 2;
        }
    }// */
    
    @Override
    protected void cancelMorObj(ArrayList<Generator<R>> objs, Arrow<R> mor) {
        Generator<R> bObj = mor.getBotGenerator();
        Generator<R> tObj = mor.getTopGenerator();
        bObj.getBotArrows().remove(mor);
        tObj.getTopArrows().remove(mor);
        for (Arrow<R> mr : tObj.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
        for (Arrow<R> mr : bObj.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        objs.remove(bObj);
        objs.remove(tObj);
        R u = mor.getValue();
        for (int ii = 0; ii < tObj.getTopArrows().size(); ii++) {
            Arrow<R> fmr = tObj.getTopArrows().get(ii);
            for (int jj = 0; jj < bObj.getBotArrows().size(); jj++) {
                Arrow<R> smr = bObj.getBotArrows().get(jj);
                boolean found = false;
                int y = 0;
                while (!found & y < fmr.getBotGenerator().getBotArrows().size()) {
                    Arrow<R> omr = fmr.getBotGenerator().getBotArrows().get(y);
                    if (omr.getTopGenerator() == smr.getTopGenerator()) found = true;
                    else y++;
                }
                Arrow<R> tmr;
                if (found) {
                    tmr = fmr.getBotGenerator().getBotArrows().get(y);
                }
                else {
                    tmr = new Arrow<R>(fmr.getBotGenerator(),
                            smr.getTopGenerator(), u.getZero());
                    //tmr.setValue(u.getZero());
                    fmr.getBotGenerator().addBotArrow(tmr);
                    smr.getTopGenerator().addTopArrow(tmr);
                }
                tmr.addValue(u.invert().negate().multiply(fmr.getValue()).multiply(smr.getValue()));
                if (tmr.getValue().isZero()) {
                    fmr.getBotGenerator().getBotArrows().remove(tmr);
                    smr.getTopGenerator().getTopArrows().remove(tmr);
                }
            }
        }
        for (Arrow<R> mr : tObj.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        for (Arrow<R> mr : bObj.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
    }
    
}
