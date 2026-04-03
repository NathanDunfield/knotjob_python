/*

Copyright (C) 2019-21 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.links;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Dirk
 */
public class Link {
    
    private final int[] crossings;
    private final int[][] paths;
    private final int unlinkComp;
    private final ArrayList<int[]> orientation;
    private final ArrayList<ArrayList<Integer>> splitComponents;
    private final int basepoint;
    
    public Link(int[] crss, int[][] pths) {
        crossings = crss;
        paths = pths;
        unlinkComp = 0;
        basepoint = 0;
        orientation = new ArrayList<int[]>();
        getOrientationFromScratch();
        splitComponents = new ArrayList<ArrayList<Integer>>();
        getSplitComps();
    }

    public Link(int[] crss, int[][] pths, int uc, String orien) {
        crossings = crss;
        paths = pths;
        unlinkComp = uc;
        basepoint = 0;
        orientation = new ArrayList<int[]>();
        if (orien == null || "".equals(orien)) getOrientationFromScratch();
        else getOrientationFrom(orien);
        splitComponents = new ArrayList<ArrayList<Integer>>();
        getSplitComps();
    }
    
    public Link(int[] crssings, int[][] pths, ArrayList<int[]> newOr, int comps) {
        crossings = crssings;
        paths = pths;
        unlinkComp = comps;
        basepoint = 0;
        orientation = newOr;
        splitComponents = new ArrayList<ArrayList<Integer>>();
        getSplitComps();
    }

    public Link(int[] crssings, int[][] pths, ArrayList<int[]> newOr, int comps, int base) {
        crossings = crssings;
        paths = pths;
        unlinkComp = comps;
        basepoint = base;
        orientation = newOr;
        splitComponents = new ArrayList<ArrayList<Integer>>();
        getSplitComps();
    }
    
    public Link(int uncomps) {
        crossings = new int[0];
        paths = new int[0][4];
        unlinkComp = uncomps;
        basepoint = 0;
        orientation = new ArrayList<int[]>();
        splitComponents = new ArrayList<ArrayList<Integer>>();
    }
    
    private void getOrientationFromScratch() {
        int[][] vstdPaths = new int[paths.length][4];
        boolean cont = true;
        while (cont) {
            int[] firstZero = firstZero(vstdPaths);
            if (firstZero[0] == -1) cont = false;
            else {
                orientation.add(firstZero);
                runAround(firstZero,vstdPaths);
            }
        }
    }

    private void getOrientationFrom(String orien) {
        boolean keepgoing = true;
        while (keepgoing) {
            int[] or = new int[2];
            int a = orien.indexOf(',');
            or[0] = Integer.parseInt(orien.substring(0,a));
            orien = orien.substring(a+1);
            int b = orien.indexOf(',');
            if (b >= 0) {
                or[1] = Integer.parseInt(orien.substring(0, b));
                orien = orien.substring(b+1);
            }
            else {
                keepgoing = false;
                or[1] = Integer.parseInt(orien);
            }
            orientation.add(or);
        }
    }
    
    public int compOf(int i) {
        ArrayList<ArrayList<Integer>> comps = this.getComponents();
        boolean found = false;
        int j = 0;
        while (!found) {
            if (comps.get(j).contains(i)) found = true;
            else j++;
        }
        return j;
    }
    
    public ArrayList<ArrayList<Integer>> getComponents() {
        ArrayList<ArrayList<Integer>> comps = new ArrayList<ArrayList<Integer>>();
        for (int[] start : orientation) {
            ArrayList<Integer> inthiscomp = new ArrayList<Integer>();
            int[] runner = new int[2];
            runner[0] = start[0];
            runner[1] = start[1];
            inthiscomp.add(getPath(runner[0],runner[1]));
            boolean cont = true;
            while (cont) {
                runner[1] = getOutOf(runner[0],runner[1]);
                runner = nextPosition(runner);
                int check = getPath(runner[0],runner[1]);
                if (inthiscomp.contains(check)) cont = false;
                else inthiscomp.add(check);
            }
            comps.add(inthiscomp);
        }
        return comps;
    }
    
    public ArrayList<int[]> getOrientationsOfCrossings() {
        ArrayList<int[]> allDudes = new ArrayList<int[]>();
        for (int i = 0; i < crossings.length; i++) allDudes.add(new int[]{-1, -1});
        for (int i = 0; i < relComponents(); i++) {
            ArrayList<int[]> orients = getOrientationsOfComponent(i);
            for (int j = 0; j < orients.size()-1; j++) {
                int[] ors = orients.get(j);
                int[] dude = allDudes.get(ors[0]);
                if (dude[0] == -1) dude[0] = ors[1];
                else dude[1] = ors[1];
            }
        }
        return allDudes;
    }
    
    public ArrayList<int[]> getOrientationsOfComponent(int i) {
        ArrayList<int[]> orients = new ArrayList<int[]>();
        if (i > orientation.size()) return null;
        int start = orientation.get(i)[0];
        int startPoint = orientation.get(i)[1];
        orients.add(new int[] {start, startPoint});
        int adder = 2;
        if (crossings[i]%2 == 0) {
            if (startPoint % 2 == 0) adder = 1;
            else adder = 3;
        }
        int point = (startPoint+adder)%4;
        int next = start;
        int check;
        do {
            int[] nextPoint = nextPoint(point, next);
            orients.add(nextPoint);
            next = nextPoint[0];
            check = nextPoint[1];
            adder = 2;
            if (crossings[next]%2 == 0) {
                if (check % 2 == 0) adder = 1;
                else adder = 3;
            }
            point = (check+adder)%4;
        } while (check != startPoint || next != start);
        return orients;
    }
    
    private int[] nextPoint(int point, int next) {
        int p = paths[next][point];
        boolean found = false;
        int i = 0;
        int j = 0;
        while (!found) {
            j = 0;
            while (!found && j < 4) {
                if (i != next || j != point) {
                    if (paths[i][j] == p) found = true;
                }
                if (!found) j++;
            }
            if (!found) i++;
        }
        return new int[] {i, j};
    }
    
    public ArrayList<Integer> getNonReducedCrossings() {
        ArrayList<Integer> nonReduced = new ArrayList<Integer>();
        ArrayList<ArrayList<Integer[]>> discs = this.getDiscs();
        for (ArrayList<Integer[]> disc : discs) {
            ArrayList<Integer> indisc = new ArrayList<Integer>();
            for (Integer[] di : disc) {
                if (indisc.contains(di[0])) nonReduced.add(di[0]);
                else indisc.add(di[0]);
            }
        }
        return nonReduced;
    }
    
    public Link graphicalReduced() {
        ArrayList<Integer> nonReduced = this.getNonReducedCrossings();
        int[] crs = new int[crossings.length+4*nonReduced.size()];
        int[][] pts = new int[paths.length+4*nonReduced.size()][4];
        ArrayList<int[]> newOr = new ArrayList<int[]>();
        for (int[] or : this.orientation) newOr.add(or);
        for (int i = 0; i < crossings.length; i++) {
            crs[i] = crossings[i];
            System.arraycopy(paths[i], 0, pts[i], 0, 4);
        }
        int n = crossings.length;
        for (int i = 0; i < nonReduced.size(); i++) {
            pts[nonReduced.get(i)][0] = 2*n + 1;
            pts[nonReduced.get(i)][1] = 2*n + 2;
            pts[nonReduced.get(i)][2] = 2*n + 3;
            pts[nonReduced.get(i)][3] = 2*n + 4;
            crs[n] = -1;
            crs[n+1] = 1;
            crs[n+2] = -1;
            crs[n+3] = 1;
            int[] nwor = new int[2];
            nwor[0] = n;
            nwor[1] = 1;
            newOr.add(nwor);
            pts[n][0] = paths[nonReduced.get(i)][0];
            pts[n][1] = 2*n+5;
            pts[n][2] = 2*n+1;
            pts[n][3] = 2*n+8;
            pts[n+1][1] = paths[nonReduced.get(i)][1];
            pts[n+1][0] = 2*n+5;
            pts[n+1][2] = 2*n+6;
            pts[n+1][3] = 2*n+2;
            pts[n+2][2] = paths[nonReduced.get(i)][2];
            pts[n+2][1] = 2*n+6;
            pts[n+2][0] = 2*n+3;
            pts[n+2][3] = 2*n+7;
            pts[n+3][3] = paths[nonReduced.get(i)][3];
            pts[n+3][1] = 2*n+4;
            pts[n+3][2] = 2*n+7;
            pts[n+3][0] = 2*n+8;
            n = n + 4;
        }
        return new Link(crs,pts,newOr, this.unlinkComp, this.basepoint);
    }
    
