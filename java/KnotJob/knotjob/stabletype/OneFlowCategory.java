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
import knotjob.homology.Arrow;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class OneFlowCategory<R extends Ring<R>> {
    
    protected final ArrayList<ArrayList<FlowGenerator<R>>> generators;
    protected final DialogWrap frame;
    protected final AbortInfo abInf;
    protected final R unit;
    protected final R unitTwo;
    protected final R unitFour;
    
    public OneFlowCategory(DialogWrap frm, AbortInfo abnf, R unt, R untt, R untf) {
        generators = new ArrayList<ArrayList<FlowGenerator<R>>>();
        frame = frm;
        abInf = abnf;
        unit = unt;
        unitTwo = untt;
        unitFour = untf;
    }
    
    public void addGenerators(ArrayList<FlowGenerator<R>> gens) {
        generators.add(0, gens);
    }
    
    public int totalObjects() {
        int total = 0;
        for (ArrayList<FlowGenerator<R>> gens : generators) total = total+gens.size();
        return total;
    }
    
    public int totalLevel() {
        return generators.size();
    }
    
    public void removeUnnecessary() {
        int i = generators.size()-1;
        boolean cont = true;
        while (cont && i >= 0) { // remove from the top
            ArrayList<FlowGenerator<R>> gens = generators.get(i);
            if (gens.isEmpty()) {
                generators.remove(i);
                i--;
            }
            else cont = false;
        }
        cont = true;
        while (cont && !generators.isEmpty()) { // remove from the bottom
            ArrayList<FlowGenerator<R>> gens = generators.get(0);
            if (gens.isEmpty()) {
                generators.remove(0);
            }
            else cont = false;
        }
    }
    
    public FlowGenerator<R> getGenerator(int i, int j) {
        return generators.get(i).get(j);
    }
    
    public int positionOf(int i, FlowGenerator<R> gen) {
        return generators.get(i).indexOf(gen);
    }
    
    public void output() {
        this.output(0,generators.size());
    }
    
    public R getUnit() {
        return unit;
    }
    
    public R getUnitTwo() {
        return unitTwo;
    }
    
    public R getUnitFour() {
        return unitFour;
    }
    
    public void output(int fh, int lh) {
        for (int i = fh; i < lh; i++) {
            System.out.println();
            System.out.println("Level "+i);
            for (int j = 0; j < (generators.get(i)).size(); j++) {
                System.out.println();
                System.out.println("Generator "+j);
                ArrayList<FlowGenerator<R>> nextLev = null;
                ArrayList<FlowGenerator<R>> vnextLev = null;
                if (i < generators.size()-1) nextLev = generators.get(i+1);
                if (i < generators.size()-2) vnextLev = generators.get(i+2);
                generators.get(i).get(j).output(nextLev, vnextLev);
            }
        }
        System.out.println();
        System.out.println();
    }
    
    private void output(FlowGenerator<R> bGen, FlowGenerator<R> tGen) {
        int i = findGen(bGen);
        System.out.println(generators.get(i).indexOf(bGen));
        System.out.println("hdeg = "+bGen.hdeg());
        for (Arrow<R> arr : bGen.getBotArrows()) {
            FlowGenerator<R> mGen = (FlowGenerator<R>) arr.getTopGenerator();
            Arrow<R> nArr = arrowTo(mGen, tGen);
            if (nArr != null) System.out.println(arr.getValue()+" to "
                    +generators.get(i+1).indexOf(mGen)+" "+nArr.getValue());
        }
        for (ModOne<R> mod : bGen.getBotMod()) {
            if (mod.getTopGenerator() == tGen) {
                for (FlowEdge<R> edge : mod.getEdges()) {
                    System.out.println("Via "+
                            generators.get(i+1).indexOf(edge.firstVertex())+
                            " and "+generators.get(i+1).indexOf(edge.secondVertex()));
                }
            }
        }
    }
    
    public void output(int i, int fg, int lg) {
        FlowGenerator<R> bGen = generators.get(i).get(fg);
        FlowGenerator<R> tGen = generators.get(i+3).get(lg);
        ArrayList<FlowGenerator<R>> iplusone = new ArrayList<FlowGenerator<R>>();
        ArrayList<FlowGenerator<R>> iplustwo = new ArrayList<FlowGenerator<R>>();
        for (Arrow<R> arr : bGen.getBotArrows()) {
            FlowGenerator<R> mGen = (FlowGenerator<R>) arr.getTopGenerator();
            if (!iplusone.contains(mGen)) iplusone.add(mGen);
        }
        for (FlowGenerator<R> gen : iplusone) {
            for (Arrow<R> arr : gen.getBotArrows()) {
                FlowGenerator<R> mGen = (FlowGenerator<R>) arr.getTopGenerator();
                if (arrowTo(mGen, tGen) != null && !iplustwo.contains(mGen))
                    iplustwo.add(mGen);
            }
        }
        int j = iplusone.size()-1;
        while (j >= 0) {
            FlowGenerator<R> mGen = iplusone.get(j);
            boolean found = false;
            int k = 0;
            while (!found && k < mGen.getBotArrows().size()) {
                FlowGenerator<R> nGen = (FlowGenerator<R>) mGen.getBotArrows().get(k).getTopGenerator();
                if (iplustwo.contains(nGen)) found = true;
                else k++;
            }
            if (!found) iplusone.remove(mGen);
            j--;
        }
        System.out.println("Level 0");
        System.out.println();
        System.out.println("Generator "+fg);
        System.out.println(bGen.gen.getPosition()+" "+bGen.gen.getSigns());
        System.out.println("hdeg = "+generators.get(i).get(fg).hdeg());
        if (bGen.gen != null) System.out.println(bGen.gen.getPosition()+" "+bGen.gen.getCircles()+
                bGen.gen.getSigns());
        for (Arrow<R> arr : bGen.getBotArrows()) {
            if (iplusone.contains((FlowGenerator<R>) arr.getTopGenerator())) 
                System.out.println("To "+generators.get(i+1).indexOf(arr.getTopGenerator())+
                        " with Value "+arr.getValue());
        }
        for (ModOne<R> mod : bGen.getBotMod()) {
            if (iplustwo.contains(mod.getTopGenerator())) {
                mod.output(generators.get(i+1), generators.get(i+2));
            }
        }
        System.out.println();
        System.out.println("Level 1");
        System.out.println();
        for (FlowGenerator<R> mGen : iplusone) {
            System.out.println("Generator "+generators.get(i+1).indexOf(mGen));
            System.out.println(mGen.gen.getPosition()+" "+mGen.gen.getSigns());
            System.out.println("hdeg = "+mGen.hdeg());
            if (mGen.gen != null) System.out.println(mGen.gen.getPosition()+" "+mGen.gen.getCircles()+
                mGen.gen.getSigns());
            for (Arrow<R> arr : mGen.getBotArrows()) {
                if (iplustwo.contains((FlowGenerator<R>) arr.getTopGenerator())) 
                    System.out.println("To "+generators.get(i+2).indexOf(arr.getTopGenerator())+
                            " with Value "+arr.getValue());
            }
            for (ModOne<R> mod : mGen.getBotMod()) {
                if (mod.getTopGenerator() == tGen) 
                mod.output(generators.get(i+2), generators.get(i+3));
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("Level 2");
        System.out.println();
        for (FlowGenerator<R> mGen : iplustwo) {
            System.out.println("Generator "+generators.get(i+2).indexOf(mGen));
            System.out.println(mGen.gen.getPosition()+" "+mGen.gen.getSigns());
            System.out.println("hdeg = "+mGen.hdeg());
            if (mGen.gen != null) System.out.println(mGen.gen.getPosition()+" "+mGen.gen.getCircles()+
                mGen.gen.getSigns());
            for (Arrow<R> arr : mGen.getBotArrows()) {
                if ((FlowGenerator<R>) arr.getTopGenerator() == tGen) 
                    System.out.println("To "+generators.get(i+3).indexOf(arr.getTopGenerator())+
                            " with Value "+arr.getValue());
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("Level 3");
        System.out.println();
        System.out.println("Generator "+generators.get(i+3).indexOf(tGen));
        System.out.println(tGen.gen.getPosition()+" "+tGen.gen.getSigns());
        System.out.println("hdeg = "+tGen.hdeg());
        if (tGen.gen != null) System.out.println(tGen.gen.getPosition()+" "+tGen.gen.getCircles()+
                tGen.gen.getSigns());
        System.out.println();
        System.out.println();
    }
    
    public void troubleChecker(int minh, int maxh) {
        ArrayList<FlowGenerator<R>> lguys = generators.get(0);
        for (FlowGenerator<R> gen : lguys) {
            if (!gen.getBotMod().isEmpty() && gen.hdeg() == minh-1) System.out.println("Trouble");
        }
        lguys = generators.get(generators.size()-1);
        for (FlowGenerator<R> gen : lguys) {
            if (!gen.getTopMod().isEmpty() && gen.hdeg() == maxh+1) System.out.println("Trouble");
        }    
    }
    
    public void removeGenerators(int minh, int maxh) {
        ArrayList<FlowGenerator<R>> lguys = generators.get(0);
        while (lguys.isEmpty() || lguys.get(0).hdeg() <= minh-1) {
            generators.remove(0);
            lguys = generators.get(0);
        }
        lguys = generators.get(generators.size()-1);
        while (lguys.isEmpty() || lguys.get(0).hdeg() >= maxh+1) {
            generators.remove(generators.size()-1);
            lguys = generators.get(generators.size()-1);
        }
    }
    
    public void changify(int i) {
        boolean changified = false;
        while (!changified) {
            FlowGenerator<R> bGen = findBestBottom(i);
            if (bGen == null) changified = true;
            else {
                ModOne<R> eta = findBestEta(bGen);
                isolateEtaTop(eta, i);
                isolateEtaBottom(eta, i);
            }
        }
    }
    
    protected void isolateEtaBottom(ModOne<R> eta, int i) {
        FlowGenerator<R> bGen = eta.getBotGenerator();
        FlowGenerator<R> tGen = eta.getTopGenerator();
        while (bGen.getBotMod().size() > 1) {
            ModOne<R> oEta = otherBotEta(bGen, eta);
            handleSlide(oEta.getTopGenerator(), tGen, unit);
            if (!oEta.getTopGenerator().getBotArrows().isEmpty()) cleanUpModZeros(i+3, false);
            if (!tGen.getTopArrows().isEmpty()) cleanUpModZeros(i+2, true);
        }
    }
    
    protected void isolateEtaTop(ModOne<R> eta, int i) {
        FlowGenerator<R> bGen = eta.getBotGenerator();
        FlowGenerator<R> tGen = eta.getTopGenerator();
        while (tGen.getTopMod().size() > 1) {
            ModOne<R> oEta = otherTopEta(tGen, eta);
            handleSlide(bGen, oEta.getBotGenerator(), unit);
            if (!oEta.getBotGenerator().getTopArrows().isEmpty()) cleanUpModZeros(i, true);
            if (!bGen.getBotArrows().isEmpty()) cleanUpModZeros(i+1, false);
        }
    }
    
    protected ModOne<R> otherBotEta(FlowGenerator<R> gen, ModOne<R> eta) {
        boolean found = false;
        int i = 0;
        while (!found) {
            ModOne<R> oEta = gen.getBotMod().get(i);
            if (oEta != eta) found = true;
            else i++;
        }
        return gen.getBotMod().get(i);
    }
    
    protected ModOne<R> otherTopEta(FlowGenerator<R> gen, ModOne<R> eta) {
        boolean found = false;
        int i = 0;
        while (!found) {
            ModOne<R> oEta = gen.getTopMod().get(i);
            if (oEta != eta) found = true;
            else i++;
        }
        return gen.getTopMod().get(i);
    }
    
    private ModOne<R> findBestEta(FlowGenerator<R> bGen) {
        int best = 0;
        int[] bestOrder = torsionOrder(bGen.getBotMod().get(0).getTopGenerator());
        for (int i = 1; i < bGen.getBotMod().size(); i++) {
            ModOne<R> nEta = bGen.getBotMod().get(i);
            int[] order = torsionOrder(nEta.getTopGenerator());
            if (compareTopOrder(bestOrder, order)) {
                best = i;
                bestOrder = order;
                //System.out.println("XX "+order[0]+" "+order[1]);
            }
        }
        return bGen.getBotMod().get(best);
    }
    
    private FlowGenerator<R> findBestBottom(int i) {
        ArrayList<FlowGenerator<R>> gens = generators.get(i);
        int[] candOrder = new int[] {0, 0};
        int cand = -1;
        int j = 0;
        while (j < gens.size()) {
            FlowGenerator<R> bGen = gens.get(j);
            if (hasLotsOfEtas(bGen)) {
                int[] order = torsionOrder(bGen);
                if (cand == -1 || compareBotOrder(candOrder, order)) {
                    cand = j;
                    candOrder = order;
                    //System.out.println(order[0]+" "+order[1]);
                }
            }
            j++;
        }
        if (cand >= 0) return gens.get(cand);
        return null;
    }
    
    private boolean compareTopOrder(int[] fOrder, int[] sOrder) { // true if sOrder is better than fOrder
        if (fOrder[0] == 0) {
            if (sOrder[0] != 0) return false;
            if (sOrder[1] == 0) return false;
            return (fOrder[1] == 0 || sOrder[1] < fOrder[1]);
        }
        if (sOrder[0] == 0) return true;
        return sOrder[0] > fOrder[0];
    }
    
    private boolean compareBotOrder(int[] fOrder, int[] sOrder) { // true if sOrder is better than fOrder
        if (fOrder[0] == 0) {
            if (sOrder[0] != 0) return true;
            if (fOrder[1] == 0) return false; // improvement not possible
            return (sOrder[1] == 0 || sOrder[1] > fOrder[1]);
        }
        if (sOrder[0] == 0) return false;
        return sOrder[0] < fOrder[0];
    }
    
    private int[] torsionOrder(FlowGenerator<R> gen) {
        int[] order = new int[2];
        if (!gen.getTopArrows().isEmpty()) order[0] = intOfValue(gen.getTopArrows().get(0).getValue());
        if (!gen.getBotArrows().isEmpty()) order[1] = intOfValue(gen.getBotArrows().get(0).getValue());
        return order;
    }
    
    private int intOfValue(R value) {
        String val = value.toString();
        int klammer = val.indexOf(" (");
        return Integer.parseInt(val.substring(0, klammer));
    }
    
    private boolean hasLotsOfEtas(FlowGenerator<R> bGen) {
        if (bGen.getBotMod().isEmpty()) return false;
        if (bGen.getBotMod().size() > 1) return true;
        FlowGenerator<R> tGen = bGen.getBotMod().get(0).getTopGenerator();
        return tGen.getTopMod().size() > 1;
    }
    
    private void cleanUpModZeros(int i, boolean botPrior) {
        // This will need to be similar to the normalization
        R prime = unit.add(unit);
        R primePower = prime;
        while (!primePower.isZero()) {
            Arrow<R> arr = findGoodArrow(i, primePower, botPrior);
            if (arr != null) {
                isolateBottom(arr);
                isolateTop(arr);
            }
            else primePower = primePower.multiply(prime);
        }
    }
    
    public void normalize() {
        int i = generators.size()-1;
        R prime = unit.add(unit); // this starts off with the value 2
        while (i >= 0 && !abInf.isAborted()) {
            R primePower = unit;
            int t = generators.get(i).size();
            frame.setLabelRight(" "+i, 1, false);
            while (!primePower.isZero() && !abInf.isAborted()) {
                Arrow<R> arr = findArrow(i, primePower, !primePower.isInvertible());
                if (arr != null) {
                    isolateBottom(arr);
                    isolateTop(arr);
                    if (primePower.isInvertible()) {
                        cancelArrow(arr, i);
                        t--;
                        frame.setLabelRight(" "+t, 2, false);
                    }
                }
                else primePower = primePower.multiply(prime);
            }
            i--;
        }
        if (abInf.isAborted()) return;
        R primePower = prime;
        while (!primePower.isZero()) {
            i = generators.size()-1;
            while (i >= 0) {
                setArrowsTo(primePower, i);
                i--;
            }
            primePower = primePower.multiply(prime);
        }
    }
    
    private void setArrowsTo(R primePower, int i) {
        for (FlowGenerator<R> gen : generators.get(i)) {
            for (Arrow<R> arr : gen.getTopArrows()) {
                if (arr.getValue().div(primePower).isInvertible()) arr.setValue(primePower);
            }
        }
    }
    
    protected void cancelArrow(Arrow<R> arr, int i) {
        generators.get(i).remove((FlowGenerator<R>) arr.getTopGenerator());
        generators.get(i-1).remove((FlowGenerator<R>) arr.getBotGenerator());
        FlowGenerator<R> top = (FlowGenerator<R>) arr.getTopGenerator();
        FlowGenerator<R> bot = (FlowGenerator<R>) arr.getBotGenerator();
        for (ModOne mod : top.getBotMod()) mod.getTopGenerator().getTopMod().remove(mod);
        for (ModOne mod : top.getTopMod()) mod.getBotGenerator().getBotMod().remove(mod);
        for (ModOne mod : bot.getBotMod()) mod.getTopGenerator().getTopMod().remove(mod);
        for (ModOne mod : bot.getTopMod()) mod.getBotGenerator().getBotMod().remove(mod);       
    }
    
    private boolean isolateBottom(Arrow<R> arr) {
        int m = arr.getBotGenerator().getBotArrows().size()-1;
        while (m >= 0) {
            Arrow<R> ar = arr.getBotGenerator().getBotArrows().get(m);
            if (ar != arr) {
                R val = ar.getValue().div(arr.getValue()).negate();
                handleSlide((FlowGenerator<R>) ar.getTopGenerator(), 
                        (FlowGenerator<R>) arr.getTopGenerator(), val);
            }
            m--;
        }
        return (arr.getBotGenerator().getBotArrows().size() == 1);
    }
    
    private boolean isolateTop(Arrow<R> arr) {
        int m = arr.getTopGenerator().getTopArrows().size()-1;
        while (m >= 0) {
            Arrow<R> ar = arr.getTopGenerator().getTopArrows().get(m);
            if (ar != arr) {
                R val = ar.getValue().div(arr.getValue());
                handleSlide((FlowGenerator<R>) arr.getBotGenerator(),
                        (FlowGenerator<R>) ar.getBotGenerator(), val);
            }
            m--;
        }
        return (arr.getTopGenerator().getTopArrows().size() == 1);
    }
    
    protected void handleSlide(FlowGenerator<R> xGen, FlowGenerator<R> yGen, R val) {
        if (!unitTwo.multiply(val).isZero()) moveModsIntoY(xGen, yGen);
        movePointsIntoY(xGen, yGen, val);
        movePointsOutX(xGen, yGen, val);
        if (!unitFour.multiply(val).isZero()) moveModsOutX(xGen, yGen, val);
        fixTheSigns(xGen);
    }
    
    protected void fixTheSigns(FlowGenerator<R> xGen) {
        for (ModOne<R> md : xGen.getTopMod()) {
            for (FlowEdge<R> ed : md.getEdges()) {
                boolean foundl = false;
                boolean foundr = false;
                int fleft = -1;
                int fright = -1;
                int i = 0;
                while (i < xGen.getTopArrows().size() && (!foundl | !foundr)) {
                    Arrow<R> ard = xGen.getTopArrows().get(i);
                    if (ard.getBotGenerator() == ed.firstVertex()) {
                        foundl = true;
                        fleft = i;
                    }
                    if (ard.getBotGenerator() == ed.secondVertex()) {
                        foundr = true;
                        fright = i;
                    }
                    i++;
                }
                Arrow<R> arl = xGen.getTopArrows().get(fleft);
                Arrow<R> arr = xGen.getTopArrows().get(fright);
                if (ed.firstUpperSign() == 
                        unitFour.multiply(arl.getValue()).add(unitFour).isZero()) {
                    ed.changeSign(false, true);
                }
                if (ed.secondUpperSign() == 
                        unitFour.multiply(arr.getValue()).add(unitFour).isZero()) {
                    ed.changeSign(false, false);
                    md.changeCircle();
                }
            }
        }
    }
    
    protected void moveModsOutX(FlowGenerator<R> xGen, FlowGenerator<R> yGen, R val) {
        boolean evenfactor = unitTwo.multiply(val).isZero();
        boolean minusslide = unitFour.multiply(val).add(unitFour).isZero();
        for (ModOne<R> mod : yGen.getTopMod()) {
            boolean found = false;
            int i = 0;
            while (!found && i < xGen.getTopMod().size()) {
                if (xGen.getTopMod().get(i).getBotGenerator() == mod.getBotGenerator()) found = true;
                else i++;
            }
            ModOne<R> md;
            if (found) md = xGen.getTopMod().get(i);
            else {
                md = new ModOne<R>(mod.getBotGenerator(), xGen);
                xGen.getTopMod().add(md);
                mod.getBotGenerator().addBotMod(md);
            }
            if (evenfactor) {
                int u = mod.edgeNumber();
                if (u % 2 != 0) md.changeCircle();
            }
            int oldEdges = md.edgeNumber();
            if (!evenfactor) {
                md.setCircle(!(md.extraCircle() ^ mod.extraCircle()));
                for (FlowEdge<R> ed : mod.getEdges()) {
                    boolean tl = !(!minusslide ^ ed.firstUpperSign());
                    boolean tr = !(!minusslide ^ ed.secondUpperSign());
                    FlowEdge<R> eddx = new FlowEdge<R>(ed.firstVertex(), ed.secondVertex(),
                            ed.firstLowerSign(), tl, ed.secondLowerSign(), tr);
                    md.getEdges().add(eddx);
                }
            }
            if (!evenfactor) cleanUpEdgesOutX(oldEdges, xGen, md);
            if (md.isEmpty()) {
                md.getBotGenerator().getBotMod().remove(md);
                md.getTopGenerator().getTopMod().remove(md);
            }
        }
    }
    
    private void cleanUpEdgesOutX(int oldEdges, FlowGenerator<R> xGen, ModOne<R> md) {
        int i = 0;
        while (i < oldEdges) {
            FlowEdge<R> oed = md.getEdges().get(i);
            boolean found = false;
            boolean modify = false;
            int j = oldEdges;
            FlowEdge<R> ned = oed;
            while (!found && j < md.edgeNumber()) {
                ned = md.getEdges().get(j);
                if (ned.firstVertex() == oed.firstVertex() || 
                        ned.secondVertex() == oed.firstVertex()) found = true;
                else j++;
            }
            boolean finished = false;
            if (found) {
                modify = true;
                FlowGenerator<R> otherGen = ned.firstVertex();
                boolean to = ned.firstUpperSign();
                boolean bo = ned.firstLowerSign();
                boolean tc = ned.secondUpperSign();
                boolean ow = false;
                if (ned.firstVertex() == oed.firstVertex()) {
                    otherGen = ned.secondVertex();
                    to = ned.secondUpperSign();
                    bo = ned.secondLowerSign();
                    tc = ned.firstUpperSign();
                    ow = true;
                }
                boolean downarrow = (tc == oed.firstUpperSign());
                oed.setFirstVertex(otherGen);
                oed.setSign(false, true, to);
                if (!oed.firstLowerSign()) md.changeCircle();
                if (ned.isDirected() && ow) md.changeCircle();
                oed.setSign(true, true, bo);
                md.getEdges().remove(ned);
                if (downarrow) md.changeCircle();
                if (otherGen == oed.secondVertex()) { // it's a circle
                    md.changeCircle();
                    if (!oed.secondLowerSign()) md.changeCircle();
                    md.getEdges().remove(oed);
                    oldEdges--;
                    i--;
                    finished = true;
                }
            }
            if (!finished) {
                found = false;
                j = oldEdges;
                while (!found && j < md.edgeNumber()) {
                    ned = md.getEdges().get(j);
                    if (ned.firstVertex() == oed.secondVertex() || 
                            ned.secondVertex() == oed.secondVertex()) found = true;
                    else j++;
                }
                if (found) {
                    modify = true;
                    FlowGenerator<R> otherGn = ned.firstVertex();
                    boolean to = ned.firstUpperSign();
                    boolean bo = ned.firstLowerSign();
                    boolean ow = true;
                    if (ned.firstVertex() == oed.secondVertex()) {
                        otherGn = ned.secondVertex();
                        to = ned.secondUpperSign();
                        bo = ned.secondLowerSign();
                        ow = false;
                    }
                    oed.setSecondVertex(otherGn);
                    oed.setSign(false, false, to);
                    if (!oed.secondLowerSign()) md.changeCircle();
                    oed.setSign(true, false, bo);
                    md.getEdges().remove(ned);
                    if (ow && ned.isDirected()) md.changeCircle();
                }
                if (modify) {
                    md.getEdges().remove(oed);
                    md.addEdge(oed);
                    oldEdges--;
                    i--;
                }
            }
            i++;
        }
    }
    
    protected void movePointsOutX(FlowGenerator<R> xGen, FlowGenerator<R> yGen, R val) {
        for (Arrow<R> ar : yGen.getTopArrows()) {
            boolean found = false;
            int i = 0;
            while (!found && i < xGen.getTopArrows().size()) {
                Arrow<R> axr = xGen.getTopArrows().get(i);
                //int rr = findGen((FlowGenerator<R>) axr.getBotGenerator());
                if (axr.getBotGenerator() == ar.getBotGenerator()) found = true;
                else i++;
            }
            if (found) {
                Arrow<R> axr = xGen.getTopArrows().get(i);
                axr.addValue(val.multiply(ar.getValue())); 
                if (axr.getValue().isZero()) {
                    axr.getTopGenerator().getTopArrows().remove(axr);
                    axr.getBotGenerator().getBotArrows().remove(axr);
                }
            }
            else {
                if (!val.multiply(ar.getValue()).isZero()) {
                    Arrow<R> axr = new Arrow<R>(ar.getBotGenerator(), xGen,
                            val.multiply(ar.getValue())); 
                    xGen.getTopArrows().add(axr);
                    ar.getBotGenerator().getBotArrows().add(axr);
                }
            }
        }
    }
    
    protected void movePointsIntoY(FlowGenerator<R> xGen, FlowGenerator<R> yGen, R val) {
        for (Arrow<R> arr : xGen.getBotArrows()) {
            boolean caution = false;
            if (!unitFour.multiply(arr.getValue()).isZero() && unitTwo.multiply(arr.getValue()).isZero())
                caution = true;
            boolean found = false;
            int i = 0;
            R u = unitFour.getZero();
            while (!found && i < arr.getTopGenerator().getTopArrows().size()) {
                Arrow<R> ar = arr.getTopGenerator().getTopArrows().get(i);
                if (ar.getBotGenerator() == yGen) found = true;
                else i++;
            }
            if (found) {
                Arrow<R> ar = arr.getTopGenerator().getTopArrows().get(i);
                u = unitFour.multiply(ar.getValue());
                //u = ar.getValue();
                ar.addValue(arr.getValue().multiply(val.negate()));
                if (ar.getValue().isZero()) {
                    ar.getBotGenerator().getBotArrows().remove(ar);
                    ar.getTopGenerator().getTopArrows().remove(ar);
                }
            }
            else {
                Arrow<R> ar = new Arrow<R>(yGen, arr.getTopGenerator(), 
                        arr.getValue().multiply(val.negate()));
                if (!ar.getValue().isZero()) {
                    yGen.addBotArrow(ar);
                    arr.getTopGenerator().addTopArrow(ar);
                }
            }
            if (!unitFour.multiply(val).isZero()) 
                for (Arrow<R> ar : yGen.getTopArrows()) 
                    dealWithMiddleMods(arr, ar, caution, u, val);
        }
    }
    
    protected void dealWithMiddleMods(Arrow<R> arr, Arrow<R> ar, boolean caution, R u, R val) {
        boolean evenFactor = unitTwo.multiply(val).isZero();
        FlowGenerator<R> aGen = (FlowGenerator<R>) arr.getTopGenerator();
        FlowGenerator<R> xGen = (FlowGenerator<R>) arr.getBotGenerator();
        FlowGenerator<R> yGen = (FlowGenerator<R>) ar.getTopGenerator();
        FlowGenerator<R> cGen = (FlowGenerator<R>) ar.getBotGenerator();
        boolean found = false;
        boolean morecaution = false;
        boolean extra = false;
        if (caution && 
                !(unitTwo.multiply(ar.getValue()).isZero() & !unitFour.multiply(ar.getValue()).isZero()))
            morecaution = true;
        int i = 0;
        R v = unitFour.getZero();
        while (!found && i < xGen.getTopArrows().size()) {
            Arrow<R> axr = xGen.getTopArrows().get(i);
            if (axr.getBotGenerator() == ar.getBotGenerator()) found = true;
            else i++;
        }
        if (found) {
            Arrow<R> axr = xGen.getTopArrows().get(i);
            v = unitFour.multiply(axr.getValue());
            if (!unitTwo.multiply(v).isZero()) extra = true;
        }
        boolean noNewEdge = true;
        found = false;
        i = 0;
        while (!found && i < aGen.getTopMod().size()) {
            if (aGen.getTopMod().get(i).getBotGenerator() == ar.getBotGenerator()) found = true;
            else i++;
        }
        ModOne<R> mbd;
        if (found) mbd = aGen.getTopMod().get(i);
        else {
            mbd = new ModOne<R>(cGen, aGen);
            aGen.addTopMod(mbd);
            cGen.addBotMod(mbd);
        }
        if (!evenFactor) {
            if (morecaution && extra) mbd.changeCircle();
            if (!(unitTwo.multiply(arr.getValue()).isZero()) && 
                    !(unitTwo.multiply(ar.getValue()).isZero())) {
                produceMiddleEdges(arr, ar, mbd, xGen, yGen, val);
                noNewEdge = false;
            }
        }
        if (noNewEdge) easyClean(found, arr, ar, mbd, v, val);
        else if (found) moreDiffClean(arr, ar, mbd, u, v, val);
        if (mbd.isEmpty()) {
            mbd.getBotGenerator().getBotMod().remove(mbd);
            mbd.getTopGenerator().getTopMod().remove(mbd);
        }
    }
    
    private void moreDiffClean(Arrow<R> arr, Arrow<R> ar, ModOne<R> mbd, R u, R v, R val) {
        FlowGenerator<R> xGen = (FlowGenerator<R>) arr.getBotGenerator();
        FlowGenerator<R> yGen = (FlowGenerator<R>) ar.getTopGenerator();
        int i = 0;
        int foundx = -1;
        int foundy = -1;
        while ((foundx == -1 || foundy == -1) && i < mbd.edgeNumber()-1) {
            FlowEdge<R> ed = mbd.getEdges().get(i);
            if (ed.firstVertex() == xGen || ed.secondVertex() == xGen) foundx = i;
            if (ed.firstVertex() == yGen || ed.secondVertex() == yGen) foundy = i;
            i++;
        }
        if (foundx == foundy) combineEdgessameXY(foundx, mbd, v, xGen);
        else combineEdges(foundx, foundy, mbd, u, v, val, arr, ar);
    }
    
    private void combineEdges(int foundx, int foundy, ModOne<R> mbd, R u, R v, R val, Arrow<R> arr,
            Arrow<R> ar) {
        FlowGenerator<R> xGen = (FlowGenerator<R>) arr.getBotGenerator();
        FlowGenerator<R> yGen = (FlowGenerator<R>) ar.getTopGenerator();
        FlowEdge<R> ex = null;
        FlowEdge<R> ey = null;
        FlowEdge<R> eNew = mbd.getEdges().get(mbd.edgeNumber()-1);
        FlowGenerator<R> ox;
        FlowGenerator<R> oy;
        boolean ty = true;
        boolean tl = eNew.firstUpperSign();
        boolean bl = unitFour.multiply(v.add(val.multiply(ar.getValue()))).add(unitFour.negate()).isZero();
        boolean tr = u.add(val.multiply(arr.getValue()).negate()).add(unitFour.negate()).isZero();
        boolean br = eNew.secondLowerSign();
        boolean cy = (tr != eNew.secondUpperSign());
        boolean chx = false;
        boolean chy = false;
        if (foundx >= 0) {
            ex = mbd.getEdges().get(foundx);
            ox = ex.firstVertex();
            tl = ex.firstUpperSign();
            bl = ex.firstLowerSign();
            if (ox == xGen) {
                ox = ex.secondVertex();
                tl = ex.secondUpperSign();
                bl = ex.secondLowerSign();
            }
            chx = ex.secondVertex() == xGen;
        }
        else ox = xGen;
        if (foundy >= 0) {
            ey = mbd.getEdges().get(foundy);
            oy = ey.firstVertex();
            tr = ey.firstUpperSign();
            br = ey.firstLowerSign();
            ty = ey.secondUpperSign();
            if (oy == yGen) {
                oy = ey.secondVertex();
                tr = ey.secondUpperSign();
                br = ey.secondLowerSign();
                ty = ey.firstUpperSign();
            }
            chy = ey.secondVertex() == yGen;
        }
        else oy = yGen;
        FlowEdge<R> newEd = new FlowEdge<R>(ox, oy, bl, tl, br, tr);
        mbd.getEdges().remove(ex);
        mbd.getEdges().remove(ey);
        mbd.getEdges().remove(eNew);
        mbd.addEdge(newEd);
        boolean value = mbd.extraCircle();
        if (foundy >= 0) {
            if (ty == eNew.secondUpperSign()) value = !value;
            if (chy) value = !value;
        }
        else if (cy) value = !value;
        if (foundx >= 0) {
            if (chx) value = !value;
        }
        mbd.setCircle(value);
    }
    
    private void combineEdgessameXY(int foundx, ModOne<R> mbd, R v, FlowGenerator<R> xGen) {
        FlowEdge<R> eNew = mbd.getEdges().get(mbd.edgeNumber()-1);
        FlowEdge<R> eOld = mbd.getEdges().get(foundx);
        boolean directed = (eOld.firstSign() == eOld.secondSign());
        boolean value = eOld.secondLowerSign();
        if (eNew.firstLowerSign() != !unitFour.multiply(v).isZero()) value = !value;
        if (directed && eOld.firstVertex() != xGen) value = !value;
        if (!value) mbd.changeCircle();
        mbd.getEdges().remove(mbd.edgeNumber()-1);
        mbd.getEdges().remove(foundx);
    }
    
    private void easyClean(boolean found, Arrow<R> arr, Arrow<R> ar, ModOne<R> mbd, R v, R val) {
        FlowGenerator<R> xGen = (FlowGenerator<R>) arr.getBotGenerator();
        if (!found) {
            if (mbd.isEmpty()) {
                ((FlowGenerator<R>) arr.getTopGenerator()).getTopMod().remove(mbd);
                ((FlowGenerator<R>) ar.getBotGenerator()).getBotMod().remove(mbd);
            }
        }
        else {
            boolean foundx = false;
            if (unitTwo.multiply(v).isZero() || unitTwo.multiply(arr.getValue()).isZero())
                foundx = true;
            else if (!unitFour.multiply(val.multiply(ar.getValue())).add(unitFour.add(unitFour)).isZero()) 
                foundx = true;
            int i = 0;
            while (!foundx && i < mbd.getEdges().size()) {
                FlowEdge<R> ed = mbd.getEdges().get(i);
                if (ed.firstVertex() == xGen) {
                    foundx = true;
                    ed.changeSign(true, true);
                }
                if (ed.secondVertex() == xGen) {
                    foundx = true;
                    ed.changeSign(true, false);
                    mbd.changeCircle();
                }
                i++;
            }
        }
    }
    
    private void produceMiddleEdges(Arrow<R> arr, Arrow<R> ar, ModOne<R> mbd, 
            FlowGenerator<R> xGen, FlowGenerator<R> yGen, R val) {
        boolean minusslide = unitFour.multiply(val).add(unitFour).isZero(); 
        boolean fl = !unitFour.multiply(ar.getValue().multiply(val)).add(unitFour).isZero();
        boolean fu = !unitFour.multiply(arr.getValue()).add(unitFour).isZero();
        boolean sl = !unitFour.multiply(ar.getValue()).add(unitFour).isZero();
        boolean su = unitFour.multiply(arr.getValue().multiply(val)).add(unitFour).isZero();
        FlowEdge<R> ed = new FlowEdge<R>(xGen, yGen, fl, fu, sl, su);
        if (sl != minusslide) mbd.changeCircle();
        mbd.addEdge(ed);
    }
    
    protected void moveModsIntoY(FlowGenerator<R> xGen, FlowGenerator<R> yGen) {
        for (ModOne<R> mod : xGen.getBotMod()) {
            FlowGenerator<R> tGen = mod.getTopGenerator();
            boolean found = false;
            int i = 0;
            while (!found && i < tGen.getTopMod().size()) {
                ModOne<R> md = tGen.getTopMod().get(i);
                if (md.getBotGenerator() == yGen) found = true;
                else i++;
            }
            if (found) {
                ModOne<R> md = tGen.getTopMod().get(i);
                tGen.getTopMod().remove(md);
                yGen.getBotMod().remove(md);
            }
            else {
                ModOne<R> md = new ModOne<R>(yGen, tGen);
                md.changeCircle();
                yGen.getBotMod().add(md);
                tGen.getTopMod().add(md);
            }
        }
    }
    
    /*private FlowVertex<R> getVertexFrom(FlowVertex<R> vert) {
        Arrow<R> fArr = vert.firstArrow();
        Arrow<R> sArr = vert.secondArrow();
        FlowGenerator<R> bGen = (FlowGenerator<R>) fArr.getBotGenerator();
        FlowGenerator<R> mGen = vert.getMiddle();
        FlowGenerator<R> tGen = (FlowGenerator<R>) sArr.getTopGenerator();
        fArr = newArrow(bGen, mGen);
        sArr = newArrow(mGen, tGen);
        if (sArr == null) {
            System.out.println(generators.get(1).indexOf(bGen)+" xxx "+
                generators.get(2).indexOf(mGen)+" xxx "
                +generators.get(3).indexOf(tGen));
            output(bGen, tGen);
            throw new UnsupportedOperationException("Not supported yet.");
        }    
        return new FlowVertex<R>(fArr, sArr);
    }// */
    
    private Arrow<R> newArrow(FlowGenerator<R> bGen, FlowGenerator<R> tGen) {
        boolean found = false;
        int i = 0;
        while (!found && i < bGen.getBotArrows().size()) {
            if (bGen.getBotArrows().get(i).getTopGenerator() == tGen) found = true;
            else i++;
        }
        if (!found) {
            System.out.println("Total "+bGen.getBotArrows().size());
            int y = findGen(tGen);
            System.out.println(y+" "+generators.get(y).indexOf(tGen)+" yyy "
                    +generators.get(y-1).indexOf(bGen));
            //output();
            return null;
            //throw new UnsupportedOperationException("Not supported yet.");
        }
        //System.out.println("Made it");
        return bGen.getBotArrows().get(i);
    }
    
    /*private int indexOf(Arrow<R> ff) {
        return ff.getBotGenerator().getBotArrows().indexOf(ff);
    }// */
    
    /*private FlowVertex<R> getVertexFrom(FlowVertex<R> vertex, FlowGenerator<R> xGen, R eps) {
        Arrow<R> fArr = vertex.firstArrow();
        Arrow<R> sArr = vertex.secondArrow();
        R val = sArr.getValue();
        if (!eps.isZero()) val = val.negate();
        Arrow<R> nArr = new Arrow<R>(sArr.getBotGenerator(), xGen, val);
        sArr.getBotGenerator().addBotArrow(nArr);
        xGen.addTopArrow(nArr);
        return new FlowVertex<R>(fArr, nArr);
    }// */
    
    private Arrow<R> findGoodArrow(int i, R power, boolean botPrior) {
        int j = 0;
        while (j < generators.get(i).size()) {
             FlowGenerator<R> gen = generators.get(i).get(j);
            int k = 0;
            while (k < gen.getTopArrows().size()) {
                Arrow<R> arr = gen.getTopArrows().get(k);
                if (arr.getValue().div(power).isInvertible()) {
                    int sizeBot = arr.getBotGenerator().getBotArrows().size();
                    int sizeTop = arr.getTopGenerator().getTopArrows().size();
                    if (sizeBot+sizeTop > 2) {
                        if (botPrior) {
                            if (sizeBot == 1) return arr;
                        }
                        else {
                            if (sizeTop == 1) return arr;
                        }
                    }
                }
                k++;
            }
            j++;
        }
        return null;
    }
    
    private Arrow<R> findArrow(int i, R power, boolean checkIsolated) {
        int j = 0;
        int max = 100000000;
        Arrow<R> fArr = null;
        while (j < generators.get(i).size()) {
            FlowGenerator<R> gen = generators.get(i).get(j);
            int k = 0;
            while (k < gen.getTopArrows().size()) {
                Arrow<R> arr = gen.getTopArrows().get(k);
                if (arr.getValue().div(power).isInvertible()) {
                    int size = arr.getBotGenerator().getBotArrows().size();
                    if (checkIsolated && size == 1 
                            && arr.getTopGenerator().getTopArrows().size() == 1) size = max;
                            // this ensures that it will not be chosen as fArr 
                    if (max > size) {
                        fArr = arr;
                        max = size;
                        if (size == 1) return fArr;
                    }
                }
                k++;
            }
            j++;
        }
        return fArr;
    }
    
    public boolean cubicalFlowCheck() {
        boolean okay = true;
        for (int i = 0; i < generators.size(); i++) {
            for (FlowGenerator<R> bGen : generators.get(i)) {
                ArrayList<FlowGenerator<R>> endPoints = new ArrayList<FlowGenerator<R>>();
                ArrayList<ArrayList<CombineMod<R>>> combos = new ArrayList<ArrayList<CombineMod<R>>>();
                for (Arrow<R> arr : bGen.getBotArrows()) {
                    FlowGenerator<R> mGen = (FlowGenerator<R>) arr.getTopGenerator();
                    for (ModOne<R> mod : mGen.getBotMod()) {
                        FlowGenerator<R> tGen = mod.getTopGenerator();
                        CombineMod<R> combine = new CombineMod<R>(arr, mod, true);
                        int p = endPoints.indexOf(tGen);
                        if (p >= 0) {
                            combos.get(p).add(combine);
                        }
                        else {
                            combos.add(new ArrayList<CombineMod<R>>());
                            combos.get(endPoints.size()).add(combine);
                            endPoints.add(tGen);
                        }
                    }
                }
                for (ModOne<R> mod : bGen.getBotMod()) {
                    FlowGenerator<R> mGen = (FlowGenerator<R>) mod.getTopGenerator();
                    for (Arrow<R> arr : mGen.getBotArrows()) {
                        FlowGenerator<R> tGen = (FlowGenerator<R>) arr.getTopGenerator();
                        CombineMod<R> combine = new CombineMod<R>(arr, mod, false);
                        int p = endPoints.indexOf(tGen);
                        if (p >= 0) {
                            combos.get(p).add(combine);
                        }
                        else {
                            combos.add(new ArrayList<CombineMod<R>>());
                            combos.get(endPoints.size()).add(combine);
                            endPoints.add(tGen);
                        }
                    }
                }
                for (int j = 0; j < combos.size(); j++) {
                    int size = combos.get(j).size();
                    if (problemWith(combos.get(j))) System.out.println(i+" Problem "+size+" from "+generators.get(i).indexOf(bGen)+
                                " to "+generators.get(i+3).indexOf(endPoints.get(j)));
                }
            }
        }
        return okay;
    }

    private boolean problemWith(ArrayList<CombineMod<R>> combo) {
        Complex<R> complex = new Complex<R>();
        for (CombineMod<R> com : combo) {
            Arrow<R> fa = com.arrow;
            ModOne<R> mod = com.mod;
            for (FlowEdge<R> edge : mod.getEdges()) {
                FlowGenerator<R> sga = edge.firstVertex();
                FlowGenerator<R> sgb = edge.secondVertex();
                if (com.arrowFirst()) {
                    Vertex<R> fv = new Vertex<R>(mod.getBotGenerator(), sga);
                    Vertex<R> sv = new Vertex<R>(mod.getBotGenerator(), sgb);
                    R extra = unitTwo;
                    if (fa.getValue().add(unit).isZero()) extra = unitTwo.getZero();
                    if (mod.extraCircle() && mod.getEdges().indexOf(edge) == 0) 
                        extra = extra.add(unitTwo);
                    complex.addEdge(fv, sv, extra, false);
                }
                else {
                    Vertex<R> fv = new Vertex<R>(sga, mod.getTopGenerator());
                    Vertex<R> sv = new Vertex<R>(sgb, mod.getTopGenerator());
                    R extra = unitTwo.getZero();
                    if (mod.extraCircle() && mod.getEdges().indexOf(edge) == 0) 
                        extra = extra.add(unitTwo);
                    complex.addEdge(fv, sv, extra, false);
                }
            }
        }
        boolean problem = (complex.edges.size() != complex.vertices.size());
        ArrayList<ArrayList<Edge<R>>> components = complex.components();
        problem = problem || (complex.edges.size() != 6 && components.size() == 1);
        problem = problem || signTrouble(components);
        return problem;
    }// */
    
    private boolean signTrouble(ArrayList<ArrayList<Edge<R>>> components) {
        boolean trouble = false;
        R sign = unitTwo.getZero();
        for (ArrayList<Edge<R>> comp : components) {
            sign = sign.add(unitTwo);
            for (Edge<R> edge : comp) {
                sign = sign.add(edge.value);
            }
        }
        if (!sign.isZero()) trouble = true;
        
        return trouble;
    }
    
    /*private int getLevelOf(FlowGenerator<R> gen) {
        int i = generators.size()-1;
        boolean found = false;
        while (!found && i >= 0) {
            if (generators.get(i).contains(gen)) found = true;
            else i--;
        }
        return i;
    }// */

    public void whitneyfy() {
        int i = generators.size()-2;
        while (i >= 0) {
            for (FlowGenerator<R> gen : generators.get(i)) {
                ArrayList<Integer> us = sameArrow(gen.getBotArrows());
                int y = us.size()-1;
                while (y >= 0) {
                    Arrow<R> fArr = gen.getBotArrows().get(us.get(y));
                    Arrow<R> sArr = gen.getBotArrows().get(us.get(y)+1);
                    fArr.addValue(sArr.getValue());
                    gen.getBotArrows().remove(sArr);
                    sArr.getTopGenerator().getTopArrows().remove(sArr);
                    if (fArr.getValue().isZero()) {
                        gen.getBotArrows().remove(fArr);
                        fArr.getTopGenerator().getTopArrows().remove(fArr);
                    }
                    y--;
                }
            }
            i--;
        }
    }
    
    /*private void edgeOutput(ModOne<R> mod) {
        FlowGenerator<R> tGen = mod.getTopGenerator();
        FlowGenerator<R> bGen = mod.getBotGenerator();
        int i = findGen(bGen) + 1;
        System.out.println("From "+generators.get(i-1).indexOf(bGen)+" to "+
                generators.get(i+1).indexOf(tGen));
        for (FlowEdge<R> edge : mod.getEdges()) {
            System.out.println(generators.get(i).indexOf(edge.firstVertex().getMiddle())+
                    " to "+generators.get(i).indexOf(edge.secondVertex().getMiddle()));
            int a = bGen.getBotArrows().indexOf(edge.firstVertex().firstArrow());
            int b = bGen.getBotArrows().indexOf(edge.secondVertex().firstArrow());
            int c = tGen.getTopArrows().indexOf(edge.firstVertex().secondArrow());
            int d = tGen.getTopArrows().indexOf(edge.secondVertex().secondArrow());
            FlowGenerator<R> mGen = edge.firstVertex().getMiddle();
            int e = mGen.getBotArrows().indexOf(edge.firstVertex().secondArrow());
            mGen = edge.secondVertex().getMiddle();
            int f = mGen.getBotArrows().indexOf(edge.secondVertex().secondArrow());
            System.out.println(a+" "+c+" -- "+b+" "+d+"      "+e+" "+f);
        }
        System.out.println("End output");
    }// */
    
    private int findGen(FlowGenerator<R> gen) {
        int i = 0;
        boolean found = false;
        while (!found) {
            if (generators.get(i).indexOf(gen) >= 0) found = true;
            else i++;
        }
        return i;
    }
    
    private ArrayList<Integer> sameArrow(ArrayList<Arrow<R>> arrows) {
        ArrayList<Integer> use = new ArrayList<Integer>();
        int i = 0;
        while (i < arrows.size()-1) {
            Arrow<R> fa = arrows.get(i);
            Arrow<R> sa = arrows.get(i+1);
            if (fa.getTopGenerator() == sa.getTopGenerator()) use.add(i);
            i++;
        }
        return use;
    }
    
    /*private void checkModulisOkay() {
        int i = generators.size()-3;
        while (i >= 0) {
            for (FlowGenerator<R> bGen : generators.get(i)) {
                for (ModOne<R> mod : bGen.getBotMod()) {
                    int count = 0;
                    FlowGenerator<R> tGen = mod.getTopGenerator();
                    for (Arrow<R> arr : bGen.getBotArrows()) {
                        if (!unitTwo.multiply(arr.getValue()).isZero()) {
                            FlowGenerator<R> mGen = (FlowGenerator<R>) arr.getTopGenerator();
                            Arrow<R> nArr = arrowTo(mGen, tGen);
                            if (nArr != null && !unitTwo.multiply(nArr.getValue()).isZero()) count++;
                        }
                    }
                    if (count != 2*mod.edgeNumber()) System.out.println("Count "+count+" edges "+mod.edgeNumber());
                    if (mod.getTopGenerator().getTopMod().indexOf(mod) < 0) System.out.println("Error "+i);
                }
            }
            i--;
        }
    }// */
    
    private Arrow<R> arrowTo(FlowGenerator<R> bGen, FlowGenerator<R> tGen) {
        boolean found = false;
        int i = 0;
        while (!found && i < bGen.getBotArrows().size()) {
            FlowGenerator<R> tg = (FlowGenerator<R>) bGen.getBotArrows().get(i).getTopGenerator();
            if (tg == tGen) found = true;
            else i++;
        }
        if (found) return bGen.getBotArrows().get(i);
        return null;
    }
    
    public void sockFlowCheck() {
        for (ArrayList<FlowGenerator<R>> gens : generators) {
            for (FlowGenerator<R> bGen : gens) {
                ArrayList<FlowGenerator<R>> endPoints = new ArrayList<FlowGenerator<R>>();
                ArrayList<ArrayList<CombineMod<R>>> combos = new ArrayList<ArrayList<CombineMod<R>>>();
                for (Arrow<R> arr : bGen.getBotArrows()) {
                    FlowGenerator<R> mGen = (FlowGenerator<R>) arr.getTopGenerator();
                    for (ModOne<R> mod : mGen.getBotMod()) {
                        FlowGenerator<R> tGen = mod.getTopGenerator();
                        CombineMod<R> combine = new CombineMod<R>(arr, mod, true);
                        int p = endPoints.indexOf(tGen);
                        if (p >= 0) {
                            combos.get(p).add(combine);
                        }
                        else {
                            combos.add(new ArrayList<CombineMod<R>>());
                            combos.get(endPoints.size()).add(combine);
                            endPoints.add(tGen);
                        }
                    }
                }
                for (ModOne<R> mod : bGen.getBotMod()) {
                    FlowGenerator<R> mGen = (FlowGenerator<R>) mod.getTopGenerator();
                    for (Arrow<R> arr : mGen.getBotArrows()) {
                        FlowGenerator<R> tGen = (FlowGenerator<R>) arr.getTopGenerator();
                        CombineMod<R> combine = new CombineMod<R>(arr, mod, false);
                        int p = endPoints.indexOf(tGen);
                        if (p >= 0) {
                            combos.get(p).add(combine);
                        }
                        else {
                            combos.add(new ArrayList<CombineMod<R>>());
                            combos.get(endPoints.size()).add(combine);
                            endPoints.add(tGen);
                        }
                    }
                }
                for (int j = 0; j < combos.size(); j++) {
                    int size = combos.get(j).size();
                    int i = generators.indexOf(gens);
                    if (sockProblem(combos.get(j))) {
                        System.out.println(i+" Problem "+size+" from "+generators.get(i).indexOf(bGen)+
                                " to "+generators.get(i+3).indexOf(endPoints.get(j)));
                        output(i, generators.get(i).indexOf(bGen), generators.get(i+3).indexOf(endPoints.get(j)));
                    }
                }
            }
            
        }
    }
    
    private boolean sockProblem(ArrayList<CombineMod<R>> combo) {
        int anyCircles = 0;
        Complex<R> complex = new Complex<R>();
        for (CombineMod<R> com : combo) {
            Arrow<R> fa = com.arrow;
            ModOne<R> mod = com.mod;
            if (mod.edgeNumber() == 0) {
                anyCircles++;
            }
            else {
                for (FlowEdge<R> edge : mod.getEdges()) {
                    FlowGenerator<R> sga = edge.firstVertex();
                    FlowGenerator<R> sgb = edge.secondVertex();
                    if (com.arrowFirst()) {
                        Vertex<R> fv = new Vertex<R>(mod.getBotGenerator(), sga);
                        Vertex<R> sv = new Vertex<R>(mod.getBotGenerator(), sgb);
                        R extra = unitTwo;
                        if (fa.getValue().add(unit).isZero()) extra = unitTwo.getZero();
                        if (!mod.extraCircle() && mod.getEdges().indexOf(edge) == 0) 
                            extra = extra.add(unitTwo);
                        complex.addEdge(fv, sv, extra, false);
                    }
                    else {
                        Vertex<R> fv = new Vertex<R>(sga, mod.getTopGenerator());
                        Vertex<R> sv = new Vertex<R>(sgb, mod.getTopGenerator());
                        R extra = unitTwo.getZero();
                        if (!mod.extraCircle() && mod.getEdges().indexOf(edge) == 0) 
                            extra = extra.add(unitTwo);
                        complex.addEdge(fv, sv, extra, false);
                    }
                }
            }
        }
        ArrayList<ArrayList<Edge<R>>> components = complex.components();
        if (components == null) {
            System.out.println("Busted");
            return true;
        }
        if (containsTwo(complex)) return false;
        if ((anyCircles % 2 != 1) == signTrouble(components)) complex.output(1);
        return (anyCircles % 2 != 1) == signTrouble(components) ;
    }
    
    private boolean containsTwo(Complex<R> complex) {
        boolean found = false;
        int i = complex.vertices.size()-1;
        while (!found && i >= 0) {
            FlowGenerator<R> bGen = complex.vertices.get(i).fGen;
            int j = bGen.getBotArrows().size()-1;
            while (!found && j >= 0) {
                Arrow<R> arr = bGen.getBotArrows().get(j);
                if (arr.getValue().add(unit).add(unit).isZero() ||
                        arr.getValue().negate().add(unit).add(unit).isZero()) found = true;
                else j--;
            }
            i--;
        }
        return found;
    }

    ArrayList<String> getFinalInfo() {
        ArrayList<String> info = new ArrayList<String>();
        String vertStr = "";
        for (int i = 0; i < generators.size(); i++) vertStr = vertStr+generators.get(i).size()+";";
        info.add(vertStr);
        String tEdges = "t";
        for (int i = 0; i < generators.size(); i++) {
            for (int j = 0; j < generators.get(i).size(); j++) {
                FlowGenerator<R> bGen = generators.get(i).get(j);
                if (!bGen.getBotArrows().isEmpty()) {
                    FlowGenerator<R> tGen = (FlowGenerator<R>) bGen.getBotArrows().get(0).getTopGenerator();
                    int val = intOfValue(bGen.getBotArrows().get(0).getValue());
                    tEdges = tEdges+i+"."+j+"."+generators.get(i+1).indexOf(tGen)+"."+val+";";
                }
            }
        }
        info.add(tEdges);
        String eEdges = "e";
        for (int i = 0; i < generators.size(); i++) {
            for (int j = 0; j < generators.get(i).size(); j++) {
                FlowGenerator<R> bGen = generators.get(i).get(j);
                for (ModOne<R> mod : bGen.getBotMod()) {
                    FlowGenerator<R> tGen = mod.getTopGenerator();
                    int k = generators.get(i+2).indexOf(tGen);
                    eEdges = eEdges+i+"."+j+"."+k+";";
                }
            }
        }
        info.add(eEdges);
        return info;
    }
    
    // The classes below are only used for flow category checks
    
    private class Complex<R extends Ring<R>> {
        
        private final ArrayList<Vertex<R>> vertices;
        private final ArrayList<Edge<R>> edges;
        private int circles;
        
        public Complex() {
            vertices = new ArrayList<Vertex<R>>();
            edges = new ArrayList<Edge<R>>();
        }
        
        private void output(int i) {
            System.out.println("Circles : "+circles);
            for (Edge<R> edge : edges) {
                String arrow = " --- ";
                if (edge.directed) arrow = " --> ";
                System.out.println(vertices.indexOf(edge.fVert)+arrow+edge.value
                        +arrow+vertices.indexOf(edge.sVert));
            }
            for (Edge<R> edge : edges) {
                System.out.println(generators.get(i+1).indexOf(edge.fVert.fGen)+" "
                        +generators.get(i+2).indexOf(edge.fVert.sGen)+" --- "+
                        generators.get(i+1).indexOf(edge.sVert.fGen)+" "
                        +generators.get(i+2).indexOf(edge.sVert.sGen));
            }// */
        }
        
        private void addEdge(Vertex<R> fv, Vertex<R> sv, R val, boolean dir) {
            Vertex<R> fVert = vertexFrom(fv);
            Vertex<R> sVert = vertexFrom(sv);
            Edge<R> edge = new Edge<R>(fVert, sVert, val, dir);
            fVert.addEdge(edge);
            sVert.addEdge(edge);
            edges.add(edge);
        }
        
        private Vertex<R> vertexFrom(Vertex<R> fv) {
            boolean found = false;
            int i = 0;
            while (!found && i < vertices.size()) {
                if (fv.agreesWith(vertices.get(i))) found = true;
                else i++;
            }
            if (!found) vertices.add(fv);
            return vertices.get(i);
        }
        
        private ArrayList<ArrayList<Edge<R>>> components() {
            ArrayList<ArrayList<Edge<R>>> components = new ArrayList<ArrayList<Edge<R>>>();
            try {
                for (Edge<R> edge : edges) {
                    ArrayList<Edge<R>> comp = getComponent(edge, components);
                    if (comp != null) components.add(comp);
                }
            }
            catch (Exception e) {
                return null;
            }
            return components;
        }
        
        private ArrayList<Edge<R>> getComponent(Edge<R> edge, 
                ArrayList<ArrayList<Edge<R>>> components) {
            boolean found = false;
            int i = 0;
            while (!found && i < components.size()) {
                if (components.get(i).contains(edge)) found = true;
                else i++;
            }
            if (found) return null;
            ArrayList<Edge<R>> component = new ArrayList<Edge<R>>();
            component.add(edge);
            Edge<R> runE = edge;
            while (!found) {
                found = true;
                int cont = 0;
                Edge<R> fe = runE.otherEdge(0);
                Edge<R> se = runE.otherEdge(1);
                if (!component.contains(fe)) {
                    component.add(fe);
                    found = false;
                    cont = 1;
                }
                if (!component.contains(se)) {
                    component.add(se);
                    found = false;
                    cont = 2;
                }
                if (cont == 1) runE = fe;
                if (cont == 2) runE = se;
            }
            return component;
        }
        
    }

    private class Vertex<R extends Ring<R>> {

        private final FlowGenerator<R> fGen;
        private final FlowGenerator<R> sGen;
        private final ArrayList<Edge<R>> edges;
        
        
        public Vertex(FlowGenerator<R> fa, FlowGenerator<R> sa) {
            fGen = fa;
            sGen = sa;
            edges = new ArrayList<Edge<R>>(2);
        }
        
        public boolean agreesWith(Vertex<R> vert) {
            return (fGen == vert.fGen && sGen == vert.sGen);
        }
        
        public void addEdge(Edge<R> edge) {
            edges.add(edge);
        }
        
        public Vertex<R> otherVertex(int i) {
            Edge<R> edge = edges.get(i);
            if (edge.fVert != this) return edge.fVert;
            else return edge.sVert;
        }
    }
    
    private class Edge<R extends Ring<R>> {
        
        private final Vertex<R> fVert;
        private final Vertex<R> sVert;
        private final R value;
        private final boolean directed;
        
        public Edge(Vertex<R> fv, Vertex<R> sv, R val, boolean dir) {
            fVert = fv;
            sVert = sv;
            value = val;
            directed = dir;
        }
        
        public Edge<R> otherEdge(int i) {
            if (i == 0) {
                if (fVert.edges.get(0) == this) return fVert.edges.get(1);
                else return fVert.edges.get(0);
            }
            if (sVert.edges.get(0) == this) return sVert.edges.get(1);
                else return sVert.edges.get(0);
        }
    }
    
    private class CombineMod<R extends Ring<R>> {

        private final Arrow<R> arrow;
        private final ModOne<R> mod;
        private final boolean arrowFirst;
        private final FlowGenerator<R> bGen;
        private final FlowGenerator<R> tGen;
        
        public CombineMod(Arrow<R> arr, ModOne<R> md, boolean af) {
            arrow = arr;
            mod = md;
            arrowFirst = af;
            if (af) {
                bGen = (FlowGenerator<R>) arr.getBotGenerator();
                tGen = mod.getTopGenerator();
            }
            else {
                bGen = mod.getBotGenerator();
                tGen = (FlowGenerator<R>) arr.getTopGenerator();
            }
        }
        
        public boolean arrowFirst() {
            return arrowFirst;
        }
    }
    
}
