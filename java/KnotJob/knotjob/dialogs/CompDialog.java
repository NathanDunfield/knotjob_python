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

package knotjob.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import knotjob.diagrams.GraphicDiagram;

/**
 *
 * @author Dirk
 */
public class CompDialog extends JDialog {
    
    private final JFrame frame;
    private final ArrayList<Color> colors;
    private final ArrayList<Boolean> showComps;
    private final ArrayList<Boolean> orieComps;
    private final ArrayList<JButton> colorButtons;
    private final ArrayList<JCheckBox> visibleBoxes;
    private final ArrayList<JCheckBox> orientBoxes;
    private final boolean orients;
    private boolean okay;
    
    public CompDialog(JFrame frma, String title, boolean bo, GraphicDiagram gDiag, boolean or) {
        super(frma,title,bo);
        frame = frma;
        colors = new ArrayList<Color>();
        for (Color col : gDiag.getColors()) colors.add(col);
        showComps = new ArrayList<Boolean>();
        for (Boolean bol : gDiag.getShownComponents()) showComps.add(bol);
        orieComps = new ArrayList<Boolean>();
        for (Boolean bol : gDiag.getOrientComponents()) orieComps.add(bol);
        colorButtons = new ArrayList<JButton>();
        visibleBoxes = new ArrayList<JCheckBox>();
        orientBoxes = new ArrayList<JCheckBox>();
        orients = or;
        okay = false;
    }
    
    public void setUpStuff() {
        this.setSize(450,400);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        JPanel centerPanel = new JPanel();
        JScrollPane scrollPanel = new JScrollPane(centerPanel);
        int size = colors.size() + 2;
        centerPanel.setLayout(new GridLayout(size,3));
        ActionListener action = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton btn = (JButton) e.getSource();
                int c = colorButtons.indexOf(btn);
                JColorChooser aChooser = new JColorChooser();
                aChooser.setColor(colors.get(c));
                AbstractColorChooserPanel[] panels = aChooser.getChooserPanels();
                for (AbstractColorChooserPanel accp : panels) {
                    if (!"RGB".equals(accp.getDisplayName())) aChooser.removeChooserPanel(accp);
                }
                ColorDialog dial = new ColorDialog(frame,"Choose Component Color", true, aChooser);
                dial.setUpStuff();
                if (dial.isOkay()) {
                    colors.set(c, aChooser.getColor());
                    colorButtons.get(c).setBackground(aChooser.getColor());
                }
            }
        };
        for (int i = 0; i < 3; i++) centerPanel.add(new JPanel());
        for (int i = 0; i < colors.size(); i++) {
            Color col = colors.get(i);
            JButton colButton = new JButton("Color");
            colButton.setBackground(col);
            colButton.setBorderPainted(false);
            colButton.setOpaque(true);
            colButton.setForeground(Color.WHITE);
            colButton.addActionListener(action);
            JPanel butPanel = new JPanel();
            butPanel.add(colButton);
            centerPanel.add(butPanel);
            colorButtons.add(colButton);
            JCheckBox visibleBox = new JCheckBox("Visible", showComps.get(i));
            JPanel visPanel = new JPanel();
            visPanel.add(visibleBox);
            visibleBoxes.add(visibleBox);
            centerPanel.add(visPanel);
            JCheckBox orientBox = new JCheckBox("Show orientation", orieComps.get(i));
            orientBox.setEnabled(orients);
            JPanel oriPanel = new JPanel();
            oriPanel.add(orientBox);
            orientBoxes.add(orientBox);
            centerPanel.add(oriPanel);
        }
        for (int i = 0; i < 3; i++) centerPanel.add(new JPanel());
        centerPanel.setPreferredSize(new Dimension(300,40 * colors.size()));
        scrollPanel.getViewport().revalidate();
        JButton okayButton = new JButton("OK");
        JButton cancButton = new JButton("Cancel");
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okay = true;
                setVisible(false);
            }
        });
        cancButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        JPanel botPanel = new JPanel();
        botPanel.add(okayButton);
        botPanel.add(cancButton);
        this.add(scrollPanel, BorderLayout.CENTER);
        this.add(botPanel, BorderLayout.SOUTH);
        this.setVisible(true);
    }
    
    public boolean isOkay() {
        return okay;
    }
    
    public ArrayList<Color> setColors() {
        for (int i = 0; i < colorButtons.size(); i++) {
            colors.set(i, colorButtons.get(i).getBackground());
        }
        return colors;
    }
    
    public ArrayList<Boolean> setShownComponents() {
        for (int i = 0; i < showComps.size(); i++) {
            showComps.set(i, visibleBoxes.get(i).isSelected());
        }
        return showComps;
    }

    public ArrayList<Boolean> setOrientComponents() {
        for (int i = 0; i < orieComps.size(); i++) {
            orieComps.set(i, orientBoxes.get(i).isSelected());
        }
        return orieComps;
    }
    
}
