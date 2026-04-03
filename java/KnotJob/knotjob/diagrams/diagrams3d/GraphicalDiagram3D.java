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

import knotjob.diagrams.GraphicDiagram;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Dirk
 */
public class GraphicalDiagram3D extends GraphicDiagram {
    
    public final LinkDiagram3D linkDiagram;
    public PointMover3D mover;
    private final ReentrantLock reLock = new ReentrantLock();
    private double[] min;
    private double[] max;
    private final double ft = 40;
    private final double tt = 30;
    private boolean edgeVis = false;
    private double c = 0.1; // make it vary between 0.025 and 0.3
    
    public GraphicalDiagram3D(LinkDiagram3D diagram) {
        super(diagram.getColors());
        linkDiagram = diagram;
        min = minimalPosition();
        max = maximalPosition();
        for (Color col : colors) {
            showComps.add(true);
            orientComps.add(false);
        }
    }
    
    public void runOnce(int runonce) {
        mover = new PointMover3D(this, true, runonce);
        mover.start();
    }
    
    public void minimizeEng(boolean selected) {
        if (selected) {
            mover = new PointMover3D(this, false, 50);
            mover.start();
        }
        else mover.stopRunning();
    }
    
    public void stopMoving() {
        if (mover != null) mover.stopRunning();
    }
    
    public void setEdgesVisible(boolean vis) {
        edgeVis = vis;
    }
    
