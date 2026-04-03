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
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class OrientLink extends JDialog {
    
    private final JFrame frame;
    private final DefaultListModel<String> listModel;
    private final ArrayList<LinkData> theLinks;
    private JScrollPane thePane;
    private final ArrayList<JCheckBox> middleBoxes;
    private final ArrayList<JCheckBox> rightBoxes;
    public int chosen;
    public final ArrayList<Integer> comps;
    public final ArrayList<Boolean> orient;
    
    public OrientLink(JFrame fram, String title, boolean bo, DefaultListModel<String> model, ArrayList<LinkData> lData) {
        super(fram,title,bo);
        frame = fram;
        listModel = model;
        theLinks = lData;
        chosen = -1;
        comps = new ArrayList<Integer>();
        orient = new ArrayList<Boolean>();
        middleBoxes = new ArrayList<JCheckBox>();
        rightBoxes = new ArrayList<JCheckBox>();
    }
    
    public void setUpStuff() {
        this.setSize(600,400);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        JList<String> listlinks = new JList<String>(listModel);
        listlinks.setSelectedIndex(0);
        JScrollPane pane = new JScrollPane(listlinks);
        pane.setPreferredSize(new Dimension(160,300));
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel westPanel = new JPanel();
        westPanel.add(pane);
        JPanel infoPanel = centPanel(0);
        thePane = new JScrollPane(infoPanel);
        thePane.setPreferredSize(new Dimension(360,300));
        JPanel centerPanel = new JPanel();
        centerPanel.add(thePane);
        listlinks.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                @SuppressWarnings("unchecked")
                JList<String> list = (JList<String>) e.getSource();
                int choice = list.getSelectedIndex();
                JPanel nextPanel = null;
                if (choice >= 0) nextPanel = centPanel(choice);
                thePane.getViewport().removeAll();
                thePane.getViewport().add(nextPanel);
            }
        });
        JButton okayButton = new JButton("OK");
        JButton cancButton = new JButton("Cancel");
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chosen = listlinks.getSelectedIndex();
                for (int i = 0; i < middleBoxes.size(); i++) {
                    if (middleBoxes.get(i).isSelected()) {
                        comps.add(i);
                    }
                    orient.add(rightBoxes.get(i).isSelected());
                }
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
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(westPanel, BorderLayout.WEST);
        this.setVisible(true);
    }

    private JPanel centPanel(int t) {
        middleBoxes.removeAll(middleBoxes);
        rightBoxes.removeAll(rightBoxes);
        JPanel infoPanel = new JPanel();
        int size = theLinks.get(t).chosenLink().components()+2;
        infoPanel.setLayout(new GridLayout(size,3));
        for (int i = 0; i < 3; i++) infoPanel.add(new JPanel());
        for (int i = 0; i < size - 2; i++) {
            JPanel leftPanel = new JPanel();
            JLabel leftLabel = new JLabel("Component "+(i+1), JLabel.CENTER);
            leftPanel.add(leftLabel);
            infoPanel.add(leftLabel);
            JCheckBox middleBox = new JCheckBox("Keep", true);
            JCheckBox rightBox = new JCheckBox("Reverse", true);
            infoPanel.add(middleBox);
            middleBoxes.add(middleBox);
            infoPanel.add(rightBox);
            rightBoxes.add(rightBox);
        }
        for (int i = 0; i < 3; i++) infoPanel.add(new JPanel());
        infoPanel.setPreferredSize(new Dimension(340,size * 30));
        return infoPanel;
    }
}
