/*

Copyright (C) 2019-20 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.polynomial.jones;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Diagram;
import knotjob.polynomial.Coefficient;
import knotjob.polynomial.Polynomial;

/**
 *
 * @author Dirk
 */
public class BracketPolynomial {

    private ArrayList<DiagPolynomial> polys;
    private ArrayList<Diagram> diagrams;
    private ArrayList<ArrayList<Integer>> paths;
    private ArrayList<Integer> posEndpts;
    private ArrayList<Integer> negEndpts;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    
    BracketPolynomial(int cross, int[] ends, DialogWrap frm, AbortInfo abf, boolean rev) {
        abInf = abf;
        frame = frm;
        posEndpts = new ArrayList<Integer>();
        negEndpts = new ArrayList<Integer>();
        int change = 0;
        if (!rev) change = 1;
        posEndpts.add(ends[0+change]);
        posEndpts.add(ends[2+change]);
        negEndpts.add(ends[1-change]);
        negEndpts.add(ends[3-change]);
        int r = Math.abs(cross);
        paths = new ArrayList<ArrayList<Integer>>();
        diagrams = new ArrayList<Diagram>();
        for (int p : posEndpts) {
            for (int n : negEndpts) paths.add(newPath(p,n));
        }
        diagrams.add(new Diagram(0,3));
        diagrams.add(new Diagram(1,2));
        polys = new ArrayList<DiagPolynomial>();
        polys.add(new DiagPolynomial(new Polynomial(new String[] {"A"},BigInteger.ONE, new int[] {cross}),0));
        ArrayList<Coefficient> coffs = new ArrayList<Coefficient>();
        BigInteger dude = BigInteger.ONE;
        int fac = -1;
        if (cross > 0) fac = 1;
        for (int i = 0; i < r; i++) {
            coffs.add(new Coefficient(new int[] {cross-2 * fac - fac *i}, dude));
            dude = dude.multiply(BigInteger.valueOf(-1));
        }
        polys.add(new DiagPolynomial(new Polynomial(new String[] {"A"}, coffs),1));
    }

    public void output() {
        System.out.println("Positive Endpts "+posEndpts);
        System.out.println("Negative Endpts "+negEndpts);
        System.out.println("Paths : "+paths);
        System.out.println("Diagrams");
        for (int i = 0; i < diagrams.size(); i++) {
            System.out.print(i+" ");
            diagrams.get(i).output();
        }
        for (DiagPolynomial dPoly : polys) dPoly.output();
        System.out.println();
        System.out.println();
    }
    
    private ArrayList<Integer> newPath(int p, int n) {
        ArrayList<Integer> path = new ArrayList<Integer>(2);
        path.add(p);
        path.add(n);
        return path;
    }
    
    public boolean negContains(int u) {
        return negEndpts.contains(u);
    }
    
    public boolean posContains(int u) {
        return posEndpts.contains(u);
    }

    void modifyLast(BracketPolynomial nextBracket) {
        ArrayList<Integer> pEndpts = new ArrayList<Integer>();
        ArrayList<Integer> nEndpts = new ArrayList<Integer>();
        frame.setLabelRight("0", 1, false);
        ArrayList<DiagPolynomial> nPolys = new ArrayList<DiagPolynomial>();
        ArrayList<ArrayList<Integer>> nPaths = new ArrayList<ArrayList<Integer>>();
        getLastPolynomial(nextBracket, pEndpts,nEndpts,nPolys,nPaths);
        polys = nPolys;
    }
    
    void modify(BracketPolynomial nextBracket, String girthInfo) {
        ArrayList<Integer> pEndpts = new ArrayList<Integer>();
        ArrayList<Integer> nEndpts = new ArrayList<Integer>();
        for (int i : posEndpts) pEndpts.add(i);
        for (int i : negEndpts) nEndpts.add(i);
        for (Integer i : nextBracket.posEndpts) {
            if (negEndpts.contains(i)) nEndpts.remove(i);
            else pEndpts.add(i);
        }
        for (Integer i : nextBracket.negEndpts) {
            if (posEndpts.contains(i)) pEndpts.remove(i);
            else nEndpts.add(i);
        }
        frame.setLabelRight(girthInfo, 1, false);
        ArrayList<DiagPolynomial> nPolys = new ArrayList<DiagPolynomial>();
        ArrayList<Diagram> nDiagrams = new ArrayList<Diagram>();
        ArrayList<ArrayList<Integer>> nPaths = new ArrayList<ArrayList<Integer>>();
        getNewPolynomials(nextBracket, pEndpts,nEndpts,nPolys,nDiagrams,nPaths);
        posEndpts = pEndpts;
        negEndpts = nEndpts;
        polys = nPolys;
        diagrams = nDiagrams;
        paths = nPaths;
    }

    private Polynomial compFactor() {
        BigInteger minus = BigInteger.valueOf(-1);
        ArrayList<Coefficient> coffs = new ArrayList<Coefficient>(2);
        coffs.add(new Coefficient(new int[] {2}, minus));
        coffs.add(new Coefficient(new int[] {-2}, minus));
        return new Polynomial(new String[] {"A"},coffs);
    }
    
    private void getLastPolynomial(BracketPolynomial nextBracket, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, 
            ArrayList<DiagPolynomial> nPolys, ArrayList<ArrayList<Integer>> nPaths) {
        Polynomial compFactor = compFactor();
        int i = 0;
        while (i < polys.size()) {
            DiagPolynomial pol = polys.get(i);
            for (DiagPolynomial nPol : nextBracket.polys) {
                Diagram nDiag = combineDiagram(diagrams.get(pol.getDiagram()), nextBracket.diagrams.get(nPol.getDiagram()),
                        pEndpts, nEndpts,nPaths, nextBracket);
                Polynomial cPol = pol.getPolynomial().multiply(nPol.getPolynomial());
                if (nDiag.circles.get(0) == 2) cPol = cPol.multiply(compFactor);
                DiagPolynomial combPoly = new DiagPolynomial(cPol,0);
                addPolynomial(combPoly, nPolys);
            }
            i++;
        }
    }
    
