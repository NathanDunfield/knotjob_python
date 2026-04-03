/*

Copyright (C) 2019-20 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Dirk
 */
public class ColorDialog extends JDialog {
    
    final private JColorChooser choice;
    final private JFrame frame;
    private boolean okay;
    
    public ColorDialog(JFrame fram, String title, boolean bo, JColorChooser chooser) {
        super(fram,title,bo);
        choice = chooser;
        frame = fram;
        okay = false;
    }
    
    public void setUpStuff() {
        this.setSize(650,350);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        this.add(choice, BorderLayout.NORTH);
        choice.setPreviewPanel(new JPanel());
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(1,3));
        centerPanel.add(new JPanel());
        JPanel previewPanel = new JPanel();
        previewPanel.setBackground(choice.getColor());
        choice.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Color color = choice.getColor();
                previewPanel.setBackground(new Color(color.getRGB()));
            }
        });
        centerPanel.add(previewPanel);
        centerPanel.add(new JPanel());
        this.add(centerPanel, BorderLayout.CENTER);
        JButton okayButton = new JButton("OK");
        JButton cancButton = new JButton("Cancel");
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okay = true;
                setVisible(false);
            }
        });
        cancButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        JPanel botPanel = new JPanel();
        botPanel.add(okayButton);
        botPanel.add(cancButton);
        this.add(botPanel, BorderLayout.SOUTH);
        this.setVisible(true);
    }
    
    public boolean isOkay() {
        return okay;
    }
    
}
