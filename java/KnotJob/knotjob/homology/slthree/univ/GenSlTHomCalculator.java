/*

Copyright (C) 2025 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.slthree.univ;

import java.math.BigInteger;
import java.util.ArrayList;
import knotjob.Calculator;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.LinkData;
import knotjob.rings.BigRat;
import knotjob.rings.BigTrans;
import knotjob.rings.ModN;
import knotjob.rings.QuadExt;

/**
 *
 * @author Dirk
 */
public class GenSlTHomCalculator extends Calculator {

    public static int TYPE_STANDARD = 0;
    public static int TYPE_ALGEBRAIC = 1;
    public static int TYPE_TRANSCENDENTAL = 2;
    public static int TYPE_CHAR_THREE = 3;
    
    private final int typ;
    private final String symbol;
    private int shift = 1;
    
    public GenSlTHomCalculator(ArrayList<LinkData> lnkLst, String sym, Options optns, DialogWrap frm) {
        super(lnkLst, optns, frm);
        symbol = sym;
        typ = getTypeFromSymbol();
    }
    
    private int getTypeFromSymbol() {
        if ("t".equals(symbol)) return TYPE_TRANSCENDENTAL;
        if ("+1".equals(symbol) || "-1".equals(symbol)) {
            shift = 1;
            if ("-1".equals(symbol)) shift = -1;
            return TYPE_CHAR_THREE;
        }  
        try {
            shift = Integer.parseInt(symbol);
        }
        catch (NumberFormatException e) {
            shift = Integer.parseInt(symbol.substring(1));
            return TYPE_ALGEBRAIC;
        }
        return TYPE_STANDARD;
    }

    @Override
    protected void setUpRestFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, true);
    }

    @Override
    protected boolean calculationRequired(LinkData theLink) {
        if (typ == TYPE_CHAR_THREE) return theLink.bltInfo(symbol, 7) == null;
        return theLink.bltInfo(symbol, 6) == null;
    }

    @Override
    protected void performCalculation(LinkData theLink) {
        ArrayList<String> theInfo;
        ArrayList<Integer> theBlocks;
        if (typ == TYPE_TRANSCENDENTAL) {
            BigTrans theT = new BigTrans();
            GenSlThreeHom<BigTrans> tSpec = new GenSlThreeHom<BigTrans>(theLink, frame, 
                    new BigTrans(BigInteger.ONE), theT, 2, options);
            tSpec.calculate(); // */
            /* // Somewhat experimental over a polynomial ring
            BigRat unit = new BigRat(BigInteger.ONE);
            PolFunc<BigRat> theT = new PolFunc<BigRat>(unit, BigInteger.ONE, unit);
            GenSlThreeHom<PolFunc<BigRat>> tSpec = new GenSlThreeHom<PolFunc<BigRat>>(theLink, frame,
                    new PolFunc<BigRat>(unit, unit), theT, options);
            tSpec.polyCalc(); 
            abInf.abort();// */
            theInfo = tSpec.getInfo();
            theBlocks = tSpec.getBlocks();
        }
        else if (typ == TYPE_STANDARD) {
            BigRat theT = new BigRat(BigInteger.valueOf(shift));
            GenSlThreeHom<BigRat> tSpec = new GenSlThreeHom<BigRat>(theLink, frame,
                    new BigRat(BigInteger.ONE), theT, 2, options);
            tSpec.calculate();
            theInfo = tSpec.getInfo();
            theBlocks = tSpec.getBlocks();
        }
        else if (typ == TYPE_ALGEBRAIC) { 
            BigRat unit = new BigRat(BigInteger.ONE);
            BigRat square = new BigRat(BigInteger.valueOf(shift));
            QuadExt<BigRat> theT = new QuadExt<BigRat>(unit.getZero(), unit, square);
            GenSlThreeHom<QuadExt<BigRat>> tSpec = new GenSlThreeHom<QuadExt<BigRat>>(theLink,
                    frame, new QuadExt<BigRat>(unit, square), theT, 2, options);
            tSpec.calculate();
            theInfo = tSpec.getInfo();
            theBlocks = tSpec.getBlocks();
        }
        else { // typ == TYPE_CHAR_THREE
            ModN unit = new ModN(1, 3);
            ModN theT = new ModN(shift, 3);
            GenSlThreeHom<ModN> tSpec = new GenSlThreeHom<ModN>(theLink, frame, unit, theT, 1, options);
            tSpec.calculate();
            theInfo = tSpec.getInfo();
            theBlocks = tSpec.getBlocks();
        }
        if (!abInf.isAborted()) {
            theLink.reLock.lock();
                try {
                    theLink.wrapUpSlTSX(theInfo, theBlocks, symbol, typ);
                }
                finally {
                    theLink.reLock.unlock();
                }
        }
    }

    @Override
    protected void adaptFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, false);
        if (options.getGirthInfo() == 2) frame.setLabelLeft("h-Level : ", 3, false);
    }
    
}
