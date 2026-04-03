/*

Copyright (C) 2021 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.diagrams.griddiagrams;

import java.util.ArrayList;
import java.util.Collections;
import knotjob.AbortInfo;
import knotjob.diagrams.Edge;
import knotjob.diagrams.SComplex;
import knotjob.diagrams.Vertex;
import knotjob.links.Link;

/**
 *
 * @author Dirk
 */
public class GridCreator {
    
    private final double factor = 3.0;
    private final Link theLink;
    private final AbortInfo abortInfo;
    private final int adder;
    private int[] orientDir;
    private int[][] playingField;
    private GridCoordinate[] positions;
    private ArrayList<Integer> usedPaths;
    
    public GridCreator(Link lnk, AbortInfo abort) {
        theLink = lnk.girthMinimize().onlyFirstPart();
        abortInfo = abort;
        adder = 10000000;
    }

    public SComplex getSComplex() {
        getBasicGrid();
        zoomOut();
        pathToNumbers();
        SComplex complex = new SComplex();
        addVertices(complex);
        addEdges(complex);
        return complex;
    }
    
    public GridDiagram getDiagram() {
        getBasicGrid();
        ArrayList<Integer> xPos = getPositions(-4);
        ArrayList<Integer> oPos = getPositions(-5);
        return new GridDiagram(xPos, oPos);
    }
    
    public void output() {
        for (int y = 0; y < playingField[0].length; y++) {
            for (int x = 0; x < playingField.length; x++) {
                int r = playingField[x][y];
                if (r == 0) System.out.print(".");
                if (r > 0) System.out.print(r%10);
                if (r < -5) System.out.print("C");
                if (r == -2) System.out.print("V");
                if (r == -3) System.out.print("H");
                if (r == -4) System.out.print("X");
                if (r == -5) System.out.print("O");
            }
            System.out.println();
        }
        System.out.println();
    }
    
    private void getBasicGrid() {
        playingField = new int[3][3];
        orientDir = new int[theLink.getComponents().size()];
        positions = new GridCoordinate[theLink.crossingLength()];
        for (int i = 0; i < positions.length; i++) positions[i] = null;
        usedPaths = new ArrayList<Integer>();
        GridCoordinate coord = new GridCoordinate(1, 1, 3);
        if (theLink.getCross(0) < 0) coord.dir = 2;
        positions[0] = coord;
        addFirstCrossingToField(coord, 0);
        for (int k = 1; k < theLink.crossingLength(); k++) {
            coord = bestCoordinate(k);
            if (coord != null) addCrossingToField(coord,k);
            else {
                zoomIn();
                break;
            }
            zoomIn();
        }
        alternateCorners();
        removeStaircases();
    }
    
    private ArrayList<Integer> getPositions(int det) {
        ArrayList<Integer> pos = new ArrayList<Integer>();
        for (int x = 0; x < playingField.length; x++) {
            int y = 0;
            boolean found = false;
            while (!found) {
                if (playingField[x][y] == det) found = true;
                else y++;
            }
            pos.add(y);
        }
        return pos;
    }
    
    private void addFirstCrossingToField(GridCoordinate coord, int pos) {
        int nort = theLink.getPath(pos, (4-coord.dir)%4);
        int sout = theLink.getPath(pos, (6-coord.dir)%4);
        int west = theLink.getPath(pos, (5-coord.dir)%4);
        int east = theLink.getPath(pos, (3-coord.dir)%4);
        checkForOrientations(pos, nort, west, sout, east);
        playingField[1][0] = nort;
        playingField[0][1] = west;
        playingField[1][1] = -(6+pos);
        playingField[2][1] = east;
        playingField[1][2] = sout;
        for (int u : theLink.getPath(pos)) usedPaths.add(u);
    }
    
    private void checkForOrientations(int k, int nort, int west, int sout, int east) {
        for (int i = 0; i < theLink.getComponents().size(); i++) {
            int[] or = theLink.orientation(i);
            if (k == or[0]) {
                int o = theLink.getPath(k, or[1]);
                orientDir[i] = directionOf(o, nort, west, sout, east);
            }
        }
    }
    
    private int directionOf(int o, int nort, int west, int sout, int east) {
        if (o == nort) return 0;
        if (o == west) return 1;
        if (o == sout) return 2;
        if (o == east) return 3;
        return -1;
    }
    
    private GridCoordinate bestCoordinate(int c) {
        ArrayList<Integer> combinePaths = new ArrayList<Integer>();
        for (int u : theLink.getPath(c)) {
            if (usedPaths.contains(u)) {
                usedPaths.remove((Integer) u);
                combinePaths.add(u);
            }
            else usedPaths.add(u);
        }
        ArrayList<GridCoordinate> numbCoords = new ArrayList<GridCoordinate>();
        for (int u : combinePaths) {
            numbCoords.add(getCoordinate(u));
        }
        ArrayList<GridCoordinate> bestCoords = new ArrayList<GridCoordinate>();
        for (int k = 0; k < numbCoords.size(); k++) {
            bestCoords.add(bestCoordinate(c,combinePaths.get(k),k,numbCoords));
        }
        int mi = 0;
        if (bestCoords.isEmpty()) return null;
        int best = bestCoords.get(0).dir;
        int i = 1;
        while (i < bestCoords.size()) {
            int next = bestCoords.get(i).dir;
            if (next < best) {
                mi = i;
                best = next;
            }
            i++;
        }
        GridCoordinate bestCoordinate = bestCoords.get(mi);
        bestCoordinate.dir = positionRel(bestCoordinate,numbCoords.get(mi));
        return bestCoordinate;
    }
    
