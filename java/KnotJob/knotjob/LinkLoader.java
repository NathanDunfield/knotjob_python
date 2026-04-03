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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import javax.swing.JFileChooser;
import knotjob.dialogs.LoadDialog;
import knotjob.dialogs.LoadDialogWrap;
import knotjob.dialogs.DialogWrap;
import knotjob.dialogs.OpenSelectDialog;
import knotjob.links.Link;
import knotjob.links.LinkCreator;
import knotjob.links.LinkData;
import knotjob.links.StringData;

/**
 *
 * @author Dirk
 */
public class LinkLoader extends Thread {
    
    private final File[] files;
    private final Knobster knob;
    private final DialogWrap frame;
    private final Comparer comparer;
    private final Options options;
    private final ArrayList<LinkData> theLinks;
    private final boolean select;
    private final boolean imprt;
    private int choice;
    private int counter;
    private String symbol;
    private int extraZero;
    public AbortInfo abInf;

    public LinkLoader(JFileChooser choose, LoadDialog fram, Knobster knb, boolean sel) {
        files = choose.getSelectedFiles();
        frame = new LoadDialogWrap(fram);
        knob = knb;
        select = sel;
        options = knb.getOptions();
        options.setLoadLinksFrom(choose.getCurrentDirectory());
        abInf = fram.abInf;
        theLinks = new ArrayList<LinkData>();
        comparer = knb.getComparer();
        imprt = false;
        counter = 1;
    }
    
    public LinkLoader(JFileChooser choose, LoadDialog fram, Knobster knb, int chce) {
        files = choose.getSelectedFiles();
        frame = new LoadDialogWrap(fram);
        knob = knb;
        select = false;
        options = knb.getOptions();
        options.setLoadLinksFrom(choose.getCurrentDirectory());
        symbol = options.getSlash();
        abInf = fram.abInf;
        comparer = knb.getComparer();
        theLinks = new ArrayList<LinkData>();
        imprt = true;
        choice = chce;
    }
    
    public LinkLoader(File[] fls, CountDownLatch countDown) {
        files = fls;
        frame = new DialogWrap(countDown, null);
        knob = null;
        options = new Options();
        abInf = new AbortInfo();
        theLinks = new ArrayList<LinkData>();
        comparer = new Comparer(0);
        counter = 1;
        select = false;
        imprt = false;
    }

    @Override
    public void run() {
        if (imprt) importLinks();
        else loadLinks();
    }
        
    private void importLinks() {
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> codes = new ArrayList<String>();
        try {
            for (File file : files) {
                if (abInf.isCancelled()) break;
                if (choice == 2) counter = counterFrom(file.toString());
                String fname = file.getAbsolutePath();
                String line;
                int y = fname.lastIndexOf(options.getSlash());
                frame.setText(fname.substring(y+1));
                try (FileReader fr = new FileReader(file)) {
                    BufferedReader in = new BufferedReader(fr);
                    boolean keepreading = true;
                    while (keepreading) {
                        line = in.readLine();
                        if (line == null) keepreading = false;
                        else dealWithLine(line, names, codes);
                    }
                    fr.close();
                }
            }
        }
        catch (IOException io) {
            abInf.cancel();
        }
        int x = names.size();
        if (codes.size() < x) x = codes.size();
        for (int i = 0; i < x; i++) theLinks.add(newLinkFrom(names.get(i), codes.get(i)));
        frame.dispose();
    }
    
    private int counterFrom(String name) {
        int b = name.lastIndexOf(symbol)+2;
        int e = name.lastIndexOf(".");
        int count = Integer.parseInt(name.substring(b, e));
        extraZero = e-b;
        return count;
    }
    
    private LinkData newLinkFrom(String name, String code) {
        LinkData theLink = null;
        if (choice == 1) {
            theLink = LinkCreator.enterPDCode(code, name, true, knob, comparer);
        }
        if (choice == 2) {
            theLink = LinkCreator.enterADTCode(code, name, true, knob, comparer);
        }
        if (theLink != null) frame.setText(theLink.name);
        return theLink;
    }
    
