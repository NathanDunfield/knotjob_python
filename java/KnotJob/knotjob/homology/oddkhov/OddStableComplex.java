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

package knotjob.homology.oddkhov;

import java.util.ArrayList;
import java.util.Iterator;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Cache;
import knotjob.homology.Diagram;
import knotjob.homology.Generator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class OddStableComplex<R extends Ring<R>> extends OddComplex<R> {
    
    private final ArrayList<Integer> relQs;
    public boolean last;
    public boolean lastCancel;
    public int noCancelq;
    
    public OddStableComplex(int crs, int[] ends, int hstart, int qstart, boolean rev, R unt, 
            DialogWrap frm, AbortInfo abt, boolean ras, ArrayList<Integer> qs) {
        super(unt, ras, abt, frm);
        int change = 0;
        if (!rev) change = 1;
        posEndpts.add(ends[0+change]);
        posEndpts.add(ends[2+change]);
        negEndpts.add(ends[1-change]);
        negEndpts.add(ends[3-change]);
        cache = new Cache(ends, change);
        generators = new ArrayList<ArrayList<Generator<R>>>(2);
        int std = 1;
        if (crs < 0) std = 0;
        OddStableGenerator<R> bGen = new OddStableGenerator<R>(1-std, hstart, qstart, false);
        ArrayList<Generator<R>> hObjs = new ArrayList<Generator<R>>(1);
        hObjs.add(bGen);
        generators.add(hObjs);
        OddStableGenerator<R> tGen = new OddStableGenerator<R>(std, hstart+1, qstart+1, true);
        hObjs = new ArrayList<Generator<R>>(1);
        hObjs.add(tGen);
        generators.add(hObjs);// objects have been created
        boolean turn = rev;
        int[] fp = new int [] { cache.getPath(1, 0), cache.getPath(1, 1) };
        int[] sp = new int [] { cache.getPath(0, 0), cache.getPath(0, 1) };
        if (crs < 0) {
            fp = new int [] { cache.getPath(3, 0), cache.getPath(3, 1) };
            sp = new int [] { cache.getPath(2, 0), cache.getPath(2, 1) };
            turn = !rev;
        }
        Chronology<R> surgery = new Chronology<R>(unit, fp, sp, std, turn);
        OddArrow<R> arrow = new OddArrow<R>(bGen, tGen, surgery);
        bGen.addBotArrow(arrow);
        tGen.addTopArrow(arrow);
        relQs = qs;
    }
    
    public OddStableComplex(int comp, R unt, AbortInfo abInf, DialogWrap frm,
            boolean ras) { // should only be used with comp = 0 or 1
        super(comp, unt, abInf, frm, ras);
        throw new UnsupportedOperationException("Not supported yet.");
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
                ((OddStableGenerator<R>) ((ArrayList<Generator<R>>) generators.get(i)).get(j)).output(nextLev);
            }
        }
        System.out.println();
        System.out.println();
    }
    
    public void modifyComplex(OddStableComplex<R> complex, String girth, boolean det) {
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
        for (int i = 0; i < complex.diagramSize(); i++) {
            Diagram dig = complex.getDiagram(i);
            for (int c : dig.circles) {
                int u = complex.getPaths(c,0);
                if (!pDots.contains(u)) {
                    pDots.add(u);
                    complex.posEndpts.add(u);
                }
            }
        }
        closureDiagrams.remove(0);
        nextClosurePaths = setClosurePaths(pEndpts, nEndpts);
        Cache tensorCache = new Cache(pDots);
        Cache deloopCache = new Cache(pEndpts);
        counter = 0;
        getNewObjects(complex, pEndpts, nEndpts, tensorCache, deloopCache, det);
        if (!rasmus) removeMorphisms(pEndpts.size()*2);
        posEndpts = pEndpts;
        negEndpts = nEndpts;
        closurePaths = nextClosurePaths;
        setClosureDiagram(closurePaths);
    }
    
    protected void getNewObjects(OddStableComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, Cache tCache, Cache dCache,
            boolean det) {
        ArrayList<ArrayList<Generator<R>>> tobjs = new ArrayList<ArrayList<Generator<R>>>(generators.size()+complex.generators.size()-1);
        ArrayList<ArrayList<Generator<R>>> dobjs = new ArrayList<ArrayList<Generator<R>>>(generators.size()+complex.generators.size()-1);
        for (int i = 0; i < generators.size()+complex.generators.size()-1; i++) {
            tobjs.add(new ArrayList<Generator<R>>());
            dobjs.add(new ArrayList<Generator<R>>());
        }
        int[][] diagTrans = new int[cache.diagramSize()][2];
        ArrayList<Integer> ddigTrans = new ArrayList<Integer>();
        int i = generators.size()-1;
        int t = complex.generators.size();
        while (i >= -t-1) {
            if (det) frame.setLabelRight(String.valueOf(i+t+1), 3, false);
            if (i >= 0) createCone(i, t, complex, pEndpts, nEndpts, tCache, diagTrans, tobjs);
            if (i < generators.size()-1 && i > -t-1) deloopGens(i+t, tobjs, dobjs, tCache, dCache, ddigTrans);
            /*if (!debug && i < generators.size()-2) {
                boolean cancel = true;
                while (cancel) cancel = gaussEliminate(i+t+1, dobjs, dCache, det);// we cancel as long as we can
            }// */
            if (lastCancel && i < generators.size()-2) gaussEliminate(i+t+1, dobjs, dCache, det);
            i--;
        }
        cache = dCache;
        generators = dobjs;
    }
    
    private void createCone(int i, int t, OddStableComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, 
            Cache tCache, int[][] diagTrans, ArrayList<ArrayList<Generator<R>>> tobjs) {
        ArrayList<Generator<R>> gens = generators.get(i);
        for (int l = 0; l < gens.size(); l++) {
            if (abInf.isAborted()) return;
            OddStableGenerator<R> fGen = (OddStableGenerator<R>) gens.get(l);
            ArrayList<Generator<R>> nGens = new ArrayList<Generator<R>>(t);
            for (int k = t-1; k >= 0; k--) {
                OddStableGenerator<R> sGen = (OddStableGenerator<R>) complex.generators.get(k).get(0);
                ArrayList<Boolean> clone = fGen.clonePosition();
                clone.add(sGen.getPosition(0));
                int dnum = getDiagNumber(fGen.getDiagram(), sGen.getDiagram(), complex, pEndpts, nEndpts, tCache, diagTrans);
                OddStableGenerator<R> nGen = new OddStableGenerator<R>(dnum, fGen.hdeg()+sGen.hdeg(), 
                        fGen.qdeg()+sGen.qdeg(), clone);
                tobjs.get(i+k).add(nGen);
                nGens.add(0, nGen);
                for (Arrow<R> arr : sGen.getBotArrows()) { // there is either one or no arrow
                    OddStableGenerator<R> ntGen = (OddStableGenerator<R>) nGens.get(1);
                    Chronology<R> oldChr = ((OddArrow<R>) arr).getChronology(0);
                    OddSurgery oldSur = oldChr.getSurgery(0);
                    Chronology<R> nChr = new Chronology<R>(oldChr.getValue(), oldSur.getFPath(), oldSur.getSPath(), ntGen.getDiagram(), 
                            oldSur.getTurn());
                    OddArrow<R> nArrow = new OddArrow<R>(nGen, ntGen, nChr);
                    nGen.addBotArrow(nArrow);
                }
            }
            fGen.clearTopArr();
            OddArrow<R> pointer = new OddArrow<R>(fGen,(OddStableGenerator<R>) nGens.get(0)); 
            fGen.addTopArrow(pointer);
            createArrowsF(fGen, complex, pEndpts, nEndpts, tCache, diagTrans);
            createArrowsG(fGen, complex, pEndpts, nEndpts, tCache, diagTrans);
            fGen.clearBotArr();
            counter = counter + t;
            frame.setLabelRight(""+counter, 2, false);
        }
        if (i < generators.size()-1) generators.set(i+1, null); // throwing away old objects of hom degree i+1
    }
    
    @Override
    protected void deloopGens(int i, ArrayList<ArrayList<Generator<R>>> tobjs, ArrayList<ArrayList<Generator<R>>> dobjs, 
            Cache tCache, Cache dCache, ArrayList<Integer> ddigTrans) {
        while (tCache.diagramSize() > ddigTrans.size()) ddigTrans.add(-1);
        for (Iterator<Generator<R>> it = tobjs.get(i).iterator(); it.hasNext();) {
            OddStableGenerator<R> oGen = (OddStableGenerator<R>) it.next();
            if (abInf.isAborted()) return;
            int oldDigNr = oGen.getDiagram();
            Diagram oldDig = tCache.getDiagram(oldDigNr);
            int newDigNr = ddigTrans.get(oldDigNr);
            if (newDigNr == -1) newDigNr = newDiagNumber(oldDig, tCache, dCache, ddigTrans, oldDigNr);
            if (oldDig.circles.isEmpty()) noDeloop(oGen, newDigNr, tCache, dCache, dobjs.get(i), ddigTrans);
            else {
                if (oldDig.circles.size() == 1) deloopOneCircle(oGen, newDigNr, tCache, dCache, dobjs.get(i), ddigTrans);
                else deloopTwoCircles(oGen, newDigNr, tCache, dCache, dobjs.get(i), ddigTrans);
            }
            frame.setLabelRight(""+counter, 2, false);
            oGen.clearBotArr();
            if (i < tobjs.size()-1) tobjs.set(i+1,null);
        }
    }
    
    private void noDeloop( OddStableGenerator<R> oGen, int ndig, Cache tCache, Cache dCache, ArrayList<Generator<R>> dobs, 
            ArrayList<Integer> ddigTrans) {
        OddStableGenerator<R> nGen = new OddStableGenerator<R>(ndig, oGen.hdeg(), oGen.qdeg(), 
                oGen.getPosition());
        Chronology<R> cchr = new Chronology<R>(unit, new ArrayList<Integer>());
        OddArrow<R> carr = new OddArrow<R>(oGen, nGen, cchr);
        oGen.addTopArrow(carr);
        createNewArrows(nGen, oGen, cchr, tCache, dCache, ddigTrans);
        dobs.add(nGen);
    }
    
    private void deloopOneCircle(OddStableGenerator<R> oGen, int newDigNr, Cache tCache, Cache dCache, ArrayList<Generator<R>> dobjs,
            ArrayList<Integer> ddigTrans) {
        ArrayList<Boolean> pos = oGen.getPosition();
        OddStableGenerator<R> nGenp = new OddStableGenerator<R>(newDigNr, oGen.hdeg(),
                oGen.qdeg()+1, pos);
        OddStableGenerator<R> nGenm = new OddStableGenerator<R>(newDigNr,oGen.hdeg(), 
                oGen.qdeg()-1, pos);
        boolean keepnGenp = !last | relQs.contains(nGenp.qdeg());
        boolean keepnGenm = !last | relQs.contains(nGenm.qdeg());
        int c = tCache.getDiagram(oGen.getDiagram()).circles.get(0);
        int dot = dotOfCircle(tCache.getPaths(c), tCache.getPts());
        Chronology<R> cchr = new Chronology<R>(dot, unit);
        Chronology<R> dchr = new Chronology<R>(unit, new ArrayList<Integer>(0));
        OddArrow<R> carr = new OddArrow<R>(oGen, nGenp, cchr);
        OddArrow<R> darr = new OddArrow<R>(oGen, nGenm, dchr);
        if (keepnGenp) oGen.addTopArrow(carr);
        if (keepnGenm) oGen.addTopArrow(darr);
        if (rasmus) {
            Chronology<R> echr = new Chronology<R>(unit.negate(), new ArrayList<Integer>(0));
            carr.addChronology(echr);
        }
        if (keepnGenp) createNewArrows(nGenp, oGen, dchr, tCache, dCache, ddigTrans);
        if (keepnGenm) createNewArrows(nGenm, oGen, cchr, tCache, dCache, ddigTrans);
        if (keepnGenp) dobjs.add(nGenp);
        if (keepnGenm) dobjs.add(nGenm);
        counter = counter + 1;
    }

    private void deloopTwoCircles(OddStableGenerator<R> oGen, int newDigNr, Cache tCache, Cache dCache, ArrayList<Generator<R>> dobjs,
            ArrayList<Integer> ddigTrans) {
        ArrayList<Boolean> pos = oGen.getPosition();
        OddStableGenerator<R> nGenpp = new OddStableGenerator<R>(newDigNr, oGen.hdeg(), 
                oGen.qdeg()+2, pos);
        OddStableGenerator<R> nGenpm = new OddStableGenerator<R>(newDigNr, oGen.hdeg(), 
                oGen.qdeg(), pos);
        OddStableGenerator<R> nGenmp = new OddStableGenerator<R>(newDigNr, oGen.hdeg(), 
                oGen.qdeg(), pos);
        OddStableGenerator<R> nGenmm = new OddStableGenerator<R>(newDigNr, oGen.hdeg(), 
                oGen.qdeg()-2, pos);
        boolean keepnGenpp = !last | relQs.contains(nGenpp.qdeg());
        boolean keepnGenpm = !last | relQs.contains(nGenpm.qdeg());
        boolean keepnGenmp = !last | relQs.contains(nGenmp.qdeg());
        boolean keepnGenmm = !last | relQs.contains(nGenmm.qdeg());
        int cOne = tCache.getDiagram(oGen.getDiagram()).circles.get(0);
        int dOne = dotOfCircle(tCache.getPaths(cOne), tCache.getPts());
        int cTwo = tCache.getDiagram(oGen.getDiagram()).circles.get(1);
        int dTwo = dotOfCircle(tCache.getPaths(cTwo), tCache.getPts());
        ArrayList<Integer> bDots = new ArrayList<Integer>(2);
        bDots.add(dOne);
        bDots.add(dTwo);
        Chronology<R> cchr = new Chronology<R>(unit, bDots);
        Chronology<R> dchr = new Chronology<R>(dOne, unit);
        Chronology<R> echr = new Chronology<R>(dTwo, unit);
        Chronology<R> fchr = new Chronology<R>(unit, new ArrayList<Integer>(0));
        OddArrow<R> carr = new OddArrow<R>(oGen, nGenpp, cchr);
        OddArrow<R> darr = new OddArrow<R>(oGen, nGenpm, dchr);
        OddArrow<R> earr = new OddArrow<R>(oGen, nGenmp, echr);
        OddArrow<R> farr = new OddArrow<R>(oGen, nGenmm, fchr);
        if (keepnGenpp) oGen.addTopArrow(carr);
        if (keepnGenpm) oGen.addTopArrow(darr);
        if (keepnGenmp) oGen.addTopArrow(earr);
        if (keepnGenmm) oGen.addTopArrow(farr);
        if (rasmus) {
            dchr = new Chronology<R>(dOne, unit.negate());
            echr = new Chronology<R>(dTwo, unit.negate());
            carr.addChronology(dchr);
            carr.addChronology(echr);
            carr.addChronology(fchr);
            fchr = new Chronology<R>(unit.negate(), new ArrayList<Integer>(0));
            darr.addChronology(fchr);
            earr.addChronology(fchr);
        }
        if (keepnGenpp) createNewArrows(nGenpp, oGen, farr.getChronology(0), tCache, dCache, ddigTrans);
        if (keepnGenpm) createNewArrows(nGenpm, oGen, earr.getChronology(0), tCache, dCache, ddigTrans);
        if (keepnGenmp) createNewArrows(nGenmp, oGen, darr.getChronology(0), tCache, dCache, ddigTrans);
        if (keepnGenmm) createNewArrows(nGenmm, oGen, carr.getChronology(0), tCache, dCache, ddigTrans);
        if (keepnGenpp) dobjs.add(nGenpp);
        if (keepnGenpm) dobjs.add(nGenpm);
        if (keepnGenmp) dobjs.add(nGenmp);
        if (keepnGenmm) dobjs.add(nGenmm);
        counter = counter + 3;
    }
    
    public void throwAway(ArrayList<Integer> qs) {
        for (ArrayList<Generator<R>> gens : generators) {
            int i = gens.size()-1;
            while (i >= 0) {
                OddStableGenerator<R> gen = (OddStableGenerator<R>) gens.get(i);
                if (!qs.contains(gen.qdeg())) gens.remove(gen);
                i--;
            }
        }
    }
    
    @Override
    protected boolean canCancel(OddArrow<R> arr) {
        if (arr.getBotGenerator().qdeg() == noCancelq) return false;
        if (arr.getBotGenerator().qdeg() != arr.getTopGenerator().qdeg()) return false;
        return arr.getChronology(0).getValue().isInvertible();
    }
    
}
