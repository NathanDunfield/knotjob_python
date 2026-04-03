/*

Copyright (C) 2021-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.frames;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;

/**
 *
 * @author Dirk
 */
public class StableGraph {
    
    private final ArrayList<Vertex> vertices;
    private final ArrayList<TEdge> tedges;
    private final ArrayList<EEdge> eedges;
    private int maxcol;
    private int maxlev;
    private final int[][] positions;
    
    public StableGraph(String verts, String ted, String eed) {
        vertices = new ArrayList<Vertex>();
        tedges = new ArrayList<TEdge>();
        eedges = new ArrayList<EEdge>();
        addVertices(verts);
        addTEdges(ted);
        addEEdges(eed);
        int n = numberOfLowestEEdges();
        positions = new int[maxcol+n][maxlev];
        placeVertices();
    }
    
    public StableGraph() {
        vertices = new ArrayList<Vertex>();
        tedges = new ArrayList<TEdge>();
        eedges = new ArrayList<EEdge>();
        positions = new int[1][1];
    }
    
    public ArrayList<String> getTikzCommands() {
        ArrayList<String> commands = new ArrayList<String>();
        int max = 0;
        for (Vertex vert : vertices) if (max < vert.position) max = vert.position;
        //if (max > 0) max--;
        double[] start = new double[] {-max*0.5, 0.25};
        for (EEdge eed : eedges) {
            double[] bPos = vertPosition(eed.bVertex, start);
            double[] tPos = vertPosition(eed.tVertex, start);
            commands.add(curvedEdge(bPos, tPos));
        }
        for (Vertex vert : vertices) {
            double[] vPos = vertPosition(vert, start);
            commands.add("\\draw[fill = black] ("+vPos[0]+", "+vPos[1]+") circle (2pt);");
        }
        for (TEdge ted : tedges) {
            double[] bPos = vertPosition(ted.bVertex, start);
            double[] tPos = vertPosition(ted.tVertex, start);
            commands.add("\\draw[thick] ("+bPos[0]+", "+bPos[1]+") -- node [right] {$\\!"+ted.val+"$} ("
                    +"("+tPos[0]+", "+tPos[1]+");");
        }
        return commands;
    }
    
    private String curvedEdge(double[] bp, double[] tp) {
        double xdiff = tp[0] - bp[0];
        if (xdiff == 0) return "\\draw[thick, color = blue] ("+bp[0]+", "+bp[1]+
                ") to [out = 105, in = 255] ("+tp[0]+", "+tp[1]+");";
        if (Math.abs(xdiff) <= 1) return "\\draw[thick, color = blue] ("+bp[0]+", "+bp[1]+
                ") -- ("+tp[0]+", "+tp[1]+");";
        double xdifff = xdiff + 2.0;
        if (xdiff < 0) xdifff = xdiff - 2.0;
        double angle = 90.0 / Math.PI * (Math.atan(2.0/xdiff)+Math.atan(2.0/xdifff));
        double outAngle = angle;
        if (angle < 0) outAngle = 180.0 + angle;
        double inAngle = 270.0 - angle;
        outAngle = Math.round(outAngle * 100)/100.0;
        inAngle = Math.round(inAngle * 100)/100.0;
        return "\\draw[thick, color = blue] ("+bp[0]+", "+bp[1]+
                ") to [out = "+outAngle+", in = "+inAngle+"] ("+tp[0]+", "+tp[1]+");";
    }
    
    private double[] vertPosition(Vertex vert, double[] start) {
        return new double[] {start[0]+1.0*vert.position, 
                start[1] - (maxlev - vert.level)};
    }
    
    public boolean containsIsolatedEta(int botDown, int botUp, int topDown, int topUp) {
        boolean found = false;
        int i = 0;
        while (!found && i < eedges.size()) {
            EEdge edge = eedges.get(i);
            Vertex bVert = edge.bVertex;
            Vertex tVert = edge.tVertex;
            found = true;
            if (numberEEdges(bVert) != 1) found = false;
            if (!found || numberEEdges(tVert) != 1) found = false;
            if (!found || wrongTorsion(bVert, botDown, true)) found = false;
            if (!found || wrongTorsion(bVert, botUp, false)) found = false;
            if (!found || wrongTorsion(tVert, topDown, true)) found = false;
            if (!found || wrongTorsion(tVert, topUp, false)) found = false;
            i++;
        }
        return found;
    }
    
