/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knotjob.filters;

import java.util.ArrayList;
import knotjob.frames.StableGraph;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class StabFilter implements Filter {

    private String name;
    private final int style;
    private final int evOdd; // 0 = odd eps 0; 1 = odd eps 1; 2 = even
    private final int botDown;
    private final int botUp;
    private final int topDown;
    private final int topUp;
    private ArrayList<String> info;
    
    public StabFilter(String nm, int od) {
        evOdd = od;
        style = 0;
        name = nm;
        botDown = 0;
        botUp = 0;
        topDown = 0;
        topUp = 0;
    }
    
    public StabFilter(String nm, int od, int sty, int dn, int md) {
        evOdd = od;
        style = sty;
        name = nm;
        botDown = 0;
        botUp = dn;
        topDown = md;
        topUp = 0;
    }
    
    public StabFilter(String nm, int od, int bd, int bu, int td, int tu) {
        evOdd = od;
        style = 1;
        name = nm;
        botDown = bd;
        botUp = bu;
        topDown = td;
        topUp = tu;
    }
    
    @Override
    public boolean linkIsFiltered(LinkData link) {
        info = link.stEvenInfo;
        if (evOdd == 0) info = link.stOddInfo;
        if (evOdd == 1) info = link.stOdeInfo;
        if (info == null) return false;
        if (style == 0) return checkForAnyEta();
        if (style == 1) return checkForEtaSpace();
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean checkForEtaSpace() {
        boolean found = false;
        ArrayList<StableGraph> theGraphs = getTheGraphs();
        int i = 0;
        while (!found && i < theGraphs.size()) {
            if (theGraphs.get(i).containsIsolatedEta(botDown, botUp, topDown, topUp)) found = true;
            i++;
        }
        return found;
    }
    
    private boolean checkForAnyEta() {
        boolean found = false;
        int i = 2;
        while (!found && i < info.size()) {
            if (info.get(i).length() > 1) found = true;
            else i = i + 3;
        }
        return found;
    }
    
    private ArrayList<StableGraph> getTheGraphs() {
        ArrayList<StableGraph> graphs = new ArrayList<StableGraph>();
        int i = 0;
        while (i < info.size()) {
            graphs.add(new StableGraph(info.get(i), info.get(i+1), info.get(i+2)));
            i = i + 3;
        }
        return graphs;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String nm) {
        name = nm;
    }
    
    private class EtaGraph {
        
        private ArrayList<EtaVertex> vertices;
        
    }
    
    private class EtaVertex {
        
        private int level;
        private int index;
        private ArrayList<EtaEdge> edgesBelow;
        private ArrayList<EtaEdge> edgesAbove;
        private TorsionEdge torBelow;
        private TorsionEdge torAbove;
        
    }
    
    private class EtaEdge {
        
        private EtaVertex top;
        private EtaVertex bot;
        
    }
    
    private class TorsionEdge {
        
        private EtaVertex top;
        private EtaVertex bot;
        private int torsion;
    }
    
}
