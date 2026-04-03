/*

Copyright (C) 2025 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import knotjob.Comparer;
import knotjob.Options;
import knotjob.homology.HomologyInfo;
import knotjob.homology.QuantumCohomology;
import knotjob.links.Link;
import knotjob.links.LinkCreator;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class CreateLinkDialog extends JDialog {
    
    private final JButton torLink = new JButton("Torus Link");
    private final JButton preLink = new JButton("Pretzel Link");
    private final JButton dtKnot  = new JButton("DT Code");
    private final JButton pdLink  = new JButton("PD Code"); 
    private final JButton gaLink  = new JButton("Gauss Code");
    private final JButton adtLink = new JButton("alphabetical DT Code");
    private final JButton braLink = new JButton("Braid Code");
    private final JButton conLink = new JButton("Concatenate Links");
    private final JButton splLink = new JButton("Split Union");
    private final JButton mirLink = new JButton("Mirror Links");
    private final JButton unLink  = new JButton("Unlink");
    private final JButton orLink  = new JButton("Change Orientation");
    private final JButton whDoub  = new JButton("Whitehead Doubles");
    private final JButton cancelB = new JButton("Cancel");
    private final Comparer comparer;
    private final JFrame frame;
    private final ArrayList<LinkData> theLinks;
    private final DefaultListModel<String> listModel;
    private final Options options;
    
    public CreateLinkDialog(JFrame frm, String title, boolean bo, Comparer comp, 
            ArrayList<LinkData> thLnks, DefaultListModel<String> lstMdl, Options opts) {
        super(frm, title, bo);
        comparer = comp;
        frame = frm;
        theLinks = thLnks;
        listModel = lstMdl;
        options = opts;
    }
    
    public void setUp() {
        setSize(400,300);
        setLocationRelativeTo(this);
        setResizable(false);
        setLayout(new GridLayout(7,2));
        torLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
                torLink.setEnabled(false);
            }
        });
        preLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
                preLink.setEnabled(false);
            }
        });
        dtKnot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
                dtKnot.setEnabled(false);
            }
        });
        pdLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
                pdLink.setEnabled(false);
            }
        });
        adtLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
                adtLink.setEnabled(false);
            }
        });
        braLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
                braLink.setEnabled(false);
            }
        });
        conLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
                conLink.setEnabled(false);
            }
        });
        splLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
                splLink.setEnabled(false);
            }
        });
        mirLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
                mirLink.setEnabled(false);
            }
        });
        cancelB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        });
        gaLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
                gaLink.setEnabled(false);
            }
        });
        unLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
                unLink.setEnabled(false);
            }
        });
        orLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
                orLink.setEnabled(false);
            }
        });
        whDoub.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
                whDoub.setEnabled(false);
            }
        });
        JPanel torPanel = new JPanel();
        JPanel prePanel = new JPanel();
        JPanel dtcPanel = new JPanel();
        JPanel pdcPanel = new JPanel();
        JPanel gauPanel = new JPanel();
        JPanel adtPanel = new JPanel();
        JPanel braPanel = new JPanel();
        JPanel conPanel = new JPanel();
        JPanel splPanel = new JPanel();
        JPanel mirPanel = new JPanel();
        JPanel unPanel  = new JPanel();
        JPanel orPanel  = new JPanel();
        JPanel whPanel  = new JPanel();
        JPanel canPanel = new JPanel();
        torPanel.add(torLink);
        prePanel.add(preLink);
        dtcPanel.add(dtKnot);
        pdcPanel.add(pdLink);
        gauPanel.add(gaLink);
        adtPanel.add(adtLink);
        braPanel.add(braLink);
        conPanel.add(conLink);
        splPanel.add(splLink);
        mirPanel.add(mirLink);
        unPanel.add(unLink);
        orPanel.add(orLink);
        whPanel.add(whDoub);
        canPanel.add(cancelB);
        add(torPanel);
        add(prePanel);
        add(braPanel);
        add(dtcPanel);
        add(pdcPanel);
        add(gauPanel);
        add(adtPanel);
        add(conPanel);
        add(splPanel);
        add(mirPanel);
        add(unPanel);
        add(orPanel);
        add(whPanel);
        add(canPanel);
        setVisible(true);
    }
    
    public ArrayList<LinkData> getLinks() {
        ArrayList<LinkData> links = new ArrayList<LinkData>();
        if (!torLink.isEnabled()) links.add(enterTorusLink());
        if (!preLink.isEnabled()) links.add(enterPretzelLink());
        if (!dtKnot.isEnabled())  links.add(LinkCreator.enterDTCode(null, null, true, frame, comparer));
        if (!pdLink.isEnabled())  links.add(LinkCreator.enterPDCode(null, null, true, frame, comparer));
        if (!gaLink.isEnabled())  links.add(LinkCreator.enterGaussCode(frame, comparer));
        if (!adtLink.isEnabled()) links.add(LinkCreator.enterADTCode(null, null, true, frame, comparer));
        if (!braLink.isEnabled()) links.add(LinkCreator.enterBraidCode(null, null, frame, true, comparer));
        if (!conLink.isEnabled()) links.add(concatenateLinks());
        if (!splLink.isEnabled()) links.add(disjointUnion());
        if (!mirLink.isEnabled()) links = mirrorLink();
        if (!unLink.isEnabled())  links.add(unLink());
        if (!orLink.isEnabled())  links.add(orLink());
        if (!whDoub.isEnabled())  links = whiteLink();
        return links;
    }
    
    private LinkData concatenateLinks() {
        if (theLinks.isEmpty()) return null;
        UnionKnot uKnot = new UnionKnot(frame, "Concatenate Links", theLinks, listModel, true);
        if (uKnot.knot == null) return null;
        String name = uKnot.name;
        return new LinkData(name,uKnot.knot.girthMinimize(),comparer);
    }
    
    private LinkData disjointUnion() {
        if (theLinks.isEmpty()) return null;
        UnionKnot uKnot = new UnionKnot(frame, "Split Union", theLinks, listModel, false);
        if (uKnot.knot == null) return null;
        String name = uKnot.name;
        return new LinkData(name,uKnot.knot.girthMinimize(),comparer);
    }
    
    private LinkData enterTorusLink() {
        JDialog fram = new JDialog(new JFrame(), "Torus Link", true);
        fram.setSize(400, 150);
        fram.setLocationRelativeTo(this);
        fram.setLayout(new BorderLayout());
        fram.setResizable(false);
        SpinnerNumberModel modelq = new SpinnerNumberModel(5, 2, 60, 1);
        JSpinner spinnerq = new JSpinner(modelq);
        JLabel labep = new JLabel("Enter a p-value :");
        SpinnerNumberModel modelp = new SpinnerNumberModel(3, 2, 10, 1);
        JSpinner spinnerp = new JSpinner(modelp);
        JLabel labeq = new JLabel("Enter a q-value :");
        JPanel panep = new JPanel();
        panep.add(labep);
        panep.add(spinnerp);
        JPanel paneq = new JPanel();
        paneq.add(labeq);
        paneq.add(spinnerq);
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        JPanel paneb = new JPanel();
        paneb.add(ok);
        paneb.add(cancel);
        fram.add(panep,BorderLayout.NORTH);
        fram.add(paneq,BorderLayout.CENTER);
        fram.add(paneb,BorderLayout.SOUTH);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ok.setEnabled(false);
                fram.dispose();
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
            }
        });
        fram.setVisible(true);
        if (ok.isEnabled()) return null;
        int p = modelp.getNumber().intValue();
        int q = modelq.getNumber().intValue();
        Link tLink = torusKnot(q,p);
        LinkData tKnot = new LinkData("T("+p+","+q+")", tLink,comparer);
        return tKnot;
    }
    
    private Link torusKnot(int p, int q) {
        int prod = p * (q-1);
        int [] crossings = new int[prod];
        for (int k = 0 ; k < prod; k++) {
            crossings[k] = 1;
        }
        int [][] paths = new int[prod][4];
        for (int i = 0; i < p; i++) {
            int u = i * (q-1);
            paths[u][0] = (2*i*(q-1)) + 2;
            paths[u][1] = ((2*i+1)*(q-1)) + 2;
            paths[u][2] = (2*(i+1)*(q-1)) + 1;
            paths[u][3] = (2*i*(q-1)) + 1;
        }
        paths[(p-1)*(q-1)][2] = 1;
        for (int j = 1; j < q-2; j++) {
            for (int i = 0; i < p ; i++) {
                int u = i * (q-1) + j;
                paths[u][0] = (2*i*(q-1)) + j + 2;
                paths[u][1] = ((2*i)+1)*(q-1) + j + 2;
                paths[u][2] = ((2*i)+2)*(q-1) + j + 1;
                paths[u][3] = ((2*i)+1)*(q-1) + j + 1;
            }
            paths[(p-1)*(q-1)+j][2] = j + 1;
        }
        for (int i = 0; i < p; i++) {
            int u = i * (q-1) + (q - 2);
            paths[u][0] = (2*i*(q-1)) + q;
            paths[u][1] = (2*(i+1)*(q-1)) + q;
            paths[u][2] = ((2*i+2)*(q-1)) + q - 1;
            if (q > 2) paths[u][3] = ((2*i+1)*(q-1)) + q - 1;
            else paths[u][3] = ((2*i+1)*(q-1));
        }
        paths[p*(q-1)-1][1] = q;
        paths[p*(q-1)-1][2] = q-1;
        Link tLink = new Link(crossings,paths);
        return tLink;
    }

    private ArrayList<LinkData> whiteLink() {
        if (theLinks.isEmpty()) return new ArrayList<LinkData>(0);
        MirrorLink fram = new MirrorLink(frame, "Whitehead Doubles", listModel);
        fram.setUpStuff();
        if (fram.getChosen() == -1) return new ArrayList<LinkData>(0);
        ArrayList<LinkData> tLinks = new ArrayList<LinkData>();
        for (int i : fram.getAllChosen()) {
            int t = fram.getTwists();
            Link[] theDoubles = theLinks.get(i).chosenLink().whiteheadDoubles(2*t);
            if (theDoubles != null) {
                t = t +  theLinks.get(i).chosenLink().writhe();
                String name = "W("+theLinks.get(i).name+", "+t+", ";
                tLinks.add(new LinkData(name+"+)", theDoubles[0], comparer));
                tLinks.add(new LinkData(name+"-)", theDoubles[1], comparer));
            }
        }
        return tLinks;
    }
    
    private ArrayList<LinkData> mirrorLink() {
        if (theLinks.isEmpty()) return new ArrayList<LinkData>(0);
        MirrorLink fram = new MirrorLink(frame, "Mirror Link", true, false, listModel);
        fram.setUpStuff();
        if (fram.getChosen() == -1) return new ArrayList<LinkData>(0);
        ArrayList<LinkData> tLinks = new ArrayList<LinkData>();
        for (int i : fram.getAllChosen()) {
            Link theMirror = theLinks.get(i).chosenLink().mirror();
            tLinks.add(mirrorData("-"+theLinks.get(i).name, theMirror, theLinks.get(i)));
        }
        return tLinks;
    }
    
    private LinkData mirrorData(String name, Link theMirror, LinkData origLink) {
        LinkData mirrorData = new LinkData(name, theMirror, comparer);
        if (origLink.sqEven != null) {
            int[] sqeven = origLink.getSqOne(true); // at the moment we don't mirror the odd sq1.
            int[] mqeven = new int[4];
            mqeven[0] = -sqeven[2];
            mqeven[1] = -sqeven[3];
            mqeven[2] = -sqeven[0];
            mqeven[3] = -sqeven[1];
            mirrorData.setSqOne(mqeven);
        }
        if (origLink.sinvariant != null) {
            int[][] sinv = origLink.sInvariants();
            for (int[] sinv1 : sinv) {
                mirrorData.setSInvariant(sinv1[0], -sinv1[1]);
            }
        }
        if (origLink.khovInfo == null) return mirrorData;
        mirrorData.khovInfo = new ArrayList<String>();
        long[][] data = origLink.getStartInfo(origLink.khovInfo, options.getPrimes());
        for (int i = 0; i < origLink.khovInfo.size(); i++) {
            String info = origLink.khovInfo.get(i);
            boolean reduced = false;
            if (info.charAt(0) == 'r') {
                reduced = true;
                if (mirrorData.redKhovHom == null) mirrorData.redKhovHom = new ArrayList<String>();
            }
            else if (mirrorData.unredKhovHom == null) mirrorData.unredKhovHom = new ArrayList<String>();
            ArrayList<String> homStrings = origLink.unredKhovHom;
            if (reduced) homStrings = origLink.redKhovHom;
            HomologyInfo homInfo = origLink.theHomology(data[i], homStrings).mirror();
            ArrayList<String> mirrorKhovs = mirrorData.unredKhovHom;
            if (reduced) mirrorKhovs = mirrorData.redKhovHom;
            for (QuantumCohomology qCoh : homInfo.getHomologies()) {
                mirrorKhovs.add(qCoh.toString());
            }
            mirrorData.khovInfo.add(info);
        }
        return mirrorData;
    }
    
    private LinkData orLink() {
        if (theLinks.isEmpty()) return null;
        OrientLink fram = new OrientLink(frame, "Change Orientation Link", true, listModel, theLinks);
        fram.setUpStuff();
        if (fram.chosen == -1) return null;
        Link theLink = theLinks.get(fram.chosen).chosenLink().componentChoice(fram.comps,fram.orient);
        return new LinkData("V"+theLinks.get(fram.chosen).name, theLink, comparer);
    }
    
    private LinkData unLink() {
        JDialog fram = new JDialog(new JFrame(), "Unlink", true);
        fram.setSize(300, 130);
        fram.setLocationRelativeTo(this);
        fram.setLayout(new BorderLayout());
        fram.setResizable(false);
        JPanel choicePanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        SpinnerNumberModel model = new SpinnerNumberModel(1,1,10,1);
        JSpinner spinner = new JSpinner(model);
        JLabel label = new JLabel("Number of components :");
        choicePanel.add(label);
        choicePanel.add(spinner);
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        buttonPanel.add(ok);
        buttonPanel.add(cancel);
        fram.add(choicePanel, BorderLayout.CENTER);
        fram.add(buttonPanel, BorderLayout.SOUTH);
        fram.add(new JPanel(), BorderLayout.NORTH);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ok.setEnabled(false);
                fram.dispose();
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fram.dispose();
            }
        });
        fram.setVisible(true);
        if (ok.isEnabled()) return null;
        int comp = model.getNumber().intValue();
        Link newLink = new Link(comp);
        String name = "U_"+comp;
        return new LinkData(name,newLink,comparer);
    }
    
    private LinkData enterPretzelLink() {
        JDialog fram = new JDialog(new JFrame(), "Pretzel Link", true);
        fram.setSize(260,250);
        fram.setLocationRelativeTo(this);
        fram.setLayout(new BorderLayout());
        fram.setResizable(false);
        SpinnerNumberModel modelp = new SpinnerNumberModel(3,2,12,1);
        JSpinner spinnerp = new JSpinner(modelp);
        JLabel PreLabel = new JLabel("No. of Pretzels");
        JPanel PrePanel = new JPanel();
        PrePanel.add(PreLabel);
        PrePanel.add(spinnerp);
        fram.add(PrePanel, BorderLayout.WEST);
        SpinnerNumberModel[] models = new SpinnerNumberModel[12];
        JSpinner[] spinners = new JSpinner[12];
        JPanel SpinPanel = new JPanel();
        for (int i = 0; i < 12; i++) {
            models[i] = new SpinnerNumberModel(2,-999,999,1);
            spinners[i] = new JSpinner(models[i]);
            spinners[i].setPreferredSize(new Dimension(50,24));
        }
        addSpinners(SpinPanel, spinners,3);
        JScrollPane SpinPane = new JScrollPane(SpinPanel);
        spinnerp.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int k = modelp.getNumber().intValue();
                addSpinners(SpinPanel,spinners,k);
            }
        });
        SpinPane.setPreferredSize(new Dimension(100,150));
        JPanel ButtonPanel = new JPanel();
        JButton OKButton = new JButton("OK");
        JButton CancelButton = new JButton("Cancel");
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
                spinnerp.setEnabled(false);
            }
        });
        CancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fram.dispose();
            }
        });
        ButtonPanel.add(OKButton);
        ButtonPanel.add(CancelButton);
        fram.add(SpinPane, BorderLayout.EAST);
        fram.add(ButtonPanel, BorderLayout.SOUTH);
        fram.setVisible(true);
        LinkData preLnk = null;
        if (!spinnerp.isEnabled()) {
            int k = modelp.getNumber().intValue();
            int[] crossings = new int[k];
            int[][] paths = new int[k][4];
            for (int u = 0; u < k; u++) crossings[u] = models[u].getNumber().intValue();
            String name = "P("+crossings[0];
            for (int u = 1; u < k; u++) name = name+","+crossings[u];
            name = name +")";
            for (int u = 0; u < k; u++) {
                paths[u][0] = 2*u + 1;
                paths[u][1] = 2*u + 2;
                paths[u][2] = 2*u;
                paths[u][3] = 2*u - 1;
            }
            paths[0][2] = 2*k;
            paths[0][3] = 2*k - 1;
            Link link = new Link(crossings, paths);
            preLnk = new LinkData(name, link, comparer);
        }
        return preLnk;
    }
    
    private void addSpinners(JPanel SpinPanel, JSpinner[] spinners, int k) {
        SpinPanel.removeAll();
        SpinPanel.setLayout(new GridLayout(k,1));
        for (int i = 0; i < k; i++) {
            JPanel panel = new JPanel();
            panel.add(spinners[i]);
            SpinPanel.add(panel);
        }
        SpinPanel.revalidate();
    }
    
}
