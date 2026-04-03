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

package knotjob.homology.oddkhov.unified.sinv;

import java.math.BigInteger;
import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.homology.QGenerator;
import knotjob.homology.evenkhov.EvenArrow;
import knotjob.homology.evenkhov.EvenGenerator;
import knotjob.homology.evenkhov.sinv.GradedComplex;
import knotjob.homology.oddkhov.unified.UnifiedChainComplex;
import knotjob.homology.oddkhov.unified.UnifiedGenerator;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.rings.BigInt;
import knotjob.rings.BigIntXi;
import knotjob.rings.ModN;

/**
 *
 * @author Dirk
 */
public class CompleteSInvariant {
    
    private Link theLink;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final BigIntXi unit;
    private final BigIntXi xi;
    private final int[] girth;
    private final boolean highDetail;
    private final boolean reduce;
    private final ArrayList<Integer> theInv;
    private Integer sinv;
    private Integer btsinv;
    private Integer btsinm;
    private String grInv;
    
    public CompleteSInvariant(LinkData linkData, DialogWrap frm, Options optns, boolean red) {
        theLink = Reidemeister.freeOfOne(linkData.chosenLink()).breakUp().girthDiscMinimize();
        girth = theLink.totalGirthArray();
        frame = frm;
        abInf = frame.getAbortInfo();
        highDetail = optns.getGirthInfo() == 2;
        sinv = linkData.sInvariant(2);
        unit = new BigIntXi(BigInteger.ONE);
        xi = new BigIntXi(BigInteger.ZERO, BigInteger.ONE);
        theInv = new ArrayList<Integer>();
        reduce = red;
        grInv = linkData.grsinv;
    }
    
