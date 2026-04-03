/*

Copyright (C) 2019-21 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author Dirk
 */
public class MirrorLink extends JDialog {
    
    private final JFrame frame;
    private final DefaultListModel<String> listModel;
    private final JList<String> listlinks;
    private final JCheckBox reducer;
    private final JCheckBox rational;
    private final JCheckBox odder;
    private final JSpinner spinn;
    private final SpinnerNumberModel twistSpinner;
    private final boolean reduce;
    private final boolean stuff;
    private int chosen;
    
    public MirrorLink(JFrame fram, String title, boolean bo, boolean red, DefaultListModel<String> model) {
        super(fram,title,bo);
        frame = fram;
        listModel = model;
        listlinks = new JList<>(listModel);
        chosen = -1;
        reduce = red;
        stuff = false;
        reducer = new JCheckBox("reduced");
        rational = new JCheckBox("rational");
        odder = new JCheckBox("odd");
        twistSpinner = null;
        spinn = null;
    }
    
    public MirrorLink(JFrame fram, String title, DefaultListModel<String> model) {
        super(fram, title, true);
        frame = fram;
        listModel = model;
        listlinks = new JList<>(listModel);
        chosen = -1;
        reduce = true;
        stuff = true;
        reducer = null;
        rational = null;
        odder = null;
        twistSpinner = new SpinnerNumberModel(0, -30, 30, 1);
        spinn = new JSpinner(twistSpinner);
    }
    
    public void setUpStuff() {
        this.setSize(300,400);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        JLabel topLabel = new JLabel("Please choose a link.");
        topPanel.add(topLabel);
        listlinks.setSelectedIndex(0);
        JScrollPane pane = new JScrollPane(listlinks);
        int ysize = 280;
        if (reduce) ysize = 260;
        pane.setPreferredSize(new Dimension(160,ysize));
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel centerPanel = new JPanel();
        centerPanel.add(pane);
        JPanel lowPanel = new JPanel();
        if (stuff) {
            lowPanel.add(new JLabel("Twists :"));
            lowPanel.add(spinn);
        }
        else {
            lowPanel.add(rational);
            lowPanel.add(reducer);
            lowPanel.add(odder);
        }
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        if (reduce) mainPanel.add(lowPanel, BorderLayout.SOUTH);
        JButton okayButton = new JButton("OK");
        JButton cancButton = new JButton("Cancel");
        JPanel botPanel = new JPanel();
        botPanel.add(okayButton);
        botPanel.add(cancButton);
        this.add(botPanel, BorderLayout.SOUTH);
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(topPanel, BorderLayout.NORTH);
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chosen = listlinks.getSelectedIndex();
                setVisible(false);
            }
        });
        cancButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        this.setVisible(true);
    }
    
    public int getChosen() {
        return chosen;
    }
    
    public int[] getAllChosen() {
        return listlinks.getSelectedIndices();
    }
    
    public boolean isRational() {
        return rational.isSelected();
    }
    
    public boolean isReduced() {
        return reducer.isSelected();
    }

    public boolean isOdd() {
        return odder.isSelected();
    }
    
    public int getTwists() {
        return (int) spinn.getValue();
    }
}
