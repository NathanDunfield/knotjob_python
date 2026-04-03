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

package knotjob.homology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.rings.Ring;

/**
 *
 * @author dirk
 * @param <R>
 */
public class TangleComplex<R extends Ring<R>> extends ChainComplex<R> {
    
    protected ArrayList<Integer> posEndpts;
    protected ArrayList<Integer> negEndpts;
    protected int counter;
    
    public TangleComplex(R unt, AbortInfo abf, DialogWrap frm) {
        super(unt, frm, abf);
        posEndpts = new ArrayList<Integer>();
        negEndpts = new ArrayList<Integer>();
    }
    
    public TangleComplex(R unt) {
        super(unt, null, null);
        posEndpts = new ArrayList<Integer>();
        negEndpts = new ArrayList<Integer>();
    }
    
    public TangleComplex(ArrayList<ArrayList<Generator<R>>> gens, R unt, 
            DialogWrap fram, AbortInfo abf) {
        super(unt, gens, fram, abf);
        posEndpts = new ArrayList<Integer>();
        negEndpts = new ArrayList<Integer>();
    }
    
    public int posNumber() {
        return posEndpts.size();
    }
    
    public boolean negContains(int path) {
        return negEndpts.contains(path);
    }

    public boolean posContains(int path) {
        return posEndpts.contains(path);
    }

    protected ArrayList<Integer> newPath(int a, int b) {
        ArrayList<Integer> path = new ArrayList<Integer>(2);
        path.add(a);
        path.add(b);
        return path;
    }
    
    protected void combinePaths(ArrayList<ArrayList<Integer>> newPaths, int e, boolean b) {
        boolean found = false;
        int i = 0;
        while (!found) {
            ArrayList<Integer> fpath = newPaths.get(i);
            if (b && fpath.get(0) == e) found = true;
            if (!b && fpath.get(fpath.size()-1) == e) found = true;
            if (!found) i++;
        }
        ArrayList<Integer> fpath = newPaths.get(i);
        found = false;
        i = 0;
        while (!found) {
            ArrayList<Integer> spath = newPaths.get(i);
            if (b && spath.get(spath.size()-1) == e) found = true;
            if (!b && spath.get(0) == e) found = true;
            if (!found) i++;
        }
        ArrayList<Integer> spath = newPaths.get(i);
        if (b) { // in this case it is not possible that fpath = spath, I think
            newPaths.remove(fpath);
            for (int k = 1; k < fpath.size(); k++) spath.add(fpath.get(k));
        }
        else { // here we might produce a circle
            if (fpath != spath) { // not a circle
                newPaths.remove(spath);
                for (int k = 1; k < spath.size(); k++) fpath.add(spath.get(k)); 
            }// else, leave it as it is
        }
    }
    
    protected ArrayList<ArrayList<Integer>> getCircles(ArrayList<ArrayList<Integer>> newPaths) {
        ArrayList<ArrayList<Integer>> circs = new ArrayList<ArrayList<Integer>>(2);
        int i = newPaths.size()-1;
        while (i >= 0) {
            ArrayList<Integer> path = newPaths.get(i);
            if (Objects.equals(path.get(0), path.get(path.size()-1))) {
                newPaths.remove(i);
                circs.add(path);
            }
            i--;
        }
        return circs;
    }
    
    protected int newDiagNumber(Diagram oldDig, Cache tCache, Cache dCache, ArrayList<Integer> ddigTrans, int oldDigNr) {
        Diagram newDig = new Diagram();
        for (int y : oldDig.paths) {
            ArrayList<Integer> npath = new ArrayList<Integer>(2);
            ArrayList<Integer> oPath = tCache.getPaths(y);
            npath.add(oPath.get(0));
            npath.add(oPath.get(oPath.size()-1));
            int np = dCache.getPathNumber(npath);
            newDig.paths.add(np);
        }
        int newDigNr = dCache.getDiagNumber(newDig);
        ddigTrans.set(oldDigNr, newDigNr);
        return newDigNr;
    }

