/*

Copyright (C) 2019-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

import java.util.ArrayList;
import knotjob.Calculator;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class JonesCalculator extends Calculator {
    
    public JonesCalculator(ArrayList<LinkData> lnkLst, Options optns, DialogWrap frm) {
        super(lnkLst, optns, frm);
    }
    
    @Override
    protected boolean calculationRequired(LinkData theLink) {
        return (theLink.jones == null);
    }

    @Override
    protected void setUpRestFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, true);
    }

    @Override
    protected void performCalculation(LinkData theLink) {
        JonesPolynomial jones = new JonesPolynomial(theLink, frame, options);
        jones.calculate();
        String info = jones.jonesInfo();
        if (info != null) theLink.setJones(info);
    }

    @Override
    protected void adaptFrame() {
        frame.setLabelLeft("Crossing : ", 0, false);
        frame.setLabelLeft("Girth : ", 1, false);
    }
    
}
