/*

Copyright (C) 2020 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.diagrams;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.util.ArrayList;
import knotjob.links.Link;

/**
 *
 * @author Dirk
 */
public class GraphicalDiagram extends GraphicDiagram {
    
    public double factorx;
    public double factory;
    public double maxx;
    public double maxy;
    protected final int originx;
    protected final int originy;
    protected final ArrayList<DrawComplex> theComplexes;
    private final Link theLink;
    public final int drawnComp;
    private PointMover mover;
    private final ArrayList<double[]> min;
    private final ArrayList<double[]> max;
    
    public GraphicalDiagram(SComplex[] complexes, double facx, double facy, Link link, int dc, 
            double fac, boolean cp, boolean grid, ArrayList<Color> clrs,
            ArrayList<Boolean> oCs, ArrayList<Boolean> sCs) {
        super(clrs);
        originx = 50;
        originy = 30;
        theLink = link;
        mover = null;
        drawnComp = dc;
        theComplexes = new ArrayList<DrawComplex>();
        for (SComplex complex : complexes) {
            if (grid) theComplexes.add(new DrawComplex(complex));
            else theComplexes.add(new DrawComplex(complex, fac));
        }
        for (int i = 0; i < link.unComponents(); i++) theComplexes.add(unKnotComplex(i));
        int sq = theComplexes.size();
        int a = (int) Math.sqrt(sq);
        int b = a;
        boolean notthere = true;
        while (notthere) {
            if (a * b >= sq) notthere = false;
            else {
                if ((a+1) * b > a * (b+1)) b++;
                else a++;
            }
        }
        maxx = (20.0 * a);
        maxy = (20.0 * b);
        min = new ArrayList<double[]>();
        max = new ArrayList<double[]>();
        for (int i = 0; i < theComplexes.size(); i++) {
            min.add(minimalPosition(i));
            max.add(maximalPosition(i));
        }
        if (clrs == null) {
            //colors = new ArrayList<Color>();
            int un = link.unComponents();
            colors.add(Color.BLACK);
            for (int j = 1; j <= dc+un-1; j++) {
                int factor = 50 + (j * 150)/(dc+un-1) ;
                colors.add(new Color(factor+ 256 * factor + 256 * 256 * factor));
            }
        }
        //else colors = clrs;
        //showComps = new ArrayList<Boolean>();
        //orientComps = new ArrayList<Boolean>();
        for (int i = 0; i < colors.size(); i++) {
            if (sCs != null) showComps.add(sCs.get(i));
            else showComps.add(true);
            if (oCs != null) orientComps.add(oCs.get(i));
            else orientComps.add(false);
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setBackground(Color.WHITE);
        for (int cp = 0; cp < theComplexes.size(); cp++) {
            DrawComplex theComplex = theComplexes.get(cp);
            for (Vertex vert : theComplex.vertices) {
                if (vert.type  == 3) {
                    int x = originx + (int) (factorx * (actualPos(vert.x, true, cp)));
                    int y = originy + (int) (factory * (actualPos(vert.y, false, cp)));
                    int i = drawnComp+vert.label;
                    if (showComps.get(i)) {
                        g2.setColor(colors.get(i));
                        g2.setStroke(new BasicStroke(3));
                        g2.drawOval(x- (int) (8 * factorx), y- (int) (8 * factory), 
                                (int) (16 * factorx), (int) (16 * factory));
                        if (orientComps.get(i)) {
                            int[] xPoints = new int[3];
                            int[] yPoints = new int[3];
                            xPoints[1] = x + (int) (8 * factorx);
                            yPoints[1] = y;
                            xPoints[0] = xPoints[1] - (int) (factorx * 0.35);
                            yPoints[0] = yPoints[1] + (int) (factory * 0.5);
                            xPoints[2] = xPoints[0] + (int) (factorx * 0.7);
                            yPoints[2] = yPoints[0];
                            g.drawPolyline(xPoints, yPoints, 3);
                        }
                    }
                }
                if (vert.type == 0) {
                    Vertex vert0 = getCoordVertex(vert.comb, vert.label, 0);
                    Vertex vert1 = getCoordVertex(vert.comb, vert.label, 1);
                    Vertex vert2 = getCoordVertex(vert.comb, vert.label, 2);
                    Vertex vert3 = getCoordVertex(vert.comb, vert.label, 3);
                    double[] cp1 = controlPoint(vert0, vert);
                    double[] cp2 = controlPoint(vert2, vert);
                    double[] cp3 = controlPoint(vert1, vert);
                    double[] cp4 = controlPoint(vert3, vert);
                    double[] oneLot = new double[] {vert0.x, vert0.y, 
                        cp1[0], cp1[1], cp2[0], cp2[1], vert2.x, vert2.y};
                    double[] twoLot = new double[] {vert1.x, vert1.y, 
                        cp3[0], cp3[1], cp4[0], cp4[1], vert3.x, vert3.y};
                    double[] fl;
                    double[] sl;
                    int mOne = theLink.compOf(vert0.label);
                    int mTwo = theLink.compOf(vert1.label);
                    boolean drawFirst = (mOne < drawnComp) && showComps.get(mOne);
                    boolean drawSecon = (mTwo < drawnComp) && showComps.get(mTwo);
                    boolean flipCol = false;
                    if (theLink.getCross(vert.label) != 1) {
                        fl = oneLot;
                        sl = twoLot;
                    }
                    else {
                        fl = twoLot;
                        sl = oneLot;
                        boolean h = drawFirst;
                        drawFirst = drawSecon;
                        drawSecon = h;
                        flipCol = true;
                    }
                    if (drawFirst) {
                        if (flipCol) g2.setColor(colors.get(mTwo));
                        else g2.setColor(colors.get(mOne));
                        g2.setStroke(new BasicStroke(3));
                        drawBezier(fl[0], fl[1], fl[2], fl[3], fl[4], fl[5], fl[6], fl[7], g2, cp);
                    }
                    if (drawSecon) {
                        g2.setStroke(new BasicStroke(11));
                        g2.setColor(g2.getBackground());
                        drawBezier(sl[0], sl[1], sl[2], sl[3], sl[4], sl[5], sl[6], sl[7], g2, cp);
                        if (flipCol) g2.setColor(colors.get(mOne));
                        else g2.setColor(colors.get(mTwo));
                        g2.setStroke(new BasicStroke(3));
                        drawBezier(sl[0], sl[1], sl[2], sl[3], sl[4], sl[5], sl[6], sl[7], g2, cp);
                    }
                }
                if (vert.type == 1) {
                    Vertex avert = vert.comb.get(0).svert;
                    Vertex bvert = vert.comb.get(1).svert;
                    double x0 = avert.x;
                    double y0 = avert.y;
                    double[] cp1 = controlPoint(avert, vert);
                    double[] cp2 = controlPoint(bvert, vert);
                    double x3 = bvert.x;
                    double y3 = bvert.y;
                    int m = theLink.compOf(vert.label);
                    if (m < drawnComp && showComps.get(m)) {
                        g2.setColor(colors.get(m));
                        g2.setStroke(new BasicStroke(3));
                        drawBezier(x0, y0, cp1[0], cp1[1], cp2[0], cp2[1], x3, y3, g2, cp);
                        //drawNumber(cp1[0], cp1[1], cp2[0], cp2[1], vert.label, g2, cp);
                    }
                }
            }
        }
        for (int i = 0; i < drawnComp; i++) if (orientComps.get(i) && showComps.get(i)) 
            drawOrientation(i,g2);  
    }
    
    private void drawOrientation(int i, Graphics2D g) {
        int[] orient = theLink.orientation(i);
        g.setColor(colors.get(i));
        g.setStroke(new BasicStroke(3));
        Vertex vert0 = getVertex(orient[0],0);
        int cp = getComponent(vert0);
        Vertex vert1 = getVertex(vert0, orient[1]);
        double[] xx = new double[3];
        double[] yy = new double[3];
        xx[1] = actualPos(vert1.x, true, cp);
        yy[1] = actualPos(vert1.y, false, cp);
        xx[0] = xx[1] - 0.5;
        yy[0] = yy[1] - 0.35;
        xx[2] = xx[1] - 0.5;
        yy[2] = yy[0] + 0.7;
        rotate(xx, yy, vert0.x - vert1.x, vert0.y - vert1.y);
        int[] x = integerArray(originx, factorx, xx);
        int[] y = integerArray(originy, factory, yy);
        g.drawPolyline(x, y, 3);
    }
    
    private void rotate(double[] x, double[] y, double cs, double si) {
        double l = Math.sqrt(cs*cs+si*si);
        double c = cs / l;
        double s = si / l;
        double nx = c * (x[0]-x[1]) - s * (y[0]-y[1]);
        double ny = s * (x[0]-x[1]) + c * (y[0]-y[1]);
        x[0] = nx + x[1];
        y[0] = ny + y[1];
        nx = c * (x[2]-x[1]) - s * (y[2]-y[1]);
        ny = s * (x[2]-x[1]) + c * (y[2]-y[1]);
        x[2] = nx + x[1];
        y[2] = ny + y[1];
    }
    
    private int[] integerArray(int orig, double fac, double[] ar) {
        int[] in = new int[ar.length];
        for (int i = 0; i < ar.length; i++) {
            in[i] = orig + (int) (fac * ar[i]);
        }
        return in;
    }
    
    private int getComponent(Vertex vert) {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (theComplexes.get(i).vertices.contains(vert)) found = true;
            else i++;
        }
        return i;
    }
    
