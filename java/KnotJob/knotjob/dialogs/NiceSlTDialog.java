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
import java.util.ArrayList;
import javax.swing.ButtonGroup;
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
public class NiceSlTDialog extends SlTSFilterDialog {
    
    private final JComboBox<String> cSpinner;
    private final JRadioButton typeS = new JRadioButton("X^3 - X");
    
    public NiceSlTDialog(JFrame fram, String title, boolean bo, Options opts) {
        super(fram, title, bo, 0, 0, false, 2);
        ArrayList<Integer> list = opts.getPrimes();
        String[] cStrings = new String[list.size()-1];
        cStrings[0] = "0";
        for (int i = 2; i < list.size(); i++) cStrings[i-1] = String.valueOf(list.get(i));
        cSpinner = new JComboBox<String>(cStrings);
    }
    
    public void setupDialog(boolean withW) {
        setSize(280, 200);
        setLocationRelativeTo(frame);
        setResizable(false);
        setLayout(new BorderLayout());
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(3, 1));
        setupButtonPanel();
        Dimension dim = new Dimension(140, 40);
        typeS.setPreferredSize(dim);
        JPanel cPanel = new JPanel();
        cPanel.add(new JLabel("c = "));
        cPanel.add(cSpinner);
        JPanel choicePanel = new JPanel();
        int x = 1;
        if (withW) x = 2;
        choicePanel.setLayout(new GridLayout(1, x));
        JPanel xPanel = new JPanel();
        xPanel.add(typeS);
        choicePanel.add(xPanel);
        if (withW) choicePanel.add(cPanel);
        infoPanel.add(choicePanel);
        setupPolynomials(infoPanel, withW);
        this.add(infoPanel, BorderLayout.CENTER);
        ButtonGroup group = new ButtonGroup();
        group.add(typeS);
        group.add(typeX);
        group.add(typeXSq);
        typeS.setSelected(true);
        setVisible(true);
    }
    
    @Override
    public String getFilterName() {
        String name = "Nice ";
        if (typeS.isSelected()) return name +"X^3-X c="+cSpinner.getSelectedItem();
        return name+super.getFilterName().substring(6);
    }
    
    @Override
    public String wSymbol() {
        if (typeS.isSelected()) return (String) cSpinner.getSelectedItem();
        return super.wSymbol();
    }
    
    @Override
    public int selectedType() {
        if (typeS.isSelected()) return 3;
        return super.selectedType();
    }

    public String getDiffFilterName() {
        String name = "Diff ";
        if (typeS.isSelected()) return name + "X^3-X";
        if (typeX.isSelected()) return name + "X^3-X-w";
        return name + "X^3-X^2-w";
    }
}
