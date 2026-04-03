/*

Copyright (C) 2022 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Dirk
 */
public class StableFilterDialog extends JDialog {
    
    private final JFrame frame;
    private final JRadioButton evenButton;
    private final JRadioButton oddButton;
    private final JRadioButton odeButton;
    private final ButtonGroup groupOfButtons;
    private final JSpinner topUpTorsion;
    private final JSpinner topDoTorsion;
    private final JSpinner botUpTorsion;
    private final JSpinner botDoTorsion;
    private final SpinnerListModel topUpList;
    private final SpinnerListModel topDoList;
    private final SpinnerListModel botUpList;
    private final SpinnerListModel botDoList;
    private boolean okay;
    
    public StableFilterDialog(JFrame fram, String title, boolean bo) {
        super(fram, title, bo);
        frame = fram;
        evenButton = new JRadioButton("Even Type");
        oddButton = new JRadioButton("Odd ("+(char)949+"=0)");
        odeButton = new JRadioButton("Odd ("+(char)949+"=1)");
        groupOfButtons = new ButtonGroup();
        groupOfButtons.add(evenButton);
        groupOfButtons.add(oddButton);
        groupOfButtons.add(odeButton);
        evenButton.setSelected(true);
        Integer[] powers = new Integer[] {0, 2, 4, 8, 16, 32, 64, 128};
        topUpList = new SpinnerListModel(powers);
        topDoList = new SpinnerListModel(powers);
        botUpList = new SpinnerListModel(powers);
        botDoList = new SpinnerListModel(powers);
        topUpTorsion = new JSpinner(topUpList);
        topDoTorsion = new JSpinner(topDoList);
        botUpTorsion = new JSpinner(botUpList);
        botDoTorsion = new JSpinner(botDoList);
        Dimension dim = new Dimension(80,40);
        topUpTorsion.setPreferredSize(dim);
        topDoTorsion.setPreferredSize(dim);
        botUpTorsion.setPreferredSize(dim);
        botDoTorsion.setPreferredSize(dim);
    }
    
    public void setupDialog() {
        setSize(440,320);
        setLocationRelativeTo(frame);
        setResizable(false);
        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        JButton okayButton = new JButton("OK");
        JButton cancButton = new JButton("Cancel");
        buttonPanel.add(okayButton);
        buttonPanel.add(cancButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(1, 3));
        radioPanel.add(evenButton);
        radioPanel.add(oddButton);
        radioPanel.add(odeButton);
        this.add(radioPanel, BorderLayout.CENTER);
        JPanel torsionPanel = new JPanel();
        torsionPanel.setLayout(new GridLayout(4, 2));
        JPanel textPanel = new JPanel();
        JPanel spinnerPanel = new JPanel();
        JLabel textLabel = new JLabel("Torsion above upper Generator");
        Dimension dim = new Dimension(200, 40);
        textLabel.setPreferredSize(dim);
        textPanel.add(textLabel);
        torsionPanel.add(textPanel);
        spinnerPanel.add(topUpTorsion);
        torsionPanel.add(spinnerPanel);
        textPanel = new JPanel();
        textLabel = new JLabel("Torsion below upper Generator");
        textLabel.setPreferredSize(dim);
        textPanel.add(textLabel);
        torsionPanel.add(textPanel);
        spinnerPanel = new JPanel();
        spinnerPanel.add(topDoTorsion);
        torsionPanel.add(spinnerPanel);
        textPanel = new JPanel();
        textLabel = new JLabel("Torsion above lower Generator");
        textLabel.setPreferredSize(dim);
        textPanel.add(textLabel);
        torsionPanel.add(textPanel);
        spinnerPanel = new JPanel();
        spinnerPanel.add(botUpTorsion);
        torsionPanel.add(spinnerPanel);
        textPanel = new JPanel();
        textLabel = new JLabel("Torsion below lower Generator");
        textLabel.setPreferredSize(dim);
        textPanel.add(textLabel);
        torsionPanel.add(textPanel);
        spinnerPanel = new JPanel();
        spinnerPanel.add(botDoTorsion);
        torsionPanel.add(spinnerPanel);
        this.add(torsionPanel, BorderLayout.NORTH);
        topUpTorsion.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int i = (int) topUpTorsion.getValue();
                if (i > 0) topDoTorsion.setValue(0);
            }
        });
        topDoTorsion.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int i = (int) topDoTorsion.getValue();
                if (i > 0) topUpTorsion.setValue(0);
            }
        });
        botUpTorsion.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int i = (int) botUpTorsion.getValue();
                if (i > 0) botDoTorsion.setValue(0);
            }
        });
        botDoTorsion.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int i = (int) botDoTorsion.getValue();
                if (i > 0) botUpTorsion.setValue(0);
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
    }
    
    public boolean isOkay() {
        return okay;
    }
    
    public int[] getTorsion() {
        int[] torsion = new int[4];
        torsion[3] = (int) topUpTorsion.getValue();
        torsion[2] = (int) topDoTorsion.getValue();
        torsion[1] = (int) botUpTorsion.getValue();
        torsion[0] = (int) topUpTorsion.getValue();
        return torsion;
    }
    
    public int getEvenType() {
        if (evenButton.isSelected()) return 2;
        if (oddButton.isSelected()) return 0;
        return 1;
    }
}