    private void dealWithLine(String line, ArrayList<String> names, ArrayList<String> codes) {
        if (choice == 1) {
            if (line.contains("'name'")) names.add(getNameOf(line.substring(line.indexOf(":"))));
            if (line.contains("'PD_code'")) codes.add(getCodeOf(line.substring(line.indexOf(":"))));
        }
        else { // assumes choice == 2
            String alt = "a";
            if (nonAlt(line)) alt = "n";
            String count = String.valueOf(counter);
            while (count.length() < extraZero) count = "0"+count;
            String name = "K"+alt+count;
            names.add(name);
            codes.add(line);
            counter++;
        }
    }
    
    private boolean nonAlt(String line) {
        for (int i = 0; i < line.length(); i++) {
            int x = (int) line.charAt(i);
            if (x>= 65 && x <= 90) return true;
        }
        return false;
    }
    
    private String getCodeOf(String line) {
        int b = line.indexOf("[");
        int e = line.indexOf("]")+1;
        return line.substring(b, e);
    }
    
    private String getNameOf(String line) {
        int b = line.indexOf("'")+1;
        int e = line.lastIndexOf("'");
        return line.substring(b, e);
    }
    
    private void loadLinks() {
        ArrayList<ArrayList<String>>infos = new ArrayList<ArrayList<String>>();
        try {
            for (File file : files) {
                if (abInf.isCancelled()) break;
                String fname = file.getAbsolutePath();
                String line;
                int y = fname.lastIndexOf(options.getSlash());
                frame.setText(fname.substring(y+1));
                try (FileReader fr = new FileReader(file)) {
                    BufferedReader in = new BufferedReader(fr);
                    boolean keepreading = true;
                    ArrayList<String> info = new ArrayList<String>();
                    info.add(fname);
                    while (keepreading) {
                        line = in.readLine();
                        if (line == null) keepreading = false;
                        else info.add(line);
                    }
                    infos.add(info);
                    fr.close();
                }
            }
        }
        catch (IOException io) {
            abInf.cancel();
        }
        if (select) infos = selectLinks(infos);
        for (ArrayList<String> info : infos) {
            if (abInf.isCancelled()) break;
            ArrayList<LinkData> newlinks = newLinkFrom(info);
            for (LinkData newlink : newlinks) theLinks.add(newlink);
        }
        frame.dispose();
    }

    public ArrayList<LinkData> getLinks() {
        return theLinks;
    }
    
    private ArrayList<LinkData> newLinkFrom(ArrayList<String> info) {
        ArrayList<LinkData> theData = new ArrayList<LinkData>();
        String filename = info.get(0);
        info.remove(filename);
        int v = filename.lastIndexOf(".");
        if (v == -1) return theData;
        String ext = filename.substring(v+1);
        if (fromKnotJob(ext)) enterKnotJobData(theData, info, ext);
        else enterOtherData(theData, info, ext);
        return theData;
    }
    
    private boolean fromKnotJob(String ext) {
        return ext.equals("kjb") || ext.equals("tkj") || ext.equals("kts");
    }
    
    private void enterOtherData(ArrayList<LinkData> theData, ArrayList<String> info, String ext) {
        if (ext.equals("gld")) addXKnotJob(theData, info);
        while (!info.isEmpty()) {
            if (abInf.isCancelled()) return;
            String inf = info.get(0);
            info.remove(inf);
            if (ext.equals("dtc")) addDowker(theData, inf);
            if (ext.equals("txt")) addPlanar(theData, inf);
            if (ext.equals("adc")) addAlpha(theData, inf);
            if (ext.equals("brd")) addBraids(theData, inf);
        }
    }
    
    private void addXKnotJob(ArrayList<LinkData> theData, ArrayList<String> info) {
        while (!info.isEmpty()) {
            if (abInf.isCancelled()) return;
            String name = info.get(0);
            String comm = info.get(1);
            String cros = info.get(2);
            String path = info.get(3);
            info.remove(0);
            info.remove(0);
            info.remove(0);
            info.remove(0);
            LinkData theNewData = new LinkData(name,comparer);
            frame.setText(name);
            if (comm.length()>0) theNewData.comment = comm;
            Link link = diagramCreator(cros,path,0,null).girthMinimize();
            if ((link != null && !linkContainsZero(link))) {
                theNewData.links.add(link);
                theData.add(theNewData);
            }
        }
    }
    
