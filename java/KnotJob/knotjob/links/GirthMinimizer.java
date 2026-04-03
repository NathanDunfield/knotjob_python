/*

Copyright (C) 2019-24 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import knotjob.Options;
import knotjob.dialogs.DialogWrap;

/**
 *
 * @author Dirk
 */
public class GirthMinimizer extends Thread {
    
    private boolean cancelled;
    private boolean aborted;
    private boolean skipped;
    private int counter;
    private final int maxCounter;
    private final boolean counts;
    private final boolean random;
    private final int extra;
    private final ArrayList<LinkData> theLinks;
    private final DialogWrap frame;
    private Diagram currentBest;
    private Diagram original;
    private int orgGirth;
    private int orgtGirth;
    private int maxGirth;
    private int totalGirth;

    public GirthMinimizer(ArrayList<LinkData> tLinks, Options opts, DialogWrap fram) {
        theLinks = tLinks;
        frame = fram;
        if (opts.getTotGirth()) extra = 0;
        else extra = 1;
        counts = opts.getBoundGirth();
        maxCounter = opts.getGirthBound();
        random = opts.getRandomGirth();
    }

    @Override
    public void run() {
        cancelled = false;
        aborted = false;
        skipped = false;
        if (random) doItRandomly();
        else doItOldSchool();
        frame.dispose();
    }
    
    String thePaths(int[][] paths) {
        String thePths = "";
        for (int i=0; i < paths.length; i++) {
            for (int j = 0; j < 4; j++) {
                thePths = thePths+paths[i][j];
                if (j!=3) thePths = thePths+",";
            }
            if (i!= paths.length-1) thePths = thePths+",";
        }
        return thePths;
    }
    
    String theCrosss(int[] crossings) {
        String theCrs = "";
        for (int i=0; i < crossings.length; i++) {
            theCrs = theCrs +crossings[i];
            if (i != crossings.length-1) theCrs = theCrs+",";
        }
        return theCrs;
    }
    
    void tryDiag(Diagram start, ArrayList<Integer> nUsed) {
        counter++;
        if (counts && counter >= maxCounter) {
            skipped = true;
            return;
        }
        if (skipped) return;
        if (nUsed.isEmpty()) {
            if (maxGirth(start.paths) <= maxGirth) {
                if (totalGirth(start.paths) < totalGirth & maxGirth(start.paths) <= maxGirth) {
                    maxGirth = maxGirth(start.paths);
                    totalGirth = totalGirth(start.paths);
                    currentBest = start;
                    frame.setLabelLeft(""+maxGirth+"/"+totalGirth, 1, false);
                }
            }
            return;
        }
        int connecter = getConnections(start, nUsed);
        ArrayList<Integer> ends = getEnds(start.paths);
        for (int u : nUsed) {
            if (skipped) return;
            if (overlap(ends, original.paths[u]) == connecter) {
                Diagram newDiag = new Diagram(start, original.paths[u], original.crossings[u],u);
                if (maxGirth(newDiag.paths) <= maxGirth-extra) tryDiag(newDiag, newDiag.notUsed);
            }
        }
    }

    int getConnections(Diagram Start, ArrayList<Integer> nUsed) {
        int connect = 0;
        ArrayList<Integer> ends = getEnds(Start.paths);
        for (int u : nUsed) {
            int t = overlap(ends, original.paths[u]);
            if (t > connect) connect = t; 
        }
        return connect;
    }

    int overlap(ArrayList<Integer> ends, int[] path) {
        int ov = 0;
        for (int t : path) {
            if (ends.contains(t)) ov++;
        }
        return ov;
    }
    
    ArrayList<Integer> getEnds(int[][] paths) {
        ArrayList<Integer> ends = new ArrayList<Integer>();
        for (int j = 0; j < paths.length; j++) {
            for (int i : paths[j]) {
                if (ends.contains((Integer) i)) ends.remove((Integer) i);
                else ends.add(i);
            }
        }
        return ends;
    }
    
    public final int maxGirth(int[][] paths) {
        int max = 0;
        ArrayList<Integer> ends = new ArrayList<Integer>();
        for (int j = 0; j < paths.length; j++) {
            for (int i : paths[j]) {
                if (ends.contains((Integer) i)) ends.remove((Integer) i);
                else ends.add(i);
            }
            if (ends.size()> max) max = ends.size();
        }
        return max;
    }

