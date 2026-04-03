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

package knotjob.homology.oddkhov.unified;

import java.math.BigInteger;
import java.util.ArrayList;
import knotjob.Calculator;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.LinkData;
import knotjob.rings.BigIntXi;
import knotjob.rings.ModNXi;

/**
 *
 * @author Dirk
 */
public class UnifiedKhovCalculator extends Calculator {

    private final long coeff;
    
    public UnifiedKhovCalculator(ArrayList<LinkData> lnkLst, long val, Options optns, 
            DialogWrap frm) {
        super(lnkLst, optns, frm);
        coeff = val;
    }
    
    @Override
    protected void setUpRestFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, true);
        if (options.getGirthInfo() == 2) frame.setLabelLeft("h-Level : ", 3, false);
    }

    @Override
    protected boolean calculationRequired(LinkData theLink) {
        return true;
    }

    @Override
    protected void performCalculation(LinkData theLink) {
        if (coeff <= 1) {
            BigIntXi unit = new BigIntXi(BigInteger.ONE);
            BigIntXi xi = new BigIntXi(BigInteger.ZERO, BigInteger.ONE);
            UnifiedKhovHomology<BigIntXi> hom = new UnifiedKhovHomology<BigIntXi>(theLink, coeff, 
                    unit, xi, frame, options);
            hom.calculate();
            //if (!abInf.isAborted()) wrapUp(theLink, hom.getOddHomology());
        }
        if (coeff == 2) {
            ModNXi unit = new ModNXi(1, 2);
            ModNXi xi = new ModNXi(0, 1, 2);
            UnifiedKhovHomology<ModNXi> hom = new UnifiedKhovHomology<ModNXi>(theLink, coeff, 
                    unit, xi, frame, options);
            hom.calculate();
        }
        /*UnifiedChainComplex<BigIntXi> complex = new UnifiedChainComplex<BigIntXi>
                (new BigIntXi(BigInteger.ONE), frame, abInf);
        UnifiedGenerator<BigIntXi> aOne = new UnifiedGenerator<BigIntXi>(0, 0);
        UnifiedGenerator<BigIntXi> aXi = new UnifiedGenerator<BigIntXi>(0, 0);
        complex.addGenerator(aOne, 0);
        complex.addGenerator(aXi, 0);
        BigIntXi unit = new BigIntXi(BigInteger.ONE);
        Arrow<BigIntXi> arr = new Arrow<BigIntXi>(aOne, aXi, unit);
        aOne.addOutArrow(arr);
        aXi.addInArrow(arr);
        arr = new Arrow<BigIntXi>(aXi, aOne, unit);
        aXi.addOutArrow(arr);
        aOne.addInArrow(arr);
        complex.output(complex);
        complex.shift(unit.add(unit.add(unit)));
        complex.output(complex);// */
    }

    @Override
    protected void adaptFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
        frame.setLabelLeft("Objects : ", 2, false);
        if (options.getGirthInfo() == 2) frame.setLabelLeft("h-Level : ", 3, false);
    }
    
}