    private Vertex getVertex(Vertex vert, int or) {
        boolean found = false;
        int lab = theLink.getPath(vert.label, or);
        int i = 0;
        while (!found) {
            if (vert.comb.get(i).svert.label == lab) found = true;
            else i++;
        }
        return vert.comb.get(i).svert;
    }
    
    private Vertex getVertex(int la, int ty) {
        boolean found = false;
        int i = 0;
        int j = 0;
        while (!found) {
            if (theComplexes.get(j).vertices.get(i).type == ty && 
                    theComplexes.get(j).vertices.get(i).label == la) found = true;
            else i++;
            if (i >= theComplexes.get(j).vertices.size()) {
                j++;
                i = 0;
            }
        }
        return theComplexes.get(j).vertices.get(i);
    }
    
    private double[] controlPoint(Vertex fvert, Vertex vert) {
        double[] cp = new double[2];
        Vertex overt = otherVertex(fvert, vert);
        double r1 = Math.sqrt(normSquare(fvert.x - vert.x, fvert.y - vert.y));
        double rt = r1 + Math.sqrt(normSquare(fvert.x - overt.x, fvert.y - overt.y));
        cp[0] = fvert.x + 0.75 * r1/rt * (vert.x - overt.x);
        cp[1] = fvert.y + 0.75 * r1/rt * (vert.y - overt.y);
        return cp;
    }
    
