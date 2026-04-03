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

package knotjob.diagrams.diagrams3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import knotjob.diagrams.DrawComplex;
import knotjob.diagrams.Edge;
import knotjob.diagrams.Vertex;
import knotjob.diagrams.griddiagrams.GridDiagram;
import knotjob.links.Link;

/**
 *
 * @author Dirk
 */
public class LinkDiagram3D {
    
    public final ArrayList<Vertex3D> vertices;
    public final ArrayList<Edge3D> edges;
    public final ArrayList<Integer> compStarts;
    private final ArrayList<Color> colors;
    private final int drawnComp;
    private final ArrayList<Boolean> shownComps;
    
    public LinkDiagram3D() {
        vertices = new ArrayList<Vertex3D>();
        edges = new ArrayList<Edge3D>();
        compStarts = new ArrayList<Integer>();
        colors = new ArrayList<Color>();
        drawnComp = 1;
        shownComps = new ArrayList<Boolean>(1);
    }
    
    public LinkDiagram3D(DrawComplex complex, Link link, int drawn, ArrayList<Boolean> shown) {
        vertices = new ArrayList<Vertex3D>();
        edges = new ArrayList<Edge3D>();
        compStarts = new ArrayList<Integer>();
        colors = new ArrayList<Color>();
        drawnComp = drawn;
        shownComps = shown;
        createDiagram(complex, link);
        Color[] startColors = new Color[] {Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN,
            Color.MAGENTA, Color.PINK, Color.CYAN, Color.ORANGE};
        for (int i = 0; i < compStarts.size(); i++) colors.add(startColors[i%8]);
    }

    public LinkDiagram3D(GridDiagram grphGrd, Link lnk) {
        vertices = new ArrayList<Vertex3D>();
        edges = new ArrayList<Edge3D>();
        compStarts = new ArrayList<Integer>();
        colors = new ArrayList<Color>();
        drawnComp = lnk.components();
        shownComps = new ArrayList<Boolean>(drawnComp);
        for (int i = 0; i < drawnComp; i++) shownComps.add(true);
        createDiagram(grphGrd, lnk);
        Color[] startColors = new Color[] {Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN,
            Color.MAGENTA, Color.PINK, Color.CYAN, Color.ORANGE};
        for (int i = 0; i < compStarts.size(); i++) colors.add(startColors[i%8]);
    }
    
    public Color colorOfVertex(Vertex3D vert, double max, double min) {
        int j = componentOfVertex(vert);
        
        Color col = colors.get(j);
        double z = -8 + 16 * (vert.z - min) / (max - min); // vert.z;
        //System.out.println(z);
        if (z >= 8.0) z = 8.0;
        if (z <= -8.0) z = -8.0;
        int red = colorAdaption(col.getRed(), z);
        int gre = colorAdaption(col.getGreen(), z);
        int blu = colorAdaption(col.getBlue(), z);
        return new Color(red, gre, blu);
    }
    
    public ArrayList<Color> getColors() {
        return colors;
    }
    
    protected int componentOfVertex(Vertex3D vert) {
        boolean found = false;
        int j = 0;
        int pos = vertices.indexOf(vert);
        while (!found) {
            if (j+1 == compStarts.size()) found = true;
            else {
                if (compStarts.get(j+1) > pos) found = true;
                else j++;
            }
        }
        return j;
    }
    
    private int colorAdaption(int rgb, double z) {
        z = (z+8.0)*20.0 / 16.0;
        if (rgb <= 75) {
            return ((int) (rgb + z * 6)); 
        }
        if (rgb >= 180) {
            return ((int) (rgb - (20 - z) * 6));
        }
        return ((int) (rgb + (z-10.0)*6.0 ));
    }
    
    private void createDiagram(DrawComplex complex, Link link) {
        for (int i = 0; i < link.getComponents().size(); i++) {
            int[] or = link.orientation(i);
            int[] run = new int[] {or[0], or[1]};
            boolean isTop = isTopOf(link, run);
            double z = -3.0;
            if (isTop) z = 3.0;
            int m = link.compOf(link.getPath(or[0], or[1]));
            if ((m < drawnComp) && shownComps.get(m)) {
                Vertex vert = findVertex(run[0], 0, complex);
                if (vert != null) {
                    Vertex3D stVert = new Vertex3D(vert.x, vert.y, z);
                    compStarts.add(vertices.size());
                    vertices.add(stVert);
                    runThroughComponent(link, run, or, stVert, complex);
                }
            }
        }
        adjustLastCoordinate();
        addUnlinkComponents(link.unComponents());
    }

