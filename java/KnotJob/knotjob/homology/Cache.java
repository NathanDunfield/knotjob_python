/*

Copyright (C) 2020 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import java.util.Objects;

/**
 *
 * @author dirk
 */
public class Cache {
    
    protected final ArrayList<Integer> dotPts;
    protected ArrayList<ArrayList<Integer>> paths;
    protected final ArrayList<Diagram> diagrams;
    
    public Cache(int[] ends, int change) {
        dotPts = new ArrayList<Integer>(2);
        paths = new ArrayList<ArrayList<Integer>>(4);
        diagrams = new ArrayList<Diagram>(2);
        dotPts.add(ends[0+change]);
        dotPts.add(ends[2+change]);
        paths.add(newPath(ends[0+change],ends[1-change]));
        paths.add(newPath(ends[2+change],ends[3-change]));
        paths.add(newPath(ends[0+3*change],ends[3-3*change]));
        paths.add(newPath(ends[2-change],ends[1+change]));
        diagrams.add(new Diagram(0,1));
        diagrams.add(new Diagram(2,3));
    }
    
    public Cache(ArrayList<Integer> pEndpts) {
        dotPts = new ArrayList<Integer>(pEndpts.size());
        //dotPwrs = new ArrayList<Integer>(pEndpts.size());
        paths = new ArrayList<ArrayList<Integer>>();
        diagrams = new ArrayList<Diagram>();
        //int j = 1;
        for (int p : pEndpts) {
            dotPts.add(p);
        }
    }

    public Integer getPath(int i, int j) {
        return paths.get(i).get(j);
    }
    
    private ArrayList<Integer> newPath(int a, int b) {
        ArrayList<Integer> path = new ArrayList<Integer>(2);
        path.add(a);
        path.add(b);
        return path;
    }

    public void output() {
        System.out.println("Dots : "+dotPts);//+" "+dotPwrs);
        System.out.println("Paths : "+paths);
        System.out.println("Diagrams");
        for (int i = 0; i < diagrams.size(); i++) {
            System.out.print(i+" ");
            diagrams.get(i).output();
        }
    }

    public int diagramSize() {
        return diagrams.size();
    }

    public Diagram getDiagram(int i) {
        return diagrams.get(i);
    }
    
    public int getDiagNumber(Diagram nDiag) {
        Collections.sort(nDiag.paths);
        Collections.sort(nDiag.circles);
        boolean found = false;
        int i = 0;
        while (!found && i < diagrams.size()) {
            Diagram cDiag = diagrams.get(i);
            if (sameDiag(nDiag,cDiag)) found = true;
            else i++;
        }
        if (found) return i;
        diagrams.add(nDiag);
        return diagrams.size()-1;
    }
    
    private boolean sameDiag(Diagram nDiag, Diagram cDiag) {
        if (nDiag.paths.size() != cDiag.paths.size() || nDiag.circles.size() != cDiag.circles.size()) return false;
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

    public ArrayList<Integer> getPaths(int i) {
        return paths.get(i);
    }
    
    public int getPathNumber(ArrayList<Integer> npth) {
        boolean found = false;
        int i = 0;
        while (!found && i < paths.size()) {
            if (samePath(npth,paths.get(i))) found = true;
            else i++;
        }
        if (found) return i;
        paths.add(npth);
        return paths.size()-1;
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

    public boolean dotsContain(int dot) {
        return dotPts.contains(dot);
    }

    public int getDot(int i) {
        return dotPts.get(i);
    }

    public ArrayList<Integer> getPts() {
        return dotPts;
    }
}
