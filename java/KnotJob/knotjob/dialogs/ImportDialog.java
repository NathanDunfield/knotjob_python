/*

Copyright (C) 2024 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import knotjob.Knobster;

/**
 *
 * @author Dirk
 */
public class ImportDialog extends JDialog {
    
    private boolean okay;
    private final Knobster knotJob;
    private final JRadioButton snappyButton;
    private final JRadioButton thistleButton;
    
    public ImportDialog(Knobster kjob) {
        super(kjob, "Import Link(s)", true);
        okay = false;
        knotJob = kjob;
        snappyButton = new JRadioButton("Snappy Links");
        thistleButton = new JRadioButton("Thistlethwaite Knots");
    }
    
    public void setup() {
        this.setSize(400, 120);
        this.setLocationRelativeTo(knotJob);
        this.setResizable(false);
        JPanel wholePanel = new JPanel();
        wholePanel.setLayout(new BorderLayout());
        JPanel buttoPanel = new JPanel();
        buttoPanel.setLayout(new GridLayout(1,2));
        JPanel cancelPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        cancelPanel.add(cancelButton);
        JPanel okayPanel = new JPanel();
        JButton okayButton = new JButton("OK");
        okayPanel.add(okayButton);
        buttoPanel.add(okayPanel);
        buttoPanel.add(cancelPanel);
        wholePanel.add(buttoPanel, BorderLayout.SOUTH);
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                okay = true;
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        ButtonGroup buttons = new ButtonGroup();
        buttons.add(snappyButton);
        buttons.add(thistleButton);
        snappyButton.setSelected(true);
        JPanel choicePanel = new JPanel(new GridLayout(2, 1));
        choicePanel.add(snappyButton);
        choicePanel.add(thistleButton);
        JPanel choPanel = new JPanel();
        choPanel.add(choicePanel);
        wholePanel.add(choPanel, BorderLayout.NORTH);
        this.add(wholePanel);
        this.setVisible(true);
    }
    
    public int getChoice() {
        int choice = 0;
        if (okay) {
            if (snappyButton.isSelected()) choice = 1;
            else choice = 2;
        }
        return choice;
    }
    
}
