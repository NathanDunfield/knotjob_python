/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knotjob.dialogs;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author dirk
 */
public class BettiFilterDialog extends CompFilterDialog {
    
    private final JSpinner homSpinner;
    private final SpinnerNumberModel homModel;
    
    public BettiFilterDialog(JFrame fram, String title, boolean bo, int l, int u, int hd) {
        super(fram, title, bo, l, u);
        homModel = new SpinnerNumberModel(hd, -65536, 65536, 1);
        homSpinner = new JSpinner(homModel);
    }
    
    @Override
    public void setupDialog() {
        setSize(400,250);
        setLocationRelativeTo(frame);
        setResizable(false);
        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        JButton okayButton = new JButton("OK");
        JButton cancButton = new JButton("Cancel");
        buttonPanel.add(okayButton);
        buttonPanel.add(cancButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
        JPanel infoPanel = new JPanel();
        int lines = 5;
        infoPanel.setLayout(new GridLayout(lines,1));
        JPanel topPanel = new JPanel();
        JLabel lowBoundLabel = new JLabel("Lower Bound : ");
        topPanel.add(lowBoundLabel);
        topPanel.add(lowerSpinner);
        JPanel midPanel = new JPanel();
        midPanel.add(aboveBox);
        JPanel lowPanel = new JPanel();
        JLabel uppBoundLabel = new JLabel("Upper Bound : ");
        lowPanel.add(uppBoundLabel);
        lowPanel.add(upperSpinner);
        JPanel homPanel = new JPanel();
        JLabel homLabel = new JLabel("Homological Degree : ");
        homPanel.add(homLabel);
        homPanel.add(homSpinner);
        JPanel reducPanel = new JPanel();
        reducPanel.add(reduced);
        reducPanel.add(odded);
        infoPanel.add(topPanel);
        infoPanel.add(midPanel);
        infoPanel.add(lowPanel);
        infoPanel.add(homPanel);
        infoPanel.add(reducPanel);
        this.add(infoPanel, BorderLayout.CENTER);
        lowerSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!aboveBox.isSelected()) return;
                int val = (int) lowerSpinner.getValue();
                int up = (int) upperSpinner.getValue();
                if (val > up) lowerSpinner.setValue(up);
                lowerModel.setMaximum(up);
                upperModel.setMinimum((int)lowerSpinner.getValue());
            }
        });
        upperSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = (int) upperSpinner.getValue();
                int bt = (int) lowerSpinner.getValue();
                if (val < bt) upperSpinner.setValue(bt);
                lowerModel.setMaximum((int)upperSpinner.getValue());
                upperModel.setMinimum(bt);
            }
        });
        aboveBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                boolean ch = aboveBox.isSelected();
                if (ch) {
                    int lval = (int) lowerSpinner.getValue();
                    int uval = (int) upperSpinner.getValue();
                    if (lval > uval) lowerSpinner.setValue(uval);
                    lowerModel.setMaximum(uval);
                    upperModel.setMinimum((int) lowerSpinner.getValue());
                    upperSpinner.setEnabled(true);
                }
                else {
                    upperSpinner.setEnabled(false);
                    lowerModel.setMaximum(totalmax);
                }
            }
        });
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okay = true;
                dispose();
            }
        });
        cancButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        setVisible(true);
        bddAbove = aboveBox.isSelected();
        lowerBound = (int) lowerSpinner.getValue();
        upperBound = (int) upperSpinner.getValue();
    }

    public int getHom() {
        return (int) homSpinner.getValue();
    }
    
    public String getHomString() {
        String name = String.valueOf(homSpinner.getValue())+"-th";
        return name;
    }
    
}