    private boolean isTopOf(Link link, int[] or) {
        return (link.getCross(or[0]) > 0) == (or[1] % 2 != 0);
    }

    private Vertex findVertex(int lb, int tp, DrawComplex complex) {
        boolean found = false;
        int i = 0;
        while (!found && i < complex.getVertices().size()) {
            if (complex.getVertices().get(i).label == lb && 
                    complex.getVertices().get(i).type == tp) found = true;
            else i++;
        }
        if (!found) return null;
        return complex.getVertices().get(i);
    }

    private void runThroughComponent(Link link, int[] run, int[] or, Vertex3D stVert, 
            DrawComplex complex) {
        Vertex curVert = findVertex(run[0], 0, complex);
        Vertex3D curV3D = stVert;
        do {
            run[1] = (run[1]+2)%4;
            boolean found = false;
            int j = 0;
            while (!found) {
                if (curVert.comb.get(j).svert.label == link.getPath(run[0], run[1])) found = true;
                else j++;
            }
            run = nextOccur(link, run);
            Vertex nextVertex = curVert.comb.get(j).svert; // this is a vertex of type 4.
            Vertex3D nextV3D = new Vertex3D(nextVertex.x, nextVertex.y, 0);
            Edge3D edge = new Edge3D(curV3D, nextV3D);
            curV3D.outEdge = edge;
            nextV3D.inEdge = edge;
            vertices.add(nextV3D);
            edges.add(edge);
            do {
                Edge fEdge = nextVertex.comb.get(0);
                Edge sEdge = nextVertex.comb.get(1);
                if (otherVertex(fEdge, nextVertex) == curVert) {
                    curVert = nextVertex;
                    nextVertex = otherVertex(sEdge, nextVertex);
                    curV3D = nextV3D;
                    double z = 0.0;
                    if (nextVertex.type == 0) {
                        z = -3.0;
                        if (isTopOf(link, run)) z = 3.0;
                    }
                    nextV3D = new Vertex3D(nextVertex.x, nextVertex.y, z);
                    edge = new Edge3D(curV3D, nextV3D);
                    curV3D.outEdge = edge;
                    nextV3D.inEdge = edge;
                    vertices.add(nextV3D);
                    edges.add(edge);
                }
                else {
                    curVert = nextVertex;
                    nextVertex = otherVertex(fEdge, nextVertex);
                    curV3D = nextV3D;
                    double z = 0.0;
                    if (nextVertex.type == 0) {
                        z = -3.0;
                        if (isTopOf(link, run)) z = 3.0;
                    }
                    nextV3D = new Vertex3D(nextVertex.x, nextVertex.y, z);
                    edge = new Edge3D(curV3D, nextV3D);
                    curV3D.outEdge = edge;
                    nextV3D.inEdge = edge;
                    vertices.add(nextV3D);
                    edges.add(edge);
                }
            } while (nextVertex.type != 0);
            
            curVert = nextVertex;
            curV3D = nextV3D;
        } while (run[0] != or[0] || run[1] != or[1]);
        vertices.remove(vertices.size()-1);
        edges.remove(edges.size()-1);
        Vertex3D lastVert = vertices.get(vertices.size()-1);
        Edge3D ed = new Edge3D(lastVert, stVert);
        lastVert.outEdge = ed;
        stVert.inEdge = ed;
        edges.add(ed);
    }

    private Vertex otherVertex(Edge fEdge, Vertex nextVertex) {
        if (fEdge.fvert == nextVertex) return fEdge.svert;
        return fEdge.fvert;
    }

    private int[] nextOccur(Link link, int[] run) {
        boolean found = false;
        int i = 0;
        int j = 0;
        while (!found) {
            j = 0;
            while (!found && j < 4) {
                if (link.getPath(i, j) == link.getPath(run[0], run[1]) && 
                        (i != run[0] | j != run[1])) found = true;
                else j++;
            }
            if (!found) i++;
        }
        return new int[] {i, j};
    }

