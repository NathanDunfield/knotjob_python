/*

Copyright (C) 2019-21 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

import java.awt.Color;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import knotjob.AbortInfo;
import knotjob.dialogs.DiagramFrame;
import knotjob.links.Link;

/**
 *
 * @author Dirk
 */
public class CircleDiagram extends Thread {
    
    private final double tau;
    private final double error;
    private final int precision;
    private final DiagramFrame frame;
    private final AbortInfo abort;
    private final Link link;
    private final double radius0;
    private final double radius1;
    private int highLight;
    private int highLabel;
    private final ArrayList<Integer> ignorers;
    private final ArrayList<Integer> counters;
    private SComplex[] theComplexes;
    private final CountDownLatch countDown;
    
    public CircleDiagram(Link theLink, int prec, DiagramFrame fram, 
            CountDownLatch counter) {
        radius0 = 1;
        radius1 = 1;
        link = theLink;
        countDown = counter;
        frame = fram;
        abort = new AbortInfo();
        precision = prec;
        error = Math.pow(10, -prec);
        tau = 2 * Math.PI;
        ignorers = new ArrayList<Integer>();
        counters = new ArrayList<Integer>();
    }

    @Override
    public void run() {
        highLight = -1;
        drawAll(null, null, null);
        countDown.countDown();
    }
    
    public SComplex[] getComplexes() {
        return theComplexes;
    }
    
    private void drawAll(ArrayList<Color> clrs, ArrayList<Boolean> oCs, ArrayList<Boolean> sCs) {
        ArrayList<ArrayList<Integer[]>> discs = link.getDiscs();
        ArrayList<ArrayList<Integer>> splitComps = link.splitComponents();
        int scomp = splitComps.size();
        DiagramInfo[] theDiagrams = new DiagramInfo[scomp];
        theComplexes = new SComplex[scomp];
        for (int i = 0; i < scomp; i++) theDiagrams[i] = new DiagramInfo();
        for (ArrayList<Integer[]> disc : discs) {
            int u = getComp(disc.get(0)[0], splitComps);
            theDiagrams[u].add(disc);
        }
        int counter = 0;
        for (int i = 0; i < scomp; i++) {
            theDiagrams[i].setIgnore();
            if (highLight<0) {
                theDiagrams[i].setIgnore();
                ignorers.add(theDiagrams[i].ignore);
            }
            else {
                theDiagrams[i].ignore = ignorers.get(i);
                int j = 0;
                if (i > 0) j = counters.get(i-1);
                if (j <= highLight && highLight < counters.get(i)) {
                    theDiagrams[i].ignore = highLabel;
                    ignorers.set(i,highLabel);
                }
            }
            theComplexes[i] = getComplex(theDiagrams[i],splitComps.get(i),counter);
            improve(theComplexes[i]);
            if (abort.isAborted()) break;
            for (Vertex vert : theComplexes[i].vertices) {
                vert.fixed = false;
            }
            position(theComplexes[i]);
            counter = counter + theComplexes[i].vertices.size();
            if (highLight<0) counters.add(counter);
        }
        if (abort.isAborted()) theComplexes = null;
    }
    
    private void position(SComplex complex) {
        Triangle trian = complex.triangles.get(0);
        trian.fvert.fixed = true;
        trian.svert.fixed = true;
        trian.tvert.fixed = true;
        setVertex(trian.fvert,0,0);
        setVertex(trian.svert,trian.fvert.r + trian.svert.r,0);
        double alpha = angleValue(trian.fvert, trian.svert, trian.tvert);
        double rad = trian.fvert.r + trian.tvert.r;
        setVertex(trian.tvert,Math.cos(alpha) * rad, Math.sin(alpha) * rad);
        complex.triangles.remove(trian);
        while (!complex.triangles.isEmpty()) {
            int i = 0;
            boolean found = false;
            while (!found) {
                trian = complex.triangles.get(i);
                if ((trian.fvert.fixed & trian.svert.fixed) | (trian.fvert.fixed & trian.tvert.fixed) | 
                        (trian.tvert.fixed & trian.svert.fixed)) found = true;
                else i++;
            }
            if (!trian.fvert.fixed) positionVertex(trian.fvert,trian.svert,trian.tvert,trian.rot);
            if (!trian.svert.fixed) positionVertex(trian.svert,trian.tvert,trian.fvert,trian.rot);
            if (!trian.tvert.fixed) positionVertex(trian.tvert,trian.fvert,trian.svert,trian.rot);
            complex.triangles.remove(i);
        }
    }
    
