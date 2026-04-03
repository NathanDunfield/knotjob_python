/*

Copyright (C) 2020-22 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Cache;
import knotjob.homology.Generator;
import knotjob.homology.TangleComplex;
import knotjob.homology.Diagram;
import knotjob.links.Link;
import knotjob.rings.Ring;

/**
 *
 * @author dirk
 * @param <R>
 */
public class OddComplex<R extends Ring<R>> extends TangleComplex<R> {
    
    protected Cache cache;
    protected final boolean rasmus;
    protected ArrayList<ArrayList<Integer>> closurePaths;
    protected ArrayList<ArrayList<Integer>> nextClosurePaths;
    protected ArrayList<int[]> closureDiagrams;
    public boolean debug;
    
    public OddComplex(R unt) {
        super(unt);
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public OddComplex(int comp, R unt, AbortInfo abInf, DialogWrap frm,
            boolean ras) { // should only be used with comp = 0 or 1
        super(unt, abInf, frm);
        rasmus = ras;
        ArrayList<Generator<R>> genList = new ArrayList<Generator<R>>(comp+1);
        int shift = -comp;
        for (int i = 0; i <= comp; i++) {
            OddGenerator<R> gen = new OddGenerator<R>(0, 0, shift);
            genList.add(gen);
            shift = shift + 2;
        }
        generators.add(genList);
    }
    
    public OddComplex(R unt, boolean rms, AbortInfo ab, DialogWrap frm) {
        super(unt, ab, frm);
        rasmus = rms;
    }
    
    public OddComplex(int crs, int[] ends, int hstart, int qstart, boolean rev, R unt, 
            DialogWrap frm, AbortInfo abt, boolean ras) {
        super(unt, abt, frm);
        rasmus = ras;
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
        OddGenerator<R> bGen = new OddGenerator<R>(1-std, hstart, qstart);
        ArrayList<Generator<R>> hObjs = new ArrayList<Generator<R>>(1);
        hObjs.add(bGen);
        generators.add(hObjs);
        OddGenerator<R> tGen = new OddGenerator<R>(std, hstart+1, qstart+1);
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
    
    public OddGenerator<R> getGenerator(int i, int j) {
        ArrayList<Generator<R>> gns = getGenerators(i);
        return (OddGenerator<R>) gns.get(j);
    }
    
    public Diagram getDiagram(int i) {
        return cache.getDiagram(i);
    }// */
    
    public int diagramSize() {
        return cache.diagramSize();
    }
    
    public int getPaths(int i, int j) {
        return cache.getPath(i, j);
    }
    
    public ArrayList<Integer> getPath(int i) {
        return cache.getPaths(i);
    }
    
    public void modifyComplex(OddComplex<R> complex, String girth, boolean det) {
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
    
    private void getNewObjects(OddComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, Cache tCache, Cache dCache,
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
            if (!debug && i < generators.size()-2) {
                boolean cancel = true;
                while (cancel) cancel = gaussEliminate(i+t+1, dobjs, dCache, det);// we cancel as long as we can
            }// */
            i--;
        }
        cache = dCache;
        generators = dobjs;
    }
    
    private void createCone(int i, int t, OddComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, 
            Cache tCache, int[][] diagTrans, ArrayList<ArrayList<Generator<R>>> tobjs) {
        ArrayList<Generator<R>> gens = generators.get(i);
        for (int l = 0; l < gens.size(); l++) {
            if (abInf.isAborted()) return;
            OddGenerator<R> fGen = (OddGenerator<R>) gens.get(l);
            ArrayList<Generator<R>> nGens = new ArrayList<Generator<R>>(t);
            for (int k = t-1; k >= 0; k--) {
                OddGenerator<R> sGen = (OddGenerator<R>) complex.generators.get(k).get(0);
                int dnum = getDiagNumber(fGen.getDiagram(), sGen.getDiagram(), complex, pEndpts, nEndpts, tCache, diagTrans);
                OddGenerator<R> nGen = new OddGenerator<R>(dnum,fGen.hdeg()+sGen.hdeg(),fGen.qdeg()+sGen.qdeg());
                tobjs.get(i+k).add(nGen);
                nGens.add(0, nGen);
                for (Arrow<R> arr : sGen.getBotArrows()) { // there is either one or no arrow
                    OddGenerator<R> ntGen = (OddGenerator<R>) nGens.get(1);
                    Chronology<R> oldChr = ((OddArrow<R>) arr).getChronology(0);
                    OddSurgery oldSur = oldChr.getSurgery(0);
                    Chronology<R> nChr = new Chronology<R>(oldChr.getValue(), oldSur.getFPath(), oldSur.getSPath(), ntGen.getDiagram(), 
                            oldSur.getTurn());
                    OddArrow<R> nArrow = new OddArrow<R>(nGen, ntGen, nChr);
                    nGen.addBotArrow(nArrow);
                }
            }
            fGen.clearTopArr();
            OddArrow<R> pointer = new OddArrow<R>(fGen,(OddGenerator<R>) nGens.get(0)); 
            fGen.addTopArrow(pointer);
            createArrowsF(fGen, complex, pEndpts, nEndpts, tCache, diagTrans);
            createArrowsG(fGen, complex, pEndpts, nEndpts, tCache, diagTrans);
            fGen.clearBotArr();
            counter = counter + t;
            frame.setLabelRight(""+counter, 2, false);
        }
        if (i < generators.size()-1) generators.set(i+1, null); // throwing away old objects of hom degree i+1
    }

    public void setClosure(Link theLink) {
        closureDiagrams = new ArrayList<int[]>();
        for (int i = 1; i < theLink.crossingLength(); i++) {
            int[] dig = new int[4];
            if (theLink.getCross(i)>0) {
                for (int j = 0; j < 4; j++) dig[j] = theLink.getPath(i, j);
            }
            else {
                for (int j = 4; j > 0; j--) dig[j%4] = theLink.getPath(i, 4-j);
            }
            closureDiagrams.add(dig);
        }
        closurePaths = setClosurePaths(posEndpts, negEndpts);
        setClosureDiagram(closurePaths);
    }

    protected ArrayList<ArrayList<Integer>> setClosurePaths(ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts) {
        if (closureDiagrams.isEmpty() || pEndpts.isEmpty()) return new ArrayList<ArrayList<Integer>>(0);
        ArrayList<ArrayList<Integer>> clPaths = new ArrayList<ArrayList<Integer>>();
        int[] first = closureDiagrams.get(0);
        boolean revert = toRevert(first, pEndpts, nEndpts);
        addPaths(clPaths, first, revert);
        for (int i = 1; i < closureDiagrams.size(); i++) {
            int[] next = closureDiagrams.get(i);
            if (notConnected(clPaths, pEndpts, nEndpts, next)) break;
            revert = toRevert(pEndpts, nEndpts, next, clPaths);
            addPaths(clPaths, next, revert);
        }
        combinePaths(clPaths);
        return clPaths;
    }
    
    private boolean notConnected(ArrayList<ArrayList<Integer>> clPaths, 
            ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, int[] next) {
        int i = 0;
        while (i < clPaths.size()) {
            ArrayList<Integer> path = clPaths.get(i);
            int e = path.get(path.size()-1);
            int a = path.get(0);
            if (a == next[0] || e == next[0]) return false;
            if (a == next[1] || e == next[1]) return false;
            if (a == next[2] || e == next[2]) return false;
            if (a == next[3] || e == next[3]) return false;
            i++;
        }
        i = 0;
        while (i < pEndpts.size()) {
            int e = pEndpts.get(i);
            int a = nEndpts.get(i);
            if (a == next[0] || e == next[0]) return false;
            if (a == next[1] || e == next[1]) return false;
            if (a == next[2] || e == next[2]) return false;
            if (a == next[3] || e == next[3]) return false;
            i++;
        }
        return true;
    }
    
    protected Diagram setClosureDiagram(ArrayList<ArrayList<Integer>> clPaths) {
        Diagram clDiagram = new Diagram();
        if (closureDiagrams.isEmpty()) return clDiagram;
        for (int i = 0; i < clPaths.size(); i++) {
            ArrayList<Integer> path = clPaths.get(i);
            if (!Objects.equals(path.get(0), path.get(path.size()-1))) clDiagram.paths.add(i);
            else clDiagram.circles.add(i);
        } 
        return clDiagram;
    }

    private boolean toRevert(ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, int[] next, ArrayList<ArrayList<Integer>> paths) {
        boolean revert = false;
        boolean found = false;
        int i = 0;
        while (!found && i < paths.size()) {
            ArrayList<Integer> path = paths.get(i);
            int index = posOf(path, next); // next[index] is contained in path, or index -1
            if (index >= 0) {
                found = true;
                if (path.get(0) == next[index]) revert = (index % 2 == 0);
                else revert = (index %2 != 0);
            }
            i++;
        }
        if (found) return revert;
        i = 0;
        while (!found && i < 4) {
            if (pEndpts.contains(next[i])) found = true;
            else i++;
        }
        if (found) return (i%2 == 0);
        i = 0;
        while (!found ) {
            if (nEndpts.contains(next[i])) found = true;
            else i++;
        }
        return (i%2 != 0);
    }
    
    private int posOf(ArrayList<Integer> path, int[] next) {
        boolean found = false;
        int i = 0;
        while (!found && i < 4) {
            if (path.contains(next[i])) found = true;
            else i++;
        }
        if (found) return i;
        return -1;
    }
    
    private boolean toRevert(int[] first, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts) {
        boolean revert = false;
        boolean found = false;
        int i = 0;
        while (!found) {
            if (pEndpts.contains(first[i])) {
                revert = (i%2 == 0);
                found = true;
            }
            if (nEndpts.contains(first[i])) {
                revert = (i%2 != 0);
                found = true;
            }
            i++;
        }
        return revert;
    }

    private void addPaths(ArrayList<ArrayList<Integer>> paths, int[] first, boolean revert) {
        ArrayList<Integer> pathOne = new ArrayList<Integer>();
        pathOne.add(first[0]);
        if (revert) pathOne.add(0, first[1]);
        else pathOne.add(first[1]);
        combinePaths(paths, pathOne);
        ArrayList<Integer> pathTwo = new ArrayList<Integer>();
        pathTwo.add(first[2]);
        if (revert) pathTwo.add(0, first[3]);
        else pathTwo.add(first[3]);
        combinePaths(paths, pathTwo);
    }

    private void combinePaths(ArrayList<ArrayList<Integer>> paths, ArrayList<Integer> path) {
        boolean found = false;
        int i = 0;
        while (!found && i < paths.size()) {
            if (paths.get(i).contains(path.get(0)) || paths.get(i).contains(path.get(1))) found = true;
            else i++;
        }
        if (found) {
            ArrayList<Integer> cpath = paths.get(i);
            if (cpath.contains(path.get(1))) cpath.add(0, path.get(0));
            else cpath.add(path.get(1));
        }
        else paths.add(path);
    }
    
    private void combinePaths(ArrayList<ArrayList<Integer>> paths) {
        int i = paths.size()-1;
        while (i>0) {
            ArrayList<Integer> lastPath = paths.get(i);
            boolean found = false;
            int j = 0;
            while (!found && j < i) {
                ArrayList<Integer> checkPath = paths.get(j);
                if (Objects.equals(checkPath.get(0), lastPath.get(lastPath.size()-1)) || 
                        Objects.equals(checkPath.get(checkPath.size()-1), lastPath.get(0))) {
                    found = true;
                    combine(checkPath, lastPath);
                    paths.remove(i);
                }
                else j++;
            }
            i--;
        }
    }
    
    private void combine(ArrayList<Integer> first, ArrayList<Integer> second) { // adds the second path to the first
        if (Objects.equals(first.get(0), second.get(second.size()-1))) {
            for (int j = second.size()-2; j >= 0; j--) first.add(0, second.get(j));
        }
        else {
            for (int j = 1; j < second.size(); j++) first.add(second.get(j));
        }
    }

    private void shrinkPaths(ArrayList<ArrayList<Integer>> paths, boolean dropCircles) {
        int i = paths.size()-1;
        while (i>=0) {
            ArrayList<Integer> path = paths.get(i);
            while (path.size()>2) path.remove(1);
            if (dropCircles) {
                if (Objects.equals(path.get(0), path.get(1))) paths.remove(i);
            }
            i--;
        }
    }

    private void removeCircles(ArrayList<ArrayList<Integer>> paths) {
        int i = paths.size()-1;
        while (i>=0) {
            ArrayList<Integer> path = paths.get(i);
            if (Objects.equals(path.get(0), path.get(path.size()-1))) paths.remove(i);
            i--;
        }
    }
    
    private Diagram combineDiagram(int fdiagram, int nsdiagram, OddComplex<R> complex, ArrayList<Integer> pEndpts, 
            ArrayList<Integer> nEndpts, Cache tCache) {
        Diagram fDiag = cache.getDiagram(fdiagram);
        Diagram sDiag = complex.getDiagram(nsdiagram);
        ArrayList<ArrayList<Integer>> newPaths = new ArrayList<ArrayList<Integer>>();
        for (int i : fDiag.paths) {
            ArrayList<Integer> cpath = new ArrayList<Integer>();
            for (int y : cache.getPaths(i)) cpath.add(y);
            newPaths.add(cpath);
        }
        for (int j : sDiag.paths) {
            ArrayList<Integer> cpath = new ArrayList<Integer>();
            for (int y : complex.getPath(j)) cpath.add(y);
            newPaths.add(cpath);
        }
        for (int j : sDiag.circles) { // we're allowing circles in the second diagram
            ArrayList<Integer> cpath = new ArrayList<Integer>();
            for (int y : complex.getPath(j)) cpath.add(y);
            newPaths.add(cpath);
        }
        for (int e : posEndpts) if (!pEndpts.contains(e)) combinePaths(newPaths,e,true);
        for (int e : negEndpts) if (!nEndpts.contains(e)) combinePaths(newPaths,e,false);
        ArrayList<ArrayList<Integer>> ncircles = getCircles(newPaths);
        Diagram nDiag = new Diagram();
        nDiag.paths = new ArrayList<Integer>(newPaths.size());
        nDiag.circles = new ArrayList<Integer>(ncircles.size());
        for (ArrayList<Integer> npth : newPaths) {
            int p = tCache.getPathNumber(npth);
            nDiag.paths.add(p);
        }
        for (ArrayList<Integer> ncir : ncircles) {
            int p = tCache.getPathNumber(ncir);
            nDiag.circles.add(p);
        }
        return nDiag;
    }

    protected int getDiagNumber(int fdig, int sdig, OddComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, 
            Cache tCache, int[][] diagTrans) {
        int dnum = diagTrans[fdig][sdig]-1;
        if (dnum == -1) {
            Diagram nDiag = combineDiagram(fdig,sdig,complex,pEndpts,nEndpts,tCache);
            dnum = tCache.getDiagNumber(nDiag);
            diagTrans[fdig][sdig] = dnum+1;
        }
        return dnum;
    }

    protected void createArrowsF(OddGenerator<R> fGen, OddComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, 
            Cache tCache, int[][] diagTrans) {
        OddGenerator<R> nGen = newTopGenerator(fGen, false);
        OddGenerator<R> sGen = complex.getGenerator(0,0);
        for (Arrow<R> arrow : fGen.getBotArrows()) {
            OddGenerator<R> ntGen = newTopGenerator((OddGenerator<R>) arrow.getTopGenerator(), false);
            ArrayList<Chronology<R>> newChrons = new ArrayList<Chronology<R>>();
            for (int i = 0; i < ((OddArrow) arrow).chronologySize(); i++) {
                Chronology<R> chron = ((OddArrow<R>) arrow).getChronology(i);
                ArrayList<OddSurgery> newSurgeries = new ArrayList<OddSurgery>();
                for (int j = 0; j < chron.surgerySize(); j++) {
                    OddSurgery surg = chron.getSurgery(j);
                    int dnum = getDiagNumber(surg.getEnd(), sGen.getDiagram(), complex, pEndpts, nEndpts, tCache, diagTrans);
                    OddSurgery nSurg = new OddSurgery(surg.getFPath(), surg.getSPath(), dnum, surg.getTurn());
                    newSurgeries.add(nSurg);
                }
                ArrayList<Integer> newDots = adaptDottings(chron.getDottings(), nGen, tCache);
                if (newDots != null) {
                    Chronology<R> nChron = new Chronology<R>(chron.getValue(), newDots, newSurgeries);
                    newChrons.add(nChron);
                }
            }
            OddArrow<R> nArrow = new OddArrow<R>(nGen, ntGen, newChrons);
            nGen.addBotArrow(nArrow);
        }
    }
    
    protected ArrayList<Integer> adaptDottings(ArrayList<Integer> oldDots, OddGenerator<R> nGen, Cache tCache) { 
                                                                                // moves a dot in the middle of a path to a pos endpoint
        ArrayList<Integer> newDots = new ArrayList<Integer>();
        for (int d : oldDots) {
            int nd = getNewDot(d, nGen, tCache);
            if (newDots.contains(nd)) {
                if (!rasmus) return null;
            }
            else newDots.add(nd);
        }
        return newDots;
    }
    
    private int getNewDot(int oDot, OddGenerator<R> nGen, Cache tCache) {
        boolean found = false;
        int i = 0;
        Diagram diag = tCache.getDiagram(nGen.getDiagram());
        while (!found && i < diag.paths.size()) {
            if (tCache.getPaths(diag.paths.get(i)).contains(oDot)) found = true;
            else i++;
        }
        if (found) return tCache.getPaths(diag.paths.get(i)).get(0);
        i = 0;
        while (!found) {
            if (tCache.getPaths(diag.circles.get(i)).contains(oDot)) found = true;
            else i++;
        }
        ArrayList<Integer> circle = tCache.getPaths(diag.circles.get(i));
        found = false;
        i = 0;
        while (!found) {
            if (circle.contains(tCache.getDot(i))) found = true;
            else i++;
        }
        return tCache.getDot(i);
    }
    
    protected void createArrowsG(OddGenerator<R> fGen, OddComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, 
            Cache tCache, int[][] diagTrans) {
        OddGenerator<R> nGen = newTopGenerator(fGen, true);
        OddGenerator sGen = complex.getGenerator(1,0);
        OddSurgery sSurg = complex.getGenerator(0,0).getBotArrow(0).getChronology(0).getSurgery(0);
        for (Arrow<R> arrow : fGen.getBotArrows()) {
            OddGenerator<R> ntGen = newTopGenerator((OddGenerator<R>) arrow.getTopGenerator(), true);
            ArrayList<Chronology<R>> newChrons = new ArrayList<Chronology<R>>();
            for (int i = 0; i < ((OddArrow<R>) arrow).chronologySize(); i++) {
                Chronology<R> chron = ((OddArrow<R>) arrow).getChronology(i);
                R val = chron.getValue();
                ArrayList<ArrayList<Integer>> circles = closeCircles(cache.getDiagram(fGen.getDiagram()), closurePaths, cache);
                SurgeryDiagram surDig = new SurgeryDiagram(circles, sSurg);
                boolean change = surDig.alternate(chron.getDottings());
                ArrayList<OddSurgery> newSurgeries = new ArrayList<OddSurgery>();
                for (int j = 0; j < chron.surgerySize(); j++) {
                    OddSurgery surg = chron.getSurgery(j);
                    if (!surDig.alternate(surg)) change = !change;
                    if (j < chron.surgerySize()-1) {
                        circles = closeCircles(cache.getDiagram(surg.getEnd()), closurePaths, cache);
                        surDig.setCircles(circles);
                    }
                    int dnum = getDiagNumber(surg.getEnd(), sGen.getDiagram(), complex, pEndpts, nEndpts, tCache, diagTrans);
                    OddSurgery nSurg = new OddSurgery(surg.getFPath(), surg.getSPath(), dnum, surg.getTurn());
                    newSurgeries.add(nSurg);
                }
                if (change) val = val.negate();
                ArrayList<Integer> newDots = adaptDottings(chron.getDottings(), nGen, tCache);
                if (newDots != null) {
                    Chronology<R> nChron = new Chronology<R>(val.negate(), newDots, newSurgeries);
                    newChrons.add(nChron);
                }
            }
            OddArrow<R> nArrow = new OddArrow<R>(nGen, ntGen, newChrons);
            nGen.addBotArrow(nArrow);
        }
    }

    protected ArrayList<ArrayList<Integer>> closeCircles(Diagram dig, ArrayList<ArrayList<Integer>> clPaths, Cache dCache) {
        ArrayList<ArrayList<Integer>> pths = clonePaths(clPaths);
        for (int i : dig.paths) pths.add(clonePath(dCache.getPaths(i)));
        combinePaths(pths);
        return pths;
    }

    private ArrayList<ArrayList<Integer>> clonePaths(ArrayList<ArrayList<Integer>> clPaths) {
        ArrayList<ArrayList<Integer>> clones = new ArrayList<ArrayList<Integer>>(clPaths.size());
        for (ArrayList<Integer> path : clPaths) clones.add(clonePath(path));
        return clones;
    }

    protected ArrayList<Integer> clonePath(ArrayList<Integer> path) {
        ArrayList<Integer> clone = new ArrayList<Integer>(path.size());
        for (Integer i : path) clone.add(i);
        return clone;
    }

    protected OddGenerator<R> newTopGenerator(OddGenerator<R> oddGenerator, boolean one) {
        OddGenerator<R> top = oddGenerator.getTopArrow(0).getTopGenerator();
        if (one) return top.getBotArrow(0).getTopGenerator();
        return top;
    }
    
    protected void deloopGens(int i, ArrayList<ArrayList<Generator<R>>> tobjs, ArrayList<ArrayList<Generator<R>>> dobjs, 
            Cache tCache, Cache dCache, ArrayList<Integer> ddigTrans) {
        while (tCache.diagramSize() > ddigTrans.size()) ddigTrans.add(-1);
        for (Iterator<Generator<R>> it = tobjs.get(i).iterator(); it.hasNext();) {
            OddGenerator<R> oGen = (OddGenerator<R>) it.next();
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
        tobjs.set(i, null);
    }
    
    private void noDeloop( OddGenerator<R> oGen, int ndig, Cache tCache, Cache dCache, ArrayList<Generator<R>> dobs, 
            ArrayList<Integer> ddigTrans) {
        OddGenerator<R> nGen = new OddGenerator<R>(ndig, oGen.hdeg(), oGen.qdeg());
        Chronology<R> cchr = new Chronology<R>(unit, new ArrayList<Integer>());
        OddArrow<R> carr = new OddArrow<R>(oGen, nGen, cchr);
        oGen.addTopArrow(carr);
        createNewArrows(nGen, oGen, cchr, tCache, dCache, ddigTrans);
        dobs.add(nGen);
    }
    
    protected void createNewArrows(OddGenerator<R> nGen, OddGenerator<R> oGen, Chronology<R> cchr, Cache tCache, Cache dCache, 
            ArrayList<Integer> ddigTrans) {
        for (Iterator<Arrow<R>> it = oGen.getBotArrows().iterator(); it.hasNext();) {
            OddArrow<R> arrow = (OddArrow<R>) it.next();
            ArrayList<Chronology<R>> newChrons = new ArrayList<Chronology<R>>();
            for (Chronology<R> fchr : arrow.getChronologies()) {
                ArrayList<Integer> newDots = combineDottings(fchr.getDottings(),cchr.getDottings());
                if (newDots != null) {
                    Chronology<R> nchr = new Chronology<R>(fchr.getValue().multiply(cchr.getValue()), newDots, fchr.getSurgeries());
                    newChrons.add(nchr);
                }
            }
            newChrons = modifyChronologies(newChrons, tCache, dCache, ddigTrans, oGen.getDiagram());
            for (int i = 0; i < arrow.getTopGenerator().getTopArrowSize(); i++) {
                OddArrow<R> carr = arrow.getTopGenerator().getTopArrow(i);
                OddGenerator<R> tnGen = carr.getTopGenerator();
                OddArrow<R> narr = new OddArrow<R>(nGen, tnGen);
                obtainNewChronologies(newChrons, narr, carr, tCache, dCache);
                if (!narr.isEmpty()) {
                    nGen.addBotArrow(narr);
                    tnGen.addTopArrow(narr);
                }
            }
        }
    }
    
    protected void obtainNewChronologies(ArrayList<Chronology<R>> newChrons, OddArrow<R> narr, OddArrow<R> carr,
            Cache tCache, Cache dCache) { // this  caps off the cobordisms
        ArrayList<Chronology<R>> relChrons = new ArrayList<Chronology<R>>();
        ArrayList<Integer> relCircles = new ArrayList<Integer>(2);
        for (int c : tCache.getDiagram(carr.getBotDiagram()).circles) 
            relCircles.add(dotOfCircle(tCache.getPaths(c), tCache.getPts()));
        for (Chronology<R> chr : newChrons) {
            for (Chronology<R> cb : carr.getChronologies()) {
                R val = chr.getValue();
                ArrayList<Integer> clDots = clonePath(chr.getDottings());
                boolean okay = true;
                for (Integer c : relCircles) {
                    boolean b = chr.getDottings().contains(c);
                    boolean a = cb.getDottings().contains(c);
                    if (!a && !b) okay = false;
                    if (!rasmus && (a & b)) okay = false;
                    if (clDots.contains(c)) {
                        int ind = clDots.indexOf(c);
                        if (ind % 2 != 0) val = val.negate();
                        if (numberOfSplits(chr.getSurgeries(), dCache) % 2 != 0) val = val.negate();
                        clDots.remove(c);
                    }
                }
                if (okay) relChrons.add(new Chronology<R>(chr, clDots, val));
            }
        }
        simplifyChronologies(relChrons, dCache, narr.getBotGenerator().getDiagram());
        for (Chronology<R> chr : relChrons) {
            narr.addChronology(chr);
        }
        //if (!checkqDegree(narr)) System.out.println("Problem");
    }
    
    private ArrayList<Integer> combineDottings(ArrayList<Integer> secDots, ArrayList<Integer> firDots) {
        ArrayList<Integer> newDots = new ArrayList<Integer>();
        for (int d : firDots) newDots.add(d);
        for (int i = secDots.size()-1; i>=0; i--) {
            int d = secDots.get(i);
            if (newDots.contains(d)) {
                if (!rasmus) return null;
            }
            else newDots.add(0, d);
        }
        return newDots;
    }
    
    protected void removeMorphisms(int girth) {
        for (ArrayList<Generator<R>> gens : generators) {
            for (Generator<R> gen : gens) {
                int qb = ((OddGenerator<R>)gen).qdeg();
                int i = gen.getBotArrows().size()-1;
                while (i >= 0) {
                    OddArrow<R> arr = (OddArrow<R>) gen.getBotArrows().get(i);
                    if (arr.getTopGenerator().qdeg()-qb > girth) {
                        gen.getBotArrows().remove(i);
                        arr.getTopGenerator().removeTopArrow(arr);
                    }
                    i--;
                }
            }
        }
    }
    
    private ArrayList<Chronology<R>> modifyChronologies(ArrayList<Chronology<R>> newChrons, Cache tCache, Cache dCache, 
            ArrayList<Integer> digTrans, int stDig) {
        ArrayList<Chronology<R>> newOnes = new ArrayList<Chronology<R>>();
        for (Chronology<R> chron : newChrons) {
            ArrayList<Chronology<R>> fromChron = new ArrayList<Chronology<R>>();
            Chronology<R> start = new Chronology<R>(chron.getValue(), chron.getDottings());
            fromChron.add(start);
            int dig = stDig;
            for (OddSurgery surg : chron.getSurgeries()) {
                fromChron = modifyFromChron(fromChron, surg, tCache, dCache, digTrans, dig);
                dig = surg.getEnd();
            }
            for (Chronology<R> from : fromChron) newOnes.add(from);
        }
        return newOnes;
    }
    
    private ArrayList<Chronology<R>> modifyFromChron(ArrayList<Chronology<R>> fromChron, OddSurgery surg, Cache tCache, Cache dCache, 
            ArrayList<Integer> digTrans, int std) {
        ArrayList<Chronology<R>> newOnes = new ArrayList<Chronology<R>>();
        Diagram stDig = tCache.getDiagram(std);
        Diagram nxDig = tCache.getDiagram(surg.getEnd());
        int change = stDig.circles.size() - nxDig.circles.size();
        ArrayList<Chronology<R>> nOns = new ArrayList<Chronology<R>>();
        for (Chronology<R> chron : fromChron) {
            if (change == 0) nOns = shortenSurgery(chron, surg, tCache, dCache, digTrans, stDig, nxDig);
            if (change == 1) nOns = absorbCircle(chron, surg, tCache, dCache, digTrans, stDig, nxDig);
            if (change == -1) nOns = splitCircle(chron, surg, tCache, dCache, nxDig);
            for (Chronology<R> chr : nOns) newOnes.add(chr);
        }
        return newOnes;
    }
    
    private ArrayList<Chronology<R>> shortenSurgery(Chronology<R> chron, OddSurgery surg, 
            Cache tCache, Cache dCache, ArrayList<Integer> digTrans, Diagram stDig, Diagram nxDig) { // surg remains a surgery
        ArrayList<Chronology<R>> newOnes = new ArrayList<Chronology<R>>(1);
        Chronology<R> nChron = new Chronology<R>(chron, chron.getDottings());
        int nDigNr = digTrans.get(surg.getEnd());
        if (nDigNr == -1) nDigNr = newDiagNumber(nxDig, tCache, dCache, digTrans, surg.getEnd());
        int[] fp = surgeryPath(surg.getFPath(0), stDig, tCache);
        int[] sp = surgeryPath(surg.getSPath(0), stDig, tCache);
        OddSurgery nSurg = new OddSurgery(fp[0], fp[1], sp[0], sp[1], nDigNr, surg.getTurn());
        nChron.addSurgery(nSurg);
        newOnes.add(nChron);
        return newOnes;
    }
    
    private ArrayList<Chronology<R>> absorbCircle(Chronology<R> chron, OddSurgery surg, 
            Cache tCache, Cache dCache, ArrayList<Integer> digTrans, Diagram stDig, Diagram nxDig) { // surg will absorb a circle
        ArrayList<Chronology<R>> newOnes = new ArrayList<Chronology<R>>(1);
        int fdot = startOfPath(surg.getFPath(0), stDig, tCache);
        int sdot = startOfPath(surg.getSPath(0), stDig, tCache);
        int cNum = numberOfCircles(fdot, sdot, dCache);
        if (cNum == 1) {
            Chronology<R> newChron = absorbOneCircle(chron, fdot, sdot, dCache);
            if (newChron != null) newOnes.add(newChron);
        }
        else {
            Chronology<R> newChron = mashTwoCircles(chron, tCache, nxDig, fdot, sdot);
            if (newChron != null) newOnes.add(newChron);
        }
        return newOnes;
    }

    private int numberOfCircles(int fdot, int sdot, Cache dCache) {
        int circs = 0;
        if (!dCache.dotsContain(fdot)) circs++;
        if (!dCache.dotsContain(sdot)) circs++;
        return circs;
    }
    
    private Chronology<R> absorbOneCircle(Chronology<R> chron, int fdot, int sdot, Cache dCache) {
        ArrayList<Integer> dots = chron.getDottings();
        R val = chron.getValue();
        int theCircle = theCircleOf(fdot, sdot, dCache);
        int thePath = thePathOf(fdot, sdot, dCache);
        int pos = dots.indexOf(theCircle);
        if (pos >= 0) {
            int shuff = shuffleDots(thePath, theCircle, dots, chron, chron.surgerySize()-1);
            if (shuff == 0) return null;
        }
        return new Chronology<R>(chron, dots, val);
    }
    
    protected int theCircleOf(int fdot, int sdot, Cache dCache) {
        if (dCache.dotsContain(fdot)) return sdot;
        return fdot;
    }
    
    protected int thePathOf(int fdot, int sdot, Cache dCache) {
        if (dCache.dotsContain(fdot)) return fdot;
        return sdot;
    }
    
    private int shuffleDots(int thePath, int theCirc, ArrayList<Integer> dots, Chronology<R> chron, int j) {
        int factor = 1;
        while (j >= 0) {
            OddSurgery surg = chron.getSurgery(j);
            if (surg.involves(thePath)) thePath = surg.smallestEnd();
            j--;
        }
        int pos = dots.indexOf(theCirc);
        if (dots.contains(thePath)) {
            if (rasmus) {
                dots.remove(pos);
            }
            else factor = 0;
        }
        else dots.set(pos, thePath);
        return factor;
    }
    
    protected void simplifyChronologies(ArrayList<Chronology<R>> newChrons, Cache dCache, int stDig) {
        // check for double surgeries, whether dots  can be moved to smaller points, whether some cobordisms are the same.
        checkDoubleSurgeries(newChrons,dCache,stDig);
        checkWhetherSame(newChrons);
    }
    
    private void checkDoubleSurgeries(ArrayList<Chronology<R>> newChrons, Cache dCache, int stDig) {
        int i = 0;
        while (i < newChrons.size()) {
            Chronology<R> chron = newChrons.get(i);
            ArrayList<OddSurgery> surgs = chron.getSurgeries();
            int fir = stDig;
            int j = -1;
            boolean found = false;
            while (!found && j < surgs.size()-2) {
                int sec = surgs.get(j+2).getEnd();
                if (fir == sec) {
                    found = true;
                    removeDoubleSurgery(j+1, chron, newChrons, dCache);
                }
                else {
                    j++;
                    fir = surgs.get(j).getEnd();
                }
            }
            if (!found) i++;
        }
    }
    
    protected void removeDoubleSurgery(int j, Chronology<R> chron, ArrayList<Chronology<R>> newChrons, Cache dCache) {
        int dig = chron.getSurgery(j+1).getEnd();
        OddSurgery ssurg = chron.getSurgery(j+1);
        ArrayList<ArrayList<Integer>> circles = closeCircles(dCache.getDiagram(dig), nextClosurePaths, dCache);
        SurgeryDiagram surDig = new SurgeryDiagram(circles, chron.getSurgery(j));
        boolean change = surDig.split();
        if (change) change = surDig.alternate(ssurg);
        R val = chron.getValue();
        chron.removeSurgery(j);
        chron.removeSurgery(j);
        newChrons.remove(chron);
        int splits = numberOfSplits(chron.getSurgeries(), j, dCache);
        if (splits % 2 != 0) change = !change;
        if (change) val = val.negate();
        ArrayList<Integer> nDots = chron.getDottings();
        ArrayList<Integer> nDot = new ArrayList<Integer>(1);
        if (ssurg.getTurn()) nDot.add(ssurg.getFPath(0));
        else nDot.add(ssurg.getSPath(0));
        nDots = newDotsFrom(nDots, nDot, chron.getSurgeries());
        if (nDots != null) newChrons.add(new Chronology<R>(val, nDots, chron.getSurgeries()));
        nDot = new ArrayList<Integer>(1);
        if (ssurg.getTurn()) nDot.add(ssurg.getSPath(0));
        else nDot.add(ssurg.getFPath(0));
        nDots = newDotsFrom(chron.getDottings(), nDot, chron.getSurgeries());
        if (nDots != null) newChrons.add(new Chronology<R>(val.negate(), nDots, chron.getSurgeries()));
        if (rasmus) {
            newChrons.add(new Chronology<R>(val.negate(), chron.getDottings(), chron.getSurgeries()));
        }// */
    }
    
    protected void checkWhetherSame(ArrayList<Chronology<R>> newChrons) {
        int i = newChrons.size()-1;
        while (i>= 1) {
            Chronology<R> lChron = newChrons.get(i);
            int j = 0;
            boolean found = false;
            int sameChron = 0;
            while (!found && j < i) {
                Chronology<R> fChron = newChrons.get(j);
                sameChron = sameChronUpToSign(fChron, lChron);
                if (sameChron != 0) found = true;
                else j++;
            }
            if (sameChron != 0) {
                R val = lChron.getValue();
                if (sameChron < 0) val = val.add(newChrons.get(j).getValue().negate());
                else val = val.add(newChrons.get(j).getValue());
                if (val.isZero()) {
                    newChrons.remove(j);
                    i--;
                }
                else newChrons.set(j, new Chronology<R>(val, lChron.getDottings(), lChron.getSurgeries()));
                newChrons.remove(lChron);
                
            }
            i--;
        }
    }
    
    private int sameDottings(Chronology<R> fChron, Chronology<R> sChron) { // 0 means different, 1 means same up to positive permutation
        int count = 0;                                                      // -1 same up to negative permutation
        int i = 0;
        ArrayList<Integer> sDots = new ArrayList<Integer>();
        for (int x : sChron.getDottings()) sDots.add(x);
        while (i < fChron.getDottings().size()) {
            int a = fChron.getDotting(i);
            int p = sDots.indexOf(a);
            if (p < 0) return 0;
            if (p != i) {
                count++;
                sDots.set(p, sDots.get(i));
                sDots.set(i, a);
            }
            i++;
        }
        if (count%2 != 0) return -1;
        return 1;
    }
    
    private Chronology<R> mashTwoCircles(Chronology<R> chron, Cache tCache, Diagram nxDig, 
            int fdot, int sdot) {
        ArrayList<Integer> dots = getDots(chron, fdot, sdot);//chron.getDottings();
        if (!rasmus && dots.size() == 2) return null;
        if (dots.isEmpty()) return new Chronology<R>(chron.getValue(), chron.getDottings(), chron.getSurgeries());
        ArrayList<Integer> circle = tCache.getPaths(nxDig.circles.get(0));
        int dot = dotOfCircle(circle, tCache.getPts());
        ArrayList<Integer> newDots = new ArrayList<Integer>();
        for (Integer i : chron.getDottings()) {
            if (dots.contains(i) & !newDots.contains(dot)) newDots.add(dot);
            if (!dots.contains(i)) newDots.add(i);
        }
        return new Chronology<R>(chron.getValue(), newDots, chron.getSurgeries());
    }

    private ArrayList<Integer> getDots(Chronology<R> chron, int fdot, int sdot) {
        ArrayList<Integer> newDots = new ArrayList<Integer>(2);
        for (Integer i : chron.getDottings()) 
            if (i == fdot || i == sdot) newDots.add(i);
        return newDots;
    }
    
    private void deloopOneCircle(OddGenerator<R> oGen, int newDigNr, Cache tCache, Cache dCache, ArrayList<Generator<R>> dobjs,
            ArrayList<Integer> ddigTrans) {
        OddGenerator<R> nGenp = new OddGenerator<R>(newDigNr,oGen.hdeg(),oGen.qdeg()+1);
        OddGenerator<R> nGenm = new OddGenerator<R>(newDigNr,oGen.hdeg(),oGen.qdeg()-1);
        int c = tCache.getDiagram(oGen.getDiagram()).circles.get(0);
        int dot = dotOfCircle(tCache.getPaths(c), tCache.getPts());
        Chronology<R> cchr = new Chronology<R>(dot, unit);
        Chronology<R> dchr = new Chronology<R>(unit, new ArrayList<Integer>(0));
        OddArrow<R> carr = new OddArrow<R>(oGen, nGenp, cchr);
        OddArrow<R> darr = new OddArrow<R>(oGen, nGenm, dchr);
        oGen.addTopArrow(carr);
        oGen.addTopArrow(darr);
        if (rasmus) {
            Chronology<R> echr = new Chronology<R>(unit.negate(), new ArrayList<Integer>(0));
            carr.addChronology(echr);
        }
        createNewArrows(nGenp, oGen, dchr, tCache, dCache, ddigTrans);
        createNewArrows(nGenm, oGen, cchr, tCache, dCache, ddigTrans);
        dobjs.add(nGenp);
        dobjs.add(nGenm);
        counter = counter + 1;
    }

    private void deloopTwoCircles(OddGenerator<R> oGen, int newDigNr, Cache tCache, Cache dCache, ArrayList<Generator<R>> dobjs,
            ArrayList<Integer> ddigTrans) {
        OddGenerator<R> nGenpp = new OddGenerator<R>(newDigNr, oGen.hdeg(), oGen.qdeg()+2);
        OddGenerator<R> nGenpm = new OddGenerator<R>(newDigNr, oGen.hdeg(), oGen.qdeg());
        OddGenerator<R> nGenmp = new OddGenerator<R>(newDigNr, oGen.hdeg(), oGen.qdeg());
        OddGenerator<R> nGenmm = new OddGenerator<R>(newDigNr, oGen.hdeg(), oGen.qdeg()-2);
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
        oGen.addTopArrow(carr);
        oGen.addTopArrow(darr);
        oGen.addTopArrow(earr);
        oGen.addTopArrow(farr);
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
        createNewArrows(nGenpp, oGen, farr.getChronology(0), tCache, dCache, ddigTrans);
        createNewArrows(nGenpm, oGen, earr.getChronology(0), tCache, dCache, ddigTrans);
        createNewArrows(nGenmp, oGen, darr.getChronology(0), tCache, dCache, ddigTrans);
        createNewArrows(nGenmm, oGen, carr.getChronology(0), tCache, dCache, ddigTrans);
        dobjs.add(nGenpp);
        dobjs.add(nGenpm);
        dobjs.add(nGenmp);
        dobjs.add(nGenmm);
        counter = counter + 3;
    }
    
    private ArrayList<Chronology<R>> splitCircle(Chronology<R> chron, OddSurgery surg, 
            Cache tCache, Cache dCache, Diagram nxDig) { // surg will split off a circle
        ArrayList<Chronology<R>> newOnes = new ArrayList<Chronology<R>>(1);
        int fdot = startOfPath(surg.getFPath(0), nxDig, tCache);
        int sdot = startOfPath(surg.getSPath(0), nxDig, tCache);
        int cNum = numberOfCircles(fdot, sdot, dCache);
        if (cNum == 1) {
            ArrayList<Chronology<R>> newChrons = splitOneCircle(chron, fdot, sdot, dCache, surg.getTurn());
            for (Chronology<R> chr : newChrons) newOnes.add(chr);
        }
        else {
            /*if (!chron.getSurgeries().isEmpty()) {
                System.out.println(chron.getSurgeries()+" "+chron.getDottings()+" "+fdot+" "+sdot);
                tCache.output();
                dCache.output();
            }// */
            ArrayList<Chronology<R>> newChrons = splitTwoCircles(chron, fdot, sdot, dCache, surg.getTurn());
            for (Chronology<R> chr : newChrons) newOnes.add(chr);
        }
        return newOnes;
    }
    
    protected ArrayList<Chronology<R>> splitOneCircle(Chronology<R> chron, int fdot, int sdot, Cache dCache, 
            boolean leftturn) {
        ArrayList<Chronology<R>> newChrons = new ArrayList<Chronology<R>>(2);
        ArrayList<Integer> dots = chron.getDottings();
        R val = chron.getValue();
        int splits = numberOfSplits(chron.getSurgeries(), dCache);
        if (splits % 2 != 0) val = val.negate();
        int theCircle = theCircleOf(fdot, sdot, dCache);
        int thePath = thePathOf(fdot, sdot, dCache);
        if (dots.contains(thePath)) {
            dots.add(0, theCircle);
            if (leftturn == (fdot != theCircle)) val = val.negate();
            newChrons.add(new Chronology<R>(chron, dots, val));
        }
        else {
            if (rasmus) {
                newChrons.add(new Chronology<R>(chron, dots, val.negate()));
            }
            if (leftturn == (fdot != theCircle)) val = val.negate();
            dots.add(0, theCircle);
            newChrons.add(new Chronology<R>(chron, dots, val));
            dots.set(0, thePath);
            val = val.negate();
            newChrons.add(new Chronology<R>(chron, dots, val));
            
        }
        return newChrons;
    }
    
    protected ArrayList<Chronology<R>> splitTwoCircles(Chronology<R> chron, int fdot, int sdot, 
            Cache dCache, boolean leftturn) {
        ArrayList<Chronology<R>> newChrons = new ArrayList<Chronology<R>>(3);
        ArrayList<Integer> dots = chron.getDottings();
        R val = chron.getValue().negate();
        int splits = numberOfSplits(chron.getSurgeries(), dCache);
        if (splits % 2 != 0) val = val.negate();
        if (!dots.contains(fdot) && !dots.contains(sdot)) {
            if (rasmus) newChrons.add(new Chronology<R>(chron, dots, val.negate()));
            if (leftturn) {
                dots.add(0, sdot);
                newChrons.add(new Chronology<R>(chron, dots, val));
                val = val.negate();
                dots.set(0, fdot);
                newChrons.add(new Chronology<R>(chron, dots, val));
            }
            else {
                dots.add(0, fdot);
                newChrons.add(new Chronology<R>(chron, dots, val));
                val = val.negate();
                dots.set(0, sdot);
                newChrons.add(new Chronology<R>(chron, dots, val));
            }
        }
        else {
            
            /*ArrayList<Integer> newDots = new ArrayList<Integer>(2);
            newDots.add(fdot);
            if (leftturn) newDots.add(sdot);
            else newDots.add(0,sdot);
            if (dots.size() > 1) System.out.println("Happenz "+dots+" "+fdot+" "+sdot+" "+dCache.diagramSize()+" "+leftturn);
            for (int i = 0; i < dots.size(); i++) {
                if (dots.get(i) != fdot && dots.get(i) != sdot) newDots.add(dots.get(i));
                else if ((i % 2 != 0))  val = val.negate();
            } // */
            R vale = val.negate();
            if (leftturn != dots.contains(fdot)) vale = vale.negate();
            if (dots.contains(fdot)) dots.add(0, sdot);
            else dots.add(0, fdot);
            
            newChrons.add(new Chronology<R>(chron, dots, vale.negate()));
        }
        return newChrons;
    }
    
    protected boolean gaussEliminate(int i, ArrayList<ArrayList<Generator<R>>> dobjs, Cache dCache, boolean det) {
        boolean cont = false;
        ArrayList<Generator<R>> objs = dobjs.get(i);
        int j = objs.size()-1;
        while (j >= 0) {
            if (abInf.isAborted()) return false;
            OddGenerator<R> bObj = (OddGenerator<R>) objs.get(j);
            int k = 0;
            boolean found = false;
            while (!found && k < bObj.getBotArrowSize()) {
                OddArrow<R> arr = bObj.getBotArrow(k);
                if (canCancel(arr)) found = true;
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
        }
        return cont;
    }
    
    protected boolean canCancel(OddArrow<R> arr) {
        if (arr.getBotGenerator().qdeg() != arr.getTopGenerator().qdeg()) return false;
        return arr.getChronology(0).getValue().isInvertible();
    }
    
    private void cancelObject(OddArrow<R> arr, ArrayList<ArrayList<Generator<R>>> dobjs, Cache dCache, int i) {
        OddGenerator<R> yGen = arr.getBotGenerator();
        OddGenerator<R> xGen = arr.getTopGenerator();
        yGen.removeBotArrow(arr);
        xGen.removeTopArrow(arr);
        for (int j = 0; j < xGen.getBotArrowSize(); j++) xGen.getBotArrow(j).getTopGenerator().removeTopArrow(xGen.getBotArrow(j));
        for (int j = 0; j < yGen.getTopArrowSize(); j++) yGen.getTopArrow(j).getBotGenerator().removeBotArrow(yGen.getTopArrow(j));
        dobjs.get(i).remove(yGen);
        dobjs.get(i+1).remove(xGen);
        R u = arr.getChronology(0).getValue();
        for (int j = 0; j < xGen.getTopArrowSize(); j++) {
            OddArrow<R> farr = xGen.getTopArrow(j);
            OddGenerator<R> bGen = farr.getBotGenerator();
            for (int k = 0; k < yGen.getBotArrowSize(); k++) {
                OddArrow<R> sarr = yGen.getBotArrow(k);
                OddGenerator<R> tGen = sarr.getTopGenerator();
                ArrayList<Chronology<R>> newChrons = zigZagChronologies(farr, sarr, dCache, u);
                simplifyChronologies(newChrons, dCache, bGen.getDiagram());
                getNewChronologies(bGen, tGen, newChrons, dCache);
            }
        }
        for (int j = 0; j < xGen.getTopArrowSize(); j++) xGen.getTopArrow(j).getBotGenerator().removeBotArrow(xGen.getTopArrow(j));
        for (int j = 0; j < yGen.getBotArrowSize(); j++) yGen.getBotArrow(j).getTopGenerator().removeTopArrow(yGen.getBotArrow(j));
    }
    
    protected ArrayList<Chronology<R>> zigZagChronologies(OddArrow<R> farr, OddArrow<R> sarr, Cache dCache, R u) {
        ArrayList<Chronology<R>> newChrons = new ArrayList<Chronology<R>>();
        for (Chronology<R> fchr : farr.getChronologies()) {
            ArrayList<OddSurgery> fsrgs = fchr.getSurgeries();
            int splits = numberOfSplits(fsrgs, dCache);
            for (Chronology<R> schr : sarr.getChronologies()) {
                ArrayList<OddSurgery> newSurgs = new ArrayList<OddSurgery>();
                for (OddSurgery fsurg : fsrgs) newSurgs.add(fsurg);
                for (OddSurgery ssurg : schr.getSurgeries()) newSurgs.add(ssurg);
                ArrayList<Integer> newDots = newDotsFrom(fchr.getDottings(), schr.getDottings(), newSurgs);
                if (newDots != null) {
                    R v = u.negate();
                    if (splits%2 != 0 && schr.getDottings().size()%2 != 0) v = v.negate();
                    R value = v.invert().multiply(fchr.getValue()).multiply(schr.getValue());
                    if (!value.isZero()) newChrons.add(new Chronology<R>(value, newDots, newSurgs));
                }
                
            }
        }
        return newChrons;// */
    }
    
    protected ArrayList<Integer> newDotsFrom(ArrayList<Integer> fdots, ArrayList<Integer> sdots, ArrayList<OddSurgery> surgs) {
        ArrayList<ArrayList<Integer>> combinations = componentsOf(surgs);
        ArrayList<Integer> theDots = new ArrayList<Integer>();
        for (int dot : sdots) {
            int ndot = newDotFrom(dot, combinations);
            if (theDots.contains(ndot)) {
                if (!rasmus) return null;
            }
            else theDots.add(ndot);
        }
        for (int dot : fdots) {
            int ndot = newDotFrom(dot, combinations);
            if (theDots.contains(ndot)) {
                if (!rasmus) return null;
            }
            else theDots.add(ndot);
        }
        return theDots;
    }
    
    private int newDotFrom(int dot, ArrayList<ArrayList<Integer>> comp) {
        int i = 0;
        boolean found = false;
        while (!found && i < comp.size()) {
            if (comp.get(i).contains(dot)) found = true;
            else i++;
        }
        if (found) return comp.get(i).get(0);
        return dot;
    }
    
    private ArrayList<ArrayList<Integer>> componentsOf(ArrayList<OddSurgery> surgs) {
        ArrayList<ArrayList<Integer>> comps = new ArrayList<ArrayList<Integer>>();
        for (OddSurgery surg : surgs) {
            ArrayList<Integer> ends = new ArrayList<Integer>(2);
            ends.add(surg.getFPath(0));
            ends.add(surg.getSPath(0));
            comps.add(ends);
        }
        int i = comps.size()-1;
        while (i >= 0) {
            int j = 0;
            while (j < i) {
                if (overlap(comps.get(i), comps.get(j))) {
                    for (int a : comps.get(j)) {
                        if (!comps.get(i).contains(a)) comps.get(i).add(a);
                    }
                    comps.remove(j);
                    i--;
                }
                else j++;
            }
            Collections.sort(comps.get(i));
            i--;
        }
        return comps;
    }
    
    private boolean overlap(ArrayList<Integer> first, ArrayList<Integer> secon) {
        boolean overlap = false;
        int i = 0;
        while (!overlap && i < first.size()) {
            if (secon.contains(first.get(i))) overlap = true;
            else i++;
        }
        return overlap;
    }
    
    private void getNewChronologies(OddGenerator<R> bGen, OddGenerator<R> tGen, ArrayList<Chronology<R>> newChrons, Cache dCache) {
        if (newChrons.isEmpty()) return;
        boolean found = false;
        int i = 0;
        OddArrow<R> arrow;
        while (!found && i < bGen.getBotArrowSize()) {
            arrow = bGen.getBotArrow(i);
            if (arrow.getTopGenerator() == tGen) found = true;
            else i++;
        }
        if (!found) {
            arrow = new OddArrow<R>(bGen, tGen, newChrons);
            bGen.addBotArrow(arrow);
            tGen.addTopArrow(arrow);
        }
        else {
            arrow = bGen.getBotArrow(i);
            combineChronologies(arrow, newChrons);
            if (arrow.isEmpty()) {
                bGen.removeBotArrow(arrow);
                tGen.removeTopArrow(arrow);
            }
        }
    }

    private void combineChronologies(OddArrow<R> arrow, ArrayList<Chronology<R>> chrons) {
        for (Chronology<R> chron : chrons) combineChronology(arrow, chron);
    }
    
    protected void combineChronology(OddArrow<R> arrow, Chronology<R> chron) {
        boolean found = false;
        int i = 0;
        while (!found && i < arrow.chronologySize()) {
            Chronology<R> oChron = arrow.getChronology(i);
            int f = sameChronUpToSign(chron, oChron);
            if (f != 0) {
                found = true;
                R val = oChron.getValue();
                if (f > 0) val = val.add(chron.getValue());
                else val = val.add(chron.getValue().negate());
                arrow.removeChronology(oChron);
                if (!val.isZero()) arrow.addChronology(new Chronology<R>(val, oChron.getDottings(), oChron.getSurgeries()));
            }
            else i++;
        }
        if (!found) arrow.addChronology(chron);
    }
    
    protected int sameChronUpToSign(Chronology<R> fChron, Chronology<R> sChron) {
        if (fChron.getDottings().size() != sChron.getDottings().size() || fChron.surgerySize() != sChron.surgerySize()) return 0;
        int same = sameDottings(fChron, sChron);
        if (same == 0) return 0;
        for (int i = 0; i < fChron.surgerySize(); i++) if (!fChron.getSurgery(i).sameAs(sChron.getSurgery(i))) same = 0;
        return same; 
    }
    
    protected int numberOfSplits(ArrayList<OddSurgery> fsrgs, Cache dCache) {
        return numberOfSplits(fsrgs, fsrgs.size(), dCache);
    }
    
    protected int numberOfSplits(ArrayList<OddSurgery> fsrgs, int j, Cache dCache) {
        int count = 0;
        for (int i = 0; i < j; i++) {
            OddSurgery surg = fsrgs.get(i);
            ArrayList<ArrayList<Integer>> circles = closeCircles(dCache.getDiagram(surg.getEnd()), nextClosurePaths, dCache);
            int fcirc = circleOf(circles, surg.getFPath(0));
            int scirc = circleOf(circles, surg.getSPath(0));
            if (fcirc != scirc) count++;
        }
        return count;
    }

    private int circleOf(ArrayList<ArrayList<Integer>> circles, int fPath) {
        int i = 0;
        boolean found = false;
        while (!found) {
            if (circles.get(i).contains(fPath)) found = true;
            else i++;
        }
        return i;
    }
    
    private boolean checkqDegree(OddArrow<R> arrow) {
        boolean okay = true;
        int qDiff = arrow.getTopGenerator().qdeg()-arrow.getBotGenerator().qdeg();
        int i = 0;
        while (okay && i < arrow.getChronologies().size()) {
            Chronology<R> chron = arrow.getChronology(i);
            if (qDiff - 2*chron.getDottings().size() - chron.getSurgeries().size() != 0) 
                okay = false;
            i++;
        }
        return okay;
    }
    
    /*public ChainComplex<R> getQComplex(int q) {
        ArrayList<ArrayList<Generator<R>>> genes = new ArrayList<ArrayList<Generator<R>>>(generators.size());
        int p = 0;
        for (ArrayList<Generator<R>> gens : generators) {
            genes.add(new ArrayList<Generator<R>>());
            int i = gens.size()-1;
            while (i >= 0) {
                OddGenerator<R> gen = (OddGenerator<R>) gens.get(i);
                if (gen.qdeg() == q) {
                    Generator<R> clGen = new Generator<R>(gen.hdeg());
                    genes.get(p).add(clGen);
                    gen.clearBotArr();
                    gen.addBotArrow(new Arrow<R>(gen, clGen, unit));
                    for (int j = 0; j < gen.getTopArrowSize(); j++) {
                        OddArrow<R> arrow = gen.getTopArrow(j);
                        R val = arrow.getChronology(0).getValue();
                        Generator<R> bGen = arrow.getBotGenerator().getBArrow(0).getTopGenerator();
                        Arrow<R> clarrow = new Arrow<R>(bGen, clGen, val);
                        bGen.addBotArrow(clarrow);
                        clGen.addTopArrow(clarrow);
                    }
                    gens.remove(i);
                }
                i--;
            }
            p++;
        }
        p = genes.size()-1;
        while (p >= 0) {
            if (genes.get(p).isEmpty()) genes.remove(p);
            p--;
        }
        return new ChainComplex<R>(unit, genes, frame, abInf);
    }// */
    
}