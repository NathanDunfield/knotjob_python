/*

Copyright (C) 2024 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import knotjob.homology.Homology;
import knotjob.homology.QuantumCohomology;
import knotjob.homology.slthree.SlTArrow;
import knotjob.homology.slthree.SlTGenerator;
import knotjob.homology.slthree.SlThreeComplex;
import knotjob.homology.slthree.foam.Edge;
import knotjob.homology.slthree.foam.SlTCache;
import knotjob.homology.slthree.foam.Web;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class UnivSlTComplex<R extends Ring<R>> extends SlThreeComplex<R> {
    
    public UnivSlTComplex(int[] pts, int c, int factor, R unt, int s, DialogWrap frm, AbortInfo abf) {
        super(pts, c, factor, unt, s, frm, abf);
    }
    
    public UnivSlTComplex(boolean red, R unt, AbortInfo abf, DialogWrap frm) {
        super(red, unt, abf, frm);
    }

    public UnivSlTComplex(R unt, DialogWrap frm, AbortInfo abf, int s, 
            int p, int n) {
        super(unt, frm, abf, s, p, n);
    }
    
    @Override
    public boolean boundaryCheck() {
        boolean check = true;
        int i = 0;
        while (check && i < generators.size()-2) {
            ArrayList<Generator<R>> gens = generators.get(i);
            ArrayList<R> values = new ArrayList<R>();
            while (values.size() < generators.get(i+2).size()) values.add(unit.getZero());
            int j = 0;
            while (check && j < gens.size()) {
                SlTGenerator<R> bGen = (SlTGenerator<R>) gens.get(j);
                for (Arrow<R> arr : bGen.getBotArrows()) {
                    SlTGenerator<R> mGen = (SlTGenerator<R>) arr.getTopGenerator();
                    for (Arrow<R> tar : mGen.getBotArrows()) {
                        SlTGenerator<R> tGen = (SlTGenerator<R>) tar.getTopGenerator();
                        int k = generators.get(i+2).indexOf(tGen);
                        values.set(k, values.get(k).add(arr.getValue().multiply(tar.getValue())));
                    }
                }
                int k = 0;
                while (check && k < values.size()) {
                    if (!values.get(k).isZero()) check = false;
                    else k++;
                }
                if (!check) System.out.println(i+" "+j+" "+
                        k+" "+values);
                j++;
                
            }
            i++;
        }
        return check;
    }
    
    @Override
    public ArrayList<String> homologyInfo() {
        ArrayList<String> theStrings = new ArrayList<String>();
        int[] qmaxmin = maxMinQ();
        int q = qmaxmin[0];
        ArrayList<QuantumCohomology> qCohs = new ArrayList<QuantumCohomology>();
        while (q >= qmaxmin[1]) {
            qCohs.add(new QuantumCohomology(q));
            q = q - 2;
        }
        for (int i = 0; i < generators.size(); i++) {
            for (int j = 0; j < generators.get(i).size(); j++) {
                SlTGenerator<R> gen = (SlTGenerator<R>) generators.get(i).get(j);
                addGenerator(qCohs, gen);
            }
        }
        for (QuantumCohomology coh : qCohs) theStrings.add(coh.toString());
        return theStrings;
    }
    
    private void addGenerator(ArrayList<QuantumCohomology> qCohs, SlTGenerator<R> gen) {
        QuantumCohomology qCoh = theCohomology(qCohs, gen.qdeg());
        Homology hom = qCoh.findHomology(gen.hdeg(), true);
        hom.setBetti(hom.getBetti()+1);
    }
    
    private QuantumCohomology theCohomology(ArrayList<QuantumCohomology> qCohs, int q) {
        boolean found = false;
        int i = 0;
        while (!found && i < qCohs.size()) {
            if (qCohs.get(i).qdeg() == q) found = true;
            else i++;
        }
        return qCohs.get(i);
    }
    
    private int[] maxMinQ() {
        int j = firstNonEmpty();
        int qmin = ((SlTGenerator<R>) generators.get(j).get(0)).qdeg();
        int qmax = qmin;
        for (int i = 0; i < generators.size(); i++) {
            for (int k = 0; k < generators.get(i).size(); k++) {
                int q = ((SlTGenerator<R>) generators.get(i).get(k)).qdeg();
                if (qmin > q) qmin = q;
                if (qmax < q) qmax = q;
            }
        }
        return new int[] {qmax, qmin};
    }
    
    private int firstNonEmpty() {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (!generators.get(i).isEmpty()) found = true;
            else i++;
        }
        return i;
    }
    
    @Override
    public boolean containsBoundaries() {
        boolean contains = false;
        int i = 0;
        while (!contains && i < generators.size()) {
            ArrayList<Generator<R>> gens = generators.get(i);
            int j = 0;
            while (!contains && j < gens.size()) {
                SlTGenerator<R> gen = (SlTGenerator<R>) gens.get(j);
                if (!gen.getBotArrows().isEmpty()) contains = true;
                else j++;
            }
            i++;
        }
        return contains;
    }
    
    @Override
    public void cancelGenerators(int qJump) {
        int i = generators.size()-1;
        while (i >= 0) {
            ArrayList<Generator<R>> gens = generators.get(i);
            int j = gens.size()-1;
            while (j >= 0) {
                SlTGenerator<R> gen = (SlTGenerator<R>) gens.get(j);
                SlTArrow<R> cancelMor = cancellingArrow(gen, qJump);
                if (cancelMor != null) cancel(cancelMor, i);
                j--;
            }
            i--;
        }
    }
    
    private SlTArrow<R> cancellingArrow(SlTGenerator<R> gen, int qJump) {
        boolean found = false;
        int i = 0;
        while (i < gen.getBotArrows().size() && !found) {
            SlTArrow<R> mor = (SlTArrow<R>) gen.getBotArrows().get(i);
            if (((SlTGenerator<R>) mor.getTopGenerator()).qdeg() - 
                    ((SlTGenerator<R>) mor.getBotGenerator()).qdeg() == qJump) found = true;
            else i++;
        }
        if (!found) return null;
        return (SlTArrow<R>) gen.getBotArrows().get(i);
    }
    
    private void cancel(SlTArrow<R> arrow, int level) {
        SlTGenerator<R> tGen = (SlTGenerator<R>) arrow.getTopGenerator();
        SlTGenerator<R> bGen = (SlTGenerator<R>) arrow.getBotGenerator();
        bGen.getBotArrows().remove(arrow);
        tGen.getTopArrows().remove(arrow);
        for (Arrow<R> mr : tGen.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
        for (Arrow<R> mr : bGen.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        R u = arrow.getValue();
        generators.get(level).remove(bGen);
        generators.get(level+1).remove(tGen);
        for (Iterator<Arrow<R>> it = tGen.getTopArrows().iterator(); it.hasNext();) {
            SlTArrow<R> far = (SlTArrow<R>) it.next();
            for (Iterator<Arrow<R>> itt = bGen.getBotArrows().iterator(); itt.hasNext();) {
                SlTArrow<R> sar = (SlTArrow<R>) itt.next();
                SlTGenerator<R> aGen = (SlTGenerator<R>) far.getBotGenerator();
                SlTGenerator<R> zGen = (SlTGenerator<R>) sar.getTopGenerator();
                SlTArrow<R> azr = getArrowBetween(aGen, zGen);
                azr.addValue(far.getValue().multiply(u.invert()).multiply(sar.getValue()).negate());
                if (azr.getValue().isZero()) {
                    aGen.getBotArrows().remove(azr);
                    zGen.getTopArrows().remove(azr);
                }
            }
        }
        for (Arrow<R> mr : tGen.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        for (Arrow<R> mr : bGen.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
    }
    
    private SlTArrow<R> getArrowBetween(SlTGenerator<R> bGen, SlTGenerator<R> tGen) {
        boolean found = false;
        int i = 0;
        while (!found && i < bGen.getBotArrows().size()) {
            SlTArrow<R> ar = (SlTArrow<R>) bGen.getBotArrows().get(i);
            if (ar.getTopGenerator() == tGen) found = true;
            else i++;
        }
        if (found) return ((SlTArrow<R>) bGen.getBotArrows().get(i));
        SlTArrow<R> arr = new SlTArrow<R>(bGen, tGen);
        arr.setValue(unit.getZero());
        bGen.addBotArrow(arr);
        tGen.addTopArrow(arr);
        return arr;
    }
    
    public void finishOffRed(String gInfo, boolean hd) {
        ArrayList<Integer> lepts = cache.lastEndpoints();
        UnivSlTComplex<R> finComplex = new UnivSlTComplex<R>(unit, frame, abInf, 
                sType, lepts.get(1), lepts.get(0));
        this.lastModification(finComplex, gInfo, hd);
    }

    private void lastModification(UnivSlTComplex<R> complex, String gInfo, boolean hd) {
        SlTCache tCache = new SlTCache(cache, complex.cache, true);
        SlTCache dCache = new SlTCache(cache, complex.cache, false);
        frame.setLabelRight(gInfo, 1, false);
        counter = 0;
        getFinalGenerators(complex, tCache, dCache, hd);
        cache = dCache;
    }

    private void getFinalGenerators(UnivSlTComplex<R> complex, SlTCache tCache, SlTCache dCache, 
            boolean hd) {
        ArrayList<ArrayList<Generator<R>>> tgens = new ArrayList<ArrayList<Generator<R>>>(generators.size()+complex.generators.size()-1);
        ArrayList<ArrayList<Generator<R>>> dgens = new ArrayList<ArrayList<Generator<R>>>(generators.size()+complex.generators.size()-1);
        for (int i = 0; i < generators.size()+complex.generators.size()-1; i++) {
            ArrayList<Generator<R>> tgeni = new ArrayList<Generator<R>>();
            tgens.add(tgeni);
            ArrayList<Generator<R>> dgeni = new ArrayList<Generator<R>>();
            dgens.add(dgeni);
        }
        int i = generators.size()-1;
        while (i >= -3) {
            if (hd) frame.setLabelRight(String.valueOf(i+3), 3, false);
            if (i >= 0) createTensor(i, complex, tCache, tgens);
            if (i < tgens.size()-2 && i > -3) {
                deloopLastGens(i+2, tgens, dgens, dCache);
                counter = counter + dgens.get(i+2).size()-tgens.get(i+2).size();
                tgens.set(i+2, null);
            }
            if (i < tgens.size()-3) {
                boolean cancel = true;
                while (cancel) cancel = gaussEliminate(i+3, dgens, hd);
            }
            i--;
        }
        generators = dgens;
    }

    private void deloopLastGens(int i, ArrayList<ArrayList<Generator<R>>> tgens, 
            ArrayList<ArrayList<Generator<R>>> dgens, SlTCache dCache) {
        for (int l = 0; l < tgens.get(i).size(); l++) {
            if (abInf.isAborted()) return;
            SlTGenerator<R> teGen = (SlTGenerator<R>) tgens.get(i).get(l);
            simplifyLastGenerator(teGen);
            ArrayList<SlTGenerator<R>> dtGens = new ArrayList<SlTGenerator<R>>();
            grabGenerators(teGen, dtGens);
            createDeloopedGenerators(teGen, dtGens, i, dgens, dCache);
        }
    }

    private void simplifyLastGenerator(SlTGenerator<R> teGen) {
        Web web = teGen.getWeb();
        Edge circle = web.getCircle();
        removeLastCircle(teGen, circle);
    }

    private void removeLastCircle(SlTGenerator<R> teGen, Edge circle) {
        Web web = teGen.getWeb();
        Web nWeb = removeCircle(web, circle);
        SlTGenerator<R> npGen = new SlTGenerator<R>(teGen.hdeg(), teGen.qdeg(), nWeb);
        SlTArrow<R> pArr = new SlTArrow<R>(teGen, npGen);
        pArr.addFoam(removeCircleFoam(web, nWeb, circle, 0, unit.negate()));
        teGen.addTopArrow(pArr);
        pArr = new SlTArrow<R>(npGen, teGen);
        pArr.addFoam(createCircleFoam(nWeb, web, circle, 2));
        npGen.addBotArrow(pArr);
    }

    public int lastQDegree() {
        ArrayList<SlTGenerator<R>> lastGens = new ArrayList<SlTGenerator<R>>();
        for (int i = 0; i < generators.size(); i++) {
            for (int j = 0; j < generators.get(i).size(); j++) {
                SlTGenerator<R> gen = (SlTGenerator<R>) generators.get(i).get(j);
                if (gen.hdeg() == 0) lastGens.add(gen);
            }
        }
        if (sType == 1 && lastGens.size() != 1) System.out.println("Problem UnivSlTComplex "+lastGens.size());
        if (sType >= 2 && lastGens.size() != 3) System.out.println("Problem 2 UnivSlTComplex");
        int val = 0;
        for (SlTGenerator<R> gen : lastGens) val = val + gen.qdeg();
        return val / lastGens.size();
    }
    
}
