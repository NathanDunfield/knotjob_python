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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import knotjob.Options;

/**
 *
 * @author Dirk
 */
public class SFanFilterDialog extends SFilterDialog {
    
    private final JRadioButton rasInv = new JRadioButton("S-Invariant");
    private final JRadioButton sltInv = new JRadioButton("sl_3 S-Invariant");
    private final JRadioButton rltInv = new JRadioButton("rl_3 S-Invariant");
    private final JComboBox<String> cSpinner;
    private final JCheckBox charBox = new JCheckBox("specify characteristic");

    public SFanFilterDialog(JFrame fram, String title, boolean bo, int lb, int ub, 
            boolean red, int jump, Options opts) {
        super(fram, title, bo, lb, ub, red, jump);
        cSpinner = new JComboBox<String>();
        cSpinner.addItem("0");
        for (int p : opts.getPrimes()) cSpinner.addItem(String.valueOf(p));
    }
    
    @Override
    public void setupDialog() {
        setSize(400,450);
        setLocationRelativeTo(frame);
        setResizable(false);
        setLayout(new BorderLayout());
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(9,1));
        setupButtonPanel();
        setupSInvariantOptions(infoPanel);
        setupTypeAndChar(infoPanel);
        setAllVisible();
    }

    private void setupTypeAndChar(JPanel infoPanel) {
        ButtonGroup group = new ButtonGroup();
        group.add(rasInv);
        group.add(sltInv);
        group.add(rltInv);
        Dimension dim = new Dimension(140, 40);
        rasInv.setPreferredSize(dim);
        sltInv.setPreferredSize(dim);
        rltInv.setPreferredSize(dim);
        JPanel rasPanel = new JPanel();
        JPanel sltPanel = new JPanel();
        JPanel rltPanel = new JPanel();
        rasPanel.add(rasInv);
        sltPanel.add(sltInv);
        rltPanel.add(rltInv);
        rasInv.setSelected(true);
        infoPanel.add(rasPanel);
        infoPanel.add(sltPanel);
        infoPanel.add(rltPanel);
        JPanel specPanel = new JPanel();
        specPanel.add(charBox);
        infoPanel.add(specPanel);
        JPanel charPanel = new JPanel();
        charPanel.add(new JLabel("c = "));
        charPanel.add(cSpinner);
        infoPanel.add(charPanel);
        cSpinner.setEnabled(false);
        charBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cSpinner.setEnabled(charBox.isSelected());
            }
        });
    }
    
    public int getBoundType() {
        if (rasInv.isSelected()) return 0;
        if (sltInv.isSelected()) return 1;
        return 2;
    }
    
    public int getCharType() {
        if (!charBox.isSelected()) return -1;
        return Integer.parseInt((String) cSpinner.getSelectedItem());
    }

    public String getTypeString() {
        if (rasInv.isSelected()) return "s-Inv";
        if (sltInv.isSelected()) return "slt s-Inv";
        return "rlt s-Inv";
    }
    
}
