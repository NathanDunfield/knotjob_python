/*

Copyright (C) 2021-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import knotjob.dialogs.DialogWrap;
import knotjob.filters.Filter;
import knotjob.filters.FilterIdentifier;
import knotjob.homology.HomologyInfo;
import knotjob.homology.evenkhov.EvenKhovCalculator;
import knotjob.homology.evenkhov.sinv.SInvariantCalculator;
import knotjob.homology.evenkhov.sinv.SqOneCalculator;
import knotjob.homology.evenkhov.sinv.GradedCalculator;
import knotjob.homology.evenkhov.sinv.SpectralCalculator;
import knotjob.homology.oddkhov.OddKhovCalculator;
import knotjob.homology.oddkhov.sinv.SqOneOddCalculator;
import knotjob.homology.oddkhov.unified.sinv.CompleteSCalculator;
import knotjob.homology.oddkhov.unified.sinv.SqOneSumCalculator;
import knotjob.homology.slthree.SlThreeHomCalculator;
import knotjob.homology.slthree.univ.SlTSInvariantCalculator;
import knotjob.invariants.SignatureCalculator;
import knotjob.links.LinkData;
import knotjob.polynomial.PoincarePolynomial;
import knotjob.stabletype.sinv.SqTwoCalculator;
import knotjob.stabletype.sinv.SqTwoOddCalculator;

/**
 *
 * @author Dirk
 * 
 * with help from Nathan D
 */
public class KnotJobCommand {

    private String[] arguments;
    private final ArrayList<String> fileNames;
    private final ArrayList<String> commands;
    private final ArrayList<Filter> filters;
    private final ArrayList<ArrayList<LinkData>> theLinks;
    private CountDownLatch countDown;
    private boolean sqOneOdd;
    private boolean blsOdd;
    private boolean sqOneEven;
    private boolean sqOneSum;
    private boolean comr;
    private boolean sqTwoEven;
    private boolean sqTwoOdd;
    private boolean sqTwoOde;
    private boolean sGraded;
    private boolean signature;
    private boolean printScreen;
    private boolean printFile;
    private boolean csvFile;
    private boolean printCompDones;
    private boolean khevenred;
    private boolean khevenunr;
    private boolean help;
    private int blsmod;
    private int filteredCount;
    private String csvDirectory;
    private final ArrayList<Integer> sInvs;
    private final ArrayList<Integer> slInvs;
    private final ArrayList<Integer> evenKhovs;
    private final ArrayList<Integer> oddKhovs;
    private final ArrayList<Integer> sltKhovs;
    private final ArrayList<Integer> allTorsionU;
    private final ArrayList<Integer> allTorsionR;
    private final ArrayList<Integer> allTorsionO;
    private final ArrayList<Integer> allTorsionSlTU;
    private final ArrayList<Integer> allTorsionSlTR;
    private final ArrayList<Integer> xtorts;
    private final Options options;
    
    public KnotJobCommand(String[] args) {
        arguments = args;
        fileNames = new ArrayList<String>();
        commands = new ArrayList<String>();
        filters = new ArrayList<Filter>();
        theLinks = new ArrayList<ArrayList<LinkData>>();
        sInvs = new ArrayList<Integer>();
        slInvs = new ArrayList<Integer>(); 
        evenKhovs = new ArrayList<Integer>();
        oddKhovs = new ArrayList<Integer>();
        sltKhovs = new ArrayList<Integer>();
        allTorsionU = new ArrayList<Integer>();
        allTorsionR = new ArrayList<Integer>();
        allTorsionO = new ArrayList<Integer>();
        allTorsionSlTU = new ArrayList<Integer>();
        allTorsionSlTR = new ArrayList<Integer>();
        xtorts = new ArrayList<Integer>();
        options = new Options();
        options.setTimeInfo(false);
        printScreen = true;
        printFile = true;
        help = false;
    }

