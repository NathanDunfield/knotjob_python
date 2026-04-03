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

package knotjob.homology.slthree.foam;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Dirk
 */
public class Web {
    
    private final ArrayList<Vertex> sVertices;
    private final ArrayList<Edge> edges;
    private final ArrayList<Edge[]> triples;
    private Web nOne; //
    private Web nTwo; // these webs point to webs in the next step 
    
    public Web() {
        sVertices = new ArrayList<Vertex>();
        edges = new ArrayList<Edge>();
        triples = new ArrayList<Edge[]>();
        nOne = null;
        nTwo = null;
    }
    
    public Web(Vertex[] verts, Edge[] edgs) {
        sVertices = new ArrayList<Vertex>();
        edges = new ArrayList<Edge>();
        triples = new ArrayList<Edge[]>();
        nOne = null;
        nTwo = null;
        sVertices.addAll(Arrays.asList(verts));
        edges.addAll(Arrays.asList(edgs));
    }
    
    public int singNumber() {
        return sVertices.size();
    }
    
    public void addTriple(Edge fe, Edge se, Edge te) {
        triples.add(new Edge[] {fe, se, te});
    }

    public Web getNextWeb(int k) {
        if (k == 0) return nOne;
        return nTwo;
    }
    
    public void setNextWeb(Web web, int k) {
        if (k == 0) nOne = web;
        if (k == 1) nTwo = web;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void addEdges(ArrayList<Edge> theEdges) {
        for (Edge ed : theEdges) edges.add(ed);
    }
    
    public Vertex getSingVertex(int i) {
        return sVertices.get(i);
    }
    
    public Edge[] getTriple(int i) {
        return triples.get(i);
    }

    public void addSingVertex(Vertex sv) {
        sVertices.add(sv);
    }
    
    public int getEuler() {
        int euler = 0;
        for (Edge ed : edges) euler = euler + ed.getEuler();
        return euler - 2 * sVertices.size();
    }
    
    @Override
    public String toString() {
        String name = "(";
        for (Edge edge : edges) name = name + edge+", ";
        if (name.length()-2 >= 0) name = name.substring(0, name.length()-2)+")";
        if (!triples.isEmpty()) {
            for (Edge[] es : triples) {
                name = name+"\n ["+es[0]+", "+es[1]+", "+es[2]+"]";
            }
        }
        return name;
    }

    public Edge getEdgeIncluding(Edge dEdge) {
        for (Edge edge : edges) {
            ArrayList<Integer> ed = edge.getVertexNames();
            if (ed.contains(dEdge.getStVertexName()) && ed.contains(dEdge.getEnVertexName())) {
                if (dEdge.getStVertexName() < 0 && dEdge.getEnVertexName() < 0) {
                    if (ed.size() == 2) return edge;
                }
                else return edge;
            }
        }
        return null;
    }
    
    public boolean containsEdge(Edge ed) {
        return edges.contains(ed);
    }
    
    public int edgesSize() {
        return edges.size();
    }

    public boolean containsSingName(Vertex v) {
        for (Vertex ver : sVertices) if (ver.getName() == v.getName()) return true;
        return false;
    }
    
    public int[] getSingPositions(Vertex aThis) {
        int p = sVertices.indexOf(aThis);
        if (p < 0) return null;
        Edge[] eds = triples.get(p);
        return new int[] {edges.indexOf(eds[0]), edges.indexOf(eds[1]), edges.indexOf(eds[2])};
    }

    // returns a circle, if one exists ///////////////////////////
    
    public Edge getCircle() {//returns the first circle it can find, or null
        for (Edge edge : edges) if (edge.getEuler() == 0) return edge;
        return null;
    }

    // returns a digon, if it exists /////////////////////////////
    
    public ArrayList<Edge> getDigon() {
        for (Edge[] eds : triples) {
            if (digon(eds[0], eds[1])) return digonWith(eds[0], eds[1]);
            if (digon(eds[1], eds[2])) return digonWith(eds[1], eds[2]);
            if (digon(eds[2], eds[0])) return digonWith(eds[2], eds[0]);
        }
        return null;
    }
    
    private boolean digon(Edge fEd, Edge sEd) {
        return (fEd.stVert.getName() == sEd.stVert.getName()) 
                & (fEd.enVert.getName() == sEd.enVert.getName());
    }
    
    private ArrayList<Edge> digonWith(Edge fEd, Edge sEd) {
        ArrayList<Edge> theDigon = new ArrayList<Edge>();
        theDigon.add(fEd);
        theDigon.add(sEd);
        int st = fEd.getStVertexName();
        int en = fEd.getEnVertexName();
        for (Edge ed : edges) {
            if (ed.getStVertexName() == st || ed.getEnVertexName() == en) {
                if (!theDigon.contains(ed)) theDigon.add(ed);
            }
        }
        return theDigon;
    }

    // returns a square, if one exists /////////////////////////////////
    
    public ArrayList<Edge> getSquare() {
        // to do
        int i = 0;
        while (i < sVertices.size()) {
            Vertex sv = sVertices.get(i);
            if (sv.getType() == 3) { // only check if source
                int j = i+1;
                while (j < sVertices.size()) {
                    Vertex nv = sVertices.get(j);
                    if (nv.getType() == 3) {
                        ArrayList<Edge> theSquare = getSquare(i, j);
                        if (theSquare != null) return theSquare;
                    }
                    j++;
                }
            }
            i++;
        }
        return null;
    }

    private ArrayList<Edge> getSquare(int i, int j) {
        Edge[] fEdges = triples.get(i);
        Edge[] sEdges = triples.get(j);
        for (int k = 0; k < 3; k++) {
            int l = (k+1)%3;
            Vertex fSink = fEdges[k].enVert;
            Vertex sSink = fEdges[l].enVert;
            if ((fSink.getType() == 4 && sSink.getType() == 4) && (fSink != sSink)) {
                int[] pos = containsSinks(sEdges, fSink, sSink);
                if (pos != null) 
                    return squareAt(fEdges, k, l, sEdges, pos[0], pos[1], fSink, sSink);
            }
            
        }
        return null;
    }
    
    private ArrayList<Edge> squareAt(Edge[] fEdges, int k, int l, Edge[] sEdges, 
            int m, int n, Vertex fSink, Vertex sSink) {
        ArrayList<Edge> square = new ArrayList<Edge>();
        ArrayList<Edge> rest = new ArrayList<Edge>();
        square.add(fEdges[k]);
        square.add(fEdges[l]);
        square.add(sEdges[m]);
        square.add(sEdges[n]);
        rest.add(otherEdgeThan(fEdges, square));
        rest.add(otherEdgeThan(sEdges, square));
        int u = vertexPosition(fSink, sVertices);
        int v = vertexPosition(sSink, sVertices);
        rest.add(otherEdgeThan(triples.get(u), square));
        rest.add(otherEdgeThan(triples.get(v), square));
        for (Edge ed : rest) square.add(ed);
        return square;
    }
    
    private Edge otherEdgeThan(Edge[] triple, ArrayList<Edge> square) {
        for (int i = 0; i < 3; i++) if (!square.contains(triple[i])) return triple[i];
        return null;
    }
    
    private int[] containsSinks(Edge[] edges, Vertex fSink, Vertex sSink) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (j == i) continue;
                if (edges[i].enVert.getName() == fSink.getName() 
                        && edges[j].enVert.getName() == sSink.getName()) return new int[] {i, j};
            }
        }
        return null;
    }
    
    // checking whether web is isomorphic to this, creates the isomorphism if so /////////
    
    public Foam isomorphismTo(Web web) { // if isomorphic, this foam has this at top and web at bottom
        if (web.sVertices.size() != sVertices.size()) return null;
        if (web.edges.size() != edges.size()) return null;
        ArrayList<Integer> tpnEdges = new ArrayList<Integer>();
        ArrayList<Integer> tpsEdges = new ArrayList<Integer>();
        ArrayList<Integer> tsnEdges = new ArrayList<Integer>();
        fillEdgeArrays(tpnEdges, tpsEdges, tsnEdges, edges);
        ArrayList<Integer> bpnEdges = new ArrayList<Integer>();
        ArrayList<Integer> bpsEdges = new ArrayList<Integer>();
        ArrayList<Integer> bsnEdges = new ArrayList<Integer>();
        fillEdgeArrays(bpnEdges, bpsEdges, bsnEdges, web.edges);
        if (tpnEdges.size() != bpnEdges.size() || tpsEdges.size() != bpsEdges.size() 
                || tsnEdges.size() != bsnEdges.size()) return null;
        int[] edgeIso = new int[edges.size()];
        if (!pnEdgesMatch(tpnEdges, bpnEdges, edgeIso, web)) return null;
        int[] vertIso = new int[sVertices.size()];
        if (!psEdgesMatch(tpsEdges, bpsEdges, edgeIso, vertIso, web)) return null;
        if (!snEdgesMatch(tsnEdges, bsnEdges, edgeIso, vertIso, web)) return null;
        if (!ssEdgesMatch(edgeIso, vertIso, web)) return null;
        Foam foam = new Foam(web, this);
        for (int j = 0; j < web.getEdges().size(); j++) {
            foam.addFacets(new Facet(web.getEdges().get(edgeIso[j]-1), edges.get(j), 0));
        }
        for (int j = 0; j < sVertices.size(); j++) {
            Vertex v = sVertices.get(j);
            if (v.getType() == 3) foam.addSingEdge(new SingEdge(v, web.getSingVertex(vertIso[j]-1), 1, 0));
            else foam.addSingEdge(new SingEdge(web.getSingVertex(vertIso[j]-1), v, 0, 1));
            foam.addSingFacets(this.getSingPositions(v));
        }
        return foam;
    }
    
    private void fillEdgeArrays(ArrayList<Integer> pnEdges, ArrayList<Integer> psEdges,
            ArrayList<Integer> snEdges, ArrayList<Edge> edgs) {
        for (int i = 0; i < edgs.size(); i++) {
            Edge ed = edgs.get(i);
            if (ed.stVert.getType() == 0) {
                if (ed.enVert.getType() == 1) pnEdges.add(i);
                else psEdges.add(i);
            }
            else {
                if (ed.enVert.getType() == 1) snEdges.add(i);
            }
        }
    }
    
    private boolean pnEdgesMatch(ArrayList<Integer> tpnEdges, ArrayList<Integer> bpnEdges,
            int[] edgeIso, Web web) {
        for (int k : tpnEdges) {
            Edge tEdge = edges.get(k);
            int p = edgePosition(tEdge, web, bpnEdges);
            if (p == -1) return false;
            edgeIso[k] = p+1; // we add one more than p so that edgeIso[k] == 0 means not determined yet
        }
        return true;
    }
    
    private int edgePosition(Edge tEdge, Web web, ArrayList<Integer> bpnEdges) {
        int st = tEdge.getStVertexName();
        int en = tEdge.getEnVertexName();
        for (int k : bpnEdges) {
            Edge edge = web.edges.get(k);
            if (edge.getStVertexName() == st && edge.getEnVertexName() == en) return k;
        }
        return -1;
    }
    
    private int edgePosition(int st, Web web, ArrayList<Integer> bEdges) {
        for (int k : bEdges) {
            Edge edge = web.edges.get(k);
            if (edge.getStVertexName() == st) return k;
        }
        return -1;
    }
    
    private int edgePosition(Web web, int en, ArrayList<Integer> bEdges) {
        for (int k : bEdges) {
            Edge edge = web.edges.get(k);
            if (edge.getEnVertexName() == en) return k;
        }
        return -1;
    }

    private int vertexPosition(Vertex v, ArrayList<Vertex> verts) {
        int nm = v.getName();
        for (int i = 0; i < verts.size(); i++) if (verts.get(i).getName() == nm) return i;
        return -1;
    }
    
    private boolean psEdgesMatch(ArrayList<Integer> tpsEdges, ArrayList<Integer> bpsEdges, 
            int[] edgeIso, int[] vertIso, Web web) {
        for (int k : tpsEdges) {
            Edge tEdge = edges.get(k);
            int p = edgePosition(tEdge.getStVertexName(), web, bpsEdges);
            if (p == -1) return false;
            edgeIso[k] = p+1;
            int q = vertexPosition(web.edges.get(p).enVert, web.sVertices);
            p = vertexPosition(tEdge.enVert, sVertices);
            if (vertIso[p] == 0) vertIso[p] = q+1;
            else if (vertIso[p] != q+1) return false;
        }
        return true;
    }

    private boolean snEdgesMatch(ArrayList<Integer> tsnEdges, ArrayList<Integer> bsnEdges, 
            int[] edgeIso, int[] vertIso, Web web) {
        for (int k : tsnEdges) {
            Edge tEdge = edges.get(k);
            int p = edgePosition(web, tEdge.getEnVertexName(), bsnEdges);
            if (p == -1) return false;
            edgeIso[k] = p+1;
            int q = vertexPosition(web.edges.get(p).stVert, web.sVertices);
            p = vertexPosition(tEdge.stVert, sVertices);
            if (vertIso[p] == 0) vertIso[p] = q+1;
            else if (vertIso[p] != q+1) return false;
        }
        return true;
    }

    private boolean ssEdgesMatch(//ArrayList<Integer> tssEdges, ArrayList<Integer> bssEdges, 
            int[] edgeIso, int[] vertIso, Web web) {
        boolean[][] vertCombo = getComboFrom(edgeIso, vertIso);
        while (containsZero(edgeIso)) {
            int k = notFullyDetermined(vertCombo);
            int j = firstNotDetermined(vertCombo[k]);
            int n = j-1;
            if (n == -1) {
                if (vertCombo[k][2]) n = 2;
                else {
                    n = 1;
                    j = 2;
                }
            }
            Edge bEdge = web.edges.get(edgeIso[edges.indexOf(triples.get(k)[n])]-1);
            int p = getIndexOf(web.triples.get(vertIso[k]-1), bEdge);
            if (p == -1) {
                System.out.println(triples.get(k)[n]);
                System.out.println(bEdge);
                System.out.println(this);
                System.out.println(Arrays.toString(edgeIso));
                System.out.println(Arrays.toString(vertIso));
                System.out.println(web);
            }
            ArrayList<Integer> tTrav = travelAlong(k, n, this);
            ArrayList<Integer> bTrav = travelAlong(vertIso[k]-1, p, web);
            if (tTrav.size() != bTrav.size()) return false;
            for (int i = 0; i < tTrav.size(); i = i+2) {
                int te = tTrav.get(i);
                int tv = tTrav.get(i+1);
                int be = bTrav.get(i);
                int bv = bTrav.get(i+1);
                if (edgeIso[te] == 0) {
                    edgeIso[te] = be+1;
                    setEdgeDetermined(te, vertCombo);
                }
                else if (edgeIso[te] - 1 != be) return false;
                if (tv >= 0) {
                    if (vertIso[tv] == 0) vertIso[tv] = bv+1;
                    else if (vertIso[tv] - 1 != bv) return false;
                }
                else if (bv >= 0) return false;
            }
        }
        return true;
    }
    
    private boolean[][] getComboFrom(int[] edgeIso, int[] vertIso) {
        boolean[][] combo = new boolean[vertIso.length][3];
        for (int i = 0; i < sVertices.size(); i++) {
            Edge[] trip = triples.get(i);
            for (int j = 0; j < 3; j++) {
                int p = edges.indexOf(trip[j]);
                combo[i][j] = edgeIso[p] != 0;
            }
        }
        return combo;
    }

    private boolean containsZero(int[] vertIso) {
        for (int k : vertIso) if (k == 0) return true;
        return false;
    }

    private int notFullyDetermined(boolean[][] vertCombo) {
        for (int i = 0; i < vertCombo.length; i++) {
            if (((vertCombo[i][0] && vertCombo[i][1] && vertCombo[i][2]) == false) && 
                    ((vertCombo[i][0] || vertCombo[i][1] || vertCombo[i][2]) == true)) return i;
        }
        return -1;
    }

    private int firstNotDetermined(boolean[] b) {
        for (int i = 0; i < 3; i++) {
            if (!b[i]) return i;
        }
        return -1;
    }

    private int getIndexOf(Edge[] edgs, Edge bEdge) {
        for (int i = 0; i < 3; i++) {
            if (edgs[i] == bEdge) return i;
        }
        return -1;
    }

    private ArrayList<Integer> travelAlong(int k, int j, Web aThis) { 
        ArrayList<Integer> travel = new ArrayList<Integer>();
        Vertex v = aThis.sVertices.get(k);
        Edge ed = aThis.triples.get(k)[j];//System.out.println(v+" "+ed);
        travel.add(aThis.edges.indexOf(ed));
        int inc = 1;//v.getType()-2; // 1 for a source, 2 for a sink
        //System.out.println("Inc "+inc);
        int r = k;
        while (r >= 0) {
            travel.add(r);
            j = (getIndexOf(aThis.triples.get(r), ed)+inc)%3;
            inc = 3 - inc;
            ed = aThis.triples.get(r)[j];
            travel.add(aThis.edges.indexOf(ed));
            //if (inc == 2) v = ed.enVert;
            //else v = ed.stVert;
            if (v.getName() == ed.getStVertexName()) v = ed.enVert;
            else v = ed.stVert;
            r = vertexPosition(v, aThis.sVertices);//.indexOf(v);
            if (r == k) break;
        }
        travel.add(r);
        return travel;
    }

    private void setEdgeDetermined(int te, boolean[][] vertCombo) {
        Edge e = edges.get(te);
        int tv = vertexPosition(e.stVert, sVertices);//.indexOf(e.stVert);System.out.println(e+" "+tv);
        if (tv >= 0) {
            int j = getIndexOf(triples.get(tv), e);
            vertCombo[tv][j] = true;
        }
        tv = vertexPosition(e.enVert, sVertices);//sVertices.indexOf(e.enVert);System.out.println(tv);
        if (tv >= 0) {
            int j = getIndexOf(triples.get(tv), e);
            vertCombo[tv][j] = true;
        }
    }
    
    // end of checking for isomorphic web ///////////////////////
    
}
