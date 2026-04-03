/*

Copyright (C) 2021 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

import java.util.concurrent.CountDownLatch;
import javax.swing.JFrame;
import knotjob.AbortInfo;

/**
 *
 * @author Dirk
 */
public class DialogWrap {

    private final CountDownLatch countDown;
    private final String endMessage;
    
    public DialogWrap(CountDownLatch cntdwn, String info) {
        countDown = cntdwn;
        endMessage = info;
    }
    
    public void setLabelLeft(String substring, int lv, boolean check) {
        
    }
    
    public void setLabelRight(String substring, int lv, boolean check) {
        
    }
    
    public void setTitleLabel(String substring, boolean check) {
        
    }
    
    public void setText(String substring) {
        
    }

    public void dispose() {
        countDown.countDown();
        if (endMessage != null) System.out.println(endMessage);
    }
    
    public JFrame getFrame() {
        return null;
    }
    
    public AbortInfo getAbortInfo() {
        return new AbortInfo();
    }
    
    protected void delay(int k) {
        try {
            Thread.sleep(k);
        }
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
}