    public void calculate() {
        setSInvariants();
        int[] wrt = theLink.crossingSigns();
        int hstart = -wrt[1];
        int qstart = wrt[0]+2*hstart;
        calculateComplete(hstart, qstart, false);
        if (abInf.isAborted()) return;
        theLink = theLink.mirror();
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, true);
        if (highDetail) frame.setLabelLeft("h-Level : ", 3, false);
        wrt = theLink.crossingSigns();
        hstart = -wrt[1];
        qstart = wrt[0]+2*hstart;
        calculateComplete(hstart, qstart, true);
    }

    public String getCInvariant() {
        if (theInv.size() != 2) return null;
        if (!reduce) {
            String res = "trivial";
            if (theInv.get(0)+theInv.get(1) != 0) res = "non-"+res;
            return res;
        }
        return "("+theInv.get(0)+", "+(-theInv.get(1))+")";
    }

    public String getGradedInvariant() {
        return grInv;
    }
    
    private void setSInvariants() {
        if (grInv == null) {
            sinv = null;
            btsinv = null;
            btsinm = null;
            return;
        }
        int k = grInv.indexOf(".");
        int[] fInf = gradedInfo(grInv.substring(0, k));
        int[] sInf = gradedInfo(grInv.substring(k+2));
        sinv = fInf[0];
        btsinv = fInf[1];
        btsinm = sInf[1];
    }
    
    private int[] gradedInfo(String str) {
        int[] grd = new int[2];
        int k = str.indexOf("(");
        if (k < 0) {
            grd[0] = Integer.parseInt(str);
            grd[1] = grd[0];
        }
        else {
            grd[0] = Integer.parseInt(str.substring(0, k-1));
            int p = kommas(str);
            grd[1] = grd[0] - 2*p -2;
        }
        return grd;
    }
    
    private int kommas(String str) {
        int k = 0;
        for (int i = 1; i < str.length(); i++) {
            if (str.charAt(i) == ',') k++;
        }
        return k;
    }
    
    private void calculateComplete(int hstart, int qstart, boolean mirror) {
        SUniComplex<BigIntXi> theComplex = getComplex(hstart, qstart);
        lipSarkize(theComplex, mirror);
    }
    
    private SUniComplex<BigIntXi> getComplex(int hstart, int qstart) {
        if (theLink.crossingLength() == 0) {
            int a = theLink.unComponents();
            if (reduce) a--;
            return new SUniComplex<BigIntXi>(a, unit, abInf, null);
        }
        int tsum = totalSum(theLink.getCrossings());
        int ign = 2;
        SUniComplex<BigIntXi> theComplex = 
                new SUniComplex<BigIntXi>(theLink.getCross(0), theLink.getPath(0), hstart, qstart,
                        false, unit, xi, frame, abInf);
        theComplex.setClosure(theLink);
        int u = 1;
        while (u < theLink.crossingLength()) {
            boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
                theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
            SUniComplex<BigIntXi> nextComplex = new SUniComplex<BigIntXi>(theLink.getCross(u),
                    theLink.getPath(u), 0, 0, orient, unit, xi, null, null);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            if (reduce && u == theLink.crossingLength() - 1) theComplex.setReduced(theLink.getPath(u, theLink.basepoint()));
            theComplex.modifyComplex(nextComplex, girthInfo(u), highDetail);
            theComplex.throwAway(tsum-ign+2,ign);
            if (theLink.getCross(u) < 0) tsum = tsum - theLink.getCross(u);
            else tsum = tsum + theLink.getCross(u);
            u++;
        }
        return theComplex;
    }
    
    private int totalSum(int[] crossings) {
        int tsum = 0;
        for (int r = 1; r < crossings.length; r++) {
            if (crossings[r] < 0) tsum = tsum + crossings[r];
            else tsum = tsum - crossings[r];
        }
        tsum--;
        return tsum;
    }
    
    private String girthInfo(int u) {
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

    private void lipSarkize(SUniComplex<BigIntXi> theComplex, boolean mirror) {
        CompleteChanger<BigInt, ModN> changer = getChanger(theComplex, mirror);
        if (abInf.isAborted()) return;
        dealWithGrading(changer, mirror);
        if (abInf.isAborted()) return;
        int bts = btsinv;
        if (mirror) bts = btsinm;
        int red = 0;
        if (!reduce) red++;
        Integer comp = changer.completeInvariant(bts+red);
        if (comp != null) theInv.add(comp-red);
    }
    
    private void dealWithGrading(CompleteChanger<BigInt, ModN> changer, boolean mirror) {
        if (mirror) {
            if (btsinm == null) {
                String grI = changer.gradedInvariant();
                if (abInf.isAborted()) return;
                grInv = grInv+". "+grI;
                int[] gr = gradedInfo(grI);
                btsinm = gr[1];
            }
        }
        else {
            if (sinv == null) {
                grInv = changer.gradedInvariant();
                if (abInf.isAborted()) return;
                int[] gr = gradedInfo(grInv);
                sinv = gr[0];
                btsinv = gr[1];
            }
        }
    }

    private CompleteChanger<BigInt, ModN> getChanger(SUniComplex<BigIntXi> theComplex, boolean mir) {
        int stage = 0;
        if (mir) stage = 4;
        ArrayList<ArrayList<Generator<BigInt>>> grGens = new ArrayList<ArrayList<Generator<BigInt>>>();
        ArrayList<ArrayList<Generator<BigInt>>> bnGens = new ArrayList<ArrayList<Generator<BigInt>>>();
        ArrayList<ArrayList<Generator<BigInt>>> odGens = new ArrayList<ArrayList<Generator<BigInt>>>();
        ArrayList<ArrayList<Generator<BigInt>>> btGens = new ArrayList<ArrayList<Generator<BigInt>>>();
        BigInt unt = unit.reduction(true);
        for (int k = 0; k < theComplex.generatorSize(); k++) {
            grGens.add(new ArrayList<Generator<BigInt>>());
            bnGens.add(new ArrayList<Generator<BigInt>>());
            odGens.add(new ArrayList<Generator<BigInt>>());
            btGens.add(new ArrayList<Generator<BigInt>>());
            ArrayList<Generator<BigIntXi>> gens = theComplex.getGenerators(k);
            int i = 0;
            while (i < gens.size()) {
                QGenerator<BigIntXi> gen = (QGenerator<BigIntXi>) gens.get(i);
                EvenGenerator<BigInt> grGen = new EvenGenerator<BigInt>(0, gen.hdeg(), gen.qdeg());
                UnifiedGenerator<BigInt> bnGen = new UnifiedGenerator<BigInt>(gen.hdeg(), gen.qdeg());
                UnifiedGenerator<BigInt> odGen = new UnifiedGenerator<BigInt>(gen.hdeg(), gen.qdeg());
                UnifiedGenerator<BigInt> btGen = new UnifiedGenerator<BigInt>(gen.hdeg(), gen.qdeg());
                grGens.get(k).add(grGen);
                bnGens.get(k).add(bnGen);
                odGens.get(k).add(odGen);
                btGens.get(k).add(btGen);
                for (int j = 0; j < gen.getTopArrows().size(); j++) {
                    Arrow<BigIntXi> arrow = gen.getTopArrow(j);
                    QGenerator<BigIntXi> bGen = (QGenerator<BigIntXi>) arrow.getBotGenerator();
                    int p = theComplex.getGenerators(k-1).indexOf(bGen);
                    BigInt valEv = arrow.getValue().reduction(true);
                    createEvenArrow((EvenGenerator<BigInt>) grGens.get(k-1).get(p), grGen, valEv);
                    createArrow(bnGens.get(k-1).get(p), bnGen, valEv);
                    createArrow(btGens.get(k-1).get(p), btGen, valEv);
                    if (bGen.qdeg() == gen.qdeg()) {
                        BigInt valOd = arrow.getValue().reduction(false);
                        createArrow(odGens.get(k-1).get(p), odGen, valOd);
                    }
                }
                createInOutArrow(odGen, btGen, unt);
                i++;
            }
        }
        UnifiedChainComplex<BigInt> bnComplex = new UnifiedChainComplex<BigInt>(bnGens, unt, 
                reduce, highDetail, frame, abInf);
        UnifiedChainComplex<BigInt> odComplex = new UnifiedChainComplex<BigInt>(odGens, unt,
                reduce, highDetail, frame, abInf);
        UnifiedChainComplex<BigInt> btComplex = new UnifiedChainComplex<BigInt>(btGens, unt,
                reduce, highDetail, frame, abInf);
        GradedComplex<BigInt> grComplex = new GradedComplex<BigInt>(grGens, unt, frame, abInf);
        return new CompleteChanger<BigInt, ModN>(stage, grComplex, bnComplex, odComplex, btComplex, 
                unt, new ModN(1, 2), reduce, highDetail, frame, abInf);
    }
    
    private void createInOutArrow(UnifiedGenerator<BigInt> oGen, UnifiedGenerator<BigInt> iGen, BigInt val) {
        Arrow<BigInt> arrow = new Arrow<BigInt>(oGen, iGen, val);
        oGen.addOutArrow(arrow);
        iGen.addInArrow(arrow);
    }
    
    private void createEvenArrow(EvenGenerator<BigInt> bGen, EvenGenerator<BigInt> tGen, BigInt val) {
        if (val.isZero()) return;
        EvenArrow<BigInt> arrow = new EvenArrow<BigInt>(bGen, tGen);
        arrow.setValue(val);
        bGen.addBotArrow(arrow);
        tGen.addTopArrow(arrow);
    }
    
    private void createArrow(Generator<BigInt> bGen, Generator<BigInt> tGen, BigInt val) {
        if (val.isZero()) return;
        Arrow<BigInt> arrow = new Arrow<BigInt>(bGen, tGen, val);
        bGen.addBotArrow(arrow);
        tGen.addTopArrow(arrow);
    }
    
}
