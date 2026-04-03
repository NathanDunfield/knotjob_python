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
 */
public class DoubleVector {
    
    private final ArrayList<Double> columns;
    
    public DoubleVector(int n) {
        columns = new ArrayList<Double>(n);
        for (int i = 0; i < n; i++) columns.add(0.0);
    }
    
    public DoubleVector add(DoubleVector a) {
        DoubleVector vec = new DoubleVector(columns.size());
        for (int i = 0; i < columns.size(); i++) vec.set(i, columns.get(i)+(a.columns.get(i)));
        return vec;
    }
    
    public DoubleVector multiply(Double val) {
        DoubleVector vec = new DoubleVector(columns.size());
        for (int i = 0; i < columns.size(); i++) vec.set(i, columns.get(i) * val);
        return vec;
    }
    
    public int size() {
        return columns.size();
    }
    
    public Double get(int i) {
        return columns.get(i);
    }
    
    public void remove(int i) {
        columns.remove(i);
    }
    
    public void set(int i, Double ent) {
        columns.set(i, ent);
    }
    
    public int smallestEntry() {
        Double smallest = 0.0;
        int sm = 0;
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i)!=0.0) {
                if (smallest==0) {
                    smallest = columns.get(i);
                    sm = i;
                }
                else if (Math.abs(smallest) > Math.abs(columns.get(i))) {
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
