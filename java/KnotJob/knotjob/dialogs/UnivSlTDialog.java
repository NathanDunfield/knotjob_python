/*

Copyright (C) 2024-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
public class UnivSlTDialog extends JDialog {
    
    private final JFrame frame;
    private int choice;
    private final JButton okay = new JButton("OK");
    private final JButton cancel = new JButton("Cancel");
    private final JSpinner charSpinner;
    private final JComboBox<String> wSpinner;
    private final JComboBox<String> wsqSpinner;
    private final SpinnerListModel model;
    private final ArrayList<Integer> theChars;
    private final JRadioButton typeThree = new JRadioButton("X^3 - 1");
    private final JRadioButton typeTwo = new JRadioButton("X^3 - X");
    private final JRadioButton typeOne = new JRadioButton("X^3 - X^2");
    private final JRadioButton typeOneRed = new JRadioButton("X^3-X^2 red");
    private final JRadioButton typeX = new JRadioButton("X^3 - X - w");
    private final JRadioButton typeXSq = new JRadioButton("X^3 - X^2 - w");
    
    public UnivSlTDialog(JFrame fram, String title, boolean bo, Options optns) {
        super(fram, title, bo);
        frame = fram;
        choice = -1;
        theChars = new ArrayList<Integer>();
        theChars.add(0);
        for (int p : optns.getPrimes()) theChars.add(p);
        model = new SpinnerListModel(theChars);
        charSpinner = new JSpinner(model);
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
    
    public int getInfo() {
        this.setSize(400,280);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        okay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                choice = (int) model.getValue();
                setVisible(false);
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        typeX.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (typeX.isSelected()) charSpinner.setValue(0);
            }
        });
        typeXSq.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (typeXSq.isSelected()) charSpinner.setValue(3);
            }
        });
        charSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (typeX.isSelected()) charSpinner.setValue(0);
                if (typeXSq.isSelected()) charSpinner.setValue(3);
            }
        });
        JPanel buttPanel = new JPanel();
        buttPanel.add(okay);
        buttPanel.add(cancel);
        JPanel modelPanel = new JPanel();
        model.setValue(0);
        modelPanel.add(new JLabel("Choose a characteristic : "));
        modelPanel.add(charSpinner);
        charSpinner.setPreferredSize(new Dimension(54, 20));
        wSpinner.setPreferredSize(new Dimension(80, 20));
        JPanel anotherPanel = new JPanel();
        anotherPanel.setLayout(new GridLayout(1, 1));
        anotherPanel.add(modelPanel);
        JPanel typePanel = new JPanel();
        ButtonGroup buttons = new ButtonGroup();
        buttons.add(typeThree);
        buttons.add(typeTwo);
        buttons.add(typeX);
        buttons.add(typeXSq);
        buttons.add(typeOne);
        buttons.add(typeOneRed);
        JPanel wPanel = new JPanel();
        wPanel.add(new JLabel("w = "));
        wPanel.add(wSpinner);
        JPanel wsqPanel = new JPanel();
        wsqPanel.add(new JLabel("w = "));
        wsqPanel.add(wsqSpinner);
        JPanel choicePanel = new JPanel();
        choicePanel.setLayout(new GridLayout(4, 2));
        choicePanel.add(typeThree);
        choicePanel.add(typeTwo);
        choicePanel.add(typeX);
        choicePanel.add(wPanel);
        choicePanel.add(typeXSq);
        choicePanel.add(wsqPanel);
        choicePanel.add(typeOne);
        //choicePanel.add(typeOneRed);
        typeThree.setSelected(true);
        typePanel.add(choicePanel);
        this.add(typePanel, BorderLayout.NORTH);
        this.add(anotherPanel, BorderLayout.CENTER);
        this.add(buttPanel, BorderLayout.SOUTH);
        this.setVisible(true);
        return choice;
    }

    public int getSSType() {
        if (typeXSq.isSelected()) return 5;
        if (typeX.isSelected()) return 4;
        if (typeThree.isSelected()) return 3;
        if (typeTwo.isSelected()) return 2;
        if (typeOne.isSelected()) return 1;
        return 0;
    }

    public String getSymbol() {
        if (typeXSq.isSelected()) return (String) wsqSpinner.getSelectedItem();
        return (String) wSpinner.getSelectedItem();
    }
    
    
    
}
