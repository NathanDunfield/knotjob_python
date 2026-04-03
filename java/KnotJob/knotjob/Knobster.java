/*

Copyright (C) 2019-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;    // to comment out for Java problems
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Taskbar;    // to comment out for Java problems
import java.awt.Toolkit;
import java.awt.desktop.AboutEvent;   // to comment out for Java problems
import java.awt.desktop.AboutHandler; // to comment out for Java problems
import java.awt.desktop.QuitEvent;    // to comment out for Java problems
import java.awt.desktop.QuitHandler;  // to comment out for Java problems
import java.awt.desktop.QuitResponse; // to comment out for Java problems
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import knotjob.diagrams.ShowDiagram;
import knotjob.diagrams.griddiagrams.ShowGridDiagram;
import knotjob.frames.GridDiagramFrame;
import knotjob.links.*;
import knotjob.dialogs.*;
import knotjob.filters.Filter;
import knotjob.frames.StringFrame;
import knotjob.frames.ViewBLTInvariant;
import knotjob.frames.ViewCohomology;
import knotjob.frames.ViewDocumentation;
import knotjob.frames.ViewOddHomology;
import knotjob.frames.ViewSInvariants;
import knotjob.frames.ViewSlTHomology;
import knotjob.frames.ViewStableType;
import knotjob.polynomial.HalfPolynomial;

/**
 *
 * @author Dirk
 */
public class Knobster extends JFrame {
    
    private final DefaultListModel<String> listModelAll;
    private final DefaultListModel<String> listModelFiltered;
    protected final ArrayList<LinkData> allLinks;
    protected final ArrayList<LinkData> filteredLinks;
    private final JList<String> list;
    protected int[] choices;
    private int choice; 
    private final JPanel panelLinks;
    private final JPanel panelKnotInfo;
    private final JPanel panelButtons;
    private final KnotButtons theButtons;
    private JLabel labelLinkNumber;
    protected boolean filtered;
    public final Options options;
    private final Comparer comparer;
    private final Image img;
    private final ArrayList<Filter> existingFilters;
    private Filter activeFilter;
    private FilterBusiness filterBusiness;
    private final Dimension panelDimSm = new Dimension(700, 24);
    private final Dimension panelDimLg = new Dimension(700, 32);
    
    public Knobster(String title, Options optns) {
        super(title);
        options = optns;
        img = options.getImage();
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        listModelAll = new DefaultListModel<String>();
        listModelFiltered = new DefaultListModel<String>();
        allLinks = new ArrayList<LinkData>();
        filteredLinks = new ArrayList<LinkData>();
        list = new JList<String>(listModelAll);
        choices = null;
        choice = -1;
        panelLinks = new JPanel();
        panelKnotInfo = new JPanel();
        panelButtons = new JPanel();
        theButtons = new KnotButtons(options, this);
        filtered = false;
        existingFilters = new ArrayList<Filter>();
        comparer = new Comparer(0);
        arrangeStuff();
    }

    public void setAbout() {
        /*if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop(); // // to comment out for Java 8 with /*
            desktop.setAboutHandler(new AboutHandler() {
                @Override
                public void handleAbout(AboutEvent e) {
                    showInfo();
                }
            });
            desktop.setQuitHandler(new QuitHandler() {
                @Override
                public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
                    //if (yesNoDialog("Quit KnotJob", Color.CYAN)) 
                        System.exit(0);
                }
            });  
        }// */ //to comment out for Java 8
    }
    
