/*

Copyright (C) 2021-24 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.links;

import java.util.ArrayList;
import javax.swing.JFrame;
import knotjob.Comparer;
import knotjob.dialogs.EnterData;

/**
 *
 * @author Dirk
 */
public class LinkCreator {
    
    public static LinkData enterDTCode(String theString, String name, boolean combin, JFrame knob, 
            Comparer comparer) {
        if (theString == null) {
            EnterData data = new EnterData(knob, "DT-Code", "DT-Code : ", true);
            if ("".equals(data.theString)) return null;
            theString = data.theString;
            combin = data.combin;
            name = data.theName;
        }
        Link theLink = obtainLink(theString);
        if (theLink == null) return null;
        if (name.length() == 0) {
            if (theLink.components() == 1) name = "Knot";
            else name = "Link";
        }
        if (combin) theLink = theLink.girthMinimize();
        LinkData theNewData = new LinkData(name, theLink, comparer);
        return theNewData;
    }
    
    public static LinkData enterPDCode(String theString, String name, boolean combin, JFrame knob, 
            Comparer comparer) {
        if (theString == null) {
            EnterData data = new EnterData(knob, "PD-Code", "PD-Code : ", true);
            if ("".equals(data.theString)) return null;
            theString = data.theString;
            combin = data.combin;
            name = data.theName;
        }
        Link theLink = obtainPDLink(theString);
        if (theLink == null) return null;
        if (name.length() == 0) {
            if (theLink.components() == 1) name = "Knot";
            else name = "Link";
        }
        if (combin) theLink = theLink.girthMinimize();
        LinkData theNewData = new LinkData(name, theLink, comparer);
        return theNewData;
    }
    
    private static Link obtainPDLink(String pdcode) {
        if (pdcode.contains("PD")) pdcode = pdcode.substring(pdcode.lastIndexOf("PD"));
        ArrayList<Integer> numbers = new ArrayList<Integer>();
        int i = 0;
        while (i < pdcode.length()) {
            int c = pdcode.codePointAt(i);
            if (c != 44 && (c < 48 | c > 57)) pdcode = pdcode.replace(pdcode.charAt(i), 'X');
            i++;
        }
        pdcode = ","+pdcode.replaceAll("X", "")+",";
        ArrayList<Integer> commas = getCommas(pdcode);
        for (int j = 0; j < commas.size()-1; j++) {
            int a = commas.get(j);
            int b = commas.get(j+1);
            if (b > a+1) numbers.add(Integer.valueOf(pdcode.substring(a+1, b)));
        }
        int size = numbers.size();
        if (size %4 != 0) return null;
        if (numbers.contains(0)) {
            for (int j = 0; j < numbers.size(); j++) numbers.set(j, numbers.get(j)+1);
        }
        int[] crossings = new int[size/4];
        int[][] paths = new int[size/4][4];
        for (int j = 0; j < size/4; j++) {
            for (int k = 0; k < 4; k++) {
                paths[j][k] = numbers.get(4*j+k);
            }
            crossings[j] = 1;
        }
        if (numbersOkay(numbers, size/2) && isPlanar(paths, size/4)) 
            return new Link(crossings, paths);
        return null;
    }
    
    private static boolean numbersOkay(ArrayList<Integer> numbers, int size) {
        int[] check = new int[size];
        for (int n : numbers) {
            if (n > 0 && n <= size) check[n-1]++;
        }
        boolean okay = true;
        for (int c : check) if (c != 2) okay = false;
        return okay;
    }
    
    private static ArrayList<Integer> getCommas(String code) {
        ArrayList<Integer> commas = new ArrayList<Integer>();
        for (int i = 0; i < code.length(); i++) 
            if (code.charAt(i) == ',') commas.add(i);
        return commas;
    }
    
