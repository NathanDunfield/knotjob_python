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
package knotjob.dialogs;

import javax.swing.JFrame;
import knotjob.AbortInfo;

/**
 *
 * @author Dirk
 */
public class CalculationDialogWrap extends DialogWrap {
    
    private final CalculationDialog frame;
    private final boolean detailed;
    private final String[] leftLabels;
    private final String[] rightLabels;
    private String title;
    private long lastTime;
    private final long diff = 500000000l;
    private final int number;
    
    public CalculationDialogWrap(CalculationDialog frm, boolean det) {
        super(null, null);
        frame = frm;
        detailed = det;
        number = frame.getNumber();
        leftLabels = new String[number];
        rightLabels = new String[number];
        long time = System.nanoTime() - 1000000000l;
        lastTime = time;
    }

    @Override
    public void dispose() {
        delay(200);
        frame.updateKnobster();
        frame.dispose();
    }
    
    @Override
    public JFrame getFrame() {
        return frame.frame;
    }
    
    @Override
    public AbortInfo getAbortInfo() {
        return frame.abInf;
    }
    
    @Override
    public void setLabelLeft(String substring, int lv, boolean check) {
        if (detailed) {
            leftLabels[lv] = substring;
            checkTime(check);
        }
    }
    
    @Override
    public void setLabelRight(String substring, int lv, boolean check) {
        if (detailed) {
            rightLabels[lv] = substring;
            checkTime(check);
        }
    }
    
    @Override
    public void setTitleLabel(String substring, boolean check) {
        title = substring;
        checkTime(check);
    }
    
    private void checkTime(boolean check) {
        long time = System.nanoTime();
        if (check || time - lastTime > diff) {
            lastTime = time;
            updateLabels();
        }
    }
    
    private void updateLabels() {
        frame.setTitleLabel(title);
        if (detailed) {
            for (int i = 0; i < number; i++) {
                frame.setLabelLeft(leftLabels[i], i);
                frame.setLabelRight(rightLabels[i], i);
            }
        }
    }
}
