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
import java.util.Iterator;
import javax.swing.JFrame;
import knotjob.AbortInfo;
import knotjob.Knobster;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.homology.TangleComplex;
import knotjob.rings.Matrix;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * with Nathan D
 * @param <R>
 */
public class UnifiedChainComplex<R extends Ring<R>> extends TangleComplex<R> {
    
    private final boolean reduced;
    private final boolean highDetail;
    private ArrayList<ArrayList<UnifiedGenerator<R>>> posCocycles;
    
    public UnifiedChainComplex(R unt, DialogWrap frm, AbortInfo abf, boolean red) {
        super(unt, abf, frm);
        reduced = red;
        if (frm == null) highDetail = false;
        else {
            JFrame jfrm = frm.getFrame();
            if (jfrm == null) highDetail = false;
            else highDetail = ((Knobster) jfrm).options.getGirthInfo() == 2;
        }
    }
    
    public UnifiedChainComplex(UnifiedChainComplex<R> complex, R unt, boolean negate, boolean red) { // only meant to work for R = ModN
        super(unt, complex.abInf, complex.frame);
        reduced = red;
        JFrame frm = complex.frame.getFrame();
        if (frm == null) highDetail = false;
        else highDetail = ((Knobster) frm).options.getGirthInfo() == 2;
        int factor = 1;
        if (negate) factor = -1;
        for (ArrayList<Generator<R>> objs : complex.generators) {
            int i = complex.generators.indexOf(objs);
            ArrayList<Generator<R>> objsc = new ArrayList<Generator<R>>();
            for (Iterator<Generator<R>> it = objs.iterator(); it.hasNext();) {
                UnifiedGenerator<R> obj = (UnifiedGenerator<R>) it.next(); 
                UnifiedGenerator<R> cObj = new UnifiedGenerator<R>(factor * obj.hdeg(),
                        factor * obj.qdeg());
                for (Iterator<Arrow<R>> itt = obj.getTopArrows().iterator(); itt.hasNext();) {
                    Arrow<R> mor = itt.next();
                    R fac = unt.multiply(mor.getValue());
                    if (!fac.isZero()) {
                        int pos = i-1;
                        if (negate) pos = 0;
                        UnifiedGenerator<R> bObj = (UnifiedGenerator<R>) generators.get(pos).get(
                                complex.generators.get(i-1).indexOf(mor.getBotGenerator()));
                        Arrow<R> cmor;
                        if (negate) cmor = new Arrow<R>(cObj, bObj, fac);
                        else cmor = new Arrow<R>(bObj, cObj, fac);
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

    public UnifiedChainComplex(ArrayList<ArrayList<Generator<R>>> gens, R unt, 
            boolean red, boolean hghDtl, DialogWrap fram, AbortInfo abf) {
        super(gens, unt, fram, abf);
        reduced = red;
        highDetail = hghDtl;
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
    
    public void addArrow(int i, int j, int k, R val) {
        if (val.isZero()) return;
        UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) generators.get(i).get(j);
        UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) generators.get(i-1).get(k);
        Arrow<R> arr = new Arrow<R>(bGen, tGen, val);
        bGen.addBotArrow(arr);
        tGen.addTopArrow(arr);
    }
    
    public Matrix<R> boundaryMatrix(int i) {
        Matrix<R> matrix = new Matrix<R>(generators.get(i).size(), generators.get(i+1).size(), unit);
        for (int j = 0; j < generators.get(i).size(); j++) {
            Generator<R> gen = generators.get(i).get(j);
            for (Arrow<R> arr : gen.getBotArrows()) {
                Generator<R> tGen = arr.getTopGenerator();
                matrix.set(j, generators.get(i+1).indexOf(tGen), arr.getValue());
            }
        }
        return matrix;
    }
    
    public int getLevel(UnifiedGenerator<R> gen) {
        boolean found = false;
        int i = 0;
        while (!found && i < generators.size()) {
            if (generators.get(i).contains(gen)) found = true;
            else i++;
        }
        if (found) return i;
        return -1;
    }
    
    public void shift(R k) {
        UnifiedGenerator<R> fGen = (UnifiedGenerator<R>) generators.get(0).get(0);
        UnifiedGenerator<R> sGen = (UnifiedGenerator<R>) generators.get(0).get(1);
        handleSlide(fGen, sGen, k);
    }
    
    public void output(UnifiedChainComplex<R> complex) {
        for (int i = 0; i < generators.size(); i++) {
            System.out.println();
            System.out.println("Level "+i);
            for (int j = 0; j < ((ArrayList<Generator<R>>) generators.get(i)).size(); j++) {
                System.out.println();
                System.out.println("Generator "+j);
                ArrayList<Generator<R>> nextLev = null;
                if (i < generators.size()-1) nextLev = generators.get(i+1);
                UnifiedGenerator<R> gen = (UnifiedGenerator<R>) generators.get(i).get(j);
                gen.output(nextLev, complex, i);
            }
        }
    }
    
    public ArrayList<UnifiedGenerator<R>> getCocycles() {// it is assumed complex in SNF
        return getCocycles(0, 0, false);
    }
    
    public ArrayList<UnifiedGenerator<R>> getCocycles(int hdeg, int qdeg) {// it is assumed complex in SNF
        return getCocycles(hdeg, qdeg, true);
    }
    
    private ArrayList<UnifiedGenerator<R>> getCocycles(int hdeg, int qdeg, boolean check) {
        ArrayList<UnifiedGenerator<R>> cocycles = new ArrayList<UnifiedGenerator<R>>();
        for (int i = 0; i < generators.size(); i++) {
            for (int j = 0; j < generators.get(i).size(); j++) {
                UnifiedGenerator<R> gen = (UnifiedGenerator<R>) generators.get(i).get(j);
                if (!check || (gen.hdeg() == hdeg && gen.qdeg() == qdeg)) {
                    if (sameQbotArrows(gen) == 0) {
                        boolean coboundary = isCoboundary(gen);
                        if (!coboundary) cocycles.add(gen);
                    }
                }
            }
        }
        return cocycles;
    }
    
    public void smithNormalize(boolean checkQ, int stage) {
        int tot = totalObjects();
        int i = generators.size()-1;
        int h = generators.get(this.getLevel(0)).get(0).hdeg();
        if (frame != null) {
            frame.setLabelLeft("Stage : ", 0, false);
            frame.setLabelRight(""+stage, 0, false);
            frame.setLabelLeft("Hom. degree : ", 1, false);
            frame.setLabelLeft("Objects : ", 2, false);
            frame.setLabelRight(""+(h+i), 1, false);
            frame.setLabelRight(""+tot, 2, false);
            if (highDetail) {
                frame.setLabelLeft("Pivot : ", 3, false);
                frame.setLabelRight("", 3, false);
            }
        }
        while (i > -1) {
            if (abInf.isAborted()) return;
            Arrow<R> arr = findSmallest(i, checkQ);
            if (arr == null) {
                i--;
                if (frame != null) frame.setLabelRight(""+(h+i), 1, false);
            }
            else {
                if (isolateBottom(arr, checkQ)) {
                    if (isolateTop(arr, checkQ)) {
                        tot = tot - 2;
                        if (frame != null) frame.setLabelRight(""+tot, 2, false);
                    }
                }
            }
        }
    }

    public void setBetaGens(Homomorphism<R> beta) {
        posCocycles = new ArrayList<ArrayList<UnifiedGenerator<R>>>();
        ArrayList<ArrayList<Integer>> positions = beta.targetPositions();
        int hdeg = beta.getTaHdeg();
        int level = getLevel(hdeg);
        for (ArrayList<Integer> pos : positions) {
            ArrayList<UnifiedGenerator<R>> gens = new ArrayList<UnifiedGenerator<R>>();
            for (int p : pos) gens.add((UnifiedGenerator<R>) generators.get(level).get(p));
            posCocycles.add(gens);
        }
    }
    
    public void setPossibleCocycles(ArrayList<Generator<R>> gens) {
        posCocycles = new ArrayList<ArrayList<UnifiedGenerator<R>>>();
        for (int i = 0; i < gens.size(); i++) {
            UnifiedGenerator<R> gen = (UnifiedGenerator<R>) gens.get(i);
            ArrayList<UnifiedGenerator<R>> cycs = new ArrayList<UnifiedGenerator<R>>();
            for (Arrow<R> ar : gen.getOutArrows()) {
                cycs.add((UnifiedGenerator<R>) ar.getTopGenerator());
            }
            if (!cycs.isEmpty()) posCocycles.add(cycs);
        }
    }
    
    public int getLevel(int hdeg) {
        for (ArrayList<Generator<R>> gens : generators) {
            for (Generator<R> gen : gens) 
                if (gen.hdeg() == hdeg) return generators.indexOf(gens);
        }
        return -1;
    }
    
    private boolean isCoboundary(UnifiedGenerator<R> gen) {
        for (Arrow<R> arr : gen.getTopArrows()) {
            UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) arr.getBotGenerator();
            if (bGen.qdeg() == gen.qdeg() && arr.getValue().isInvertible()) return true;
        }
        return false;
    }
    
    private int sameQbotArrows(UnifiedGenerator<R> gen) {
        int same = 0;
        for (Arrow<R> arr : gen.getBotArrows()) {
            UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
            if (tGen.qdeg() == gen.qdeg()) same++;
        }
        return same;
    }
    
    private Arrow<R> findSmallest(int i, boolean checkQ) {
        ArrayList<Arrow<R>> possSmallest = new ArrayList<Arrow<R>>();
        int j = 0;
        //boolean found = false;
        Arrow<R> smallest = null;
        while (j < generators.get(i).size()) {
            Generator<R> gen = generators.get(i).get(j);
            int k = 0;
            while (k < gen.getTopArrows().size()) {
                Arrow<R> arr = gen.getTopArrows().get(k);
                if (smallest == null && notIsolated(arr, checkQ)){
                    smallest = arr;
                    possSmallest.add(arr);
                    //if (smallest.getValue().isInvertible()) big = ;
                }
                else{
                    if(smallest != null && !arr.getValue().isBigger(smallest.getValue()) && 
                            notIsolated(arr, checkQ)) {
                        smallest = arr;
                        possSmallest.add(0, arr);
                        //if (smallest.getValue().isInvertible()) found = true;
                    }
                }
                k++;
            }
            j++;
        }
        if (possSmallest.size() <= 1) return smallest;
        R minValue = possSmallest.get(0).getValue();
        int tot = getStuff(smallest);
        //R pivotNorm = getNorm(smallest, unit.getZero());
        i = 1;
        while (i < possSmallest.size() && !possSmallest.get(i).getValue().isBigger(minValue)) {
            int t = getStuff(possSmallest.get(i));
            if (tot > t) {
                tot = t;
                smallest = possSmallest.get(i);
            }
            /*R norm = getNorm(possSmallest.get(i), pivotNorm);
            if (pivotNorm.isBigger(norm)) {
                pivotNorm = norm;
                smallest = possSmallest.get(i);
            }// */
            i++;
        }
        if (highDetail) {
            String str = minValue.toString();
            int length = str.length();
            if (length > 20) frame.setLabelRight("10^"+length, 3, false);
            else frame.setLabelRight(str, 3, false);
        }
        return smallest;
    }
    
    private int getStuff(Arrow<R> arr) {
        UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) arr.getBotGenerator();
        UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
        return bGen.getBotArrows().size()+tGen.getTopArrowSize();
    }
    
    private R getNorm(Arrow<R> arr, R max) {
        boolean check = !max.isZero();
        boolean cont = true;
        UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) arr.getBotGenerator();
        UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
        int i = 0;
        R val = unit.getZero();
        while (cont && i < bGen.getBotArrows().size()) {
            val = val.add(bGen.getBotArrows().get(i).getValue().abs(0));
            if (check && val.isBigger(max)) cont = false;
            i++;
        }
        i = 0;
        R valu = unit.getZero();
        while (cont && i < tGen.getTopArrowSize()) {
            valu = valu.add(tGen.getTopArrow(i).getValue().abs(0));
            if (check && valu.multiply(val).isBigger(max)) cont = false;
            i++;
        }
        return val.multiply(valu);
    }
    
    private boolean notIsolated(Arrow<R> arr, boolean checkQ) {
        if (checkQ) return notIsolated(arr);
        Generator<R> bGen = arr.getBotGenerator();
        Generator<R> tGen = arr.getTopGenerator();
        return (bGen.getBotArrows().size()+bGen.getTopArrows().size() +
                tGen.getBotArrows().size()+tGen.getTopArrows().size() > 2);
    }
    
    private boolean notIsolated(Arrow<R> arr) {
        UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) arr.getBotGenerator();
        UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
        if (bGen.qdeg() != tGen.qdeg()) return false;
        if (sameQinOut(bGen) > 1) return true;
        return (sameQinOut(tGen) > 1);
    }
    
