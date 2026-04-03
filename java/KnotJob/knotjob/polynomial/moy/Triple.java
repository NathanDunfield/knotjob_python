/*

Copyright (C) 2024 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.polynomial.moy;

/**
 *
 * @author Dirk
 * @param <G>
 */
public class Triple<G> {
    
    private G objOne;
    private G objTwo;
    private G objThr; // three things in a cyclic order
    
    public Triple(G one, G two, G thr) {
        objOne = one;
        objTwo = two;
        objThr = thr;
    }
    
    public Triple(G[] t) {
        objOne = t[0];
        objTwo = t[1];
        objThr = t[2];
    }
    
    public boolean equals(Triple<G> trip) {
        if (objOne.equals(trip.objOne) && objTwo.equals(trip.objTwo) && objThr.equals(trip.objThr))
            return true;
        if (objOne.equals(trip.objTwo) && objTwo.equals(trip.objThr) && objThr.equals(trip.objOne))
            return true;
        return objOne.equals(trip.objThr) && objTwo.equals(trip.objOne) && objThr.equals(trip.objTwo);
    }
    
    public boolean contains(G obj) {
        if (objOne.equals(obj)) return true;
        if (objTwo.equals(obj)) return true;
        return objThr.equals(obj);
    }
    
    public void replace(G oObj, G nObj) {
        if (objOne.equals(oObj)) {
            objOne = nObj;
            return;
        }
        if (objTwo.equals(oObj)) {
            objTwo = nObj;
            return;
        }
        if (objThr.equals(oObj)) objThr = nObj;
    }
    
    public G getOne() {
        return objOne;
    }
    
    public G getTwo() {
        return objTwo;
    }
    
    public G getThree() {
        return objThr;
    }
    
    @Override
    public String toString() {
        return "<"+objOne+", "+objTwo+", "+objThr+">";
    }
    
}