    private GridCoordinate getCoordinate(int u) {
        boolean found = false;
        int i = 0;
        int j = 0;
        while (!found) {
            j = 0;
            while (!found && j < playingField[0].length) {
                if (playingField[i][j] == u) found = true;
                else j++;
            }
            if (!found) i++;
        }
        GridCoordinate coord = new GridCoordinate(i,j,0);
        if (i>0 && playingField[i-1][j] < -5) coord.dir = 3;
        if (j>0 && playingField[i][j-1] < -5) coord.dir = 2;
        if (i<playingField.length-1 && playingField[i+1][j] < -5) coord.dir = 1;// the direction is pointing out of the crossing
        return coord; // and to the number
    }
    
    private GridCoordinate bestCoordinate(int c, int u, int k, ArrayList<GridCoordinate> numbCoords) {
        GridCoordinate ofU = numbCoords.get(k);
        int direction = theDirection(ofU.dir,c,u);
        if (direction == ofU.dir) {
            int rot = rotationOf(c,u,ofU);
            ofU.dir = distanceOf(ofU,c,rot,numbCoords);
        }
        else {
            GridCoordinate toLU = new GridCoordinate(ofU.x,ofU.y,0);
            GridCoordinate toRD = new GridCoordinate(ofU.x,ofU.y,0);
            if (ofU.dir%2 == 0) {
                toLU.x--;
                toLU.dir = 1;
                toRD.x++;
                toRD.dir = 3;
            }
            else {
                toLU.y--;
                toLU.dir = 0;
                toRD.y++;
                toRD.dir = 2;
            }
            int rot = rotationOf(c,u,toLU);
            toLU.dir = distanceOf(toLU,c,rot,numbCoords);
            if (playingField[toLU.x][toLU.y] != 0) toLU.dir = toLU.dir+2;
            rot = rotationOf(c,u,toRD);
            toRD.dir = distanceOf(toRD,c,rot,numbCoords);
            if (playingField[toRD.x][toRD.y] != 0) toRD.dir = toRD.dir+2;
            ofU = toLU;
            if (toRD.dir < toLU.dir) ofU = toRD;
        }
        return ofU;
    }
    