    private void addAlpha(ArrayList<LinkData> theData, String info) {
        LinkData theLink = LinkCreator.enterADTCode(info, info, true, knob, comparer);
        if (theLink != null) {
            frame.setText(theLink.name);
            theData.add(theLink);
        }
    }
    
    private void addPlanar(ArrayList<LinkData> theData, String info) {
        LinkData theLink = LinkCreator.enterPDCode(info, "Knot "+counter, true, knob, comparer);
        if (theLink != null) {
            if (theLink.chosenLink().components()>1) theLink.name = "Link "+counter;
            if (info.contains(" = ")) theLink.name = info.substring(0, info.indexOf(" = "));
            counter++;
            frame.setText(theLink.name);
            theData.add(theLink);
        }
    }
    
    private void addBraids(ArrayList<LinkData> theData, String info) {
        LinkData theLink = LinkCreator.enterBraidCode(info, info, knob, true, comparer);
        if (theLink != null) {
            frame.setText(theLink.name);
            theData.add(theLink);
        }
    }
    
    private void addDowker(ArrayList<LinkData> theData, String info) {
        int a = info.indexOf(' ');
        int cn = Integer.parseInt(info.substring(0, a));
        info = info.substring(a);
        while (info.charAt(0) == ' ') info = info.substring(1);
        a = info.indexOf(' ');
        int number = Integer.parseInt(info.substring(0,a));
        info = info.substring(a);
        while (info.charAt(0) == ' ') info = info.substring(1);
        String name = getDTName(cn,number,info);
        LinkData theLink = LinkCreator.enterDTCode(info, name, true, knob, comparer);
        if (theLink != null) {
            frame.setText(theLink.name);
            theData.add(theLink);
        }
    }
    
    private String getDTName(int cn, int number, String info) {
        boolean alt = info.indexOf('-') == -1;
        int length = lengthOf(cn,alt);
        String al = "n";
        if (alt) al = "a";
        String rest = String.valueOf(number);
        while (rest.length() < length) rest = "0"+rest;
        String name = cn+al+rest;
        return name;
    }
    
    private int lengthOf(int cn, boolean alt) {
        if (cn > 16) return 1;
        int[] alts = {1,1,1,1,1,1,1,1,2,2,3,3,4,4,5,5,6};
        int[] nons = {1,1,1,1,1,1,1,1,1,1,2,3,3,4,5,6,7};
        if (alt) return alts[cn];
        return nons[cn];
    }
    