    private static boolean isPlanar(int[][] paths, int max) {
        boolean [][] checkedSurfaces = new boolean[paths.length][4];
        int discs = 0;
        int runner;
        int pos1 = 0;
        int pos2 = 0;
        int twice = 0;
        for (int i = 0; i < paths.length; i++) 
            if (paths[i][0] == 0 & paths[i][1] == 0) {
                for (int j = 0; j < 4; j++) checkedSurfaces[i][j] = true;
            }
        while (notAll(checkedSurfaces)) {
            checkedSurfaces[pos1][pos2] = true;
            pos2 = (pos2+1)%4;
            runner = paths[pos1][pos2];
            if (runner >= 1 ) {
                boolean found = false;
                int i = 0;
                int j = 0;
                while (!found & i < paths.length) {
                    j = 0;
                    while (!found & j < 4) {
                        if (paths[i][j] == runner & (!(i == pos1 & j == pos2)))
                            found = true;
                        else j++;
                    }
                    if (!found) i++;
                }
                if (found) {
                    pos1 = i;
                    pos2 = j;
                    twice++;
                }
            }
            if (checkedSurfaces[pos1][pos2]) {
                discs++;
                boolean found = false;
                pos1 = 0;
                while (!found & pos1 < paths.length) {
                    pos2 = 0;
                    while (!found & pos2 < 4) {
                        if (!checkedSurfaces[pos1][pos2]) found = true;
                        else pos2++;
                    }
                    if (!found) pos1++;
                }
            }
        }
        int vme = 0;
        for (int i = 0; i < paths.length; i++) if (paths[i][0]!=0) vme++;
        return (vme+discs-(twice/2) >= 2);
    }
    
    private static boolean notAll(boolean [][] check) {
        boolean notall = false;
        int i = 0;
        int j;
        while (i < check.length & !notall) {
            j = 0;
            while (j < 4 & !notall) {
                if (!check[i][j]) notall = true;
                else j++;
            }
            if (!notall) i++;
        }
        return notall;
    }
    
    public static LinkData enterADTCode(String theString, String name, boolean combin, JFrame knob,
            Comparer comparer) {
        if (theString == null) {
            EnterData data = new EnterData(knob, "Alphabetical DT-Code", "ADT-Code : ", true);
            if ("".equals(data.theString)) return null;
            theString = data.theString;
            name = data.theName;
            combin = data.combin;
        }
        Link theLink = obtainLinkAlp(theString);
        if (theLink == null) return null;
        if (name.length() == 0) {
            if (theLink.components() == 1) name = "Knot";
            else name = "Link";
        }
        if (combin) theLink = theLink.girthMinimize();
        LinkData theNewData = new LinkData(name,theLink,comparer);
        return theNewData;
    }
    
    public static Link obtainLinkAlp(String code) {
        Link link;
        char[] chars = code.toCharArray();
        if (chars.length < 3) return null;
        int numberCross = Character.getNumericValue(chars[0])-9;
        if (numberCross < 1) return null;
        int numberComp = Character.getNumericValue(chars[1])-9;
        if (numberComp < 1) return null;
        if (chars.length < 2+numberComp) return null;
        int[] compCross = new int[numberComp];
        for (int e = 2; e < 2+numberComp ; e++) compCross[e-2] = Character.getNumericValue(chars[e])-9;
        int k = 0;
        int sum = 0;
        boolean bad = false;
        while (!bad & k < numberComp) {
            if (compCross[k] < 1) bad = true;
            sum = sum + compCross[k];
            k++;
        }
        if (sum != numberCross) bad = true;
        if (bad) return null;
        for (int e = 1; e < compCross.length; e++) compCross[e] = compCross[e]+compCross[e-1];
        if (chars.length != 2 + numberComp + numberCross) return null;
        int[] numbers = new int[numberCross];
        boolean[] used = new boolean[numberCross];
        for (int i = 2+numberComp; i < chars.length; i++) {
            int v = Character.getNumericValue(chars[i])-9;
            int factor = -1;
            if (Character.isLowerCase(chars[i])) factor = 1;
            numbers[i-2-numberComp] = 2 * v * factor;
            if (v >= 1 & v <= numberCross) used[v-1] = true;
        }
        boolean allgood = true;
        int y = 0;
        while (y < used.length & allgood) {
            allgood = used[y];
            y++;
        }
        if (!allgood) return null;
        int[][] paths = new int[numberCross][4];
        paths[0][0] = 1;
        paths[0][2] = 2;
        paths = modifyPaths(paths,compCross,2,numbers);
        if (paths == null) return null;
        int[] crossings = new int[numberCross];
        for (int j = 0; j < numbers.length; j++) {
            int odd = 2*j+1;
            int even = odd+1;
            boolean found = false;
            int w = 0;
            while (!found) {
                if (contains(paths[w],odd,even)) found = true;
                else w++;
            }
            if (paths[w][0] == odd | paths[w][2] == odd) {
                if (numbers[j] < 0) crossings[w] = 1;
                else crossings[w] = -1;
            }
            else {
                if (numbers[j] < 0) crossings[w] = -1;
                else crossings[w] = 1;
            }
        }
        link = new Link(crossings,paths);
        return link;
    }
    
