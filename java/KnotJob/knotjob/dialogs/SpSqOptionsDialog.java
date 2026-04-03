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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import knotjob.Options;

/**
 *
 * @author Dirk
 */
public class SpSqOptionsDialog extends JDialog {

    private final JFrame frame;
    private final JButton okayButton = new JButton("OK");
    private final JButton cancelButton = new JButton("Cancel");
    private final ButtonGroup group = new ButtonGroup();
    private final JRadioButton pageButton = new JRadioButton("BLT/SLT Page Bounds");
    private final JRadioButton sppgButton = new JRadioButton("Special SLT Page Bounds");
    private final JRadioButton diffButton = new JRadioButton("Difference Filter");
    private final JRadioButton niceButton = new JRadioButton("Nice E_infinity page");
    private final JRadioButton unsiButton = new JRadioButton("Unusual S-Invariants");
    private int choice;
    
    public SpSqOptionsDialog(JFrame knob, String title, boolean b, Options optns) {
        super(knob, title, b);
        frame = knob;
        group.add(pageButton);
        group.add(sppgButton);
        group.add(diffButton);
        group.add(niceButton);
        group.add(unsiButton);
    }
    
    public void setUp() {
        this.setSize(360,260);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        Dimension dim = new Dimension(200,40);
        JPanel selectPanel = new JPanel();
        selectPanel.setLayout(new GridLayout(5, 1));
        JPanel pagePanel = new JPanel();
        pageButton.setPreferredSize(dim);
        pagePanel.add(pageButton);
        JPanel sppgPanel = new JPanel();
        sppgButton.setPreferredSize(dim);
        sppgPanel.add(sppgButton);
        JPanel diffPanel = new JPanel();
        diffButton.setPreferredSize(dim);
        diffPanel.add(diffButton);
        JPanel nicePanel = new JPanel();
        niceButton.setPreferredSize(dim);
        nicePanel.add(niceButton);
        JPanel unsiPanel = new JPanel();
        unsiButton.setPreferredSize(dim);
        unsiPanel.add(unsiButton);
        selectPanel.add(pagePanel);
        selectPanel.add(sppgPanel);
        selectPanel.add(diffPanel);
        selectPanel.add(nicePanel);
        selectPanel.add(unsiPanel);
        pageButton.setSelected(true);
        this.add(selectPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        JPanel okayPanel = new JPanel();
        JPanel cancPanel = new JPanel();
        okayPanel.add(okayButton);
        cancPanel.add(cancelButton);
        buttonPanel.add(okayPanel);
        buttonPanel.add(cancPanel);
        this.add(buttonPanel, BorderLayout.SOUTH);
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                choice = obtainChoice();
                dispose();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                choice = -1;
                dispose();
            }
        });
        this.setVisible(true);
    }
    
    private int obtainChoice() {
        if (pageButton.isSelected()) return 0;
        if (sppgButton.isSelected()) return 1;
        if (diffButton.isSelected()) return 2;
        if (niceButton.isSelected()) return 3;
        if (unsiButton.isSelected()) return 4;
        return 5;
    }
    
    public int getChoice() {
        return choice;
    }
    
}
