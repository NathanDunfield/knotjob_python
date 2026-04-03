/*

Copyright (C) 2021-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

/**
 *
 * @author Dirk
 */
public class AlexanderCalculator extends Calculator {
    
    public AlexanderCalculator(ArrayList<LinkData> lnkLst, Options optns, DialogWrap frm) {
        super(lnkLst, optns, frm);
    }
    
    @Override
    protected boolean calculationRequired(LinkData theLink) {
        return (theLink.alex == null);
    }

    @Override
    protected void setUpRestFrame() {
        
    }

    @Override
    protected void performCalculation(LinkData theLink) {
        AlexanderPolynomial alex = new AlexanderPolynomial(theLink, frame, options);
        alex.calculate();
        String info = alex.alexanderInfo();
        if (info != null) {
            theLink.setAlexander(info);
            if (theLink.determinant == null) theLink.determinant = alex.detInfo();
        }
    }

    @Override
    protected void adaptFrame() {
        
    }
    
}
