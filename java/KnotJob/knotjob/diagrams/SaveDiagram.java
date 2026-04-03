/*

Copyright (C) 2021 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import knotjob.Options;

/**
 *
 * @author Dirk
 */
public class SaveDiagram {
    
    public static void saveCommands(String title, String ext, ArrayList<String> commands, 
            Options opts, JFrame fram) {
        JFileChooser chooser = new JFileChooser();
        if (opts.getSaveImage() != null) chooser.setCurrentDirectory(opts.getSaveImage());
        FileNameExtensionFilter filterTex = new FileNameExtensionFilter("LaTeX files (*.tex)", "tex");
        FileNameExtensionFilter filterPov = new FileNameExtensionFilter("Pov-Ray files (*.pov)", "pov");
        FileNameExtensionFilter filterCvs = new FileNameExtensionFilter("CSV files (*.csv)", "csv");
        if (null == ext) chooser.setFileFilter(filterCvs);
        else switch (ext) {
            case ".tex":
                chooser.setFileFilter(filterTex);
                break;
            case ".pov":
                chooser.setFileFilter(filterPov);
                break;
            default:
                chooser.setFileFilter(filterCvs);
                break;
        }
        chooser.setSelectedFile(new File(title+ext));
        int val = chooser.showSaveDialog(fram);
        if (val == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                opts.setSaveImage(chooser.getCurrentDirectory());
                String fname = file.getAbsolutePath();
                if(!fname.endsWith(ext) ) {
                    file = new File(fname + ext);
                }
                try (FileWriter fw = new FileWriter(file);PrintWriter pw = new PrintWriter(fw)) {
                    for (String command : commands) pw.println(command);
                }
            }
            catch (IOException e) {

            }
        }
    }
    
}
