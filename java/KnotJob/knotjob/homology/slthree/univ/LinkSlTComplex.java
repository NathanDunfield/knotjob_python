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

package knotjob.homology.slthree.univ;

import java.util.ArrayList;
import java.util.Iterator;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.homology.slthree.SlTArrow;
import knotjob.homology.slthree.SlTGenerator;
import knotjob.homology.slthree.SlThreeComplex;
import knotjob.homology.slthree.foam.Edge;
import knotjob.homology.slthree.foam.Facet;
import knotjob.homology.slthree.foam.Foam;
import knotjob.homology.slthree.foam.SlTCache;
import knotjob.homology.slthree.foam.Web;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class LinkSlTComplex<R extends Ring<R>> extends SlThreeComplex<R> {
    
    private SlTGenerator<R> extGen;
    
    public LinkSlTComplex(int[] pts, int c, int factor, R unt, int s, DialogWrap frm, AbortInfo abf) {
        super(pts, c, factor, unt, s, frm, abf);
        SlTGenerator<R> gen = (SlTGenerator<R>) generators.get(0).get(0);
        if (gen.hdeg() != 0) gen = (SlTGenerator<R>) generators.get(1).get(0);
        extGen = new SlTGenerator<R>(0, 0, gen.getWeb());
        SlTArrow<R> arr = new SlTArrow<R>(extGen, gen);
        if (sType == 3) addNineFoams(arr);
        if (sType == 2) addFourFoams(arr);
        extGen.addBotArrow(arr);
        gen.addTopArrow(arr);
    }
    
    public LinkSlTComplex(R unt, DialogWrap frm, AbortInfo abf, int s, 
            int p, int n) {
        super(unt, frm, abf, s, p, n);
        SlTGenerator<R> gen = (SlTGenerator<R>) generators.get(0).get(0);
        extGen = new SlTGenerator<R>(0, 0, gen.getWeb());
        SlTArrow<R> arr = new SlTArrow<R>(extGen, gen);
        if (sType == 3) addThreeFoams(arr);
        if (sType == 2) addTwoFoams(arr);
        extGen.addBotArrow(arr);
        gen.addTopArrow(arr);
    }

    @Override
    public void output(int fh, int lh) {
        cache.output();
        SlTArrow<R> arr = (SlTArrow<R>) extGen.getBotArrows().get(0);
        SlTGenerator<R> gen = (SlTGenerator<R>) arr.getTopGenerator();
        System.out.println("Extra Generator");
        extGen.output(levelContaining(gen));
        System.out.println("H "+extGen.getBotArrows().get(0).getTopGenerator().hdeg());
        for (int i = fh; i < lh; i++) {
            System.out.println();
            System.out.println("Level "+i);
            for (int j = 0; j < ( generators.get(i)).size(); j++) {
                System.out.println();
                System.out.println("Generator "+j);
                ArrayList<Generator<R>> nextLev = null;
                if (i < generators.size()-1) nextLev = generators.get(i+1);
                ((SlTGenerator<R>) (generators.get(i)).get(j)).output(nextLev);
            }
        }
        System.out.println();
        System.out.println();
    }
    
    private ArrayList<Generator<R>> levelContaining(Generator<R> gen) {
        for (ArrayList<Generator<R>> gens : generators) if (gens.contains(gen)) return gens;
        return null;
    }
    
    public void finishOffRed(String string, boolean highDetail) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    @Override
    public void finishOff(String gInfo, boolean hd) {
        ArrayList<Integer> lepts = cache.lastEndpoints();
        LinkSlTComplex<R> finComplex = new LinkSlTComplex<R>(unit, frame, abInf, 
                sType, lepts.get(1), lepts.get(0));
        this.modifyComplex(finComplex, gInfo, hd);
    }

    private void addNineFoams(SlTArrow<R> arr) {
        R three = unit.add(unit).add(unit);
        R ninv = three.multiply(three).invert();
        Edge eOne = extGen.getWeb().getEdges().get(0);
        Edge eTwo = extGen.getWeb().getEdges().get(1);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Foam<R> foam = new Foam<R>(extGen.getWeb(), extGen.getWeb(), ninv);
                foam.addFacets(new Facet(eOne, i));
                foam.addFacets(new Facet(eTwo, j));
                arr.addFoam(foam);
            }
        }
    }

    private void addFourFoams(SlTArrow<R> arr) {
        R mOne = unit.negate();
        Edge eOne = extGen.getWeb().getEdges().get(0);
        Edge eTwo = extGen.getWeb().getEdges().get(1);
        for (int i = 1; i < 3; i++) {
            for (int j = 1; j < 3; j++) {
                Foam<R> foam = new Foam<R>(extGen.getWeb(), extGen.getWeb(), mOne);
                foam.addFacets(new Facet(eOne, i));
                foam.addFacets(new Facet(eTwo, j));
                arr.addFoam(foam);
            }
        }
    }

    private void addThreeFoams(SlTArrow<R> arr) {
        R thrnv = unit.add(unit).add(unit).invert();
        Edge edge = extGen.getWeb().getEdges().get(0);
        for (int i = 0; i < 3; i++) {
            Foam<R> foam = new Foam<R>(extGen.getWeb(), extGen.getWeb(), thrnv);
            foam.addFacets(new Facet(edge, i));
            arr.addFoam(foam);
        }
    }

    private void addTwoFoams(SlTArrow<R> arr) {
        R mOne = unit.negate();
        Edge edge = extGen.getWeb().getEdges().get(0);
        for (int i = 1; i < 3; i++) {
            Foam<R> foam = new Foam<R>(extGen.getWeb(), extGen.getWeb(), mOne);
            foam.addFacets(new Facet(edge, i));
            arr.addFoam(foam);
        }
    }
    
    @Override
    protected void createTensor(int i, SlThreeComplex<R> complex, SlTCache tCache,
            ArrayList<ArrayList<Generator<R>>> tgens) {
        LinkSlTComplex<R> nComp = (LinkSlTComplex<R>) complex;
        boolean fLevelZero = generators.get(i).contains(extGen.getBotArrows().get(0).getTopGenerator());
        SlTGenerator<R> nExtGen = extGen;
        ArrayList<Foam<R>> magFms = null;
        if (fLevelZero) {
            nExtGen = combineExtGens(nComp, tCache);
            SlTGenerator<R> eGen = nComp.extGen;
            SlTArrow<R> arr = (SlTArrow<R>) eGen.getBotArrows().get(0);
            Web oWeb = extGen.getWeb();
            magFms = foamsFromArrow(arr, nExtGen.getWeb(), nExtGen.getWeb(), oWeb, 1, tCache);
        }
        for (int l = 0; l < generators.get(i).size(); l++) {
            if (abInf.isAborted()) return;
            SlTGenerator<R> fGen = (SlTGenerator<R>) generators.get(i).get(l);
            boolean fInImage = fLevelZero && inImage(fGen);
            ArrayList<SlTGenerator<R>> nGens = new ArrayList<SlTGenerator<R>>(2);
            int t = complex.generatorSize()-1;
            for (int k = t; k >= 0; k--) {
                boolean sLevelZero = nComp.generators.get(k).contains(nComp.extGen.getBotArrows().get(0).getTopGenerator());
                for (int v = 0; v < nComp.generators.get(k).size(); v++) {
                    SlTGenerator<R> sGen = (SlTGenerator<R>) nComp.generators.get(k).get(v);
                    Web nWeb = fGen.getWeb().getNextWeb(k);
                    if (nWeb == null) {
                        nWeb = tCache.newWeb(fGen.getWeb(), sGen.getWeb());
                        fGen.getWeb().setNextWeb(nWeb, k);
                    }
                    SlTGenerator<R> nGen = new SlTGenerator<R>(fGen.hdeg()+sGen.hdeg(), 
                            fGen.qdeg()+sGen.qdeg(), nWeb);
                    tgens.get(i+k).add(nGen);
                    nGens.add(0, nGen);
                    if (k == 0 && t == 1) {
                        SlTArrow<R> arr = (SlTArrow<R>) sGen.getBotArrows().get(0);
                        int fac = 1;
                        if (fGen.hdeg()%2 != 0) fac = -1;
                        addArrow(arr, nGen, nGens.get(1), fGen.getWeb(), fac, tCache);
                    }
                    for (int j = 0; j < fGen.getBotArrows().size(); j++) {
                        SlTArrow<R> arr = (SlTArrow<R>) fGen.getBotArrows().get(j);
                        SlTGenerator<R> tGen = (SlTGenerator<R>) arr.getTopGenerator().getTopArrows().get(0).getTopGenerator();
                        if (k == 1) tGen = (SlTGenerator<R>) tGen.getBotArrows().get(0).getTopGenerator();
                        addArrow(arr, nGen, tGen, sGen.getWeb(), 1, tCache);
                    }
                    if (fInImage && sLevelZero) {
                        for (Iterator<Arrow<R>> it = extGen.getBotArrows().iterator(); it.hasNext();) {
                            SlTArrow<R> arr = (SlTArrow<R>) it.next();
                            if (arr.getTopGenerator() == fGen) {
                                ArrayList<Foam<R>> moves = foamsFromArrow(arr, nExtGen.getWeb(), 
                                        nGen.getWeb(), sGen.getWeb(), 1, tCache);
                                ArrayList<Foam<R>> nmoves = combineFoam(moves, magFms);
                                SlTArrow<R> nArr = new SlTArrow<R>(nExtGen, nGen, nmoves);
                                nExtGen.addBotArrow(nArr);
                            }// */
                        }
                    } 
                }
            }
            counter = counter + 2;
            frame.setLabelRight(""+counter, 2, false);
            fGen.clearTopArr();
            fGen.clearBotArr();
            SlTArrow<R> pointer = new SlTArrow<R>(fGen, nGens.get(0)); 
            fGen.addTopArrow(pointer);
        }
        if (i < generators.size()-1) generators.set(i+1, null); // throwing away old objects of hom degree i+1
        if (fLevelZero) extGen = nExtGen;
    }

    private ArrayList<Foam<R>> foamsFromArrow(SlTArrow<R> arr, Web nWeb, Web tWeb, 
            Web oldWeb, int f, SlTCache tCache) {
        ArrayList<Foam<R>> foams = new ArrayList<Foam<R>>();
        for (Foam<R> foam : arr.getFoams()) {
            R val = foam.getValue();
            if (f < 0) val = val.negate();
            Foam<R> nFoam = new Foam<R>(nWeb, tWeb, val);
            nFoam.addHorizontalFacets(foam, oldWeb, tCache);
            foams.add(nFoam);
        }
        return foams;
    }
    
    private SlTGenerator<R> combineExtGens(LinkSlTComplex<R> nComp, SlTCache tCache) {
        SlTGenerator<R> nGen = nComp.extGen;
        Web nWeb = tCache.newWeb(extGen.getWeb(), nGen.getWeb());
        return new SlTGenerator<R>(0, 0, nWeb);
    }

    private boolean inImage(SlTGenerator<R> fGen) {
        for (Iterator<Arrow<R>> it = extGen.getBotArrows().iterator(); it.hasNext();) {
            SlTArrow<R> arr = (SlTArrow<R>) it.next();
            if (arr.getTopGenerator() == fGen) return true;
        }
        return false;
    }

    private ArrayList<Foam<R>> combineFoam(ArrayList<Foam<R>> moves, ArrayList<Foam<R>> magFms) {
        ArrayList<Foam<R>> nmoves = new ArrayList<Foam<R>>();
        for (Foam<R> fm : moves) {
            for (Foam<R> mFm : magFms) {
                Foam<R> cFm = mFm.composeWith(fm);
                addMoves(nmoves, cFm);
            }
        }
        return nmoves;
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void addMoves(ArrayList<Foam<R>> nmoves, Foam<R> cFm) {
        for (Facet f : cFm.getFacets()) {
            while (f.getDots() >= 3) f.reduceDots(sType);
        }
        Foam<R> fm = getSameFoam(nmoves, cFm);
        if (fm == null) {
            nmoves.add(cFm);
            return;
        }
        fm.addValue(cFm.getValue());
        if (fm.getValue().isZero()) nmoves.remove(fm);
    }

    private Foam<R> getSameFoam(ArrayList<Foam<R>> nmoves, Foam<R> cFm) {
        for (Foam<R> fm : nmoves) if (fm.sameAs(cFm)) return fm;
        return null;
    }
    
    @Override
    protected void deloopGens(int i, ArrayList<ArrayList<Generator<R>>> tgens, 
            ArrayList<ArrayList<Generator<R>>> dgens, SlTCache dCache) {
        boolean lvlZero = tgens.get(i).contains(extGen.getBotArrows().get(0).getTopGenerator());
        for (int l = 0; l < tgens.get(i).size(); l++) {
            if (abInf.isAborted()) return;
            SlTGenerator<R> teGen = (SlTGenerator<R>) tgens.get(i).get(l);
            simplifyGenerator(teGen);
            ArrayList<SlTGenerator<R>> dtGens = new ArrayList<SlTGenerator<R>>();
            grabGenerators(teGen, dtGens);
            createDeloopedGenerators(teGen, dtGens, i, dgens, dCache);
        }
        if (lvlZero) {
            if (abInf.isAborted()) return;
            simplifyGenerator(extGen);
            ArrayList<SlTGenerator<R>> dtGens = new ArrayList<SlTGenerator<R>>();
            grabGenerators(extGen, dtGens);
            createDeloopedExtGen(extGen, dtGens.get(0), dCache);
        }
    }
    
    private void createDeloopedExtGen(SlTGenerator<R> tGen, SlTGenerator<R> dtGen,
            SlTCache dCache) {
        ArrayList<Web> knownWebs = new ArrayList<Web>();
        if (tGen.getWeb().getNextWeb(0) != null) knownWebs.add(tGen.getWeb().getNextWeb(0));
        if (tGen.getWeb().getNextWeb(1) != null) knownWebs.add(tGen.getWeb().getNextWeb(1));
        //ArrayList<SlTArrow<R>> newArrows = new ArrayList<SlTArrow<R>>();
        Foam nWeb = getTheWeb(dtGen.getWeb(), knownWebs, dCache);
        SlTGenerator<R> ntGen = new SlTGenerator<R>(dtGen.hdeg(), dtGen.qdeg(), nWeb.getCoWeb());
        //dgens.get(i).add(ntGen);
        /*SlTArrow<R> nArr = */getDeloopedArrow(tGen, ntGen, dtGen, nWeb);
        //newArrows.add(nArr);
        getComposedArrows(tGen, ntGen);
        simplifyArrowsFrom(ntGen);
        extGen = ntGen;
        //tGen.clearTopArr();
        //for (SlTArrow<R> arr : newArrows) tGen.addTopArrow(arr); // pointers to the ntGen
        //if (tGen.getWeb().getNextWeb(0) == null) tGen.getWeb().setNextWeb(knownWebs.get(0), 0);
        //if (tGen.getWeb().getNextWeb(1) == null && knownWebs.size() > 1) 
        //    tGen.getWeb().setNextWeb(knownWebs.get(1), 1);
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
            SlTGenerator<R> gen = (SlTGenerator<R>) generators.get(i).get(c);
            if (!gen.getBotArrows().isEmpty()) {
                cont = true;
                for (Iterator<Arrow<R>> at = gen.getBotArrows().iterator(); at.hasNext();) {
                    SlTArrow<R> ar = (SlTArrow<R>) at.next();
                    if (abInf.isAborted()) return false;
                    if (qDifference(ar) == jump) {
                        cancelGen(ar, i);
                        break;
                    }
                }
            }
            c--;
        }
        return cont;
    }
    
    private int qDifference(SlTArrow<R> ar) {
        SlTGenerator<R> bGen = (SlTGenerator<R>) ar.getBotGenerator();
        SlTGenerator<R> tGen = (SlTGenerator<R>) ar.getTopGenerator();
        return tGen.qdeg() - bGen.qdeg();
    }
    
    private void cancelGen(SlTArrow<R> ar, int i) {
        SlTGenerator<R> yGen = (SlTGenerator<R>) ar.getBotGenerator();
        SlTGenerator<R> xGen = (SlTGenerator<R>) ar.getTopGenerator();
        R val = ar.getFoams().get(0).getValue();
        yGen.getBotArrows().remove(ar);
        xGen.getTopArrows().remove(ar);
        for (Arrow<R> tr : xGen.getBotArrows()) tr.getTopGenerator().getTopArrows().remove(tr);
        for (Arrow<R> br : yGen.getTopArrows()) br.getBotGenerator().getBotArrows().remove(br);
        generators.get(i).remove(yGen);
        generators.get(i+1).remove(xGen);
        for (Iterator<Arrow<R>> it = xGen.getTopArrows().iterator(); it.hasNext();) {
            SlTArrow<R> fmr = (SlTArrow<R>) it.next();
            for (Iterator<Arrow<R>> itt = yGen.getBotArrows().iterator(); itt.hasNext();) {
                SlTArrow<R> smr = (SlTArrow<R>) itt.next();
                SlTGenerator<R> bGen = (SlTGenerator<R>) fmr.getBotGenerator();
                SlTGenerator<R> tGen = (SlTGenerator<R>) smr.getTopGenerator();
                SlTArrow<R> oAr = arrowBetween(bGen, tGen);
                R nv = oAr.getFoams().get(0).getValue().add(smr.getFoams().get(0).getValue()
                        .multiply(fmr.getFoams().get(0).getValue()).multiply(val.invert()).negate());
                if (nv.isZero()) {
                    bGen.getBotArrows().remove(oAr);
                    tGen.getTopArrows().remove(oAr);
                }
                else oAr.getFoams().get(0).setValue(nv);
            }
        }
        for (Arrow<R> mr : xGen.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        for (Arrow<R> mr : yGen.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
    }
    
    private SlTArrow<R> arrowBetween(SlTGenerator<R> bGen, SlTGenerator<R> tGen) {
        for (Iterator<Arrow<R>> it = bGen.getBotArrows().iterator(); it.hasNext();) {
            SlTArrow<R> ar = (SlTArrow<R>) it.next();
            if (ar.getTopGenerator() == tGen) return ar;
        }
        SlTArrow<R> nAr = new SlTArrow<R>(bGen, tGen);
        Foam<R> cob = new Foam<R>(new Web(), new Web(), unit.getZero());
        nAr.addFoam(cob);
        bGen.addBotArrow(nAr);
        tGen.addTopArrow(nAr);
        return nAr;
    }
    
    public boolean cocycleCheck() {
        ArrayList<SlTGenerator<R>> img = new ArrayList<SlTGenerator<R>>();
        ArrayList<R> endValues = new ArrayList<R>();
        for (Arrow<R> arr : extGen.getBotArrows()) {
            SlTArrow<R> ar = (SlTArrow<R>) arr;
            R val = ar.getValue();
            SlTGenerator<R> next = (SlTGenerator<R>) ar.getTopGenerator();
            for (int i = 0; i < next.getBotArrows().size(); i++) {
                SlTArrow<R> sar = (SlTArrow<R>) next.getBotArrows().get(i);
                int pos = positionOf((SlTGenerator<R>) sar.getTopGenerator(), img);
                if (pos < 0) {
                    img.add((SlTGenerator<R>) sar.getTopGenerator());
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

    private int positionOf(SlTGenerator<R> topGenerator, ArrayList<SlTGenerator<R>> img) {
        for (int i = 0; i < img.size(); i++) {
            if (img.get(i) == topGenerator) return i;
        }
        return -1;
    }

    public int maximalImageQs() {
        if (abInf.isAborted()) return 0;
        Iterator<Arrow<R>> it = extGen.getBotArrows().iterator();
        SlTArrow<R> arr = (SlTArrow<R>) it.next();
        int max = ((SlTGenerator<R>) arr.getTopGenerator()).qdeg();
        while (it.hasNext()) {
            arr = (SlTArrow<R>) it.next();
            int q = ((SlTGenerator<R>) arr.getTopGenerator()).qdeg();
            if (max < q) max = q;
        }
        return max;
    }
    
}
