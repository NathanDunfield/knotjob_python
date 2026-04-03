/*

Copyright (C) 2020-21 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.oddkhov;

import java.math.BigInteger;
import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.ChainComplex;
import knotjob.homology.Homology;
import knotjob.homology.HomologyInfo;
import knotjob.homology.QuantumCohomology;
import knotjob.homology.evenkhov.EvenKhovCalculator;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.rings.Ring;

/**
 *
 * @author dirk
 * @param <R>
 */
public class OddKhovHomology<R extends Ring<R>> {
    
    private final Link theLink;
    private final long coeff;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final Options options;
    private final int[] girth;
    private final R unit;
    private final R thePrime;
    private final boolean highDetail;
    private final ArrayList<String> endHom;
    
    public OddKhovHomology(LinkData link, long cff, DialogWrap frm, Options optns, R unt, R prime) {
        theLink = Reidemeister.freeOfOne(link.chosenLink()).breakUp().girthDiscMinimize();
        girth = theLink.totalGirthArray();
        coeff = cff;
        frame = frm;
        abInf = frame.getAbortInfo();
        options = optns;
        unit = unt;
        thePrime = prime;
        endHom = new ArrayList<String>();
        highDetail = optns.getGirthInfo() == 2;
    }
    
    public ArrayList<String> getOddHomology() {
        return endHom;
    }
    
    public void calculate() {
        int[] wrt = theLink.crossingSigns();
        int hstart = -wrt[1];
        int qstart = wrt[0]+2*hstart;
        if (coeff == 0) calculateIntegral(hstart,qstart);
        if (coeff == 1) calculateRational(hstart,qstart);
        if (coeff >  1) calculateModular(hstart,qstart);
        if (coeff <  0) calculateLocalized(hstart,qstart);
    }

    private void calculateIntegral(int hstart, int qstart) {
        OddComplex<R> theComplex = getComplex(hstart, qstart);
        ArrayList<String> finalInfo = smithNormalize(theComplex, new int[0]);
        if (finalInfo != null) finishOff(finalInfo);
    }

    private void calculateRational(int hstart, int qstart) {
        OddComplex<R> theComplex = getComplex(hstart, qstart);
        ArrayList<String> finalInfo = finishUp(theComplex);
        if (finalInfo != null) finishOff(finalInfo);
    }

    private void calculateModular(int hstart, int qstart) {
        OddComplex<R> theComplex = getComplex(hstart, qstart);
        ArrayList<String> finalInfo = modNormalize(theComplex);
        if (finalInfo != null) finishOff(finalInfo);
    }

    private void calculateLocalized(int hstart, int qstart) {
        ArrayList<Integer> prms = EvenKhovCalculator.getPrimes(coeff, options.getPrimes());
        int[] primes = new int[prms.size()];
        for (int i = 0; i < prms.size(); i++) primes[i] = prms.get(i);
        OddComplex<R> theComplex = getComplex(hstart, qstart);
        ArrayList<String> finalInfo = smithNormalize(theComplex, primes);
        if (finalInfo != null) finishOff(finalInfo);
    }
    
    private OddComplex<R> getComplex(int hstart, int qstart) {
        if (theLink.crossingLength() == 0) return new OddComplex<R>(0, unit, abInf, null, false);
        OddComplex<R> theComplex;
        theComplex = firstComplex(hstart, qstart);
        theComplex.setClosure(theLink);
        int u = 1;
        while (u < theLink.crossingLength() && !abInf.isAborted()) {
            boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
                theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
            OddComplex<R> nextComplex = new OddComplex<R>(theLink.getCross(u), theLink.getPath(u), 0,
                    0, orient, unit, null, null, false);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            theComplex.modifyComplex(nextComplex,girthInfo(u), highDetail);
            u++;
        }
        return theComplex;
    }

    private ArrayList<String> finishUp(OddComplex<R> theComplex) { // this is only okay for fields.
        if (abInf.isAborted()) return null;
        ArrayList<Integer> relevantQs = theComplex.getQs();
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        frame.setLabelLeft("Quantum degree : ", 0, false);
        frame.setLabelLeft("Homological degree : ", 1, false);
        for (int q : relevantQs) {
            frame.setLabelRight("" + q, 0, false);
            ChainComplex<R> qComplex = theComplex.getQComplex(q);
            //if (!qComplex.boundaryCheck()) System.out.println("Boundary Error"); //for debugging
            QuantumCohomology qCoh = new QuantumCohomology(q+1, qComplex.obtainBettis());
            if (abInf.isCancelled()) return null;
            cohoms.add(qCoh);
        }
        return reduceInformation(cohoms);
    }
    