    private boolean wrongTorsion(Vertex vert, int tor, boolean isTop) {
        TEdge edge = getTEdge(vert, isTop);
        if (edge == null) return tor != 0;
        return edge.val != tor;
    }
    
    private TEdge getTEdge(Vertex vert, boolean isTop) {
        int i = 0;
        while (i < tedges.size()) {
            TEdge edge = tedges.get(i);
            Vertex vt = edge.bVertex;
            if (isTop) vt = edge.tVertex;
            if (vert == vt) return edge;
            i++;
        }
        return null;
    }
    
    private int numberEEdges(Vertex vert) {
        int num = 0;
        for (EEdge edge : eedges) {
            if (edge.bVertex == vert || edge.tVertex == vert) num++;
        }
        return num;
    }
    
    private void placeVertices() {
        int st = 0;
        for (EEdge edge : eedges) {
            if (edge.bVertex.level == 0) {
                edge.bVertex.position = st;
                edge.tVertex.position = st+1;
                positions[st][0] = 1; // just means this position is occupied
                positions[st+1][2] = 1;
                TEdge ted = edge.tVertex.bEdge;
                if (ted != null) {
                    positions[st+1][1] = 1;
                    ted.bVertex.position = st+1;
                }
                st = st+2;
            }
        }
        for (TEdge edge : tedges) {
            if (edge.bVertex.position >= 0 && edge.tVertex.position < 0) { 
                edge.tVertex.position = edge.bVertex.position;
                positions[edge.tVertex.position][edge.tVertex.level] = 1;
            }
            else if (edge.tVertex.position >= 0) {
                edge.bVertex.position = edge.tVertex.position;
                positions[edge.bVertex.position][edge.bVertex.level] = 1;
            }
            else {
                int pos = nextFree(edge.bVertex.level, true);
                edge.bVertex.position = pos;
                edge.tVertex.position = pos;
                positions[pos][edge.bVertex.level] = 1;
                positions[pos][edge.tVertex.level] = 1;
            }
        }
        for (Vertex vert : vertices) {
            if (vert.position < 0) {
                int pos = nextFree(vert.level, false);
                vert.position = pos;
                positions[pos][vert.level] = 1;
            }
        }
    }
    
    private int nextFree(int lev, boolean extra) {
        boolean found = false;
        int p = 0;
        while (!found) {
            if (positions[p][lev] == 0) {
                if (!extra || positions[p][lev+1] == 0) found = true;
                else p++;
            }
            else p++;
        }
        return p;
    }
    
    private void addVertices(String verts) {
        String str = verts;
        maxcol = 0;
        maxlev = 0;
        int lev = 0;
        while (str.length()>0) {
            int a = str.indexOf(";");
            int v = Integer.parseInt(str.substring(0, a));
            for (int u = 0; u < v; u++) {
                Vertex vert = new Vertex(lev, u);
                vertices.add(vert);
            }
            if (v > maxcol) maxcol = v;
            lev++;
            if (lev > maxlev) maxlev = lev;
            str = str.substring(a+1);
        }
    }

    private void addTEdges(String ted) {
        String str = ted.substring(1);
        while (str.length()>0) {
            int a = str.indexOf(".");
            int lev = Integer.parseInt(str.substring(0, a));
            str = str.substring(a+1);
            int b = str.indexOf(".");
            int f = Integer.parseInt(str.substring(0, b));
            str = str.substring(b+1);
            int c = str.indexOf(".");
            int n = Integer.parseInt(str.substring(0, c));
            int d = str.indexOf(";");
            int t = Integer.parseInt(str.substring(c+1, d));
            Vertex bVert = findVertex(lev, f);
            Vertex tVert = findVertex(lev+1, n);
            TEdge edge = new TEdge(bVert, tVert, t);
            tVert.bEdge = edge;
            tedges.add(edge);
            str = str.substring(d+1);
        }
    }

