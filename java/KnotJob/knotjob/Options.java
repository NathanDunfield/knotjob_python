/*

Copyright (C) 2019-24 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 *
 * @author Dirk
 */
public class Options {
    private File loadLinksFrom;
    private File saveKhovanov;
    private File saveImage;
    private String slashSymbol;
    private boolean gridDiagram;
    private boolean girthTot;
    private boolean boundGirth;
    private int girthBound;
    private int combAttempts;
    private int stableBound;
    private boolean girthRandom;
    private boolean redKhov;
    private boolean unrKhov;
    private boolean primary;
    private boolean combine;
    private int detailedGirth;
    private boolean showTime;
    private double divFactor;
    private boolean changeNumbers;
    private int maxSaveCount;
    private final ArrayList<Integer> primes;
    private final ArrayList<Integer> powers;
    protected Image img;
    protected Image medImg;
    private final Color versionColor;
    private final int opSys; // Windows : 1;  Mac : 2; else : 0
    
    public Options() { 
        float[] hsb = Color.RGBtoHSB(127, 255, 0, null); // 7FFF00 Chartreuse
        versionColor = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        girthTot = true;
        boundGirth = true;
        girthRandom = true;
        redKhov = true;
        unrKhov = true;
        primary = true;
        changeNumbers = false;
        detailedGirth = 0;
        maxSaveCount = 1000;
        girthBound = 5000;
        combAttempts = 5000;
        stableBound = 15;
        divFactor = 6.0;
        loadLinksFrom = null;
        saveKhovanov = null;
        saveImage = null;
        slashSymbol = "/";
        gridDiagram = false;
        showTime = true;
        String fileLabel = "knotjob_med.png";
        String medLabel = fileLabel;
        String system = System.getProperty("os.name").toLowerCase();
        int os = 0;
        if (system.contains("windows")) os = 1;
        if (system.contains("mac")) os = 2;
        opSys = os;
        if (opSys == 1) { 
            slashSymbol = "\\";
            fileLabel = "knotjob.png";
        }
        else slashSymbol = "/";
        try {
            img = Toolkit.getDefaultToolkit().getImage(fileLabel);
            medImg = Toolkit.getDefaultToolkit().getImage(medLabel);
        }
        catch (NullPointerException e) {
            img = null;
            medImg = null;
        }
        int[] primesOfInterest = {2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97,101,103,107,109,113,127,
            131,137,139,149,151,157,163,167,173,179,181,191,193,197,199,211};
        primes = new ArrayList<Integer>();
        for (int p : primesOfInterest) primes.add(p);
        powers = getPowers(primes);
    }

    private ArrayList<Integer> getPowers(ArrayList<Integer> primes) {
        ArrayList<Integer> pwrs = new ArrayList<Integer>();
        for (int p : primes) {
            int pton = p;
            BigInteger bigpton = BigInteger.valueOf(p);
            int counter = 0;
            while (bigpton.equals(BigInteger.valueOf(pton))) {
                counter++;
                bigpton = bigpton.multiply(BigInteger.valueOf(p));
                pton = pton * p;
            }
            counter = counter / 2;
            pwrs.add(counter);
        }
        return pwrs;
    }
    
    public Color getColor() {
        return versionColor;
    }
    
    public int getGridDiagram() {
        if (gridDiagram) return 0;
        return 1;
    }
    
    public Image getImage() {
        return img;
    }
    
    public Image getMedImage() {
        return medImg;
    }
    
    public int getOperatingSystem() {
        return opSys;
    }
    
    public int getGirthInfo() {
        return detailedGirth;
    }
    
    public int getMaxSaveCount() {
        return maxSaveCount;
    }
    
    public boolean getChangeOfNumbers() {
        return changeNumbers;
    }
    
    public double getDivFactor() {
        return divFactor;
    }
    
    public ArrayList<Integer> getPrimes() {
        return primes;
    }
    
    public ArrayList<Integer> getPowers() {
        return powers;
    }
    
    public boolean getKhovRed() {
        return redKhov;
    }
    
    public boolean getKhovUnred() {
        return unrKhov;
    }
    
    public File getLoadLinksFrom() {
        return loadLinksFrom;
    }
    
    public boolean getTotGirth() {
        return girthTot;
    }
    
    public boolean getBoundGirth() {
        return boundGirth;
    }
    
    public boolean getRandomGirth() {
        return girthRandom;
    }
    
    public int getGirthBound() {
        return girthBound;
    }
    
    public boolean getPrimary() {
        return primary;
    }
    
    public File getSaveImage() {
        return saveImage;
    }
    
    public File getSaveKhovanov() {
        return saveKhovanov;
    }
    
    public String getSlash() {
        return slashSymbol;
    }
    
    public boolean isPrimary() {
        return primary;
    }
    
    public void setGridDiagram(boolean b) {
        gridDiagram = b;
    }
    
    public void setKhovRed(boolean b) {
        redKhov = b;
    }
    
    public void setKhovUnred(boolean b) {
        unrKhov = b;
    }
    
    public void setGirthInfo(int m) {
        detailedGirth = m;
    }
    
    public void setDivFactor(double fc) {
        divFactor = fc;
    }
    
    public void setLoadLinksFrom(File lf) {
        loadLinksFrom = lf;
    }
    
    public void setSaveKhovanov(File fs) {
        saveKhovanov = fs;
    }
    
    public void setSaveImage(File fs) {
        saveImage = fs;
    }
    
    public void setTotGirth(boolean b) {
        girthTot = b;
    }
    
    public void setBoundGirth(boolean b) {
        boundGirth = b;
    }
    
    public void setRandomGirth(boolean b) {
        girthRandom = b;
    }
    
    public void setGirthBound(int bound) {
        girthBound = bound;
    }
    
    public void setPrimary(boolean b) {
        primary = b;
    }

    public void setMaxSaveCount(int c) {
        maxSaveCount = c;
    }
    
    public void setChangeOfNumbers(boolean b) {
        changeNumbers = b;
    }
    
    public void setTimeInfo(boolean b) {
        showTime = b;
    }
    
    public boolean getTimeInfo() {
        return showTime;
    }

    public boolean getCombine() {
        return combine;
    }
    
    public void setCombine(boolean b) {
        combine = b;
    }

    public int getCombAtt() {
        return combAttempts;
    }
    
    public void setCombAtt(int u) {
        combAttempts = u;
    }
    
    public int getStableBound() {
        return stableBound;
    }
    
    public void setStableBound(int v) {
        stableBound = v;
    }
}
