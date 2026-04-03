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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Dirk
 */
public class RotateDialog extends JDialog {
    
    public ArrayList<Integer> angle;
    private final JFrame frame;
    private final ArrayList<JSlider> theSliders;
    private final ArrayList<String> theStrings;
    private final ArrayList<JLabel> theLabels;
    
    public RotateDialog(JFrame fram, String name, boolean bo) {
        super(fram,name,bo);
        angle = new ArrayList<Integer>();
        frame = fram;
        theSliders = new ArrayList<JSlider>();
        theStrings = new ArrayList<String>();
        theLabels = new ArrayList<JLabel>();
    }
    
    public void setUpStuff(String[] labels) {
        int ysize = 30 + 100 * labels.length;
        this.setSize(400, ysize);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(labels.length, 1));
        for (String label : labels) {
            theStrings.add(label);
            JPanel labelPanel = new JPanel();
            JLabel topLabel = new JLabel(label+"0 degrees");
            theLabels.add(topLabel);
            labelPanel.add(topLabel);
            JSlider rotateSlider = new JSlider(JSlider.HORIZONTAL, -180, 180, 0);
            theSliders.add(rotateSlider);
            rotateSlider.setMajorTickSpacing(45);
            rotateSlider.setMinorTickSpacing(15);
            rotateSlider.setPaintTicks(true);
            rotateSlider.setPreferredSize(new Dimension(360,20));
            rotateSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int i = theSliders.indexOf(rotateSlider);
                    theLabels.get(i).setText(theStrings.get(i)+rotateSlider.getValue()+" degrees");
                }
            });
            labelPanel.add(rotateSlider);
            centerPanel.add(labelPanel);
        }
        JButton okayButton = new JButton("OK");
        JButton cancButton = new JButton("Cancel");
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (JSlider rotateSlider : theSliders) {
                    angle.add(rotateSlider.getValue());
                }
                dispose();
            }
        });
        cancButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel botPanel = new JPanel();
        botPanel.add(okayButton);
        botPanel.add(cancButton);
        //this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(botPanel, BorderLayout.SOUTH);
        this.setVisible(true);
    }
    
}
