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

import javax.swing.JFrame;

/**
 *
 * @author Dirk
 * 
 * Version history:
 * 
 * This version is chartreuse
 * 
 * Previous version have been
 * aquamarine
 * gray
 * pink
 * blue
 * magenta
 * red
 * orange
 * green
 * cyan (TKnotJob)
 * yellow (SKnotJob)
 * 
 */
public class KnotJob {

    private final Options options;
    private final Knobster knobster;
    
    public KnotJob() {
        options = new Options();
        knobster = new Knobster("KnotJob", options);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0) commandLine(args);
        else javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getTheJobDone();
            }
        });
    }
    
    private static void commandLine(String[] args) {
        System.setProperty("java.awt.headless", "true");
        KnotJobCommand knotJobCommand = new KnotJobCommand(args);
        knotJobCommand.getStarted();
    }
    
    private static void getTheJobDone() {
        KnotJob knotjob;
        knotjob = new KnotJob();
        knotjob.getStarted();
    }
    
    private void getStarted() {
        knobster.setSize(1000, 800);
        knobster.setLocationRelativeTo(null);
        knobster.setResizable(false);
        knobster.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        knobster.setClosing();
        knobster.setIcon();
        knobster.setAbout();
        knobster.setVisible(true);
    }
    
}