    public final int totalGirth(int[][] paths) {
        int tot = 0;
        ArrayList<Integer> ends = new ArrayList<Integer>();
        for (int j = 0; j < paths.length; j++) {
            for (int i : paths[j]) {
                if (ends.contains((Integer) i)) ends.remove((Integer) i);
                else ends.add(i);
            }
            tot = tot + ends.size();
        }
        return tot;
    }
    
    public void setSkipped(boolean b) {
        skipped = b;
    }

    public void setAborted(boolean b) {
        aborted = b;
    }

    public void setCancelled(boolean b) {
        cancelled = b;
    }

    private void doItOldSchool() {
        int countr = 0;
        while (!cancelled & countr < theLinks.size() ) {
            int starter = 0;
            LinkData lData = theLinks.get(countr);
            frame.setText(lData.name);
            original = new Diagram(lData.links.get(lData.chosen));
            orgGirth = maxGirth(original.paths);
            orgtGirth = totalGirth(original.paths);
            maxGirth = orgGirth;
            totalGirth = orgtGirth;
            frame.setLabelLeft(""+maxGirth+"/"+totalGirth, 1, false);
            counter = 0;
            while (!aborted & starter < original.crossings.length) {
                Diagram startDiag = new Diagram(original, starter);
                frame.setLabelLeft(String.valueOf(starter), 0, false);
                tryDiag(startDiag, startDiag.notUsed);
                skipped = false;
                starter++;
                counter = 0;
            }
            countr++;
            if (!cancelled) {
                if (maxGirth < orgGirth | totalGirth < orgtGirth) {
                    ArrayList<int[]> orients = original.getOrientations(currentBest.paths);
                    Link newLink = new Link(currentBest.crossings, currentBest.paths, orients, original.comps);
                    lData.links.add(newLink);
                }
            }
            aborted = false;
        }
    }

    private void doItRandomly() {
        int countr = 0;
        while (!cancelled & countr < theLinks.size() ) {
            LinkData lData = theLinks.get(countr);
            frame.setText(lData.name);
            original = new Diagram(lData.links.get(lData.chosen));
            orgGirth = maxGirth(original.paths);
            orgtGirth = totalGirth(original.paths);
            maxGirth = orgGirth;
            totalGirth = orgtGirth;
            frame.setLabelLeft(""+maxGirth+"/"+totalGirth, 1, false);
            counter = 0;
            ArrayList<Integer> usedStarters = new ArrayList<Integer>();
            int crosss = lData.chosenLink().crossingLength();
            int starter = nextStarter(usedStarters, crosss);
            while (!aborted & usedStarters.size() < crosss) {
                frame.setLabelLeft(""+usedStarters.size(), 0, false);
                Diagram startDiag = new Diagram(original, starter);
                tryDiagramFancy(startDiag, startDiag.notUsed);
                skipped = false;
                usedStarters.add(starter);
                counter = 0;
                if (usedStarters.size() < crosss) starter = nextStarter(usedStarters, crosss);
            }
            countr++;
            if (!cancelled) {
                if (maxGirth < orgGirth | totalGirth < orgtGirth) {
                    ArrayList<int[]> orients = original.getOrientations(currentBest.paths);
                    Link newLink = new Link(currentBest.crossings, currentBest.paths, orients, original.comps);
                    lData.links.add(newLink);
                }
            }
            aborted = false;
        }
    }

    private int nextStarter(ArrayList<Integer> usedStarters, int crossings) {
        int rndm = (int) (Math.random() * crossings);
        while (usedStarters.contains(rndm)) {
            rndm = (rndm+1)%crossings;
        }
        return rndm;
    }

