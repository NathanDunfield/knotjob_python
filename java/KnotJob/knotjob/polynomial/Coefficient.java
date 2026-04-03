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

package knotjob.polynomial;

import java.math.BigInteger;

/**
 *
 * @author Dirk
 */
public class Coefficient implements Comparable<Coefficient> {
    
    final protected int[] powers;
    final protected BigInteger value;
    
    public Coefficient(int[] pwr, BigInteger val) {
        powers = pwr;
        value = val;
    }
    
    public Coefficient multiply(Coefficient coe) {
        int[] pwrs = new int[powers.length];
        for (int i = 0; i < pwrs.length; i++) pwrs[i] = powers[i]+coe.powers[i];
        return new Coefficient(pwrs,value.multiply(coe.value));
    }

    public Coefficient add(Coefficient coe) {
        if (this.compareTo(coe) != 0) return null;
        return new Coefficient(powers, value.add(coe.value));
    }
    
    public BigInteger getValue() {
        return value;
    }
    
    public int getPower(int i) {
        return powers[i];
    }
    
    public boolean isZero() {
        return value.equals(BigInteger.ZERO);
    }
    
    public String toString(boolean latex, String[] labels) {
        String pl = "";
        String pr = "";
        if (latex) {
            pl = "{";
            pr = "}";
        }
        String info = " "+value.toString();
        if (value.compareTo(BigInteger.valueOf(-1)) < 0) info = " - "+value.abs().toString();
        if (value.equals(BigInteger.ONE)) info = "";
        if (value.equals(BigInteger.valueOf(-1))) info = " -";
        if (allPowersZero()) {
            info = " "+value.toString();
            if (value.compareTo(BigInteger.ZERO) < 0) info = " - "+value.abs().toString();
        }
        else for (int i = 0; i < labels.length; i++) {
            if (powers[i] != 0) info = info+" "+theLabel(labels[i], pl, pr, powers[i]);
        }
        return info;
    }
    
    private String theLabel(String lb, String pl, String pr, int pwr) {
        if (pwr == 1) return lb;
        return lb+"^"+pl+pwr+pr;
    }
    
    protected boolean allPowersZero() {
        boolean allzero = true;
        int i = 0;
        while (allzero && i < powers.length) {
            if (powers[i] != 0) allzero = false;
            i++;
        }
        return allzero;
    }
    
    @Override
    public int compareTo(Coefficient o) {
        for (int i = 0; i < powers.length; i++) {
            if (powers[i] != o.powers[i]) return powers[i]-o.powers[i];
        }
        return 0;
    }
    
    
}
