/*

Copyright (C) 2022 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.stabletype.sinv;

import java.util.ArrayList;
import knotjob.stabletype.FlowGenerator;
import knotjob.rings.Ring;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class FlowSGenerator<R extends Ring<R>> extends FlowGenerator<R>  {
    
    private final ArrayList<Arrow<R>> bqArr;
    private final ArrayList<Arrow<R>> tqArr;
    private final int qdeg;
    
    public FlowSGenerator(int hd, int qd) {
        super(hd);
        bqArr = new ArrayList<Arrow<R>>();
        tqArr = new ArrayList<Arrow<R>>();
        qdeg = qd;
    }
    
    public int qdeg() {
        return qdeg;
    }
    
    public void addBotqArr(Arrow<R> arr) {
        bqArr.add(arr);
    }
    
    public void addTopqArr(Arrow<R> arr) {
        tqArr.add(arr);
    }
    
    public ArrayList<Arrow<R>> getTopqArr() {
        return tqArr;
    }
    
    public ArrayList<Arrow<R>> getBotqArr() {
        return bqArr;
    }
    
    @Override
    public void output(ArrayList<FlowGenerator<R>> nextLevel, ArrayList<FlowGenerator<R>> vnextLevel) {
        System.out.println("qdeg = "+qdeg);
        super.output(nextLevel, vnextLevel);
        if (nextLevel == null) return;
        ArrayList<Generator<R>> nxLevel = new ArrayList<Generator<R>>();
        for (FlowGenerator<R> gn : nextLevel) nxLevel.add(gn);
        for (int i = 0; i < bqArr.size(); i++) {
            System.out.print("Q");
            bqArr.get(i).output(nxLevel);
        }
    }
    
}
