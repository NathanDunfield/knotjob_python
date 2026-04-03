/*

Copyright (C) 2023 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Dirk
 */
public class ReducedDialog extends JDialog {
    
    private final JFrame frame;
    private final JButton cancelButton;
    private final JButton reducedButton;
    private final JButton unreducedButton;
    private int decision;
    
    public ReducedDialog(JFrame fram, String title, boolean bo) {
        super(fram, title, bo);
        frame = fram;
        cancelButton = new JButton("Cancel");
        reducedButton = new JButton("Reduced");
        unreducedButton = new JButton("Unreduced");
    }
    
    public void showDialog() {
        this.setSize(350,150);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        JPanel redPanel = new JPanel();
        redPanel.add(reducedButton);
        JPanel unrPanel = new JPanel();
        unrPanel.add(unreducedButton);
        buttonPanel.add(redPanel);
        buttonPanel.add(unrPanel);
        JPanel cancelPanel = new JPanel();
        JPanel canPanel = new JPanel();
        canPanel.add(cancelButton);
        cancelPanel.add(canPanel);
        this.add(buttonPanel, BorderLayout.CENTER);
        this.add(cancelPanel, BorderLayout.SOUTH);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        reducedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                decision = 1;
                dispose();
            }
        });
        unreducedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                decision = 2;
                dispose();
            }
        });
        this.setVisible(true);
    }

    public int getDecision() {
        return decision;
    }
    
}