    private void enterKnotJobData(ArrayList<LinkData> theData, ArrayList<String> info, String ext) {
        StringData currentData = null;
        while (!info.isEmpty()) {
            if (abInf.isCancelled()) return;
            String inf = info.get(0);
            info.remove(inf);
            if ("0:".equals(inf.substring(0,2))) {
                if (currentData != null) finishOff(currentData, theData, ext);
                currentData = new StringData(inf.substring(2));
                currentData.createLists();
                //currentData.name = inf.substring(2);
                frame.setText(currentData.name);
            }
            if (currentData != null) {
                int ch = check(inf);
                switch(ch) {
                    case 1  : currentData.comment = inf.substring(2);break;
                    case 2  : currentData.crossings.add(inf.substring(2));break;
                    case 3  : currentData.paths.add(inf.substring(2));break;
                    case 4  : currentData.unredKhovHom.add(inf.substring(2));break;
                    case 5  : currentData.redKhovHom.add(inf.substring(2));break;
                    case 6  : addSix(currentData,inf.substring(2),ext);break;
                    case 7  : currentData.sqEven = inf.substring(2);break;
                    case 8  : currentData.sinvariant = inf.substring(2);break;
                    case 9  : currentData.orientations.add(inf.substring(2));break;
                    case 10 : currentData.oddKhovHom.add(inf.substring(3));break;
                    case 11 : currentData.okhovInfo.add(inf.substring(3));break;
                    case 12 : currentData.sqOdd = inf.substring(3);break;
                    case 13 : currentData.jones = inf.substring(3);break;
                    case 14 : currentData.alex = inf.substring(3);break;
                    case 15: currentData.signature = inf.substring(3);break;
                    case 16: currentData.determinant = inf.substring(3);break;
                    case 17: currentData.grsinv = inf.substring(3);break;
                    case 18: currentData.unredBLT.add(inf.substring(3));break;
                    case 19: currentData.redBLT.add(inf.substring(3));break;
                    case 21: currentData.stEven = inf.substring(3);break;
                    case 22: currentData.stEvenInfo.add(inf.substring(3));break;
                    case 23: currentData.stOdd = inf.substring(3);break;
                    case 24 : currentData.stOddInfo.add(inf.substring(3));break;
                    case 25 : currentData.stOde = inf.substring(3);break;
                    case 26 : currentData.stOdeInfo.add(inf.substring(3));break;
                    case 27 : currentData.sqtEven = inf.substring(3);break;
                    case 28 : currentData.sqtOdd = inf.substring(3);break;
                    case 29 : currentData.sqtOde = inf.substring(3);break;
                    case 30 : currentData.bsOdd = inf.substring(3);break;
                    case 31 : currentData.beta = inf.substring(3);break;
                    case 32 : currentData.sltsinvariant = inf.substring(3);break;
                    case 33 : currentData.cmpinv = inf.substring(3);break;
                    case 34 : currentData.sltInfo.add(inf.substring(3));break;
                    case 35 : currentData.unredSlT.add(inf.substring(3));break;
                    case 36 : currentData.redSlT.add(inf.substring(3));break;
                    case 37 : currentData.sltTypeOne.add(inf.substring(3));break;
                    case 38 : currentData.sltTypeOneRed.add(inf.substring(3));break;
                    case 39 : currentData.sltTypeTwo.add(inf.substring(3));break;
                    case 40 : currentData.sltTypeThree.add(inf.substring(3));break;
                    case 41 : currentData.sltrinvariant = inf.substring(3);break;
                    case 42 : currentData.sltTypeX.add(inf.substring(3));break;
                    case 43 : currentData.sltTypeXSq.add(inf.substring(3));break;
                }// */
                /*if (checker("1:", inf)) currentData.comment = inf.substring(2);
                if (checker("2:", inf)) currentData.crossings.add(inf.substring(2));
                if (checker("3:", inf)) currentData.paths.add(inf.substring(2));
                if (checker("4:", inf)) currentData.unredKhovHom.add(inf.substring(2));
                if (checker("5:", inf)) currentData.redKhovHom.add(inf.substring(2));
                if (checker("6:", inf)) addSix(currentData,inf.substring(2),ext);
                if (checker("7:", inf)) currentData.sqEven = inf.substring(2);
                if (checker("8:", inf)) currentData.sinvariant = inf.substring(2);
                if (checker("9:", inf)) currentData.orientations.add(inf.substring(2));
                if (checker("10:", inf)) currentData.oddKhovHom.add(inf.substring(3));
                if (checker("11:", inf)) currentData.okhovInfo.add(inf.substring(3));
                if (checker("12:", inf)) currentData.sqOdd = inf.substring(3);
                if (checker("13:", inf)) currentData.jones = inf.substring(3);
                if (checker("14:", inf)) currentData.alex = inf.substring(3);
                if (checker("15:", inf)) currentData.signature = inf.substring(3);
                if (checker("16:", inf)) currentData.determinant = inf.substring(3);
                if (checker("17:", inf)) currentData.grsinv = inf.substring(3);
                if (checker("18:", inf)) currentData.unredBLT.add(inf.substring(3));
                if (checker("19:", inf)) currentData.redBLT.add(inf.substring(3));
                if (checker("21:", inf)) currentData.stEven = inf.substring(3);
                if (checker("22:", inf)) currentData.stEvenInfo.add(inf.substring(3));
                if (checker("23:", inf)) currentData.stOdd = inf.substring(3);
                if (checker("24:", inf)) currentData.stOddInfo.add(inf.substring(3));
                if (checker("25:", inf)) currentData.stOde = inf.substring(3);
                if (checker("26:", inf)) currentData.stOdeInfo.add(inf.substring(3));
                if (checker("27:", inf)) currentData.sqtEven = inf.substring(3);
                if (checker("28:", inf)) currentData.sqtOdd = inf.substring(3);
                if (checker("29:", inf)) currentData.sqtOde = inf.substring(3);
                if (checker("30:", inf)) currentData.bsOdd = inf.substring(3);
                if (checker("31:", inf)) currentData.beta = inf.substring(3);
                if (checker("32:", inf)) currentData.sltsinvariant = inf.substring(3);
                if (checker("33:", inf)) currentData.cmpinv = inf.substring(3);
                if (checker("34:", inf)) currentData.sltInfo.add(inf.substring(3));
                if (checker("35:", inf)) currentData.unredSlT.add(inf.substring(3));
                if (checker("36:", inf)) currentData.redSlT.add(inf.substring(3));
                if (checker("37:", inf)) currentData.sltTypeOne.add(inf.substring(3));
                if (checker("38:", inf)) currentData.sltTypeOneRed.add(inf.substring(3));
                if (checker("39:", inf)) currentData.sltTypeTwo.add(inf.substring(3));
                if (checker("40:", inf)) currentData.sltTypeThree.add(inf.substring(3));
                if (checker("41:", inf)) currentData.sltrinvariant = inf.substring(3);// */
            }
        }
        if (currentData != null) finishOff(currentData, theData, ext);
    }
    