    private void getNewPolynomials(BracketPolynomial nextBracket, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts, 
            ArrayList<DiagPolynomial> nPolys, ArrayList<Diagram> nDiagrams, ArrayList<ArrayList<Integer>> nPaths) {
        Polynomial compFactor = compFactor();
        int i = 0;
        while (i < polys.size()) {
            DiagPolynomial pol = polys.get(i);
            for (DiagPolynomial nPol : nextBracket.polys) {
                Diagram nDiag = combineDiagram(diagrams.get(pol.getDiagram()), nextBracket.diagrams.get(nPol.getDiagram()),
                        pEndpts, nEndpts, nPaths, nextBracket);
                Polynomial cPol = pol.getPolynomial().multiply(nPol.getPolynomial());
                for (int j = 0; j < nDiag.circles.get(0); j++) cPol = cPol.multiply(compFactor);
                DiagPolynomial combPoly = new DiagPolynomial(cPol, getDiagNumber(nDiag, nDiagrams));
                addPolynomial(combPoly, nPolys);
            }
            i++;
        }
    }

    private Diagram combineDiagram(Diagram fDiag, Diagram sDiag, ArrayList<Integer> pEndpts, ArrayList<Integer> nEndpts,
            ArrayList<ArrayList<Integer>> nPaths, BracketPolynomial next) {
        ArrayList<ArrayList<Integer>> newPaths = new ArrayList<ArrayList<Integer>>();
        for (int i : fDiag.paths) {
            ArrayList<Integer> cpath = new ArrayList<Integer>();
            for (int y : paths.get(i)) cpath.add(y);
            newPaths.add(cpath);
        }
        for (int j : sDiag.paths) {
            ArrayList<Integer> cpath = new ArrayList<Integer>();
            for (int y : next.paths.get(j)) cpath.add(y);
            newPaths.add(cpath);
        }
        for (int e : posEndpts) if (!pEndpts.contains(e)) combinePaths(newPaths, e, true);
        for (int e : negEndpts) if (!nEndpts.contains(e)) combinePaths(newPaths, e, false);
        ArrayList<ArrayList<Integer>> ncircles = getCircles(newPaths);
        Diagram nDiag = new Diagram();
        nDiag.paths = new ArrayList<Integer>(newPaths.size());
        nDiag.circles = new ArrayList<Integer>(1);
        for (ArrayList<Integer> npth : newPaths) {
            while (npth.size()>2) npth.remove(1);
            int p = getPathNumber(npth, nPaths);
            nDiag.paths.add(p);
        }
        nDiag.circles.add(ncircles.size());
        return nDiag;
    }
    
    private int getDiagNumber(Diagram nDiag, ArrayList<Diagram> dgrams) {
        Collections.sort(nDiag.paths);
        boolean found = false;
        int i = 0;
        while (!found && i < dgrams.size()) {
            Diagram cDiag = dgrams.get(i);
            if (sameDiag(nDiag,cDiag)) found = true;
            else i++;
        }
        if (found) return i;
        dgrams.add(nDiag);
        return dgrams.size()-1;
    }
    
    private boolean sameDiag(Diagram nDiag, Diagram cDiag) {
        if (nDiag.paths.size() != cDiag.paths.size()) return false;
        boolean same = true;
        int i = 0;
        int t = nDiag.paths.size();
        while (same && i < t) {
            if (!Objects.equals(nDiag.paths.get(i), cDiag.paths.get(i))) same = false;
            else i++;
        }
        if (!same) return false;
        i = 0;
        t = nDiag.circles.size();
        while (same && i < t) {
            if (!Objects.equals(nDiag.circles.get(i), cDiag.circles.get(i))) same = false;
            else i++;
        }
        return same;
    }

    private void addPolynomial(DiagPolynomial combPoly, ArrayList<DiagPolynomial> nPolys) {
        boolean found = false;
        int i = 0;
        while (!found && i < nPolys.size()) {
            DiagPolynomial cand = nPolys.get(i);
            if (cand.getDiagram() == combPoly.getDiagram()) {
                found = true;
                cand.setPolynomial(cand.getPolynomial().add(combPoly.getPolynomial()));
                if (cand.getPolynomial().isZero()) nPolys.remove(cand);
            }
            else i++;
        }
        if (!found) nPolys.add(combPoly);
    }
    
    private void combinePaths(ArrayList<ArrayList<Integer>> newPaths, int e, boolean b) {
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
    
    private ArrayList<ArrayList<Integer>> getCircles(ArrayList<ArrayList<Integer>> newPaths) {
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
    
    private int getPathNumber(ArrayList<Integer> npth, ArrayList<ArrayList<Integer>> pths) {
        boolean found = false;
        int i = 0;
        while (!found && i < pths.size()) {
            if (samePath(npth,pths.get(i))) found = true;
            else i++;
        }
        if (found) return i;
        pths.add(npth);
        return pths.size()-1;
    }
    
    private boolean samePath(ArrayList<Integer> npth, ArrayList<Integer> opth) {
        if (npth.size()!=opth.size()) return false;
        boolean same = true;
        int i = 0;
        int t = npth.size();
        while (same && i < t) {
            if (!Objects.equals(npth.get(i), opth.get(i))) same = false;
            else i++;
        }
        return same;
    }
    
    public Polynomial finalPolynomial() {
        return polys.get(0).getPolynomial();
    }
    
}
