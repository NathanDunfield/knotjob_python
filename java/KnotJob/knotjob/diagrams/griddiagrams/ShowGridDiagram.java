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
package knotjob.diagrams.griddiagrams;

import java.util.concurrent.CountDownLatch;
import knotjob.frames.GridDiagramFrame;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class ShowGridDiagram extends Thread {
    
    private final LinkData theLink;
    private final GridDiagramFrame frame;
    private CountDownLatch countDown;
    private GridDiagram gridDiagram;
    
    public ShowGridDiagram(LinkData tlnk, GridDiagramFrame frm) {
        theLink = tlnk;
        frame = frm;
    }
    
    @Override
    public void run() {
        countDown = new CountDownLatch(1);
        GridThread gridDig = new GridThread(theLink, frame, true, countDown);
        gridDig.start();
        try {
            countDown.await();
            gridDiagram = gridDig.getDiagram(0);
        } 
        catch (InterruptedException ex) {
            gridDiagram = null;
            //Logger.getLogger(ShowDiagram.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (gridDiagram == null) {
            frame.dispose();
            return;
        }
        delay(100);
        frame.setButtons(gridDiagram);
    }
    
    void delay(int k) {
        try {
            Thread.sleep(k);
        }
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
