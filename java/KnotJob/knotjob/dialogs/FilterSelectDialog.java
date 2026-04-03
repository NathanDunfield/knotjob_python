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
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import knotjob.filters.Filter;

/**
 *
 * @author Dirk
 */
public class FilterSelectDialog extends JDialog {
    
    private final JFrame frame;
    private int choice;
    private final JButton selectButton;
    private final JButton cancelButton;
    private final ButtonGroup groupOfButtons;
    private final ArrayList<Filter> filters;
    
    public FilterSelectDialog(JFrame fram, String title, boolean bo, ArrayList<Filter> fltrs) {
        super(fram, title, bo);
        frame = fram;
        choice = -1;
        selectButton = new JButton("Select Filter");
        cancelButton = new JButton("Cancel");
        groupOfButtons = new ButtonGroup();
        filters = fltrs;
    }
    
    public int getSelection() {
        this.setSize(500,360);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
        JPanel mainPanel = new JPanel();
        JScrollPane filterPane = new JScrollPane(mainPanel);
        filterPane.setPreferredSize(new Dimension(400,200));
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(filters.size(),1));
        for (Filter filt : filters) {
            JRadioButton radio = new JRadioButton(filt.getName());
            radio.setPreferredSize(new Dimension(360,40));
            if (filters.indexOf(filt) == 0) radio.setSelected(true);
            radioPanel.add(radio);
            groupOfButtons.add(radio);
        }
        mainPanel.add(radioPanel);
        this.add(filterPane, BorderLayout.CENTER);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Enumeration<AbstractButton> buttons = groupOfButtons.getElements();
                boolean found = false;
                int i = 0;
                while (!found && buttons.hasMoreElements()) {
                    AbstractButton button = buttons.nextElement();
                    if (button.isSelected()) {
                        found = true;
                        choice = i;
                    }
                    i++;
                }
                dispose();
            }
        });
        this.setVisible(true);
        return choice;
    }
    
}