    private Vertex otherVertex(Vertex fvert, Vertex vert) {
        if (fvert.comb.get(0).fvert == vert) return fvert.comb.get(1).fvert;
        return fvert.comb.get(0).fvert;
    }
    
    private Vertex getCoordVertex(ArrayList<Edge> edges, int label, int pos) {
        boolean found = false;
        int j = 0;
        while (!found) {
            if (edges.get(j).svert.label == theLink.getPath(label, pos)) found = true;
            else j++;
        }
        return edges.get(j).svert;
    }
    
    private void drawNumber(double x1, double y1, double x2, double y2, int nm, 
            Graphics2D g, int cp) {
        float x0 = (float) (originx + factorx * actualPos((x1+x2)/2, true, cp));
        float y0 = (float) (originy + factory * actualPos((y1+y2)/2, false, cp));
        g.drawString(""+nm, x0, y0);
    }
    
    private void drawBezier(double x0, double y0, double x1, double y1, double x2, double y2,
            double x3, double y3, Graphics2D g, int cp) {
        x0 = originx + factorx * actualPos(x0, true, cp);
        y0 = originy + factory * actualPos(y0, false, cp);
        x1 = originx + factorx * actualPos(x1, true, cp);
        y1 = originy + factory * actualPos(y1, false, cp);
        x2 = originx + factorx * actualPos(x2, true, cp);
        y2 = originy + factory * actualPos(y2, false, cp);
        x3 = originx + factorx * actualPos(x3, true, cp);
        y3 = originy + factory * actualPos(y3, false, cp);
        CubicCurve2D curve = new CubicCurve2D.Double(x0, y0, x1, y1, x2, y2, x3, y3);
        g.draw(curve);
    }
    
