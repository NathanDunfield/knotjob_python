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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import knotjob.filters.Filter;

/**
 *
 * @author Dirk
 */
public class FilterAndOrDialog extends JDialog {
    
    private final boolean and;
    private boolean okay;
    private final ArrayList<Filter> filters;
    private final JFrame frame;
    private final JButton okayButton;
    private final JButton cancelButton;
    
    public FilterAndOrDialog(JFrame frm, String title, boolean bo, boolean nd, ArrayList<Filter> flts) {
        super(frm,title,bo);
        and = nd;
        filters = flts;
        frame = frm;
        okayButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        okay = true;
    }
    
    public ArrayList<Filter> getFilters() {
        this.setSize(640,360);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        JPanel headLine = new JPanel();
        String log = "OR.";
        if (and) log = "AND.";
        JLabel headLabe = new JLabel("Choose filters to be combined by "+log);
        headLine.add(headLabe,SwingConstants.CENTER);
        this.add(headLine, BorderLayout.NORTH);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1,3));
        JPanel leftPanel = new JPanel();
        DefaultListModel<String> filterList = listFrom(filters);
        JList<String> possFilters = new JList<String>(filterList);
        JScrollPane leftPane = new JScrollPane(possFilters);
        possFilters.setSelectedIndex(0);
        leftPane.setPreferredSize(new Dimension(200,240));
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
        DefaultListModel<String> localFilt = listFrom(new ArrayList<Filter>(0));
        JList<String> chosPrimes = new JList<String>(localFilt);
        JScrollPane rightPane = new JScrollPane(chosPrimes);
        rightPane.setPreferredSize(new Dimension(200,240));
        rightPanel.add(rightPane);
        mainPanel.add(leftPanel);
        mainPanel.add(centrePanel);
        mainPanel.add(rightPanel);
        this.add(mainPanel,BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1,2));
        JPanel cancelPanel = new JPanel();
        JPanel okayPanel = new JPanel();
        cancelPanel.add(cancelButton);
        okayPanel.add(okayButton);
        buttonPanel.add(okayPanel);
        buttonPanel.add(cancelPanel);
        this.add(buttonPanel, BorderLayout.SOUTH);
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                int[] chosen = possFilters.getSelectedIndices();
                for (int i = chosen.length-1; i >= 0; i--) {
                    String filter = filterList.getElementAt(chosen[i]);
                    filterList.removeElementAt(chosen[i]);
                    int pos = position(filter,localFilt);
                    localFilt.add(pos, filter);
                }
            }
        });
        moveAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = filterList.getSize()-1; i >= 0; i--) {
                    String filter = filterList.getElementAt(i);
                    filterList.removeElementAt(i);
                    int pos = position(filter,localFilt);
                    localFilt.add(pos, filter);
                }
            }
        });
        remoOne.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] chosen = chosPrimes.getSelectedIndices();
                for (int i = chosen.length-1; i >= 0; i--) {
                    String filter= localFilt.getElementAt(chosen[i]);
                    localFilt.removeElementAt(chosen[i]);
                    int pos = position(filter,filterList);
                    filterList.add(pos, filter);
                }
            }
        });
        remoAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = localFilt.getSize()-1; i >= 0; i--) {
                    String filter = localFilt.getElementAt(i);
                    localFilt.removeElementAt(i);
                    int pos = position(filter,filterList);
                    filterList.add(pos, filter);
                }
            }
        });
        setVisible(true);
        if (!okay) return null;
        return infoFrom(localFilt);
    }

    private DefaultListModel<String> listFrom(ArrayList<Filter> filt) {
        DefaultListModel<String> theList = new DefaultListModel<String>();
        for (Filter fl : filt) theList.addElement(fl.getName());
        return theList;
    }
    
    private int position(String prime, DefaultListModel<String> theList) {
        int p = 0;
        int k = posOf(prime);
        boolean found = false;
        while (p < theList.getSize() && !found) {
            String comp = theList.getElementAt(p);
            if (k < posOf(comp)) found = true;
            else p++;
        }
        return p;
    }

    private int posOf(String prime) {
        int i = 0;
        boolean found = false;
        while (!found) {
            if (prime.equals(filters.get(i).getName())) found = true;
            else i++;
        }
        return i;
    }

    private ArrayList<Filter> infoFrom(DefaultListModel<String> localFilt) {
        if (localFilt.size() < 2) return null;
        ArrayList<Filter> theFilters = new ArrayList<Filter>(localFilt.size());
        for (int i = 0; i < localFilt.size(); i++) {
            int k = posOf(localFilt.elementAt(i));
            theFilters.add(filters.get(k));
        }
        return theFilters;
    }
    
}
