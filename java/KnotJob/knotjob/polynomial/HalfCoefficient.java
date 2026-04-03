/*

Copyright (C) 2021 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
public class HalfCoefficient extends Coefficient {
    
    public HalfCoefficient(int[] pwr, BigInteger val) {
        super(pwr, val);
    }
    
    public HalfCoefficient multiply(HalfCoefficient coe) {
        int[] pwrs = new int[powers.length];
        for (int i = 0; i < pwrs.length; i++) pwrs[i] = powers[i]+coe.powers[i];
        return new HalfCoefficient(pwrs,value.multiply(coe.value));
    }
    
    public HalfCoefficient add(HalfCoefficient coe) {
        if (this.compareTo(coe) != 0) return null;
        return new HalfCoefficient(powers, value.add(coe.value));
    }
    
    @Override
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
        if (pwr % 2 == 0) {
            if (pwr / 2 == 1) return lb;
            return lb+"^"+pl+(pwr/2)+pr;
        }
        return lb+"^"+pl+String.valueOf(pwr)+"/2"+pr;
    }
    
}
