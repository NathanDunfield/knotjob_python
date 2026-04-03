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
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.rings.Ring;
import knotjob.homology.Generator;
import knotjob.homology.ChainComplex;
import knotjob.homology.Arrow;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class CatFiller<R extends Ring<R>> {
    
    protected final OneFlowCategory<R> cat;
    protected final ArrayList<ArrayList<Generator<R>>> comparers;
    protected final int qdeg;
    protected final int hmin;
    protected final int hmax;
    protected final R unit;
    protected final R unitTwo;
    protected final R unitFour;
    protected final DialogWrap frame;
    private final AbortInfo abInf;
    protected int counter = 0;
    
    public CatFiller(OneFlowCategory<R> ct, int q, int hn, int hx, DialogWrap frm, AbortInfo ain) {
        cat = ct;
        qdeg = q;
        hmin = hn;
        hmax = hx;
        comparers = new ArrayList<ArrayList<Generator<R>>>();
        frame = frm;
        abInf = ain;
        unit = cat.getUnit();
        unitTwo = cat.getUnitTwo();
        unitFour = cat.getUnitFour();
    }
    
    public void fill(ChainComplex<R> complex) {
        int i = complex.generatorSize()-1;
        while (i >= 0 && !abInf.isAborted()) {
            frame.setLabelRight(" "+i, 1, false);
            ArrayList<Generator<R>> gens = complex.getGenerators(i);
            ArrayList<Generator<R>> compare = new ArrayList<Generator<R>>();
            ArrayList<FlowGenerator<R>> newGens = new ArrayList<FlowGenerator<R>>();
            int j = gens.size()-1;
            while (j >= 0 && !abInf.isAborted()) {
                createCategory(gens, compare, newGens, j);
                j--;
            }
            comparers.clear();
            comparers.add(compare);
            cat.addGenerators(newGens);
            i--;
        }
        cat.removeUnnecessary();
    }
    
    protected void createCategory(ArrayList<Generator<R>> gens, ArrayList<Generator<R>> compare,
            ArrayList<FlowGenerator<R>> newGens, int j) {
        // to be overwritten
    }
    
    protected void addModuliSpaces(FlowGenerator<R> nGen, Generator<R> gen) {
        if (comparers.isEmpty()) return;
        ArrayList<Generator<R>> compare = comparers.get(0);
        addZeroModuliSpaces(nGen, gen, compare);
        addOneModuliSpaces(nGen, gen, compare);
        frame.setLabelRight(" "+counter, 2, false);
        counter++;
    }
    
    protected void addZeroModuliSpaces(FlowGenerator<R> nGen, Generator<R> gen,
            ArrayList<Generator<R>> compare) {
        for (int i = 0; i < gen.getBotArrows().size(); i++) 
            addArrows(nGen, gen, i, compare);
    }
    
    protected void addArrows(FlowGenerator<R> nGen, Generator<R> gen, int i,
            ArrayList<Generator<R>> compare) {
        // to be overwritten
    }
    
    protected void addOneModuliSpaces(FlowGenerator<R> nGen, Generator<R> gen,
            ArrayList<Generator<R>> compare) {
        ArrayList<ArrayList<FlowVertex<R>>> vertices = new ArrayList<ArrayList<FlowVertex<R>>>();
        ArrayList<FlowGenerator<R>> endPoints = new ArrayList<FlowGenerator<R>>();
        for (Arrow<R> arr : nGen.getBotArrows()) {
            FlowGenerator<R> mGen = (FlowGenerator<R>) arr.getTopGenerator();
            for (Arrow<R> sarr : mGen.getBotArrows()) {
                FlowGenerator<R> tGen = (FlowGenerator<R>) sarr.getTopGenerator();
                int pos = endPoints.indexOf(tGen);
                if (pos < 0) {
                    ArrayList<FlowVertex<R>> verts = new ArrayList<FlowVertex<R>>();
                    verts.add(new FlowVertex<R>(arr, sarr));
                    vertices.add(verts);
                    endPoints.add(tGen);
                }
                else vertices.get(pos).add(new FlowVertex<R>(arr, sarr));
            }
        }
        for (int i = 0; i < endPoints.size(); i++) {
            FlowGenerator<R> tGen = endPoints.get(i);
            ArrayList<FlowVertex<R>> verts = vertices.get(i);
            ModOne<R> mod = new ModOne<R>(nGen, tGen);
            nGen.addBotMod(mod);
            tGen.addTopMod(mod);
            GraphStructure<R> graph = theStructureOf(verts, compare, gen);
            mod.setEdges(graph);
            if (mod.isEmpty()) {
                mod.getBotGenerator().getBotMod().remove(mod);
                mod.getTopGenerator().getTopMod().remove(mod);
            }
        }
    }
    
    protected GraphStructure<R> theStructureOf(ArrayList<FlowVertex<R>> verts, 
            ArrayList<Generator<R>> compare, Generator<R> gen) {
        ArrayList<FlowVertex<R>> topLine = new ArrayList<FlowVertex<R>>();
        ArrayList<FlowVertex<R>> botLine = new ArrayList<FlowVertex<R>>();
        ArrayList<Integer> positions = new ArrayList<Integer>();
        for (FlowVertex<R> vert : verts) {
            FlowGenerator<R> mGen = vert.getMiddle();
            int pos = cat.positionOf(0, mGen);
            positions.add(pos);
        }
        return theGraphStructureOf(positions, compare, gen, topLine, botLine, verts);
    }
    
    protected GraphStructure<R> theGraphStructureOf(ArrayList<Integer> positions, 
            ArrayList<Generator<R>> compare, Generator<R> gn, ArrayList<FlowVertex<R>> topLine, 
            ArrayList<FlowVertex<R>> botLine, ArrayList<FlowVertex<R>> verts) {
        // to be overwritten
        return null;
    }
    
    
}
