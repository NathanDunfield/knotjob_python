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
import knotjob.homology.QGenerator;
import knotjob.rings.Ring;

/**
 *
 * @author dirk
 * @param <R>
 */
public class OddGenerator<R extends Ring<R>> extends QGenerator<R> {
    
    protected final int diagram;
    
    public OddGenerator(int dg, int hd, int qd) {
        super(hd, qd);
        diagram = dg;
    }
    
    @Override
    public void output(ArrayList<Generator<R>> nextLevel) {
        System.out.println("Diagram "+diagram);
        System.out.println("hdeg = "+hdeg);
        System.out.println("qdeg = "+qdeg);
        for (int i = 0; i < bMor.size(); i++) ((OddArrow<R>) bMor.get(i)).output(nextLevel);
    }

    public int getDiagram() {
        return diagram;
    }

    @Override
    public OddArrow<R> getTopArrow(int i) {
        return (OddArrow<R>) tMor.get(i);
    }
    
    public OddArrow<R> getBotArrow(int i) {
        return (OddArrow<R>) bMor.get(i);
    }
    
    public Arrow<R> getBArrow(int i) {
        return bMor.get(i);
    }
    
    public void addBotArrow(OddArrow<R> arrow) {
        bMor.add(arrow);
    }
    
    public void addTopArrow(OddArrow<R> arrow) {
        tMor.add(arrow);
    }

    public int getBotArrowSize() {
        return bMor.size();
    }
    
    public void removeBotArrow(OddArrow<R> arr) {
        bMor.remove(arr);
    }
    
    public void removeTopArrow(OddArrow<R> arr) {
        tMor.remove(arr);
    }
    
}