    private void positionVertex(Vertex fvert, Vertex svert, Vertex tvert,boolean ro) {
        double angle = angleValue(svert,tvert,fvert);
        double rad = fvert.r+svert.r;
        double entry = (tvert.x-svert.x)/(tvert.r+svert.r);
        if (entry > 1) entry = 1;
        if (entry < -1) entry = -1;
        double sangl = Math.acos(entry);
        if (tvert.y-svert.y < 0) sangl = -sangl;
        double ang = sangl-angle;
        if (ro) ang = angle+sangl;
        fvert.x = svert.x + Math.cos(ang) * rad;
        fvert.y = svert.y + Math.sin(ang) * rad;
        fvert.fixed = true;
    }
    
    private void setVertex(Vertex vert, double x, double y) {
        vert.x = x;
        vert.y = y;
    }
    
    private SComplex getComplex(DiagramInfo theDiagram, ArrayList<Integer> crossings, int counter) {
        SComplex complex = new SComplex();
        ArrayList<Integer> paths = new ArrayList<Integer>();
        for (int i : crossings) {
            Vertex vert = new Vertex(radius0,0,i,false);
            complex.vertices.add(vert);
            for (int j : link.getPath(i)) if (!paths.contains(j)) paths.add(j);
        }
        for (int i : paths) {
            Vertex vert = new Vertex(radius1,1,i,false);
            complex.vertices.add(vert);
        }
        for (int i = 0; i < theDiagram.discNumber(); i++) {
            if (i != theDiagram.getIgnore()) {
                Vertex vert = new Vertex(1,2,i,false);
                complex.vertices.add(vert);
            }
        }
        for (int i : crossings) {
            for (int j = 0; j < 4; j++) {
                Edge edge = createEdge(i,link.getPath(i, j),0,1,complex.vertices);
                complex.edges.add(edge);
                edge.fvert.comb.add(edge);
                edge.svert.comb.add(edge);
            }
        }
        for (int i = 0; i < theDiagram.discNumber(); i++) {
            if (i!= theDiagram.getIgnore()) {
                for (int u = 0; u < theDiagram.getDisc(i).size(); u++) {
                    Integer[] dentry = theDiagram.getDisc(i).get(u);
                    Edge edge = createEdge(dentry[0],i,0,2,complex.vertices);
                    complex.edges.add(edge);
                    edge = createEdge(link.getPath(dentry[0],dentry[1]),i,1,2,complex.vertices);
                    complex.edges.add(edge);
                    Triangle triang = createTriangle(dentry[0],link.getPath(dentry[0],dentry[1]),i,complex.vertices,true);
                    complex.triangles.add(triang);
                    triang.fvert.rose.add(triang);
                    triang.svert.rose.add(triang);
                    triang.tvert.rose.add(triang);
                    if (u < theDiagram.getDisc(i).size()-1) {
                        Integer[] nentry = theDiagram.getDisc(i).get(u+1);
                        triang = createTriangle(nentry[0],link.getPath(dentry[0],dentry[1]),i,complex.vertices,false);
                        complex.triangles.add(triang);
                        triang.fvert.rose.add(triang);
                        triang.svert.rose.add(triang);
                        triang.tvert.rose.add(triang);
                    }
                }
                Integer[] fentry = theDiagram.getDisc(i).get(0);
                Integer[] lentry = theDiagram.getDisc(i).get(theDiagram.getDisc(i).size()-1);
                Triangle triang = createTriangle(fentry[0],link.getPath(lentry[0],lentry[1]),i,complex.vertices,false);
                complex.triangles.add(triang);
                triang.fvert.rose.add(triang);
                triang.svert.rose.add(triang);
                triang.tvert.rose.add(triang);
            }
        }// */
        fixVertices(complex,theDiagram.getDisc(theDiagram.getIgnore()));
        return complex;
    }
    
    private void fixVertices(SComplex complex, ArrayList<Integer[]> disc) {
        ArrayList<Integer> pathlabels = edgesOf(disc);
        ArrayList<Integer> dotlabels = dotsOf(disc);
        for (Vertex vert : complex.vertices) {
            if (vert.type == 0 && dotlabels.contains(vert.label)) {
                vert.fixed = true;
            }
            if (vert.type == 1 && pathlabels.contains(vert.label)) {
                vert.fixed = true;
            }
        }
    }
    
