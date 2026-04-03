/*

Copyright (C) 2021-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import knotjob.Options;
import knotjob.homology.Homology;
import knotjob.homology.QuantumCohomology;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class ViewBLTInvariant extends JFrame {
    
    private final JSlider zoomSlider;
    private final JFrame frame;
    private final JButton closeButton;
    private final JButton exportButton;
    private final LinkData theLink;
    //private final JPanel infoPanel;
    private final JComboBox<String> charSpinner;
    private final JLabel collLabel;
    private final JSpinner eSpinner;
    private final SpinnerListModel eModel;
    private final ArrayList<String> theChars;
    private final String[] items;
    private final ArrayList<Integer> theEs;
    private final int typ;
    private final JScrollPane scroller;
    private final BLTComponent bLTComp;
    private final Options options;
    
    public ViewBLTInvariant(String name, JFrame fram, LinkData tLnk, int tp, Options opts) {
        super(name);
        theLink = tLnk;
        frame = fram;
        options = opts;
        typ = tp;
        zoomSlider = new JSlider(JSlider.HORIZONTAL, 10, 50, 24);
        closeButton = new JButton("Close");
        exportButton = new JButton("Export");
        //infoPanel = new JPanel();
        theChars = theLink.bltCharacteristics(typ);
        items = new String[theChars.size()];
        for (int i = 0; i < theChars.size(); i++) items[i] = theChars.get(i);
        charSpinner = new JComboBox<String>(items);//charModel);
        charSpinner.setPreferredSize(new Dimension(76, 24));
        theEs = theLink.bltEPages(theChars.get(0), typ);
        eModel = new SpinnerListModel(theEs);
        eSpinner = new JSpinner(eModel);
        eSpinner.setPreferredSize(new Dimension(48, 24));
        collLabel = new JLabel("Collapses at Page "+theEs.size());
        collLabel.setPreferredSize(new Dimension(150, 24));
        bLTComp = new BLTComponent(theChars.get(0), typ, theLink);
        scroller = new JScrollPane(bLTComp);
    }
    
    public void setUp(String charTitle) {
        setSize(800,600);
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(frame);
        JPanel choicePanel = new JPanel();
        choicePanel.setLayout(new GridLayout(1, 3));
        JPanel charPanel = new JPanel();
        charPanel.add(new JLabel(charTitle));
        charPanel.add(charSpinner);
        choicePanel.add(charPanel);
        JPanel collPanel = new JPanel();
        collPanel.add(collLabel);
        choicePanel.add(collPanel);
        JPanel ePanel = new JPanel();
        ePanel.add(new JLabel("E-Page :"));
        ePanel.add(eSpinner);
        choicePanel.add(ePanel);
        JPanel buttPane = new JPanel();
        JLabel zoomLabel = new JLabel("Zoom : ");
        buttPane.add(zoomLabel);
        buttPane.add(zoomSlider);
        buttPane.add(exportButton);
        buttPane.add(closeButton);
        add(choicePanel, BorderLayout.NORTH);
        add(scroller, BorderLayout.CENTER);
        add(buttPane, BorderLayout.SOUTH);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        });
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveBLTSS();
            }
        });
        charSpinner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String chr = items[charSpinner.getSelectedIndex()];
                eModel.setList(theLink.bltEPages(String.valueOf(chr), typ));
                eSpinner.setValue(1);
                bLTComp.setCharacteristic(chr);
                collLabel.setText("Collapses at Page "+eModel.getList().size());
                scroller.getViewport().revalidate();
            }
        });
        eSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                bLTComp.setEPage((int) eSpinner.getValue());
            }
        });
        zoomSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                bLTComp.setBoxsize(zoomSlider.getValue());
                bLTComp.setTheSize();
                scroller.getViewport().revalidate();
            }
        });
        bLTComp.setTheSize();
        setVisible(true);
        closeButton.requestFocus();
    }
    
    private void saveBLTSS() {
        ArrayList<String> commands = new ArrayList<String>();
        String field = "$\\mathbb{Q}$";
        try {
            if (Integer.parseInt(bLTComp.characteristic) > 0 && typ < 6) field = "$\\mathbb{F}_{"+bLTComp.characteristic+"}$";
        }
        catch (NumberFormatException e) {
            field = field+"("+bLTComp.characteristic+")";
        }
        if (typ == 1 || typ == 5) field = field+" reduced";
        commands.add("\\documentclass[border=1bp]{standalone}");
        commands.add("\\usepackage{amssymb,amsmath,array,diagbox}");
        commands.add("\\begin{document}");
        commands.add("\\setlength\\extrarowheight{2pt}");
        addFirstLine(commands);
        int h = 2+bLTComp.hmax - bLTComp.hmin;
        commands.add("\\multicolumn{"+h+"}{c}{BLTSS over "+field+"}\\\\");
        for (int page = 1; page <= eModel.getList().size(); page++) {
            commands.add("\\multicolumn{"+h+"}{c}{Page "+page+"} \\\\");
            addThePage(page-1, commands);
        }
        commands.add("\\end{tabular}");
        commands.add("\\end{document}");
        String prefix = "BLT_";
        if (typ >= 2) prefix = "SL3SS_";
        String theTitle = prefix+theLink.name+"_"+bLTComp.characteristic;
        if (typ == 1 || typ == 5) theTitle = theTitle+"_r";
        saveCommands(theTitle, commands);
    }
    
    private void saveCommands(String title, ArrayList<String> commands) {
        JFileChooser chooser = new JFileChooser();
        if (options.getSaveKhovanov() != null) chooser.setCurrentDirectory(options.getSaveKhovanov());
        FileNameExtensionFilter filter = new FileNameExtensionFilter("LaTeX files (*.tex)", "tex");
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File(title+".tex"));
        int val = chooser.showSaveDialog(this);
        if (val == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                options.setSaveKhovanov(chooser.getCurrentDirectory());
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
    
    private void addFirstLine(ArrayList<String> commands) {
        String next = "\\begin{tabular}{|c||";
        for (int i = bLTComp.hmin; i <= bLTComp.hmax; i++) next = next + "c|";
        next = next +"}";
        commands.add(next);
    }
    
    private void addThePage(int page, ArrayList<String> commands) {
        String after = "\\backslashbox{\\!$q$\\!}{\\!$h$\\!} ";
        for (int i = bLTComp.hmin; i <= bLTComp.hmax; i++) after = after + "& $"+i+"$ ";
        after = after + "\\\\";
        commands.add("\\hline");
        commands.add(after);
        commands.add("\\hline");
        commands.add("\\hline");
        String[][] theMatrix = getLaTeXTable(page, bLTComp.qmin, bLTComp.qmax, bLTComp.hmin, bLTComp.hmax);
        for (int r = bLTComp.qmax; r >= bLTComp.qmin; r = r - 2) {
            String theLine = "$"+r+"$ ";
            for (int j = 0; j < theMatrix[0].length; j++) 
                theLine = theLine+" & "+theMatrix[(r-bLTComp.qmin)/2][j];
            theLine = theLine +" \\\\";
            commands.add(theLine);
            commands.add("\\hline");
        }
    }
    
    private String[][] getLaTeXTable(int page, int qmin, int qmax, int hmin, int hmax) {
        String[][] theTable;
        theTable = new String[((qmax-qmin)/2)+1][hmax-hmin+1];
        for (String[] theTable1 : theTable) {
            for (int j = 0; j < theTable[0].length; j++) {
                theTable1[j] = " ";
            }
        }
        ArrayList<String> qStrings = bLTComp.quantumStrings(page);
        for (String qString : qStrings) {
            QuantumCohomology quant = new QuantumCohomology(qString);
            if (quant.qdeg() >= qmin && quant.qdeg() <= qmax) {
                for (Homology hom : quant.getHomGroups()) {
                    theTable[(quant.qdeg()-qmin)/2][hom.hdeg() - hmin] = "$"+hom.getBetti()+"$";
                }
            }
        }
        return theTable;
    }
    
}
