/*

Copyright (C) 2025 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.slthree.univ;

import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.slthree.SlTArrow;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class GenSlTComplex<R extends Ring<R>> extends UnivSlTComplex<R> {
    
    private final R t;
    
    public GenSlTComplex(int[] pts, int c, int factor, R unt, R tt, int s, DialogWrap frm, AbortInfo abf) {
        super(pts, c, factor, unt, s, frm, abf);
        t = tt;
    }
    
    public GenSlTComplex(boolean red, R unt, R tt, AbortInfo abf, DialogWrap frm) {
        super(red, unt, abf, frm);
        t = tt;
    }

    public GenSlTComplex(R unt, R tt, DialogWrap frm, AbortInfo abf, int s, 
            int p, int n) {
        super(unt, frm, abf, s, p, n);
        t = tt;
    }
    
    @Override
    protected void simplifyArrow(SlTArrow<R> arr) {
        if (abInf.isAborted()) return;
        //arr.simplifyFoams(sType, unit); // different to UnivSlTComplex
        arr.simplifyFoams(sType, unit, t);
        if (arr.noFoams()) {
            removeArrow(arr);
            return;
        }
        if (abInf.isAborted()) return;
        arr.checkCylinders(sType, unit); 
        //arr.dotChecker(sType, unit); // different to UnivSlTComplex
        arr.dotChecker(sType, unit, t);
        if (arr.noFoams()) {
            removeArrow(arr);
            return;
        }
        if (abInf.isAborted()) return;
        arr.checkCombineFoams();
        if (arr.noFoams()) removeArrow(arr);
    }
    
}
