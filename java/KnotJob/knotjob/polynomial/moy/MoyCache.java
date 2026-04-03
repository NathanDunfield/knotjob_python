/*

Copyright (C) 2024 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.polynomial.moy;

import java.util.ArrayList;

/**
 *
 * @author Dirk
 */
public class MoyCache {
    
    private final ArrayList<Vertex> vertices;
    private final ArrayList<Edge> edges;
    private final ArrayList<MoyWeb> webs;
    private final ArrayList<ArrayList<ArrayList<Integer>>> webLabels;
    private final ArrayList<Integer> posEndpts;
    private final ArrayList<Integer> negEndpts;
    
    public MoyCache(ArrayList<Integer> posEnd, ArrayList<Integer> negEnd) {
        vertices = new ArrayList<Vertex>();
        edges = new ArrayList<Edge>();
        webs = new ArrayList<MoyWeb>();
        webLabels = new ArrayList<ArrayList<ArrayList<Integer>>>();
        posEndpts = posEnd;
        negEndpts = negEnd;
        for (int i : posEndpts) vertices.add(new Vertex(i, 0));
        for (int i : negEndpts) vertices.add(new Vertex(i, 1));
    }
    
    public MoyCache(int[] pos, int[] neg, int crs) {
        vertices = new ArrayList<Vertex>();
        edges = new ArrayList<Edge>();
        webs = new ArrayList<MoyWeb>();
        webLabels = new ArrayList<ArrayList<ArrayList<Integer>>>();
        posEndpts = listOf(pos);
        negEndpts = listOf(neg);
        for (int i : posEndpts) vertices.add(new Vertex(i, 0));
        for (int i : negEndpts) vertices.add(new Vertex(i, 1));
        vertices.add(new Vertex(-2*crs-1, 2));
        vertices.add(new Vertex(-2*crs-2, 2));
        edges.add(new Edge(vertices.get(0), vertices.get(2)));
        edges.add(new Edge(vertices.get(1), vertices.get(3)));
        edges.add(new Edge(vertices.get(0), vertices.get(4)));
        edges.add(new Edge(vertices.get(1), vertices.get(4)));
        edges.add(new Edge(vertices.get(4), vertices.get(5)));
        edges.add(new Edge(vertices.get(5), vertices.get(3)));
        edges.add(new Edge(vertices.get(5), vertices.get(2)));
        webs.add(new MoyWeb(new Integer[][] {}, new int[] {0, 1}));
        webs.add(new MoyWeb(new Integer[][] {new Integer[] {2, 3, 4}, new Integer[] {4, 5, 6}}, new int[] {2, 3, 4, 5, 6}));
        ArrayList<ArrayList<Integer>> fList = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> labels = new ArrayList<Integer>();
        labels.add(1);
        labels.add(1);
        fList.add(labels);
        webLabels.add(fList);
        labels = new ArrayList<Integer>();
        labels.add(1);
        labels.add(1);
        labels.add(2);
        labels.add(1);
        labels.add(1);
        fList = new ArrayList<ArrayList<Integer>>();
        fList.add(labels);
        webLabels.add(fList);
    }

