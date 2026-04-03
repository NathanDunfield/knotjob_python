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

package knotjob.stabletype.sinv;

import java.util.ArrayList;
import knotjob.rings.Ring;
import knotjob.stabletype.EvenCatFiller;
import knotjob.stabletype.OneFlowCategory;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.homology.evenkhov.EvenArrow;
import knotjob.homology.evenkhov.EvenStableGenerator;
import knotjob.links.Link;
import knotjob.stabletype.FlowGenerator;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class EvenSCatFiller<R extends Ring<R>> extends EvenCatFiller<R> {
    
    public EvenSCatFiller(OneFlowCategory<R> ct, int q, int hn, int hx, DialogWrap frm, 
            AbortInfo ain, boolean sck, Link link) {
        super(ct, q, hn, hx, frm, ain, sck, link);
    }
    
    @Override
    protected void createCategory(ArrayList<Generator<R>> gens, ArrayList<Generator<R>> compare,
            ArrayList<FlowGenerator<R>> newGens, int j) {
        EvenStableGenerator<R> gen = (EvenStableGenerator<R>) gens.get(j);
        if (gen.hdeg() >= hmin && gen.hdeg() <= hmax) {
            if (sock && gen.hdeg() < hmax && gen.qdeg() == qdeg) 
                checkForZeroMod((EvenStableGenerator<R>) gen);
            compare.add(gen);
            FlowSGenerator<R> nGen = new FlowSGenerator<R>(gen.hdeg(), gen.qdeg());
            newGens.add(nGen);
            nGen.gen = gen;
            addModuliSpaces(nGen, gen);
        }
    }
    
    @Override
    protected void addModuliSpaces(FlowGenerator<R> nGen, Generator<R> gen) {
        if (comparers.isEmpty()) return;
        ArrayList<Generator<R>> compare = comparers.get(0);
        addZeroModuliSpaces(nGen, gen, compare);
        if (((EvenStableGenerator<R>)gen).qdeg() == qdeg) addOneModuliSpaces(nGen, gen, compare);
        frame.setLabelRight(" "+counter, 2, false);
        counter++;
    }
    
    @Override
    protected void addArrows(FlowGenerator<R> nGn, Generator<R> gn, int i,
            ArrayList<Generator<R>> compare) {
        FlowSGenerator<R> nGen = (FlowSGenerator<R>) nGn;
        EvenArrow<R> arr = (EvenArrow<R>) gn.getBotArrows().get(i);
        EvenStableGenerator<R> gen = (EvenStableGenerator<R>) gn;
        EvenStableGenerator<R> tGen = (EvenStableGenerator<R>) arr.getTopGenerator();
        int pos = compare.indexOf(tGen);
        if (pos >= 0) {
            FlowSGenerator<R> ntGen = (FlowSGenerator<R>) cat.getGenerator(0, pos);
            if (gen.qdeg() == qdeg && tGen.qdeg() == qdeg) {
                if (unitTwo.multiply(arr.getValue()).isZero()) { // value is 2 or -2 or 0
                    if (unitFour.multiply(arr.getValue()).isZero()) {
                        Arrow<R> pArr = new Arrow<R>(nGen, ntGen, unit);
                        Arrow<R> mArr = new Arrow<R>(nGen, ntGen, unit.negate());
                        nGen.addBotArrow(pArr);
                        ntGen.addTopArrow(pArr);
                        nGen.addBotArrow(mArr);
                        ntGen.addTopArrow(mArr);
                    }
                    else {
                        int j = changeOf(pos, compare, gen.getPosition());
                        R sign = signOf(gen.getPosition(), j, true);
                        Arrow<R> pArr = new Arrow<R>(nGen, ntGen, sign);
                        Arrow<R> mArr = new Arrow<R>(nGen, ntGen, sign);
                        nGen.addBotArrow(pArr);
                        ntGen.addTopArrow(pArr);
                        nGen.addBotArrow(mArr);
                        ntGen.addTopArrow(mArr);
                    }
                }
                else {
                    int j = changeOf(pos, compare, gen.getPosition());
                    boolean neg = checkIfNegative(arr, j);
                    R sign = signOf(gen.getPosition(), j, !neg);
                    Arrow<R> nArr = new Arrow<R>(nGen, ntGen, sign);
                    nGen.addBotArrow(nArr);
                    ntGen.addTopArrow(nArr);
                }
            }
            else {
                //int j = changeOf(pos, compare, gen.getPosition());
                //boolean neg = checkIfNegative(arr, j);
                //R sign = signOf(gen.getPosition(), j, !neg);
                Arrow<R> nArr = new Arrow<R>(nGen, ntGen, arr.getValue());
                if (gen.qdeg() == tGen.qdeg()) {
                    nGen.addBotArrow(nArr);
                    ntGen.addTopArrow(nArr);
                }
                else {
                    nGen.addBotqArr(nArr);
                    ntGen.addTopqArr(nArr);
                }
            }// */
            /*if (unitTwo.multiply(arr.getValue()).isZero()) { // value is 2 or -2 or 0
                if (unitFour.multiply(arr.getValue()).isZero()) {
                    Arrow<R> pArr = new Arrow<R>(nGen, ntGen, unit);
                    Arrow<R> mArr = new Arrow<R>(nGen, ntGen, unit.negate());
                    nGen.addBotArrow(pArr);
                    ntGen.addTopArrow(pArr);
                    nGen.addBotArrow(mArr);
                    ntGen.addTopArrow(mArr);
                }
                else {
                    if (gen.qdeg() != tGen.qdeg()) System.out.println("Happenz!?!");
                    int j = changeOf(pos, compare, gen.getPosition());
                    R sign = signOf(gen.getPosition(), j, true);
                    Arrow<R> pArr = new Arrow<R>(nGen, ntGen, sign);
                    Arrow<R> mArr = new Arrow<R>(nGen, ntGen, sign);
                    if (gen.qdeg() == tGen.qdeg()) {
                        nGen.addBotArrow(pArr);
                        ntGen.addTopArrow(pArr);
                        nGen.addBotArrow(mArr);
                        ntGen.addTopArrow(mArr);
                    }
                    else {
                        nGen.addBotqArr(pArr);
                        ntGen.addTopqArr(pArr);
                        nGen.addBotqArr(mArr);
                        ntGen.addTopqArr(mArr);
                    }
                }
            }
            else {
                int j = changeOf(pos, compare, gen.getPosition());
                boolean neg = checkIfNegative(arr, j);System.out.println(neg);
                R sign = signOf(gen.getPosition(), j, !neg);
                Arrow<R> nArr = new Arrow<R>(nGen, ntGen, sign);
                if (gen.qdeg() == tGen.qdeg()) {
                    nGen.addBotArrow(nArr);
                    ntGen.addTopArrow(nArr);
                }
                else {
                    nGen.addBotqArr(nArr);
                    ntGen.addTopqArr(nArr);
                }
            }// */
        }
    }
    
}
