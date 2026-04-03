/*

Copyright (C) 2019-24 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import knotjob.Options;

/**
 *
 * @author Dirk
 */
public class CharDialog extends JDialog {
    
    private final JFrame frame;
    private int choice;
    private final boolean slt;
    private final JButton okay;
    private final JButton cancel;
    private final JSpinner charSpinner;
    private final SpinnerListModel model;
    private final JCheckBox redBox;
    private final JCheckBox sltBox;
    //private final boolean useBox;
    private final ArrayList<Integer> theChars;
    
    public CharDialog(JFrame fram, String title, boolean bo, boolean s, Options optns) {
        super(fram,title,bo);
        frame = fram;
        choice = -1;
        slt = s;
        okay = new JButton("OK");
        cancel = new JButton("Cancel");
        theChars = new ArrayList<Integer>();
        theChars.add(0);
        for (int p : optns.getPrimes()) theChars.add(p);
        model = new SpinnerListModel(theChars);
        charSpinner = new JSpinner(model);
        redBox = new JCheckBox("reduced");redBox.setEnabled(!s);
        sltBox = new JCheckBox("sl_3");
        //useBox = !"".equals(redBoxTitle);
    }

    public int getChar(boolean withOptions) {
        this.setSize(350,160);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        okay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                choice = (int) model.getValue();
                setVisible(false);
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        JPanel buttPanel = new JPanel();
        buttPanel.add(okay);
        buttPanel.add(cancel);
        JPanel modelPanel = new JPanel();
        model.setValue(2);
        modelPanel.add(new JLabel("Choose a characteristic : "));
        modelPanel.add(charSpinner);
        charSpinner.setPreferredSize(new Dimension(54,20));
        JPanel anotherPanel = new JPanel();
        anotherPanel.setLayout(new GridLayout(2, 1));
        anotherPanel.add(modelPanel);
        JPanel redPanel = new JPanel();
        if (slt) redPanel.add(sltBox);
        redPanel.add(redBox);
        if (withOptions) anotherPanel.add(redPanel);
        /*if (useBox) {
            JPanel redPanel = new JPanel();
            redPanel.add(redBox);
            anotherPanel.add(redPanel);
        }// */
        this.add(new JLabel(" "), BorderLayout.NORTH);
        this.add(anotherPanel, BorderLayout.CENTER);
        this.add(buttPanel, BorderLayout.SOUTH);
        this.setVisible(true);
        return choice;
    }
    
    public boolean getSlt() {
        return sltBox.isSelected();
    }
    
    public boolean getReduced() {
        return redBox.isSelected();
    }
    
}
