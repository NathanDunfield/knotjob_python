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

package knotjob.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import knotjob.Options;
import knotjob.diagrams.SaveDiagram;
import knotjob.diagrams.diagrams3d.LinkDiagram3D;
import knotjob.diagrams.griddiagrams.GraphicalGridDiagram;
import knotjob.diagrams.griddiagrams.GridDiagram;
import knotjob.dialogs.DiagramFrame3D;
import knotjob.dialogs.ExportDialog;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class GridDiagramFrame extends JFrame implements MouseListener, MouseMotionListener {
    
    private final JCheckBox showLink;
    private final JCheckBox showOX;
    private final JCheckBox whoseAbove;
    private final JCheckBox showGrid;
    private final JButton closeButton;
    private final JButton exportButton;
    private final JButton improveButton;
    private final JSlider zoomSlider;
    private final JFrame frame;
    private final JPanel infoPanel;
    private final Options opts;
    private final LinkData theLink;
    private JScrollPane scroller;
    private GraphicalGridDiagram graphGrid;
    private GridDiagram theGrid;
    
    public GridDiagramFrame(String name, JFrame fram, Options ops, LinkData tlnk) {
        super(name);
        opts = ops;
        theLink = tlnk;
        showLink = new JCheckBox("Show Link",true);
        showOX = new JCheckBox("Show O/X",true);
        whoseAbove = new JCheckBox("Link above O/X",true);
        showGrid = new JCheckBox("Show Grid",true);
        closeButton = new JButton("Close");
        exportButton = new JButton("Export");
        improveButton = new JButton("Improve Grid");
        zoomSlider = new JSlider(JSlider.HORIZONTAL, 10, 50, 24);
        frame = fram;
        infoPanel = new JPanel();
    }
    
    public void setUp() {
        setSize(800,600);
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(frame);
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new GridLayout(1,4));
        checkBoxPanel.add(showLink);
        checkBoxPanel.add(showOX);
        checkBoxPanel.add(whoseAbove);
        checkBoxPanel.add(showGrid);
        JPanel buttPane = new JPanel();
        JLabel zoomLabel = new JLabel("Zoom : ");
        buttPane.add(improveButton);
        buttPane.add(zoomLabel);
        buttPane.add(zoomSlider);
        buttPane.add(exportButton);
        buttPane.add(closeButton);
        JLabel infoLabel = new JLabel("Calculating Grid Diagram", SwingConstants.CENTER);
        infoPanel.setLayout(new GridLayout(1,1));
        infoPanel.add(infoLabel);
        add(checkBoxPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
        add(buttPane, BorderLayout.SOUTH);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        });
        setVisible(true);
    }

    public void setButtons(GridDiagram grid) {
        theGrid = grid;
        graphGrid = new GraphicalGridDiagram(grid);
        graphGrid.setPreferredSize(new Dimension(200+graphGrid.boxsize() * grid.size(), 
                200+graphGrid.boxsize() * grid.size()));
        scroller = new JScrollPane(graphGrid);
        scroller.getViewport().setBackground(Color.WHITE);
        Container c = getContentPane();
        c.remove(infoPanel);
        c.add(scroller, BorderLayout.CENTER);
        revalidate();
        graphGrid.addMouseMotionListener(this);
        graphGrid.addMouseListener(this);
        showLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphGrid.setShowlink(showLink.isSelected());
                graphGrid.repaint();
                scroller.getViewport().revalidate();
            }
        });// */
        showOX.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphGrid.setShowOX(showOX.isSelected());
                graphGrid.repaint();
                scroller.getViewport().revalidate();
            }
        });// */
        showGrid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphGrid.setShowGrid(showGrid.isSelected());
                graphGrid.repaint();
                scroller.getViewport().revalidate();
            }
        });// */
        whoseAbove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphGrid.setSymbfirst(whoseAbove.isSelected());
                graphGrid.repaint();
                scroller.getViewport().revalidate();
            }
        });// */
        zoomSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                graphGrid.setBoxsize(zoomSlider.getValue());
                graphGrid.setPreferredSize(new Dimension(200+graphGrid.boxsize() * grid.size(), 
                        200+graphGrid.boxsize() * grid.size()));
                graphGrid.repaint();
                scroller.getViewport().revalidate();
            }
        });
        improveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                graphGrid.tryToImprove();
                graphGrid.setPreferredSize(new Dimension(200+graphGrid.boxsize() * grid.size(), 
                        200+graphGrid.boxsize() * grid.size()));
                graphGrid.repaint();
                scroller.getViewport().revalidate();
            }
        });
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                exportDiagram();
            }
        });
    }
    
    public int zoomFactor() {
        return zoomSlider.getValue();
    }

    private void exportDiagram() {
        ExportDialog expDiag = new ExportDialog(this, "Export as ...", new int[] {0, 2});
        int choice = expDiag.getValue();
        if (choice == 0) saveAsTikz();
        if (choice == 2) exportTo3D();
    }
    
    private void saveAsTikz() {
        ArrayList<String> theStrings = graphGrid.printCoordinates();
        SaveDiagram.saveCommands("grid_"+theLink.name, ".tex", theStrings, opts, frame);
    }
    
    private void exportTo3D() {
        DiagramFrame3D anotherDig = new DiagramFrame3D(frame.getTitle()+" - 3D", theLink.name, opts);
        anotherDig.setSize(700,800);
        anotherDig.setResizable(false);
        anotherDig.setLocationRelativeTo(frame);
        anotherDig.addBasics();
        LinkDiagram3D diag3D = new LinkDiagram3D(theGrid, 
                theLink.chosenLink().girthMinimize().onlyFirstPart());
        anotherDig.setButtons(diag3D, 1);
    }
    
    
    @Override
    public void mouseDragged(MouseEvent me) {
        graphGrid.moveEdge(me.getX(), me.getY());
        graphGrid.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        graphGrid.detectEdge(me.getX(), me.getY());
        graphGrid.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        if (me.getClickCount() == 2 && me.getButton() == MouseEvent.BUTTON1) {
            graphGrid.tryToStabilize(me.getX(), me.getY());
            scroller.getViewport().revalidate();
            return;
        }
        if (me.getButton() == MouseEvent.BUTTON1) {
            graphGrid.tryCancel(me.getX(), me.getY());
        }
        if (me.getButton() == MouseEvent.BUTTON3) graphGrid.flipPreference();
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        graphGrid.release();
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }
    
}
