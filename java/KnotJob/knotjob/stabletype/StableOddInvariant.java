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

package knotjob.stabletype;

import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.oddkhov.OddStableComplex;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class StableOddInvariant<R extends Ring<R>> {
    
    private final Link theLink;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final R unit;
    private final R unitTwo;
    private final R unitFour;
    private final R epsilon;
    private final int[] girth;
    private final boolean highDetail;
    private final ArrayList<Integer> relQs;
    private final ArrayList<Integer> qMins;
    private final ArrayList<Integer> qMaxs;
    private final String oldString;
    private final ArrayList<String> infoStrings;
    private String newString;
    
    public StableOddInvariant(LinkData link, R unt, R untt, R untf, DialogWrap frm, Options optns, 
            ArrayList<Integer> qs, int deleps) {
        theLink = Reidemeister.freeOfOne(link.chosenLink()).breakUp().girthDiscMinimize();
        frame = frm;
        unit = unt;
        unitTwo = untt;
        unitFour = untf;
        girth = theLink.totalGirthArray();
        abInf = frm.getAbortInfo();
        highDetail = false; // no high detail optns.getGirthInfo() == 2;
        relQs = new ArrayList<Integer>();
        qMins = new ArrayList<Integer>();
        qMaxs = new ArrayList<Integer>();
        for (int i = 0; i < qs.size()/3; i++) {
            relQs.add(qs.get(3*i));
            qMins.add(qs.get(3*i+1));
            qMaxs.add(qs.get(3*i+2));
        }
        if (deleps == 2) epsilon = untt;
        else epsilon = untt.getZero();
        ArrayList<String> oldies = link.stOddInfo;
        if (deleps == 2) {
            oldString = link.stOde;
            oldies = link.stOdeInfo;
        }
        else oldString = link.stOdd;
        infoStrings = new ArrayList<String>();
        if (oldString != null) for (String st : oldies) infoStrings.add(st);
    }
    
    public void calculate() {
        int[] wrt = theLink.crossingSigns();
        int hstart = -wrt[1];
        int qstart = wrt[0]+2*hstart;
        calculateStableType(hstart, qstart);
    }
    
    public String getInfo() {
        return newString;
    }
    
    public ArrayList<String> getInfoStrings() {
        return infoStrings;
    }

    private void calculateStableType(int hstart, int qstart) {
        OddStableComplex<R> theComplex = getComplex(hstart, qstart);
        if (!abInf.isAborted()) obtainStableTypes(theComplex);
    }
    
    private OddStableComplex<R> getComplex(int hstart, int qstart) {
        OddStableComplex<R> theComplex = firstComplex(hstart, qstart);
        theComplex.setClosure(theLink);
        int u = 1;
        while (u < theLink.crossingLength() && !abInf.isAborted()) {
            boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
                theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
            OddStableComplex<R> nextComplex = new OddStableComplex<R>(theLink.getCross(u), theLink.getPath(u), 0,
                    0, orient, unit, null, null, false, relQs);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            theComplex.modifyComplex(nextComplex, girthInfo(u), highDetail);
            u++;
            if (u == theLink.crossingLength()-1) theComplex.last = true;
        }
        return theComplex;
    }
    
    private OddStableComplex<R> firstComplex(int hstart, int qstart) {
        OddStableComplex<R> theComplex = new OddStableComplex<R>(theLink.getCross(0), theLink.getPath(0), hstart,
                qstart, false, unit, frame, abInf, false, relQs);
        if (theComplex.posNumber() == 2) return theComplex;
        OddStableComplex<R> unComp = new OddStableComplex<R>(1, unit, abInf, frame, false);
        unComp.modifyComplex(theComplex," ",highDetail);
        return unComp;
    }
    
    private String girthInfo(int u) {
        String info = String.valueOf(girth[u]);
        if (!highDetail) return info;
        if (u < girth.length - 1) info = info+" ("+girth[u+1];
        else return info;
        for (int i = 1; i < 3; i++) {
            if (u < girth.length - i - 1) info = info+", "+girth[u+1+i];
        }
        info = info+")";
        return info;
    }
    
    private void obtainStableTypes(OddStableComplex<R> theComplex) {
        frame.setLabelLeft("q-degree : ", 0, false);
        frame.setLabelLeft("h-degree : ", 1, false);
        String info = "";
        if (oldString != null) info = adaptInfo();
        for (int i = 0; i < relQs.size(); i++) {
            frame.setLabelRight(""+relQs.get(i), 0, false);
            OneFlowCategory<R> cat = new OneFlowCategory<R>(frame, abInf, unit, unitTwo, unitFour);
            OddCatFiller<R> filler = new OddCatFiller<R>(cat, relQs.get(i), qMins.get(i)-100,
                    qMaxs.get(i)+100, frame, abInf, epsilon);
            filler.fill(theComplex);
            //cat.cubicalFlowCheck(); // for debugging purposes
            cat.normalize();
            if (abInf.isCancelled()) break;
            if (!abInf.isAborted()) {
               // cat.output();
                //cat.troubleChecker(qMins.get(i), qMaxs.get(i));
                cat.removeGenerators(qMins.get(i), qMaxs.get(i));
                //System.out.println("q = "+relQs.get(i));
                //cat.output();
                cat.changify(0);
                info = info+"q="+relQs.get(i)+":"+infoStrings.size()+";";
                ArrayList<String> data = cat.getFinalInfo();
                for (String str : data) infoStrings.add(str);
            }
            else {
                info = info+"q="+relQs.get(i)+":aborted;";
                abInf.deAbort();
            }
        }
        newString = info;
    }
    
    private String adaptInfo() {
        String string = oldString;
        String info = "";
        while (string.length()>0) {
            int a = string.indexOf(";");
            String help = string.substring(0, a+1);
            if (!help.contains("aborted")) {
                info = info + help;
                int b = help.indexOf("=");
                int c = help.indexOf(":");
                int q = Integer.parseInt(help.substring(b+1, c));
                int p = relQs.indexOf(q);
                relQs.remove(p);
                qMaxs.remove(p);
                qMins.remove(p);
            }
            string = string.substring(a+1);
        }
        return info;
    }
    
}
