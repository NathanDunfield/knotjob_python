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

package knotjob.homology.oddkhov;

import java.util.Arrays;

/**
 *
 * @author dirk
 */
public class OddSurgery {
    
    private final int[] fpath;
    private final int[] spath;
    private final int endDiagram;
    private final boolean leftTurn;
    //private final int qdeg;
    
    /*
    This is either a surgery directed from the fpath to the spath (fpath and spath refering to the list of paths in an OddComplex,
    or the dotting of the fpath, in which case spath is null or contains only -1.
    */

    public OddSurgery(int fpOne, int fpTwo, int spOne, int spTwo, int std, boolean lt) {
        fpath = new int[] { fpOne, fpTwo };
        spath = new int[] { spOne, spTwo };
        endDiagram = std;
        leftTurn = lt;
        //qdeg = qd;
    }
    
    public OddSurgery(int[] fp, int[] sp, int std, boolean lt) {
        fpath = fp;
        spath = sp;
        endDiagram = std;
        leftTurn = lt;
        //qdeg = qd;
    }
    
    public int[] getFPath() {
        return fpath;
    }
    
    public int[] getSPath() {
        return spath;
    }
    
    public int getFPath(int i) {
        return fpath[i];
    }
    
    public int getSPath(int i) {
        return spath[i];
    }
    
    @Override
    public String toString() {
        String turn = "right";
        if (leftTurn) turn = "left";
        return turn+" ["+Arrays.toString(fpath)+", "+Arrays.toString(spath)+"] "+endDiagram;//+" {"+qdeg+"}";
    }

    public int getEnd() {
        return endDiagram;
    }

    public boolean getTurn() {
        return leftTurn;
    }
    
    /*public int qdeg() {
        return qdeg;
    }// */

    boolean involves(int thePath) {
        return (fpath[0] == thePath | spath[0] == thePath);
    }

    int smallestEnd() {
        if (fpath[0] < spath[0]) return fpath[0];
        return spath[0];
    }

    boolean sameAs(OddSurgery surgery) {
        return (fpath[0] == surgery.fpath[0] & fpath[1] == surgery.fpath[1] & spath[0] == surgery.spath[0] & 
                spath[1] == surgery.spath[1]);
    }
    
}