    private static int[][] modifyPaths(int[][] paths, int[] components, int runner, int[] numbers) {
        if (!isPlanar(paths,runner)) return null;
        if (runner > 2 * paths.length) return paths;
        int [][] copyPaths = copyOf(paths);
        int odd;
        int compbegin = 1;
        int comp = 1;
        boolean cont = true;
        while (cont) {
            if (runner <= 2 * components[comp-1]) cont = false;
            else comp++;
            if (comp >= components.length) cont = false;
        }
        boolean lastInComp = false;
        if (runner == 2 * components[comp-1]) lastInComp = true;
        if (comp > 1) compbegin = 2 * components[comp-2] + 1;
        if (runner % 2 == 0) {
            boolean found = false;
            int i = 0;
            while (!found) {
                if (Math.abs(numbers[i]) == runner) found = true;
                else i++;
            }
            odd = 2*i + 1;
            if (odd > runner) {
                found = false;
                int u = 0;
                while (!found) {
                    if (copyPaths[u][0] == 0 & copyPaths[u][1] == 0) found = true;
                    else u++;
                }
                copyPaths[u][0] = runner;
                if (!lastInComp) copyPaths[u][2] = runner+1;
                else copyPaths[u][2] = compbegin;
                return modifyPaths(copyPaths,components,runner+1,numbers);
            }
            else {
                found = false;
                int u = 0;
                while (!found) {
                    if (copyPaths[u][0] == odd) found = true;
                    else u++;
                }
                copyPaths[u][1] = runner;
                runner++;
                int nextrun = runner;
                if (lastInComp) runner = compbegin;
                copyPaths[u][3] = runner;
                copyPaths = modifyPaths(copyPaths,components,nextrun,numbers);
                if (copyPaths != null) return copyPaths;
                else {
                    int [][] cop2Paths = copyOf(paths);
                    cop2Paths[u][3] = nextrun - 1;
                    cop2Paths[u][1] = runner;
                    return modifyPaths(cop2Paths,components,nextrun,numbers);
                }
            }
        }
        else {
            int i = (runner - 1)/2;
            int even = Math.abs(numbers[i]);
            if (even > runner) {
                boolean found = false;
                int u = 0;
                while (!found) {
                    if (copyPaths[u][0] == 0 & copyPaths[u][1] == 0) found = true;
                    else u++;
                }
                copyPaths[u][0] = runner;
                if (!lastInComp) copyPaths[u][2] = runner+1;
                else copyPaths[u][2] = compbegin;
                return modifyPaths(copyPaths,components,runner+1,numbers);
            }
            else {
                boolean found = false;
                int u = 0;
                while (!found) {
                    if (copyPaths[u][0] == even) found = true;
                    else u++;
                }
                copyPaths[u][1] = runner;
                runner++;
                int nextrun = runner;
                if (lastInComp) runner = compbegin;
                copyPaths[u][3] = runner;
                copyPaths = modifyPaths(copyPaths,components,nextrun,numbers);
                if (copyPaths != null) return copyPaths;
                else {
                    int [][] cop2Paths = copyOf(paths);
                    cop2Paths[u][3] = nextrun - 1;
                    cop2Paths[u][1] = runner;
                    return modifyPaths(cop2Paths,components,nextrun,numbers);
                }
            }
        }
    }
    
