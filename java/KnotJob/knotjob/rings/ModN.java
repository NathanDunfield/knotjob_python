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

package knotjob.rings;

/**
 *
 * @author Dirk
 */
public class ModN implements Ring<ModN> {
    protected final int mod;
    protected final int value;
    
    public ModN(int v, int m) {
        mod = m;
        value = (mod + v % mod)%mod;
    }

    private int gcd(int a, int b) {
        if (b == 0) return a;
        return gcd(b,a%b);
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
    public ModN add(ModN r) {
        return new ModN(value+r.value,mod);
    }

    @Override
    public ModN multiply(ModN r) {
        return new ModN(value * r.value, mod);
    }

    @Override
    public ModN negate() {
        return new ModN(mod-value,mod);
    }

    @Override
    public boolean isZero() {
        return (value % mod == 0);
    }

    @Override
    public boolean isInvertible() {
        return (gcd(mod, value) == 1);
    }

    @Override
    public ModN invert() {
        if (mod <= 3) return new ModN(value, mod); // WARNING : one should check beforehand that number is invertible
        if (value == 0) return new ModN(value, mod); // one should really throw a divide by 0 exception
        int[] bez = bezout(mod, value);
        return new ModN(bez[1], mod); // won't be the inverse if value isn't invertible
    }

    @Override
    public ModN getZero() {
        return new ModN(0, mod);
    }
    
    @Override
    public String toString() {
        return ""+value+" ("+mod+")";
    }

    @Override
    public boolean isBigger(ModN r) {
        return (gcd(mod, value) > gcd(r.mod, r.value));
    }

    @Override
    public ModN div(ModN r) {
        return new ModN(value/gcd(mod,r.value),mod).multiply(r.invert());
    }

    @Override
    public boolean divides(ModN r) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ModN abs(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
