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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import knotjob.filters.Filter;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class EditDialog extends JDialog {
    
    private final JFrame frume;
    private final String theName;
    private final String theComment;
    private final boolean withComment;
    private final JTextField name;
    private final JTextField comment;
    private boolean okay;
    
    public EditDialog(JFrame fram, String title, boolean bo, LinkData link) {
        super(fram,title,bo);
        frume = fram;
        theName = link.name;
        theComment = link.comment;
        withComment = true;
        okay = false;
        name = new JTextField();
        comment = new JTextField();
    }
    
    public EditDialog(JFrame fram, String title, boolean bo, Filter filt) {
        super(fram,title,bo);
        frume = fram;
        theName = filt.getName();
        theComment = "No Comment";
        withComment = false;
        okay = false;
        name = new JTextField();
        comment = new JTextField();
    }
    
    public boolean isOkay() {
        return okay;
    }
    
    public String getComment() {
        return comment.getText();
    }
    
    public String getNewName() {
        return name.getText();
    }
    
    public void setupDialog() {
        int ysize = 120;
        int num = 1;
        if (withComment) {
            ysize = 150;
            num = 2;
        }
        setSize(400, ysize);
        setLocationRelativeTo(frume);
        setResizable(false);
        setLayout(new BorderLayout());
        comment.setText(theComment);
        comment.selectAll();
        comment.setPreferredSize(new Dimension(300,30));
        JPanel fielPanel = new JPanel();
        fielPanel.add(comment);
        fielPanel.setPreferredSize(new Dimension(300,40));
        name.setText(theName);
        name.selectAll();
        name.setPreferredSize(new Dimension(300,30));
        JPanel npanel = new JPanel();
        npanel.add(name);
        JLabel label = new JLabel(" Comment :", SwingConstants.RIGHT);
        JLabel nlabel = new JLabel(" Name :", SwingConstants.RIGHT);
        JPanel pane = new JPanel(new BorderLayout());
        JPanel namePanel = new JPanel(new GridLayout(num,1));
        JPanel fieldPanel = new JPanel(new GridLayout(num,1));
        namePanel.add(nlabel);
        if (withComment) namePanel.add(label);
        fieldPanel.add(npanel);
        if (withComment) fieldPanel.add(fielPanel);
        JPanel buttons = new JPanel();
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        buttons.add(ok);
        buttons.add(cancel);
        pane.add(namePanel, BorderLayout.WEST);
        pane.add(fieldPanel, BorderLayout.CENTER);
        add(pane,BorderLayout.NORTH);
        add(buttons, BorderLayout.SOUTH);
        comment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okay = true;
                setVisible(false);
            }
        });
        name.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okay = true;
                setVisible(false);
            }
        });
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okay = true;
                setVisible(false);
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        setVisible(true);
    }
    
}
