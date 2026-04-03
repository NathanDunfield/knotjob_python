/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knotjob.diagrams;

import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JComponent;

/**
 *
 * @author Dirk
 */
public class GraphicDiagram extends JComponent {
    
    protected final ArrayList<Color> colors;
    public final ArrayList<Boolean> showComps;
    protected final ArrayList<Boolean> orientComps;
    
    public GraphicDiagram(ArrayList<Color> clrs) {
        if (clrs == null) colors = new ArrayList<Color>();
        else colors = clrs;
        showComps = new ArrayList<Boolean>();
        orientComps = new ArrayList<Boolean>();
    }

    public void setColors(ArrayList<Color> setColors) {
        colors.clear();
        for (Color col : setColors) colors.add(col);
    }

    public void setShownComponents(ArrayList<Boolean> setShownComponents) {
        showComps.clear();
        for (Boolean bol : setShownComponents) showComps.add(bol);
    }

    public void setOrientComponents(ArrayList<Boolean> setOrientComponents) {
        orientComps.clear();
        for (Boolean bol : setOrientComponents) orientComps.add(bol);
    }

    public Iterable<Color> getColors() {
        return colors;
    }

    public Iterable<Boolean> getShownComponents() {
        return showComps;
    }

    public Iterable<Boolean> getOrientComponents() {
        return orientComps;
    }
    
}
