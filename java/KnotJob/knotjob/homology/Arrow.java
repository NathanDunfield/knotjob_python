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
import knotjob.rings.Ring;

/**
 *
 * @author dirk
 * @param <R>
 */
public class Arrow<R extends Ring<R>> {
    
    protected R value;
    protected final Generator<R> bObj;
    protected final Generator<R> tObj;
    
    public Arrow(Generator<R> bo, Generator<R> to, R val) {
        value = val;
        bObj = bo;
        tObj = to;
    }
    
    public Generator<R> getBotGenerator() {
        return bObj;
    }
    
    public Generator<R> getTopGenerator() {
        return tObj;
    }
    
    public void addValue(R ad) {
        value = value.add(ad);
    }
    
    public R getValue() {
        return value;
    }
    
    public void setValue(R nv) {
        value = nv;
    }
    
    public void output(ArrayList<Generator<R>> nextLevel) {
        System.out.println("To : "+nextLevel.indexOf(tObj)+" with Value : "+value);
    }
}
