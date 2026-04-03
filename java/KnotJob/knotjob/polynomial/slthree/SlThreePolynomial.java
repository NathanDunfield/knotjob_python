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

package knotjob.polynomial.slthree;

import java.math.BigInteger;
import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.polynomial.Coefficient;
import knotjob.polynomial.Polynomial;

/**
 *
 * @author Dirk
 */
public class SlThreePolynomial {
    
    private final Link theLink;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final Options options;
    private final int[] girth;
    
    public SlThreePolynomial(LinkData link, DialogWrap frm, Options optns) {
        theLink = Reidemeister.freeOfOne(link.chosenLink()).breakUp().girthMinimize();
        frame = frm;
        options = optns;
        abInf = frame.getAbortInfo();
        girth = theLink.totalGirthArray();
    }
    
    public void calculate() {
        Polynomial bracket = getBracket();
        System.out.println(bracket);
    }
    
    private Polynomial getBracket() {
        if (theLink.crossingNumber() == 0) return unknotPoly();
        SpiderPolynomial spider = firstPolynomial();
        int u = 1;
        while (u < theLink.crossingLength() && !abInf.isAborted()) {
            SpiderPolynomial next = nextPolynomial(u, spider);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            spider.modify(next, girthInfo(u));
            u++;
        }
        return spider.getLastPolynomial();
    }

    private String girthInfo(int u) {
        String info = String.valueOf(girth[u]);
        if (options.getGirthInfo()!= 2) return info;
        if (u < girth.length - 1) info = info+" ("+girth[u+1];
        else return info;
        for (int i = 1; i < 3; i++) {
            if (u < girth.length - i - 1) info = info+", "+girth[u+1+i];
        }
        info = info+")";
        return info;
    }
    
    private int[] positionsOf(int i) {
        ArrayList<ArrayList<Integer>> comps = theLink.getComponents();
        int[] path = theLink.getPath(i);
        int[] pos = new int[4];
        for (int k = 0; k < 4; k++) {
            int j = theLink.compOf(path[k]);
            pos[k] = comps.get(j).indexOf(path[k]);
        }
        return pos;
    }

    private Polynomial unknotPoly() {
        ArrayList<Coefficient> coeffs = new ArrayList<Coefficient>();
        for (int i = -2; i <= 2; i = i+2) {
            coeffs.add(new Coefficient(new int[] {i}, BigInteger.ONE));
        }
        Polynomial polThr = new Polynomial(new String[] {"q"}, coeffs);
        Polynomial pol = new Polynomial(new String[] {"q"}, coeffs);
        int k = theLink.unComponents();
        while (k > 1) {
            k--;
            pol = pol.multiply(polThr);
        }
        return pol;
    }

    private SpiderPolynomial firstPolynomial() {
        int[] pos = positionsOf(0);
        int[] pts = new int[4];
        boolean right = (biggerPos(pos[2], pos[0])) == (biggerPos(pos[1], pos[3]));
        int start = 1;
        if (right) start = 0;
        if (biggerPos(pos[0], pos[2])) {
            pts[0] = theLink.getPath(0, start);
            pts[1] = theLink.getPath(0, (3+start)%4);
            pts[2] = theLink.getPath(0, start+1);
            pts[3] = theLink.getPath(0, start+2);
        }
        else {
            pts[3] = theLink.getPath(0, start);
            pts[2] = theLink.getPath(0, (3+start)%4);
            pts[1] = theLink.getPath(0, start+1);
            pts[0] = theLink.getPath(0, start+2);
        }
        if (theLink.getCross(0) < 0) right = !right;
        int factor = 1;
        if (!right) factor = -1;
        SpiderPolynomial spider = new SpiderPolynomial(pts, 0, factor, frame, abInf);
        return spider;
    }

    private SpiderPolynomial nextPolynomial(int u, SpiderPolynomial spider) {
        int[] path = theLink.getPath(u);
        ArrayList<Integer> overlap = spider.overlap(path);
        int[] pos = positionsOf(u);
        boolean ne = biggerPos(pos[0], pos[2]);
        boolean nw = biggerPos(pos[3], pos[1]);
        if (overlap.contains(0) && overlap.contains(2)) ne = spider.direction(path[0]);
        if (overlap.contains(1) && overlap.contains(3)) nw = spider.direction(path[3]);
        boolean right = ne == nw;
        int start = 1;
        if (right) start = 0;
        int[] pts = new int[4];
        if (ne) {
            pts[0] = theLink.getPath(u, start);
            pts[1] = theLink.getPath(u, (3+start)%4);
            pts[2] = theLink.getPath(u, start+1);
            pts[3] = theLink.getPath(u, start+2);
        }
        else {
            pts[3] = theLink.getPath(u, start);
            pts[2] = theLink.getPath(u, (3+start)%4);
            pts[1] = theLink.getPath(u, start+1);
            pts[0] = theLink.getPath(u, start+2);
        }
        if (theLink.getCross(u) < 0) right = !right;
        int factor = 1;
        if (!right) factor = -1;
        SpiderPolynomial nxSpider = new SpiderPolynomial(pts, u, factor, frame, abInf);
        return nxSpider;
    }
    
    private boolean biggerPos(int a, int b) { // is a < b ? cyclically?
        if (a < b) return b-a == 1;
        return a-b > 1;
    }
    
}
