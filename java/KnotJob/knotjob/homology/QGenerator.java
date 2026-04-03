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
 * @author Dirk
 * @param <R>
 */
public class QGenerator<R extends Ring<R>> extends Generator<R> {
    
    protected final int qdeg;
    
    public QGenerator(int hd, int qd) {
        super(hd);
        qdeg = qd;
    }
    
    public int qdeg() {
        return qdeg;
    }

    public int getTopArrowSize() {
        return tMor.size();
    }

    public Arrow<R> getTopArrow(int i) {
        return tMor.get(i);
    }
    
    @Override
    public void output(ArrayList<Generator<R>> nextLevel) {
        System.out.println("hdeg = "+hdeg);
        System.out.println("qdeg = "+qdeg);
        for (int i = 0; i < bMor.size(); i++) bMor.get(i).output(nextLevel);
    }
    
}
