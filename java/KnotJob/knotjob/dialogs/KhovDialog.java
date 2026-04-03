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
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import knotjob.Knobster;

/**
 *
 * @author Dirk
 */
public class KhovDialog extends JDialog {
    
    private final Knobster knobby;
    private final boolean powers;
    private final boolean mod;
    private boolean okay;
    private JSpinner primeSpinner;
    private JSpinner powerSpinner;
    private final JRadioButton integralButton;
    private final JRadioButton rationalButton;
    private final JRadioButton localizeButton;
    private final JRadioButton modularButton;
    
    public KhovDialog(Knobster kjob, boolean md) {
        super(kjob, "Khovanov Cohomology", true);
        mod = md;
        knobby = kjob;
        powers = true;
        okay = false;
        integralButton = new JRadioButton("Integral");
        rationalButton = new JRadioButton("Rational");
        localizeButton = new JRadioButton("Local");
        modularButton = new JRadioButton("Modular");
    }
    
    public KhovDialog(Knobster kjob, boolean md, String title, boolean pwr) {
        super(kjob, title, true);
        mod = md;
        knobby = kjob;
        powers = pwr;
        okay = false;
        integralButton = new JRadioButton("Integral");
        rationalButton = new JRadioButton("Rational");
        localizeButton = new JRadioButton("Local");
        modularButton = new JRadioButton("Modular");
    }
    
    public void setUp() {
        JPanel wholePanel = new JPanel();
        if (mod) setUpMod(wholePanel);
        else setUpChar(wholePanel);
        JPanel buttoPanel = new JPanel();
        buttoPanel.setLayout(new GridLayout(1, 2));
        JPanel cancelPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        cancelPanel.add(cancelButton);
        JPanel okayPanel = new JPanel();
        JButton okayButton = new JButton("OK");
        okayPanel.add(okayButton);
        buttoPanel.add(okayPanel);
        buttoPanel.add(cancelPanel);
        wholePanel.add(buttoPanel);
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                okay = true;
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        add(wholePanel);
    }