    private ArrayList<ArrayList<Integer>> getComponentCrossings() {
        ArrayList<ArrayList<Integer>> crscomps = new ArrayList<ArrayList<Integer>>();
        for (int[] start : orientation) {
            ArrayList<Integer> inthiscomp = new ArrayList<Integer>();
            int[] runner = new int[2];
            runner[0] = start[0];
            runner[1] = start[1];
            boolean cont = true;
            while (cont) {
                runner[1] = getOutOf(runner[0],runner[1]);
                runner = nextPosition(runner);
                if (!inthiscomp.contains(runner[0])) inthiscomp.add(runner[0]);
                cont = !(runner[0] == start[0] & runner[1] == start[1]);
            }
            crscomps.add(inthiscomp);
        }
        return crscomps;
    }
    
    private void getSplitComps() {
        ArrayList<ArrayList<Integer>> crscomps = this.getComponentCrossings();
        for (ArrayList<Integer> comp : crscomps) splitComponents.add(comp);
        int i = crscomps.size()-1;
        while (i > 0) {
            int k = 0;
            boolean canStay = true;
            while (canStay && k < splitComponents.get(i).size()) {
                int cr = splitComponents.get(i).get(k);
                int j = 0;
                while (canStay && j < i) {
                    if (splitComponents.get(j).contains(cr)) canStay = false;
                    else j++;
                }
                if (!canStay) {
                    for (int crs : splitComponents.get(i)) if (!splitComponents.get(j).contains(crs)) splitComponents.get(j).add(crs);
                    splitComponents.remove(i);
                }
                k++;
            }
            i--;
        }
    }

    private int[] firstZero(int[][] vstdPaths) {
        int i = 0;
        int j = 0;
        boolean found = false;
        while (!found && i < vstdPaths.length) {
            j = 4;
            while (!found && j > 0) {
                if (vstdPaths[i][j%4] == 0) found = true;
                else j--;
            }
            if (!found) i++;
        }
        int[] pos = new int[2];
        if (found) {
            pos[0] = i;
            pos[1] = j%4;
        }
        else pos[0] = -1;
        return pos;
    }

    private void runAround(int[] firstZero, int[][] vstdPaths) {
        int[] runner = new int[2];
        runner[0] = firstZero[0];
        runner[1] = firstZero[1];
        boolean cont = true;
        while (cont) {
            vstdPaths[runner[0]][runner[1]] = 1;
            runner[1] = getOutOf(runner[0],runner[1]);
            vstdPaths[runner[0]][runner[1]] = -1;
            runner = nextPosition(runner);
            if (runner[0] == firstZero[0] && runner[1] == firstZero[1]) cont = false;
        }
    }

    private int getOutOf(int i, int j) {
        boolean odd = true;
        if (crossings[i] % 2 == 0) odd = false;
        if (odd) j = (j+2)% 4;
        else {
            if (j % 2 == 0) j++;
            else j--;
        }
        return j;
    }

    private int[] nextPosition(int[] runner) {
        int u = 0;
        int v = 0;
        int search = paths[runner[0]][runner[1]];
        boolean found = false;
        while (!found) {
            v = 0;
            while (!found && v < 4) {
                if (u != runner[0] || v != runner[1]) {
                    if (paths[u][v] == search) found = true;
                    else v++;
                }
                else v++;
            }
            if (!found) u++;
        }
        int[] dude = new int[2];
        dude[0] = u;
        dude[1] = v;
        return dude;
    }
    
    private int[] orNextPosition(int[] runner) {
        int[] dude = new int[2];
        dude[0] = runner[0];
        dude[1] = runner[1];
        if (crossings[runner[0]] % 2 == 0) {
            if (runner[1] % 2 == 0) dude[1]++;
            else dude[1]--;
        }
        else dude[1] = (dude[1]+2)%4;
        return nextPosition(dude);
    }
    
    public int basepoint() {
        return basepoint;
    }
    
    public int basecomponent() {
        ArrayList<ArrayList<Integer>> comps = this.getComponents();
        if (paths.length == 0) return 0;
        int b = paths[paths.length-1][basepoint];
        boolean found = false;
        int i = 0;
        while (!found) {
            if (comps.get(i).contains(b)) found = true;
            else i++;
        }
        return i;
    }
    
    public int components() {
        return orientation.size()+unlinkComp;
    }
    
    public int unComponents() {
        return unlinkComp;
    }
    
    public int relComponents() {
        return orientation.size();
    }
    
    public int[] orientation(int i) {
        return orientation.get(i);
    }
    
    public int getComponentPath(int i) {
        return paths[orientation.get(i)[0]][orientation.get(i)[1]];
    }
    
    public int crossingLength() {
        return crossings.length;
    }
    
    public int getCross(int i) {
        return crossings[i];
    }
    
    public int[] getCrossings() {
        return crossings;
    }
    
    public int[] cloneCrossings() {
        int[] clone = new int[crossings.length];
        System.arraycopy(crossings, 0, clone, 0, crossings.length);
        return clone;
    }
    
    public int[][] clonePaths() {
        int[][] clone = new int[paths.length][4];
        for (int i = 0; i < paths.length; i++) {
            System.arraycopy(paths[i], 0, clone[i], 0, 4);
        }
        return clone;
    }
    
    public Link cloneLink() {
        return new Link(cloneCrossings(), clonePaths(), cloneOr(), this.unlinkComp);
    }
    
    public int getPath(int i, int j) {
        return paths[i][j];
    }
    
    public void setPath(int i, int j, int v) {
        paths[i][j] = v;
    }
    
    public int[] getPath(int i) {
        return paths[i];
    }
    
    public int[][] getPaths() {
        return paths;
    }
    
    public int[] crossingSigns() {
        int[] crs = new int[2];
        int[][] vstdPaths = new int[paths.length][4];
        for (int[] or : orientation) runAround(or,vstdPaths);
        for (int i = 0; i < vstdPaths.length; i++) {
            int factor = 1;
            int[] path = vstdPaths[i];
            if (path[1] != path[2]) factor = -1;
            int summand = factor * crossings[i];
            if (summand > 0) crs[0] = crs[0]+summand;
            else crs[1] = crs[1]-summand;
        }
        return crs;
    }
    
    public int[] allCrossingSigns() {
        int[] crs = new int[paths.length];
        int[][] vstdPaths = new int[paths.length][4];
        for (int[] or : orientation) runAround(or, vstdPaths);
        for (int i = 0; i < vstdPaths.length; i++) {
            int factor = 1;
            int[] path = vstdPaths[i];
            if (path[1] != path[2]) factor = -1;
            crs[i] = factor * crossings[i];
        }
        return crs;
    }
    
    public int crossingNumber() {
        int[] wrt = this.crossingSigns();
        return wrt[0]+wrt[1];
    }
    
    public int writhe() {
        int[] wrt = this.crossingSigns();
        return wrt[0]-wrt[1];
    }
        
    public String crossingsToString() {
        if (crossings.length == 0) return ""+unlinkComp;
        String cross = ""+unlinkComp+","+crossings[0];
        for (int u = 1; u < crossings.length; u++) cross = cross + ","+ crossings[u];
        return cross;
    }
    
    public String orientToString() {
        if (orientation.isEmpty()) return "";
        int[] or = orientation.get(0);
        String orieString = ""+or[0]+","+or[1];
        int i = 1;
        while (i < orientation.size()) {
            or = orientation.get(i);
            orieString = orieString + ","+or[0]+","+or[1];
            i++;
        }
        return orieString;
    }
    
