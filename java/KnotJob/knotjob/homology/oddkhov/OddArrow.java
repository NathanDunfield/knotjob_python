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
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.rings.Ring;

/**
 *
 * @author dirk
 * @param <R>
 */
public class OddArrow<R extends Ring<R>> extends Arrow<R> {
    
    private final ArrayList<Chronology<R>> chronologies;
    
    public OddArrow(OddGenerator<R> bo, OddGenerator<R> to) {
        super(bo, to, null);
        chronologies = new ArrayList<Chronology<R>>();
    }

    public OddArrow(OddGenerator<R> bGen, OddGenerator<R> tGen, Chronology<R> surgery) {
        super(bGen, tGen, null);
        chronologies = new ArrayList<Chronology<R>>(1);
        chronologies.add(surgery);
    }
    
    public OddArrow(OddGenerator<R> bGen, OddGenerator<R> tGen, ArrayList<Chronology<R>> chrons) {
        super(bGen, tGen, null);
        chronologies = chrons;
    }
    
    public void addChronology(Chronology<R> chr) {
        chronologies.add(chr);
    }
    
    public int chronologySize() {
        return chronologies.size();
    }
    
    public ArrayList<Chronology<R>> getChronologies() {
        return chronologies;
    }
    
    public Chronology<R> getChronology(int i) {
        return chronologies.get(i);
    }
    
    public int getBotDiagram() {
        return ((OddGenerator<R>) bObj).getDiagram();
    }
    
    @Override
    public OddGenerator<R> getBotGenerator() {
        return (OddGenerator<R>) bObj;
    }
    
    @Override
    public OddGenerator<R> getTopGenerator() {
        return (OddGenerator<R>) tObj;
    }
    
    public void removeChronology(Chronology<R> chron) {
        chronologies.remove(chron);
    }
    
    @Override
    public R getValue() {
        return chronologies.get(0).getValue();
    }
    
    @Override
    public void setValue(R val) {
        Chronology<R> chron = new Chronology<R>(val, new ArrayList<Integer>(0));
        chronologies.clear();
        chronologies.add(chron);
    }
    
    @Override
    public void addValue(R val) {
        setValue(chronologies.get(0).getValue().add(val));
    }
    
    @Override
    public void output(ArrayList<Generator<R>> nextLevel) {
        System.out.println("To : "+nextLevel.indexOf(tObj));
        for (Chronology<R> chrono : chronologies) chrono.output();
    }

    public boolean isEmpty() {
        return chronologies.isEmpty();
    }
    
}
