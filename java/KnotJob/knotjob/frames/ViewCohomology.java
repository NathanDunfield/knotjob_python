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

package knotjob.frames;

import knotjob.homology.HomologyInfo;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import knotjob.Options;
import knotjob.homology.Homology;
import knotjob.homology.QuantumCohomology;
import knotjob.homology.evenkhov.EvenKhovCalculator;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class ViewCohomology extends JFrame {
    
    protected HomologyInfo integralHom;
    protected HomologyInfo rationalHom;
    private HomologyInfo showHom;
    protected ArrayList<HomologyInfo> plusInfos;
    private final ButtonGroup groupOfButtons;
    private JRadioButton integralRadio;
    private JRadioButton rationalRadio;
    private JRadioButton modularRadio;
    private JSpinner primeSpinner;
    private JSpinner powerSpinner;
    private JButton latexBut;
    private JButton closeBut;
    private JScrollPane quantumScrollPane;
    private JScrollPane homologyScrollPane;
    private JPanel homologyPanel;
    private JLabel approxWarning;
    private JLabel improvWarning;
    private JList<String> quantumList;
    protected final LinkData link;
    protected final boolean reduced;
    protected boolean intGood;
    protected boolean ratGood;
    protected final ArrayList<Integer> availPrimes;
    protected final ArrayList<Integer> availPowers;
    protected final ArrayList<Integer> imprPrimes;
    private final Image img;
    protected final Options opts;
    
    public ViewCohomology(LinkData theLink, boolean red, Options options) {
        link = theLink;
        reduced = red;
        opts = options;
        img = opts.getImage();
        groupOfButtons = new ButtonGroup();
        availPrimes = new ArrayList<Integer>();
        availPowers = new ArrayList<Integer>();
        imprPrimes = new ArrayList<Integer>();
    }
    
    protected void getApproximation() {
        ArrayList<String> theStrings = link.unredKhovHom;
        if (reduced) theStrings = link.redKhovHom;
        HomologyInfo approxInfo;
        if (ratGood) approxInfo = rationalHom.rationalHomFrom();
        else approxInfo = new HomologyInfo(0l,1);
        approxInfo.setPrime(0l);
        ArrayList<HomologyInfo> minusInfos = new ArrayList<HomologyInfo>();
        plusInfos = new ArrayList<HomologyInfo>();
        for (int p : availPrimes) availPowers.add(0);
        char check = 'u';
        if (reduced) check = 'r';
        ArrayList<String> relInfo = getRelevantInfo(link.khovInfo, check);
        long[][] startInfo = getStartInfo(relInfo);
        for (int i = 0; i < relInfo.size(); i++) {
            String info = relInfo.get(i);
            if (positiveInfo(info)) plusInfos.add(link.theHomology(startInfo[i], theStrings));
            else minusInfos.add(link.theHomology(startInfo[i], theStrings));
        }
        for (HomologyInfo hInfo : minusInfos) { 
            for (QuantumCohomology coh : hInfo.getHomologies()) {
                approxInfo.addTorsion(coh,onlyAvailable());
            }
            ArrayList<Integer> primes = EvenKhovCalculator.getPrimes(hInfo.getPrime(), availPrimes);
            for (int i = 0; i < availPrimes.size(); i++) {
                if (!primes.contains(availPrimes.get(i))) availPowers.set(i, opts.getPowers().get(i));
            }
        }
        boolean setBetti = !ratGood;
        for (HomologyInfo hInfo : plusInfos) {
            int prime = (int) hInfo.getPrime();
            if (availPowers.get(availPrimes.indexOf(prime)) == 0) {
                ArrayList<Integer> primes = new ArrayList<Integer>(1);
                primes.add(prime);
                if (!setBetti) approxInfo.adjustBetti(hInfo);
                for (QuantumCohomology coh : hInfo.getHomologies()) {
                    approxInfo.addTorsion(coh,primes);
                    if (setBetti) approxInfo.setBetti(coh);
                }
                setBetti = false;
            }
        }
        for (HomologyInfo hInfo : plusInfos) {
            int prime = (int) hInfo.getPrime();
            if (availPowers.get(availPrimes.indexOf(prime)) == 0) {
                if (approxInfo.compareBetti(hInfo)) imprPrimes.add(prime);
            }
            availPowers.set(availPrimes.indexOf(prime),hInfo.getMaxpower());
        }
        for (int i = availPrimes.size()-1; i >= 0; i--) {
            if (availPowers.get(i) == 0) {
                availPowers.remove(i);
                availPrimes.remove(i);
            }
        }
        if (availPowers.size() == opts.getPowers().size() && imprPrimes.isEmpty() && !minusInfos.isEmpty()) {
            for (int i = 0; i < availPowers.size(); i++) availPowers.set(i, opts.getPowers().get(i));
            intGood = true;
        }
        integralHom = approxInfo;
        rationalHom = approxInfo.rationalHomFrom();
    }
    
    protected ArrayList<Integer> onlyAvailable() {
        ArrayList<Integer> availables = new ArrayList<Integer>();
        for (int i = 0; i < availPrimes.size(); i++) {
            if (availPowers.get(i) == 0) availables.add(availPrimes.get(i));
        }
        return availables;
    }
    
    protected boolean positiveInfo(String info) {
        return info.charAt(1)!='-';
    }
    
    protected long[][] getStartInfo(ArrayList<String> relInfo) {
        long[][] theInfo = new long[relInfo.size()][4];
        for (int i = 0; i < relInfo.size(); i++) {
            long[] prime = primeAndPower(relInfo.get(i));
            int[] start = startAndEnd(relInfo.get(i));
            theInfo[i][0] = prime[0];
            theInfo[i][1] = prime[1];
            theInfo[i][2] = start[0];
            theInfo[i][3] = start[1];
        }
        return theInfo;
    }
    
    private int[] startAndEnd(String info) {
        int[] sae = new int[2];
        int sp = info.lastIndexOf('.');
        int mp = info.lastIndexOf('-');
        sae[0] = Integer.parseInt(info.substring(sp+1, mp));
        sae[1] = Integer.parseInt(info.substring(mp+1));
        return sae;
    }
    
    private long[] primeAndPower(String info) {
        long[] pap = new long[2];
        ArrayList<Integer> primesOfInterest = opts.getPrimes();
        boolean found = false;
        int i = 0;
        int end = info.indexOf(".");
        long ring = Long.parseLong(info.substring(1, end));
        if (ring < 2) {
            pap[0] = ring;
            pap[1] = 1;
            return pap;
        }
        int prime = 2;
        while (!found) {
            prime = primesOfInterest.get(i);
            if (ring%prime == 0) found = true;
            else i++;
        }
        int power = 0;
        while (ring%prime == 0) {
            ring = ring/prime;
            power++;
        }
        pap[0] = prime;
        pap[1] = power;
        return pap;
    }
    
    protected ArrayList<String> getRelevantInfo(ArrayList<String> theInfo, char check) {
        //char check = 'u';
        //if (reduced) check = 'r';
        ArrayList<String> rels = new ArrayList<String>();
        for (String checker : theInfo) {
            if (checker.charAt(0)==check && !"1.".equals(checker.substring(1,3))) rels.add(checker);
        }
        return rels;
    }
    
    /*private HomologyInfo modHomFrom(HomologyInfo intHom, int prime, int power) {
        int max = prime;
        for (int i = 1; i < power; i++) max = max * prime;
        HomologyInfo newInfo = new HomologyInfo((long) prime,power);
        for (QuantumCohomology quant : intHom.getHomologies()) {
            QuantumCohomology newQuant = new QuantumCohomology(quant.qdeg());
            for (Homology hom : quant.getHomGroups()) {
                ArrayList<BigInteger> relTorsion = relevantTorsion(hom.getTorsion(),prime,max);
                if (hom.getBetti() > 0 || !relTorsion.isEmpty()) {
                    Homology newHom = new Homology(hom.hdeg(), hom.getBetti());
                    Homology lowHom = newQuant.findHomology(hom.hdeg()-1,!relTorsion.isEmpty());
                    for (BigInteger tor : relTorsion) {
                        if (tor.equals(BigInteger.ZERO)) {
                            newHom.setBetti(newHom.getBetti()+1);
                            lowHom.setBetti(lowHom.getBetti()+1);
                        }
                        else {
                            newHom.addTorsion(tor);
                            lowHom.addTorsion(tor);
                        }
                    }
                    newQuant.addHomology(newHom);
                }
            }
            newInfo.addCohomology(newQuant);
        }
        return newInfo;
    }
    
    private ArrayList<BigInteger> relevantTorsion(ArrayList<BigInteger> torsion, int prime, int power) {
        BigInteger bigPrime = BigInteger.valueOf(prime);
        BigInteger max = BigInteger.valueOf(power);
        ArrayList<BigInteger> relevant = new ArrayList<BigInteger>();
        for (BigInteger tor : torsion) {
            BigInteger run = max;
            if (tor.mod(max).equals(BigInteger.ZERO)) relevant.add(BigInteger.ZERO);
            else {
                while (run.compareTo(bigPrime) > 0) {
                    run = run.divide(bigPrime);
                    if (tor.mod(run).equals(BigInteger.ZERO)) {
                        relevant.add(run);
                        run = BigInteger.ONE;
                    }
                }
            }
        }
        return relevant;
    }//
    
    protected HomologyInfo rationalHomFrom(HomologyInfo intHom) {
        HomologyInfo newInfo = new HomologyInfo(1l,1);
        for (QuantumCohomology quant : intHom.getHomologies()) {
            QuantumCohomology newQuant = new QuantumCohomology(quant.qdeg());
            for (Homology hom : quant.getHomGroups()) {
                if (hom.getBetti() > 0) {
                    Homology newHom = new Homology(hom.hdeg(), hom.getBetti());
                    newQuant.addHomology(newHom);
                }
            }
            newInfo.addCohomology(newQuant);
        }
        return newInfo;
    }// */
    
    private void setupIntegral() {
        if (!opts.isPrimary()) juggleTorsion();
        approxWarning = new JLabel("               ",SwingConstants.CENTER);
        improvWarning = new JLabel("               ",SwingConstants.CENTER);
        if (!intGood) approxWarning.setText("Approximation");
        if (!imprPrimes.isEmpty()) setImprWarning();
        showHom = integralHom;
        integralRadio = new JRadioButton("integral");
        rationalRadio = new JRadioButton("rational");
        modularRadio = new JRadioButton("modular");
        integralRadio.setSelected(true);
        groupOfButtons.add(integralRadio);
        groupOfButtons.add(rationalRadio);
        groupOfButtons.add(modularRadio);
        integralRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int u = quantumList.getSelectedIndex();
                showHom = integralHom;
                DefaultListModel<String> theModel = (DefaultListModel<String>) quantumList.getModel();
                theModel.removeAllElements();
                addToTheqList(theModel);
                if (u > showHom.getHomologies().size()) u = showHom.getHomologies().size();
                setNewHomologyPane(u);
                quantumList.setSelectedIndex(u);
                if (intGood) approxWarning.setText("               ");
                else approxWarning.setText("Approximation");
                if (!imprPrimes.isEmpty()) setImprWarning();
            }
        });
        rationalRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int u = quantumList.getSelectedIndex();
                showHom = rationalHom;
                DefaultListModel<String> theModel = (DefaultListModel<String>) quantumList.getModel();
                theModel.removeAllElements();
                addToTheqList(theModel);
                if (u > showHom.getHomologies().size()) u = showHom.getHomologies().size();
                setNewHomologyPane(u);
                quantumList.setSelectedIndex(u);
                if (ratGood) approxWarning.setText("               ");
                else approxWarning.setText("Approximation");
                improvWarning.setText("                ");
            }
        });
        modularRadio.setEnabled(!availPrimes.isEmpty());
        modularRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int u = quantumList.getSelectedIndex();
                int prime = (int) primeSpinner.getValue();
                showHom = integralHom.modHomFrom(prime, (int) powerSpinner.getValue());
                DefaultListModel<String> theModel = (DefaultListModel<String>) quantumList.getModel();
                theModel.removeAllElements();
                addToTheqList(theModel);
                if (u > showHom.getHomologies().size()) u = showHom.getHomologies().size();
                setNewHomologyPane(u);
                quantumList.setSelectedIndex(u);
                if (!imprPrimes.contains(prime)) approxWarning.setText("               ");
                else approxWarning.setText("Approximation");
                improvWarning.setText("                ");
            }
        });
        boolean spinnerActive = true;
        if (availPrimes.isEmpty()) {
            availPrimes.add(2);
            availPowers.add(1);
            spinnerActive = false;
        }
        SpinnerListModel modelPrimes = new SpinnerListModel(availPrimes);
        primeSpinner = new JSpinner(modelPrimes);
        primeSpinner.setPreferredSize(new Dimension(45,20));
        int top = availPowers.get(0);
        SpinnerNumberModel powermodel = new SpinnerNumberModel(1, 1, top, 1);
        powerSpinner = new JSpinner(powermodel);
        powerSpinner.setPreferredSize(new Dimension(45,20));
        primeSpinner.setEnabled(spinnerActive);
        powerSpinner.setEnabled(spinnerActive);
        primeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int pw = (int) powermodel.getNumber();
                SpinnerNumberModel powermodel = (SpinnerNumberModel) powerSpinner.getModel();
                int mpw = availPowers.get(availPrimes.indexOf(modelPrimes.getValue()));
                if (pw > mpw) pw = mpw;
                powermodel.setValue(pw);
                powermodel.setMaximum(mpw);
                if (modularRadio.isSelected()) modularRadio.doClick();
            }
        });
        powerSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (modularRadio.isSelected()) modularRadio.doClick();
            }
        });
        setupButtons();
        setupQuantumList();
        int a = showHom.getHomologies().size();
        homologyPanel = homologyOf(showHom.getHomologies().get(a-1).getHomGroups(),(int) showHom.getPrime(),showHom.getPower());
        homologyScrollPane = new JScrollPane(homologyPanel);
        homologyScrollPane.setPreferredSize(new Dimension(360,280));
    }
    
    private void setImprWarning() {
        String warning = "Improvable Primes :";
        for (int i = 0; i < imprPrimes.size()-1; i++) warning = warning+" "+imprPrimes.get(i)+",";
        warning = warning+" "+imprPrimes.get(imprPrimes.size()-1)+".";
        improvWarning.setText(warning);
    }
    
    private void juggleTorsion() {
        for (QuantumCohomology coh : integralHom.getHomologies()) {
            for (Homology hom : coh.getHomGroups()) {
                hom.dePrimarizeTorsion();
            }
        }
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
                    setNewHomologyPane(u);
                }
            }
        });
        quantumScrollPane = new JScrollPane(quantumList);
        quantumScrollPane.setPreferredSize(new Dimension(150,240));
    }
    
    private void setupButtons() {
        latexBut = new JButton("Save as LaTeX file");
        closeBut = new JButton("Close");
        latexBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveKhovHom();
            }
        });
        closeBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
            }
        });
    }
    
    private void saveKhovHom() {
        int qmax = showHom.getHomologies().get(showHom.getHomologies().size()-1).qdeg();
        int qmin = showHom.getHomologies().get(0).qdeg();
        JDialog fram = new JDialog(new JFrame(),"Options", true);
        fram.setBounds(getX()+150, getY()+100, 300, 200);
        fram.setResizable(false);
        fram.setLayout(new GridLayout(3,1));
        JLabel orient = new JLabel("Orientation :");
        final ButtonGroup orientations = new ButtonGroup();
        JRadioButton qslashh = new JRadioButton("q \\ h",true);
        JRadioButton hslashq = new JRadioButton("h \\ q");
        orientations.add(qslashh);
        orientations.add(hslashq);
        JPanel orButtons = new JPanel();
        orButtons.add(orient);
        orButtons.add(qslashh);
        orButtons.add(hslashq);
        JLabel qrange = new JLabel("Quantum range :");
        int qlow = qmin;
        int qhih = qmax;
        SpinnerNumberModel modelmin = new SpinnerNumberModel(qlow,qmin,qhih,2);
        JSpinner spinnermin = new JSpinner(modelmin);
        SpinnerNumberModel modelmax = new SpinnerNumberModel(qhih,qlow,qmax,2);
        JSpinner spinnermax = new JSpinner(modelmax);
        JPanel rangePan = new JPanel();
        rangePan.add(qrange);
        rangePan.add(spinnermin);
        rangePan.add(spinnermax);
        JButton cancel = new JButton("Cancel");
        JButton save = new JButton("Save");
        JPanel butPanel = new JPanel();
        butPanel.add(save);
        butPanel.add(cancel);
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.setVisible(false);
            }
        });
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                save.setEnabled(false);
                fram.setVisible(false);
            }
        });
        spinnermin.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                int k = (int) spinnermin.getValue();
                modelmax.setMinimum(k);
            }
        });
        spinnermax.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                int k = (int) spinnermax.getValue();
                modelmin.setMaximum(k);
            }
        });
        fram.add(orButtons);
        fram.add(rangePan);
        fram.add(butPanel);
        fram.setVisible(true);
        if (!save.isEnabled()) saveLaTeXFile(qslashh.isSelected(),(int) spinnermin.getValue(),(int) spinnermax.getValue(),qmin,qmax);
    }
    
    private void saveLaTeXFile(boolean qh, int qmin, int qmax, int tqn, int tqm) {
        String ringOne = "\\mathbb{Z}";
        String ringMor = "\\mathbb{Z}";
        String torOne = "\\mathbb{Z}";
        String torMor = "(\\mathbb{Z}_{#1})";
        int ring = (int) showHom.getPrime();
        if (ring > 1) {
            for (int y = 1; y < (int) showHom.getPower(); y++) ring = ring * (int) showHom.getPrime();
            ringOne = "\\mathbb{Z}_{"+ring+"}";
            ringMor = "(\\mathbb{Z}_{"+ring+"})";
        }
        if (ring == 1) {
            ringOne = "\\mathbb{Q}";
            ringMor = "\\mathbb{Q}";
        }
        int[] homex = extremalHomology(qmin,qmax);
        ArrayList<String> commands = new ArrayList<String>();
        commands.add("\\documentclass[border=1bp]{standalone}");
        commands.add("\\usepackage{amssymb,amsmath,array,diagbox}");
        commands.add("\\newcommand{\\Rone}{"+ringOne+"}");
        commands.add("\\newcommand{\\Rmor}[1]{"+ringMor+"^{#1}}");
        commands.add("\\newcommand{\\Tone}[1]{"+torOne+"_{#1}}");
        commands.add("\\newcommand{\\Tmor}[2]{"+torMor+"^{#2}}");
        commands.add("\\newcommand{\\Zero}{$0$}");
        commands.add("\\begin{document}");
        commands.add("\\setlength\\extrarowheight{2pt}");
        String next = "\\begin{tabular}{|c||";
        String after;
        int starter;
        int ender;
        int shifter;
        if (qh) {
            after = "\\backslashbox{\\!$q$\\!}{\\!$h$\\!} ";
            for (int i = homex[0]; i <= homex[1]; i++) {
                next = next + "c|";
                after = after + "& $"+i+"$ ";
            }
            next = next +"}";
            after = after + "\\\\";
            starter = qmax;
            ender = qmin;
            shifter = 2;
        }
        else {
            after = "\\backslashbox{\\!$h$\\!}{\\!$q$\\!} ";
            for (int i =qmin; i <= qmax; i = i+2) {
                next = next + "c|";
                after = after + "& $"+i+"$ ";
            }
            next = next +"}";
            after = after + "\\\\";
            starter = homex[1];
            ender = homex[0];
            shifter = 1;
        }
        commands.add(next);
        commands.add("\\hline");
        commands.add(after);
        commands.add("\\hline");
        commands.add("\\hline");
        String[][] theMatrix = getLaTeXTable(qh,qmin,qmax,homex[0],homex[1]);
        for (int r = starter; r >= ender; r = r - shifter) {
            String theLine = "$"+r+"$ ";
            for (int j = 0; j < theMatrix[0].length; j++) theLine = theLine+" & "+theMatrix[(r-ender)/shifter][j];
            theLine = theLine +" \\\\";
            commands.add(theLine);
            commands.add("\\hline");
        }
        commands.add("\\end{tabular}");
        commands.add("\\end{document}");
        String theTitle = theTitleForHom();
        if (ring == 0 && !intGood) theTitle = "A"+theTitle;
        if (ring == 1 && !ratGood) theTitle = "AQ"+theTitle;
        if (ring == 1 && ratGood) theTitle = "Q"+theTitle;
        if (ring >= 2) {
            theTitle = "M"+ring+theTitle;
            if (imprPrimes.contains((int) showHom.getPrime())) theTitle = "A"+theTitle;
        }
        if (qmax - qmin < tqm - tqn) theTitle = theTitle + "_"+qmin+"_"+qmax;
        if (!qh) theTitle = theTitle +"^";
            if (reduced) theTitle = theTitle +"_red";
        saveCommands(theTitle,commands);
    }
    
    protected String theTitleForHom() {
        return "Kh_"+link.name;
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
    
    private String[][] getLaTeXTable(boolean qh, int qmin, int qmax, int hmin, int hmax) {
        String[][] theTable;
        if (!qh) theTable = new String[hmax-hmin+1][((qmax-qmin)/2)+1];
        else theTable = new String[((qmax-qmin)/2)+1][hmax-hmin+1];
        for (int i = 0; i < theTable.length; i++) {
            for (int j = 0; j < theTable[0].length; j++) theTable[i][j] = " ";
        }
        for (QuantumCohomology quant : showHom.getHomologies()) {
            if (quant.qdeg() >= qmin && quant.qdeg() <= qmax) {
                for (Homology hom : quant.getHomGroups()) {
                    int[] pos = new int[2];
                    int x = 0;
                    int y = 1;
                    if (!qh) {
                        x = 1;
                        y = 0;
                    }
                    pos[x] = (quant.qdeg()-qmin)/2;
                    pos[y] = hom.hdeg() - hmin;
                    theTable[pos[0]][pos[1]] = theLaTeXString(hom);
                }
            }
        }
        return theTable;
    }
    
    private String theLaTeXString(Homology hom) {
        if (hom.getBetti() == 0 && hom.getTorsion().isEmpty()) return "\\Zero";
        String dude = "$ ";
        String plus = "";
        if (hom.getBetti() > 0) {
            plus = "\\oplus ";
            if (hom.getBetti() == 1) dude = dude + "\\Rone ";
            else dude = dude + "\\Rmor{"+hom.getBetti()+"} ";
        }
        ArrayList<BigInteger> tors = new ArrayList<BigInteger>();
        ArrayList<Integer> pwrs = new ArrayList<Integer>();
        for (BigInteger t : hom.getTorsion()) {
            if (tors.contains(t)) {
                int p = pwrs.get(tors.indexOf(t))+1;
                pwrs.set(tors.indexOf(t), p);
            }
            else {
                tors.add(t);
                pwrs.add(1);
            }
        }
        for (int j = 0; j < tors.size(); j++) {
            BigInteger t = tors.get(j);
            int p = pwrs.get(j);
            if (p > 1) dude = dude + plus + "\\Tmor{"+t+"}{"+p+"} ";
            else dude = dude + plus + "\\Tone{"+t+"} ";
            plus = "\\oplus ";
        }
        dude = dude+"$";
        return dude;
    }
    
    private int[] extremalHomology(int qmin, int qmax) {
        int[] exhom = new int[2];
        boolean beenset = false;
        for (QuantumCohomology quant : showHom.getHomologies()) {
            if (quant.qdeg() >= qmin && quant.qdeg() <= qmax && !quant.getHomGroups().isEmpty()) {
                if (!beenset) {
                    exhom[0] = quant.getHomGroups().get(0).hdeg();
                    exhom[1] = quant.getHomGroups().get(quant.getHomGroups().size()-1).hdeg();
                    beenset = true;
                }
                else {
                    if (exhom[0] > quant.getHomGroups().get(0).hdeg()) exhom[0] = quant.getHomGroups().get(0).hdeg();
                    if (exhom[1] < quant.getHomGroups().get(quant.getHomGroups().size()-1).hdeg())
                      exhom[1] = quant.getHomGroups().get(quant.getHomGroups().size()-1).hdeg();  
                }
            }
        }
        return exhom;
    }
    
    private void addToTheqList(DefaultListModel<String> theModel) {
        int a = showHom.getHomologies().size();
        for (int i = a-1; i >=0; i--) {
            String label = "q = "+showHom.getHomologies().get(i).qdeg();
            theModel.addElement(label);
        }
    }
    
    private void setNewHomologyPane(int u) {
        int v = showHom.getHomologies().size()-1-u;
        homologyScrollPane.getViewport().removeAll();
        homologyPanel = homologyOf(showHom.getHomologies().get(v).getHomGroups(),(int) showHom.getPrime(),showHom.getPower());
        homologyScrollPane.getViewport().add(homologyPanel);
    }
    
    private JPanel homologyOf(ArrayList<Homology> homGroups, int prime, int power) {
        int extra = 0;
        int lines = homGroups.size();
        if (lines < 5) extra++;
        if (lines < 3) extra++;
        JPanel homPanel = new JPanel(new BorderLayout());
        JPanel homs = new JPanel(new GridLayout(2*extra+homGroups.size(),1));
        JPanel groups = new JPanel(new GridLayout(2*extra+homGroups.size(),1));
        for (int i = 0; i < extra; i++) addExtra(homs,groups);
        for (int i = homGroups.size()-1; i >= 0; i--) {
            Homology hom = homGroups.get(i);
            JLabel homlabel = new JLabel("H^"+hom.hdeg()+" = ",SwingConstants.RIGHT);
            JLabel grolabel = new JLabel(theLabel(hom,prime,power),SwingConstants.LEFT);
            homlabel.setPreferredSize(new Dimension(60,40));
            homs.add(homlabel);
            groups.add(grolabel);
        }
        homPanel.add(homs, BorderLayout.WEST);
        homPanel.add(groups, BorderLayout.CENTER);
        return homPanel;
    }
    
    private void addExtra(JPanel homs, JPanel groups) {
        JLabel homLabel = new JLabel("  ");
        JLabel groLabel = new JLabel("  ");
        homLabel.setPreferredSize(new Dimension(60,40));
        groLabel.setPreferredSize(new Dimension(90,40));
        homs.add(homLabel);
        groups.add(groLabel);
    }
    
    private String theLabel(Homology hom, int prime, int power) {
        if (hom.getBetti() == 0 && hom.getTorsion().isEmpty()) return "0";
        int bet = hom.getBetti();
        String label = "";
        if (prime > 1) {
            BigInteger[] tors = new BigInteger[power-1];
            int[] pwrs = new int[power-1];
            BigInteger ring = BigInteger.valueOf(prime);
            for (int i = 1; i < power; i++) {
                tors[i-1] = ring;
                ring = ring.multiply(BigInteger.valueOf(prime));
            }
            for (BigInteger t : hom.getTorsion()) {
                int j = 0;
                boolean found = false;
                while (!found && j < power-1) {
                    if (tors[j].equals(t)) found = true;
                    else j++;
                }
                if (found) pwrs[j]++;
                else bet++;
            }
            String pls = "";
            if (bet > 0) {
                pls = " + ";
                if (bet == 1) label = "Z/"+ring;
                else label = "(Z/"+ring+")^"+bet;
            }
            for (int i = 0; i < pwrs.length; i++) {
                if (pwrs[i] > 0) {
                    if (pwrs[i] == 1) label = label+pls+"Z/"+tors[i];
                    else label = label+pls+"(Z/"+tors[i]+")^"+pwrs[i];
                    pls = " + ";
                }
            }
        }
        else {
            ArrayList<BigInteger> tors = new ArrayList<BigInteger>();
            ArrayList<Integer> pwrs = new ArrayList<Integer>();
            for (BigInteger t : hom.getTorsion()) {
                if (tors.contains(t)) {
                    int p = pwrs.get(tors.indexOf(t))+1;
                    pwrs.set(tors.indexOf(t), p);
                }
                else {
                    tors.add(t);
                    pwrs.add(1);
                }
            }
            String pls = "";
            if (bet > 0) {
                label = "Z";
                if (prime == 1) label = "Q";
                if (bet > 1) label = label +"^"+bet;
                pls = " + ";
            }
            for (int j = 0; j < tors.size(); j++) {
                BigInteger t = tors.get(j);
                int p = pwrs.get(j);
                if (p > 1) label = label + pls + "(Z/"+t+")^"+p;
                else label = label + pls + "Z/"+t;
                pls = " + ";
            }
        }
        return label;
    }

    public void setupFrame() {
        if (img != null) setIconImage(img);
        this.setLayout(new BorderLayout());
        JPanel paneList = new JPanel(new BorderLayout());
        JLabel labeList = new JLabel("Quantum degrees", SwingConstants.CENTER);
        labeList.setPreferredSize(new Dimension(120,30));
        JPanel buttonPane = new JPanel();
        buttonPane.add(latexBut);
        buttonPane.add(closeBut);
        JPanel extraPanel = new JPanel();
        extraPanel.setLayout(new GridLayout(1,2));
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(3,1));
        radioPanel.add(integralRadio);
        radioPanel.add(rationalRadio);
        radioPanel.add(modularRadio);
        extraPanel.add(radioPanel);
        JPanel primePowerPanel = new JPanel();
        primePowerPanel.setLayout(new GridLayout(2,1));
        JPanel primepanel = new JPanel();
        primepanel.setLayout(new GridLayout(1,2));
        JPanel pritePanel = new JPanel();
        JLabel primelabel = new JLabel("Prime : ");
        pritePanel.add(primelabel);
        JPanel spinpPanel = new JPanel();
        spinpPanel.add(primeSpinner);
        primepanel.add(pritePanel);
        primepanel.add(spinpPanel);
        primePowerPanel.add(primepanel);
        JPanel powerpanel = new JPanel();
        powerpanel.setLayout(new GridLayout(1,2));
        JPanel powtepanel = new JPanel();
        JLabel powerlabel = new JLabel("Power : ");
        powtepanel.add(powerlabel);
        JPanel spinwPanel = new JPanel();
        spinwPanel.add(powerSpinner);
        powerpanel.add(powtepanel);
        powerpanel.add(spinwPanel);
        primePowerPanel.add(powerpanel);
        primePowerPanel.setPreferredSize(new Dimension(90,50));
        extraPanel.add(primePowerPanel);
        paneList.add(labeList, BorderLayout.NORTH);
        paneList.add(quantumScrollPane, BorderLayout.CENTER);
        paneList.add(extraPanel, BorderLayout.SOUTH);
        JPanel middlePanel = new JPanel();
        middlePanel.add(homologyScrollPane);
        JPanel infoPanel = new JPanel(new GridLayout(2,1));
        infoPanel.add(approxWarning);
        infoPanel.add(improvWarning);
        middlePanel.add(infoPanel);
        add(paneList, BorderLayout.WEST);
        add(middlePanel, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.SOUTH);
    }

    public void setUpStuff(String title) {
        integralHom = getIntegralHomology();//link.integralHomology(reduced);
        intGood = (integralHom!=null);
        if (intGood) rationalHom = integralHom.rationalHomFrom();
        else rationalHom = getRationalHomology();//link.rationalHomology(reduced);
        ratGood = (rationalHom != null);
        for (int p : opts.getPrimes()) availPrimes.add(p);
        if (intGood) {
            for (int p : opts.getPowers()) availPowers.add(p);
        }
        else getApproximation();
        setupIntegral();
        this.setTitle(title);
        this.setSize(600,400);
        this.setLocationRelativeTo(this);
        this.setResizable(false);
        this.setupFrame();
        this.setVisible(true);
    }
    
    protected HomologyInfo getIntegralHomology() {
        ArrayList<String> theStrings = link.unredKhovHom;
        ArrayList<String> theInfo = link.khovInfo;
        if (reduced) theStrings = link.redKhovHom;
        char reduz = 'u';
        if (reduced) reduz = 'r';
        return link.integralHomology(reduz, theStrings, theInfo);
    }
    
    protected HomologyInfo getRationalHomology() {
        ArrayList<String> theStrings = link.unredKhovHom;
        ArrayList<String> theInfo = link.khovInfo;
        if (reduced) theStrings = link.redKhovHom;
        char reduz = 'u';
        if (reduced) reduz = 'r';
        return link.rationalHomology(reduz, theStrings, theInfo);
    }
}
