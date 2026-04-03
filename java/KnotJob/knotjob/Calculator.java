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

package knotjob;

import java.util.ArrayList;
import knotjob.dialogs.DialogWrap;
import knotjob.dialogs.TimerDialog;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public abstract class Calculator extends Thread  {
    
    private final ArrayList<LinkData> linkList;
    protected final DialogWrap frame;
    protected final Options options;
    protected final AbortInfo abInf;
    
    public Calculator(ArrayList<LinkData> lnkLst, Options optns, DialogWrap frm) {
        linkList = lnkLst;
        frame = frm;
        options = optns;
        abInf = frame.getAbortInfo();
    }
    
    @Override
    public void run() {
        long start = System.nanoTime();
        frame.setTitleLabel(linkList.get(0).name, true);
        setUpRestFrame();
        for (LinkData theLink : linkList) {
            adaptFrame();
            frame.setTitleLabel(theLink.name, false);
            if (calculationRequired(theLink)) {
                performCalculation(theLink);
            }
            if (abInf.isCancelled()) break;
            if (abInf.isAborted()) abInf.deAbort();
        }
        long end = System.nanoTime();
        frame.dispose();
        if (options.getTimeInfo()) {
            TimerDialog dialog = new TimerDialog(frame.getFrame(), "Calculation Time", true, end - start);
            dialog.setup();
        }
    }

    protected abstract void setUpRestFrame();

    protected abstract boolean calculationRequired(LinkData theLink);

    protected abstract void performCalculation(LinkData theLink);

    protected abstract void adaptFrame();
}
