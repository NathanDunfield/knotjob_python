/*

Copyright (C) 2024 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology;

import java.util.ArrayList;
import knotjob.Calculation;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.Link;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public abstract class HomologyCalculation<R extends Ring<R>> extends Calculation<R> {
    
    private final R thePrime;
    protected final ArrayList<String> endunredHom;
    protected final ArrayList<String> endredHom;
    protected final boolean unred;
    protected final boolean red;
    protected final long coeff;
    public int totunr = 0;
    public int totred = 0;
    public int towunr = 0;
    public int towred = 0;
    protected int theTwo = 2;
    
    public HomologyCalculation(Link lnk, long cff, DialogWrap frm, R unt, Options opts,
            R prime, boolean ur, boolean r) {
        super(lnk, frm, unt, opts);
        coeff = cff;
        thePrime = prime;
        unred = ur;
        red = r;
        endunredHom = new ArrayList<String>();
        endredHom = new ArrayList<String>();
    }
    
    public ArrayList<String> getReduced() {
        return endredHom;
    }
    
    public ArrayList<String> getUnreduced() {
        return endunredHom;
    }
    
    protected ArrayList<String> finishUp(ChainComplex<R> theComplex, boolean rd) { // this is only okay for fields.
        if (abInf.isAborted()) return null;
        ArrayList<Integer> relevantQs = theComplex.getQs();
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        frame.setLabelLeft("Quantum degree : ", 0, false);
        frame.setLabelLeft("Homological degree : ", 1, false);
        for (int q : relevantQs) {
            frame.setLabelRight("" + q, 0, false);
            ChainComplex<R> qComplex = theComplex.getQComplex(q);
            if (!qComplex.boundaryCheck()) System.out.println("Boundary Error");
            QuantumCohomology qCoh = new QuantumCohomology(q, qComplex.obtainBettis());
            if (abInf.isCancelled()) return null;
            cohoms.add(qCoh);
        }
        return reduceInformation(cohoms, rd);
    }
    
    protected ArrayList<String> modNormalize(ChainComplex<R> theComplex, boolean rd) {
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
            if (!qComplex.boundaryCheck()) System.out.println("Boundary Error "+q);
            if (abInf.isCancelled()) return null;
            QuantumCohomology qCoh = new QuantumCohomology(q, qComplex.modNormalize(thePrime));
            cohoms.add(qCoh);
        }
        return reduceInformation(cohoms, rd);
    }
    
    protected ArrayList<String> smithNormalize(ChainComplex<R> theComplex, int[] primes, boolean rd) {
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
            if (!qComplex.boundaryCheck()) System.out.println("Boundary Error "+q);
            if (abInf.isCancelled()) return null;
            QuantumCohomology qCoh = new QuantumCohomology(q, qComplex.smithNormalize(primes));
            cohoms.add(qCoh);
        }
        return reduceInformation(cohoms, rd);
    }

    private ArrayList<String> reduceInformation(ArrayList<QuantumCohomology> cohoms, boolean rd) {
        if (rd && theLink.unComponents() > 0) {
            HomologyInfo unredInfo = new HomologyInfo(0l, 1, 1, cohoms, theLink.components() %2);
            HomologyInfo redInfo = new HomologyInfo(0l, 1, 1, cohoms, theLink.components() % 2 +1);
            int u = theLink.unComponents();
            if (theLink.components() == u) u--;
            while (u > 0) {
                unredInfo = unredInfo.doubleHom();
                redInfo = redInfo.doubleHom();
                u--;
            }
            cohoms = unredInfo.getHomologies();
            for (QuantumCohomology coh : redInfo.getHomologies()) cohoms.add(coh);
        }// */
        ArrayList<String> theStrings = new ArrayList<String>();
        for (QuantumCohomology currCoh : cohoms) {
            theStrings.add(currCoh.toString());
            if ((currCoh.qdeg() + theLink.components())%2 == 0) {
                totunr = totunr+currCoh.totalBetti(0);
                towunr = towunr+currCoh.totalBetti(theTwo);
            }
            else {
                totred = totred+currCoh.totalBetti(0);
                towred = towred+currCoh.totalBetti(theTwo);
            }
        }
        return theStrings;
    }
    
    
    
}
