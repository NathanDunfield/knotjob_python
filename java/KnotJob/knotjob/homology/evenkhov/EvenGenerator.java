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

import java.util.ArrayList;
import knotjob.homology.Generator;
import knotjob.homology.QGenerator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class EvenGenerator<R extends Ring<R>> extends QGenerator<R> {
    
    protected final int diagram;
    
    public EvenGenerator(int dg, int hd, int qd) {
        super(hd, qd);
        diagram = dg;
    }
    
    @Override
    public EvenArrow<R> getTopArrow(int i) {
        return (EvenArrow<R>) tMor.get(i);
    }
    
    public EvenArrow<R> getBotArrow(int i) {
        return (EvenArrow<R>) bMor.get(i);
    }
    
    public void clearTopArrow() {
        tMor.clear();
    }

    public void clearBotArrow() {
        bMor.clear();
    }
    
    @Override
    public void output(ArrayList<Generator<R>> nextLevel) {
        System.out.println("Diagram "+diagram);
        System.out.println("hdeg = "+hdeg);
        System.out.println("qdeg = "+qdeg);
        for (int i = 0; i < bMor.size(); i++) ((EvenArrow<R>) bMor.get(i)).output(nextLevel);
    }

    public int getDiagram() {
        return diagram;
    }

    public int tMorSize() {
        return tMor.size();
    }

    public int bMorSize() {
        return bMor.size();
    }
    
}
