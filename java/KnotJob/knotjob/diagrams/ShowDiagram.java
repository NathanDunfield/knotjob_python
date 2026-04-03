/*

Copyright (C) 2019-21 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.diagrams;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.diagrams.diagrams3d.GraphicalDiagram3D;
import knotjob.diagrams.diagrams3d.LinkDiagram3D;
import knotjob.diagrams.griddiagrams.GridThread;
import knotjob.dialogs.CompDialog;
import knotjob.dialogs.DiagramFrame;
import knotjob.dialogs.DiagramFrame3D;
import knotjob.dialogs.ExportDialog;
import knotjob.dialogs.RotateDialog;
import knotjob.links.Link;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class ShowDiagram extends Thread {

    private final DiagramFrame frame;
    private final Options opts;
    private final AbortInfo abort;
    private final LinkData theLink;
    private final Link link;
    private final String name;
    private final boolean gridDiagram;
    private SComplex[] theComplexes;
    private JScrollPane scroller;
    private final double factor;
    private final int cmps;
    private GraphicalDiagram gDiag;
    private CountDownLatch countDown;
    
    public ShowDiagram(LinkData tLnk, DiagramFrame fram, Options opns) {
        frame = fram;
        opts = opns;
        factor = opts.getDivFactor();
        abort = new AbortInfo();
        theLink = tLnk;
        name = tLnk.name;
        gridDiagram = (opts.getGridDiagram() == 0);
        Link nlink = theLink.chosenLink().breakUp();
        cmps = nlink.relComponents();
        if (nlink.isReduced()) link = nlink;
        else link = nlink.graphicalReduced();
        gDiag = null;
    }

    @Override
    public void run() {
        frame.closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                frame.dispose();
                if (gDiag != null) gDiag.stopMoving();
                abort.cancel();
            }
        });
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                frame.dispose();
                if (gDiag != null) gDiag.stopMoving();
                abort.cancel();
            }
        });
        countDown = new CountDownLatch(1);
        if (gridDiagram) {
            GridThread gridDig = new GridThread(theLink, frame, false, countDown);
            gridDig.start();
            try {
                countDown.await();
            } 
            catch (InterruptedException ex) {
                //Logger.getLogger(ShowDiagram.class.getName()).log(Level.SEVERE, null, ex);
            }
            theComplexes = new SComplex[] {gridDig.getSComplex(0)};
        }
        else {
            CircleDiagram circleDig = new CircleDiagram(link, 12, frame, countDown);
            circleDig.start();
            try {
                countDown.await();
            } 
            catch (InterruptedException ex) {
                //Logger.getLogger(ShowDiagram.class.getName()).log(Level.SEVERE, null, ex);
            }
            theComplexes = circleDig.getComplexes();
        }
        if (theComplexes == null) {
            frame.dispose();
            return;
        }
        frame.exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                exportDiagram();
            }
        });
        delay(100);
        drawDiagram(null, null, null);
        setStuff(20);
        frame.zoomSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int factor = 19+frame.zoomSlider.getValue();
                setStuff(factor);

            }
        });
        frame.minimizeEng.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JCheckBox theBox = (JCheckBox) ae.getSource();
                gDiag.minimizeEng(theBox.isSelected());
                gDiag.repaint();
            }
        });
        frame.rotateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gDiag.stopMoving();
                frame.minimizeEng.setSelected(false);
                RotateDialog fram = new RotateDialog(frame, "Rotate Link by ", true);
                fram.setUpStuff(new String[] {"Rotate by "});
                if (!fram.angle.isEmpty()) gDiag.rotateDiagram(fram.angle.get(0));
                gDiag.setPreferredSize(new Dimension(100 + (int) (gDiag.factorx * gDiag.maxx),60 + 
                        (int) (gDiag.factory * gDiag.maxy)));
                gDiag.repaint();
            }
        });
        frame.compButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gDiag.stopMoving();
                frame.minimizeEng.setSelected(false);
                CompDialog fram = new CompDialog(frame, "Components", true, gDiag, true);
                fram.setUpStuff();
                if (fram.isOkay()) {
                    gDiag.setColors(fram.setColors());
                    gDiag.setShownComponents(fram.setShownComponents());
                    gDiag.setOrientComponents(fram.setOrientComponents());
                }
                gDiag.repaint();
            }
        });
    }
    
    void delay(int k) {
        try {
            Thread.sleep(k);
        }
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void drawDiagram(ArrayList<Color> clrs, ArrayList<Boolean> oCs, ArrayList<Boolean> sCs) {
        Link lnk = link;
        if (gridDiagram) lnk = link.mirror(); // somehow I get the wrong picture otherwise
        gDiag = new GraphicalDiagram(theComplexes,19+frame.zoomSlider.getValue(), 
                19+frame.zoomSlider.getValue(), lnk, cmps, factor,
                frame.minimizeEng.isSelected(), gridDiagram, clrs, oCs, sCs);
        gDiag.setPreferredSize(new Dimension(100 + (int) (20 * gDiag.maxx),
                60 + (int) (20 * gDiag.maxy)));
        Container c = frame.getContentPane();
        c.remove(frame.infoPanel);
        scroller = new JScrollPane(gDiag);
        scroller.getViewport().setBackground(Color.WHITE);
        c.add(scroller, BorderLayout.CENTER);
        frame.revalidate();
    }

    private void setStuff(int factor) {
        gDiag.factorx = (double) (factor);
        gDiag.factory = (double) (factor);
        gDiag.setPreferredSize(new Dimension(100 + (int) (factor * gDiag.maxx),
                60 + (int) (factor * gDiag.maxy)));
        gDiag.repaint();
        scroller.getViewport().revalidate();
    }
    
    private void exportDiagram() {
        if (frame.minimizeEng.isSelected()) {
            frame.minimizeEng.setSelected(false);
            gDiag.stopMoving();
        }
        ExportDialog expDiag = new ExportDialog(frame, "Export as ...", new int[] {0, 1, 2});
        int choice = expDiag.getValue();
        if (choice == 2) getNewFrame();
        if (choice == 1) {
            LinkDiagram3D diag3D = new LinkDiagram3D(gDiag.theComplexes.get(0), link, 
                gDiag.drawnComp, gDiag.showComps);
            GraphicalDiagram3D dig = new GraphicalDiagram3D(diag3D);
            dig.runOnce(50);
            try {
                dig.mover.join();
            } catch (InterruptedException ex) {
                //Logger.getLogger(ShowDiagram.class.getName()).log(Level.SEVERE, null, ex);
            }
            ArrayList<String> theStrings = diag3D.printCoordinates();
            SaveDiagram.saveCommands("img_"+name, ".pov", theStrings, opts, frame);
        }
        if (choice == 0) { // export as tikzpicture
            ArrayList<String> theStrings = gDiag.printCoordinates();
            SaveDiagram.saveCommands("img_"+name, ".tex", theStrings, opts, frame);
        }
    }

    private void getNewFrame() {
        DiagramFrame3D anotherDig = new DiagramFrame3D(frame.getTitle()+" - 3D", name, opts);
        anotherDig.setSize(700,800);
        anotherDig.setResizable(false);
        anotherDig.setLocationRelativeTo(frame);
        anotherDig.addBasics();
        LinkDiagram3D diag3D = new LinkDiagram3D(gDiag.theComplexes.get(0), link, 
                gDiag.drawnComp, gDiag.showComps);
        anotherDig.setButtons(diag3D, 50);
    }

    
}
