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

package knotjob.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import knotjob.Options;
import knotjob.diagrams.SaveDiagram;
import knotjob.diagrams.diagrams3d.GraphicalDiagram3D;
import knotjob.diagrams.diagrams3d.LinkDiagram3D;

/**
 *
 * @author Dirk
 */
public class DiagramFrame3D extends JFrame {
    
    private final JPanel buttPane;
    public final JButton closeButton;
    public final JButton exportButton;
    private final JLabel thickLabel;
    public final JSlider thickSlider;
    private final JPanel topPanel;
    private final Image img;
    public final JCheckBox minimizeEng;
    public final JCheckBox showEdges;
    public final JButton rotateButton;
    public final JButton subdivButton;
    public final JButton compButton;
    private final Options opts;
    private final String name;
    
    public DiagramFrame3D(String nme, String nm, Options ops) {
        super(nme);
        img = ops.getImage();
        opts = ops;
        name = nm;
        buttPane = new JPanel();
        closeButton = new JButton("Close");
        exportButton = new JButton("Export");
        thickLabel = new JLabel("Thickness : ");
        thickSlider = new JSlider(JSlider.HORIZONTAL, 250, 3000, 1000);
        buttPane.add(thickLabel);
        buttPane.add(thickSlider);
        buttPane.add(exportButton);
        buttPane.add(closeButton);
        topPanel = new JPanel();
        showEdges = new JCheckBox("Show Edges", false);
        minimizeEng = new JCheckBox("Minimize Energy", false);
        rotateButton = new JButton("Rotate");
        compButton = new JButton("Components");
        subdivButton = new JButton("Subdivide");
        topPanel.add(minimizeEng);
        topPanel.add(rotateButton);
        topPanel.add(compButton);
        topPanel.add(subdivButton);
        topPanel.add(showEdges);
    }
    
    public void addBasics() {
        if (img != null) setIconImage(img);
        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());
        c.add(buttPane, BorderLayout.SOUTH);
        c.add(topPanel, BorderLayout.NORTH);
    }
    
    public void setButtons(LinkDiagram3D diag3D, int count) {
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        GraphicalDiagram3D dig = new GraphicalDiagram3D(diag3D);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                dispose();
                dig.stopMoving();
            }
        });
        this.closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
                dig.stopMoving();
            }
        });
        this.subdivButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                minimizeEng.setSelected(false);
                dig.stopMoving();
                subdivide(diag3D, dig);
                dig.repaint();
            }
        });
        this.compButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                minimizeEng.setSelected(false);
                dig.stopMoving();
                components(dig);
                
                dig.repaint();
            }
        });
        this.thickSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double factor = thickSlider.getValue()/10000d;
                dig.setThickness(factor);
                dig.repaint();
            }
        });
        this.rotateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dig.stopMoving();
                minimizeEng.setSelected(false);
                rotate(dig);
                dig.repaint();
            }
        });
        this.showEdges.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JCheckBox theBox = (JCheckBox) ae.getSource();
                dig.setEdgesVisible(theBox.isSelected());
                dig.repaint();
            }
        });
        this.exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                export3DDiagram(dig);
            }
        });
        this.setVisible(true);
        dig.runOnce(50);
        try {
            dig.mover.join();
        } catch (InterruptedException ex) {
            //Logger.getLogger(ShowDiagram.class.getName()).log(Level.SEVERE, null, ex);
        }
        Container c = this.getContentPane();
        c.setBackground(Color.WHITE);
        c.add(dig, BorderLayout.CENTER);
        this.revalidate();
        this.minimizeEng.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JCheckBox theBox = (JCheckBox) ae.getSource();
                dig.minimizeEng(theBox.isSelected());
            }
        });
    }
    
    private void subdivide(LinkDiagram3D diag3D, GraphicalDiagram3D dig) {
        SubdivDialog fram = new SubdivDialog(this, "Subdivide", true, dig);
                fram.setupStuff();
                int i = fram.theValue();
                if (i == 0) diag3D.removeVertices(fram.getRemoveValue());
                if (i == 1) diag3D.subdivide(fram.getSubdivValue());
    }

    private void components(GraphicalDiagram3D dig) {
        CompDialog fram = new CompDialog(this, "Components", true, dig, false);
        fram.setUpStuff();
        if (fram.isOkay()) {
            dig.setColors(fram.setColors());
            dig.setShownComponents(fram.setShownComponents());
        }
    }
    
    private void rotate(GraphicalDiagram3D dig) {
        RotateDialog fram = new RotateDialog(this, "Rotate Link by ", true);
        fram.setUpStuff(new String[] {"Rotate z-axis by ", "Rotate y-axis by ", 
                "Rotate x-axis by "});
        if (!fram.angle.isEmpty()) {
            dig.rotateDiagram(fram.angle.get(0), 0);
            dig.rotateDiagram(fram.angle.get(1), 1);
            dig.rotateDiagram(fram.angle.get(2), 2);
        }
    }
    
    private void export3DDiagram(GraphicalDiagram3D dig) {
        if (this.minimizeEng.isSelected()) {
            this.minimizeEng.setSelected(false);
            dig.stopMoving();
        }
        ExportDialog expDiag = new ExportDialog(this, "Export as ...", new int[] {0, 1, 3});
        int choice = expDiag.getValue();
        if (choice == 0) { // export as tikzpicture
            ArrayList<String> theStrings = dig.printCoordinates();
            SaveDiagram.saveCommands("img_3d_"+name, ".tex", theStrings, opts, this);
        }
        if (choice == 1) {
            ArrayList<String> theStrings = dig.linkDiagram.printCoordinates();
            SaveDiagram.saveCommands("img_"+name, ".pov", theStrings, opts, this);
        }
        if (choice == 3) {
            ArrayList<String> theStrings = dig.linkDiagram.printVertices();
            SaveDiagram.saveCommands("vts_"+name, ".csv", theStrings, opts, this);
        }
    }
    
}
