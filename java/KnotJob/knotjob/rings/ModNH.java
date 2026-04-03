/*

Copyright (C) 2023 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.rings;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Dirk
 */
public class ModNH implements Ring<ModNH> {

    // A polynomial ring over a field. If mod is not a prime number, expect garbage.
    
    private final int mod;
    private final ArrayList<Monomial> values;
    
    public ModNH(int val, int pwr, int md) {
        values = new ArrayList<Monomial>();
        Monomial mono = new Monomial(val, pwr);
        mod = md;
        if (!mono.isZero()) values.add(mono);
    }
    
    public ModNH(int md) { // this returns 0.
        values = new ArrayList<Monomial>();
        mod = md;
    }
    
    @Override
    public ModNH add(ModNH r) {
        ModNH sum = new ModNH(mod);
        for (Monomial mono : values) sum.values.add(mono);
        for (Monomial mono : r.values) sum.values.add(mono);
        sum.combineMonomials();
        return sum;
    }

    @Override
    public ModNH div(ModNH r) {
        if (r.isZero()) return null;
        if (r.isBigger(this)) return new ModNH(mod).add(this);
        ModNH help = new ModNH(mod);
        ModNH div = new ModNH(mod);
        help = help.add(this);
        int diff = help.highestPower() - r.highestPower();
        while (diff >= 0) {
            Monomial nMono = help.values.get(help.values.size()-1).div(
                    r.values.get(r.values.size()-1));
            div.values.add(nMono);
            help = help.add(r.multiply(new ModNH(nMono.val, nMono.gPwr, mod)).negate());
            diff = help.highestPower() - r.highestPower();
        }
        div.combineMonomials();
        return div;
    }

    private int[] bezout(int a, int b) {
        int[] bez = new int[2];
        if (a >= b) bez = getBezout(a,b);
        else {
            int[] bezalt = getBezout(b,a);
            bez[0] = bezalt[1];
            bez[1] = bezalt[0];
        }
        return bez;
    }
    
    private int[] getBezout(int a, int b) {
        int[] bez = new int[2];
        if (a == b) {
            bez[0] = 1;
            bez[1] = 0;
            return bez;
        }
        if ( a % b == 0 ) {
            bez[0] = 0;
            bez[1] = 1;
            return bez;
        }
        int [] bezou = getBezout(b,a%b);
        bez[0] = bezou[1];
        bez[1] = bezou[0] - ((a/b) * bezou[1]);
        return bez;
    }
    
    @Override
    public ModNH getZero() {
        return new ModNH(mod);
    }

    @Override
    public ModNH invert() {
        Monomial entry = values.get(0).invert();
        return new ModNH(entry.val, entry.gPwr, mod);
    }

    @Override
    public boolean divides(ModNH r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isBigger(ModNH r) {
        return highestPower() - r.highestPower() > 0;
    }

    @Override
    public boolean isInvertible() { // will not work for mod non-prime.
        return highestPower() == 0;
    }

    @Override
    public boolean isZero() {
        return values.isEmpty();
    }

    @Override
    public ModNH multiply(ModNH r) {
        ModNH prod = new ModNH(mod);
        for (Monomial mono : values) {
            for (Monomial smon : r.values) {
                Monomial pro = mono.multiply(smon);
                prod.values.add(pro);
            }
        }
        prod.combineMonomials();
        return prod;
    }

    @Override
    public ModNH negate() {
        ModNH neg = new ModNH(mod);
        for (Monomial mono : values) neg.values.add(mono.negate());
        return neg;
    }

    @Override
    public ModNH abs(int i) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void combineMonomials() {
        Collections.sort(values);
        int i = values.size()-1;
        while (i > 0) {
            Monomial first = values.get(i);
            Monomial secon = values.get(i-1);
            if (first.gPwr == secon.gPwr) {
                Monomial add = first.add(secon);
                values.remove(i-1);
                values.remove(i-1);
                if (!add.isZero()) values.add(i-1, add);
                else i--;
            }
            i--;
        }
    }
    
    @Override
    public String toString() {
        String str = "0";
        if (!values.isEmpty()) {
            str = values.get(0).stringValue(false);
            for (int i = 1; i < values.size(); i++) str = str+values.get(i).stringValue(true);
        }
        return str+" (mod "+mod+")";
    }

    private int highestPower() {
        if (values.isEmpty()) return -1;
        return values.get(values.size()-1).gPwr;
    }

    private class Monomial implements Comparable<Monomial> {

        private final int val;
        private final int gPwr;
        
        public Monomial(int vl, int pwr) {
            val = vl;
            gPwr = pwr;
        }

        @Override
        public int compareTo(Monomial o) {
            return gPwr - o.gPwr;
        }

        private Monomial add(Monomial secon) {
            return new Monomial((val+secon.val)%mod, gPwr);
        }

        private boolean isZero() {
            return (val%mod == 0);
        }

        private String stringValue(boolean b) {
            String str = String.valueOf(val);
            if (b) str = " + "+str;
            if (gPwr > 0) str = str+" H";
            if (gPwr > 1) str = str+"^"+gPwr;
            return str;
        }

        private Monomial negate() {
            return new Monomial((mod - val)%mod, gPwr);
        }

        private Monomial multiply(Monomial smon) {
            return new Monomial((val * smon.val)% mod, gPwr+smon.gPwr);
        }

        private Monomial div(Monomial smon) {
            if (smon.gPwr > gPwr) return null;
            int[] bez = bezout(mod, smon.val);
            return new Monomial((val * bez[1])%mod, gPwr-smon.gPwr);
        }
        
        private Monomial invert() { // assuming that ground ring is a field
            int[] bez = bezout(mod, val);
        return new Monomial(bez[1], -gPwr); 
        }
        
    }
    
}
