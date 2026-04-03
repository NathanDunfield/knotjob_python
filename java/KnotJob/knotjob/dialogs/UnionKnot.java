/*

Copyright (C) 2019-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import knotjob.links.*;

/**
 *
 * @author Dirk
 */
public class UnionKnot {
    
    ArrayList<LinkData> theLinks;
    public String name;
    public Link knot = null;
    int choiceOne = 0;
    int choiceTwo = 0;
    int factor1 = 1;
    int factor2 = 1;
    boolean choicOne = false;
    boolean choicTwo = false;
        
    public UnionKnot(JFrame knob, String title, ArrayList<LinkData> links, DefaultListModel<String> listModel, boolean concat) {
        theLinks = links;
        JDialog fram = new JDialog(new JFrame(), title, true);
        fram.setSize(450,400);
        fram.setLocationRelativeTo(knob);
        fram.setResizable(false);
        fram.setLayout(new BorderLayout());
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ok.setEnabled(false);
        JLabel label = new JLabel("Please choose two links.", SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(140,30));
        fram.add(label, BorderLayout.NORTH);
        JPanel buttons = new JPanel();
        buttons.add(ok);
        buttons.add(cancel);
        fram.add(buttons, BorderLayout.SOUTH);
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.setVisible(false);
            }
        });
        JList<String> listlinks = new JList<>(listModel);
        listlinks.setSelectedIndex(0);
        JScrollPane pane = new JScrollPane(listlinks);
        pane.setPreferredSize(new Dimension(140,300));
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        fram.add(pane, BorderLayout.WEST);
        JButton LinkOne = new JButton("--- Link 1 -->");
        JButton LinkTwo = new JButton("--- Link 2 -->");
        Container middleButtons = Box.createVerticalBox();
        middleButtons.setPreferredSize(new Dimension(140,300));
        middleButtons.add(Box.createVerticalGlue());
        middleButtons.add(LinkOne);
        middleButtons.add(Box.createVerticalGlue());
        middleButtons.add(LinkTwo);
        middleButtons.add(Box.createVerticalGlue());
        fram.add(middleButtons, BorderLayout.CENTER);
        JLabel KnotOne = new JLabel(" ");
        JLabel KnotTwo = new JLabel(" ");
        JCheckBox mirrorOne = new JCheckBox("Mirror");
        JCheckBox mirrorTwo = new JCheckBox("Mirror");
        SpinnerNumberModel spmdOne = new SpinnerNumberModel(1,1,1,1);
        SpinnerNumberModel spmdTwo = new SpinnerNumberModel(1,1,1,1);
        JSpinner spinOne = new JSpinner(spmdOne);
        JSpinner spinTwo = new JSpinner(spmdTwo);
        spinOne.setPreferredSize(new Dimension(20,16));
        spinTwo.setPreferredSize(new Dimension(30,12));
        JLabel labOne = new JLabel("Component");
        JLabel labTwo = new JLabel("Component");
        Container compPanOne = Box.createHorizontalBox();
        Container compPanTwo = Box.createHorizontalBox();
        compPanOne.add(labOne);
        compPanOne.add(spinOne);
        compPanTwo.add(labTwo);
        compPanTwo.add(spinTwo);
        JPanel paneOne = new JPanel(new GridLayout(3,1));
        JPanel paneTwo = new JPanel(new GridLayout(3,1));
        paneOne.add(KnotOne);
        paneOne.add(mirrorOne);
        paneTwo.add(KnotTwo);
        paneTwo.add(mirrorTwo);
        if (concat) {
            paneOne.add(compPanOne);
            paneTwo.add(compPanTwo);
        }
        Container rightStuff = Box.createVerticalBox();
        rightStuff.add(Box.createVerticalGlue());
        rightStuff.add(paneOne);
        rightStuff.add(Box.createVerticalGlue());
        rightStuff.add(paneTwo);
        rightStuff.add(Box.createVerticalGlue());
        rightStuff.setPreferredSize(new Dimension(160,300));
        fram.add(rightStuff, BorderLayout.EAST);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.setVisible(false);
                Link link1 = theLinks.get(choiceOne).chosenLink();
                Link link2 = theLinks.get(choiceTwo).chosenLink();
                int k = link1.crossingLength() + link2.crossingLength();
                int[] crossings = new int[k];
                int[][] paths = new int[k][4];
                if (mirrorOne.isSelected()) factor1 = -1;
                for (int i = 0; i < link1.crossingLength(); i++) {
                    crossings[i] = factor1 * link1.getCross(i);
                    for (int j = 0; j < 4; j++) paths[i][j] = link1.getPath(i, j);
                }
                if (mirrorTwo.isSelected()) factor2 = -1;
                int m = link1.crossingLength();
                for (int i = 0; i < link2.crossingLength(); i++) {
                    crossings[m+i] = factor2 * link2.getCross(i);
                    for (int j = 0; j < 4; j++) paths[m+i][j] = link2.getPath(i, j)+2*m;
                }
                name = " U ";
                boolean abort = false;
                int unlinks = link1.unComponents()+link2.unComponents();
                int leavOut = -1;
                if (concat) {
                    int comp2 = (int) spinTwo.getValue() - 1;
                    if (comp2 >= link2.relComponents()) unlinks--;
                    else leavOut = comp2;
                }
                ArrayList<int[]> newOr = new ArrayList<int[]>();
                for (int i = 0; i < link1.relComponents(); i++) newOr.add(link1.orientation(i));
                for (int i = 0; i < link2.relComponents(); i++) {
                    if (i != leavOut) {
                        int[] nwOr = new int[2];
                        nwOr[0] = link2.orientation(i)[0]+link1.crossingLength();
                        nwOr[1] = link2.orientation(i)[1];
                        newOr.add(nwOr);
                    }
                }
                if (concat) {
                    name = " # ";
                    int comp1 = (int) spinOne.getValue() - 1;
                    int comp2 = (int) spinTwo.getValue() - 1;
                    if (link1.components() == 0 || link2.components() == 0) abort = true;
                    else {
                        int number1 = link1.getComponentPath(comp1);
                        int number2 = link2.getComponentPath(comp2)+2*m;
                        boolean notonce = true;
                        boolean notwice = true;
                        int a1 = 0;
                        int a2 = 0;
                        while (notonce|notwice) {
                            if (paths[a1][a2] == number1) {
                                if (notonce) notonce = false;
                                else notwice = false;
                            }
                            if (notwice) {
                                a2++;
                                if (a2 == 4) {
                                    a2 = 0;
                                    a1++;
                                }
                            }
                        }
                        paths[a1][a2] = number2;
                        a1 = m;
                        a2 = 0;
                        notonce = true;
                        notwice = true;
                        while (notonce|notwice) {
                            if (paths[a1][a2] == number2) {
                                if (notonce) notonce = false;
                                else notwice = false;
                            }
                            if (notwice) {
                                a2++;
                                if (a2 == 4) {
                                    a2 = 0;
                                    a1++;
                                }
                            }
                        }
                        paths[a1][a2] = number1;
                    }
                }
                String sign1 = "";
                String sign2 = "";
                if (factor1 == -1) sign1 = "-";
                if (factor2 == -1) sign2 = "-";
                name = sign1+theLinks.get(choiceOne).name+name+sign2+theLinks.get(choiceTwo).name;
                if (!abort) {
                    knot = new Link(crossings,paths,newOr,unlinks);
                }
            }
        });
        LinkOne.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int u = listlinks.getSelectedIndex();
                String name = theLinks.get(u).name;
                KnotOne.setText(name);
                choicOne = true;
                if (choicTwo) ok.setEnabled(true);
                choiceOne = u;
                if (concat) {
                    int max = theLinks.get(u).links.get(0).components();
                    if (max < 1) max = 1;
                    spmdOne.setMaximum(max);
                    spmdOne.setValue(1);
                }
            }
        });
        LinkTwo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int u = listlinks.getSelectedIndex();
                name = theLinks.get(u).name;
                KnotTwo.setText(name);
                choicTwo = true;
                if (choicOne) ok.setEnabled(true);
                choiceTwo = u;
                if (concat) {
                    int max = theLinks.get(u).links.get(0).components();
                    if (max < 1) max = 1;
                    spmdTwo.setMaximum(max);
                    spmdTwo.setValue(1);
                }
            }
        });
        fram.setVisible(true);
    }
}
