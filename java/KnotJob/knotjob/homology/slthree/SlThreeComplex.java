/*

Copyright (C) 2023-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.slthree;

import knotjob.homology.slthree.foam.SlTCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.ChainComplex;
import knotjob.homology.Generator;
import knotjob.homology.QGenerator;
import knotjob.homology.slthree.foam.Edge;
import knotjob.homology.slthree.foam.Facet;
import knotjob.homology.slthree.foam.Foam;
import knotjob.homology.slthree.foam.SingEdge;
import knotjob.homology.slthree.foam.Vertex;
import knotjob.homology.slthree.foam.Web;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class SlThreeComplex<R extends Ring<R>> extends ChainComplex<R> {
    
    protected SlTCache cache;
    protected final int sType; 
    protected int counter;
    //public boolean switcher = false;
    
    public SlThreeComplex(boolean red, R unt, AbortInfo abf, DialogWrap frm) {
        super(unt, frm, abf);
        sType = 0;
        cache = new SlTCache();
        Web web = new Web();
        cache.addWeb(web);
        ArrayList<Generator<R>> gens = new ArrayList<Generator<R>>();
        generators.add(gens);
        SlTGenerator<R> tGen = new SlTGenerator<R>(0, 2, web);
        SlTGenerator<R> zGen = new SlTGenerator<R>(0, 0, web);
        SlTGenerator<R> mGen = new SlTGenerator<R>(0, -2, web);
        if (!red) gens.add(tGen);
        gens.add(zGen);
        if (!red) gens.add(mGen);
    }

    public SlThreeComplex(int[] pts, int c, int factor, R unt, int s, DialogWrap frm, AbortInfo abf) {
        super(unt, frm, abf);
        sType = s;
        cache = new SlTCache(pts, -(2*c+1), -(2*c+2));
        addVerticesEdges();
        addGenerators(factor);
        addArrow(factor);
    }
    
    public SlThreeComplex(R unt, DialogWrap frm, AbortInfo abf, int s) {
        super(unt, frm, abf);
        sType = s;
    }
    
    public SlThreeComplex(R unt, DialogWrap frm, AbortInfo abf, int s, int p, int n) {
        super(unt, frm, abf);
        sType = s;
        cache = new SlTCache(p, n);
        cache.addEdge(0, 1);
        cache.addWeb(new Web(new Vertex[] { }, new Edge[] {cache.getEdge(0)}));
        ArrayList<Generator<R>> gens = new ArrayList<Generator<R>>();
        gens.add(new SlTGenerator<R>(0, 0, cache.getWeb(0, 0)));
        generators.add(gens);
    }

    private void addVerticesEdges() {
        cache.addEdge(0, 2);
        cache.addEdge(1, 3);
        cache.addEdge(0, 5);
        cache.addEdge(1, 5);
        cache.addEdge(4, 2);
        cache.addEdge(4, 3);
        cache.addEdge(4, 5);
        Web wOne = new Web(new Vertex[] { }, 
                            new Edge[] {cache.getEdge(0), cache.getEdge(1)});
        Web wTwo = new Web(new Vertex[] {cache.getVertex(4), cache.getVertex(5)}, 
                new Edge[] {cache.getEdge(2), cache.getEdge(3), cache.getEdge(4),
                            cache.getEdge(5), cache.getEdge(6)});
        wTwo.addTriple(cache.getEdge(4), cache.getEdge(5), cache.getEdge(6));
        wTwo.addTriple(cache.getEdge(2), cache.getEdge(3), cache.getEdge(6));
        cache.addWeb(wOne);
        cache.addWeb(wTwo);
    }

    private void addGenerators(int factor) {
        SlTGenerator<R> zGen = new SlTGenerator<R>(0, 2*factor, cache.getWeb(0, 0));
        SlTGenerator<R> oGen = new SlTGenerator<R>(-factor, 3*factor, cache.getWeb(1, 0));
        ArrayList<Generator<R>> fGens = new ArrayList<Generator<R>>();
        ArrayList<Generator<R>> sGens = new ArrayList<Generator<R>>();
        if (factor == 1) {
            fGens.add(oGen);
            sGens.add(zGen);
        }
        else {
            fGens.add(zGen);
            sGens.add(oGen);
        }
        generators.add(fGens);
        generators.add(sGens);
    }

    private void addArrow(int factor) {
        SlTGenerator<R> zGen = (SlTGenerator<R>) generators.get(0).get(0);
        SlTGenerator<R> oGen = (SlTGenerator<R>) generators.get(1).get(0);
        SlTArrow<R> arr = new SlTArrow<R>(zGen, oGen);
        Foam<R> foam = new Foam<R>(zGen.getWeb(), oGen.getWeb(), unit);
        arr.addFoam(foam);
        zGen.addBotArrow(arr);
        oGen.addTopArrow(arr);
        int a = 4;
        int b = 5;
        int stl = 1;
        int enl = 1;
        if (factor == 1) {
            a = 5;
            b = 4;
            stl = 0;
            enl = 0;
        }
        SingEdge sEdge = new SingEdge(cache.getVertex(a), cache.getVertex(b), stl, enl);
        cache.addEdge(sEdge);
        foam.addSingEdge(sEdge);
        Facet fFac = new Facet(0, 1);
        Facet sFac = new Facet(0, 1);
        Facet tFac = new Facet(0, 1);
        foam.addSingFacets(fFac, sFac, tFac);
        foam.addFacets(fFac);
        foam.addFacets(sFac);
        foam.addFacets(tFac);
        Edge[] fbEdges = new Edge[] {cache.getEdge(2), cache.getEdge(4)};
        Edge[] ftEdges = new Edge[] {cache.getEdge(0)};
        Edge[] sbEdges = new Edge[] {cache.getEdge(3), cache.getEdge(5)};
        Edge[] stEdges = new Edge[] {cache.getEdge(1)};
        Edge[] tbEdges = new Edge[] {cache.getEdge(6)};
        Edge[] ttEdges = new Edge[0];
        if (factor == 1) {
            fFac.addEdges(fbEdges, ftEdges);
            sFac.addEdges(sbEdges, stEdges);
            tFac.addEdges(tbEdges, ttEdges);
        }
        else {
            fFac.addEdges(ftEdges, fbEdges);
            sFac.addEdges(stEdges, sbEdges);
            tFac.addEdges(ttEdges, tbEdges);
        }
    }
    
    @Override
    public void output() {
        this.output(0,generators.size());
    }
    
    @Override
    public void output(int fh, int lh) {
        cache.output();
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
    
    @Override
    public ArrayList<Integer> getQs() {
        ArrayList<Integer> theQs = new ArrayList<Integer>();
        for (ArrayList<Generator<R>> gens : generators) {
            for (Iterator<Generator<R>> it = gens.iterator(); it.hasNext();) {
                QGenerator<R> gen = (QGenerator<R>) it.next();
                if (!theQs.contains(gen.qdeg())) theQs.add(gen.qdeg());
            }
        }
        Collections.sort(theQs);
        return theQs;
    }
    
    @Override
    public ChainComplex<R> getQComplex(int q) {
        ArrayList<ArrayList<Generator<R>>> genes = new ArrayList<ArrayList<Generator<R>>>(generators.size());
        int p = 0;
        for (ArrayList<Generator<R>> gens : generators) {
            genes.add(new ArrayList<Generator<R>>());
            int i = gens.size()-1;
            while (i >= 0) {
                QGenerator<R> gen = (QGenerator<R>) gens.get(i);
                if (gen.qdeg() == q) {
                    Generator<R> clGen = new Generator<R>(gen.hdeg());
                    genes.get(p).add(clGen);
                    gen.clearBotArr();
                    gen.addBotArrow(new Arrow<R>(gen, clGen, unit));
                    for (int j = 0; j < gen.getTopArrowSize(); j++) {
                        Arrow<R> arrow = gen.getTopArrow(j);
                        R val = arrow.getValue();
                        Generator<R> bGen = arrow.getBotGenerator().getBotArrows().get(0).getTopGenerator();
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
    }
    
    public boolean direction(int p) {
        return cache.negEndptsContains(p);
    }
    
    public ArrayList<Integer> overlap(int[] path) {
        ArrayList<Integer> ovlp = new ArrayList<Integer>();
        for (int i = 0; i < 4; i++) {
            if (cache.posEndptsContains(path[i])) ovlp.add(i);
            if (cache.negEndptsContains(path[i])) ovlp.add(i);
        }
        return ovlp;
    }
    
    public void modifyComplex(SlThreeComplex<R> complex, String gInfo, boolean det) {
        SlTCache tCache = new SlTCache(cache, complex.cache, true);
        SlTCache dCache = new SlTCache(cache, complex.cache, false);
        frame.setLabelRight(gInfo, 1, false);
        counter = 0;
        getNewGenerators(complex, tCache, dCache, det);
        checkArrows(generators);
    }
    
    public ArrayList<Integer> getLastEndpoints() {
        return cache.lastEndpoints();
    }
    
    public void testJumps() { // tests by how much quantum degree the boundary can jump.
        int biggest = 0;
        for (ArrayList<Generator<R>> gens : generators) {
            for (int i = 0; i < gens.size(); i++) {
                SlTGenerator<R> bGen = (SlTGenerator<R>) gens.get(i);
                int q = bGen.qdeg();
                for (int j = 0; j < bGen.getBotArrows().size(); j++) {
                    SlTArrow<R> arr = (SlTArrow<R>) bGen.getBotArrows().get(j);
                    SlTGenerator<R> tGen = (SlTGenerator<R>) arr.getTopGenerator();
                    if (q - tGen.qdeg() > biggest) biggest = q - tGen.qdeg();
                }
            }
        }
        System.out.println(biggest+" . "+cache.lastEndpoints().size());
    }
    
    public int checkQofArrows() {
        int count = 0;
        for (ArrayList<Generator<R>> gens : generators) {
            for (int i = 0; i < gens.size(); i++) {
                SlTGenerator<R> bGen = (SlTGenerator<R>) gens.get(i);
                int q = bGen.qdeg();
                for (int j = 0; j < bGen.getBotArrows().size(); j++) {
                    SlTArrow<R> arr = (SlTArrow<R>) bGen.getBotArrows().get(j);
                    SlTGenerator<R> tGen = (SlTGenerator<R>) arr.getTopGenerator();
                    for (Foam<R> foam : arr.getFoams()) {
                        int deg = foam.degree();
                        count++;
                        if (q-deg != tGen.qdeg()) System.out.println(q+" "+deg+" "+tGen.qdeg());
                    }
                }
            }
        }
        for (ArrayList<Generator<R>> gens : generators) {
            for (int i = 0; i < gens.size(); i++) {
                SlTGenerator<R> tGen = (SlTGenerator<R>) gens.get(i);
                int q = tGen.qdeg();
                for (int j = 0; j < tGen.getTopArrows().size(); j++) {
                    SlTArrow<R> arr = (SlTArrow<R>) tGen.getTopArrows().get(j);
                    SlTGenerator<R> bGen = (SlTGenerator<R>) arr.getBotGenerator();
                    for (Foam<R> foam : arr.getFoams()) {
                        int deg = foam.degree();
                        count++;
                        if (q+deg != bGen.qdeg()) System.out.println(q+" "+deg+" "+bGen.qdeg());
                    }
                }
            }
        }
        return count;
    }
    
    private void checkArrows(ArrayList<ArrayList<Generator<R>>> gns) {  // this is checking whether a singular circle 
        int coun = 0;                                                   // can have the same facet more than once 
        for (ArrayList<Generator<R>> gens : gns) {                      // 
            for (int i = 0; i < gens.size(); i++) {
                SlTGenerator<R> bGen = (SlTGenerator<R>) gens.get(i);
                for (int j = 0; j < bGen.getBotArrows().size(); j++) {
                    SlTArrow<R> arr = (SlTArrow<R>) bGen.getBotArrows().get(j);
                    for (Foam<R> foam : arr.getFoams()) {
                        coun++;
                        if (foam.hasStrangeFacets()) System.out.println("Hey");
                    }
                }
            }
        }
        //System.out.println("Total check "+coun);
    }// */

    private void getNewGenerators(SlThreeComplex<R> complex, SlTCache tCache, 
            SlTCache dCache, boolean det) {
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
            if (det) frame.setLabelRight(String.valueOf(i+3), 3, false);
            if (i >= 0) createTensor(i, complex, tCache, tgens);
            if (i < tgens.size()-2 && i > -3) {
                deloopGens(i+2, tgens, dgens, dCache);
                counter = counter + dgens.get(i+2).size()-tgens.get(i+2).size();
                tgens.set(i+2, null);
            }
            if (i < tgens.size()-3) {
                boolean cancel = true;
                while (cancel) cancel = gaussEliminate(i+3, dgens, det);
            }
            i--;
        }
        generators = dgens;
        cache = dCache;
     }

    protected boolean gaussEliminate(int i, ArrayList<ArrayList<Generator<R>>> dgens,
            boolean det) {
        boolean cont = false;
        ArrayList<Generator<R>> gens = dgens.get(i);
        int j = gens.size()-1;
        while (j >= 0) {
            if (abInf.isAborted()) return false;
            SlTGenerator<R> bGen = (SlTGenerator<R>) gens.get(j);
            boolean found = false;
            int k = 0;
            while (!found && k < bGen.getBotArrows().size()) {
                SlTArrow<R> arr = (SlTArrow<R>) bGen.getBotArrows().get(k);
                if (canCancel(arr)) found = true;
                else k++;
            }
            if (found) {
                cancelObject((SlTArrow<R>) bGen.getBotArrows().get(k), dgens, i);
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
    
    private void cancelObject(SlTArrow<R> arr, ArrayList<ArrayList<Generator<R>>> dgens,
            int i) {
        SlTGenerator<R> yGen = (SlTGenerator<R>) arr.getBotGenerator();
        SlTGenerator<R> xGen = (SlTGenerator<R>) arr.getTopGenerator();
        yGen.getBotArrows().remove(arr);
        xGen.getTopArrows().remove(arr);
        for (Arrow<R> mr : xGen.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
        for (Arrow<R> mr : yGen.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        dgens.get(i).remove(yGen);
        dgens.get(i+1).remove(xGen);
        for (Iterator<Arrow<R>> it = xGen.getTopArrows().iterator(); it.hasNext();) {
            SlTArrow<R> far = (SlTArrow<R>) it.next();
            for (Iterator<Arrow<R>> itt = yGen.getBotArrows().iterator(); itt.hasNext();) {
                SlTArrow<R> sar = (SlTArrow<R>) itt.next();
                SlTGenerator<R> bGen = (SlTGenerator<R>) far.getBotGenerator();
                SlTGenerator<R> tGen = (SlTGenerator<R>) sar.getTopGenerator();
                ArrayList<Foam<R>> newFoams = zigZagFoams(far, sar, arr);
                getNewFoams(bGen, tGen, newFoams);
                if (abInf.isAborted()) return;
            }
        }
        for (Arrow<R> ar : xGen.getTopArrows()) ar.getBotGenerator().getBotArrows().remove(ar);
        for (Arrow<R> ar : yGen.getBotArrows()) ar.getTopGenerator().getTopArrows().remove(ar);
    }
    
    private void getNewFoams(SlTGenerator<R> bGen, SlTGenerator<R> tGen, 
            ArrayList<Foam<R>> nFoams) {
        boolean found = false;
        int k = 0;
        SlTArrow<R> arr;
        while (!found && k < bGen.getBotArrows().size()) {
            arr = (SlTArrow<R>) bGen.getBotArrows().get(k);
            if (arr.getTopGenerator() == tGen) found = true;
            else k++;
        }
        if (!found) {
            arr = new SlTArrow<R>(bGen, tGen);
            bGen.addBotArrow(arr);
            tGen.addTopArrow(arr);
        }
        else arr = (SlTArrow<R>) bGen.getBotArrows().get(k);
        combineFoams(arr, nFoams);
    }
    
    private void combineFoams(SlTArrow<R> arr, ArrayList<Foam<R>> nFoams) {
        for (Foam<R> foam : nFoams) arr.addFoam(foam);
        simplifyArrow(arr);
    }
    
    private ArrayList<Foam<R>> zigZagFoams(SlTArrow<R> far, SlTArrow<R> sar, 
            SlTArrow<R> arr) {
        ArrayList<Foam<R>> newFoams = new ArrayList<Foam<R>>();
        Foam<R> pFoam = arr.getFoams().get(0).reverse();
        R u = pFoam.getValue().invert().negate();
        pFoam.setValue(u);
        for (Foam<R> fFoam : far.getFoams()) {
            for (Foam<R> sFoam : sar.getFoams()) {
                newFoams.add(fFoam.composeWith(pFoam).composeWith(sFoam));
            }
        }
        return newFoams;
    }
    
    private boolean canCancel(SlTArrow<R> arr) {
        if (arr.getFoams().size() != 1) return false;
        if (((SlTGenerator<R>) arr.getBotGenerator()).qdeg() != 
                ((SlTGenerator<R>) arr.getTopGenerator()).qdeg()) return false;
        Foam<R> foam = arr.getFoams().get(0);
        if (!foam.getValue().isInvertible()) return false;
        return foam.isProduct();
    }
    
    private void deloopGens(int i, ArrayList<ArrayList<Generator<R>>> tgens, 
            ArrayList<ArrayList<Generator<R>>> dgens, SlTCache dCache) {
        for (int l = 0; l < tgens.get(i).size(); l++) {
            if (abInf.isAborted()) return;
            SlTGenerator<R> teGen = (SlTGenerator<R>) tgens.get(i).get(l);
            simplifyGenerator(teGen);
            ArrayList<SlTGenerator<R>> dtGens = new ArrayList<SlTGenerator<R>>();
            grabGenerators(teGen, dtGens);
            createDeloopedGenerators(teGen, dtGens, i, dgens, dCache);
        }
    }
    
    protected void createDeloopedGenerators(SlTGenerator<R> tGen, ArrayList<SlTGenerator<R>> dtGens,
            int i, ArrayList<ArrayList<Generator<R>>> dgens, SlTCache dCache) {
        ArrayList<Web> knownWebs = new ArrayList<Web>();
        if (tGen.getWeb().getNextWeb(0) != null) knownWebs.add(tGen.getWeb().getNextWeb(0));
        if (tGen.getWeb().getNextWeb(1) != null) knownWebs.add(tGen.getWeb().getNextWeb(1));
        ArrayList<SlTArrow<R>> newArrows = new ArrayList<SlTArrow<R>>();
        for (SlTGenerator<R> nGen : dtGens) {
            Foam nWeb = getTheWeb(nGen.getWeb(), knownWebs, dCache);
            SlTGenerator<R> ntGen = new SlTGenerator<R>(nGen.hdeg(), nGen.qdeg(), nWeb.getCoWeb());
            dgens.get(i).add(ntGen);
            SlTArrow<R> nArr = getDeloopedArrow(tGen, ntGen, nGen, nWeb);
            newArrows.add(nArr);
            getComposedArrows(tGen, ntGen);
            simplifyArrowsFrom(ntGen);
        }
        tGen.clearTopArr();
        for (SlTArrow<R> arr : newArrows) tGen.addTopArrow(arr); // pointers to the ntGen
        if (tGen.getWeb().getNextWeb(0) == null) tGen.getWeb().setNextWeb(knownWebs.get(0), 0);
        if (tGen.getWeb().getNextWeb(1) == null && knownWebs.size() > 1) 
            tGen.getWeb().setNextWeb(knownWebs.get(1), 1);
    }
    
    private void simplifyArrowsFrom(SlTGenerator<R> bGen) {// here we check whether any of the foams can be simplified.
        int i = bGen.getBotArrows().size()-1;
        while (i >= 0) {
            if (abInf.isAborted()) return;
            SlTArrow<R> arr = (SlTArrow<R>) bGen.getBotArrows().get(i);
            simplifyArrow(arr);
            i--;
        }
    }
    
    protected void simplifyArrow(SlTArrow<R> arr) {
        if (abInf.isAborted()) return;
        arr.simplifyFoams(sType, unit);
        if (arr.noFoams()) {
            removeArrow(arr);
            return;
        }
        if (abInf.isAborted()) return;
        arr.checkCylinders(sType, unit); 
        arr.dotChecker(sType, unit);
        if (arr.noFoams()) {
            removeArrow(arr);
            return;
        }
        if (abInf.isAborted()) return;
        arr.checkCombineFoams();
        if (arr.noFoams()) removeArrow(arr);
    }
    
    protected void removeArrow(SlTArrow<R> arr) {
        arr.getTopGenerator().getTopArrows().remove(arr);
        arr.getBotGenerator().getBotArrows().remove(arr);
    }
    
    private void getComposedArrows(SlTGenerator<R> bGen, SlTGenerator<R> nbGen) {
        SlTArrow<R> fArr = (SlTArrow<R>) nbGen.getBotArrows().get(0);
        for (int i = 0; i < bGen.getBotArrows().size(); i++) {
            SlTArrow<R> arr = (SlTArrow<R>) bGen.getBotArrows().get(i);
            SlTGenerator<R> tGen = (SlTGenerator<R>) arr.getTopGenerator();
            for (int j = 0; j < tGen.getTopArrowSize(); j++) {
                SlTArrow<R> lArr = (SlTArrow<R>) tGen.getTopArrow(j);
                SlTGenerator<R> ntGen = (SlTGenerator<R>) lArr.getTopGenerator();
                SlTArrow<R> nArr = fArr.composeWith(arr).composeWith(lArr);
                nbGen.addBotArrow(nArr);
                ntGen.addTopArrow(nArr);
            }
        }
        nbGen.getBotArrows().remove(0); // remove the pointer, no longer needed
    }
    
    private SlTArrow<R> getDeloopedArrow(SlTGenerator<R> tGen, SlTGenerator<R> ntGen,
            SlTGenerator<R> nGen, Foam fm) {
        ArrayList<SlTArrow<R>> fArrs = new ArrayList<SlTArrow<R>>();
        ArrayList<SlTArrow<R>> bArrs = new ArrayList<SlTArrow<R>>();
        bArrs.add(getFirstArrow(ntGen, nGen, new Foam<R>(fm, unit).reverse()));
        fArrs.add(getFirstArrow(nGen, ntGen, new Foam<R>(fm, unit)));
        while (nGen != tGen) {
            SlTArrow<R> arr = (SlTArrow<R>) nGen.getBotArrows().get(0);
            bArrs.add(arr);
            SlTGenerator<R> nbGen = (SlTGenerator<R>) arr.getTopGenerator();
            fArrs.add(0, arrowFromTo(nbGen, nGen));
            nGen = nbGen;
        }
        SlTArrow<R> bArr = composeArrows(bArrs);
        SlTArrow<R> fArr = composeArrows(fArrs);
        ntGen.addBotArrow(bArr); // this is a pointer to the object from the tensor
        return fArr;
    }
    
    private SlTArrow<R> getFirstArrow(SlTGenerator<R> fGen, SlTGenerator<R> sGen, Foam<R> foam) {
        SlTArrow<R> arr = new SlTArrow<R>(fGen, sGen);
        arr.addFoam(foam);
        return arr;
    }
    
    private SlTArrow<R> composeArrows(ArrayList<SlTArrow<R>> arrows) {
        SlTArrow<R> arr = arrows.get(0);
        for (int i = 1; i < arrows.size(); i++) arr = arr.composeWith(arrows.get(i));
        return arr;
    }
    
    /*private Foam<R> composeFoams(ArrayList<Foam<R>> foams) {
        Foam<R> foam = foams.get(0);
        for (int i = 1; i < foams.size(); i++) 
            foam = foam.composeWith(foams.get(i));
        return foam;
    }// */
    
    private SlTArrow<R> arrowFromTo(SlTGenerator<R> bGen, SlTGenerator<R> tGen) {
        for (int i = 0; i < bGen.getTopArrowSize(); i++) {
            SlTArrow<R> arr = (SlTArrow<R>) bGen.getTopArrow(i);
            if (arr.getTopGenerator() == tGen) return arr;
        }
        System.out.println("What");
        return null;
    }
    
    private Foam getTheWeb(Web web, ArrayList<Web> knownWebs, SlTCache dCache) {
        for (Web kWeb : knownWebs) {
            Foam foam = kWeb.isomorphismTo(web);
            if (foam != null) return foam;
        }
        Foam foam =  dCache.getWebLike(web);
        knownWebs.add(foam.getCoWeb());
        return foam;
    }
    
    protected void grabGenerators(SlTGenerator<R> tGen, ArrayList<SlTGenerator<R>> dtGens) {
        if (tGen.getTopArrowSize() == 0) dtGens.add(tGen);
        else {
            for (int i = 0; i < tGen.getTopArrowSize(); i++) {
                SlTGenerator<R> nGen = (SlTGenerator<R>) tGen.getTopArrow(i).getTopGenerator();
                grabGenerators(nGen, dtGens);
            }
        }
    }
    
    private void simplifyGenerator(SlTGenerator<R> teGen) {
        Web web = teGen.getWeb();
        Edge circle = web.getCircle();
        if (circle == null) {
            ArrayList<Edge> digon = web.getDigon();
            if (digon == null) {
                ArrayList<Edge> square = web.getSquare();
                if (square != null) { // deal with square
                    removeSquare(teGen, square);
                }
            }
            else { //deal with digon
                removeDigon(teGen, digon);
            }
        }
        else { //deal with circle
            removeCircle(teGen, circle);
        }
        for (int i = 0; i < teGen.getTopArrowSize(); i++) {
            simplifyGenerator((SlTGenerator<R>) teGen.getTopArrow(i).getTopGenerator());
        }
    }
    
    private void removeCircle(SlTGenerator<R> teGen, Edge circle) {
        Web web = teGen.getWeb();
        Web nWeb = removeCircle(web, circle);
        SlTGenerator<R> npGen = new SlTGenerator<R>(teGen.hdeg(), teGen.qdeg()+2, nWeb);
        SlTGenerator<R> nsGen = new SlTGenerator<R>(teGen.hdeg(), teGen.qdeg(), nWeb);
        SlTGenerator<R> nmGen = new SlTGenerator<R>(teGen.hdeg(), teGen.qdeg()-2, nWeb);
        SlTArrow<R> pArr = new SlTArrow<R>(teGen, npGen);
        SlTArrow<R> sArr = new SlTArrow<R>(teGen, nsGen);
        SlTArrow<R> mArr = new SlTArrow<R>(teGen, nmGen);
        pArr.addFoam(removeCircleFoam(web, nWeb, circle, 0, unit.negate()));
        sArr.addFoam(removeCircleFoam(web, nWeb, circle, 1, unit.negate()));
        mArr.addFoam(removeCircleFoam(web, nWeb, circle, 2, unit.negate()));
        if (sType == 1) {
            sArr.addFoam(removeCircleFoam(web, nWeb, circle, 0, unit));
            mArr.addFoam(removeCircleFoam(web, nWeb, circle, 1, unit));
        }
        if (sType == 2) mArr.addFoam(removeCircleFoam(web, nWeb, circle, 0, unit));
        teGen.addTopArrow(pArr);
        teGen.addTopArrow(sArr);
        teGen.addTopArrow(mArr);
        pArr = new SlTArrow<R>(npGen, teGen);
        sArr = new SlTArrow<R>(nsGen, teGen);
        mArr = new SlTArrow<R>(nmGen, teGen);
        pArr.addFoam(createCircleFoam(nWeb, web, circle, 2));
        sArr.addFoam(createCircleFoam(nWeb, web, circle, 1));
        mArr.addFoam(createCircleFoam(nWeb, web, circle, 0));
        npGen.addBotArrow(pArr);
        nsGen.addBotArrow(sArr);
        nmGen.addBotArrow(mArr);
    }
    
    protected Foam<R> createCircleFoam(Web bWeb, Web tWeb, Edge circle, int dts) {
        Foam<R> nFoam = new Foam<R>(bWeb, tWeb, unit);
        for (int i = 0; i < bWeb.edgesSize(); i++) {
            nFoam.addFacets(new Facet(bWeb.getEdges().get(i), 0));
        }
        Facet disc = new Facet(dts, 1);
        disc.addEdges(new Edge[0], new Edge[] {circle});
        nFoam.addFacets(disc);
        addSingEdges(nFoam, bWeb);
        return nFoam;
    }
    
    protected Foam<R> removeCircleFoam(Web bWeb, Web tWeb, Edge circle, int dts, R fac) {
        Foam<R> nFoam = new Foam<R>(bWeb, tWeb, fac);
        for (int i = 0; i < tWeb.edgesSize(); i++) {
            nFoam.addFacets(new Facet(tWeb.getEdges().get(i), 0));
        }
        Facet disc = new Facet(dts, 1);
        disc.addEdges(new Edge[] {circle}, new Edge[0]);
        nFoam.addFacets(disc);
        addSingEdges(nFoam, tWeb);
        return nFoam;
    }
    
    protected Web removeCircle(Web web, Edge circle) {
        Web nWeb = new Web();
        for (Edge ed : web.getEdges()) if (ed != circle) nWeb.getEdges().add(ed);
        for (int i = 0; i < web.singNumber(); i++) {
            Vertex v = web.getSingVertex(i);
            nWeb.addSingVertex(v);
            Edge[] sinEd = web.getTriple(i);
            nWeb.addTriple(sinEd[0], sinEd[1], sinEd[2]);
        }
        return nWeb;
    }
    
    private void removeSquare(SlTGenerator<R> teGen, ArrayList<Edge> square) {
        Web web = teGen.getWeb();
        Web[] nWeb = removeSquare(web, square);
        SlTGenerator<R> noGen = new SlTGenerator<R>(teGen.hdeg(), teGen.qdeg(), nWeb[0]);
        SlTGenerator<R> ntGen = new SlTGenerator<R>(teGen.hdeg(), teGen.qdeg(), nWeb[1]);
        SlTArrow<R> pArr = new SlTArrow<R>(teGen, noGen);
        SlTArrow<R> nArr = new SlTArrow<R>(teGen, ntGen);
        pArr.addFoam(removeSquareFoam(web, nWeb[0], square, 0));
        nArr.addFoam(removeSquareFoam(web, nWeb[1], square, 1));
        teGen.addTopArrow(pArr);
        teGen.addTopArrow(nArr);
        pArr = new SlTArrow<R>(noGen, teGen);
        nArr = new SlTArrow<R>(ntGen, teGen);
        pArr.addFoam(createSquareFoam(nWeb[0], web, square, 0));
        nArr.addFoam(createSquareFoam(nWeb[1], web, square, 1));
        noGen.addBotArrow(pArr);
        ntGen.addBotArrow(nArr);
    }
    
    private Foam<R> createSquareFoam(Web bWeb, Web tWeb, ArrayList<Edge> square, int first) {
        Foam<R> nFoam = new Foam<R>(bWeb, tWeb, unit);
        int bws = bWeb.edgesSize();
        for (int i = 0; i < bws-2; i++) { //create a bunch of product facets
            nFoam.addFacets(new Facet(bWeb.getEdges().get(i), 0));
        }
        Edge fEdge = bWeb.getEdges().get(bws-2);
        Edge sEdge = bWeb.getEdges().get(bws-1);
        Facet fbFac = new Facet(0, fEdge.getEuler());
        Facet fdFac = new Facet(0, 1);
        Facet sqFac = new Facet(0, 1);
        Facet sbFac = new Facet(0, sEdge.getEuler());
        Facet sdFac = new Facet(0, 1);
        fbFac.addEdges(new Edge[] {fEdge}, new Edge[] {square.get(4+first), square.get(6)});
        fdFac.addEdges(new Edge[0], new Edge[] {square.get(0+first)});
        sqFac.addEdges(new Edge[0], new Edge[] {square.get(1-first), square.get(2+first)});
        sbFac.addEdges(new Edge[] {sEdge}, new Edge[] {square.get(7), square.get(5-first)});
        sdFac.addEdges(new Edge[0], new Edge[] {square.get(3-first)});
        nFoam.addFacets(fbFac);
        nFoam.addFacets(sbFac);
        nFoam.addFacets(fdFac);
        nFoam.addFacets(sdFac);
        nFoam.addFacets(sqFac);
        addSingEdges(nFoam, bWeb);
        Vertex vOne = square.get(first).getStVertex();
        Vertex vTwo = square.get(first).getEnVertex();
        nFoam.addSingEdge(new SingEdge(vOne, vTwo, 1, 1));
        int i = nFoam.facetSize();
        int k = 3;
        int l = 5;
        if (first == 1) {
            k = 4;
            l = 3;
        }
        nFoam.addSingFacets(new int[] {i-k, i-1, i-l});
        vOne = square.get(3-first).getStVertex();
        vTwo = square.get(3-first).getEnVertex();
        nFoam.addSingEdge(new SingEdge(vOne, vTwo, 1, 1));
        k = 2;
        l = 4;
        if (first == 1) {
            k = 5;
            l = 2;
        }
        nFoam.addSingFacets(new int[] {i-k, i-1, i-l});
        return nFoam;
    }
    
    private Foam<R> removeSquareFoam(Web bWeb, Web tWeb, ArrayList<Edge> square, int first) {
        Foam<R> nFoam = new Foam<R>(bWeb, tWeb, unit.negate());
        int tws = tWeb.edgesSize();
        for (int i = 0; i < tws-2; i++) { // create a bunch of product facets
            nFoam.addFacets(new Facet(tWeb.getEdges().get(i), 0));
        }
        Edge fEdge = tWeb.getEdges().get(tws-2);
        Edge sEdge = tWeb.getEdges().get(tws-1);
        Facet fbFac = new Facet(0, fEdge.getEuler());
        Facet fdFac = new Facet(0, 1);
        Facet sqFac = new Facet(0, 1);
        Facet sbFac = new Facet(0, sEdge.getEuler());
        Facet sdFac = new Facet(0, 1);
        fbFac.addEdges(new Edge[] {square.get(4+first), square.get(6)}, new Edge[] {fEdge});
        fdFac.addEdges(new Edge[] {square.get(0+first)}, new Edge[0]);
        sqFac.addEdges(new Edge[] {square.get(1-first), square.get(2+first)}, new Edge[0]);
        sbFac.addEdges(new Edge[] {square.get(7), square.get(5-first)}, new Edge[] {sEdge});
        sdFac.addEdges(new Edge[] {square.get(3-first)}, new Edge[0]);
        nFoam.addFacets(fbFac);
        nFoam.addFacets(sbFac);
        nFoam.addFacets(fdFac);
        nFoam.addFacets(sdFac);
        nFoam.addFacets(sqFac);
        addSingEdges(nFoam, tWeb);
        Vertex vOne = square.get(first).getStVertex();
        Vertex vTwo = square.get(first).getEnVertex();
        nFoam.addSingEdge(new SingEdge(vTwo, vOne, 0, 0));
        int i = nFoam.facetSize();
        int k = 3;
        int l = 5;
        if (first == 1) {
            k = 4;
            l = 3;
        }
        nFoam.addSingFacets(new int[] {i-k, i-1, i-l});
        vOne = square.get(3-first).getStVertex();
        vTwo = square.get(3-first).getEnVertex();
        nFoam.addSingEdge(new SingEdge(vTwo, vOne, 0, 0));
        k = 2;
        l = 4;
        if (first == 1) {
            k = 5;
            l = 2;
        }
        nFoam.addSingFacets(new int[] {i-k, i-1, i-l});
        return nFoam;
    }
    
    private Web[] removeSquare(Web web, ArrayList<Edge> square) {
        Web[] nWeb = new Web[2];
        ArrayList<Edge> newEdges = newEdgesFromSquare(web, square, 4, 5);
        nWeb[0] = new Web();
        nWeb[0].addEdges(newEdges);
        ArrayList<Integer> souSinks = new ArrayList<Integer>();
        souSinks.add(square.get(0).getStVertexName());
        souSinks.add(square.get(2).getStVertexName());
        souSinks.add(square.get(0).getEnVertexName());
        souSinks.add(square.get(1).getEnVertexName());
        dealWithSingVertices(web, nWeb[0], souSinks);
        newEdges = newEdgesFromSquare(web, square, 5, 4);
        nWeb[1] = new Web();
        nWeb[1].addEdges(newEdges);
        dealWithSingVertices(web, nWeb[1], souSinks);
        return nWeb;
    }
    
    private void dealWithSingVertices(Web web, Web nWeb, ArrayList<Integer> souSinks) {
        for (int i = 0; i < web.singNumber(); i++) {
            int svn = web.getSingVertex(i).getName();
            if (!souSinks.contains(svn)) {
                nWeb.addSingVertex(web.getSingVertex(i));
                Edge[] triple = web.getTriple(i);
                Edge[] pos = getPositions(triple, nWeb);
                nWeb.addTriple(pos[0], pos[1], pos[2]);
            }
        }
    }
    
    private Edge[] getPositions(Edge[] triple, Web nWeb) {
        Edge[] pos = new Edge[3];
        int l = nWeb.edgesSize();
        Edge fEd = nWeb.getEdges().get(l-2);
        Edge sEd = nWeb.getEdges().get(l-1);
        for (int i = 0; i < 3; i++) {
            if (nWeb.containsEdge(triple[i])) pos[i] = triple[i];
            else {
                ArrayList<Integer> fEds = fEd.getVertexNames();
                if (fEds.contains(triple[i].getEnVertexName())
                        && fEds.contains(triple[i].getStVertexName())) pos[i] = fEd;
                else pos[i] = sEd;
            }
        }
        return pos;
    }
    
    private ArrayList<Edge> newEdgesFromSquare(Web web, ArrayList<Edge> square, 
            int se, int fo) { // implicitly assuming that web has no digons
        ArrayList<Edge> nEdges = new ArrayList<Edge>();
        for (Edge edge : web.getEdges()) if (!square.contains(edge)) nEdges.add(edge);
        Vertex st = square.get(6).getStVertex();
        Vertex en = square.get(se).getEnVertex();
        nEdges.add(new Edge(st, en, square.get(6).getEnVertex(), square.get(se).getStVertex()));
        st = square.get(7).getStVertex();
        en = square.get(fo).getEnVertex();
        nEdges.add(new Edge(st, en, square.get(7).getEnVertex(), square.get(fo).getStVertex()));
        return nEdges;
    }
    
    private void removeDigon(SlTGenerator<R> teGen, ArrayList<Edge> digon) {
        Web web = teGen.getWeb();
        Web nWeb = removeDigon(web, digon);
        SlTGenerator<R> tpGen = new SlTGenerator<R>(teGen.hdeg(), teGen.qdeg()+1, nWeb);
        SlTGenerator<R> tnGen = new SlTGenerator<R>(teGen.hdeg(), teGen.qdeg()-1, nWeb);
        SlTArrow<R> pArr = new SlTArrow<R>(teGen, tpGen);
        SlTArrow<R> nArr = new SlTArrow<R>(teGen, tnGen);
        pArr.addFoam(removeDigonFoam(1, web, nWeb, digon));
        nArr.addFoam(removeDigonFoam(-1, web, nWeb, digon));
        teGen.addTopArrow(pArr);
        teGen.addTopArrow(nArr);
        pArr = new SlTArrow<R>(tpGen, teGen);
        nArr = new SlTArrow<R>(tnGen, teGen);
        pArr.addFoam(createDigonFoam(1, nWeb, web, digon));
        nArr.addFoam(createDigonFoam(0, nWeb, web, digon));
        tpGen.addBotArrow(pArr);
        tnGen.addBotArrow(nArr);
    }
    
    private Foam<R> createDigonFoam(int dot, Web bWeb, Web tWeb, ArrayList<Edge> digon) {
        Foam<R> theFoam = new Foam<R>(bWeb, tWeb, unit);
        for (int i = 0; i < bWeb.getEdges().size()-1; i++) { // create a bunch of product facets
            theFoam.addFacets(new Facet(bWeb.getEdges().get(i), 0));
        }
        Edge lEdge = bWeb.getEdges().get(bWeb.getEdges().size()-1);
        Facet fDigFac = new Facet(dot, 1);
        Facet sDigFac = new Facet(0, 1);
        Facet bFac = new Facet(0, lEdge.getEuler());
        fDigFac.addEdges(new Edge[0], new Edge[] {digon.get(0)});
        sDigFac.addEdges(new Edge[0], new Edge[] {digon.get(1)});
        Edge[] digs = new Edge[digon.size()-2];
        for (int i = 2; i < digon.size(); i++) digs[i-2] = digon.get(i);
        bFac.addEdges(new Edge[] {lEdge}, digs);
        theFoam.addFacets(bFac);
        theFoam.addFacets(fDigFac);
        theFoam.addFacets(sDigFac);
        addSingEdges(theFoam, bWeb);
        Vertex sou = new Vertex(digon.get(0).getStVertexName(), 3);
        Vertex sin = new Vertex(digon.get(0).getEnVertexName(), 4);
        SingEdge sEdge = new SingEdge(sou, sin, 1, 1);
        theFoam.addSingEdge(sEdge);
        int i = theFoam.facetSize();
        theFoam.addSingFacets(new int[] {i-3, i-2, i-1});
        return theFoam;
    }
    
    private Foam<R> removeDigonFoam(int fac, Web bWeb, Web tWeb, ArrayList<Edge> digon) {
        R value = unit;
        if (fac == -1) value = unit.negate();
        Foam<R> theFoam = new Foam<R>(bWeb, tWeb, value);
        for (int i = 0; i < tWeb.getEdges().size()-1; i++) { // create a bunch of product facets
            theFoam.addFacets(new Facet(tWeb.getEdges().get(i), 0));
        }
        Edge lEdge = tWeb.getEdges().get(tWeb.getEdges().size()-1);
        Facet fDigFac = new Facet(0, 1);
        Facet sDigFac = new Facet((1-fac)/2, 1);// gets a dot if fac = -1
        Facet bFac = new Facet(0, lEdge.getEuler());
        fDigFac.addEdges(new Edge[] {digon.get(0)}, new Edge[0]);
        sDigFac.addEdges(new Edge[] {digon.get(1)}, new Edge[0]);
        Edge[] digs = new Edge[digon.size()-2];
        for (int i = 2; i < digon.size(); i++) digs[i-2] = digon.get(i);
        bFac.addEdges(digs, new Edge[] {lEdge});
        theFoam.addFacets(bFac);
        theFoam.addFacets(fDigFac);
        theFoam.addFacets(sDigFac);
        addSingEdges(theFoam, tWeb);
        Vertex sou = new Vertex(digon.get(0).getStVertexName(), 3);
        Vertex sin = new Vertex(digon.get(0).getEnVertexName(), 4);
        SingEdge sEdge = new SingEdge(sin, sou, 0, 0);
        theFoam.addSingEdge(sEdge);
        int i = theFoam.facetSize();
        theFoam.addSingFacets(new int[] {i-3, i-2, i-1});
        return theFoam;
    }
    
    private void addSingEdges(Foam<R> theFoam, Web web) {
        for (int i = 0; i < web.singNumber(); i++) {
            Vertex v = web.getSingVertex(i);
            int s = 0;
            int e = 1;
            if (v.getType() == 3) {
                s = 1;
                e = 0;
            }
            theFoam.addSingEdge(new SingEdge(v, v, s, e));
            int[] pos = web.getSingPositions(v);
            theFoam.addSingFacets(pos);
        }
    }
    
    private Web removeDigon(Web web, ArrayList<Edge> digon) {
        int source = digon.get(0).getStVertexName();
        int sink = digon.get(0).getEnVertexName();
        ArrayList<Edge> newEdges = newEdgesRemovedDigon(web, digon, source, sink);
        Web nWeb = new Web();
        nWeb.addEdges(newEdges);
        for (int i = 0; i < web.singNumber(); i++) {
            Vertex v = web.getSingVertex(i);
            if (v.getName() != source && v.getName() != sink) {
                nWeb.addSingVertex(v);
                Edge[] triple = web.getTriple(i);
                Edge[] nTriple = newTripleFrom(triple, nWeb);
                nWeb.addTriple(nTriple[0], nTriple[1], nTriple[2]);
            }
        }
        return nWeb;
    }
    
    private Edge[] newTripleFrom(Edge[] triple, Web nWeb) {
        Edge[] nTriple = new Edge[3];
        Edge lastOne = nWeb.getEdges().get(nWeb.getEdges().size()-1);
        for (int i = 0; i < 3; i++) {
            if (nWeb.getEdges().contains(triple[i])) nTriple[i] = triple[i];
            else nTriple[i] = lastOne;
        }
        return nTriple;
    }
    
    private ArrayList<Edge> newEdgesRemovedDigon(Web web, ArrayList<Edge> digon,
            int source, int sink) {
        ArrayList<Edge> newEdges = new ArrayList<Edge>();
        for (Edge edge : web.getEdges()) if (!digon.contains(edge)) newEdges.add(edge);
        int a = digon.get(2).getStVertexName();
        int b = digon.get(2).getEnVertexName();
        if (a == source && b == sink) { // need a circle
            Vertex va = new Vertex(a, 2);
            Vertex vb = new Vertex(b, 2);
            Edge ed = new Edge(va, va);
            ed.addMiddle(vb);
            newEdges.add(ed);
        }
        else {
            int c = digon.get(3).getStVertexName();
            int d = digon.get(3).getEnVertexName();
            int ta = 0;
            int tb = 1;
            if (a == source) {
                a = c;
                if (c < 0) ta = 3;
                if (b < 0) tb = 4;
            }
            else {
                if (a < 0) ta = 3;
                b = d;
                if (b < 0) tb = 4;
            }
            Vertex va = new Vertex(a, ta);
            Vertex vb = new Vertex(b, tb);
            Edge ne = new Edge(va, vb);
            ne.addMiddle(new Vertex(source, 2));
            ne.addMiddle(new Vertex(sink, 2));
            newEdges.add(ne);
        }
        return newEdges;
    }
    
    protected void createTensor(int i, SlThreeComplex<R> complex, SlTCache tCache,
            ArrayList<ArrayList<Generator<R>>> tgens) {
        for (int l = 0; l < generators.get(i).size(); l++) {
            if (abInf.isAborted()) return;
            SlTGenerator<R> fGen = (SlTGenerator<R>) generators.get(i).get(l);
            ArrayList<SlTGenerator<R>> nGens = new ArrayList<SlTGenerator<R>>(2);
            int t = complex.generatorSize()-1;
            for (int k = t; k >= 0; k--) {
                for (int v = 0; v < complex.generators.get(k).size(); v++) {
                    SlTGenerator<R> sGen = (SlTGenerator<R>) complex.generators.get(k).get(v);
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
    }
    
    private void addArrow(SlTArrow<R> arr, SlTGenerator<R> nGen, SlTGenerator<R> tGen, 
            Web oldWeb, int f, SlTCache tCache) {
        SlTArrow<R> nArr = new SlTArrow<R>(nGen, tGen);
        nGen.addBotArrow(nArr);
        for (Foam<R> foam : arr.getFoams()) {
            R val = foam.getValue();
            if (f < 0) val = val.negate();
            Foam<R> nFoam = new Foam<R>(nGen.getWeb(), tGen.getWeb(), val);
            nFoam.addHorizontalFacets(foam, oldWeb, tCache);
            nArr.addFoam(nFoam);
        }
    }

    public void throwQAway(int q) {
        for (ArrayList<Generator<R>> gns : generators) {
            int i = gns.size()-1;
            while (i >= 0) {
                SlTGenerator<R> gen = (SlTGenerator<R>) gns.get(i);
                if (gen.qdeg() != q) gns.remove(i);
                i--;
            }
        }
    }
    
    public SlThreeComplex<R> reducify() { // this reducify only to be done if sType == 0
        SlThreeComplex<R> redComplex = new SlThreeComplex<R>(unit, frame, abInf, 0);
        for (int j = generators.size()-1; j>=0; j--) {
            redComplex.generators.add(0, new ArrayList<Generator<R>>());
            for (int i = 0; i < generators.get(j).size(); i++) {
                SlTGenerator<R> oGen = (SlTGenerator<R>) generators.get(j).get(i);
                SlTGenerator<R> nGen = new SlTGenerator<R>(oGen.hdeg(), oGen.qdeg(), oGen.getWeb());
                redComplex.generators.get(0).add(nGen);
                for (int k = 0; k < oGen.getBotArrows().size(); k++) {
                    SlTArrow<R> arr = (SlTArrow<R>) oGen.getBotArrows().get(k);
                    SlTGenerator<R> otGen = (SlTGenerator<R>) arr.getTopGenerator();
                    if (oGen.qdeg() == otGen.qdeg()) {
                        int ind = generators.get(j+1).indexOf(otGen);
                        SlTGenerator<R> ntGen = (SlTGenerator<R>) redComplex.generators.get(1).get(ind);
                        SlTArrow<R> nArr = new SlTArrow<R>(nGen, ntGen);
                        R val = arr.getValue();
                        nArr.addFoam(new Foam<R>(nGen.getWeb(), ntGen.getWeb(), val));
                        nGen.addBotArrow(nArr);
                        ntGen.addTopArrow(nArr);
                    }
                }
            }
        }
        redComplex.cache = cache;
        return redComplex;
    }

    public void finishOff(String gInfo, boolean hd) {
        ArrayList<Integer> lepts = cache.lastEndpoints();
        SlThreeComplex<R> finComplex = new SlThreeComplex<R>(unit, frame, abInf, 
                sType, lepts.get(1), lepts.get(0));
        this.modifyComplex(finComplex, gInfo, hd);
    }
    
}
