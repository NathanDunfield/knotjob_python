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
import java.util.Collections;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.homology.oddkhov.OddArrow;
import knotjob.homology.oddkhov.OddComplex;
import knotjob.homology.oddkhov.OddGenerator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class UnifiedSES<R extends Ring<R>> {
    
    private final OddComplex<R> theComplex;
    private final R unit;
    private final R xi;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private UnifiedChainComplex<R> cComplex;
    private UnifiedChainComplex<R> dComplex;
    private UnifiedChainComplex<R> eComplex;
    
    public UnifiedSES(OddComplex<R> complex, R unt, R x, DialogWrap frm, AbortInfo ab) {
        theComplex = complex;
        unit = unt;
        xi = x;
        frame = frm;
        abInf = ab;
    }
    
    public ArrayList<Homomorphism<R>> getConnectingHoms(boolean first, boolean second) {
        setupComplexes(first);
        frame.setLabelLeft(" ", 0, false);
        frame.setLabelRight(" ", 0, false);
        frame.setLabelLeft("Cocycle : ", 1, false);
        frame.setLabelLeft(" ", 2, false);
        frame.setLabelRight(" ", 2, false);
        ArrayList<UnifiedGenerator<R>> cocycles = eComplex.getCocycles();
        snakeThrough(cocycles);
        return generateHomomorphisms(cocycles, first, second);
    }
    
    public Homomorphism<R> getConnectingHom(boolean first, boolean second, int hdeg, int qdeg) {
        setupComplexes(first);
        ArrayList<UnifiedGenerator<R>> cocycles = eComplex.getCocycles(hdeg, qdeg);
        snakeThrough(cocycles);
        ArrayList<Homomorphism<R>> homs = generateHomomorphisms(cocycles, first, second);
        if (homs.isEmpty()) return null;
        return homs.get(0);
    }
    
    public UnifiedChainComplex<R> getCComplex() {
        return cComplex;
    }
    
    private void snakeThrough(ArrayList<UnifiedGenerator<R>> cocycles) {
        ArrayList<UnifiedCochain<R>> cochains = new ArrayList<UnifiedCochain<R>>();
        ArrayList<UnifiedCochain<R>> bounds = new ArrayList<UnifiedCochain<R>>();
        int r = cocycles.size();
        for (UnifiedGenerator<R> cocycle : cocycles) {
            frame.setLabelRight(""+r, 1, false);
            r--;
            cochains.add(new UnifiedCochain<R>(cocycle, unit));
        }
            
        for (UnifiedCochain<R> cochain : cochains) 
            bounds.add(cochain.boundary());
        for (int i = 0; i < cocycles.size();i ++) {
            UnifiedGenerator<R> cocyc = cocycles.get(i);
            UnifiedCochain<R> bound = bounds.get(i);
            bound.addArrowsFrom(cocyc);
        }
        cComplex.smithNormalize(true, 2);
    }
    
    private void setupComplexes(boolean first) {
        cComplex = new UnifiedChainComplex<R>(unit, frame, abInf, false);  // SES odd to uni to even
        dComplex = new UnifiedChainComplex<R>(unit, frame, abInf, false);  // or other way
        eComplex = new UnifiedChainComplex<R>(unit, frame, abInf, false);
        fillComplexes(true, true, true, first);
        eComplex.smithNormalize(true, 1);
    }
    
    private void fillComplexes(boolean fillC, boolean fillD, boolean fillE, boolean eToO) {
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
    }
    
    private ArrayList<Homomorphism<R>> generateHomomorphisms(ArrayList<UnifiedGenerator<R>> cocycles,
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
    }

    public void outputComplexes() {
        System.out.println("C");
        cComplex.output(dComplex);
        System.out.println("D");
        dComplex.output(eComplex);
        System.out.println("E");
        eComplex.output();
        
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
