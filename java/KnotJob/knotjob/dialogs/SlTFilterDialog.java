/*

Copyright (C) 2025 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
public class SlTFilterDialog extends CompFilterDialog {
    
    private final JComboBox<String> wSpinner;
    private final JComboBox<String> wsqSpinner;
    private final JRadioButton typeX = new JRadioButton("X^3 - X - w");
    private final JRadioButton typeXSq = new JRadioButton("X^3 - X^2 - w");
    protected final JCheckBox relPages = new JCheckBox("relevant pages");
    protected boolean relevant;
    
    public SlTFilterDialog(JFrame fram, String title, boolean bo) {
        super(fram, title, bo, 1, 65536);
        String[] theWs = new String[17];
        theWs[0] = "t";
        for (int i = 1; i < 10; i++) theWs[i] = String.valueOf(i);
        theWs[10] = "\u221A-1";
        theWs[11] = "\u221A2";
        theWs[12] = "\u221A3";
        theWs[13] = "\u221A5";
        theWs[14] = "\u221A6";
        theWs[15] = "\u221A7";
        theWs[16] = "\u221A8";
        String[] theWSqs = new String[2];
        theWSqs[0] = "1";
        theWSqs[1] = "-1";
        wSpinner = new JComboBox<String>(theWs);
        wsqSpinner = new JComboBox<String>(theWSqs);
    }
    
    @Override
    public void setupDialog() {
        setSize(280,300);
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
        int lines = 7;
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
        Dimension dim = new Dimension(140, 40);
        typeX.setPreferredSize(dim);
        typeXSq.setPreferredSize(dim);
        relPages.setPreferredSize(dim);
        ButtonGroup group = new ButtonGroup();
        group.add(typeX);
        group.add(typeXSq);
        typeX.setSelected(true);
        JPanel wPanel = new JPanel();
        wPanel.add(new JLabel("w = "));
        wPanel.add(wSpinner);
        JPanel wsqPanel = new JPanel();
        wsqPanel.add(new JLabel("w = "));
        wsqPanel.add(wsqSpinner);
        JPanel choiceXPanel = new JPanel();
        choiceXPanel.setLayout(new GridLayout(1, 2));
        JPanel choiceXSqPanel = new JPanel();
        choiceXSqPanel.setLayout(new GridLayout(1, 2));
        JPanel xPanel = new JPanel();
        xPanel.add(typeX);
        JPanel sqPanel = new JPanel();
        sqPanel.add(typeXSq);
        choiceXPanel.add(xPanel);
        choiceXPanel.add(wPanel);
        choiceXSqPanel.add(sqPanel);
        choiceXSqPanel.add(wsqPanel);
        JPanel relPanel = new JPanel();
        relPanel.add(relPages);
        infoPanel.add(labelPanel);
        infoPanel.add(topPanel);
        infoPanel.add(midPanel);
        infoPanel.add(lowPanel);
        infoPanel.add(choiceXPanel);
        infoPanel.add(choiceXSqPanel);
        infoPanel.add(relPanel);
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
        this.setVisible(true);
        bddAbove = aboveBox.isSelected();
        lowerBound = (int) lowerSpinner.getValue();
        upperBound = (int) upperSpinner.getValue();
        relevant = relPages.isSelected();
    }
    
    public int getFilterType() {
        if (typeX.isSelected()) return 6;
        return 7;
    }
    
    public String wValue() {
        if (typeX.isSelected()) return (String) wSpinner.getSelectedItem();
        return (String) wsqSpinner.getSelectedItem();
    }
    
    private String boundString() {
        String bound = String.valueOf(lowerBound);
        String redux = "pages";
        if (relevant) redux = "rel pages";
        if (bddAbove && upperBound == lowerBound) {
            bound = bound +"="+redux;
        }
        else {
            bound = bound +"<="+redux;
        }
        if (bddAbove && upperBound > lowerBound) bound = bound+"<="+upperBound;
        return bound;
    }
    
    public String getFilterName(int typ) {
        switch(typ) {
            case 6 -> {return "X^3-X-"+wValue()+" "+this.boundString();}
            case 7 -> {return "X^3-X^2-"+wValue()+" "+this.boundString();}
        }
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    public boolean getRelevance() {
        return relevant;
    }
    
}
