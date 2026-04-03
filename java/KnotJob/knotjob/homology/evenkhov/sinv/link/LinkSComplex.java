/*

Copyright (C) 2025 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.evenkhov.sinv.link;

import java.util.ArrayList;
import java.util.Iterator;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Diagram;
import knotjob.homology.Generator;
import knotjob.homology.evenkhov.CobordInfo;
import knotjob.homology.evenkhov.Cobordism;
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
public class LinkSComplex<R extends Ring<R>> extends EvenComplex<R> {
    
    private EvenGenerator<R> extGen;
    
    public LinkSComplex(int crs, int[] ends, int hstart, int qstart, int a, int b, boolean rev, 
            boolean ras, R unt, DialogWrap frm, AbortInfo abt) {
        super(crs, ends, hstart, qstart, rev, ras, true, false, unt, frm, abt);
        EvenGenerator<R> gen = (EvenGenerator<R>) generators.get(0).get(0);
        Diagram dig = cache.getDiagram(gen.getDiagram());
        ArrayList<Integer> fPath = cache.getPaths(dig.paths.get(0));
        ArrayList<Integer> sPath = cache.getPaths(dig.paths.get(1));
        if ((fPath.contains(a) && fPath.contains(b)) || (sPath.contains(a) && sPath.contains(b))) {
            gen = (EvenGenerator<R>) generators.get(1).get(0);
        }
        a = adjustValue(gen, a);
        int dot = cache.getPts().indexOf(a)+1;
        extGen = new EvenGenerator<R>(gen.getDiagram(), 0, 0);
        Cobordism<R> fCob = new Cobordism<R>(unit, dot, 0);
        Cobordism<R> sCob = new Cobordism<R>(unit.negate(), 3, 0);
        EvenArrow<R> arrow = new EvenArrow<R>(extGen, gen);
        arrow.addCobordism(fCob);
        arrow.addCobordism(sCob);
        extGen.addBotArrow(arrow);
        gen.addTopArrow(arrow);
    }
    
    private int adjustValue(EvenGenerator<R> gen, int a) {
        Diagram dig = cache.getDiagram(gen.getDiagram());
        ArrayList<Integer> fPath = cache.getPaths(dig.paths.get(0));
        ArrayList<Integer> sPath = cache.getPaths(dig.paths.get(1));
        if (fPath.contains(a)) return fPath.get(0);
        return sPath.get(0);
    }
    
    @Override
    public void output(int fh, int lh) {
        System.out.println("Positive Endpts "+posEndpts);
        System.out.println("Negative Endpts "+negEndpts);
        if (cache != null) cache.output();
        EvenArrow<R> arr = extGen.getBotArrow(0);
        EvenGenerator<R> gen = arr.getTopGenerator();
        System.out.println("Extra Generator");
        extGen.output(levelContaining(gen));
        System.out.println("H "+extGen.getBotArrow(0).getTopGenerator().hdeg());
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
    }// */
    
    private ArrayList<Generator<R>> levelContaining(Generator<R> gen) {
        for (ArrayList<Generator<R>> gens : generators) if (gens.contains(gen)) return gens;
        return null;
    }
    
    @Override
    protected void createTensor(int i, int t, EvenComplex<R> complex, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts,
            EvenCache tCache, int[][] diagTrans, ArrayList<ArrayList<Generator<R>>> objs) {
        LinkSComplex<R> nComp = (LinkSComplex<R>) complex;
        boolean fLevelZero = generators.get(i).contains(extGen.getBotArrow(0).getTopGenerator());
        EvenGenerator<R> nExtGen = extGen;
        ArrayList<Cobordism<R>> magCobs = null;
        if (fLevelZero) {
            nExtGen = combineExtGens(nComp, diagTrans, pEndpts, nEndpts, tCache);
            EvenGenerator<R> eGen = nComp.extGen;
            EvenArrow<R> mor = eGen.getBotArrow(0);
            int d = nExtGen.getDiagram();
            magCobs = alterCobordisms(mor.getCobordisms(), false, tCache, d, complex);
        }
        for (int l = 0; l < generators.get(i).size(); l++) {
            if (abInf.isAborted()) return;
            EvenGenerator<R> fObj = (EvenGenerator<R>) generators.get(i).get(l);
            boolean fInImage = fLevelZero && inImage(fObj);
            ArrayList<Generator<R>> nObjs = new ArrayList<Generator<R>>(t);
            for (int k = t-1; k >= 0; k--) {
                boolean sLevelZero = nComp.generators.get(k).contains(nComp.extGen.getBotArrow(0).getTopGenerator());
                EvenGenerator<R> sObj = (EvenGenerator<R>) nComp.generators.get(k).get(0);
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
                if (fInImage && sLevelZero) {
                    for (Iterator<Arrow<R>> it = extGen.getBotArrows().iterator(); it.hasNext();) {
                        EvenArrow<R> arr = (EvenArrow<R>) it.next();
                        if (arr.getTopGenerator() == fObj) {
                            ArrayList<Cobordism<R>> moves = adjustedCobordisms(arr.getCobordisms(), sObj.getDiagram(),
                                    complex, tCache, pEndpts, nEndpts, nObj.getDiagram(), arr.getTopDiagram(), diagTrans);
                            ArrayList<Cobordism<R>> nmoves = combineCob(moves, magCobs);
                            EvenArrow<R> nArr = new EvenArrow<R>(nExtGen, nObj, nmoves);
                            nExtGen.addBotArrow(nArr);
                        }
                    }
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
        if (fLevelZero) extGen = nExtGen;
    }// */

    private EvenGenerator<R> combineExtGens(LinkSComplex<R> nComp, int[][] diagTrans, 
            ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, EvenCache tCache) {
        EvenGenerator<R> nGen = nComp.extGen;
        int dnum = diagTrans[extGen.getDiagram()][nGen.getDiagram()]-1;
        if (dnum == -1) {
            Diagram nDiag = combineDiagram(extGen.getDiagram(),nGen.getDiagram(), nComp, pEndpts, nEndpts, tCache);
            dnum = getDiagNumber(nDiag,tCache.getDiagrams());
            diagTrans[extGen.getDiagram()][nGen.getDiagram()] = dnum+1;
        }
        return new EvenGenerator<R>(dnum, 0, 0);
    }

    private boolean inImage(EvenGenerator<R> fObj) {
        for (Iterator<Arrow<R>> it = extGen.getBotArrows().iterator(); it.hasNext();) {
            EvenArrow<R> arr = (EvenArrow<R>) it.next();
            if (arr.getTopGenerator() == fObj) return true;
        }
        return false;
    }

    private ArrayList<Cobordism<R>> combineCob(ArrayList<Cobordism<R>> moves, 
            ArrayList<Cobordism<R>> magCobs) {
        ArrayList<Cobordism<R>> nmoves = new ArrayList<Cobordism<R>>();
        for (Cobordism<R> cob : moves) {
            for (Cobordism<R> mcob : magCobs) {
                Cobordism<R> nCob = new Cobordism<R>(cob.getValue().multiply(mcob.getValue()), 
                        combineDottings(cob.getDottings(), mcob.getDottings()), cob.getSurgery());
                nmoves.add(nCob);
            }
        }
        return nmoves;
    }
    
    @Override
    protected void deloopObjects(int i, ArrayList<ArrayList<Generator<R>>> tobjs, ArrayList<ArrayList<Generator<R>>> dobjs, 
            EvenCache tCache, EvenCache dCache, ArrayList<Integer> ddigTrans) {
        boolean lvlZero = tobjs.get(i).contains(extGen.getBotArrow(0).getTopGenerator());
        ArrayList<Diagram> tDigs = tCache.getDiagrams();
        while (tDigs.size() > ddigTrans.size()) ddigTrans.add(-1);
        for (Iterator<Generator<R>> it = tobjs.get(i).iterator(); it.hasNext();) {
            EvenGenerator<R> oObj = (EvenGenerator<R>) it.next();
            if (abInf.isAborted()) return;
            int oldDigNr = oObj.getDiagram();
            Diagram oldDig = tCache.getDiagram(oldDigNr);
            int newDigNr = ddigTrans.get(oldDigNr);
            if (newDigNr == -1) newDigNr = newDiagNumber(oldDig, tCache, dCache, ddigTrans, oldDigNr);
            if (oldDig.circles.isEmpty()) noDeloop(oObj,newDigNr,tCache,dCache,dobjs.get(i),ddigTrans);
            else {
                if (oldDig.circles.size() == 1) deloopOneCircle(oObj,newDigNr,tCache,dCache,dobjs.get(i),ddigTrans,oldDig);
                else {
                    deloopTwoCircles(oObj,newDigNr,tCache,dCache,dobjs.get(i),ddigTrans,oldDig);
                }
            }
            frame.setLabelRight(""+counter, 2, false);
            oObj.clearBotArrow();
        }
        if (lvlZero) {
            EvenGenerator<R> nExtGen = getNewExtGenDelooped(tCache, dCache, ddigTrans);
            Diagram oldDig = tCache.getDiagram(extGen.getDiagram());
            if (oldDig.circles.isEmpty()) noSpecDeloop(nExtGen, tCache, dCache, ddigTrans);
            else {
                if (oldDig.circles.size() == 1) deloopOneSpec(nExtGen, tCache, dCache, ddigTrans, oldDig);
                else deloopTwoSpec(nExtGen, tCache, dCache, ddigTrans, oldDig);
            }
            extGen = nExtGen;
        }
        tobjs.set(i, null);
    }
    
    private EvenGenerator<R> getNewExtGenDelooped(EvenCache tCache, EvenCache dCache,
            ArrayList<Integer> ddigTrans) {
        int oldDigNr = extGen.getDiagram();
        Diagram oldDig = tCache.getDiagram(oldDigNr);
        int newDigNr = ddigTrans.get(oldDigNr);
        if (newDigNr == -1) newDigNr = newDiagNumber(oldDig, tCache, dCache, ddigTrans, oldDigNr);
        return new EvenGenerator<R>(newDigNr, 0, 0);    
    }

    private void noSpecDeloop(EvenGenerator<R> nExtGen, EvenCache tCache, EvenCache dCache, 
            ArrayList<Integer> ddigTrans) {
        Cobordism<R> ccob = new Cobordism<R>(0, unit);
        EvenArrow<R> cmor = new EvenArrow<R>(extGen, nExtGen, ccob);
        extGen.addTopArrow(cmor);
        createNewMorphisms(nExtGen, extGen, ccob, tCache, dCache, ddigTrans);
    }

    private void deloopOneSpec(EvenGenerator<R> nExtGen, EvenCache tCache, EvenCache dCache, 
            ArrayList<Integer> ddigTrans, Diagram oldDig) {
        int dot = tCache.getPowrs().get(tCache.getPts().indexOf(tCache.getPaths().get(oldDig.circles.get(0)).get(0)));
        Cobordism<R> ccob = new Cobordism<R>(dot, unit);
        Cobordism<R> dcob = new Cobordism<R>(0, unit);
        EvenArrow<R> cmor = new EvenArrow<R>(extGen, nExtGen, ccob);
        extGen.addTopArrow(cmor);
        Cobordism<R> ecob = new Cobordism<R>(0, unit.negate());
        cmor.addCobordism(ecob);
        createNewMorphisms(nExtGen, extGen, dcob, tCache, dCache, ddigTrans);
    }

    private void deloopTwoSpec(EvenGenerator<R> nExtGen, EvenCache tCache, EvenCache dCache, 
            ArrayList<Integer> ddigTrans, Diagram oldDig) {
        int dot1 = tCache.getPowrs().get(tCache.getPts().indexOf(tCache.getPaths().get(oldDig.circles.get(0)).get(0)));
        int dot2 = tCache.getPowrs().get(tCache.getPts().indexOf(tCache.getPaths().get(oldDig.circles.get(1)).get(0)));
        int dot3 = dot1+dot2;
        Cobordism<R> ccob = new Cobordism<R>(dot3, unit);
        Cobordism<R> fcob = new Cobordism<R>(0, unit);
        EvenArrow<R> morpp = new EvenArrow<R>(extGen, nExtGen, ccob);
        extGen.addTopArrow(morpp);
        Cobordism<R> dcob = new Cobordism<R>(dot1, unit.negate());
        Cobordism<R> ecob = new Cobordism<R>(dot2, unit.negate());
        morpp.addCobordism(dcob);
        morpp.addCobordism(ecob);
        morpp.addCobordism(fcob);
        createNewMorphisms(nExtGen, extGen, fcob, tCache, dCache, ddigTrans);
    }

    @Override
    protected void createNewMorphisms(EvenGenerator<R> nObj, EvenGenerator<R> oObj, Cobordism<R> ccob, 
            EvenCache tCache, EvenCache dCache, ArrayList<Integer> ddigTrans) {
        for (Iterator<Arrow<R>> it = oObj.getBotArrows().iterator(); it.hasNext();) {
            EvenArrow<R> mor = (EvenArrow<R>) it.next();
            ArrayList<CobordInfo<R>> newCobs = new ArrayList<CobordInfo<R>>();
            for (Cobordism<R> fcob : mor.getCobordisms()) {
                int newDots = newDottings(fcob.getDottings(),ccob.getDottings(),tCache);
                ArrayList<Integer> surgs = tCache.getSurgeries(fcob.getSurgery(), mor.getTopGenerator().getDiagram());
                CobordInfo<R> ncob = new CobordInfo<R>(ccob.getValue().multiply(fcob.getValue()), newDots, surgs);
                newCobs.add(ncob);
            }
            modifyCobordisms(newCobs, tCache, dCache, ddigTrans, oObj.getDiagram());
            for (Iterator<Arrow<R>> itt = mor.getTopGenerator().getTopArrows().iterator(); itt.hasNext();) {
                EvenArrow<R> cmor = (EvenArrow<R>) itt.next();
                EvenGenerator<R> tnObj = cmor.getTopGenerator();
                EvenArrow<R> nmor = new EvenArrow<R>(nObj, tnObj);
                if (oObj == extGen) simplifyNewCobs(newCobs, nmor, cmor, tCache, dCache);
                else obtainNewCobordisms(newCobs, nmor, cmor, tCache, dCache);
                if (!nmor.isEmpty()) {
                    nObj.addBotArrow(nmor);
                    tnObj.addTopArrow(nmor);
                }
            }
        }
    }
    
    private void simplifyNewCobs(ArrayList<CobordInfo<R>> newCobs,
            EvenArrow<R> nmor, EvenArrow<R> cmor, EvenCache tCache, EvenCache dCache) {
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
    
    public boolean cocycleCheck() {
        ArrayList<EvenGenerator<R>> img = new ArrayList<EvenGenerator<R>>();
        ArrayList<R> endValues = new ArrayList<R>();
        for (Arrow<R> arr : extGen.getBotArrows()) {
            EvenArrow<R> ar = (EvenArrow<R>) arr;
            R val = ar.getValue();
            EvenGenerator<R> next = ar.getTopGenerator();
            for (int i = 0; i < next.bMorSize(); i++) {
                EvenArrow<R> sar = next.getBotArrow(i);
                int pos = positionOf(sar.getTopGenerator(), img);
                if (pos < 0) {
                    img.add(sar.getTopGenerator());
                    endValues.add(val.multiply(sar.getValue()));
                }
                else {
                    R curr = endValues.get(pos);
                    endValues.set(pos, curr.add(val.multiply(sar.getValue())));
                }
            }
        }
        for (R val : endValues) if (!val.isZero()) return false;
        return true;
    }

    private int positionOf(EvenGenerator<R> topGenerator, ArrayList<EvenGenerator<R>> img) {
        for (int i = 0; i < img.size(); i++) {
            if (img.get(i) == topGenerator) return i;
        }
        return -1;
    }

    public int minimalImageQs() {
        if (abInf.isAborted()) return 0;
        Iterator<Arrow<R>> it = extGen.getBotArrows().iterator();
        EvenArrow<R> arr = (EvenArrow<R>) it.next();
        int min = arr.getTopGenerator().qdeg();
        while (it.hasNext()) {
            arr = (EvenArrow<R>) it.next();
            int q = arr.getTopGenerator().qdeg();
            if (min > q) min = q;
        }
        return min;
    }
    
    @Override
    protected void oneCircleIntoTwo(CobordInfo<R> cob, int cid, EvenCache tCache, ArrayList<CobordInfo<R>> newGuys, 
            ArrayList<Integer> fDiags, int sid, int j) {
        if (dotContains(cob.getDottings(), cid, tCache)) 
            cob.setDottings(cob.getDottings() | tCache.getPowrs().get(tCache.getPts().indexOf(sid)));
        else {
            CobordInfo<R> cob1 = cobClone(cob, true);
            cob1.setDottings(cob1.getDottings() | tCache.getPowrs().get(tCache.getPts().indexOf(sid)));
            newGuys.add(cob1);
            fDiags.add(fDiags.get(j));
            CobordInfo<R> cob2 = cobClone(cob, false); 
            newGuys.add(cob2);
            fDiags.add(fDiags.get(j));
            cob.setDottings(cob.getDottings() | tCache.getPowrs().get(tCache.getPts().indexOf(cid)));
        }
    }
    
    @Override
    protected void pathIntoCircle(int sid, Diagram fDiag, EvenCache tCache, CobordInfo<R> cob, ArrayList<CobordInfo<R>> newGuys, 
            ArrayList<Integer> fDiags, int j) {
        int pid = pathContaining(sid, fDiag.paths, tCache);
        int p = tCache.getPaths().get(pid).get(0);
        if (dotContains(cob.getDottings(), p, tCache)) 
            cob.setDottings(cob.getDottings()|tCache.getPowrs().get(tCache.getPts().indexOf(sid)));
        else {
            CobordInfo<R> cob1 = cobClone(cob, true);
            cob1.setDottings(cob1.getDottings()| tCache.getPowrs().get(tCache.getPts().indexOf(sid)));
            newGuys.add(cob1);
            fDiags.add(fDiags.get(j));
            CobordInfo<R> cob2 = cobClone(cob, false);
            newGuys.add(cob2);
            fDiags.add(fDiags.get(j));
            cob.setDottings(cob.getDottings() | tCache.getPowrs().get(tCache.getPts().indexOf(p)));
        }
    }
    
    @Override
    protected void pathIntoCircle(EvenCache tCache, Diagram fDiag, Diagram sDiag, CobordInfo<R> cob, ArrayList<CobordInfo<R>> newGuys, 
            ArrayList<Integer> fDiags, int j) {
        int cid = tCache.getPaths().get(sDiag.circles.get(0)).get(0);
        int pid = pathContaining(cid, fDiag.paths, tCache);
        int p = tCache.getPaths().get(pid).get(0);
        if (dotContains(cob.getDottings(), p, tCache)) 
            cob.setDottings(cob.getDottings() | tCache.getPowrs().get(tCache.getPts().indexOf(cid)));
        else {
            CobordInfo<R> cob1 = cobClone(cob, true);
            cob1.setDottings(cob.getDottings() | tCache.getPowrs().get(tCache.getPts().indexOf(cid)));
            newGuys.add(cob1);
            fDiags.add(fDiags.get(j));
            CobordInfo<R> cob2 = cobClone(cob, false);
            newGuys.add(cob2);
            fDiags.add(fDiags.get(j));
            cob.setDottings(cob.getDottings() | tCache.getPowrs().get(tCache.getPts().indexOf(p)));
        }
    }
    
    public void specSeq(int jump) {
        int orjmp = jump;
        boolean cont = true;
        while (cont) {
            cont = false;
            int i = generators.size()-2;
            while (i >= 0) {
                cont = cancelSequence(i, cont, jump);
                i--;
            }
            jump = jump + orjmp;
            if (abInf.isAborted()) return;
        }
    }

    private boolean cancelSequence(int i, boolean cont, int jump) {
        int c = generators.get(i).size()-1;
        while (c >= 0) {
            EvenGenerator<R> gen = (EvenGenerator<R>) generators.get(i).get(c);
            if (!gen.getBotArrows().isEmpty()) {
                cont = true;
                for (Iterator<Arrow<R>> at = gen.getBotArrows().iterator(); at.hasNext();) {
                    EvenArrow<R> ar = (EvenArrow<R>) at.next();
                    if (abInf.isAborted()) return false;
                    if (qDifference(ar) == jump) {
                        cancelGen(ar, i);
                        break;
                    }
                }
            }
            c--;
        }
        if (!this.cocycleCheck()) throw new UnsupportedOperationException("Problem (LinkSComplex.java).");
        return cont;
    }
    
    private int qDifference(EvenArrow<R> ar) {
        EvenGenerator<R> bGen = ar.getBotGenerator();
        EvenGenerator<R> tGen = ar.getTopGenerator();
        return tGen.qdeg() - bGen.qdeg();
    }
    
    private void cancelGen(EvenArrow<R> ar, int i) {
        EvenGenerator<R> yGen = ar.getBotGenerator();
        EvenGenerator<R> xGen = ar.getTopGenerator();
        R val = ar.getCobordism(0).getValue();
        yGen.getBotArrows().remove(ar);
        xGen.getTopArrows().remove(ar);
        for (Arrow<R> tr : xGen.getBotArrows()) tr.getTopGenerator().getTopArrows().remove(tr);
        for (Arrow<R> br : yGen.getTopArrows()) br.getBotGenerator().getBotArrows().remove(br);
        generators.get(i).remove(yGen);
        generators.get(i+1).remove(xGen);
        for (Iterator<Arrow<R>> it = xGen.getTopArrows().iterator(); it.hasNext();) {
            EvenArrow<R> fmr = (EvenArrow<R>) it.next();
            for (Iterator<Arrow<R>> itt = yGen.getBotArrows().iterator(); itt.hasNext();) {
                EvenArrow<R> smr = (EvenArrow<R>) itt.next();
                EvenGenerator<R> bGen = fmr.getBotGenerator();
                EvenGenerator<R> tGen = smr.getTopGenerator();
                EvenArrow<R> oAr = arrowBetween(bGen, tGen);
                R nv = oAr.getCobordism(0).getValue().add(smr.getCobordism(0).getValue()
                        .multiply(fmr.getCobordism(0).getValue()).multiply(val.invert()).negate());
                if (nv.isZero()) {
                    bGen.getBotArrows().remove(oAr);
                    tGen.getTopArrows().remove(oAr);
                }
                else oAr.getCobordism(0).setValue(nv);
            }
        }
        for (Arrow<R> mr : xGen.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        for (Arrow<R> mr : yGen.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
    }

    private EvenArrow<R> arrowBetween(EvenGenerator<R> bGen, EvenGenerator<R> tGen) {
        for (Iterator<Arrow<R>> it = bGen.getBotArrows().iterator(); it.hasNext();) {
            EvenArrow<R> ar = (EvenArrow<R>) it.next();
            if (ar.getTopGenerator() == tGen) return ar;
        }
        EvenArrow<R> nAr = new EvenArrow<R>(bGen, tGen);
        Cobordism<R> cob = new Cobordism<R>(unit.getZero(), 0, 0);
        nAr.addCobordism(cob);
        bGen.addBotArrow(nAr);
        tGen.addTopArrow(nAr);
        return nAr;
    }

    public ArrayList<Integer> theQs() {
        ArrayList<Generator<R>> homZero = theZeros();
        ArrayList<Integer> theqs = new ArrayList<Integer>();
        for (Generator<R> gen : homZero) theqs.add(((EvenGenerator<R>) gen).qdeg());
        return theqs;
    }

    private ArrayList<Generator<R>> theZeros() {
        for (ArrayList<Generator<R>> gens : generators) {
            if (!gens.isEmpty() && gens.get(0).hdeg() == 0) return gens;
        } 
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
