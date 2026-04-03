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
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Cache;
import knotjob.homology.Diagram;
import knotjob.homology.Generator;
import knotjob.homology.oddkhov.Chronology;
import knotjob.homology.oddkhov.OddArrow;
import knotjob.homology.oddkhov.OddComplex;
import knotjob.homology.oddkhov.OddGenerator;
import knotjob.homology.oddkhov.OddSurgery;
import knotjob.homology.oddkhov.SurgeryDiagram;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class UnifiedComplex<R extends Ring<R>> extends OddComplex<R>{
    
    private final R xi;
    private boolean reduced;
    private int reducer;
    
    public UnifiedComplex(int comp, R unt, AbortInfo abInf, DialogWrap frm,
            boolean ras) { // should only be used with comp = 0 or 1
        super(comp, unt, abInf, frm, ras);
        xi = null;
    }
    
    public UnifiedComplex(int crs, int[] ends, int hstart, int qstart, boolean rev, R unt, 
            R x, DialogWrap frm, AbortInfo abt, boolean ras) {
        super(crs, ends, hstart, qstart, rev, unt, frm, abt, ras);
        xi = x;
        reduced = false;
        reducer = 0;
    }
    
    public UnifiedComplex(R unt, R x, boolean rms, AbortInfo ab, DialogWrap frm) {
        super(unt, rms, ab, frm);
        xi = x;
    }
    
    public void setReduced(int red) {
        reduced = true;
        reducer = red;
    }
    
    public void modifyComplex(UnifiedComplex<R> complex, String girth, boolean det) {
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
    
    private void getNewObjects(UnifiedComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, Cache tCache, Cache dCache,
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
            if (i < generators.size()-1 && i > -t-1) delooping(i+t, tobjs, dobjs, tCache, dCache, ddigTrans);
            if (!debug && i < generators.size()-2) {
                boolean cancel = true;
                while (cancel) cancel = gaussEliminate(i+t+1, dobjs, dCache, det);// we cancel as long as we can
            }// */
            i--;
        }
        cache = dCache;
        generators = dobjs;
    }
    
    private void delooping(int i, ArrayList<ArrayList<Generator<R>>> tobjs, ArrayList<ArrayList<Generator<R>>> dobjs, 
            Cache tCache, Cache dCache, ArrayList<Integer> ddigTrans) {
        if (!reduced) deloopGens(i, tobjs, dobjs, tCache, dCache, ddigTrans);
        else deloopRedGens(i, tobjs, dobjs, tCache, dCache, ddigTrans);
    }
    
    private void deloopRedGens(int i, ArrayList<ArrayList<Generator<R>>> tobjs, ArrayList<ArrayList<Generator<R>>> dobjs, 
            Cache tCache, Cache dCache, ArrayList<Integer> ddigTrans) {
        while (tCache.diagramSize() > ddigTrans.size()) ddigTrans.add(-1);
        for (Iterator<Generator<R>> it = tobjs.get(i).iterator(); it.hasNext();) {
            OddGenerator<R> oGen = (OddGenerator<R>) it.next();
            if (abInf.isAborted()) return;
            int oldDigNr = oGen.getDiagram();
            Diagram oldDig = tCache.getDiagram(oldDigNr);
            int newDigNr = ddigTrans.get(oldDigNr);
            if (newDigNr == -1) newDigNr = newDiagNumber(oldDig, tCache, dCache, ddigTrans, oldDigNr);
            if (oldDig.circles.size() == 1) deloopOneRedCircle(oGen, newDigNr, tCache, dCache, dobjs.get(i), ddigTrans);
            else deloopTwoRedCircles(oGen, newDigNr, tCache, dCache, dobjs.get(i), ddigTrans, oldDig);
            frame.setLabelRight(""+counter, 2, false);
            oGen.clearBotArr();
            if (i < tobjs.size()-1) tobjs.set(i+1,null);
        }
    }
    
    private void deloopOneRedCircle(OddGenerator<R> oObj, int newDigNr, Cache tCache, Cache dCache, 
            ArrayList<Generator<R>> dobjs, ArrayList<Integer> ddigTrans) {
        OddGenerator<R> nObjr = new OddGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg());
        int c = tCache.getDiagram(oObj.getDiagram()).circles.get(0);
        int dot = dotOfCircle(tCache.getPaths(c), tCache.getPts());
        Chronology<R> ccob = new Chronology<R>(dot, unit);
        Chronology<R> dcob = new Chronology<R>(unit, new ArrayList<Integer>(0));
        OddArrow<R> emor = new OddArrow<R>(oObj, nObjr, dcob);
        oObj.addTopArrow(emor);
        createNewArrows(nObjr, oObj, ccob, tCache, dCache, ddigTrans);
        dobjs.add(nObjr);
    }
    
    private void deloopTwoRedCircles(OddGenerator<R> oObj, int newDigNr, Cache tCache, Cache dCache, 
            ArrayList<Generator<R>> dobjs, ArrayList<Integer> ddigTrans, Diagram oldDig) {
        int point = Math.abs(reducer)-1;
        boolean pm = true;
        if (tCache.getPaths(oldDig.circles.get(0)).contains(point)) pm = false;
        OddGenerator<R> nObjpp = new OddGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg()+2);
        OddGenerator<R> nObjpm = new OddGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg());
        OddGenerator<R> nObjmp = new OddGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg());
        OddGenerator<R> nObjrp = new OddGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg()+1);
        OddGenerator<R> nObjrm = new OddGenerator<R>(newDigNr, oObj.hdeg(), oObj.qdeg()-1);
        int cOne = tCache.getDiagram(oObj.getDiagram()).circles.get(0);
        int dOne = dotOfCircle(tCache.getPaths(cOne), tCache.getPts());
        int cTwo = tCache.getDiagram(oObj.getDiagram()).circles.get(1);
        int dTwo = dotOfCircle(tCache.getPaths(cTwo), tCache.getPts());
        ArrayList<Integer> bDots = new ArrayList<Integer>(2);
        bDots.add(dOne);
        bDots.add(dTwo);
        //int dot1 = tCache.getPowrs().get(tCache.getPts().indexOf(tCache.getPaths().get(oldDig.circles.get(0)).get(0)));
        //int dot2 = tCache.getPowrs().get(tCache.getPts().indexOf(tCache.getPaths().get(oldDig.circles.get(1)).get(0)));
        //int dot3 = dot1+dot2;
        Chronology<R> ccob = new Chronology<R>(unit, bDots);
        Chronology<R> dcob = new Chronology<R>(dOne, unit);
        Chronology<R> ecob = new Chronology<R>(dTwo, unit);
        Chronology<R> fcob = new Chronology<R>(unit, new ArrayList<Integer>(0));
        OddArrow<R> morpp = new OddArrow<R>(oObj, nObjpp, ccob);
        OddArrow<R> morpm = new OddArrow<R>(oObj, nObjpm, dcob);
        OddArrow<R> mormp = new OddArrow<R>(oObj, nObjmp, ecob);
        OddArrow<R> morrm = new OddArrow<R>(oObj, nObjrm, fcob);
        OddArrow<R> morrp;
        if (pm) morrp = new OddArrow<R>(oObj, nObjrp, dcob);
        else morrp = new OddArrow<R>(oObj, nObjrp, ecob);
        
        oObj.addTopArrow(morrm);
        oObj.addTopArrow(morrp);
        if (rasmus) {  // this should only occur with reduced > 0
            fcob = new Chronology<R>(0, unit.negate());
            morrp.addChronology(fcob); 
        }
        if (pm) createNewArrows(nObjrp, oObj, mormp.getChronology(0), tCache, dCache, ddigTrans);
        else createNewArrows(nObjrp, oObj, morpm.getChronology(0), tCache, dCache, ddigTrans);
        createNewArrows(nObjrm, oObj, morpp.getChronology(0), tCache, dCache, ddigTrans);
        
        dobjs.add(nObjrp);
        dobjs.add(nObjrm);
        counter = counter + 1;
    }
    
    private void createCone(int i, int t, UnifiedComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, 
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

    protected void createArrowsF(OddGenerator<R> fGen, UnifiedComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, 
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
    
    protected void createArrowsG(OddGenerator<R> fGen, UnifiedComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, 
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
                if (change) val = val.multiply(xi); // change the value by xi instead of -1
                ArrayList<Integer> newDots = adaptDottings(chron.getDottings(), nGen, tCache);
                if (newDots != null) {
                    Chronology<R> nChron = new Chronology<R>(val.negate(), newDots, newSurgeries);
                    newChrons.add(nChron); // this is multiplication by -1 (above line)
                }
            }
            OddArrow<R> nArrow = new OddArrow<R>(nGen, ntGen, newChrons);
            nGen.addBotArrow(nArrow);
        }
    }

    @Override
    protected ArrayList<Chronology<R>> splitOneCircle(Chronology<R> chron, int fdot, int sdot, Cache dCache, 
            boolean leftturn) {
        ArrayList<Chronology<R>> newChrons = new ArrayList<Chronology<R>>(2);
        ArrayList<Integer> dots = chron.getDottings();
        R val = chron.getValue();
        int splits = numberOfSplits(chron.getSurgeries(), dCache);
        if (splits % 2 != 0) val = val.multiply(xi); // multiplication with xi instead of -1
        int theCircle = theCircleOf(fdot, sdot, dCache);
        int thePath = thePathOf(fdot, sdot, dCache);
        if (dots.contains(thePath)) {
            dots.add(0, theCircle);
            if (leftturn == (fdot != theCircle)) val = val.multiply(xi);// multiplication with xi instead of -1
            newChrons.add(new Chronology<R>(chron, dots, val));
        }
        else {
            if (rasmus) {
                newChrons.add(new Chronology<R>(chron, dots, val.negate()));
            }
            if (leftturn == (fdot != theCircle)) val = val.multiply(xi);// multiplication with xi instead of -1
            dots.add(0, theCircle);
            newChrons.add(new Chronology<R>(chron, dots, val));
            dots.set(0, thePath);
            val = val.multiply(xi); // multiplication with xi instead of -1
            newChrons.add(new Chronology<R>(chron, dots, val));
            
        }
        return newChrons;
    }
    
    @Override
    protected ArrayList<Chronology<R>> splitTwoCircles(Chronology<R> chron, int fdot, int sdot, 
            Cache dCache, boolean leftturn) {
        ArrayList<Chronology<R>> newChrons = new ArrayList<Chronology<R>>(3);
        ArrayList<Integer> dots = chron.getDottings();
        R val = chron.getValue().multiply(xi); // multiply by xi instead of -1
        int splits = numberOfSplits(chron.getSurgeries(), dCache);
        if (splits % 2 != 0) val = val.multiply(xi);// multiply by xi instead of -1
        if (!dots.contains(fdot) && !dots.contains(sdot)) {
            if (rasmus) newChrons.add(new Chronology<R>(chron, dots, val.negate()));
            if (leftturn) {
                dots.add(0, sdot);
                newChrons.add(new Chronology<R>(chron, dots, val));
                val = val.multiply(xi);// multiply by xi instead of -1
                dots.set(0, fdot);
                newChrons.add(new Chronology<R>(chron, dots, val));
            }
            else {
                dots.add(0, fdot);
                newChrons.add(new Chronology<R>(chron, dots, val));
                val = val.multiply(xi); // multiply by xi instead of -1
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
            R vale = val.multiply(xi); // multiply by xi instead of -1
            if (leftturn != dots.contains(fdot)) vale = vale.multiply(xi);// multiply by xi instead of -1
            if (dots.contains(fdot)) dots.add(0, sdot);
            else dots.add(0, fdot);
            
            newChrons.add(new Chronology<R>(chron, dots, vale.multiply(xi)));
        }
        return newChrons;
    }
    
    @Override
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
                        if (ind % 2 != 0) val = val.multiply(xi); // multiply by xi instead of -1
                        if (numberOfSplits(chr.getSurgeries(), dCache) % 2 != 0) 
                            val = val.multiply(xi); // multiply by xi instead of -1
                        clDots.remove(c);
                    }
                }
                if (okay) relChrons.add(new Chronology<R>(chr, clDots, val.multiply(cb.getValue())));
            }
        }
        simplifyChronologies(relChrons, dCache,narr.getBotGenerator().getDiagram());
        for (Chronology<R> chr : relChrons) {
            narr.addChronology(chr);
        }
        //if (!checkqDegree(narr)) System.out.println("Problem");
    }
    
    @Override
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
        if (change) val = val.multiply(xi); // multiply by xi instead of -1
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
        if (nDots != null) newChrons.add(new Chronology<R>(val.multiply(xi), 
                nDots, chron.getSurgeries())); // multiply by xi instead of -1
        if (rasmus) {
            newChrons.add(new Chronology<R>(val.negate(), chron.getDottings(), chron.getSurgeries()));
        }// */
    }
    
    @Override
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
                else val = val.add(chron.getValue().multiply(xi));// multiply by xi instead of -1
                arrow.removeChronology(oChron);
                if (!val.isZero()) arrow.addChronology(new Chronology<R>(val, oChron.getDottings(), oChron.getSurgeries()));
            }
            else i++;
        }
        if (!found) arrow.addChronology(chron);
    }
    
    @Override
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
                if (sameChron < 0) val = val.add(newChrons.get(j).getValue().multiply(xi)); // multiply by xi instead of -1
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
    
    @Override
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
                    R v = u.negate(); // this should be correct because of zig zag
                    if (splits%2 != 0 && schr.getDottings().size()%2 != 0) v = v.multiply(xi); // multiply by xi instead of -1
                    R value = v.invert().multiply(fchr.getValue()).multiply(schr.getValue());
                    if (!value.isZero()) newChrons.add(new Chronology<R>(value, newDots, newSurgs));
                }
                
            }
        }
        return newChrons;// */
    }
    
}
