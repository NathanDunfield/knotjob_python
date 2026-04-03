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

package knotjob.diagrams.griddiagrams;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.swing.JComponent;

/**
 *
 * @author Dirk
 */
public class GraphicalGridDiagram extends JComponent {

    private final GridDiagram theGrid;
    private final Color gridColor;
    private final Color symbColor;
    private final ArrayList<Color> linkColors;
    private final int originx;
    private final int originy;
    private int boxsize;
    private boolean showlink;
    private boolean showOX;
    private boolean showGrid;
    private boolean symbfirst;
    private int highlightedEdge;
    private int draggedEdge;
    private boolean horFirst;
    private final double fa = 0.5;
    
    public GraphicalGridDiagram(GridDiagram grid) {
        theGrid = grid;
        gridColor = Color.LIGHT_GRAY;
        symbColor = Color.BLACK;
        linkColors = new ArrayList<Color>();
        linkColors.add(Color.BLUE);
        originx = 100;
        originy = 100;
        boxsize = 24;
        showlink = true;
        showOX = true;
        showGrid = true;
        symbfirst = true;
    }
    
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setBackground(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, boxsize-2)); 
        String info = theGrid.size()+" x "+theGrid.size();
        g2.drawString(info, originx+2*boxsize, 52);
        if (draggedEdge > 0) draggedHorizontal(g2);
        if (draggedEdge < 0) draggedVertical(g2);
        if (symbfirst && showOX) {
            drawTheOs(g2);
            drawTheXs(g2);
        }
        if (showGrid) drawVerticalGrid(g2);
        if (highlightedEdge > 0) highlightHorizontal(g2);
        if (showlink) drawHorizontalLink(g2);
        if (showlink) drawVerticalCrossings(g2);
        if (showGrid) drawHorizontalGrid(g2);
        if (highlightedEdge < 0) highlightVertical(g2);
        if (showlink) drawVerticalLink(g2);
        if (!symbfirst && showOX) {
            drawTheOs(g2);
            drawTheXs(g2);
        }
    }

    public ArrayList<String> printCoordinates() {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("\\documentclass[border=1bp]{standalone}");
        strings.add("\\usepackage{tikz}");
        strings.add("\\begin{document}");
        strings.add("\\begin{tikzpicture}");
        strings.add("\\tikzstyle{box}=[-, line width = 2pt, color = blue]");
        strings.add("\\tikzstyle{letbox}=[-, line width = 2pt, color = black]");
        strings.add("\\tikzstyle{crsbox}=[-, line width = 6pt, color = white]");
        strings.add("\\tikzstyle{gridbox}=[-, line width = 0.5pt, color = gray]");
        if (symbfirst && showOX) {
            printTheOs(strings);
            printTheXs(strings);
        }
        if (showGrid) printVerticalGrid(strings);
        if (showlink) printHorizontalLink(strings);
        if (showlink) printVerticalCrossings(strings);
        if (showGrid) printHorizontalGrid(strings);
        if (showlink) printVerticalLink(strings);
        if (!symbfirst && showOX) {
            printTheOs(strings);
            printTheXs(strings);
        }
        strings.add("\\end{tikzpicture}");
        strings.add("\\end{document}");
        return strings;
    }
    
    private void drawVerticalGrid(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1));
        g2.setPaint(gridColor);
        for (int i = 0; i <= theGrid.size(); i++) {
            g2.drawLine(originx+i*boxsize, originy, originx+i*boxsize, 
                    originy+boxsize*theGrid.size());
        }
    }

    private void printVerticalGrid(ArrayList<String> strings) {
        for (int i = 0; i <= theGrid.size(); i++) {
            strings.add("\\draw[gridbox] ("+i*fa+", 0) -- ("
                    +i*fa+", -"+fa*theGrid.size()+");");
        }
    }
    
    private void drawHorizontalGrid(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1));
        g2.setPaint(gridColor);
        for (int i = 0; i <= theGrid.size(); i++) {
            g2.drawLine(originx, originy+i*boxsize, originx+boxsize*theGrid.size(),
                    originy+i*boxsize);
        }
    }

    private void printHorizontalGrid(ArrayList<String> strings) {
        for (int i = 0; i <= theGrid.size(); i++) {
            strings.add("\\draw[gridbox] (0, -"+fa*i+") -- ("
                    +fa*theGrid.size()+", -"+fa*i+");");
        }
    }
    
    private void drawTheOs(Graphics2D g2) {
        for (int i = 0; i < theGrid.size(); i++) drawAnO(g2,i);
    }

    private void drawTheXs(Graphics2D g2) {
        for (int i = 0; i < theGrid.size(); i++) drawAnX(g2,i);
    }
    
    private void drawAnO(Graphics2D g2, int i) {
        g2.setStroke(new BasicStroke(3));
        g2.setPaint(symbColor);
        g2.drawOval(originx+boxsize*i+2, originy+boxsize*theGrid.getOofColumn(i)+2, boxsize-4, 
                boxsize-4);
    }

    private void printTheOs(ArrayList<String> strings) {
        for (int i = 0; i < theGrid.size(); i++) {
            strings.add("\\draw[letbox] ("+fa * (i+0.5)+", -"+fa * (theGrid.getOofColumn(i)+0.5)+
                    ") circle ("+fa * 0.36+");");
        }
    }
    
    private void drawAnX(Graphics2D g2, int i) {
        g2.setStroke(new BasicStroke(3));
        g2.setPaint(symbColor);
        g2.drawLine(originx+boxsize*i+3,originy+boxsize*theGrid.getXofColumn(i)+3, 
                originx+boxsize*i+boxsize-3, originy+boxsize*theGrid.getXofColumn(i)+boxsize-3);
        g2.drawLine(originx+boxsize*i+3,originy+boxsize*theGrid.getXofColumn(i)+boxsize-3, 
                originx+boxsize*i+boxsize-3, originy+boxsize*theGrid.getXofColumn(i)+3);
    }

    private void printTheXs(ArrayList<String> strings) {
        for (int i = 0; i < theGrid.size(); i++) {
            strings.add("\\draw[letbox] ("+fa * (i+0.125)+", -"+fa * (theGrid.getXofColumn(i)+0.125)+
                    ") -- ("+fa * (i+0.875)+", -"+fa * (theGrid.getXofColumn(i)+0.875)+");");
            strings.add("\\draw[letbox] ("+fa * (i+0.125)+", -"+fa * (theGrid.getXofColumn(i)+0.875)+
                    ") -- ("+fa * (i+0.875)+", -"+fa * (theGrid.getXofColumn(i)+0.125)+");");
        }
    }
    
    private void drawHorizontalLink(Graphics2D g2) {
        for (int i = 0; i < theGrid.size(); i++) drawHorizontal(g2,i);
    }

    private void printHorizontalLink(ArrayList<String> strings) {
        for (int i = 0; i < theGrid.size(); i++) {
            double x1 = fa * (theGrid.getOofRow(i)+0.5);
            double x2 = fa * (theGrid.getXofRow(i)+0.5);
            double y = fa * (i+0.5);
            strings.add("\\draw[box] ("+x1+", -"+y+") -- ("+x2+", -"+y+");");
        }
    }
    
    private void drawHorizontal(Graphics2D g2, int i) {
        g2.setStroke(new BasicStroke(4));
        g2.setPaint(linkColors.get(0));
        int x1 = originx+theGrid.getOofRow(i)*boxsize+boxsize/2;
        int x2 = originx+theGrid.getXofRow(i)*boxsize+boxsize/2;
        int y = originy+i*boxsize+boxsize/2;
        g2.drawLine(x1, y, x2, y);
    }
    
    private void drawVerticalLink(Graphics2D g2) {
        for (int i = 0; i < theGrid.size(); i++) drawVertical(g2,i);
    }

    private void drawVertical(Graphics2D g2, int i) {
        int x = originx+i*boxsize+boxsize/2;
        int y1 = originy+theGrid.getXofColumn(i)*boxsize+boxsize/2;
        int y2 = originy+theGrid.getOofColumn(i)*boxsize+boxsize/2;
        g2.setStroke(new BasicStroke(4));
        g2.setPaint(linkColors.get(0));
        g2.drawLine(x, y1, x, y2);
    }

    private void printVerticalLink(ArrayList<String> strings) {
        for (int i = 0; i < theGrid.size(); i++) {
            double x = fa * (i+0.5);
            double y1 = fa * (theGrid.getXofColumn(i)+0.5);
            double y2 = fa * (theGrid.getOofColumn(i)+0.5);
            if (y1 < y2) {
                y1 = y1 - fa * 0.07;
                y2 = y2 + fa * 0.07;
            }
            else {
                y1 = y1 + fa * 0.07;
                y2 = y2 - fa * 0.07;
            }
            strings.add("\\draw[box] ("+x+", -"+y1+") -- ("+x+", -"+y2+");");
        }
    }
    
    private void drawVerticalCrossings(Graphics2D g2) {
        for (int i = 0; i < theGrid.size(); i++) drawVerticalCross(g2,i);
    }
    
    private void drawVerticalCross(Graphics2D g2, int i) {    
        int x = originx+i*boxsize+boxsize/2;
        int y1 = originy+theGrid.getXofColumn(i)*boxsize+boxsize/2;
        int y2 = originy+theGrid.getOofColumn(i)*boxsize+boxsize/2;
        if (Math.abs(y1-y2) <= boxsize) return;
        if (y1 < y2) {
            y1 = y1 + boxsize;
            y2 = y2 - boxsize;
        }
        else {
            y1 = y1 - boxsize;
            y2 = y2 + boxsize;
        }
        g2.setStroke(new BasicStroke(8));
        g2.setPaint(Color.WHITE);
        g2.drawLine(x, y1, x, y2);
    }
    
    private void printVerticalCrossings(ArrayList<String> strings) {
        for (int i = 0; i < theGrid.size(); i++) {
            double x = fa * (i+0.5);
            double y1 = fa * (theGrid.getXofColumn(i)+0.5);
            double y2 = fa * (theGrid.getOofColumn(i)+0.5);
            if (Math.abs(y1-y2) <= fa) return;
            if (y1 < y2) {
                y1 = y1 + fa/2;
                y2 = y2 - fa/2;
            }
            else {
                y1 = y1 - fa/2;
                y2 = y2 + fa/2;
            }// */
            strings.add("\\draw[crsbox] ("+x+", -"+y1+") -- ("+x+", -"+y2+");");
        }
    }

    public int boxsize() {
        return boxsize;
    }

    public void setShowlink(boolean selected) {
        showlink = selected;
    }

    public void setShowOX(boolean selected) {
        showOX = selected;
    }

    public void setShowGrid(boolean selected) {
        showGrid = selected;
    }

    public void setSymbfirst(boolean selected) {
        symbfirst = selected;
    }

    public void setBoxsize(int value) {
        boxsize = value;
    }

    public void detectEdge(int x, int y) {
        int[] coord = getCoordinates(x, y);
        highlightedEdge = getTheEdge(coord);
    }

    public void moveEdge(int x, int y) {
        if (highlightedEdge != 0) {
            int[] coord = getCoordinates(x, y);
            draggedEdge = getTheEdgeRelativeTo(coord);
        }
    }

    private int getTheEdgeRelativeTo(int[] coord) {
        if (coord[0] == -1 && coord[1] == -1) return draggedEdge;
        if (highlightedEdge > 0) return horizontalShift(coord);
        return verticalShift(coord);
    }
    
    private int horizontalShift(int[] coord) {
        if (coord[1] == highlightedEdge - 1) return highlightedEdge + 1;
        return maximalCommuting(coord[1] - highlightedEdge + 1, true);
    }
    
    private int verticalShift(int[] coord) {
        if (coord[0] == - 1 - highlightedEdge) return highlightedEdge - 1;
        return maximalCommuting(coord[0] + 1 + highlightedEdge, false);
    }
    
    private int maximalCommuting(int rel, boolean hor) {
        int fac = rel / Math.abs(rel);
        if (highlightedEdge == 1 && fac == -1) return 1;
        if (highlightedEdge == theGrid.size() && Math.abs(rel)>= theGrid.size()) return highlightedEdge+2;
        if (highlightedEdge == -1 && fac == -1) return -1;
        if (highlightedEdge == -theGrid.size() && Math.abs(rel)>= theGrid.size()) return highlightedEdge-2;
        int lastOne = -(1+highlightedEdge);
        if (hor) lastOne = highlightedEdge - 1;
        int line = lastOne;
        boolean okay = true;
        while (okay && lastOne != line + rel) {
            if (lastOne + fac >= 0 && lastOne + fac < theGrid.size() && 
                    theGrid.commutes(line, lastOne + fac, hor)) lastOne = lastOne + fac;
            else okay = false;
        }
        int max = -(lastOne + 2);
        if (hor) max = lastOne + 2;
        return max;
    }
    
    private int getTheEdge(int[] coord) {
        if (coord[0] == -1 || coord[1] == -1) return 0;
        int u = 0;
        int fc = 1;
        if (horFirst) {
            u = 1;
            fc = -1;
        }
        int theX = getColOrRow(coord, u, true);
        int theO = getColOrRow(coord, u, false);
        if ((theX >= coord[1-u] && theO <= coord[1-u]) || 
                (theX <= coord[1-u] && theO >= coord[1-u])) return -fc * (coord[u]+1);
        theX = getColOrRow(coord, 1-u, true);
        theO = getColOrRow(coord, 1-u, false);
        if ((theX >= coord[u] && theO <= coord[u]) || 
                (theX <= coord[u] && theO >= coord[u])) return fc * (coord[1-u]+1);
        return 0;
    }
    
    private int getColOrRow(int[] coord, int u, boolean isX) {
        if (u == 0 && isX) return theGrid.getXofColumn(coord[0]);
        if (u == 0) return theGrid.getOofColumn(coord[0]);
        if (isX) return theGrid.getXofRow(coord[1]);
        return theGrid.getOofRow(coord[1]);
    }
    
    private int[] getCoordinates(int x, int y) {
        int xx = (x - 1 - originx)/boxsize;
        int yy = (y - 1 - originy)/boxsize;
        if (x < originx+2 || x > originx + theGrid.size() * boxsize) xx = -1;
        if (y < originy + 2 || y > originy  + theGrid.size() * boxsize) yy = -1;
        return new int[] {xx, yy};
    }
    
    private void highlightHorizontal(Graphics2D g2) {
        g2.setStroke(new BasicStroke(boxsize - 4));
        g2.setPaint(Color.YELLOW);
        int i = highlightedEdge-1;
        int x1 = originx+theGrid.getOofRow(i)*boxsize+boxsize/2;
        int x2 = originx+theGrid.getXofRow(i)*boxsize+boxsize/2;
        int y = originy+i*boxsize+boxsize/2;
        g2.drawLine(x1, y, x2, y);
        if (showOX) {
            drawAnO(g2, theGrid.getOofRow(i));
            drawAnX(g2, theGrid.getXofRow(i));
        }
    }

    private void highlightVertical(Graphics2D g2) {
        g2.setStroke(new BasicStroke(boxsize - 4));
        g2.setPaint(Color.YELLOW);
        int i = -(highlightedEdge + 1);
        int x = originx+i*boxsize+boxsize/2;
        int y1 = originy+theGrid.getXofColumn(i)*boxsize+boxsize/2;
        int y2 = originy+theGrid.getOofColumn(i)*boxsize+boxsize/2;
        g2.drawLine(x, y1, x, y2);
        if (showOX) {
            drawAnO(g2, i);
            drawAnX(g2, i);
        }
    }

    private void draggedHorizontal(Graphics2D g2) {
        g2.setStroke(new BasicStroke(boxsize - 4));
        g2.setPaint(Color.CYAN);
        int i = highlightedEdge-1;
        int j = highlightedEdge - draggedEdge + 1;
        int x1 = originx+theGrid.getOofRow(i)*boxsize+boxsize/2;
        int x2 = originx+theGrid.getXofRow(i)*boxsize+boxsize/2;
        int y = originy+i*boxsize+boxsize/2-j * boxsize;
        g2.drawLine(x1, y, x2, y);
    }

    private void draggedVertical(Graphics2D g2) {
        g2.setStroke(new BasicStroke(boxsize - 4));
        g2.setPaint(Color.CYAN);
        int i = -(highlightedEdge + 1);
        int j = 1 + draggedEdge - highlightedEdge;
        int x = originx+i*boxsize+boxsize/2-j * boxsize;
        int y1 = originy+theGrid.getXofColumn(i)*boxsize+boxsize/2;
        int y2 = originy+theGrid.getOofColumn(i)*boxsize+boxsize/2;
        g2.drawLine(x, y1, x, y2);
    }

    public void release() {
        if (draggedEdge == 0) return;
        int fixedEdge = draggedEdge - 1;
        if (draggedEdge < 0) fixedEdge = draggedEdge + 1;
        if (fixedEdge == 0 || Math.abs(fixedEdge) == theGrid.size()+1) {
            if (draggedEdge == 1) theGrid.commuteHor(0, theGrid.size()-1);
            if (draggedEdge == theGrid.size()+2) theGrid.commuteHor(theGrid.size()-1, 0);
            if (draggedEdge == -1) theGrid.commuteVert(0, theGrid.size()-1);
            if (draggedEdge == -(theGrid.size()+2)) theGrid.commuteVert(theGrid.size()-1, 0);
        }
        else if (fixedEdge != highlightedEdge) {
            if (highlightedEdge < 0) theGrid.commuteVert(-1-highlightedEdge, -1-fixedEdge);
            else theGrid.commuteHor(highlightedEdge-1, fixedEdge-1);
            highlightedEdge = 0;
        }
        highlightedEdge = 0;
        draggedEdge = 0;
        repaint();
    }

    public void tryToStabilize(int x, int y) {
        int[] coord = getCoordinates(x, y);
        if (coord[0] == -1 || coord[1] == -1) return;
        if (theGrid.isX(coord) || theGrid.isO(coord)) {
            theGrid.stabilize(coord);
            setPreferredSize(new Dimension(200+boxsize * theGrid.size(), 
                        200+ boxsize * theGrid.size()));
            repaint();
        }
    }

    public void tryCancel(int x, int y) {
        int[] coord = getCoordinates(x, y);
        if (coord[0] == -1 || coord[1] == -1) return;
        theGrid.cancel(coord);
        highlightedEdge = 0;
        draggedEdge = 0;
        repaint();
    }
    
    public void flipPreference() {
        horFirst = !horFirst;
    }

    public void tryToImprove() {
        int counter = 1000;
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        while (counter >= 0) {
            theGrid.tryToCancel();
            theGrid.shiftColumns();
            theGrid.shiftRows();
            theGrid.searchStaircase();
            theGrid.randomMoves(5);
            counter--;
        }
        theGrid.tryToCancel();
        highlightedEdge = 0;
        draggedEdge = 0;
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}
