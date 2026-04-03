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
import knotjob.Calculator;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class MoyCalculator extends Calculator {

    private int theN;
    
    public MoyCalculator(ArrayList<LinkData> lnkLst, Options optns, DialogWrap frm, int n) {
        super(lnkLst, optns, frm);
        theN = n;
    }
    
    @Override
    protected void setUpRestFrame() {
        
    }

    @Override
    protected boolean calculationRequired(LinkData theLink) {
        return true;
    }

    @Override
    protected void performCalculation(LinkData theLink) {
        MoyPolynomial pol = new MoyPolynomial(theLink, theN, frame, options);
        pol.calculate();
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    protected void adaptFrame() {
        
    }
    
}