    private ArrayList<Integer> listOf(int[] pos) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i : pos) list.add(i);
        return list;
    }
    
    public void output() {
        System.out.println("Positive Endpoints "+posEndpts);
        System.out.println("Negative Endpoints "+negEndpts);
        System.out.print("Vertices ");
        for (Vertex vert : vertices) System.out.print(vert+", ");
        System.out.println();
        System.out.print("Edges ");
        for (Edge ed : edges) System.out.print(ed+", ");
        System.out.println();
        System.out.println("Webs ");
        for (int i = 0; i < webs.size(); i++) {
            System.out.println(webs.get(i).output(this)+", "+webLabels.get(i));
        }
        System.out.println();
    }

    public Edge getEdge(int ed) {
        return edges.get(ed);
    }

    private int getEdgeNumber(Edge edge) {
        for (int i = 0; i < edges.size(); i++) {
            if (edge.equals(edges.get(i))) return i;
        }
        edges.add(edge);
        return edges.size()-1;
    }

    public MoyCache combineCache(MoyCache cache) {
        ArrayList<Integer> posEnd = new ArrayList<Integer>();
        ArrayList<Integer> negEnd = new ArrayList<Integer>();
        for (int u : posEndpts) posEnd.add(u);
        for (int u : negEndpts) negEnd.add(u);
        for (int u : cache.posEndpts) {
            if (negEnd.contains(u)) negEnd.remove((Integer) u);
            else posEnd.add(u);
        }
        for (int u : cache.negEndpts) {
            if (posEnd.contains(u)) posEnd.remove((Integer) u);
            else negEnd.add(u);
        }
        return new MoyCache(posEnd, negEnd);
    }

    public ArrayList<Edge> getEdgesFromWeb(int w) {
        MoyWeb web = webs.get(w);
        ArrayList<Edge> eds = new ArrayList<Edge>();
        for (int i : web.getEdges()) {
            eds.add(edges.get(i));
        }
        return eds;
    }
    
    public ArrayList<Triple<Edge>> getTriplesFromWeb(int w) {
        ArrayList<Triple<Edge>> triples = new ArrayList<Triple<Edge>>();
        MoyWeb web = webs.get(w);
        for (Triple<Integer> tr : web.getTriples()) {
            Triple<Edge> triple = new Triple<Edge>(edges.get(tr.getOne()), edges.get(tr.getTwo()), edges.get(tr.getThree()));
            triples.add(triple);
        }
        return triples;
    }
    
    ArrayList<Integer> getLabelsFromWeb(int w, int l) {
        ArrayList<Integer> labels = new ArrayList<Integer>();
        for (int u : webLabels.get(w).get(l)) labels.add(u);
        return labels;
    }

    int[] theWebFrom(ArrayList<Edge> firstEdges, ArrayList<Edge> seconEdges, 
            ArrayList<Triple<Edge>> firstTriples, ArrayList<Triple<Edge>> seconTriples,
            ArrayList<Integer> firstLabels, ArrayList<Integer> seconLabels) {
        ArrayList<Edge> finalEdges = new ArrayList<Edge>();
        ArrayList<Triple<Edge>> finalTriples = combineTriples(firstTriples, seconTriples);
        ArrayList<Integer> finalLabels = new ArrayList<Integer>();
        addEdgesAndLabels(finalEdges, finalLabels, firstEdges, firstLabels);
        addEdgesAndLabels(finalEdges, finalLabels, seconEdges, seconLabels);
        // after this, firstLabels and seconLabels only contain 1, and can be ignored.
        for (int i = 0; i < seconEdges.size(); i++) {
            Edge ed = seconEdges.get(i);
            int[] labels = ed.vertexLabels();
            Edge cStart = combinedEdgeWith(firstEdges, labels[0]);
            Edge cEnd = combinedEdgeWith(firstEdges, labels[1]);
            if (cStart == null && cEnd == null) { // no combining
                finalEdges.add(ed);
                finalLabels.add(1);
            }
            else {
                if (cStart != null) firstEdges.remove(cStart);
                if (cEnd != null) firstEdges.remove(cEnd);
                firstEdges.add(combineEdges(ed, cStart, cEnd, finalTriples));
            }
        }
        for (Edge ed : firstEdges) {
            finalEdges.add(0, ed);
            finalLabels.add(0, 1);
        }
        return labelledWebWith(finalEdges, finalTriples, finalLabels);
    }

    private ArrayList<Triple<Edge>> combineTriples(ArrayList<Triple<Edge>> fTriples,
            ArrayList<Triple<Edge>> sTriples) {
        ArrayList<Triple<Edge>> finalTriples = new ArrayList<Triple<Edge>>();
        for (Triple<Edge> tr : fTriples) finalTriples.add(tr);
        for (Triple<Edge> tr : sTriples) finalTriples.add(tr);
        return finalTriples;
    }
    
    private int[] labelledWebWith(ArrayList<Edge> edges, ArrayList<Triple<Edge>> triples, 
            ArrayList<Integer> labels) {
        int[] labWeb = new int[2];
        ArrayList<Integer> iso = isomorphismWithWeb(edges, triples);
        if (iso == null) { // need to create a new MoyWeb
            System.out.println(edges);
            for (Triple tr : triples) System.out.println(tr);
            int[] eds = new int[edges.size()];
            for (int i = 0; i < edges.size(); i++) eds[i] = getEdgeNumber(edges.get(i));
            Integer[][] trips = new Integer[triples.size()][3];
            for (int k = 0; k < triples.size(); k++) {
                Triple<Edge> tr = triples.get(k);
                trips[k][0] = eds[edges.indexOf(tr.getOne())];
                trips[k][1] = eds[edges.indexOf(tr.getTwo())];
                trips[k][2] = eds[edges.indexOf(tr.getThree())];
            }
            labWeb[0] = webs.size();
            webs.add(new MoyWeb(trips, eds));
            ArrayList<ArrayList<Integer>> newLabels = new ArrayList<ArrayList<Integer>>();
            newLabels.add(labels);
            webLabels.add(newLabels);
            return labWeb;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private ArrayList<Integer> isomorphismWithWeb(ArrayList<Edge> edges, 
            ArrayList<Triple<Edge>> triples) {
        return null;
        
        /* Still needs to be done
        
        for (int i = 0; i < webs.size(); i++) {
            ArrayList<Edge> oEdges = getEdgesFromWeb(i);
            ArrayList<Integer> iso = sameEdges(edges, oEdges);
            if (iso != null) {
                iso.add(0, i);
                return iso;
            }
        }
        // */
    }
    
    /*private ArrayList<Integer> sameEdges(ArrayList<Edge> edges, ArrayList<Edge>oEdges) { // checks if edges give the same web
        if (edges.size() != oEdges.size()) return null;
        int[] transfer = new int[edges.size()];
        
    }// */
    
    private Edge combineEdges(Edge ed, Edge cs, Edge ce, ArrayList<Triple<Edge>> triples) {
        if (cs == null) return newCombinedEdge(ed, ce, triples);
        if (ce == null) return newCombinedEdge(cs, ed, triples);
        if (cs != ce) return newCombinedEdge(cs, ce, triples);
        Vertex circleVert = new Vertex(ed.getStartVertex().theLabel(), 3);
        return new Edge(circleVert, circleVert); // It's a circle!
    }
    
    private Edge newCombinedEdge(Edge fe, Edge se, ArrayList<Triple<Edge>> triples) {
        Vertex stVert = fe.getStartVertex();
        Vertex enVert = se.getEndVertex();
        Edge newEdge = new Edge(stVert, enVert);
        for (Triple<Edge> tr : triples) {
            if (tr.contains(fe)) tr.replace(fe, newEdge);
            if (tr.contains(se)) tr.replace(se, newEdge);
        }
        return newEdge;
    }
    
    private Edge combinedEdgeWith(ArrayList<Edge> fEdges, int lab) {
        if (lab < 0) return null;
        for (Edge edge : fEdges) {
            int[] labels = edge.vertexLabels();
            if (lab == labels[0] || lab == labels[1]) return edge;
        }
        return null;
    }
    
    private void addEdgesAndLabels(ArrayList<Edge> finalEdges, ArrayList<Integer> finalLabels, 
            ArrayList<Edge> fEdges, ArrayList<Integer> fLabels) {
        for (int i = 0; i < fEdges.size(); i++) {
            Edge ed = fEdges.get(i);
            int[] ty = ed.vertexTypes();
            if (ty[0] > 1 && ty[1] > 1) {
                finalEdges.add(ed);
                fEdges.remove(i);
                finalLabels.add(fLabels.remove(i));
            }
        }
    }
    
    
    
}
