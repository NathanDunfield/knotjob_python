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
 * @author dirk
 * @param <R>
 */
public class Generator<R extends Ring<R>> {
    
    protected final int hdeg;
    protected final ArrayList<Arrow<R>> bMor;
    protected final ArrayList<Arrow<R>> tMor;
    
    public Generator(int hd) {
        hdeg = hd;
        bMor = new ArrayList<Arrow<R>>();
        tMor = new ArrayList<Arrow<R>>();
    }
    
    public void addBotArrow(Arrow<R> arrow) {
        bMor.add(arrow);
    }
    
    public void addTopArrow(Arrow<R> arrow) {
        tMor.add(arrow);
    }
    
    public void clearTopArr() {
        tMor.clear();
    }

    public void clearBotArr() {
        bMor.clear();
    }
    
    public int hdeg() {
        return hdeg;
    }
    
    public ArrayList<Arrow<R>> getBotArrows() {
        return bMor;
    }
    
    public ArrayList<Arrow<R>> getTopArrows() {
        return tMor;
    }
    
    public void output(ArrayList<Generator<R>> nextLevel) {
        System.out.println("hdeg = "+hdeg);
        for (int i = 0; i < bMor.size(); i++) bMor.get(i).output(nextLevel);
    }
    
}
