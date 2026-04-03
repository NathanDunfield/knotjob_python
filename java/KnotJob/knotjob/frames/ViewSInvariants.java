/*

Copyright (C) 2019-20 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import knotjob.Options;
import knotjob.links.LinkData;
import knotjob.links.SInv;

/**
 *
 * @author Dirk
 */
public class ViewSInvariants extends JFrame {
    
    private final LinkData theLink;
    private final Options options;
    private final JButton closeButton;
    private final JScrollPane sInvariantPane;
    private final JPanel sInvPanel;
    private final Image img;
    
    public ViewSInvariants(LinkData link, Options opts) {
        theLink = link;
        options = opts;
        img = options.getImage();
        closeButton = new JButton("Close");
        sInvPanel = new JPanel();
        sInvariantPane = new JScrollPane(sInvPanel);
    }
    
    public void setupFrame() {
        this.setLayout(new BorderLayout());
        this.setSize(300,200);
        this.setResizable(false);
        if (img != null) this.setIconImage(img);
        this.setTitle("s("+theLink.name+")");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        this.add(buttonPanel,BorderLayout.SOUTH);
        JPanel mainPanel = new JPanel();
        int[][] data = theLink.sInvariants();
        int[][] tdata = theLink.sSlTInvariants(false);
        int[][] rdata = theLink.sSlTInvariants(true);
        ArrayList<SInv> sdata = invariants(data);
        ArrayList<SInv> stdata = invariants(tdata);
        ArrayList<SInv> srdata = invariants(rdata);
        sInvPanel.setPreferredSize(new Dimension(220, 28*(data.length+tdata.length+rdata.length)));
        sInvPanel.setLayout(new GridLayout(data.length+tdata.length+rdata.length, 1));
        for (int i = 0; i < data.length; i++) {
            sInvPanel.add(new JLabel(sdata.get(i).sInv()+"  (mod "+sdata.get(i).sChar()+")", SwingConstants.CENTER));
        }
        for (int i = 0; i < tdata.length; i++) {
            sInvPanel.add(new JLabel(stdata.get(i).sInv()+" (sl3 mod "+stdata.get(i).sChar()+")", SwingConstants.CENTER));
        }for (int i = 0; i < rdata.length; i++) {
            sInvPanel.add(new JLabel(srdata.get(i).sInv()+" (rl3 mod "+srdata.get(i).sChar()+")", SwingConstants.CENTER));
        }
        sInvariantPane.setPreferredSize(new Dimension(240,120));
        mainPanel.add(sInvariantPane);
        this.add(mainPanel, BorderLayout.CENTER);
    }
    
    private ArrayList<SInv> invariants(int[][] data) {
        ArrayList<SInv> invs = new ArrayList<SInv>();
        for (int i = 0; i < data.length; i++) {
            invs.add(new SInv(data[i][1],data[i][0]));
        }
        Collections.sort(invs);
        return invs;
    }
    
}
