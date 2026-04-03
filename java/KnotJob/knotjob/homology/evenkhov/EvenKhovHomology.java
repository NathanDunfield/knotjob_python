/*

Copyright (C) 2020-24 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.evenkhov;

import java.util.ArrayList;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.HomologyCalculation;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class EvenKhovHomology <R extends Ring<R>> extends HomologyCalculation<R> {
    
    public EvenKhovHomology(LinkData link, long cff, DialogWrap frm, boolean unrd, boolean rd,
            Options optns, R unt, R prime) {
        super(Reidemeister.freeOfOne(link.chosenLink()), cff, frm, unt, optns, prime, unrd, rd);
    }

    @Override
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
        EvenComplex<R> theComplex = getComplex(hstart, qstart);
        ArrayList<String> finalInfo = smithNormalize(theComplex, new int[0], true);
        if (finalInfo != null) finishOff(finalInfo);
    }

    private void calculateRational(int hstart, int qstart) {
        EvenComplex<R> theComplex = getComplex(hstart, qstart);
        ArrayList<String> finalInfo = finishUp(theComplex, true);
        if (finalInfo != null) finishOff(finalInfo);
    }

    private void calculateModular(int hstart, int qstart) {
        theTwo = getThePrime();
        EvenComplex<R> theComplex = getComplex(hstart, qstart);
        ArrayList<String> finalInfo = modNormalize(theComplex, true);
        if (finalInfo != null) finishOff(finalInfo);
    }

    private void calculateLocalized(int hstart, int qstart) {
        ArrayList<Integer> prms = EvenKhovCalculator.getPrimes(coeff, options.getPrimes());
        int[] primes = new int[prms.size()];
        for (int i = 0; i < prms.size(); i++) primes[i] = prms.get(i);
        EvenComplex<R> theComplex = getComplex(hstart, qstart);
        ArrayList<String> finalInfo = smithNormalize(theComplex, primes, true);
        if (finalInfo != null) finishOff(finalInfo);
    }
    
    private EvenComplex<R> getComplex(int hstart, int qstart) {
        if (theLink.crossingLength() == 0) return new EvenComplex<R>(0, unit, unred, red, abInf, null);
        EvenComplex<R> theComplex;
        if (theLink.crossingLength() == 1) theComplex = oneCrossingComplex(hstart, qstart);
        else {
            theComplex = firstComplex(hstart, qstart);
            int u = 1;
            int d = 0;
            if (red) d = 1;
            while (u < theLink.crossingLength() - d && !abInf.isAborted()) {
                boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
                    theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
                EvenComplex<R> nextComplex = new EvenComplex<R>(theLink.getCross(u),theLink.getPath(u),0,0,orient,false,unred,
                        false, unit,null,null);
                frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
                theComplex.modifyComplex(nextComplex, 0, girthInfo(u), highDetail);
                u++;
            }
            if (red && !abInf.isAborted()) theComplex = lastComplex(theComplex, u);
        }
        return theComplex;
    }
    
    private EvenComplex<R> oneCrossingComplex(int hs, int qs) {
        EvenComplex<R> theComplex = new EvenComplex<R>(theLink.getCross(0), theLink.getPath(0),hs,qs,false,false,unred,red,
                unit, frame, abInf);
        EvenComplex<R> unComp = new EvenComplex<R>(1, unit, false,true, abInf, frame);
        unComp.modifyComplex(theComplex,reducer(), " ", highDetail);
        return unComp;
        // there is a bug with the oneCrossingComplex. At the moment this is resolved by
        // not using this method. The method containsOne below ensures that this method
        // is not used.
    }
    
    private EvenComplex<R> firstComplex(int hs, int qs) {
        EvenComplex<R> theComplex = 
                new EvenComplex<R>(theLink.getCross(0), theLink.getPath(0),hs,qs,false,false,true,false,unit,frame,abInf);
        if (theComplex.posNumber() == 2) return theComplex;
        EvenComplex<R> unComp = new EvenComplex<R>(1, unit, false, true, abInf, frame);
        unComp.modifyComplex(theComplex, 0, " ", highDetail);
        return unComp;
    }
    
    private EvenComplex<R> lastComplex(EvenComplex<R> theComplex, int u) {
        boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
            theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
        EvenComplex<R> nextComplex = new EvenComplex<R>(theLink.getCross(u),theLink.getPath(u),0,0,orient,false,unred,
                false,unit,null,null);
        frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
        int c = 1+theLink.getPath(u, theLink.basepoint());
        if (unred) c = -c;
        theComplex.modifyComplex(nextComplex, c, "0", highDetail);
        return theComplex;
    }
    
    private int reducer() {
        int ucer = 0;
        if (red) ucer = 1+theLink.getPath(0, theLink.basepoint());
        if (unred) ucer = -ucer;
        return ucer;
    }

    private int getThePrime() {
        int pp = (int) coeff;
        int i = 1;
        int p = options.getPrimes().get(0);
        while (pp % p != 0) {
            p = options.getPrimes().get(i);
            i++;
        }
        return p;
    }
    
    protected void finishOff(ArrayList<String> finalInfo) {
        int add = 0;
        if (theLink.components() % 2 == 0) add++;
        for (String info : finalInfo) {
            if (!info.contains("x")) {
                if (Math.abs(quantum(info)) % 2 == add) endredHom.add(info);
                else endunredHom.add(info);
            }
        }
        if (!endredHom.isEmpty()) lastLine(true, endredHom);
        if (!endunredHom.isEmpty()) lastLine(false, endunredHom);
    }
    
    private void lastLine(boolean reduced, ArrayList<String> endHom) {
        String last = "u"+coeff+".";
        if (reduced) last = "r"+coeff+".";
        for (String info : endHom) {
            if (info.contains("aborted")) last = last+"a"+quantum(info)+".";
        }
        if (reduced) last = last+theLink.basecomponent()+"c.";
        endHom.add(0,last);
    }
    
    private int quantum(String info) {
        int e = info.indexOf('h');
        if (e == -1) e = info.indexOf('a');
        return Integer.parseInt(info.substring(1, e));
    }
    
}
