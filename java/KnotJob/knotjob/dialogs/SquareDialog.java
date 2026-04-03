/*

Copyright (C) 2019-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Dirk
 */
public class SquareDialog extends JDialog {
    
    private final JFrame frame;
    private int square;
    
    public SquareDialog(JFrame fram, String title, boolean bo) {
        super(fram,title,bo);
        frame = fram;
        square = 0;
    }
    
    public int getSquare() {
        setSize(500,200);
        setResizable(false);
        setLayout(new BorderLayout());
        setLocationRelativeTo(frame);
        JPanel choicePanel = new JPanel();
        choicePanel.setLayout(new GridLayout(3, 3));
        JButton sqoneButton = new JButton("Sq^1 even");
        JButton sqblsButton = new JButton("BLS odd");
        JButton sqonoButton = new JButton("Sq^1 odd");
        JButton sqsumButton = new JButton("Sq^1 sum");
        JButton complButton = new JButton("CLS inv");
        JButton sqtwoButton = new JButton("Sq^2 even");
        JButton sqtodButton = new JButton("Sq^2 odd ("+(char)949+"=0)");
        JButton sqtoeButton = new JButton("Sq^2 odd ("+(char)949+"=1)");
        JPanel sqonePanel = new JPanel();
        JPanel sqblsPanel = new JPanel();
        JPanel sqonoPanel = new JPanel();
        JPanel sqsumPanel = new JPanel();
        JPanel sqtwoPanel = new JPanel();
        JPanel sqtodPanel = new JPanel();
        JPanel sqtoePanel = new JPanel();
        JPanel unifyPanel = new JPanel();
        JPanel complPanel = new JPanel();
        sqonePanel.add(sqoneButton);
        sqblsPanel.add(sqblsButton);
        sqonoPanel.add(sqonoButton);
        sqsumPanel.add(sqsumButton);
        sqtwoPanel.add(sqtwoButton);
        sqtodPanel.add(sqtodButton);
        sqtoePanel.add(sqtoeButton);
        //unifyPanel.add(unifyButton);
        complPanel.add(complButton);
        choicePanel.add(sqonePanel);
        choicePanel.add(sqsumPanel);
        choicePanel.add(sqonoPanel);
        choicePanel.add(sqblsPanel);
        choicePanel.add(unifyPanel);
        choicePanel.add(complPanel);
        choicePanel.add(sqtwoPanel);
        choicePanel.add(sqtodPanel);
        choicePanel.add(sqtoePanel);
        JPanel cancelPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        cancelPanel.add(cancelButton);
        sqoneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                square = 1;
                setVisible(false);
            }
        });
        sqblsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                square = 3;
                setVisible(false);
            }
        });
        sqonoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                square = -1;
                setVisible(false);
            }
        });
        sqsumButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                square = 4;
                setVisible(false);
            }
        });
        complButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                square = -5;
                setVisible(false);
            }
        });
        sqtwoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                square = 2;
                setVisible(false);
            }
        });
        sqtodButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                square = -2;
                setVisible(false);
            }
        });
        sqtoeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                square = -3;
                setVisible(false);
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
            }
        });
        add(choicePanel,BorderLayout.NORTH);
        add(cancelPanel,BorderLayout.SOUTH);
        setVisible(true);
        return square;
    }
    
}
