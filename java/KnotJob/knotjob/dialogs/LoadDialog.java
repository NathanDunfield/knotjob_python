/*

Copyright (C) 2019-21 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import knotjob.AbortInfo;

/**
 *
 * @author Dirk
 */
public class LoadDialog extends JDialog {
    
    private final JFrame frame;
    private final JLabel titleLabel;
    private final JButton cancelButton;
    public final AbortInfo abInf;
    
    public LoadDialog(JFrame fram, String title, boolean bo) {
        super(fram,title,bo);
        frame = fram;
        titleLabel = new JLabel(" ");
        cancelButton = new JButton("Cancel");
        abInf = new AbortInfo();
    }
    
    public void setUpStuff() {
        setLayout(new BorderLayout());
        setSize(400,150);
        setLocationRelativeTo(frame);
        setResizable(false);
        JPanel panel = new JPanel();
        panel.add(titleLabel);
        add(panel, BorderLayout.CENTER);
        JPanel bpanel = new JPanel();
        bpanel.add(cancelButton);
        add(bpanel, BorderLayout.SOUTH);
        add(new JLabel(" "), BorderLayout.NORTH);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abInf.cancel();
            }
        });
        setVisible(true);
    }
    
    public void setText(String text) {
        titleLabel.setText(text);
    }
    
}
