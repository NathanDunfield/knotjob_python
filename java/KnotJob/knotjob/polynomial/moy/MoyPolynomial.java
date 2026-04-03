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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import knotjob.AbortInfo;
import knotjob.Calculation;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.polynomial.Polynomial;
import knotjob.rings.BigInt;

/**
 *
 * @author Dirk
 */
public class MoyPolynomial extends Calculation<BigInt> {
    
    //private final Link theLink;
    private final int theN;
    //private final DialogWrap frame;
    //private final AbortInfo abInf;
    //private final Options options;
    //private final int[] girth;
    private final ArrayList<int[]> orients;
    private final int[] signs;
    private String moyPoly;
    
    public MoyPolynomial(LinkData link, int n, DialogWrap frm, Options optns) {
        super(Reidemeister.freeOfOne(link.chosenLink()).breakUp().girthDiscMinimize(), 
                frm, new BigInt(1),optns);
        theN = n;
        //frame = frm;
        //abInf = frm.getAbortInfo();
        //options = optns;
        orients = theLink.getOrientationsOfCrossings();
        signs = theLink.allCrossingSigns();
        //girth = theLink.totalGirthArray();
        moyPoly = null;
    }
    
    @Override
    public void calculate() {
        Polynomial moy = getMoyPolynomial();
        moyPoly = moy.toString();
    }

    public String moyInfo() {
        return moyPoly;
    }
    
    private Polynomial getMoyPolynomial() {
        if (theLink.crossingLength() == 0) return null;
        if (theLink.crossingLength() == 1) return null;
        MoyBracket bracket = thePolynomial(0);
        int u = 1;
        while (u < theLink.crossingLength() && !abInf.isAborted()) {
            MoyBracket nextBracket = thePolynomial(u);
            bracket = bracket.combineWith(nextBracket, girthInfo(u));
            u++;
            bracket.output();
            throw new UnsupportedOperationException("Not supported yet.");
        }
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private MoyBracket thePolynomial(int p) {
        int[] orientStart = orients.get(p);
        int[] path = theLink.getPath(p);
        int[] pos = new int[2];
        int[] neg = new int[2];
        if (orientStart[0] == (orientStart[1]+1)%4) {
            pos[0] = path[orientStart[0]];
            pos[1] = path[orientStart[1]];
            neg[0] = path[(orientStart[0]+1)%4];
            neg[1] = path[(orientStart[1]+3)%4];
        }
        else {
            pos[0] = path[orientStart[1]];
            pos[1] = path[orientStart[0]];
            neg[0] = path[(orientStart[1]+1)%4];
            neg[1] = path[(orientStart[0]+3)%4];
        }
        //System.out.println(Arrays.toString(pos)+" "+Arrays.toString(neg));
        //System.out.println(Arrays.toString(signs));
        Polynomial zeroPol = new Polynomial(new String[] {"q"}, BigInteger.ONE, new int[] {-2 * signs[p]});
        Polynomial nonzPol = new Polynomial(new String[] {"q"}, BigInteger.ONE.negate(), 
                new int[] {-3 * signs[p]});
        MoyBracket bracket = new MoyBracket(pos, neg, p, frame, abInf);
        bracket.addPoly(zeroPol, 0, 0);
        bracket.addPoly(nonzPol, 1, 0);
        return bracket;
    }
    
}
