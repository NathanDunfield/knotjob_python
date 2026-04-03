/*

Copyright (C) 2022 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import knotjob.homology.Generator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class OddStableGenerator<R extends Ring<R>> extends OddGenerator<R> {
    
    private final ArrayList<Boolean> position;
    
    public OddStableGenerator(int dg, int hd, int qd, ArrayList<Boolean> pos) {
        super(dg, hd, qd);
        position = pos;
    }
    
    public OddStableGenerator(int dg, int hd, int qd, boolean bol) {
        super(dg, hd, qd);
        position = new ArrayList<Boolean>(1);
        position.add(bol);
    }
    
    @Override
    public void output(ArrayList<Generator<R>> nextLevel) {
        System.out.println("Diagram "+diagram);
        System.out.println("hdeg = "+hdeg);
        System.out.println("qdeg = "+qdeg);
        System.out.println("Position = "+position);
        for (int i = 0; i < bMor.size(); i++) ((OddArrow<R>) bMor.get(i)).output(nextLevel);
    }
    
    public ArrayList<Boolean> clonePosition() {
        ArrayList<Boolean> clone = new ArrayList<Boolean>(position.size()+1);
        for (Boolean bol : position) clone.add(bol);
        return clone;
    }
    
    public ArrayList<Boolean> getPosition() {
        return position;
    }

    public Boolean getPosition(int i) {
        return position.get(i);
    }
    
}
