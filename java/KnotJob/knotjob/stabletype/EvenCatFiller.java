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
import knotjob.homology.evenkhov.EvenArrow;
import knotjob.homology.evenkhov.EvenStableGenerator;
import knotjob.links.Link;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class EvenCatFiller<R extends Ring<R>> extends CatFiller<R> {
    
    protected final boolean sock;
    protected final Link theLink;
    
    public EvenCatFiller(OneFlowCategory<R> ct, int q, int hn, int hx, DialogWrap frm, 
            AbortInfo ain, boolean sck, Link link) {
        super(ct, q, hn, hx, frm, ain);
        sock = sck;
        theLink = link;
    }
    
    @Override
    protected void createCategory(ArrayList<Generator<R>> gens, ArrayList<Generator<R>> compare,
            ArrayList<FlowGenerator<R>> newGens, int j) {
        EvenStableGenerator<R> gen = (EvenStableGenerator<R>) gens.get(j);
        if (gen.qdeg() == qdeg) {
            if (gen.hdeg() >= hmin && gen.hdeg() <= hmax) {
                if (sock && gen.hdeg() < hmax) checkForZeroMod((EvenStableGenerator<R>) gen);
                compare.add(gen);
                FlowGenerator<R> nGen = new FlowGenerator<R>(gen.hdeg());
                newGens.add(nGen);
                nGen.gen = gen;
                addModuliSpaces(nGen, gen);
            }
            gens.remove(j);
        }
    }
    
    protected void checkForZeroMod(EvenStableGenerator<R> gen) {
        ArrayList<Integer> position = gen.getPosition();
        for (int i = 0; i < position.size(); i++) {
            int p = position.get(i);
            int a = theLink.getCross(i);
            if (a < 0) {
                p = p + a;
                if (p < 0 && p % 2 == 0) {
                    findTheZero(gen, position, i, comparers);
                }
            }
            else {
                if (a > 1 && p < a && p % 2 != 0 ) {
                    findTheZero(gen, position, i, comparers);
                }
            }
        }
    }
    
    private void findTheZero(EvenStableGenerator<R> gen, ArrayList<Integer> position, int i,
            ArrayList<ArrayList<Generator<R>>> comparers) {
        ArrayList<Integer> circle = theLink.circleAt(theLink.getPath(i, 0), position);
        if (circle.contains(theLink.getPath(i, 2))) {
            int u = circleWith(gen.getCircles(), circle);
            boolean poss = gen.getSigns().get(u);
            if (poss) {
                EvenStableGenerator<R> nextGuy = getGeneratorFrom(position, i, u,
                        gen.getSigns(), comparers);
                if (nextGuy != null) {
                    EvenArrow<R> arr = new EvenArrow<R>(gen, nextGuy);
                    arr.addValue(unit.getZero());
                    gen.addBotArrow(arr);
                    nextGuy.addTopArrow(arr);
                }
            }
        }
    }
    
    private EvenStableGenerator<R> getGeneratorFrom(ArrayList<Integer> position, int i, int u,
            ArrayList<Boolean> signs, ArrayList<ArrayList<Generator<R>>> comparers) {
        if (comparers.isEmpty()) return null;
        boolean found = false;
        int k = 0;
        while (!found) {
            EvenStableGenerator<R> gen = (EvenStableGenerator<R>) comparers.get(0).get(k);
            if (rightPosition(gen, i, position)) {
                if (rightSigns(gen, u, signs)) found = true;
            }
            if (!found) k++;
        }
        return (EvenStableGenerator<R>) comparers.get(0).get(k);
    }
    
    private boolean rightPosition(EvenStableGenerator<R> gen, int i, ArrayList<Integer> position) {
        boolean right = true;
        int k = 0;
        ArrayList<Integer> possis = gen.getPosition();
        while (right && k < position.size()) {
            if (k != i) {
                if (!Objects.equals(position.get(k), possis.get(k))) right = false;
            }
            k++;
        }
        return right;
    }
    
    private boolean rightSigns(EvenStableGenerator<R> gen, int u, ArrayList<Boolean> signs) {
        boolean right = true;
        int k = 0;
        ArrayList<Boolean> possis = gen.getSigns();
        while (right && k < signs.size()) {
            if (k != u) {
                if (!Objects.equals(signs.get(k), possis.get(k))) right = false;
            }
            k++;
        }
        return right;
    }
    
    private int circleWith(ArrayList<ArrayList<Integer>> circles, ArrayList<Integer> circle) {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (circle.contains(circles.get(i).get(0))) found = true;
            else i++;
        }
        return i;
    }
    
    @Override
    protected void addArrows(FlowGenerator<R> nGen, Generator<R> gn, int i,
            ArrayList<Generator<R>> compare) {
        EvenArrow<R> arr = (EvenArrow<R>) gn.getBotArrows().get(i);
        EvenStableGenerator<R> gen = (EvenStableGenerator<R>) gn;
        EvenStableGenerator<R> tGen = (EvenStableGenerator<R>) arr.getTopGenerator();
        int pos = compare.indexOf(tGen);
        if (pos >= 0) {
            FlowGenerator<R> ntGen = cat.getGenerator(0, pos);
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
    }
    
    protected int changeOf(int pos, ArrayList<Generator<R>> compare, 
            ArrayList<Integer> fpos) {
        EvenStableGenerator<R> mGen = (EvenStableGenerator<R>) compare.get(pos);
        boolean same = true;
        ArrayList<Integer> spos = mGen.getPosition();
        int i = 0;
        while (same) {
            if (!Objects.equals(fpos.get(i), spos.get(i))) same = false;
            else i++;
        }
        return i;
    }
    
    protected R signOf(ArrayList<Integer> position, int j, boolean pos) {
        int s = 0;
        for (int i = 0; i < j; i++) {
            int p = position.get(i);
            int crs = theLink.getCross(i);
            if (crs < 0) p = p + crs;
            s = s + p;
        }
        if (!pos) s++;
        if (s%2 == 0) return unit;
        return unit.negate();
    }
    
    protected boolean checkIfNegative(EvenArrow<R> arr, int j) {
        EvenStableGenerator<R> bGen = (EvenStableGenerator<R>) arr.getBotGenerator();
        EvenStableGenerator<R> tGen = (EvenStableGenerator<R>) arr.getTopGenerator();
        if (bGen.getSigns().size() != tGen.getSigns().size()) return false;
        int crs = theLink.getCross(j);
        int p = bGen.getPosition(j);
        if (crs < 0) {
            p = p + crs;
            if (p % 2 != 0) return false;
        }
        else {
            if (p % 2 == 0) return false;
        }
        ArrayList<Integer> circle = theLink.circleAt(theLink.getPath(j, 0), bGen.getPosition());
        int u = circleWith(bGen.getCircles(), circle);
        boolean poss = bGen.getSigns().get(u);
        boolean oths = tGen.getSigns().get(u);
        return poss == oths;
    }
    
    @Override
    protected GraphStructure<R> theGraphStructureOf(ArrayList<Integer> positions, 
            ArrayList<Generator<R>> compare, Generator<R> gn, ArrayList<FlowVertex<R>> topLine, 
            ArrayList<FlowVertex<R>> botLine, ArrayList<FlowVertex<R>> verts) {
        EvenStableGenerator<R> gen = (EvenStableGenerator<R>) gn;
        ArrayList<Integer> start = gen.getPosition();
        int fChange = firstChange(positions, compare, start);
        int sChange = secondChange(positions, compare, start);
        for (int i = 0; i < positions.size(); i++) {
            int p = positions.get(i);
            if (fChange == changeOf(p, compare, start)) topLine.add(verts.get(i));
            else botLine.add(verts.get(i));
        }
        ArrayList<FlowEdge<R>> edges = new ArrayList<FlowEdge<R>>(2);
        boolean extraCircle = false;
        if (fChange == sChange) {
            extraCircle = dealWithNFraming(fChange, gen, topLine, edges);
        }
        else {
            if (topLine.size() == 1) {
                if (bothSurgeries(gen.getPosition(), fChange, sChange)) {
                    FlowVertex<R> vert = topLine.get(0);
                    FlowVertex<R> svert = botLine.get(0);
                    R frm = getFrameValue(vert, svert);
                    //R frm = getFrameValue(gen.getPosition(), fChange, sChange, 
                    //        getTypesOf(topLine).get(0));
                    if (frm.add(unitTwo).isZero()) extraCircle = !extraCircle;
                    boolean fl = !vert.firstSign().add(unit).isZero();
                    boolean fu = !vert.secondSign().add(unit).isZero();
                    boolean sl = !svert.firstSign().add(unit).isZero();
                    boolean su = !svert.secondSign().add(unit).isZero();
                    FlowEdge<R> edge = new FlowEdge<R>(vert.getMiddle(), svert.getMiddle(), fl, fu,
                            sl, su);
                    edges.add(edge);
                }
                else {
                    ArrayList<int[]> topTypes = getTypesOf(topLine);
                    FlowVertex<R> vert = topLine.get(0);
                    FlowVertex<R> svert = botLine.get(0);
                    R frm = getFrameValue(gen.getPosition(), fChange, sChange, topTypes.get(0));
                    if (frm.add(unitTwo).isZero()) extraCircle = !extraCircle;
                    boolean fl = !vert.firstSign().add(unit).isZero();
                    boolean fu = !vert.secondSign().add(unit).isZero();
                    boolean sl = !svert.firstSign().add(unit).isZero();
                    boolean su = !svert.secondSign().add(unit).isZero();
                    FlowEdge<R> edge = new FlowEdge<R>(vert.getMiddle(), svert.getMiddle(), 
                            fl, fu, sl, su);
                    edges.add(edge);
                }
            }
            else {
                if (bothSurgeries(gen.getPosition(), fChange, sChange)) {
                    dealWithLadyBug(fChange, sChange, gen, topLine, botLine, compare, cat, edges);
                }
                else {
                    if (topLine.size() == 4) {
                        ArrayList<int[]> topTypes = getTypesOf(topLine);
                        boolean onetwo = false;
                        for (int i = 0; i < topLine.size(); i++) {
                            if (topTypes.get(i)[0] == 1 || topTypes.get(i)[1] == 1) onetwo = true;
                        }
                        extraCircle = !onetwo;
                    }
                    else {
                        ArrayList<int[]> topTypes = getTypesOf(topLine);
                        ArrayList<int[]> botTypes = getTypesOf(botLine);
                        R frm = unitTwo.getZero();
                        for (int i = 0; i < topLine.size(); i++) {
                            int k = matchWith(topTypes.get(i), botTypes);
                            FlowVertex<R> vert = topLine.get(i);
                            FlowVertex<R> svert = botLine.get(k);
                            frm = frm.add(getFrameValue(gen.getPosition(), fChange, sChange, 
                                    topTypes.get(i)));
                            boolean fl = !vert.firstSign().add(unit).isZero();
                            boolean fu = !vert.secondSign().add(unit).isZero();
                            boolean sl = !svert.firstSign().add(unit).isZero();
                            boolean su = !svert.secondSign().add(unit).isZero();
                            FlowEdge<R> edge = new FlowEdge<R>(vert.getMiddle(), svert.getMiddle(), 
                                    fl, fu, sl, su);
                            edges.add(edge);
                        }
                        if (!frm.isZero()) extraCircle = !extraCircle;
                        boolean combine = checkForSameEndpoints(edges);
                        if (combine) {
                            extraCircle = combineEndpoints(edges, extraCircle, topTypes);
                        }
                    }
                }
            }
        }
        return new GraphStructure<R>(edges, extraCircle);
    }
    
    private int firstChange(ArrayList<Integer> positions, ArrayList<Generator<R>> compare, 
            ArrayList<Integer> fpos) {
        int fc = 1000000;
        for (int pos : positions) {
            int change = changeOf(pos, compare, fpos);
            if (change < fc) fc = change;
        }
        return fc;
    }
    
    private int secondChange(ArrayList<Integer> positions, ArrayList<Generator<R>> compare, 
            ArrayList<Integer> fpos) {
        int sc = -1;
        for (int pos : positions) {
            int change = changeOf(pos, compare, fpos);
            if (change > sc) sc = change;
        }
        return sc;
    }
    
    private boolean dealWithNFraming(int fChange, EvenStableGenerator<R> gen,
            ArrayList<FlowVertex<R>> topLine, ArrayList<FlowEdge<R>> edges) {
        int crs = theLink.getCross(fChange);
        int p = gen.getPosition(fChange);
        boolean tilde = true;
        if (crs < 0) {
            p = p + crs;
            if (p % 2 == 0) tilde = false;
        }
        else {
            if (p % 2 != 0) tilde = false;
        }
        R frm = unitTwo.getZero();
        if (!tilde) frm = frameFrom(gen.getPosition(), fChange);
        FlowVertex<R> vert = topLine.get(0);
        FlowVertex<R> svert = topLine.get(1);
        boolean fl = !vert.firstSign().add(unit).isZero();
        boolean fu = !vert.secondSign().add(unit).isZero();
        boolean sl = !svert.firstSign().add(unit).isZero();
        boolean su = !svert.secondSign().add(unit).isZero();
        if (vert.getMiddle() == svert.getMiddle()) {
            if (tilde && !fl) frm = frm.add(unitTwo); // extra sign change in Whitney
            return frm.isZero();
        }// 
        FlowEdge<R> edge = new FlowEdge<R>(vert.getMiddle(), svert.getMiddle(), fl, fu,
                    sl, su);
        edges.add(edge);
        return !frm.isZero();
    }
    
    private R frameFrom(ArrayList<Integer> position, int j) {
        int a = 0;
        for (int i = 0; i < j; i++) {
            int p = position.get(i);
            int crs = theLink.getCross(i);
            if (crs < 0) p = p + crs;
            a = a + p;
        }
        if (a%2 == 0) return unitTwo.getZero();
        return unitTwo;
    }
    
    private boolean bothSurgeries(ArrayList<Integer> position, int fChange, int sChange) {
        int crs = theLink.getCross(fChange);
        int p = position.get(fChange);
        if (crs < 0) {
            p = p + crs;
            if (p != -1) return false;
        }
        else {
            if (p != 0) return false;
        }
        crs = theLink.getCross(sChange);
        p = position.get(sChange);
        if (crs < 0) {
            p = p + crs;
            if (p != -1) return false;
        }
        else {
            if (p != 0) return false;
        }
        return true;
    }
    
    private R getFrameValue(FlowVertex<R> vert, FlowVertex<R> svert) {
        R a = vert.firstSign();
        R b = svert.firstSign();
        if (a.add(unit).isZero()) a = unitTwo;
        else a = unitTwo.getZero();
        if (b.add(unit).isZero()) b = unitTwo;
        else b = unitTwo.getZero();
        return a.multiply(a.add(b));
    }
    
    private ArrayList<int[]> getTypesOf(ArrayList<FlowVertex<R>> line) {
        ArrayList<int[]> types = new ArrayList<int[]>();
        for (int i = 0; i < line.size(); i++) {
            int[] typ = new int[2];
            typ[0] = getTypeOfMorphism(line.get(i).firstArrow());
            typ[1] = getTypeOfMorphism(line.get(i).secondArrow());
            types.add(typ);
        }
        return types;
    }
    
    private int getTypeOfMorphism(Arrow<R> arr) {
        FlowGenerator<R> bGen = (FlowGenerator<R>) arr.getBotGenerator();
        FlowGenerator<R> tGen = (FlowGenerator<R>) arr.getTopGenerator();
        if (bGen.gen.getSigns().size() != tGen.gen.getSigns().size()) return 0; // it's a surgery
        boolean found = false;
        int i = 0;
        while (!found) {
            if (!Objects.equals(bGen.gen.getPosition(i), tGen.gen.getPosition(i))) found = true;
            else i++;
        }
        R sign = signOf(bGen.gen.getPosition(), i, true);
        if (sign.add(arr.getValue()).isZero()) return 1; // it's a negative point.
        int p = bGen.gen.getPosition(i);
        int crs = theLink.getCross(i);
        if (crs < 0) {
            if ((p+crs) % 2 == 0) return 2; // it's a positive point, which comes along with a neg one
        }
        else if (p % 2 != 0) return 2;
        ArrayList<Integer> circle = theLink.circleAt(theLink.getPath(i, 0), bGen.gen.getPosition());
        if (circle.contains(theLink.getPath(i, 2))) {// both P dot the same circle
            int apos = bGen.getBotArrows().indexOf(arr);
            if (apos == 0) return 3; // it's the first positive point of two
            FlowGenerator<R> nGen = (FlowGenerator<R>) bGen.getBotArrows().get(apos-1).getTopGenerator();
            if (nGen == tGen) return 4; // it's the second positive point of two
            return 3;
        }
        int u = circleWith(bGen.gen.getCircles(), circle);
        boolean fsgn = bGen.gen.getSigns().get(u);
        boolean ssgn = tGen.gen.getSigns().get(u);
        if (fsgn == ssgn) return 4;
        return 3;
    }
    
    private R getFrameValue(ArrayList<Integer> pos, int fc, int sc, int[] typ) {
        int a = 0;
        for (int i = 0; i < fc; i++) {
            int crs = theLink.getCross(i);
            int p = pos.get(i);
            if (crs < 0) p = p + crs;
            a = a + p;
        }
        int b = 0;
        int u = fc;
        if (typ[0] == 1) {
            a++;
            if (theLink.getCross(fc) < 0) b = 1;
            u++;
        }
        for (int i = u; i < sc; i++) {
            int crs = theLink.getCross(i);
            int p = pos.get(i);
            if (crs < 0) p = p + crs;
            b = b + p;
        }
        if (typ[1] == 1) b++;
        if ((a*b)%2 == 0) return unitTwo.getZero();
        return unitTwo;
    }
    
    private void dealWithLadyBug(int fChange, int sChange, EvenStableGenerator<R> gen,
            ArrayList<FlowVertex<R>> topLine, ArrayList<FlowVertex<R>> botLine,
            ArrayList<Generator<R>> compare, OneFlowCategory<R> cat, ArrayList<FlowEdge<R>> edges) {
        int cst = theLink.getPath(fChange, 0);
        ArrayList<ArrayList<Integer>> relCircles = theLink.getLadyBug(cst, fChange, sChange, 
                gen.getPosition());
        FlowVertex<R> svert = botLine.get(0);
        int ps = cat.positionOf(0, svert.getMiddle());
        EvenStableGenerator<R> smGen = (EvenStableGenerator<R>) compare.get(ps);
        int sp = smGen.circleWith(relCircles.get(1));
        boolean ss = smGen.getSigns().get(sp);
        FlowVertex<R> vert = topLine.get(0);
        FlowGenerator<R> fGen = vert.getMiddle();
        int pos = cat.positionOf(0, fGen);
        EvenStableGenerator<R> mGen = (EvenStableGenerator<R>) compare.get(pos);
        int p = mGen.circleWith(relCircles.get(0));
        boolean s = mGen.getSigns().get(p);
        if (s == ss) {
            boolean fl = !vert.firstSign().add(unit).isZero();
            boolean fu = !vert.secondSign().add(unit).isZero();
            boolean sl = !svert.firstSign().add(unit).isZero();
            boolean su = !svert.secondSign().add(unit).isZero();
            FlowEdge<R> edge = new FlowEdge<R>(vert.getMiddle(), svert.getMiddle(), fl, fu,
                    sl, su);
            edges.add(edge);
            vert = topLine.get(1);
            svert = botLine.get(1);
            fl = !vert.firstSign().add(unit).isZero();
            fu = !vert.secondSign().add(unit).isZero();
            sl = !svert.firstSign().add(unit).isZero();
            su = !svert.secondSign().add(unit).isZero();
            edge = new FlowEdge<R>(vert.getMiddle(), svert.getMiddle(), fl, fu,
                    sl, su);
            edges.add(edge);
        }
        else {
            boolean fl = !vert.firstSign().add(unit).isZero();
            boolean fu = !vert.secondSign().add(unit).isZero();
            boolean sl = !botLine.get(1).firstSign().add(unit).isZero();
            boolean su = !botLine.get(1).secondSign().add(unit).isZero();
            FlowEdge<R> edge = new FlowEdge<R>(vert.getMiddle(), botLine.get(1).getMiddle(), fl, fu,
                    sl, su);
            edges.add(edge);
            fl = !topLine.get(1).firstSign().add(unit).isZero();
            fu = !topLine.get(1).secondSign().add(unit).isZero();
            sl = !svert.firstSign().add(unit).isZero();
            su = !svert.secondSign().add(unit).isZero();
            edge = new FlowEdge<R>(topLine.get(1).getMiddle(), svert.getMiddle(), fl, fu,
            sl, su);
            edges.add(edge);
        }
    }
    
    private boolean combineEndpoints(ArrayList<FlowEdge<R>> edges, boolean extra, 
            ArrayList<int[]> topTypes) {
        // we're implicitly assuming there are two edges, and that fE.fV != sE.sV
        FlowEdge<R> fEdge = edges.get(0);
        FlowEdge<R> sEdge = edges.get(1);
        edges.clear();
        if (fEdge.firstVertex() == sEdge.firstVertex()) {
            if (fEdge.secondVertex() == sEdge.secondVertex()) { // get one circle
                if (fEdge.firstSign() != sEdge.firstSign()) {
                    if (fEdge.firstLowerSign() == sEdge.firstLowerSign()) {
                        if (!fEdge.firstLowerSign()) extra = !extra; // this is whitney trick in second coordinate with pos in first
                    }
                    if (fEdge.secondLowerSign() == sEdge.secondLowerSign()) {
                        if (!fEdge.secondLowerSign()) extra = !extra;
                    }
                    return !extra;
                }
                else {
                    boolean pos;
                    int[] topone = topTypes.get(0);
                    int[] toptwo = topTypes.get(1);
                    if (topone[0] == toptwo[0]) pos = fEdge.firstLowerSign();
                    else pos = fEdge.secondLowerSign();
                    return !pos;
                }
            }
            else {// one interval by combining first vertices
                if (fEdge.firstSign() != sEdge.firstSign()) { // Whitney situation
                    if (fEdge.firstLowerSign() == sEdge.firstLowerSign() && !fEdge.firstLowerSign())
                        extra = !extra;
                    edges.add(new FlowEdge<R>(fEdge.secondVertex(), sEdge.secondVertex(), 
                            fEdge.secondLowerSign(), fEdge.secondUpperSign(), 
                            sEdge.secondLowerSign(), sEdge.secondUpperSign()));
                    return extra;
                }
                else { // we get a directed edge
                    if (topTypes.get(0)[0] == 3 && topTypes.get(1)[0] == 4) { // from fEdge to sEdge
                        edges.add(new FlowEdge<R>(fEdge.secondVertex(), sEdge.secondVertex(), 
                                fEdge.secondLowerSign(), fEdge.secondUpperSign(), 
                                sEdge.secondLowerSign(), sEdge.secondUpperSign()));
                        return extra; 
                    }
                    if (topTypes.get(0)[0] == 4 && topTypes.get(1)[0] == 3) {
                        edges.add(new FlowEdge<R>(sEdge.secondVertex(), fEdge.secondVertex(), 
                                sEdge.secondLowerSign(), sEdge.secondUpperSign(),
                                fEdge.secondLowerSign(), fEdge.secondUpperSign()));
                        return extra; 
                    }
                    if (!fEdge.firstLowerSign()) extra = !extra; 
                    if (topTypes.get(0)[1] == 3 && topTypes.get(1)[1] == 4) {
                        edges.add(new FlowEdge<R>(fEdge.secondVertex(), sEdge.secondVertex(), 
                                fEdge.secondLowerSign(), fEdge.secondUpperSign(), 
                                sEdge.secondLowerSign(), sEdge.secondUpperSign()));
                        return extra;
                    }
                    if (topTypes.get(0)[1] == 4 && topTypes.get(1)[1] == 3) {
                        edges.add(new FlowEdge<R>(sEdge.secondVertex(), fEdge.secondVertex(), 
                                sEdge.secondLowerSign(), sEdge.secondUpperSign(),
                                fEdge.secondLowerSign(), fEdge.secondUpperSign()));
                        return extra;
                    }
                }
            }
        }
        else { // fE.sV == sE.sV
            if (fEdge.secondSign() != sEdge.secondSign()) { // Whitney situation
                if (fEdge.secondLowerSign() == sEdge.secondLowerSign() && !fEdge.secondLowerSign())
                        extra = !extra;
                edges.add(new FlowEdge<R>(fEdge.firstVertex(), sEdge.firstVertex(), 
                        fEdge.firstLowerSign(), fEdge.firstUpperSign(), 
                        sEdge.firstLowerSign(), sEdge.firstUpperSign()));
                return extra;
            }
            else {
                if (topTypes.get(0)[1] == 3 && topTypes.get(1)[1] == 4) { // from fEdge to sEdge
                    edges.add(new FlowEdge<R>(fEdge.firstVertex(), sEdge.firstVertex(), 
                            fEdge.firstLowerSign(), fEdge.firstUpperSign(), 
                            sEdge.firstLowerSign(), sEdge.firstUpperSign()));
                    return extra; 
                }
                if (topTypes.get(0)[1] == 4 && topTypes.get(1)[1] == 3) {
                    edges.add(new FlowEdge<R>(sEdge.firstVertex(), fEdge.firstVertex(), 
                            sEdge.firstLowerSign(), sEdge.firstUpperSign(),
                            fEdge.firstLowerSign(), fEdge.firstUpperSign()));
                    return extra;
                }
                if (!fEdge.secondLowerSign()) extra = !extra;
                if (topTypes.get(0)[0] == 3 && topTypes.get(1)[0] == 4) {
                    edges.add(new FlowEdge<R>(fEdge.firstVertex(), sEdge.firstVertex(), 
                            fEdge.firstLowerSign(), fEdge.firstUpperSign(), 
                            sEdge.firstLowerSign(), sEdge.firstUpperSign()));
                    return extra;
                }
                if (topTypes.get(0)[0] == 4 && topTypes.get(1)[0] == 3) {
                    edges.add(new FlowEdge<R>(sEdge.firstVertex(), fEdge.firstVertex(), 
                            sEdge.firstLowerSign(), sEdge.firstUpperSign(),
                            fEdge.firstLowerSign(), fEdge.firstUpperSign()));
                    return extra;
                }
            }
        }
        //System.out.println("Can we get here?");
        return extra;
    }
    
    private boolean checkForSameEndpoints(ArrayList<FlowEdge<R>> edges) {
        ArrayList<FlowGenerator<R>> gens = new ArrayList<FlowGenerator<R>>();
        boolean same = false;
        for (FlowEdge<R> edge : edges) {
            if (gens.contains(edge.firstVertex())) same = true;
            else gens.add(edge.firstVertex());
            if (gens.contains(edge.secondVertex())) same = true;
            else gens.add(edge.secondVertex());
        }
        return same;
    }
    
    private int matchWith(int[] typ, ArrayList<int[]> types) {
        boolean found = false;
        int i = 0;
        while (!found) {
            int[] ntyp = types.get(i);
            if (ntyp[0] == typ[1] && ntyp[1] == typ[0]) found = true;
            else i++;
        }
        return i;
    }
    
}
