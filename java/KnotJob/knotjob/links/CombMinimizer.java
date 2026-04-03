/*

Copyright (C) 2019-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.links;

import java.util.ArrayList;
import knotjob.dialogs.DialogWrap;

/**
 *
 * @author Dirk
 */
public class CombMinimizer extends Thread {
    
    private boolean cancelled;
    private boolean aborted;
    private boolean skip;
    private int counter;
    private final int counterMax;
    private final boolean combine;
    private final ArrayList<LinkData> theLinks;
    private final DialogWrap frame;
    
    public CombMinimizer(ArrayList<LinkData> thLnks, DialogWrap frm, boolean comb) {
        theLinks = thLnks;
        frame = frm;
        cancelled = false;
        aborted = false;
        combine = comb;
        counterMax = 10000;
    }
    
    public CombMinimizer(LinkData lnk, int max, boolean comb) {
        theLinks = new ArrayList<LinkData>();
        theLinks.add(lnk);
        combine = comb;
        counterMax = max;
        frame = null;
    }
    
    @Override
    public void run() {
        int i = 0;
        while (!cancelled && i < theLinks.size()) {
            counter = 0;
            skip = false;
            minimize(theLinks.get(i));
            aborted = false;
            i++;
        }
        frame.dispose();
    }

    public Link getMinimized() {
        return getOptimized(theLinks.get(0).chosenLink()).girthMinimize();
    }
    
    private void minimize(LinkData link) {
        frame.setText(link.name);
        Link optLink = getOptimized(link.chosenLink());
        int extra = 1;
        if (combine) extra--;
        if (!aborted && optLink.crossingLength() < link.chosenLink().crossingLength()+extra) 
            if (!link.containsLink(optLink)) link.links.add(optLink.girthMinimize());
    }
    
    private Link getOptimized(Link link) {
        boolean cont = true;
        Link goodOne = Reidemeister.combineCrossings(link);
        int start = link.crossingLength();
        while (cont) {
            link = doReidemeisterII(link);
            ArrayList<int[]> moves = Reidemeister.findReidemeisterIII(link);
            if (moves.isEmpty()) {
                if (combine) return Reidemeister.combineCrossings(link);
                return link;
            }
            int r = (int) Math.round(Math.random() * (moves.size()-1));
            link = doReidemeisterII(Reidemeister.performMove(link, moves.get(r)));
            if (combine) {
                Link next = Reidemeister.combineCrossings(link);
                if (next.crossingLength() < goodOne.crossingLength()) {
                    goodOne = next;
                }
            }
            counter++;
            if (counter %500 == 0 && frame != null) {
                frame.setLabelLeft(""+counter, 0, false);
                if (combine) frame.setLabelLeft(goodOne.crossingLength()+"/"+start, 1, false);
                else frame.setLabelLeft(link.crossingLength()+"/"+start, 1, false);
            }
            if (counter >= counterMax || skip || aborted) cont = false;
        }
        if (aborted) return null;
        if (combine) return goodOne;
        return link;
    }
    
    private Link doReidemeisterII(Link link) {
        boolean cont = true;
        while (cont) {
            link = Reidemeister.freeOfOne(link);
            int[] move = Reidemeister.findReidemeisterII(link);
            if (move[0] == -1) cont = false;
            else link = Reidemeister.performMove(link, move);
        }
        return link;
    }

    public void setAborted() {
        aborted = true;
    }

    public void setCancelled() {
        cancelled = true;
    }

    public void setSkipped() {
        skip = true;
    }
    
}
