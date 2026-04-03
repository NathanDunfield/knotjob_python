/*

Copyright (C) 2022 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.stabletype;

import java.util.ArrayList;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class ModOne<R extends Ring<R>> {
    
    private final ArrayList<FlowEdge<R>> edges;
    private final FlowGenerator<R> bGen;
    private final FlowGenerator<R> tGen;
    private boolean framedCircle; // true means there is an even number of framed circle
    
    public ModOne(FlowGenerator<R> bg, FlowGenerator<R> tg) {
        bGen = bg;
        tGen = tg;
        edges = new ArrayList<FlowEdge<R>>();
        framedCircle = true; 
    }
    
    public FlowGenerator<R> getBotGenerator() {
        return bGen;
    }
    
    public FlowGenerator<R> getTopGenerator() {
        return tGen;
    }
    
    public ArrayList<FlowEdge<R>> getEdges() {
        return edges;
    }
    
    public void setEdges(GraphStructure<R> graph) {
        framedCircle = !graph.extraCircle();
        ///if (graph.getEdges().isEmpty()) framedCircle = !framedCircle;
        for (FlowEdge<R> edge : graph.getEdges()) {
            edges.add(edge);
        }
    }
    
    public int edgeNumber() {
        return edges.size();
    }
    
    public void changeCircle() {
        framedCircle = !framedCircle;
    }
    
    public boolean extraCircle() {
        return framedCircle;
    }
    
    public void setCircle(boolean val) {
        framedCircle = val;
    }
    
    public void addEdge(FlowEdge<R> edge) {
        edges.add(edge);
    }
    
    public void removeEdge(FlowEdge<R> edge) {
        edges.remove(edge);
    }
    
    public void output(ArrayList<FlowGenerator<R>> nextLevel, ArrayList<FlowGenerator<R>> vnextLevel) {
        System.out.println("Mod One to : "+vnextLevel.indexOf(tGen)+" with "+framedCircle+" via ");
        
        for (FlowEdge<R> edge : edges) {
            System.out.println(nextLevel.indexOf(edge.firstVertex())+" [---] "
                    +nextLevel.indexOf(edge.secondVertex()));
        }
    }

    public boolean isEmpty() {
        return (edges.isEmpty() && framedCircle);
    }
    
}
