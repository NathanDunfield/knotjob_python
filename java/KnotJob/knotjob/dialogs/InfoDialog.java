/*

Copyright (C) 2020-24 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import knotjob.Options;

/**
 *
 * @author dirk
 */
public class InfoDialog extends JDialog {
    
    private final JFrame frame;
    private final JButton closeButton;
    private final Image img;
    private final Color col;
    
    public InfoDialog(JFrame fram, String title, boolean bo, Options optns) {
        super(fram,title,bo);
        frame = fram;
        img = optns.getMedImage();
        col = optns.getColor();
        closeButton = new JButton("Close");
    }
    
    public void showInfo() {
        this.setSize(350,310);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JLabel knotjobLabel = new JLabel("KnotJob");
        knotjobLabel.setFont(new Font("Sans Serif", Font.BOLD, 24));
        JPanel topPanel = new JPanel();
        topPanel.add(knotjobLabel);
        JLabel picLabel = new JLabel(new ImageIcon(img));
        JPanel medPanel = new JPanel();
        JPanel picPanel = new JPanel();
        picPanel.setBackground(Color.red);
        picPanel.setPreferredSize(new Dimension(160,106));
        picPanel.add(picLabel);
        picPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        JLabel dirkLabel = new JLabel("Written by Dirk Schütz", SwingConstants.CENTER);
        JPanel dirkPanel = new JPanel();
        dirkPanel.setLayout(new GridLayout(4, 1));
        dirkPanel.add(dirkLabel);
        JLabel versionLabel = new JLabel("Version");
        JPanel colorPanel = new JPanel();
        colorPanel.setBackground(col);
        colorPanel.setPreferredSize(new Dimension(50,20));
        colorPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        JPanel versionPanel = new JPanel();
        versionPanel.setLayout(new BorderLayout());
        versionPanel.add(versionLabel, BorderLayout.WEST);
        versionPanel.add(colorPanel, BorderLayout.EAST);
        versionPanel.setPreferredSize(new Dimension(120, 20));
        JPanel verPanel = new JPanel();
        verPanel.add(versionPanel);
        dirkPanel.add(verPanel);
        JPanel licPanel = new JPanel();
        JPanel licencePanel = new JPanel();
        JLabel licOneLabel = new JLabel("Licenced under", SwingConstants.CENTER);// ");
        JLabel licTwoLabel = new JLabel("GNU General Public License version 3", 
                SwingConstants.CENTER);
        licencePanel.setLayout(new GridLayout(2, 1));
        licencePanel.add(licOneLabel);
        licencePanel.add(licTwoLabel);
        licPanel.add(licencePanel);
        dirkPanel.add(licPanel);
        JLabel thanksLabel = new JLabel("Thanks to : ", SwingConstants.CENTER);
        dirkPanel.add(thanksLabel);
        JPanel thxPanel = new JPanel();
        JPanel thanksPanel = new JPanel();
        JLabel[] thxLabels = new JLabel[] {new JLabel("Nathan Dunfield", SwingConstants.CENTER),
                                           new JLabel("Michael Gasser", SwingConstants.CENTER),
                                           new JLabel("Sherry Gong", SwingConstants.CENTER),
                                           new JLabel("Davide Gurnari", SwingConstants.CENTER),
                                           new JLabel("Charles Livingston", SwingConstants.CENTER),
                                           new JLabel("Allison Moore", SwingConstants.CENTER),
                                           new JLabel("Qiuyu Ren", SwingConstants.CENTER)
                                          };
        thanksPanel.setLayout(new GridLayout(thxLabels.length, 1));
        for (JLabel lab : thxLabels) thanksPanel.add(lab);
        thxPanel.add(thanksPanel);
        dirkPanel.setPreferredSize(new Dimension(200, 200));
        medPanel.setLayout(new BorderLayout());
        JPanel middlePanel = new JPanel();
        middlePanel.add(picPanel);
        medPanel.add(middlePanel, BorderLayout.NORTH);
        medPanel.add(dirkPanel, BorderLayout.CENTER);
        medPanel.add(thxPanel, BorderLayout.SOUTH);
        this.add(topPanel, BorderLayout.NORTH);
        this.add(new JScrollPane(medPanel), BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.setVisible(true);
    }
    
}
