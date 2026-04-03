/*

Copyright (C) 2021-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.evenkhov.sinv;

import java.util.ArrayList;
import java.util.Iterator;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.homology.evenkhov.Cobordism;
import knotjob.homology.evenkhov.EvenArrow;
import knotjob.homology.evenkhov.EvenComplex;
import knotjob.homology.evenkhov.EvenGenerator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class GradedComplex<R extends Ring<R>> extends EvenComplex<R> {
    
    public GradedComplex(int comp, R unt, boolean unr, boolean red, AbortInfo ab, 
            DialogWrap frm) {
        super(comp, unt, unr, red, ab, frm);
    }
    
    public GradedComplex(int crs, int[] ends, int hstart, int qstart, boolean rev, 
            boolean ras, boolean unred, boolean red, R unt, DialogWrap frm, AbortInfo abt) {
        super(crs, ends, hstart, qstart, rev, ras, unred, red, unt, frm, abt);
    }
    
    public GradedComplex(R unt, AbortInfo ab, DialogWrap frm) {
        super(unt, true, ab, frm);
    }

    public GradedComplex(ArrayList<ArrayList<Generator<R>>> grGens, R unt, 
            DialogWrap frame, AbortInfo abInf) {
        super(grGens, unt, frame, abInf);
    }
    
    public void smithNormalizeZero() {
        int hlevel = getZeroLevel();
        int level = hlevel+1;
        while (level >= hlevel) {
            if (abInf.isAborted()) return;
            EvenArrow<R> arrow = findSmallest(level);
            if (arrow == null) {
                level--;
            }
            else {//System.out.println(arrow.getBotGenerator().qdeg()+" "+generators.get(hlevel).size());
                if (isolateBottom(arrow)) {
                    if (isolateTop(arrow)) {
                        if (arrow.getValue().isInvertible()) cancelArrow(arrow, level);
                        frame.setLabelRight(""+generators.get(hlevel).size(), 2, false);
                    }
                }
            }
        }
    }

    private void  cancelArrow(EvenArrow<R> arrow, int level) {
        EvenGenerator<R> tGen = arrow.getTopGenerator();
        EvenGenerator<R> bGen = arrow.getBotGenerator();
        bGen.getBotArrows().remove(arrow);
        tGen.getTopArrows().remove(arrow);
        for (Arrow<R> mr : tGen.getBotArrows()) mr.getTopGenerator().getTopArrows().remove(mr);
        for (Arrow<R> mr : bGen.getTopArrows()) mr.getBotGenerator().getBotArrows().remove(mr);
        R u = arrow.getValue();
        generators.get(level-1).remove(bGen);
        generators.get(level).remove(tGen);
        for (Iterator<Arrow<R>> it = tGen.getTopArrows().iterator(); it.hasNext();) {
            EvenArrow<R> far = (EvenArrow<R>) it.next();
            for (Iterator<Arrow<R>> itt = bGen.getBotArrows().iterator(); itt.hasNext();) {
                EvenArrow<R> sar = (EvenArrow<R>) itt.next();
                EvenGenerator<R> aGen = far.getBotGenerator();
                EvenGenerator<R> zGen = sar.getTopGenerator();
                EvenArrow<R> azr = getArrowBetween(aGen, zGen);
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
    
    private EvenArrow<R> getArrowBetween(EvenGenerator<R> bGen, EvenGenerator<R> tGen) {
        boolean found = false;
        int i = 0;
        while (!found && i < bGen.bMorSize()) {
            EvenArrow<R> ar = bGen.getBotArrow(i);
            if (ar.getTopGenerator() == tGen) found = true;
            else i++;
        }
        if (found) return bGen.getBotArrow(i);
        EvenArrow<R> arr = new EvenArrow<R>(bGen, tGen);
        arr.setValue(unit.getZero());
        bGen.addBotArrow(arr);
        tGen.addTopArrow(arr);
        return arr;
    }
    
    private EvenArrow<R> findSmallest(int i, int q) {
        if (i >= generators.size()) return null;
        int j = 0;
        EvenArrow<R> smallest = null;
        while (j < generators.get(i).size()) {
            EvenGenerator<R> gen = (EvenGenerator<R>) generators.get(i).get(j);
            if (gen.qdeg() >= q) {
                int k = 0;
                while (k < gen.tMorSize()) {
                    EvenArrow<R> arr = gen.getTopArrow(k);
                    //if (q == 0) System.out.println(arr.getValue()+" "+arr.getBotGenerator().qdeg());
                    if (arr.getBotGenerator().qdeg() >= q) {
                        //if (q == 0) System.out.println("We're here "+notIsolated(arr, q, false));
                        if (arr.getValue().isInvertible()) return arr;
                        if (notIsolated(arr, q, false)) {
                            if (smallest == null || smallest.getValue().isBigger(arr.getValue())) 
                                smallest = arr;
                        }
                    }
                    k++;
                }
            }
            j++;
        }
        return smallest;
    }
    
    private EvenArrow<R> findSmallest(int i) {
        int j = 0;//System.out.println("Level "+i);
        if (i >= generators.size()) return null;
        EvenArrow<R> smallest = null;
        while (j < generators.get(i).size()) {
            EvenGenerator<R> gen = (EvenGenerator<R>) generators.get(i).get(j);
            int k = 0;
            while (k < gen.tMorSize()) {
                EvenArrow<R> arr = gen.getTopArrow(k);
                int qDiff = arr.getTopGenerator().qdeg() - arr.getBotGenerator().qdeg();
                if (qDiff == 0) {//System.out.println("Level "+i+" and "+notIsolated(arr)+" "+arr.getValue());
                    if (arr.getValue().isInvertible()) return arr;
                    if (notIsolated(arr, arr.getTopGenerator().qdeg(), true)) {
                        if (smallest == null || smallest.getValue().isBigger(arr.getValue())) 
                            smallest = arr;
                    }
                }
                k++;
            }
            j++;
        }
        return smallest;
    }
    
    private boolean notIsolated(EvenArrow<R> arr, int qmin, boolean boundedAbove) {
        EvenGenerator<R> gen = arr.getTopGenerator();
        boolean keepGoing = true;
        int k = 0;
        while (keepGoing && k < gen.tMorSize()) {
            EvenArrow<R> nArr = gen.getTopArrow(k);
            if (nArr != arr && withinRange(nArr.getBotGenerator().qdeg(), qmin, boundedAbove)) 
                keepGoing = false;
            else k++;
        }
        if (!keepGoing) return true;
        gen = arr.getBotGenerator();
        k = 0;
        while (keepGoing && k < gen.bMorSize()) {
            EvenArrow<R> nArr = gen.getBotArrow(k);
            if (nArr != arr && withinRange(nArr.getTopGenerator().qdeg(), qmin, boundedAbove)) 
                keepGoing = false;
            else k++;
        }
        return !keepGoing;
    }
    
    private boolean withinRange(int q, int qmin, boolean boundedAbove) {
        if (boundedAbove) return (q == qmin);
        return (q >= qmin);
    }
    
    /*private boolean notIsolated(EvenArrow<R> arr) {
        EvenGenerator<R> gen = arr.getTopGenerator();
        int q = gen.qdeg();
        boolean keepGoing = true;
        int k = 0;
        while (keepGoing && k < gen.tMorSize()) {
            EvenArrow<R> nArr = gen.getTopArrow(k);
            if (nArr != arr && nArr.getBotGenerator().qdeg() == q) keepGoing = false;
            else k++;
        }
        //System.out.println("First "+keepGoing);
        if (!keepGoing) return true;
        gen = arr.getBotGenerator();
        k = 0;
        while (keepGoing && k < gen.bMorSize()) {
            EvenArrow<R> nArr = gen.getBotArrow(k);
            if (nArr != arr && nArr.getTopGenerator().qdeg() == q) keepGoing = false;
            else k++;
        }
        //System.out.println("Second "+keepGoing);
        return !keepGoing;
    }// */
    
    private int getZeroLevel() {
        boolean found = false;
        int lev = 0;
        while (!found) {
            if (generators.get(lev).isEmpty()) lev++;
            else {
                Generator gen = generators.get(lev).get(0);
                if (gen.hdeg() == 0) found = true;
                else lev++;
            }
        }
        return lev;
    }
    
    private boolean isolateTop(EvenArrow<R> arr) {
        //System.out.println("Before iso top "+boundaryCheck());
        int m = arr.getTopGenerator().tMorSize()-1;
        int q = arr.getBotGenerator().qdeg();
        //System.out.println("HereTop "+arr.getValue()+" "+q);
        while (m >= 0) {
            EvenArrow<R> ar = arr.getTopGenerator().getTopArrow(m);
            if (ar != arr && ar.getBotGenerator().qdeg() == q) {
                R k = ar.getValue().div(arr.getValue()).negate();
                EvenGenerator<R> aGen = ar.getBotGenerator();
                for (int j = 0; j < arr.getBotGenerator().bMorSize(); j++) {
                    EvenArrow<R> nar = arr.getBotGenerator().getBotArrow(j);
                    boolean found = false;
                    int i = 0;
                    while (!found && i < aGen.bMorSize()) {
                        if (aGen.getBotArrow(i).getTopGenerator() == nar.getTopGenerator()) found = true;
                        else i++;
                    }
                    EvenArrow<R> narr;
                    if (found) {
                        narr = aGen.getBotArrow(i);
                        narr.addValue(k.multiply(nar.getValue()));
                        if (narr.getValue().isZero()) {
                            narr.getBotGenerator().getBotArrows().remove(narr);
                            narr.getTopGenerator().getTopArrows().remove(narr);
                        }
                    }
                    else {
                        narr = new EvenArrow<R>(aGen, nar.getTopGenerator(), 
                                new Cobordism<R>(0, k.multiply(nar.getValue())));
                        if (!narr.getValue().isZero()) {
                            nar.getTopGenerator().addTopArrow(narr);
                            aGen.addBotArrow(narr);
                        }
                    }
                }
                for (int j = 0; j < aGen.tMorSize(); j++) {
                    EvenArrow<R> bar = aGen.getTopArrow(j);
                    boolean found = false;
                    int i = 0;
                    while (!found && i < bar.getBotGenerator().bMorSize()) {
                        if (bar.getBotGenerator().getBotArrow(i).getTopGenerator() 
                                == arr.getBotGenerator()) found = true;
                        else i++;
                    }
                    EvenArrow<R> narr;
                    if (found) {
                        narr = bar.getBotGenerator().getBotArrow(i);
                        narr.addValue(k.multiply(bar.getValue()).negate());
                        if (narr.getValue().isZero()) {
                            narr.getBotGenerator().getBotArrows().remove(narr);
                            narr.getTopGenerator().getTopArrows().remove(narr);
                        }
                    }
                    else {
                        narr = new EvenArrow<R>(bar.getBotGenerator(), arr.getBotGenerator(), 
                                new Cobordism<R>(0, k.multiply(bar.getValue()).negate()));
                        if (!narr.getValue().isZero()) {
                            narr.getBotGenerator().addBotArrow(narr);
                            narr.getTopGenerator().addTopArrow(narr);
                        }
                    }
                }
            }
            m--;
        }
        //System.out.println("After iso top "+boundaryCheck());
        //System.out.println(numberOfqsTop(arr.getTopGenerator(), q));
        return (numberOfqsTop(arr.getTopGenerator(), q, false) == 1);
    }
    
    private boolean isolateBottom(EvenArrow<R> arr) {
        //System.out.println("Before iso bot "+boundaryCheck());
        int m = arr.getBotGenerator().bMorSize()-1;
        int q = arr.getBotGenerator().qdeg();
        //System.out.println("HereBot "+arr.getValue()+" "+q+" "+arr.getBotGenerator().hdeg());
        while (m >= 0) {
            EvenArrow<R> ar = arr.getBotGenerator().getBotArrow(m);
            if (ar != arr && ar.getTopGenerator().qdeg() == q) {
                R k = ar.getValue().div(arr.getValue()).negate();
                EvenGenerator<R> aGen = ar.getTopGenerator();
                for (int j = 0; j < arr.getTopGenerator().tMorSize(); j++) {
                    EvenArrow<R> nar = arr.getTopGenerator().getTopArrow(j);
                    boolean found = false;
                    int i = 0;
                    while (!found && i < aGen.tMorSize()) {
                        if (aGen.getTopArrow(i).getBotGenerator() == nar.getBotGenerator()) found = true;
                        else i++;
                    }
                    EvenArrow<R> narr;
                    if (found) {
                        narr = aGen.getTopArrow(i);
                        narr.addValue(k.multiply(nar.getValue()));
                        if (narr.getValue().isZero()) {
                            narr.getBotGenerator().getBotArrows().remove(narr);
                            aGen.getTopArrows().remove(narr);
                        }
                    }
                    else {
                        narr = new EvenArrow<R>(nar.getBotGenerator(), aGen, 
                                new Cobordism<R>(0, k.multiply(nar.getValue())));
                        if (!narr.getValue().isZero()) {
                            narr.getBotGenerator().addBotArrow(narr);
                            aGen.addTopArrow(narr);
                        }
                    }
                }
                for (int j = 0; j < aGen.bMorSize(); j++) {
                    EvenArrow<R> tar = aGen.getBotArrow(j);
                    boolean found = false;
                    int i = 0;
                    while (!found && i < tar.getTopGenerator().tMorSize()) {
                        if (tar.getTopGenerator().getTopArrow(i).getBotGenerator() 
                                == arr.getTopGenerator()) found = true;
                        else i++;
                    }
                    EvenArrow<R> narr;
                    if (found) {
                        narr = tar.getTopGenerator().getTopArrow(i);
                        narr.addValue(k.multiply(tar.getValue()).negate());
                        if (narr.getValue().isZero()) {
                            narr.getBotGenerator().getBotArrows().remove(narr);
                            narr.getTopGenerator().getTopArrows().remove(narr);
                        }
                    }
                    else {
                        narr = new EvenArrow<R>(arr.getTopGenerator(), tar.getTopGenerator(), 
                                new Cobordism<R>(0, k.multiply(tar.getValue()).negate()));
                        if (!narr.getValue().isZero()) {
                            narr.getBotGenerator().addBotArrow(narr);
                            narr.getTopGenerator().addTopArrow(narr);
                        }
                    }
                }
            }
            m--;
        }
        //System.out.println("After iso bot "+boundaryCheck());
        //System.out.println(numberOfqsBot(arr.getBotGenerator(), q));
        return (numberOfqsBot(arr.getBotGenerator(), q, false) == 1);
    }
    
    private int numberOfqsTop(EvenGenerator<R> tGen, int q, boolean ge) {
        int qs = 0;
        for (int i = 0; i < tGen.tMorSize(); i++) {
            if (!ge && tGen.getTopArrow(i).getBotGenerator().qdeg() == q) qs++;
            if (ge && tGen.getTopArrow(i).getBotGenerator().qdeg() >= q) qs++;
        }
        return qs;
    }
    
    private int numberOfqsBot(EvenGenerator<R> bGen, int q, boolean ge) {
        int qs = 0;
        for (int i = 0; i < bGen.bMorSize(); i++) {
            if (!ge && bGen.getBotArrow(i).getTopGenerator().qdeg() == q) qs++;
            if (ge && bGen.getBotArrow(i).getTopGenerator().qdeg() >= q) qs++;
        }
        return qs;
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
                EvenGenerator<R> bGen = (EvenGenerator<R>) gens.get(j);
                for (int l = 0; l < bGen.bMorSize(); l++) {
                    EvenArrow<R> arr = bGen.getBotArrow(l);
                    EvenGenerator<R> mGen = arr.getTopGenerator();
                    for (int m = 0; m < mGen.bMorSize(); m++) {
                    EvenArrow<R> tar = mGen.getBotArrow(m);
                    EvenGenerator<R> tGen = tar.getTopGenerator();
                        int k = generators.get(i+2).indexOf(tGen);
                        values.set(k, values.get(k).add(arr.getValue().multiply(tar.getValue())));
                    }
                }
                int k = 0;
                while (check && k < values.size()) {
                    if (!values.get(k).isZero()) check = false;
                    else k++;
                    
                }
                if (!check) System.out.println(i+" "+(generators.get(i).size()-j)+" "+
                        (generators.get(i+2).size()-k)+" "+values);
                j++;
            }
            i++;
        }
        return check;
    }
    
    public String getGrading() {
        int hlevel = getZeroLevel();
        int bl = hlevel-1;
        if (bl < 0) bl++;
        int tl = hlevel+2;
        int[] qmaxmin = maxminQ(hlevel);
        ArrayList<CocycleList<R>> theList = new ArrayList<CocycleList<R>>();
        int q = qmaxmin[0];
        //System.out.println(boundaryCheck());
        while (q >= qmaxmin[1]) {
            smithNormalizeFiltration(q, hlevel, theList);
            if (abInf.isAborted()) return null;
            //System.out.println("For "+q+"   "+boundaryCheck());
            //output(bl, tl);
            //for (CocycleList<R> listElt : theList) listElt.output();
            q = q-2;
        }
        //System.out.println("Von");
        //for (CocycleList<R> listElt : theList) listElt.output();
        //System.out.println("Ende "+theList.size());
        for (CocycleList<R> listElt : theList) listElt.combineCocycles();
        return setValue(theList);
        //if (theValue.contains("(")) 
        //    System.out.println("Value : "+theValue);
    }
    
    private String setValue(ArrayList<CocycleList<R>> theList) {
        int i = firstNonzero(theList);
        String aValue = theList.get(i).qdeg+"";
        String extra = " (";
        while (notOne(theList.get(i))) {
            R val = theList.get(i).getValue().div(theList.get(i+1).getValue());
            aValue = aValue+ extra+val.toString();
            extra = ",";
            i++;
        }
        if (",".equals(extra)) extra = ")";
        else extra = "";
        aValue = aValue+extra;
        return aValue;
    }
    
    private boolean notOne(CocycleList<R> elt) {
        return (!elt.cocycles.get(0).getValue().add(unit.negate()).isZero());
    }
    
    private int firstNonzero(ArrayList<CocycleList<R>> theList) {
        boolean found = false;
        int i = 0;
        while (!found) {
            CocycleList<R> elt = theList.get(i);
            if (!elt.cocycles.isEmpty()) found = true;
            else i++;
        }
        return i;
    }
    
    private void smithNormalizeFiltration(int q, int zl, ArrayList<CocycleList<R>> theList) {
        int level = zl+1;
        while (level >= zl) {
            if (abInf.isAborted()) return;
            EvenArrow<R> arrow = findSmallest(level, q);
            if (arrow == null) {
                level--;
            }
            else {//System.out.println("Filt "+arrow.getBotGenerator().qdeg()+" "+generators.get(zl).size());
                if (isolateBottom(arrow, q, theList)) {
                    if (isolateTop(arrow, q, theList)) {
                        if (arrow.getValue().isInvertible()) cancelArrow(arrow, level, theList);
                        frame.setLabelRight(""+generators.get(zl).size(), 2, false);
                    }
                }
            }
        }
        updateTheList(theList, zl, q);
    }
    
    private void updateTheList(ArrayList<CocycleList<R>> theList, int zl, int q) {
        ArrayList<EvenGenerator<R>> gensOfInt = new ArrayList<EvenGenerator<R>>();
        for (int i = 0; i < generators.get(zl).size(); i++) {
            EvenGenerator<R> gen = (EvenGenerator<R>) generators.get(zl).get(i);
            if (gen.qdeg() >= q && gen.bMorSize() == 0 && numberOfqsTop(gen, q, true) == 0)
                gensOfInt.add(gen);
        }
        CocycleList<R> newList = new CocycleList<R>(q);
        for (EvenGenerator<R> gen : gensOfInt) {
            Cocycle<R> coc = new Cocycle<R>(unit, gen);
            newList.addCocycle(coc);
        }
        theList.add(newList);
    }
    
    private int[] maxminQ(int i) {
        int qx = ((EvenGenerator) generators.get(i).get(0)).qdeg();
        int qn = qx;
        for (int j = 1; j < generators.get(i).size(); j++) {
            int q = ((EvenGenerator) generators.get(i).get(j)).qdeg();
            if (qx < q) qx = q;
            if (qn > q) qn = q;
        }
        if (i > 0) {
            for (int j = 0; j < generators.get(i-1).size(); j++) {
                int q = ((EvenGenerator) generators.get(i-1).get(j)).qdeg();
                if (qn > q) qn = q;
            }
        }
        return new int[] {qx, qn};
    }
    
    private boolean isolateTop(EvenArrow<R> arr, int q, ArrayList<CocycleList<R>> theList) {
        int m = arr.getTopGenerator().tMorSize()-1;
        while (m >= 0) {
            EvenArrow<R> ar = arr.getTopGenerator().getTopArrow(m);
            if (ar != arr && ar.getBotGenerator().qdeg() >= q) {
                R k = ar.getValue().div(arr.getValue()).negate();
                EvenGenerator<R> aGen = ar.getBotGenerator();
                for (int j = 0; j < arr.getBotGenerator().bMorSize(); j++) {
                    EvenArrow<R> nar = arr.getBotGenerator().getBotArrow(j);
                    boolean found = false;
                    int i = 0;
                    while (!found && i < aGen.bMorSize()) {
                        if (aGen.getBotArrow(i).getTopGenerator() == nar.getTopGenerator()) found = true;
                        else i++;
                    }
                    EvenArrow<R> narr;
                    if (found) {
                        narr = aGen.getBotArrow(i);
                        narr.addValue(k.multiply(nar.getValue()));
                        if (narr.getValue().isZero()) {
                            narr.getBotGenerator().getBotArrows().remove(narr);
                            narr.getTopGenerator().getTopArrows().remove(narr);
                        }
                    }
                    else {
                        narr = new EvenArrow<R>(aGen, nar.getTopGenerator(), 
                                new Cobordism<R>(0, k.multiply(nar.getValue())));
                        if (!narr.getValue().isZero()) {
                            nar.getTopGenerator().addTopArrow(narr);
                            aGen.addBotArrow(narr);
                        }
                    }
                }
                for (int j = 0; j < aGen.tMorSize(); j++) {
                    EvenArrow<R> bar = aGen.getTopArrow(j);
                    boolean found = false;
                    int i = 0;
                    while (!found && i < bar.getBotGenerator().bMorSize()) {
                        if (bar.getBotGenerator().getBotArrow(i).getTopGenerator() 
                                == arr.getBotGenerator()) found = true;
                        else i++;
                    }
                    EvenArrow<R> narr;
                    if (found) {
                        narr = bar.getBotGenerator().getBotArrow(i);
                        narr.addValue(k.multiply(bar.getValue()).negate());
                        if (narr.getValue().isZero()) {
                            narr.getBotGenerator().getBotArrows().remove(narr);
                            narr.getTopGenerator().getTopArrows().remove(narr);
                        }
                    }
                    else {
                        narr = new EvenArrow<R>(bar.getBotGenerator(), arr.getBotGenerator(), 
                                new Cobordism<R>(0, k.multiply(bar.getValue()).negate()));
                        if (!narr.getValue().isZero()) {
                            narr.getBotGenerator().addBotArrow(narr);
                            narr.getTopGenerator().addTopArrow(narr);
                        }
                    }
                }
                if (aGen.hdeg() == 0) changeCocycles(theList, aGen, arr.getBotGenerator(), k);
            }
            m--;
        }
        return (numberOfqsTop(arr.getTopGenerator(), q, true) == 1);
    }
    
    private boolean isolateBottom(EvenArrow<R> arr, int q, ArrayList<CocycleList<R>> theList) {
        int m = arr.getBotGenerator().bMorSize()-1;
        while (m >= 0) {
            EvenArrow<R> ar = arr.getBotGenerator().getBotArrow(m);
            if (ar != arr) {
                R k = ar.getValue().div(arr.getValue()).negate();
                EvenGenerator<R> aGen = ar.getTopGenerator();
                for (int j = 0; j < arr.getTopGenerator().tMorSize(); j++) {
                    EvenArrow<R> nar = arr.getTopGenerator().getTopArrow(j);
                    boolean found = false;
                    int i = 0;
                    while (!found && i < aGen.tMorSize()) {
                        if (aGen.getTopArrow(i).getBotGenerator() == nar.getBotGenerator()) found = true;
                        else i++;
                    }
                    EvenArrow<R> narr;
                    if (found) {
                        narr = aGen.getTopArrow(i);
                        narr.addValue(k.multiply(nar.getValue()));
                        if (narr.getValue().isZero()) {
                            narr.getBotGenerator().getBotArrows().remove(narr);
                            aGen.getTopArrows().remove(narr);
                        }
                    }
                    else {
                        narr = new EvenArrow<R>(nar.getBotGenerator(), aGen, 
                                new Cobordism<R>(0, k.multiply(nar.getValue())));
                        if (!narr.getValue().isZero()) {
                            narr.getBotGenerator().addBotArrow(narr);
                            aGen.addTopArrow(narr);
                        }
                    }
                }
                for (int j = 0; j < aGen.bMorSize(); j++) {
                    EvenArrow<R> tar = aGen.getBotArrow(j);
                    boolean found = false;
                    int i = 0;
                    while (!found && i < tar.getTopGenerator().tMorSize()) {
                        if (tar.getTopGenerator().getTopArrow(i).getBotGenerator() 
                                == arr.getTopGenerator()) found = true;
                        else i++;
                    }
                    EvenArrow<R> narr;
                    if (found) {
                        narr = tar.getTopGenerator().getTopArrow(i);
                        narr.addValue(k.multiply(tar.getValue()).negate());
                        if (narr.getValue().isZero()) {
                            narr.getBotGenerator().getBotArrows().remove(narr);
                            narr.getTopGenerator().getTopArrows().remove(narr);
                        }
                    }
                    else {
                        narr = new EvenArrow<R>(arr.getTopGenerator(), tar.getTopGenerator(), 
                                new Cobordism<R>(0, k.multiply(tar.getValue()).negate()));
                        if (!narr.getValue().isZero()) {
                            narr.getBotGenerator().addBotArrow(narr);
                            narr.getTopGenerator().addTopArrow(narr);
                        }
                    }
                }
                if (aGen.hdeg() == 0) changeCocycles(theList, arr.getTopGenerator(), aGen, k);
            }
            m--;
        }
        return (numberOfqsBot(arr.getBotGenerator(), q, true) == 1);
    }
    
    private void changeCocycles(ArrayList<CocycleList<R>> theList, EvenGenerator<R> aGen, 
            EvenGenerator<R> bGen, R k) {
        for (CocycleList<R> listElt : theList) {
            for (Cocycle<R> coc : listElt.cocycles) {
                if (coc.contains(aGen)) coc.addGen(aGen, bGen, k);
            }
        }
    }
    
    private void  cancelArrow(EvenArrow<R> arrow, int level, ArrayList<CocycleList<R>> theList) {
        EvenGenerator<R> tGen = arrow.getTopGenerator();
        EvenGenerator<R> bGen = arrow.getBotGenerator();
        for (int j = 0; j < tGen.bMorSize(); j++) {
            EvenArrow<R> arr = tGen.getBotArrow(j);
            arr.getTopGenerator().getTopArrows().remove(arr);
        }
        for (int j = 0; j < tGen.tMorSize(); j++) {
            EvenArrow<R> arr = tGen.getTopArrow(j);
            arr.getBotGenerator().getBotArrows().remove(arr);
        }
        for (int j = 0; j < bGen.tMorSize(); j++) {
            EvenArrow<R> arr = bGen.getTopArrow(j);
            arr.getBotGenerator().getBotArrows().remove(arr);
        }
        generators.get(level-1).remove(bGen);
        generators.get(level).remove(tGen);
        if (bGen.hdeg() == 0) removeGeneratorFrom(theList, bGen);
        if (tGen.hdeg() == 0) removeGeneratorFrom(theList, tGen);
    }
    
    private void removeGeneratorFrom(ArrayList<CocycleList<R>> theList, EvenGenerator<R> gen) {
        for (CocycleList<R> listElt : theList) {
            for (Cocycle<R> coc : listElt.cocycles) {
                coc.removeGen(gen);
            }
            listElt.removeZeroes();
        }
    }
    
    public GradedComplex<R> mirror() {
        GradedComplex<R> mirror = new GradedComplex<R>(unit, abInf, frame);
        int zl = getZeroLevel();
        int hl = zl;
        if (generators.size() > zl+1) {
            ArrayList<Generator<R>> levelOne = new ArrayList<Generator<R>>();
            for (int i = 0; i < generators.get(zl+1).size(); i++) {
                EvenGenerator<R> gen = (EvenGenerator<R>) generators.get(zl+1).get(i);
                EvenGenerator<R> nGen = new EvenGenerator<R>(0, -gen.hdeg(), -gen.qdeg());
                levelOne.add(nGen);
            }
            mirror.generators.add(levelOne);
        }
        while (hl >= zl-1 && hl >= 0) {
            ArrayList<Generator<R>> nextLevel = new ArrayList<Generator<R>>();
            for (int i = 0; i < generators.get(hl).size(); i++) {
                EvenGenerator<R> gen = (EvenGenerator<R>) generators.get(hl).get(i);
                EvenGenerator<R> nGen = new EvenGenerator<R>(0, -gen.hdeg(), -gen.qdeg());
                nextLevel.add(nGen);
                for (int j = 0; j < gen.bMorSize(); j++) {
                    EvenArrow<R> arr = gen.getBotArrow(j);
                    int k = generators.get(hl+1).indexOf(arr.getTopGenerator());
                    int u = mirror.generators.size()-1;
                    EvenGenerator<R> bGen = (EvenGenerator<R>) mirror.generators.get(u).get(k);
                    EvenArrow<R> nArr = new EvenArrow<R>(bGen,
                            nGen, new Cobordism<R>(0, arr.getValue()));
                    bGen.addBotArrow(nArr);
                    nGen.addTopArrow(nArr);
                }
            }
            mirror.generators.add(nextLevel);
            hl--;
        }
        return mirror;
    }
    
    
    
    
    
    
    private class Cocycle<R extends Ring<R>> {
        
        private final ArrayList<R> values;
        private final ArrayList<EvenGenerator<R>> geners;
        
        private Cocycle(R unit, EvenGenerator<R> gen) {
            values = new ArrayList<R>();
            geners = new ArrayList<EvenGenerator<R>>();
            values.add(unit);
            geners.add(gen);
        }
        
        private boolean contains(EvenGenerator<R> gen) {
            return (geners.contains(gen));
        }
        
        private void addGen(EvenGenerator<R> aGen, EvenGenerator<R> bGen, R k) {
            int u = geners.indexOf(aGen);
            R val = values.get(u);
            int v = geners.indexOf(bGen);
            if (v == -1) {
                v = geners.size();
                geners.add(bGen);
                values.add(val.multiply(k));
            }
            else {
                values.set(v, values.get(v).add(val.multiply(k)));
            }
            if (values.get(v).isZero()) {
                values.remove(v);
                geners.remove(v);
            }
        }
        
        private void removeGen(EvenGenerator<R> gen) {
            int u = geners.indexOf(gen);
            if (u >= 0) {
                geners.remove(u);
                values.remove(u);
            }
        }
        
        private R getValue() { // it is assumed that there is exactly one value
            return values.get(0);
        }
        
        private void setValue(R val) {
            values.set(0, val);
        }
        
        private void output() {
            int zl = getZeroLevel();
            for (int i = 0; i < geners.size(); i++) {
                System.out.print(values.get(i)+" ("+generators.get(zl).indexOf(geners.get(i))+")  ");
            }
            System.out.println();
        }
    }
    
    private class CocycleList<R extends Ring<R>> {
        
        private final int qdeg;
        private final ArrayList<Cocycle<R>> cocycles;
        
        private CocycleList(int q) {
            qdeg = q;
            cocycles = new ArrayList<Cocycle<R>>();
        }
        
        private void removeZeroes() {
            int u = cocycles.size()-1;
            while (u >= 0) {
                if (cocycles.get(u).geners.isEmpty()) cocycles.remove(u);
                u--;
            }
        }
        
        private void addCocycle(Cocycle<R> coc) {
            cocycles.add(coc);
        }
        
        private R getValue() {
            return cocycles.get(0).getValue();
        }
        
        private void output() {
            System.out.println("q = "+qdeg);
            System.out.println("Cocycles:");
            for (Cocycle<R> coc : cocycles) coc.output();
            System.out.println();
        }

        private void combineCocycles() {
            for (Cocycle<R> coc : cocycles) { // make all values positive.
                for (int i = 0; i < coc.values.size(); i++) 
                    coc.values.set(i, coc.values.get(i).abs(0));
            }
            while (cocycles.size() > 1) {
                Cocycle<R> firstCoc = cocycles.get(0);
                Cocycle<R> seconCoc = cocycles.get(1);
                R fValue = firstCoc.getValue();
                R sValue = seconCoc.getValue();
                //System.out.println(fValue+" "+sValue);
                if (fValue.divides(sValue)) cocycles.remove(1);
                else {
                    if (sValue.divides(fValue)) cocycles.remove(0);
                    else {
                        if (fValue.isBigger(sValue)) 
                            firstCoc.setValue(fValue.add(sValue.multiply(fValue.div(sValue)).negate()));
                        else seconCoc.setValue(sValue.add(fValue.multiply(sValue.div(fValue)).negate()));
                    }
                }
            }
        }
        
    }
    
}