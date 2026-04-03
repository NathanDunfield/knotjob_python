/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knotjob.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author Dirk
 */
public class ExportDialog extends JDialog {
    
    private final JFrame frame;
    private final JRadioButton tikzButton;
    private final JRadioButton povrButton;
    private final JRadioButton thrdButton;
    private final JRadioButton fourButton;
    private int choice;
    private final ArrayList<Integer> options;
    
    public ExportDialog(JFrame fram, String title, int[] opts) {
        super(fram, title, true);
        frame = fram;
        choice = -1;
        options = new ArrayList<Integer>();
        for (int i : opts) options.add(i);
        tikzButton = new JRadioButton("TikzPicture File");
        povrButton = new JRadioButton("Pov-Ray File");
        thrdButton = new JRadioButton("3D-Picture");
        fourButton = new JRadioButton("CSV File");
    }
    
    private void setUp() {
        JPanel wholePanel = new JPanel();
        setUpOptions(wholePanel);
        JPanel buttoPanel = new JPanel();
        buttoPanel.setLayout(new GridLayout(1,2));
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
                if (tikzButton.isSelected()) choice = 0;
                if (povrButton.isSelected()) choice = 1;
                if (thrdButton.isSelected()) choice = 2;
                if (fourButton.isSelected()) choice = 3;
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        add(wholePanel);
        this.setSize(new Dimension(250,150));
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setVisible(true);
    }
    
    private void setUpOptions(JPanel wholePanel) {
        wholePanel.setLayout(new BorderLayout());
        int grd = options.size();
        JPanel choicePanel = new JPanel(new GridLayout(grd,1));
        ButtonGroup buttons = new ButtonGroup();
        tikzButton.setSelected(true);
        if (options.contains(0)) buttons.add(tikzButton);
        if (options.contains(1)) buttons.add(povrButton);
        if (options.contains(2)) buttons.add(thrdButton);
        if (options.contains(3)) buttons.add(fourButton);
        if (options.contains(0)) choicePanel.add(tikzButton);
        if (options.contains(1)) choicePanel.add(povrButton);
        if (options.contains(2)) choicePanel.add(thrdButton);
        if (options.contains(3)) choicePanel.add(fourButton);
        JPanel anotherPanel = new JPanel();
        anotherPanel.add(choicePanel);
        wholePanel.add(anotherPanel, BorderLayout.NORTH);
    }
    
    public int getValue() {
        setUp();
        return choice;
    }
}
