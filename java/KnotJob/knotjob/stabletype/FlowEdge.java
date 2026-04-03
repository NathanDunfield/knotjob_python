/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knotjob.stabletype;

import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class FlowEdge<R extends Ring<R>> {
    
    private FlowGenerator<R> fVert;
    private FlowGenerator<R> sVert;
    private boolean firLow;
    private boolean firUpp;
    private boolean secLow;
    private boolean secUpp;
    
    public FlowEdge(FlowGenerator<R> fv, FlowGenerator<R> sv, boolean fl, boolean fu,
            boolean sl, boolean su) {
        fVert = fv;
        sVert = sv;
        firLow = fl;
        firUpp = fu;
        secLow = sl;
        secUpp = su;
    }
    
    public FlowGenerator<R> firstVertex() {
        return fVert;
    }
    
    public FlowGenerator<R> secondVertex() {
        return sVert;
    }
    
    public void changeSign(boolean low, boolean fir) {
        if ( low &&  fir) firLow = !firLow;
        if ( low && !fir) secLow = !secLow;
        if (!low &&  fir) firUpp = !firUpp;
        if (!low && !fir) secUpp = !secUpp;
    }
    
    public void setSign(boolean low, boolean fir, boolean val) {
        if ( low &&  fir) firLow = val;
        if ( low && !fir) secLow = val;
        if (!low &&  fir) firUpp = val;
        if (!low && !fir) secUpp = val;
    }
    
    public boolean firstSign() {
        return firLow == firUpp;
    }
    
    public boolean secondSign() {
        return secLow == secUpp;
    }
    
    public boolean firstLowerSign() {
        return firLow;
    }
    
    public boolean firstUpperSign() {
        return firUpp;
    }
    
    public boolean secondLowerSign() {
        return secLow;
    }
    
    public boolean secondUpperSign() {
        return secUpp;
    }
    
    public boolean isDirected() {
        return firstSign() == secondSign();
    }
    
    public void setFirstVertex(FlowGenerator<R> nf) {
        fVert = nf;
    }
    
    public void setSecondVertex(FlowGenerator<R> ns) {
        sVert = ns;
    }
    
}
