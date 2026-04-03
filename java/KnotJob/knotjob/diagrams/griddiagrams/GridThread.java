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

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import javax.swing.JFrame;
import knotjob.AbortInfo;
import knotjob.diagrams.SComplex;
import knotjob.links.Link;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class GridThread extends Thread {

    private final CountDownLatch countDown;
    private final JFrame frame;
    private final AbortInfo abort;
    private final ArrayList<LinkData> theLinks;
    private final boolean getDig;
    private final ArrayList<SComplex> theComplexes;
    private final ArrayList<GridDiagram> theDiagrams;
    
    public GridThread(LinkData lnk, JFrame fram, boolean dig, CountDownLatch counter) {
        countDown = counter;
        frame = fram;
        theLinks = new ArrayList<LinkData>();
        theLinks.add(lnk);
        abort = new AbortInfo();
        getDig = dig;
        theComplexes = new ArrayList<SComplex>();
        theDiagrams = new ArrayList<GridDiagram>();
    }
    
    @Override
    public void run() {
        for (LinkData lData : theLinks) {
            if (getDig) getTheDiagram(lData);
            else getTheGrid(lData);
            if (abort.isCancelled()) break;
        }
        countDown.countDown();
    }

    public SComplex getSComplex(int i) {
        return theComplexes.get(i);
    }
    
    public GridDiagram getDiagram(int i) {
        return theDiagrams.get(i);
    }
    
    private void getTheGrid(LinkData lData) {
        Link link = lData.chosenLink().breakUp();
        GridCreator grid = new GridCreator(link, abort);
        theComplexes.add(grid.getSComplex());
    }
    
    private void getTheDiagram(LinkData lData) {
        Link link = lData.chosenLink().breakUp();
        GridCreator grid = new GridCreator(link, abort);
        theDiagrams.add(grid.getDiagram());
    }
    
}