    private int theDirection(int diro, int c, int u) {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (theLink.getPath(c, i) == u) found = true;
            else i++;
        }
        if (theLink.getCross(c) > 0) i = (i+1)%4;
        else i = (i+2)%4;// i is now pointing in the direction of where the crossing is.
        if ((diro+i)%2 == 0) return diro; // keep pointing into the same direction
        return i; // there will be a change of direction
    }
    
    private int distanceOf(GridCoordinate coord, int c, int rot, ArrayList<GridCoordinate> numbCoords) {
        int dist = 0;
        for (int i = 0; i < 4; i++) {
            int k = theLink.getPath(c, i);
            if (!usedPaths.contains(k)) { // this means there is a k on the field
                GridCoordinate ncoord = rotatedCoord(coord,rot,i);
                dist = dist + theDistance(ncoord,k,numbCoords);
                if (!outsideField(ncoord)) 
                    if (playingField[ncoord.x][ncoord.y] != 0 && playingField[ncoord.x][ncoord.y] != k && 
                        playingField[ncoord.x][ncoord.y] >= -5) dist = dist + 2;
            }
        }
        return dist;
    }
    
    private GridCoordinate rotatedCoord(GridCoordinate coord, int rot, int i) {
        GridCoordinate ncord = new GridCoordinate(coord.x, coord.y, coord.dir);
        int dir = (2 + i + rot)%4;
        if (dir == 0) ncord.y--;
        if (dir == 1) ncord.x--;
        if (dir == 2) ncord.y++;
        if (dir == 3) ncord.x++;
        return ncord;
    }
    
    private int theDistance(GridCoordinate coord, int k, ArrayList<GridCoordinate> numbCoords) {
        int dist;
        boolean found = false;
        int i = 0;
        while (!found) {
            if (playingField[numbCoords.get(i).x][numbCoords.get(i).y] == k) found = true;
            else i++;
        }
        dist = Math.abs(coord.x - numbCoords.get(i).x) + Math.abs(coord.y - numbCoords.get(i).y);
        if (dist >= 5) dist = 5;
        return dist;
    }
    
    private boolean outsideField(GridCoordinate coord) {
        return coord.x < 0 || coord.x >= playingField.length || coord.y < 0 
                || coord.y >= playingField[0].length;
    }
    
    private int rotationOf(int c, int u, GridCoordinate coord) {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (theLink.getPath(c, i) == u) found = true;
            else i++;
        }
        return (4+coord.dir-i)%4;
    }
    
    private int positionRel(GridCoordinate coord, GridCoordinate bcord) {
        if (coord.y < bcord.y) return 0;
        if (coord.x < bcord.x) return 1;
        if (coord.y > bcord.y) return 2;
        if (coord.x > bcord.x) return 3;
        return 4;
    }
    
    private void addCrossingToField(GridCoordinate coord, int k) {
        int x = coord.x;
        int y = coord.y;
        if (coord.dir == 3) x--;
        if (coord.dir == 2) y--;
        if (coord.dir == 1) x++;
        if (coord.dir == 0) y++;
        int combineNumber = playingField[x][y];
        GridCoordinate nextCrossing = crossingNextTo(x,y);
        int rotation;
        if (coord.dir != 4) rotation = rotationOf(k,combineNumber,coord);
        else rotation = rotationOf(k,combineNumber,nextCrossing);
        int nort = theLink.getPath(k, (6-rotation)%4);
        int sout = theLink.getPath(k, (4-rotation)%4);
        int west = theLink.getPath(k, 3-rotation);
        int east = theLink.getPath(k, (5-rotation)%4);
        checkForOrientations(k, nort, west, sout, east);
        if (coord.dir != 4) { 
            if (playingField[coord.x][coord.y] != 0) {
                if (coord.dir == 0) {
                    addRow(coord.y+1);
                    coord.y++;
                    nextCrossing.y++;
                }
                if (coord.dir == 1) {
                    addColumn(coord.x+1);
                    coord.x++;
                    nextCrossing.x++;
                }
                if (coord.dir == 2) addRow(coord.y);
                if (coord.dir == 3) addColumn(coord.x);
            }
        }
        if (coord.dir != 0 && needExpansion(coord,sout,2)) addRow(coord.y+1);
        if (coord.dir != 2 && needExpansion(coord,nort,0)) {
            addRow(coord.y);
            coord.y++;
            nextCrossing.y++;
        }
        if (coord.dir != 1 && needExpansion(coord,east,3)) addColumn(coord.x+1);
        if (coord.dir != 3 && needExpansion(coord,west,1)) {
            addColumn(coord.x);
            coord.x++;
            nextCrossing.x++;
        }
        boolean extra = false;
        int ext = playingField[coord.x][coord.y];
        playingField[coord.x][coord.y] = -5;
        if (usedPaths.contains(west)) playingField[coord.x-1][coord.y] = west;
        if (usedPaths.contains(east)) playingField[coord.x+1][coord.y] = east;
        if (usedPaths.contains(nort)) playingField[coord.x][coord.y-1] = nort;
        if (usedPaths.contains(sout)) playingField[coord.x][coord.y+1] = sout;
        if (ext > 0) { // special case where we need to trick the connecting of paths
            extra = true;
            usedPaths.add(ext);
        }
        positions[k] = coord;
        connectPaths(coord,nort,west,sout,east);
        if (extra) usedPaths.remove((Integer) ext);
        playingField[coord.x][coord.y] = -(6+k);
    }
    
    private GridCoordinate crossingNextTo(int x, int y) { // finds position of crossing next to (x,y), direction points to (x,y)
        GridCoordinate coord = new GridCoordinate(x,y,0);
        if (x > 0 && playingField[x-1][y] < -5) {
            coord.x--;
            coord.dir = 3;
            return coord;
        }
        if (x < playingField.length-1 && playingField[x+1][y] < -5) {
            coord.x++;
            coord.dir = 1;
            return coord;
        }
        if (y > 0 && playingField[x][y-1] < -5) {
            coord.y--;
            coord.dir = 2;
            return coord;
        }
        coord.y++;
        coord.dir = 0;
        return coord;
    }
    
    private void addRow(int i) {
        int[][] newField = new int[playingField.length][playingField[0].length+1];
        for (int x = 0; x < playingField.length; x++) {
            for (int y = 0; y < playingField[0].length; y++) {
                if (y < i) newField[x][y] = playingField[x][y];
                if (y >= i) newField[x][y+1] = playingField[x][y];
            }
        }
        for (GridCoordinate coord : positions) {
            if (coord != null) {
                if (coord.y >= i) coord.y++;
            }
        }
        if (i > 0 && i < playingField[0].length) {
            for (int x = 0; x < playingField.length; x++) {
                int posa = playingField[x][i-1];
                int posb = playingField[x][i];
                if (posa == -2 || posb == -2) newField[x][i] = -2;
                if (posa == -4 && posb == -4) {
                    if (countVerticalX(x,i-1)%2 == 0) newField[x][i] = -2;
                }
                if (posa <= -5) {
                    if (posb > 0) {
                        newField[x][i] = posb;
                        newField[x][i+1] = 0;
                    }
                    if (posb <= -4) newField[x][i] = -2;
                }
                if (posb <= -5) {
                    if (posa > 0) {
                        newField[x][i] = posa;
                        newField[x][i-1] = 0;
                    }
                    if (posa <= -4) newField[x][i] = -2;
                }
            }
        }
        playingField = newField;
    }
    
    private int countVerticalX(int i, int j) {
        int count = 0;
        for (int u = 0; u < j; u++) {
            if (goodAsAnX(i,u,true)) count++; 
        }
        return count;
    }
    
    private boolean goodAsAnX(int i, int j, boolean ud) {
        int pos = playingField[i][j];
        if (pos == -4) return true;
        if (pos <= 0) return false;
        if (ud) { // checking up or down
            if (j > 0) {
                pos = playingField[i][j-1];
                if (pos <= -5) return true;
            }
            if ( j < playingField[0].length - 1) {
                pos = playingField[i][j+1];
                if (pos <= -5) return true;
            }
        }
        else { // checking left or right
            if (i > 0) {
                pos = playingField[i-1][j];
                if (pos <= -5) return true;
            }
            if ( i < playingField.length - 1) {
                pos = playingField[i+1][j];
                if (pos <= -5) return true;
            }
        }
        return false;
    }
    
    private void addColumn(int i) {
        int[][] newField = new int[playingField.length+1][playingField[0].length];
        for (int x = 0; x < playingField.length; x++) {
            for (int y = 0; y < playingField[0].length; y++) {
                if (x < i) newField[x][y] = playingField[x][y];
                if (x >= i) newField[x+1][y] = playingField[x][y];
            }
        }
        for (GridCoordinate coord : positions) {
            if (coord != null) {
                if (coord.x >= i) coord.x++;
            }
        }
        if (i > 0 && i < playingField.length) {
            for (int y = 0; y < playingField[0].length; y++) {
                int posa = playingField[i-1][y];
                int posb = playingField[i][y];
                if (posa == -3 || posb == -3) newField[i][y] = -3;
                if (posa == -4 && posb == -4) {
                    if (countHorizontalX(i-1,y)%2 == 0) newField[i][y] = -3;
                }
                if (posa <= -5) {
                    if (posb > 0) {
                        newField[i][y] = posb;
                        newField[i+1][y] = 0;
                    }
                    if (posb <= -4) newField[i][y] = -3;
                }
                if (posb <= -5) {
                    if (posa > 0) {
                        newField[i][y] = posa;
                        newField[i-1][y] = 0;
                    }
                    if (posa <= -4) newField[i][y] = -3;
                }
            }
        }
        playingField = newField;
    }
    
    private int countHorizontalX(int i, int j) {
        int count = 0;
        for (int u = 0; u < i; u++) {
            if (goodAsAnX(u,j,false)) count++; 
        }
        return count;
    }
    
    private boolean needExpansion(GridCoordinate coord, int number, int dir) {
        GridCoordinate check = new GridCoordinate(coord.x,coord.y,0);
        if (dir == 0) check.y--;
        if (dir == 1) check.x--;
        if (dir == 2) check.y++;
        if (dir == 3) check.x++;
        if (outsideField(check)) return true;
        return playingField[check.x][check.y] != 0 && playingField[check.x][check.y] != number;
    }
    
    private void connectPaths(GridCoordinate coord, int nort, int west, int sout, 
            int east) {
        ArrayList<Pair> thePairs = new ArrayList<Pair>();
        if (!usedPaths.contains(nort)) addToPairs(thePairs, nort, 0, coord.x,coord.y-1);
        if (!usedPaths.contains(west)) addToPairs(thePairs, west, 1, coord.x-1,coord.y);
        if (!usedPaths.contains(sout)) addToPairs(thePairs, sout, 2, coord.x,coord.y+1);
        if (!usedPaths.contains(east)) addToPairs(thePairs, east, 3, coord.x+1,coord.y);
        Collections.sort(thePairs);
        for (Pair pair : thePairs) {
            GridPath path = new GridPath(playingField, adder, pair.n, pair.samepos);
            if (path.thePath == null) {
                zoomOut();
                path = new GridPath(playingField, adder, pair.n, pair.samepos);
            }
            GridCoordinate first = path.thePath.get(0);
            if (first.dir % 2 == 0) playingField[first.x][first.y] = -2;
            else playingField[first.x][first.y] = -3;
            for (int j = 1; j < path.thePath.size(); j++) {
                GridCoordinate  secon = path.thePath.get(j);
                if (first.sameFlatCoord(secon)) playingField[first.x][first.y] = -4;
                else {
                    if (first.dir % 2 == 0) playingField[secon.x][secon.y] = -2;
                    else playingField[secon.x][secon.y] = -3;
                }
                first = secon;
                
            }
            if (abortInfo != null && abortInfo.isAborted()) return;
        }
        checkCorners();
    }
    
    private void addToPairs(ArrayList<Pair> thePairs, int nort, int dir, int x, int y) {
        Pair pair = new Pair(nort, dir, getTheDistance(nort,x,y,false));
        if (playingField[x][y] == 0) playingField[x][y] = nort + adder;
        else pair.samepos = true;
        thePairs.add(pair);
    }
    
    private int getTheDistance(int cort, int i, int j, boolean capped) {
        boolean found = false;
        int x = 0;
        int y = 0;
        int dist;
        while (!found && x < playingField.length) {
            y = 0;
            while (!found && y < playingField[0].length) {
                if (playingField[x][y] == cort) found = true;
                else y++;
            }
            if (!found) x++;
        }
        if (!found) dist = 5;
        else dist = Math.abs(i-x)+Math.abs(j-y);
        if (capped && dist > 5) dist = 5;
        if (i >= 0 && i < playingField.length && j >= 0 && j < playingField[0].length) {
            if (playingField[i][j] != cort && playingField[i][j] != 0) dist = 5;
        }
        
        return dist;
    }
    
    private void zoomOut() {
        int i = playingField.length;
        while (i >= 0) {
            addColumn(i);
            i--;
        }
        int j = playingField[0].length;
        while (j >= 0) {
            addRow(j);
            j--;
        }
    }
    
    private void checkCorners() {
        int i = 0;
        int j;
        while (i < playingField.length) {
            j = 0;
            ArrayList<Integer> xpositions = new ArrayList<Integer>();
            while (j < playingField[0].length && xpositions.size()<=2) {
                if (goodAsAnX(i,j,true)) xpositions.add(j);
                j++;
            }
            if (xpositions.size()>=3) addShiftedColumn(i,xpositions.get(2));
            i++;
        }
        j = 0;
        while (j < playingField[0].length) {
            i = 0;
            ArrayList<Integer> xpositions = new ArrayList<Integer>();
            while (i < playingField.length && xpositions.size()<= 2) {
                if (goodAsAnX(i,j,false)) xpositions.add(i);
                i++;
            }
            if (xpositions.size()>=3) addShiftedRow(j,xpositions.get(2));
            j++;
        }
    }
    
    private void addShiftedColumn(int i, Integer pos) {
        int[][] newField = new int[playingField.length+1][playingField[0].length];
        for (int x = 0; x < i; x++) {
            System.arraycopy(playingField[x], 0, newField[x], 0, playingField[0].length);
        }
        for (int x = i+1; x < playingField.length; x++) {
            System.arraycopy(playingField[x], 0, newField[x+1], 0, playingField[0].length);
        }
        for (int y = 0; y < pos; y++) setBetweenColumn(newField,i,y);
        for (int y = pos; y < playingField[0].length; y++) setBetweenColumn(newField,i-1,y);
        for (GridCoordinate coord : positions) {
            if (coord != null) {
                if (coord.x > i) coord.x++;
                if (coord.x == i && coord.y >= pos) coord.x++;
            }
        }
        playingField = newField;
    }

    private void addShiftedRow(int j, Integer pos) {
        int[][] newField = new int[playingField.length][playingField[0].length+1];
        for (int y = 0; y < j; y++) {
            for (int x = 0; x < playingField.length; x++) newField[x][y] = playingField[x][y];
        }
        for (int y = j+1; y < playingField[0].length; y++) {
            for (int x = 0; x < playingField.length; x++) newField[x][y+1] = playingField[x][y];
        }
        for (int x = 0; x < pos; x++) setBetweenRow(newField,j,x);
        for (int x = pos; x < playingField.length; x++) setBetweenRow(newField,j-1,x);
        for (GridCoordinate coord : positions) {
            if (coord != null) {
                if (coord.y > j) coord.y++;
                if (coord.y == j && coord.x >= pos) coord.y++;
            }
        }
        playingField = newField;
    }
    
    private void setBetweenColumn(int[][] newField, int i, int y) {
        if (i < 0) {
            newField[1][y] = playingField[0][y];
            newField[2][y] = playingField[1][y];
            return;
        }
        if (i >= playingField.length-1) {
            newField[i][y] = playingField[i][y];
            return;
        }
        int posa = playingField[i][y];
        int posb = playingField[i+1][y];
        if (posa == -4) {
            newField[i][y] = posa;
            newField[i+2][y] = posb;
            if (posb == -3 || posb <= -5) newField[i+1][y] = -3;
            if (posb == -4 && countHorizontalX(i,y)%2 == 0) newField[i+1][y] = -3;
        }
        if (posa == -3 || posb == -3) {
            newField[i][y] = posa;
            newField[i+1][y] = -3;
            newField[i+2][y] = posb;
        }
        if (posa == -2 || posa >= 0) {
            newField[i][y] = posa;
            newField[i+2][y] = posb;
        }
        if (posa <= -5) {
            if (posb > 0) {
                newField[i][y] = posa;
                newField[i+1][y] = posb;
                newField[i+2][y] = 0;
            }
            if (posb <= -4) {
                newField[i][y] = posa;
                newField[i+1][y] = -3;
                newField[i+2][y] = posb;
            }
        }
        if (posb <= -5) {
            if (posa > 0) {
                newField[i][y] = 0;
                newField[i+1][y] = posa;
                newField[i+2][y] = posb;
            }
        }
    }
    
    private void setBetweenRow(int[][] newField, int j, int x) {
        if (j < 0) {
            newField[x][1] = playingField[x][0];
            newField[x][2] = playingField[x][1];
            return;
        }
        if (j >= playingField[0].length - 1) {
            newField[x][j] = playingField[x][j];
            return;
        }
        int posa = playingField[x][j];
        int posb = playingField[x][j+1];
        if (posa == -4) {
            newField[x][j] = posa;
            newField[x][j+2] = posb;
            if (posb == -2 || posb <= -5) newField[x][j+1] = -2;
            if (posb == -4 && countVerticalX(x,j)%2 == 0) newField[x][j+1] = -2;
        }
        if (posa == -3 || posa >= 0) {
            newField[x][j] = posa;
            newField[x][j+2] = posb;
        }
        if (posa == -2 || posb == -2) {
            newField[x][j] = posa;
            newField[x][j+1] = -2;
            newField[x][j+2] = posb;
        }
        if (posa <= -5) {
            if (posb > 0) {
                newField[x][j] = posa;
                newField[x][j+1] = posb;
                newField[x][j+2] = 0;
            }
            if (posb <= -4) {
                newField[x][j] = posa;
                newField[x][j+1] = -2;
                newField[x][j+2] = posb;
            }
        }
        if (posb <= -5) {
            if (posa > 0) {
                newField[x][j] = 0;
                newField[x][j+1] = posa;
                newField[x][j+2] = posb;
            }
        }
    }
    
    private void zoomIn() {
        int i = 0;
        while (i < playingField.length) {
            boolean onlyh = true;
            int j = 0;
            while (onlyh && j < playingField[0].length) {
                if (playingField[i][j] != 0 && playingField[i][j] != -3) onlyh = false;
                j++;
            }
            if (onlyh) {
                removeColumn(i);
            }
            else i++;
        }
        i = 0;
        while (i < playingField[0].length) {
            boolean onlyv = true;
            int j = 0;
            while (onlyv && j < playingField.length) {
                if (playingField[j][i] != 0 && playingField[j][i] != -2) onlyv = false;
                j++;
            }
            if (onlyv) {
                removeRow(i);
            }
            else i++;
        }
    }
    
    private void removeColumn(int i) {
        int[][] newField = new int[playingField.length-1][playingField[0].length];
        for (int x = 0; x < newField.length; x++) {
            for (int y = 0; y < playingField[0].length; y++) {
                if (x < i) newField[x][y] = playingField[x][y];
                else newField[x][y] = playingField[x+1][y];
            }
        }
        for (GridCoordinate coord : positions) {
            if (coord != null) {
                if (coord.x >= i) coord.x--;
            }
        }
        playingField = newField;
    }
    
    private void removeRow(int i) {
        int[][] newField = new int[playingField.length][playingField[0].length-1];
        for (int x = 0; x < playingField.length; x++) {
            for (int y = 0; y < newField[0].length; y++) {
                if (y < i) newField[x][y] = playingField[x][y];
                else newField[x][y] = playingField[x][y+1];
            }
        }
        for (GridCoordinate coord : positions) {
            if (coord != null) {
                if (coord.y >= i) coord.y--;
            }
        }
        playingField = newField;
    }

    private void alternateCorners() {
        for (int u = 0; u < theLink.getComponents().size(); u++) {
            int[] or = theLink.orientation(u);
            GridCoordinate coord = positions[or[0]];
            coord.dir = orientDir[u];
            int counter = 0;
            int rundir = (coord.dir)%4;
            if (rundir % 2 != 0) counter++;
            GridCoordinate ncord = new GridCoordinate(coord.x,coord.y,rundir);
            ncord = nextPosition(ncord,rundir);
            boolean cont = true;
            while (cont) {
                if (playingField[ncord.x][ncord.y] == -4 || playingField[ncord.x][ncord.y] == -5) {
                    counter++;
                    if (counter % 2 != 0) playingField[ncord.x][ncord.y] = -5;
                    if (rundir % 2 == 0) rundir = newDirection(ncord,false);
                    else rundir = newDirection(ncord,true);
                }
                ncord = nextPosition(ncord,rundir);
                if (ncord.sameCoord(coord)) cont = false;
            }
        }
    }
    
    private GridCoordinate nextPosition(GridCoordinate ncord, int rundir) {
        GridCoordinate coord = new GridCoordinate(ncord.x,ncord.y,rundir);
        if (rundir == 0) coord.y--;
        if (rundir == 1) coord.x--;
        if (rundir == 2) coord.y++;
        if (rundir == 3) coord.x++;
        return coord;
    }

    private int newDirection(GridCoordinate ncord, boolean northsouth) {
        int changeX = 1;
        int changeY = 0;
        int allowed = -3;
        if (northsouth) {
            changeX = 0;
            changeY = 1;
            allowed = -2;
        }
        int i = ncord.x-changeX;
        int j = ncord.y+changeY;
        boolean good = false;
        if (i>=0 && i < playingField.length && j >= 0 && j < playingField[0].length) {
            if (playingField[i][j] <= -6 || playingField[i][j] == -4 || playingField[i][j] == allowed) good = true;
        }
        if (good) {
            if (northsouth) allowed = 2;
            else allowed = 1;
        }
        else {
            if (northsouth) allowed = 0;
            else allowed = 3;
        }
        return allowed;
    }
    
    private void removeStaircases() {
        boolean keepchecking = true;
        while (keepchecking) {
            int k = playingField.length;
            searchStaircase();
            if (k == playingField.length) keepchecking = false;
        }
    }
    
    private void searchStaircase() {
        int i = playingField.length-1;
        while (i >= 0) { // checks for vertical edges that can be removed
            if (emptyVerticalEdge(i)) {
                int[] edge = edgeInColumn(i);
                int dist = edge[1] - edge[0];
                boolean improvable = true;
                while (improvable) {
                    if (dist == 1) {
                        deStabilize(i, edge);
                        improvable = false;
                    }
                    else {
                        pushAlongVerticalEdge(edge);
                        edge = edgeInColumn(i);
                        int newdist = edge[1] - edge[0];
                        if (newdist == dist) improvable = false;
                        else dist = newdist;
                    }
                }
            }
            i--;
        }
        i = playingField.length-1;
        while (i >= 0) {
            if (emptyHorizontalEdge(i)) {
                int[] edge = edgeInRow(i);
                int dist = edge[1] - edge[0];
                boolean improvable = true;
                while (improvable) {
                    if (dist == 1) {
                        deStabilize(edge, i);
                        improvable = false;
                    }
                    else {
                        pushAlongHorizontalEdge(edge);
                        edge = edgeInRow(i);
                        int newdist = edge[1] - edge[0];
                        if (newdist == dist) improvable = false;
                        else dist = newdist;
                    }
                }
            }
            i--;
        }
    }
    
    private void deStabilize(int[] edge, int i) {
        int[] lEdge = edgeInColumn(edge[0]);
        int[] rEdge = edgeInColumn(edge[1]);
        if (lEdge[0] == rEdge[0] || lEdge[1] == rEdge[1]) return;
        int[][] newField = new int[playingField.length - 1][playingField.length - 1];
        for (int k = 0; k < i; k++) {
            for (int j = 0; j < edge[0]; j++) newField[j][k] = playingField[j][k];
            if (lEdge[0] < i) newField[edge[0]][k] = playingField[edge[0]][k];
            else newField[edge[0]][k] = playingField[edge[0]+1][k];
            for (int j = edge[1]+1; j < playingField.length; j++)
                newField[j-1][k] = playingField[j][k];
        }
        for (int k = i+1; k < playingField.length; k++) {
            for (int j = 0; j < edge[0]; j++) newField[j][k-1] = playingField[j][k];
            if (lEdge[0] < i) newField[edge[0]][k-1] = playingField[edge[0]+1][k];
            else newField[edge[0]][k-1] = playingField[edge[0]][k];
            for (int j = edge[1]+1; j < playingField.length; j++)
                newField[j-1][k-1] = playingField[j][k];
        }
        playingField = newField;
    }
    
    private void deStabilize(int i, int[] edge) {
        int[] uEdge = edgeInRow(edge[0]);
        int[] lEdge = edgeInRow(edge[1]);
        if (uEdge[0] == lEdge[0] || uEdge[1] == lEdge[1]) return;
        int[][] newField = new int[playingField.length - 1][playingField.length - 1];
        for (int k = 0; k < i; k++) {
            System.arraycopy(playingField[k], 0, newField[k], 0, edge[0]);
            if (uEdge[0] < i) newField[k][edge[0]] = playingField[k][edge[0]];
            else newField[k][edge[0]] = playingField[k][edge[0]+1];
            for (int j = edge[1]+1; j < playingField.length; j++)
                newField[k][j-1] = playingField[k][j];
        }
        for (int k = i+1; k < playingField.length; k++) {
            System.arraycopy(playingField[k], 0, newField[k-1], 0, edge[0]);
            if (uEdge[0] < i) newField[k-1][edge[0]] = playingField[k][edge[0]+1];
            else newField[k-1][edge[0]] = playingField[k][edge[0]];
            for (int j = edge[1]+1; j < playingField.length; j++)
                newField[k-1][j-1] = playingField[k][j];
        }
        playingField = newField;
    }
    
    private boolean emptyVerticalEdge(int i) {
        boolean empty = true;
        int[] edge = edgeInColumn(i);
        int j = edge[0];
        int k = edge[1];
        while (empty && j < k) {
            if (playingField[i][j] < -5) empty = false;
            j++;
        }
        return empty;
    }
    
    private boolean emptyHorizontalEdge(int i) {
        boolean empty = true;
        int[] edge = edgeInRow(i);
        int j = edge[0];
        int k = edge[1];
        while (empty && j < k) {
            if (playingField[j][i] < -5) empty = false;
            j++;
        }
        return empty;
    }
    
    private int[] edgeInColumn(int i) { 
        int j = 0;
        int k = playingField.length - 1; 
        boolean incj = true;
        boolean deck = true;
        while (incj || deck) {
            if (playingField[i][j] == -4 || playingField[i][j] == -5) incj = false;
            if (playingField[i][k] == -4 || playingField[i][k] == -5) deck = false;
            if (incj) j++;
            if (deck) k--;
        }
        return new int[] {j, k};
    }
    
    private int[] edgeInRow(int i) {
        int j = 0;
        int k = playingField.length - 1; 
        boolean incj = true;
        boolean deck = true;
        while (incj || deck) {
            if (playingField[j][i] == -4 || playingField[j][i] == -5) incj = false;
            if (playingField[k][i] == -4 || playingField[k][i] == -5) deck = false;
            if (incj) j++;
            if (deck) k--;
        }
        return new int[] {j, k};
    }
    
    private void pushAlongHorizontalEdge(int[] edge) {
        int[] lEdge = edgeInColumn(edge[0]);
        int[] rEdge = edgeInColumn(edge[0]+1);
        if (lEdge[1] < rEdge[0] || lEdge[0] > rEdge[1]) {
            flipColumns(edge[0], lEdge, rEdge);
            return;
        }
        lEdge = edgeInColumn(edge[1]-1);
        rEdge = edgeInColumn(edge[1]);
        if (lEdge[1] < rEdge[0] || lEdge[0] > rEdge[1]) flipColumns(edge[1] - 1, lEdge, rEdge);
    }
    
    private void pushAlongVerticalEdge(int[] edge) {
        int[] uEdge = edgeInRow(edge[0]);
        int[] lEdge = edgeInRow(edge[0]+1);
        if (uEdge[1] < lEdge[0] || uEdge[0] > lEdge[1]) {
            flipRows(edge[0], uEdge, lEdge);
            return;
        }
        uEdge = edgeInRow(edge[1]-1);
        lEdge = edgeInRow(edge[1]);
        if (uEdge[1] < lEdge[0] || uEdge[0] > lEdge[1]) flipRows(edge[1] - 1, uEdge, lEdge);
    }
    
    private void flipRows(int i, int[] uEdge, int[] lEdge) { // assumes one edge is completely to
        int[] helper = new int[uEdge[1]-uEdge[0]+1];         // the right of the other.
        for (int k = uEdge[0]; k <= uEdge[1]; k++) {
            helper[k-uEdge[0]] = playingField[k][i+1];
            if (helper[0] == 0) helper[0] = -2;
            else helper[0] = 0;
            if (helper[helper.length-1] == 0) helper[helper.length-1] = -2;
            else helper[helper.length-1] = 0;
            playingField[k][i+1] = playingField[k][i];
            playingField[k][i] = helper[k-uEdge[0]];
        }
        helper = new int[lEdge[1]-lEdge[0]+1];
        for (int k = lEdge[0]; k <= lEdge[1]; k++) {
            helper[k-lEdge[0]] = playingField[k][i];
            if (helper[0] == 0) helper[0] = -2;
            else helper[0] = 0;
            if (helper[helper.length-1] == 0) helper[helper.length-1] = -2;
            else helper[helper.length-1] = 0;
            playingField[k][i] = playingField[k][i+1];
            playingField[k][i+1] = helper[k-lEdge[0]];
        }
    }
    
    private void flipColumns(int i, int[] lEdge, int[] rEdge) {
        int[] helper = new int[lEdge[1]-lEdge[0]+1];         // 
        for (int k = lEdge[0]; k <= lEdge[1]; k++) {
            helper[k-lEdge[0]] = playingField[i+1][k];
            if (helper[0] == 0) helper[0] = -3;
            else helper[0] = 0;
            if (helper[helper.length-1] == 0) helper[helper.length-1] = -3;
            else helper[helper.length-1] = 0;
            playingField[i+1][k] = playingField[i][k];
            playingField[i][k] = helper[k-lEdge[0]];
        }
        helper = new int[rEdge[1]-rEdge[0]+1];
        for (int k = rEdge[0]; k <= rEdge[1]; k++) {
            helper[k-rEdge[0]] = playingField[i][k];
            if (helper[0] == 0) helper[0] = -3;
            else helper[0] = 0;
            if (helper[helper.length-1] == 0) helper[helper.length-1] = -3;
            else helper[helper.length-1] = 0;
            playingField[i][k] = playingField[i+1][k];
            playingField[i+1][k] = helper[k-rEdge[0]];
        }
    }
    
    private void pathToNumbers() {
        for (int i = 0; i < orientDir.length; i++) {
            int[] or = theLink.orientation(i);
            int[] pos = positionOfCrossing(or[0]);
            int[] paths = theLink.getPath(or[0]);
            int start = 4 + or[1] - orientDir[i];
            fillTheField(pos, 0, paths[start % 4]);
            fillTheField(pos, 1, paths[(start+1) % 4]);
            fillTheField(pos, 2, paths[(start+2) % 4]);
            fillTheField(pos, 3, paths[(start+3) % 4]);
        }
        int[] or = theLink.orientation(0);
        for (int i = 1; i < theLink.crossingNumber(); i++) {
            int p = (i+or[0]) % theLink.crossingNumber();
            int[] pos = positionOfCrossing(p);
            int dir = theStartOf(pos, p);
            fillTheField(pos, dir, theLink.getPath(p, 0));
            fillTheField(pos, (dir+1)%4, theLink.getPath(p, 1));
            fillTheField(pos, (dir+2)%4, theLink.getPath(p, 2));
            fillTheField(pos, (dir+3)%4, theLink.getPath(p, 3));
        }
    }

    private int theStartOf(int[] pos, int p) {
        int[] paths = theLink.getPath(p);
        boolean found = false;
        int i = 0;
        int dir = 0;
        while (!found) {
            dir = 0;
            while (!found && dir < 4) {
                int[] q =  nextPos(pos, dir);
                if (playingField[q[0]][q[1]] == paths[i]) found = true;
                else dir++;
            }
            if (!found) i++;
        }
        return (4-i+dir)%4;
    }
    
    private void fillTheField(int[] pos, int dir, int num) {
        boolean cont = true;
        int[] p = nextPos(pos, dir);
        while (cont) {
            int u = playingField[p[0]][p[1]];
            if (u < -5 || u > 0) cont = false;
            else {
                playingField[p[0]][p[1]] = num;
                if (u == -4 || u == -5) {
                    int[] q = nextPos(p, (dir + 1)% 4);
                    if (playingField[q[0]][q[1]] != 0) dir = (dir + 1) % 4;
                    else dir = (dir + 3) % 4;
                }
            }
            p = nextPos(p, dir);
        }
        
    }
    
    private int[] nextPos(int[] pos, int dir) {
        int x = pos[0];
        int y = pos[1];
        if (dir % 2 == 0) y = y - 1 + dir;
        else x = x - 2 + dir;
        return new int[] {x, y};
    }
    
    private int[] positionOfCrossing(int cr) {
        boolean found = false;
        int i = 0;
        int j = 0;
        while (!found) {
            j = 0;
            while (!found && j < playingField.length) {
                if (playingField[i][j] == -(cr+6)) found = true;
                else j++;
            }
            if (!found) i++;
        }
        return new int[] {i, j};
    }

    private void addVertices(SComplex complex) {
        for (int i = 0; i < playingField.length; i++) {
            for (int j = 0; j < playingField.length; j++) {
                int p = playingField[i][j];
                if (p != 0) {
                    int t = 1;
                    int v = p;
                    if (p < 0) {
                        t = 0;
                        v = -p-6;
                    }
                    Vertex vert = new Vertex(i * factor, j * factor, t, v, false);
                    complex.addVertex(vert);
                }
            }
        }
    }

    private void addEdges(SComplex complex) {
        for (int j = 0; j < playingField.length - 1; j++) { // deals with horizontal edges
            for (int i = 0; i < playingField.length; i++) {
                if (playingField[i][j] != 0 && playingField[i+1][j] != 0) {
                    Vertex fVert = complex.vertexAt(i * factor, j * factor);
                    Vertex sVert = complex.vertexAt((i + 1.0) * factor, j * factor);
                    int v = fVert.label;
                    if (fVert.type == 0) v = sVert.label;
                    Vertex bVert = new Vertex((fVert.x+sVert.x)/2.0, fVert.y, 4, v, false);
                    addTwoEdges(fVert, sVert, bVert, complex);
                    
                }
            }
        }
        for (int i = 0; i < playingField.length; i++) { // deals with horizontal edges
            for (int j = 0; j < playingField.length - 1; j++) {
                if (playingField[i][j] != 0 && playingField[i][j+1] != 0) {
                    Vertex fVert = complex.vertexAt(i * factor, j * factor);
                    Vertex sVert = complex.vertexAt(i * factor, (j + 1.0) * factor);
                    int v = fVert.label;
                    if (fVert.type == 0) v = sVert.label;
                    Vertex bVert = new Vertex(fVert.x, (fVert.y + sVert.y)/2.0, 4, v, false);
                    addTwoEdges(fVert, sVert, bVert, complex);
                }
            }
        }
    }
    
    private void addTwoEdges(Vertex fVert, Vertex sVert, Vertex bVert, SComplex complex) {
        Edge fEdge = new Edge(fVert, bVert);
        Edge sEdge = new Edge(sVert, bVert);
        fVert.comb.add(fEdge);
        sVert.comb.add(sEdge);
        bVert.comb.add(fEdge);
        bVert.comb.add(sEdge);
        complex.addVertex(bVert);
        complex.addEdge(fEdge);
        complex.addEdge(sEdge);
    }
    
}