    private void tryDiagramFancy(Diagram start, ArrayList<Integer> nUsed) {
        // this begins with a starting position, then looks three steps ahead in an
        // attempt to minimize girth. There is a random element, in case three steps
        // ahead does not give a clear winner. Follows ideas from N Dunfield and S Morrison
        counter++;
        if (counts && counter >= maxCounter) {
            skipped = true;
            return;
        }
        if (skipped) return;
        if (nUsed.isEmpty()) {
            if (maxGirth(start.paths) <= maxGirth) {
                if (totalGirth(start.paths) < totalGirth & maxGirth(start.paths) <= maxGirth) {
                    maxGirth = maxGirth(start.paths);
                    totalGirth = totalGirth(start.paths);
                    currentBest = start;
                    frame.setLabelLeft(""+maxGirth+"/"+totalGirth, 1, false);
                }
            }
            return;
        }
        int connecter = getConnections(start, nUsed);
        ArrayList<Integer> ends = getEnds(start.paths);
        ArrayList<Integer> nextPossibles = new ArrayList<Integer>();
        ArrayList<Double>  nextScores = new ArrayList<Double>();
        for (int u : nUsed) {
            if (overlap(ends, original.paths[u]) == connecter) {
                nextPossibles.add(u);
                nextScores.add(scoreOf(start, u));
            }
        }
        int pos = highestScore(nextScores);
        int u = nextPossibles.get(pos);
        Diagram newDiag = new Diagram(start, original.paths[u], original.crossings[u], u);
        if (maxGirth(newDiag.paths) <= maxGirth-extra) tryDiagramFancy(newDiag, newDiag.notUsed);
        /*pos = secondScore(nextScores, pos);   // this would check the second best
        if (pos >= 0) {                         // leads to exponential checks. 
            u = nextPossibles.get(pos);
            newDiag = new Diagram(start, original.paths[u], original.crossings[u], u);
            if (maxGirth(newDiag.paths) <= maxGirth-extra) tryDiagramFancy(newDiag, newDiag.notUsed);
        }// */
    }

    private Double scoreOf(Diagram start, int u) {
        ArrayList<Diagram> nextGuys = new ArrayList<Diagram>();
        Diagram next = new Diagram(start, original.paths[u], original.crossings[u], u);
        if (!next.notUsed.isEmpty()) {
            ArrayList<Diagram> moreGuys = addToDiagram(next);   // the ones in moreGuys look two steps
                                                                // ahead of start
            if (!moreGuys.get(0).notUsed.isEmpty()) {
                for (Diagram more : moreGuys) {
                    ArrayList<Diagram> evenMore = addToDiagram(more);
                    for (Diagram even : evenMore) nextGuys.add(even);
                }
            }
            else {
                for (Diagram more : moreGuys) nextGuys.add(more);
            }
        }
        else nextGuys.add(next);
        Double curScore = 0.0;
        for (Diagram guy : nextGuys) {
            int score = overlapOf(next, guy);
            if ((double) score > curScore) curScore = (double) score;
        }
        curScore = curScore+Math.random(); // add something random to spice things up
        return curScore;
    }

    private ArrayList<Diagram> addToDiagram(Diagram next) {
        ArrayList<Diagram> newGuys = new ArrayList<Diagram>();
        int connecter = getConnections(next, next.notUsed);
        int slack = 0;
        if (connecter > 1) slack = 1;
        ArrayList<Integer> ends = getEnds(next.paths);
        for (int u : next.notUsed) {
            if (overlap(ends, original.paths[u]) >= connecter - slack) 
                newGuys.add(new Diagram(next, original.paths[u], original.crossings[u], u));
        }
        return newGuys;
    }

    private int overlapOf(Diagram next, Diagram guy) {
        int st = next.crossings.length;
        int overlap = 0;
        ArrayList<Integer> ends = getEnds(next.paths);
        for (int i = st; i < guy.crossings.length; i++) {
            int[] pos = guy.paths[i];
            for (int p : pos) {
                if (ends.contains(p)) {
                    overlap++;
                    ends.remove((Integer) p);
                }
                else ends.add(p);
            }
        }
        return overlap;
    }

    private int highestScore(ArrayList<Double> nextScores) {
        Double high = nextScores.get(0);
        int i = 0;
        for (int j = 1; j < nextScores.size(); j++) {
            if (nextScores.get(j) > high) {
                high = nextScores.get(j);
                i = j;
            }
        }
        return i;
    }
    
    private int secondScore(ArrayList<Double> nextScores, int max) {
        if (nextScores.size() <= 1) return -1;
        Double high = 0.0;
        int i = 0;
        for (int j = 0; j < nextScores.size(); j++) {
            if (j != max && nextScores.get(j) > high) {
                high = nextScores.get(j);
                i = j;
            }
        }
        return i;
    }
    
}
