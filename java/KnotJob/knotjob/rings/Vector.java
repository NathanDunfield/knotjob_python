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

package knotjob.rings;

import java.util.ArrayList;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class Vector<R extends Ring<R>> {
    
    private final ArrayList<R> columns;
    private final R unit;
    
    public Vector(int n, R unt) {
        unit = unt;
        columns = new ArrayList<R>(n);
        for (int i = 0; i < n; i++) columns.add(unit.getZero());
    }
    
    public Vector<R> add(Vector<R> a) {
        Vector<R> vec = new Vector<R>(columns.size(), unit);
        for (int i = 0; i < columns.size(); i++) vec.set(i, columns.get(i).add(a.columns.get(i)));
        return vec;
    }
    
    public Vector<R> multiply(R val) {
        Vector<R> vec = new Vector<R>(columns.size(), unit);
        for (int i = 0; i < columns.size(); i++) vec.set(i, columns.get(i).multiply(val));
        return vec;
    }
    
    public int size() {
        return columns.size();
    }
    
    public R get(int i) {
        return columns.get(i);
    }
    
    public void remove(int i) {
        columns.remove(i);
    }
    
    public void set(int i, R ent) {
        columns.set(i, ent);
    }
    
    public int smallestEntry() {
        R smallest = unit.getZero();
        int sm = 0;
        for (int i = 0; i < columns.size(); i++) {
            if (!columns.get(i).isZero()) {
                if (smallest.isZero()) {
                    smallest = columns.get(i);
                    sm = i;
                }
                else if (smallest.isBigger(columns.get(i))) {
                    smallest = columns.get(i);
                    sm = i;
                }
            }
        }
        return sm;
    }
    
    public String toString(int lng) {
        String info = "";
        for (int i = 0; i < size(); i++) {
            String inf = columns.get(i).toString();
            while (inf.length() < lng+1) inf = " "+inf;
            info = info + inf;
        }
        return info;
    }
    
    @Override
    public String toString() {
        String info = "";
        int longest = 0;
        String[] inf = new String[size()];
        for (int i = 0; i < size(); i++) {
            inf[i] = columns.get(i).toString();
            if (inf[i].length() > longest) longest = inf[i].length();
        }
        for (int i = 0; i < size(); i++) {
            while (inf[i].length() < longest+1) inf[i] = " "+inf[i];
            info = info + inf[i];
        }
        return info;
    }
}
