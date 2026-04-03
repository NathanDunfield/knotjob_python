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
import java.util.Objects;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.homology.oddkhov.OddArrow;
import knotjob.homology.oddkhov.OddStableGenerator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class OddCatFiller<R extends Ring<R>> extends CatFiller<R> {
    
    protected final R epsilon;
    
    public OddCatFiller(OneFlowCategory<R> ct, int q, int hn, int hx, DialogWrap frm, 
            AbortInfo ain, R eps) {
        super(ct, q, hn, hx, frm, ain);
        epsilon = eps;
    }
    
    @Override
    protected void createCategory(ArrayList<Generator<R>> gens, ArrayList<Generator<R>> compare,
            ArrayList<FlowGenerator<R>> newGens, int j) {
        OddStableGenerator<R> gen = (OddStableGenerator<R>) gens.get(j);
        if (gen.qdeg() == qdeg) {
            if (gen.hdeg() >= hmin && gen.hdeg() <= hmax) {
                compare.add(gen);
                FlowGenerator<R> nGen = new FlowGenerator<R>(gen.hdeg());
                newGens.add(nGen);
                addModuliSpaces(nGen, gen);
            }
            gens.remove(j);
        }
    }
    
    @Override
    protected void addArrows(FlowGenerator<R> nGen, Generator<R> gen, int i,
            ArrayList<Generator<R>> compare) {
        OddArrow<R> arr = (OddArrow<R>) gen.getBotArrows().get(i);
        OddStableGenerator<R> tGen = (OddStableGenerator<R>) arr.getTopGenerator();
        int pos = compare.indexOf(tGen);
        if (pos >= 0) {
            FlowGenerator<R> ntGen = cat.getGenerator(0, pos);
            Arrow<R> nArr = new Arrow<R>(nGen, ntGen, arr.getValue());
            nGen.addBotArrow(nArr);
            ntGen.addTopArrow(nArr);
        }
    }
    
    @Override
    protected GraphStructure<R> theGraphStructureOf(ArrayList<Integer> positions, 
            ArrayList<Generator<R>> compare, Generator<R> gn, ArrayList<FlowVertex<R>> topLine, 
            ArrayList<FlowVertex<R>> botLine, ArrayList<FlowVertex<R>> verts) {
        OddStableGenerator<R> gen = (OddStableGenerator<R>) gn;
        ArrayList<Boolean> start = gen.getPosition();
        int fChange = firstChange(positions, compare, start);
        int sChange = secondChange(positions, compare, start);
        for (int i = 0; i < positions.size(); i++) {
            int p = positions.get(i);
            if (fChange == changeOf(p, compare, start)) topLine.add(verts.get(i));
            else botLine.add(verts.get(i));
        }
        ArrayList<FlowEdge<R>> edges = new ArrayList<FlowEdge<R>>(2);
        boolean extraCircle = false;
        for (int i = 0; i < topLine.size(); i++) {
            FlowVertex<R> vert = topLine.get(i);
            FlowVertex<R> svert = botLine.get(i);
            if (!vert.sign().add(svert.sign()).isZero()) svert = botLine.get(1-i);
            R frm = getCorrectFrame(vert, svert, fChange, sChange, start);
            if (frm.add(unitTwo).isZero()) extraCircle = !extraCircle;
            boolean fl = !vert.firstSign().add(unit).isZero();
            boolean fu = !vert.secondSign().add(unit).isZero();
            boolean sl = !svert.firstSign().add(unit).isZero();
            boolean su = !svert.secondSign().add(unit).isZero();
            FlowEdge<R> edge = new FlowEdge<R>(vert.getMiddle(), svert.getMiddle(), fl, fu,
                            sl, su);
            edges.add(edge);
        }
        return new GraphStructure<R>(edges, extraCircle);
    }
    
    private int firstChange(ArrayList<Integer> positions, ArrayList<Generator<R>> compare, 
            ArrayList<Boolean> fpos) {
        int fc = 1000000;
        for (int pos : positions) {
            int change = changeOf(pos, compare, fpos);
            if (change < fc) fc = change;
        }
        return fc;
    }
    
    private int secondChange(ArrayList<Integer> positions, ArrayList<Generator<R>> compare, 
            ArrayList<Boolean> fpos) {
        int sc = -1;
        for (int pos : positions) {
            int change = changeOf(pos, compare, fpos);
            if (change > sc) sc = change;
        }
        return sc;
    }
    
    private int changeOf(int pos, ArrayList<Generator<R>> compare, 
            ArrayList<Boolean> fpos) {
        OddStableGenerator<R> mGen = (OddStableGenerator<R>) compare.get(pos);
        boolean same = true;
        ArrayList<Boolean> spos = mGen.getPosition();
        int i = 0;
        while (same) {
            if (!Objects.equals(fpos.get(i), spos.get(i))) same = false;
            else i++;
        }
        return i;
    }
    
    private R getCorrectFrame(FlowVertex<R> vert, FlowVertex<R> svert, int fc, int sc,
            ArrayList<Boolean> fpos) {
        int a = 0;
        for (int i = 0; i < fc; i++) {
            if (fpos.get(i)) a++;
        }
        int b = a;
        for (int i = fc+1; i < sc; i++) {
            if (fpos.get(i)) b++;
        }
        R ft = unitTwo;
        if (a%2 == 0) ft = ft.getZero();
        R fb = unitTwo;
        if (b%2 == 0) fb = fb.getZero();
        R st = fb.add(unitTwo);
        R sb = ft; // these represent what the standard sign assignment would be
        return framingOf(ft, st, fb, sb, vert, svert, a*(a+b));
    }
    
    private R framingOf(R ft, R st, R fb, R sb, FlowVertex<R> vert, FlowVertex<R> svert, int fv) {
        R frm = unitTwo.getZero();
        if (fv % 2 != 0) frm = unitTwo;
        R nft = unitTwo;
        if (!vert.firstSign().add(unit).isZero()) nft = nft.getZero();
        R nst = unitTwo;
        if (!vert.secondSign().add(unit).isZero()) nst = nst.getZero();
        R nfb = unitTwo;
        if (!svert.firstSign().add(unit).isZero()) nfb = nfb.getZero();
        R nsb = unitTwo;
        if (!svert.secondSign().add(unit).isZero()) nsb = nsb.getZero();
        boolean fuc = ft.add(nft).isZero();
        boolean suc = st.add(nst).isZero();
        boolean flc = fb.add(nfb).isZero();
        boolean slc = sb.add(nsb).isZero();
        /*if (!fuc && !flc &&  slc &&  suc) return frm.add(unitTwo);//this one doesn't work
        if ( fuc &&  flc && !slc && !suc) return frm;
        if (!fuc &&  flc &&  slc && !suc) return frm;
        if ( fuc && !flc && !slc &&  suc) return frm.add(unitTwo);
        if (!fuc &&  flc && !slc &&  suc) return frm.add(epsilon);
        if ( fuc && !flc &&  slc && !suc) return frm.add(unitTwo).add(epsilon);
        if (!fuc && !flc && !slc && !suc) return frm.add(unitTwo);// */
        /*if (!fuc && !flc &&  slc &&  suc) return frm.add(st.add(sb));// works, but with different eps
        if ( fuc &&  flc && !slc && !suc) return frm;
        if (!fuc &&  flc &&  slc && !suc) return frm.add(st);
        if ( fuc && !flc && !slc &&  suc) return frm.add(sb);
        if (!fuc &&  flc && !slc &&  suc) return frm.add(st).add(epsilon);
        if ( fuc && !flc &&  slc && !suc) return frm.add(sb).add(epsilon);
        if (!fuc && !flc && !slc && !suc) return frm.add(st).add(sb);// */
        if (!fuc && !flc &&  slc &&  suc) return frm.add(unitTwo);
        if ( fuc &&  flc && !slc && !suc) return frm.add(st.add(sb));
        if (!fuc &&  flc &&  slc && !suc) return frm.add(ft);
        if ( fuc && !flc && !slc &&  suc) return frm.add(fb);
        if (!fuc &&  flc && !slc &&  suc) return frm.add(fb).add(epsilon);
        if ( fuc && !flc &&  slc && !suc) return frm.add(ft).add(epsilon);
        if (!fuc && !flc && !slc && !suc) return frm.add(ft).add(fb);// */
        return frm;
    }
    
}