    public void getStarted() {
        getTheCommandsAndFiles();
        // arguments ending in .txt etc are considered filenames, arguments beginning with
        // "-" are considered commands.
        loadTheLinks();
        // each filename produces an ArrayList of LinkData, and these arraylists are now 
        // in theLinks.
        identifyCommands();
        // the booleans sqOneOdd and sqOneEven are now set depending on whether the 
        // command was found, sInvs contains those primes (or 0) for which s-invariant
        // should be calculated, similar with evenKhovs and oddKhovs.
        calculate();
    }

    private void resetCommands() {
        sqOneOdd = false;
        blsOdd = false;
        sqOneEven = false;
        sqOneSum = false;
        comr = false;
        sqTwoEven = false;
        sqTwoOdd = false;
        sqTwoOde = false;
        sGraded = false;
        signature = false;
        //printScreen = false;
        //printFile = false;
        csvFile = false;
        printCompDones = false;
        khevenred = false;
        khevenunr = false;
        help = false;
        commands.clear();
        filters.clear();
        sInvs.clear();
        slInvs.clear(); 
        evenKhovs.clear();
        oddKhovs.clear();
        sltKhovs.clear();
        allTorsionU.clear();
        allTorsionR.clear();
        allTorsionO.clear();
        allTorsionSlTU.clear();
        allTorsionSlTR.clear();
        xtorts.clear();
    }
    
