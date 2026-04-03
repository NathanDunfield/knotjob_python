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

package knotjob.polynomial.slthree;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.polynomial.Coefficient;
import knotjob.polynomial.Polynomial;

/**
 *
 * @author Dirk
 */
public class SpiderPolynomial {
    
    private ArrayList<GridPolynomial> polys;
    private ArrayList<Grid> grids;
    private ArrayList<ArrayList<Integer>> paths;
    private ArrayList<Integer> posEndpts;
    private ArrayList<Integer> negEndpts;
    private ArrayList<Integer> sources;
    private ArrayList<Integer> sinks;
    private final Polynomial polyTwo;
    private final Polynomial polyThr;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    
    public SpiderPolynomial(int[] endpts, int u, int factor, DialogWrap frm, AbortInfo abf) {
        frame = frm;
        abInf = abf;
        polys = new ArrayList<GridPolynomial>();
        grids = new ArrayList<Grid>();
        paths = new ArrayList<ArrayList<Integer>>();
        posEndpts = new ArrayList<Integer>();
        negEndpts = new ArrayList<Integer>();
        sources = new ArrayList<Integer>();
        sinks = new ArrayList<Integer>();
        posEndpts.add(endpts[0]);
        posEndpts.add(endpts[1]);
        negEndpts.add(endpts[2]);
        negEndpts.add(endpts[3]);
        sinks.add(-(2*u+1));
        sources.add(-(2*u+2));
        addPaths();
        Grid grid = new Grid();
        grid.addPath(0);
        grid.addPath(1);
        grids.add(grid);
        grid = new Grid();
        grid.addPath(2);
        grid.addPath(3);
        grid.addPath(4);
        grid.addPath(5);
        grid.addPath(6);
        grids.add(grid);
        GridPolynomial poly = new GridPolynomial(new Polynomial(new String[] {"q"}, 
                BigInteger.ONE, new int[] {2*factor}), 0);
        polys.add(poly);
        poly = new GridPolynomial(new Polynomial(new String[] {"q"}, BigInteger.ONE.negate(),
                new int[] {3*factor}), 1);
        polys.add(poly);
        ArrayList<Coefficient> two = new ArrayList<Coefficient>();
        two.add(new Coefficient(new int[] {1}, BigInteger.ONE));
        two.add(new Coefficient(new int[] {-1}, BigInteger.ONE));
        polyTwo = new Polynomial(new String[] {"q"}, two);
        ArrayList<Coefficient> thr = new ArrayList<Coefficient>();
        thr.add(new Coefficient(new int[] {2}, BigInteger.ONE));
        thr.add(new Coefficient(new int[] {0}, BigInteger.ONE));
        thr.add(new Coefficient(new int[] {-2}, BigInteger.ONE));
        polyThr = new Polynomial(new String[] {"q"}, thr);
    }

    private void addPaths() {
        ArrayList<Integer> path = standardPath(0);
        paths.add(path);
        path = standardPath(1);
        paths.add(path);
        path = indexPath(0, true, 0);
        paths.add(path);
        path = indexPath(1, true, 1);
        paths.add(path);
        path = indexPath(0, false, 0);
        paths.add(path);
        path = indexPath(1, false, 1);
        paths.add(path);
        path = new ArrayList<Integer>();
        path.add(sources.get(0));
        path.add(2);
        path.add(sinks.get(0));
        path.add(2);
        paths.add(path);
    }

    private ArrayList<Integer> indexPath(int i, boolean sink, int pos) {
        ArrayList<Integer> path = new ArrayList<Integer>();
        int fir = posEndpts.get(i);
        int sec = 0;
        int thi = sinks.get(0);
        int vie = pos;
        if (!sink) {
            fir = sources.get(0);
            sec = pos;
            thi = negEndpts.get(i);
            vie = 0;
        }
        path.add(fir);
        path.add(sec);
        path.add(thi);
        path.add(vie);
        return path;
    }
    
