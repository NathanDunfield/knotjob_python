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

package knotjob.homology.evenkhov;

import java.util.ArrayList;
import java.util.Iterator;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Diagram;
import knotjob.homology.Generator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class EvenStableComplex<R extends Ring<R>> extends EvenComplex<R> {
    
    private final ArrayList<Integer> relQs;
    public boolean last;
    public boolean lastCancel;
    public int noCancelq;
    
    public EvenStableComplex(int crs, int[] ends, int hstart, int qstart, boolean rev, 
            boolean ras, boolean unred, boolean red, R unt, DialogWrap frm, AbortInfo abt,
            ArrayList<Integer> qs) {
        super(unt, ras, abt, frm);
        int change = 0;
        if (!rev) change = 1;
        posEndpts.add(ends[0+change]);
        posEndpts.add(ends[2+change]);
        negEndpts.add(ends[1-change]);
        negEndpts.add(ends[3-change]);
        cache = new EvenCache(ends,change);
        int r = Math.abs(crs);
        int std = 1;
        if (crs < 0) std = 0;
        for (int i = 0; i <= r; i++) {
            ArrayList<Generator<R>> hObjs = new ArrayList<Generator<R>>(1);
            int s = std;
            if (i == 0 && crs > 0) s = 1-s;
            if (i != r && crs < 0) s = 1-s;
            int qdeg = qstart+i;
            if (crs > 0 && i >= 2) qdeg = qdeg + i - 1;
            if (crs < 0 && i < r - 1) qdeg = qdeg - (r - i - 1);
            hObjs.add(new EvenStableGenerator<R>(s, hstart+i, qdeg, i));
            generators.add(hObjs);
        } // objects have been created
        Cobordism<R> surgery = new Cobordism<R>(unt, 0, 1);
        Cobordism<R> dot1 = new Cobordism<R>(unt, 1, 0);
        Cobordism<R> dot2 = new Cobordism<R>(unt, 2, 0);
        Cobordism<R> dotn = new Cobordism<R>(unt.negate(), 2, 0);
        Cobordism<R> mid = new Cobordism<R>(unt.negate(), 0, 0); 
        for (int i = 0; i < r; i++) {
            Cobordism<R> scob = dot1;
            Cobordism<R> extr = dot2;
            int addr = 0;
            if (crs < 0) addr = 1+crs;
            if (i == 0 && crs > 0) {
                scob = surgery;
                extr = null;
            }
            if (i == r-1 && crs < 0) {
                scob = surgery;
                extr = null;
            }
            EvenStableGenerator<R> bObj = (EvenStableGenerator<R>) generators.get(i).get(0);
            EvenStableGenerator<R> tObj = (EvenStableGenerator<R>) generators.get(i+1).get(0);
            EvenArrow<R> mor = new EvenArrow<R>(bObj, tObj, scob);
            bObj.addBotArrow(mor);
            tObj.addTopArrow(mor);
            if (extr != null) {
                if ((addr+i)%2 != 0) extr = dotn;
                else if (rasmus) mor.addCobordism(mid);
                mor.addCobordism(extr);
            }
        }// Morphisms have been created
        /*ArrayList<Integer> overlap = overlapOf(posEndpts,negEndpts);
        if (!overlap.isEmpty()) modifyDiagrams(overlap);
        for (Integer ov : overlap) {
            posEndpts.remove(ov);
            negEndpts.remove(ov);
        }// */
        relQs = qs;
    }
    
    public EvenStableComplex(int comp, R unt, boolean unr, boolean red, AbortInfo ab, 
            DialogWrap frm) { // this returns complex for an unlink with comp components.
        super(unt, false, ab, frm);
        
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public EvenStableComplex(int comp, R unt, AbortInfo abInf, DialogWrap frm,
            boolean ras) { // should only be used with comp = 0 or 1
        super(unt, false, abInf, frm);
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
                ((EvenStableGenerator<R>) ((ArrayList<Generator<R>>) generators.get(i)).get(j)).output(nextLev);
            }
        }
        System.out.println();
        System.out.println();
    }
    
    public void modifyStableComplex(EvenStableComplex<R> complex, int reduce, String girth, boolean det) {
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
        if (reduce == 0) getNewObjects(complex, pEndpts, nEndpts, tensorCache, deloopCache, det);
        //else getReducedNewObjects(complex, pEndpts, nEndpts, tensorCache, deloopCache, reduce, det);
        if (!rasmus) removeMorphisms(pEndpts.size()*2);
        posEndpts = pEndpts;
        negEndpts = nEndpts;
        cache = deloopCache;
    }
    
    private void getNewObjects(EvenStableComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts,
            EvenCache tCache, EvenCache dCache, boolean det) {
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
            if (i >= 0) createTensor(i, t, complex, pEndpts, nEndpts, tCache, diagTrans, tobjs);
            if (i < generators.size()-1 && i > -t-1) deloopObjects(i+t, tobjs, dobjs, tCache, dCache, ddigTrans);
            /*if (i < generators.size()-2) {
                gaussEliminate(i+t+1,dobjs, dCache, det); 
                gaussEliminate(i+t+1,dobjs, dCache, det); // we run it twice in case some possible cancellations were created
            }// */
            if (lastCancel && i < generators.size()-2) gaussEliminate(i+t+1, dobjs, dCache, det);
            i--;
        }
        generators = dobjs;
    }
    
    private void createTensor(int i, int t, EvenStableComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts,
            EvenCache tCache, int[][] diagTrans, ArrayList<ArrayList<Generator<R>>> objs) {
        for (int l = 0; l < generators.get(i).size(); l++) {
            if (abInf.isAborted()) return;
            EvenStableGenerator<R> fObj = (EvenStableGenerator<R>) generators.get(i).get(l);
            ArrayList<Generator<R>> nObjs = new ArrayList<Generator<R>>(t);
            for (int k = t-1; k >= 0; k--) {
                EvenStableGenerator<R> sObj = (EvenStableGenerator<R>) complex.generators.get(k).get(0);
                ArrayList<Integer> clonePos = fObj.clonePosition();
                clonePos.add(k);
                ArrayList<ArrayList<Integer>> cloneCir = fObj.cloneCircles();
                ArrayList<Boolean> cloneSig = fObj.cloneSigns();
                int dnum = diagTrans[fObj.getDiagram()][sObj.getDiagram()]-1;
                if (dnum == -1) {
                    Diagram nDiag = combineDiagram(fObj.getDiagram(), sObj.getDiagram(), complex, pEndpts,nEndpts,tCache);
                    dnum = getDiagNumber(nDiag, tCache.getDiagrams());
                    diagTrans[fObj.getDiagram()][sObj.getDiagram()] = dnum+1;
                }
                EvenStableGenerator<R> nObj = new EvenStableGenerator<R>(dnum, fObj.hdeg()+sObj.hdeg(),
                        fObj.qdeg()+sObj.qdeg(), clonePos, cloneCir, cloneSig);
                objs.get(i+k).add(nObj);
                nObjs.add(0, nObj);
                for (Iterator<Arrow<R>> it = sObj.getBotArrows().iterator(); it.hasNext();) {
                    EvenArrow<R> mor = (EvenArrow<R>) it.next();
                    EvenStableGenerator<R> ntObj = (EvenStableGenerator<R>) nObjs.get(1);
                    ArrayList<Cobordism<R>> nmoves = alterCobordisms(mor.getCobordisms(),((i+k-1)%2 != 0),tCache,
                            nObj.getDiagram(),complex);
                    EvenArrow<R> nmor = new EvenArrow<R>(nObj,ntObj,nmoves);
                    nObj.addBotArrow(nmor);
                }
                for (Iterator<Arrow<R>> it = fObj.getBotArrows().iterator(); it.hasNext();) {
                    EvenArrow<R> mor = (EvenArrow<R>) it.next();
                    EvenStableGenerator<R> ntObj = (EvenStableGenerator<R>) getTopObject(mor.getTopGenerator(),k);
                    ArrayList<Cobordism<R>> nmoves = adjustedCobordisms(mor.getCobordisms(),sObj.getDiagram(),complex,tCache,
                            pEndpts,nEndpts,nObj.getDiagram(),mor.getTopDiagram(),diagTrans);
                    EvenArrow<R> nmor = new EvenArrow<R>(nObj,ntObj,nmoves);
                    nObj.addBotArrow(nmor);
                }
            }
            counter = counter + t;
            frame.setLabelRight(""+counter, 2, false);
            fObj.clearTopArrow();
            fObj.clearBotArrow();
            EvenArrow<R> pointer = new EvenArrow<R>(fObj, (EvenGenerator<R>) nObjs.get(0)); 
            fObj.addTopArrow(pointer);
        }
        if (i < generators.size()-1) generators.set(i+1, null); // throwing away old objects of hom degree i+1
    }
    
    @Override
    protected void deloopObjects(int i, ArrayList<ArrayList<Generator<R>>> tobjs, ArrayList<ArrayList<Generator<R>>> dobjs, 
            EvenCache tCache, EvenCache dCache, ArrayList<Integer> ddigTrans) {
        ArrayList<Diagram> tDigs = tCache.getDiagrams();
        while (tDigs.size() > ddigTrans.size()) ddigTrans.add(-1);
        for (Iterator<Generator<R>> it = tobjs.get(i).iterator(); it.hasNext();) {
            EvenStableGenerator<R> oObj = (EvenStableGenerator<R>) it.next();
            if (abInf.isAborted()) return;
            int oldDigNr = oObj.getDiagram();
            Diagram oldDig = tCache.getDiagram(oldDigNr);
            int newDigNr = ddigTrans.get(oldDigNr);
            if (newDigNr == -1) newDigNr = newDiagNumber(oldDig, tCache, dCache, ddigTrans, oldDigNr);
            if (oldDig.circles.isEmpty()) noDeloop(oObj, newDigNr, tCache, dCache, dobjs.get(i), 
                    ddigTrans);
            else {
                if (oldDig.circles.size() == 1) deloopOneCircle(oObj, newDigNr, tCache, dCache, 
                        dobjs.get(i), ddigTrans, oldDig);
                else {
                    deloopTwoCircles(oObj, newDigNr, tCache, dCache,dobjs.get(i), ddigTrans, oldDig);
                }
            }
            frame.setLabelRight(""+counter, 2, false);
            oObj.clearBotArrow();
            if (i < tobjs.size()-1) tobjs.set(i+1,null);
        }
    }
    
    private void noDeloop(EvenStableGenerator<R> oObj, int newDigNr, EvenCache tCache, EvenCache dCache, 
            ArrayList<Generator<R>> dobjs, ArrayList<Integer> ddigTrans) {
        EvenStableGenerator<R> nObj = new EvenStableGenerator<R>(newDigNr,oObj.hdeg(),oObj.qdeg(),
                oObj.getPosition(), oObj.getCircles(), oObj.getSigns());
        Cobordism<R> ccob = new Cobordism<R>(0,unit);
        EvenArrow<R> cmor = new EvenArrow<R>(oObj,nObj,ccob);
        oObj.addTopArrow(cmor);
        createNewMorphisms(nObj, oObj, ccob, tCache, dCache, ddigTrans);
        dobjs.add(nObj);
    }
    
    private void deloopOneCircle(EvenStableGenerator<R> oObj, int newDigNr, EvenCache tCache, EvenCache dCache, 
            ArrayList<Generator<R>> dobjs, ArrayList<Integer> ddigTrans, Diagram oldDig) {
        ArrayList<Integer> pos = oObj.getPosition();
        ArrayList<ArrayList<Integer>> circs = oObj.cloneCircles();
        circs.add(tCache.getPaths(oldDig.circles.get(0)));
        ArrayList<Boolean> pSigns = oObj.cloneSigns();
        pSigns.add(true);
        ArrayList<Boolean> nSigns = oObj.cloneSigns();
        nSigns.add(false);
        EvenStableGenerator<R> nObjp = new EvenStableGenerator<R>(newDigNr, oObj.hdeg(), 
                oObj.qdeg()+1, pos, circs, pSigns);
        EvenStableGenerator<R> nObjm = new EvenStableGenerator<R>(newDigNr, oObj.hdeg(), 
                oObj.qdeg()-1, pos, circs, nSigns);
        boolean keepnGenp = !last | relQs.contains(nObjp.qdeg());
        boolean keepnGenm = !last | relQs.contains(nObjm.qdeg());
        int dot = tCache.getPowrs().get(tCache.getPts().indexOf(tCache.getPaths().get(oldDig.circles.get(0)).get(0)));
        Cobordism<R> ccob = new Cobordism<R>(dot, unit);
        Cobordism<R> dcob = new Cobordism<R>(0, unit);
        EvenArrow<R> cmor = new EvenArrow<R>(oObj, nObjp, ccob);
        EvenArrow<R> dmor = new EvenArrow<R>(oObj, nObjm, dcob);
        if (keepnGenp) oObj.addTopArrow(cmor);
        if (keepnGenm) oObj.addTopArrow(dmor);
        if (rasmus) {
            Cobordism<R> ecob = new Cobordism<R>(0, unit.negate());
            cmor.addCobordism(ecob);
        }
        if (keepnGenp) createNewMorphisms(nObjp, oObj, dcob, tCache, dCache, ddigTrans);
        if (keepnGenm) createNewMorphisms(nObjm, oObj, ccob, tCache, dCache, ddigTrans);
        if (keepnGenp) dobjs.add(nObjp);
        if (keepnGenm) dobjs.add(nObjm);
        counter = counter + 1;
    }
    
    private void deloopTwoCircles(EvenStableGenerator<R> oObj, int newDigNr, EvenCache tCache, EvenCache dCache, 
            ArrayList<Generator<R>> dobjs, ArrayList<Integer> ddigTrans, Diagram oldDig) {
        ArrayList<Integer> pos = oObj.getPosition();
        ArrayList<ArrayList<Integer>> circs = oObj.cloneCircles();
        circs.add(tCache.getPaths(oldDig.circles.get(0)));
        circs.add(tCache.getPaths(oldDig.circles.get(1)));
        ArrayList<Boolean> ppSigns = oObj.cloneSigns();
        ppSigns.add(true);
        ppSigns.add(true);
        ArrayList<Boolean> pmSigns = oObj.cloneSigns();
        pmSigns.add(true);
        pmSigns.add(false);
        ArrayList<Boolean> mpSigns = oObj.cloneSigns();
        mpSigns.add(false);
        mpSigns.add(true);
        ArrayList<Boolean> mmSigns = oObj.cloneSigns();
        mmSigns.add(false);
        mmSigns.add(false);
        EvenStableGenerator<R> nObjpp = new EvenStableGenerator<R>(newDigNr, oObj.hdeg(), 
                oObj.qdeg()+2, pos, circs, ppSigns);
        EvenStableGenerator<R> nObjpm = new EvenStableGenerator<R>(newDigNr, oObj.hdeg(), 
                oObj.qdeg(), pos, circs, pmSigns);
        EvenStableGenerator<R> nObjmp = new EvenStableGenerator<R>(newDigNr, oObj.hdeg(), 
                oObj.qdeg(), pos, circs, mpSigns);
        EvenStableGenerator<R> nObjmm = new EvenStableGenerator<R>(newDigNr, oObj.hdeg(), 
                oObj.qdeg()-2, pos, circs, mmSigns);
        boolean keepnGenpp = !last | relQs.contains(nObjpp.qdeg());
        boolean keepnGenpm = !last | relQs.contains(nObjpm.qdeg());
        boolean keepnGenmp = !last | relQs.contains(nObjmp.qdeg());
        boolean keepnGenmm = !last | relQs.contains(nObjmm.qdeg());
        int dot1 = tCache.getPowrs().get(tCache.getPts().indexOf(tCache.getPaths().get(oldDig.circles.get(0)).get(0)));
        int dot2 = tCache.getPowrs().get(tCache.getPts().indexOf(tCache.getPaths().get(oldDig.circles.get(1)).get(0)));
        int dot3 = dot1+dot2;
        Cobordism<R> ccob = new Cobordism<R>(dot3, unit);
        Cobordism<R> dcob = new Cobordism<R>(dot1, unit);
        Cobordism<R> ecob = new Cobordism<R>(dot2, unit);
        Cobordism<R> fcob = new Cobordism<R>(0, unit);
        EvenArrow<R> morpp = new EvenArrow<R>(oObj, nObjpp, ccob);
        EvenArrow<R> morpm = new EvenArrow<R>(oObj, nObjpm, dcob);
        EvenArrow<R> mormp = new EvenArrow<R>(oObj, nObjmp, ecob);
        EvenArrow<R> mormm = new EvenArrow<R>(oObj, nObjmm, fcob);
        if (keepnGenpp) oObj.addTopArrow(morpp);
        if (keepnGenpm) oObj.addTopArrow(morpm);
        if (keepnGenmp) oObj.addTopArrow(mormp);
        if (keepnGenmm) oObj.addTopArrow(mormm);
        if (rasmus) {
            dcob = new Cobordism<R>(dot1, unit.negate());
            ecob = new Cobordism<R>(dot2, unit.negate());
            morpp.addCobordism(dcob);
            morpp.addCobordism(ecob);
            morpp.addCobordism(fcob);
            fcob = new Cobordism<R>(0, unit.negate());
            morpm.addCobordism(fcob);
            mormp.addCobordism(fcob);
        }
        if (keepnGenpp) createNewMorphisms(nObjpp, oObj, mormm.getCobordism(0), tCache, dCache, ddigTrans);
        if (keepnGenpm) createNewMorphisms(nObjpm, oObj, mormp.getCobordism(0), tCache, dCache, ddigTrans);
        if (keepnGenmp) createNewMorphisms(nObjmp, oObj, morpm.getCobordism(0), tCache, dCache, ddigTrans);
        if (keepnGenmm) createNewMorphisms(nObjmm, oObj, morpp.getCobordism(0), tCache, dCache, ddigTrans);
        if (keepnGenpp) dobjs.add(nObjpp);
        if (keepnGenpm) dobjs.add(nObjpm);
        if (keepnGenmp) dobjs.add(nObjmp);
        if (keepnGenmm) dobjs.add(nObjmm);
        counter = counter + 3;
    }
    
    @Override
    protected boolean canCancel(EvenArrow<R> mor) {
        if (mor.getBotGenerator().qdeg() == noCancelq) return false;
        if (mor.getBotGenerator().qdeg() != mor.getTopGenerator().qdeg()) return false;
        Cobordism cob = mor.getCobordism(0);
        if (cob.getDottings()!=0 || cob.getSurgery()!=0) return false;
        return cob.getValue().isInvertible();
    }
    
}
