/*

Copyright (C) 2019-23 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import knotjob.Options;

/**
 *
 * @author Dirk
 */
public class OptionDialog extends JDialog {
    
    private final JFrame frame;
    private final JButton cancelButton;
    private final JButton okayButton;
    private final JCheckBox totGirthBox;
    private final JCheckBox boundGirth;
    private final JCheckBox randomGirth;
    private final JSpinner girthSpinner;
    private final JSpinner subdivSpinner;
    private final JSpinner combineSpinner;
    private final JSpinner boundSpinner;
    private final JComboBox detailBox;
    private final JComboBox diagramBox;
    private final JCheckBox primTorBox;
    private final JCheckBox combBox;
    private final JCheckBox calcTimeBox;
    private final JComboBox redOptions;
    private final JCheckBox changeNames;
    private final JSpinner saveMax;
    private final Options options;
    private final int startPage;
    
    public OptionDialog(JFrame fram, String title, boolean bo, Options opts, int page) {
        super(fram,title,bo);
        frame = fram;
        options = opts;
        cancelButton = new JButton("Cancel");
        okayButton = new JButton("OK");
        totGirthBox = new JCheckBox("total");
        totGirthBox.setSelected(options.getTotGirth());
        boundGirth = new JCheckBox("bound");
        boundGirth.setSelected(options.getBoundGirth());
        randomGirth = new JCheckBox("random");
        randomGirth.setSelected(options.getRandomGirth());
        girthSpinner = new JSpinner(new SpinnerNumberModel(opts.getGirthBound(), 100, 100000, 100));
        girthSpinner.setEnabled(boundGirth.isSelected());
        subdivSpinner = new JSpinner(new SpinnerNumberModel(opts.getDivFactor(), 2.5, 1000.0, 0.5));
        combineSpinner = new JSpinner(new SpinnerNumberModel(opts.getCombAtt(), 100, 50000, 100));
        boundSpinner = new JSpinner(new SpinnerNumberModel(opts.getStableBound(), 5, 50, 1));
        String [] details = {"low", "medium", "high"};
        detailBox = new JComboBox<String>(details);
        detailBox.setSelectedIndex(options.getGirthInfo());
        String [] grids = {"yes", "no"};
        diagramBox = new JComboBox<String>(grids);
        diagramBox.setSelectedIndex(options.getGridDiagram());
        primTorBox = new JCheckBox("primary");
        primTorBox.setSelected(options.getPrimary());
        combBox = new JCheckBox("combine");
        combBox.setSelected(options.getCombine());
        calcTimeBox = new JCheckBox("show");
        calcTimeBox.setSelected(options.getTimeInfo());
        String [] items = {"both", "reduced", "unreduced"};
        redOptions = new JComboBox<String>(items);
        changeNames = new JCheckBox("change Numbers");
        changeNames.setSelected(options.getChangeOfNumbers());
        saveMax = new JSpinner(new SpinnerNumberModel(opts.getMaxSaveCount(), 100, 1050000, 100));
        startPage = page;
    }
    
    public void setUpStuff() {
        this.setSize(400,300);
        this.setLocationRelativeTo(frame);
        this.setResizable(false);
        JTabbedPane theTab = new JTabbedPane();
        JPanel khovanov = new JPanel();
        JPanel files = new JPanel();
        JPanel girth = new JPanel();
        JPanel diagram = new JPanel();
        JPanel calculations = new JPanel();
        JPanel stable = new JPanel();
        theTab.addTab("Calculations", calculations);
        theTab.addTab("Girth", girth);
        theTab.addTab("Diagrams", diagram);
        theTab.addTab("Files", files);
        theTab.addTab("Khovanov Cohomology", khovanov);
        theTab.addTab("Stable Homotopy", stable);
        theTab.setSelectedIndex(startPage);
        JPanel optPanel = new JPanel();
        JPanel butPanel = new JPanel();
        butPanel.add(okayButton);
        butPanel.add(cancelButton);
        optPanel.add(theTab);
        diagram.setPreferredSize(new Dimension(380,195));
        this.setLayout(new BorderLayout());
        this.add(butPanel, BorderLayout.SOUTH);
        this.add(optPanel, BorderLayout.CENTER);
        addToCalculationOptions(calculations);
        addToGirthOptions(girth);
        addToDiagramOptions(diagram);
        addToFilesOptions(files);
        addToKhovanovOptions(khovanov);
        addToStableOptions(stable);
        setUpButtons();
        this.setVisible(true);
    }

