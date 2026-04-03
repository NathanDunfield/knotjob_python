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
import java.util.Collections;

/**
 *
 * @author Dirk
 */
public class QuantumCohomology implements Comparable<QuantumCohomology> {
    
    private final int qdeg;
    private final ArrayList<Homology> homGroups;

    public QuantumCohomology(String infoString) {
        boolean abort = false;
        int e = infoString.indexOf('h');
        if (e == -1) e = infoString.indexOf('x');
        if (e == -1) {
            e = infoString.indexOf('a');
            abort = true;
        }
        if (abort) homGroups = null;
        else {
            homGroups = new ArrayList<Homology>();
            if (infoString.charAt(e) == 'h') determineHomology(infoString.substring(e));
        }
        qdeg = Integer.parseInt(infoString.substring(1, e));
    }
    
    public QuantumCohomology(int q) {
        qdeg = q;
        homGroups = new ArrayList<Homology>();
    }

    public QuantumCohomology(int q, ArrayList<Homology> homs) {
        qdeg = q;
        if (homs == null) homGroups = null;
        else {
            homGroups = new ArrayList<Homology>();
            for (Homology hom : homs) homGroups.add(new Homology(hom.hdeg(), hom.getBetti(), hom.getTorsion()));
        }
    }
    
    public Homology findHomology(int h, boolean add) {
        Homology hm;
        boolean found = false;
            int i = 0;
        while (!found && i < getHomGroups().size()) {
            if (getHomGroups().get(i).hdeg() == h) found = true;
            else i++;
        }
        if (found) hm = getHomGroups().get(i);
        else {
            if (!add) return null;
            hm = new Homology(h,0);
            addHomology(hm);
        }
        return hm;
    }
    
    public int totalBetti(int p) {
        int betti = 0;
        BigInteger chr = BigInteger.valueOf(p);
        for (Homology hom : homGroups) {
            betti = betti + hom.getBetti();
            if (p > 0) {
                for (BigInteger tor : hom.getTorsion()) {
                    if (tor.mod(chr).equals(BigInteger.ZERO)) betti = betti + 2;
                }
            }
        }
        return betti;
    }
    
    public int qdeg() {
        return qdeg;
    }
    
    public boolean isEmpty() {
        return homGroups.isEmpty();
    }
    
    public void addHomology(Homology hm) {
        homGroups.add(hm);
        Collections.sort(homGroups);
    }
    
    public ArrayList<Homology> getHomGroups() {
        return homGroups;
    }
    
    public BigInteger maxTorsion(int prime) {
        BigInteger max = BigInteger.valueOf(1);
        BigInteger p = BigInteger.valueOf(prime);
        for (Homology hom : homGroups) {
            ArrayList<BigInteger> torsion = hom.getTorsion();
            for (BigInteger tor : torsion) {
                if (tor.mod(p).compareTo(BigInteger.ZERO) == 0) {
                    if (tor.compareTo(max) > 0) max = tor;
                }
            }
        }
        return max;
    }
    
    @Override
    public int compareTo(QuantumCohomology o) {
        return qdeg - o.qdeg;
    }

    @Override
    public String toString() {
        String theString = "q"+qdeg;
        if (homGroups == null) return theString+"a";
        if (homGroups.isEmpty()) return theString+"x";
        for (Homology hom : homGroups) {
            if (hom.getBetti()>0 | !hom.getTorsion().isEmpty()) {
                theString = theString+"h"+hom.hdeg()+"b"+hom.getBetti();
                theString = theString+torsionInfo(hom.getTorsion());
            }
        }
        return theString;
    }
    
    private String torsionInfo(ArrayList<BigInteger> torsion) {
        Collections.sort(torsion);
        String info = "";
        BigInteger f = BigInteger.ZERO;
        int power = 1;
        for (BigInteger p : torsion) {
            if (f.compareTo(p) == 0) power++;
            else {
                if (f.compareTo(BigInteger.ZERO)!=0) {
                    if (power == 1) info = info+"t"+f;
                    else info = info+"t"+f+"^"+power;
                }
                power = 1;
                f = p;
            }
        }
        if (f.compareTo(BigInteger.ZERO) != 0) {
            if (power == 1) info = info+"t"+f;
            else info = info+"t"+f+"^"+power;
        }
        return info;
    }
    
    private void determineHomology(String substring) {
        ArrayList<Integer> hpos = positionsOf('h', substring);
        ArrayList<String> subs = homSubStrings(substring,hpos);
        for (String info : subs) {
            int b = info.indexOf('b');
            int hd = Integer.parseInt(info.substring(1, b));
            int t = info.indexOf('t');
            int bt;
            if (t >= 0) bt = Integer.parseInt(info.substring(b+1,t));
            else bt = Integer.parseInt(info.substring(b+1));
            Homology hom = new Homology(hd,bt);
            if (t >= 0) {
                ArrayList<BigInteger> tors = getTorsion(info.substring(t));
                for (BigInteger tt : tors) hom.addTorsion(tt);
            }
            homGroups.add(hom);
        }
    }

    private ArrayList<BigInteger> getTorsion(String info) {
        ArrayList<Integer> tpos = positionsOf('t', info);
        ArrayList<String> subs = homSubStrings(info,tpos);
        ArrayList<BigInteger> torsion = new ArrayList<BigInteger>();
        for (String sub : subs) {
            int cap = sub.indexOf('^');
            BigInteger t;
            int pw = 1;
            if (cap >= 0) {
                t = new BigInteger(sub.substring(1, cap));
                pw = Integer.parseInt(sub.substring(cap+1));
            }
            else t = new BigInteger(sub.substring(1));
            for (int j = 0; j < pw; j++) torsion.add(t);
        }
        return torsion;
    }
    
    private ArrayList<String> homSubStrings(String ori, ArrayList<Integer> hpos) {
        ArrayList<String> theStrings = new ArrayList<String>();
        for (int i = 0; i < hpos.size()-1; i++) theStrings.add(ori.substring(hpos.get(i),hpos.get(i+1)));
        theStrings.add(ori.substring(hpos.get(hpos.size()-1)));
        return theStrings;
    }
    
    private ArrayList<Integer> positionsOf(char c, String substring) {
        ArrayList<Integer> pos = new ArrayList<Integer>();
        int i = -1;
        int e = 0;
        while (e >= 0) {
            e = substring.substring(i+1).indexOf(c);
            if (e >= 0) pos.add(i+1+e);
            i = i+e+1;
        }
        return pos;
    }

    public int width() {
        if (homGroups.isEmpty()) return 0;
        int min = homGroups.get(0).hdeg();
        int max = min;
        for (Homology hom : homGroups) {
            int hmin = hom.hdeg();
            int hmax = hmin;
            if (!hom.getTorsion().isEmpty()) hmin--;
            if (min > hmin) min = hmin;
            if (max < hmax) max = hmax;
        }
        return max - min+1;
    }
    
    public int[] widthAndMax() {
        if (homGroups.isEmpty()) return new int[] {0, 0, 0};
        int min = homGroups.get(0).hdeg();
        int max = min;
        for (Homology hom : homGroups) {
            int hmin = hom.hdeg();
            int hmax = hmin;
            if (!hom.getTorsion().isEmpty()) hmin--;
            if (min > hmin) min = hmin;
            if (max < hmax) max = hmax;
        }
        return new int[] {max - min+1, min, max};
    }
    
}
