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

package knotjob.homology.evenkhov;

import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class Cobordism<R extends Ring<R>> {

    private R value;
    private final int dottings;
    private long surgeries;  
    
    public Cobordism(R val, int dot, int sur) {
        value = val;
        dottings = dot;
        surgeries = sur;
    }
    
    public Cobordism(R val, int dot, long sur) {
        value = val;
        dottings = dot;
        surgeries = sur;
    }
    
    public Cobordism(int dot, R val) {
        value = val;
        dottings = dot;
    }
    
    public void output() {
        System.out.println(value + "  dot : "+dottings+"   surgery : "+surgeries);
    }
    
    public int getDottings() {
        return dottings;
    }
    
    public long getSurgery() {
        return surgeries;
    }
    
    public R getValue() {
        return value;
    }
    
    public void setValue(R nv) {
        value = nv;
    }
    
    public void addValue(R val) {
        value = value.add(val);
    }
    
    public void setSurgery(long surg) {
        surgeries = surg;
    }
    
}
