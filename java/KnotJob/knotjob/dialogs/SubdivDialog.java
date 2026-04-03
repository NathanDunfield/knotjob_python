/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knotjob.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import knotjob.diagrams.diagrams3d.Edge3D;
import knotjob.diagrams.diagrams3d.GraphicalDiagram3D;

/**
 *
 * @author Dirk
 */
public class SubdivDialog extends JDialog {
    
    private final JFrame frame;
    private final GraphicalDiagram3D dig3D;
    private final JButton subdivButton;
    private final JButton removeButton;
    private final JButton cancelButton;
    private JSpinner maxSpinner;
    private JSpinner minSpinner;
    private int value;
    
    public SubdivDialog(JFrame frm, String title, boolean bo, GraphicalDiagram3D dig) {
        super(frm, title, bo);
        frame = frm;
        dig3D = dig;
        value = -1;
        subdivButton = new JButton("Subdivide");
        removeButton = new JButton("Remove");
        cancelButton = new JButton("Cancel");
    }
    
    public void setupStuff() {
        this.setSize(450,300);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        JPanel buttPanel = new JPanel();
        buttPanel.add(cancelButton);
        buttPanel.add(removeButton);
        buttPanel.add(subdivButton);
        this.add(buttPanel, BorderLayout.SOUTH);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        });
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                value = 0;
                dispose();
            }
        });
        subdivButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                value = 1;
                dispose();
            }
        });
        double[] maxmin = getMaxMinDistance();
        JLabel minLabel = new JLabel("Minimal Vertex distance : "+round(maxmin[1], 100.0), SwingConstants.CENTER);
        JLabel maxLabel = new JLabel("Maximal Vertex distance : "+round(maxmin[0], 100.0), SwingConstants.CENTER);
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new GridLayout(4,1));
        labelPanel.add(new JLabel(" "));
        labelPanel.add(maxLabel);
        labelPanel.add(new JLabel(" "));
        labelPanel.add(minLabel);
        this.add(labelPanel, BorderLayout.NORTH);
        JLabel subdivLabel = new JLabel("Subdivide edges of distance bigger than ", 0);
        JLabel removeLabel = new JLabel("Remove vertices with distances less than ", 0);
        double mxval = round(maxmin[0] * .75 + maxmin[1] * 0.25, 100.0);
        double mxmax = round(maxmin[0], 100.0)+0.01;
        double mxmin = round(maxmin[1] * 0.5 + maxmin[0] * 0.5, 100.0);
        double mnmin = round(maxmin[1], 100.0) - 0.01;
        SpinnerNumberModel mxmodel = new SpinnerNumberModel(mxval, mnmin, mxmax, 0.01);
        SpinnerNumberModel mnmodel = new SpinnerNumberModel(mnmin, mnmin, mxmin, 0.01);
        maxSpinner = new JSpinner(mxmodel);
        maxSpinner.setPreferredSize(new Dimension(70, 20));
        minSpinner = new JSpinner(mnmodel);
        JPanel upperPanel = new JPanel();
        upperPanel.add(subdivLabel);
        upperPanel.add(maxSpinner);
        JPanel lowerPanel = new JPanel();
        lowerPanel.add(removeLabel);
        lowerPanel.add(minSpinner);
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(3,1));
        centerPanel.add(new JPanel());
        centerPanel.add(upperPanel);
        centerPanel.add(lowerPanel);
        this.add(centerPanel, BorderLayout.CENTER);
        this.setVisible(true);
    }

    public int theValue() {
        return value;
    }
    
    public double getSubdivValue() {
        return (double) maxSpinner.getValue();
    }
    
    public double getRemoveValue() {
        return (double) minSpinner.getValue();
    }
    
    private double round(double num, double fac) {
        return (Math.round(fac * num))/fac;
    }
    
    private double[] getMaxMinDistance() {
        double max = 0;
        double min = 10000d;
        for (Edge3D edge : dig3D.linkDiagram.edges) {
            double dist = edge.length();
            if (max < dist) max = dist;
            if (min > dist) min = dist;
        }
        return new double[] {max, min};
    }
    
}
