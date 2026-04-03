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
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Image;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import knotjob.Options;

/**
 *
 * @author Dirk
 */
public class DiagramFrame extends JFrame {
    
    private final JPanel buttPane;
    public final JButton closeButton;
    public final JButton exportButton;
    private final JLabel zoomLabel;
    public final JSlider zoomSlider;
    public final JPanel infoPanel;
    private final JLabel infoLabe;
    public final JLabel infoLabel;
    private final JPanel topPanel;
    private final Image img;
    public final JCheckBox minimizeEng;
    public final JButton rotateButton;
    public final JButton compButton;
    
    public DiagramFrame(String name, Options opts) {
        super(name);
        img = opts.getImage();
        buttPane = new JPanel();
        closeButton = new JButton("Close");
        exportButton = new JButton("Export");
        zoomLabel = new JLabel("Zoom : ");
        zoomSlider = new JSlider(JSlider.HORIZONTAL, 1, 81, 1);
        buttPane.add(zoomLabel);
        buttPane.add(zoomSlider);
        buttPane.add(exportButton);
        buttPane.add(closeButton);
        infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(1,2));
        infoLabe = new JLabel("Circle Packing : ", SwingConstants.RIGHT);
        infoLabel = new JLabel("0%");
        infoPanel.add(infoLabe);
        infoPanel.add(infoLabel);
        topPanel = new JPanel();
        rotateButton = new JButton("Rotate");
        minimizeEng = new JCheckBox("Minimize Energy", false);
        compButton = new JButton("Components");
        topPanel.add(minimizeEng);
        topPanel.add(rotateButton);
        topPanel.add(compButton);
    }
    
    public void addBasics() {
        if (img != null) setIconImage(img);
        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());
        c.add(buttPane, BorderLayout.SOUTH);
        c.add(infoPanel, BorderLayout.CENTER);
        c.add(topPanel, BorderLayout.NORTH);
    }
}
