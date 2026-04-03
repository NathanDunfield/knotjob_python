/*

Copyright (C) 2023-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import knotjob.Options;

/**
 *
 * @author Dirk
 */
public class BLTFilterDialog extends CompFilterDialog {
    
    private final JSpinner charSpinner;
    private final SpinnerListModel charModel;
    private final JRadioButton unrBLT = new JRadioButton("unreduced BLT");
    private final JRadioButton redBLT = new JRadioButton("reduced BLT");
    private final JRadioButton sltOne = new JRadioButton("X^3-1");
    private final JRadioButton sltTwo = new JRadioButton("X^3-X");
    private final JRadioButton sltThr = new JRadioButton("X^3-X^2");
    
    public BLTFilterDialog(JFrame fram, String title, boolean bo, Options optns) {
        super(fram, title, bo, 1, 65536);
        ArrayList<Integer> theChars = new ArrayList<Integer>();
        theChars.add(0);
        for (int p : optns.getPrimes()) theChars.add(p);
        charModel = new SpinnerListModel(theChars);
        charSpinner = new JSpinner(charModel);
    }
    
    @Override
    public void setupDialog() {
        setSize(400,450);
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
        int lines = 10;
        infoPanel.setLayout(new GridLayout(lines, 1));
        JPanel labelPanel = new JPanel();
        labelPanel.add(new JLabel("Number of Pages"));
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
        JPanel charPanel = new JPanel();
        JLabel charLabel = new JLabel("Characteristic : ");
        charPanel.add(charLabel);
        charPanel.add(charSpinner);
        charModel.setValue(2);
        charSpinner.setPreferredSize(new Dimension(54,20));
        Dimension dim = new Dimension(160, 40);
        unrBLT.setPreferredSize(dim);
        redBLT.setPreferredSize(dim);
        sltOne.setPreferredSize(dim);
        sltTwo.setPreferredSize(dim);
        sltThr.setPreferredSize(dim);
        JPanel unrBLTPanel = new JPanel();
        unrBLTPanel.add(unrBLT);
        unrBLT.setSelected(true);
        JPanel redBLTPanel = new JPanel();
        redBLTPanel.add(redBLT);
        JPanel sltOnePanel = new JPanel();
        sltOnePanel.add(sltOne);
        JPanel sltTwoPanel = new JPanel();
        sltTwoPanel.add(sltTwo);
        JPanel sltThrPanel = new JPanel();
        sltThrPanel.add(sltThr);
        ButtonGroup group = new ButtonGroup();
        group.add(unrBLT);
        group.add(redBLT);
        group.add(sltOne);
        group.add(sltTwo);
        group.add(sltThr);
        infoPanel.add(labelPanel);
        infoPanel.add(topPanel);
        infoPanel.add(midPanel);
        infoPanel.add(lowPanel);
        infoPanel.add(charPanel);
        infoPanel.add(unrBLTPanel);
        infoPanel.add(redBLTPanel);
        infoPanel.add(sltOnePanel);
        infoPanel.add(sltTwoPanel);
        infoPanel.add(sltThrPanel);
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

    public String getCharacteristic() {
        return String.valueOf(charSpinner.getValue());
    }

    public String boundString() {
        String bound = String.valueOf(lowerBound);
        String redux = "pages";
        if (bddAbove && upperBound == lowerBound) {
            bound = bound +"="+redux;
        }
        else {
            bound = bound +"<="+redux;
        }
        if (bddAbove && upperBound > lowerBound) bound = bound+"<="+upperBound;
        return bound;
    }

    public int getSeqType() {
        if (unrBLT.isSelected()) return 0;
        if (redBLT.isSelected()) return 1;
        if (sltOne.isSelected()) return 2;
        if (sltTwo.isSelected()) return 3;
        if (sltThr.isSelected()) return 4;
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public String getFilterName(int typ) {
        switch (typ) {
            case 0 -> {
                return "unrBLT c="+this.getCharacteristic()+" "+this.boundString();
            }
            case 1 -> {
                return "redBLT c="+this.getCharacteristic()+" "+this.boundString();
            }
            case 2 -> {
                return "X^3-1 c="+this.getCharacteristic()+" "+this.boundString();
            }
            case 3 -> {
                return "X^3-X c="+this.getCharacteristic()+" "+this.boundString();
            }
            case 4 -> {
                return "X^3-X^2 c="+this.getCharacteristic()+" "+this.boundString();
            }
        }
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
