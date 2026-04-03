/*

Copyright (C) 2021 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.frames;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 *
 * @author Dirk
 */
public class StringFrame extends JFrame {
    
    private final boolean extra;
    private final JCheckBox extraBox;
    private String info;
    private String altInfo;
    private JLabel labb;
    
    public StringFrame(String title, boolean ext) {
        super(title);
        extra = ext;
        extraBox = new JCheckBox("LaTeX");
    }

    public void setupStuff(JFrame fram, String title, String code) {
        this.setLayout(new GridLayout(2,1));
        this.setSize(680,140);
        this.setLocationRelativeTo(fram);
        info = code;
        altInfo = "Dirk was here";
        JLabel laba = new JLabel(title+" : ", SwingConstants.RIGHT);
        labb = new JLabel(info, SwingConstants.LEFT);
        JScrollPane scroller = new JScrollPane(labb);
        scroller.setPreferredSize(new Dimension(560,40));
        JPanel pane = new JPanel();
        pane.add(laba);
        pane.add(scroller);
        JButton copyButton = new JButton("Copy");
        JPanel panf = new JPanel();
        if (extra) panf.add(extraBox);
        panf.add(copyButton);
        JButton close = new JButton("Close");
        panf.add(close);
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        });
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                StringSelection stringSelection = new StringSelection(code);
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                clpbrd.setContents(stringSelection, null);
            }
        });
        extraBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (extraBox.isSelected()) labb.setText(altInfo);
                else labb.setText(info);
            }
        });
        this.add(pane);
        this.add(panf);
        this.setResizable(false);
        this.setVisible(true);
    }

    public void setAlternative(String toString) {
        altInfo = toString;
    }
    
}