    private ArrayList<Integer> standardPath(int i) {
        ArrayList<Integer> path = new ArrayList<Integer>();
        path.add(posEndpts.get(i));
        path.add(0);
        path.add(negEndpts.get(i));
        path.add(0);
        return path;
    }
    
    public Polynomial getLastPolynomial() {
        return polys.get(0).getPolynomial();
    }
    
    public void output() {
        System.out.println("Positive Endpoints "+posEndpts);
        System.out.println("Negative Endpoints "+negEndpts);
        System.out.println("Sources "+sources);
        System.out.println("Sinks   "+sinks);
        System.out.println();
        System.out.println("Paths : "+paths);
        System.out.println();
        System.out.println("Grids");
        for (Grid grid : grids) {
            grid.output();
            System.out.println();
        }
        System.out.println("Polynomials");
        for (GridPolynomial poly : polys) poly.output();
    }

    public ArrayList<Integer> overlap(int[] path) {
        ArrayList<Integer> ovlp = new ArrayList<Integer>();
        for (int i = 0; i < 4; i++) {
            if (posEndpts.contains(path[i])) ovlp.add(i);
            if (negEndpts.contains(path[i])) ovlp.add(i);
        }
        return ovlp;
    }
    
    public boolean direction(int p) {
        return negEndpts.contains(p);
    }

    void modify(SpiderPolynomial nextSpider, String girthInfo) {
        ArrayList<Integer> pEndpts = new ArrayList<Integer>();
        ArrayList<Integer> nEndpts = new ArrayList<Integer>();
        ArrayList<Integer> snks = new ArrayList<Integer>();
        ArrayList<Integer> srcs = new ArrayList<Integer>();
        for (int i : posEndpts) pEndpts.add(i);
        for (int i : negEndpts) nEndpts.add(i);
        for (int i : sinks) snks.add(i);
        for (int i : sources) srcs.add(i);
        for (int i : nextSpider.sinks) snks.add(i);
        for (int i : nextSpider.sources) srcs.add(i);
        for (Integer i : nextSpider.posEndpts) {
            if (negEndpts.contains(i)) nEndpts.remove(i);
            else pEndpts.add(i);
        }
        for (Integer i : nextSpider.negEndpts) {
            if (posEndpts.contains(i)) pEndpts.remove(i);
            else nEndpts.add(i);
        }
        frame.setLabelRight(girthInfo, 1, false);
        ArrayList<GridPolynomial> nPolys = new ArrayList<GridPolynomial>();
        ArrayList<Grid> nGrids = new ArrayList<Grid>();
        ArrayList<ArrayList<Integer>> nPaths = new ArrayList<ArrayList<Integer>>();
        getNewPolynomials(nextSpider, pEndpts, nEndpts, nPolys, nGrids, nPaths);
        posEndpts = pEndpts;
        negEndpts = nEndpts;
        sources = srcs;
        sinks = snks;
        polys = nPolys;
        grids = nGrids;
        paths = nPaths;
        
        //output();
        removePieces();
        combinePolynomials();
        //output();
        
        //nowCrash();
    }

    private void getNewPolynomials(SpiderPolynomial nextSpider, ArrayList<Integer> pEndpts, 
            ArrayList<Integer> nEndpts, ArrayList<GridPolynomial> nPolys, 
            ArrayList<Grid> nGrids, ArrayList<ArrayList<Integer>> nPaths) {
        int i = 0;
        while (i < polys.size()) {
            GridPolynomial pol = polys.get(i);
            for (GridPolynomial nPol : nextSpider.polys) {
                Grid nGrid = combineGrid(grids.get(pol.getGrid()), nextSpider.grids.get(nPol.getGrid()),
                        pEndpts, nEndpts, nPaths, nextSpider);
                Polynomial cPol = pol.getPolynomial().multiply(nPol.getPolynomial());
                GridPolynomial combPoly = new GridPolynomial(cPol, getGridNumber(nGrid, nGrids));
                addPolynomial(combPoly, nPolys);
            }
            i++;
        }
    }

