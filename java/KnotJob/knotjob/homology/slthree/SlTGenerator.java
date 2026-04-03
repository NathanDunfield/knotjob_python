/*

Copyright (C) 2023-24 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.slthree;

import java.util.ArrayList;
import knotjob.homology.Generator;
import knotjob.homology.QGenerator;
import knotjob.homology.slthree.foam.Web;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class SlTGenerator<R extends Ring<R>> extends QGenerator<R> {
    
    private final Web web;
    
    public SlTGenerator(int hd, int qd, Web wb) {
        super(hd, qd);
        web = wb;
    }
    
    public Web getWeb() {
        return web;
    }
    
    @Override
    public void output(ArrayList<Generator<R>> nextLevel) {
        System.out.println("Web  = "+web);
        System.out.println("hdeg = "+hdeg);
        System.out.println("qdeg = "+qdeg);
        for (int i = 0; i < bMor.size(); i++) ((SlTArrow<R>) bMor.get(i)).output(nextLevel);
    }

    /*public void clearTopArrow() {
        tMor.clear();
    }

    public void clearBotArrow() {
        bMor.clear();
    }// */
    
}
