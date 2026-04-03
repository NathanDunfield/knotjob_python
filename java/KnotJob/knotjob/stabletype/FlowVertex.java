/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knotjob.stabletype;

import knotjob.homology.Arrow;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class FlowVertex<R extends Ring<R>> {
    
    private final Arrow<R> fArr;
    private final Arrow<R> sArr;
    
    public FlowVertex(Arrow<R> fAr, Arrow<R> sAr) {
        fArr = fAr;
        sArr = sAr;
    }
    
    public FlowGenerator<R> getMiddle() {
        return (FlowGenerator<R>) fArr.getTopGenerator();
    }
    
    public R sign() {
        R sign = fArr.getValue().multiply(sArr.getValue());
        return sign;
    }
    
    public R firstSign() {
        return fArr.getValue();
    }
    
    public R secondSign() {
        return sArr.getValue();
    }
    
    public Arrow<R> firstArrow() {
        return fArr;
    }
    
    public Arrow<R> secondArrow() {
        return sArr;
    }
    
    public String output() {
        return "("+fArr.getValue()+", "+sArr.getValue()+") ";
    }
    
}
