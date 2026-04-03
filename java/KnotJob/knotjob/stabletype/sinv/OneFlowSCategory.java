/*

Copyright (C) 2022 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.stabletype.sinv;

import java.util.ArrayList;
import java.util.Iterator;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.rings.Ring;
import knotjob.stabletype.FlowGenerator;
import knotjob.stabletype.ModOne;
import knotjob.stabletype.OneFlowCategory;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class OneFlowSCategory<R extends Ring<R>> extends OneFlowCategory<R> {
    
    private final int qdeg;
    
    public OneFlowSCategory(DialogWrap frm, AbortInfo abnf, R unt, R untt, R untf, int q) {
        super(frm, abnf, unt, untt, untf);
        qdeg = q;
    }
    
    public void checkQArrows() {
        boolean trouble = false;
        for (ArrayList<FlowGenerator<R>> gens : generators) {
            for (FlowGenerator<R> gn : gens) {
                FlowSGenerator<R> gen = (FlowSGenerator<R>) gn;
                ArrayList<FlowSGenerator<R>> tops = new ArrayList<FlowSGenerator<R>>();
                for (Arrow<R> arr : gen.getBotqArr()) {
                    if (!tops.contains((FlowSGenerator<R>)arr.getTopGenerator())) 
                        tops.add((FlowSGenerator<R>) arr.getTopGenerator());
                    else trouble = true;
                }
            }
        }
        System.out.println("Trouble "+trouble);
    }
    
    @Override
    protected void cancelArrow(Arrow<R> arr, int i) {
        super.cancelArrow(arr, i);
        FlowSGenerator<R> top = (FlowSGenerator<R>) arr.getTopGenerator();
        FlowSGenerator<R> bot = (FlowSGenerator<R>) arr.getBotGenerator();
        for (Arrow<R> ar : top.getBotqArr()) 
            ((FlowSGenerator<R>) ar.getTopGenerator()).getTopqArr().remove(ar);
        for (Arrow<R> ar : bot.getTopqArr())
            ((FlowSGenerator<R>) ar.getBotGenerator()).getBotqArr().remove(ar);
        for (Arrow<R> far : bot.getBotqArr()) { // zigzag
            FlowSGenerator<R> zTop = (FlowSGenerator<R>) far.getTopGenerator();
            for (Arrow<R> sar : top.getTopqArr()) {
                FlowSGenerator<R> zBot = (FlowSGenerator<R>) sar.getBotGenerator();
                Arrow<R> arz = arrowBetween(zBot, zTop);
                R val = arz.getValue();
                val = val.add(far.getValue().multiply(sar.getValue())); // we don't care about signs
                arz.setValue(val);
                if (val.isZero()) {
                    zTop.getTopqArr().remove(arz);
                    zBot.getBotqArr().remove(arz);
                }
            }
            zTop.getTopqArr().remove(far);
        } 
        for (Arrow<R> ar : top.getTopqArr()) 
            ((FlowSGenerator<R>) ar.getBotGenerator()).getBotqArr().remove(ar);
    }
    
    private Arrow<R> arrowBetween(FlowSGenerator<R> bot, FlowSGenerator<R> top) {
        boolean found = false;
        int i = 0;
        while (!found && i < bot.getBotqArr().size()) {
            Arrow<R> ar = bot.getBotqArr().get(i);
            if (ar.getTopGenerator() == top) found = true;
            else i++;
        }
        if (found) return bot.getBotqArr().get(i);
        Arrow<R> nAr = new Arrow<R>(bot, top, unit.getZero());
        bot.addBotqArr(nAr);
        top.addTopqArr(nAr);
        return nAr;
    }
    
    @Override
    protected void handleSlide(FlowGenerator<R> xGn, FlowGenerator<R> yGn, R val) {
        FlowSGenerator<R> xGen = (FlowSGenerator<R>) xGn;
        FlowSGenerator<R> yGen = (FlowSGenerator<R>) yGn;
        if (xGen.qdeg() == qdeg && !unitTwo.multiply(val).isZero()) moveModsIntoY(xGen, yGen);
        movePointsIntoY(xGen, yGen, val);
        moveQPointsIntoY(xGen, yGen, val);
        movePointsOutX(xGen, yGen, val);
        moveQPointsOutX(xGen, yGen, val);
        if (xGen.qdeg() == qdeg && !unitFour.multiply(val).isZero()) moveModsOutX(xGen, yGen, val);
        fixTheSigns(xGen);
    }
    
    private void moveQPointsIntoY(FlowSGenerator<R> xGen, FlowSGenerator<R> yGen, R val) {
        for (Arrow<R> arr : xGen.getBotqArr()) {
            boolean found = false;
            int i = 0;
            while (!found && i < ((FlowSGenerator<R>) arr.getTopGenerator()).getTopqArr().size()) {
                Arrow<R> ar = ((FlowSGenerator<R>) arr.getTopGenerator()).getTopqArr().get(i);
                if (ar.getBotGenerator() == yGen) found = true;
                else i++;
            }
            if (found) {
                Arrow<R> ar = ((FlowSGenerator<R>) arr.getTopGenerator()).getTopqArr().get(i);
                ar.addValue(arr.getValue().multiply(val.negate()));
                if (ar.getValue().isZero()) {
                    ((FlowSGenerator<R>) ar.getBotGenerator()).getBotqArr().remove(ar);
                    ((FlowSGenerator<R>) ar.getTopGenerator()).getTopqArr().remove(ar);
                }
            }
            else {
                Arrow<R> ar = new Arrow<R>(yGen, (FlowSGenerator<R>) arr.getTopGenerator(), 
                        arr.getValue().multiply(val.negate()));
                if (!ar.getValue().isZero()) {
                    yGen.addBotqArr(ar);
                    ((FlowSGenerator<R>) arr.getTopGenerator()).addTopqArr(ar);
                }
            }
        }
    }
    
    private void moveQPointsOutX(FlowSGenerator<R> xGen, FlowSGenerator<R> yGen, R val) {
        for (Arrow<R> ar : yGen.getTopqArr()) {
            boolean found = false;
            int i = 0;
            while (!found && i < xGen.getTopqArr().size()) {
                Arrow<R> axr = xGen.getTopqArr().get(i);
                if (axr.getBotGenerator() == ar.getBotGenerator()) found = true;
                else i++;
            }
            if (found) {
                Arrow<R> axr = xGen.getTopqArr().get(i);
                axr.addValue(val.multiply(ar.getValue())); 
                if (axr.getValue().isZero()) {
                    ((FlowSGenerator<R>) axr.getTopGenerator()).getTopqArr().remove(axr);
                    ((FlowSGenerator<R>) axr.getBotGenerator()).getBotqArr().remove(axr);
                }
            }
            else {
                if (!val.multiply(ar.getValue()).isZero()) {
                    Arrow<R> axr = new Arrow<R>((FlowSGenerator<R>) ar.getBotGenerator(), xGen,
                            val.multiply(ar.getValue())); 
                    xGen.getTopqArr().add(axr);
                    ((FlowSGenerator<R>) ar.getBotGenerator()).getBotqArr().add(axr);
                }
            }
        }
    }
    
    @Override
    protected void movePointsIntoY(FlowGenerator<R> xGen, FlowGenerator<R> yGen, R val) {
        int q = ((FlowSGenerator<R>) xGen).qdeg();
        for (Arrow<R> arr : xGen.getBotArrows()) {
            boolean caution = false;
            if (!unitFour.multiply(arr.getValue()).isZero() && unitTwo.multiply(arr.getValue()).isZero())
                caution = true;
            boolean found = false;
            int i = 0;
            R u = unitFour.getZero();
            while (!found && i < arr.getTopGenerator().getTopArrows().size()) {
                Arrow<R> ar = arr.getTopGenerator().getTopArrows().get(i);
                if (ar.getBotGenerator() == yGen) found = true;
                else i++;
            }
            if (found) {
                Arrow<R> ar = arr.getTopGenerator().getTopArrows().get(i);
                u = unitFour.multiply(ar.getValue());
                ar.addValue(arr.getValue().multiply(val.negate()));
                if (ar.getValue().isZero()) {
                    ar.getBotGenerator().getBotArrows().remove(ar);
                    ar.getTopGenerator().getTopArrows().remove(ar);
                }
            }
            else {
                Arrow<R> ar = new Arrow<R>(yGen, arr.getTopGenerator(), 
                        arr.getValue().multiply(val.negate()));
                if (!ar.getValue().isZero()) {
                    yGen.addBotArrow(ar);
                    arr.getTopGenerator().addTopArrow(ar);
                }
            }
            if (q == qdeg && !unitFour.multiply(val).isZero()) 
                for (Arrow<R> ar : yGen.getTopArrows()) 
                    dealWithMiddleMods(arr, ar, caution, u, val);
        }
    }
    
    public void simplify() {// throws away all objects of hom degree < -2 and > 1,
        int i = generators.size()-1;
        boolean found = false;
        while (!found && i >= 0) {
            if (generators.get(i).isEmpty()) generators.remove(i);
            else found = true;
            i--;
        }
        found = false;
        while (!found) {
            if (generators.get(0).isEmpty()) generators.remove(0);
            else found = true;
        }
        i = generators.size()-1;
        while (i >= 0) {
            if (!generators.get(i).isEmpty()) {
                FlowGenerator<R> gen = generators.get(i).get(0);
                if (gen.hdeg() > 1 || gen.hdeg() < -2) generators.remove(i);
                if (gen.hdeg() == 1) removeBotArrows(generators.get(i));
                if (gen.hdeg() == 0) removeBotMods(generators.get(i));
                if (gen.hdeg() == -1) removeTopMods(generators.get(i));
                if (gen.hdeg() == -2) removeTopArrows(generators.get(i));
            }
            i--;
        }
        i = generators.size()-1;// and turns the category over Z/2Z
        while (i >= 0) {
            ArrayList<FlowGenerator<R>> gens = generators.get(i);
            for (FlowGenerator<R> gn : gens) {
                modulizeArrows(gn);
            }
            i--;
        }
    }
    
    private void removeBotArrows(ArrayList<FlowGenerator<R>> gens) {
        for (FlowGenerator<R> gn : gens) {
            FlowSGenerator<R> gen = (FlowSGenerator<R>) gn;
            gen.getBotArrows().clear();
            gen.getBotqArr().clear();
            gen.getBotMod().clear(); // not just arrows, also mods
        }
    }
    
    private void removeTopArrows(ArrayList<FlowGenerator<R>> gens) {
        for (FlowGenerator<R> gn : gens) {
            FlowSGenerator<R> gen = (FlowSGenerator<R>) gn;
            gen.getTopArrows().clear();
            gen.getTopqArr().clear();
            gen.getTopMod().clear(); // not just arrows, also mods
        }
    }
    
    private void removeBotMods(ArrayList<FlowGenerator<R>> gens) {
        for (FlowGenerator<R> gn : gens) gn.getBotMod().clear();
    }
    
    private void removeTopMods(ArrayList<FlowGenerator<R>> gens) {
        for (FlowGenerator<R> gn : gens) gn.getTopMod().clear();
    }
    
    private void modulizeArrows(FlowGenerator<R> gn) {
        FlowSGenerator<R> gen = (FlowSGenerator<R>) gn;
        gen.getBotArrows().clear();
        gen.getTopArrows().clear();
        /*int j = gen.getBotArrows().size()-1;
        while (j >= 0) {
            Arrow<R> arr = gen.getBotArrows().get(j);
            arr.setValue(unitTwo.multiply(arr.getValue()));
            if (arr.getValue().isZero()) {
                gen.getBotArrows().remove(arr);
                arr.getTopGenerator().getTopArrows().remove(arr);
            }
            j--;
        }// */
        int j = gen.getBotqArr().size()-1;
        while (j >= 0) {
            Arrow<R> arr = gen.getBotqArr().get(j);
            arr.setValue(unitTwo.multiply(arr.getValue()));
            if (arr.getValue().isZero()) {
                gen.getBotqArr().remove(arr);
                ((FlowSGenerator<R>) arr.getTopGenerator()).getTopqArr().remove(arr);
            }
            j--;
        }
    }
    
    public int obtainRefinement() {
        int qmax = maximalQ();
        int qmin = minimalQ(qmax);
        cancelTopGenerators(qmax);
        ArrayList<FlowSGenerator<R>> possCocycles = getPossCocycles();
        if (possCocycles.isEmpty()) return 0;
        ArrayList<FlowSGenerator<R>> boundaries = getBoundaries(possCocycles);
        ArrayList<ArrayList<R>> matrix = fillMatrix(boundaries, possCocycles);
        improveMatrix(matrix);
        if (matrix.isEmpty()) matrix = zeroMatrix(1, possCocycles.size());
        ArrayList<ArrayList<Integer>> cocycles = getCocycles(matrix);
        ArrayList<ArrayList<FlowSGenerator<R>>> coObjects = getCoObjects(cocycles, possCocycles);
        modOutObjects(qdeg+1);
        cancelFromTop(coObjects);
        cancelFromBot(qmin, qmax, coObjects);
        if (coObjects.isEmpty()) return 0;
        return 2;
    }
    
    private void improveMatrix(ArrayList<ArrayList<R>> matrix) {
        boolean done = false;
        int[] corner = new int[2];
        ArrayList<Integer> avoid = new ArrayList<Integer>();
        while (!done) {
            corner = newEntry(matrix,avoid,corner[1]);
            if (corner[0] == -1) done = true;
            else {
                for (int j = 0; j < matrix.size(); j++) {
                    if (j != corner[0] & matrix.get(j).get(corner[1]).isInvertible()) 
                        addRow(matrix,corner[0],j);
                }
            }
            corner[1]++;
        }
    }
    
    private int[] newEntry(ArrayList<ArrayList<R>> matrix, ArrayList<Integer> avoid, int b) {
        int[] entry = new int[2];
        int i = 0;
        while (avoid.contains(i)) i++;
        if (i >= matrix.size()) {
            entry[0] = -1;
            return entry;
        }
        int a = i;
        int j = b;
        boolean found = false;
        while (!found & j < matrix.get(0).size()) {
            if (!matrix.get(i).get(j).isZero()) found = true;
            else i++;
            while(avoid.contains(i)) i++;
            if (i >= matrix.size()) {
                i = a;
                j++;
            }
        }
        if (!found) entry[0] = -1;
        else {
            entry[0] = i;
            entry[1] = j;
            avoid.add(i);
        }
        return entry;
    }

    private void addRow(ArrayList<ArrayList<R>> matrix, int a, int b) {
        for (int i = 0; i < matrix.get(a).size(); i++) 
            matrix.get(b).set(i, matrix.get(b).get(i).add(matrix.get(a).get(i)));
    }
    
    private ArrayList<ArrayList<Integer>> getCocycles(ArrayList<ArrayList<R>> matrix) {
        ArrayList<ArrayList<Integer>> cocycles = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> freeVariables = new ArrayList<Integer>();
        for (int j = 0; j < matrix.get(0).size(); j++) {
            boolean found = false;
            int i = 0;
            while (!found & i < matrix.size()) {
                if (!matrix.get(i).get(j).isZero()) found = true;
                else i++;
            }
            if (!found) freeVariables.add(j);
            else {
                found = false;
                int k = 0;
                while (k < j & !found) {
                    if (!matrix.get(i).get(k).isZero()) found = true;
                    else k++;
                }
                if (found) freeVariables.add(j);
            }
        }
        for (int j : freeVariables) {
            ArrayList<Integer> cocycle = new ArrayList<Integer>();
            cocycle.add(j);
            for (int i = 0; i < matrix.size(); i++) {
                if (!matrix.get(i).get(j).isZero()) {
                    int k = 0;
                    boolean found = false;
                    while (!found) {
                        if (!matrix.get(i).get(k).isZero()) found = true;
                        else k++;
                    }
                    cocycle.add(k);
                }
            }
            cocycles.add(cocycle);
        }
        return cocycles;
    }
    
    private ArrayList<ArrayList<FlowSGenerator<R>>> getCoObjects(
            ArrayList<ArrayList<Integer>> cocycles, ArrayList<FlowSGenerator<R>> posCocycles) {
        ArrayList<ArrayList<FlowSGenerator<R>>> objs = new ArrayList<ArrayList<FlowSGenerator<R>>>();
        for (ArrayList<Integer> ints : cocycles) {
            ArrayList<FlowSGenerator<R>> cocs = new ArrayList<FlowSGenerator<R>>();
            for (int y : ints) cocs.add(posCocycles.get(y));
            objs.add(cocs);
        }
        return objs;
    }
    
    private ArrayList<ArrayList<R>> zeroMatrix(int i, int j) {
        ArrayList<ArrayList<R>> matrix = new ArrayList<ArrayList<R>>();
        for (int y = 0; y < i; y++) matrix.add(new ArrayList<R>(j));
        for (int y = 0; y < i; y++) {
            for (int z = 0; z < j; z++) matrix.get(y).add(unitTwo.getZero());
        }
        return matrix;
    }
    
    private ArrayList<ArrayList<R>> fillMatrix(ArrayList<FlowSGenerator<R>> bnds, 
            ArrayList<FlowSGenerator<R>> cycs) {
        ArrayList<ArrayList<R>> matrix = zeroMatrix(bnds.size(), cycs.size());
        for (int i = 0; i < cycs.size(); i++) {
            for (Arrow<R> arr : cycs.get(i).getBotqArr()) {
                if (bnds.contains((FlowSGenerator<R>) arr.getTopGenerator())) {
                    int j = bnds.indexOf(arr.getTopGenerator());
                    matrix.get(j).set(i, unitTwo);
                }
            }
        }
        return matrix;
    }
    
    private ArrayList<FlowSGenerator<R>> getBoundaries(ArrayList<FlowSGenerator<R>> cycles) {
        ArrayList<FlowSGenerator<R>> boundaries = new ArrayList<FlowSGenerator<R>>();
        if (generators.size() == 3) return boundaries;
        for (Iterator<FlowSGenerator<R>> it = cycles.iterator(); it.hasNext();) {
            FlowSGenerator<R> gen = it.next();
            for (Arrow<R> arr : gen.getBotqArr()) {
                if (!boundaries.contains((FlowSGenerator<R>) arr.getTopGenerator())) 
                    boundaries.add((FlowSGenerator<R>) arr.getTopGenerator());
            }
        }
        return boundaries;
    }
    
    private ArrayList<FlowSGenerator<R>> getPossCocycles() {
        ArrayList<FlowSGenerator<R>> possCocycles = new ArrayList<FlowSGenerator<R>>();
        for (Iterator<FlowGenerator<R>> it = generators.get(2).iterator(); it.hasNext();) {
            FlowSGenerator<R> gen = (FlowSGenerator<R>) it.next();
            if (!gen.getTopMod().isEmpty()) possCocycles.add(gen);
        }
        return possCocycles;
    }
    
    private int maximalQ() {
        int qmax = ((FlowSGenerator<R>) generators.get(2).get(0)).qdeg();
        for (int i = 1; i < generators.get(2).size(); i++) {
            int q = ((FlowSGenerator<R>) generators.get(2).get(i)).qdeg();
            if (q > qmax) qmax = q;
        }
        return qmax;
    }
    
    private int minimalQ(int qmax) {
        int qmin = qmax;
        for (int i = 0; i < generators.get(1).size(); i++) {
            int q = ((FlowSGenerator<R>) generators.get(1).get(i)).qdeg();
            if (q < qmin) qmin = q;
        }
        return qmin;
    }
    
    private void cancelTopGenerators(int qmax) {
        int qrun = qmax;
        while (qrun > qdeg) {
            int i = generators.get(2).size()-1;
            while (i >= 0) {
                FlowSGenerator<R> gen = (FlowSGenerator<R>) generators.get(2).get(i);
                if (gen.qdeg() == qrun && !gen.getBotqArr().isEmpty()) {
                    cancelGenerator(gen);
                }
                i--;
            }
            qrun = qrun - 2;
        }
    }
    
    private void cancelGenerator(FlowSGenerator<R> bGen) {
        Arrow<R> arr = bGen.getBotqArr().get(0);
        FlowSGenerator<R> tGen = (FlowSGenerator<R>) arr.getTopGenerator();
        bGen.getBotqArr().remove(arr);
        tGen.getTopqArr().remove(arr);
        for (Arrow<R> far : tGen.getTopqArr()) {
            FlowSGenerator<R> fGen = (FlowSGenerator<R>) far.getBotGenerator();
            for (Arrow<R> sar : bGen.getBotqArr()) {
                FlowSGenerator<R> sGen = (FlowSGenerator<R>) sar.getTopGenerator();
                Arrow<R> nar = arrowBetween(fGen, sGen);
                if (nar.getValue().isZero()) {
                    nar.setValue(unitTwo);
                }
                else {
                    fGen.getBotqArr().remove(nar);
                    sGen.getTopqArr().remove(nar);
                }
            }
        }
        generators.get(2).remove(bGen);
        generators.get(3).remove(tGen);
        for (Arrow<R> ar : tGen.getTopqArr()) 
            ((FlowSGenerator<R>) ar.getBotGenerator()).getBotqArr().remove(ar);
        for (Arrow<R> ar : bGen.getBotqArr()) 
            ((FlowSGenerator<R>) ar.getTopGenerator()).getTopqArr().remove(ar);
        for (Arrow<R> ar : bGen.getTopqArr()) 
            ((FlowSGenerator<R>) ar.getBotGenerator()).getBotqArr().remove(ar);
    }
    
    private void modOutObjects(int q) {
        int v = 0;
        while ( v < generators.size()) {
            ArrayList<FlowGenerator<R>> objs = generators.get(v);
            int t = 0;
            while (t < objs.size()) {
                FlowSGenerator<R> obj = (FlowSGenerator<R>) objs.get(t);
                if (obj.qdeg() > q) objs.remove(obj);
                else {
                    t++;
                    int s = 0;
                    while (s < obj.getBotqArr().size()) {
                        Arrow<R> mor = obj.getBotqArr().get(s);
                        if (((FlowSGenerator) mor.getTopGenerator()).qdeg() > q) 
                            obj.getBotqArr().remove(mor);
                        else s++;
                    }
                }
            }
            v++;
        }
    }
    
    private void cancelFromTop(ArrayList<ArrayList<FlowSGenerator<R>>> cocycles) {
        boolean found;
        ArrayList<FlowGenerator<R>> objs = generators.get(2);
        int t = 0;
        while (t < objs.size()) {
            FlowSGenerator<R> bObj = (FlowSGenerator<R>) objs.get(t);
            int b = 0;
            found = false;
            while (b < bObj.getBotArrows().size() & !found) {
                Arrow<R> mor = bObj.getBotqArr().get(b);
                if (mor.getValue().isInvertible()) found = true;
                else b++;
            }
            if (found) {
                for (ArrayList<FlowSGenerator<R>> cocycle : cocycles) {
                    if (cocycle.contains(bObj)) cocycle.remove(bObj);
                }
                int y = 0;
                while (y < cocycles.size()) {
                    if (cocycles.get(y).isEmpty()) cocycles.remove(y);
                    else y++;
                }
                cancelMorObj(bObj.getBotqArr().get(b));
                t--;
                if (t>=0) t--;
            }
            t++;
        }
    }
    
    private void cancelFromBot(int qmin, int qmax, ArrayList<ArrayList<FlowSGenerator<R>>> cocycles) {
        int qrun = qmin;
        ArrayList<FlowGenerator<R>> objs = generators.get(2);
        while (qrun <= qmax) {
            int t = 0;
            while (t < objs.size()) {
                FlowSGenerator<R> tObj = (FlowSGenerator<R>) objs.get(t);
                if (tObj.qdeg() == qrun) {
                    int b = 0;
                    boolean fund = false;
                    while (b < tObj.getTopqArr().size() & !fund) {
                        Arrow<R> mor = tObj.getTopqArr().get(b);
                        if (mor.getValue().isInvertible()) fund = true;
                        else b++;
                    }
                    if (fund) {
                        for (ArrayList<FlowSGenerator<R>> cocycle : cocycles) {
                            if (cocycle.contains(tObj)) {
                                FlowSGenerator<R> bObj = (FlowSGenerator<R>) tObj.getTopqArr().get(b).getBotGenerator();
                                for (Iterator<Arrow<R>> it = bObj.getBotqArr().iterator(); it.hasNext();) {
                                    Arrow<R> mor = it.next();
                                    if (mor.getValue().isInvertible()) {
                                        if (cocycle.contains((FlowSGenerator<R>) mor.getTopGenerator())) 
                                            cocycle.remove((FlowSGenerator<R>) mor.getTopGenerator());
                                        else cocycle.add((FlowSGenerator<R>) mor.getTopGenerator());
                                    }
                                }
                            }
                        }
                        int y = 0;
                        while (y < cocycles.size()) {
                            if (cocycles.get(y).isEmpty()) cocycles.remove(y);
                            else y++;
                        }
                        cancelMorObj(tObj.getTopqArr().get(b));
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
        int h = 2;
        FlowSGenerator<R> bObj = (FlowSGenerator<R>) mor.getBotGenerator();
        FlowSGenerator<R> tObj = (FlowSGenerator<R>) mor.getTopGenerator();
        bObj.getBotqArr().remove(mor);
        tObj.getTopqArr().remove(mor);
        for (Arrow<R> mr : tObj.getBotqArr()) 
            ((FlowSGenerator<R>) mr.getTopGenerator()).getTopqArr().remove(mr);
        for (Arrow<R> mr : bObj.getTopqArr()) 
            ((FlowSGenerator<R>) mr.getBotGenerator()).getBotqArr().remove(mr);
        generators.get(h+bObj.hdeg()).remove(bObj);
        generators.get(h+tObj.hdeg()).remove(tObj);
        for (Arrow<R> far : tObj.getTopqArr()) {
            FlowSGenerator<R> fGen = (FlowSGenerator<R>) far.getBotGenerator();
            
            if (fGen == bObj) System.out.println("Shouldnt");
            
            for (Arrow<R> sar : bObj.getBotqArr()) {
                FlowSGenerator<R> sGen = (FlowSGenerator<R>) sar.getTopGenerator();
                Arrow<R> nar = arrowBetween(fGen, sGen);
                if (nar.getValue().isZero()) {
                    nar.setValue(unitTwo);
                }
                else {
                    fGen.getBotqArr().remove(nar);
                    sGen.getTopqArr().remove(nar);
                }
            }
        }
        for (Arrow<R> mr : tObj.getTopqArr()) 
            ((FlowSGenerator<R>) mr.getBotGenerator()).getBotqArr().remove(mr);
        for (Arrow<R> mr : bObj.getBotqArr()) 
            ((FlowSGenerator<R>) mr.getTopGenerator()).getTopqArr().remove(mr);
    }
    
    // stuff for changify
    
    @Override
    protected void isolateEtaBottom(ModOne<R> eta, int i) {
        FlowGenerator<R> bGen = eta.getBotGenerator();
        FlowGenerator<R> tGen = eta.getTopGenerator();
        while (bGen.getBotMod().size() > 1) {
            ModOne<R> oEta = otherBotEta(bGen, eta);
            handleSlide(oEta.getTopGenerator(), tGen, unitTwo);
        }
    }
    
    @Override
    protected void isolateEtaTop(ModOne<R> eta, int i) {
        FlowGenerator<R> bGen = eta.getBotGenerator();
        FlowGenerator<R> tGen = eta.getTopGenerator();
        while (tGen.getTopMod().size() > 1) {
            ModOne<R> oEta = otherTopEta(tGen, eta);
            handleSlide(bGen, oEta.getBotGenerator(), unitTwo);
        }
    }
    
}
