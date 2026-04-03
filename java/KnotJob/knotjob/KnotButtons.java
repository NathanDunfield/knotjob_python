/*

Copyright (C) 2023-24 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
import knotjob.dialogs.CalculationDialog;
import knotjob.dialogs.CalculationDialogWrap;
import knotjob.dialogs.CharDialog;
import knotjob.dialogs.KhovDialog;
import knotjob.dialogs.SquareDialog;
import knotjob.dialogs.StableDialog;
import knotjob.dialogs.UnivSlTDialog;
import knotjob.homology.evenkhov.EvenKhovCalculator;
import knotjob.homology.evenkhov.sinv.GradedCalculator;
import knotjob.homology.evenkhov.sinv.SInvariantCalculator;
import knotjob.homology.evenkhov.sinv.SpectralCalculator;
import knotjob.homology.evenkhov.sinv.SqOneCalculator;
import knotjob.homology.evenkhov.sinv.UnivKhovCalculator;
import knotjob.homology.oddkhov.OddKhovCalculator;
import knotjob.homology.oddkhov.sinv.SqOneOddCalculator;
import knotjob.homology.oddkhov.unified.UnifiedKhovCalculator;
import knotjob.homology.oddkhov.unified.sinv.CompleteSCalculator;
import knotjob.homology.oddkhov.unified.sinv.SqOneSumCalculator;
import knotjob.homology.oddkhov.unified.sinv.UnifiedSCalculator;
import knotjob.homology.slthree.SlThreeHomCalculator;
import knotjob.homology.slthree.univ.GenSlTHomCalculator;
import knotjob.homology.slthree.univ.SlTSInvariantCalculator;
import knotjob.homology.slthree.univ.UnivSlTHomCalculator;
import knotjob.invariants.SignatureCalculator;
import knotjob.links.LinkData;
import knotjob.polynomial.alexander.FoxMilnorCalculator;
import knotjob.polynomial.jones.JonesCalculator;
import knotjob.polynomial.moy.MoyCalculator;
import knotjob.polynomial.slthree.SlThreePolCalculator;
import knotjob.stabletype.StableEvenCalculator;
import knotjob.stabletype.StableOddCalculator;
import knotjob.stabletype.sinv.SqTwoCalculator;
import knotjob.stabletype.sinv.SqTwoOddCalculator;

/**
 *
 * @author Dirk
 */
public class KnotButtons {

    private final Options options;
    private final Knobster knobster;
    
    public KnotButtons(Options optns, Knobster knbstr) {
        options = optns;
        knobster = knbstr;
    }
    
