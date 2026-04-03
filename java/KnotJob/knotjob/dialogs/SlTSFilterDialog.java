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
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author Dirk
 */
public class SlTSFilterDialog extends SFilterDialog {
    
    protected final JComboBox<String> wSpinner;
    protected final JComboBox<String> wsqSpinner;
    protected final JRadioButton typeX = new JRadioButton("X^3 - X - w");
    protected final JRadioButton typeXSq = new JRadioButton("X^3 - X^2 - w");
    
    public SlTSFilterDialog(JFrame fram, String title, boolean bo, int lb, int ub, boolean red, 
            int jump) {
        super(fram, title, bo, lb, ub, red, jump);
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
        String[] theWSqs = new String[] {"1", "-1"};
        wSpinner = new JComboBox<String>(theWs);
        wsqSpinner = new JComboBox<String>(theWSqs);
    }
    
    @Override
    public void setupDialog() {
        setSize(280, 300);
        setLocationRelativeTo(frame);
        setResizable(false);
        setLayout(new BorderLayout());
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(6, 1));
        setupButtonPanel();
        setupSInvariantOptions(infoPanel);
        setupPolynomials(infoPanel, true);
        ButtonGroup group = new ButtonGroup();
        group.add(typeX);
        group.add(typeXSq);
        typeX.setSelected(true);
        setAllVisible();
    }

    public String getFilterName() {
        String name = "S-Inv ";
        if (typeX.isSelected()) name = name +"X^3-X-"+wSpinner.getSelectedItem();
        if (typeXSq.isSelected()) name = name +"X^3-X^2-"+wsqSpinner.getSelectedItem();
        return name;
    }
    
    public String wSymbol() {
        if (typeX.isSelected()) return (String) wSpinner.getSelectedItem();
        String w = (String) wsqSpinner.getSelectedItem();
        if (w.startsWith("1")) w = "+1";
        return w;
    }
    
    public int selectedType() {
        if (typeX.isSelected()) return 6;
        return 7;
    }

    protected void setupPolynomials(JPanel infoPanel, boolean withW) {
        Dimension dim = new Dimension(140, 40);
        typeX.setPreferredSize(dim);
        typeXSq.setPreferredSize(dim);
        JPanel wPanel = new JPanel();
        wPanel.add(new JLabel("w = "));
        wPanel.add(wSpinner);
        JPanel wsqPanel = new JPanel();
        wsqPanel.add(new JLabel("w = "));
        wsqPanel.add(wsqSpinner);
        JPanel choiceXPanel = new JPanel();
        int x = 1;
        if (withW) x = 2;
        choiceXPanel.setLayout(new GridLayout(1, x));
        JPanel choiceXSqPanel = new JPanel();
        choiceXSqPanel.setLayout(new GridLayout(1, x));
        JPanel xPanel = new JPanel();
        xPanel.add(typeX);
        JPanel sqPanel = new JPanel();
        sqPanel.add(typeXSq);
        choiceXPanel.add(xPanel);
        if (withW) choiceXPanel.add(wPanel);
        choiceXSqPanel.add(sqPanel);
        if (withW) choiceXSqPanel.add(wsqPanel);
        infoPanel.add(choiceXPanel);
        infoPanel.add(choiceXSqPanel);
    }
    
}
