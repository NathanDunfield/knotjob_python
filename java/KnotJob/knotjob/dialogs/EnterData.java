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
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Dirk
 */
public class EnterData {
    
    public String theString;
    public String theName;
    public boolean combin;

    
    public EnterData(JFrame frame, String title, String code, boolean allow) {
        theString = null;
        theName = null;
        combin = true;
        JDialog fram = new JDialog(new JFrame(), title, true);
        fram.setSize(400, 200);
        fram.setLocationRelativeTo(frame);
        fram.setResizable(false);
        fram.setLayout(new BorderLayout());
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(300,30));
        JPanel fielPanel = new JPanel();
        fielPanel.add(field);
        fielPanel.setPreferredSize(new Dimension(300,40));
        JTextField name = new JTextField();
        name.setPreferredSize(new Dimension(300,30));
        JPanel npanel = new JPanel();
        npanel.add(name);
        JLabel label = new JLabel(" "+code);
        JLabel nlabel = new JLabel(" Name : ");
        JPanel pane = new JPanel(new BorderLayout());
        JPanel namePanel = new JPanel(new GridLayout(2,1));
        JPanel fieldPanel = new JPanel(new GridLayout(2,1));
        namePanel.add(nlabel);
        namePanel.add(label);
        fieldPanel.add(npanel);
        fieldPanel.add(fielPanel);
        JPanel buttons = new JPanel();
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        buttons.add(ok);
        buttons.add(cancel);
        pane.add(namePanel, BorderLayout.WEST);
        pane.add(fieldPanel, BorderLayout.CENTER);
        JPanel boxes = new JPanel(new GridLayout(1,1));
        JCheckBox combine = new JCheckBox();
        combine.setSelected(true);
        boxes.add(combine);
        JLabel combilab = new JLabel("Minimize Girth");
        JPanel text = new JPanel(new GridLayout(1,1));
        text.add(combilab);
        JPanel centerb = new JPanel();
        centerb.add(boxes);
        centerb.add(text);
        fram.add(centerb, BorderLayout.CENTER);
        fram.add(pane,BorderLayout.NORTH);
        fram.add(buttons, BorderLayout.SOUTH);
        field.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                theString = field.getText();
                theName = name.getText();
                combin = combine.isSelected();
                fram.setVisible(false);
            }
        });
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                theString = field.getText();
                theName = name.getText();
                combin = combine.isSelected();
                fram.setVisible(false);
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                theString = "";
                fram.setVisible(false);
            }
        });
        fram.setVisible(true);
    }
    
}
