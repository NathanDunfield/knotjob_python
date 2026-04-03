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

package knotjob.homology.evenkhov.sinv;

import java.util.ArrayList;
import java.util.Iterator;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Diagram;
import knotjob.homology.Generator;
import knotjob.homology.evenkhov.EvenArrow;
import knotjob.homology.evenkhov.EvenCache;
import knotjob.homology.evenkhov.EvenComplex;
import knotjob.homology.evenkhov.EvenGenerator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class UnivComplex<R extends Ring<R>> extends EvenComplex<R> {
    
    private final R theH;
    
    public UnivComplex(int comp, R unt, boolean unr, boolean red, AbortInfo ab, 
            DialogWrap frm, R h) {
        super(comp, unt, unr, red, ab, frm);
        theH = h;
    }
    
    public UnivComplex(int crs, int[] ends, int hstart, int qstart, R h, boolean rev, 
            boolean ras, boolean unred, boolean red, R unt, DialogWrap frm, AbortInfo abt) {
        super(crs, ends, hstart, qstart, rev, ras, unred, red, unt, frm, abt);
        theH = h;
    }
    
    public void lastModification(UnivComplex<R> complex, String girth, boolean det) {
        ArrayList<Integer> pDots = new ArrayList<Integer>();
        ArrayList<Integer> pEndpts = new ArrayList<Integer>();
        ArrayList<Integer> nEndpts = new ArrayList<Integer>();
        for (int i : posEndpts) {
            pDots.add(i);
            pEndpts.add(i);
        }
        for (int i : negEndpts) nEndpts.add(i);
        for (Integer i : complex.posEndpts) {
            pDots.add(i);
            if (negEndpts.contains(i)) nEndpts.remove(i);
            else pEndpts.add(i);
        }
        for (Integer i : complex.negEndpts) {
            if (posEndpts.contains(i)) pEndpts.remove(i);
            else nEndpts.add(i);
        }
        frame.setLabelRight(girth, 1, false);
        for (Diagram dig : complex.cache.getDiagrams()) {
            for (int c : dig.circles) {
                int u = complex.cache.getPaths().get(c).get(0);
                if (!pDots.contains(u)) {
                    pDots.add(u);
                    complex.posEndpts.add(u);
                }
            }
        }
        EvenCache tensorCache = new EvenCache(pEndpts, nEndpts, pDots);
        EvenCache deloopCache = new EvenCache(pEndpts, nEndpts);
        counter = 0;
        getLastObjects(complex, pEndpts, nEndpts, tensorCache, deloopCache, det);
        posEndpts = pEndpts;
        negEndpts = nEndpts;
        cache = deloopCache; // needs to be deloopCache;
    }
    
    private void getLastObjects(UnivComplex<R> complex, ArrayList<Integer> pEndpts, 
            ArrayList<Integer> nEndpts, EvenCache tCache, EvenCache dCache, boolean det) {
        ArrayList<ArrayList<Generator<R>>> tobjs = new ArrayList<ArrayList<Generator<R>>>(generators.size()+complex.generators.size()-1);
        ArrayList<ArrayList<Generator<R>>> dobjs = new ArrayList<ArrayList<Generator<R>>>(generators.size()+complex.generators.size()-1);
        for (int i = 0; i < generators.size()+complex.generators.size()-1; i++) {
            ArrayList<Generator<R>> tobji = new ArrayList<Generator<R>>();
            tobjs.add(tobji);
            ArrayList<Generator<R>> dobji = new ArrayList<Generator<R>>();
            dobjs.add(dobji);
        }
        int[][] diagTrans = new int[cache.diagramSize()][2];
        ArrayList<Integer> ddigTrans = new ArrayList<Integer>();
        int i = generators.size()-1;
        int t = complex.generators.size();
        while (i >= -t-1) {
            if (det) frame.setLabelRight(String.valueOf(i+t+1), 3, false);
            if (i >= 0) createTensor(i, t, (EvenComplex<R>) complex, pEndpts, nEndpts, tCache, diagTrans, tobjs);
            if (i < generators.size()-1 && i > -t-1) 
                deloopObjects(i+t, tobjs, dobjs, tCache, dCache, ddigTrans);
            if (i < generators.size()-2) gaussEliminate(i+t+1, dobjs, dCache, det); 
            i--;
        }
        generators = dobjs; // will need to be dobjs later
    }

    @Override
    protected void deloopObjects(int i, ArrayList<ArrayList<Generator<R>>> tobjs, 
            ArrayList<ArrayList<Generator<R>>> dobjs, EvenCache tCache, EvenCache dCache, 
            ArrayList<Integer> ddigTrans) {
        ArrayList<Diagram> tDigs = tCache.getDiagrams();
        while (tDigs.size() > ddigTrans.size()) ddigTrans.add(-1);
        for (Iterator<Generator<R>> it = tobjs.get(i).iterator(); it.hasNext();) {
            EvenGenerator<R> oObj = (EvenGenerator<R>) it.next();
            if (abInf.isAborted()) return;
            int oldDigNr = oObj.getDiagram();
            Diagram oldDig = tCache.getDiagram(oldDigNr);
            int newDigNr = ddigTrans.get(oldDigNr);
            if (newDigNr == -1) newDigNr = newDiagNumber(oldDig,tCache,dCache,ddigTrans,oldDigNr);
            if (oldDig.circles.size() == 1) 
                deloopOneCircle(oObj, newDigNr, tCache, dCache, dobjs.get(i), ddigTrans, oldDig);
            else deloopTwoCircles(oObj, newDigNr, tCache, dCache, dobjs.get(i), ddigTrans, oldDig);
            frame.setLabelRight(""+counter, 2, false);
            for (Iterator<Arrow<R>> ot = oObj.getTopArrows().iterator(); ot.hasNext();) {
                EvenGenerator<R> nObj = (EvenGenerator<R>) ot.next().getTopGenerator();
                for (Iterator<Arrow<R>> ut = nObj.getBotArrows().iterator(); ut.hasNext();) {
                    EvenArrow<R> arr = (EvenArrow<R>) ut.next();
                    int diff = (arr.getTopGenerator().qdeg() - arr.getBotGenerator().qdeg())/2;
                    if (diff > 0) {
                        R newVal = unit;
                        for (int j = 0; j < diff; j++) newVal = newVal.multiply(theH);
                        arr.setValue(arr.getValue().multiply(newVal));
                    }
                }
            }
            oObj.clearBotArrow();
            if (i < tobjs.size()-1) tobjs.set(i+1,null);
        }
    }

    public boolean notInSmithForm() {
        for (ArrayList<Generator<R>> gens : generators) {
            for (Generator<R> gen : gens) {
                int arrs = gen.getBotArrows().size()+gen.getTopArrows().size();
                if (arrs > 1) return true;
            }
        }
        return false;
    }
    
    
    
}
