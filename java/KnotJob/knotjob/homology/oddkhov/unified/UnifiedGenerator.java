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

package knotjob.homology.oddkhov.unified;

import java.util.ArrayList;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.homology.QGenerator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class UnifiedGenerator<R extends Ring<R>> extends QGenerator<R> {
    
    private final ArrayList<Arrow<R>> outArrows;
    private final ArrayList<Arrow<R>> inArrows;
    
    public UnifiedGenerator(int hd, int qd) {
        super(hd, qd);
        outArrows = new ArrayList<Arrow<R>>();
        inArrows = new ArrayList<Arrow<R>>();
    }
    
    public void addOutArrow(Arrow<R> arr) {
        outArrows.add(arr);
    }
    
    public void addInArrow(Arrow<R> arr) {
        inArrows.add(arr);
    }
    
    public ArrayList<Arrow<R>> getInArrows() {
        return inArrows;
    }
    
    public ArrayList<Arrow<R>> getOutArrows() {
        return outArrows;
    }
    
    public void output(ArrayList<Generator<R>> nextLevel, UnifiedChainComplex<R> complex, int k) {
        System.out.println("hdeg = "+hdeg);
        System.out.println("qdeg = "+qdeg);
        for (int i = 0; i < bMor.size(); i++) bMor.get(i).output(nextLevel);
        if (complex != null) {
            System.out.println("Chain map");
            for (int i = 0; i < outArrows.size(); i++) {
                Arrow<R> arr = outArrows.get(i);
                Generator<R> gen = arr.getTopGenerator();
                int l = complex.getLevel((UnifiedGenerator<R>) gen);
                arr.output(complex.getGenerators(l));
            }
        }
    }
    
}