    private int check(String start) {
        int x = start.indexOf(":");
        return Integer.parseInt(start.substring(0, x));
    }
    
    private boolean checker(String start, String inf) {
        if (inf.length() < start.length()) return false;
        return start.equals(inf.substring(0, start.length()));
    }
    
    private void addSix(StringData theLink, String info, String ext) {
        if ("kts".equals(ext)) {
            int a = info.indexOf('.');
            int b = info.lastIndexOf('.');
            String fs = info.substring(0,a);
            String ss = info.substring(a+1,b);
            String ts = info.substring(b+1);
            String rel = "";
            if (!"null".equals(fs)) rel = "0:"+fs;
            if (!"null".equals(ss)) {
                if ("".equals(rel)) rel = "2:"+ss;
                else rel = rel+",2:"+ss;
            }
            if (!"null".equals(ts)) {
                if ("".equals(rel)) rel = "3:"+ts;
                else rel = rel+",3:"+ts;
            }
            theLink.sinvariant = rel;
        }
        else theLink.khovInfo.add(info);
    }
    
    private void finishOff(StringData theLink, ArrayList<LinkData> theData, String ext) {
        if (theLink.crossings.size()!=theLink.paths.size()) return;
        LinkData theNewData = new LinkData(theLink.name, comparer);
        theNewData.comment = theLink.comment;
        boolean okay = true;
        for (int i = 0; i < theLink.crossings.size(); i++) {
            String crossings = theLink.crossings.get(i);
            String paths = theLink.paths.get(i);
            String orien = null;
            int comp = 0;
            if ("kjb".equals(ext)) {
                int a = crossings.indexOf(',');
                if (a >= 0) {
                    comp = Integer.parseInt(crossings.substring(0, a));
                    crossings = crossings.substring(a+1);
                }
                else {
                    comp = Integer.parseInt(crossings);
                    crossings = "";
                }
                orien = theLink.orientations.get(i);
            }
            Link link = diagramCreator(crossings,paths,comp,orien);
            if (link == null || linkContainsZero(link)) okay = false;
            else theNewData.links.add(link);
        }
        if (okay) {
            theNewData.setSInvariant(theLink.sinvariant);
            theNewData.setSLtSInvariant(theLink.sltsinvariant, false);
            theNewData.setSLtSInvariant(theLink.sltrinvariant, true);
            theNewData.sqEven = theLink.sqEven;
            theNewData.sqOdd = theLink.sqOdd;
            theNewData.bsOdd = theLink.bsOdd;
            theNewData.beta = theLink.beta;
            theNewData.cmpinv = theLink.cmpinv;
            theNewData.sqtEven = theLink.sqtEven;
            theNewData.sqtOdd = theLink.sqtOdd;
            theNewData.sqtOde = theLink.sqtOde;
            theNewData.stEven = theLink.stEven;
            theNewData.stOdd = theLink.stOdd;
            theNewData.stOde = theLink.stOde;
            theNewData.jones = theLink.jones;
            theNewData.alex = theLink.alex;
            theNewData.signature = theLink.signature;
            theNewData.determinant = theLink.determinant;
            theNewData.grsinv = theLink.grsinv;
            //theNewData.totalKhov = theLink.totalKhov;
            if (!theLink.unredKhovHom.isEmpty()) theNewData.unredKhovHom = theLink.unredKhovHom;
            if (!theLink.redKhovHom.isEmpty()) theNewData.redKhovHom = theLink.redKhovHom;
            if (!theLink.oddKhovHom.isEmpty()) theNewData.oddKhovHom = theLink.oddKhovHom;
            if (!theLink.okhovInfo.isEmpty()) theNewData.okhovInfo = theLink.okhovInfo;
            if (!theLink.stEvenInfo.isEmpty()) theNewData.stEvenInfo = theLink.stEvenInfo;
            if (!theLink.stOddInfo.isEmpty()) theNewData.stOddInfo = theLink.stOddInfo;
            if (!theLink.stOdeInfo.isEmpty()) theNewData.stOdeInfo = theLink.stOdeInfo;
            if (!theLink.unredBLT.isEmpty()) theNewData.unredBLT = theLink.unredBLT;
            if (!theLink.redBLT.isEmpty()) theNewData.redBLT = theLink.redBLT;
            if (!theLink.sltInfo.isEmpty()) theNewData.sltInfo = theLink.sltInfo;
            if (!theLink.unredSlT.isEmpty()) theNewData.unredSlT = theLink.unredSlT;
            if (!theLink.redSlT.isEmpty()) theNewData.redSlT = theLink.redSlT;
            if (!theLink.sltTypeOne.isEmpty()) theNewData.sltTypeOne = theLink.sltTypeOne;
            if (!theLink.sltTypeOneRed.isEmpty()) theNewData.sltTypeOneRed = theLink.sltTypeOneRed;
            if (!theLink.sltTypeTwo.isEmpty()) theNewData.sltTypeTwo = theLink.sltTypeTwo;
            if (!theLink.sltTypeThree.isEmpty()) theNewData.sltTypeThree = theLink.sltTypeThree;
            if (!theLink.sltTypeX.isEmpty()) theNewData.sltTypeX = theLink.sltTypeX;
            if (!theLink.sltTypeXSq.isEmpty()) theNewData.sltTypeXSq = theLink.sltTypeXSq;
            if ("kts".equals(ext)) {
                if (!theLink.unredKhovHom.isEmpty() || !theLink.redKhovHom.isEmpty()) 
                    theNewData.khovInfo = new ArrayList<String>();
                if (!theLink.unredKhovHom.isEmpty()) theNewData.khovInfo.add("u0.0-"+theLink.unredKhovHom.size());
                if (!theLink.redKhovHom.isEmpty()) theNewData.khovInfo.add("r0.0c.0-"+theLink.redKhovHom.size());
            }
            else {
                if ("tkj".equals(ext)) {
                    int[][] rel = relevantInfo(theLink);
                    if (rel.length > 0) theNewData.khovInfo = new ArrayList<String>();
                    for (int u = 0; u < rel.length; u++) {
                        theNewData.khovInfo.add("u"+rel[u][0]+"."+rel[u][1]+"-"+rel[u][2]);
                        theNewData.khovInfo.add("r"+rel[u][0]+".0c."+rel[u][3]+"-"+rel[u][4]);
                    }
                }
                else theNewData.khovInfo = theLink.khovInfo;
            }
            theData.add(theNewData);
        }
    }
    
