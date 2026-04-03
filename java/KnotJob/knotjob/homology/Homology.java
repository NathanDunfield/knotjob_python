/*

Copyright (C) 2019-20 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 *
 * @author Dirk
 */
public class Homology implements Comparable<Homology> {
    
    private final int hdeg;
    private int betti;
    private final ArrayList<BigInteger> torsion;
    
    public Homology(int hd, int bt) {
        hdeg = hd;
        betti = bt;
        torsion = new ArrayList<BigInteger>();
    }
    
    public Homology(int hd, BigInteger bg) {
        hdeg = hd;
        betti = 0;
        torsion = new ArrayList<BigInteger>();
        torsion.add(bg);
    }
    
    public Homology(int hd, ArrayList<BigInteger> trsn) {
        hdeg = hd;
        torsion = trsn;
    }
    
    public Homology(int hd, int bn, ArrayList<BigInteger> trsn) {
        hdeg = hd;
        betti = bn;
        torsion = new ArrayList<BigInteger>();
        for (BigInteger tor : trsn) torsion.add(tor);
    }
    
    public void dePrimarizeTorsion() {
        ArrayList<ArrayList<BigInteger>> matrix = new ArrayList<ArrayList<BigInteger>>();
        BigInteger p = BigInteger.valueOf(2);
        matrix.add(new ArrayList<BigInteger>());
        int j = 0;
        while (!torsion.isEmpty()) {
            int i = 0;
            while (i < torsion.size()) {
                if (torsion.get(i).mod(p).equals(BigInteger.ZERO)) {
                    matrix.get(j).add(0,torsion.get(i));
                    torsion.remove(i);
                }
                else i++;
            }
            p = p.nextProbablePrime();
            j++;
            matrix.add(new ArrayList<BigInteger>());
        }
        while (!p.equals(BigInteger.ONE)) {
            p = BigInteger.ONE;
            for (ArrayList<BigInteger> powers : matrix) {
                if (!powers.isEmpty()) {
                    p = p.multiply(powers.get(0));
                    powers.remove(0);
                }
            }
            if (!p.equals(BigInteger.ONE)) torsion.add(0,p);
        }
    }
    
    public int hdeg() {
        return hdeg;
    }
    
    public int getBetti() {
        return betti;
    }
    
    public void setBetti(int bet) {
        betti = bet;
    }
    
    public void addTorsion(BigInteger bg) {
        torsion.add(bg);
    }
    
    public void addTorsion(ArrayList<BigInteger> facs) {
        for (BigInteger f : facs) torsion.add(f);
    }
    
    public ArrayList<BigInteger> getTorsion() {
        return torsion;
    }
    
    public void removeTorsion() {
        torsion.clear();
    }
    
    @Override
    public int compareTo(Homology o) {
        return hdeg - o.hdeg;
    }
    
}