    void addButtonsTo(JPanel panelButtons) {
        JButton calcSInvariant = new JButton("s-Invariants");
        calcSInvariant.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sInvAction();
            }
        });
        JButton lipSarInvariant = new JButton("Lipshitz-Sarkar Invariants");
        lipSarInvariant.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lipSarAction();
            }
        });
        JButton calcChaKhovHom = new JButton("Khovanov Cohomology");
        calcChaKhovHom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                khovhomAction();
            }
        });
        JButton calcChaOddHom = new JButton("Odd Khovanov Homology");
        calcChaOddHom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                oddhomAction();
            }
        });
        JButton gradedInv = new JButton("Graded s-Invariants");
        gradedInv.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                gradedAction();
            }
        });
        JButton bLTSS = new JButton("BLT-Spectral Sequence");
        bLTSS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                spectralAction();
            }
        });
        JButton stableType = new JButton("Stable Homotopy Type");
        stableType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                stableAction();
            }
        });
        JButton slthreehom = new JButton("sl_3-Homology");
        slthreehom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                slthomAction();
            }
        });
        JButton slthreespsq = new JButton("sl_3-Spectral Sequence");
        slthreespsq.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sltSpSqAction();
            }
        });
        JButton slthreepol = new JButton("sl_3-Polynomial");
        slthreepol.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sltpolAction();
            }
        });
        JButton signature = new JButton("Signature");
        signature.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                signAction();
            }
        });
        JButton jonesPolynomial = new JButton("Jones Polynomial");
        jonesPolynomial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jonesAction();
            }
        });
        JButton alexPolynomial = new JButton("Alexander Polynomial");
        alexPolynomial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                foxmilAction();
            }
        });
        JPanel calcSPanel = buttonPanel(calcSInvariant);
        JPanel lipSarPanel = buttonPanel(lipSarInvariant);
        //JPanel modevKhPanel = buttonPanel(calcModKhovHom);
        JPanel charevKhPanel = buttonPanel(calcChaKhovHom);
        //JPanel mododKhPanel = buttonPanel(calcModOddHom);
        JPanel charodKhPanel = buttonPanel(calcChaOddHom);
        JPanel gradedPanel = buttonPanel(gradedInv);
        JPanel bLTSSPanel = buttonPanel(bLTSS);
        JPanel signaturePanel = buttonPanel(signature);
        JPanel slthomPanel = buttonPanel(slthreehom);
        JPanel sltspsPanel = buttonPanel(slthreespsq);
        //JPanel sltpolPanel = buttonPanel(slthreepol);
        JPanel jonesPanel = buttonPanel(jonesPolynomial);
        JPanel alexPanel = buttonPanel(alexPolynomial);
        JPanel stablePanel = buttonPanel(stableType);
        panelButtons.setLayout(new GridLayout(6, 3));
        for (int i = 0; i < 3; i++) panelButtons.add(buttonPanel(null));
        panelButtons.add(charevKhPanel);
        panelButtons.add(charodKhPanel);
        panelButtons.add(slthomPanel);
        panelButtons.add(calcSPanel);
        panelButtons.add(lipSarPanel);
        //panelButtons.add(modevKhPanel);
        //panelButtons.add(mododKhPanel);
        panelButtons.add(gradedPanel);
        panelButtons.add(bLTSSPanel);
        panelButtons.add(sltspsPanel);
        panelButtons.add(stablePanel);
        //panelButtons.add(sltpolPanel);
        panelButtons.add(jonesPanel);
        panelButtons.add(alexPanel);
        panelButtons.add(signaturePanel);
        for (int i = 0; i < 3; i++) panelButtons.add(buttonPanel(null));
    }
    
    private JPanel buttonPanel(JButton theButton) {
        JPanel thePanel = new JPanel();
        thePanel.setBackground(options.getColor());
        if (theButton != null) thePanel.add(theButton);
        return thePanel;
    }
    
    protected void sInvAction() {
        if (knobster.choices != null) {
            if (knobster.choices.length > 0) {
                ArrayList<LinkData> linkList = knobster.allLinks;
                if (knobster.filtered) linkList = knobster.filteredLinks;
                calculateSInvariant(chosenLinks(linkList, knobster.choices));
            }
        }
    }
    
    protected void lipSarAction() {
        if (knobster.choices != null) {
            if (knobster.choices.length > 0) {
                ArrayList<LinkData> linkList = knobster.allLinks;
                if (knobster.filtered) linkList = knobster.filteredLinks;
                calculateLipSar(chosenLinks(linkList, knobster.choices));
            }
        }
    }
    
    protected void gradedAction() {
        if (knobster.choices != null) {
            if (knobster.choices.length > 0) {
                ArrayList<LinkData> linkList = knobster.allLinks;
                if (knobster.filtered) linkList = knobster.filteredLinks;
                calculateGraded(chosenLinks(linkList, knobster.choices));
            }
        }
    }
    
    protected void stableAction() {
        if (knobster.choices != null) {
            if (knobster.choices.length > 0) {
                ArrayList<LinkData> linkList = knobster.allLinks;
                if (knobster.filtered) linkList = knobster.filteredLinks;
                calculateStableType(chosenLinks(linkList, knobster.choices));
            }
        }
    }
    
    protected void sltSpSqAction() {
        if (knobster.choices != null) {
            if (knobster.choices.length > 0) {
                ArrayList<LinkData> linkList = knobster.allLinks;
                if (knobster.filtered) linkList = knobster.filteredLinks;
                calculateUnivSlTHom(chosenLinks(linkList, knobster.choices));
            }
        }
    }
    
    protected void spectralAction() {
        if (knobster.choices != null) {
            if (knobster.choices.length > 0) {
                ArrayList<LinkData> linkList = knobster.allLinks;
                if (knobster.filtered) linkList = knobster.filteredLinks;
                calculateSpectralSequence(chosenLinks(linkList, knobster.choices));
            }
        }
    }
    
    protected void khovhomAction() {
        if (knobster.choices != null) {
            if (knobster.choices.length > 0) {
                ArrayList<LinkData> linkList = knobster.allLinks;
                if (knobster.filtered) linkList = knobster.filteredLinks;
                calculateKhovHom(chosenLinks(linkList, knobster.choices));
            }
        }
    }
    
    protected void slthomAction() {
        if (knobster.choices != null) {
            if (knobster.choices.length > 0) {
                ArrayList<LinkData> linkList = knobster.allLinks;
                if (knobster.filtered) linkList = knobster.filteredLinks;
                calculateSlThreeHom(chosenLinks(linkList, knobster.choices));
            }
        }
    }
    
    protected void oddhomAction() {
        if (knobster.choices != null) {
            if (knobster.choices.length > 0) {
                ArrayList<LinkData> linkList = knobster.allLinks;
                if (knobster.filtered) linkList = knobster.filteredLinks;
                calculateOddKhovHom(chosenLinks(linkList, knobster.choices));
            }
        }
    }
    
    protected void signAction() {
        if (knobster.choices != null) {
            if (knobster.choices.length > 0) {
                ArrayList<LinkData> linkList = knobster.allLinks;
                if (knobster.filtered) linkList = knobster.filteredLinks;
                calculateSignature(chosenLinks(linkList, knobster.choices));
            }
        }
    }
    
    protected void jonesAction() {
        if (knobster.choices != null) {
            if (knobster.choices.length > 0) {
                ArrayList<LinkData> linkList = knobster.allLinks;
                if (knobster.filtered) linkList = knobster.filteredLinks;
                calculateJones(chosenLinks(linkList, knobster.choices));
            }
        }
    }
    
    /*protected void alexAction() {
        if (knobster.choices != null) {
            if (knobster.choices.length > 0) {
                ArrayList<LinkData> linkList = knobster.allLinks;
                if (knobster.filtered) linkList = knobster.filteredLinks;
                calculateAlexander(chosenLinks(linkList, knobster.choices));
            }
        }
    }// */
    
    protected void foxmilAction() {
        if (knobster.choices != null) {
            if (knobster.choices.length > 0) {
                ArrayList<LinkData> linkList = knobster.allLinks;
                if (knobster.filtered) linkList = knobster.filteredLinks;
                calculateFoxMilnor(chosenLinks(linkList, knobster.choices));
            }
        }
    }
    
    protected void sltpolAction() {
        if (knobster.choices != null) {
            if (knobster.choices.length > 0) {
                ArrayList<LinkData> linkList = knobster.allLinks;
                if (knobster.filtered) linkList = knobster.filteredLinks;
                calculateSlThreePol(chosenLinks(linkList, knobster.choices));
            }
        }
    }
    
    private ArrayList<LinkData> chosenLinks(ArrayList<LinkData> linkList, int[] choices) {
        ArrayList<LinkData> theList = new ArrayList<LinkData>(choices.length);
        for (int c : choices) theList.add(linkList.get(c));
        return theList;
    }
    
    protected void calculateUnivKhov(ArrayList<LinkData> linkList) {
        String title = "Universal Khovanov";
        KhovDialog diagFrame = new KhovDialog(knobster, true, title, false);
        int val = (int) diagFrame.getValue();
        if (val < 0) return;
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(knobster, title, lev);
        UnivKhovCalculator calculator = new UnivKhovCalculator(linkList, val, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    protected void calculateUnifiedKhov(ArrayList<LinkData> linkList) {
        String title = "Unified Khovanov";
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(knobster, title, lev);
        UnifiedKhovCalculator calculator = new UnifiedKhovCalculator(linkList, 1, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateKhovHom(ArrayList<LinkData> linkList) {
        KhovDialog diagFrame = new KhovDialog(knobster, false);
        long val = diagFrame.getValue();
        diagFrame.dispose();
        boolean okay = diagFrame.isOkay();
        if (diagFrame.isOkay() && val == 2) {
            KhovDialog modFrame = new KhovDialog(knobster, true);
            val = modFrame.getValue();
            modFrame.dispose();
            okay = modFrame.isOkay();
        }
        if (okay) calculateKnotHom(linkList, val);
    }
    
    private void calculateOddKhovHom(ArrayList<LinkData> linkList) {
        KhovDialog diagFrame = new KhovDialog(knobster, false);
        diagFrame.setTitle("Odd Khovanov Homology");
        long val = diagFrame.getValue();
        diagFrame.dispose();
        boolean okay = diagFrame.isOkay();
        if (diagFrame.isOkay() && val == 2) {
            KhovDialog modFrame = new KhovDialog(knobster, true);
            modFrame.setTitle("Odd Khovanov Homology");
            val = modFrame.getValue();
            modFrame.dispose();
            okay = modFrame.isOkay();
        }
        if (okay) calculateOddKnotHom(linkList, val);
    }
    
    private void calculateKnotHom(ArrayList<LinkData> linkList, long val) {
        String khovTitle = "Integral Khovanov Cohomology";
        if (val == 1) khovTitle = "Rational Khovanov Cohomology";
        if (val < 0) khovTitle = "Local Khovanov Cohomology";
        if (val > 1) khovTitle = "Khovanov Cohomology mod "+val;
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(knobster, khovTitle, lev);
        EvenKhovCalculator calculator = new EvenKhovCalculator(linkList, val, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateOddKnotHom(ArrayList<LinkData> linkList, long val) {
        String khovTitle = "Integral Odd Khovanov Homology";
        if (val == 1) khovTitle = "Rational Odd Khovanov Homology";
        if (val < 0) khovTitle = "Local Odd Khovanov Homology";
        if (val > 1) khovTitle = "Odd Khovanov Homology mod "+val;
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(knobster, khovTitle, lev);
        OddKhovCalculator calculator = new OddKhovCalculator(linkList, val, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    protected void calculateSlThreeHom(ArrayList<LinkData> linkList) {
        KhovDialog diagFrame = new KhovDialog(knobster, false);
        diagFrame.setTitle("Sl_3 Homology");
        long val = diagFrame.getValue();
        diagFrame.dispose();
        boolean okay = diagFrame.isOkay();
        if (diagFrame.isOkay() && val == 2) {
            KhovDialog modFrame = new KhovDialog(knobster, true);
            val = modFrame.getValue();
            modFrame.dispose();
            okay = modFrame.isOkay();
        }
        if (okay) {
            String title = "Integral Sl_3 Homology";
            if (val == 1) title = "Rational Sl_3 Homology";
            if (val < 0) title = "Local Sl_3 Homology";
            if (val > 1) title = "Sl_3 Homology mod "+val;
            int lev = 3;
            if (options.getGirthInfo() == 2) lev = 4;
            CalculationDialog frame = new CalculationDialog(knobster, title, lev);
            SlThreeHomCalculator calculator = new SlThreeHomCalculator(linkList, val, options,
                    new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
            calculator.start();
            frame.setUpStuff();
        }
    }
    
    protected void calculateUnivSlTHom(ArrayList<LinkData> linkList) {
        UnivSlTDialog dialog = new UnivSlTDialog(knobster, "Sl_3-Spectral Sequence", true, options);
        int choice = dialog.getInfo();
        if (choice >= 0) {
            int sstype = dialog.getSSType();
            boolean red = false;
            if (sstype == 0) {
                sstype = 1;
                red = true;
            }
            int lev = 3;
            if (options.getGirthInfo() == 2) lev = 4;
            if (sstype <= 3) {
                String title = "Univ sl_3-Homology mod "+choice;
                CalculationDialog frame = new CalculationDialog(knobster, title, lev);
                UnivSlTHomCalculator calculator = new UnivSlTHomCalculator(linkList, choice, sstype, red, 
                        options, new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
                calculator.start();
                frame.setUpStuff();
            }
            else {
                String symbol = dialog.getSymbol();
                String extra = "";
                if (sstype == 5) extra = "^2";
                String title = "sl_3-Homology X^3 - X"+extra+" - "+symbol;
                if (sstype == 5 && "1".equals(symbol)) symbol = "+1";
                CalculationDialog frame = new CalculationDialog(knobster, title, lev);
                GenSlTHomCalculator calculator = new GenSlTHomCalculator(linkList, 
                        symbol, options, new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
                calculator.start();
                frame.setUpStuff();
            }
            // */
            
            /*if (sstype == 2) {
                UnivSlTHomCalculator calculator = new UnivSlTHomCalculator(linkList, choice, sstype, red, 
                    options, new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
                calculator.start();
            }
            else {
                GenSlTHomCalculator calculator = new GenSlTHomCalculator(linkList, 
                        GenSlTHomCalculator.TYPE_STANDARD, 0, options, 
                        new CalculationDialogWrap(frame, options.getGirthInfo() > 0));

                calculator.start();
            }// */
        }
    }
    
    private void calculateSInvariant(ArrayList<LinkData> linkList) {
        CharDialog charFrame = new CharDialog(knobster, "s-Invariant", true, true, options);
        int p = charFrame.getChar(true);
        boolean slthree = charFrame.getSlt();
        boolean red = charFrame.getReduced();
        charFrame.dispose();
        if (p<0) return;
        if (slthree) calculateSlTInvariant(linkList, p, red);
        else {
            int lev = 3;
            if (options.getGirthInfo() == 2) lev = 4;
            CalculationDialog frame = new CalculationDialog(knobster, "Calculate s-Invariant mod "+p, 
                    lev);
            SInvariantCalculator calculator = new SInvariantCalculator(linkList, p, options, 
                    new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
            calculator.start();
            frame.setUpStuff();
        }
    }
    
    private void calculateSlTInvariant(ArrayList<LinkData> linkList, int p, boolean r) {
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        String s = "s";
        if (r) s = "r";
        CalculationDialog frame = new CalculationDialog(knobster, "Calculate "+s+"l_3 s-Invariant mod "+p, 
                lev);
        SlTSInvariantCalculator calculator = new SlTSInvariantCalculator(linkList, p, r, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateGraded(ArrayList<LinkData> linkList) {
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(knobster, "Calculate graded s-Invariant", lev);
        GradedCalculator calculator = new GradedCalculator(linkList, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateStableType(ArrayList<LinkData> linkList) {
        StableDialog staFrame = new StableDialog(knobster, "Stable Homotopy Types", true);
        int par = staFrame.getParity();
        staFrame.dispose();
        String title = "Calculate Stable Type";
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(knobster, title, lev);
        if (par < 0) return;
        if (par == 0) {
            StableEvenCalculator calculator = new StableEvenCalculator(linkList, options, 
                    new CalculationDialogWrap(frame, options.getGirthInfo() >0));
            calculator.start();
        }
        if (par >= 1) {
            StableOddCalculator calculator = new StableOddCalculator(linkList, options, 
                    new CalculationDialogWrap(frame, options.getGirthInfo() >0), par);
            calculator.start();
        }
        frame.setUpStuff();
    }
    
    private void calculateSpectralSequence(ArrayList<LinkData> linkList) {
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CharDialog charFrame = new CharDialog(knobster, "BLT-Spectral Sequence", true, false, options);
        int p = charFrame.getChar(true);
        if (p < 0) return;
        boolean red = charFrame.getReduced();
        charFrame.dispose();
        String title = "Calculate BLTSS mod "+p;
        if (red) title = title + " reduced";
        CalculationDialog frame = new CalculationDialog(knobster, title, lev);
        SpectralCalculator calculator = new SpectralCalculator(linkList, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0), p, red);
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateLipSar(ArrayList<LinkData> linkList) {
        SquareDialog squaFrame = new SquareDialog(knobster, "Lipshitz-Sarkar Invariants", true);
        int dig = squaFrame.getSquare();
        squaFrame.dispose();
        if (dig == 1) calculateLipSarEvenSqOne(linkList);
        if (dig == 3) calculateBLSOdd(linkList);
        if (dig == 4) calculateLipSarSumSqOne(linkList);
        if (dig == -1) calculateLipSarOddSqOne(linkList);
        if (dig == 2) calculateLipSarEvenSqTwo(linkList);
        if (dig == -2) calculateLipSarOddSqTwo(linkList, dig+2);
        if (dig == -3) calculateLipSarOddSqTwo(linkList, dig+4);
        if (dig == -4) calculateUnifiedInv(linkList);
        if (dig == -5) calculateCompleteSInv(linkList);
    }
    
    protected void calculateCompleteSInv(ArrayList<LinkData> linkList) {
        String title = "Complete s-Invariant";
        title = title+" reduced";
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(knobster, title, lev);
        CompleteSCalculator calculator = new CompleteSCalculator(linkList, options,
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0), true);
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateUnifiedInv(ArrayList<LinkData> linkList) {
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(knobster, "Calculate Unified s-Invariant", lev);
        UnifiedSCalculator calculator = new UnifiedSCalculator(linkList, options,
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateLipSarSumSqOne(ArrayList<LinkData> linkList) {
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(knobster, "Calculate Sq^1 sum s-Invariant", lev);
        SqOneSumCalculator calculator = new SqOneSumCalculator(linkList, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateLipSarOddSqOne(ArrayList<LinkData> linkList) {
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(knobster, "Calculate odd Sq^1 s-Invariant", lev);
        SqOneOddCalculator calculator = new SqOneOddCalculator(linkList, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0), 4);
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateBLSOdd(ArrayList<LinkData> linkList) {
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(knobster, "Calculate odd BLS s-Invariant", lev);
        SqOneOddCalculator calculator = new SqOneOddCalculator(linkList, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0), 32768);
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateLipSarOddSqTwo(ArrayList<LinkData> linkList, int eps) {
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(knobster, "Calculate odd Sq^2 ("+
                (char)949+"="+eps+") s-Invariant", lev);
        SqTwoOddCalculator calculator = new SqTwoOddCalculator(linkList, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0), eps);
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateLipSarEvenSqOne(ArrayList<LinkData> linkList) {
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(knobster, "Calculate even Sq^1 s-Invariant", lev);
        SqOneCalculator calculator = new SqOneCalculator(linkList, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateLipSarEvenSqTwo(ArrayList<LinkData> linkList) {
        int lev = 3;
        if (options.getGirthInfo() == 2) lev = 4;
        CalculationDialog frame = new CalculationDialog(knobster, "Calculate even Sq^2 s-Invariant", lev);
        SqTwoCalculator calculator = new SqTwoCalculator(linkList, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0), 
                options.getStableBound(), options.getCombAtt(), options.getCombine());
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateSignature(ArrayList<LinkData> linkList) {
        CalculationDialog frame = new CalculationDialog(knobster, "Calculate Signature", 1);
        SignatureCalculator calculator = new SignatureCalculator(linkList, options,
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    protected void calculateMoy(ArrayList<LinkData> linkList, int n) {
        CalculationDialog frame = new CalculationDialog(knobster, "Calculate sl_n-Moy Polynomial", 2);
        MoyCalculator calculator = new MoyCalculator(linkList, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0), n);
        calculator.start();
        frame.setUpStuff();
    }// */
    
    private void calculateFoxMilnor(ArrayList<LinkData> linkList) {
        CalculationDialog frame = new CalculationDialog(knobster, "Calculate Alexander Polynomial", 2);
        FoxMilnorCalculator calculator = new FoxMilnorCalculator(linkList, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    private void calculateJones(ArrayList<LinkData> linkList) {
        CalculationDialog frame = new CalculationDialog(knobster, "Calculate Jones Polynomial", 2);
        JonesCalculator calculator = new JonesCalculator(linkList, options, 
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
    
    protected void calculateSlThreePol(ArrayList<LinkData> linkList) {
        CalculationDialog frame = new CalculationDialog(knobster, "Calculate sl_3-Polynomial", 2);
        SlThreePolCalculator calculator = new SlThreePolCalculator(linkList, options,
                new CalculationDialogWrap(frame, options.getGirthInfo() > 0));
        calculator.start();
        frame.setUpStuff();
    }
}