    private int[][] relevantInfo(StringData theLink) {
        int c = theLink.khovInfo.size();
        int[][] rel = new int[c][5];
        if (c == 0) return rel;
        for (int i = 0; i < c; i++) {
            String info = theLink.khovInfo.get(i);
            int a = info.indexOf('.');
            int b = info.lastIndexOf('.');
            rel[i][0] = Integer.parseInt(info.substring(1,a));
            rel[i][1] = Integer.parseInt(info.substring(a+1,b));
            rel[i][3] = Integer.parseInt(info.substring(b+1));
            if (i>0) {
                rel[i-1][2] = rel[i][1];
                rel[i-1][4] = rel[i][3];
            }
        }
        rel[c-1][2] = theLink.unredKhovHom.size();
        rel[c-1][4] = theLink.redKhovHom.size();
        return rel;
    }
    
    private Link diagramCreator(String arg1, String arg2, int comp, String orien) {
        Link link = null;
        int fool = 0;
        int [] crossings;
        int [][] paths;
        arg1 = arg1+",";
        int k = arg1.length();
        ArrayList<Integer> commas = new ArrayList<Integer>();
        int j = 1;
        while (j < k) {
            if (arg1.charAt(j) == ',') commas.add(j);
            j++;
        }
        crossings = new int [commas.size()];
        try {
            int pos = 0;
            for (int i = 0 ; i < commas.size(); i++) {
                    crossings[i] = Integer.parseInt(arg1.substring(pos,commas.get(i)));
                    pos = commas.get(i)+1;
            }
        }
        catch (NumberFormatException e) {
            fool = 4;
        }
        arg2 = arg2+",";
        k = arg2.length();
        commas.clear();
        j = 1;
        while (j < k) {
            if (arg2.charAt(j) == ',') commas.add(j);
            j++;
        }
        paths = new int [crossings.length][4];
        if (commas.size() == 4 * crossings.length) {
            try {
                int pos = 0;
                for (int i = 0; i < commas.size(); i++) {
                    paths[i/4][i%4] = Integer.parseInt(arg2.substring(pos,commas.get(i)));
                    pos = commas.get(i)+1;
                }
            }
            catch (NumberFormatException e) {
                fool = 5;
            }
        }
        else fool = 5;
        if (fool == 0) link = new Link(crossings,paths,comp,orien);
        return link;
    }

