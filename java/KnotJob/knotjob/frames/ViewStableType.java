/*

Copyright (C) 2022-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import knotjob.Options;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class ViewStableType extends JFrame {
    
    private final LinkData theLink;
    private final JFrame frame;
    private final int odd;
    private final String theType;
    private final ArrayList<String> theInfo;
    private JButton latexBut;
    private JButton closeBut;
    private JScrollPane quantumScrollPane;
    private JScrollPane stableScrollPane;
    private GraphComponent comp;
    private JList<String> quantumList;
    private final ArrayList<Integer> qPositions;
    private final Options opts;
    
    public ViewStableType(LinkData link, JFrame frm, int od, Options ops) {
        theLink = link;
        frame = frm;
        odd = od;
        String typ = theLink.stEven;
        if (odd == 0) typ = theLink.stOdd;
        if (odd == 1) typ = theLink.stOde;
        theType = typ;
        ArrayList<String> info = theLink.stEvenInfo;
        if (odd == 0) info = theLink.stOddInfo;
        if (odd == 1) info = theLink.stOdeInfo;
        theInfo = info;
        qPositions = new ArrayList<Integer>();
        opts = ops;
    }
    
    public void setupViewer(String title) {
        this.setTitle(title);
        this.setResizable(false);
        this.setSize(650, 400);
        setupFrame();
        this.setLocationRelativeTo(frame);
        this.setVisible(true);
    }
    
    private void setupFrame() {
        this.setLayout(new BorderLayout());
        JPanel paneList = new JPanel(new BorderLayout());
        JLabel labeList = new JLabel("Quantum degrees", SwingConstants.CENTER);
        labeList.setPreferredSize(new Dimension(120,30));
        paneList.add(labeList, BorderLayout.NORTH);
        setupQuantumList();
        paneList.add(quantumScrollPane, BorderLayout.CENTER);
        JPanel middlePanel = new JPanel();
        if (qPositions.isEmpty()) trivialSituation(middlePanel);
        else {
            setupGraphComponent(0);
            stableScrollPane = new JScrollPane(comp);
            stableScrollPane.setPreferredSize(new Dimension(450,328));
            stableScrollPane.getViewport().setBackground(middlePanel.getBackground());
            middlePanel.add(stableScrollPane);
        }
//        middlePanel.add(stableScrollPane);
        JPanel buttonPane = new JPanel();
        latexBut = new JButton("Save as LaTeX file");
        closeBut = new JButton("Close");
        latexBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveStableType();
            }
        });
        closeBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        });
        if (qPositions.isEmpty()) latexBut.setEnabled(false);
        buttonPane.add(latexBut);
        buttonPane.add(closeBut);
        add(paneList, BorderLayout.WEST);
        add(middlePanel, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.SOUTH);
    }
    
    private void saveStableType() {
        ArrayList<String> commands = new ArrayList<String>();
        commands.add("\\documentclass[border=1bp]{standalone}");
        commands.add("\\usepackage{tikz}");
        commands.add("\\begin{document}");
        commands.add("\\begin{tikzpicture}");
        String start = "Odd ($\\varepsilon = "+odd+"$)";
        if (odd == 2) start = "Even";
        commands.add("\\node at (0,0) {"+start+" Stable Homotopy Type of "
                +theLink.name+" at $"+quantumList.getSelectedValue()+"$};");
        ArrayList<String> tikzcommands = comp.getTikzCommands();
        for (String com : tikzcommands) commands.add(com);
        commands.add("\\end{tikzpicture}");
        commands.add("\\end{document}");
        String theTitle = "Odd("+odd+")";
        if (odd == 2) theTitle = "Even";
        String qs = quantumList.getSelectedValue().replaceAll(" ", "");
        theTitle = "Stable_Type_"+theTitle+"_"+theLink.name+"_"+qs;
        saveCommands(theTitle, commands);
    }
    
    private void saveCommands(String title, ArrayList<String> commands) {
        JFileChooser chooser = new JFileChooser();
        if (opts.getSaveKhovanov() != null) chooser.setCurrentDirectory(opts.getSaveKhovanov());
        FileNameExtensionFilter filter = new FileNameExtensionFilter("LaTeX files (*.tex)", "tex");
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File(title+".tex"));
        int val = chooser.showSaveDialog(this);
        if (val == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                opts.setSaveKhovanov(chooser.getCurrentDirectory());
                String fname = file.getAbsolutePath();
                if(!fname.endsWith(".tex") ) {
                    file = new File(fname + ".tex");
                }
                try (FileWriter fw = new FileWriter(file);PrintWriter pw = new PrintWriter(fw)) {
                    for (String command : commands) pw.println(command);
                }
            }
            catch (IOException e) {

            }
        }
    }
    
    private void setupGraphComponent(int pos) {
        if (pos < 0) {
            comp = new GraphComponent(new StableGraph());
            return;
        }
        pos = pos/3;
        StableGraph graph = new StableGraph(theInfo.get(pos), theInfo.get(pos+1), theInfo.get(pos+2));
        comp = new GraphComponent(graph);
        comp.setPreferredSize(new Dimension(120+50 * graph.getX(), 40 +50 * graph.getY()));
    }
    
    private void trivialSituation(JPanel panel) {
        JLabel label = new JLabel("All stable homotopy types are trivially determined by homology");
        panel.add(label);
    }
    
    
    private void setupQuantumList() {
        DefaultListModel<String> qList = new DefaultListModel<String>();
        addToTheqList(qList);
        quantumList = new JList<String>(qList);
        quantumList.setSelectedIndex(0);
        quantumList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                @SuppressWarnings("unchecked")
                JList<String> list = (JList<String>) e.getSource();
                int u = list.getSelectedIndex();
                if ( isActive() & u >= 0 ) {
                    setNewStablePane(qPositions.get(u));
                }
            }
        });
        quantumScrollPane = new JScrollPane(quantumList);
        quantumScrollPane.setPreferredSize(new Dimension(150,300));
    }

    private void addToTheqList(DefaultListModel<String> qList) {
        ArrayList<Integer> theQs = getTheQs();
        for (int i = 0; i < theQs.size()/2; i++) {
            String label = "q = "+theQs.get(2*i);
            qList.addElement(label);
            qPositions.add(theQs.get(2*i+1));
        }
    }
    
    private ArrayList<Integer> getTheQs() {
        ArrayList<Integer> qs = new ArrayList<Integer>();
        ArrayList<Integer> ps = new ArrayList<Integer>();
        if ("trivial".equals(theType)) return qs;
        String all = theType;
        while (!"".equals(all)) {
            int a = all.indexOf("=")+1;
            int b = all.indexOf(":");
            int c = all.indexOf(";");
            int q = Integer.parseInt(all.substring(a, b));
            int f;
            try {
                f = Integer.parseInt(all.substring(b+1, c));
            }
            catch (NumberFormatException e) {
                f = -1;
            }
            int pos = getPosition(qs, q);
            qs.add(pos, q);
            ps.add(pos, f);
            all = all.substring(c+1);
        }
        for (int u = 0; u < ps.size(); u++) qs.add(2*u+1, ps.get(u));
        return qs;
    }
    
    private int getPosition(ArrayList<Integer> qs, int q) {
        int pos = 0;
        while (pos < qs.size()) {
            if (qs.get(pos) < q) pos++;
            else break;
        }
        return pos;
    }
    
    private void setNewStablePane(int u) {
        setupGraphComponent(3*u);
        stableScrollPane.getViewport().removeAll();
        stableScrollPane.getViewport().add(comp);
    }
    
}
