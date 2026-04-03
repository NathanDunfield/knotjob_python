/*

Copyright (C) 2020-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Diagram;
import knotjob.homology.Generator;
import knotjob.homology.TangleComplex;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class EvenComplex<R extends Ring<R>> extends TangleComplex<R> {
    
    protected EvenCache cache;
    protected final boolean rasmus;
    
    public EvenComplex(int comp, R unt, boolean unr, boolean red, AbortInfo ab, 
            DialogWrap frm) { // this returns complex for an unlink with comp components.
        super(unt, ab, frm);
        rasmus = false;
        cache = new EvenCache(posEndpts,negEndpts,posEndpts);
        Diagram empty = new Diagram();
        cache.addDiagram(empty);
        ArrayList<Generator<R>> unlinkObjects = unlinkObjs(comp, unr, red);
        generators.add(unlinkObjects);
    }
    
    public EvenComplex(R unt, boolean rms, AbortInfo ab, DialogWrap frm) {
        super(unt, ab, frm);
        rasmus = rms;
    }
    
    public EvenComplex(int crs, int[] ends, int hstart, int qstart, boolean rev, 
            boolean ras, boolean unred, boolean red, R unt, DialogWrap frm, AbortInfo abt) {
        super(unt, abt, frm);
        int change = 0;
        if (!rev) change = 1;
        posEndpts.add(ends[0+change]);
        posEndpts.add(ends[2+change]);
        negEndpts.add(ends[1-change]);
        negEndpts.add(ends[3-change]);
        rasmus = ras;
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
            hObjs.add(new EvenGenerator<R>(s,hstart+i,qdeg));
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
            EvenGenerator<R> bObj = (EvenGenerator<R>) generators.get(i).get(0);
            EvenGenerator<R> tObj = (EvenGenerator<R>) generators.get(i+1).get(0);
            EvenArrow<R> mor = new EvenArrow<R>(bObj, tObj, scob);
            bObj.addBotArrow(mor);
            tObj.addTopArrow(mor);
            if (extr != null) {
                if ((addr+i)%2 != 0) extr = dotn;
                else if (rasmus) mor.addCobordism(mid);
                mor.addCobordism(extr);
            }
        }// Morphisms have been created
        ArrayList<Integer> overlap = overlapOf(posEndpts,negEndpts);
        if (!overlap.isEmpty()) modifyDiagrams(overlap);
        for (Integer ov : overlap) {
            posEndpts.remove(ov);
            negEndpts.remove(ov);
        }
    }
    
    public EvenComplex(ArrayList<ArrayList<Generator<R>>> gens, R unt, 
            DialogWrap fram, AbortInfo abf) {
        super(gens, unt, fram, abf);
        rasmus = false;
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
            for (int j = 0; j < ( generators.get(i)).size(); j++) {
                System.out.println();
                System.out.println("Generator "+j);
                ArrayList<Generator<R>> nextLev = null;
                if (i < generators.size()-1) nextLev = generators.get(i+1);
                ((EvenGenerator<R>) (generators.get(i)).get(j)).output(nextLev);
            }
        }
        System.out.println();
        System.out.println();
    }
    
    private ArrayList<Integer> overlapOf(ArrayList<Integer> first, ArrayList<Integer> secon) {
        ArrayList<Integer> ov = new ArrayList<Integer>(2);
        for (int f : first) {
            if (secon.contains(f)) ov.add(f);
        }
        return ov;
    }
    
    private void modifyDiagrams(ArrayList<Integer> overlap) {
        ArrayList<ArrayList<Integer>> newPaths = new ArrayList<ArrayList<Integer>>();
        for (Diagram diag : cache.getDiagrams()) {
            int i = diag.paths.size()-1;
            ArrayList<ArrayList<Integer>> combinePaths = new ArrayList<ArrayList<Integer>>();
            while (i>=0) {
                ArrayList<Integer> path = cache.getPaths().get(diag.paths.get(i));
                if (overlapOf(path,overlap).isEmpty()) {
                    diag.paths.set(i, getPathNumber(path,newPaths));
                }
                else {
                    combinePaths.add(path);
                    diag.paths.remove(i);
                }
                i--;
            }
            for (int ov : overlap) {
                combinePaths(combinePaths,ov);
            }
            for (ArrayList<Integer> path : combinePaths) {
                if (Objects.equals(path.get(0), path.get(path.size()-1))) diag.circles.add(getPathNumber(path,newPaths));
                else diag.paths.add(getPathNumber(path,newPaths));
            }
        }
        cache.setPaths(newPaths);
    }
    
    private ArrayList<Generator<R>> unlinkObjs(int comp, boolean unr, boolean red) {
        ArrayList<Generator<R>> objs = new ArrayList<Generator<R>>();
        if (unr) {
            objs.add(new EvenGenerator<R>(0,0,1));
            objs.add(new EvenGenerator<R>(0,0,-1));
        }
        if (red) {
            objs.add(new EvenGenerator<R>(0,0,0));
        }
        if (comp == 0) return objs;
        return tensorObjects(objs, unlinkObjs(comp-1, true, false));
    }

    private ArrayList<Generator<R>> tensorObjects(ArrayList<Generator<R>> aObjs, 
            ArrayList<Generator<R>> bObjs) { // this is only meant to work for unlinks
        ArrayList<Generator<R>> newObjs = new ArrayList<Generator<R>>();
        for (Generator<R> aObj: aObjs) {
            for (Generator<R> bObj : bObjs) {
                newObjs.add(new EvenGenerator<R>(0,0,((EvenGenerator<R>) aObj).qdeg()
                        +((EvenGenerator<R>) bObj).qdeg()));
            }
        }
        return newObjs;
    }
    
    private int getPathNumber(ArrayList<Integer> npth, ArrayList<ArrayList<Integer>> pths) {
        boolean found = false;
        int i = 0;
        while (!found && i < pths.size()) {
            if (samePath(npth,pths.get(i))) found = true;
            else i++;
        }
        if (found) return i;
        pths.add(npth);
        return pths.size()-1;
    }
    
    private boolean samePath(ArrayList<Integer> npth, ArrayList<Integer> opth) {
        if (npth.size()!=opth.size()) return false;
        boolean same = true;
        int i = 0;
        int t = npth.size();
        while (same && i < t) {
            if (!Objects.equals(npth.get(i), opth.get(i))) same = false;
            else i++;
        }
        return same;
    }
    
    private void combinePaths(ArrayList<ArrayList<Integer>> paths, int ov) {
        boolean ffound = false;
        boolean sfound = false;
        ArrayList<Integer> fpath = null;
        ArrayList<Integer> spath = null;
        int i = 0;
        while (!(ffound & sfound)) {
            if (paths.get(i).get(0) == ov) {
                ffound = true;
                fpath = paths.get(i);
            }
            if (paths.get(i).get(paths.size()-1) == ov) {
                sfound = true;
                spath = paths.get(i);
            }
            i++;
        }
        paths.remove(fpath);
        if (fpath == spath) {
            paths.add(fpath);
            return;
        }
        paths.remove(spath);
        ArrayList<Integer> cpath = new ArrayList<Integer>();
        if (spath != null) for (int j : spath) cpath.add(j);
        if (fpath != null) for (int j : fpath) cpath.add(j);
        paths.add(cpath);
    }
    
    public void modifyComplex(EvenComplex<R> complex, int reduce, String girth, boolean det) {
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
        else getReducedNewObjects(complex, pEndpts, nEndpts, tensorCache, deloopCache, reduce, det);
        if (!rasmus) removeMorphisms(pEndpts.size()*2);
        posEndpts = pEndpts;
        negEndpts = nEndpts;
        cache = deloopCache;
    }
    
    protected void removeMorphisms(int girth) {
        for (ArrayList<Generator<R>> objs : generators) {
            for (Iterator<Generator<R>> it = objs.iterator(); it.hasNext();) {
                EvenGenerator<R> obj = (EvenGenerator<R>) it.next();
                int qb = ( obj).qdeg();
                int i = obj.getBotArrows().size()-1;
                while (i >= 0) {
                    EvenArrow<R> mor = obj.getBotArrow(i);
                    if (mor.getTopGenerator().qdeg()-qb > girth) {
                        obj.getBotArrows().remove(i);
                        mor.getTopGenerator().getTopArrows().remove(mor);
                    }
                    i--;
                }
            }
        }
    }
    
    private void getReducedNewObjects(EvenComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts,
            EvenCache tCache, EvenCache dCache, int reduce, boolean det) {
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
            if (i < generators.size()-1 && i > -t-1) deloopRedObjects(i+t, tobjs, dobjs, tCache,
                    dCache, ddigTrans, reduce);
            if (i < generators.size()-2) {
                gaussEliminate(i+t+1, dobjs, dCache, det);
                gaussEliminate(i+t+1, dobjs, dCache, det); // we run it twice in case some possible cancellations were created
            }
            i--;
        }
        generators = dobjs;
    }
    
    private void getNewObjects(EvenComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts,
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
            if (i < generators.size()-2) {
                boolean cancel = true;
                while (cancel) cancel = gaussEliminate(i+t+1, dobjs, dCache, det);// we cancel as long as we can
                //gaussEliminate(i+t+1,dobjs, dCache, det); 
                //gaussEliminate(i+t+1,dobjs, dCache, det); // we run it twice in case some possible cancellations were created
            }
            i--;
        }
        generators = dobjs;
    }
    
    protected boolean gaussEliminate(int i, ArrayList<ArrayList<Generator<R>>> dobjs, EvenCache dCache, 
            boolean det) {
        boolean cont = false;
        ArrayList<Generator<R>> objs = dobjs.get(i);
        int j = objs.size()-1;
        while (j >= 0) {
            if (abInf.isAborted()) return false;
            EvenGenerator<R> bObj = (EvenGenerator<R>) objs.get(j);
            int k = 0;
            boolean found = false;
            while (!found && k < bObj.getBotArrows().size()) {
                EvenArrow<R> mor = bObj.getBotArrow(k);
                if (canCancel(mor)) found = true;
                else k++;
            }
            if (found) {
                cancelObject(bObj.getBotArrow(k),dobjs,dCache,i);
                counter = counter - 2;
                String label = ""+counter;
                if (det) label = label+" ("+j+")";
                frame.setLabelRight(label, 2, false);
                cont = true;
            }
            j--;
        }// */
        return cont;
    }
    
    protected boolean canCancel(EvenArrow<R> mor) {
        if (mor.getBotGenerator().qdeg() != mor.getTopGenerator().qdeg()) return false;
        Cobordism cob = mor.getCobordism(0);
        if (cob.getDottings()!=0 || cob.getSurgery()!=0) return false;
        return cob.getValue().isInvertible();
    }
    
    private void cancelObject(EvenArrow<R> mor, ArrayList<ArrayList<Generator<R>>> dobjs, EvenCache dCache, int i) {
        EvenGenerator<R> yObj = mor.getBotGenerator();
        EvenGenerator<R> xObj = mor.getTopGenerator();
        yObj.getBotArrows().remove(mor);
        xObj.getTopArrows().remove(mor);
        for (Arrow<R> mr : xObj.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
        for (Arrow<R> mr : yObj.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        dobjs.get(i).remove(yObj);
        dobjs.get(i+1).remove(xObj);
        R u = (R) mor.getCobordism(0).getValue();
        for (Iterator<Arrow<R>> it = xObj.getTopArrows().iterator(); it.hasNext();) {
            EvenArrow<R> fmr = (EvenArrow<R>) it.next();
            for (Iterator<Arrow<R>> itt = yObj.getBotArrows().iterator(); itt.hasNext();) {
                EvenArrow<R> smr = (EvenArrow<R>) itt.next();
                EvenGenerator<R> bObj = fmr.getBotGenerator();
                EvenGenerator<R> tObj = smr.getTopGenerator();
                ArrayList<CobordInfo<R>> newCobs = zigZagCobordisms(fmr,smr,dCache,u);
                checkDoubleSurgeries(newCobs,dCache,bObj.getDiagram());
                checkMovingDots(newCobs, dCache,bObj.getDiagram());
                checkWhetherSame(newCobs, bObj.getDiagram());
                getNewCobordisms(bObj,tObj,newCobs,dCache);
            }
        }
        for (Arrow<R> mr : xObj.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        for (Arrow<R> mr : yObj.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
    }
    
    private void getNewCobordisms(EvenGenerator<R> bObj, EvenGenerator<R> tObj, ArrayList<CobordInfo<R>> newCobs, 
            EvenCache dCache) {
        boolean found = false;
        int i = 0;
        EvenArrow<R> mor;
        while (!found && i < bObj.getBotArrows().size()) {
            mor = bObj.getBotArrow(i);
            if (mor.getTopGenerator() == tObj) found = true;
            else i++;
        }
        if (!found) {
            mor = new EvenArrow<R>(bObj,tObj);
            bObj.addBotArrow(mor);
            tObj.addTopArrow(mor);
        }
        else mor = bObj.getBotArrow(i);
        combineMoves(mor,newCobs,dCache);
        if (mor.isEmpty()) {
            bObj.getBotArrows().remove(mor);
            tObj.getTopArrows().remove(mor);
        }
    }
    
    private void combineMoves(EvenArrow<R> mor, ArrayList<CobordInfo<R>> newCobs, EvenCache dCache) {
        for (CobordInfo<R> ncob : newCobs) {
            boolean found = false;
            int i = 0;
            while (!found && i < mor.getCobordisms().size()) {
                Cobordism<R> cob = mor.getCobordism(i);
                if (practicallySame(cob,ncob,dCache,mor.getTopDiagram())) found = true;
                else i++;
            }
            if (!found) {
                long surg = dCache.getSurgeries(ncob.getSurgeries());
                Cobordism<R> cob = new Cobordism<R>(ncob.getValue(), ncob.getDottings(), surg);
                if (!cob.getValue().isZero()) mor.addCobordism(cob);
            }
            else {
                Cobordism<R> cob = mor.getCobordism(i);
                cob.addValue(ncob.getValue());
                if (cob.getValue().isZero()) mor.getCobordisms().remove(i);
            }
        }
    }
    
    private boolean practicallySame(Cobordism cob, CobordInfo<R> ncob, EvenCache dCache, int lDiag) {
        if (cob.getDottings() != ncob.getDottings()) return false;
        //long e = cob.getSurgery();
        ArrayList<Integer> surgs = dCache.getSurgeries(cob.getSurgery(), lDiag);
        return essentiallySameSurgery(surgs, ncob.getSurgeries(), -1);
    }
    
    private boolean essentiallySameSurgery(ArrayList<Integer> fSurgs, ArrayList<Integer> sSurgs, int st) {
        if (fSurgs.size() != sSurgs.size()) return false;
        if (fSurgs.size() <= 2) return true;
        int i = 0;
        boolean essSame = true;
        while (essSame && i < fSurgs.size()) {
            if (!Objects.equals(fSurgs.get(i), sSurgs.get(i))) essSame = false;
            else i = i + 2;
        }
        if (essSame) return true;
        i = 1;
        while (essSame && i < fSurgs.size()) {
            if (!Objects.equals(fSurgs.get(i), sSurgs.get(i))) essSame = false;
            else i = i + 2;
        }
        return essSame;
    }
    
    private ArrayList<CobordInfo<R>> zigZagCobordisms(EvenArrow<R> fmr, EvenArrow<R> smr, EvenCache dCache, R u) {
        ArrayList<CobordInfo<R>> newCobs = new ArrayList<CobordInfo<R>>();
        for (Cobordism<R> fcob : fmr.getCobordisms()) {
            ArrayList<Integer> surgs = dCache.getSurgeries(fcob.getSurgery(), fmr.getTopGenerator().getDiagram());
            for (Cobordism<R> scob : smr.getCobordisms()) {
                ArrayList<Integer> newSurgs = 
                        combineIntegerList(surgs,dCache.getSurgeries(scob.getSurgery(), smr.getTopGenerator().getDiagram()));
                int newdot = combineDottings(fcob.getDottings(), scob.getDottings());
                if (newdot >= 0) {
                    R value = u.invert().negate().multiply(fcob.getValue()).multiply(scob.getValue());
                    CobordInfo<R> ninf = new CobordInfo<R>(value,newdot,newSurgs);
                    newCobs.add(ninf);
                }
            }
        }
        return newCobs;
    }
    
    private ArrayList<Integer> combineIntegerList(ArrayList<Integer> flist, ArrayList<Integer> slist) {
        ArrayList<Integer> nlist = new ArrayList<Integer>(flist.size()+slist.size());
        for (int i : flist) nlist.add(i);
        for (int i : slist) nlist.add(i);
        return nlist;
    }
    
    protected int combineDottings(int fdot, int sdot) {
        int newdot = fdot;
        int fac = 1;
        while (sdot > 0) {
            if (sdot%2 != 0) {
                if (fdot%2 == 0) newdot = newdot + fac;
                else if (!rasmus) return -1;
            }
            fac = fac * 2;
            sdot = sdot /2;
            fdot = fdot /2;
        }
        return newdot;
    }
    
    private void checkDoubleSurgeries(ArrayList<CobordInfo<R>> newCobs, EvenCache dCache, int stDig) {
        int i = 0;
        while (i < newCobs.size()) {
            CobordInfo<R> cob = newCobs.get(i);
            ArrayList<Integer> surgs = cob.getSurgeries();
            int fir = stDig;
            int j = -1;
            boolean found = false;
            while (!found && j < surgs.size()-2) {
                int sec = surgs.get(j+2);
                if (fir == sec) {
                    found = true;
                    removeDoubleSurgery(j,cob,newCobs,dCache);
                }
                else {
                    j++;
                    fir = surgs.get(j);
                }
            }
            if (!found) i++;
        }
    }
    
    private void removeDoubleSurgery(int j, CobordInfo<R> cob, ArrayList<CobordInfo<R>> newCobs, EvenCache dCache) {
        int midDig = cob.getSurgery(j+1);
        int outDig = cob.getSurgery(j+2);
        int[] diff = differencePaths(midDig,outDig,dCache);
        cob.getSurgeries().remove(j+1);
        cob.getSurgeries().remove(j+1);
        newCobs.remove(cob);
        CobordInfo<R> cob1 = newCobordInfo(cob,diff[0],true,dCache);
        CobordInfo<R> cob2 = newCobordInfo(cob,diff[1],true,dCache);
        if (cob1 != null) newCobs.add(cob1);
        if (cob2 != null) newCobs.add(cob2);
        if (rasmus) {
            CobordInfo<R> cob3 = newCobordInfo(cob,0,false,dCache);
            newCobs.add(cob3);
        }
    }
    
    private CobordInfo<R> newCobordInfo(CobordInfo<R> cob, int toDot, boolean keep, EvenCache dCache) {
        ArrayList<Integer> surg = cloneList(cob.getSurgeries());
        int newDot = cob.getDottings();
        if (toDot > 0) {
            int pos = dCache.getPts().indexOf(toDot);
            int pwr = dCache.getPowrs().get(pos);
            if ((newDot/pwr)%2 == 0) newDot = newDot + pwr; // wasn't dotted, now it's dotted
            else if (!rasmus) newDot = -1;
        }
        if (newDot == -1) return null;
        R val = cob.getValue();
        if (!keep) val = val.negate();
        CobordInfo<R> newInf = new CobordInfo<R>(val,newDot,surg);
        return newInf;
    }
    
    protected ArrayList<Integer> cloneList(ArrayList<Integer> org) {
        ArrayList<Integer> clone = new ArrayList<Integer>(org.size());
        for (int y : org) clone.add(y);
        return clone;
    }
    
    private int[] differencePaths(int fdig, int sdig, EvenCache dCache) {
        Diagram fDia = dCache.getDiagram(fdig);
        Diagram sDia = dCache.getDiagram(sdig);
        int i = 0;
        int j = 0;
        int[] paths = new int[2];
        while (j < 2) {
            if (!sDia.paths.contains(fDia.paths.get(i))) {
                paths[j] = fDia.paths.get(i);
                j++;
            }
            i++;
        }
        paths[0] = dCache.getPaths().get(paths[0]).get(0);
        paths[1] = dCache.getPaths().get(paths[1]).get(0);
        return paths;
    }
    
    private void checkMovingDots(ArrayList<CobordInfo<R>> newCobs, EvenCache dCache, int stDig) {
        int i = newCobs.size()-1;
        while (i >= 0) {
            CobordInfo<R> cob = newCobs.get(i);
            if (cob.getDottings()>1 && !cob.getSurgeries().isEmpty()) {
                moveDots(cob,dCache,stDig);
                if (cob.getDottings() < 0) newCobs.remove(i);
            }
            i--;
        }
    }
    
    private void moveDots(CobordInfo<R> cob, EvenCache dCache, int stDig) {
        ArrayList<Integer> interestingDots = new ArrayList<Integer>();
        int dot = cob.getDottings();
        int i = 0;
        while (dot > 0) {
            if (dot%2 != 0) interestingDots.add(dCache.getPts().get(i));
            dot = dot / 2;
            i++;
        }
        ArrayList<ArrayList<Integer>> combined = new ArrayList<ArrayList<Integer>>();
        i = -1;
        int fDig = stDig;
        while (i < cob.getSurgeries().size()-1) {
            int sDig = cob.getSurgery(i+1);
            int[] comb = differencePaths(fDig,sDig,dCache);
            addEndpoints(combined,comb);
            i++;
            fDig = sDig;
        }
        boolean keep = true;
        for (int d : interestingDots) {
            int nd = seeIfSame(combined,d);
            dot = obtainRightDot(dot,nd,dCache);
            if (dot == -1) keep = false;
        }
        if (keep) cob.setDottings(dot);
        else cob.setDottings(-1);
    }
    
    private int obtainRightDot(int dot, int nd, EvenCache dCache) {
        int d = dCache.getPowrs().get(dCache.getPts().indexOf(nd));
        if ((dot/d)%2 == 0) dot = dot + d;
        else if (!rasmus) dot = -1;
        return dot;
    }
    
    private int seeIfSame(ArrayList<ArrayList<Integer>> combined, int d) {
        boolean found = false;
        int i = 0;
        while (!found && i < combined.size()) {
            ArrayList<Integer> comber = combined.get(i);
            if (comber.contains(d)) found = true;
            else i++;
        }
        if (!found) return d;
        return combined.get(i).get(0);
    }
    
    private void addEndpoints(ArrayList<ArrayList<Integer>> combined, int[] comb) {
        boolean found = false;
        int i = 0;
        while (!found && i < combined.size()) {
            ArrayList<Integer> comber = combined.get(i);
            if (comber.contains(comb[0]) | comber.contains(comb[1])) found = true;
            else i++;
        }
        if (found) {
            ArrayList<Integer> comber = combined.get(i);
            if (!comber.contains(comb[0])) comber.add(comb[0]);
            if (!comber.contains(comb[1])) comber.add(comb[1]);
            Collections.sort(comber);
        }
        else {
            ArrayList<Integer> comber = new ArrayList<Integer>();
            comber.add(comb[0]);comber.add(comb[1]);
            Collections.sort(comber);
            combined.add(comber);
        }
    }
    
    private void checkWhetherSame(ArrayList<CobordInfo<R>> newCobs, int st) {
        int i = newCobs.size()-1;
        while (i >= 1) {
            CobordInfo<R> cob = newCobs.get(i);
            int j = 0;
            boolean found = false;
            while (!found && j < i) {
                CobordInfo<R> ccob = newCobs.get(j);
                boolean samesurg = essentiallySameSurgery(ccob,cob,st);
                if (samesurg) cob.setSurgeries(ccob.getSurgeries());
                boolean same = samesurg & (cob.getDottings() == ccob.getDottings());
                if (same) {
                    found = true;
                    ccob.addValue(cob.getValue());
                    newCobs.remove(i);
                    if (ccob.getValue().isZero()) {
                        newCobs.remove(j);
                        i--;
                    }
                }
                j++;
            }
            i--;
        }
    }
    
    private boolean essentiallySameSurgery(CobordInfo<R> fcob, CobordInfo<R> scob, int st) {
        ArrayList<Integer> fSurgs = fcob.getSurgeries();
        ArrayList<Integer> sSurgs = scob.getSurgeries();
        return essentiallySameSurgery(fSurgs,sSurgs,st);
    }
    
    protected void createTensor(int i, int t, EvenComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts,
            EvenCache tCache, int[][] diagTrans, ArrayList<ArrayList<Generator<R>>> objs) {
        for (int l = 0; l < generators.get(i).size(); l++) {
            if (abInf.isAborted()) return;
            EvenGenerator<R> fObj = (EvenGenerator<R>) generators.get(i).get(l);
            ArrayList<Generator<R>> nObjs = new ArrayList<Generator<R>>(t);
            for (int k = t-1; k >= 0; k--) {
                EvenGenerator<R> sObj = (EvenGenerator<R>) complex.generators.get(k).get(0);
                int dnum = diagTrans[fObj.getDiagram()][sObj.getDiagram()]-1;
                if (dnum == -1) {
                    Diagram nDiag = combineDiagram(fObj.getDiagram(),sObj.getDiagram(),complex,pEndpts,nEndpts,tCache);
                    dnum = getDiagNumber(nDiag,tCache.getDiagrams());
                    diagTrans[fObj.getDiagram()][sObj.getDiagram()] = dnum+1;
                }
                EvenGenerator<R> nObj = new EvenGenerator<R>(dnum,fObj.hdeg()+sObj.hdeg(),fObj.qdeg()+sObj.qdeg());
                objs.get(i+k).add(nObj);
                nObjs.add(0, nObj);
                for (Iterator<Arrow<R>> it = sObj.getBotArrows().iterator(); it.hasNext();) {
                    EvenArrow<R> mor = (EvenArrow<R>) it.next();
                    EvenGenerator<R> ntObj = (EvenGenerator<R>) nObjs.get(1);
                    ArrayList<Cobordism<R>> nmoves = alterCobordisms(mor.getCobordisms(),((i+k-1)%2 != 0),tCache,
                            nObj.getDiagram(),complex);
                    EvenArrow<R> nmor = new EvenArrow<R>(nObj,ntObj,nmoves);
                    nObj.addBotArrow(nmor);
                }
                for (Iterator<Arrow<R>> it = fObj.getBotArrows().iterator(); it.hasNext();) {
                    EvenArrow<R> mor = (EvenArrow<R>) it.next();
                    EvenGenerator<R> ntObj = getTopObject(mor.getTopGenerator(),k);
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
    
    protected Diagram combineDiagram(int fdiagram, int nsdiagram, EvenComplex<R> complex,  
            ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, EvenCache tCache) {
        Diagram fDiag = cache.getDiagram(fdiagram);
        Diagram sDiag = complex.cache.getDiagram(nsdiagram);
        ArrayList<ArrayList<Integer>> newPaths = new ArrayList<ArrayList<Integer>>();
        for (int i : fDiag.paths) {
            ArrayList<Integer> cpath = new ArrayList<Integer>();
            for (int y : cache.getPaths().get(i)) cpath.add(y);
            newPaths.add(cpath);
        }
        for (int j : sDiag.paths) {
            ArrayList<Integer> cpath = new ArrayList<Integer>();
            for (int y : complex.cache.getPaths().get(j)) cpath.add(y);
            newPaths.add(cpath);
        }
        for (int j : sDiag.circles) { // we're allowing circles in the second diagram
            ArrayList<Integer> cpath = new ArrayList<Integer>();
            for (int y : complex.cache.getPaths().get(j)) cpath.add(y);
            newPaths.add(cpath);
        }
        for (int e : posEndpts) if (!pEndpts.contains(e)) combinePaths(newPaths,e,true);
        for (int e : negEndpts) if (!nEndpts.contains(e)) combinePaths(newPaths,e,false);
        ArrayList<ArrayList<Integer>> ncircles = getCircles(newPaths);
        Diagram nDiag = new Diagram();
        nDiag.paths = new ArrayList<Integer>(newPaths.size());
        nDiag.circles = new ArrayList<Integer>(ncircles.size());
        for (ArrayList<Integer> npth : newPaths) {
            int p = getPathNumber(npth,tCache.getPaths());
            nDiag.paths.add(p);
        }
        for (ArrayList<Integer> ncir : ncircles) {
            int p = getPathNumber(ncir,tCache.getPaths());
            nDiag.circles.add(p);
        }
        return nDiag;
    }
    
    protected int getDiagNumber(Diagram nDiag, ArrayList<Diagram> dgrams) {
        Collections.sort(nDiag.paths);
        Collections.sort(nDiag.circles);
        boolean found = false;
        int i = 0;
        while (!found && i < dgrams.size()) {
            Diagram cDiag = dgrams.get(i);
            if (sameDiag(nDiag,cDiag)) found = true;
            else i++;
        }
        if (found) return i;
        dgrams.add(nDiag);
        return dgrams.size()-1;
    }
    
    private boolean sameDiag(Diagram nDiag, Diagram cDiag) {
        if (nDiag.paths.size() != cDiag.paths.size() || nDiag.circles.size() != cDiag.circles.size()) return false;
        boolean same = true;
        int i = 0;
        int t = nDiag.paths.size();
        while (same && i < t) {
            if (!Objects.equals(nDiag.paths.get(i), cDiag.paths.get(i))) same = false;
            else i++;
        }
        if (!same) return false;
        i = 0;
        t = nDiag.circles.size();
        while (same && i < t) {
            if (!Objects.equals(nDiag.circles.get(i), cDiag.circles.get(i))) same = false;
            else i++;
        }
        return same;
    }
    
    protected EvenGenerator<R> getTopObject(EvenGenerator<R> stObj,int k) {
        EvenGenerator<R> endObj = stObj.getTopArrow(0).getTopGenerator();
        while (k > 0) {
            endObj = endObj.getBotArrow(0).getTopGenerator();
            k--;
        }
        return endObj;
    }
    
    protected ArrayList<Cobordism<R>> alterCobordisms(ArrayList<Cobordism<R>> moves, boolean alternate,
            EvenCache tCache, int bdiagram, EvenComplex<R> complex) {
        ArrayList<Cobordism<R>> newMoves = new ArrayList<Cobordism<R>>(moves.size());
        for (Cobordism<R> cob : moves) {
            Cobordism<R> ncob;
            int newdottings = getNewDottings(cob.getDottings(), bdiagram, tCache, complex.posEndpts);
            if (newdottings >=0) {
                if (alternate) ncob = new Cobordism<R>(cob.getValue().negate(),newdottings,cob.getSurgery());
                else ncob = new Cobordism<R>(cob.getValue(),newdottings,cob.getSurgery());
                newMoves.add(ncob);
            }
        }
        return newMoves;
    }
    
    private int getNewDottings(int olddottings, int bdiagram, EvenCache tCache, ArrayList<Integer> endps) {
        Diagram diag = tCache.getDiagram(bdiagram);
        int newdottings = 0;
        boolean doubledot = false;
        int i = 0;
        int pwr = 2;
        while (olddottings > 0) {
            if (olddottings%pwr != 0) {
                int end = theEndpt(endps.get(i),diag.paths,diag.circles,tCache);
                if (newdottings % (2* end) > (newdottings % end)) doubledot = true;
                else newdottings = newdottings+end;
                olddottings = olddottings - olddottings%pwr;
            }
            pwr = pwr*2;
            i++;
        }
        if (doubledot && !rasmus) newdottings = -1; // means the cobordism should be dropped
        return newdottings;
    }
    
    private int theEndpt(int d, ArrayList<Integer> dpaths, ArrayList<Integer> dcircs, EvenCache tCache) {
        ArrayList<ArrayList<Integer>> pths = tCache.getPaths();
        boolean found = false;
        int i = 0;
        while (!found && i < dpaths.size()) {
            ArrayList<Integer> path = pths.get(dpaths.get(i));
            if (path.contains(d)) found = true;
            else i++;
        }
        if (found) return tCache.getPowrs().get(tCache.getPts().indexOf(pths.get(dpaths.get(i)).get(0)));
        i = 0;
        while (!found) {
            ArrayList<Integer> path = pths.get(dcircs.get(i));
            if (path.contains(d)) found = true;
            else i++;
        }
        return tCache.getPowrs().get(tCache.getPts().indexOf(pths.get(dcircs.get(i)).get(0)));
    }
    
    protected ArrayList<Cobordism<R>> adjustedCobordisms(ArrayList<Cobordism<R>> moves, int diagram, EvenComplex<R> nextComplex, 
            EvenCache tCache, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts,
            int bdiagram, int tdiagram, int[][] diagTrans) {
        ArrayList<Diagram> dgrams = tCache.getDiagrams();
        ArrayList<Cobordism<R>> newCobs = new ArrayList<Cobordism<R>>();
        for (Cobordism<R> cob : moves) {
            int newdottings = getNewDottings(cob.getDottings(),bdiagram,tCache,cache.getPts());//posEndpts);
            if (newdottings >= 0) {
                Cobordism<R> ncob = new Cobordism<R>(newdottings,cob.getValue());
                ArrayList<Integer> surgs = cache.getSurgeries(cob.getSurgery(),tdiagram);
                ArrayList<Integer> nsurgs = new ArrayList<Integer>(surgs.size());
                for (int y : surgs) {
                    int dnum = diagTrans[y][diagram]-1;
                    if (dnum == -1) {
                        Diagram nDiag = combineDiagram(y,diagram,nextComplex,pEndpts,nEndpts,tCache);
                        dnum = getDiagNumber(nDiag,dgrams);
                        diagTrans[y][diagram] = dnum+1;
                    }
                    nsurgs.add(dnum);
                }
                ncob.setSurgery(tCache.getSurgeries(nsurgs));
                newCobs.add(ncob);
            }
        }
        return newCobs;
    }
    
    protected void deloopObjects(int i, ArrayList<ArrayList<Generator<R>>> tobjs, ArrayList<ArrayList<Generator<R>>> dobjs, 
            EvenCache tCache, EvenCache dCache, ArrayList<Integer> ddigTrans) {
        ArrayList<Diagram> tDigs = tCache.getDiagrams();
        while (tDigs.size() > ddigTrans.size()) ddigTrans.add(-1);
        for (Iterator<Generator<R>> it = tobjs.get(i).iterator(); it.hasNext();) {
            EvenGenerator<R> oObj = (EvenGenerator<R>) it.next();
            if (abInf.isAborted()) return;
            int oldDigNr = oObj.getDiagram();
            Diagram oldDig = tCache.getDiagram(oldDigNr);
            int newDigNr = ddigTrans.get(oldDigNr);
            if (newDigNr == -1) newDigNr = newDiagNumber(oldDig,tCache,dCache,ddigTrans,oldDigNr);
            if (oldDig.circles.isEmpty()) noDeloop(oObj,newDigNr,tCache,dCache,dobjs.get(i),ddigTrans);
            else {
                if (oldDig.circles.size() == 1) deloopOneCircle(oObj,newDigNr,tCache,dCache,dobjs.get(i),ddigTrans,oldDig);
                else {
                    deloopTwoCircles(oObj,newDigNr,tCache,dCache,dobjs.get(i),ddigTrans,oldDig);
                }
            }
            frame.setLabelRight(""+counter, 2, false);
            oObj.clearBotArrow();
            //if (i < tobjs.size()-1) tobjs.set(i+1,null);
        }
        tobjs.set(i, null);
    }
    
    private void deloopRedObjects(int i, ArrayList<ArrayList<Generator<R>>> tobjs, ArrayList<ArrayList<Generator<R>>> dobjs, 
            EvenCache tCache, EvenCache dCache, ArrayList<Integer> ddigTrans, int reduce) {
        ArrayList<Diagram> tDigs = tCache.getDiagrams();
        while (tDigs.size() > ddigTrans.size()) ddigTrans.add(-1);
        for (Iterator<Generator<R>> it = tobjs.get(i).iterator(); it.hasNext();) {
            EvenGenerator<R> oObj = (EvenGenerator<R>) it.next();
            if (abInf.isAborted()) return;
            int oldDigNr = oObj.getDiagram();
            Diagram oldDig = tCache.getDiagram(oldDigNr);
            int newDigNr = ddigTrans.get(oldDigNr);
            if (newDigNr == -1) newDigNr = newDiagNumber(oldDig, tCache, dCache, ddigTrans, oldDigNr);
            if (oldDig.circles.size() == 1) deloopOneRedCircle(oObj, newDigNr, tCache, dCache, dobjs.get(i), ddigTrans, oldDig, reduce);
            else deloopTwoRedCircles(oObj, newDigNr, tCache, dCache, dobjs.get(i), ddigTrans, oldDig, reduce);
            frame.setLabelRight(""+counter, 2, false);
            oObj.clearBotArrow();
            if (i < tobjs.size()-1) tobjs.set(i+1, null);
        }
    }
    
    protected void noDeloop(EvenGenerator<R> oObj, int newDigNr, EvenCache tCache, EvenCache dCache, 
            ArrayList<Generator<R>> dobjs, ArrayList<Integer> ddigTrans) {
        EvenGenerator<R> nObj = new EvenGenerator<R>(newDigNr,oObj.hdeg(),oObj.qdeg());
        Cobordism<R> ccob = new Cobordism<R>(0,unit);
        EvenArrow<R> cmor = new EvenArrow<R>(oObj,nObj,ccob);
        oObj.addTopArrow(cmor);
        createNewMorphisms(nObj,oObj,ccob,tCache,dCache,ddigTrans);
        dobjs.add(nObj);
    }
    
    protected void deloopOneCircle(EvenGenerator<R> oObj, int newDigNr, EvenCache tCache, EvenCache dCache, 
            ArrayList<Generator<R>> dobjs, ArrayList<Integer> ddigTrans, Diagram oldDig) {
        EvenGenerator<R> nObjp = new EvenGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg()+1);
        EvenGenerator<R> nObjm = new EvenGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg()-1);
        int dot = tCache.getPowrs().get(tCache.getPts().indexOf(tCache.getPaths().get(oldDig.circles.get(0)).get(0)));
        Cobordism<R> ccob = new Cobordism<R>(dot, unit);
        Cobordism<R> dcob = new Cobordism<R>(0, unit);
        EvenArrow<R> cmor = new EvenArrow<R>(oObj, nObjp, ccob);
        EvenArrow<R> dmor = new EvenArrow<R>(oObj, nObjm, dcob);
        oObj.addTopArrow(cmor);
        oObj.addTopArrow(dmor);
        if (rasmus) {
            Cobordism<R> ecob = new Cobordism<R>(0, unit.negate());
            cmor.addCobordism(ecob);
        }
        createNewMorphisms(nObjp, oObj, dcob, tCache, dCache, ddigTrans);
        createNewMorphisms(nObjm, oObj, ccob, tCache, dCache, ddigTrans);
        dobjs.add(nObjp);
        dobjs.add(nObjm);
        counter = counter + 1;
    }
    
    private void deloopOneRedCircle(EvenGenerator<R> oObj, int newDigNr, EvenCache tCache, EvenCache dCache, 
            ArrayList<Generator<R>> dobjs, ArrayList<Integer> ddigTrans, Diagram oldDig, int reduced) {
        EvenGenerator<R> nObjp = new EvenGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg()+1);
        EvenGenerator<R> nObjm = new EvenGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg()-1);
        EvenGenerator<R> nObjr = new EvenGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg());
        int dot = tCache.getPowrs().get(tCache.getPts().indexOf(tCache.getPaths().get(oldDig.circles.get(0)).get(0)));
        Cobordism<R> ccob = new Cobordism<R>(dot, unit);
        Cobordism<R> dcob = new Cobordism<R>(0, unit);
        EvenArrow<R> cmor = new EvenArrow<R>(oObj, nObjp, ccob);
        EvenArrow<R> dmor = new EvenArrow<R>(oObj, nObjm, dcob);
        EvenArrow<R> emor = new EvenArrow<R>(oObj, nObjr, dcob);
        if (reduced < 0) {
            oObj.addTopArrow(cmor);
            oObj.addTopArrow(dmor);
        }
        oObj.addTopArrow(emor);
        if (reduced < 0) {
            createNewMorphisms(nObjp, oObj, dcob, tCache, dCache, ddigTrans);
            createNewMorphisms(nObjm, oObj, ccob, tCache, dCache, ddigTrans);
        }
        createNewMorphisms(nObjr, oObj, ccob, tCache, dCache, ddigTrans);
        if (reduced < 0) {
            fixMorphisms(nObjp);
            fixMorphisms(nObjm);
            fixMorphisms(nObjr);
            dobjs.add(nObjp);
            dobjs.add(nObjm);
            counter = counter + 2;
        }
        dobjs.add(nObjr);
    }
    
    protected void deloopTwoCircles(EvenGenerator<R> oObj, int newDigNr, EvenCache tCache, EvenCache dCache, 
            ArrayList<Generator<R>> dobjs, ArrayList<Integer> ddigTrans, Diagram oldDig) {
        EvenGenerator<R> nObjpp = new EvenGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg()+2);
        EvenGenerator<R> nObjpm = new EvenGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg());
        EvenGenerator<R> nObjmp = new EvenGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg());
        EvenGenerator<R> nObjmm = new EvenGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg()-2);
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
        oObj.addTopArrow(morpp);
        oObj.addTopArrow(morpm);
        oObj.addTopArrow(mormp);
        oObj.addTopArrow(mormm);
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
        createNewMorphisms(nObjpp, oObj, mormm.getCobordism(0), tCache, dCache, ddigTrans);
        createNewMorphisms(nObjpm, oObj, mormp.getCobordism(0), tCache, dCache, ddigTrans);
        createNewMorphisms(nObjmp, oObj, morpm.getCobordism(0), tCache, dCache, ddigTrans);
        createNewMorphisms(nObjmm, oObj, morpp.getCobordism(0), tCache, dCache, ddigTrans);
        dobjs.add(nObjpp);
        dobjs.add(nObjpm);
        dobjs.add(nObjmp);
        dobjs.add(nObjmm);
        counter = counter + 3;
    }
    
    private void deloopTwoRedCircles(EvenGenerator<R> oObj, int newDigNr, EvenCache tCache, EvenCache dCache, 
            ArrayList<Generator<R>> dobjs, ArrayList<Integer> ddigTrans, Diagram oldDig, int reduced) {
        int point = Math.abs(reduced)-1;
        boolean pm = true;
        if (oldDig.circles.isEmpty()) System.out.println("Expect Error");
        if (tCache.getPaths().get(oldDig.circles.get(0)).get(0) == point) pm = false;
        EvenGenerator<R> nObjpp = new EvenGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg()+2);
        EvenGenerator<R> nObjpm = new EvenGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg());
        EvenGenerator<R> nObjmp = new EvenGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg());
        EvenGenerator<R> nObjmm = new EvenGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg()-2);
        EvenGenerator<R> nObjrp = new EvenGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg()+1);
        EvenGenerator<R> nObjrm = new EvenGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg()-1);
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
        EvenArrow<R> morrm = new EvenArrow<R>(oObj, nObjrm, fcob);
        EvenArrow<R> morrp;
        if (pm) morrp = new EvenArrow<R>(oObj, nObjrp, dcob);
        else morrp = new EvenArrow<R>(oObj, nObjrp, ecob);
        if (reduced < 0) {
            oObj.addTopArrow(morpp);
            oObj.addTopArrow(morpm);
            oObj.addTopArrow(mormp);
            oObj.addTopArrow(mormm);
        }
        oObj.addTopArrow(morrm);
        oObj.addTopArrow(morrp);
        if (rasmus) {  // this should only occur with reduced > 0
            fcob = new Cobordism<R>(0, unit.negate());
            morrp.addCobordism(fcob); 
        }
        if (reduced < 0) {
            createNewMorphisms(nObjpp, oObj, mormm.getCobordism(0), tCache, dCache, ddigTrans);
            createNewMorphisms(nObjpm, oObj, mormp.getCobordism(0), tCache, dCache, ddigTrans);
            createNewMorphisms(nObjmp, oObj, morpm.getCobordism(0), tCache, dCache, ddigTrans);
            createNewMorphisms(nObjmm, oObj, morpp.getCobordism(0), tCache, dCache, ddigTrans);
        } 
        if (pm) createNewMorphisms(nObjrp, oObj, mormp.getCobordism(0), tCache, dCache, ddigTrans);
        else createNewMorphisms(nObjrp, oObj, morpm.getCobordism(0), tCache, dCache, ddigTrans);
        createNewMorphisms(nObjrm, oObj, morpp.getCobordism(0), tCache, dCache, ddigTrans);
        if (reduced < 0) {
            fixMorphisms(nObjpp);
            fixMorphisms(nObjpm);
            fixMorphisms(nObjmp);
            fixMorphisms(nObjmm);
            fixMorphisms(nObjrp);
            fixMorphisms(nObjrm);
            dobjs.add(nObjpp);
            dobjs.add(nObjpm);
            dobjs.add(nObjmp);
            dobjs.add(nObjmm);
        }
        dobjs.add(nObjrp);
        dobjs.add(nObjrm);
        if (reduced < 0) counter = counter + 5;
        else counter = counter + 1;
    }
    
    private void fixMorphisms(EvenGenerator<R> obj) {
        int i = obj.getBotArrows().size()-1;
        while (i >= 0) {
            EvenArrow<R> mor = obj.getBotArrow(i);
            if (mor.getBotGenerator().qdeg() != mor.getTopGenerator().qdeg()) {
                obj.getBotArrows().remove(mor);
                mor.getTopGenerator().getTopArrows().remove(mor);
            }
            i--;
        }
    }
    
    protected void createNewMorphisms(EvenGenerator<R> nObj, EvenGenerator<R> oObj, Cobordism<R> ccob, 
            EvenCache tCache, EvenCache dCache, ArrayList<Integer> ddigTrans) {
        for (Iterator<Arrow<R>> it = oObj.getBotArrows().iterator(); it.hasNext();) {
            EvenArrow<R> mor = (EvenArrow<R>) it.next();
            ArrayList<CobordInfo<R>> newCobs = new ArrayList<CobordInfo<R>>();
            for (Cobordism<R> fcob : mor.getCobordisms()) {
                int newDots = newDottings(fcob.getDottings(),ccob.getDottings(),tCache);
                if (rasmus || newDots == fcob.getDottings()+ccob.getDottings()) {
                    ArrayList<Integer> surgs = tCache.getSurgeries(fcob.getSurgery(), mor.getTopGenerator().getDiagram());
                    CobordInfo<R> ncob = new CobordInfo<R>(ccob.getValue().multiply(fcob.getValue()),newDots,surgs);
                    newCobs.add(ncob);
                }
            }
            modifyCobordisms(newCobs,tCache,dCache,ddigTrans,oObj.getDiagram());
            for (Iterator<Arrow<R>> itt = mor.getTopGenerator().getTopArrows().iterator(); itt.hasNext();) {
                EvenArrow<R> cmor = (EvenArrow<R>) itt.next();
                EvenGenerator<R> tnObj = cmor.getTopGenerator();
                EvenArrow<R> nmor = new EvenArrow<R>(nObj,tnObj);
                obtainNewCobordisms(newCobs,nmor,cmor,tCache,dCache);
                if (!nmor.isEmpty()) {
                    nObj.addBotArrow(nmor);
                    tnObj.addTopArrow(nmor);
                }
            }
        }
    }
    
    protected int newDottings(int fdot, int sdot, EvenCache tCache) {
        int newdot = fdot;
        if (sdot == 0) return newdot;
        if (tCache.getPowrs().contains(sdot)) {
            if (fdot % (sdot*2) < sdot) return newdot+sdot;
            return newdot;
        }
        int pwr = tCache.getPowrs().get(tCache.getPowrs().size()-1);
        if (fdot < pwr) newdot = newdot + pwr;
        if (fdot % pwr < (pwr/2)) newdot = newdot+pwr/2;
        return newdot;
    }
    
    protected void modifyCobordisms(ArrayList<CobordInfo<R>> newCobs, EvenCache tCache, EvenCache dCache, 
            ArrayList<Integer> digTrans, int stDig) {
        if (newCobs.isEmpty()) return;
        int i = 0;
        int maxi = newCobs.get(0).getSurgeries().size();
        for (int r = 1; r < newCobs.size(); r++) if (newCobs.get(r).getSurgeries().size()> maxi) 
            maxi = newCobs.get(r).getSurgeries().size();
        Diagram fDiag;// = tCache.getDiagram(stDig);
        ArrayList<Integer> fDiags = new ArrayList<Integer>();
        for (int u = 0; u < newCobs.size(); u++) fDiags.add(stDig);
        while (i < maxi) {
            int j = 0;
            Diagram sDiag;
            ArrayList<CobordInfo<R>> newGuys = new ArrayList<CobordInfo<R>>();
            while (j < newCobs.size()) {
                boolean throwaway = false;
                CobordInfo<R> cob = newCobs.get(j);
                fDiag = tCache.getDiagram(fDiags.get(j));
                if (cob.getSurgeries().size()> i) {
                    sDiag = tCache.getDiagram(cob.getSurgery(i));
                    fDiags.set(j,(int) cob.getSurgery(i));
                    if (fDiag.circles.size() == sDiag.circles.size()) { // remains a surgery
                        int nDignr = digTrans.get(cob.getSurgery(i));
                        if (nDignr == -1) nDignr = newDiagNumber(sDiag,tCache,dCache,digTrans,cob.getSurgery(i));
                        cob.setSurgery(i,nDignr);
                    }
                    else throwaway = dealWithCircles(cob,i,fDiag,sDiag,tCache,throwaway,newGuys,fDiags,j);
                }
                if (!throwaway) j++;
                else {
                    newCobs.remove(j);
                    fDiags.remove(j);
                }
            }
            for (CobordInfo<R> ncob : newGuys) newCobs.add(ncob);
            i++;
        }
        int j = newCobs.size()-1;
        while (j >= 0) {
            CobordInfo cob = newCobs.get(j);
            i = cob.getSurgeries().size()-1;
            while (i >= 0) {
                if (cob.getSurgery(i)==-1) cob.getSurgeries().remove(i);
                i--;
            }
            j--;
        }
    }
    
    protected boolean dealWithCircles(CobordInfo<R> cob, int i, Diagram fDiag, Diagram sDiag, EvenCache tCache, 
            boolean throwaway, ArrayList<CobordInfo<R>> newGuys, ArrayList<Integer> fDiags, int j) {
        cob.setSurgery(i,-1); // no change in diagram
        if (fDiag.circles.size() > sDiag.circles.size()) { // a circle is merged
            if (!sDiag.circles.isEmpty()) {
                int cid = tCache.getPaths().get(sDiag.circles.get(0)).get(0);
                int fid = 0;
                if (!tCache.getPaths().get(fDiag.circles.get(0)).contains(cid)) fid = 1;
                int sid = tCache.getPaths().get(fDiag.circles.get(1-fid)).get(0);
                if (tCache.getPaths().get(sDiag.circles.get(0)).contains(sid)) 
                    throwaway = mergeTwoCirclesOne(cob, cid, sid, tCache, throwaway);
                else throwaway = mergeCircleWithPath(sid, sDiag, tCache, cob, throwaway); // circle containing sid merges with path
            }
            else throwaway = mergeCircleWithPath(fDiag, sDiag, tCache, cob, throwaway); // circle merges with path
        }
        else createCircle(fDiag,sDiag,tCache,cob,newGuys,fDiags,j); // a circle is created
        return throwaway;
    }
    
    private boolean mergeTwoCirclesOne(CobordInfo<R> cob, int cid, int sid, EvenCache tCache, boolean throwaway) {
        boolean a = dotContains(cob.getDottings(),cid,tCache);
        boolean b = dotContains(cob.getDottings(),sid,tCache);
        if (a && b) {
            cob.setDottings(cob.getDottings() - tCache.getPowrs().get(tCache.getPts().indexOf(sid)));
            if (!rasmus) throwaway = true;
        }
        else if (b) {
            cob.setDottings(cob.getDottings() - tCache.getPowrs().get(tCache.getPts().indexOf(sid)));
            cob.setDottings(cob.getDottings() + tCache.getPowrs().get(tCache.getPts().indexOf(cid)));
        }
        return throwaway;
    }
    
    protected boolean dotContains(int dottings, int pathid, EvenCache tCache) {
        int pwr = tCache.getPowrs().get(tCache.getPts().indexOf(pathid));
        return dottings % (2*pwr) >= pwr;
    }
    
    private boolean mergeCircleWithPath(Diagram fDiag, Diagram sDiag, EvenCache tCache, CobordInfo<R> cob, boolean throwaway) {
        int cid = tCache.getPaths().get(fDiag.circles.get(0)).get(0);
        int pid = pathContaining(cid,sDiag.paths,tCache);
        int p = tCache.getPaths().get(pid).get(0);
        boolean a = dotContains(cob.getDottings(),p,tCache);
        boolean b = dotContains(cob.getDottings(),cid,tCache);
        if (a && b) {
            if (!rasmus) throwaway = true;
            else cob.setDottings(cob.getDottings() - tCache.getPowrs().get(tCache.getPts().indexOf(cid)));
        }
        else if (b) {
            cob.setDottings(cob.getDottings() - tCache.getPowrs().get(tCache.getPts().indexOf(cid)));
            cob.setDottings(cob.getDottings() + tCache.getPowrs().get(tCache.getPts().indexOf(p)));
        }
        return throwaway;
    }
    
    private boolean mergeCircleWithPath(int sid, Diagram sDiag, EvenCache tCache, CobordInfo<R> cob, boolean throwaway) {
        int pid = pathContaining(sid,sDiag.paths,tCache);
        int p = tCache.getPaths().get(pid).get(0);
        boolean a = dotContains(cob.getDottings(),p,tCache);
        boolean b = dotContains(cob.getDottings(),sid,tCache);
        if (a && b) {
            cob.setDottings(cob.getDottings() - tCache.getPowrs().get(tCache.getPts().indexOf(sid)));
            if (!rasmus) throwaway = true;
        }
        else if (b) {
            cob.setDottings(cob.getDottings() - tCache.getPowrs().get(tCache.getPts().indexOf(sid)));
            cob.setDottings(cob.getDottings() + tCache.getPowrs().get(tCache.getPts().indexOf(p)));
        }
        return throwaway;
    }
    
    protected int pathContaining(int sid, ArrayList<Integer> pths, EvenCache tCache) {
        boolean found = false;
        int i = 0;
        while (!found && i < pths.size()) {
            ArrayList<Integer> path = tCache.getPaths().get(pths.get(i));
            if (path.contains(sid)) found = true;
            else i++;
        }
        return pths.get(i);
    }
    
    private void createCircle(Diagram fDiag, Diagram sDiag, EvenCache tCache, CobordInfo<R> cob, 
            ArrayList<CobordInfo<R>> newGuys, ArrayList<Integer> fDiags, int j) {
        if (!fDiag.circles.isEmpty()) {
            int cid = tCache.getPaths().get(fDiag.circles.get(0)).get(0);
            int fid = 0;
            if (!tCache.getPaths().get(sDiag.circles.get(0)).contains(cid)) fid = 1;
            int sid = tCache.getPaths().get(sDiag.circles.get(1-fid)).get(0);
            if (tCache.getPaths().get(fDiag.circles.get(0)).contains(sid)) 
                oneCircleIntoTwo(cob,cid,tCache,newGuys,fDiags,sid,j);
            else pathIntoCircle(sid,fDiag,tCache,cob,newGuys,fDiags,j); // path containing sid creates circle
        }
        else pathIntoCircle(tCache,fDiag,sDiag,cob,newGuys,fDiags,j); // a path creates a circle
    }
    
    protected void oneCircleIntoTwo(CobordInfo<R> cob, int cid, EvenCache tCache, ArrayList<CobordInfo<R>> newGuys, 
            ArrayList<Integer> fDiags, int sid, int j) {
        if (dotContains(cob.getDottings(),cid,tCache)) 
            cob.setDottings(cob.getDottings() + tCache.getPowrs().get(tCache.getPts().indexOf(sid)));
        else {
            CobordInfo<R> cob1 = cobClone(cob,true);
            cob1.setDottings(cob1.getDottings() + tCache.getPowrs().get(tCache.getPts().indexOf(sid)));
            newGuys.add(cob1);
            fDiags.add(fDiags.get(j));
            if (rasmus) {
                CobordInfo<R> cob2 = cobClone(cob,false); 
                newGuys.add(cob2);
                fDiags.add(fDiags.get(j));
            }
            cob.setDottings(cob.getDottings() + tCache.getPowrs().get(tCache.getPts().indexOf(cid)));
        }
    }
    
    protected CobordInfo<R> cobClone(CobordInfo<R> cob, boolean b) {
        CobordInfo<R> clob;
        if (b) clob = new CobordInfo<R>(cob.getValue(),cob.getDottings(),cob.getSurgeries().size());
        else clob = new CobordInfo<R>(cob.getValue().negate(),cob.getDottings(),cob.getSurgeries().size());
        for (int w : cob.getSurgeries()) clob.addSurgery(w);
        return clob;
    }
    
    protected void pathIntoCircle(int sid, Diagram fDiag, EvenCache tCache, CobordInfo<R> cob, ArrayList<CobordInfo<R>> newGuys, 
            ArrayList<Integer> fDiags, int j) {
        int pid = pathContaining(sid,fDiag.paths,tCache);
        int p = tCache.getPaths().get(pid).get(0);
        if (dotContains(cob.getDottings(),p,tCache)) 
            cob.setDottings(cob.getDottings()+tCache.getPowrs().get(tCache.getPts().indexOf(sid)));
        else {
            CobordInfo<R> cob1 = cobClone(cob,true);
            cob1.setDottings(cob1.getDottings()+ tCache.getPowrs().get(tCache.getPts().indexOf(sid)));
            newGuys.add(cob1);
            fDiags.add(fDiags.get(j));
            if (rasmus) {
                CobordInfo<R> cob2 = cobClone(cob,false);
                newGuys.add(cob2);
                fDiags.add(fDiags.get(j));
            }
            cob.setDottings(cob.getDottings() + tCache.getPowrs().get(tCache.getPts().indexOf(p)));
        }
    }
    
    protected void pathIntoCircle(EvenCache tCache, Diagram fDiag, Diagram sDiag, CobordInfo<R> cob, ArrayList<CobordInfo<R>> newGuys, 
            ArrayList<Integer> fDiags, int j) {
        int cid = tCache.getPaths().get(sDiag.circles.get(0)).get(0);
        int pid = pathContaining(cid,fDiag.paths,tCache);
        int p = tCache.getPaths().get(pid).get(0);
        if (dotContains(cob.getDottings(),p,tCache)) 
            cob.setDottings(cob.getDottings() + tCache.getPowrs().get(tCache.getPts().indexOf(cid)));
        else {
            CobordInfo<R> cob1 = cobClone(cob,true);
            cob1.setDottings(cob.getDottings() + tCache.getPowrs().get(tCache.getPts().indexOf(cid)));
            newGuys.add(cob1);
            fDiags.add(fDiags.get(j));
            if (rasmus) {
                CobordInfo<R> cob2 = cobClone(cob,false);
                newGuys.add(cob2);
                fDiags.add(fDiags.get(j));
            }// */
            cob.setDottings(cob.getDottings() + tCache.getPowrs().get(tCache.getPts().indexOf(p)));
        }
    }
    
    protected void obtainNewCobordisms(ArrayList<CobordInfo<R>> newCobs, EvenArrow<R> nmor, EvenArrow<R> cmor,
            EvenCache tCache, EvenCache dCache) { // this  caps off the cobordisms
        ArrayList<CobordInfo<R>> relCobs = new ArrayList<CobordInfo<R>>();
        ArrayList<Integer> pEndpts = dCache.getPts();
        ArrayList<Integer> relCircles = new ArrayList<Integer>(2);
        for (int c : tCache.getDiagram(cmor.getBotDiagram()).circles) relCircles.add(tCache.getPaths().get(c).get(0));
        for (CobordInfo<R> cob : newCobs) {
            int relDottings = 0;
            for (int y = 0; y < pEndpts.size(); y++) 
                if (dotContains(cob.getDottings(),pEndpts.get(y),tCache)) 
                    relDottings = relDottings + tCache.getPowrs().get(y);
            for (Cobordism<R> cb : cmor.getCobordisms()) {
                boolean okay = true;
                for (int c : relCircles) {
                    boolean a = dotContains(cob.getDottings(), c, tCache);
                    boolean b = dotContains(cb.getDottings(), c, tCache);
                    if (!a && !b) okay = false;
                    if (!rasmus && (a & b)) okay = false;
                }
                if (okay) 
                    relCobs.add(new CobordInfo<R>(cob.getValue().multiply(cb.getValue()),relDottings,cloneList(cob.getSurgeries()))); 
            }
        }
        simplifyCobordisms(relCobs, dCache, nmor.getBotGenerator().getDiagram());
        for (CobordInfo<R> cob : relCobs) {
            long surgs = dCache.getSurgeries(cob.getSurgeries());
            nmor.addCobordism(new Cobordism<R>(cob.getValue(),cob.getDottings(),surgs));
        }
    }
    
    protected void simplifyCobordisms(ArrayList<CobordInfo<R>> newCobs, EvenCache dCache, int stDig) {
        // check for double surgeries, whether dots  can be moved to smaller points, whether some cobordisms are the same.
        checkDoubleSurgeries(newCobs,dCache,stDig);
        checkMovingDots(newCobs,dCache,stDig);
        checkWhetherSame(newCobs,stDig);
    }
    
}