    private int sameQinOut(UnifiedGenerator<R> gen) {
        int same = 0;
        for (Arrow<R> arr : gen.getBotArrows()) {
            UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
            if (tGen.qdeg() == gen.qdeg()) same++;
        }
        if (same > 1) return same;
        for (Arrow<R> arr : gen.getTopArrows()) {
            UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) arr.getBotGenerator();
            if (bGen.qdeg() == gen.qdeg()) same++;
        }
        return same;
    }
    
    private int sameQOut(UnifiedGenerator<R> gen, boolean checkQ) {
        int same = 0;
        for (Arrow<R> arr : gen.getBotArrows()) {
            UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
            if (!checkQ || tGen.qdeg() == gen.qdeg()) same++;
        }
        return same;
    }
    
    private boolean isolateBottom(Arrow<R> arr, boolean checkQ) {
        Generator<R> bGen = arr.getBotGenerator();
        UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
        int m = bGen.getBotArrows().size() - 1;
        while (m >= 0) {
            Arrow<R> ar = bGen.getBotArrows().get(m);
            UnifiedGenerator<R> aGen = (UnifiedGenerator<R>) ar.getTopGenerator();
            if ((!checkQ || aGen.qdeg() == tGen.qdeg()) && ar != arr) {
                R k = ar.getValue().div(arr.getValue()).negate();
                handleSlide(aGen, (UnifiedGenerator<R>) tGen, k.negate());
            }
            m--;
        }
        return sameQOut((UnifiedGenerator<R>) bGen, checkQ) == 1;
    }
    
    private boolean isolateTop(Arrow<R> arr, boolean checkQ) {
        UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) arr.getBotGenerator();
        Generator<R> tGen = arr.getTopGenerator();
            int m = tGen.getTopArrows().size()-1;
            while (m >= 0) {
                Arrow<R> ar = arr.getTopGenerator().getTopArrows().get(m);
                UnifiedGenerator<R> aGen = (UnifiedGenerator<R>) ar.getBotGenerator();
                if ((!checkQ || bGen.qdeg() == aGen.qdeg()) && ar != arr) {
                    R k = ar.getValue().div(arr.getValue()).negate();
                    handleSlide((UnifiedGenerator<R>) bGen, (UnifiedGenerator<R>) aGen, k);
                }
                m--;
            }
        return sameQinOut((UnifiedGenerator<R>) tGen) == 1;
    }
    
    private void handleSlide(UnifiedGenerator<R> xGen, UnifiedGenerator<R> yGen,
            R value) { // basis x, y turns into x, y+kx
        for (Arrow<R> arr : yGen.getTopArrows()) {
            UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) arr.getBotGenerator();
            boolean found = false;
            int i = 0;
            while (!found && i < xGen.getTopArrowSize()) {
                if (xGen.getTopArrow(i).getBotGenerator() == bGen) found = true;
                else i++;
            }
            slideBottomArrows(found, i, xGen, arr, value);
        }
        for (Arrow<R> arr : yGen.getInArrows()) {
            UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) arr.getBotGenerator();
            boolean found = false;
            int i = 0;
            while (!found && i < xGen.getInArrows().size()) {
                if (xGen.getInArrows().get(i).getBotGenerator() == bGen) found = true;
                else i++;
            }
            slideInArrows(found, i, xGen, arr, value);
        }
        for (Arrow<R> arr : xGen.getBotArrows()) {
            UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
            boolean found = false;
            int i = 0;
            while (!found && i < yGen.getBotArrows().size()) {
                if (yGen.getBotArrows().get(i).getTopGenerator() == tGen) found = true;
                else i++;
            }
            slideTopArrows(found, i, yGen, arr, value);
        }
        for (Arrow<R> arr : xGen.getOutArrows()) {
            UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
            boolean found = false;
            int i = 0;
            while (!found && i < yGen.getOutArrows().size()) {
                if (yGen.getOutArrows().get(i).getTopGenerator() == tGen) found = true;
                else i++;
            }
            slideOutArrows(found, i, yGen, arr, value);
        }
    }
    
    private void slideTopArrows(boolean found, int i, UnifiedGenerator<R> yGen,
            Arrow<R> arr, R k) {
        Arrow<R> narr;
        if (found) {
            narr = yGen.getBotArrows().get(i);
            narr.addValue(k.multiply(arr.getValue()));
            if (narr.getValue().isZero()) {
                narr.getBotGenerator().getBotArrows().remove(narr);
                narr.getTopGenerator().getTopArrows().remove(narr);
            }
        }
        else {
            narr = new Arrow<R>(yGen, arr.getTopGenerator(), k.multiply(arr.getValue()));
            if (!narr.getValue().isZero()) {
                narr.getBotGenerator().getBotArrows().add(narr);
                narr.getTopGenerator().getTopArrows().add(narr);
            }
        }
    }
    
    private void slideOutArrows(boolean found, int i, UnifiedGenerator<R> yGen,
            Arrow<R> arr, R k) {
        Arrow<R> narr;
        if (found) {
            narr = yGen.getOutArrows().get(i);
            UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) narr.getTopGenerator();
            narr.addValue(k.multiply(arr.getValue()));
            if (narr.getValue().isZero()) {
                yGen.getOutArrows().remove(narr);
                tGen.getInArrows().remove(narr);
            }
        }
        else {
            UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
            narr = new Arrow<R>(yGen, tGen, k.multiply(arr.getValue()));
            if (!narr.getValue().isZero()) {
                yGen.getOutArrows().add(narr);
                tGen.getInArrows().add(narr);
            }
        }
    }
    
    private void slideBottomArrows(boolean found, int i, UnifiedGenerator<R> xGen,
            Arrow<R> arr, R k) {
        Arrow<R> narr;
        if (found) {
            narr = xGen.getTopArrow(i);
            narr.addValue(k.multiply(arr.getValue()).negate());
            if (narr.getValue().isZero()) {
                narr.getBotGenerator().getBotArrows().remove(narr);
                narr.getTopGenerator().getTopArrows().remove(narr);
            }
        }
        else {
            narr = new Arrow<R>(arr.getBotGenerator(), xGen, k.multiply(arr.getValue()).negate());
            if (!narr.getValue().isZero()) {
                narr.getBotGenerator().getBotArrows().add(narr);
                narr.getTopGenerator().getTopArrows().add(narr);
            }
        }
    }
    
    private void slideInArrows(boolean found, int i, UnifiedGenerator<R> xGen,
            Arrow<R> arr, R k) {
        Arrow<R> narr;
        if (found) {
            narr = xGen.getInArrows().get(i);
            UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) narr.getBotGenerator();
            narr.addValue(k.multiply(arr.getValue()).negate());
            if (narr.getValue().isZero()) {
                bGen.getOutArrows().remove(narr);
                xGen.getInArrows().remove(narr);
            }
        }
        else {
            UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) arr.getBotGenerator();
            narr = new Arrow<R>(bGen, xGen, k.multiply(arr.getValue()).negate());
            if (!narr.getValue().isZero()) {
                bGen.getOutArrows().add(narr);
                xGen.getInArrows().add(narr);
            }
        }
    }
    
    // What follows is needed for sq^1 sum refinement.
    
    @Override
    protected boolean extraCocycles(int sinv, int qmax, int qmin, R twoUnit) {
        int add = 3;
        if (reduced) add--;
        cancelFromTop(qmax, sinv+add);
        ArrayList<UnifiedGenerator<R>> boundaries = new ArrayList<UnifiedGenerator<R>>();
        for (ArrayList<UnifiedGenerator<R>> poscyc : posCocycles) {
            for (UnifiedGenerator<R> bGen : poscyc) {
                for (Arrow<R> arr : bGen.getBotArrows()) {
                    UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
                    if (!boundaries.contains(tGen) && arr.getValue().isInvertible())
                        boundaries.add(tGen);
                }
            }
        }
        ArrayList<ArrayList<R>> matrix = zeroMatrix(boundaries.size(),posCocycles.size(), twoUnit);
        for (int i = 0; i < posCocycles.size(); i++) {
            for (UnifiedGenerator<R> gen : posCocycles.get(i)) {
                for (Arrow<R> arr : gen.getBotArrows()) {
                    UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
                    if (arr.getValue().isInvertible()) {
                        int j = boundaries.indexOf(tGen);
                        matrix.get(j).set(i, matrix.get(j).get(i).add(twoUnit));
                    }
                }
            }
        }
        improveMatrix(matrix); // sits in TangleComplex.java
        if (matrix.isEmpty()) matrix = zeroMatrix(1, posCocycles.size(), twoUnit);
        ArrayList<ArrayList<Integer>> cocycles = getCocycles(matrix);
        ArrayList<ArrayList<UnifiedGenerator<R>>> coObjects = getCoGenerators(cocycles, posCocycles);
        modOutObjects(sinv+1);
        cancelFromTop(coObjects);
        cancelFromBot(qmin, qmax, coObjects);
        return !coObjects.isEmpty();
    }
    
    private void cancelFromTop(int qmax, int qmin) { 
        int qrun = qmax;
        while (qrun >= qmin) {
            boolean found = false;
            int t = 0;
            int u = 0;
            int ij = objectsDegree(0);
            while (!found & t < generators.get(ij).size()) {
                UnifiedGenerator<R> bObj = (UnifiedGenerator<R>) generators.get(ij).get(t);
                if (bObj.qdeg() == qrun ) {
                    u = 0;
                    while (u < bObj.getBotArrows().size() & !found) {
                        Arrow<R> mor = bObj.getBotArrows().get(u);
                        if (mor.getValue().isInvertible()) found = true;
                        else u++;
                    }
                }
                t++;
            }
            if (found) cancelMorObj((generators.get(ij).get(t-1)).getBotArrows().get(u));
            else qrun = qrun - 2;
        }
    }
    
    private ArrayList<ArrayList<UnifiedGenerator<R>>> getCoGenerators(ArrayList<ArrayList<Integer>> cocycles, 
            ArrayList<ArrayList<UnifiedGenerator<R>>> posCocycles) {
        ArrayList<ArrayList<UnifiedGenerator<R>>> objs = new ArrayList<ArrayList<UnifiedGenerator<R>>>();
        for (ArrayList<Integer> ints : cocycles) {
            ArrayList<UnifiedGenerator<R>> cocs = new ArrayList<UnifiedGenerator<R>>();
            for (int y : ints) {
                for (UnifiedGenerator<R> gen : posCocycles.get(y)) {
                    if (cocs.contains(gen)) cocs.remove(gen);
                    else cocs.add(gen);
                }
            }
            if (!cocs.isEmpty()) objs.add(cocs);
        }
        return objs;
    }
    
    private void modOutObjects(int q) {
        int v = 0;
        while ( v < generators.size()) {
            ArrayList<Generator<R>> objs = generators.get(v);
            int t = 0;
            while (t < objs.size()) {
                UnifiedGenerator<R> obj = (UnifiedGenerator<R>) objs.get(t);
                if (obj.qdeg() > q) objs.remove(obj);
                else {
                    t++;
                    int s = 0;
                    while (s < obj.getBotArrows().size()) {
                        Arrow<R> mor = obj.getBotArrows().get(s);
                        if (((UnifiedGenerator<R>)mor.getTopGenerator()).qdeg() > q) obj.getBotArrows().remove(mor);
                        else s++;
                    }
                }
            }
            v++;
        }
    }
    
    private void cancelFromTop(ArrayList<ArrayList<UnifiedGenerator<R>>> cocycles) {
        boolean found;
        ArrayList<Generator<R>> objs = generators.get(objectsDegree(0));
        int t = 0;
        while (t < objs.size()) {
            UnifiedGenerator<R> bObj = (UnifiedGenerator<R>) objs.get(t);
            int b = 0;
            found = false;
            while (b < bObj.getBotArrows().size() & !found) {
                Arrow<R> mor = bObj.getBotArrows().get(b);
                if (mor.getValue().isInvertible()) found = true;
                else b++;
            }
            if (found) {
                for (ArrayList<UnifiedGenerator<R>> cocycle : cocycles) {
                    if (cocycle.contains(bObj)) cocycle.remove(bObj);
                }
                int y = 0;
                while (y < cocycles.size()) {
                    if (cocycles.get(y).isEmpty()) cocycles.remove(y);
                    else y++;
                }
                cancelMorObj(bObj.getBotArrows().get(b));
                t--;
                if (t>=0) t--;
            }
            t++;
        }
    }
    
    private void cancelFromBot(int qmin, int qmax, ArrayList<ArrayList<UnifiedGenerator<R>>> cocycles) {
        int qrun = qmin;
        ArrayList<Generator<R>> objs = generators.get(objectsDegree(0));
        while (qrun <= qmax) {
            int t = 0;
            while (t < objs.size()) {
                UnifiedGenerator<R> tObj = (UnifiedGenerator<R>) objs.get(t);
                if (tObj.qdeg() == qrun) {
                    int b = 0;
                    boolean fund = false;
                    while (b < tObj.getTopArrows().size() & !fund) {
                        Arrow<R> mor = tObj.getTopArrows().get(b);
                        if (mor.getValue().isInvertible()) fund = true;
                        else b++;
                    }
                    if (fund) {
                        for (ArrayList<UnifiedGenerator<R>> cocycle : cocycles) {
                            if (cocycle.contains(tObj)) {
                                Generator<R> bObj = tObj.getTopArrow(b).getBotGenerator();
                                for (Iterator<Arrow<R>> it = bObj.getBotArrows().iterator(); it.hasNext();) {
                                    Arrow<R> mor = it.next();
                                    if (mor.getValue().isInvertible()) {
                                        if (cocycle.contains((UnifiedGenerator<R>) mor.getTopGenerator())) 
                                            cocycle.remove((UnifiedGenerator<R>) mor.getTopGenerator());
                                        else cocycle.add((UnifiedGenerator<R>) mor.getTopGenerator());
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
    
    private void cancelMorObj(Arrow<R> mor) {
        int h = objectsDegree(0);
        Generator<R> bObj = mor.getBotGenerator();
        Generator<R> tObj = mor.getTopGenerator();
        bObj.getBotArrows().remove(mor);
        tObj.getTopArrows().remove(mor);
        for (Arrow<R> mr : tObj.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
        for (Arrow<R> mr : bObj.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        generators.get(h+bObj.hdeg()).remove(bObj);
        generators.get(h+tObj.hdeg()).remove(tObj);
        R u = mor.getValue();
        for (int fm = 0; fm < tObj.getTopArrows().size(); fm++) {
            Arrow<R> fmr = tObj.getTopArrows().get(fm);
            for (int sm = 0; sm < bObj.getBotArrows().size(); sm++) {
                Arrow<R> smr = bObj.getBotArrows().get(sm);
                R nv = u.negate().multiply(fmr.getValue().multiply(smr.getValue()));//((-1)*u*fmr.value*smr.value+8*mod) % mod;
                checkMorphism(nv, fmr.getBotGenerator(),smr.getTopGenerator());
            }
        }
        for (Arrow<R> mr : tObj.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        for (Arrow<R> mr : bObj.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
    }
    
    private void checkMorphism(R v, Generator<R> bObj, Generator<R> tObj) {
        boolean found = false;
        int i = 0;
        while (i < bObj.getBotArrows().size() & !found) {
            Arrow<R> mr = bObj.getBotArrows().get(i);
            if ((mr.getTopGenerator() == tObj) ) found = true;
            else i++;
        }
        if (found) {
            Arrow<R> mr = bObj.getBotArrows().get(i);
            mr.addValue(v); // = (mr.value + v + mod) % mod;
            if (mr.getValue().isZero()) {
                bObj.getBotArrows().remove(mr);
                tObj.getTopArrows().remove(mr);
            }
        }
        else if (!v.isZero()) {
            Arrow<R> mr = new Arrow<R>(bObj, tObj, v);
            //mr.setValue(v);// = v;
            bObj.addBotArrow(mr);
            tObj.addTopArrow(mr);
        }
    }

    public void removeBoundaries() { // assuming the complex is in Smith normal form
        for (ArrayList<Generator<R>> gens : generators) {
            int i = gens.size()-1;
            while (i >= 0) {
                UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) gens.get(i);
                if (!tGen.getTopArrows().isEmpty()) {
                    int j = 0;
                    boolean found = false;
                    while (j < tGen.getTopArrowSize() && !found) {
                        UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) tGen.getTopArrow(j).getBotGenerator();
                        if (bGen.qdeg() == tGen.qdeg() && tGen.getTopArrow(j).getValue().isInvertible())
                            found = true;
                        else j++;
                    }
                    if (found) removeArrow(tGen.getTopArrow(j));
                }
                i--;
            }
        }
    }
    
    private void removeArrow(Arrow<R> arr) {
        UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) arr.getBotGenerator();
        UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
        int i = this.objectsDegree(bGen.hdeg());
        generators.get(i).remove(bGen);
        generators.get(i+1).remove(tGen);
        for (Arrow<R> ar : bGen.getBotArrows()) ar.getTopGenerator().getTopArrows().remove(ar);
        for (Arrow<R> ar : bGen.getTopArrows()) ar.getBotGenerator().getBotArrows().remove(ar);
        for (Arrow<R> ar : bGen.getOutArrows()) 
            ((UnifiedGenerator<R>) ar.getTopGenerator()).getInArrows().remove(ar);
        for (Arrow<R> ar : bGen.getInArrows()) 
            ((UnifiedGenerator<R>) ar.getBotGenerator()).getOutArrows().remove(ar);
        for (Arrow<R> ar : tGen.getBotArrows()) ar.getTopGenerator().getTopArrows().remove(ar);
        for (Arrow<R> ar : tGen.getTopArrows()) ar.getBotGenerator().getBotArrows().remove(ar);
        for (Arrow<R> ar : tGen.getOutArrows()) 
            ((UnifiedGenerator<R>) ar.getTopGenerator()).getInArrows().remove(ar);
        for (Arrow<R> ar : tGen.getInArrows()) 
            ((UnifiedGenerator<R>) ar.getBotGenerator()).getOutArrows().remove(ar);
    }
    
    public void cancelBoundaries(boolean checkQ) {
        for (ArrayList<Generator<R>> gens : generators) {
            int i = gens.size()-1;
            while (i >= 0) {
                UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) gens.get(i);
                if (!tGen.getTopArrows().isEmpty()) {
                    int j = 0;
                    boolean found = false;
                    while (!found && j < tGen.getTopArrowSize()) {
                        Arrow<R> arr = tGen.getTopArrow(j);
                        UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) arr.getBotGenerator();
                        if ((!checkQ || bGen.qdeg() == tGen.qdeg()) && arr.getValue().isInvertible()) 
                            found = true;
                        else j++;
                    }
                    if (found) {
                        cancelArrow(tGen.getTopArrow(j), generators.indexOf(gens));
                    }
                }
                i--;
            }
        }
    }
    
    private void cancelArrow(Arrow<R> arr, int i) {
        UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
        UnifiedGenerator<R> bGen = (UnifiedGenerator<R>) arr.getBotGenerator();
        R factor = arr.getValue().negate();
        tGen.getTopArrows().remove(arr);
        bGen.getBotArrows().remove(arr);
        for (Arrow<R> ar : tGen.getTopArrows()) {
            UnifiedGenerator<R> cGen = (UnifiedGenerator<R>) ar.getBotGenerator();
            for (Arrow<R> tar : bGen.getBotArrows()) {
                UnifiedGenerator<R> uGen = (UnifiedGenerator<R>) tar.getTopGenerator();
                boolean found = false;
                int k = 0;
                Arrow<R> oar;
                while (k < cGen.getBotArrows().size() && !found) {
                    oar = cGen.getBotArrows().get(k);
                    if (oar.getTopGenerator() == uGen) found = true;
                    else k++;
                }
                if (found) {
                    oar = cGen.getBotArrows().get(k);
                    oar.addValue(ar.getValue().multiply(factor).multiply(tar.getValue()));
                    if (oar.getValue().isZero()) {
                        cGen.getBotArrows().remove(oar);
                        uGen.getTopArrows().remove(oar);
                    }
                }
                else {
                    oar = new Arrow<R>(cGen, uGen, ar.getValue().multiply(factor).multiply(tar.getValue()));
                    if (!oar.getValue().isZero()) {
                        cGen.getBotArrows().add(oar);
                        uGen.getTopArrows().add(oar);
                    }
                }
            }
        }
        for (Arrow<R> ar : bGen.getBotArrows()) ar.getTopGenerator().getTopArrows().remove(ar);
        for (Arrow<R> ar : bGen.getTopArrows()) ar.getBotGenerator().getBotArrows().remove(ar);
        for (Arrow<R> ar : tGen.getBotArrows()) ar.getTopGenerator().getTopArrows().remove(ar);
        for (Arrow<R> ar : tGen.getTopArrows()) ar.getBotGenerator().getBotArrows().remove(ar);
        for (Arrow<R> ar : bGen.getOutArrows()) 
            ((UnifiedGenerator<R>) ar.getTopGenerator()).getInArrows().remove(ar);
        for (Arrow<R> ar : bGen.getInArrows()) 
            ((UnifiedGenerator<R>) ar.getBotGenerator()).getOutArrows().remove(ar);
        for (Arrow<R> ar : tGen.getOutArrows()) 
            ((UnifiedGenerator<R>) ar.getTopGenerator()).getInArrows().remove(ar);
        for (Arrow<R> ar : tGen.getInArrows()) 
            ((UnifiedGenerator<R>) ar.getBotGenerator()).getOutArrows().remove(ar);
        generators.get(i).remove(tGen);
        generators.get(i-1).remove(bGen);
    }

    public void outputBetti() {
        ArrayList<Integer> theQs = this.getQs();
        int qmax = theQs.get(theQs.size()-1);
        int qmin = theQs.get(0);
        for (int j = qmin; j <= qmax; j = j+2) {
            for (ArrayList<Generator<R>> gens : generators) {
                if (!gens.isEmpty()) {
                    int h = gens.get(0).hdeg();
                    int betti = 0;
                    for (int i = 0; i < gens.size(); i++) {
                        UnifiedGenerator<R> gen = (UnifiedGenerator<R>) gens.get(i);
                        if (gen.qdeg() == j) betti++;
                        if (sameQinOut(gen) > 0) System.out.println("Dubious");
                    }
                    if (betti > 0) System.out.println("("+h+", "+j+") = "+betti);
                }
            }
        }
    }
    
    public void prepareComplex(UnifiedChainComplex<R> otComplex, UnifiedChainComplex<R> bnComplex) {
        int lv = this.getLevel(0);
        int blv = bnComplex.getLevel(0);
        ArrayList<UnifiedGenerator<R>> bnGens = new ArrayList<UnifiedGenerator<R>>();
        for (Generator<R> gen : bnComplex.getGenerators(blv)) {
            bnGens.add(new UnifiedGenerator<R>(1, 0));
        }
        ArrayList<Generator<R>> gens = this.getGenerators(lv);
        for (int i = 0; i < gens.size(); i++) {
            UnifiedGenerator<R> fGen = (UnifiedGenerator<R>) gens.get(i);
            UnifiedGenerator<R> sGen = (UnifiedGenerator<R>) otComplex.getGenerators(lv).get(i);
            if (!fGen.getOutArrows().isEmpty()) 
                redirectArrows(fGen, bnComplex.getGenerators(blv), bnGens);
            Arrow<R> cArr = new Arrow<R>(fGen, sGen, unit);
            fGen.addOutArrow(cArr);
            sGen.addInArrow(cArr);
        }
        if (this.generatorSize() <= lv+1) generators.add(new ArrayList<Generator<R>>());
        for (UnifiedGenerator<R> cGen : bnGens) generators.get(lv+1).add(cGen);
    }
    
    private void redirectArrows(UnifiedGenerator<R> fGen, ArrayList<Generator<R>> gens,
            ArrayList<UnifiedGenerator<R>> bnGens) {
        if (fGen.getBotArrows().isEmpty()) {
            for (Arrow<R> arr : fGen.getOutArrows()) {
                int k = gens.indexOf(arr.getTopGenerator());
                Arrow<R> nArr = new Arrow<R>(fGen, bnGens.get(k), arr.getValue());
                fGen.addBotArrow(nArr);
                bnGens.get(k).addTopArrow(nArr);
            }
        }
        fGen.getOutArrows().clear();
    }
    
    public void throwAway(int qdeg) {   // this throws away all generators whose quantum degree
                                        // is different from qdeg, without paying attention to 
                                        // morphisms changing q-degree
        for (ArrayList<Generator<R>> gens : generators) {
            int k = gens.size()-1;
            while (k >= 0) {
                UnifiedGenerator<R> gen = (UnifiedGenerator<R>) gens.get(k);
                if (gen.qdeg() != qdeg) gens.remove(k);
                k--;
            }
        }
    }
    
}