    private void setUpButtons() {
        boundGirth.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                girthSpinner.setEnabled(boundGirth.isSelected());
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean newTotGirth = totGirthBox.isSelected();
                options.setTotGirth(newTotGirth);
                options.setBoundGirth(boundGirth.isSelected());
                options.setRandomGirth(randomGirth.isSelected());
                options.setGirthBound((int) girthSpinner.getValue());
                double factor = (double) subdivSpinner.getValue();
                options.setDivFactor(factor);
                boolean newPrimary = primTorBox.isSelected();
                options.setPrimary(newPrimary);
                options.setGirthInfo(detailBox.getSelectedIndex());
                options.setTimeInfo(calcTimeBox.isSelected());
                options.setChangeOfNumbers(changeNames.isSelected());
                options.setMaxSaveCount((int) saveMax.getValue());
                int n = redOptions.getSelectedIndex();
                boolean r = true;
                boolean u = true;
                if (n == 1) u = false;
                if (n == 2) r = false;
                n = diagramBox.getSelectedIndex();
                options.setGridDiagram(n == 0);
                options.setKhovRed(r);
                options.setKhovUnred(u);
                options.setCombine(combBox.isSelected());
                options.setCombAtt((int) combineSpinner.getValue());
                options.setStableBound((int) boundSpinner.getValue());
                dispose();
            }
        });
    }
    
    private void addToGirthOptions(JPanel panel) {
        JLabel girthLabel = new JLabel("Girth minimizing : ");
        JPanel girthPanel = new JPanel();
        girthPanel.add(girthLabel);
        girthPanel.setPreferredSize(new Dimension(190, 30));
        JPanel totalPanel = new JPanel();
        totalPanel.add(totGirthBox);
        totalPanel.setPreferredSize(new Dimension(190, 30));
        JPanel girTotPanel = new JPanel();
        girTotPanel.setLayout(new GridLayout(1, 2));
        girTotPanel.add(girthPanel);
        girTotPanel.add(totalPanel);
        JLabel randomLabel = new JLabel("Minimizing type : ");
        JPanel randomPanel = new JPanel();
        randomPanel.add(randomLabel);
        randomPanel.setPreferredSize(new Dimension(190, 30));
        JPanel rndPanel = new JPanel();
        rndPanel.add(randomGirth);
        rndPanel.setPreferredSize(new Dimension(190, 30));
        JPanel allrnPanel = new JPanel();
        allrnPanel.setLayout(new GridLayout(1, 2));
        allrnPanel.add(randomPanel);
        allrnPanel.add(rndPanel);
        JLabel boundLabel = new JLabel("Bound attempts : ");
        JPanel boundPanel = new JPanel();
        boundPanel.add(boundLabel);
        boundPanel.setPreferredSize(new Dimension(190, 30));
        JPanel upbndPanel = new JPanel();
        upbndPanel.add(boundGirth);
        upbndPanel.setPreferredSize(new Dimension(190, 30));
        JPanel allbnPanel = new JPanel();
        allbnPanel.setLayout(new GridLayout(1, 2));
        allbnPanel.add(boundPanel);
        allbnPanel.add(upbndPanel);
        JLabel upperLabel = new JLabel("Upper bound : ");
        JPanel upperPanel = new JPanel();
        upperPanel.add(upperLabel);
        upperPanel.setPreferredSize(new Dimension(190, 30));
        JPanel upSpnPanel = new JPanel();
        upSpnPanel.add(girthSpinner);
        upSpnPanel.setPreferredSize(new Dimension(150, 30));
        JPanel giSpnPanel = new JPanel();
        giSpnPanel.setLayout(new GridLayout(1, 2));
        giSpnPanel.add(upperPanel);
        giSpnPanel.add(upSpnPanel);
        panel.setLayout(new GridLayout(4, 1));
        panel.add(girTotPanel);
        panel.add(allrnPanel);
        panel.add(allbnPanel);
        panel.add(giSpnPanel);
    }
    
    private void addToDiagramOptions(JPanel diagram) {
        JLabel diagramLabel = new JLabel("Grid diagram : ");
        JPanel diagramPanel = new JPanel();
        diagramPanel.add(diagramLabel);
        diagramPanel.setPreferredSize(new Dimension(190, 30));
        JPanel gridPanel = new JPanel();
        gridPanel.add(diagramBox);
        gridPanel.setPreferredSize(new Dimension(190, 30));
        JPanel gridDigPanel = new JPanel();
        gridDigPanel.setLayout(new GridLayout(1, 2));
        gridDigPanel.add(diagramPanel);
        gridDigPanel.add(gridPanel);
        JLabel divLabel = new JLabel("Subdivision Factor : ");
        JPanel divPanel = new JPanel();
        divPanel.add(divLabel);
        divPanel.setPreferredSize(new Dimension(190, 30));
        JPanel subPanel = new JPanel();
        subPanel.add(subdivSpinner);
        subPanel.setPreferredSize(new Dimension(190, 30));
        JPanel subdivPanel = new JPanel();
        subdivPanel.setLayout(new GridLayout(1,2));
        subdivPanel.add(divPanel);
        subdivPanel.add(subPanel);
        diagram.setLayout(new GridLayout(2,1));
        diagram.add(gridDigPanel);
        diagram.add(subdivPanel);
    }

    private void addToStableOptions(JPanel stable) {
        JLabel improveLabel = new JLabel("Combine Crossings : ");
        JPanel improvePanel = new JPanel();
        improvePanel.add(improveLabel);
        improvePanel.setPreferredSize(new Dimension(190,30));
        JPanel combinePanel = new JPanel();
        combinePanel.add(combBox);
        combinePanel.setPreferredSize(new Dimension(190, 30));
        JPanel combPanel = new JPanel();
        combPanel.setLayout(new GridLayout(1,2));
        combPanel.add(improvePanel);
        combPanel.add(combinePanel);
        JLabel attemptLabel = new JLabel("Combining attempts : ");
        JPanel attemptPanel = new JPanel();
        attemptPanel.add(attemptLabel);
        attemptPanel.setPreferredSize(new Dimension(190, 30));
        JPanel combspinPanel = new JPanel();
        combspinPanel.add(combineSpinner);
        combspinPanel.setPreferredSize(new Dimension(190, 30));
        JPanel spinPanel = new JPanel();
        spinPanel.setLayout(new GridLayout(1,2));
        spinPanel.add(attemptPanel);
        spinPanel.add(combspinPanel);
        JLabel crossLabel = new JLabel("Crossing bound : ");
        JPanel crossPanel = new JPanel();
        crossPanel.add(crossLabel);
        crossPanel.setPreferredSize(new Dimension(190, 30));
        JPanel boundSpin = new JPanel();
        boundSpin.add(boundSpinner);
        boundSpin.setPreferredSize(new Dimension(190, 30));
        JPanel boundPanel = new JPanel();
        boundPanel.setLayout(new GridLayout(1, 2));
        boundPanel.add(crossPanel);
        boundPanel.add(boundSpin);
        stable.setLayout(new GridLayout(3, 1));
        stable.add(combPanel);
        stable.add(spinPanel);
        stable.add(boundPanel);
    }
    
    private void addToKhovanovOptions(JPanel khovanov) {
        JLabel torsionLabel = new JLabel("Torsion : ");
        JPanel torsionPanel = new JPanel();
        torsionPanel.add(torsionLabel);
        torsionPanel.setPreferredSize(new Dimension(190,30));
        JPanel primaryPanel = new JPanel();
        primaryPanel.add(primTorBox);
        primaryPanel.setPreferredSize(new Dimension(190,30));
        JPanel torPrimPanel = new JPanel();
        torPrimPanel.setLayout(new GridLayout(1,2));
        torPrimPanel.add(torsionPanel);
        torPrimPanel.add(primaryPanel);
        JLabel reductionLabel = new JLabel("Reduced/Unreduced : ");
        JPanel reductLabPanel = new JPanel();
        reductLabPanel.add(reductionLabel);
        reductLabPanel.setPreferredSize(new Dimension(190,30));
        JPanel reductBoxPanel = new JPanel();
        reductBoxPanel.add(redOptions);
        reductBoxPanel.setPreferredSize(new Dimension(190,30));
        JPanel reductionPanel = new JPanel();
        reductionPanel.setLayout(new GridLayout(1,2));
        reductionPanel.add(reductLabPanel);
        reductionPanel.add(reductBoxPanel);
        int n = 0;
        if (options.getKhovRed() & !options.getKhovUnred()) n = 1;
        if (!options.getKhovRed() & options.getKhovUnred()) n = 2;
        redOptions.setSelectedIndex(n);
        khovanov.setLayout(new GridLayout(2,1));
        khovanov.add(torPrimPanel);
        khovanov.add(reductionPanel);
    }

    private void addToCalculationOptions(JPanel calculations) {
        JLabel girthLabel = new JLabel("Information detail : ");
        JPanel girthPanel = new JPanel();
        girthPanel.add(girthLabel);
        girthPanel.setPreferredSize(new Dimension(190,30));
        JPanel detailPanel = new JPanel();
        detailPanel.add(detailBox);
        detailPanel.setPreferredSize(new Dimension(190,30));
        JPanel detGirthPanel = new JPanel();
        detGirthPanel.setLayout(new GridLayout(1,2));
        detGirthPanel.add(girthPanel);
        detGirthPanel.add(detailPanel);
        JLabel showLabel = new JLabel("Calculation Time : ");
        JPanel showPanel = new JPanel();
        showPanel.add(showLabel);
        JPanel calcPanel = new JPanel();
        calcPanel.add(calcTimeBox);
        JPanel detShowPanel = new JPanel();
        detShowPanel.setLayout(new GridLayout(1,2));
        detShowPanel.add(showPanel);
        detShowPanel.add(calcPanel);
        calculations.setLayout(new GridLayout(2,1));
        calculations.add(detGirthPanel);
        calculations.add(detShowPanel);
    }
    
    private void addToFilesOptions(JPanel files) {
        JLabel maxLinksLabel = new JLabel("Maximal links in file :");
        JPanel maxLinksPanel = new JPanel();
        maxLinksPanel.add(maxLinksLabel);
        maxLinksPanel.setPreferredSize(new Dimension(190, 30));
        JPanel spinnerPanel = new JPanel();
        spinnerPanel.add(saveMax);
        spinnerPanel.setPreferredSize(new Dimension(190, 30));
        JPanel maxPanel = new JPanel();
        maxPanel.setLayout(new GridLayout(1, 2));
        maxPanel.add(maxLinksPanel);
        maxPanel.add(spinnerPanel);
        JLabel letterLabel = new JLabel("Export Names :");
        JPanel letterPanel = new JPanel();
        letterPanel.add(letterLabel);
        letterPanel.setPreferredSize(new Dimension(190, 30));
        JPanel changePanel = new JPanel();
        changePanel.add(changeNames);
        changePanel.setPreferredSize(new Dimension(190, 30));
        JPanel expPanel = new JPanel();
        expPanel.setLayout(new GridLayout(1, 2));
        expPanel.add(letterPanel);
        expPanel.add(changePanel);
        files.setLayout(new GridLayout(2, 1));
        files.add(maxPanel);
        files.add(expPanel);
    }
    
}
