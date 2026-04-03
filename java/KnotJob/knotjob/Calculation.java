/*

Copyright (C) 2024 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob;

import java.util.ArrayList;
import knotjob.dialogs.DialogWrap;
import knotjob.links.Link;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public abstract class Calculation<R extends Ring<R>> {
    
    protected final Link theLink;
    protected final DialogWrap frame;
    protected final AbortInfo abInf;
    protected final R unit;
    protected final Options options;
    private final int[] girth;
    protected final boolean highDetail;
    
    public Calculation(Link lnk, DialogWrap frm, R unt, Options opts) {
        theLink = lnk;
        frame = frm;
        abInf = frame.getAbortInfo();
        unit = unt;
        options = opts;
        girth = theLink.totalGirthArray();
        highDetail = options.getGirthInfo() == 2;
    }
    
    protected String girthInfo(int u) {
        if (u == -1) return "0";
        String info = String.valueOf(girth[u]);
        if (!highDetail) return info;
        if (u < girth.length - 1) info = info+" ("+girth[u+1];
        else return info;
        for (int i = 1; i < 3; i++) {
            if (u < girth.length - i - 1) info = info+", "+girth[u+1+i];
        }
        info = info+")";
        return info;
    }
    
    protected int girthDifference(int i) {
        return girth[i] - girth[i+1];
    }
    
    protected int getDiscPosition(int u) {
        ArrayList<Integer> disc = theLink.getDiscAt(u-1);
        ArrayList<Integer> pos = getPosition(u, disc);
        return boundaryOf(pos);
    }

    private ArrayList<Integer> getPosition(int u, ArrayList<Integer> disc) {
        ArrayList<Integer> pos = new ArrayList<Integer>();
        for (int k : theLink.getPath(u)) pos.add(disc.indexOf(k));
        return pos;
    }

    private int boundaryOf(ArrayList<Integer> pos) {
        int i = 0;
        while (true) {
            if (pos.contains(i) && !pos.contains(i+1)) break;
            i++;
        }
        return pos.indexOf(i);
    }
    
    public abstract void calculate();
    
}