    private String stringBezier(double x0, double y0, double x1, double y1, double x2, double y2,
            double x3, double y3, double fact, int cp, int m) {
        float xf0 = (float) (fact * factorx * actualPos(x0, true, cp));
        float yf0 = (float) (-fact * factory * actualPos(y0, false, cp));
        float xf1 = (float) (fact * factorx * actualPos(x1, true, cp));
        float yf1 = (float) (-fact * factory * actualPos(y1, false, cp));
        float xf2 = (float) (fact * factorx * actualPos(x2, true, cp));
        float yf2 = (float) (-fact * factory * actualPos(y2, false, cp));
        float xf3 = (float) (fact * factorx * actualPos(x3, true, cp));
        float yf3 = (float) (-fact * factory * actualPos(y3, false, cp));
        String box = "box";
        if (m == -1) box = "bigbox";
        String info = "\\draw["+box+", colr"+m+"] ("+xf0+", "+yf0+") .. controls ("+xf1+", "+yf1+") and (";
        info = info + xf2+", "+yf2+") .. ("+xf3+", "+yf3+");";
        //String info = "\\draw["+box+", colr"+m+"] ($(o)+("+xf0+", "+yf0+")$) .. controls +("
        //        +(xf1-xf0)+", "+(yf1-yf0)+") and +("+(xf2-xf3)+", "+(yf2-yf3)+") .. +("+(xf3-xf0)
        //        +", "+(yf3-yf0)+");";
        return info;
    }
    
    private double actualPos(double x, boolean xcoord, int cp) {
        double stretchX = 16 / (max.get(cp)[0] - min.get(cp)[0]);
        double stretchY = 16 / (max.get(cp)[1] - min.get(cp)[1]);
        double shiftX = shiftBy(cp, true);
        double shiftY = shiftBy(cp, false);
        if (xcoord) return shiftX + 2 + stretchX * (x - min.get(cp)[0]);
        return shiftY + 2 + stretchY * (x - min.get(cp)[1]);
    } 
    
    private double shiftBy(int cp, boolean xcoord) {
        int i = theComplexes.size();
        int a = (int) Math.sqrt(i+0.5);
        if (xcoord) return (20.0 * (cp / a));
        return (20.0 * (cp % a));
    }
    
    private double[] minimalPosition(int i) {
        double[] mini = new double[2];
        mini[0] = 100;
        mini[1] = 100;
        for (Vertex vert : theComplexes.get(i).vertices) {
            if (vert.x < mini[0]) mini[0] = vert.x;
            if (vert.y < mini[1]) mini[1] = vert.y;
        }
        return mini;
    }
    
    private double[] maximalPosition(int i) {
        double[] maxi = new double[2];
        maxi[0] = 0;
        maxi[1] = 0;
        for (Vertex vert : theComplexes.get(i).vertices) {
            if (vert.x > maxi[0]) maxi[0] = vert.x;
            if (vert.y > maxi[1]) maxi[1] = vert.y;
        }
        return maxi;
    }
    
    public void stopMoving() {
        if (mover != null) mover.stopRunning();
    }
    
    public void movePoints(int cp) {
        ArrayList<double[]> newPoints = new ArrayList<double[]>();
        double dist = 0.0;
        for (Vertex vert : theComplexes.get(cp).vertices) {
            ArrayList<Vertex> adjacents = new ArrayList<Vertex>();
            if (vert.type <= 1) {
                for (Edge edge : vert.comb) adjacents.add(edge.svert);
            }
            else for (Edge edge : vert.comb) adjacents.add(edge.fvert);
            double[] point = new double[] {0.0, 0.0};
            for (Vertex svert : theComplexes.get(cp).vertices) {
                if (svert != vert) {
                    point = addPoint(point, movePoint(vert, svert, adjacents));
                }
            }
            double norm = Math.sqrt(normSquare(point[0], point[1]));
            if (norm > dist) dist = norm;
            newPoints.add(point);
        }
        double factor = 0.01 / dist;
        for (int i = 0; i < newPoints.size(); i++) {
            Vertex vert = theComplexes.get(cp).vertices.get(i);
            if (vert.type != 2) {
                vert.x = vert.x + newPoints.get(i)[0] * factor;
                vert.y = vert.y + newPoints.get(i)[1] * factor;
            }
        }
        for (int i = 0; i < newPoints.size(); i++) {
            Vertex vert = theComplexes.get(cp).vertices.get(i);
            if (vert.type == 2) {
                int j = 0;
                boolean moveOkay = true;
                while (moveOkay && j < theComplexes.get(cp).vertices.size()) {
                    Vertex svert = theComplexes.get(cp).vertices.get(j);
                    if (svert.type != 2 && tooClose(vert, svert, newPoints.get(i))) moveOkay = false;
                    j++;
                }
                if (moveOkay) {
                    vert.x = vert.x + newPoints.get(i)[0] * factor;
                    vert.y = vert.y + newPoints.get(i)[1] * factor;
                }
            }
        }
        min.set(cp, minimalPosition(cp));
        max.set(cp, maximalPosition(cp));
    }
    