    private static int[][] copyOf(int[][] orig) {
        int[][] copy = new int[orig.length][4];
        for (int i = 0; i < orig.length; i++) {
            System.arraycopy(orig[i], 0, copy[i], 0, 4);
        }
        return copy;
    }
    
    private static boolean contains(int[] path, int o, int e) {
        boolean conto = false;
        boolean conte = false;
        for (int r : path) {
            if (r == o) conto = true;
            if (r == e) conte = true;
        }
        return conto & conte;
    }
    
    public static Link obtainLink(String dtcode) {
        Link link;
        ArrayList<Integer> numbers = new ArrayList<Integer>();
        boolean ok = true;
        int u = 0;
        int v = 0;
        String number;
        dtcode = dtcode+" ";
        while (ok & u < dtcode.length()) {
            if (dtcode.charAt(u)== ' ') {
                number = dtcode.substring(v, u);
                v = u+1;
                if (number.length() > 0) {
                    try {
                        int numb = Integer.parseInt(number);
                        numbers.add(numb);
                    }
                    catch(NumberFormatException e) {
                        ok = false;
                    }
                }
            }
            u++;
        }
        if (!ok) return null;
        int k = numbers.size();
        ArrayList<Integer> check = new ArrayList<Integer>();
        for (int i = 0; i < 2*k; i++) check.add(i+1);
        int i = 0;
        while (i < k & ok) {
            if (!check.contains(Math.abs(numbers.get(i)))|numbers.get(i)%2 !=0) ok = false;
            else i++;
        }
        if (!ok) return null;
        int[] crossings = new int[numbers.size()];
        int[][] paths = new int[crossings.length][4];
        paths[0][0] = 1;
        paths[0][2] = 2;
        paths = modifyPaths(paths,2,numbers);
        if (paths == null) return null;
        for (int j = 0; j < numbers.size(); j++) {
            int odd = 2*j+1;
            int even = odd+1;
            boolean found = false;
            int w = 0;
            while (!found) {
                if (contains(paths[w],odd,even)) found = true;
                else w++;
            }
            if (paths[w][0] == odd | paths[w][2] == odd) {
                if (numbers.get(j) < 0) crossings[w] = 1;
                else crossings[w] = -1;
            }
            else {
                if (numbers.get(j) < 0) crossings[w] = -1;
                else crossings[w] = 1;
            }
        }
        link = new Link(crossings,paths);
        return link;
    }
    
