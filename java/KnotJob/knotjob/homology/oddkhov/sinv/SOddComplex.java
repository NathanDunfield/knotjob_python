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

package knotjob.homology.oddkhov.sinv;

import java.util.ArrayList;
import java.util.Iterator;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.homology.QGenerator;
import knotjob.homology.oddkhov.OddArrow;
import knotjob.homology.oddkhov.OddComplex;
import knotjob.homology.oddkhov.OddGenerator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class SOddComplex<R extends Ring<R>> extends OddComplex<R> {
    
    public SOddComplex(int comp, R unt, AbortInfo ab, 
            DialogWrap frm) {
        super(comp, unt, ab, frm, true);
    }
    
    public SOddComplex(int crs, int[] ends, int hstart, int qstart, boolean rev, 
            R unt, DialogWrap frm, AbortInfo abt) {
        super(crs, ends, hstart, qstart, rev, unt, frm, abt, true);
    }
    
    public SOddComplex(SOddComplex<R> complex, R unt, boolean negate) { // only meant to work for R = ModN
        super(unt, true, complex.abInf, complex.frame);
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
                    R fac = unt.multiply(mor.getValue());
                    if (!fac.isZero()) {
                        int pos = i-1;
                        if (negate) pos = 0;
                        OddGenerator<R> bObj = (OddGenerator<R>) generators.get(pos).get(
                                complex.generators.get(i-1).indexOf(mor.getBotGenerator()));
                        OddArrow<R> cmor;
                        if (negate) cmor = new OddArrow<R>(cObj,bObj);
                        else cmor = new OddArrow<R>(bObj,cObj);
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

    public void forgetBarNatan() {// for debugging, to see if Khov homology is recovered.
        for (ArrayList<Generator<R>> objs : generators) {
            for (Generator<R> obj : objs) {
                int i = obj.getBotArrows().size()-1;
                while (i >= 0) {
                    OddArrow<R> arr = (OddArrow<R>) obj.getBotArrows().get(i);
                    if (arr.getBotGenerator().qdeg() != arr.getTopGenerator().qdeg()) {
                        arr.getBotGenerator().removeBotArrow(arr);
                        arr.getTopGenerator().removeTopArrow(arr);
                    }
                    i--;
                }
            }
        }
    }
    
    public void slideTwoPowerTorsion() {
        R two = unit.add(unit);
        R power = unit.add(unit);
        while (!power.isZero()) {
            for (ArrayList<Generator<R>> objs : generators) {
                for (int i = 0; i < objs.size(); i++) {
                    OddArrow<R> arrow = findArrow((OddGenerator<R>) objs.get(i), power);
                    if (arrow != null) {
                        slideTop(arrow);
                        slideBot(arrow);
                    }
                }
            }
            power = power.multiply(two);
        }
        //testForNormalForm();
    }
    
    /*private void testForNormalForm() {    
        for (ArrayList<Generator<R>> objs : generators) {
            for (int i = 0; i < objs.size(); i++) {
                OddGenerator<R> gen = (OddGenerator<R>) objs.get(i);
                int qs = 0;
                for (int j = 0; j < gen.getBotArrowSize(); j++) {
                    OddGenerator<R> tGen = gen.getBotArrow(j).getTopGenerator();
                    if (gen.qdeg() == tGen.qdeg()) qs++;
                }
                if (qs > 1) System.out.println("Problem");
                for (int j = 0; j < gen.getTopArrowSize(); j++) {
                    OddGenerator<R> bGen = gen.getTopArrow(j).getBotGenerator();
                    if (gen.qdeg() == bGen.qdeg()) qs++;
                }
                if (qs > 1) System.out.println("XProblem");
            }
        }
    }// for debugging */
    
    private OddArrow<R> findArrow(OddGenerator<R> gen, R power) {
        boolean found = false;
        OddArrow<R> fArr = null;
        int k = 0;
        while (!found && k < gen.getBotArrowSize()) {
            OddArrow<R> arr = gen.getBotArrow(k);
            if (arr.getValue().div(power).isInvertible() && 
                    arr.getBotGenerator().qdeg() == arr.getTopGenerator().qdeg()) {
                fArr = arr;
                found = true;
            }
            k++;
        }
        return fArr;
    }
    
    public void slideTwoTorsion() {
        for (ArrayList<Generator<R>> objs : generators) {
            for (Generator<R> obj : objs) {
                int i = 0;
                while (i < obj.getBotArrows().size()) {
                    OddArrow<R> mor = (OddArrow<R>) obj.getBotArrows().get(i);
                    if (mor.getBotGenerator().qdeg() == mor.getTopGenerator().qdeg()) {
                        slideTop(mor);
                        slideBot(mor);
                    }
                    i++;
                }
            }
        }
    }
    
    private void slideTop(OddArrow<R> mor) {
        OddGenerator<R> bObj = mor.getBotGenerator();
        OddGenerator<R> tObj = mor.getTopGenerator();
        int i = 0;
        while (i < bObj.getBotArrows().size()) {
            OddArrow<R> mr = bObj.getBotArrow(i);
            if (mr != mor) {
                if (mr.getBotGenerator().qdeg() == mr.getTopGenerator().qdeg()) {
                    i--;
                    R k = mr.getValue().div(mor.getValue()).negate();
                    moveTopArrows(mr.getTopGenerator(), tObj, k);
                    moveBotArrows(tObj,mr.getTopGenerator(), k);
                }
            }
            i++;
        }
    }
    
    private void slideBot(OddArrow<R> mor) {
        OddGenerator<R> bObj = mor.getBotGenerator();
        OddGenerator<R> tObj = mor.getTopGenerator();
        int i = 0;
        while (i < tObj.getTopArrows().size()) {
            OddArrow<R> mr = tObj.getTopArrow(i);
            if (mr != mor) {
                if (mr.getBotGenerator().qdeg() == mr.getTopGenerator().qdeg()) {
                    i--;
                    R k = mr.getValue().div(mor.getValue());
                    moveTopArrows(bObj,mr.getBotGenerator(), k);
                    moveBotArrows(mr.getBotGenerator(),bObj, k);
                }
            }
            i++;
        }
    }
    
    private void moveTopArrows(OddGenerator<R> doObj, OddGenerator<R> taObj, R k) {
        for (Iterator<Arrow<R>> it = doObj.getBotArrows().iterator(); it.hasNext();) {
            OddArrow<R> mor = (OddArrow<R>) it.next();
            boolean found = false;
            int i = 0;
            while (!found & i < taObj.getBotArrows().size()) {
                if (taObj.getBotArrow(i).getTopGenerator() == mor.getTopGenerator()) found = true;
                else i++;
            }
            if (found) {
                OddArrow<R> mr = taObj.getBotArrow(i);
                mr.addValue(mor.getValue().multiply(k.negate()));
                if (mr.getValue().isZero()) {
                    mr.getBotGenerator().getBotArrows().remove(mr);
                    mr.getTopGenerator().getTopArrows().remove(mr);
                }
            }
            else {
                OddArrow<R> mr = new OddArrow<R>(taObj, mor.getTopGenerator());
                mr.setValue(mor.getValue().multiply(k.negate()));
                if (!mr.getValue().isZero()) {
                    taObj.addBotArrow(mr);//.botMor.add(mr);
                    mor.getTopGenerator().addTopArrow(mr);
                }
            }
        }
    }
    
    private void moveBotArrows(OddGenerator<R> doObj, OddGenerator<R> taObj, R k) {
        for (Iterator<Arrow<R>> it = doObj.getTopArrows().iterator(); it.hasNext();) {
            OddArrow<R> mor = (OddArrow<R>) it.next();
            boolean found = false;
            int i = 0;
            while (!found & i < taObj.getTopArrows().size()) {
                if (taObj.getTopArrow(i).getBotGenerator() == mor.getBotGenerator()) found = true;
                else i++;
            }
            if (found) {
                OddArrow<R> mr = taObj.getTopArrow(i);
                mr.addValue(mor.getValue().multiply(k));
                if (mr.getValue().isZero()) {
                    mr.getBotGenerator().getBotArrows().remove(mr);
                    mr.getTopGenerator().getTopArrows().remove(mr);
                }
            }
            else {
                OddArrow<R> mr = new OddArrow<R>(mor.getBotGenerator(), taObj);
                mr.setValue(mor.getValue().multiply(k));
                if (!mr.getValue().isZero()) {
                    taObj.addTopArrow(mr);
                    mor.getBotGenerator().addBotArrow(mr);
                }
            }
        }
    }
    
    /*private void moveTopArrows(OddGenerator<R> doObj, OddGenerator<R> taObj) {
        for (Iterator<Arrow<R>> it = doObj.getBotArrows().iterator(); it.hasNext();) {
            OddArrow<R> mor = (OddArrow<R>) it.next();
            boolean found = false;
            int i = 0;
            while (!found & i < taObj.getBotArrows().size()) {
                if (taObj.getBotArrow(i).getTopGenerator() == mor.getTopGenerator()) found = true;
                else i++;
            }
            if (found) {
                OddArrow<R> mr = taObj.getBotArrow(i);
                mr.addValue(mor.getValue());//always adding when moving top
                if (mr.getValue().isZero()) {
                    mr.getBotGenerator().getBotArrows().remove(mr);
                    mr.getTopGenerator().getTopArrows().remove(mr);
                }
            }
            else {
                OddArrow<R> mr = new OddArrow<R>(taObj, mor.getTopGenerator());
                mr.setValue(mor.getValue());
                taObj.addBotArrow(mr);//.botMor.add(mr);
                mor.getTopGenerator().addTopArrow(mr);
            }
        }
    }
    
    private void moveBotArrows(OddGenerator<R> doObj, OddGenerator<R> taObj) {
        for (Iterator<Arrow<R>> it = doObj.getTopArrows().iterator(); it.hasNext();) {
            OddArrow<R> mor = (OddArrow<R>) it.next();
            boolean found = false;
            int i = 0;
            while (!found & i < taObj.getTopArrows().size()) {
                if (taObj.getTopArrow(i).getBotGenerator() == mor.getBotGenerator()) found = true;
                else i++;
            }
            if (found) {
                OddArrow<R> mr = taObj.getTopArrow(i);
                mr.addValue(mor.getValue().negate());// always subtracting when moving bot
                if (mr.getValue().isZero()) {
                    mr.getBotGenerator().getBotArrows().remove(mr);
                    mr.getTopGenerator().getTopArrows().remove(mr);
                }
            }
            else {
                OddArrow<R> mr = new OddArrow<R>(mor.getBotGenerator(), taObj);
                mr.setValue(mor.getValue().negate());
                taObj.addTopArrow(mr);
                mor.getBotGenerator().addBotArrow(mr);
            }
        }
    }// old version that worked for mod 4 */

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
            OddArrow<R> fmr = (OddArrow<R>) tObj.getTopArrows().get(ii);
            for (int jj = 0; jj < bObj.getBotArrows().size(); jj++) {
                OddArrow<R> smr = (OddArrow<R>) bObj.getBotArrows().get(jj);
                boolean found = false;
                int y = 0;
                while (!found & y < fmr.getBotGenerator().getBotArrows().size()) {
                    Arrow<R> omr = fmr.getBotGenerator().getBotArrows().get(y);
                    if (omr.getTopGenerator() == smr.getTopGenerator()) found = true;
                    else y++;
                }
                OddArrow<R> tmr;
                if (found) {
                    tmr = (OddArrow<R>) fmr.getBotGenerator().getBotArrows().get(y);
                }
                else {
                    tmr = new OddArrow<R>((OddGenerator<R>) fmr.getBotGenerator(),
                            (OddGenerator<R>) smr.getTopGenerator());
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
    
    @Override
    protected boolean extraCocycles(int sinv, int qmax, int qmin, R twoUnit) {
        cancelFromTopMod4(qmax,sinv+3);
        ArrayList<Generator<R>> posCocycles = new ArrayList<Generator<R>>();
        int ij = objectsDegree(0);
        for (Iterator<Generator<R>> it = generators.get(ij).iterator(); it.hasNext();) {
            OddGenerator<R> obj = (OddGenerator<R>) it.next();
            if (obj.qdeg() == sinv+1) {
                boolean found = false;
                int i = 0;
                while (i < obj.getTopArrows().size() & !found) {
                    OddArrow<R> mor = obj.getTopArrow(i);
                    if (!mor.getValue().isInvertible() & mor.getBotGenerator().qdeg() == sinv+1) 
                        found = true;
                    else i++;
                }
                if (found) posCocycles.add(obj);
            }
        }// the generators in posCocycles span the image of Sq^1 (or other Bockstein)
        if (posCocycles.isEmpty()) return false;
        ArrayList<Generator<R>> boundaries = new ArrayList<Generator<R>>();
        for (Iterator<Generator<R>> it = posCocycles.iterator(); it.hasNext();) {
            OddGenerator<R> obj = (OddGenerator<R>) it.next();
            for (Iterator<Arrow<R>> itt = obj.getBotArrows().iterator(); itt.hasNext();) {
                OddArrow<R> mor = (OddArrow<R>) itt.next();
                if ((mor.getValue().isInvertible()) & !boundaries.contains(mor.getTopGenerator())) 
                    boundaries.add(mor.getTopGenerator());
            }
        }// boundaries contain all generators in the image of generators in posCocycles 
        ArrayList<ArrayList<R>> matrix = zeroMatrix(boundaries.size(),posCocycles.size(),twoUnit);
        for (int i = 0; i < posCocycles.size(); i++) {
            for (Arrow<R> mor : posCocycles.get(i).getBotArrows()) {
                if (boundaries.contains(mor.getTopGenerator())) {
                    int j = boundaries.indexOf(mor.getTopGenerator());
                    matrix.get(j).set(i, twoUnit.multiply(mor.getValue()));//2+mor.value) % 2);
                }
            }
        } // matrix is the matrix of the coboundary, restricted to generators in posCocycles
        improveMatrix(matrix); // sits in TangleComplex.java
        if (matrix.isEmpty()) matrix = zeroMatrix(1, posCocycles.size(), twoUnit);
        ArrayList<ArrayList<Integer>> cocycles = getCocycles(matrix); // in TangleComplex.java
        ArrayList<ArrayList<Generator<R>>> coObjects = getCoObjects(cocycles, posCocycles);
        modOutObjects(sinv+1); // removes all objects with q > sinv+1
        cancelFromTop(coObjects);
        cancelFromBot(qmin, qmax, coObjects);
        return !coObjects.isEmpty();
    }
    
    private void cancelFromTopMod4(int qmax, int qmin) { // it's called mod4, but also works for mod8 etc
        int qrun = qmax;
        while (qrun >= qmin) {
            boolean found = false;
            int t = 0;
            int u = 0;
            int ij = objectsDegree(0);
            while (!found & t < generators.get(ij).size()) {
                OddGenerator<R> bObj = (OddGenerator<R>) generators.get(ij).get(t);
                if (bObj.qdeg() == qrun ) {
                    u = 0;
                    while (u < bObj.getBotArrows().size() & !found) {
                        OddArrow<R> mor = bObj.getBotArrow(u);
                        if (mor.getValue().isInvertible()) found = true;
                        else u++;
                    }
                }
                t++;
            }
            if (found) cancelMorObj(((OddGenerator<R>) generators.get(ij).get(t-1)).getBotArrow(u));
            else qrun = qrun - 2;
        }
    }
    
    private void modOutObjects(int q) {
        int v = 0;
        while ( v < generators.size()) {
            ArrayList<Generator<R>> objs = generators.get(v);
            int t = 0;
            while (t < objs.size()) {
                OddGenerator<R> obj = (OddGenerator<R>) objs.get(t);
                if (obj.qdeg() > q) objs.remove(obj);
                else {
                    t++;
                    int s = 0;
                    while (s < obj.getBotArrows().size()) {
                        OddArrow<R> mor = obj.getBotArrow(s);
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
            OddGenerator<R> bObj = (OddGenerator<R>) objs.get(t);
            int b = 0;
            found = false;
            while (b < bObj.getBotArrows().size() & !found) {
                OddArrow<R> mor = bObj.getBotArrow(b);
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
                OddGenerator<R> tObj = (OddGenerator<R>) objs.get(t);
                if (tObj.qdeg() == qrun) {
                    int b = 0;
                    boolean fund = false;
                    while (b < tObj.getTopArrows().size() & !fund) {
                        OddArrow<R> mor = tObj.getTopArrow(b);
                        if (mor.getValue().isInvertible()) fund = true;
                        else b++;
                    }
                    if (fund) {
                        for (ArrayList<Generator<R>> cocycle : cocycles) {
                            if (cocycle.contains(tObj)) {
                                OddGenerator<R> bObj = tObj.getTopArrow(b).getBotGenerator();
                                for (Iterator<Arrow<R>> it = bObj.getBotArrows().iterator(); it.hasNext();) {
                                    OddArrow<R> mor = (OddArrow<R>) it.next();
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
    
    private void cancelMorObj(OddArrow<R> mor) {
        int h = objectsDegree(0);
        OddGenerator<R> bObj = mor.getBotGenerator();
        OddGenerator<R> tObj = mor.getTopGenerator();
        bObj.getBotArrows().remove(mor);
        tObj.getTopArrows().remove(mor);
        for (Arrow<R> mr : tObj.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
        for (Arrow<R> mr : bObj.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        generators.get(h+bObj.hdeg()).remove(bObj);
        generators.get(h+tObj.hdeg()).remove(tObj);
        R u = mor.getValue();
        for (int fm = 0; fm < tObj.getTopArrows().size(); fm++) {
            OddArrow<R> fmr = tObj.getTopArrow(fm);
            for (int sm = 0; sm < bObj.getBotArrows().size(); sm++) {
                OddArrow<R> smr = bObj.getBotArrow(sm);
                R nv = u.negate().multiply(fmr.getValue().multiply(smr.getValue()));//((-1)*u*fmr.value*smr.value+8*mod) % mod;
                checkMorphism(nv, fmr.getBotGenerator(),smr.getTopGenerator());
            }
        }
        for (Arrow<R> mr : tObj.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        for (Arrow<R> mr : bObj.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
    }
    
    private void checkMorphism(R v, OddGenerator<R> bObj, OddGenerator<R> tObj) {
        boolean found = false;
        int i = 0;
        while (i < bObj.getBotArrows().size() & !found) {
            OddArrow<R> mr = bObj.getBotArrow(i);
            if ((mr.getTopGenerator() == tObj) ) found = true;
            else i++;
        }
        if (found) {
            OddArrow<R> mr = bObj.getBotArrow(i);
            mr.addValue(v); // = (mr.value + v + mod) % mod;
            if (mr.getValue().isZero()) {
                bObj.getBotArrows().remove(mr);
                tObj.getTopArrows().remove(mr);
            }
        }
        else if (!v.isZero()) {
            OddArrow<R> mr = new OddArrow<R>(bObj,tObj);
            mr.setValue(v);// = v;
            bObj.addBotArrow(mr);
            tObj.addTopArrow(mr);
        }
    }
    
}
