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
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class UnifiedKhovHomology<R extends Ring<R>> {
    
    private final Link theLink;
    private final long coeff;
    private final R unit;
    private final R xi;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final Options options;
    private final int[] girth;
    private final boolean highDetail;
    private final ArrayList<String> endHom;
    private final ArrayList<ArrayList<Homomorphism<R>>> homs;
    private UnifiedComplex<R> theComplex;
    private ArrayList<Homomorphism<R>> phioe;
    private ArrayList<Homomorphism<R>> phieo;
    private ArrayList<Homomorphism<R>> beta;
    private final String name;    
    
    public UnifiedKhovHomology(LinkData link, long cff, R unt, R x, DialogWrap frm, Options optns) {
        theLink = Reidemeister.freeOfOne(link.chosenLink()).breakUp().girthDiscMinimize();
        girth = theLink.totalGirthArray();
        coeff = cff;
        unit = unt;
        xi = x;
        frame = frm;
        abInf = frame.getAbortInfo();
        options = optns;
        endHom = new ArrayList<String>();
        highDetail = options.getGirthInfo() == 2;
        homs = new ArrayList<ArrayList<Homomorphism<R>>>();
        name = link.name;
    }
    
    public ArrayList<String> getUnifiedHomology() {
        return endHom;
    }
    
    public void calculate() {
        int[] wrt = theLink.crossingSigns();
        int hstart = -wrt[1];
        int qstart = wrt[0]+2*hstart;
        if (coeff == 0) calculateIntegral(hstart, qstart);
        if (coeff == 1) calculateIntegralConnectingHom(hstart, qstart);
        if (coeff == 2) calculateModularConnectingHom(hstart, qstart);
    }
    
    private void calculateIntegral(int hstart, int qstart) {
        theComplex = getComplex(hstart, qstart);
        UnifiedAction<R> action = new UnifiedAction<R>(theComplex, unit, xi, frame, abInf);
        action.setupComplex(true);
        action.smithNormalize(true);
        action.outputComplex();
    }
    
    private void calculateIntegralConnectingHom(int hstart, int qstart) {
        theComplex = getComplex(hstart, qstart);
        UnifiedSES<R> ses = new UnifiedSES<R>(theComplex, unit, xi, frame, abInf);
        phieo = ses.getConnectingHoms(true, false);
        phioe = ses.getConnectingHoms(false, true);
        //for (Homomorphism<R> hom : phieo) System.out.println(hom);
        //for (Homomorphism<R> hom : phioe) System.out.println(hom);
        homs.add(phieo);
        homs.add(phioe);
        ArrayList<Homomorphism<R>> thete = phieo;
        ArrayList<Homomorphism<R>> theto = phioe;
        boolean even = false;
        while (!thete.isEmpty() || !theto.isEmpty()) {
            if (even) {
                thete = compose(phieo, thete);
                theto = compose(phioe, theto);
            }
            else {
                thete = compose(phioe, thete);
                theto = compose(phieo, theto);
            }
            even = !even;
            homs.add(thete);
            homs.add(theto);
        }
        System.out.println(name);
        for (int i = 0; i < homs.size(); i++) {
            ArrayList<Homomorphism<R>> rho = homs.get(i);
            System.out.println("Homomorphism "+i);
            //System.out.println(shortLabel(rho));
            for (Homomorphism<R> hom : rho) System.out.println(hom);
        }
        //for (Homomorphism<R> hom : phieo) System.out.println(hom.rank());
    }
    
    private void calculateModularConnectingHom(int hstart, int qstart) {
        theComplex = getComplex(hstart, qstart);
        UnifiedSES<R> ses = new UnifiedSES<R>(theComplex, unit, xi, frame, abInf);
        beta = ses.getConnectingHoms(true, true);
        homs.add(beta);
        
        //for (Homomorphism<R> hom : beta) System.out.println(hom);
        ArrayList<Homomorphism<R>> comp = compose(beta, beta);
        while (!comp.isEmpty()) {
            homs.add(comp);
            comp = compose(comp, beta);
        }
        for (int i = 0; i < homs.size(); i++) {
            ArrayList<Homomorphism<R>> rho = homs.get(i);
            System.out.println("Homomorphism "+i);
            //System.out.println(shortLabel(rho));
        }
        //System.out.println("Composition");
        //for (Homomorphism<R> hom : comp) {
        //    System.out.println(hom);
        //}// */
    }
    
    private UnifiedComplex<R> getComplex(int hstart, int qstart) {
        if (theLink.crossingLength() == 0) return new UnifiedComplex<R>(0, unit, abInf, null, false);
        theComplex = firstComplex(hstart, qstart);
        theComplex.setClosure(theLink);
        int u = 1;
        while (u < theLink.crossingLength() && !abInf.isAborted()) {
            boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
                theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
            UnifiedComplex<R> nextComplex = new UnifiedComplex<R>(theLink.getCross(u), theLink.getPath(u), 0,
                    0, orient, unit, xi, null, null, false);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            theComplex.modifyComplex(nextComplex,girthInfo(u), highDetail);
            u++;
        }
        return theComplex;
    }
    
    private String girthInfo(int u) {
        String info = String.valueOf(girth[u]);
        if (!highDetail) return info;
        if (u < girth.length - 1) info = info+" ("+girth[u+1];
        else return info;
        for (int i = 1; i < 3; i++) {
            if (u < girth.length - i - 1) info = info+", "+girth[u+1+i];
        }
        info = info+")";
        return info;
    }

    private UnifiedComplex<R> firstComplex(int hstart, int qstart) {
        UnifiedComplex<R> complex = new UnifiedComplex<R>(theLink.getCross(0), theLink.getPath(0), hstart,
                qstart, false, unit, xi, frame, abInf, false);
        return complex;
    }
    
    /*private ArrayList<Homomorphism<R>> getShortExactSequence(boolean first, boolean second) { // this is the
        cComplex = new UnifiedChainComplex<R>(unit, frame, abInf);  // SES odd to uni to even
        dComplex = new UnifiedChainComplex<R>(unit, frame, abInf);  // or other way
        eComplex = new UnifiedChainComplex<R>(unit, frame, abInf);
        fillComplexes(true, true, true, first);
        eComplex.smithNormalize();
        ArrayList<UnifiedGenerator<R>> cocycles = eComplex.getCocycles();
        ArrayList<UnifiedCochain<R>> cochains = new ArrayList<UnifiedCochain<R>>();
        ArrayList<UnifiedCochain<R>> bounds = new ArrayList<UnifiedCochain<R>>();
        for (UnifiedGenerator<R> cocycle : cocycles) 
            cochains.add(new UnifiedCochain<R>(cocycle, unit));
        for (UnifiedCochain<R> cochain : cochains) 
            bounds.add(cochain.boundary());//System.out.println("The cochain ");cochain.output(dComplex);}
        for (int i = 0; i < cocycles.size();i ++) {
            UnifiedGenerator<R> cocyc = cocycles.get(i);
            UnifiedCochain<R> bound = bounds.get(i);
            bound.addArrowsFrom(cocyc);//System.out.println("The bound ");bound.output(dComplex);
        }
        cComplex.smithNormalize();
        //eComplex.output(cComplex);
        //cComplex.output();
        return generateHomomorphisms(cocycles, first, second);
        // */
        //for (Homomorphism<R> hom : phioe) System.out.println(hom);
        
        /*UnifiedChainComplex<R> uniComplex = new UnifiedChainComplex<R>(unit, frame, abInf);
        UnifiedChainComplex<R> eveComplex = new UnifiedChainComplex<R>(unit, frame, abInf);
        UnifiedChainComplex<R> oddComplex = new UnifiedChainComplex<R>(unit, frame, abInf);
        R extra = unit.add(xi);
        if (evenToOdd) extra = unit.add(xi.negate());
        for (int j = 0; j < tot; j++) {
            ArrayList<Generator<R>> gens = theComplex.getGenerators(j);
            for (int i = 0; i < gens.size(); i++) {
                OddGenerator<R> aGen = (OddGenerator<R>) gens.get(i);
                UnifiedGenerator<R> uGen = new UnifiedGenerator<R>(aGen.hdeg(), aGen.qdeg());
                UnifiedGenerator<R> eGen = new UnifiedGenerator<R>(aGen.hdeg(), aGen.qdeg());
                UnifiedGenerator<R> oGen = new UnifiedGenerator<R>(aGen.hdeg(), aGen.qdeg());
                uniComplex.addGenerator(uGen, j);
                eveComplex.addGenerator(eGen, j);
                oddComplex.addGenerator(oGen, j);
                if (evenToOdd) {
                    Arrow<R> iAr = new Arrow<R>(oGen, uGen, extra);
                    Arrow<R> jAr = new Arrow<R>(uGen, eGen, unit);
                    oGen.addOutArrow(iAr);
                    uGen.addInArrow(iAr);
                    uGen.addOutArrow(jAr);
                    eGen.addInArrow(jAr);
                }
                else {
                    Arrow<R> iAr = new Arrow<R>(eGen, uGen, extra);
                    Arrow<R> jAr = new Arrow<R>(uGen, oGen, unit);
                    eGen.addOutArrow(iAr);
                    uGen.addInArrow(iAr);
                    uGen.addOutArrow(jAr);
                    oGen.addInArrow(jAr);
                }
                for (int k = 0; k < aGen.getTopArrowSize(); k++) {
                    OddArrow<R> arr = aGen.getTopArrow(k);
                    ArrayList<Generator<R>> lowGens = theComplex.getGenerators(j-1);
                    int p = lowGens.indexOf(arr.getBotGenerator());
                    uniComplex.addArrow(uGen, j-1, p, arr.getValue());
                    eveComplex.addArrow(eGen, j-1, p, arr.getValue().abs(1));
                    oddComplex.addArrow(oGen, j-1, p, arr.getValue().abs(2));
                }
            }
        }
        UnifiedChainComplex<R> eComplex = oddComplex;
        UnifiedChainComplex<R> cComplex = eveComplex;
        if (evenToOdd) {
            eComplex = eveComplex;
            cComplex = oddComplex;
        }// */
        
        
            
            //if (!bound.checkDivision(extra)) System.out.println("Problem");
            
        //eComplex.output(cComplex);
        //System.out.println("The next");
        //cComplex.output();
        //uniComplex.output(eveComplex);
        //System.out.println("Smith-Normalize");
        
        //eComplex.output(cComplex);
        //cComplex.output();
    //}
    
    /*private ArrayList<Homomorphism<R>> generateHomomorphisms(ArrayList<UnifiedGenerator<R>> cocycles,
            boolean first, boolean second) {
        ArrayList<Homomorphism<R>> homs = new ArrayList<Homomorphism<R>>();
        for (UnifiedGenerator<R> gen : cocycles) {
            if (!gen.getOutArrows().isEmpty()) {
                int h = getHom(homs, gen.hdeg(), gen.qdeg());
                Homomorphism<R> hom;
                if (h >= 0) hom = homs.get(h);
                else {
                    hom = new Homomorphism<R>(gen.hdeg(), gen.qdeg(), gen.hdeg()+1, 
                        gen.qdeg(), first, second, unit);
                    homs.add(hom);
                }
                hom.addGenerator(gen, eComplex, cComplex);
            }
        }
        int i = homs.size()-1;
        while (i >= 0) {
            if (!homs.get(i).nontrivial()) homs.remove(i);
            i--;
        }
        Collections.sort(homs);
        return homs;
    }
    
    private int getHom(ArrayList<Homomorphism<R>> homs, int h, int q) {
        boolean found = false;
        int i = 0;
        while (!found && i < homs.size()) {
            if (homs.get(i).domainDegree(h, q)) found = true;
            else i++;
        }
        if (found) return i;
        return -1;
    }// */
    
    /*private void fillComplexes(boolean fillC, boolean fillD, boolean fillE, boolean eToO) {
        R extra = unit.add(xi);
        if (eToO) extra = unit.add(xi.negate());
        int tot = theComplex.generatorSize();
        for (int j = 0; j < tot; j++) {
            ArrayList<Generator<R>> gens = theComplex.getGenerators(j);
            for (int i = 0; i < gens.size(); i++) {
                OddGenerator<R> aGen = (OddGenerator<R>) gens.get(i);
                UnifiedGenerator<R> cGen = new UnifiedGenerator<R>(aGen.hdeg(), aGen.qdeg());
                UnifiedGenerator<R> dGen = new UnifiedGenerator<R>(aGen.hdeg(), aGen.qdeg());
                UnifiedGenerator<R> eGen = new UnifiedGenerator<R>(aGen.hdeg(), aGen.qdeg());
                if (fillC) cComplex.addGenerator(cGen, j);
                if (fillD) dComplex.addGenerator(dGen, j);
                if (fillE) eComplex.addGenerator(eGen, j);
                Arrow<R> iAr = new Arrow<R>(cGen, dGen, extra);
                Arrow<R> jAr = new Arrow<R>(dGen, eGen, unit);
                if (fillC && fillD) {
                    cGen.addOutArrow(iAr);
                    dGen.addInArrow(iAr);
                }
                if (fillD && fillE) {
                    dGen.addOutArrow(jAr);
                    eGen.addInArrow(jAr);
                }
                for (int k = 0; k < aGen.getTopArrowSize(); k++) {
                    OddArrow<R> arr = aGen.getTopArrow(k);
                    ArrayList<Generator<R>> lowGens = theComplex.getGenerators(j-1);
                    int p = lowGens.indexOf(arr.getBotGenerator());
                    int t = 0;
                    if (eToO) t = 1;
                    cComplex.addArrow(cGen, j-1, p, arr.getValue().abs(1+t));
                    dComplex.addArrow(dGen, j-1, p, arr.getValue());
                    eComplex.addArrow(eGen, j-1, p, arr.getValue().abs(2-t));
                }
            }
        }
    }// */
    
    private ArrayList<Homomorphism<R>> compose(ArrayList<Homomorphism<R>> second, 
            ArrayList<Homomorphism<R>> first) {
        ArrayList<Homomorphism<R>> comp = new ArrayList<Homomorphism<R>>();
        for (Homomorphism<R> sec : second) {
            for (Homomorphism<R> fir : first) {
                comp.add(sec.compose(fir));
            }
        }// */
        int i = comp.size()-1;
        while (i >= 0) {
            if (comp.get(i) == null) comp.remove(i);
            i--;
        }// */
        return comp;
    }
    
    private String shortLabel(ArrayList<Homomorphism<R>> homs) {
        String label = "";
        for (Homomorphism<R> rho : homs) {
            label = label+rho.shortInfo();
        }
        return label;
    }
    
}