    private static int[][] modifyPaths(int[][] paths, int runner, ArrayList<Integer> numbers) {
        if (!isPlanar(paths,runner)) return null;
        if (runner > 2 * paths.length) return paths;
        int [][] copyPaths = copyOf(paths);
        int odd;
        if (runner % 2 == 0) {
            boolean found = false;
            int i = 0;
            while (!found) {
                if (Math.abs(numbers.get(i)) == runner) found = true;
                else i++;
            }
            odd = 2*i + 1;
            if (odd > runner) {
                found = false;
                int u = 0;
                while (!found) {
                    if (copyPaths[u][0] == 0 & copyPaths[u][1] == 0) found = true;
                    else u++;
                }
                copyPaths[u][0] = runner;
                copyPaths[u][2] = runner+1;
                return modifyPaths(copyPaths,runner+1,numbers);
            }
            else {
                found = false;
                int u = 0;
                while (!found) {
                    if (copyPaths[u][0] == odd) found = true;
                    else u++;
                }
                copyPaths[u][1] = runner;
                runner++;
                int nextrun = runner;
                if (runner > 2*paths.length) runner = 1;
                copyPaths[u][3] = runner;
                copyPaths = modifyPaths(copyPaths,nextrun,numbers);
                if (copyPaths != null) return copyPaths;
                else {
                    int [][] cop2Paths = copyOf(paths);
                    cop2Paths[u][3] = nextrun - 1;
                    cop2Paths[u][1] = runner;
                    return modifyPaths(cop2Paths,nextrun,numbers);
                }
            }
        }
        else {
            int i = (runner - 1)/2;
            int even = Math.abs(numbers.get(i));
            if (even > runner) {
                boolean found = false;
                int u = 0;
                while (!found) {
                    if (copyPaths[u][0] == 0 & copyPaths[u][1] == 0) found = true;
                    else u++;
                }
                copyPaths[u][0] = runner;
                copyPaths[u][2] = runner+1;
                return modifyPaths(copyPaths,runner+1,numbers);
            }
            else {
                boolean found = false;
                int u = 0;
                while (!found) {
                    if (copyPaths[u][0] == even) found = true;
                    else u++;
                }
                copyPaths[u][1] = runner;
                runner++;
                int nextrun = runner;
                if (runner > 2*paths.length) runner = 1;
                copyPaths[u][3] = runner;
                copyPaths = modifyPaths(copyPaths,nextrun,numbers);
                if (copyPaths != null) return copyPaths;
                else {
                    int [][] cop2Paths = copyOf(paths);
                    cop2Paths[u][3] = nextrun - 1;
                    cop2Paths[u][1] = runner;
                    return modifyPaths(cop2Paths,nextrun,numbers);
                }
            }
        }
    }
    
    private static int[][] modifyPaths(int[][] paths, ArrayList<ArrayList<Integer>> codes, int runner) {
        if (!isGaussPlanar(paths)) return null;
        if (runner > 2 * paths.length) return paths;
        int [][] copyPaths = copyOf(paths);
        int comp = 0;
        int size = codes.get(comp).size();
        while (size < runner ) {
            comp++;
            size = size + codes.get(comp).size();
        }
        int bsize = size - codes.get(comp).size();
        int cross = codes.get(comp).get(runner-bsize-1);
        if (cross > 0) {
            int newrunner = runner+1;
            if (size == runner) newrunner = bsize+1;
            copyPaths[cross-1][3] = runner;
            copyPaths[cross-1][1] = newrunner;
            copyPaths = modifyPaths(copyPaths,codes,runner+1);
            if (copyPaths != null) return copyPaths;
            else {
                copyPaths = copyOf(paths);
                copyPaths[cross-1][1] = runner;
                copyPaths[cross-1][3] = newrunner;
                return modifyPaths(copyPaths,codes,runner+1);
            }
        }
        else {
            int newrunner = runner+1;
            if (size == runner) newrunner = bsize+1;
            copyPaths[-cross-1][0] = runner;
            copyPaths[-cross-1][2] = newrunner;
            copyPaths = modifyPaths(copyPaths,codes,runner+1);
            if (copyPaths != null) return copyPaths;
            else {
                copyPaths = copyOf(paths);
                copyPaths[-cross-1][2] = runner;
                copyPaths[-cross-1][0] = newrunner;
                return modifyPaths(copyPaths,codes,runner+1);
            }
        }
    }
    
