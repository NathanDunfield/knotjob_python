/*

Copyright (C) 2021-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.swing.JComponent;
import knotjob.homology.Homology;
import knotjob.homology.QuantumCohomology;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class BLTComponent extends JComponent {
    
    private final int typ;
    private final LinkData theLink;
    private final ArrayList<QuantumCohomology> qCohs;
    public String characteristic;
    public int qmin;
    public int qmax;
    public int hmin;
    public int hmax;
    private int boxsize;
    
    public BLTComponent(String chr, int tp, LinkData tLnk) {
        typ = tp;
        theLink = tLnk;
        qCohs = new ArrayList<QuantumCohomology>();
        characteristic = chr;
        boxsize = 24;
        obtainQuantumCohomologies(0);
        getMinMaxBounds();
    }
    
    public void setCharacteristic(String chr) {
        characteristic = chr;
        obtainQuantumCohomologies(0);
        getMinMaxBounds();
        this.setTheSize();
    }

    public void setEPage(int page) {
        obtainQuantumCohomologies(page-1);
        this.repaint();
    }
    
    public void setTheSize() {
        this.setPreferredSize(new Dimension(this.gridSize(true), 
                        this.gridSize(false)));
        this.repaint();
    }
    
    private int gridSize(boolean hdir) {
        if (hdir) return  200 + 2 * boxsize + 2 * (hmax - hmin) * (boxsize);
        return 60 + 2 * boxsize + 50 + (qmax - qmin)/2 * (boxsize+10);
    }
    
    public void setBoxsize(int value) {
        boxsize = value;
    }
    
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setFont(new Font("SansSerif", Font.PLAIN, boxsize - 2));
        drawQs(g2);
        drawHs(g2);
        drawBettis(g2);
        drawLines(g2);
    }

    private void drawLines(Graphics2D g2) {
        int hd = 1 + hmax - hmin;
        int qd = 1 + (qmax - qmin)/2;
        for (int q = 0; q <= qd; q++) {
            g2.drawLine(80 + 2 * boxsize, 50 + boxsize + q * (boxsize+10), 
                    80 + 2 * boxsize + 2 * hd * (boxsize), 
                    50 + boxsize + q * (boxsize+10));
        }
        for (int h = 0; h <= hd; h++) {
            g2.drawLine(80 + 2 * boxsize + 2 * h * boxsize, 50 + boxsize, 
                    80 + 2 * boxsize + 2 * h * boxsize, 
                    50 + boxsize + qd * (boxsize+10));
        }
    }
    
    private void drawBettis(Graphics2D g2) {
        for (QuantumCohomology coh : qCohs) {
            int q = (qmax - coh.qdeg())/2;
            for (Homology hom : coh.getHomGroups()) {
                int h = hom.hdeg() - hmin;
                String betti = String.valueOf(hom.getBetti());
                while (betti.length() <= 4) betti = " "+betti;
                g2.drawString(betti, 80 + (int) (1.6 * boxsize) + 2 * h * (boxsize), 
                        2 * boxsize + 50 + q * (boxsize+10));
            }
        }
    }
    
    private void drawQs(Graphics2D g2) {
        for (int i = 0; i <= (qmax - qmin)/2; i++) {
            String qstr = String.valueOf(qmax - 2 * i);
            while (qstr.length() < 4) qstr = " "+qstr;
            g2.drawString(qstr, 40, 2 * boxsize + 50 + i * (boxsize+10));
        }
    }
    
    private void drawHs(Graphics2D g2) {
        for (int i = 0; i <= hmax - hmin; i++) {
            String hstr = String.valueOf(hmin+i);
            while (hstr.length() < 4) hstr = " "+hstr;
            g2.drawString(hstr, 80 + 2 * boxsize + 2 * i * (boxsize), 30+boxsize);
        }
    }
    
    public ArrayList<String> quantumStrings(int page) {
        int[] info = theLink.bltInfo(String.valueOf(characteristic), typ);
        ArrayList<String> qStrings = new ArrayList<String>();
        ArrayList<String> relevant = theLink.unredBLT;
        if (typ == 1) relevant = theLink.redBLT;
        if (typ == 2) relevant = theLink.sltTypeThree;
        if (typ == 3) relevant = theLink.sltTypeTwo;
        if (typ == 4) relevant = theLink.sltTypeOne;
        if (typ == 5) relevant = theLink.sltTypeOneRed;
        if (typ == 6) relevant = theLink.sltTypeX;
        if (typ == 7) relevant = theLink.sltTypeXSq;
        for (int i = info[page*2]; i <= info[page*2+1]; i++) {
            qStrings.add(relevant.get(i));
        }
        return qStrings;
    } 
    
    private void obtainQuantumCohomologies(int page) {
        qCohs.clear();
        ArrayList<String> qStrings = quantumStrings(page); 
        for (String str : qStrings) qCohs.add(new QuantumCohomology(str));
    }

    private void getMinMaxBounds() {
        qmax = qCohs.get(0).qdeg();
        qmin = qmax;
        hmin = qCohs.get(0).getHomGroups().get(0).hdeg();
        hmax = hmin;
        for (QuantumCohomology qcoh : qCohs) {
            if (qmax < qcoh.qdeg()) qmax = qcoh.qdeg();
            if (qmin > qcoh.qdeg()) qmin = qcoh.qdeg();
            for (Homology hom : qcoh.getHomGroups()) {
                if (hmax < hom.hdeg()) hmax = hom.hdeg();
                if (hmin > hom.hdeg()) hmin = hom.hdeg();
            }
        }
    }
    
}
