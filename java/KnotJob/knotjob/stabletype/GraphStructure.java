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
public class GraphStructure<R extends Ring<R>> {
    
    private final ArrayList<FlowGenerator<R>> vertices;
    private final ArrayList<FlowEdge<R>> edges;
    private boolean extraCircle;
    
    /*public GraphStructure(FlowVertex<R> fvert, FlowVertex<R> svert, R val, R uf) {
        vertices = new ArrayList<FlowGenerator<R>>();
        edges = new ArrayList<FlowEdge<R>>();
        vertices.add(fvert);
        vertices.add(svert);
        edges.add(new FlowEdge<R>(fvert, svert, val));
        unitFour = uf;
    }
    
    public GraphStructure(FlowVertex<R> fvert, FlowVertex<R> svert, R val,
            FlowVertex<R> tvert, FlowVertex<R> vvert, R sval, R uf) {
        vertices = new ArrayList<FlowVertex<R>>();
        edges = new ArrayList<FlowEdge<R>>();
        vertices.add(fvert);
        vertices.add(svert);
        vertices.add(tvert);
        vertices.add(vvert);
        edges.add(new FlowEdge<R>(fvert, svert, val));
        edges.add(new FlowEdge<R>(tvert, vvert, sval));
        unitFour = uf;
    }// */
    
    public GraphStructure(ArrayList<FlowEdge<R>> edgs, boolean ec) {
        vertices = new ArrayList<FlowGenerator<R>>();
        edges = new ArrayList<FlowEdge<R>>();
        for (FlowEdge<R> edge : edgs) {
            vertices.add(edge.firstVertex());
            vertices.add(edge.secondVertex());
            edges.add(edge);
        }
        extraCircle = ec;
    }
    
    public ArrayList<FlowEdge<R>> getEdges() {
        return edges;
    }
    
    public boolean extraCircle() {
        return extraCircle;
    }
    
    public void changeCircle() {
        extraCircle = !extraCircle;
    }
    
}