    public void setClosing() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                if (yesNoDialog("Quit KnotJob", Color.ORANGE)) 
                    System.exit(0);
            }
        });
    }
    
    public void setIcon() {
        if (img != null) setIconImage(img);
        /*if (Taskbar.isTaskbarSupported()) {             // to comment out for Java 8
            Taskbar taskbar = Taskbar.getTaskbar();     // to comment out for Java 8
            if (img != null) taskbar.setIconImage(img); // to comment out for Java 8
        }// */
    }
    
    private void arrangeStuff() {
        addMenus();
        addLinkList();
        addFilters();
        setKnotInfoEmpty();
    }

    private void addFilters() {
        filterBusiness = new FilterBusiness(existingFilters, options, 
                this, listModelAll, allLinks);
        activeFilter = filterBusiness.resetFilters();
    }
    
    private void addMenus() {
        JMenuBar menubar = new JMenuBar();
        addFiletoMenu(menubar);
        addEdittoMenu(menubar);
        addCalctoMenu(menubar);
        addFilttoMenu(menubar);
        addDiagramtoMenu(menubar);
        addHelptoMenu(menubar);
        this.setJMenuBar(menubar);
    }

    private void addFiletoMenu(JMenuBar menubar) {
        JMenu file = new JMenu("File");
        JMenuItem newlink = new JMenuItem("New Link");
        JMenuItem lodlink = new JMenuItem("Open Link(s)");
        JMenuItem lsdlink = new JMenuItem("Open selected Link(s)");
        JMenuItem savlink = new JMenuItem("Save Link(s)");
        JMenuItem implink = new JMenuItem("Import Link(s)");
        JMenuItem explink = new JMenuItem("Export Link(s)");
        JMenuItem quilink = new JMenuItem("Quit");
        if (options.getOperatingSystem() != 2) {
            quilink.setMnemonic(KeyEvent.VK_Q);
            quilink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            // replace the previous line with the line below for Java 8
            //quilink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }
        quilink.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                //if (yesNoDialog("Quit KnotJob", Color.GREEN)) 
                    System.exit(0);
            }
        });
        lodlink.setMnemonic(KeyEvent.VK_O);
        lodlink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        // replace the previous line with the line below for Java 8
        //lodlink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        lodlink.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                ArrayList<LinkData> links = loadLinks(false);
                for (LinkData ink : links) {
                    allLinks.add(ink);
                    listModelAll.addElement(ink.name);
                }
                if (!filtered) labelLinkNumber.setText("Links : "+allLinks.size());
                else labelLinkNumber.setText("Links : "+filteredLinks.size());
            }
        });// */
        //lsdlink.setMnemonic(KeyEvent.VK_E);
        //lsdlink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        // replace the previous line with the line below for Java 8
        //lsdlink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        lsdlink.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                ArrayList<LinkData> links = loadLinks(true);
                for (LinkData ink : links) {
                    allLinks.add(ink);
                    listModelAll.addElement(ink.name);
                }
                if (!filtered) labelLinkNumber.setText("Links : "+allLinks.size());
                else labelLinkNumber.setText("Links : "+filteredLinks.size());
            }
        });
        newlink.setMnemonic(KeyEvent.VK_N);
        newlink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        // replace the previous line with the line below for Java 8
        // newlink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        newlink.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                ArrayList<LinkData> inks = getNewLink();
                for (LinkData ink : inks) {
                    if (ink != null) {
                        allLinks.add(ink);
                        listModelAll.addElement(ink.name);
                    }
                }
                if (!filtered) labelLinkNumber.setText("Links : "+allLinks.size());// */
                else labelLinkNumber.setText("Links : "+filteredLinks.size());
            }
        });// */
        savlink.setMnemonic(KeyEvent.VK_S);
        savlink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        // replace the previous line with the line below for Java 8
        // savlink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        savlink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (choices != null) saveLinks();
            }
        });// */
        implink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<LinkData> links = importLinks();
                for (LinkData ink : links) {
                    allLinks.add(ink);
                    listModelAll.addElement(ink.name);
                }
                if (!filtered) labelLinkNumber.setText("Links : "+allLinks.size());
                else labelLinkNumber.setText("Links : "+filteredLinks.size());
            }
            
        });
        explink.setMnemonic(KeyEvent.VK_E);
        explink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        // replace the previous line with the line below for Java 8
        // explink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        explink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (choices != null) exportLinks();
            }
        });
        file.add(newlink);
        file.add(lodlink);
        file.add(lsdlink);
        file.add(savlink);
        file.addSeparator();
        file.add(implink);
        file.add(explink);
        file.addSeparator();
        file.add(quilink);
        menubar.add(file);
    }

    private void addEdittoMenu(JMenuBar menubar) {
        JMenu edit = new JMenu("Edit");
        JMenuItem editlink = new JMenuItem("Edit Link");
        JMenuItem deletelink = new JMenuItem("Remove Link(s)");
        JMenuItem chosallink = new JMenuItem("Select All");
        JMenuItem optionlink = new JMenuItem("Options");
        editlink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choice >= 0) editLink();
            }
        });
        deletelink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choice >= 0 && yesNoDialog("Remove selected links", Color.BLUE)) {
                    setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    ArrayList<LinkData> linkList = allLinks;
                    if (filtered) linkList = filteredLinks;
                    int i = choices.length-1;
                    while (i >= 0) {
                        LinkData toRemove = linkList.get(choices[i]);
                        listModelAll.removeElementAt(allLinks.indexOf(toRemove));
                        allLinks.remove(toRemove);
                        if (filteredLinks.contains(toRemove)) {
                            listModelFiltered.removeElementAt(filteredLinks.indexOf(toRemove));
                            filteredLinks.remove(toRemove);
                        }
                        i--;
                    }
                    if (filtered) list.setModel(listModelFiltered);
                    else list.setModel(listModelAll);
                    setKnotInfoEmpty();
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        chosallink.setMnemonic(KeyEvent.VK_A);
        chosallink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        // replace the previous line with the line below for Java 8
        // chosallink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        chosallink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                int n = allLinks.size();
                if (filtered) n = filteredLinks.size();
                int[] all = new int[n];
                for (int i = 0; i < all.length; i++) all[i]=i;
                list.setSelectedIndices(all);
                choices = all;
                if (choices.length > 0) choice = 0;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        optionlink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setOptions(0);
            }
        });
        edit.add(editlink);
        edit.add(deletelink);
        edit.add(chosallink);
        edit.addSeparator();
        edit.add(optionlink);
        menubar.add(edit);
    }

    private void addCalctoMenu(JMenuBar menubar) {
        JMenu calculations = new JMenu("Calculations");
        JMenuItem sinvs = new JMenuItem("s-Invariants");
        JMenuItem slipsar = new JMenuItem("Lipshitz-Sarkar Invariants");
        JMenuItem sgraded = new JMenuItem("Graded s-Invariants");
        JMenuItem specseq = new JMenuItem("BLT-Spectral Sequence");
        JMenuItem sltseq = new JMenuItem("sl_3-Spectral Sequence");
        JMenuItem sign = new JMenuItem("Signature");
        JMenuItem khovhom = new JMenuItem("Khovanov Cohomology");
        JMenuItem oddhom = new JMenuItem("Odd Khovanov Homology");
        JMenuItem slthom = new JMenuItem("sl_3 Homology");
        JMenuItem jones = new JMenuItem("Jones Polynomial");
        JMenuItem alex = new JMenuItem("Alexander Polynomial");
        JMenuItem stable = new JMenuItem("Stable Homotopy Type");
        JMenuItem special = new JMenuItem("Special");
        sinvs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                theButtons.sInvAction();
            }
        });
        slipsar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                theButtons.lipSarAction();
            }
        });
        sgraded.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                theButtons.gradedAction();
            }
        });
        specseq.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                theButtons.spectralAction();
            }
        });
        sltseq.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                theButtons.sltSpSqAction();
            }
        });
        sign.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                theButtons.signAction();
            }
        });
        khovhom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                theButtons.khovhomAction();
            }
        });
        oddhom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                theButtons.oddhomAction();
            }
        });
        slthom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                theButtons.slthomAction();
            }
        });
        jones.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                theButtons.jonesAction();
            }
        });
        alex.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                theButtons.foxmilAction();
            }
        });
        stable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                theButtons.stableAction();
            }
        });
        special.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                specialAction();
            }
        });
        calculations.add(sinvs);
        calculations.add(slipsar);
        calculations.add(sgraded);
        calculations.add(specseq);
        calculations.add(sltseq);
        calculations.add(sign);
        calculations.addSeparator();
        calculations.add(khovhom);
        calculations.add(oddhom);
        calculations.add(slthom);
        calculations.addSeparator();
        calculations.add(jones);
        calculations.add(alex);
        calculations.addSeparator();
        calculations.add(stable);
        //calculations.add(special);
        menubar.add(calculations);
    }
    
    private void addFilttoMenu(JMenuBar menubar) {
        JMenu sorter = new JMenu("Filters");
        JMenuItem sortlinks = new JMenuItem("Sort Links");
        JMenuItem creafilter = new JMenuItem("Create Filter");
        JMenuItem selfilter = new JMenuItem("Select Filter");
        JMenuItem recfilter = new JMenuItem("Recalculate Filter");
        JMenuItem editfilter = new JMenuItem("Edit Filter Name");
        JMenuItem remfilter = new JMenuItem("Remove Filter");
        selfilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Filter fil = filterBusiness.selectFilters();
                if (fil != null) setActiveFilter(fil);
            }
        });
        creafilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterBusiness.createFilter();
            }
        });
        recfilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFilter();
            }
        });
        editfilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterBusiness.editFilter();
            }
        });
        remfilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterBusiness.removeFilter();
            }
        });
        sortlinks.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sortLinks();
            }
        });
        recfilter.setMnemonic(KeyEvent.VK_R);
        recfilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        // replace the previous line with the line below for Java 8
        // recfilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        selfilter.setMnemonic(KeyEvent.VK_F);
        selfilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        // replace the previous line with the line below for Java 8
        // selfilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        sorter.add(selfilter);
        sorter.add(creafilter);
        sorter.add(recfilter);
        sorter.add(editfilter);
        sorter.add(remfilter);
        sorter.addSeparator();
        sorter.add(sortlinks);
        menubar.add(sorter);
    }
    
    private void addDiagramtoMenu(JMenuBar menubar) {
        JMenu diagram = new JMenu("Diagram");
        JMenuItem girthdiag = new JMenuItem("Minimize Girth");
        JMenuItem setdiag = new JMenuItem("Choose Girth-minimized Diagram");
        JMenuItem reiddiag = new JMenuItem("Perform Reidemeister Moves");
        JMenuItem crosdiag = new JMenuItem("Choose Crossing-minimized Diagram");
        JMenuItem combdiag = new JMenuItem("Combine Crossings");
        JMenuItem micodiag = new JMenuItem("Minimize combined Crossings");
        JMenuItem selediag = new JMenuItem("Choose minimal combined Diagram");
        JMenuItem optDiag = new JMenuItem("Girth Options");
        setdiag.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choices != null) {
                    ArrayList<LinkData> theLinks = allLinks;
                    if (filtered) theLinks = filteredLinks;
                    for (int i : choices) {
                        LinkData lData = theLinks.get(i);
                        lData.choseGirthMinimized();
                    }
                    if (choices.length > 0) setLinkInfo(theLinks.get(choice));
                }
            }
        });
        girthdiag.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choices != null) {
                    ArrayList<LinkData> linkList = allLinks;
                    if (filtered) linkList = filteredLinks;
                    linkList = chosenLinks(linkList, choices);
                    minimizeGirth(linkList);
                    if (choices.length > 0) setLinkInfo(linkList.get(0));
                }
            }
        });
        girthdiag.setMnemonic(KeyEvent.VK_G);
        girthdiag.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        // replace the previous line with the line below for Java 8
        // girthdiag.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        combdiag.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choices != null) {
                    ArrayList<LinkData> linkList = allLinks;
                    if (filtered) linkList = filteredLinks;
                    linkList = chosenLinks(linkList, choices);
                    combineCrossings(linkList);
                    if (choices.length > 0) setLinkInfo(linkList.get(0));
                }
            }
        });
        reiddiag.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choices != null) {
                    ArrayList<LinkData> linkList = allLinks;
                    if (filtered) linkList = filteredLinks;
                    linkList = chosenLinks(linkList, choices);
                    minimizeDiagramCrossings(linkList, false);
                    if (choices.length > 0) setLinkInfo(linkList.get(0));
                }
            }
        });
        crosdiag.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choices != null) {
                    ArrayList<LinkData> theLinks = allLinks;
                    if (filtered) theLinks = filteredLinks;
                    for (int i : choices) {
                        LinkData lData = theLinks.get(i);
                        lData.choseCrossingMinimized();
                    }
                    if (choices.length > 0) setLinkInfo(theLinks.get(choice));
                }
            }
        });
        micodiag.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choices != null) {
                    ArrayList<LinkData> linkList = allLinks;
                    if (filtered) linkList = filteredLinks;
                    linkList = chosenLinks(linkList, choices);
                    minimizeDiagramCrossings(linkList, true);
                    if (choices.length > 0) setLinkInfo(linkList.get(0));
                }
            }
        });
        selediag.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (choices != null) {
                    ArrayList<LinkData> theLinks = allLinks;
                    if (filtered) theLinks = filteredLinks;
                    for (int i : choices) {
                        LinkData lData = theLinks.get(i);
                        lData.choseCombinedMinimized();
                    }
                    if (choices.length > 0) setLinkInfo(theLinks.get(choice));
                }
            }
        });
        optDiag.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setOptions(1);
            }
        });
        diagram.add(girthdiag);
        diagram.add(setdiag);
        diagram.addSeparator();
        diagram.add(reiddiag);
        diagram.add(crosdiag);
        diagram.addSeparator();
        diagram.add(combdiag);
        diagram.add(micodiag);
        diagram.add(selediag);
        diagram.addSeparator();
        diagram.add(optDiag);
        menubar.add(diagram);
    }
    
    private void addHelptoMenu(JMenuBar menubar) {
        JMenu help = new JMenu("Help");
        JMenuItem helpItem = new JMenuItem("KnotJob Documentation");
        JMenuItem infoItem = new JMenuItem("About KnotJob");
        helpItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                helpInfo();
            }
        });
        infoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showInfo();
            }
        });
        help.add(helpItem);
        help.addSeparator();
        help.add(infoItem);
        menubar.add(help);
    }
    
    private void helpInfo() {
        ViewDocumentation viewer = new ViewDocumentation(this, "KnotJob Documentation", options);
        viewer.setUpStuff();
    }
    
    protected void showInfo() {
        InfoDialog dial = new InfoDialog(null, "About", true, options);
        dial.showInfo();
    }
    
    protected boolean yesNoDialog(String title, Color col) {
        YesNoDialog dial = new YesNoDialog(this, title, true, options, col);
        return dial.showDialog();
    }
    
    private void addLinkList() {
        Container box = Box.createVerticalBox();
        JScrollPane scroller = new JScrollPane(list);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        box.setPreferredSize(new Dimension(200,470));
        box.add(scroller);
        panelLinks.setLayout(new BorderLayout());
        panelLinks.add(box, BorderLayout.CENTER);
        labelLinkNumber = new JLabel("Links : 0");
        JCheckBox filteredOk = new JCheckBox("Filter");
        JPanel filterPanel = new JPanel();
        filterPanel.add(labelLinkNumber);
        filterPanel.add(filteredOk);
        filteredOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JCheckBox theBox = (JCheckBox) ae.getSource();
                if (theBox.isSelected()) {
                    filtered = true;
                    list.setModel(listModelFiltered);
                    labelLinkNumber.setText("Links : "+filteredLinks.size());
                }
                else {
                    filtered = false;
                    list.setModel(listModelAll);
                    labelLinkNumber.setText("Links : "+allLinks.size());
                }
                setKnotInfoEmpty();
            }
        });
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent le) {
                @SuppressWarnings("unchecked")
                JList<String> list = (JList<String>) le.getSource();
                choices = list.getSelectedIndices();
                int u = list.getSelectedIndex();
                if ( u >= 0 & u != choice) {
                    ArrayList<LinkData> linkList = allLinks;
                    if (filtered) linkList = filteredLinks;
                    setLinkInfo(linkList.get(u));
                    choice = u;
                }
            }
        });
        panelLinks.add(filterPanel, BorderLayout.SOUTH);
        setUpPanelButtons();
        setLayout(new BorderLayout());
        add(panelLinks, BorderLayout.WEST);
        add(panelKnotInfo, BorderLayout.CENTER);
        add(panelButtons, BorderLayout.SOUTH);
    }

    private void setUpPanelButtons() {
        panelButtons.setPreferredSize(new Dimension(200, 200));
        panelButtons.setBackground(options.getColor());
        theButtons.addButtonsTo(panelButtons);
    }
    
    private void specialAction() {
        if (choices != null) {
            if (choices.length > 0) {
                ArrayList<LinkData> linkList = allLinks;
                if (filtered) linkList = filteredLinks;
                
                int count = 0;
                for (LinkData data : linkList) {
                    //int rs = Math.abs(data.sInvariant(3));
                    //int rt = Math.abs(data.sInvariant(2));
                    int sw = Math.abs(data.sSlTInvariant(2, false));
                    //int tt = Math.abs(data.sSlTInvariant(7, false));
                    //int st = Math.abs(data.sSlTInvariant(5, false));
                    if (sw == 0) {
                         System.out.println(data.name);
                         count++;
                    }
                }
                System.out.println("Count "+count);// */
                
                //for (LinkData data : linkList) System.out.println(data.relBltPages("1", 6));
                
                //for (LinkData data : linkList) {
                    /*int[] pointsPlus = data.bltInfo("+1", 7);
                    int[] pointsMinus = data.bltInfo("-1", 7);
                    if (pointsPlus.length != pointsPlus.length) {
                        System.out.println(data.name);
                        continue;
                    }
                    for (int i = 0; i < pointsPlus.length/2; i++) {
                        if (pointsPlus[2*i+1] - pointsPlus[2*i] != pointsMinus[2*i+1] - pointsMinus[2*i]) {
                            System.out.println(data.name);
                        }
                        else {
                            for (int j = 0; j <= pointsPlus[2*i+1]-pointsPlus[2*i]; j++) {
                                if (!data.sltTypeXSq.get(pointsPlus[2*i]+j).equals(data.sltTypeXSq.get(pointsMinus[2*i]+j))) System.out.println(data.name);
                            }
                        }
                    }// */
                    /*Integer dude = data.nonStandardS("+1", 7);
                    if (dude % 4 != 0) System.out.println(data.name+" "+dude);
                    String quark = data.speSeqHomZeroPolynomial("+1", 7);
                    if (quark.indexOf("+") == quark.lastIndexOf("+")) System.out.println(data.name+" "+quark);
                }// */
                
                /*  // for finding strange sl_3 spectral sequences
                for (LinkData data : linkList) {
                    ArrayList<String> chars = data.bltCharacteristics(6);
                    int[] points = data.bltInfo("0", 3);
                    ArrayList<String> zeroData = new ArrayList<String>();
                    if (points != null) {
                        for (int u = 0; u < (points.length-2)/2; u++) {
                            for (int j = points[2*u]; j <= points[2*u+1]; j++) 
                                zeroData.add(data.sltTypeTwo.get(j));
                            for (int j = points[2*u]; j <= points[2*u+1]; j++) 
                                zeroData.add(data.sltTypeTwo.get(j));
                        }
                        for (int j = points[points.length-2]; j <= points[points.length-1]; j++) 
                            zeroData.add(data.sltTypeTwo.get(j));
                    }
                    ArrayList<ArrayList<String>> allData = new ArrayList<ArrayList<String>>();
                    for (String chr : chars) {
                        points = data.bltInfo(chr, 6);
                        ArrayList<String> chrData = new ArrayList<String>();
                        for (int u = points[0]; u <= points[points.length-1]; u++) 
                            chrData.add(data.sltTypeX.get(u));
                        allData.add(chrData);
                    }
                    if (allData.size() > 1) {
                        boolean allSame = true;
                        int size = allData.get(0).size();
                        for (int u = 1; u < allData.size(); u++) 
                            if (allData.get(u).size() != size) allSame = false;
                        if (allSame) {
                            for (int i = 0; i < size; i++) {
                                String dude = allData.get(0).get(i);
                                for (int u = 1; u < allData.size(); u++) {
                                    if (!dude.equals(allData.get(u).get(i))) allSame = false;
                                }
                            }
                        }
                        if (!allSame) System.out.println(data.name);
                        allSame = true;
                        if (size != zeroData.size()) System.out.println("*"+data.name);
                        else {
                            for (int i = 0; i < size; i++) {
                                String dude = allData.get(0).get(i);
                                if (!dude.equals(zeroData.get(i))) allSame = false;
                            } 
                        }
                        if (!allSame) System.out.println("**"+data.name);
                    }
                }// */
                
                
                /* // MOY-tests
                for (LinkData link : linkList) {
                    Link ln = link.chosenLink();
                    for (int i = 0; i < ln.relComponents(); i++) {
                        ArrayList<int[]> ors = ln.getOrientationsOfComponent(i);
                        for (int[] or : ors) System.out.print(Arrays.toString(or)+"  ");
                        System.out.println();
                    }
                    System.out.println("---XXX---");
                    ArrayList<int[]> ors = ln.getOrientationsOfCrossings();
                    for (int[] or : ors) System.out.print(Arrays.toString(or)+"  ");
                    System.out.println();
                    System.out.println(ln.pathToString());
                }
                System.out.println("Let's go");
                theButtons.calculateMoy(linkList, 4);// */
                
            }
        }
    }
    
    private ArrayList<LinkData> getNewLink() {
        ArrayList<LinkData> theLinks = allLinks;
        DefaultListModel<String> listModel = listModelAll;
        if (filtered) {
            theLinks = filteredLinks;
            listModel = listModelFiltered;
        }
        CreateLinkDialog fram = new CreateLinkDialog(this, "New Link", true, comparer, 
                theLinks, listModel, options);
        fram.setUp();
        ArrayList<LinkData> links = fram.getLinks();
        return links;
    }
    
    private void setKnotInfoEmpty() {
        panelKnotInfo.removeAll();
        JLabel label = new JLabel(" ");
        label.setPreferredSize(new Dimension(790,600));
        panelKnotInfo.add(label);
        panelKnotInfo.revalidate();
        choice = -1;
    }
    
    private void setLinkInfo(LinkData theLink) {
        JLabel label = new JLabel(theLink.name, JLabel.CENTER);
        label.setFont(new Font("Sans Serif",Font.BOLD, 16));
        panelKnotInfo.removeAll();
        JPanel topInfo = new JPanel();
        String commStr = " ";
        if (theLink.comment != null) commStr = "Comment : "+theLink.comment;
        JLabel comment = new JLabel(commStr, JLabel.CENTER);
        topInfo.setLayout(new GridLayout(2,1));
        topInfo.add(label);
        topInfo.add(comment);
        topInfo.setPreferredSize(new Dimension(500,50));
        JPanel crossingInfo = new JPanel();
        setUpCrossingInfo(crossingInfo,theLink);
        JPanel diagInfo = new JPanel();
        diagInfo.setLayout(new GridLayout(1,5));
        JLabel diagLabel = new JLabel("Diagrams", JLabel.CENTER);
        JPanel diagChoice = new JPanel();
        int n = theLink.links.size();
        diagChoice.setLayout(new GridLayout(n,1));
        ButtonGroup diagramButtons = new ButtonGroup();
        ActionListener listener;
        listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JRadioButton btn = (JRadioButton) ae.getSource();
                int i = 0;
                try {
                    i = Integer.parseInt(btn.getName());
                }
                catch(NumberFormatException e) {

                }
                theLink.setChosen(i);
                setUpCrossingInfo(crossingInfo,theLink);
            }
        };
        for (int i = 0; i < n; i++) {
            int j = i+1;
            JRadioButton rButton = new JRadioButton("Diagram "+j);
            if (i == theLink.chosen()) rButton.setSelected(true);
            diagramButtons.add(rButton);
            diagChoice.add(rButton);
            rButton.setName(String.valueOf(i));
            rButton.addActionListener(listener);
        }
        JButton diagShow = new JButton("Show Diagram");
        JPanel diagShowP = new JPanel();
        diagShowP.add(diagShow, BorderLayout.CENTER);
        JButton diagDTCode = new JButton("DT-Code");
        JPanel diagDTCodeP = new JPanel();
        if (theLink.links.get(0).components() > 1) diagDTCode.setEnabled(false);
        diagShow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTheDiagram(theLink);
            }
        });
        diagDTCode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String code = theLink.chosenLink().dowkerThistle();
                showString("DT Code", code, theLink.name);
            }
        });
        diagDTCodeP.add(diagDTCode,BorderLayout.SOUTH);
        JButton diagGauss = new JButton("Gauss-Code");
        JPanel diagGaussP = new JPanel();
        diagGauss.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String code = theLink.chosenLink().gaussCode();
                showString("Gauss Code", code, theLink.name);
            }
        });
        diagGaussP.add(diagGauss, BorderLayout.CENTER);
        JScrollPane diagPane = new JScrollPane(diagChoice);
        diagPane.setPreferredSize(new Dimension(80, 80));
        diagInfo.add(diagLabel);
        diagInfo.add(diagPane);
        diagInfo.add(diagShowP);
        diagInfo.add(diagDTCodeP);
        diagInfo.add(diagGaussP);
        JPanel invPanel = new JPanel();
        invPanel.setLayout(new GridLayout(11, 1));
        JPanel signaturePanel = new JPanel();
        fillSignature(signaturePanel, theLink);
        invPanel.add(signaturePanel);
        JPanel rasmusPanel = new JPanel();
        fillRasmussen(rasmusPanel, theLink);
        invPanel.add(rasmusPanel);
        JPanel lipsarPanel = new JPanel();
        fillLipSar(lipsarPanel, theLink);
        invPanel.add(lipsarPanel);
        JPanel lipsarMorePanel = new JPanel();
        fillLipSarMore(lipsarMorePanel, theLink);
        invPanel.add(lipsarMorePanel);
        JPanel lipsartwoPanel = new JPanel();
        fillLipSarTwo(lipsartwoPanel, theLink);
        invPanel.add(lipsartwoPanel);
        JPanel khovanovPanel = new JPanel();
        fillKhovanov(khovanovPanel, theLink);
        invPanel.add(khovanovPanel);
        JPanel bLTPanel = new JPanel();
        fillBLT(bLTPanel, theLink);
        invPanel.add(bLTPanel);
        JPanel stablePanel = new JPanel();
        fillStablePanel(stablePanel, theLink);
        invPanel.add(stablePanel);
        JPanel sltPanel = new JPanel();
        fillSlTPanel(sltPanel, theLink);
        invPanel.add(sltPanel);
        JPanel usltPanel = new JPanel();
        fillUSlT(usltPanel, theLink);
        invPanel.add(usltPanel);
        JPanel polyPanel = new JPanel();
        fillPolyPanel(polyPanel, theLink);
        invPanel.add(polyPanel);
        invPanel.setPreferredSize(new Dimension(750, 350));
        panelKnotInfo.add(topInfo);
        panelKnotInfo.add(diagInfo);
        panelKnotInfo.add(crossingInfo);
        panelKnotInfo.add(invPanel);
        panelKnotInfo.revalidate();
    }

    private void fillSignature(JPanel panel, LinkData theLink) {
        panel.setLayout(new GridLayout(1, 3));
        String sign = "  Signature : ";
        if (theLink.signature != null) sign = sign + theLink.signature;
        JPanel signPanel = new JPanel();
        signPanel.add(new JLabel(sign));
        panel.add(signPanel);
        String det = "  Determinant : ";
        if (theLink.determinant != null) det = det + theLink.determinant;
        JPanel detPanel = new JPanel();
        detPanel.add(new JLabel(det));
        panel.add(detPanel);
        String grs = "  Graded s-Invariant : ";
        if (theLink.grsinv != null) grs = grs + theLink.grsinv;
        JPanel grPanel = new JPanel();
        grPanel.add(new JLabel(grs));
        panel.add(grPanel);
        panel.setPreferredSize(panelDimSm);
    }
    
    private void fillRasmussen(JPanel panel, LinkData theLink) {
        panel.setLayout(new GridLayout(1, 3));
        int[][] data = theLink.sInvariants();
        Integer ras = getValue(data, 0);
        String rasmus = "     Rasmussen invariant : ";
        if (ras != null) rasmus = rasmus+ras;
        JLabel rasmusLabel = new JLabel(rasmus);
        ras = getValue(data,2);
        String sinv = "     s-Invariant mod 2 : ";
        if (ras != null) sinv = sinv+ras;
        JLabel sinvmodtwoLabel = new JLabel(sinv);
        JButton otherInvButton = new JButton("Other s-Invariants");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(otherInvButton);
        otherInvButton.setEnabled(theLink.otherSInvariants());
        otherInvButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayOtherSInvariants(theLink);
            }
        });
        panel.add(rasmusLabel);
        panel.add(sinvmodtwoLabel);
        panel.add(buttonPanel);
        panel.setPreferredSize(panelDimLg);
    }
    
    private void fillLipSar(JPanel panel, LinkData link) {
        panel.setLayout(new GridLayout(1,3));
        JLabel LpSkLabel = new JLabel("LS-Invariants ");
        JPanel LpSkPanel = new JPanel();
        LpSkPanel.add(LpSkLabel);
        panel.add(LpSkPanel);
        String sq1Inv = "Sq^1-even : ";
        if (link.sqEven != null) sq1Inv = sq1Inv+link.sqEven;
        JLabel sq1Label = new JLabel(sq1Inv);
        JPanel sq1Panel = new JPanel();
        sq1Panel.add(sq1Label);
        panel.add(sq1Panel);
        String sq1oInv = "Sq^1-odd : ";
        if (link.sqOdd != null) sq1oInv = sq1oInv+link.sqOdd;
        JLabel sq1oLabel = new JLabel(sq1oInv);
        JPanel sq1oPanel = new JPanel();
        sq1oPanel.add(sq1oLabel);
        panel.add(sq1oPanel);
        panel.setPreferredSize(panelDimSm);
    }
    
    private void fillLipSarMore(JPanel panel, LinkData link) {
        panel.setLayout(new GridLayout(1,3));
        String unInv = "Complete LS-Inv : ";
        if (link.cmpinv != null) unInv = unInv+link.cmpinv;
        JLabel LpSkLabel = new JLabel(unInv);
        JPanel LpSkPanel = new JPanel();
        LpSkPanel.add(LpSkLabel);
        panel.add(LpSkPanel);
        String sq1Inv = "Sq^1-sum : ";
        if (link.beta != null) sq1Inv = sq1Inv+link.beta;
        JLabel sq1Label = new JLabel(sq1Inv);
        JPanel sq1Panel = new JPanel();
        sq1Panel.add(sq1Label);
        panel.add(sq1Panel);
        String bsInv = "BLS-odd : ";
        if (link.bsOdd != null) bsInv = bsInv+link.bsOdd;
        JLabel bsLabel = new JLabel(bsInv);
        JPanel bsPanel = new JPanel();
        bsPanel.add(bsLabel);
        panel.add(bsPanel);
        panel.setPreferredSize(panelDimSm);
    }
    
    private void fillLipSarTwo(JPanel panel, LinkData Link) {
        panel.setLayout(new GridLayout(1,3));
        String sq2Inv = "Sq^2-even : ";
        if (Link.sqtEven != null) sq2Inv = sq2Inv+Link.sqtEven;
        JLabel sq1Label = new JLabel(sq2Inv);
        JPanel sq1Panel = new JPanel();
        sq1Panel.add(sq1Label);
        panel.add(sq1Panel);
        String sqodInv = "Sq^2_0-odd : ";
        if (Link.sqtOdd != null) sqodInv = sqodInv+Link.sqtOdd;
        JLabel sqodLabel = new JLabel(sqodInv);
        JPanel sqodPanel = new JPanel();
        sqodPanel.add(sqodLabel);
        panel.add(sqodPanel);
        String sqoeInv = "Sq^2_1-odd : ";
        if (Link.sqtOde != null) sqoeInv = sqoeInv+Link.sqtOde;
        JLabel sqoeLabel = new JLabel(sqoeInv);
        JPanel sqoePanel = new JPanel();
        sqoePanel.add(sqoeLabel);
        panel.add(sqoePanel);
        panel.setPreferredSize(panelDimSm);
    }
    
    private void displayOtherSInvariants(LinkData theLink) {
        ViewSInvariants viewer = new ViewSInvariants(theLink, options);
        viewer.setupFrame();
        viewer.setLocationRelativeTo(this);
        viewer.setVisible(true);
    }
    
    private Integer getValue(int[][] data, int field) {
        boolean found = false;
        int i = 0;
        while (!found && i < data.length) {
            if (data[i][0] == field) found = true;
            else i++;
        }
        if (found) return data[i][1];
        return null;
    }
    
    private void fillPolyPanel(JPanel panel, LinkData theLink) {
        panel.setLayout(new GridLayout(1,2));
        String polyString = "Polynomials";
        JLabel polyLabel = new JLabel(polyString);
        JPanel polyPanel = new JPanel();
        polyPanel.add(polyLabel);
        panel.add(polyPanel);
        JButton jonesButton = new JButton("Jones");
        jonesButton.setEnabled(theLink.jones != null);
        JButton alexButton = new JButton("Alexander");
        alexButton.setEnabled(theLink.alex != null);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(jonesButton);
        buttonPanel.add(alexButton);
        panel.add(buttonPanel);
        panel.setPreferredSize(panelDimLg);
        jonesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                showPolynomial("Jones Polynomial of "+theLink.name, "Jones", theLink.jones, "q");
            }
        });
        alexButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                showPolynomial("Alexander Polynomial of "+theLink.name, "Alexander", 
                        theLink.alex, "t");
            }
        });
    }
    
    private void fillStablePanel(JPanel panel, LinkData theLink) {
        panel.setLayout(new GridLayout(1, 2));
        String name = "Stable Homotopy Type";
        JLabel nameLabel = new JLabel(name);
        JPanel namePanel = new JPanel();
        namePanel.add(nameLabel);
        JButton evenButton = new JButton("even");
        evenButton.setEnabled(theLink.showStEvenButton());
        char delta = (char) 949; // it's really epsilon
        JButton oddButton = new JButton("odd ("+delta+"=0)");
        oddButton.setEnabled(theLink.showStOddButton(0));
        JButton odeButton = new JButton("odd ("+delta+"=1)");
        odeButton.setEnabled(theLink.showStOddButton(1));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(evenButton);
        buttonPanel.add(oddButton);
        buttonPanel.add(odeButton);
        panel.add(namePanel);
        panel.add(buttonPanel);
        panel.setPreferredSize(panelDimLg);
        evenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                showStableType(theLink, 2);
            }
        });
        oddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                showStableType(theLink, 0);
            }
        });
        odeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                showStableType(theLink, 1);
            }
        });
    }
    
    private void fillUSlT(JPanel panel, LinkData theLink) {
        panel.setLayout(new GridLayout(1, 3));
        String title = "sl_3-Spectral Sequence";
        JLabel titleLabel = new JLabel(title);
        JPanel titlePanel = new JPanel();
        titlePanel.add(titleLabel);
        panel.add(titlePanel);
        JButton typeThrButton = new JButton("X^3-1");
        typeThrButton.setEnabled(theLink.showSlThreeButton());
        JPanel typePanel = new JPanel();
        typePanel.add(typeThrButton);
        JButton typeTwoButton = new JButton("X^3-X");
        typeTwoButton.setEnabled(theLink.showSlTwoButton());
        typePanel.add(typeTwoButton);
        JButton typeWButton = new JButton("X^3-X-w");
        typeWButton.setEnabled(theLink.showSlTwButton(false));
        JPanel typeWPanel = new JPanel();
        typeWPanel.add(typeWButton);
        JButton typeWSqButton = new JButton("X^3-X^2-w");
        typeWSqButton.setEnabled(theLink.showSlTwButton(true));
        typeWPanel.add(typeWSqButton);
        panel.add(typePanel);
        //panel.add(typeOnePanel);
        panel.add(typeWPanel);
        panel.setPreferredSize(panelDimLg);
        typeThrButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSlTSS(theLink, 2);
            }
        });
        typeTwoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSlTSS(theLink, 3);
            }
        });
        typeWButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSlTSS(theLink, 6);
            }
        });
        typeWSqButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSlTSS(theLink, 7);
            }
        });
    }
    
    private void fillBLT(JPanel panel, LinkData theLink) {
        panel.setLayout(new GridLayout(1, 2));
        String khovHom = "Bar Natan-Lee-Turner Spectral Sequence";
        JLabel akhovHomLabel = new JLabel(khovHom);
        JPanel akhovHomPanel = new JPanel();
        akhovHomPanel.add(akhovHomLabel);
        panel.add(akhovHomPanel);
        JButton aunredButton = new JButton("unreduced");
        aunredButton.setEnabled(theLink.showBLTButton(false));
        JPanel aunredPanel = new JPanel();
        aunredPanel.add(aunredButton);
        JButton aredButton = new JButton("reduced");
        aredButton.setEnabled(theLink.showBLTButton(true));
        aunredPanel.add(aredButton);
        panel.add(aunredPanel);
        panel.setPreferredSize(panelDimLg);
        aunredButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showBLT(theLink, false);
            }
        });
        aredButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showBLT(theLink, true);
            }
        });
    }
    
    private void fillSlTPanel(JPanel panel, LinkData theLink) {
        panel.setLayout(new GridLayout(1, 3));
        String khovHom = "sl_3 Homology";
        JLabel akhovHomLabel = new JLabel(khovHom);
        JPanel akhovHomPanel = new JPanel();
        akhovHomPanel.add(akhovHomLabel);
        panel.add(akhovHomPanel);
        JButton aunredButton = new JButton("unreduced");
        aunredButton.setEnabled(theLink.showSlTHomButton(false));
        JPanel aunredPanel = new JPanel();
        aunredPanel.add(aunredButton);
        JButton aredButton = new JButton("reduced");
        aredButton.setEnabled(theLink.showSlTHomButton(true));
        aunredPanel.add(aredButton);
        JButton typeOneButton = new JButton("X^3-X^2");
        typeOneButton.setEnabled(theLink.showSlOneButton(false));
        JPanel typeOnePanel = new JPanel();
        typeOnePanel.add(typeOneButton);
        JButton typeRedButton = new JButton("X^3-X^2 red");
        typeRedButton.setEnabled(theLink.showSlOneButton(true));
        //typeOnePanel.add(typeRedButton);
        panel.add(aunredPanel);
        panel.add(typeOnePanel);
        panel.setPreferredSize(panelDimLg);
        aunredButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSlTHom(theLink, false);
            }
        });
        aredButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSlTHom(theLink, true);
            }
        });// */
        typeOneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSlTSS(theLink, 4);
            }
        });
        typeRedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSlTSS(theLink, 5);
            }
        });
    }
    
    private void fillKhovanov(JPanel panel, LinkData theLink) {
        panel.setLayout(new GridLayout(1,3));
        String khovHom = "Khovanov Cohomology";
        JLabel akhovHomLabel = new JLabel(khovHom);
        JPanel akhovHomPanel = new JPanel();
        akhovHomPanel.add(akhovHomLabel);
        panel.add(akhovHomPanel);
        JButton aunredButton = new JButton("even");
        aunredButton.setEnabled(theLink.showKhovHomButton(false));
        JPanel aunredPanel = new JPanel();
        aunredPanel.add(aunredButton);
        JButton aredButton = new JButton("even reduced");
        aredButton.setEnabled(theLink.showKhovHomButton(true));
        aunredPanel.add(aredButton);
        JButton ounredButton = new JButton("odd");
        ounredButton.setEnabled(theLink.showOddKhovButton());
        JPanel ounredPanel = new JPanel();
        ounredPanel.add(ounredButton);
        JButton oredButton = new JButton("odd reduced");
        oredButton.setEnabled(theLink.showOddKhovButton());
        ounredPanel.add(oredButton);
        panel.add(aunredPanel);
        panel.add(ounredPanel);
        panel.setPreferredSize(panelDimLg);
        aunredButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHomology(theLink,false);
            }
        });
        aredButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHomology(theLink,true);
            }
        });
        ounredButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOddHomology(theLink,false);
            }
        });
        oredButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOddHomology(theLink,true);
            }
        });
    }
    
    private void showOddHomology(LinkData theLink, boolean reduced) {
        ViewOddHomology viewer = new ViewOddHomology(theLink, reduced, options);
        String title;
        if (reduced) title = "Reduced Odd Khovanov Homology of "+theLink.name;
        else title = "Unreduced Odd Khovanov Homology of "+theLink.name;
        viewer.setUpStuff(title);
    }
    
    private void showHomology(LinkData theLink, boolean reduced) {
        ViewCohomology viewer = new ViewCohomology(theLink, reduced, options);
        String title;
        if (reduced) title = "Reduced Khovanov Cohomology of "+theLink.name;
        else title = "Unreduced Khovanov Cohomology of "+theLink.name;
        viewer.setUpStuff(title);
    }
    
    private void showSlTHom(LinkData theLink, boolean reduced) {
        ViewSlTHomology viewer = new ViewSlTHomology(theLink, reduced, options);
        String title;
        if (reduced) title = "Reduced sl_3 Homology of "+theLink.name;
        else title = "Unreduced sl_3 Homology of "+theLink.name;
        viewer.setUpStuff(title);
    }
    
    private void showBLT(LinkData theLink, boolean reduced) {
        String title = "Bar Natan-Lee-Turner Spectral Sequence of "+theLink.name;
        if (reduced) title = "Reduced "+title;
        else title = "Unreduced "+title;
        int typ = 0;
        if (reduced) typ = 1;
        ViewBLTInvariant viewer = new ViewBLTInvariant(title, this, theLink, typ, options);
        viewer.setUp("Characteristic :");
    }
    
    private void showSlTSS(LinkData theLink, int typ) {
        String title = "Sl_3-Spectral Sequence of "+theLink.name;
        String charTitle = "Characteristic :";
        if (typ == 2) title = "X^3 - 1 "+title;
        if (typ == 3) title = "X^3 - X "+title;
        if (typ == 4) title = "X^3 - X^2 "+title;
        if (typ == 5) title = "X^3 - X^2 "+title;
        if (typ == 6) {
            title = "X^3 - X - w "+title;
            charTitle = "w = ";
        }
        if (typ == 7) {
            title = "X^3 - X^2 - w "+title;
            charTitle = "w = ";
        }
        ViewBLTInvariant viewer = new ViewBLTInvariant(title, this, theLink, typ, options);
        viewer.setUp(charTitle);
    }
    
    private void showStableType(LinkData theLink, int odd) {
        ViewStableType viewer = new ViewStableType(theLink, this, odd, options);
        String title = " Stable Homotopy Type of "+theLink.name;
        if (odd <= 1) title = "Odd ("+(char)949+"="+odd+")"+title;
        else title = "Even"+title;
        viewer.setupViewer(title);
    }
    
    private void showPolynomial(String title, String altTitle, String code, String label) {
        StringFrame fram = new StringFrame(title, true);
        HalfPolynomial poly = new HalfPolynomial(new String[] {label}, code);
        code = poly.toString();
        fram.setupStuff(this, altTitle, code);
        poly.setLatex(true);
        fram.setAlternative(poly.toString());
    }
    
    private void showString(String title, String code, String name) {
        StringFrame fram = new StringFrame(title+" of "+name, false);
        fram.setupStuff(this, title, code);
    }

    private void setUpCrossingInfo(JPanel panel, LinkData theLink) {
        panel.removeAll();
        panel.setLayout(new GridLayout(1,4));
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();
        JPanel panel4 = new JPanel();
        int bc = 1 + theLink.chosenLink().basecomponent();
        JLabel compLabel = new JLabel("Components : "+theLink.chosenLink().components()+" ("+bc+")");
        int[] crsigns = theLink.chosenLink().crossingSigns();
        int writhe = crsigns[0]-crsigns[1];
        JLabel writLabel = new JLabel("Writhe : "+writhe);
        int crossngs = crsigns[0]+crsigns[1];
        int crossngz = theLink.chosenLink().crossingLength();
        JLabel crosLabel = new JLabel("Crossings : "+crossngs+"/"+crossngz);
        int girth = theLink.chosenLink().maxGirth();
        int tgirth = theLink.chosenLink().totalGirth();
        JLabel girtLabel = new JLabel("Girth : "+girth+"/"+tgirth);
        panel1.add(compLabel);
        panel2.add(writLabel);
        panel3.add(crosLabel);
        panel4.add(girtLabel);
        panel.add(panel1);
        panel.add(panel2);
        panel.add(panel3);
        panel.add(panel4);
        panel.setPreferredSize(panelDimSm);
        panel.revalidate();
    }
    
    private void showTheDiagram(LinkData theLink) {
        if (options.getGridDiagram() == 0) {
            showGridDiagram(theLink);
            return;
        }
        int number = theLink.chosen()+1;
        DiagramFrame frame = new DiagramFrame(theLink.name+" - Diagram "+number, options);
        frame.setSize(524, 600);
        frame.setMinimumSize(new Dimension(524, 600));
        frame.setLocationRelativeTo(this);
        frame.addBasics();
        frame.setVisible(true);
        ShowDiagram showDiagram = new ShowDiagram(theLink, frame, options);
        showDiagram.start();
    }

    private void showGridDiagram(LinkData theLink) {
        int number = theLink.chosen()+1;
        GridDiagramFrame frame = new GridDiagramFrame(theLink.name+" - Diagram "+number, 
                this, options, theLink);
        frame.setUp();
        ShowGridDiagram showDiagram = new ShowGridDiagram(theLink, frame);
        showDiagram.start();
    }
    
    private void exportLinks() {
        ArrayList<LinkData> theLinks = allLinks;
        if (filtered) theLinks = filteredLinks;
        LinkSaver exporter = new LinkSaver(theLinks, choices, options, this);
        exporter.export();
        
    }
    
    private void saveLinks() {
        ArrayList<LinkData> theLinks = allLinks;
        if (filtered) theLinks = filteredLinks;
        LinkSaver saver = new LinkSaver(theLinks, choices, options, this);
        saver.save();
    }
    
    private ArrayList<LinkData> importLinks() {
        ArrayList<LinkData> theData = new ArrayList<LinkData>();
        ImportDialog dialog = new ImportDialog(this);
        dialog.setup();
        int ch = dialog.getChoice();
        if (ch != 0) {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            if (options.getLoadLinksFrom() != null) chooser.setCurrentDirectory(options.getLoadLinksFrom());
            FileNameExtensionFilter filtes = new FileNameExtensionFilter("Snappy Links (*.txt)", "txt");
            FileNameExtensionFilter filtet = new FileNameExtensionFilter("Thistlethwaite Links (*.adc)", "adc");
            if (ch == 1) chooser.setFileFilter(filtes);
            if (ch == 2) chooser.setFileFilter(filtet);
            chooser.setAcceptAllFileFilterUsed(false);
            int val = chooser.showOpenDialog(this);
            if (val == JFileChooser.APPROVE_OPTION) {
                LoadDialog fram = new LoadDialog(this, "Importing Links", true);
                LinkLoader importing = new LinkLoader(chooser, fram, this, ch);
                importing.start();
                fram.setUpStuff();
                theData = importing.getLinks();
            }
        }
        return theData;
    }
    
    private ArrayList<LinkData> loadLinks(boolean select) {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        ArrayList<LinkData> theLinks = new ArrayList<LinkData>();
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        if (options.getLoadLinksFrom() != null) chooser.setCurrentDirectory(options.getLoadLinksFrom());
        FileNameExtensionFilter filtek = new FileNameExtensionFilter("KnotJob Links (*.kjb)", "kjb");
        FileNameExtensionFilter filtet = new FileNameExtensionFilter("TKnotJob Links (*.tkj)", "tkj");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("SKnotJob Links (*.kts)", "kts");
        FileNameExtensionFilter filtex = new FileNameExtensionFilter("XKnotJob Links (*.gld)", "gld");
        FileNameExtensionFilter filtes = new FileNameExtensionFilter("KnotScape Knots (*.dtc)", "dtc");
        FileNameExtensionFilter filtey = new FileNameExtensionFilter("Planar Diagrams (*.txt)", "txt");
        FileNameExtensionFilter filteb = new FileNameExtensionFilter("Braid Diagrams (*.brd)", "brd");
        FileNameExtensionFilter filtea = new FileNameExtensionFilter("Alphabetical DT-Code (*.adc)", "adc");
        chooser.setFileFilter(filtek);
        chooser.addChoosableFileFilter(filter);
        chooser.addChoosableFileFilter(filtet);
        if (!select) chooser.addChoosableFileFilter(filtex);
        chooser.addChoosableFileFilter(filtes);
        chooser.addChoosableFileFilter(filtey);
        chooser.addChoosableFileFilter(filteb);
        chooser.addChoosableFileFilter(filtea);
        int val = chooser.showOpenDialog(this);
        if (val == JFileChooser.APPROVE_OPTION) {
            LoadDialog fram = new LoadDialog(this, "Loading Links", true);
            LinkLoader loading = new LinkLoader(chooser, fram, this, select);
            loading.start();
            fram.setUpStuff();
            theLinks = loading.getLinks();
        }
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        return theLinks;
    }
    
    private ArrayList<LinkData> chosenLinks(ArrayList<LinkData> linkList, int[] choices) {
        ArrayList<LinkData> theList = new ArrayList<LinkData>(choices.length);
        for (int c : choices) theList.add(linkList.get(c));
        return theList;
    }
    
    private void minimizeGirth(ArrayList<LinkData> theLinks) {
        GirthDialogWrap fram = new GirthDialogWrap(this);
        GirthMinimizer giMi = new GirthMinimizer(theLinks, options, fram);
        giMi.start();
        fram.setup(giMi);
        giMi.setCancelled(true);
    }
    
    private void combineCrossings(ArrayList<LinkData> theLinks) {
        for (LinkData link : theLinks) {
            Link oldLink = link.chosenLink();
            Link newLink = Reidemeister.combineCrossings(oldLink);
            if (newLink.crossingLength() < oldLink.crossingLength()) {
                link.links.add(newLink.girthMinimize());
            }
        }
    }
    
    private void minimizeDiagramCrossings(ArrayList<LinkData> theLinks, boolean combine) {
        CombDialogWrap fram = new CombDialogWrap(this);
        CombMinimizer coMi = new CombMinimizer(theLinks, fram, combine);
        coMi.start();
        fram.setup(coMi);
    }
    
    private void setOptions(int page) {
        OptionDialog opts = new OptionDialog(this, "Options", true, options, page);
        opts.setUpStuff();
    }
    
    public Options getOptions() {
        return options;
    }
    
    private void editLink() {
        ArrayList<LinkData> theLinks = allLinks;
        if (filtered) theLinks = filteredLinks;
        LinkData theData = theLinks.get(choice);
        EditDialog editor = new EditDialog(this,"Edit "+theData.name,true,theData);
        editor.setupDialog();
        if (editor.isOkay()) {
            theData.name = editor.getNewName();
            theData.comment = editor.getComment();
            setLinkInfo(theData);
            DefaultListModel<String> listModel = listModelAll;
            if (filtered) listModel = listModelFiltered;
            listModel.setElementAt(theData.name, choice);
        }
    }
    
    public void setActiveFilter(Filter filter) {
        activeFilter = filter;
        setFilter();
    }
    
    private void setFilter() {
        if (activeFilter == null) return;
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        filteredLinks.clear();
        listModelFiltered.removeAllElements();
        for (LinkData link : allLinks) {
            if (activeFilter.linkIsFiltered(link)) {
                filteredLinks.add(link);
                listModelFiltered.addElement(link.name);
            }
        }
        if (filtered) labelLinkNumber.setText("Links : "+filteredLinks.size());
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    public Comparer getComparer() {
        return comparer;
    }
    
    private void sortLinks() {
        SortDialog sorter = new SortDialog(this, "Sort Links by", true, comparer.isReversed());
        int sort = sorter.getSelected();
        if (sort < 0) return;
        comparer.setType(sort);
        comparer.setReversed(sorter.isReversed());
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Collections.sort(filteredLinks);
        listModelFiltered.removeAllElements();
        for (LinkData link : filteredLinks) listModelFiltered.addElement(link.name);
        Collections.sort(allLinks);
        listModelAll.removeAllElements();
        for (LinkData link : allLinks) listModelAll.addElement(link.name);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        setKnotInfoEmpty();
    }
    
    public void updateLink() {
        if (filtered) {
            if (0 <= choice && choice < filteredLinks.size())
                setLinkInfo(filteredLinks.get(choice));
        }
        else {
            if (0 <= choice) setLinkInfo(allLinks.get(choice));
        }
    }
    
}