    protected int[] surgeryPath(int fPath, Diagram dig, Cache tCache) {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (tCache.getPaths(dig.paths.get(i)).contains(fPath)) found = true;
            else i++;
        }
        ArrayList<Integer> path = tCache.getPaths(dig.paths.get(i));
        return new int[] { path.get(0), path.get(path.size()-1) };
    }
    
    protected int startOfPath(int fPath, Diagram dig, Cache tCache) {
        boolean found = false;
        int i = 0;
        while (!found && i < dig.paths.size()) {
            if (tCache.getPaths(dig.paths.get(i)).contains(fPath)) found = true;
            else i++;
        }
        if (found) return tCache.getPaths(dig.paths.get(i)).get(0);
        i = 0;
        while (!found) {
            if (tCache.getPaths(dig.circles.get(i)).contains(fPath)) found = true;
            else i++;
        }
        ArrayList<Integer> circle = tCache.getPaths(dig.circles.get(i));
        return dotOfCircle(circle, tCache.dotPts);
    }

    protected int dotOfCircle(ArrayList<Integer> circle, ArrayList<Integer> dotPts) {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (circle.contains(dotPts.get(i))) found = true;
            else i++;
        }
        return dotPts.get(i);
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
    
    public ChainComplex<R> getComplex() {
        ArrayList<ArrayList<Generator<R>>> genes = new ArrayList<ArrayList<Generator<R>>>(generators.size());
        int p = 0;
        for (ArrayList<Generator<R>> gens : generators) {
            genes.add(new ArrayList<Generator<R>>());
            int i = gens.size()-1;
            while (i >= 0) {
                QGenerator<R> gen = (QGenerator<R>) gens.get(i);
                Generator<R> clGen = new Generator<R>(gen.hdeg());
                genes.get(p).add(0, clGen);
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
    
    // stuff for s-invariant
    
    protected int relevantLine() {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (!generators.get(i).isEmpty()) found = true;
            else i++;
        }
        return i - generators.get(i).get(0).hdeg();
    }
    
    protected ArrayList<ArrayList<R>> zeroMatrix(int i, int j, R twoUnit) {
        ArrayList<ArrayList<R>> matrix = new ArrayList<ArrayList<R>>();
        for (int y = 0; y < i; y++) matrix.add(new ArrayList<R>(j));
        for (int y = 0; y < i; y++) {
            for (int z = 0; z < j; z++) matrix.get(y).add(twoUnit.getZero());
        }
        return matrix;
    }
    
    protected int objectsDegree(int h) {
        boolean found = false;
        int j = 0;
        while (!found) {
            if (!generators.get(j).isEmpty()) {
                if (generators.get(j).get(0).hdeg() == h) found = true;
            }
            if (!found) j++;
        }
        return j;
    }
    
    protected void improveMatrix(ArrayList<ArrayList<R>> matrix) {
        boolean done = false;
        int[] corner = new int[2];
        ArrayList<Integer> avoid = new ArrayList<Integer>();
        while (!done) {
            corner = newEntry(matrix, avoid, corner[1]);
            if (corner[0] == -1) done = true;
            else {
                for (int j = 0; j < matrix.size(); j++) {
                    if (j != corner[0] & matrix.get(j).get(corner[1]).isInvertible()) 
                        addRow(matrix,corner[0],j);
                }
            }
            corner[1]++;
        }
    }
    
    private int[] newEntry(ArrayList<ArrayList<R>> matrix, ArrayList<Integer> avoid, int b) {
        int[] entry = new int[2];
        int i = 0;
        while (avoid.contains(i)) i++;
        if (i >= matrix.size()) {
            entry[0] = -1;
            return entry;
        }
        int a = i;
        int j = b;
        boolean found = false;
        while (!found & j < matrix.get(0).size()) {
            if (!matrix.get(i).get(j).isZero()) found = true;
            else i++;
            while(avoid.contains(i)) i++;
            if (i >= matrix.size()) {
                i = a;
                j++;
            }
        }
        if (!found) entry[0] = -1;
        else {
            entry[0] = i;
            entry[1] = j;
            avoid.add(i);
        }
        return entry;
    }

    private void addRow(ArrayList<ArrayList<R>> matrix, int a, int b) {
        for (int i = 0; i < matrix.get(a).size(); i++) 
            matrix.get(b).set(i, matrix.get(b).get(i).add(matrix.get(a).get(i)));
    }
    
    protected ArrayList<ArrayList<Integer>> getCocycles(ArrayList<ArrayList<R>> matrix) {
        ArrayList<ArrayList<Integer>> cocycles = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> freeVariables = new ArrayList<Integer>();
        for (int j = 0; j < matrix.get(0).size(); j++) {
            boolean found = false;
            int i = 0;
            while (!found & i < matrix.size()) {
                if (!matrix.get(i).get(j).isZero()) found = true;
                else i++;
            }
            if (!found) freeVariables.add(j);
            else {
                found = false;
                int k = 0;
                while (k < j & !found) {
                    if (!matrix.get(i).get(k).isZero()) found = true;
                    else k++;
                }
                if (found) freeVariables.add(j);
            }
        }
        for (int j : freeVariables) {
            ArrayList<Integer> cocycle = new ArrayList<Integer>();
            cocycle.add(j);
            for (int i = 0; i < matrix.size(); i++) {
                if (!matrix.get(i).get(j).isZero()) {
                    int k = 0;
                    boolean found = false;
                    while (!found) {
                        if (!matrix.get(i).get(k).isZero()) found = true;
                        else k++;
                    }
                    cocycle.add(k);
                }
            }
            cocycles.add(cocycle);
        }
        return cocycles;
    }
    
    protected ArrayList<ArrayList<Generator<R>>> getCoObjects(ArrayList<ArrayList<Integer>> cocycles, 
            ArrayList<Generator<R>> posCocycles) {
        ArrayList<ArrayList<Generator<R>>> objs = new ArrayList<ArrayList<Generator<R>>>();
        for (ArrayList<Integer> ints : cocycles) {
            ArrayList<Generator<R>> cocs = new ArrayList<Generator<R>>();
            for (int y : ints) cocs.add(posCocycles.get(y));
            objs.add(cocs);
        }
        return objs;
    }
    
    public int getrPlus(int sinv, int qmax, int qmin, R twoUnit) {
        int rplus = sinv;
        if (extraCocycles(sinv, qmax, qmin, twoUnit)) rplus = rplus + 2;
        return rplus;
    }
    
    public int getsPlus(int sinv, int qmax, int qmin, R twoUnit) {
        int splus = sinv;
        if (extraCocycles(sinv-2, qmax, qmin, twoUnit)) splus = splus + 2;
        return splus;
    }
    
    protected boolean extraCocycles(int sinv, int qmax, int qmin, R twoUnit) {
        return true; // will be overwritten
    }
    
    public int barnatize(int x) { // x = 0 for reduced version, x = 1 for unreduced
        int t = relevantLine();
        int qmax = ((QGenerator<R>) generators.get(t).get(0)).qdeg();
        int qmin = qmax;
        for (Iterator<Generator<R>> it = generators.get(t).iterator(); it.hasNext();) {
            QGenerator<R> obj = (QGenerator<R>) it.next();
            if (qmax < obj.qdeg()) qmax = obj.qdeg();
            if (qmin > obj.qdeg()) qmin = obj.qdeg();
        }
        cancelFromTop(generators.get(t),qmax,qmin);
        cancelFromBot(generators.get(t),qmax,qmin);
        //if (generators.get(t).size()!= 2) System.out.println("Wrong "+generators.get(t).get(0).hdeg());
        QGenerator<R> genOne = (QGenerator<R>) generators.get(t).get(0);
        QGenerator<R> genTwo = (QGenerator<R>) generators.get(t).get(x);
        int sinvariant = (genOne.qdeg() + genTwo.qdeg())/2;
        return sinvariant;
    }
    
    private void cancelFromTop(ArrayList<Generator<R>> objects0, int qmax, int qmin) {
        int qrun = qmax;
        while (qrun >= qmin) {
            boolean found = false;
            int t = 0;
            while (!found & t < objects0.size()) {
                QGenerator<R> bObj = (QGenerator<R>) objects0.get(t);
                if (bObj.qdeg() == qrun & bObj.hdeg() == 0) {
                    if (!bObj.getBotArrows().isEmpty()) found = true;
                }
                t++;
            }
            if (found) cancelMorObj(objects0, objects0.get(t-1).getBotArrows().get(0));
            else qrun = qrun - 2;
        }
    }
    
    private void cancelFromBot(ArrayList<Generator<R>> objects0, int qmax, int qmin) {
        int qrun = qmin;
        while (qrun <= qmax) {
            boolean found = false;
            int t = 0;
            while (!found & t < objects0.size()) {
                QGenerator<R> tObj = (QGenerator<R>) objects0.get(t);
                if (tObj.qdeg() == qrun & tObj.hdeg() == 0) {
                    if (!tObj.getTopArrows().isEmpty()) found = true;
                }
                t++;
            }
            if (found) cancelMorObj(objects0, objects0.get(t-1).getTopArrows().get(0));
            else qrun = qrun + 2;
        }
    }
    
    protected void cancelMorObj(ArrayList<Generator<R>> objs, Arrow<R> mor) {
        // will be overwritten
    }
    
}
