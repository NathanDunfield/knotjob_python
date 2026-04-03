/*

Copyright (C) 2019-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import knotjob.AbortInfo;
import knotjob.Knobster;

/**
 *
 * @author Dirk
 */
public class CalculationDialog extends JFrame {
    
    private final int number;
    public final Knobster frame;
    private final JLabel titleLabel;
    private final JLabel[] theLabelsLeft;
    private final JLabel[] theLabelsRight;
    private final JPanel titlePanel;
    private final JPanel[] thePanels;
    private final JPanel mainPanel;
    private final JPanel buttonPanel;
    private final JButton abortButton;
    private final JButton cancelButton;
    public final AbortInfo abInf;
    
    public CalculationDialog(JFrame fram, String title, int numb) {
        super(title);
        frame = (Knobster) fram;
        if (numb >=5) numb = 4;
        number = numb;
        abortButton = new JButton("Abort");
        cancelButton = new JButton("Cancel");
        titlePanel = new JPanel();
        thePanels = new JPanel[number];
        theLabelsLeft = new JLabel[number];
        theLabelsRight = new JLabel[number];
        for (int i = 0; i < number; i++) {
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(1,2));
            JLabel lableft = new JLabel("   ", JLabel.RIGHT);
            JLabel labrigh = new JLabel("   ");
            panel.add(lableft);
            panel.add(labrigh);
            thePanels[i] = panel;
            theLabelsLeft[i] = lableft;
            theLabelsRight[i] = labrigh;
        }
        titleLabel = new JLabel(" ");
        titlePanel.add(titleLabel);
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(number,1));
        for (int i = 0; i < number; i++) mainPanel.add(thePanels[i]);
        buttonPanel = new JPanel();
        buttonPanel.add(abortButton);
        buttonPanel.add(cancelButton);
        abInf = new AbortInfo();
    }
    
    public void setUpStuff() {
        this.setSize(360,300);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        this.add(titlePanel, BorderLayout.NORTH);
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
        abortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abInf.abort();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abInf.cancel();
            }
        });
        this.setVisible(true);
    }
    
    public void setTitleLabel(String title) {
        titleLabel.setText(title);
    }
    
    public void setLabelLeft(String label, int i) {
        theLabelsLeft[i].setText(label);
    }
    
    public void setLabelRight(String label, int i) {
        theLabelsRight[i].setText(label);
    }
    
    public int getNumber() {
        return number;
    }
    
    public void updateKnobster() {
        frame.updateLink();
    }
    
}
