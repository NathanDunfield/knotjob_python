/*

Copyright (C) 2019-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
public class ModNXi implements Ring<ModNXi> {
    
    private final int value;
    private final int xiValue;
    private final int mod;
    
    public ModNXi(int v, int m) {
        value = v;
        xiValue = 0;
        mod = m;
    }
    
    public ModNXi(int v, int w, int m) {
        mod = m;
        value = (mod + v % mod) % mod;
        xiValue = (mod + w % mod) % mod;
    }
    
    public ModNXi(ModN r) {
        value = r.value;
        mod = r.mod;
        xiValue = 0;
    }
    
    @Override
    public ModNXi add(ModNXi r) {
        return new ModNXi(value+r.value, xiValue+r.xiValue, mod);
    }
    
    @Override
    public ModNXi multiply(ModNXi r) {
        return new ModNXi(value * r.value + xiValue * r.xiValue, 
                value * r.xiValue + xiValue * r.value, mod);
    }

    @Override
    public ModNXi negate() {
        return new ModNXi(mod-value, mod-xiValue, mod);
    }

    @Override
    public boolean isZero() {
        return (value % mod == 0 && xiValue % mod == 0);
    }
    
    @Override
    public boolean isInvertible() {
        return (gcd(mod, value + xiValue) == 1 && gcd(mod, mod + value - xiValue) == 1);
    }
    
    @Override
    public ModNXi invert() {
        if (xiValue%mod == 0) {
            int[] bez = bezout(mod, value);
            return new ModNXi((mod + bez[1])%mod, mod);
        } 
        ModN inv = new ModN(value * value + (mod * mod - xiValue * xiValue), mod);
        return new ModNXi(value, mod-xiValue, mod).multiply(new ModNXi(inv.invert()));
    }
    
    @Override
    public ModNXi getZero() {
        return new ModNXi(0, mod);
    }
    
    @Override
    public String toString() {
        if (this.isZero()) return "0 ("+mod+")";
        String str = "";
        if (value % mod != 0) {
            str = str+value;
            if (xiValue % mod != 0) str = str+" + ";
        }
        if (xiValue % mod != 0) str = str +xiValue + " "+((char) 958);
        return str+" ("+mod+")";
    }

    @Override
    public ModNXi div(ModNXi r) {
        if (xiValue % mod == 0) return new ModNXi((mod +(value/r.value)%mod)%mod, mod);
        if ((mod + r.value%mod)%mod == 1) 
            if (((mod + r.xiValue%mod)%mod) == 1 || ((mod + r.xiValue%mod)%mod) == mod - 1)
                return new ModNXi((mod + value%mod)%mod, mod);
        return null;
    }

    @Override
    public boolean divides(ModNXi r) {
        if (r.isZero()) return true;
        if (isZero()) return r.isZero();// this has to do with the way it is used, should really be false
        
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isBigger(ModNXi r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ModNXi abs(int i) {
        if (i > 0) {
            int factor = 1;
            if (i == 2) factor = -1;
            return new ModNXi((mod + (value+xiValue* factor)%mod)%mod, mod);
        }
        return new ModNXi((mod + value%mod)% mod, mod);
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
}
