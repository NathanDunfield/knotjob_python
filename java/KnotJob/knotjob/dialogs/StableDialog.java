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
import java.awt.GridLayout;
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
public class StableDialog extends JDialog {
    
    private final JFrame frame;
    private int even;
    
    public StableDialog(JFrame fram, String title, boolean bo) {
        super(fram,title,bo);
        frame = fram;
        even = -1;
    }
    
    public int getParity() {
        setSize(500,125);
        setResizable(false);
        setLayout(new BorderLayout());
        setLocationRelativeTo(frame);
        JPanel choicePanel = new JPanel();
        choicePanel.setLayout(new GridLayout(1,2));
        JButton eveButton = new JButton("Even Type");
        JButton oddButton = new JButton("Odd Type ("+(char) 949+"=0)");
        JButton odeButton = new JButton("Odd Type ("+(char) 949+"=1)");
        JPanel oddPanel = new JPanel();
        JPanel odePanel = new JPanel();
        JPanel evePanel = new JPanel();
        oddPanel.add(oddButton);
        odePanel.add(odeButton);
        evePanel.add(eveButton);
        choicePanel.add(evePanel);
        choicePanel.add(oddPanel);
        choicePanel.add(odePanel);
        JPanel cancelPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        cancelPanel.add(cancelButton);
        eveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                even = 0;
                setVisible(false);
            }
        });
        oddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                even = 1;
                setVisible(false);
            }
        });
        odeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                even = 2;
                setVisible(false);
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
            }
        });
        add(choicePanel,BorderLayout.NORTH);
        add(cancelPanel,BorderLayout.SOUTH);
        setVisible(true);
        return even;
    }
    
}