    private boolean tooClose(Vertex fvert, Vertex svert, double[] shift) {
        Vertex tvert = svert.comb.get(0).fvert;
        double[] a = new double[] {fvert.x - tvert.x, fvert.y - tvert.y};
        double[] b = new double[] {svert.x - tvert.x, svert.y - tvert.y};
        double[] c = new double[] {a[0] + shift[0], a[1] + shift[1]};
        if (determinant(a,b) * determinant(c,b) <= 0) return true;
        Vertex vvert = svert.comb.get(1).fvert;
        a = new double[] {fvert.x - vvert.x, fvert.y - vvert.y};
        b = new double[] {svert.x - vvert.x, svert.y - vvert.y};
        c = new double[] {a[0] + shift[0], a[1] + shift[1]};
        return determinant(a,b) * determinant(c,b) <= 0;
    }
    
    private double determinant(double[] a, double[] b) {
        return a[0]*b[1] - a[1] * b[0];
    }
    
    private double[] movePoint(Vertex vert, Vertex svert, ArrayList<Vertex> adjacents) {
        double[] move;
        if (adjacents.contains(svert)) {
            move = diffVector(svert, vert);
            double nrm = normSquare(move[0], move[1]);
            move[0] = 1 * move[0] * nrm;
            move[1] = 1 * move[1] * nrm;
        }
        else {
            move = diffVector(vert, svert);
            double nrm = normSquare(move[0], move[1]);
            move[0] = move[0] / (nrm * nrm);// * nrm);
            move[1] = move[1] / (nrm * nrm);// * nrm);
        }
        return move;
    }
    
    private double[] addPoint(double[] pointA, double[] pointB) {
        return new double[] {pointA[0] + pointB[0], pointA[1] + pointB[1]};
    }
    
    private double[] diffVector(Vertex vert, Vertex oVert) {
        double x = vert.x - oVert.x;
        double y = vert.y - oVert.y;
        return new double[] {x, y};
    }
    
    private double normSquare(double x, double y) {
        return (x*x+ y*y);
    }
    
    public void minimizeEng(boolean selected) {
        if (selected) {
            mover = new PointMover(this);
            mover.start();
        }
        else mover.stopRunning();
    }
    
    public void rotateDiagram(int angle) {
        double ang = 2* Math.PI * angle/ 360;
        for (int cp = 0; cp < theComplexes.size(); cp++) {
            for (Vertex vert : theComplexes.get(cp).vertices) {
                double nx = Math.cos(ang) * vert.x + Math.sin(ang) * vert.y;
                double ny = -Math.sin(ang) * vert.x + Math.cos(ang) * vert.y;
                vert.x = nx;
                vert.y = ny;
            }
            min.set(cp, minimalPosition(cp));
            max.set(cp, maximalPosition(cp));
        }
    }

    private DrawComplex unKnotComplex(int i) {
        return new DrawComplex(i);
    }
    
