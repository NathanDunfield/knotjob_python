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

import java.util.ArrayList;
import knotjob.rings.Ring;

/**
 *
 * @author dirk
 * @param <R>
 */
public class Chronology<R extends Ring<R>> {
    
    private final R value;
    private final ArrayList<Integer> dottings;
    private final ArrayList<OddSurgery> surgeries;
    
    public Chronology(R val, ArrayList<Integer> dots) {
        value = val;
        dottings = new ArrayList<Integer>();
        for (int d : dots) dottings.add(d);
        surgeries = new ArrayList<OddSurgery>();
    }

    public Chronology(int dot, R val) {
        value = val;
        dottings = new ArrayList<Integer>();
        dottings.add(dot);
        surgeries = new ArrayList<OddSurgery>();
    }
    
    public Chronology(R val, ArrayList<Integer> dts, ArrayList<OddSurgery> surgs) {
        value = val;
        dottings = new ArrayList<Integer>(dts.size());
        for (int d : dts) dottings.add(d);
        surgeries = new ArrayList<OddSurgery>();
        for (OddSurgery surg : surgs) surgeries.add(surg);
    }
    
    public Chronology(R val, int[] fp, int[] sp, int std, boolean lt) {
        value = val;
        dottings = new ArrayList<Integer>();
        surgeries = new ArrayList<OddSurgery>(1);
        surgeries.add(new OddSurgery(fp[0], fp[1],sp[0], sp[1], std, lt));
    }

    public Chronology(Chronology<R> chron, ArrayList<Integer> dots) {
        value = chron.value;
        dottings = new ArrayList<Integer>();
        for (int d : dots) dottings.add(d);
        surgeries = new ArrayList<OddSurgery>(chron.surgerySize()+1);
        for (OddSurgery surg : chron.surgeries) surgeries.add(surg);
    }
    
    public Chronology(Chronology<R> chron, ArrayList<Integer> dots, R val) {
        value = val;
        dottings = new ArrayList<Integer>();
        for (int d : dots) dottings.add(d);
        surgeries = new ArrayList<OddSurgery>(chron.surgerySize()+1);
        for (OddSurgery surg : chron.surgeries) surgeries.add(surg);
    }
    
    public int surgerySize() {
        return surgeries.size();
    }
    
    public OddSurgery getSurgery(int i) {
        return surgeries.get(i);
    }
    
    public R getValue() {
        return value;
    }
    
    public ArrayList<Integer> getDottings() {
        return dottings;
    }
    
    public void output() {
        System.out.println(value + "  dot : "+dottings+"   surgery : "+surgeries);
    }

    public ArrayList<OddSurgery> getSurgeries() {
        return surgeries;
    }

    public void addSurgery(OddSurgery nSurg) {
        surgeries.add(nSurg);
    }

    public int getDotting(int i) {
        return dottings.get(i);
    }

    public void removeSurgery(int j) {
        surgeries.remove(j);
    }
    
}
