/*

Copyright (C) 2019-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Dirk
 */
public class SFilterDialog extends CompFilterDialog {
    
    protected final int totalmin;
    protected final JCheckBox belowBox;
    protected boolean bddBelow;
    
    
    public SFilterDialog(JFrame fram, String title, boolean bo, int lb, int ub, boolean red, 
            int jump) {
        super(fram, title, bo, lb, ub, red, jump);
        totalmin = lb;
        belowBox = new JCheckBox("Bounded Below");
        belowBox.setSelected(true);
    }
    
    public boolean getBoundedBelow() {
        return bddBelow;
    }
    
    @Override
    public void setupDialog() {
        setSize(400,250);
        setLocationRelativeTo(frame);
        setResizable(false);
        setLayout(new BorderLayout());
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(4,1));
        setupButtonPanel();
        setupSInvariantOptions(infoPanel);
        setAllVisible();
    }
    
    protected void setAllVisible() {
        setVisible(true);
        bddAbove = aboveBox.isSelected();
        bddBelow = belowBox.isSelected();
        lowerBound = (int) lowerSpinner.getValue();
        upperBound = (int) upperSpinner.getValue();
    }

    protected void setupButtonPanel() {
        JPanel buttonPanel = new JPanel();
        JButton okayButton = new JButton("OK");
        JButton cancButton = new JButton("Cancel");
        buttonPanel.add(okayButton);
        buttonPanel.add(cancButton);
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okay = true;
                dispose();
            }
        });
        cancButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        this.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    protected void setupSInvariantOptions(JPanel infoPanel) {
        JPanel vtpPanel = new JPanel();
        vtpPanel.add(belowBox);
        JPanel topPanel = new JPanel();
        JLabel lowBoundLabel = new JLabel("Lower Bound : ");
        topPanel.add(lowBoundLabel);
        topPanel.add(lowerSpinner);
        JPanel midPanel = new JPanel();
        midPanel.add(aboveBox);
        JPanel lowPanel = new JPanel();
        JLabel uppBoundLabel = new JLabel("Upper Bound : ");
        lowPanel.add(uppBoundLabel);
        lowPanel.add(upperSpinner);
        infoPanel.add(vtpPanel);
        infoPanel.add(topPanel);
        infoPanel.add(midPanel);
        infoPanel.add(lowPanel);
        this.add(infoPanel, BorderLayout.CENTER);
        lowerSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!aboveBox.isSelected()) return;
                int val = (int) lowerSpinner.getValue();
                int up = (int) upperSpinner.getValue();
                if (val > up) lowerSpinner.setValue(up);
                lowerModel.setMaximum(up);
                upperModel.setMinimum((int)lowerSpinner.getValue());
            }
        });
        upperSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = (int) upperSpinner.getValue();
                int bt = (int) lowerSpinner.getValue();
                if (val < bt) upperSpinner.setValue(bt);
                lowerModel.setMaximum((int)upperSpinner.getValue());
                upperModel.setMinimum(bt);
            }
        });
        belowBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                boolean ch = belowBox.isSelected();
                if (ch) {
                    int lval = (int) lowerSpinner.getValue();
                    int uval = (int) upperSpinner.getValue();
                    if (lval > uval) upperSpinner.setValue(lval);
                    lowerModel.setMaximum((int) upperSpinner.getValue());
                    upperModel.setMinimum(lval);
                    lowerSpinner.setEnabled(true);
                }
                else {
                    lowerSpinner.setEnabled(false);
                    upperModel.setMinimum(totalmin);
                }
            }
        });
        aboveBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                boolean ch = aboveBox.isSelected();
                if (ch) {
                    int lval = (int) lowerSpinner.getValue();
                    int uval = (int) upperSpinner.getValue();
                    if (lval > uval) lowerSpinner.setValue(uval);
                    lowerModel.setMaximum(uval);
                    upperModel.setMinimum((int) lowerSpinner.getValue());
                    upperSpinner.setEnabled(true);
                }
                else {
                    upperSpinner.setEnabled(false);
                    lowerModel.setMaximum(totalmax);
                }
            }
        });
    }
    
}