    private void setUpMod(JPanel wholePanel) {
        int paneldepth = 2;
        if (powers) paneldepth = 3;
        wholePanel.setLayout(new GridLayout(paneldepth, 1));
        JPanel primePanel = new JPanel();
        primePanel.setLayout(new GridLayout(1, 2));
        JPanel pritePanel = new JPanel();
        JLabel priteLabel = new JLabel("Choose a prime : ");
        pritePanel.add(priteLabel);
        SpinnerListModel modelPrimes = new SpinnerListModel(knobby.options.getPrimes());
        primeSpinner = new JSpinner(modelPrimes);
        primeSpinner.setPreferredSize(new Dimension(40,20));
        JPanel spinpPanel = new JPanel();
        spinpPanel.add(primeSpinner);
        primePanel.add(pritePanel);
        primePanel.add(spinpPanel);
        wholePanel.add(primePanel);
        JPanel powerPanel = new JPanel();
        powerPanel.setLayout(new GridLayout(1,2));
        JPanel pwrtePanel = new JPanel();
        JLabel pwrteLabel = new JLabel("Choose a power : ");
        pwrtePanel.add(pwrteLabel);
        ArrayList<ArrayList<Integer>> possPowers = new ArrayList<ArrayList<Integer>>();
        for (int pwr : knobby.options.getPowers()) {
            ArrayList<Integer> possPwr = new ArrayList<Integer>();
            for (int r = 1; r <= pwr; r++) possPwr.add(r);
            possPowers.add(possPwr);
        }
        SpinnerListModel modelPowers = new SpinnerListModel(possPowers.get(0));
        powerSpinner = new JSpinner(modelPowers);
        powerSpinner.setPreferredSize(new Dimension(40,20));
        powerSpinner.setValue(2);
        JPanel spinwPanel = new JPanel();
        spinwPanel.add(powerSpinner);
        powerPanel.add(pwrtePanel);
        powerPanel.add(spinwPanel);
        if (powers) wholePanel.add(powerPanel);
        else powerSpinner.setValue(1);
        primeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int newState = (int) primeSpinner.getValue();
                int pos = knobby.options.getPrimes().indexOf(newState);
                int ppos = (int) powerSpinner.getValue();
                if (ppos > possPowers.get(pos).size()) ppos = possPowers.get(pos).size();
                SpinnerListModel md = (SpinnerListModel) powerSpinner.getModel();
                md.setList(possPowers.get(pos));
                powerSpinner.setValue(ppos);
            }
        });
    }
    
    private void setUpChar(JPanel wholePanel) {
        wholePanel.setLayout(new BorderLayout());
        JPanel choicePanel = new JPanel(new GridLayout(4, 1));
        ButtonGroup buttons = new ButtonGroup();
        integralButton.setSelected(true);
        buttons.add(integralButton);
        buttons.add(rationalButton);
        buttons.add(localizeButton);
        buttons.add(modularButton);
        choicePanel.add(integralButton);
        choicePanel.add(rationalButton);
        choicePanel.add(localizeButton);
        choicePanel.add(modularButton);
        JPanel anotherPanel = new JPanel();
        anotherPanel.add(choicePanel);
        wholePanel.add(anotherPanel, BorderLayout.NORTH);
    }
    
    public long getValue() {
        int y = 150;
        if (!mod) y = 180;
        this.setSize(new Dimension(250, y));
        this.setLocationRelativeTo(knobby);
        this.setResizable(false);
        setUp();
        this.setVisible(true);
        if (okay) return theValue();
        return -1;
    }
    
    public boolean isOkay() {
        return okay;
    }
    
    private long theValue() {
        long result = 0;
        if (mod) {
            int p = (int) primeSpinner.getValue();
            int n = (int) powerSpinner.getValue();
            int pwr = 1;
            for (int i = 0; i < n; i++) pwr = pwr * p;
            result = (long) pwr;
        }
        else {
            if (integralButton.isSelected()) result = 0;
            if (rationalButton.isSelected()) result = 1;
            if (localizeButton.isSelected()) result = choosePrimes(knobby.options.getPrimes());
            if (modularButton.isSelected()) result = 2;
        }
        return result;
    }
    
    private long choosePrimes(ArrayList<Integer> primes) {
        JDialog frame = new JDialog(knobby, "Localize Primes", true);
        frame.setSize(400,400);
        frame.setLocationRelativeTo(knobby);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        JPanel headLine = new JPanel();
        JLabel headLabe = new JLabel("Choose primes to be inverted.");
        headLine.add(headLabe,SwingConstants.CENTER);
        frame.add(headLine, BorderLayout.NORTH);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1,3));
        JPanel leftPanel = new JPanel();
        DefaultListModel<Integer> primeList = listFrom(primes);
        JList<Integer> possPrimes = new JList<Integer>(primeList);
        JScrollPane leftPane = new JScrollPane(possPrimes);
        possPrimes.setSelectedIndex(0);
        leftPane.setPreferredSize(new Dimension(60,280));
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
        DefaultListModel<Integer> localList = listFrom(new ArrayList<Integer>(0));
        JList<Integer> chosPrimes = new JList<Integer>(localList);
        JScrollPane rightPane = new JScrollPane(chosPrimes);
        rightPane.setPreferredSize(new Dimension(60,280));
        rightPanel.add(rightPane);
        mainPanel.add(leftPanel);
        mainPanel.add(centrePanel);
        mainPanel.add(rightPanel);
        frame.add(mainPanel,BorderLayout.CENTER);
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
        frame.add(buttonPanel, BorderLayout.SOUTH);
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okay = false;
                frame.dispose();
            }
        });
        moveOne.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] chosen = possPrimes.getSelectedIndices();
                for (int i = chosen.length-1; i >= 0; i--) {
                    int prime = primeList.getElementAt(chosen[i]);
                    primeList.removeElementAt(chosen[i]);
                    int pos = position(prime,localList);
                    localList.add(pos, prime);
                }
            }
        });
        moveAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = primeList.getSize()-1; i >= 0; i--) {
                    int prime = primeList.getElementAt(i);
                    primeList.removeElementAt(i);
                    int pos = position(prime,localList);
                    localList.add(pos, prime);
                }
            }
        });
        remoOne.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] chosen = chosPrimes.getSelectedIndices();
                for (int i = chosen.length-1; i >= 0; i--) {
                    int prime = localList.getElementAt(chosen[i]);
                    localList.removeElementAt(chosen[i]);
                    int pos = position(prime,primeList);
                    primeList.add(pos, prime);
                }
            }
        });
        remoAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = localList.getSize()-1; i >= 0; i--) {
                    int prime = localList.getElementAt(i);
                    localList.removeElementAt(i);
                    int pos = position(prime,primeList);
                    primeList.add(pos, prime);
                }
            }
        });
        frame.setVisible(true);
        if (okay) {
            ArrayList<Integer> chosen = new ArrayList<Integer>();
            for (int i = 0; i < localList.getSize(); i++) chosen.add(localList.getElementAt(i));
            return theValueFrom(primes, chosen);
        }
        return -1;
    }
    
    private DefaultListModel<Integer> listFrom(ArrayList<Integer> powers) {
        DefaultListModel<Integer> theList = new DefaultListModel<Integer>();
        for (int p : powers) theList.addElement(p);
        return theList;
    }
    
    private int position(int prime, DefaultListModel<Integer> theList) {
        int p = 0;
        boolean found = false;
        while (p < theList.getSize() && !found) {
            int comp = theList.getElementAt(p);
            if (prime < comp) found = true;
            else p++;
        }
        return p;
    }
    
    private long theValueFrom(ArrayList<Integer> primes, ArrayList<Integer> chosen) {
        long value = 0;
        long power = 1;
        for (int i = 0; i < primes.size(); i++) {
            if (chosen.contains(primes.get(i))) value = value - power;
            power = power * 2;
        }
        return value;
    }
}
