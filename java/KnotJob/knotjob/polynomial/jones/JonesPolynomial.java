/*

Copyright (C) 2019-21 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.polynomial.jones;

import java.math.BigInteger;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.polynomial.HalfPolynomial;
import knotjob.polynomial.Polynomial;

/**
 *
 * @author Dirk
 */
public class JonesPolynomial {

    private final Link theLink;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final Options options;
    private final int[] girth;
    private HalfPolynomial jonesPoly;
    
    public JonesPolynomial(LinkData link, DialogWrap frm, Options optns) {
        theLink = Reidemeister.freeOfOne(link.chosenLink()).breakUp().girthMinimize();
        frame = frm;
        options = optns;
        abInf = frame.getAbortInfo();
        girth = theLink.totalGirthArray();
        jonesPoly = null;
    }

    public void calculate() {
        Polynomial bracket = getBracket();
        if (bracket == null) return;
        bracket = bracket.multiply(writheShift());
        jonesPoly = jonesify(bracket);
    }
    
    public String jonesInfo() {
        if (jonesPoly == null) return null;
        return jonesPoly.toReducedString();
    }

    private Polynomial getBracket() {
        if (theLink.crossingLength() == 0) return unknotPoly();
        if (theLink.crossingLength() == 1) return oneCrossingPoly();
        BracketPolynomial bracket = firstPolynomial();
        int u = 1;
        while (u < theLink.crossingLength()-1 && !abInf.isAborted()) {
            boolean orient = (bracket.negContains(theLink.getPath(u, 0))| bracket.negContains(theLink.getPath(u, 2))| 
                    bracket.posContains(theLink.getPath(u,1)) | bracket.posContains(theLink.getPath(u,3)));
            BracketPolynomial nextBracket = new BracketPolynomial(theLink.getCross(u), theLink.getPath(u), 
                    frame, abInf, orient);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            bracket.modify(nextBracket, girthInfo(u));
            u++;
        }
        if (abInf.isAborted()) return null;
        boolean orient = (bracket.negContains(theLink.getPath(u, 0))| bracket.negContains(theLink.getPath(u, 2))| 
                    bracket.posContains(theLink.getPath(u,1)) | bracket.posContains(theLink.getPath(u,3)));
        BracketPolynomial nextBracket = new BracketPolynomial(theLink.getCross(u), theLink.getPath(u), 
                frame, abInf, orient);
        frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
        bracket.modifyLast(nextBracket);
        return bracket.finalPolynomial();
    }

    private Polynomial oneCrossingPoly() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private BracketPolynomial firstPolynomial() {
        BracketPolynomial bracket = new BracketPolynomial(theLink.getCross(0), theLink.getPath(0),
                frame, abInf, false);
        return bracket;
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

    private Polynomial writheShift() {
        int writhe = theLink.writhe();
        BigInteger value = BigInteger.valueOf(-1);
        if (writhe%2 == 0) value = BigInteger.ONE;
        return new Polynomial(new String[] {"A"}, value, new int[] {-3*writhe});
    }
    
    private HalfPolynomial jonesify(Polynomial poly) {
        if (theLink.unComponents() > 0) {
            int u = theLink.unComponents();
            if (theLink.components() == u) u--;
            Polynomial unlinkPoly = new Polynomial(new String[] {"A"}, 
                    BigInteger.valueOf(-1), new int[] {2});
            unlinkPoly = unlinkPoly.add(new Polynomial(new String[] {"A"}, 
                    BigInteger.valueOf(-1), new int[] {-2}));
            while (u > 0) {
                poly = poly.multiply(unlinkPoly);
                u--;
            }
            
        }
        return new HalfPolynomial(poly, new String[] {"q"}, new int[] {-2});
    }

    private Polynomial unknotPoly() {
        return new Polynomial(new String[] {"A"}, BigInteger.ONE, new int[] {0});
    }
    
}
