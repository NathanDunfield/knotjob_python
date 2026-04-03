/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knotjob.homology.evenkhov;

import java.util.ArrayList;
import knotjob.homology.Generator;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class EvenStableGenerator <R extends Ring<R>> extends EvenGenerator<R> {
    
    private final ArrayList<Integer> position;
    private final ArrayList<ArrayList<Integer>> circles;
    private final ArrayList<Boolean> signs;
    
    public EvenStableGenerator(int dg, int hd, int qd, ArrayList<Integer> pos, 
            ArrayList<ArrayList<Integer>> crcls, ArrayList<Boolean> sgns) {
        super(dg, hd, qd);
        position = pos;
        circles = crcls;
        signs = sgns;
    }
    
    public EvenStableGenerator(int dg, int hd, int qd, int bol) {
        super(dg, hd, qd);
        position = new ArrayList<Integer>(1);
        position.add(bol);
        circles = new ArrayList<ArrayList<Integer>>();
        signs = new ArrayList<Boolean>();
    }
    
    @Override
    public void output(ArrayList<Generator<R>> nextLevel) {
        System.out.println("Diagram "+diagram);
        System.out.println("hdeg = "+hdeg);
        System.out.println("qdeg = "+qdeg);
        System.out.println("Position = "+position);
        System.out.println("Circles = "+circles);
        System.out.println("Signs = "+signs);
        for (int i = 0; i < bMor.size(); i++) ((EvenArrow<R>) bMor.get(i)).output(nextLevel);
    }
    
    public ArrayList<Integer> clonePosition() {
        ArrayList<Integer> clone = new ArrayList<Integer>(position.size()+1);
        for (Integer bol : position) clone.add(bol);
        return clone;
    }
    
    public ArrayList<Integer> getPosition() {
        return position;
    }

    public Integer getPosition(int i) {
        return position.get(i);
    }

    public ArrayList<ArrayList<Integer>> cloneCircles() {
        ArrayList<ArrayList<Integer>> clone = new ArrayList<ArrayList<Integer>>();
        for (ArrayList<Integer> circle : circles) clone.add(circle);
        return clone;
    }

    public ArrayList<ArrayList<Integer>> getCircles() {
        return circles;
    }
    
    public int circleWith(ArrayList<Integer> circle) {
        boolean found = false;
        int i = 0;
        while (!found) {
            int a = circles.get(i).get(0);
            if (circle.contains(a)) found = true;
            else i++;
        }
        return i;
    }
    
    public ArrayList<Boolean> cloneSigns() {
        ArrayList<Boolean> clone = new ArrayList<Boolean>();
        for (Boolean bol : signs) clone.add(bol);
        return clone;
    }
    
    public ArrayList<Boolean> getSigns() {
        return signs;
    }
}
