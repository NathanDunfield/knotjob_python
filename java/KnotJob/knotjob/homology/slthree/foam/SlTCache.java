/*

Copyright (C) 2023-24 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import java.util.Objects;

/**
 *
 * @author Dirk
 */
public class SlTCache {
    
    protected final ArrayList<Integer> posEndpts;
    protected final ArrayList<Integer> negEndpts;
    protected final ArrayList<Integer> sources;
    protected final ArrayList<Integer> sinks;
    protected final ArrayList<Edge> edges;
    protected final ArrayList<Vertex> vertices;
    protected final ArrayList<ArrayList<Web>> webs;
    
    public SlTCache() {
        edges = new ArrayList<Edge>();
        vertices = new ArrayList<Vertex>();
        webs = new ArrayList<ArrayList<Web>>();
        posEndpts = new ArrayList<Integer>();
        negEndpts = new ArrayList<Integer>();
        sources = new ArrayList<Integer>();
        sinks = new ArrayList<Integer>();
    }
    
    public SlTCache(int[] pts, int sn, int sr) {
        edges = new ArrayList<Edge>();
        vertices = new ArrayList<Vertex>();
        webs = new ArrayList<ArrayList<Web>>();
        posEndpts = new ArrayList<Integer>();
        negEndpts = new ArrayList<Integer>();
        sources = new ArrayList<Integer>();
        sinks = new ArrayList<Integer>();
        posEndpts.add(pts[0]);
        posEndpts.add(pts[1]);
        negEndpts.add(pts[2]);
        negEndpts.add(pts[3]);
        sinks.add(sn);
        sources.add(sr);
        addVertices(null, false);
    }
    
    public SlTCache(int pe, int ne) {
        edges = new ArrayList<Edge>();
        vertices = new ArrayList<Vertex>();
        webs = new ArrayList<ArrayList<Web>>();
        posEndpts = new ArrayList<Integer>();
        negEndpts = new ArrayList<Integer>();
        sources = new ArrayList<Integer>();
        sinks = new ArrayList<Integer>();
        posEndpts.add(pe);
        negEndpts.add(ne);
        addVertices(null, false);
    }
    
    public SlTCache(SlTCache fCache, SlTCache sCache, boolean ib) {
        edges = new ArrayList<Edge>();
        vertices = new ArrayList<Vertex>();
        webs = new ArrayList<ArrayList<Web>>();
        posEndpts = new ArrayList<Integer>();
        negEndpts = new ArrayList<Integer>();
        sources = new ArrayList<Integer>();
        sinks = new ArrayList<Integer>();
        ArrayList<Integer> inBet = new ArrayList<Integer>();
        for (Integer i : fCache.posEndpts) posEndpts.add(i);
        for (Integer i : fCache.negEndpts) negEndpts.add(i);
        for (Integer i : sCache.posEndpts) {
            if (negEndpts.contains(i)) {
                negEndpts.remove(i);
                inBet.add(i);
            }
            else posEndpts.add(i);
        }
        for (Integer i : sCache.negEndpts) {
            if (posEndpts.contains(i)) {
                posEndpts.remove(i);
                inBet.add(i);
            }
            else negEndpts.add(i);
        }
        for (Integer i : fCache.sources) sources.add(i);
        for (Integer i : sCache.sources) sources.add(i);
        for (Integer i : fCache.sinks) sinks.add(i);
        for (Integer i : sCache.sinks) sinks.add(i);
        addVertices(inBet, ib);
    }
    
    public void output() {
        System.out.println("Positive Endpts "+posEndpts);
        System.out.println("Negative Endpts "+negEndpts);
        System.out.println("Sources         "+sources);
        System.out.println("Sinks           "+sinks);
        System.out.println("Edges : "+edges);
        System.out.println("Verts : "+vertices);
        System.out.println("Webs :");
        for (int i = 0; i < webs.size(); i++) {
            for (int j = 0; j < webs.get(i).size(); j++) {
            System.out.println("Web "+webs.get(i).get(j));
            }
            //webs.get(i).output();
        }
    }

    /*public ArrayList<Integer> totalWebNumber() {
        ArrayList<Integer> w = new ArrayList<Integer>();
        for (ArrayList<Web> wbs : webs) w.add(wbs.size());
        return w;
    }// */
    
    public void addWeb(Web web) {
        int n = web.singNumber()/2;
        while (webs.size() <= n) webs.add(new ArrayList<Web>());
        webs.get(n).add(web);
    }

    public void addVertex(Vertex vertex) {
        vertices.add(vertex);
    }