    private ArrayList<Integer> edgesOf(ArrayList<Integer[]> disc) {
        ArrayList<Integer> edges = new ArrayList<Integer>(disc.size());
        for (Integer[] dentry : disc) {
            edges.add(link.getPath(dentry[0],dentry[1]));
        }
        return edges;
    }
    
    private ArrayList<Integer> dotsOf(ArrayList<Integer[]> disc) {
        ArrayList<Integer> edges = new ArrayList<Integer>(disc.size());
        for (Integer[] dentry : disc) {
            edges.add(dentry[0]);
        }
        return edges;
    }
    
    private Triangle createTriangle(int lab1, int lab2, int lab3, ArrayList<Vertex> vertices, boolean ro) {
        boolean found = false;
        int i = 0;
        while (!found) {
            Vertex vert = vertices.get(i);
            if (vert.label == lab1 && vert.type == 0) found = true;
            else i++;
        }
        found = false;
        int j = 0;
        while (!found) {
            Vertex vert = vertices.get(j);
            if (vert.label == lab2 && vert.type == 1) found = true;
            else j++;
        }
        found = false;
        int k = 0;
        while (!found) {
            Vertex vert = vertices.get(k);
            if (vert.label == lab3 && vert.type == 2) found = true;
            else k++;
        }
        Triangle triang = new Triangle(vertices.get(i),vertices.get(j),vertices.get(k),ro);
        return triang;
    }
    
    private Edge createEdge(int lab1, int lab2, int typ1, int typ2, ArrayList<Vertex> vertices) {
        boolean found = false;
        int i = 0;
        while (!found) {
            Vertex vert = vertices.get(i);
            if (vert.label == lab1 && vert.type == typ1) found = true;
            else i++;
        }
        found = false;
        int j = 0;
        while (!found) {
            Vertex vert = vertices.get(j);
            if (vert.label == lab2 && vert.type == typ2) found = true;
            else j++;
        }
        Edge edge = new Edge(vertices.get(i),vertices.get(j));
        return edge;
    }
    
    private int getComp(int u, ArrayList<ArrayList<Integer>> splitComps) {
        int v = 0;
        boolean found = false;
        while (!found) {
            if (splitComps.get(v).contains(u)) found = true;
            else v++;
        }
        return v;
    }
    
    private void improve(SComplex complex) {
        double complexError = error(complex);
        while (Math.abs(complexError) > error) {
            change(complex);
            complexError = error(complex);
            int precis = precisionOf(Math.abs(complexError)); 
            frame.infoLabel.setText(" "+precis+" %");
        }
    }
    
    private int precisionOf(double err) {
        int i = 0;
        double comp = error;
        while (err > comp) {
            comp = comp * 10;
            i++;
        }
        int pre = 100 - (100/precision) * i;
        if (pre < 0) pre = 0;
        return pre;
    }
    
    private void change(SComplex complex) {
        for (Vertex vert : complex.vertices) {
            if (!vert.fixed) {
                double fact = (double) 2* vert.rose.size();
                double beta = Math.sin(angleSum(vert)/fact);
                double delta = Math.sin(tau/fact);
                double hatv = vert.r*beta/(1-beta);
                vert.r = hatv*(1-delta)/delta;
            }
        }
    }
    
    private double error(SComplex complex) {
        double err = 0;
        for (Vertex vert : complex.vertices) {
            if (!vert.fixed) err = err + Math.abs(angleSum(vert) - tau);
        }
        return err;
    }
    
    private double angleValue(Vertex first, Vertex sec, Vertex thi) {
        double root = Math.sqrt(sec.r*thi.r/((first.r+sec.r)*(first.r+thi.r)));
        return 2*Math.asin(root);
    }
    
    private double angleSum(Vertex vert) {
        double sum = 0;
        int theCase = vert.type;
        for (Triangle triang : vert.rose) {
            if (theCase == 0) sum = sum + angleValue(vert,triang.svert,triang.tvert);
            if (theCase == 1) sum = sum + angleValue(vert,triang.fvert,triang.tvert);
            if (theCase == 2) sum = sum + angleValue(vert,triang.fvert,triang.svert);
        }
        return sum;
    }
    
}