    public String pathToString() {
        if (crossings.length == 0) return "";
        String path = ""+paths[0][0]+","+paths[0][1]+","+paths[0][2]+","+paths[0][3];
        for (int u = 1; u < paths.length; u++) 
            path = path+","+paths[u][0]+","+paths[u][1]+","+paths[u][2]+","+paths[u][3];
        return path;
    }
    
    public String gaussCode() {
        String gauss = "";
        int[][] pats = copyOf(paths);
        ArrayList<ArrayList<Integer>> comps = this.getComponents();
        for (ArrayList<Integer> comp : comps) {
            for (int c : comp) {
                boolean found = false;
                int i = 0;
                int j = 0;
                while (!found) {
                    j = 0;
                    while (!found & j < 4) {
                        if (pats[i][j] == c) found = true;
                        else j++;
                    }
                    if (!found) i++;
                }
                pats[i][j] = 0;
                if (crossings[i]%2 == 0) {
                    if (j == 0) pats[i][1] = 0;
                    if (j == 1) pats[i][0] = 0;
                    if (j == 2) pats[i][3] = 0;
                    if (j == 3) pats[i][2] = 0;
                }
                else{
                    if (j == 0) pats[i][2] = 0;
                    if (j == 1) pats[i][3] = 0;
                    if (j == 2) pats[i][0] = 0;
                    if (j == 3) pats[i][1] = 0;
                }
                if (crossings[i]!=0) {
                    int pos = gaussPos(crossings,i,j);
                    boolean up = (j == 0 | j == 3);
                    boolean plus = false;
                    if (crossings[i] < 0) {
                        if (j == 3 | j == 1) gauss = gauss+"-";
                        else plus = true;
                    }
                    else {
                        if (j == 0 | j == 2) gauss = gauss+"-";
                        else plus = true;
                    }
                    gauss = gauss+String.valueOf(pos);
                    int factor = -1;
                    if (up) factor = 1;
                    for (int t = 1; t < Math.abs(crossings[i]); t++) {
                        gauss = gauss +",";
                        if (plus) {
                            gauss = gauss + "-";
                        }
                        plus = !plus;
                        gauss = gauss + String.valueOf(pos+factor*t);
                    }
                    if (comp.indexOf(c) != comp.size()-1) gauss = gauss+",";
                }
            }
            if (comps.indexOf(comp) != comps.size()-1) gauss = gauss+":";
        }
        return gauss;
    }
    
    private int gaussPos(int[] cross, int i, int j) {
        int pos = 0;
        for (int k = 0; k < i; k++) pos = pos + Math.abs(cross[k]);
        if (j == 1 | j == 2) pos = pos + Math.abs(cross[i]);
        else pos++;
        return pos;
    }
    
    private int[][] copyOf(int[][] orig) {
        int[][] copy = new int[orig.length][4];
        for (int i = 0; i < orig.length; i++) {
            System.arraycopy(orig[i], 0, copy[i], 0, 4);
        }
        return copy;
    }
    
    public String dowkerThistle() { 
        String dowker;
        if (this.components() > 1) return "Not implemented for links";
        int[] checked = new int[paths.length];
        boolean[] beenThere = new boolean[paths.length];
        int[][] pats = copyOf(paths);
        int start = paths[0][0];
        int runner;
        int v;
        if (crossings[0]%2 == 0) v = 1;
        else v = 2;
        runner = paths[0][v];
        int cross = Math.abs(crossings[0]);
        int cros = cross;
        ArrayList<Coefficient> crosses = new ArrayList<Coefficient>();
        for (int y = 1; y <= cross; y++) {
            int z = y;
            if (z%2 == 0 & crossings[0]>0) z = -z;
            Coefficient first = new Coefficient(z,0);
            crosses.add(first);
        }
        pats[0][0] = -1;
        if (crossings[0]%2 == 0) pats[0][1] = 0;
        else pats[0][2] = 0;
        checked[0] = cros;
        beenThere[0] = true;
        while (runner != start) {
            boolean found = false;
            int u = 0;
            v = 0;
            while (!found) {
                int[] pts = pats[u];
                v = 0;
                while (!found & v < 4) {
                    if (pts[v] == runner) found = true;
                    else v++;
                }
                if (!found) u++;
            }
            boolean beenHere = false;
            int here = 0;
            if (beenThere[u]) {
                beenHere = true;
                here = checked[u];
            }
            else {
                cros = cros + Math.abs(crossings[u]);
                checked[u] = cros;
                beenThere[u] = true;
            }
            for (int y = 1; y <= Math.abs(crossings[u]); y++) {
                int z = cross + y;
                if (crossings[u] < 0) {
                    if (v == 1 | v == 3) { //for odd y get undercrossing
                        if (z % 2 == 0 & y % 2 == 0) z = -z;
                    }
                    else { // for even y get undercrossing
                        if (z % 2 == 0 & y % 2 != 0) z = -z;
                    }
                }
                else {
                    if (v == 1 | v == 3) {
                        if (z % 2 == 0 & y % 2 != 0) z = -z;
                    }
                    else { // for even y get undercrossing
                        if (z % 2 == 0 & y % 2 == 0) z = -z;
                    }
                }
                if (!beenHere) { // just get a new entry
                    Coefficient next = new Coefficient(z,0);
                        crosses.add(next);
                }
                else { // more involved
                    boolean opposite = true;
                    switch(v) {
                        case 0 : if (pats[u][3] == -1) opposite = false; break;
                        case 1 : if (pats[u][2] == -1) opposite = false; break;
                        case 2 : if (pats[u][1] == -1) opposite = false; break;
                        case 3 : if (pats[u][0] == -1) opposite = false; break;
                    }
                    if (opposite) {
                        Coefficient next = crosses.get(here-y);
                        next.power = z;
                    }
                    else {
                        Coefficient next = crosses.get(here - Math.abs(crossings[u]) - 1 + y);
                        next.power = z;
                    }
                }
            }
            cross = cross + Math.abs(crossings[u]);
            pats[u][v] = -1;
            switch(v) {
                case 0 : if (crossings[u]%2 == 0) v = 1; else v = 2; break;
                case 1 : if (crossings[u]%2 == 0) v = 0; else v = 3; break;
                case 2 : if (crossings[u]%2 == 0) v = 3; else v = 0; break;
                case 3 : if (crossings[u]%2 == 0) v = 2; else v = 1; break;
            }
            pats[u][v] = 0;
            runner = paths[u][v];
        }
        int run = 3;
        dowker = String.valueOf(crosses.get(0).power);
        while (run < 2 * crosses.size()) {
            boolean found = false;
            int y = 0;
            while (!found) {
                Coefficient ct = crosses.get(y);
                if (Math.abs(ct.entry) == run) found = true;
                if (Math.abs(ct.power) == run) found = true;
                if (!found) y++;
            }
            int str = 0;
            if (crosses.get(y).entry == run ) str = crosses.get(y).power;
            if (crosses.get(y).entry == -run ) str = -crosses.get(y).power;
            if (crosses.get(y).power == run ) str = crosses.get(y).entry;
            if (crosses.get(y).power == -run ) str = -crosses.get(y).entry;
            dowker = dowker + " " + String.valueOf(str);
            run = run + 2;
        }
        return dowker;
    }
    
    public Link cycleLink(int n) {
        int m = crossings.length;
        int[] crs = new int[m];
        int[][] pts = new int[m][4];
        ArrayList<int[]> ors = new ArrayList<int[]>();
        for (int i = 0; i < m; i++) {
            crs[i] = crossings[(n+i)%m];
            System.arraycopy(paths[(n+i)%m], 0, pts[i], 0, 4);
        }
        for (int[] or : orientation)  {
            int[] nor = new int[2];
            nor[0] = (or[0]+m-n)%m;
            nor[1] = or[1];
            ors.add(nor);
        }
        return new Link(crs,pts,ors,this.unlinkComp, this.basepoint);
    }
    
    public int maxGirth() {
        int max = 0;
        ArrayList<Integer> ends = new ArrayList<Integer>();
        for (int j = 0; j < paths.length; j++) {
            for (int i : paths[j]) {
                if (ends.contains((Integer) i)) ends.remove((Integer) i);
                else ends.add(i);
            }
            if (ends.size()> max) max = ends.size();
        }
        return max;
    }

