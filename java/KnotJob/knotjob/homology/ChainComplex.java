/*

Copyright (C) 2020-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import knotjob.AbortInfo;
import knotjob.dialogs.DialogWrap;
import knotjob.rings.Ring;

/**
 *
 * @author dirk
 * @param <R>
 */
public class ChainComplex<R extends Ring<R>> {
    
    protected ArrayList<ArrayList<Generator<R>>> generators;
    protected final DialogWrap frame;
    protected final AbortInfo abInf;
    protected final R unit;
    
    public ChainComplex(R unt, DialogWrap frm, AbortInfo abf) {
        unit = unt;
        generators = new ArrayList<ArrayList<Generator<R>>>();
        frame = frm;
        abInf = abf;
    }
    
    public ChainComplex(R unt, ArrayList<ArrayList<Generator<R>>> genes, DialogWrap frm, AbortInfo abf) {
        unit = unt;
        generators = genes;
        frame = frm;
        abInf = abf;
    }
    
    public ArrayList<Generator<R>> getGenerators(int i) {
        return generators.get(i);
    }
    
    public int generatorSize() {
        return generators.size();
    }
    
    public ArrayList<Integer> getQs() {
        // to be overwritten
        return null;
    }
    
    public ChainComplex<R> getQComplex(int q) {
        // to be overwritten
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    public boolean containsBoundaries() {
        // to be overwritten
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    public void cancelGenerators(int qJump) {
        // to be overwritten
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    public ArrayList<String> homologyInfo() {
        // to be overwritten
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    public void output() {
        this.output(0,generators.size());
    }
    
    public void output(int fh, int lh) {
        for (int i = fh; i < lh; i++) {
            System.out.println();
            System.out.println("Level "+i);
            for (int j = 0; j < ((ArrayList<Generator<R>>) generators.get(i)).size(); j++) {
                System.out.println();
                System.out.println("Generator "+j);
                ArrayList<Generator<R>> nextLev = null;
                if (i < generators.size()-1) nextLev = generators.get(i+1);
                generators.get(i).get(j).output(nextLev);
            }
        }
        System.out.println();
        System.out.println();
    }

    public boolean boundaryCheck() {
        boolean check = true;
        int i = 0;
        while (check && i < generators.size()-2) {
            ArrayList<Generator<R>> gens = generators.get(i);
            ArrayList<R> values = new ArrayList<R>();
            while (values.size() < generators.get(i+2).size()) values.add(unit.getZero());
            int j = 0;
            while (check && j < gens.size()) {
                Generator<R> bGen = gens.get(j);
                for (Arrow<R> arr : bGen.getBotArrows()) {
                    Generator<R> mGen = arr.getTopGenerator();
                    for (Arrow<R> tar : mGen.getBotArrows()) {
                        Generator<R> tGen = tar.getTopGenerator();
                        int k = generators.get(i+2).indexOf(tGen);
                        values.set(k, values.get(k).add(arr.value.multiply(tar.value)));
                    }
                }
                int k = 0;
                while (check && k < values.size()) {
                    if (!values.get(k).isZero()) check = false;
                    else k++;
                }
                if (!check) System.out.println(i+" "+(generators.get(i).size()-j)+" "+
                        (generators.get(i+2).size()-k)+" "+values);
                j++;
            }
            i++;
        }
        return check;
    }
    
    public ArrayList<Homology> obtainBettis() {
        ArrayList<Homology> homology = new ArrayList<Homology>();
        int i = generators.size()-1;
        int h = generators.get(0).get(0).hdeg();
        if (frame != null) frame.setLabelRight(""+(h+i), 1, false);
        while (i > -1) {
            if (abInf.isAborted()) return null;
            ArrayList<Generator<R>> objs = generators.get(i);
            if (!objs.isEmpty()) addHomology(objs.get(0).hdeg(),objs.size(),homology);
            i--;
        }
        Collections.sort(homology);
        return homology;
    }
    
    public ArrayList<Homology> smithNormalize(int[] primes) {
        ArrayList<Homology> homology = new ArrayList<Homology>();
        int tot = totalObjects();
        int i = generators.size()-1;
        int h = generators.get(0).get(0).hdeg();
        if (frame != null) frame.setLabelRight(""+(h+i), 1, false);
        while (i > -1) {
            if (abInf.isAborted()) return null;
            Arrow<R> arr = findSmallest(i);
            if (arr == null) {
                i--;
                if (frame != null) frame.setLabelRight(""+(h+i), 1, false);
            }
            else {
                if (isolateBottom(arr)) {
                    if (isolateTop(arr)) {
                        if (!arr.value.isInvertible()) addHomology(arr, homology, primes);
                        generators.get(i).remove(arr.tObj);
                        generators.get(i-1).remove(arr.bObj);
                        tot = tot - 2;
                        frame.setLabelRight(""+tot, 2, false);
                    }
                }
            }
        }
        for (ArrayList<Generator<R>> gens : generators) {
            if (!gens.isEmpty()) addHomology(gens.get(0).hdeg(),gens.size(),homology);
        }
        Collections.sort(homology);
        return homology;
    }
    
    public int totalObjects() {
        int tot = 0;
        for (ArrayList<Generator<R>> objs : generators) tot = tot+objs.size();
        return tot;
    }
    
    private Arrow<R> findSmallest(int i) {
        int j = 0;
        boolean found = false;
        Arrow<R> smallest = null;
        while (!found && j < generators.get(i).size()) {
            Generator<R> gen = generators.get(i).get(j);
            int k = 0;
            while (!found && k < gen.tMor.size()) {
                Arrow<R> arr = gen.tMor.get(k);
                if (smallest == null || smallest.value.isBigger(arr.value)) smallest = arr;
                if (smallest != null && smallest.value.isInvertible()) found = true;
                k++;
            }
            j++;
        }
        return smallest;
    }
    
    private boolean isolateBottom(Arrow<R> arr) {
        int m = arr.bObj.bMor.size()-1;
        while (m >= 0) {
            Arrow<R> ar = arr.bObj.bMor.get(m);
            if (ar != arr) {
                R k = ar.value.div(arr.value).negate();
                Generator<R> aGen = ar.tObj;
                for (Arrow<R> nar : arr.tObj.tMor) {
                    boolean found = false;
                    int i = 0;
                    while (!found && i < aGen.tMor.size()) {
                        if (aGen.tMor.get(i).bObj == nar.bObj) found = true;
                        else i++;
                    }
                    Arrow<R> narr;
                    if (found) {
                        narr = aGen.tMor.get(i);
                        narr.addValue(k.multiply(nar.value));
                        if (narr.value.isZero()) {
                            narr.bObj.bMor.remove(narr);
                            aGen.tMor.remove(narr);
                        }
                    }
                    else {
                        narr = new Arrow<R>(nar.bObj, aGen, k.multiply(nar.value));
                        if (!narr.value.isZero()) {
                            narr.bObj.bMor.add(narr);
                            aGen.tMor.add(narr);
                        }
                    }
                }
                for (Arrow<R> tar : aGen.bMor) {
                    boolean found = false;
                    int i = 0;
                    while (!found && i < tar.tObj.tMor.size()) {
                        if (tar.tObj.tMor.get(i).bObj == arr.tObj) found = true;
                        else i++;
                    }
                    Arrow<R> narr;
                    if (found) {
                        narr = tar.tObj.tMor.get(i);
                        narr.addValue(k.multiply(tar.value).negate());
                        if (narr.value.isZero()) {
                            narr.bObj.bMor.remove(narr);
                            narr.tObj.tMor.remove(narr);
                        }
                    }
                    else {
                        narr = new Arrow<R>(arr.tObj, tar.tObj, k.multiply(tar.value).negate());
                        if (!narr.value.isZero()) {
                            narr.bObj.bMor.add(narr);
                            narr.tObj.tMor.add(narr);
                        }
                    }
                }
            }
            m--;
        }
        return (arr.bObj.bMor.size() == 1);
    }
    
    private boolean isolateTop(Arrow<R> arr) {
        int m = arr.tObj.tMor.size()-1;
        while (m >= 0) {
            Arrow<R> ar = arr.tObj.tMor.get(m);
            if (ar != arr) {
                R k = ar.value.div(arr.value).negate();
                Generator<R> aGen = ar.bObj;
                for (Arrow<R> nar : arr.bObj.bMor) {
                    boolean found = false;
                    int i = 0;
                    while (!found && i < aGen.bMor.size()) {
                        if (aGen.bMor.get(i).tObj == nar.tObj) found = true;
                        else i++;
                    }
                    Arrow<R> narr;
                    if (found) {
                        narr = aGen.bMor.get(i);
                        narr.addValue(k.multiply(nar.value));
                        if (narr.value.isZero()) {
                            narr.bObj.bMor.remove(narr);
                            narr.tObj.tMor.remove(narr);
                        }
                    }
                    else {
                        narr = new Arrow<R>(aGen,nar.tObj, k.multiply(nar.value));
                        if (!narr.value.isZero()) {
                            nar.tObj.tMor.add(narr);
                            aGen.bMor.add(narr);
                        }
                    }
                }
                for (Arrow<R> bar : aGen.tMor) {
                    boolean found = false;
                    int i = 0;
                    while (!found && i < bar.bObj.bMor.size()) {
                        if (bar.bObj.bMor.get(i).tObj == arr.bObj) found = true;
                        else i++;
                    }
                    Arrow<R> narr;
                    if (found) {
                        narr = bar.bObj.bMor.get(i);
                        narr.addValue(k.multiply(bar.value).negate());
                        if (narr.value.isZero()) {
                            narr.bObj.bMor.remove(narr);
                            narr.tObj.tMor.remove(narr);
                        }
                    }
                    else {
                        narr = new Arrow<R>(bar.bObj, arr.bObj, k.multiply(bar.value).negate());
                        if (!narr.value.isZero()) {
                            narr.bObj.bMor.add(narr);
                            narr.tObj.tMor.add(narr);
                        }
                    }
                }
            }
            m--;
        }
        return (arr.tObj.tMor.size() == 1);
    }
    
    private void addHomology(int hdeg, int size, ArrayList<Homology> homology) {
        boolean found = false;
        int i = 0;
        while (!found && i < homology.size()) {
            if (homology.get(i).hdeg() == hdeg) found = true;
            else i++;
        }
        if (found) {
            Homology hom = homology.get(i);
            hom.setBetti(size);
        }
        else {
            Homology hom = new Homology(hdeg,size);
            homology.add(hom);
        }
    }
    
    private void addHomology(Arrow<R> arr, ArrayList<Homology> homology, int[] primes) {
        String valueStr = arr.value.toString();
        int app = valueStr.indexOf("/");
        if (app == -1) app = valueStr.length();
        BigInteger tor;
        ArrayList<BigInteger> factors = new ArrayList<BigInteger>();
        if (primes == null) {
            tor = getPowerH(valueStr.substring(0, app));
            factors.add(tor);
        }
        else {
            tor = new BigInteger(valueStr.substring(0, app)).abs();
            for (int p : primes) {
                BigInteger bigP = BigInteger.valueOf(p);
                while (tor.mod(bigP).compareTo(BigInteger.ZERO) == 0) tor = tor.divide(bigP);
            }
            factors = primeFactors(tor);
        }
        addTorsion(arr.tObj.hdeg(), factors, homology);
    }
    
    private BigInteger getPowerH(String str) {
        int u = str.indexOf("^");
        if (u < 0) return BigInteger.ONE;
        int v = str.substring(u).indexOf(" ");
        return new BigInteger(str.substring(u+1, u+v));
    }
    
    private void addTorsion(int hdeg, ArrayList<BigInteger> factors, ArrayList<Homology> homology) {
        boolean found = false;
        int i = 0;
        while (!found && i < homology.size()) {
            if (homology.get(i).hdeg() == hdeg) found = true;
            else i++;
        }
        if (found) {
            Homology hom = homology.get(i);
            hom.addTorsion(factors);
        }
        else {
            Homology hom = new Homology(hdeg,factors);
            homology.add(hom);
        }
    }
    
    private ArrayList<BigInteger> primeFactors(BigInteger t) { // it's really prime power factors
        ArrayList<BigInteger> primes = new ArrayList<BigInteger>();
        BigInteger p = BigInteger.valueOf(2);
        while (p.compareTo(t) <= 0) {
            int power = 0;
            BigInteger q = BigInteger.ONE;
            while ( t.mod(p).compareTo(BigInteger.ZERO) == 0) {
                t = t.divide(p);
                power++;
                q = q.multiply(p);
            }
            if (power > 0) primes.add(q);
            p = p.add(BigInteger.ONE);
        }
        return primes;
    }

    public ArrayList<Homology> modNormalize(R prime) {
        ArrayList<Homology> homology = new ArrayList<Homology>();
        int tot = totalObjects();
        R primepower = prime.add(prime.getZero());
        while (!primepower.isZero()) {
            int i = generators.size()-1;
            while ( i > -1 ) {
                Arrow<R> arr = findArrow(i,primepower);
                if (arr != null) {
                    if (isolateBottom(arr)) {
                        if (isolateTop(arr)) {
                            addModHomology(arr,homology);
                            generators.get(i).remove(arr.tObj);
                            generators.get(i-1).remove(arr.bObj);
                            tot = tot - 2;
                            frame.setLabelRight(String.valueOf(tot), 2, false);
                        }
                    }
                }
                else i--;
            }
            primepower = primepower.multiply(prime);
        }
        for (ArrayList<Generator<R>> gens : generators) {
            if (!gens.isEmpty()) addHomology(gens.get(0).hdeg(), gens.size(),homology);
        }
        Collections.sort(homology);
        return homology;
    }
    
    private void addModHomology(Arrow<R> arr, ArrayList<Homology> homology) {
        R torVal = arr.value.multiply(arr.value.invert());
        String valueStr = torVal.toString();
        int a = valueStr.indexOf("(");
        valueStr = valueStr.substring(0, a-1);
        int tor = Integer.parseInt(valueStr);
        addTorsion(arr.tObj.hdeg(), tor, homology);
    }
    
    private void addTorsion(int hdeg, int tor, ArrayList<Homology> homology) {
        boolean found = false;
        int i = 0;
        while (!found && i < homology.size()) {
            if (homology.get(i).hdeg() == hdeg) found = true;
            else i++;
        }
        if (found) {
            Homology hom = homology.get(i);
            hom.addTorsion(BigInteger.valueOf(tor));
        }
        else {
            Homology hom = new Homology(hdeg, BigInteger.valueOf(tor));
            homology.add(hom);
        }
    }
    
    private Arrow<R> findArrow(int i, R power) {
        int j = 0;
        boolean found = false;
        Arrow<R> farr = null;
        while (!found && j < generators.get(i).size()) {
            Generator<R> gen = generators.get(i).get(j);
            int k = 0;
            while (!found && k < gen.tMor.size()) {
                Arrow<R> arr = gen.tMor.get(k);
                if (arr.value.div(power).isInvertible()) {
                    farr = arr;
                    found = true;
                }
                k++;
            }
            j++;
        }
        return farr;
    }
    
    public void throwAway(int min, int max) {
        int i = generators.size()-1;
        while (i >= 0) {
            if (!generators.get(i).isEmpty()) {
                int h = generators.get(i).get(0).hdeg();
                boolean away = false;
                if (h < min || h > max) away = true;
                if (h == min) {
                    for (Generator<R> obj : generators.get(i)) obj.clearTopArr();
                }
                if (h == max) {
                    for (Generator<R> obj : generators.get(i)) obj.clearBotArr();
                }
                if (away) generators.remove(i);
            }
            i--;
        }
        i = generators.size()-1;
        while (generators.get(i).isEmpty()) {
            generators.remove(i);
            i--;
        }
        while (generators.get(0).isEmpty()) {
            generators.remove(0);
        }
    }
    
}