    public Vertex getVertex(int i) {
        return vertices.get(i);
    }

    public void addEdge(Edge ed) {
        edges.add(ed);
    }
    
    public void addEdge(int st, int en) {
        edges.add(new Edge(vertices.get(st), vertices.get(en)));
    }

    public Edge getEdge(int i) {
        return edges.get(i);
    }
    
    public Web getWeb(int i, int j) {
        return webs.get(i).get(j);
    }

    private void addVertices(ArrayList<Integer> inBet, boolean ib) {
        for (Integer i : posEndpts) addVertex(new Vertex(i, 0));
        for (Integer i : negEndpts) addVertex(new Vertex(i, 1));
        for (Integer i : sources) addVertex(new Vertex(i, 3));
        for (Integer i : sinks) addVertex(new Vertex(i, 4));
        if (ib) for (Integer i : inBet) addVertex(new Vertex(i, 2));
    }

    public ArrayList<Integer> lastEndpoints() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i : posEndpts) list.add(i);
        for (int i : negEndpts) list.add(i);
        return list;
    }
    
    public boolean negEndptsContains(int p) {
        return negEndpts.contains(p);
    }

    public boolean posEndptsContains(int p) {
        return posEndpts.contains(p);
    }

    public Web newWeb(Web fWeb, Web sWeb) {
        Web nWeb = new Web();
        ArrayList<ArrayList<Integer>> edgs = new ArrayList<ArrayList<Integer>>();
        addEdges(edgs, fWeb);
        addEdges(edgs, sWeb);
        combineEdges(edgs);
        ArrayList<Edge> theEdges = getEdgesFrom(edgs);
        nWeb.addEdges(theEdges);
        addSingularVertices(nWeb, fWeb);
        addSingularVertices(nWeb, sWeb);
        addWeb(nWeb);
        return nWeb;
    }
    
    private void addSingularVertices(Web nWeb, Web fWeb) {
        for (int i = 0; i < fWeb.singNumber(); i++) {
            Vertex sVert = fWeb.getSingVertex(i);
            nWeb.addSingVertex(sVert);
            Edge[] triple = fWeb.getTriple(i);
            Edge fEdge = getEdgeWith(triple[0].getStVertexName(), triple[0].getEnVertexName(), nWeb.getEdges());
            Edge sEdge = getEdgeWith(triple[1].getStVertexName(), triple[1].getEnVertexName(), nWeb.getEdges());
            Edge tEdge = getEdgeWith(triple[2].getStVertexName(), triple[2].getEnVertexName(), nWeb.getEdges());
            nWeb.addTriple(fEdge, sEdge, tEdge);
        } 
    }

    private Edge getEdgeWith(int f, int s, ArrayList<Edge> edgs) {
        for (Edge ed : edgs) {
            ArrayList<Integer> e = ed.getVertexNames();
            if (e.contains(f) && e.contains(s)) {
                if (f < 0 && s < 0) {
                    if (e.size() == 2) return ed;
                }
                else return ed;
            }
        }
        return null;
    }
    
    private void addEdges(ArrayList<ArrayList<Integer>> edgs, Web web) {
        for (Edge ed : web.getEdges()) {
            ArrayList<Integer> ned = new ArrayList<Integer>();
            ned.add(ed.getStVertexName());
            ned.add(ed.getEnVertexName());
            edgs.add(ned);
        }
    }

    private void combineEdges(ArrayList<ArrayList<Integer>> edgs) {
        int i = edgs.size()-1;
        while (i >= 0) {
            ArrayList<Integer> ed = edgs.get(i);
            int e = ed.get(0);
            if (e >= 0 && !posEndpts.contains(e)) {
                int j = edgs.size()-1;
                boolean found = false;
                while (!found) {
                    ArrayList<Integer> ned = edgs.get(j);
                    if (ned.get(ned.size()-1) == e) {
                        found = true;
                        if (i == j) break;
                        for (int k = 1; k < ed.size(); k++) ned.add(ed.get(k));
                        edgs.remove(ed);
                    }
                    j--;
                }
            }
            i--;
        }
        i = edgs.size()-1;
        while (i >= 0) {
            ArrayList<Integer> ed = edgs.get(i);
            int e = ed.get(ed.size()-1);
            if (e >= 0 && !negEndpts.contains(e)) {
                int j = edgs.size()-1;
                boolean found = false;
                while (!found) {
                    ArrayList<Integer> ned = edgs.get(j);
                    if (ned.get(0) == e) {
                        found = true;
                        if (i == j) break;
                        for (int k = ed.size()-2; k >= 0; k--) ned.add(0, ed.get(k));
                        edgs.remove(ed);
                    }
                    j--;
                }
            }
            i--;
        }
    }

    private ArrayList<Edge> getEdgesFrom(ArrayList<ArrayList<Integer>> edgs) {
        ArrayList<Edge> theEdges = new ArrayList<Edge>();
        for (ArrayList<Integer> ed : edgs) {
            theEdges.add(theEdgeWith(ed));
        }
        return theEdges;
    }
    
    private Edge theEdgeWith(ArrayList<Integer> edge) {
        int i = 0;
        while (i < edges.size()) {
            Edge ed = edges.get(i);
            if (ed.getStVertexName() == edge.get(0) && 
                    ed.getEnVertexName() == edge.get(edge.size()-1)) {
                ArrayList<Integer> cmp = ed.getVertexNames();
                if (sameArray(edge, cmp)) return ed;
            }
            i++;
        }
        ArrayList<Vertex> verts = new ArrayList<Vertex>();
        for (int n : edge) verts.add(getVertexWithName(n));
        Edge nEdge = new Edge(verts.get(0), verts.get(verts.size()-1));
        verts.remove(0);
        verts.remove(verts.size()-1);
        nEdge.addMiddle(verts);
        edges.add(nEdge);
        return nEdge;
    }

    private Vertex getVertexWithName(int n) {
        for (Vertex v : vertices) if (v.getName() == n) return v;
        return null;
    }
    
    private Edge getEdgeWithName(int st, int en) {
        for (Edge e : edges) if (e.getStVertexName() == st && e.getEnVertexName() == en) return e;
        Edge edge = new Edge(getVertexWithName(st), getVertexWithName(en));
        edges.add(edge);
        return edge;
    }
    
    private boolean sameArray(ArrayList<Integer> edge, ArrayList<Integer> cmp) {
        if (edge.size() != cmp.size()) return false;
        for (int i = 0; i < edge.size(); i++) 
            if (!Objects.equals(edge.get(i), cmp.get(i))) return false;
        return true;
    }

    public SingEdge getSingularEdge(SingEdge edge) {
        int stV = edge.getStVertexName();
        int enV = edge.getEnVertexName();
        int stL = edge.getStLevel();
        int enL = edge.getEnLevel();
        for (int i = 0; i < edges.size(); i++) {
            Edge ed = edges.get(i);
            if (ed.isSingular()) {
                SingEdge edg = (SingEdge) ed;
                if (stV == edg.getStVertexName() && enV == edg.getEnVertexName() &&
                        stL == edg.getStLevel() && enL == edg.getEnLevel()) return edg;
            }
        }
        SingEdge edg = new SingEdge(getVertexWithName(stV), getVertexWithName(enV), stL, enL);
        edges.add(edg);
        return edg;
    }

    public Foam getWebLike(Web web) {
        int i = web.singNumber()/2;
        Foam foam = findIsomorphicWeb(web, i);
        if (foam != null) return foam; 
        ArrayList<Edge> newEdges = new ArrayList<Edge>(); // we need to create a new web now
        for (Edge edge : web.getEdges()) {
            newEdges.add(getEdgeWithName(edge.getStVertexName(), edge.getEnVertexName()));
        }
        Web nWeb = new Web();
        nWeb.addEdges(newEdges);
        addSingularVertices(nWeb, web);
        webs.get(i).add(nWeb);
        foam = new Foam(web, nWeb);
        for (int j = 0; j < web.getEdges().size(); j++) {
            foam.addFacets(new Facet(web.getEdges().get(j), nWeb.getEdges().get(j), 0));
        }
        for (int j = 0; j < web.singNumber(); j++) {
            Vertex v = web.getSingVertex(j);
            if (v.getType() == 3) foam.addSingEdge(new SingEdge(nWeb.getSingVertex(j), v, 1, 0));
            else foam.addSingEdge(new SingEdge(v, nWeb.getSingVertex(j), 0, 1));
            foam.addSingFacets(web.getSingPositions(v));
        }
        return foam;
    }
    
    private Foam findIsomorphicWeb(Web web, int i) {
        while (webs.size() <= i) webs.add(new ArrayList<Web>());
        for (Web iWeb : webs.get(i)) {
            Foam foam = iWeb.isomorphismTo(web);
            if (foam != null) return foam;
        }
        return null;
    }

    public String webSizes() {
        String sizes = "Size ";
        for (ArrayList<Web> wbs : webs) sizes = sizes+" "+wbs.size();
        return sizes;
    }
    
}
