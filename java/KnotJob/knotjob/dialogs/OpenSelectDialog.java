/*

Copyright (C) 2022 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import knotjob.Knobster;

/**
 *
 * @author Dirk
 */
public class OpenSelectDialog extends JDialog {
    
    private final ArrayList<String> modNames;
    private final ArrayList<String> names;
    private final DefaultListModel<String> selectList;
    private final int ext;
    private final Knobster knobby;
    private boolean okay;
    
    public OpenSelectDialog(Knobster kjob, ArrayList<String> nmes, int ex) {
        super(kjob, "Select Link(s)", true);
        modNames = new ArrayList<String>();
        names = new ArrayList<String>();
        for (String name : nmes) names.add(name);
        ext = ex;
        knobby = kjob;
        selectList = listFrom(true);
    }
    
    public void setUp() {
        this.setSize(720,400);
        this.setLocationRelativeTo(knobby);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        JPanel headLine = new JPanel();
        JLabel headLabe = new JLabel("Choose links to be opened.");
        headLine.add(headLabe,SwingConstants.CENTER);
        this.add(headLine, BorderLayout.NORTH);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1,3));
        JPanel leftPanel = new JPanel();
        DefaultListModel<String> nameList = listFrom(false);
        JList<String> possNames = new JList<String>(nameList);
        JScrollPane leftPane = new JScrollPane(possNames);
        possNames.setSelectedIndex(0);
        leftPane.setPreferredSize(new Dimension(220,280));
        leftPanel.add(leftPane);
        JPanel centrePanel = new JPanel();
        centrePanel.setLayout(new GridLayout(6,1));
        JButton moveOne = new JButton(" = Move => ");
        JButton moveAll = new JButton(" = All  => ");
        JButton remoOne = new JButton(" <= Move = ");
        JButton remoAll = new JButton(" <= All  = ");
        JPanel moPanel = new JPanel();
        JPanel maPanel = new JPanel();
        JPanel roPanel = new JPanel();
        JPanel raPanel = new JPanel();
        moPanel.add(moveOne);
        maPanel.add(moveAll);
        roPanel.add(remoOne);
        raPanel.add(remoAll);
        centrePanel.add(new JPanel());
        centrePanel.add(moPanel);
        centrePanel.add(maPanel);
        centrePanel.add(roPanel);
        centrePanel.add(raPanel);
        JPanel rightPanel = new JPanel();
        JList<String> chosPrimes = new JList<String>(selectList);
        JScrollPane rightPane = new JScrollPane(chosPrimes);
        rightPane.setPreferredSize(new Dimension(220,280));
        rightPanel.add(rightPane);
        mainPanel.add(leftPanel);
        mainPanel.add(centrePanel);
        mainPanel.add(rightPanel);
        this.add(mainPanel,BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1,2));
        JPanel cancelPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        cancelPanel.add(cancelButton);
        JPanel okayPanel = new JPanel();
        JButton okayButton = new JButton("OK");
        okayPanel.add(okayButton);
        buttonPanel.add(okayPanel);
        buttonPanel.add(cancelPanel);
        this.add(buttonPanel, BorderLayout.SOUTH);
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okay = true;
                dispose();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okay = false;
                dispose();
            }
        });
        moveOne.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] chosen = possNames.getSelectedIndices();
                for (int i = chosen.length-1; i >= 0; i--) {
                    String name = nameList.getElementAt(chosen[i]);
                    nameList.removeElementAt(chosen[i]);
                    int pos = position(name, selectList);
                    selectList.add(pos, name);
                }
            }
        });
        remoOne.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] chosen = chosPrimes.getSelectedIndices();
                for (int i = chosen.length-1; i >= 0; i--) {
                    String name = selectList.getElementAt(chosen[i]);
                    selectList.removeElementAt(chosen[i]);
                    int pos = position(name, nameList);
                    nameList.add(pos, name);
                }
            }
        });
        moveAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = nameList.getSize()-1; i >= 0; i--) {
                    String name = nameList.getElementAt(i);
                    nameList.removeElementAt(i);
                    int pos = position(name, selectList);
                    selectList.add(pos, name);
                }
            }
        });
        remoAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = selectList.getSize()-1; i >= 0; i--) {
                    String name = selectList.getElementAt(i);
                    selectList.removeElementAt(i);
                    int pos = position(name, nameList);
                    nameList.add(pos, name);
                }
            }
        });
        this.setVisible(true);
    }

    private DefaultListModel<String> listFrom(boolean empty) {
        DefaultListModel<String> model = new DefaultListModel<String>();
        if (empty) return model;
        if (ext == 0) {
            for (String name : names) {
                model.addElement(name.substring(2));
                modNames.add(name.substring(2));
            }
            return model;
        }
        if (ext == 1) {
            for (String name : names) {
                int pos = name.indexOf("= PD[");
                if (pos > 0) {
                    model.addElement(name.substring(0, pos));
                    modNames.add(name.substring(0, pos));
                }
                else {
                    model.addElement(name);
                    modNames.add(name);
                }
            }
            return model;
        }
        for (String name : names) {
            model.addElement(name);
            modNames.add(name);
        }
        return model;
    }
    
    private int position(String name, DefaultListModel<String> selectList) {
        int pos = 0;
        boolean keep = !selectList.isEmpty();
        while (keep) {
            if (selectList.size() < pos+1) keep = false;
            else if (name.compareTo(selectList.getElementAt(pos)) < 0) keep = false;
            if (keep) pos++;
        }
        return pos;
    }

    public boolean isOkay() {
        return okay;
    }

    public ArrayList<String> getNames() {
        ArrayList<String> chosen = new ArrayList<String>();
        for (int i = 0; i < selectList.size(); i++) {
            String chos = selectList.getElementAt(i);
            int pos = modNames.indexOf(chos);
            chosen.add(names.get(pos));
        }
        return chosen;
    }
    
}