    public void rotateDiagram(int angle, int axis) {
        double ang = 2* Math.PI * angle/ 360;
        double[] xrow = new double[] {Math.cos(ang), -Math.sin(ang), 0};
        double[] yrow = new double[] {Math.sin(ang), Math.cos(ang), 0};
        double[] zrow = new double[] {0, 0, 1};
        if (axis == 1) { // rotation along the y axis
            xrow = new double[] {Math.cos(ang), 0, -Math.sin(ang)};
            yrow = new double[] {0, 1, 0};
            zrow = new double[] {Math.sin(ang), 0, Math.cos(ang)};
        }
        if (axis == 2) { // rotation along the x axis
            xrow = new double[] {1, 0, 0};
            yrow = new double[] {0, Math.cos(ang), -Math.sin(ang)};
            zrow = new double[] {0, Math.sin(ang), Math.cos(ang)};
        }
        for (Vertex3D vert : linkDiagram.vertices) {
            double nx = xrow[0] * vert.x + xrow[1] * vert.y + xrow[2] * vert.z;
            double ny = yrow[0] * vert.x + yrow[1] * vert.y + yrow[2] * vert.z;
            double nz = zrow[0] * vert.x + zrow[1] * vert.y + zrow[2] * vert.z;
            vert.x = nx;
            vert.y = ny;
            vert.z = nz;
        }
        max = maximalPosition();
        min = minimalPosition();
    }
    
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        sortEdges();
        double[] maxMin = maxMinZ();
        int u = 0;
        while (u < linkDiagram.edges.size()) {
            Edge3D edge = linkDiagram.edges.get(u);
            u++;
            if (showComps.get(linkDiagram.componentOfVertex(edge.fVertex))) { 
                Color fcol = linkDiagram.colorOfVertex(edge.fVertex, maxMin[0]+0.5, maxMin[1]-0.5);
                Color scol = linkDiagram.colorOfVertex(edge.sVertex, maxMin[0]+0.5, maxMin[1]-0.5);
                float xf = (float) (ft + tt * actualPos(edge.fVertex.x, true));
                float yf = (float) (ft + tt * actualPos(edge.fVertex.y, false));
                float xs = (float) (ft + tt * actualPos(edge.sVertex.x, true));
                float ys = (float) (ft + tt * actualPos(edge.sVertex.y, false));
                GradientPaint gradPaint = new GradientPaint(xf, yf, fcol, xs, ys, scol);
                g2.setPaint(gradPaint);
                if (edgeVis) {
                    GeneralPath arc = curvedPathOf(edge);
                    g2.fill(arc);
                    g2.setColor(Color.BLACK);
                    g2.draw(arc);
                }
                else {
                    GeneralPath[] arcs = curvedPathsOf(edge);
                    g2.fill(arcs[0]);
                    g2.setColor(Color.BLACK);
                    g2.draw(arcs[1]);
                    g2.draw(arcs[2]);
                }
            }
        }
    }
    
    public ArrayList<String> printCoordinates() {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("\\documentclass[border=1bp]{standalone}");
        strings.add("\\usepackage{tikz}");
        strings.add("\\newcommand{\\edge}[7]{");
        strings.add("\\definecolor{coll}{rgb}{#6}");
        strings.add("\\definecolor{colr}{rgb}{#5}");
        if (edgeVis) strings.add("\\shadedraw[box, left color = coll, right color = colr, shading angle = #7] #1 #2 #3 #4;}");
        else {
            strings.add("\\shade[left color = coll, right color = colr, shading angle = #7] #1 #2 #3 #4;");
            strings.add("\\draw[box] #1;");
            strings.add("\\draw[box] #3;}");
        }
        strings.add("\\tikzstyle{box}=[-, black, line width = 0.25pt]");
        strings.add("\\begin{document}");
        strings.add("\\begin{tikzpicture}");
        sortEdges();
        int u = 0;
        while (u < linkDiagram.edges.size()) {
            Edge3D edge = linkDiagram.edges.get(u);
            u++;
            if (showComps.get(linkDiagram.componentOfVertex(edge.fVertex))) {
                Color fcol = linkDiagram.colorOfVertex(edge.fVertex, max[2]+0.5, min[2]-0.5);
                Color scol = linkDiagram.colorOfVertex(edge.sVertex, max[2]+0.5, min[2]-0.5);
                String number5 = colorString(fcol);
                String number6 = colorString(scol);
                double xf = ft + tt * actualPos(edge.fVertex.x, true);
                double yf = ft + tt * actualPos(edge.fVertex.y, false);
                double xs = ft + tt * actualPos(edge.sVertex.x, true);
                double ys = ft + tt * actualPos(edge.sVertex.y, false);
                String number7 = angleOf(xf - xs, yf - ys);
                double[][] pts = thePoints(edge);
                String edgeString = "\\edge" + curveString(pts, 0, 1, 2, 3) + "{" +
                        curveString(pts, 4, 5) + "}" + curveString(pts, 6, 7, 8, 9) + "{" +
                        curveString(pts, 10, 11, 0)+"}" + number5 + number6 + number7;
                strings.add(edgeString);
            }
        }
        strings.add("\\end{tikzpicture}");
        strings.add("\\end{document}");
        return strings;
    }
    
    private String curveString(double[][] pts, int e, int f, int g, int h) {
        return "{"+coordString(pts, e)+curveString(pts, f, g)+coordString(pts, h)+"}";
    }
    
    private String curveString(double[][] pts, int f, int g, int h) {
        return curveString(pts, f, g)+coordString(pts, h);
    }
    
    private String curveString(double[][] pts, int f, int g) {
        return " .. controls "+coordString(pts, f)+" and "+ coordString(pts, g)+" .. ";
    }
    
    private String coordString(double[][] pts, int a) {
        float fact = 0.01f;
        float x = fact * (float) pts[a][0];
        float y = -fact * (float) pts[a][1];
        return "("+x+", "+y+")";
    }
    
    private String angleOf(double diffx, double diffy) {
        double angle = Math.acos(diffx/Math.sqrt(diffx * diffx + diffy * diffy));
        if (diffy > 0) angle = 2 * Math.PI - angle;
        float degree = 90f + (float) (360 / (2 * Math.PI) * angle);
        return "{"+degree+"}";
    }
    
    private String colorString(Color col) {
        return "{"+(col.getRed()/255f)+", "+(col.getGreen()/255f)+", "+(col.getBlue()/255f)+"}";
        
    }
    
    private double actualPos(double x, boolean xcoord) {
        double stretchX = 16 / (max[0] - min[0]);
        double stretchY = 16 / (max[1] - min[1]);
        if (xcoord) return 2 + stretchX * (x - min[0]);
        return 2 + stretchY * (x - min[1]);
    }

    private GeneralPath[] curvedPathsOf(Edge3D edge) {
        GeneralPath[] thePaths = new GeneralPath[3];
        for (int i = 0; i < 3; i++) thePaths[i] = new GeneralPath();
        double[][] pts = thePoints(edge);
        thePaths[0].moveTo(pts[0][0], pts[0][1]);
        thePaths[1].moveTo(pts[0][0], pts[0][1]);
        thePaths[0].curveTo(pts[1][0], pts[1][1], pts[2][0], pts[2][1], pts[3][0], pts[3][1]);
        thePaths[1].curveTo(pts[1][0], pts[1][1], pts[2][0], pts[2][1], pts[3][0], pts[3][1]);
        thePaths[0].curveTo(pts[4][0], pts[4][1], pts[5][0], pts[5][1], pts[6][0], pts[6][1]);
        thePaths[2].moveTo(pts[6][0], pts[6][1]);
        thePaths[0].curveTo(pts[7][0], pts[7][1], pts[8][0], pts[8][1], pts[9][0], pts[9][1]);
        thePaths[2].curveTo(pts[7][0], pts[7][1], pts[8][0], pts[8][1], pts[9][0], pts[9][1]);
        thePaths[0].curveTo(pts[10][0], pts[10][1], pts[11][0], pts[11][1], pts[0][0], pts[0][1]);
        thePaths[0].closePath();
        return thePaths;
    }
    
    private double[][] thePoints(Edge3D edge) {
        double diff = max[2]-min[2];
        if (diff < 4) diff = 4;
        if (diff > 12) diff = 12;
        double a = 3.0 - diff/6;
        double b = diff/3;
        double depthf = a + b * (edge.fVertex.z - min[2])/(max[2]-min[2]+0.001);
        double depths = a + b * (edge.sVertex.z - min[2])/(max[2]-min[2]+0.001);
        double fx = edge.fVertex.x;
        double fy = edge.fVertex.y;
        double sx = edge.sVertex.x;
        double sy = edge.sVertex.y;
        double length = Math.sqrt((fx-sx) * (fx-sx) + (fy-sy) * (fy-sy));
        double fcx = (sx - edge.fVertex.inEdge.fVertex.x);
        double fcy = (sy - edge.fVertex.inEdge.fVertex.y);
        double fcl = Math.sqrt((fcx*fcx + fcy*fcy));
        double scx = (fx - edge.sVertex.outEdge.sVertex.x);
        double scy = (fy - edge.sVertex.outEdge.sVertex.y);
        double scl = Math.sqrt((scx*scx + scy*scy));
        fcx = fcx / fcl * length / 3;
        fcy = fcy / fcl * length / 3;
        scx = scx / fcl * length / 3;
        scy = scy / fcl * length / 3;
        double[] fa = adjustment(edge.fVertex);
        double[] sa = adjustment(edge.sVertex);
        double x0 = ft + tt * actualPos(fx + depthf * c * fa[0], true);
        double y0 = ft + tt * actualPos(fy + depthf * c * fa[1], false);
        double x1 = ft + tt * actualPos(fx + depthf * c * fa[0] + fcx, true);
        double y1 = ft + tt * actualPos(fy + depthf * c * fa[1] + fcy, false);
        double x2 = ft + tt * actualPos(sx + depths * c * sa[0] + scx, true);
        double y2 = ft + tt * actualPos(sy + depths * c * sa[1] + scy, false);
        double x3 = ft + tt * actualPos(sx + depths * c * sa[0], true);
        double y3 = ft + tt * actualPos(sy + depths * c * sa[1], false);
        double x4 = ft + tt * actualPos(sx + 0.5 * depths * c * (sa[0] + (sx - fx) / length), true);
        double y4 = ft + tt * actualPos(sy + 0.5 * depths * c * (sa[1] + (sy - fy) / length), false);
        double x5 = ft + tt * actualPos(sx + 0.5 * depths * c * (-sa[0] + (sx - fx) / length), true);
        double y5 = ft + tt * actualPos(sy + 0.5 * depths * c * (-sa[1] + (sy - fy) / length), false);
        double x6 = ft + tt * actualPos(sx - depths * c * sa[0], true);
        double y6 = ft + tt * actualPos(sy - depths * c * sa[1], false);
        double x7 = ft + tt * actualPos(sx - depths * c * sa[0] + scx, true);
        double y7 = ft + tt * actualPos(sy - depths * c * sa[1] + scy, false);
        double x8 = ft + tt * actualPos(fx - depthf * c * fa[0] + fcx, true);
        double y8 = ft + tt * actualPos(fy - depthf * c * fa[1] + fcy, false);
        double x9 = ft + tt * actualPos(fx - depthf * c * fa[0], true);
        double y9 = ft + tt * actualPos(fy - depthf * c * fa[1], false);
        double xa = ft + tt * actualPos(fx - 0.5 * depthf * c * (fa[0] + (sx - fx) / length), true);
        double ya = ft + tt * actualPos(fy - 0.5 * depthf * c * (fa[1] + (sy - fy) / length), false);
        double xb = ft + tt * actualPos(fx + 0.5 * depthf * c * (fa[0] - (sx - fx) / length), true);
        double yb = ft + tt * actualPos(fy + 0.5 * depthf * c * (fa[1] - (sy - fy) / length), false);
        return new double[][] {{x0, y0}, {x1, y1}, {x2, y2}, {x3, y3}, {x4, y4}, {x5, y5},
                               {x6, y6}, {x7, y7}, {x8, y8}, {x9, y9}, {xa, ya}, {xb, yb}};
    }
    
    private double[] adjustment(Vertex3D vert) {
        double a = vert.outEdge.sVertex.x;
        double b = vert.outEdge.sVertex.y;
        double e = vert.inEdge.fVertex.x;
        double f = vert.inEdge.fVertex.y;
        double length = Math.sqrt((a-e)*(a-e) + (b-f)*(b-f));
        return new double[] { (b-f)/length, (e-a)/length};
    }
    
    private GeneralPath curvedPathOf(Edge3D edge) {
        GeneralPath thePath = new GeneralPath();
        double[][] pts = thePoints(edge);
        thePath.moveTo(pts[0][0], pts[0][1]);
        thePath.curveTo(pts[1][0], pts[1][1], pts[2][0], pts[2][1], pts[3][0], pts[3][1]);
        thePath.curveTo(pts[4][0], pts[4][1], pts[5][0], pts[5][1], pts[6][0], pts[6][1]);
        thePath.curveTo(pts[7][0], pts[7][1], pts[8][0], pts[8][1], pts[9][0], pts[9][1]);
        thePath.curveTo(pts[10][0], pts[10][1], pts[11][0], pts[11][1], pts[0][0], pts[0][1]);
        thePath.closePath();
        return thePath;
    }
    
    public void sortEdges() {
        reLock.lock();
        try {
            Collections.sort(linkDiagram.edges);
        } 
        finally {
            reLock.unlock();
        }
    }
    
    private double[] maxMinZ() {
        return new double[] {max[2], min[2]};
    }
    
    private double[] minimalPosition() {
        double[] mini = new double[3];
        mini[0] = 10000;
        mini[1] = 10000;
        mini[2] = 10000;
        for (Vertex3D vert : linkDiagram.vertices) {
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
        for (Vertex3D vert : linkDiagram.vertices) {
            if (vert.x > maxi[0]) maxi[0] = vert.x;
            if (vert.y > maxi[1]) maxi[1] = vert.y;
            if (vert.z > maxi[2]) maxi[2] = vert.z;
        }
        return maxi;
    }
    
    public void movePoints() {
        ArrayList<double[]> newPoints = new ArrayList<double[]>();
        double dist = 0.0;
        for (Vertex3D vert : linkDiagram.vertices) {
            double[] point = new double[] {0.0, 0.0, 0.0};
            for (Vertex3D svert : linkDiagram.vertices) {
                if (vert != svert) point = addPoint(point, movePoint(vert, svert));
            }
            double norm = Math.sqrt(normSquare(point[0], point[1], point[2]));
            if (norm > dist) dist = norm;
            newPoints.add(point);
        }
        double factor = 0.1 / dist;
        reLock.lock();
        try {
            for (int i = 0; i < newPoints.size(); i++) {
                Vertex3D vert = linkDiagram.vertices.get(i);
                if (movable(vert.inEdge, vert.outEdge)) {
                    vert.x = vert.x + newPoints.get(i)[0] * factor;
                    vert.y = vert.y + newPoints.get(i)[1] * factor;
                    vert.z = vert.z + newPoints.get(i)[2] * factor;
                }
            }
        }
        finally {
            reLock.unlock();
        }
        min = minimalPosition();
        max = maximalPosition();
    }
    
    private boolean movable(Edge3D edgeOne, Edge3D edgeTwo) {
        boolean move = true;
        int i = 0;
        while (i < linkDiagram.edges.size() && move) {
            Edge3D edge = linkDiagram.edges.get(i);
            if (edge != edgeOne && edge != edgeTwo) {
                if (edge.fVertex != edgeTwo.sVertex && edge.sVertex != edgeOne.fVertex) {
                    //double d = distance(edge, edgeOne);
                    //if (d < 0.5) System.out.println(d);
                    if (distance(edge, edgeOne) < 0.15 || distance(edge, edgeTwo) < 0.15) 
                        move = false;
                }
            }
            i++;
        }
        return move;
    }
    
    private double distance(Edge3D edgeOne, Edge3D edgeTwo) {
        double[] dirOne = diffVector(edgeOne.sVertex, edgeOne.fVertex);
        double[] dirTwo = diffVector(edgeTwo.sVertex, edgeTwo.fVertex);
        double eOne = normSquare(dirOne[0], dirOne[1], dirOne[2]);
        double eTwo = normSquare(dirTwo[0], dirTwo[1], dirTwo[2]);
        double e = dirOne[0] * dirTwo[0] + dirOne[1] * dirTwo[1] + dirOne[2] * dirTwo[2];
        if (eOne * eTwo - e * e < 0.01) { // practically parallel
            double distF = distance(edgeOne, edgeTwo.fVertex);
            double distS = distance(edgeOne, edgeTwo.sVertex);
            if (distF < distS) return distF;
            return distS;
        }
        double[] a = new double[] {edgeOne.fVertex.x - edgeTwo.fVertex.x, 
                                   edgeOne.fVertex.y - edgeTwo.fVertex.y,
                                   edgeOne.fVertex.z - edgeTwo.fVertex.z };
        double bOne = a[0] * dirOne[0] + a[1] * dirOne[1] + a[2] * dirOne[2];
        double bTwo = a[0] * dirTwo[0] + a[1] * dirTwo[1] + a[2] * dirTwo[2];
        double cs = eOne * eTwo - e * e;
        double tTwo = (bTwo * eOne - bOne * e) / cs;
        double tOne = tTwo * e / eOne - bOne / eOne;
        boolean outOne = (tOne > 1 | tOne < 0);
        boolean outTwo = (tTwo > 1 | tTwo < 0);
        if (!outOne && !outTwo) {
            double dst = a[0] * a[0] + a[1] * a[1] + a[2] * a[2] + 2 * tOne * bOne;
            dst = dst - 2 * tTwo * bTwo - 2 * tOne * tTwo * e;
            dst = dst + tOne * tOne * eOne + tTwo * tTwo * eTwo;
            return Math.sqrt(dst);
        }
        
        double distF = distance(edgeOne, edgeTwo.fVertex);
        double distS = distance(edgeOne, edgeTwo.sVertex);
        if (distF < distS) return distF;
        return distS;
    }
    
    private double distance(Edge3D edge, Vertex3D vert) {
        double t = optimal(edge, vert);
        double x = (1-t) * edge.fVertex.x + t * edge.sVertex.x - vert.x;
        double y = (1-t) * edge.fVertex.y + t * edge.sVertex.y - vert.y;
        double z = (1-t) * edge.fVertex.z + t * edge.sVertex.z - vert.z;
        return Math.sqrt(normSquare(x, y, z));
    }
    
    private double optimal(Edge3D edge, Vertex3D vert) {
        double[] yz = diffVector(edge.fVertex, edge.sVertex);
        double[] yx = diffVector(edge.fVertex, vert);
        double t = (yz[0] * yx[0] + yz[1] * yx[1] + yz[2] * yz[2])/ normSquare(yz[0], yz[1], yz[2]);
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        return t;
    }
    
    private double[] movePoint(Vertex3D vert, Vertex3D svert) {
        double[] move;
        if (svert == vert.inEdge.fVertex || svert == vert.outEdge.sVertex) {
            move = diffVector(svert, vert);
            double nrm = normSquare(move[0], move[1], move[2]);
            move[0] = 1 * move[0] * nrm;
            move[1] = 1 * move[1] * nrm;
            move[2] = 1 * move[2] * nrm;
        }
        else {
            move = diffVector(vert, svert);
            double nrm = normSquare(move[0], move[1], move[2]);
            double fac = 2;
            move[0] = fac * move[0] / (nrm * nrm * nrm);
            move[1] = fac * move[1] / (nrm * nrm * nrm);
            move[2] = fac * move[2] / (nrm * nrm * nrm);
        }
        return move;
    }
    
    private double[] diffVector(Vertex3D vert, Vertex3D oVert) {
        double x = vert.x - oVert.x;
        double y = vert.y - oVert.y;
        double z = vert.z - oVert.z;
        return new double[] {x, y, z};
    }
    
    private double normSquare(double x, double y, double z) {
        return (x*x + y*y + z*z);
    }
    
    private double[] addPoint(double[] pointA, double[] pointB) {
        return new double[] {pointA[0] + pointB[0], pointA[1] + pointB[1], pointA[2] + pointB[2]};
    }

    public void setThickness(double factor) {
        c = factor;
    }
    
}
