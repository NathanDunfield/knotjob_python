/*

Copyright (C) 2020-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import knotjob.Options;

/**
 *
 * @author dirk
 */
public class YesNoDialog extends JDialog {
    private final JFrame frame;
    private final JButton yesButton;
    private final JButton noButton;
    private final Image img;
    private final Color col;
    private boolean decision;
    
    public YesNoDialog(JFrame fram, String title, boolean bo, Options optns, Color co) {
        super(fram,title,bo);
        frame = fram;
        img = optns.getMedImage();
        yesButton = new JButton("Yes");
        noButton = new JButton("No");
        col = co;
        decision = false;
        noButton.setSelected(true);
    }

    public boolean showDialog() {
        this.setSize(320,190);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        JPanel picPanel = new JPanel();
        JPanel anotherpicPanel = new JPanel();
        JPanel textPanel = new JPanel();
        JLabel picLabel = new JLabel(new ImageIcon(img));
        JLabel textLabel = new JLabel("Are you sure?", SwingConstants.CENTER);
        picPanel.add(picLabel);
        picPanel.setBackground(col);
        picPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        anotherpicPanel.add(picPanel);
        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel, BorderLayout.CENTER);
        centerPanel.add(anotherpicPanel, BorderLayout.WEST);
        centerPanel.add(textPanel, BorderLayout.CENTER);
        centerPanel.setBackground(Color.orange);
        JPanel buttonPanel = new JPanel();
        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                decision = true;
                dispose();
            }
        });
        noButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonPanel.add(noButton);
        buttonPanel.add(yesButton);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.setVisible(true);
        return decision;
    }
    
    
}
