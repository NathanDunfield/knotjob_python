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

/**
 *
 * @author Dirk
 */
public class GridPath {
    
    ArrayList<GridCoordinate> thePath;
    ArrayList<GridCoordinate> currentBest;
    int beginner;
    int number;
    int factor;
    
    public GridPath(int[][] theField, int beginshere, int numb, boolean same) {
        beginner = beginshere;
        factor = 10;
        if (same) factor = 1;
        number = numb;
        thePath = bestPath(theField, endCoordinate(theField, number));
    }

    private GridCoordinate endCoordinate(int[][] theField, int number) {
        boolean found = false;
        int x = 0;
        int y = 0;
        while (!found & x < theField.length) {
            y = 0;
            while (!found & y < theField[0].length) {
                if (theField[x][y] % (factor * beginner) == number) found = true;
                else y++;
            }
            if (!found) x++;
        }
        int dir = directionOf(x,y,theField);
        return new GridCoordinate(x,y,dir);
    }

    private ArrayList<GridCoordinate> bestPath(int[][] theField, GridCoordinate endCoord) {
        int[][] triedField = new int[theField.length][theField[0].length]; 
        ArrayList<GridCoordinate> firstPositions = new ArrayList<GridCoordinate>();
        firstPositions.add(endCoord);
        triedField[endCoord.x][endCoord.y] = 1;
        boolean cont = true;
        boolean found = false;
        int counter = 2;
        GridCoordinate start = null;
        while (cont) {
            ArrayList<GridCoordinate> seconPos = new ArrayList<GridCoordinate>();
            for (GridCoordinate starter : firstPositions) {
                if (theField[starter.x][starter.y] % (factor * beginner) == (beginner + number) % (factor * beginner)) {
                    cont = false;
                    found = true;
                    start = startingPosition(starter,theField);
                }
                else {
                    GridCoordinate nCoord = new GridCoordinate(starter.x,starter.y-1,0);
                    if (legalCoordinate(nCoord,theField,triedField)) setCoordinate(nCoord,seconPos,triedField,counter);
                    GridCoordinate sCoord = new GridCoordinate(starter.x,starter.y+1,0);
                    if (legalCoordinate(sCoord,theField,triedField)) setCoordinate(sCoord,seconPos,triedField,counter);
                    GridCoordinate wCoord = new GridCoordinate(starter.x-1,starter.y,0);
                    if (legalCoordinate(wCoord,theField,triedField)) setCoordinate(wCoord,seconPos,triedField,counter);
                    GridCoordinate eCoord = new GridCoordinate(starter.x+1,starter.y,0);
                    if (legalCoordinate(eCoord,theField,triedField)) setCoordinate(eCoord,seconPos,triedField,counter);
                }
            }
            counter++;
            if (seconPos.isEmpty()) cont = false;
            firstPositions = seconPos;
        }
        if (!found) return null;
        return shortestPath(start,endCoord,triedField);
    }

    private int directionOf(int x, int y, int[][] theField) {
        if (x > 0 && theField[x-1][y] < -5) return 1;
        if (x < theField.length-1 && theField[x+1][y] < -5) return 3;
        if (y > 0 && theField[x][y-1] < -5) return 0;
        return 2;
    }

    private boolean legalCoordinate(GridCoordinate nCoord, int[][] theField, int[][] triedField) {
        if (nCoord.x < 0 || nCoord.x >= theField.length || nCoord.y < 0 || nCoord.y >= theField[0].length) return false;
        if (triedField[nCoord.x][nCoord.y] > 0) return false;
        if (theField[nCoord.x][nCoord.y] % (factor * beginner) == (beginner + number) % (factor * beginner)) return true;
        return theField[nCoord.x][nCoord.y] == 0;
    }

    private void setCoordinate(GridCoordinate nCoord, ArrayList<GridCoordinate> coords, 
            int[][] triedField, int counter) {
        coords.add(nCoord);
        triedField[nCoord.x][nCoord.y] = counter;
    }

    private ArrayList<GridCoordinate> shortestPath(GridCoordinate coord, GridCoordinate endCoord, int[][] triedField) {
        ArrayList<GridCoordinate> shortPath = new ArrayList<GridCoordinate>();
        shortPath.add(coord);
        if (endCoord.sameCoord(coord)) return shortPath;
        GridCoordinate runner = nextBest(coord,triedField);
        shortPath.add(runner);
        if (runner.sameCoord(endCoord)) return shortPath;
        while (!runner.sameFlatCoord(endCoord)) {
            runner = nextBest(runner,triedField);
            shortPath.add(runner);
        }
        if (!runner.sameCoord(endCoord)) shortPath.add(endCoord);
        return shortPath;
    }

    private GridCoordinate nextBest(GridCoordinate coord, int[][] triedField) {
        int counter = triedField[coord.x][coord.y];
        if (counter == 1) return coord;
        ArrayList<Integer> directions = new ArrayList<Integer>();
        for (int i = 0; i < 4; i++) {
            if (acceptableCoordinate(i,counter,coord,triedField)) directions.add(i); 
        }
        if (directions.contains(coord.dir)) return contCoord(coord);
        return new GridCoordinate(coord.x,coord.y,directions.get(0));
    }

    private boolean acceptableCoordinate(int dir, int counter, GridCoordinate coord, int[][] triedField) {
        int x = coord.x;
        int y = coord.y;
        if (dir == 0) y--;
        if (dir == 1) x--;
        if (dir == 2) y++;
        if (dir == 3) x++;
        if (x < 0 || x >= triedField.length || y < 0 || y >= triedField[0].length) return false;
        return triedField[x][y] == counter -1;
    }

    private GridCoordinate contCoord(GridCoordinate coord) {
        int x = coord.x;
        int y = coord.y;
        if (coord.dir == 0) y--;
        if (coord.dir == 1) x--;
        if (coord.dir == 2) y++;
        if (coord.dir == 3) x++;
        return new GridCoordinate(x,y,coord.dir);
    }

    private GridCoordinate startingPosition(GridCoordinate starter, int[][] theField) {
        GridCoordinate start = new GridCoordinate(starter.x,starter.y,starter.dir);
        if (starter.y > 0 && theField[starter.x][starter.y-1] == -5) start.dir = 2;
        if (starter.y < theField[0].length - 1 && theField[starter.x][starter.y+1] == -5) start.dir = 0;
        if (starter.x > 0 && theField[starter.x-1][starter.y] == -5) start.dir = 3;
        if (starter.x < theField.length - 1 && theField[starter.x+1][starter.y] == -5) start.dir = 1;
        return start;
    }
    
}
