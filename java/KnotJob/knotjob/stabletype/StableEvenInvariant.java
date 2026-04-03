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
import knotjob.homology.evenkhov.EvenStableComplex;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.links.Reidemeister;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class StableEvenInvariant<R extends Ring<R>> {
    
    private final Link theLink;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final R unit;
    private final R unitTwo;
    private final R unitFour;
    private final int[] girth;
    private final boolean highDetail;
    private final ArrayList<Integer> relQs;
    private final ArrayList<Integer> hMins;
    private final ArrayList<Integer> hMaxs;
    private final String oldString;
    private final ArrayList<String> infoStrings;
    private String newString;
    //private int counter = 0;
    
    public StableEvenInvariant(LinkData link, R unt, R untt, R untf, DialogWrap frm, Options optns, 
            ArrayList<Integer> qs) {
        theLink = Reidemeister.freeOfOne(link.chosenLink());
        //theLink = Reidemeister.freeOfOne(link.chosenLink()).breakUp().girthDiscMinimize();
        frame = frm;
        unit = unt;
        unitTwo = untt;
        unitFour = untf;
        girth = theLink.totalGirthArray();
        abInf = frm.getAbortInfo();
        highDetail = false;// no high detail optns.getGirthInfo() == 2;
        relQs = new ArrayList<Integer>();
        hMins = new ArrayList<Integer>();
        hMaxs = new ArrayList<Integer>();
        for (int i = 0; i < qs.size()/3; i++) {
            relQs.add(qs.get(3*i));
            hMins.add(qs.get(3*i+1));
            hMaxs.add(qs.get(3*i+2));
        }
        oldString = link.stEven;
        infoStrings = new ArrayList<String>();
        if (oldString != null) for (String st : link.stEvenInfo) infoStrings.add(st);
    }
    
    public String getInfo() {
        return newString;
    }
    
    public ArrayList<String> getInfoStrings() {
        return infoStrings;
    }
    
    public void calculate() {
        int[] wrt = theLink.crossingSigns();
        int hstart = -wrt[1];
        int qstart = wrt[0]+2*hstart;
        calculateStableType(hstart, qstart);
    }

    private void calculateStableType(int hstart, int qstart) {
        EvenStableComplex<R> theComplex = getComplex(hstart, qstart);
        if (!abInf.isAborted()) obtainStableTypes(theComplex);
    }
    
    private EvenStableComplex<R> getComplex(int hstart, int qstart) {
        EvenStableComplex<R> theComplex = firstComplex(hstart, qstart);
        int u = 1;
        while (u < theLink.crossingLength() && !abInf.isAborted()) {
            boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
                theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
            EvenStableComplex<R> nextComplex = new EvenStableComplex<R>(theLink.getCross(u),theLink.getPath(u),0,0,orient,false,true,
                    false, unit, null, null, relQs);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0, false);
            theComplex.modifyStableComplex(nextComplex, 0, girthInfo(u), highDetail);
            u++;
            if (u == theLink.crossingLength()-1) theComplex.last = true;
        }
        return theComplex;
    }
    
    private EvenStableComplex<R> firstComplex(int hs, int qs) {
        EvenStableComplex<R> theComplex = new EvenStableComplex<R>(theLink.getCross(0), 
                theLink.getPath(0), hs, qs, false, false, true, false, unit, frame, abInf, relQs);
        if (theComplex.posNumber() == 2) return theComplex;
        EvenStableComplex<R> unComp = new EvenStableComplex<R>(1, unit, false, true, abInf, frame);
        unComp.modifyComplex(theComplex, 0, " ", highDetail);
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
    
    private void obtainStableTypes(EvenStableComplex<R> theComplex) {
        frame.setLabelLeft("q-degree : ", 0, false);
        frame.setLabelLeft("h-degree : ", 1, false);
        String info = "";
        if (oldString != null) info = adaptInfo();
        for (int i = 0; i < relQs.size(); i++) {
            frame.setLabelRight(""+relQs.get(i), 0, false);
            //counter = 0;
            OneFlowCategory<R> cat = new OneFlowCategory<R>(frame, abInf, unit, unitTwo, unitFour);
            //theComplex.output();
            boolean sock = sockComplex();
            EvenCatFiller<R> filler = new EvenCatFiller<R>(cat, relQs.get(i), hMins.get(i)-100,
                    hMaxs.get(i)+100, frame, abInf, sock, theLink);
            filler.fill(theComplex);
            
            //fillCategory(cat, theComplex, relQs.get(i), hMins.get(i)-100, hMaxs.get(i)+100, sock);
            if (sock) cat.whitneyfy();
            //if (sock) cat.sockFlowCheck(); // will only work for double crossings <= 2
            //if (!sock) cat.cubicalFlowCheck(); // can be used for debugging
            cat.normalize();
            if (abInf.isCancelled()) break;
            if (!abInf.isAborted()) {
                //cat.troubleChecker(hMins.get(i), hMaxs.get(i)); // for debugging purposes
                cat.removeGenerators(hMins.get(i), hMaxs.get(i));
                //System.out.println("q = "+relQs.get(i));
                //cat.output();
                //System.out.println("After Changification");
                cat.changify(0);
                info = info+"q="+relQs.get(i)+":"+infoStrings.size()+";";
                ArrayList<String> data = cat.getFinalInfo();
                for (String str : data) infoStrings.add(str);
                //cat.output();
            }
            else {
                info = info+"q="+relQs.get(i)+":aborted;";
                abInf.deAbort();
            }// */
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
                hMaxs.remove(p);
                hMins.remove(p);
            }
            string = string.substring(a+1);
        }
        return info;
    }
    
    private boolean sockComplex() {
        boolean cube = true;
        int i = 0;
        while (i < theLink.crossingLength() && cube) {
            if (theLink.getCross(i) != 1 && theLink.getCross(i) != -1) cube = false;
            else i++;
        }
        return !cube;
    }
    
}
