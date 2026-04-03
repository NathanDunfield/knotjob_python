/*

Copyright (C) 2020 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.diagrams;

/**
 *
 * @author Dirk
 */
public class PointMover extends Thread {
    
    private final GraphicalDiagram theDiagram;
    private boolean keepRunning;
    
    public PointMover(GraphicalDiagram diagram) {
        theDiagram = diagram;
        keepRunning = true;
    }
    
    @Override
    public void run() {
        while (keepRunning) {
            for (int cp = 0; cp < theDiagram.theComplexes.size(); cp++) {
                theDiagram.movePoints(cp);
            }
            theDiagram.repaint();
        }
        theDiagram.repaint();
    }
    
    public void stopRunning() {
        keepRunning = false;
    }
    
}