    public int[] totalGirthArray() {
        int[] total = new int[paths.length];
        ArrayList<Integer> ends = new ArrayList<Integer>();
        for (int j = 0; j < paths.length; j++) {
            for (int i : paths[j]) {
                if (ends.contains((Integer) i)) ends.remove((Integer) i);
                else ends.add(i);
            }
            total[j] = ends.size();
        }
        return total;
    }
    
    public int totalGirth() {
        int tot = 0;
        ArrayList<Integer> ends = new ArrayList<Integer>();
        for (int j = 0; j < paths.length; j++) {
            for (int i : paths[j]) {
                if (ends.contains((Integer) i)) ends.remove((Integer) i);
                else ends.add(i);
            }
            tot = tot + ends.size();
        }
        return tot;
    }
    
    public Link breakUp() {
        ArrayList<int[]> orClone = cloneOr();
        int[][] newPaths = breakUpPaths();
        int[] newCrossings = breakUpCrossings(orClone);
        return new Link(newCrossings,newPaths,orClone,unlinkComp,basepoint);
    }
    
    public ArrayList<int[]> cloneOr() {
        ArrayList<int[]> orClone = new ArrayList<int[]>();
        for (int[] or : orientation) {
            int[] nor = new int[2];
            nor[0] = or[0];
            nor[1] = or[1];
            orClone.add(nor);
        }
        return orClone;
    }
    
    private int[] breakUpCrossings(ArrayList<int[]> orClone) {
        int length = crossings.length;
        for (int i = 0; i < crossings.length; i++) {
            int diff = Math.abs(crossings[i]);
            if (diff > 1) length = length + diff - 1;
        }
        int[] breakUp = new int[length];
        int counter = 0;
        for (int i = 0; i < crossings.length; i++) {
            int diff = Math.abs(crossings[i]);
            if (diff <= 1) {
                breakUp[counter] = crossings[i];
                counter++;
            }
            else {
                for (int[] or : orClone) {
                    if (or[0] >= counter) {
                        if (or[0] == counter) {
                            if (or[1] == 1 || or[1] == 2) or[0] = or[0] + diff - 1;
                        }
                        else or[0] = or[0] + diff - 1;
                    }
                }
                int entry = 1;
                if (crossings[i] < 0) entry = -1;
                for (int j = 0; j < diff; j++) {
                    breakUp[counter] = entry;
                    counter++;
                }
            }
        }
        return breakUp;
    }
    
    private int[][] breakUpPaths() {
        int length = crossings.length;
        int n = 2 * length;
        for (int i = 0; i < crossings.length; i++) {
            int diff = Math.abs(crossings[i]);
            if (diff > 1) length = length + diff - 1;
        }
        int[][] breakUpPaths = new int[length][4];
        int counter = 0;
        for (int i = 0; i < crossings.length; i++) {
            int diff = Math.abs(crossings[i]);
            if (diff <= 1) {
                System.arraycopy(paths[i], 0, breakUpPaths[counter], 0, 4);
                counter++;
            }
            else {
                breakUpPaths[counter][0] = paths[i][0];
                breakUpPaths[counter][3] = paths[i][3];
                for (int j = 0; j < diff - 1; j++) {
                    breakUpPaths[counter][1] = n+1;
                    breakUpPaths[counter][2] = n+2;
                    counter++;
                    breakUpPaths[counter][0] = n+1;
                    breakUpPaths[counter][3] = n+2;
                    n = n + 2;
                }
                breakUpPaths[counter][1] = paths[i][1];
                breakUpPaths[counter][2] = paths[i][2];
                counter++;
            }
        }
        return breakUpPaths;
    }
    
    public boolean isReduced() {
        boolean red = true;
        ArrayList<ArrayList<Integer[]>> discs = getDiscs();
        int i = discs.size()-1;
        while (i >= 0 && red) {
            ArrayList<Integer[]> disc = discs.get(i);
            ArrayList<Integer> used = new ArrayList<Integer>();
            for (Integer[] edge : disc) {
                if (used.contains(edge[0])) red = false;
                else used.add(edge[0]);
            }
            i--;
        }
        return red;
    }
    
    public ArrayList<ArrayList<Integer>> splitComponents() {
        return splitComponents;
    }
    
    public ArrayList<ArrayList<Integer[]>> getDiscs() {
        ArrayList<ArrayList<Integer[]>> discs = new ArrayList<ArrayList<Integer[]>>();
        if (crossings.length == 0) return discs;
        boolean[] handled = new boolean[crossings.length*4];
        int start = 0;
        boolean keepgoing = true;
        while (keepgoing) {
            handled[start] = true;
            ArrayList<Integer[]> disc = new ArrayList<Integer[]>();
            Integer[] dentry = discPos(start);
            disc.add(dentry);
            int discrun = nextPos(start);
            while (discrun != start) {
                handled[discrun] = true;
                dentry = discPos(discrun);
                disc.add(dentry);
                discrun = nextPos(discrun);
            }
            discs.add(disc);
            start = nextEntry(handled);
            if (start == -1) keepgoing = false;
        }
        return discs;
    }
    
    private int nextEntry(boolean[] handled) {
        int y = 0;
        boolean found = false;
        while (!found && y < handled.length) {
            if (!handled[y]) found = true;
            else y++;
        }
        if (!found) y = -1;
        return y;
    }
    
    private int nextPos(int start) {
        Integer[] spos = discPos(start);
        boolean found = false;
        int find = paths[spos[0]][spos[1]];
        int u = 0;
        while (!found) {
            if (u != start) {
                Integer[] npos = discPos(u);
                if (find == paths[npos[0]][npos[1]]) found = true;
                else u++;
            }
            else u++;
        }
        if (u % 4 == 0) u = u+3;
        else u--;
        return u;
    }
    
    private Integer[] discPos(int pos) {
        Integer[] dp = new Integer[2];
        dp[0] = pos/4;
        dp[1] = pos%4;
        return dp;
    }
    
    @Override
    public String toString() {
        String string = "Unlink components : "+unlinkComp+"\n";
        string = string+"Crossings : [ ";
        for (int u : crossings) string = string + u+" ";
        string = string+"]\n"+"Paths : [";
        for (int i = 0; i < paths.length; i++) {
            string = string +"[ ";
            for (int j = 0; j < 4; j++) string = string + paths[i][j]+" ";
            string = string +" ] ";
        }
        string = string+"]\n"+"Orientations : ";
        for (int[] or : orientation) string = string+"[ "+or[0]+" "+or[1]+" ] ";
        return string;
    }
    
    public Link mirror() {
        int[] mcrossings = new int[crossings.length];
        for (int i = 0; i < crossings.length; i++) mcrossings[i] = -crossings[i];
        return new Link(mcrossings, paths, orientation, unlinkComp,basepoint);
    }
    