    private void addEEdges(String eed) {
        String str = eed.substring(1);
        while (str.length()>0) {
            int a = str.indexOf(".");
            int lev = Integer.parseInt(str.substring(0, a));
            str = str.substring(a+1);
            int b = str.indexOf(".");
            int f = Integer.parseInt(str.substring(0, b));
            str = str.substring(b+1);
            int c = str.indexOf(";");
            int n = Integer.parseInt(str.substring(0, c));
            Vertex bVert = findVertex(lev, f);
            Vertex tVert = findVertex(lev+2, n);
            EEdge edge = new EEdge(bVert, tVert);
            eedges.add(edge);
            str = str.substring(c+1);
        }
    }

    private int numberOfLowestEEdges() {
        int count = 0;
        for (EEdge edge : eedges) {
            if (edge.bVertex.level == 0) count++;
        }
        return count;
    }

    private Vertex findVertex(int lev, int b) {
        boolean found = false;
        int i = 0;
        while (!found) {
            Vertex vert = vertices.get(i);
            if (vert.level == lev && vert.number == b) found = true;
            else i++;
        }
        return vertices.get(i);
    }

    public void drawEEdges(Graphics2D g2) {
        g2.setColor(Color.blue);
        g2.setStroke(new BasicStroke(4));
        for (EEdge edge : eedges) {
            int xbot = 60 + 50 * edge.bVertex.position;
            int ybot = 20 + 50 * (maxlev - edge.bVertex.level);
            int xtop = 60 + 50 * edge.tVertex.position;
            int ytop = 20 + 50 * (maxlev - edge.tVertex.level);
            int cx = (xbot+xtop-40)/2;
            int cy = (ybot+ytop)/2;
            if (Math.abs(edge.bVertex.position-edge.tVertex.position) == 1) 
                g2.drawLine(xbot, ybot, xtop, ytop);
            else {
                QuadCurve2D.Double path = new QuadCurve2D.Double(xbot, ybot, cx, cy, xtop, ytop);
                g2.draw(path);
            }
        }
    }
    
    public void drawTEdges(Graphics2D g2) {
        g2.setColor(Color.black);
        g2.setStroke(new BasicStroke(4));
        for (TEdge edge : tedges) {
            int xbot = 60 + 50 * edge.bVertex.position;
            int ybot = 20 + 50 * (maxlev - edge.bVertex.level);
            int xtop = 60 + 50 * edge.tVertex.position;
            int ytop = 20 + 50 * (maxlev - edge.tVertex.level);
            g2.drawLine(xbot, ybot, xtop, ytop);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g2.drawString(""+edge.val, xbot+3, ytop+28);
        }
    }
    
    public void drawVertices(Graphics2D g2) {
        g2.setColor(Color.black);
        for (Vertex vert : vertices) {
            int xpos = 53 + 50 * vert.position;
            int ypos = 13 + 50 * (maxlev - vert.level);
            g2.fillOval(xpos, ypos, 14, 14);
        }
    }

    public int getX() {
        return positions.length;
    }

    public int getY() {
        return maxlev;
    }
    
    private class Vertex {
        
        int level;
        int number;
        int position;
        TEdge bEdge;

        private Vertex(int lev, int u) {
            level = lev;
            number = u;
            position = -1;
            bEdge = null;
        }
        
    }
    
    private class TEdge {
        
        Vertex bVertex;
        Vertex tVertex;
        int val;

        private TEdge(Vertex bVert, Vertex tVert, int t) {
            bVertex = bVert;
            tVertex = tVert;
            val = t;
        }
        
    }
    
    private class EEdge {
        
        Vertex bVertex;
        Vertex tVertex;

        private EEdge(Vertex bVert, Vertex tVert) {
            bVertex = bVert;
            tVertex = tVert;
        }
    }
    
}