    private void adjustLastCoordinate() {
        for (int i = 0; i < compStarts.size(); i++) {
            int first = compStarts.get(i);
            int next;
            if (i == compStarts.size()-1) next = vertices.size();
            else next = compStarts.get(i+1);
            double curz = vertices.get(first).z;
            int j = first;
            while (j < next) {
                int k = nextzNonzero(j, first, next);
                double nextz = vertices.get(k).z;
                if (k == first) {
                    double step = (nextz - curz)/(next - j);
                    for (int u = 1; u < next - j; u++) {
                        vertices.get(j+u).z = curz + u * step;
                    }
                    j = next;
                }
                else {
                    double step = (nextz - curz)/(k - j);
                    for (int u = 1; u < k - j; u++) {
                        vertices.get(j+u).z = curz + u * step;
                    }
                    j = k;
                    curz = nextz;
                }
            }
            
        }
    }

    private int nextzNonzero(int pos, int first, int next) {
        boolean found = false;
        pos++;
        while (pos < next && !found) {
            if (vertices.get(pos).z != 0) found = true;
            else pos++;
        }
        if (found) return pos;
        else return first;
    }

    private void addUnlinkComponents(int unComponents) {
        double radius = 1.0;
        double tau = Math.PI * 2;
        int numberVert = 20;
        for (int i = 0; i < unComponents; i++) {
            Vertex3D startVert = new Vertex3D(radius+i, 0, -4.0);
            Vertex3D curVert = startVert;
            compStarts.add(vertices.size());
            vertices.add(curVert);
            for (int j = 1; j < numberVert; j++) {
                Vertex3D nexVert = new Vertex3D((radius+i)* Math.cos(tau * j / numberVert), 
                                                (radius+i)* Math.sin(tau * j / numberVert), -4.0);
                Edge3D edge = new Edge3D(curVert, nexVert);
                curVert.outEdge = edge;
                nexVert.inEdge = edge;
                curVert = nexVert;
                vertices.add(curVert);
                edges.add(edge);
            }
            Edge3D edge = new Edge3D(vertices.get(vertices.size()-1), startVert);
            vertices.get(vertices.size()-1).outEdge = edge;
            startVert.inEdge = edge;
            edges.add(edge);
        }
    }

    public ArrayList<String> printVertices() {
        ArrayList<String> strings = new ArrayList<String>();
        for (int i = 0; i < compStarts.size(); i++) {
            strings.add(String.valueOf(i));
            Color col = colors.get(i);
            strings.add(col.getRed()+", "+col.getGreen()+", "+col.getBlue());
            Vertex3D vert = vertices.get(compStarts.get(i));
            addString(vert, strings);
            Vertex3D next = vert.outEdge.sVertex;
            while (next != vert) {
                addString(next, strings);
                next = next.outEdge.sVertex;
            }
            
            
            
            /*int end = vertices.size();
            if (i < compStarts.size()-1) end = compStarts.get(i+1);
            for (int j = compStarts.get(i); j < end; j++) 
                strings.add((float)vertices.get(j).x+", "+(float)vertices.get(j).y+
                        ", "+(float)vertices.get(j).z);// */
        }
        return strings;
    }
    
    private void addString(Vertex3D vert, ArrayList<String> strings) {
        strings.add((float)vert.x+", "+(float)vert.y+
                ", "+(float)vert.z);
    }
    
    public ArrayList<String> printCoordinates() {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("camera {location <0, 0, -400> look_at <0, 0, 0>}");
        strings.add("light_source {<0, 0, -200> rgb 1}");
        strings.add("background {rgb 1}");
        strings.add("#include \"spline.mcr\"");
        strings.add("#include \"textures.inc\"");
        strings.add("#include \"colors.inc\"");
        for (int i = 1; i <= compStarts.size(); i++) {
            int size = vertices.size() - compStarts.get(i-1);
            if (i < compStarts.size()) size = size + compStarts.get(i) - vertices.size();
            strings.add("#declare comp"+i+" = create_spline ( array["+(size)+"] {");
            for (int j = 0; j < size -1; j++) {
                Vertex3D vert = vertices.get(compStarts.get(i-1)+j);
                strings.add(coordString(vert, false)+", ");
            }
            int end = vertices.size()-1;
            if (i < compStarts.size()) end = compStarts.get(i)-1;
            strings.add(coordString(vertices.get(end), true)+" }, "
                    + "create_default_spline + spline_loop (yes) + spline_tension (0))");
            strings.add("union { pipe_spline  (comp"+i+", spline_radius (6) + spline_step_size(1))");
            float red = roundDown(colors.get(i-1).getRed()/255f);
            float gre = roundDown(colors.get(i-1).getGreen()/255f);
            float blu = roundDown(colors.get(i-1).getBlue()/255f);
            strings.add("pigment {rgb <"+red+", "+gre+", "+blu+">}");
            strings.add("finish{ambient .2 diffuse .6 phong .8 phong_size 25} }");
        }
        return strings;
    }