    private boolean linkContainsZero(Link link) {
        boolean noGood = false;
        int i = 0;
        while (!noGood && i < link.crossingLength()) {
            if (link.getCross(i) == 0) noGood = true;
            i++;
        }
        return noGood;
    }

    private ArrayList<ArrayList<String>> selectLinks(ArrayList<ArrayList<String>> infos) {
        ArrayList<ArrayList<String>> selectInfos = new ArrayList<ArrayList<String>>();
        for (ArrayList<String> info : infos) selectInfos.add(new ArrayList<String>());
        ArrayList<String> names = new ArrayList<String>();
        int ex = 2;
        for (ArrayList<String> info : infos) {
            String filename = info.get(0);
            int v = filename.lastIndexOf(".");
            if (v >= 0) {
                String ext = filename.substring(v+1);
                if (fromKnotJob(ext)) {
                    ex = 0;
                    for (String inf : info) if (inf.startsWith("0:")) names.add(inf);
                }
                else {
                    if ("txt".equals(ext)) ex = 1;
                    for (int i = 1; i < info.size(); i++) names.add(info.get(i));
                }
            }
        }
        OpenSelectDialog dialog = new OpenSelectDialog(knob, names, ex);
        dialog.setUp();
        if (dialog.isOkay()) {
            ArrayList<String> theNames = dialog.getNames();
            if (ex == 0) modifyKnotJobFiles(theNames, selectInfos, infos);
            else modifyOtherFiles(theNames, selectInfos, infos);
        }
        if (!dialog.isOkay()) return new ArrayList<ArrayList<String>>();
        return selectInfos;
    }

    private void modifyKnotJobFiles(ArrayList<String> theNames, ArrayList<ArrayList<String>> selectInfos, ArrayList<ArrayList<String>> infos) {
        for (int i = 0; i < infos.size(); i++) {
            selectInfos.get(i).add(infos.get(i).get(0));
            int j = 1;
            while (j < infos.get(i).size()) {
                String info = infos.get(i).get(j);
                j++;
                if (theNames.contains(info)) {
                    selectInfos.get(i).add(info);
                    boolean keepgoing = true;
                    while (keepgoing && j < infos.get(i).size()) {
                        info = infos.get(i).get(j);
                        if (!info.startsWith("0:")) {
                            selectInfos.get(i).add(info);
                            j++;
                        }
                        else keepgoing = false;
                    }
                }
            }
        }
    }

    private void modifyOtherFiles(ArrayList<String> theNames, ArrayList<ArrayList<String>> selectInfos, ArrayList<ArrayList<String>> infos) {
        for (int i = 0; i < infos.size(); i++) {
            selectInfos.get(i).add(infos.get(i).get(0));
            for (String info : infos.get(i)) {
                if (theNames.contains(info)) selectInfos.get(i).add(info);
            }
        }
    }
}
