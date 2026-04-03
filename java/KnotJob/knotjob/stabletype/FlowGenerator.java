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

package knotjob.stabletype;

import java.util.ArrayList;
import knotjob.homology.Generator;
import knotjob.homology.evenkhov.EvenStableGenerator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class FlowGenerator<R extends Ring<R>> extends Generator<R> {
    
    private final ArrayList<ModOne<R>> bMod;
    private final ArrayList<ModOne<R>> tMod;
    public EvenStableGenerator<R> gen;
    
    public FlowGenerator(int hd) {
        super(hd);
        bMod = new ArrayList<ModOne<R>>();
        tMod = new ArrayList<ModOne<R>>();
    }
    
    public void addBotMod(ModOne<R> mod) {
        bMod.add(mod);
    }
    
    public void addTopMod(ModOne<R> mod) {
        tMod.add(mod);
    }
    
    public ArrayList<ModOne<R>> getTopMod() {
        return tMod;
    }
    
    public ArrayList<ModOne<R>> getBotMod() {
        return bMod;
    }
    
    public void output(ArrayList<FlowGenerator<R>> nextLevel, ArrayList<FlowGenerator<R>> vnextLevel) {
        System.out.println("hdeg = "+hdeg);
        //System.out.println(gen.getPosition()+" "+gen.getSigns());
        if (nextLevel == null) return;
        ArrayList<Generator<R>> nxLevel = new ArrayList<Generator<R>>();
        for (FlowGenerator<R> gn : nextLevel) nxLevel.add(gn);
        for (int i = 0; i < bMor.size(); i++) bMor.get(i).output(nxLevel);
        if (vnextLevel == null) return;
        for (int i = 0; i < bMod.size(); i++) bMod.get(i).output(nextLevel, vnextLevel);
    }
    
}