    private static boolean isGaussPlanar(int[][] paths) {
        boolean [][] checkedSurfaces = new boolean[paths.length][4];
        int discs = 0;
        int runner = 1;
        int spos1 = 0;
        int spos2 = 0;
        boolean okay = false;
        while (!okay) {
            if (paths[spos1][spos2] == runner) okay = true;
            else {
                spos2++;
                if (spos2>3) {
                    spos2 = 0;
                    spos1++;
                }
            }
        }
        int twice = 0;
        for (int i = 0; i < paths.length; i++) 
            if (paths[i][0] == 0 & paths[i][1] == 0 & 
                    paths[i][2] == 0 & paths[i][3] == 0) {
                for (int j = 0; j < 4; j++) checkedSurfaces[i][j] = true;
            }
        int pos1 = spos1;
        int pos2 = spos2;
        while (notAll(checkedSurfaces)) {
            checkedSurfaces[pos1][pos2] = true;
            pos2 = (pos2+1)%4;
            runner = paths[pos1][pos2];
            if (runner >= 1 ) {
                boolean found = false;
                int i = 0;
                int j = 0;
                while (!found & i < paths.length) {
                    j = 0;
                    while (!found & j < 4) {
                        if (paths[i][j] == runner & (!(i == pos1 & j == pos2)))
                            found = true;
                        else j++;
                    }
                    if (!found) i++;
                }
                if (found) {
                    pos1 = i;
                    pos2 = j;
                    twice++;
                }
            }
            if (checkedSurfaces[pos1][pos2]) {
                discs++;
                boolean found = false;
                pos1 = 0;
                while (!found & pos1 < paths.length) {
                    pos2 = 0;
                    while (!found & pos2 < 4) {
                        if (!checkedSurfaces[pos1][pos2]) found = true;
                        else pos2++;
                    }
                    if (!found) pos1++;
                }
            }
        }
        int vme = paths.length;
        for (int i = 0; i < paths.length; i++) if (paths[i][0]==0 & paths[i][3]==0) vme--;
        return (vme+discs-(twice/2) >= 2);
    }
    
    public static LinkData enterGaussCode(JFrame knob, Comparer comparer) {
        EnterData data = new EnterData(knob, "Gauss-Code", "Gauss-Code : ", true);
        if ("".equals(data.theString)) return null;
        Link theLink = obtainGaussLink(removeSpaces(data.theString));
        if (theLink == null) return null;
        String name = data.theName;
        if (name.length() == 0) {
            if (theLink.components() == 1) name = "Knot";
            else name = "Link";
        }
        if (data.combin) theLink = theLink.girthMinimize();
        LinkData theNewData = new LinkData(name,theLink,comparer);
        return theNewData;
    }
    
    private static String removeSpaces(String withSpaces) {
        String withOutSpaces = "";
        for (int i = 0; i < withSpaces.length(); i++) {
            if (withSpaces.charAt(i)!= ' ') withOutSpaces = withOutSpaces+withSpaces.charAt(i);
        }
        return withOutSpaces;
    }
    
    private static Link obtainGaussLink(String gacode) {
        Link link;
        ArrayList<ArrayList<Integer>> theCode = getTheCode(gacode);
        if (theCode == null) return null;
        theCode = alternate(theCode);
        if (failedTest(theCode)) return null;
        int size = 0;
        for (ArrayList<Integer> code : theCode) size = size + code.size();
        int[] crossings = new int[size/2];
        for (int i = 0; i < crossings.length; i++) crossings[i]=1;
        int[][] paths = new int[size/2][4];
        int st = theCode.get(0).get(0);
        if (st > 0) {
            paths[st-1][3] = 1;
            paths[st-1][1] = 2;
        }
        else {
            paths[-st-1][0] = 1;
            paths[-st-1][2] = 2;
        }
        paths = modifyPaths(paths,theCode,2);
        if (paths == null) return null;
        link = new Link(crossings,paths);
        return link;
    }
    
    private static ArrayList<ArrayList<Integer>> alternate(ArrayList<ArrayList<Integer>> theCode) {
        if (notAllPositive(theCode)) return theCode;
        boolean sign;
        ArrayList<ArrayList<Integer>> newCode = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> checked = new ArrayList<Integer>();
        for (ArrayList<Integer> comp : theCode) {
            ArrayList<Integer> newcomp = new ArrayList<Integer>();
            int u = starter(comp,checked);
            sign = otherSignOf(comp.get(u),newCode);
            for (int v = 0; v < comp.size(); v++) {
                int r = comp.get(u);
                if (!checked.contains(r)) checked.add(r);
                if (sign) newcomp.add(r);
                else newcomp.add(-r);
                u = (u+1) % comp.size();
                sign = !sign;
            }
            newCode.add(newcomp);
        }
        return newCode;
    }
    