    private ArrayList<String> smithNormalize(OddComplex<R> theComplex, int[] primes) {
        if (abInf.isAborted()) return null;
        if (highDetail) {
            frame.setLabelLeft(" ", 3, false);
            frame.setLabelRight(" ", 3, false);
        }
        ArrayList<Integer> relevantQs = theComplex.getQs();
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        frame.setLabelLeft("Quantum degree : ", 0, false);
        frame.setLabelLeft("Homological degree : ", 1, false);
        for (int q : relevantQs) {
            frame.setLabelRight("" + q, 0, false);
            ChainComplex<R> qComplex = theComplex.getQComplex(q);
            //if (!qComplex.boundaryCheck()) System.out.println("Boundary Error "+q); // for debugging.
            if (abInf.isCancelled()) return null;
            QuantumCohomology qCoh = new QuantumCohomology(q+1, qComplex.smithNormalize(primes));
            cohoms.add(qCoh);
        }
        return reduceInformation(cohoms);
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

    private OddComplex<R> firstComplex(int hstart, int qstart) {
        OddComplex<R> theComplex = new OddComplex<R>(theLink.getCross(0), theLink.getPath(0), hstart,
                qstart, false, unit, frame, abInf, false);
        if (theComplex.posNumber() == 2) return theComplex;
        OddComplex<R> unComp = new OddComplex<R>(1, unit, abInf, frame, false);
        unComp.modifyComplex(theComplex," ",highDetail);
        return unComp;
    }

    private ArrayList<String> reduceInformation(ArrayList<QuantumCohomology> cohoms) {
        if (theLink.unComponents() > 0) {
            HomologyInfo homInfo = new HomologyInfo(0l, 1, 1, cohoms);
            int u = theLink.unComponents();
            while (u > 0) {
                homInfo = homInfo.doubleHom();
                u--;
            }
            cohoms = homInfo.getHomologies();
        }
        ArrayList<String> theStrings = new ArrayList<String>();
        QuantumCohomology currCoh = cohoms.get(0);
        theStrings.add(currCoh.toString());
        for (int i = 1; i < cohoms.size(); i++) {
            QuantumCohomology prevCoh = currCoh;
            currCoh = cohoms.get(i);
            if (currCoh.qdeg() == prevCoh.qdeg()+2) currCoh = splitOff(currCoh, prevCoh);
            theStrings.add(currCoh.toString());
        }
        return theStrings;
    }

    private QuantumCohomology splitOff(QuantumCohomology currCoh, QuantumCohomology prevCoh) {
        ArrayList<Homology> newHoms = new ArrayList<Homology>();
        for (Homology hom : currCoh.getHomGroups()) {
            Homology pHom = prevCoh.findHomology(hom.hdeg(), false);
            ArrayList<BigInteger> nTorsion = new ArrayList<BigInteger>();
            for (BigInteger tor : hom.getTorsion()) nTorsion.add(tor);
            int nb = hom.getBetti();
            if (pHom != null) {
                for (BigInteger tor : pHom.getTorsion()) {
                    int ind = getIndex(tor, nTorsion);
                    if (ind < 0) System.out.println("Problem");
                    nTorsion.remove(ind);
                }
                nb = nb - pHom.getBetti();
                if (nb < 0) System.out.println("Another Problem");
            }
            if (nb > 0 || !nTorsion.isEmpty()) newHoms.add(new Homology(hom.hdeg(), nb, nTorsion));
        }
        return new QuantumCohomology(currCoh.qdeg(), newHoms);
    }

    private int getIndex(BigInteger tor, ArrayList<BigInteger> nTorsion) {
        boolean found = false;
        int i = 0;
        while (!found && i < nTorsion.size()) {
            if (nTorsion.get(i).equals(tor)) found = true;
            else i++;
        }
        if (!found) return -1;
        return i;
    }

    private void finishOff(ArrayList<String> finalInfo) {
        for (String info : finalInfo) {
            if (!info.contains("x")) endHom.add(info);
        }
        String last = "o"+coeff+".";
        boolean okay = true;
        for (String info : endHom) {
            if (okay && info.contains("aborted")) {
                okay = false;
                last = last+"a"+quantum(info)+".";
            }
        }
        endHom.add(0,last);
    }
    
    private int quantum(String info) {
        int e = info.indexOf('h');
        if (e == -1) e = info.indexOf('a');
        return Integer.parseInt(info.substring(1, e));
    }
    
    private ArrayList<String> modNormalize(OddComplex<R> theComplex) {
        if (abInf.isAborted()) return null;
        if (highDetail) {
            frame.setLabelLeft(" ", 3, false);
            frame.setLabelRight(" ", 3, false);
        }
        ArrayList<Integer> relevantQs = theComplex.getQs();
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        frame.setLabelLeft("Quantum degree : ", 0, false);
        frame.setLabelLeft("Homological degree : ", 1, false);
        for (int q : relevantQs) {
            frame.setLabelRight("" + q, 0, false);
            ChainComplex<R> qComplex = theComplex.getQComplex(q);
            //if (!qComplex.boundaryCheck()) System.out.println("Boundary Error "+q);
            if (abInf.isCancelled()) return null;
            QuantumCohomology qCoh = new QuantumCohomology(q+1, qComplex.modNormalize(thePrime));
            cohoms.add(qCoh);
        }
        return reduceInformation(cohoms);
    }
    
}
