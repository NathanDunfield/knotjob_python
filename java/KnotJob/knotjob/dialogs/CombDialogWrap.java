/*

Copyright (C) 2022 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.dialogs;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import knotjob.links.CombMinimizer;

/**
 *
 * @author Dirk
 */
public class CombDialogWrap extends DialogWrap {
    
    private final JDialog frame;
    private final JFrame frm;
    private final JLabel knotLabel;
    private final JLabel posLabel;
    private final JLabel optnumLabel;
    
    public CombDialogWrap(JFrame fram) {
        super(null, null);
        frame = new JDialog(new JFrame(), "Minimize Combined Crossings", true);
        frm = fram;
        knotLabel = new JLabel("Knot",JLabel.CENTER);
        posLabel = new JLabel("0", JLabel.LEFT);
        optnumLabel = new JLabel(" 0/0", JLabel.LEFT);
        
    }
    
    public void setup(CombMinimizer coMi) {
        frame.setSize(300,250);
        frame.setLocationRelativeTo(frm);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);
        JButton abortButton = new JButton("Abort");
        JButton cancelButton = new JButton("Cancel");
        JPanel abortPanel = new JPanel();
        JPanel cancelPanel = new JPanel();
        abortPanel.add(abortButton);
        cancelPanel.add(cancelButton);
        JPanel btnPanel = new JPanel();
        btnPanel.add(abortPanel);
        btnPanel.add(cancelPanel);
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(4,1));
        JPanel startPanel = new JPanel();
        startPanel.setLayout(new GridLayout(1,3));
        JLabel startLabel = new JLabel("Start : ", JLabel.RIGHT);
        JButton skipButton = new JButton("Skip");
        JPanel skipPanel = new JPanel();
        skipPanel.add(skipButton);
        JPanel optPanel = new JPanel();
        optPanel.setLayout(new GridLayout(1,2));
        JLabel optLabel = new JLabel("Combined Crossings: ", JLabel.RIGHT);
        optPanel.add(optLabel);
        optPanel.add(optnumLabel);
        startPanel.add(startLabel);
        startPanel.add(posLabel);
        startPanel.add(skipPanel);
        infoPanel.add(knotLabel);
        infoPanel.add(startPanel);
        infoPanel.add(optPanel);
        infoPanel.add(btnPanel);
        frame.add(infoPanel);
        skipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                coMi.setSkipped();
            }
        });
        abortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //giMi.setSkipped(true);
                coMi.setAborted();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //giMi.setSkipped(true);
                coMi.setAborted();
                coMi.setCancelled();
                frame.dispose();
            }
        });
        frame.setVisible(true);
    }
    
    @Override
    public void dispose() {
        delay(200);
        frame.dispose();
    }
    
    @Override
    public void setLabelLeft(String substring, int lv, boolean check) {
        if (lv == 1) optnumLabel.setText(substring);
        if (lv == 0) posLabel.setText(substring);
    }
    
    @Override
    public void setText(String substring) {
        knotLabel.setText(substring);
    }
    
}