    private Grid combineGrid(Grid grid, Grid nGrid, ArrayList<Integer> pEndpts, 
            ArrayList<Integer> nEndpts, ArrayList<ArrayList<Integer>> nPaths, 
            SpiderPolynomial nextSpider) {
        ArrayList<ArrayList<Integer>> newPaths = new ArrayList<ArrayList<Integer>>();
        for (int i : grid.getPaths()) {
            ArrayList<Integer> cpath = new ArrayList<Integer>();
            for (int y : paths.get(i)) cpath.add(y);
            newPaths.add(cpath);
        }
        for (int j : nGrid.getPaths()) {
            ArrayList<Integer> cpath = new ArrayList<Integer>();
            for (int y : nextSpider.paths.get(j)) cpath.add(y);
            newPaths.add(cpath);
        }
        for (int e : nextSpider.posEndpts) if (!pEndpts.contains(e)) combinePaths(newPaths, e);
        for (int e : nextSpider.negEndpts) if (!nEndpts.contains(e)) combinePaths(newPaths, e);
        Grid ngrid = new Grid();
        for (ArrayList<Integer> npth : newPaths) {
            while (npth.size() > 4) npth.remove(2);
            int p = getPathNumber(npth, nPaths);
            ngrid.addPath(p);
        }
        return ngrid;
    }

    private void combinePaths(ArrayList<ArrayList<Integer>> newPaths, int e) {
        int i = 0;
        int ffound = -1;
        int sfound = -1;
        while (i < newPaths.size() && (ffound < 0 || sfound < 0)) {
            ArrayList<Integer> path = newPaths.get(i);
            if (path.get(0) == e) ffound = i;
            if (path.get(path.size()-2) == e) sfound = i;
            i++;
        }
        if (ffound >= 0 && sfound >= 0) {
            if (ffound != sfound) {
                ArrayList<Integer> fpath = newPaths.get(ffound);
                ArrayList<Integer> spath = newPaths.get(sfound);
                ArrayList<Integer> npath = new ArrayList<Integer>();
                for (int u : spath) npath.add(u);
                for (int u : fpath) npath.add(u);
                newPaths.remove(fpath);
                newPaths.remove(spath);
                newPaths.add(npath);
            }
        }
    }

