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

package knotjob;

import java.math.BigInteger;
import knotjob.homology.HomologyInfo;
import knotjob.homology.Homology;
import knotjob.homology.QuantumCohomology;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class Comparer {

    private int compType; // 0 means name
    private boolean reverse;
    
    public Comparer(int ctype) {
        compType = ctype;
    }
    
    public int getType() {
        return compType;
    }
    
    public void setType(int ctype) {
        compType = ctype;
    }
    
    public int compare(LinkData linkOne, LinkData linkTwo, int cmpt) {
        int returner = 0;
        if (cmpt == 0) {
            returner = linkOne.name.compareTo(linkTwo.name);
        }
        if (cmpt == 1) {
            returner = linkOne.chosenLink().components()-linkTwo.chosenLink().components();
        }
        if (cmpt == 2) {
            returner = linkOne.chosenLink().writhe()-linkTwo.chosenLink().writhe();
        }
        if (cmpt == 3) {
            returner = linkOne.chosenLink().crossingNumber()-linkTwo.chosenLink().crossingNumber();
        }
        if (cmpt == 4) {
            int maxOne = linkOne.chosenLink().maxGirth();
            int maxTwo = linkTwo.chosenLink().maxGirth();
            if (maxOne != maxTwo) returner = maxOne-maxTwo;
            else returner = linkOne.chosenLink().totalGirth()-linkTwo.chosenLink().totalGirth();
        }
        if (cmpt == 5) { // comparing mod 2 sInvariant
            Integer first = linkOne.sInvariant(2);
            Integer secon = linkTwo.sInvariant(2);
            if (first == null) returner++;
            if (secon == null) returner--;
            if (first != null && secon != null) returner = first-secon;
        }
        if (cmpt == 6) { // comparing mod 3 sInvariant
            Integer first = linkOne.sInvariant(3);
            Integer secon = linkTwo.sInvariant(3);
            if (first == null) returner++;
            if (secon == null) returner--;
            if (first != null && secon != null) returner = first-secon;
        }
        if (cmpt == 7) { // comparing Rasmussen sInvariant
            Integer first = linkOne.sInvariant(0);
            Integer secon = linkTwo.sInvariant(0);
            if (first == null) returner++;
            if (secon == null) returner--;
            if (first != null && secon != null) returner = first-secon;
        }
        if (cmpt == 8) { // comparing unred Khov
            returner = khovCompare(linkOne ,linkTwo, false);
        }
        if (cmpt == 9) { // comparing odd Khov
            returner = khovCompare(linkOne ,linkTwo, true);
        }
        if (cmpt == 10) { // comparing Jones Polynomial
            if (linkOne.jones == null) returner++;
            if (linkTwo.jones == null) returner--;
            if (linkOne.jones != null && linkTwo.jones != null) 
                returner = linkOne.jones.compareTo(linkTwo.jones);
        }
        if (cmpt == 11) { // comparing Alexander Polynomial
            if (linkOne.alex == null) returner++;
            if (linkTwo.alex == null) returner--;
            if (linkOne.alex != null && linkTwo.alex != null) 
                returner = linkOne.alex.compareTo(linkTwo.alex);
        }
        if (cmpt == 12) { // comparing signature
            if (linkOne.signature == null) returner++;
            if (linkTwo.signature == null) returner--;
            if (linkOne.signature != null && linkTwo.signature != null) {
                int signOne = Integer.parseInt(linkOne.signature);
                int signTwo = Integer.parseInt(linkTwo.signature);
                returner = signOne - signTwo;
            }
        }
        if (cmpt == 13) { // comparing determinant
            if (linkOne.determinant == null) returner++;
            if (linkTwo.determinant == null) returner--;
            if (linkOne.determinant != null && linkTwo.determinant != null) {
                int detOne = Integer.parseInt(linkOne.determinant);
                int detTwo = Integer.parseInt(linkTwo.determinant);
                returner = detOne - detTwo;
            }
        }
        if (reverse) returner = -returner;
        return returner;
    }
    
    private int khovCompare(LinkData linkOne, LinkData linkTwo, boolean odd) {
        HomologyInfo homTwo;
        if (odd) homTwo = linkTwo.integralOddKhHomology();
        else homTwo = linkTwo.integralKhovHomology(false);
        if (homTwo == null) return -1;
        HomologyInfo homOne;
        if (odd) homOne = linkOne.integralOddKhHomology();
        else homOne = linkOne.integralKhovHomology(false);
        if (homOne == null) return +1;
        int qOne = homOne.getHomologies().get(0).qdeg();
        int qTwo = homTwo.getHomologies().get(0).qdeg();
        if (qOne != qTwo) return qOne-qTwo;
        qOne = homOne.getHomologies().get(homOne.getHomologies().size()-1).qdeg();
        qTwo = homTwo.getHomologies().get(homTwo.getHomologies().size()-1).qdeg();
        int i = 0;
        while (i < homOne.getHomologies().size() && i < homTwo.getHomologies().size()) {
            QuantumCohomology qCohOne = homOne.getHomologies().get(i);
            QuantumCohomology qCohTwo = homTwo.getHomologies().get(i);
            int qCompare = quantCompare(qCohOne,qCohTwo);
            if (qCompare != 0) return qCompare;
            i++;
        }
        return qOne-qTwo;
    }
    
    private int quantCompare(QuantumCohomology qCohOne, QuantumCohomology qCohTwo) {
        int j = 0;
        int factor = 1;
        while (j < qCohOne.getHomGroups().size() && j < qCohTwo.getHomGroups().size()) {
            Homology hOne = qCohOne.getHomGroups().get(j);
            Homology hTwo = qCohTwo.getHomGroups().get(j);
            if (hOne.hdeg() != hTwo.hdeg()) return factor * (hOne.hdeg() - hTwo.hdeg());
            factor = -1;
            if (hOne.getBetti() != hTwo.getBetti()) return hOne.getBetti() - hTwo.getBetti();
            int t = torsionCompare(hOne,hTwo);
            if (t != 0) return t;
            j++;
        }
        return qCohOne.getHomGroups().size() - qCohTwo.getHomGroups().size();
    }
    
    private int torsionCompare(Homology hOne, Homology hTwo) {
        int h = 0;
        while (h < hOne.getTorsion().size() && h < hTwo.getTorsion().size()) {
            BigInteger tOne = hOne.getTorsion().get(h);
            BigInteger tTwo = hTwo.getTorsion().get(h);
            if (!tOne.equals(tTwo)) return tOne.compareTo(tTwo);
            h++;
        }
        return hOne.getTorsion().size() - hTwo.getTorsion().size();
    }

    public boolean isReversed() {
        return reverse;
    }
    
    public void setReversed(boolean rev) {
        reverse = rev;
    }
}
