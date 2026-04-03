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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author Dirk
 */
public class SortDialog extends JDialog {
    
    private final JFrame frame;
    private int choice;
    private final JButton okay;
    private final JButton cancel;
    private final JCheckBox reverse;
    private final ButtonGroup group;
    private final JRadioButton nameButton;
    private final JRadioButton compButton;
    private final JRadioButton writButton;
    private final JRadioButton crosButton;
    private final JRadioButton girtButton;
    private final JRadioButton signButton;
    private final JRadioButton detButton;
    private final JRadioButton smdtwoButton;
    private final JRadioButton smdthrButton;
    private final JRadioButton rasmusButton;
    private final JRadioButton unrKhoButton;
    private final JRadioButton redKhoButton;
    private final JRadioButton jonesButton;
    private final JRadioButton alexButton;
    
    public SortDialog(JFrame frm, String title, boolean bo, boolean rev) {
        super(frm,title,bo);
        frame = frm;
        choice = -1;
        okay = new JButton("OK");
        cancel = new JButton("Cancel");
        reverse = new JCheckBox("Reverse order");
        reverse.setSelected(rev);
        group = new ButtonGroup();
        nameButton = new JRadioButton("Name");
        compButton = new JRadioButton("Components");
        writButton = new JRadioButton("Writhe");
        crosButton = new JRadioButton("Crossings");
        girtButton = new JRadioButton("Girth");
        smdtwoButton = new JRadioButton("mod 2 s-Invariant");
        smdthrButton = new JRadioButton("mod 3 s-Invariant");
        rasmusButton = new JRadioButton("Rasmussen Invariant");
        signButton = new JRadioButton("Signature");
        detButton = new JRadioButton("Determinant");
        unrKhoButton = new JRadioButton("Khovanov Cohomology");
        redKhoButton = new JRadioButton("odd Khovanov Homology");
        jonesButton = new JRadioButton("Jones Polynomial");
        alexButton = new JRadioButton("Alexander Polynomial");
    }
    
    public int getSelected() {
        this.setSize(450,350);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        okay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setChoice();
                dispose();
            }
        });
        group.add(nameButton);
        group.add(compButton);
        group.add(writButton);
        group.add(crosButton);
        group.add(girtButton);
        group.add(smdtwoButton);
        group.add(smdthrButton);
        group.add(rasmusButton);
        group.add(unrKhoButton);
        group.add(redKhoButton);
        group.add(jonesButton);
        group.add(alexButton);
        group.add(signButton);
        group.add(detButton);
        JPanel mainPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(7, 2));
        nameButton.setSelected(true);
        Dimension dim = new Dimension(180, 40);
        nameButton.setPreferredSize(dim);
        compButton.setPreferredSize(dim);
        writButton.setPreferredSize(dim);
        crosButton.setPreferredSize(dim);
        girtButton.setPreferredSize(dim);
        smdtwoButton.setPreferredSize(dim);
        smdthrButton.setPreferredSize(dim);
        rasmusButton.setPreferredSize(dim);
        unrKhoButton.setPreferredSize(dim);
        redKhoButton.setPreferredSize(dim);
        jonesButton.setPreferredSize(dim);
        alexButton.setPreferredSize(dim);
        signButton.setPreferredSize(dim);
        detButton.setPreferredSize(dim);
        buttonPanel.add(nameButton);
        buttonPanel.add(compButton);
        buttonPanel.add(writButton);
        buttonPanel.add(crosButton);
        buttonPanel.add(girtButton);
        buttonPanel.add(smdtwoButton);
        buttonPanel.add(smdthrButton);
        buttonPanel.add(rasmusButton);
        buttonPanel.add(unrKhoButton);
        buttonPanel.add(redKhoButton);
        buttonPanel.add(jonesButton);
        buttonPanel.add(alexButton);
        buttonPanel.add(signButton);
        buttonPanel.add(detButton);
        mainPanel.add(buttonPanel);
        JPanel buttPanel = new JPanel();
        buttPanel.add(reverse);
        buttPanel.add(okay);
        buttPanel.add(cancel);
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(buttPanel, BorderLayout.SOUTH);
        this.setVisible(true);
        return choice;
    }
    
    public boolean isReversed() {
        return reverse.isSelected();
    }
    
    private void setChoice() {
        Enumeration<AbstractButton> buttons = group.getElements();
        int count = 0;
        boolean found = false;
        while (!found) {
            AbstractButton butt = buttons.nextElement();
            if (butt.isSelected()) found = true;
            else count++;
        }
        choice = count;
    }
}
