/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knotjob.stabletype.sinv;

import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.homology.oddkhov.OddArrow;
import knotjob.homology.oddkhov.OddStableGenerator;
import knotjob.rings.Ring;
import knotjob.stabletype.FlowGenerator;
import knotjob.stabletype.OddCatFiller;
import knotjob.stabletype.OneFlowCategory;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class OddSCatFiller<R extends Ring<R>> extends OddCatFiller<R> {
    
    public OddSCatFiller(OneFlowCategory<R> ct, int q, int hn, int hx, DialogWrap frm, 
            AbortInfo ain, R eps) {
        super(ct, q, hn, hx, frm, ain, eps);
    }
    
    @Override
    protected void createCategory(ArrayList<Generator<R>> gens, ArrayList<Generator<R>> compare,
            ArrayList<FlowGenerator<R>> newGens, int j) {
        OddStableGenerator<R> gen = (OddStableGenerator<R>) gens.get(j);
        if (gen.hdeg() >= hmin && gen.hdeg() <= hmax) {
            compare.add(gen);
            FlowSGenerator<R> nGen = new FlowSGenerator<R>(gen.hdeg(), gen.qdeg());
            newGens.add(nGen);
            addModuliSpaces(nGen, gen);
        }
    }
    
    @Override
    protected void addModuliSpaces(FlowGenerator<R> nGen, Generator<R> gen) {
        if (comparers.isEmpty()) return;
        ArrayList<Generator<R>> compare = comparers.get(0);
        addZeroModuliSpaces(nGen, gen, compare);
        if (((OddStableGenerator<R>)gen).qdeg() == qdeg) addOneModuliSpaces(nGen, gen, compare);
        frame.setLabelRight(" "+counter, 2, false);
        counter++;
    }
    
    @Override
    protected void addArrows(FlowGenerator<R> nGn, Generator<R> gn, int i,
            ArrayList<Generator<R>> compare) {
        FlowSGenerator<R> nGen = (FlowSGenerator<R>) nGn;
        OddArrow<R> arr = (OddArrow<R>) gn.getBotArrows().get(i);
        OddStableGenerator<R> gen = (OddStableGenerator<R>) gn;
        OddStableGenerator<R> tGen = (OddStableGenerator<R>) arr.getTopGenerator();
        int pos = compare.indexOf(tGen);
        if (pos >= 0) {
            FlowSGenerator<R> ntGen = (FlowSGenerator<R>) cat.getGenerator(0, pos);
            Arrow<R> nArr = new Arrow<R>(nGen, ntGen, arr.getValue());
            if (gen.qdeg() == tGen.qdeg()) {
                nGen.addBotArrow(nArr);
                ntGen.addTopArrow(nArr);
            }
            else {
                nGen.addBotqArr(nArr);
                ntGen.addTopqArr(nArr);
            }
        }
    }
    
}