    private static boolean otherSignOf(int v, ArrayList<ArrayList<Integer>> code) {
        boolean found = false;
        int i = 0;
        int j = 0;
        while (!found && i < code.size()) {
            j = 0;
            while (!found && j < code.get(i).size()) {
                if (code.get(i).get(j) == v || code.get(i).get(j) == -v) found = true;
                else j++;
            }
            if (!found) i++;
        }
        if (!found) return true;
        return (code.get(i).get(j) < 0); 
    }
    
    private static boolean notAllPositive(ArrayList<ArrayList<Integer>> theCode) {
        boolean allpos = true;
        int i = 0;
        while (allpos && i < theCode.size()) {
            int j = 0;
            while (allpos && j < theCode.get(i).size()) {
                if (theCode.get(i).get(j) < 0) allpos = false;
                j++;
            }
            i++;
        }
        return !allpos;
    }
    
    private static boolean failedTest(ArrayList<ArrayList<Integer>> theCode) {
        boolean test = false;
        int size = 0;
        for (ArrayList<Integer> liset : theCode) size = size+liset.size();
        if (size/2 == 0 ) return true;
        for (int u = 1; u <= size/2; u++) {
            if (!isInThere(theCode,u)) test = true;
            if (!isInThere(theCode,-u)) test = true;
        }
        return test;
    }
    
    private static boolean isInThere(ArrayList<ArrayList<Integer>> theCode, int y) {
        boolean found = false;
        int u = 0;
        while (!found && u < theCode.size()) {
            if (theCode.get(u).contains(y)) found = true;
            u++;
        }
        return found;
    }
    
    private static ArrayList<ArrayList<Integer>> getTheCode(String code) {
        ArrayList<ArrayList<Integer>> theCode = new ArrayList<ArrayList<Integer>>();
        boolean error = false;
        code = ":"+code+":";
        ArrayList<Integer> commas = new ArrayList<Integer>();
        ArrayList<Integer> colons = new ArrayList<Integer>();
        for (int i = 0; i < code.length(); i++) {
            if (code.charAt(i)==',') commas.add(i);
            if (code.charAt(i)==':') {
                colons.add(i);
                commas.add(i);
            }
        }
        int u = 1;
        for (int k = 1; k < colons.size(); k++) {
            ArrayList<Integer> numbers = new ArrayList<Integer>();
            int colon = colons.get(k);
            boolean keepgoing = true;
            int firstcomma = colons.get(k-1);
            while (keepgoing) {
                int seconcomma = commas.get(u);
                String number = code.substring(firstcomma+1, seconcomma);
                try {
                    int numb = Integer.parseInt(number);
                    numbers.add(numb);
                }
                catch(NumberFormatException e) {
                    error = true;
                }
                if (seconcomma == colon) keepgoing = false;
                u++;
                firstcomma = seconcomma;
            }
            theCode.add(numbers);
        }
        if (error) return null;
        return theCode;
    }
    
    private static int starter(ArrayList<Integer> comp, ArrayList<Integer> checked) {
        int y = 0;
        boolean found = false;
        while (!found && y < comp.size()) {
            if (checked.contains(comp.get(y))) found = true;
            else y++;
        }
        if (found) return y;
        return 0;
    }
    
    public static LinkData enterBraidCode(String theString, String name, JFrame knob, 
            boolean combine, Comparer comparer) {
        if (theString == null) {
            EnterData data = new EnterData(knob, "Braid Code", "Braid : ", true);
            if (data.theString == null) return null;
            theString = data.theString;
            name = data.theName;
            combine = data.combin;
            
        }
        Link theLink = obtainBraid(theString);
        if (theLink == null) return null;
        if (name.length() == 0) {
            if (theLink.components() == 1) name = "Knot";
            else name = "Link";
        }
        if (combine) theLink = theLink.girthMinimize();
        LinkData theNewData = new LinkData(name,theLink,comparer);
        return theNewData;
    }
    
