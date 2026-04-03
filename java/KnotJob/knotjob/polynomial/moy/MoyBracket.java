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

package knotjob.polynomial.moy;

import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.polynomial.Polynomial;

/**
 *
 * @author Dirk
 */
public class MoyBracket {
    
    private final ArrayList<MoyWebPolynomial> polynomials;
    private final MoyCache cache;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    
    public MoyBracket(int[] pos, int[] neg, int crs, DialogWrap frm, AbortInfo abf) {
        frame = frm;
        abInf = abf;
        cache = new MoyCache(pos, neg, crs);
        polynomials = new ArrayList<MoyWebPolynomial>();
    }

    private MoyBracket(ArrayList<MoyWebPolynomial> newPolys, MoyCache newCache,
            DialogWrap frm, AbortInfo abf) {
        frame = frm;
        abInf = abf;
        cache = newCache;
        polynomials = newPolys;
    }

    public void addPoly(Polynomial poly, int w, int l) {
        polynomials.add(new MoyWebPolynomial(poly, w, l));
    }
    
    public void output() {
        cache.output();
        for (MoyWebPolynomial poly : polynomials) poly.output();
    }

    public MoyBracket combineWith(MoyBracket nextBracket, String gInfo) {
        frame.setLabelRight(gInfo, 1, false);
        MoyCache newCache = cache.combineCache(nextBracket.cache);
        MoyCache intCache = cache.combineCache(nextBracket.cache);
        ArrayList<MoyWebPolynomial> newPolys = newPolynomialsFrom(nextBracket, newCache, intCache);
        return new MoyBracket(newPolys, newCache, frame, abInf);
    }

    private ArrayList<MoyWebPolynomial> newPolynomialsFrom(MoyBracket nextBracket, 
            MoyCache newCache, MoyCache intCache) {
        ArrayList<MoyWebPolynomial> newPolys = new ArrayList<MoyWebPolynomial>();
        for (MoyWebPolynomial poly : polynomials) {
            for (MoyWebPolynomial next : nextBracket.polynomials) {
                int[] newWeb = newWebFrom(poly, next, nextBracket, intCache);
                Polynomial pol = poly.getPolynomial().multiply(next.getPolynomial());
                newPolys.add(new MoyWebPolynomial(pol, newWeb[0], newWeb[1]));
            }
        }
        
        
        
        return newPolys;
    }

    private int[] newWebFrom(MoyWebPolynomial poly, MoyWebPolynomial next, MoyBracket nextBracket, 
            MoyCache intCache) {
        ArrayList<Edge> fEdges = poly.getWeb(cache);
        ArrayList<Edge> sEdges = next.getWeb(nextBracket.cache);
        ArrayList<Triple<Edge>> fTriples = poly.getTriples(cache);
        ArrayList<Triple<Edge>> sTriples = next.getTriples(nextBracket.cache);
        ArrayList<Integer> fLabels = poly.getLabels(cache);
        ArrayList<Integer> sLabels = next.getLabels(nextBracket.cache);
        return intCache.theWebFrom(fEdges, sEdges, fTriples, sTriples, fLabels, sLabels);
    }
    
}
