/*

Copyright (C) 2022 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import knotjob.links.Link;
import knotjob.links.LinkCreator;

/**
 *
 * @author Dirk
 */
public class KnotFileConverter {
    
    int counter = 0;
    int max = 50000;
    int filemax = 200;
    int totfile = 0;
    int fold = 0;
    
    KnotFileConverter() {
        
    }
    
    public static void main(String[] args) {
        KnotFileConverter conv = new KnotFileConverter();
        conv.getStarted();
        
    }

    private void getStarted() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        int val = chooser.showOpenDialog(null);
        if (val == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            File directory = chooser.getCurrentDirectory();
            String fileName = files[0].getName();
            //countFiles(files[0]);
            if (fileName.endsWith(".csv")) extractNames(files[0], directory, fileName);
            if (fileName.endsWith(".names")) convertNames(files, directory);
            if (fileName.endsWith(".mtt")) dealWithThistles(files[0], directory);
            if (fileName.endsWith(".adc")) dealWithAddDTCodes(files, directory);
            if (fileName.endsWith(".kjb")) dealWithData(files, directory);
        }
    }

    private String getName(String line, String det) {
        int end = line.indexOf(det);
        return line.substring(0, end);
    }

    private String getDTCode(String line) {
        int end = line.indexOf(",");
        String help = line.substring(end+1);
        end = help.indexOf(",");
        help = help.substring(end+1);
        end = help.indexOf(",");
        if (end < 0) return help;
        return help.substring(0, end);
    }

    private void createFile(String name, File directory, ArrayList<String> lines, String ext) {
        totfile++;
        if (totfile >= filemax) {
            fold++;
            totfile = 0;
        }
        //System.out.println(name+" "+fold+" "+totfile);
        String first = getName(lines.get(0), " ");
        String last = "";
        String foldName = ""+fold;
        //while (foldName.length() < 3) foldName = "0"+foldName;
        if (lines.size()>1) last = "-"+getName(lines.get(lines.size()-1), " ");
        saveNames(first+last+ext, directory+"/"+name+"_"+foldName, lines);
    }

    private void createFile(String name, File directory, ArrayList<String> lines, 
            String ext, int tot) {
        totfile++;
        if (totfile >= filemax) {
            fold++;
            totfile = 0;
        }
        String countName = ""+tot;
        while (countName.length() < 10) countName = "0"+countName;
        String foldName = ""+fold;
        while (foldName.length() < 2) foldName = "0"+foldName;
        saveNames("Z"+countName+ext, directory+"/"+name+"_"+foldName, lines);
    }
    
    private void extractNames(File file, File directory, String fileName) {
        int dot = fileName.indexOf(".");
        fileName = fileName.substring(0, dot);
        try (FileReader fr = new FileReader(file)) {
            BufferedReader in = new BufferedReader(fr);
            boolean keepreading = true;
            String line;
            in.readLine();
            ArrayList<String> lines = new ArrayList<String>();
            while (keepreading) {
                line = in.readLine();
                if (line == null) keepreading = false;
                if (keepreading) {
                    counter++;
                    if (counter > max) {
                        counter = 1;
                        createFile(fileName, directory, lines, ".names");
                        lines = new ArrayList<String>();
                    }
                    String name = getName(line, ",");
                    String dtcd = getDTCode(line);
                    lines.add(name+" "+dtcd);
                }
            }
            if (!lines.isEmpty()) createFile(fileName, directory, lines, ".names");
            fr.close();
        }
        catch (IOException io) {

        }
    }

    private void saveNames(String name, String directory, ArrayList<String> lines) {
        File direc = new File(directory);
        if (!direc.exists()) direc.mkdirs();
        File file = new File(directory+"/"+name);
        try (FileWriter fw = new FileWriter(file)) {
            try (PrintWriter pw = new PrintWriter(fw)) {
                for (String data : lines) pw.println(data);
            }
        }
        catch (IOException e) {
            
        }// */
        System.out.println("Saved "+file);
    }

    private void convertNames(File[] files, File directory) {
        for (File file : files) {
            ArrayList<String> lines = new ArrayList<String>();
            try (FileReader fr = new FileReader(file)) {
                BufferedReader in = new BufferedReader(fr);
                boolean keepreading = true;
                String line;
                while (keepreading) {
                    line = in.readLine();
                    if (line == null) keepreading = false;
                    if (keepreading) {
                        String name = getName(line, " ");
                        String dtcd = getDTCode(line, " ");
                        Link link = LinkCreator.obtainLink(dtcd).girthMinimize();
                        lines.add(name+" ="+LinkSaver.exportData(link));
                    }
                }
                fr.close();
            }
            catch (IOException io) {

            }
            String direc = ""+directory;
            String fileName = file.getName();
            int u = fileName.indexOf(".");
            saveNames(fileName.substring(0, u)+".txt", (String) direc, lines);
        }
    }

    private String getDTCode(String line, String det) {
        int pos = line.indexOf(" ");
        String code = line.substring(pos+1);
        String dtcode = "";
        for (char c : code.toCharArray()) {
            int ascii = (int) c;
            int val;
            if (ascii > 96) val = 2*(ascii - 96);
            else val = (-2) *(ascii - 64);
            dtcode = dtcode+val+" ";
        }
        return dtcode;
    }

    private void dealWithThistles(File file, File directory) {
        max = max * 2;
        totfile--;
        int totcount = counter;
        String fileName = "20n-hyp";
        try (FileReader fr = new FileReader(file)) {
            BufferedReader in = new BufferedReader(fr);
            boolean keepreading = true;
            String line;
            //in.readLine();
            ArrayList<String> lines = new ArrayList<String>();
            while (keepreading) {
                line = in.readLine();
                if (line == null) keepreading = false;
                if (keepreading) {
                    counter++;
                    totcount++;
                    if (counter > max) {
                        /*filenr++;
                        if (filenr >= 500) {
                            dir++;
                            filenr = 0;
                            keepreading = false;
                        } // */
                        counter = 1;
                        createFile(fileName, directory, lines, ".adc", totcount-lines.size());
                        lines = new ArrayList<String>();
                    }
                    //boolean alt = checkAlt(line);
                    //String name = getName(totcount, alt);
                    //String pdiag = getDiagram(line);
                    //System.out.println(name+pdiag+"   "+filenr+"   "+dir);
                    //String name = getName(line, ",");
                    //String dtcd = getDTCode(line);
                    lines.add(line);
                }
            }
            if (!lines.isEmpty()) createFile(fileName, directory, lines, ".adc", 1+totcount-lines.size());
            fr.close();
        }
        catch (IOException io) {

        }
        System.out.println(totcount);
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private boolean checkAlt(String line) {
        for (int i = 0; i < line.length(); i++) {
            if ((int) line.charAt(i) <= 90) return false;
        }
        return true;
    }

    private String getName(int totcount, boolean alt) {
        String name = ""+totcount;
        int length = 10;
        if (alt) length = 9;
        while (name.length() < length) name = "0"+name;
        String prefix = "20nh";
        if (alt) prefix = "20ah";
        return prefix+"_"+name;
    }

    private String getDiagram(String line) {
        Link link = LinkCreator.obtainLinkAlp(line).girthMinimize();
        return " ="+LinkSaver.exportData(link);
    }

    private void dealWithAddDTCodes(File[] files, File directory) {
        for (File file : files) {
            String fileName = file.getName();
            int u = fileName.indexOf(".");
            int totCount = 0;
            int start = Integer.parseInt(fileName.substring(1, u));
            ArrayList<String> lines = new ArrayList<String>();
            try (FileReader fr = new FileReader(file)) {
                BufferedReader in = new BufferedReader(fr);
                boolean keepreading = true;
                String line;
                while (keepreading) {
                    line = in.readLine();
                    if (line == null) keepreading = false;
                    else {
                        totCount++;
                        boolean alt = checkAlt(line);
                        String name = getName(totCount+start-1, alt);
                        String pdiag = getDiagram(line);
                        if (totCount%10000 == 0) System.out.println(name+pdiag);
                        lines.add(name+pdiag);
                    }
                }
                fr.close();
            }
            catch (IOException io) {

            }
            String first = lines.get(0);
            String last = lines.get(lines.size()-1);
            //System.out.println("First "+first);
            //System.out.println("Last  "+last);
            int e = first.indexOf(" =");
            int f = last.indexOf(" =");
            //System.out.println(first.substring(0, e)+"-"+last.substring(0, f));
            String direc = ""+directory;
            fileName = first.substring(0, e)+"-"+last.substring(0, f)+".txt";
            saveNames(fileName, (String) direc, lines);
        }
    }

    private void countFiles(File file) {
        int totCount = 0;
        try (FileReader fr = new FileReader(file)) {
            BufferedReader in = new BufferedReader(fr);
            boolean keepreading = true;
            String line;
            while (keepreading) {
                line = in.readLine();
                if (line == null) keepreading = false;
                else {
                    totCount++;
                }
            }
        }
        catch (IOException io) {
            
        }
        System.out.println(totCount);
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    private void dealWithData(File[] files, File directory) {
        ArrayList<String> bigList = new ArrayList<String>();
        for (File file : files) {
            String fileName = file.getName();
            int u = fileName.indexOf(".");
            try (FileReader fr = new FileReader(file)) {
                BufferedReader in = new BufferedReader(fr);
                boolean keepreading = true;
                String line;
                while (keepreading) {
                    line = in.readLine();
                    if (line == null) keepreading = false;
                    else {
                        if (line.startsWith("0:")) bigList.add(line);
                        if (eLine(line)) bigList.add(line);
                    }
                }
                fr.close();
            }
            catch (IOException io) {

            }
        }
        printInteresting(bigList);
    }
    
    private boolean eLine(String line) {
        if (!line.startsWith("19:")) return false;
        return line.contains("r:e");
    }

    private void printInteresting(ArrayList<String> bigList) {
        ArrayList<KnotData> data = new ArrayList<KnotData>();
        KnotData current = new KnotData("Dummy");
        for (String line : bigList) {
            if (line.startsWith("0:")) {
                if (!"Dummy".equals(current.name)) data.add(current);
                current = new KnotData(line.substring(2));
            }
            else current.addInfo(line);
        }
        if (!"Dummy".equals(current.name)) data.add(current);
        counter = 0;
        for (KnotData line : data) {
            if (line.notConstant() || line.biggestX() >= 4) {
                counter++;
                line.output();
            }
        }
        System.out.println(counter+" "+bigList.size()+" "+data.size());
    }
    
    private class KnotData {
        
        private String name;
        private final ArrayList<Integer> chars;
        private final ArrayList<Integer> exer;
        
        private KnotData(String nm) {
            name = nm;
            chars = new ArrayList<Integer>();
            exer = new ArrayList<Integer>();
        }

        private void addInfo(String line) {
            String subline = line.substring(3);
            int a = subline.indexOf(":");
            chars.add(Integer.valueOf(subline.substring(0, a)));
            exer.add(biggestE(subline));
        }
        
        private int biggestE(String line) {
            int u = 1;
            while (line.contains("e"+u+".")) {
                u++;
            }
            return u-1;
        }
        
        private int biggestX() {
            if (exer.isEmpty()) return -1;
            int big = exer.get(0);
            for (int j = 1; j < exer.size(); j++) {
                if (big < exer.get(j)) big = exer.get(j);
            }
            return big;
        }
        
        private boolean notConstant() {
            if (exer.size()<=1) return false;
            int a = exer.get(0);
            for (int j = 1; j < exer.size(); j++) if (a != exer.get(j)) return true;
            return false;
        }
        
        private void output() {
            System.out.print(name+": ");
            for (int y = 0; y < chars.size(); y++) System.out.print(chars.get(y)+" "+exer.get(y)+"     ");
            System.out.println();
        }
        
    }
    
}