    public ArrayList<String> printCoordinates() {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("\\documentclass[border=1bp]{standalone}");
        strings.add("\\usepackage{tikz}");
        strings.add("\\begin{document}");
        strings.add("\\begin{tikzpicture}");
        strings.add("\\tikzstyle{box}=[-, line width = 1pt]");
        strings.add("\\tikzstyle{bigbox}=[-, line width = 3pt]");
        strings.add("\\colorlet{colr-1}{white}");
        for (Color col : colors) {
            float r = ((float) col.getRed())/255;
            float g = col.getGreen()/255f;
            float b = col.getBlue()/255f;
            strings.add("\\definecolor{colr"+colors.indexOf(col)+"}{rgb}{"+r+", "+g+", "+b+"}");
        }
        for (int cp = 0; cp < theComplexes.size(); cp++) {
            double fact = 0.01;
            DrawComplex theComplex = theComplexes.get(cp);
                for (Vertex vert : theComplex.vertices) {
                    if (vert.type  == 3) {
                    double x = fact * (factorx * (actualPos(vert.x, true, cp)));
                    double y = fact * (factory * (actualPos(vert.y, false, cp)));
                    double r = fact * (4 * factorx);
                    int i = drawnComp+vert.label;
                    if (showComps.get(i)) {
                        String colr = "colr"+i;
                        strings.add("\\draw["+colr+"] ("+x+", "+y+") circle (" + r +");");
                        /*if (orientComps.get(i)) {
                            int[] xPoints = new int[3];
                            int[] yPoints = new int[3];
                            xPoints[1] = x + (int) (8 * factorx);
                            yPoints[1] = y;
                            xPoints[0] = xPoints[1] - (int) (factorx * 0.35);
                            yPoints[0] = yPoints[1] + (int) (factory * 0.5);
                            xPoints[2] = xPoints[0] + (int) (factorx * 0.7);
                            yPoints[2] = yPoints[0];
                            g.drawPolyline(xPoints, yPoints, 3);
                        }// orientations, maybe later */
                    }
                }
                if (vert.type == 0) {
                    Vertex vert0 = getCoordVertex(vert.comb, vert.label, 0);
                    Vertex vert1 = getCoordVertex(vert.comb, vert.label, 1);
                    Vertex vert2 = getCoordVertex(vert.comb, vert.label, 2);
                    Vertex vert3 = getCoordVertex(vert.comb, vert.label, 3);
                    double[] cp1 = controlPoint(vert0, vert);
                    double[] cp2 = controlPoint(vert2, vert);
                    double[] cp3 = controlPoint(vert1, vert);
                    double[] cp4 = controlPoint(vert3, vert);
                    double[] oneLot = new double[] {vert0.x, vert0.y, 
                        cp1[0], cp1[1], cp2[0], cp2[1], vert2.x, vert2.y};
                    double[] twoLot = new double[] {vert1.x, vert1.y, 
                        cp3[0], cp3[1], cp4[0], cp4[1], vert3.x, vert3.y};
                    double[] fl;
                    double[] sl;
                    int mOne = theLink.compOf(vert0.label);
                    int mTwo = theLink.compOf(vert1.label);
                    boolean drawFirst = (mOne < drawnComp) && showComps.get(mOne);
                    boolean drawSecon = (mTwo < drawnComp) && showComps.get(mTwo);
                    boolean flipCol = false;
                    if (theLink.getCross(vert.label) != 1) {
                        fl = oneLot;
                        sl = twoLot;
                    }
                    else {
                        fl = twoLot;
                        sl = oneLot;
                        boolean h = drawFirst;
                        drawFirst = drawSecon;
                        drawSecon = h;
                        flipCol = true;
                    }
                    int m = mOne;
                    if (drawFirst) {
                        if (flipCol) m = mTwo;
                        strings.add(stringBezier(fl[0], fl[1], fl[2], fl[3], fl[4], fl[5], fl[6], 
                                fl[7], fact, cp, m));
                    }
                    if (drawSecon) {
                        
                        strings.add(stringBezier(sl[0], sl[1], sl[2], sl[3], sl[4], sl[5], sl[6], 
                                sl[7], fact, cp, -1));
                        //int m;
                        if (flipCol) m = mOne;
                        else m = mTwo;
                        strings.add(stringBezier(sl[0], sl[1], sl[2], sl[3], sl[4], sl[5], sl[6], 
                                sl[7], fact, cp, m));
                    }
                } // */
                if (vert.type == 1) {
                    Vertex avert = vert.comb.get(0).svert;
                    Vertex bvert = vert.comb.get(1).svert;
                    double x0 = avert.x;
                    double y0 = avert.y;
                    double[] cp1 = controlPoint(avert, vert);
                    double[] cp2 = controlPoint(bvert, vert);
                    double x3 = bvert.x;
                    double y3 = bvert.y;
                    int m = theLink.compOf(vert.label);
                    if (m < drawnComp && showComps.get(m)) {
                        strings.add(stringBezier(x0, y0, cp1[0], cp1[1], cp2[0], cp2[1], x3, y3,
                                fact, cp, m));
                    }
                }// */
            }
        }
        strings.add("\\end{tikzpicture}");
        strings.add("\\end{document}");
        return strings;
    }
    
}