    private String coordString(Vertex3D vert, boolean addBit) {
        float[] adjCds = adjustedCoordinates(vert);
        if (addBit) adjCds[2] = adjCds[2]+0.001f;
        
        String string = "<"+roundDown(adjCds[0])+", "+roundDown(adjCds[1])+
                ", "+roundDown(adjCds[2])+">";
        return string;
    }

    /*private String controlString(Vertex3D vert, boolean first) {
        double[] control;
        if (first) control = getControl(vert.inEdge.fVertex, vert.outEdge.sVertex);
        else control = getControl(vert.outEdge.sVertex.outEdge.sVertex, vert);
        Vertex3D nVert;
        if (first) nVert = new Vertex3D(vert.x + control[0], vert.y + control[1], 
                vert.z + control[2]);
        else nVert = new Vertex3D(vert.outEdge.sVertex.x + control[0], 
                                  vert.outEdge.sVertex.y + control[1],
                                  vert.outEdge.sVertex.z + control[2]);
        return coordString(nVert, false);
    }// */
    
    /*private double[] getControl(Vertex3D fVert, Vertex3D sVert) {
        double[] diff = new double[] {sVert.x - fVert.x, sVert.y - fVert.y, 
                                      sVert.z - fVert.z};
        return new double[] {diff[0]/ 4, diff[1]/ 4, diff[2]/ 4};
    }// */
    
    private float[] adjustedCoordinates(Vertex3D vert) {
        double[] max = maximalPosition();
        double[] min = minimalPosition();
        double[] shift = new double[] {(0.05 + max[0] - min[0])/2, (0.05 + max[1] - min[1])/2,
                                         (0.05 + max[2] - min[2])/2};
        double maxx = shift[0] * 2;
        if (shift[1] * 2 > maxx) maxx = shift[1] * 2;
        float factor = (float) (300 / maxx);
        return new float[] {factor * (float) (vert.x - min[0] - shift[0]), 
                            factor * (float) (vert.y - min[1] - shift[1]),
                            factor * (float) (vert.z - min[2] - shift[2])};
    }
    
    private double[] minimalPosition() {
        double[] mini = new double[3];
        mini[0] = 10000;
        mini[1] = 10000;
        mini[2] = 10000;
        for (Vertex3D vert : vertices) {
            if (vert.x < mini[0]) mini[0] = vert.x;
            if (vert.y < mini[1]) mini[1] = vert.y;
            if (vert.z < mini[2]) mini[2] = vert.z;
        }
        return mini;
    }
    
    private double[] maximalPosition() {
        double[] maxi = new double[3];
        maxi[0] = -10000;
        maxi[1] = -10000;
        maxi[2] = -10000;
        for (Vertex3D vert : vertices) {
            if (vert.x > maxi[0]) maxi[0] = vert.x;
            if (vert.y > maxi[1]) maxi[1] = vert.y;
            if (vert.z > maxi[2]) maxi[2] = vert.z;
        }
        return maxi;
    }

    private float roundDown(float f) {
        int fl = (int) (1000 * f);
        return fl/1000f;
    }

    public void removeVertices(double removeValue) {
        int i = vertices.size()-1;
        while (i >= 0) {
            Vertex3D vert = vertices.get(i);
            i--;
            double fx = vert.x - vert.inEdge.fVertex.x;
            double fy = vert.y - vert.inEdge.fVertex.y;
            double fz = vert.z - vert.inEdge.fVertex.z;
            double fdist = Math.sqrt(fx * fx + fy * fy + fz * fz);
            if (fdist < removeValue) {
                double sx = vert.x - vert.outEdge.sVertex.x;
                double sy = vert.y - vert.outEdge.sVertex.y;
                double sz = vert.z - vert.outEdge.fVertex.z;
                double sdist = Math.sqrt(fx * fx + fy * fy + fz * fz);
                if (sdist < removeValue) {
                    double scalar = (fx * sx + fy * sy + fz * sz) / (fdist * sdist);
                    if (scalar < -0.7) { // only remove if they point in similar direction
                        Edge3D edge = new Edge3D(vert.inEdge.fVertex, vert.outEdge.sVertex);
                        vert.inEdge.fVertex.outEdge = edge;
                        vert.outEdge.sVertex.inEdge = edge;
                        edges.remove(vert.inEdge);
                        edges.remove(vert.outEdge);
                        edges.add(edge);
                        vertices.remove(vert);
                    }
                }
            }
        }
        Collections.sort(edges);
    }