    private static Link obtainBraid(String braid) {
        Link theBraid = null;
        char[] chars = braid.toCharArray();
        if (chars.length < 1) return null;
        ArrayList<Integer> crossins = new ArrayList<Integer>();
        ArrayList<ArrayList<Integer>> pathz = new ArrayList<ArrayList<Integer>>();
        int numberBraids = largestGen(chars);
        if (numberBraids < 2 | numberBraids > 10) return theBraid;
        int[] ends = new int[numberBraids];
        int lastNumber = numberBraids;
        for (int i = 1; i <= numberBraids; i++) ends[i-1] = i;
        for (int j = 0; j < chars.length; j++) {
            int e = Character.getNumericValue(chars[j])-10;
            if (e < 0 | e >= numberBraids) return theBraid;
            if (Character.isLowerCase(chars[j])) crossins.add(1);
            else crossins.add(-1);
            ArrayList<Integer> path = new ArrayList<Integer>();
            path.add(ends[e+1]);
            path.add(lastNumber+2);
            path.add(lastNumber+1);
            path.add(ends[e]);
            ends[e] = lastNumber+1;
            ends[e+1] = lastNumber+2;
            lastNumber = lastNumber+2;
            pathz.add(path);
        }
        ArrayList<Integer> loops = new ArrayList<Integer>();
        for (int r = 0; r < ends.length; r++) {
            if (ends[r] == r+1) {
                loops.add(r+1);
                ends[r] = 0;
            }
        }
        int[] crossings = new int[crossins.size()];
        int[][] paths = new int[crossins.size()][4];
        for (int i = 0; i < crossins.size(); i++) {
            crossings[i] = crossins.get(i);
            for (int j = 0; j < 4; j++) paths[i][j] = difference(loops, pathz.get(i).get(j));
        }
        for (int e = 0; e < ends.length; e++) ends[e] = difference(loops, ends[e]);
        ends = removeZeroes(ends);
        for (int[] path : paths) {
            for (int j = 0; j < 4; j++) {
                if (contains(ends,path[j])) {
                    for (int[] pat : paths) reduce(pat, path[j]);
                    int t = path[j];
                    path[j] = numberOf(ends,path[j]);
                    reduce(ends, t);
                    ends[path[j]-1] = -1;
                    lastNumber--;
                }
            }
        }
        theBraid = new Link(crossings, paths, loops.size(), null);
        return theBraid;
    }
    
    private static int[] removeZeroes(int[] ends) {
        ArrayList<Integer> nendz = new ArrayList<Integer>();
        for (int u : ends) if (u != 0) nendz.add(u);
        int[] nends = new int[nendz.size()];
        for (int i = 0; i < nends.length; i++) nends[i] = nendz.get(i);
        return nends;
    } 
    
    private static int difference(ArrayList<Integer> loops, int val) {
        int y = loops.size()-1;
        while (y >= 0) {
            if (val >= loops.get(y)) val--;
            y--;
        }
        return val;
    }
    
    private static int largestGen(char[] chars) {
        int large = 0;
        for (int i = 0; i < chars.length; i++) {
            if (large < Character.getNumericValue(chars[i])) large = Character.getNumericValue(chars[i]);
        }
        return (large-8);
    }
    
    private static void reduce(int[] path, int value) {
        for (int j = 0; j < path.length; j++) if (path[j] > value) path[j] = path[j]-1;
    }
    
    private static int numberOf(int[] aray, int entry) {
        boolean found = false;
        int i = 0;
        int n = -1;
        while (!found & i < aray.length) {
            if (aray[i] == entry) found = true;
            else i++;
        }
        if (found) n = i+1;
        return n;
    }
    
    private static boolean contains(int[] aray, int u) {
        boolean inthere = false;
        int t = 0;
        while (t < aray.length & !inthere) {
            if (aray[t] == u) inthere = true;
            else t++;
        }
        return inthere;
    }
}
