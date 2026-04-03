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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Dirk
 */
public class CompFilterDialog extends JDialog {
    
    protected final JFrame frame;
    protected final JSpinner lowerSpinner;
    protected final JSpinner upperSpinner;
    protected final SpinnerNumberModel lowerModel;
    protected final SpinnerNumberModel upperModel;
    protected final JCheckBox aboveBox;
    protected final JCheckBox reduced;
    protected final JCheckBox odded;
    protected final JCheckBox slted;
    protected final int totalmax;
    private final boolean reduc;
    protected boolean bddAbove;
    protected int lowerBound;
    protected int upperBound;
    protected boolean okay;
    
    public CompFilterDialog(JFrame fram, String title, boolean bo) {
        super(fram,title,bo);
        frame = fram;
        lowerModel = new SpinnerNumberModel(1,1,100,1);
        lowerSpinner = new JSpinner(lowerModel);
        upperModel = new SpinnerNumberModel(4,1,100,1);
        upperSpinner = new JSpinner(upperModel);
        aboveBox = new JCheckBox("Bounded Above");
        aboveBox.setSelected(true);
        reduced = new JCheckBox("reduced");
        odded = new JCheckBox("odd");
        slted = new JCheckBox("sl_3");
        okay = false;
        totalmax = 100;
        reduc = false;
    }
    
    public CompFilterDialog(JFrame fram, String title, boolean bo, int l, int u) {
        super(fram,title,bo);
        frame = fram;
        lowerModel = new SpinnerNumberModel(l,l,u,1);
        lowerSpinner = new JSpinner(lowerModel);
        upperModel = new SpinnerNumberModel(l+2,1,u,1);
        upperSpinner = new JSpinner(upperModel);
        aboveBox = new JCheckBox("Bounded Above");
        aboveBox.setSelected(true);
        reduced = new JCheckBox("reduced");
        odded = new JCheckBox("odd");
        slted = new JCheckBox("sl_3");
        totalmax = u;
        okay = false;
        reduc = true;
    }
    
    public CompFilterDialog(JFrame fram, String title, boolean bo, int lb, int ub, boolean red, 
            int jump) {
        super(fram,title,bo);
        frame = fram;
        int min = -2;
        int max = 2;
        if (min < lb) min = lb;
        if (max > ub) max = ub;
        lowerModel = new SpinnerNumberModel(min, lb, ub, jump);
        lowerSpinner = new JSpinner(lowerModel);
        upperModel = new SpinnerNumberModel(max, lb, ub, jump);
        upperSpinner = new JSpinner(upperModel);
        aboveBox = new JCheckBox("Bounded Above");
        aboveBox.setSelected(true);
        reduced = new JCheckBox("reduced");
        odded = new JCheckBox("odd");
        slted = new JCheckBox("sl_3");
        totalmax = ub;
        okay = false;
        reduc = red;
    }
    
    public boolean isOkay() {
        return okay;
    }
    
    public boolean getBoundedAbove() {
        return bddAbove;
    }
    
    public int getLowerBound() {
        return lowerBound;
    }
    
    public int getUpperBound() {
        return upperBound;
    }
    
    public boolean isReduced() {
        return reduced.isSelected();
    }
    
    public boolean isOdd() {
        return odded.isSelected();
    }
    
    public boolean isSlT() {
        return slted.isSelected();
    }
    
    public void setupDialog() {
        setSize(400,250);
        setLocationRelativeTo(frame);
        setResizable(false);
        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        JButton okayButton = new JButton("OK");
        JButton cancButton = new JButton("Cancel");
        buttonPanel.add(okayButton);
        buttonPanel.add(cancButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
        JPanel infoPanel = new JPanel();
        int lines = 3;
        if (reduc) lines++;
        infoPanel.setLayout(new GridLayout(lines,1));
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
        JPanel reducPanel = new JPanel();
        reducPanel.add(reduced);
        reducPanel.add(odded);
        reducPanel.add(slted);
        infoPanel.add(topPanel);
        infoPanel.add(midPanel);
        infoPanel.add(lowPanel);
        if (reduc) infoPanel.add(reducPanel);
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
        setVisible(true);
        bddAbove = aboveBox.isSelected();
        lowerBound = (int) lowerSpinner.getValue();
        upperBound = (int) upperSpinner.getValue();
    }
    
    public void setLowerBound(int b) {
        lowerSpinner.setValue(b);
    }
    
    public void setUpperBound(int b) {
        upperSpinner.setValue(b);
    }
    
}
