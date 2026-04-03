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

package knotjob.links;

import java.util.ArrayList;

/**
 *
 * @author Dirk
 */
public class StringData {
    
    public String name;
    public String comment;
    public String sinvariant;
    public String sltsinvariant;
    public String sltrinvariant;
    public String sqEven;
    public String sqOdd;
    public String bsOdd;
    public String beta;
    public String cmpinv;
    public String sqtEven;
    public String sqtOdd;
    public String sqtOde;
    public String stEven;
    public String stOdd;
    public String stOde;
    public String grsinv;
    public String jones;
    public String alex;
    public String determinant;
    public String signature;
    //public String totalKhov;
    public ArrayList<String> crossings;
    public ArrayList<String> paths;
    public ArrayList<String> orientations;
    public ArrayList<String> unredKhovHom;
    public ArrayList<String> redKhovHom;
    public ArrayList<String> khovInfo;
    public ArrayList<String> oddKhovHom;
    public ArrayList<String> okhovInfo;
    public ArrayList<String> stEvenInfo;
    public ArrayList<String> stOddInfo;
    public ArrayList<String> stOdeInfo;
    public ArrayList<String> unredBLT;
    public ArrayList<String> redBLT;
    public ArrayList<String> unredSlT;
    public ArrayList<String> redSlT;
    public ArrayList<String> sltInfo;
    public ArrayList<String> sltTypeThree;
    public ArrayList<String> sltTypeTwo;
    public ArrayList<String> sltTypeOne;
    public ArrayList<String> sltTypeOneRed;
    public ArrayList<String> sltTypeX;
    public ArrayList<String> sltTypeXSq;
    
    public StringData(String title) {
        name = title;
        stuffToNull();
    }
    
    private void stuffToNull() {
        comment = null;
        sinvariant = null;
        sltsinvariant = null;
        sltrinvariant = null;
        sqEven = null;
        sqOdd = null;
        bsOdd = null;
        beta = null;
        cmpinv = null;
        sqtEven = null;
        sqtOdd = null;
        sqtOde = null;
        stEven = null;
        stOdd = null;
        stOde = null;
        grsinv = null;
        jones = null;
        alex = null;
        determinant = null;
        signature = null;
        //totalKhov = null;
        unredKhovHom = null;
        redKhovHom = null;
        khovInfo = null;
        oddKhovHom = null;
        okhovInfo = null;
        unredBLT = null;
        redBLT = null;
        unredSlT = null;
        redSlT = null;
        sltTypeThree = null;
        sltTypeTwo = null;
        sltTypeOne = null;
        sltTypeOneRed = null;
        sltTypeX = null;
        stEvenInfo = null;
        stOddInfo = null;
        stOdeInfo = null;
    }

    public void createLists() {
        crossings = new ArrayList<String>();
        paths = new ArrayList<String>();
        orientations = new ArrayList<String>();
        unredKhovHom = new ArrayList<String>();
        redKhovHom = new ArrayList<String>();
        oddKhovHom = new ArrayList<String>();
        khovInfo = new ArrayList<String>();
        okhovInfo = new ArrayList<String>();
        stEvenInfo = new ArrayList<String>();
        stOddInfo = new ArrayList<String>();
        stOdeInfo = new ArrayList<String>();
        unredBLT = new ArrayList<String>();
        redBLT = new ArrayList<String>();
        unredSlT = new ArrayList<String>();
        redSlT = new ArrayList<String>();
        sltInfo = new ArrayList<String>();
        sltTypeThree = new ArrayList<String>();
        sltTypeTwo = new ArrayList<String>();
        sltTypeOne = new ArrayList<String>();
        sltTypeOneRed = new ArrayList<String>();
        sltTypeX = new ArrayList<String>();
        sltTypeXSq = new ArrayList<String>();
    }
    
}
