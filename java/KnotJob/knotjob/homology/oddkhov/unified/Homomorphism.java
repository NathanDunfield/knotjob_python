/*

Copyright (C) 2023 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.oddkhov.unified;

import java.util.ArrayList;
import knotjob.homology.Arrow;
import knotjob.rings.Matrix;
import knotjob.rings.ModN;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class Homomorphism<R extends Ring<R>> implements Comparable<Homomorphism<R>> {
    
    private final int hdegDo;
    private final int qdegDo;
    private final int hdegTa;
    private final int qdegTa;
    private final boolean parDo; // true means even hom, false odd
    private final boolean parTa;
    private final ArrayList<HomGen> gensDo;
    private final ArrayList<ArrayList<HomGen>> gensTa;
    private final R unit;
    
    public Homomorphism(int hd, int qd, int ht, int qt, boolean pd, boolean pt, R unt) {
        hdegDo = hd;
        qdegDo = qd;
        hdegTa = ht;
        qdegTa = qt;
        parDo = pd;
        parTa = pt;
        gensDo = new ArrayList<HomGen>();
        gensTa = new ArrayList<ArrayList<HomGen>>();
        unit = unt;
    }

    public boolean domainDegree(int h, int q) {
        return hdegDo == h & qdegDo == q;
    }
    
    public int rank() {
        ArrayList<HomGen> target = new ArrayList<HomGen>();
        for (int i = 0; i < gensDo.size(); i++) {
            HomGen gen = gensDo.get(i);
            for (HomGen tGen : gensTa.get(i))
                if (position(target, tGen) == -1) target.add(tGen);
        }
        ModN one = new ModN(1, 2);
        Matrix<ModN> matrix = new Matrix<ModN>(target.size(), gensDo.size(), one);
        for (int i = 0; i < gensDo.size(); i++) {
            for (HomGen tGen : gensTa.get(i)) matrix.set(position(target, tGen), i, one);
        }
        return matrix.rank();
    }
    
    public Homomorphism<R> compose(Homomorphism<R> hom) {
        if (!domainDegree(hom.hdegTa, hom.qdegTa)) return null;
        Homomorphism<R> comp = new Homomorphism<R>(hom.hdegDo, hom.qdegDo, this.hdegTa,
                this.qdegTa, hom.parDo, this.parTa, unit);
        ArrayList<HomGen> domains = new ArrayList<HomGen>();
        ArrayList<ArrayList<HomGen>> targets = new ArrayList<ArrayList<HomGen>>();
        for (int i = 0; i < hom.gensDo.size(); i++) {
            HomGen gen = hom.gensDo.get(i);
            ArrayList<HomGen> target = new ArrayList<HomGen>();
            for (HomGen tGen : hom.gensTa.get(i)) {
                ArrayList<HomGen> ntar = compositionWith(tGen);
                for (HomGen ftGen : ntar) addToGens(target, ftGen);
            }
            if (!target.isEmpty()) {
                domains.add(gen);
                targets.add(target);
            }
        }
        if (domains.isEmpty()) return null;
        for (HomGen gen : domains) comp.gensDo.add(gen);
        for (ArrayList<HomGen> gens : targets) comp.gensTa.add(gens);
        return comp;
    }
    
    public void addGenerator(UnifiedGenerator<R> gen, UnifiedChainComplex<R> eComplex,
            UnifiedChainComplex<R> cComplex) {
        int p = eComplex.getLevel(gen);
        R order = orderOf(gen);
        HomGen hg = new HomGen(eComplex.getGenerators(p).indexOf(gen), unit, order);
        gensDo.add(hg);
        ArrayList<HomGen> genTa = new ArrayList<HomGen>();
        for (Arrow<R> arr : gen.getOutArrows()) {
            UnifiedGenerator<R> tGen = (UnifiedGenerator<R>) arr.getTopGenerator();
            p = cComplex.getLevel(tGen);
            order = orderOf(tGen);
            R val = arr.getValue();
            if (!order.divides(val)) {
                HomGen thg = new HomGen(cComplex.getGenerators(p).indexOf(tGen), val, order);
                genTa.add(thg);
            }
        }
        if (genTa.isEmpty()) gensDo.remove(hg);
        else gensTa.add(genTa);
    }
    
    public ArrayList<ArrayList<Integer>> targetPositions() {
        ArrayList<ArrayList<Integer>> positions = new ArrayList<ArrayList<Integer>>();
        for (ArrayList<HomGen> gens : gensTa) {
            ArrayList<Integer> ints = new ArrayList<Integer>();
            for (HomGen gen : gens) ints.add(gen.position);
            positions.add(ints);
        }
        return positions;
    }
    
    public int getTaHdeg() {
        return hdegTa;
    }
    
    private int position(ArrayList<HomGen> gens, HomGen gen) {
        int pos = -1;
        int i = 0;
        while (pos == -1 && i < gens.size()) {
            if (gens.get(i).samePosition(gen)) pos = i;
            i++;
        }
        return pos;
    }
    
    private ArrayList<HomGen> compositionWith(HomGen tGen) {
        ArrayList<HomGen> gens = new ArrayList<HomGen>();
        for (int i = 0; i < gensDo.size(); i++) {
            if (gensDo.get(i).samePosition(tGen)) {
                for (HomGen gn : gensTa.get(i)) {
                    HomGen nGen = new HomGen(gn.position, gn.value.multiply(tGen.value), gn.order);
                    if (!nGen.order.divides(nGen.value)) addToGens(gens, nGen);
                }
            }
        }
        return gens;
    }
    
    private void addToGens(ArrayList<HomGen> gens, HomGen gen) {
        boolean found = false;
        int i = 0;
        while (!found && i < gens.size()) {
            HomGen xGen = gens.get(i);
            if (xGen.samePosition(gen)) found = true;
            else i++;
        }
        if (!found) gens.add(gen);
        else {
            HomGen xGen = gens.get(i);
            gens.remove(xGen);
            R add = xGen.value.add(gen.value);
            if (!xGen.order.divides(add)) {
                gens.add(new HomGen(xGen.position, add, xGen.order));
                System.out.println("Can't believe this happens");
            }
            //else System.out.println("Told you so");
        }
    }
    
    private R orderOf(UnifiedGenerator<R> gen) {
        int q = gen.qdeg();
        boolean found = false;
        int i = 0;
        while (!found && i < gen.getTopArrows().size()) {
            if (((UnifiedGenerator<R>) gen.getTopArrow(i).getBotGenerator()).qdeg() == gen.qdeg())
                found = true;
            else i++;
        }
        if (!found) return unit.getZero();
        return gen.getTopArrow(i).getValue().abs(0);
    }

    boolean nontrivial() {
        boolean triv = true;
        int i = 0;
        while (triv && i < gensTa.size()) {
            triv = gensTa.get(i).isEmpty();
            i++;
        }
        return !triv;
    }
    
    public String detailedInfo() {
        String str = this.toString();
        for (int i = 0; i < gensDo.size(); i++) { // for more detail
            HomGen hg = gensDo.get(i);
            str = str+ hg.toString()+" to ";
            String plus = " ";
            for (HomGen thg : gensTa.get(i)) {
                str = str +plus+thg.toString();
                plus = " + ";
            }
            str = str+"\n";
        }
        return str;
    }
    
    @Override
    public String toString() {
        String helpOne = "odd";
        String helpTwo = "odd";
        if (parDo) helpOne = "even";
        if (parTa) helpTwo = "even";
        String str = "("+hdegDo+", "+qdegTa+") "+helpOne+" to ("+
                hdegTa+", "+qdegTa+") "+helpTwo+" Rank "+this.rank()+"\n";
        return str;
    }

    @Override
    public int compareTo(Homomorphism<R> o) {
        int qdiff = qdegDo - o.qdegDo;
        if (qdiff == 0) return hdegDo - o.hdegDo;
        return qdiff;
    }

    String shortInfo() {
        return hdegDo+","+qdegDo+","+rank()+".";
    }

    private class HomGen {

        private final int position;
        private final R value;
        private final R order;
        
        public HomGen(int p, R val, R or) {
            position = p;
            value = val;
            order = or;
        }
        
        public boolean samePosition(HomGen gen) {
            return position == gen.position;
        }
        
        @Override
        public String toString() {
            String val = "";
            if (!value.add(unit.negate()).isZero()) val = value.toString();
            if (value.add(unit).isZero()) val = "-";
            return val+"["+position+"] ("+order+")";
        }
    }
    
}
