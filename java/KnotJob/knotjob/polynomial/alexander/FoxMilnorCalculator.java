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

package knotjob.polynomial.alexander;

import java.util.ArrayList;
import knotjob.Calculator;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.LinkData;
import knotjob.polynomial.HalfPolynomial;

/**
 *
 * @author Dirk
 */
public class FoxMilnorCalculator extends Calculator  {

    public FoxMilnorCalculator(ArrayList<LinkData> lnkLst, Options optns, DialogWrap frm) {
        super(lnkLst, optns, frm);
    }
    
    @Override
    protected void setUpRestFrame() {
        
    }

    @Override
    protected boolean calculationRequired(LinkData theLink) {
        if (theLink.alex == null) return true;
        if (theLink.alex.contains("N")) return false;
        return !theLink.alex.contains("F");
    }

    @Override
    protected void performCalculation(LinkData theLink) {
        if (theLink.alex == null) {
            AlexanderPolynomial alex = new AlexanderPolynomial(theLink, frame, options);
            alex.calculate();
            String info = alex.alexanderInfo();
            if (info != null) {
                theLink.setAlexander(info);
                if (theLink.determinant == null) theLink.determinant = alex.detInfo();
                if (alex.foxMilnor()) theLink.alex = theLink.alex+"F";
                else theLink.alex = theLink.alex+"N";
            }
        }
        else {
            HalfPolynomial poly = new HalfPolynomial(new String[] {"t"}, theLink.alex);
            if (poly.foxMilnor()) theLink.alex = theLink.alex+"F";
                else theLink.alex = theLink.alex+"N";
        }
    }

    @Override
    protected void adaptFrame() {
        
    }
    
}
