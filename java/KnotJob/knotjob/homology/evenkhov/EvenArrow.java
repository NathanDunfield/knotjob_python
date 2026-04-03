/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knotjob.homology.evenkhov;

import java.util.ArrayList;
import knotjob.homology.Arrow;
import knotjob.homology.Generator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class EvenArrow<R extends Ring<R>> extends Arrow<R> {
    
    private final ArrayList<Cobordism<R>> cobordisms;
    
    public EvenArrow(EvenGenerator<R> bo, EvenGenerator<R> to) {
        super(bo, to, null);
        cobordisms = new ArrayList<Cobordism<R>>();
    }

    public EvenArrow(EvenGenerator<R> bGen, EvenGenerator<R> tGen, Cobordism<R> surgery) {
        super(bGen, tGen, null);
        cobordisms = new ArrayList<Cobordism<R>>(1);
        cobordisms.add(surgery);
    }
    
    public EvenArrow(EvenGenerator<R> bGen, EvenGenerator<R> tGen, ArrayList<Cobordism<R>> chrons) {
        super(bGen, tGen, null);
        cobordisms = chrons;
    }
    
    public void addCobordism(Cobordism<R> cob) {
        cobordisms.add(cob);
    }
    
    public ArrayList<Cobordism<R>> getCobordisms() {
        return cobordisms;
    }
    
    public Cobordism<R> getCobordism(int i) {
        return cobordisms.get(i);
    }
    
    public int getBotDiagram() {
        return ((EvenGenerator<R>) bObj).getDiagram();
    }
    
    public int getTopDiagram() {
        return ((EvenGenerator<R>) tObj).getDiagram();
    }
    
    @Override
    public R getValue() {
        return cobordisms.get(0).getValue();
    }
    
    @Override
    public void setValue(R nv) {
        if (cobordisms.isEmpty()) cobordisms.add(new Cobordism<R>(nv, 0, 0l));
        else cobordisms.get(0).setValue(nv);
    }
    
    @Override
    public void addValue(R ad) {
        if (cobordisms.isEmpty()) cobordisms.add(new Cobordism<R>(ad, 0, 0l));
        else cobordisms.get(0).addValue(ad);
    }
    
    boolean isEmpty() {
        return cobordisms.isEmpty();
    }
    
    @Override
    public EvenGenerator<R> getBotGenerator() {
        return (EvenGenerator<R>) bObj;
    }
    
    @Override
    public EvenGenerator<R> getTopGenerator() {
        return (EvenGenerator<R>) tObj;
    }
    
    @Override
    public void output(ArrayList<Generator<R>> nextLevel) {
        System.out.println("To : "+nextLevel.indexOf(tObj));
        for (Cobordism<R> cobord : cobordisms) cobord.output();
    }
    
}
