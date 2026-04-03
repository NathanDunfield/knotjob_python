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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author Dirk
 */
public class FilterTypeDialog extends JDialog {
    
    private final JFrame frame;
    private final ArrayList<JRadioButton> boxes;
    private final ButtonGroup group;
    private final JButton okayButton;
    private final JButton cancButton;
    private boolean okay;
    
    public FilterTypeDialog(JFrame fram, String title, boolean bo, ArrayList<String> opts) {
        super(fram,title,bo);
        frame = fram;
        boxes = new ArrayList<JRadioButton>(opts.size());
        group = new ButtonGroup();
        for (String label : opts) {
            JRadioButton newBox = new JRadioButton(label);
            group.add(newBox);
            boxes.add(newBox);
        }
        boxes.get(0).setSelected(true);
        okayButton = new JButton("OK");
        cancButton = new JButton("Cancel");
        okay = false;
    }
    
    public int getFilterType() {
        int ysize = 100 + 40 * boxes.size();
        this.setSize(300,ysize);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okayButton);
        buttonPanel.add(cancButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
        JPanel mainPanel = new JPanel();
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(boxes.size(),1));
        for (JRadioButton box : boxes) {
            radioPanel.add(box);
            box.setPreferredSize(new Dimension(200,40));
        }
        mainPanel.add(radioPanel);
        this.add(mainPanel, BorderLayout.CENTER);
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okay = true;
                dispose();
            }
        });
        cancButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        this.setVisible(true);
        if (!okay) return -1;
        boolean found = false;
        int i = 0;
        while (!found && i < boxes.size()) {
            if (boxes.get(i).isSelected()) found = true;
            else i++;
        }
        return i;
    }
}
