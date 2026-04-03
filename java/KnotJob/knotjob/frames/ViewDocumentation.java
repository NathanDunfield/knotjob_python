/*

Copyright (C) 2020-22 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;
import knotjob.Options;

/**
 *
 * @author dirk
 */
public class ViewDocumentation extends JFrame {
    
    private final JFrame frame;
    private final JPanel centerPanel;
    private final String[] topics;
    private final Options options;
    private int active;
    private Color[] colors;
    private boolean centered;
    
    public ViewDocumentation(JFrame fram, String title, Options opts) {
        super(title);
        frame = fram;
        options = opts;
        centerPanel = new JPanel();
        topics = new String[] {"New Links", "Open Links", "Save Links", "Export Links", 
                "s-Invariants", "Lipshitz-Sarkar Invariants", "Graded s-Invariants", 
                "Khovanov cohomology", "Stable Homotopy Types", "Minimize Girth", 
                "Combine Crossings"};
        active = -1;
    }

    public void setUpStuff() {
        this.setSize(800,600);
        this.setResizable(false);
        this.setLocationRelativeTo(frame);
        this.setLayout(new BorderLayout());
        JPanel topicPanel = topicsPanel();
        setupCenter(-1);
        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("Close");
        buttonPanel.add(closeButton);
        this.add(topicPanel, BorderLayout.WEST);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        this.setVisible(true);
    }
    
    private JPanel topicsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.CYAN);
        JLabel topicLabel = new JLabel("Topics :", SwingConstants.CENTER);
        JPanel topicPanel = new JPanel();
        topicPanel.add(topicLabel);
        JPanel topicsPanel = new JPanel();
        topicsPanel.setLayout(new GridLayout(topics.length, 1));
        panel.setLayout(new BorderLayout());
        panel.add(topicPanel, BorderLayout.NORTH);
        for (int i = 0; i < topics.length; i++) {
            JButton topicButton = new JButton(topics[i]);
            topicButton.addActionListener(actionOf(i));
            JPanel topcPanel = new JPanel();
            topcPanel.add(topicButton);
            topcPanel.setBackground(Color.GRAY);
            topcPanel.setPreferredSize(new Dimension(200, 50));
            topicsPanel.add(topcPanel);
        }
        //topicsPanel.setPreferredSize(new Dimension(220, 300));
        JScrollPane scroller = new JScrollPane();
        scroller.getViewport().add(topicsPanel);
        //scroller.setPreferredSize(new Dimension(220, 300));
        panel.add(scroller, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(220, 500));
        return panel;
    }

    private ActionListener actionOf(int topic) {
        ActionListener action = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (topic != active) {
                    setupCenter(topic);
                    active = topic;
                }
            }
        };
        return action;
    }
    
    private void setupCenter(int typ) {
        centerPanel.removeAll();
        if (typ == -1) {
            JLabel standardLabel = new JLabel("Please choose a topic.", SwingConstants.CENTER);
            centerPanel.add(standardLabel);
        }
        else {
            RTFEditorKit rtf = new RTFEditorKit();
            JEditorPane thePane = new JEditorPane();
            thePane.setEditorKit(rtf);
            thePane.setEditable(false);
            JScrollPane scroller = new JScrollPane();
            scroller.getViewport().add(thePane);
            thePane.setPreferredSize(new Dimension(570,534));
            scroller.setPreferredSize(new Dimension(570,534));
            try {
                FileInputStream fi = new FileInputStream("Documentation"+options.getSlash()+""+topics[typ]+".rtf");
                rtf.read(fi, thePane.getDocument(), 0);
            }
            catch (IOException | BadLocationException ex) {
                try {
                    thePane.getDocument().insertString(0, "File not found.", null);
                } 
                catch (BadLocationException ex1) {
                    
                }
            }
            centerPanel.add(scroller);
        }
        centerPanel.repaint();
        centerPanel.revalidate();
    }
    
}
