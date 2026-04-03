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

package knotjob.homology.evenkhov;

import java.util.ArrayList;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class CobordInfo<R extends Ring<R>> {
    
    private R value;
    private int dottings;
    private ArrayList<Integer> surgeries;
    
    public CobordInfo(R val, int dot, ArrayList<Integer> surgs) {
        value = val;
        dottings = dot;
        surgeries = surgs;
    }
    
    public CobordInfo(R val, int dot, int size) {
        value = val;
        dottings = dot;
        surgeries = new ArrayList<Integer>(size);
    }
    
    public void addValue(R summand) {
        value = value.add(summand);
    }
    
    public void addSurgery(int w) {
        surgeries.add(w);
    }
    
    public int getDottings() {
        return dottings;
    }
    
    public ArrayList<Integer> getSurgeries() {
        return surgeries;
    }
    
    public R getValue() {
        return value;
    }
    
    public int getSurgery(int i) {
        return surgeries.get(i);
    }
    
    public void setDottings(int i) {
        dottings = i;
    }
    
    public void setSurgery(int i, int h) {
        surgeries.set(i, h);
    }
    
    public void setSurgeries(ArrayList<Integer> surgs) {
        surgeries = surgs;
    }
    
}
