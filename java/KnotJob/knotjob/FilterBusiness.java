/*

Copyright (C) 2023-25 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import knotjob.dialogs.BLTFilterDialog;
import knotjob.dialogs.BettiFilterDialog;
import knotjob.dialogs.CharDialog;
import knotjob.dialogs.CompFilterDialog;
import knotjob.dialogs.EditDialog;
import knotjob.dialogs.FilterAndOrDialog;
import knotjob.dialogs.FilterCreateDialog;
import knotjob.dialogs.FilterSelectDialog;
import knotjob.dialogs.FilterTypeDialog;
import knotjob.dialogs.MirrorLink;
import knotjob.dialogs.NiceSlTDialog;
import knotjob.dialogs.SFanFilterDialog;
import knotjob.dialogs.SFilterDialog;
import knotjob.dialogs.SelectTypeDialog;
import knotjob.dialogs.SlTFilterDialog;
import knotjob.dialogs.SlTSFilterDialog;
import knotjob.dialogs.SpSqOptionsDialog;
import knotjob.dialogs.StableFilterDialog;
import knotjob.filters.AndFilter;
import knotjob.filters.BLTFilter;
import knotjob.filters.CompFilter;
import knotjob.filters.Filter;
import knotjob.filters.KhovFilter;
import knotjob.filters.NotFilter;
import knotjob.filters.OrFilter;
import knotjob.filters.SInvFilter;
import knotjob.filters.SignFilter;
import knotjob.filters.SlTFilter;
import knotjob.filters.StabFilter;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class FilterBusiness {
    
    private final ArrayList<Filter> existingFilters;
    private final DefaultListModel<String> listModelAll;
    private final ArrayList<LinkData> allLinks;
    private final Options options;
    private final Knobster knobster;
    
    public FilterBusiness(ArrayList<Filter> filters, Options optns, Knobster frame,
            DefaultListModel<String> listModel, ArrayList<LinkData> all) {
        existingFilters = filters;
        options = optns;
        knobster = frame;
        listModelAll = listModel;
        allLinks = all;
    }

    public Filter resetFilters() {
        SInvFilter sinvFilter = new SInvFilter("Non const s-Invariant", 1, false, true, false, false, 0, 0);
        SInvFilter stnvFilter = new SInvFilter("Non const sl_3 s-Invariant", 2, false, true, false, false, 0, 0);
        //SInvFilter srnvFilter = new SInvFilter("Non const rl_3 s-Invariant", 4, false, true, false, false, 0, 0);
        SInvFilter sqnvFilter = new SInvFilter("Non const Sq^1-even", 0, true, true, false, false, 0, 0);
        SInvFilter sqovFilter = new SInvFilter("Non const Sq^1-odd ", 0, true, false, false, false, 0, 0);
        SInvFilter bsodFilter = new SInvFilter("Non const BLS-odd", 0, true, false, false, false, 0, 1);
        SInvFilter sqosFilter = new SInvFilter("Non const Sq^1-sum", 0, true, false, false, false, 0, 2);
        //SInvFilter unifFilter = new SInvFilter("Non const ULS-Inv", false, true, false, false, false, 0, 3);
        SInvFilter crifFilter = new SInvFilter("Non const CLS-Inv", 0, true, false, false, false, 0, 4);
        //SInvFilter cuifFilter = new SInvFilter("Non const unr CLS-Inv", false, true, false, false, false, 0, 5);
        SInvFilter swnvFilter = new SInvFilter("Non const Sq^2-even", 2);
        SInvFilter swodFilter = new SInvFilter("Non const Sq^2-odd ("+
                (char)949+"=0)", 0);
        SInvFilter swoeFilter = new SInvFilter("Non const Sq^2-odd ("+
                (char)949+"=1)", 1);
        SInvFilter nstdFilter = new SInvFilter("Non std graded s-Inv", true);
        StabFilter stevFilter = new StabFilter("Non triv ev stab", 2);
        StabFilter stodFilter = new StabFilter("Non triv odd stab ("+
                (char)949+"=0)", 0);
        StabFilter stoeFilter = new StabFilter("Non triv odd stab ("+
                (char)949+"=1)", 1);
        SignFilter signFilter = new SignFilter("s-Inv != signature", true, false, false, false, 0, 0);
        SignFilter dtsqFilter = new SignFilter("det is square", false, true, true, true, 0, -1);
        SignFilter fmcsFilter = new SignFilter("Fox-Milnor satisfied", false, false, true, true, 0, -1);
        KhovFilter khovFilter = new KhovFilter("3<=Tor unred Kh", 0, false, false, 3, 0,options.getPrimes());
        KhovFilter khorFilter = new KhovFilter("3<=Tor red Kh", 0, true, false, 3, 0,options.getPrimes());
        KhovFilter khmiFilter = new KhovFilter("Mirror inv unr Kh", 0, false, false, options.getPrimes());
        KhovFilter khmrFilter = new KhovFilter("Mirror inv red rat Kh", 0, true, true, options.getPrimes());
        KhovFilter widtFilter = new KhovFilter("3<=Width unred Kh",options.getPrimes(), 0, false, false, 3, 0);
        BLTFilter  blt0Filter = new BLTFilter("BLT c=0 3 <= unred", 0, "0", 3, 0, false);
        //CompFilter compFilter = new CompFilter("2-Component Links", true, 2, 2);
        existingFilters.add(sinvFilter);
        existingFilters.add(stnvFilter);
        //existingFilters.add(srnvFilter);
        existingFilters.add(sqnvFilter);
        existingFilters.add(sqovFilter);
        existingFilters.add(sqosFilter);
        existingFilters.add(bsodFilter);
        //existingFilters.add(unifFilter);
        existingFilters.add(crifFilter);
        //existingFilters.add(cuifFilter);
        existingFilters.add(swnvFilter);
        existingFilters.add(swodFilter);
        existingFilters.add(swoeFilter);
        existingFilters.add(nstdFilter);
        existingFilters.add(stevFilter);
        existingFilters.add(stodFilter);
        existingFilters.add(stoeFilter);
        existingFilters.add(khovFilter);
        existingFilters.add(khorFilter);
        existingFilters.add(widtFilter);
        existingFilters.add(khmiFilter);
        existingFilters.add(khmrFilter);
        existingFilters.add(signFilter);
        existingFilters.add(dtsqFilter);
        existingFilters.add(fmcsFilter);
        existingFilters.add(blt0Filter);
        //existingFilters.add(compFilter);
        return null;
    }
    
    public Filter selectFilters() {
        FilterSelectDialog dial = new FilterSelectDialog(knobster, "Select Filter", true, existingFilters);
        int sel = dial.getSelection();
        if (sel < 0) return null;
        return existingFilters.get(sel);
    }
    
    public void editFilter() {
        FilterSelectDialog diag = new FilterSelectDialog(knobster, "Choose Filter to Edit", true, existingFilters);
        int sel = diag.getSelection();
        if (sel < 0) return;
        EditDialog dial = new EditDialog(knobster, "Edit Name", true, existingFilters.get(sel));
        dial.setupDialog();
        if (dial.isOkay()) existingFilters.get(sel).setName(dial.getNewName());
    }
    
    public void removeFilter() {
        FilterSelectDialog diag = new FilterSelectDialog(knobster, "Choose Filter to Remove", true, existingFilters);
        int sel = diag.getSelection();
        if (sel < 0) return;
        if (JOptionPane.showConfirmDialog(null, "Are you sure?", "Remove selected filter", JOptionPane.YES_NO_OPTION) 
                        == JOptionPane.OK_OPTION) {
            existingFilters.remove(sel);
        }
    }
    
    public void createFilter() {
        FilterCreateDialog create = new FilterCreateDialog(knobster, "Create Filter", true);
        int sel = create.getSelection();
        if (sel < 0) return;
        if (sel == 0) createCompFilter();
        if (sel == 1) createKhovFilter();
        if (sel == 2) createSInvFilter();
        if (sel == 3) createAndFilter();
        if (sel == 4) createOrFilter();
        if (sel == 5) createNotFilter();
        if (sel == 6) createSignFilter();
        if (sel == 7) createDetFilter();
        if (sel == 8) createStabFilter();
        if (sel == 9) createBLTFilter();
    }
    
    private void createCompFilter() {
        CompFilterDialog dial = new CompFilterDialog(knobster, "Component Filter", true);
        dial.setupDialog();
        if (!dial.isOkay()) return;
        boolean bddAb = dial.getBoundedAbove();
        int lb = dial.getLowerBound();
        int ub = dial.getUpperBound();
        String name = "-Component Links";
        if (bddAb) {
            name = String.valueOf(ub)+name;
            if (lb < ub) name = String.valueOf(lb)+"-"+name;
        }
        else {
            name = ">="+lb+name;
        }
        existingFilters.add(new CompFilter(name,bddAb,lb,ub));
    }
    
    private void createStabFilter() {
        StableFilterDialog dial = new StableFilterDialog(knobster, "Specific Eta Word Filter", true);
        dial.setupDialog();
        if (!dial.isOkay()) return;
        int[] torsion = dial.getTorsion();
        int typ = dial.getEvenType();
        String name = "eta";
        if (torsion[0] > 0) name = torsion[0]+"_"+name;
        if (torsion[1] > 0) name = torsion[1]+name;
        if (torsion[2] > 0) name = name+torsion[2];
        if (torsion[3] > 0) name = name+"^"+torsion[3];
        if (typ == 0) name = name + " odd (0)";
        if (typ == 1) name = name + " odd (1)";
        if (typ == 2) name = name + " even";
        existingFilters.add(new StabFilter(name, typ, torsion[0], torsion[1], torsion[2],
                torsion[3]));
    }
    
    private void createKhovFilter() {
        ArrayList<String> opts = new ArrayList<String>(3);
        opts.add("Betti Number Filter");
        opts.add("Torsion Filter");
        opts.add("Width Filter");
        opts.add("Comparison Filter");
        FilterTypeDialog dial = new FilterTypeDialog(knobster, "Khovanov Filter", true, opts);
        int typ = dial.getFilterType();
        if (typ == 0) createKhBettiFilter();
        if (typ == 1) createKhTorsionFilter();
        if (typ == 2) createKhWidthFilter();
        if (typ == 3) createKhComparFilter();
    }
    
    private void createKhBettiFilter() {
        BettiFilterDialog dial = new BettiFilterDialog(knobster, "Khovanov Betti Number Filter", true, 0, 65536, 0);
        dial.setupDialog();
        if (!dial.isOkay()) return;
        String name = dial.getHomString()+" Betti";
        int homTy = 0;
        if (dial.isOdd()) homTy = 1;
        if (dial.isSlT() && homTy == 0) homTy = 2;
        existingFilters.add(new KhovFilter(getKhName(dial, name), options.getPrimes(), homTy, dial.isReduced(),
                dial.getBoundedAbove(), dial.getLowerBound(), dial.getUpperBound(), dial.getHom()));
    }
    
    private void createKhTorsionFilter() {
        CompFilterDialog dial = new CompFilterDialog(knobster, "Khovanov Torsion Filter", true, 2, 65536);
        dial.setupDialog();
        if (!dial.isOkay()) return;
        int homTy = 0;
        if (dial.isOdd()) homTy = 1;
        if (dial.isSlT() && homTy == 0) homTy = 2;
        existingFilters.add(new KhovFilter(getKhName(dial, "Tor"), homTy, dial.isReduced(),dial.getBoundedAbove(), 
                dial.getLowerBound(), dial.getUpperBound(), options.getPrimes()));
    }
    
    private void createKhWidthFilter() {
        CompFilterDialog dial = new CompFilterDialog(knobster, "Khovanov Width Filter", true, 1, 15);
        dial.setupDialog();
        if (!dial.isOkay()) return;
        int homTy = 0;
        if (dial.isOdd()) homTy = 1;
        if (dial.isSlT() && homTy == 0) homTy = 2;
        existingFilters.add(new KhovFilter(getKhName(dial,"Width"),options.getPrimes(), homTy, dial.isReduced(),
                dial.getBoundedAbove(), dial.getLowerBound(), dial.getUpperBound()));
    }
    
    private String getKhName(CompFilterDialog dial, String add) {
        boolean bab = dial.getBoundedAbove();
        int lb = dial.getLowerBound();
        int ub = dial.getUpperBound();
        boolean red = dial.isReduced();
        boolean odd = dial.isOdd();
        boolean slt = dial.isSlT();
        String name = String.valueOf(lb)+"<="+add;
        if (bab && lb == ub) name = String.valueOf(lb)+"-"+add;
        if (bab && lb < ub) name = name+"<="+ub;
        String extra = " unred Kh";
        if (red) extra = " red Kh";
        if (odd) extra = " OKh";
        if (slt && !odd) extra = extra+"sl_3";
        return name + extra;
    }
    
    private void createKhComparFilter() {
        if (allLinks.isEmpty()) return;
        MirrorLink dial = new MirrorLink(knobster, "Same Khovanov Cohomology as", true, true, listModelAll);
        dial.setUpStuff();
        int chosen = dial.getChosen();
        if (chosen < 0) return;
        boolean rat = dial.isRational();
        boolean red = dial.isReduced();
        boolean odd = dial.isOdd();
        int homTy = 0;
        if (dial.isOdd()) homTy = 1;
        //if (dial.isSlT()) homTy = 2;
        String name = getSameNameAs(chosen, odd, rat, red);
        existingFilters.add(new KhovFilter(name, homTy, rat, red, allLinks.get(chosen),options.getPrimes()));
    }
    
    private String getSameNameAs(int chosen, boolean odd, boolean rat, boolean red) {
        String name = "Same ";
        if (rat) name = name+"rat ";
        if (odd) name = name+"odd ";
        else {
            if (red) name = name+"red ";
            else name = name+"unred ";
        }
        name = name +"Kh as "+allLinks.get(chosen).name;
        return name;
    }
    
    private void createSInvFilter() {
        ArrayList<String> opts = new ArrayList<String>(3);
        opts.add("S-Invariant Bound Filter");
        opts.add("Non-constant Filter");
        opts.add("sl_3 vs rl_3 Filter");
        FilterTypeDialog dial = new FilterTypeDialog(knobster, "S-Invariant Filter", true, opts);
        int typ = dial.getFilterType();
        if (typ == 0) createSBoundsFilter();
        if (typ == 1) createNonCnstFilter();
        if (typ == 2) createSltRltFilter();
    }
    
    private void createNonCnstFilter() {
        ArrayList<String> opts = new ArrayList<String>();
        opts.add("S-Invariant");
        opts.add("sl_3 S-Invariant");
        opts.add("rl_3 S-Invariant");
        SelectTypeDialog dial = new SelectTypeDialog(knobster, "Non-constant S-invariants", true, opts);
        int p = dial.getSelectType();
        if (p <= 0) return;
        String name = "Non const ";
        if ((p & 1) != 0) name = name +"S ";
        if ((p & 2) != 0) name = name +"sl_3 ";
        if ((p & 4) != 0) name = name +"rl_3 ";
        existingFilters.add(new SInvFilter(name, p, false, true, false, false, 0, 0));
    }
    
    private void createSltRltFilter() {
        CharDialog dial = new CharDialog(knobster, "sl_3 != rl_3", true, false, options);
        int p = dial.getChar(false);
        if (p < 0) return;
        String name = "sl_3 != rl_3 in "+p;
        existingFilters.add(new SInvFilter(name, 8 + p, false, true, false, false, 0, 0));
    }
    
    private void createSBoundsFilter() {    
        SFanFilterDialog dial = new SFanFilterDialog(knobster, "S-Invariant Filter", 
                true, -1000, 1000, false, 2, options);
        dial.setupDialog();
        if (!dial.isOkay()) return;
        String name = boundName(dial, dial.getTypeString());
        int c = dial.getCharType();
        if (c >= 0) name = name + " c="+c;
        existingFilters.add(new SInvFilter(name, dial.getBoundedBelow(), dial.getBoundedAbove(), 
                dial.getLowerBound(), dial.getUpperBound(), dial.getBoundType(), c));
    }
    
    private void createSignFilter() {
        SFilterDialog dial = new SFilterDialog(knobster, "Signature Filter", true, -1000, 1000, false, 2);
        dial.setupDialog();
        if (!dial.isOkay()) return;
        String name = boundName(dial, "sgn");
        existingFilters.add(new SignFilter(name, false, false, dial.getBoundedBelow(), dial.getBoundedAbove(), 
                dial.getLowerBound(), dial.getUpperBound()));
    }
    
    private void createDetFilter() {
        SFilterDialog dial = new SFilterDialog(knobster, "Determinant Filter", true, 0, 1000, false, 1);
        dial.setupDialog();
        if (!dial.isOkay()) return;
        String name = boundName(dial, "det");
        existingFilters.add(new SignFilter(name, false, true, dial.getBoundedBelow(), dial.getBoundedAbove(), 
                dial.getLowerBound(), dial.getUpperBound()));
    }
    
    private String boundName(SFilterDialog dial, String name) {
        boolean bb = dial.getBoundedBelow();
        boolean ba = dial.getBoundedAbove();
        int lb = dial.getLowerBound();
        int ub = dial.getUpperBound();
        //String name = "s-Inv";
        if (bb) name = String.valueOf(lb)+"<="+name;
        if (ba) name = name+"<="+String.valueOf(ub);
        return name;
    }
    
    private void createBLTFilter() {
        SpSqOptionsDialog frm = new SpSqOptionsDialog(knobster, "Spectral Sequences", true, options);
        frm.setUp();
        int choice = frm.getChoice();
        if (choice < 0) return;
        switch (choice) {
            case 0 -> standardPageFilter();
            case 1 -> specialPageFilter();
            case 2 -> differenceFilter();
            case 3 -> niceFilter();
            case 4 -> unusualSFilter();
        }
    }

    private void standardPageFilter() {
        BLTFilterDialog dial = new BLTFilterDialog(knobster, "Page Bound Filter", true, options);
        dial.setupDialog();
        if (!dial.isOkay()) return;
        int typ = dial.getSeqType();
        String name = dial.getFilterName(typ);
        existingFilters.add(new BLTFilter(name, typ, 
                dial.getCharacteristic(), dial.getLowerBound(), 
                dial.getUpperBound(), dial.getBoundedAbove()));
    }

    private void specialPageFilter() {
        SlTFilterDialog dial = new SlTFilterDialog(knobster, "Page Bound Filter", true);
        dial.setUpperBound(6);
        dial.setupDialog();
        String name = dial.getFilterName(dial.getFilterType());
        existingFilters.add(new SlTFilter(name, dial.wValue(), dial.getFilterType(), 
                dial.getLowerBound(), dial.getUpperBound(), true, 
                dial.getBoundedAbove(), true, dial.getRelevance()));
    }

    private void differenceFilter() {
        NiceSlTDialog dial = new NiceSlTDialog(knobster, "Difference Filter", true, options);
        dial.setupDialog(false);
        if (!dial.isOkay()) return;
        existingFilters.add(new SlTFilter(dial.getDiffFilterName(), dial.wSymbol(), dial.selectedType(), 
                true, false));
    }

    private void niceFilter() {
        NiceSlTDialog dial = new NiceSlTDialog(knobster, "Nice E_infinity Filter", true, options);
        dial.setupDialog(true);
        if (!dial.isOkay()) return;
        existingFilters.add(new SlTFilter(dial.getFilterName(), dial.wSymbol(), dial.selectedType(), 
                false, true));
    }
    
    private void unusualSFilter() {
        SlTSFilterDialog dial = new SlTSFilterDialog(knobster, "Unusual S-Invariant Filter", true, -1000, 1000, false, 2);
        dial.setupDialog();
        if (!dial.isOkay()) return;
        String name = boundName(dial, dial.getFilterName());
        existingFilters.add(new SlTFilter(name, dial.wSymbol(), dial.selectedType(), 
                dial.getLowerBound(), dial.getUpperBound(), dial.getBoundedBelow(), 
                dial.getBoundedAbove(), false, false));
    }
    
    private void createAndFilter() {
        FilterAndOrDialog diag = new FilterAndOrDialog(knobster, "Logical AND Filter", true, true, existingFilters);
        ArrayList<Filter> andFilters = diag.getFilters();
        if (andFilters != null) {
            String name = getAndOrName(andFilters,true);
            AndFilter andFilter = new AndFilter(name,andFilters);
            existingFilters.add(andFilter);
        }
    }
    
    private void createOrFilter() {
        FilterAndOrDialog diag = new FilterAndOrDialog(knobster, "Logical OR Filter", true, false, existingFilters);
        ArrayList<Filter> orFilters = diag.getFilters();
        if (orFilters != null) {
            String name = getAndOrName(orFilters,false);
            OrFilter orFilter = new OrFilter(name,orFilters);
            existingFilters.add(orFilter);
        }
    }
    
    private String getAndOrName(ArrayList<Filter> filters, boolean and) {
        String extra = "OR";
        if (and) extra = "AND";
        String name = "("+filters.get(0).getName()+")";
        for (int i = 1; i < filters.size(); i++) name = name + extra + "("+filters.get(i).getName()+")";
        return name;
    }
    
    private void createNotFilter() {
        FilterSelectDialog diag = new FilterSelectDialog(knobster, "Logical NOT Filter", true,existingFilters);
        int sel = diag.getSelection();
        if (sel < 0) return;
        Filter filt = existingFilters.get(sel);
        String name = "NOT("+filt.getName()+")";
        existingFilters.add(new NotFilter(name,filt));
    }
    
}