    private void loadTheLinks() {
        for (int i = 0; i < fileNames.size(); i++) {
            File[] files = new File[1];
            files[0] = new File(fileNames.get(i));
            countDown = new CountDownLatch(1);
            LinkLoader loader = new LinkLoader(files, countDown);
            loader.start();
            try {
                countDown.await();
            }
            catch (InterruptedException ex) {
                Logger.getLogger(KnotJobCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
            theLinks.add(loader.getLinks());
        } 
    }

    private void getTheCommandsAndFiles() {
        for (String command : arguments) {
            if (acceptableFileName(command)) fileNames.add(command);
            if (command.startsWith("-")) commands.add(command);
        }
    }

    private boolean acceptableFileName(String command) {
        if (command.endsWith(".txt")) return true;
        if (command.endsWith(".dtc")) return true;
        if (command.endsWith(".adc")) return true;
        if (command.endsWith(".brd")) return true;
        if (command.endsWith(".kts")) return true;
        return command.endsWith(".kjb");
    }

    private void identifyCommands() {
        for (String command : commands) {
            if (command.startsWith("-s")) checkSInvariant(command);
            if (command.startsWith("-k")) checkKhovInvariant(command);
            if (command.startsWith("-f")) checkFilter(command);
            if (command.startsWith("-n")) checkPrintOptions(command);
            if (command.equals("-h")) help = true;
            if (command.startsWith("-csv")) {
                printFile = false;
                csvFile = true;
                if (command.length()>4) csvDirectory = command.substring(4);
                else csvDirectory = null;
            }
            if (command.equals("-print-dones")) {
                printCompDones = true;
            }
        }
        removeRedundancies(evenKhovs);
        removeRedundancies(oddKhovs);
        options.setKhovRed(khevenred);
        options.setKhovUnred(khevenunr);
    }

    private void checkSInvariant(String command) {
        if ("-sqo".equals(command) || "-sq1o".equals(command)) {
            sqOneOdd = true;
            return;
        }
        if ("-sqe".equals(command) || "-sq1e".equals(command)) {
            sqOneEven = true;
            return;
        }
        if ("-sqs".equals(command) || "-sq1s".equals(command)) {
            sqOneSum = true;
            return;
        }
        if ("-scr".equals(command)) {
            comr = true;
            return;
        }
        if ("-sq2e".equals(command)) {
            sqTwoEven = true;
            return;
        }
	if ("-sq2o0".equals(command)) {
            sqTwoOdd = true;
            return;
        }
        if ("-sq2o1".equals(command)) {
            sqTwoOde = true;
            return;
        }
        if (command.length()>=5 && "-sbls".equals(command.substring(0, 5))) {
            blsOdd = true;
            int pwr = 3;
            try {
                pwr = Integer.parseInt(command.substring(5));
            }
            catch(NumberFormatException ex) {
                // pwr will remain 3
            }
            if (pwr < 3) pwr = 3;
            if (pwr > 15) pwr = 15;
            blsmod = 2; 
            for (int i = 1; i < pwr; i++) blsmod = blsmod*2;
            return;
        }
	if ("-sgr".equals(command)) {
            sGraded = true;
            return;
        }
        if ("-sgn".equals(command)) {
            signature = true;
            return;
        }
        if (command.startsWith("-sl")) {
            int characteristic = getSNumber(command.substring(1));
            if (characteristic >= 0 && !slInvs.contains(characteristic)) slInvs.add(characteristic);
            return;
        }
        int characteristic = getSNumber(command);
        if (characteristic >= 0 && !sInvs.contains(characteristic)) sInvs.add(characteristic);
    }

    private void checkKhovInvariant(String command) {
        if (command.startsWith("-kr")) {
            int coef = getKhNumber(command);
            khevenred = true;
            if (coef >= 0 && !evenKhovs.contains(coef)) evenKhovs.add(coef);
        }
        if (command.startsWith("-ku")) {
            int coef = getKhNumber(command);
            khevenunr = true;
            if (coef >= 0 && !evenKhovs.contains(coef)) evenKhovs.add(coef);
        }
        if (command.startsWith("-kb")) {
            int coef = getKhNumber(command);
            khevenred = true;
            khevenunr = true;
            if (coef >= 0 && !evenKhovs.contains(coef)) evenKhovs.add(coef);
        }
        if (command.startsWith("-ko")) {
            int coef = getKhNumber(command);
            if (coef >= 0 && !oddKhovs.contains(coef)) oddKhovs.add(coef);
        }
        if (command.startsWith("-kx")) {
            int coef = getKhNumber(command);
            if (coef >= 0 && !xtorts.contains(coef)) xtorts.add(coef);
        }
        if (command.startsWith("-ks")) {
            int coef = getKhNumber(command);
            if (coef >= 0 && !sltKhovs.contains(coef)) sltKhovs.add(coef);
        }
    }

    private void checkFilter(String command) {
        FilterIdentifier id = new FilterIdentifier(command.substring(2), filters);
        Filter fil = id.getFilter();
        if (fil != null) filters.add(fil);
    }
    
    private void checkPrintOptions(String command) {
        if ("-ns".equals(command)) printScreen = false;
        if ("-nf".equals(command)) printFile = false;
    }
    
    private int getSNumber(String command) {
        int ch;
        try {
            ch = Integer.parseInt(command.substring(2));
        }
        catch (NumberFormatException ne) {
            ch = -1;
        }
        if (ch <= 0) return ch;
        if (acceptedPrime(ch)) return ch;
        return -1;
    }

    private boolean acceptedPrime(int ch) {
        boolean accepted = false;
        int i = 0;
        while (!accepted && i < options.getPrimes().size()) {
            if (ch == options.getPrimes().get(i)) accepted = true;
            else i++;
        }
        return accepted;
    }

    private int getKhNumber(String command) {
        int ch;
        try {
            ch = Integer.parseInt(command.substring(3));
        }
        catch (NumberFormatException ne) {
            ch = -1;
        }
        if (ch <= 1) return ch;
        if (acceptedPrimePower(ch)) return ch;
        return -1;
    }

    private boolean acceptedPrimePower(int ch) {
        boolean accepted = false;
        int i = 0;
        while (!accepted && i < options.getPrimes().size()) {
            int prime = options.getPrimes().get(i);
            if (ch%prime == 0 && powerOkay(ch, prime)) accepted = true;
            else i++;
        }
        return accepted;
    }

    private boolean powerOkay(int ch, int prime) {
        int maxPower = options.getPowers().get(options.getPrimes().indexOf(prime));
        int i = 1;
        int p = prime;
        boolean okay = false;
        while (!okay && i <= maxPower) {
            if (ch == p) okay = true;
            else {
                i++;
                p = p * prime;
            }
        }
        return okay;
    }

    private void removeRedundancies(ArrayList<Integer> khovs) {
        if (khovs.contains(0)) {
            if (khovs.size() == 1) return;
            khovs.clear();
            khovs.add(0);
            return;
        }
        for (int prime : options.getPrimes()) {
            ArrayList<Integer> powers = new ArrayList<Integer>();
            for (int u : khovs) {
                if (u % prime == 0) powers.add(u);
            }
            Collections.sort(powers);
            for (int i = 0; i < powers.size()-1; i++) khovs.remove(powers.get(i));
        }
    }

    private void calculate() {
        if (help) {
            schreiNachHilfe();
            return;
        }
        if (fileNames.isEmpty()) { // if there are no files, you can enter a pd-code, or hit return to end the program.
            printFile = false;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String linkString;
                while (((linkString=reader.readLine())!=null) & (linkString.length() != 0)){
                    if (linkString.charAt(0) == '-') {
                        resetCommands();
                        arguments = linkString.split("\\s+");
                        getTheCommandsAndFiles();
                        identifyCommands();
                    }
                    else {
                        readOneLink(linkString);
                        doCalculations(false);
                        printTheStuff(0, null);
                        theLinks.remove(0);
                    }
                }
            }
             catch (IOException ex) {
                Logger.getLogger(KnotJobCommand.class.getName()).log(Level.SEVERE, null, ex);
             }
        }
        else {
            doCalculations(true);
            printOutInfo();
            if (!filters.isEmpty()) System.out.println("Total number : "+filteredCount);
        }
    }
    
    private void readOneLink(String linkString) {
        theLinks.add(new ArrayList<LinkData>(1));
        LinkData theData = knotjob.links.LinkCreator.enterPDCode(linkString, "Knot", true, null, null);
        if (theData != null) theLinks.get(0).add(theData);
        // Ideally, this would check whether linkString is a planar diagram, or a DT-code, or whatever.
        // At the moment, it has to be a planar diagram. 
    }
    
    private void doCalculations(boolean extra) {
        int counter = sInvs.size()+slInvs.size()+evenKhovs.size()+oddKhovs.size()
                +xtorts.size()+sltKhovs.size();
        if (sqOneOdd) counter++;
        if (sqOneEven) counter++;
        if (sqOneSum) counter++;
        if (comr) counter++;
        if (blsOdd) counter++;
        if (sqTwoEven) counter++;
	if (sqTwoOdd) counter++;
	if (sqTwoOde) counter++;
	if (sGraded) counter++;
        if (signature) counter++;
        countDown = new CountDownLatch(counter * theLinks.size());
        int count = -1;
        for (ArrayList<LinkData> links : theLinks) {
            count++;
            String extraInfo = null;
            for (int ch : sInvs) {
                if (extra) extraInfo = "S-invariant mod "+ch+" of file "
                                +fileNames.get(count)+" finished.";
                SInvariantCalculator calcS = new SInvariantCalculator(links, ch, options, 
                        new DialogWrap(countDown, extraInfo));
                // the String in DialogWrap can be set null to avoid printout.
                calcS.start();
            }
            if (sqOneEven) {
                if (extra) extraInfo = "Sq^1 even of file "
                                +fileNames.get(count)+" finished.";
                SqOneCalculator calculator = new SqOneCalculator(links, options, 
                        new DialogWrap(countDown, extraInfo));
                calculator.start();
            }
            if (sqOneOdd) {
                if (extra) extraInfo = "Sq^1 odd of file "
                                +fileNames.get(count)+" finished.";
                SqOneOddCalculator calculator = new SqOneOddCalculator(links, options, 
                        new DialogWrap(countDown, extraInfo), 4);
                calculator.start();
            }
            if (sqOneSum) {
                if (extra) extraInfo = "Sq^1 sum of file "
                        +fileNames.get(count)+" finished.";
                SqOneSumCalculator calculator = new SqOneSumCalculator(links, options,
                        new DialogWrap(countDown, extraInfo));
                calculator.start();
            }
            if (comr) {
                if (extra) extraInfo = "ULS reduced of file "
                        +fileNames.get(count)+" finished.";
                CompleteSCalculator calculator = new CompleteSCalculator(links, options,
                        new DialogWrap(countDown, extraInfo), true);
                calculator.start();
            }
            if (blsOdd) {
                if (extra) extraInfo = "BLS odd of file "
                        +fileNames.get(count)+" finished.";
                SqOneOddCalculator calculator = new SqOneOddCalculator(links, options,
                        new DialogWrap(countDown, extraInfo), blsmod);
                calculator.start();
            }
            if (sqTwoEven) {
                if (extra) extraInfo = "Sq^2 even of file "
                                +fileNames.get(count)+" finished.";
                SqTwoCalculator calculator = new SqTwoCalculator(links, options,
                        new DialogWrap(countDown, extraInfo), 20, 10000, true);
                calculator.start();
            }
	    if (sqTwoOdd) {
                if (extra) extraInfo = "Sq^2 odd (eps=0) of file "
                                +fileNames.get(count)+" finished.";
                SqTwoOddCalculator calculator = new SqTwoOddCalculator(links, options,
			   new DialogWrap(countDown, extraInfo), 0);
                calculator.start();
            }
	    if (sqTwoOde) {
                if (extra) extraInfo = "Sq^2 odd (eps=1) of file "
                                +fileNames.get(count)+" finished.";
                SqTwoOddCalculator calculator = new SqTwoOddCalculator(links, options,
			   new DialogWrap(countDown, extraInfo), 1);
                calculator.start();
	    }
	    if (sGraded) {
                if (extra) extraInfo = "Graded s of file "
                                +fileNames.get(count)+" finished.";
                GradedCalculator calculator = new GradedCalculator(links, options, 
                        new DialogWrap(countDown, extraInfo));
                calculator.start();
            }
            if (signature) {
                if (extra) extraInfo = "Signature of file "
                                +fileNames.get(count)+" finished.";
                SignatureCalculator calculator = new SignatureCalculator(links, options,
                        new DialogWrap(countDown, extraInfo));
                calculator.start();
            }
            for (int co : evenKhovs) {
                if (extra) extraInfo = "Even Kh mod "+co+" of file "
                                +fileNames.get(count)+" finished.";
                EvenKhovCalculator calculator = new EvenKhovCalculator(links, co, options, 
                        new DialogWrap(countDown, extraInfo));
                calculator.start();
            }
            for (int co : oddKhovs) {
                if (extra) extraInfo = "Odd Kh mod "+co+" of file "
                                +fileNames.get(count)+" finished.";
                OddKhovCalculator calculator = new OddKhovCalculator(links, co, options, 
                        new DialogWrap(countDown, extraInfo));
                calculator.start();
            }
            for (int co : xtorts) {
                if (extra) extraInfo = "X-Tortion mod "+co+" of file "
                                +fileNames.get(count)+" finished.";
                SpectralCalculator calculator = new SpectralCalculator(links, options, 
                        new DialogWrap(countDown, extraInfo), co, true);
                calculator.start();
            }
            for (int co : sltKhovs) {
                if (extra) extraInfo = "sl_3 Kh mod "+co+" of file "
                                +fileNames.get(count)+" finished.";
                SlThreeHomCalculator calculator = new SlThreeHomCalculator(links, co, options, 
                        new DialogWrap(countDown, extraInfo));
                calculator.start();
            }
            for (int ch : slInvs) {
                if (extra) extraInfo = "sl_3 S-invariant mod "+ch+" of file "
                                +fileNames.get(count)+" finished.";
                SlTSInvariantCalculator calcS = new SlTSInvariantCalculator(links, ch, false, options, 
                        new DialogWrap(countDown, extraInfo));
                calcS.start();
            }
        }
        try {
            countDown.await(); // all these calculators are threads. Here we wait 
            // until they are all finished.
        } 
        catch (InterruptedException ex) {
            Logger.getLogger(KnotJobCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void printOutInfo() {
        for (int i = 0; i < theLinks.size(); i++) {
            String newFile = newFileName(fileNames.get(i));
            if (printScreen) System.out.println(newFile);
            File file = new File(newFile);
            try {
                FileWriter fw = null;
                PrintWriter pw = null;
                if (printFile || csvFile) {
                    fw = new FileWriter(file);
                    pw = new PrintWriter(fw);
                }
                printTheStuff(i, pw);
                if (printFile || csvFile) {
                    if (fw != null) fw.close();
                    if (pw != null) pw.close();
                }
            }
            catch (IOException e) {
                
            }
        }
    }
    
    private void khovanovOutput(HomologyInfo info, String extra, boolean doubl, PrintWriter pw, int k) {
        printOut(extra, info.poincarePolynomial().toString().substring(1), pw);
        ArrayList<PoincarePolynomial> torPolys = info.torsionPolynomials(doubl);
        if (csvFile) {
            ArrayList<Integer> tors = new ArrayList<Integer>();
            if (k == 0) tors = allTorsionU;
            if (k == 1) tors = allTorsionR;
            if (k == 2) tors = allTorsionO;
            if (k == 3) tors = allTorsionSlTU;
            if (k == 4) tors = allTorsionSlTR;
            for (int t : tors) {
                PoincarePolynomial poly = thePoly(t, torPolys);
                if (poly != null) printOut("Torsion of order "+poly.torsion()+" : ", poly.toString().substring(1), pw);
                else printOut(",", " ", pw);
            }
        } 
        else {
            for (PoincarePolynomial poly : torPolys) {
                printOut("Torsion of order "+poly.torsion()+" :", poly.toString(), pw);
            }
        }
    }
    
    private PoincarePolynomial thePoly(int t, ArrayList<PoincarePolynomial> torPolys) {
        int k = 0;
        while (k < torPolys.size()) {
            if (torPolys.get(k).torsion().intValue() == t) return torPolys.get(k);
            k++;
        }
        return null;
    } 
    
    private void printOut(String str, String str2, PrintWriter pw) {
        if (printScreen) System.out.println(str+str2);
        if (printFile) pw.println(str+str2);
        if (csvFile) {
            if (!"".equals(str)) str= ", ";
            pw.print(str+str2);
        }
    }

    private void printTheStuff(int i, PrintWriter pw) {
        if (csvFile) {
            String firstLine = firstLine(i);
            pw.println(firstLine);
        }
        for (LinkData data : theLinks.get(i)) {
            boolean filtered = filters.isEmpty();
            int l = 0;
            while (l < filters.size() && !filtered) { // if one of the filters produces true it will be filtered
                filtered = filters.get(l).linkIsFiltered(data);
                l++;
            }
            if (filtered) printData(data, pw);
        }
    }
    
    private void printData(LinkData data, PrintWriter pw) {
        filteredCount++;
        printOut("", data.name, pw);
        for (int s : sInvs) {
            printOut("S-Invariant mod "+s+" : ", data.sInvariant(s).toString(), pw);
        }
        for (int s : slInvs) {
            printOut("sl_3 S-Invariant mod "+s+" : ", data.sSlTInvariant(s, false).toString(), pw);
        }
        if (sqOneEven) printOut("Even Sq^1 Invariant : ", data.sqEven, pw);
        if (sqOneOdd) printOut("Odd Sq^1 Invariant : ", data.sqOdd, pw);
        if (sqOneSum) printOut("Sum Sq^1 Invariant : ", data.beta, pw);
        if (comr) printOut("Complete reduced LS-Invariant : ", data.cmpinv, pw);
        if (blsOdd) printOut("Odd BLS "+blsmod+" Invariant : ", data.bsOdd, pw);
        if (sqTwoEven) printOut("Even Sq^2 Invariant : ", data.sqtEven, pw);
        if (sqTwoOdd) printOut("Odd Sq^2 Invariant (eps=0) : ", data.sqtOdd, pw);
        if (sqTwoOde) printOut("Odd Sq^2 Invariant (eps=1) : ", data.sqtOde, pw);
        if (sGraded) printOut("Graded s-invariant : ", data.grsinv, pw);
        if (signature) printOut("Signature : ", data.signature, pw);
        for (int pp : evenKhovs) {
            if (pp == 0) {
                if (khevenunr) khovanovOutput(data.integralKhovHomology(false), 
                        "Integral unreduced Khovanov Homology : ", false, pw, 0);
                if (khevenred) khovanovOutput(data.integralKhovHomology(true), 
                        "Integral reduced Khovanov Homology : ", false, pw, 1);
            }
            if (pp == 1) {
                if (khevenunr) khovanovOutput(data.rationalKhovHomology(false), 
                        "Rational unreduced Khovanov Homology : ", false, pw, -1);
                if (khevenred) khovanovOutput(data.rationalKhovHomology(true), 
                        "Rational reduced Khovanov Homology : ", false, pw, -1);
            }
            if (pp > 1) {
                if (khevenunr) khovanovOutput(data.modKhovHomology(false, pp), 
                        "Unreduced Khovanov Homology mod "+pp+" : ", true, pw, -1);
                if (khevenred) khovanovOutput(data.modKhovHomology(true, pp), 
                        "Reduced Khovanov Homology mod "+pp+" : ", true, pw, -1);
            }
        }
        for (int pp : oddKhovs) {
            if (pp == 0) {
                khovanovOutput(data.integralOddKhHomology(), 
                        "Odd integral Khovanov Homology : ", false, pw, 2);
            }
            if (pp == 1) {
                khovanovOutput(data.rationalOddKhHomology(), 
                        "Odd rational Khovanov Homology : ", false, pw, -1);
            }
            if (pp > 1) {
                khovanovOutput(data.modOddKhHomology(pp), 
                        "Odd Khovanov Homology mod "+pp+" : ", true, pw, -1);
            }
        }
        for (int pp : sltKhovs) {
            if (pp == 0) {
                khovanovOutput(data.integralSlTHomology(false), 
                        "Unreduced integral sl_3 Homology : ", false, pw, 3);
                khovanovOutput(data.integralSlTHomology(true),
                        "Reduced integral sl_3 Homology : ", false, pw, 4);
            }
            /*if (pp == 1) { // to follow
                khovanovOutput(data.rationalOddKhHomology(), 
                        "Odd rational Khovanov Homology : ", false, pw, -1);
            }
            if (pp > 1) {
                khovanovOutput(data.modOddKhHomology(pp), 
                        "Odd Khovanov Homology mod "+pp+" : ", true, pw, -1);
            }// */
        }
        for (int co : xtorts) {
            printOut("Extortion Order mod "+co+" : ", data.xtortion(co), pw);
        }
        if (csvFile) pw.println();
        if (printCompDones) printOut("computation done", "", pw);
    }
    
    private String firstLine(int i) {
        String line = "knot_id";
        for (int s : sInvs) line = line+", S-invariant mod "+s;
        for (int s : slInvs) line = line+", sl_3 S-invariant mod "+s;
        if (sqOneEven) line = line+", Even Sq^1 Invariant";
        if (sqOneOdd) line = line+", Odd Sq^1 Invariant";
        if (sqOneSum) line = line+", Sum Sq^1 Invariant";
        if (comr) line = line+", Complete red LS-Invariant";
        if (blsOdd) line = line+", Odd BLS "+blsmod+" Invariant";
        if (sqTwoEven) line = line+", Even Sq^2 Invariant";
        if (sqTwoOdd) line = line+", Odd Sq^2 Invariant (eps=0)";
        if (sqTwoOde) line = line+", Odd Sq^2 Invariant (eps=1)";
        if (sGraded) line = line+", Graded s-invariant";
        if (signature) line = line+", Signature";
        if (khevenunr && evenKhovs.contains(0)) {
            getTorsionFrom(i, 0);
            line = line+", Integral unreduced Khovanov homology";
            for (int t : allTorsionU) line = line+", Torsion of order "+t;
        }// */
        if (khevenred && evenKhovs.contains(0)) {
            getTorsionFrom(i, 1);
            line = line+", Integral reduced Khovanov homology";
            for (int t : allTorsionR) line = line+", Torsion of order "+t;
        }
        if (oddKhovs.contains(0)) {
            getTorsionFrom(i, 2);
            line = line+", Integral odd Khovanov homology";
            for (int t : allTorsionO) line = line+", Torsion of order "+t;
        }
        if (sltKhovs.contains(0)) {
            getTorsionFrom(i, 3);
            line = line+", Integral unreduced sl_3 homology";
            for (int t : allTorsionSlTU) line = line+", Torsion of order "+t;
            getTorsionFrom(i, 4);
            line = line+", Integral reduced sl_3 homology";
            for (int t : allTorsionSlTR) line = line+", Torsion of order "+t;
        }
        for (int c : xtorts) line = line+", Extortion mod "+c;
        return line;
    }
    
    private void getTorsionFrom(int i, int kind) {
        if (kind <= 1) {
            boolean sign = kind == 1;
            for (LinkData data : theLinks.get(i)) {
                HomologyInfo info = data.integralKhovHomology(sign);
                ArrayList<PoincarePolynomial> torPolys = info.torsionPolynomials(false);
                for (PoincarePolynomial pol : torPolys) {
                    int t = pol.torsion().intValue();
                    if (kind == 0 && !allTorsionU.contains(t)) allTorsionU.add(t);
                    if (kind == 1 && !allTorsionR.contains(t)) allTorsionR.add(t);
                }
            }
        }
        if (kind == 2) {
            for (LinkData data : theLinks.get(i)) {
                HomologyInfo info = data.integralOddKhHomology();
                ArrayList<PoincarePolynomial> torPolys = info.torsionPolynomials(false);
                for (PoincarePolynomial pol : torPolys) {
                    int t = pol.torsion().intValue();
                    if (!allTorsionO.contains(t)) allTorsionO.add(t);
                }
            }
        }
        if (kind == 3 || kind == 4) {
            boolean red = false;
            ArrayList<Integer> allTor = allTorsionSlTU;
            if (kind == 4) {
                red = true;
                allTor = allTorsionSlTR;
            }
            for (LinkData data : theLinks.get(i)) {
                HomologyInfo info = data.integralSlTHomology(red);
                ArrayList<PoincarePolynomial> torPolys = info.torsionPolynomials(false);
                for (PoincarePolynomial pol : torPolys) {
                    int t = pol.torsion().intValue();
                    if (!allTor.contains(t)) allTor.add(t);
                }
            }
        }
    }
    
    private String newFileName(String name) {
        for (int s : sInvs) name = name+"_s"+s;
        for (int s : slInvs) name = name+"_sl3s"+s;
        if (sqOneEven) name = name+"_sqe";
        if (sqOneOdd) name = name+"_sqo";
        if (signature) name = name+"_sgn";
        String kh = "_kb";
        if (!khevenred) kh = "_ku";
        if (!khevenunr) kh = "_kr";
        for (int p : evenKhovs) name = name+kh+p;
        for (int p : oddKhovs) name = name+"_ko"+p;
        if (csvFile) {
            int u = name.indexOf(".");
            name = name.substring(0, u)+".csv";
            if (csvDirectory != null) {
                int v = name.lastIndexOf(options.getSlash());
                name = csvDirectory+name.substring(v+1);
            }
        }
        return name;
    }

    private void schreiNachHilfe() {
        File file = new File("README.TXT");
        try (FileReader fr = new FileReader(file); BufferedReader in = new BufferedReader(fr)) {
            boolean keepreading = true;
            while (keepreading) {
                String line = in.readLine();
                if (line == null) keepreading = false;
                else System.out.println(line);
            }
        }
        catch (IOException io) {
            System.out.println("Help!");
        }
    }
    
}