    public void subdivide(double subdivValue) {
        int i = edges.size()-1;
        while (i >= 0) {
            Edge3D edge = edges.get(i);
            i--;
            double x = edge.fVertex.x - edge.sVertex.x;
            double y = edge.fVertex.y - edge.sVertex.y;
            double z = edge.fVertex.z - edge.sVertex.z;
            if (x * x + y * y + z * z > subdivValue) {
                edges.remove(edge);
                Vertex3D mVertex = new Vertex3D(edge.sVertex.x + 0.5 * x, edge.sVertex.y + 0.5 * y, 
                                            edge.sVertex.z + 0.5 * z);
                Edge3D fEdge = new Edge3D(edge.fVertex, mVertex);
                Edge3D sEdge = new Edge3D(mVertex, edge.sVertex);
                edge.fVertex.outEdge = fEdge;
                edge.sVertex.inEdge = sEdge;
                mVertex.inEdge = fEdge;
                mVertex.outEdge = sEdge;
                edges.add(fEdge);
                edges.add(sEdge);
                vertices.add(mVertex);
            }
        }
        Collections.sort(edges);
    }
    
    private void createDiagram(GridDiagram gridDig, Link link) {
        ArrayList<Integer> used = new ArrayList<Integer>();
        while (used.size() < gridDig.size()) {
            int fx = firstUnusedX(used);
            int fxy = gridDig.getXofColumn(fx);
            used.add(fx);
            compStarts.add(vertices.size());
            Vertex3D fVert = new Vertex3D(fx, fxy, 0);
            Vertex3D cVert = fVert;
            int[] cpos = new int[] {fx, fxy};
            vertices.add(fVert);
            boolean keepgoing = true;
            boolean isX = true;
            while (keepgoing) {
                int[] npos = getNext(isX, cpos, gridDig);
                if (npos[0] == fx && npos[1] == fxy) {
                    keepgoing = false;
                    Vertex3D mVert = new Vertex3D(cpos[0], (cpos[1]+fxy)/2.0, 2.0);
                    vertices.add(mVert);
                    Edge3D fEdge = new Edge3D(cVert, mVert);
                    Edge3D sEdge = new Edge3D(mVert, fVert);
                    cVert.outEdge = fEdge;
                    mVert.inEdge = fEdge;
                    mVert.outEdge = sEdge;
                    fVert.inEdge = sEdge;
                    edges.add(fEdge);
                    edges.add(sEdge);
                }
                else {
                    double mpx = npos[0];
                    double mpy = (cpos[1]+npos[1])/2.0;
                    double mpz = 2.0;
                    if (isX) {
                        mpx = (cpos[0]+npos[0])/2.0;
                        mpy = npos[1];
                        mpz = -2.0;
                    }
                    else used.add(npos[0]);
                    Vertex3D mVert = new Vertex3D(mpx, mpy, mpz);
                    Vertex3D nVert = new Vertex3D(npos[0], npos[1], 0.0);
                    vertices.add(mVert);
                    vertices.add(nVert);
                    Edge3D fEdge = new Edge3D(cVert, mVert);
                    Edge3D sEdge = new Edge3D(mVert, nVert);
                    cVert.outEdge = fEdge;
                    mVert.inEdge = fEdge;
                    mVert.outEdge = sEdge;
                    nVert.inEdge = sEdge;
                    edges.add(fEdge);
                    edges.add(sEdge);
                    cVert = nVert;
                }
                isX = !isX;
                cpos = npos;
            }
        }
        subdivide(0.0);
    }

    private int[] getNext(boolean isX, int[] cpos, GridDiagram gridDig) {
        int[] npos = new int[2]; 
        if (isX) {
            npos[0] = gridDig.getOofRow(cpos[1]);
            npos[1] = cpos[1];
        }
        else {
            npos[0] = cpos[0];
            npos[1] = gridDig.getXofColumn(cpos[0]);
        }
        return npos;
    }
    
    private int firstUnusedX(ArrayList<Integer> used) {
        int i = 0;
        boolean found = false;
        while (!found) {
            if (!used.contains(i)) found = true;
            else i++;
        }
        return i;
    }
}
