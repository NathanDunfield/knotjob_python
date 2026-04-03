/*

Copyright (C) 2020-24 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.evenkhov.sinv;

import java.util.ArrayList;
import java.util.Iterator;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.homology.Homology;
import knotjob.homology.QuantumCohomology;
import knotjob.homology.evenkhov.EvenArrow;
import knotjob.homology.evenkhov.EvenComplex;
import knotjob.homology.evenkhov.EvenGenerator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class SInvComplex<R extends Ring<R>> extends EvenComplex<R> {
    
    public SInvComplex(int comp, R unt, boolean unr, boolean red, AbortInfo ab, 
            DialogWrap frm) {
        super(comp, unt, unr, red, ab, frm);
    }
    
    public SInvComplex(int crs, int[] ends, int hstart, int qstart, boolean rev, 
            boolean ras, boolean unred, boolean red, R unt, DialogWrap frm, AbortInfo abt) {
        super(crs, ends, hstart, qstart, rev, ras, unred, red, unt, frm, abt);
    }
    
    public SInvComplex(SInvComplex<R> complex, R unt, boolean negate) { // only meant to work for R = ModN
        super(unt, true, complex.abInf, complex.frame);
        int factor = 1;
        if (negate) factor = -1;
        for (ArrayList<Generator<R>> objs : complex.generators) {
            int i = complex.generators.indexOf(objs);
            ArrayList<Generator<R>> objsc = new ArrayList<Generator<R>>();
            for (Iterator<Generator<R>> it = objs.iterator(); it.hasNext();) {
                EvenGenerator<R> obj = (EvenGenerator<R>) it.next(); 
                EvenGenerator<R> cObj = new EvenGenerator<R>(0, factor * obj.hdeg(),
                        factor * obj.qdeg());
                for (Iterator<Arrow<R>> itt = obj.getTopArrows().iterator(); itt.hasNext();) {
                    EvenArrow<R> mor = (EvenArrow<R>) itt.next();
                    R fac = unt.multiply(mor.getValue());
                    if (!fac.isZero()) {
                        int pos = i-1;
                        if (negate) pos = 0;
                        EvenGenerator<R> bObj = (EvenGenerator<R>) generators.get(pos).get(
                                complex.generators.get(i-1).indexOf(mor.getBotGenerator()));
                        EvenArrow<R> cmor;
                        if (negate) cmor = new EvenArrow<R>(cObj,bObj);
                        else cmor = new EvenArrow<R>(bObj,cObj);
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
            if (negate) generators.add(0,objsc);
            else generators.add(objsc);
        }
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
        if (generators.get(t).size()!= 2) {
            System.out.println("Wrong ");
            /*ArrayList<Integer> theQs = new ArrayList<Integer>();
            for (Iterator<Generator<R>> it = generators.get(t).iterator(); it.hasNext();) {
                QGenerator<R> obj = (QGenerator<R>) it.next();
                theQs.add(obj.qdeg());
            }
            Collections.sort(theQs);
            //System.out.println(theQs+" ");
            if (theQs.get(theQs.size()-1) - theQs.get(0) > 2) System.out.println(theQs+" ");
            // could be useful for links */
        /*}
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
            if (found) cancelMorObj(objects0,objects0.get(t-1).getBotArrows().get(0));
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
            EvenArrow<R> fmr = (EvenArrow<R>) tObj.getTopArrows().get(ii);
            for (int jj = 0; jj < bObj.getBotArrows().size(); jj++) {
                EvenArrow<R> smr = (EvenArrow<R>) bObj.getBotArrows().get(jj);
                boolean found = false;
                int y = 0;
                while (!found & y < fmr.getBotGenerator().getBotArrows().size()) {
                    Arrow<R> omr = fmr.getBotGenerator().getBotArrows().get(y);
                    if (omr.getTopGenerator() == smr.getTopGenerator()) found = true;
                    else y++;
                }
                EvenArrow<R> tmr;
                if (found) {
                    tmr = (EvenArrow<R>) fmr.getBotGenerator().getBotArrows().get(y);
                }
                else {
                    tmr = new EvenArrow<R>((EvenGenerator<R>) fmr.getBotGenerator(),
                            (EvenGenerator<R>) smr.getTopGenerator());
                    tmr.setValue(u.getZero());
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
    
    // material for the Lipshitz-Sarkar invariant
    
    public void slideTwoTorsion() {
        for (ArrayList<Generator<R>> objs : generators) {
            for (Generator<R> obj : objs) {
                int i = 0;
                while (i < obj.getBotArrows().size()) {
                    EvenArrow<R> mor = (EvenArrow<R>) obj.getBotArrows().get(i);
                    if (mor.getBotGenerator().qdeg() == mor.getTopGenerator().qdeg()) {
                        slideTop(mor);
                        slideBot(mor);
                    }
                    i++;
                }
            }
        }
    }
    
    private void slideTop(EvenArrow<R> mor) {
        EvenGenerator<R> bObj = mor.getBotGenerator();
        EvenGenerator<R> tObj = mor.getTopGenerator();
        int i = 0;
        while (i < bObj.getBotArrows().size()) {
            EvenArrow<R> mr = bObj.getBotArrow(i);
            if (mr != mor) {
                if (mr.getBotGenerator().qdeg() == mr.getTopGenerator().qdeg()) {
                    i--;
                    moveTopArrows(mr.getTopGenerator(),tObj);
                    moveBotArrows(tObj,mr.getTopGenerator());
                }
            }
            i++;
        }
    }
    
    private void slideBot(EvenArrow<R> mor) {
        EvenGenerator<R> bObj = mor.getBotGenerator();
        EvenGenerator<R> tObj = mor.getTopGenerator();
        int i = 0;
        while (i < tObj.getTopArrows().size()) {
            EvenArrow<R> mr = tObj.getTopArrow(i);
            if (mr != mor) {
                if (mr.getBotGenerator().qdeg() == mr.getTopGenerator().qdeg()) {
                    i--;
                    moveTopArrows(bObj,mr.getBotGenerator());
                    moveBotArrows(mr.getBotGenerator(),bObj);
                }
            }
            i++;
        }
    }
    
    private void moveTopArrows(EvenGenerator<R> doObj, EvenGenerator<R> taObj) {
        for (Iterator<Arrow<R>> it = doObj.getBotArrows().iterator(); it.hasNext();) {
            EvenArrow<R> mor = (EvenArrow<R>) it.next();
            boolean found = false;
            int i = 0;
            while (!found & i < taObj.getBotArrows().size()) {
                if (taObj.getBotArrow(i).getTopGenerator() == mor.getTopGenerator()) found = true;
                else i++;
            }
            if (found) {
                EvenArrow<R> mr = taObj.getBotArrow(i);
                mr.addValue(mor.getValue());//always adding when moving top
                if (mr.getValue().isZero()) {
                    mr.getBotGenerator().getBotArrows().remove(mr);
                    mr.getTopGenerator().getTopArrows().remove(mr);
                }
            }
            else {
                EvenArrow<R> mr = new EvenArrow<R>(taObj, mor.getTopGenerator());
                mr.setValue(mor.getValue());
                taObj.addBotArrow(mr);//.botMor.add(mr);
                mor.getTopGenerator().addTopArrow(mr);
            }
        }
    }
    
    private void moveBotArrows(EvenGenerator<R> doObj, EvenGenerator<R> taObj) {
        for (Iterator<Arrow<R>> it = doObj.getTopArrows().iterator(); it.hasNext();) {
            EvenArrow<R> mor = (EvenArrow<R>) it.next();
            boolean found = false;
            int i = 0;
            while (!found & i < taObj.getTopArrows().size()) {
                if (taObj.getTopArrow(i).getBotGenerator() == mor.getBotGenerator()) found = true;
                else i++;
            }
            if (found) {
                EvenArrow<R> mr = taObj.getTopArrow(i);
                mr.addValue(mor.getValue().negate());// always subtracting when moving bot
                if (mr.getValue().isZero()) {
                    mr.getBotGenerator().getBotArrows().remove(mr);
                    mr.getTopGenerator().getTopArrows().remove(mr);
                }
            }
            else {
                EvenArrow<R> mr = new EvenArrow<R>(mor.getBotGenerator(), taObj);
                mr.setValue(mor.getValue().negate());
                taObj.addTopArrow(mr);
                mor.getBotGenerator().addBotArrow(mr);
            }
        }
    }
    
    @Override
    protected boolean extraCocycles(int sinv, int qmax, int qmin, R twoUnit) {
        cancelFromTopMod4(qmax,sinv+3);
        ArrayList<Generator<R>> posCocycles = new ArrayList<Generator<R>>();
        int ij = objectsDegree(0);
        for (Iterator<Generator<R>> it = generators.get(ij).iterator(); it.hasNext();) {
            EvenGenerator<R> obj = (EvenGenerator<R>) it.next();
            if (obj.qdeg() == sinv+1) {
                boolean found = false;
                int i = 0;
                while (i < obj.getTopArrows().size() & !found) {
                    EvenArrow<R> mor = obj.getTopArrow(i);
                    if (!mor.getValue().isInvertible() & mor.getBotGenerator().qdeg() == sinv+1) 
                        found = true;
                    else i++;
                }
                if (found) posCocycles.add(obj);
            }
        }
        if (posCocycles.isEmpty()) return false;
        ArrayList<Generator<R>> boundaries = new ArrayList<Generator<R>>();
        for (Iterator<Generator<R>> it = posCocycles.iterator(); it.hasNext();) {
            EvenGenerator<R> obj = (EvenGenerator<R>) it.next(); 
            for (Iterator<Arrow<R>> itt = obj.getBotArrows().iterator(); itt.hasNext();) {
                EvenArrow<R> mor = (EvenArrow<R>) itt.next();
                if ((mor.getValue().isInvertible()) & !boundaries.contains(mor.getTopGenerator())) 
                    boundaries.add(mor.getTopGenerator());
            }
        }
        ArrayList<ArrayList<R>> matrix = zeroMatrix(boundaries.size(),posCocycles.size(),twoUnit);
        for (int i = 0; i < posCocycles.size(); i++) {
            for (Arrow<R> mor : posCocycles.get(i).getBotArrows()) {
                if (boundaries.contains(mor.getTopGenerator())) {
                    int j = boundaries.indexOf(mor.getTopGenerator());
                    matrix.get(j).set(i, twoUnit.multiply(mor.getValue()));//2+mor.value) % 2);
                }
            }
        }
        improveMatrix(matrix);
        if (matrix.isEmpty()) matrix = zeroMatrix(1,posCocycles.size(),twoUnit);
        ArrayList<ArrayList<Integer>> cocycles = getCocycles(matrix);
        ArrayList<ArrayList<Generator<R>>> coObjects = getCoObjects(cocycles,posCocycles);
        modOutObjects(sinv+1);
        cancelFromTop(coObjects);
        cancelFromBot(qmin, qmax, coObjects);
        int p = this.objectsDegree(0);
        return !coObjects.isEmpty();
    }
    
    private void cancelFromTopMod4(int qmax, int qmin) {
        int qrun = qmax;
        while (qrun >= qmin) {
            boolean found = false;
            int t = 0;
            int u = 0;
            int ij = objectsDegree(0);
            while (!found & t < generators.get(ij).size()) {
                EvenGenerator<R> bObj = (EvenGenerator<R>) generators.get(ij).get(t);
                if (bObj.qdeg() == qrun ) {
                    u = 0;
                    while (u < bObj.getBotArrows().size() & !found) {
                        EvenArrow<R> mor = bObj.getBotArrow(u);
                        if (mor.getValue().isInvertible()) found = true;
                        else u++;
                    }
                }
                t++;
            }
            if (found) cancelMorObj(((EvenGenerator<R>) generators.get(ij).get(t-1)).getBotArrow(u));
            else qrun = qrun - 2;
        }
    }

    private void modOutObjects(int q) {
        int v = 0;
        while ( v < generators.size()) {
            ArrayList<Generator<R>> objs = generators.get(v);
            int t = 0;
            while (t < objs.size()) {
                EvenGenerator<R> obj = (EvenGenerator<R>) objs.get(t);
                if (obj.qdeg() > q) objs.remove(obj);
                else {
                    t++;
                    int s = 0;
                    while (s < obj.getBotArrows().size()) {
                        EvenArrow<R> mor = obj.getBotArrow(s);
                        if (mor.getTopGenerator().qdeg() > q) obj.getBotArrows().remove(mor);
                        else s++;
                    }
                }
            }
            v++;
        }
    }
    
    private void cancelFromTop(ArrayList<ArrayList<Generator<R>>> cocycles) {
        boolean found;
        ArrayList<Generator<R>> objs = generators.get(objectsDegree(0));
        int t = 0;
        while (t < objs.size()) {
            EvenGenerator<R> bObj = (EvenGenerator<R>) objs.get(t);
            int b = 0;
            found = false;
            while (b < bObj.getBotArrows().size() & !found) {
                EvenArrow<R> mor = bObj.getBotArrow(b);
                if (mor.getValue().isInvertible()) found = true;
                else b++;
            }
            if (found) {
                for (ArrayList<Generator<R>> cocycle : cocycles) {
                    if (cocycle.contains(bObj)) cocycle.remove(bObj);
                }
                int y = 0;
                while (y < cocycles.size()) {
                    if (cocycles.get(y).isEmpty()) cocycles.remove(y);
                    else y++;
                }
                cancelMorObj(bObj.getBotArrow(b));
                t--;
                if (t>=0) t--;
            }
            t++;
        }
    }
    
    private void cancelFromBot(int qmin, int qmax, ArrayList<ArrayList<Generator<R>>> cocycles) {
        int qrun = qmin;
        ArrayList<Generator<R>> objs = generators.get(objectsDegree(0));
        while (qrun <= qmax) {
            int t = 0;
            while (t < objs.size()) {
                EvenGenerator<R> tObj = (EvenGenerator<R>) objs.get(t);
                if (tObj.qdeg() == qrun) {
                    int b = 0;
                    boolean fund = false;
                    while (b < tObj.getTopArrows().size() & !fund) {
                        EvenArrow<R> mor = tObj.getTopArrow(b);
                        if (mor.getValue().isInvertible()) fund = true;
                        else b++;
                    }
                    if (fund) {
                        for (ArrayList<Generator<R>> cocycle : cocycles) {
                            if (cocycle.contains(tObj)) {
                                EvenGenerator<R> bObj = tObj.getTopArrow(b).getBotGenerator();
                                for (Iterator<Arrow<R>> it = bObj.getBotArrows().iterator(); it.hasNext();) {
                                    EvenArrow<R> mor = (EvenArrow<R>) it.next();
                                    if (mor.getValue().isInvertible()) {
                                        if (cocycle.contains(mor.getTopGenerator())) 
                                            cocycle.remove(mor.getTopGenerator());
                                        else cocycle.add(mor.getTopGenerator());
                                    }
                                }
                            }
                        }
                        int y = 0;
                        while (y < cocycles.size()) {
                            if (cocycles.get(y).isEmpty()) cocycles.remove(y);
                            else y++;
                        }
                        cancelMorObj(tObj.getTopArrow(b));
                        t--;
                        if (t>=0) t--;
                    }
                }
                t++;
            }
            qrun = qrun + 2;
        }
    }
    
    private void cancelMorObj(EvenArrow<R> mor) {
        int h = objectsDegree(0);
        EvenGenerator<R> bObj = mor.getBotGenerator();
        EvenGenerator<R> tObj = mor.getTopGenerator();
        bObj.getBotArrows().remove(mor);
        tObj.getTopArrows().remove(mor);
        for (Arrow<R> mr : tObj.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
        for (Arrow<R> mr : bObj.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        generators.get(h+bObj.hdeg()).remove(bObj);
        generators.get(h+tObj.hdeg()).remove(tObj);
        R u = mor.getValue();
        for (int fm = 0; fm < tObj.getTopArrows().size(); fm++) {
            EvenArrow<R> fmr = tObj.getTopArrow(fm);
            for (int sm = 0; sm < bObj.getBotArrows().size(); sm++) {
                EvenArrow<R> smr = bObj.getBotArrow(sm);
                R nv = u.negate().multiply(fmr.getValue().multiply(smr.getValue()));//((-1)*u*fmr.value*smr.value+8*mod) % mod;
                checkMorphism(nv, fmr.getBotGenerator(),smr.getTopGenerator());
            }
        }
        for (Arrow<R> mr : tObj.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        for (Arrow<R> mr : bObj.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
    }
    
    private void checkMorphism(R v, EvenGenerator<R> bObj, EvenGenerator<R> tObj) {
        boolean found = false;
        int i = 0;
        while (i < bObj.getBotArrows().size() & !found) {
            EvenArrow<R> mr = bObj.getBotArrow(i);
            if ((mr.getTopGenerator() == tObj) ) found = true;
            else i++;
        }
        if (found) {
            EvenArrow<R> mr = bObj.getBotArrow(i);
            mr.addValue(v); // = (mr.value + v + mod) % mod;
            if (mr.getValue().isZero()) {
                bObj.getBotArrows().remove(mr);
                tObj.getTopArrows().remove(mr);
            }
        }
        else if (!v.isZero()) {
            EvenArrow<R> mr = new EvenArrow<R>(bObj,tObj);
            mr.setValue(v);// = v;
            bObj.addBotArrow(mr);
            tObj.addTopArrow(mr);
        }
    }
    
    // for Bar-Natan spectral sequence
    
    @Override
    public boolean containsBoundaries() {
        boolean contains = false;
        int i = 0;
        while (!contains && i < generators.size()) {
            ArrayList<Generator<R>> gens = generators.get(i);
            int j = 0;
            while (!contains && j < gens.size()) {
                EvenGenerator<R> gen = (EvenGenerator<R>) gens.get(j);
                if (gen.bMorSize() > 0) contains = true;
                else j++;
            }
            i++;
        }
        return contains;
    }
    
    @Override
    public void cancelGenerators(int qJump) {
        int i = generators.size()-1;
        while (i >= 0) {
            ArrayList<Generator<R>> gens = generators.get(i);
            int j = gens.size()-1;
            while (j >= 0) {
                EvenGenerator<R> gen = (EvenGenerator<R>) gens.get(j);
                EvenArrow<R> cancelMor = cancellingArrow(gen, qJump);
                if (cancelMor != null) cancel(cancelMor, i);
                j--;
            }
            i--;
        }
    }
    
    private EvenArrow<R> cancellingArrow(EvenGenerator<R> gen, int qJump) {
        boolean found = false;
        int i = 0;
        while (i < gen.bMorSize() && !found) {
            EvenArrow<R> mor = gen.getBotArrow(i);
            if (mor.getTopGenerator().qdeg()-mor.getBotGenerator().qdeg() == qJump) found = true;
            else i++;
        }
        if (!found) return null;
        return gen.getBotArrow(i);
    }
    
    private void cancel(EvenArrow<R> arrow, int level) {
        EvenGenerator<R> tGen = arrow.getTopGenerator();
        EvenGenerator<R> bGen = arrow.getBotGenerator();
        bGen.getBotArrows().remove(arrow);
        tGen.getTopArrows().remove(arrow);
        for (Arrow<R> mr : tGen.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
        for (Arrow<R> mr : bGen.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        R u = arrow.getValue();
        generators.get(level).remove(bGen);
        generators.get(level+1).remove(tGen);
        for (Iterator<Arrow<R>> it = tGen.getTopArrows().iterator(); it.hasNext();) {
            EvenArrow<R> far = (EvenArrow<R>) it.next();
            for (Iterator<Arrow<R>> itt = bGen.getBotArrows().iterator(); itt.hasNext();) {
                EvenArrow<R> sar = (EvenArrow<R>) itt.next();
                EvenGenerator<R> aGen = far.getBotGenerator();
                EvenGenerator<R> zGen = sar.getTopGenerator();
                EvenArrow<R> azr = getArrowBetween(aGen, zGen);
                azr.addValue(far.getValue().multiply(u.invert()).multiply(sar.getValue()).negate());
                if (azr.getValue().isZero()) {
                    aGen.getBotArrows().remove(azr);
                    zGen.getTopArrows().remove(azr);
                }
            }
        }
        for (Arrow<R> mr : tGen.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        for (Arrow<R> mr : bGen.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
    }
    
    private EvenArrow<R> getArrowBetween(EvenGenerator<R> bGen, EvenGenerator<R> tGen) {
        boolean found = false;
        int i = 0;
        while (!found && i < bGen.bMorSize()) {
            EvenArrow<R> ar = bGen.getBotArrow(i);
            if (ar.getTopGenerator() == tGen) found = true;
            else i++;
        }
        if (found) return bGen.getBotArrow(i);
        EvenArrow<R> arr = new EvenArrow<R>(bGen, tGen);
        arr.setValue(unit.getZero());
        bGen.addBotArrow(arr);
        tGen.addTopArrow(arr);
        return arr;
    }
    
    @Override
    public ArrayList<String> homologyInfo() {
        ArrayList<String> theStrings = new ArrayList<String>();
        int[] qmaxmin = maxMinQ();
        int q = qmaxmin[1];
        ArrayList<QuantumCohomology> qCohs = new ArrayList<QuantumCohomology>();
        while (q <= qmaxmin[0]) {
            qCohs.add(new QuantumCohomology(q));
            q = q + 2;
        }
        for (int i = 0; i < generators.size(); i++) {
            for (int j = 0; j < generators.get(i).size(); j++) {
                EvenGenerator gen = (EvenGenerator) generators.get(i).get(j);
                addGenerator(qCohs, gen);
            }
        }
        for (QuantumCohomology coh : qCohs) theStrings.add(coh.toString());
        return theStrings;
    }
    
    private void addGenerator(ArrayList<QuantumCohomology> qCohs, EvenGenerator gen) {
        QuantumCohomology qCoh = theCohomology(qCohs, gen.qdeg());
        Homology hom = qCoh.findHomology(gen.hdeg(), true);
        hom.setBetti(hom.getBetti()+1);
    }
    
    private QuantumCohomology theCohomology(ArrayList<QuantumCohomology> qCohs, int q) {
        boolean found = false;
        int i = 0;
        while (!found && i < qCohs.size()) {
            if (qCohs.get(i).qdeg() == q) found = true;
            else i++;
        }
        return qCohs.get(i);
    }
    
    private int[] maxMinQ() {
        int j = firstNonEmpty();
        int qmin = ((EvenGenerator) generators.get(j).get(0)).qdeg();
        int qmax = qmin;
        for (int i = 0; i < generators.size(); i++) {
            for (int k = 0; k < generators.get(i).size(); k++) {
                int q = ((EvenGenerator) generators.get(i).get(k)).qdeg();
                if (qmin > q) qmin = q;
                if (qmax < q) qmax = q;
            }
        }
        return new int[] {qmax, qmin};
    }
    
    private int firstNonEmpty() {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (!generators.get(i).isEmpty()) found = true;
            else i++;
        }
        return i;
    }
    
}