    public Link[] whiteheadDoubles(int twists) {
        Link lnk = breakUp().girthMinimize();
        int n = lnk.crossingLength();
        if (n == 0) return null;
        int t = 0;
        if (twists != 0) t = 1;
        int[] flipper = new int[2*n];
        int[] changer = new int[2*n];
        int[] wpcrossings = new int[4*n+2+t];
        int[] wmcrossings = new int[4*n+2+t];
        int[][] wpaths = new int[4*n+2+t][4];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 4; j++) {
                wpcrossings[4*i+j] = lnk.crossings[i];
                wmcrossings[4*i+j] = lnk.crossings[i];
                if (flipper[lnk.paths[i][j]-1] == 0) flipper[lnk.paths[i][j]-1] = j%2+1;
                else if ((flipper[lnk.paths[i][j]-1] + j)%2 != 0) changer[lnk.paths[i][j]-1] = 1;
            }
            int[] ch = getChanges(changer, lnk.paths[i], n);
            wpaths[4*i] = new int[] {ch[0], ch[1], 4*n+4*i+2, 4*n+4*i+1};
            wpaths[4*i+1] = new int[] {4*n+4*i+4, 4*n+4*i+3, ch[2], 
                                        ch[3]};
            wpaths[4*i+2] = new int[] {4*n+4*i+2, ch[5], 
                                        ch[6], 4*n+4*i+3};
            wpaths[4*i+3] = new int[] {ch[4], 4*n+4*i+1, 4*n+4*i+4,
                                        ch[7]};
        }
        int k = lnk.paths[0][0];
        wpaths[4*n] = new int[] {k, 8*n+1+4*t, 8*n+3, 8*n+4};
        wpaths[4*n+1] = new int[] {8*n+4, 8*n+3, 8*n+2+4*t, k+2*n};
        wpaths[0][0] = 8*n+1;
        wpaths[3][0] = 8*n+2;
        wpcrossings[4*n] = 1;
        wpcrossings[4*n+1] = 1;
        wmcrossings[4*n] = -1;
        wmcrossings[4*n+1] = -1;
        if (t != 0) {
            wpaths[4*n+2] = new int[] {8*n+5, 8*n+1, 8*n+2, 8*n+6};
            wpcrossings[4*n+2] = twists;
            wmcrossings[4*n+2] = twists;
        }
        Link plnk = new Link(wpcrossings, wpaths);
        Link mlnk = new Link(wmcrossings, wpaths);
        return new Link[] {plnk.breakUp().girthMinimize(), mlnk.breakUp().girthMinimize()};
    }
    
    private int[] getChanges(int[] changer, int[] pth, int n) {
        int[] ch = new int[8];
        if (changer[pth[0]-1] != 0) {
            ch[0] = pth[0]+2*n;
            ch[4] = pth[0];
        }
        else {
            ch[0] = pth[0];
            ch[4] = pth[0]+2*n;
        }
        if (changer[pth[1]-1] != 0) {
            ch[1] = pth[1]+2*n;
            ch[5] = pth[1];
        }
        else {
            ch[5] = pth[1]+2*n;
            ch[1] = pth[1];
        }
        if (changer[pth[2]-1] != 0) {
            ch[2] = pth[2]+2*n;
            ch[6] = pth[2];
        }
        else {
            ch[6] = pth[2]+2*n;
            ch[2] = pth[2];
        }
        if (changer[pth[3]-1] != 0) {
            ch[3] = pth[3]+2*n;
            ch[7] = pth[3];
        }
        else {
            ch[7] = pth[3]+2*n;
            ch[3] = pth[3];
        }
        return ch;
    }
    
    public Link girthMinimize() {
        if (crossings.length == 0) return this;
        int[] crssings = new int[crossings.length];
        int[][] pths = new int[paths.length][4];
        ArrayList<int[]> orCrossings = new ArrayList<int[]>();
        for (int[] or : orientation) {
            int[] guy = new int[4];
            System.arraycopy(paths[or[0]], 0, guy, 0, 4);
            orCrossings.add(guy);
        }
        ArrayList<Integer> used = new ArrayList<Integer>();
        ArrayList<Integer> usedPaths = new ArrayList<Integer>();
        crssings[0] = crossings[0];
        for (int i = 0; i < 4; i++) {
            pths[0][i] = paths[0][i];
            usedPaths.add(paths[0][i]);
        }
        used.add(0);
        int k = 1;
        while (k < crssings.length) {
            int candidate = 1;
            int overlap = -1;
            for (int j = 1; j < crssings.length; j++) {
                if (!used.contains(j)) {
                    int newov = overlap(usedPaths,paths[j]); 
                    if (newov > overlap) {
                        overlap = newov;
                        candidate = j;
                    }
                }
            }
            used.add(candidate);
            for (int l = 0; l < 4; l++) {
                crssings[k] = crossings[candidate];
                pths[k][l] = paths[candidate][l];
                usedPaths.add(pths[k][l]);
            }
            k++;
        }
        ArrayList<int[]> newOr = new ArrayList<int[]>();
        for (int i=0; i < orCrossings.size(); i++) {
            int[] orPath = orCrossings.get(i);
            int y = findThisPath(pths,orPath);
            int[] nwOr = new int[2];
            nwOr[0] = y;
            nwOr[1] = orientation.get(i)[1];
            newOr.add(nwOr);
        }
        Link theLink = new Link(crssings,pths,newOr,unlinkComp);
        return theLink;
    }
    
    private int overlap(ArrayList<Integer> usedPaths, int[] path) {
        int lap = 0;
        for (int t : path) if (usedPaths.contains(t)) lap++;
        if (lap != 2) return lap;
        if ((usedPaths.contains(path[0]) & usedPaths.contains(path[2])) | 
                (usedPaths.contains(path[1]) & usedPaths.contains(path[3]))) return 0;
        return 2;
    }

    private int findThisPath(int[][] pths, int[] orPath) {
        boolean found = false;
        int i = 0;
        while (!found) {
            int j = 0;
            boolean same = true;
            while (same && j < 4) {
                if (pths[i][j] != orPath[j]) same = false;
                else j++;
            }
            if (same) found = true;
            else i++;
        }
        return i;
    }

    public Link componentChoice(ArrayList<Integer> comps, ArrayList<Boolean> orient) {
        if (comps.isEmpty()) return null;
        ArrayList<Integer> ncross = new ArrayList<Integer>();
        ArrayList<ArrayList<Integer>> npaths = new ArrayList<ArrayList<Integer>>();
        ArrayList<int[]> norient = new ArrayList<int[]>();
        ArrayList<ArrayList<Integer>> components = getComponents();
        ArrayList<Integer> relPaths = new ArrayList<Integer>();
        for (int i : comps) {
            for (int j : components.get(i)) relPaths.add(j);
            int[] nor = new int[2];
            nor[0] = orientation.get(i)[0];
            int dir = orientation.get(i)[1];
            if (!orient.get(i)) nor[1] = dir;
            else {
                if (crossings[i]%2 == 0) {
                    if (dir % 2 == 0) nor[1] = dir +1;
                    else nor[1] = dir -1;
                }
                else nor[1] = (dir + 2) % 4;
            }
            norient.add(nor);
        }
        for (int i = 0; i < paths.length; i++) {
            ArrayList<Integer> npath = new ArrayList<Integer>();
            for (int j = 0; j < 4; j++) {
                if (relPaths.contains(paths[i][j])) npath.add(paths[i][j]);
            }
            npaths.add(npath);
            ncross.add(crossings[i]);
        }
        adjustOrientation(norient,npaths);
        ArrayList<int[]> realOrient = new ArrayList<int[]>();
        ArrayList<ArrayList<Integer>> newpaths = new ArrayList<ArrayList<Integer>>();
        int u = npaths.size()-1;
        while (u >= 0) {
            ArrayList<Integer> npath = npaths.get(u);
            if (npath.size() == 4) {
                int e = norient.size()-1;
                while (e >= 0) {
                    int[] or = norient.get(e);
                    if (or[0] == npaths.indexOf(npath)) {
                        or[0] = newpaths.size();
                        norient.remove(e);
                        realOrient.add(or);
                    }
                    e--;
                }
                npaths.remove(u);
                newpaths.add(npath);
            }
            else ncross.remove(u);
            if (npath.isEmpty()) npaths.remove(u);
            u--;
        }
        combinePaths(npaths);
        checkPaths(newpaths,npaths);
        int ul = unlinksIn(npaths);
        reducePaths(newpaths,npaths);
        int[] ncrossings = new int[ncross.size()];
        for (int i = 0; i < ncross.size(); i++) ncrossings[i] = ncross.get(i);
        int[][] nupaths = new int[newpaths.size()][4];
        for (int i = 0; i < newpaths.size(); i++) {
            for (int j = 0; j < 4; j++) nupaths[i][j] = newpaths.get(i).get(j);
        }
        return new Link(ncrossings,nupaths,realOrient,ul).girthMinimize();
    }

    private int unlinksIn(ArrayList<ArrayList<Integer>> npaths) {
        int ul = 0;
        int k = npaths.size()-1;
        while (k >= 0) {
            ArrayList<Integer> npt = npaths.get(k);
            if (!npt.contains(-1)) {
                ul++;
                npaths.remove(k);
            }
            k--;
        }
        return ul;
    }
    
    private void combinePaths(ArrayList<ArrayList<Integer>> npaths) {
        int u = npaths.size()-1;
        while (u >= 1) {
            int v = u - 1;
            ArrayList<Integer> npath = npaths.get(u);
            while (v >= 0) {
                ArrayList<Integer> spath = npaths.get(v);
                if (overlapPaths(npath,spath)) {
                    combinePaths(npath,spath);
                    npaths.remove(spath);
                    u--;
                    v = u;
                }
                v--;
            }
            u--;
        }
    }

    private boolean overlapPaths(ArrayList<Integer> npath, ArrayList<Integer> spath) {
        boolean overlap = false;
        int u = 0;
        while (!overlap && u < npath.size()) {
            if (spath.contains(npath.get(u))) overlap = true;
            else u++;
        }
        return overlap;
    }

    private void combinePaths(ArrayList<Integer> npath, ArrayList<Integer> spath) {
        for (int i : spath) {
            if (!npath.contains(i)) npath.add(i);
        }
    }

    private void checkPaths(ArrayList<ArrayList<Integer>> newpaths, ArrayList<ArrayList<Integer>> npaths) {
        for (ArrayList<Integer> npath : newpaths) {
            for (int j = 0; j < 4; j++) {
                int k = npath.get(j);
                checkValue(k,npaths);
            }
        }
    }
    
    private void checkValue(int k, ArrayList<ArrayList<Integer>> npaths) {
        boolean found = false;
        int i = 0;
        while (!found && i < npaths.size()) {
            ArrayList<Integer> npath = npaths.get(i);
            if (npath.contains(k)) {
                found = true;
                npath.add(-1);
            }
            i++;
        }
        if (!found) {
            ArrayList<Integer> justOne = new ArrayList<Integer>();
            justOne.add(k);
            npaths.add(justOne);
        }
    }
    
    private void reducePaths(ArrayList<ArrayList<Integer>> newpaths, ArrayList<ArrayList<Integer>> npaths) {
        for (ArrayList<Integer> npath : newpaths) {
            for (int j = 0; j < 4; j++) {
                int k = npath.get(j);
                int kk = newValue(k,npaths);
                npath.set(j, kk);
            }
        }
    }

    private int newValue(int k, ArrayList<ArrayList<Integer>> npaths) {
        boolean found = false;
        int i = 0;
        while (!found) {
            ArrayList<Integer> npath = npaths.get(i);
            if (npath.contains(k)) {
                found = true;
                npath.add(-1);
            }
            i++;
        }
        return i;
    }

    private void adjustOrientation(ArrayList<int[]> norient, ArrayList<ArrayList<Integer>> npaths) {
        for (int[] or : norient) {
            int[] nor = new int[2];
            nor[0] = or[0];
            nor[1] = or[1];
            boolean cont = true;
            boolean bad = false;
            while (cont) {
                if (npaths.get(nor[0]).size() == 4) cont = false;
                else {
                    if (crossings[nor[0]] % 2 == 0) {
                        if (nor[1] % 2 == 0) nor[1]++;
                        else nor[1]--;
                    }
                    else nor[1] = (nor[1]+2)% 4;
                    nor = nextPosition(nor);
                    if (nor[0] == or[0] && nor[1] == or[1]) {
                        bad = true;
                        cont = false;
                    }
                }
            }
            if (bad) nor[0] = -1;
            or[0] = nor[0];
            or[1] = nor[1];
        }
        int u = norient.size()-1;
        while (u >= 0) {
            if (norient.get(u)[0] == -1) norient.remove(u);
            u--;
        }
    }

    public Link onlyFirstPart() {
        int[] tot = this.totalGirthArray();
        boolean found = false;
        int i = 0;
        while (!found) {
            if (tot[i] == 0) found = true;
            i++;
        }
        int[][] pths = new int[i][4];
        int[] crs = new int[i];
        for (int j = 0; j < i; j++) {
            System.arraycopy(paths[j], 0, pths[j], 0, 4);
            crs[j] = crossings[j];
        }
        return new Link(crs, pths);
    }
    
    public ArrayList<Link> addTwist(int t) {
        ArrayList<Link> theLinks = new ArrayList<Link>();
        if (crossings.length == 0) return theLinks;
        int[] crssings = new int[crossings.length];
        int[][] pths = new int[paths.length][4];
        ArrayList<Integer> used = new ArrayList<Integer>();
        ArrayList<Integer> usedDisc = new ArrayList<Integer>();
        ArrayList<Integer> theDisc = new ArrayList<Integer>();
        crssings[0] = crossings[0];
        for (int i = 0; i < 4; i++) {
            pths[0][i] = paths[0][i];
            usedDisc.add((Integer) paths[0][i]);
        }
        used.add(0);
        int reached = -1;
        while (used.size() < crossings.length) {
            int candidate = -1;
            int overlap = 5;
            if (reached >= 0) overlap = -1;
            for (int j = 1; j < crossings.length; j++) {
                if (!used.contains(j)) {
                    int ov = discOverlap(usedDisc, paths[j]);
                    if (reached < 0) {// want to minimize overlap
                        if (ov > 0 && ov < overlap) {
                            overlap = ov;
                            candidate = j;
                        }
                    }
                    else {
                        if (ov > overlap) {
                            overlap = ov;
                            candidate = j;
                        }
                    }
                }
            }
            crssings[used.size()] = crossings[candidate];
            System.arraycopy(paths[candidate], 0, pths[used.size()], 0, 4);
            adjustDisc(usedDisc, paths[candidate]);
            used.add(candidate);
            if (reached < 0 && usedDisc.size() >= 2*(t)) {
                reached = used.size()-1;
                for (int i : usedDisc) theDisc.add(i);
            }
        }
        if (reached < 0) return theLinks;
        for (int i = 0; i < theDisc.size(); i++) {
            ArrayList<Integer> newDisc = new ArrayList<Integer>();
            for (int j = i; j <= i+t-1; j++) {
                int k = j;
                if (k >= theDisc.size()) k = k - theDisc.size();
                newDisc.add(theDisc.get(k));
            }
            theLinks.add(twistedLink(t, newDisc, reached, crssings, pths));
        }
        return theLinks;
    }
        
    private Link twistedLink(int t, ArrayList<Integer> theDisc, int reached, int[] crssings,
            int[][] pths) {
        int a = t * (t-1);
        int[] tcrssings = new int[crossings.length+a];
        int[][] tpths = new int[paths.length+a][4];
        for (int i = 0; i <= reached; i++) {
            tcrssings[i] = crssings[i];
            System.arraycopy(pths[i], 0, tpths[i], 0, 4);
        }
        for (int i = reached+1; i < crssings.length; i++) {
            tcrssings[i+a] = crssings[i];
            System.arraycopy(pths[i], 0, tpths[i+a], 0, 4);
        }
        int b = 1 + 2 * crssings.length;
        for (int i = 0; i < t-1; i++) {
            if (i == 0) {
                tpths[reached+1][0] = theDisc.get(t-2);
                tpths[reached+1][3] = theDisc.get(t-1);
                tpths[reached+1][2] = b;
                tpths[reached+1][1] = b+1;
            }
            else {
                tpths[reached+1+i][0] = theDisc.get(t-2-i);
                tpths[reached+1+i][1] = b + 1 + 2 * i;
                tpths[reached+1+i][2] = b + 2 * i;
                tpths[reached+1+i][3] = b-1 + 2 * i;
            }
        }
        for (int i = 1; i < t; i++) {
            for (int j = 0; j < t-1; j++) {
                int x = 0;
                if (j == t-2) x = 1;
                int y = 0;
                if (j == 0) y = 2 * (t-1) - 1;
                tpths[reached+1+(t-1) * i+j][0] = b + 2*(t-1) * i - 2 * (t-2) + 2 * j - x;
                tpths[reached+1+(t-1) * i+j][1] = b + 2*(t-1) * i + 1 + 2 * j;
                tpths[reached+1+(t-1) * i+j][2] = b + 2*(t-1) * i + 2 * j;
                tpths[reached+1+(t-1) * i+j][3] = b + 2*(t-1) * i + 1 + 2 * (j-1) - y;
            }
        }
        for (int i = 1; i <= a; i++) {
            tcrssings[i+reached] = 1;
        }
        for (int i = 0; i < t; i++) {
            int x = 0;
            if (i == t-1) x = 1;
            theDisc.add(b+ 2 * (t-1) * (t-1)+ 2*i - x);
        }
        for (int i = reached+a+1; i < tpths.length; i++) {
            for (int j = 0; j < 4; j++) {
                if (theDisc.contains(tpths[i][j])) {
                    tpths[i][j] = theDisc.get(2*t-1-theDisc.indexOf(tpths[i][j]));
                }
            }
        }
        ArrayList<Integer> test = new ArrayList<Integer>();
        for (int i = 0; i < tpths.length; i++) {
            for (int j = 0; j < 4; j++) {
                if (test.contains(tpths[i][j])) test.remove((Integer) tpths[i][j]);
                else test.add(tpths[i][j]);
            }
        }
        //System.out.println(Arrays.deepToString(pths));
        //System.out.println(reached);
        //System.out.println(Arrays.toString(tcrssings));
        //System.out.println(Arrays.deepToString(tpths));
        //return null;
        return new Link(tcrssings, tpths);
    }
    
    public Link girthDiscMinimize() {
        if (crossings.length == 0) return this;
        if (isDiscAlways()) return this;
        int[] crssings = new int[crossings.length];
        int[][] pths = new int[paths.length][4];
        ArrayList<int[]> orCrossings = new ArrayList<int[]>();
        for (int[] or : orientation) {
            int[] guy = new int[4];
            System.arraycopy(paths[or[0]], 0, guy, 0, 4);
            orCrossings.add(guy);
        }
        ArrayList<Integer> used = new ArrayList<Integer>();
        ArrayList<Integer> usedDisc = new ArrayList<Integer>();
        crssings[0] = crossings[0];
        for (int i = 0; i < 4; i++) {
            pths[0][i] = paths[0][i];
            usedDisc.add((Integer) paths[0][i]);
        }
        used.add(0);
        while (used.size() < crossings.length) {
            int candidate = -1;
            int overlap = -1;
            for (int j = 1; j < crossings.length; j++) {
                if (!used.contains(j)) {
                    int ov = discOverlap(usedDisc, paths[j]);
                    if (ov > overlap) {
                        overlap = ov;
                        candidate = j;
                    }
                }
            }
            crssings[used.size()] = crossings[candidate];
            System.arraycopy(paths[candidate], 0, pths[used.size()], 0, 4);
            adjustDisc(usedDisc, paths[candidate]);
            used.add(candidate);
        }
        ArrayList<int[]> newOr = new ArrayList<int[]>();
        for (int i=0; i < orCrossings.size(); i++) {
            int[] orPath = orCrossings.get(i);
            int y = findThisPath(pths,orPath);
            int[] nwOr = new int[2];
            nwOr[0] = y;
            nwOr[1] = orientation.get(i)[1];
            newOr.add(nwOr);
        }
        Link theLink = new Link(crssings,pths,newOr,unlinkComp);
        return theLink;
    }// */
    
    private void adjustDisc(ArrayList<Integer> disc, int[] pths) {
        int u = firstPos(disc, pths);
        if (u < 0) {
            for (int p : pths) disc.add((Integer) p);
        }
        else {
            int st = startPos(pths, disc.get(u));
            ArrayList<Integer> removers = new ArrayList<Integer>();
            for (int j = 0; j < 4; j++) {
                if (disc.contains(pths[j])) {
                    removers.add(pths[j]);
                    disc.remove((Integer) pths[j]);
                }
            }
            for (int j = 0; j < 4; j++) {
                int c = (4+st-j)% 4;
                if (!removers.contains(pths[c])) disc.add(u, (Integer) pths[c]);
            }
        }
    }// */
    
    private int startPos(int[] pths, int v) {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (pths[i] == v) found = true;
            else i++;
        }
        return i;
    }
    
    private int firstPos(ArrayList<Integer> disc, int[] pths) {
        boolean found = false;
        int i = 0;
        while (i < disc.size() && !found) {
            int d = disc.get(i);
            if (pths[0] == d || pths[1] == d || pths[2] == d || pths[3] == d) found = true;
            else i++;
        }
        if (found) return i;
        return -1;
    }
    
    private int discOverlap(ArrayList<Integer> disc, int[] pths) {
        ArrayList<Integer> ovlp = new ArrayList<Integer>();
        for (int p : pths) {
            int pos = disc.indexOf(p);
            if (pos >= 0) ovlp.add(pos);
        }
        if (ovlp.size() > 1) Collections.sort(ovlp);
        if (disconnected(ovlp, disc.size())) return -1;
        return ovlp.size();
    }// */
    
    private boolean disconnected(ArrayList<Integer> ovlp, int sz) {
        if (ovlp.size() <= 1) return false;
        boolean connected = true;
        int i = 0;
        while (connected && i < ovlp.size()-1) {
            if (ovlp.get(i+1) - ovlp.get(i) > 1) {
                if (ovlp.get(i+1) != sz -1 | ovlp.get(0) != 0) connected = false;
            }
            i++;
        }
        return !connected;
    }// */
    
    public ArrayList<Integer> getDirections() {
        ArrayList<Integer> dirs = new ArrayList<Integer>();
        ArrayList<ArrayList<Integer>> comps = this.getComponents();
        for (int i = 0; i < this.crossingLength(); i++) {
            int[] path = this.getPath(i);
            ArrayList<Integer> comp = compContaining(comps, path[0]);
            int dirOne = getDirection(comp, path[0], path[2], i, 0);
            comp = compContaining(comps, path[3]);
            int dirTwo = getDirection(comp, path[3], path[1], i, 3);
            dirs.add(dirOne + 2 * dirTwo); // 0 means north, 1 means east, 2 means west, 3 means south
        }
        return dirs;
    }
    
    private ArrayList<Integer> compContaining(ArrayList<ArrayList<Integer>> comps, int i) {
        boolean found = false;
        int j = 0;
        while (!found) {
            if (comps.get(j).contains(i)) found = true;
            else j++;
        }
        return comps.get(j);
    }
    
    private int getDirection(ArrayList<Integer> comp, int l, int u, int pi, int low) {
        if (comp.size() == 2) {
            int[] or = this.orientation(this.getComponents().indexOf(comp));
            int os = this.getPath(or[0], or[1]);
            if (pi == or[0]) {
                if (low == or[1]) return 0;             // 0 means going up
                if ((low + 2)% 4 == or[1]) return 1;    // 1 means going down
                if (l == comp.get(0)) return 1;
                return 0;
            }
            if (l == comp.get(0)) return 1;
            return 0;
        }
        int i = comp.indexOf(l);
        int j = comp.indexOf(u);
        if (i < j) {
            if (j - i == 1) return 0;
            return 1;
        }
        if (i - j == 1) return 1;
        return 0;
    }
    
    public ArrayList<Integer> circleAt(int start, ArrayList<Integer> position) {
        ArrayList<int[]> lPaths = littlePaths(position);
        //for (int[] pth : lPaths) System.out.print(Arrays.toString(pth));
        //System.out.println();
        ArrayList<Integer> circle = combineLittlePaths(lPaths, start);
        return circle;
    }
    
    private ArrayList<Integer> combineLittlePaths(ArrayList<int[]> lPaths, int start) {
        ArrayList<Integer> combined = new ArrayList<Integer>();
        combined.add(start);
        int runner = start;
        boolean cont = true;
        while (cont) {
            int[] lpth = getLittlePath(lPaths, runner);
            lPaths.remove(lpth);
            if (lpth[0] != runner) {
                combined.add(lpth[0]);
                runner = lpth[0];
            }
            else {
                combined.add(lpth[1]);
                runner = lpth[1];
            }
            if (runner == start) cont = false;
        }
        return combined;
    }
    
    private int[] getLittlePath(ArrayList<int[]> lPaths, int start) {
        boolean found = false;
        int i = 0;
        while (!found) {
            int[] lp = lPaths.get(i);
            if (lp[0] == start || lp[1] == start) found = true;
            else i++;
        }
        return lPaths.get(i);
    }
    
    private ArrayList<int[]> littlePaths(ArrayList<Integer> position) {
        ArrayList<int[]> lpaths = new ArrayList<int[]>();
        for (int i = 0; i < position.size(); i++) {
            int[] path = this.getPath(i);
            int cross = this.getCross(i);
            int pos = position.get(i);
            int[] arrange = arrangement(path, cross, pos);
            lpaths.add(new int[] {arrange[0], arrange[1]});
            lpaths.add(new int[] {arrange[2], arrange[3]});
        }
        return lpaths;
    }
    
    private int[] arrangement(int[] path, int cross, int pos) {
        int[] arrange = path;
        if (cross > 0) {
            if (pos != 0) arrange = new int[] {path[0], path[3], path[2], path[1]};
        }
        else {
            if (pos != -cross) arrange = new int[] {path[0], path[3], path[2], path[1]};
        }
        return arrange;
    }
    
    public ArrayList<ArrayList<Integer>> getLadyBug(int st, int fChange, int sChange, 
            ArrayList<Integer> position) {
        boolean att = false;
        ArrayList<ArrayList<Integer>> circles = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> theCircle = this.circleAt(st, position);
        ArrayList<ArrayList<Integer>> splitOne = splitCircle(theCircle, fChange);
        ArrayList<ArrayList<Integer>> splitTwo = splitCircle(theCircle, sChange);
        int p = 0;
        if (this.getCross(fChange) < 0) p = 1;
        int a = this.getPath(fChange, p);
        if (splitOne.get(0).contains(a)) circles.add(splitOne.get(0));
        else circles.add(splitOne.get(1));
        if (splitTwo.get(0).contains(a)) circles.add(splitTwo.get(0));
        else circles.add(splitTwo.get(1));
        return circles;
    }
    
    private ArrayList<ArrayList<Integer>> splitCircle(ArrayList<Integer> theCircle, int ch) {
        ArrayList<ArrayList<Integer>> split = new ArrayList<ArrayList<Integer>>();
        int[] path = this.getPath(ch);
        int crs = this.getCross(ch);
        ArrayList<Integer> fCircle = new ArrayList<Integer>();
        ArrayList<Integer> sCircle = new ArrayList<Integer>();
        split.add(fCircle);
        split.add(sCircle);
        boolean fc = true;
        for (int i = 0; i < theCircle.size(); i++) {
            int run = theCircle.get(i);
            int pos = positionInPath(run, path);
            if (pos >= 0) {
                if (i+1 < theCircle.size()) {
                    int next = theCircle.get(i+1);
                    int np = positionInPath(next, path);
                    if (fc) fCircle.add(run);
                    else sCircle.add(run);
                    if (np >= 0) {
                        if (changeCircle(pos, np, crs)) fc = !fc;
                    }
                }
            }
            else {
                if (fc) fCircle.add(run);
                else sCircle.add(run);
            }
        }
        return split;
    }
    
    private boolean changeCircle(int pos, int npos, int crs) {
        if (crs > 0) {
            if (pos == 0) return npos == 1;
            if (pos == 1) return npos == 0;
            if (pos == 2) return npos == 3;
            if (pos == 3) return npos == 2;
        }
        if (pos == 0) return npos == 3;
        if (pos == 1) return npos == 2;
        if (pos == 2) return npos == 1;
        return npos == 0;
    }
    
    private int positionInPath(int x, int[] path) {
        if (path[0] == x) return 0;
        if (path[1] == x) return 1;
        if (path[2] == x) return 2;
        if (path[3] == x) return 3;
        return -1;
    }
    
    private boolean isDiscAlways() {
        ArrayList<Integer> disc = new ArrayList<Integer>();
        for (int i = 0; i < paths.length; i++) {
            int[] path = paths[i];
            ArrayList<Integer> ovOld = new ArrayList<Integer>();
            ArrayList<Integer> ovNew = new ArrayList<Integer>();
            disc = overlapDisc(disc, path, ovOld, ovNew);
            if (disc == null) return false;
        }
        return true;
    }
    
    public ArrayList<Integer> getDiscAt(int u) {
        ArrayList<Integer> disc = new ArrayList<Integer>();
        for (int i = 0; i <= u; i++) {
            int[] path = paths[i];
            ArrayList<Integer> ovOld = new ArrayList<Integer>();
            ArrayList<Integer> ovNew = new ArrayList<Integer>();
            disc = overlapDisc(disc, path, ovOld, ovNew);
            if (disc == null) return null;
        }
        return disc;
    }

    private ArrayList<Integer> overlapDisc(ArrayList<Integer> disc, int[] path, ArrayList<Integer> ovOld, 
            ArrayList<Integer> ovNew) {
        for (int i = 0; i < 4; i++) {
            if (disc.contains(path[i])) {
                ovOld.add(disc.indexOf(path[i]));
                ovNew.add(i);
            }
        }
        if (ovOld.isEmpty()) {
            if (!disc.isEmpty()) return null;
            for (int i = 0; i < 4; i++) disc.add(path[i]);
            return disc;
        }
        if (ovOld.size() == disc.size()) {
            disc.clear();
            for (int i = 0; i < 4; i++) {
                if (!ovNew.contains(i)) disc.add(path[i]);
            }
            return disc;
        }
        int[] range = appears(ovOld);
        int start = range[0];
        int end = range[1];
        if (start == 0 && end == disc.size()-1) {
            while (ovOld.contains(start+1)) start++;
            while (ovOld.contains(end-1)) end--;
            int help = start;
            start = end;
            end = help;
        }
        if (!overlapAsDisc(disc, ovOld, start, end)) {
            //System.out.println(disc);             // this can happen, uncommand to see
            //System.out.println(ovOld);
            //System.out.println(start+" "+ end);
            return null;
        }
        ArrayList<Integer> newDisc = new ArrayList<Integer>();
        for (int u = 1; u < disc.size(); u++) {
            int run = (u+end) % disc.size();
            if (run != start) newDisc.add(disc.get(run));
            else break;
        }
        if (ovNew.size() == 1) {
            int k = ovNew.get(0);
            for (int u = 1; u < 4; u++) {
                int run = (u+k) % 4;
                newDisc.add(path[run]);
            }
        }
        if (ovNew.size() == 2) {
            int st = ovNew.get(0);
            int en = ovNew.get(1);
            if (st == 0 && en == 3) {
                //st = 3;
                en = 0;
            }
            for (int u = 1; u < 3; u++) {
                int run = (u+en) % 4;
                newDisc.add(path[run]);
            }
        }
        if (ovNew.size() == 3) {
            for (int u = 0; u < 4; u++) {
                if (!ovNew.contains(u)) newDisc.add(path[u]);
            }
        }
        return newDisc;
        //disc.clear();
        //for (int u : newDisc) disc.add(u);
    }
    
    private int[] appears(ArrayList<Integer> list) {
        int max = list.get(0);
        int min = list.get(0);
        for (int u : list) {
            if (u > max) max = u;
            if (u < min) min = u;
        }
        return new int[] {min, max};
    }

    private boolean overlapAsDisc(ArrayList<Integer> disc, ArrayList<Integer> ovOld, 
            int start, int end) {
        if (ovOld.size() == disc.size()) return true;
        if (ovOld.size() == 1) return true;
        if (ovOld.size() == 2) return end - start < 2;
        if (ovOld.size() == 3) {
            if (end - start >= 3) return false;
            return !(end == 0 && start == disc.size()-1);
        }
        if (end - start >= 4) return false;
        if (start > end) return start - end == disc.size() - 3;
        return true;
    }

    public boolean agreesWith(Link link) {
        for (int j = 0; j < link.paths.length; j++) {
            int[] path = link.getPath(j);
            int i = pathNumber(path);
            if (i == -1) return false;
            if (link.crossings[j] != crossings[i]) return false;
        }
        return true;
    }
    
    private int pathNumber(int[] path) {
        for (int i = 0; i < paths.length; i++) {
            if (samePath(paths[i], path)) return i;
        }
        return -1;
    }
    
    private boolean samePath(int[] pOne, int[] pTwo) {
        boolean identical = true;
        for (int i = 0; i < 4; i++) {
            if (pOne[i] != pTwo[i]) {
                identical = false;
                break;
            }
        }
        if (identical) return true;
        for (int i = 0; i < 4; i++) {
            if (pOne[i] != pTwo[(i+2)%4]) return false;
        }
        return true;
    }
    
}