    private int getPathNumber(ArrayList<Integer> npth, ArrayList<ArrayList<Integer>> pths) {
        boolean found = false;
        int i = 0;
        while (!found && i < pths.size()) {
            if (samePath(npth, pths.get(i))) found = true;
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

    private int getGridNumber(Grid nGrid, ArrayList<Grid> nGrids) {
        Collections.sort(nGrid.getPaths());
        boolean found = false;
        int i = 0;
        while (!found && i < nGrids.size()) {
            Grid cGrid = nGrids.get(i);
            if (sameGrid(nGrid, cGrid)) found = true;
            else i++;
        }
        if (found) return i;
        nGrids.add(nGrid);
        return nGrids.size()-1;
    }

    private void addPolynomial(GridPolynomial combPoly, ArrayList<GridPolynomial> nPolys) {
        boolean found = false;
        int i = 0;
        while (!found && i < nPolys.size()) {
            GridPolynomial cand = nPolys.get(i);
            if (cand.getGrid() == combPoly.getGrid()) {
                found = true;
                cand.setPolynomial(cand.getPolynomial().add(combPoly.getPolynomial()));
                if (cand.getPolynomial().isZero()) nPolys.remove(cand);
            }
            else i++;
        }
        if (!found) nPolys.add(combPoly);
    }

    private boolean sameGrid(Grid nGrid, Grid cGrid) {
        if (nGrid.getPaths().size() != cGrid.getPaths().size()) return false;
        boolean same = true;
        int i = 0;
        int t = nGrid.getPaths().size();
        while (same && i < t) {
            if (!Objects.equals(nGrid.getPaths().get(i), cGrid.getPaths().get(i))) same = false;
            else i++;
        }
        return same;
    }

    private void removePieces() {
        int i = grids.size()-1;
        while (i >= 0) {
            Grid grid = grids.get(i);
            ArrayList<Integer> digon = containsDigon(grid);
            if (digon != null) {
                removeDigon(grid, digon, i);
                for (GridPolynomial poly : polys) {
                    if (poly.getGrid() == i) {
                        poly.setPolynomial(poly.getPolynomial().multiply(polyTwo));
                    }
                }
            }
            else {
                ArrayList<Integer> square = containsSquare(grid);
                if (square != null) {
                    int ng = removeSquare(grid, square, i);
                    int j = polys.size()-1;
                    while (j >= 0) {
                        if (polys.get(j).getGrid() == i) {
                            GridPolynomial nPol = new GridPolynomial(polys.get(j).getPolynomial(), ng);
                            polys.add(nPol);
                        }
                        j--;
                    }
                    i = grids.size()-1;
                }
                else {
                    ArrayList<Integer> theCircles = getCircles(grid);
                    if (!theCircles.isEmpty()) {
                        Polynomial factor = powerOf(polyThr, theCircles.size());
                        for (GridPolynomial poly : polys) {
                            if (poly.getGrid() == i) {
                                poly.setPolynomial(poly.getPolynomial().multiply(factor));
                            }
                        }
                    }
                    i--;
                }
            }
        }
    }
    
    private Polynomial powerOf(Polynomial poly, int pwr) {
        Polynomial pol = new Polynomial(new String[] {"q"}, BigInteger.ONE, new int[] {0});
        while (pwr > 0) {
            pol = pol.multiply(poly);
            pwr--;
        }
        return pol;
    }
    
    private ArrayList<Integer> containsDigon(Grid grid) {
        int i = 0;
        while (i < sources.size()) {
            ArrayList<Integer> start = startWith(sources.get(i), grid.getPaths());
            if (start.size() == 3) {
                if (sameEndPoint(start.get(0), start.get(1))) 
                    return listWith(start.get(0), start.get(1));
                if (sameEndPoint(start.get(0), start.get(2))) 
                    return listWith(start.get(0), start.get(2));
                if (sameEndPoint(start.get(1), start.get(2))) 
                    return listWith(start.get(1), start.get(2));
            }
            i++;
        }
        return null; // this means there is no digon
    }

    private boolean sameEndPoint(int i, int j) {
        int eOne = paths.get(i).get(2);
        int eTwo = paths.get(j).get(2);
        return eOne == eTwo;
    }
    
    private ArrayList<Integer> listWith(int i, int j) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(i);
        list.add(j);
        return list;
    }
    
    private ArrayList<Integer> startWith(int k, ArrayList<Integer> thePaths) {
        ArrayList<Integer> start = new ArrayList<Integer>();
        for (int y : thePaths) {
            ArrayList<Integer> path = paths.get(y);
            if (path.get(0) == k) start.add(y);
        }
        return start;
    }
    
    private void removeDigon(Grid grid, ArrayList<Integer> digon, int i) {
        int source = paths.get(digon.get(0)).get(0);
        int sink = paths.get(digon.get(0)).get(2);
        int fpath = pathStartsWith(grid.getPaths(), source, digon);
        int spath = pathEndsWith(grid.getPaths(), sink, digon);
        Grid nGrid = new Grid();
        for (int u : grid.getPaths()) {
            if (!digon.contains(u) && fpath != u && spath != u) nGrid.addPath(u);
        }
        if (fpath == spath) { // add a circle
            ArrayList<Integer> circle = new ArrayList<Integer>();
            circle.add(source);
            circle.add(0);
            circle.add(source);
            circle.add(0);
            int c = getPathNumber(circle, paths);
            nGrid.addPath(c);
        }
        else {
            ArrayList<Integer> path = new ArrayList<Integer>();
            path.add(paths.get(spath).get(0));
            path.add(paths.get(spath).get(1));
            path.add(paths.get(fpath).get(2));
            path.add(paths.get(fpath).get(3));
            int p = getPathNumber(path, paths);
            nGrid.addPath(p);
        }
        Collections.sort(nGrid.getPaths());
        grids.set(i, nGrid);
    }
    
    private int pathStartsWith(ArrayList<Integer> thePaths, int source, ArrayList<Integer> digon) {
        for (int i : thePaths) {
            if (!digon.contains(i)) {
                ArrayList<Integer> path = paths.get(i);
                if (path.get(0) == source) return i;
            }
        }
        return -1; // shouldn't get here
    }
    
    private int pathEndsWith(ArrayList<Integer> thePaths, int sink, ArrayList<Integer> digon) {
        for (int i : thePaths) {
            if (!digon.contains(i)) {
                ArrayList<Integer> path = paths.get(i);
                if (path.get(2) == sink) return i;
            }
        }
        return -1; // shouldn't get here
    }
    
    private int removeSquare(Grid grid, ArrayList<Integer> square, int i) {
        ArrayList<Integer> sourceOne = new ArrayList<Integer>();
        ArrayList<Integer> sourceTwo = new ArrayList<Integer>();
        int sOne = paths.get(square.get(0)).get(0);
        sourceOne.add(square.get(0));
        for (int j = 1; j < 4; j++) {
            int sTwo = paths.get(square.get(j)).get(0);
            if (sOne == sTwo) sourceOne.add(square.get(j));
            else sourceTwo.add(square.get(j));
        }
        int sTwo = paths.get(sourceTwo.get(0)).get(0);
        int sThr = paths.get(square.get(0)).get(2);
        ArrayList<Integer> sinkOne = new ArrayList<Integer>();
        ArrayList<Integer> sinkTwo = new ArrayList<Integer>();
        sinkOne.add(square.get(0));
        for (int j = 1; j < 4; j++) {
            int sFou = paths.get(square.get(j)).get(2);
            if (sThr == sFou) sinkOne.add(square.get(j));
            else sinkTwo.add(square.get(j));
        }
        int sFou = paths.get(sinkTwo.get(0)).get(2);
        sOne = pathStartsWith(grid.getPaths(), sOne, sourceOne);
        sTwo = pathStartsWith(grid.getPaths(), sTwo, sourceTwo);
        sThr = pathEndsWith(grid.getPaths(), sThr, sinkOne);
        sFou = pathEndsWith(grid.getPaths(), sFou, sinkTwo);
        Grid fGrid = new Grid();
        Grid sGrid = new Grid();
        for (int u : grid.getPaths()) {
            if (u != sOne && u != sTwo && u != sThr && u != sFou) {
                if (!sourceOne.contains(u) && !sourceTwo.contains(u) &&
                        !sinkOne.contains(u) && !sinkTwo.contains(u)) {
                    fGrid.addPath(u);
                    sGrid.addPath(u);
                }
            }
        }
        combinePaths(fGrid, sOne, sTwo, sThr, sFou);
        combinePaths(sGrid, sOne, sTwo, sFou, sThr);
        grids.set(i, fGrid);
        return getGridNumber(sGrid, grids);
    }
    
    private void combinePaths(Grid grid, int sOne, int sTwo, int sThr, int sFou) {
        ArrayList<Integer> pathOne = new ArrayList<Integer>();
        ArrayList<Integer> pathTwo = new ArrayList<Integer>();
        pathOne.add(paths.get(sThr).get(0));
        pathOne.add(paths.get(sThr).get(1));
        pathOne.add(paths.get(sOne).get(2));
        pathOne.add(paths.get(sOne).get(3));
        pathTwo.add(paths.get(sFou).get(0));
        pathTwo.add(paths.get(sFou).get(1));
        pathTwo.add(paths.get(sTwo).get(2));
        pathTwo.add(paths.get(sTwo).get(3));
        int po = getPathNumber(pathOne, paths);
        int pt = getPathNumber(pathTwo, paths);
        grid.addPath(po);
        grid.addPath(pt);
        Collections.sort(grid.getPaths());
    }
    
    private ArrayList<Integer> containsSquare(Grid grid) {
        ArrayList<Integer> thePaths = thePathsFrom(grid);
        ArrayList<ArrayList<Integer>> thesinks = new ArrayList<ArrayList<Integer>>();
        for (int source : sources) {
            ArrayList<Integer> snks = new ArrayList<Integer>();
            snks.add(source);
            int k = 0;
            while (k < thePaths.size() && snks.size() < 4) {
                ArrayList<Integer> path = paths.get(thePaths.get(k));
                if (path.get(0) == source && path.get(2) < 0) {
                    if (!snks.contains(path.get(2))) snks.add(path.get(2));
                }
                k++;
            }
            thesinks.add(snks);
        }
        int i = 0;
        while (i < thesinks.size()-1) {
            int j = i + 1;
            while (j < thesinks.size()) {
                ArrayList<Integer> ovlp = overlap(thesinks.get(i), thesinks.get(j));
                if (ovlp.size() >= 2) {
                    if (ovlp.size() == 3) System.out.println("Happens");
                    ovlp.add(0, thesinks.get(i).get(0));
                    ovlp.add(1, thesinks.get(j).get(0));
                    return theSquare(ovlp, thePaths);
                }
                j++;
            }
            i++;
        }
        return null;
    }

    private ArrayList<Integer> thePathsFrom(Grid grid) {
        ArrayList<Integer> thePaths = new ArrayList<Integer>();
        for (int i = 0; i < grid.getPaths().size(); i++) {
            ArrayList<Integer> path = paths.get(grid.getPaths().get(i));
            if (path.get(0) < 0 && path.get(2) < 0) thePaths.add(grid.getPaths().get(i));
        }
        return thePaths;
    }
    
    private ArrayList<Integer> theSquare(ArrayList<Integer> ovlp, ArrayList<Integer> pths) {
        ArrayList<Integer> thePaths = new ArrayList<Integer>();
        for (int k : pths) {
            ArrayList<Integer> path = paths.get(k);
            if (Objects.equals(path.get(0), ovlp.get(0)) || Objects.equals(path.get(0), ovlp.get(1))) {
                if (Objects.equals(path.get(2), ovlp.get(2)) || Objects.equals(path.get(2), ovlp.get(3))) {
                    thePaths.add(k);
                }
            } 
        }
        return thePaths;
    }

    private ArrayList<Integer> overlap(ArrayList<Integer> fir, ArrayList<Integer> sec) {
        ArrayList<Integer> ovlap = new ArrayList<Integer>();
        for (int i : fir) if (sec.contains(i)) ovlap.add(i);
        return ovlap;
    }

    private ArrayList<Integer> getCircles(Grid grid) {
        ArrayList<Integer> circles = new ArrayList<Integer>();
        int i = grid.getPaths().size()-1;
        while (i >= 0) {
            ArrayList<Integer> path = paths.get(grid.getPaths().get(i));
            if (Objects.equals(path.get(0), path.get(2))) {
                circles.add(grid.getPaths().get(i));
                grid.getPaths().remove(i);
            }
            i--;
        }
        return circles;
    }

    private void combinePolynomials() {
        int i = polys.size()-1;
        while (i > 0) {
            int j = i-1;
            Grid fGrid = grids.get(polys.get(j).getGrid());
            Grid sGrid = grids.get(polys.get(i).getGrid());
            if (sameGrid(fGrid, sGrid)) {
                Polynomial pol = polys.get(j).getPolynomial().add(polys.get(i).getPolynomial());
                polys.remove(i);
                if (pol.isZero()) {
                    polys.remove(j);
                    i--;
                }
                else polys.get(j).setPolynomial(pol);
            }
            i--;
        }
    }

    private void nowCrash() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
