/*

Copyright (C) 2019-20 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author Dirk
 */
public class TimerDialog extends JDialog {
    
    private final long time;
    private final JFrame frame;
    
    public TimerDialog(JFrame fram, String title, boolean bo, long tm) {
        super(fram, title, bo);
        frame = fram;
        time = tm/ 100000000l; // the last character is a small L
    }
    
    public void setup() {
        setSize(360,100);
        setResizable(false);
        setLocationRelativeTo(frame);
        setLayout(new BorderLayout());
        JLabel label = new JLabel(timeOf(), SwingConstants.CENTER);
        JPanel centerPanel = new JPanel();
        centerPanel.add(label);
        add(centerPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        JButton okayButton = new JButton("OK");
        buttonPanel.add(okayButton);
        add(buttonPanel, BorderLayout.SOUTH);
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        setVisible(true);
    }
    
    private String timeOf() {
        long tenthsec = time;// + 600l  * 51l + 600l * 60l * 1l + 600l *60l * 24l * 15l;
        String value = String.valueOf((tenthsec/10l)%60l)+"."+String.valueOf(tenthsec%10l)+" Seconds";
        long minute = (tenthsec/600l);
        String min = " Minute";
        if (minute%60l != 1l) min = min+"s";
        if (minute == 0) return value;
        if (minute % 60l != 0) value = String.valueOf(minute%60l)+min+" "+value;
        long hour = (minute/60l);
        String hours = " Hour";
        if (hour%24l != 1l) hours = hours+"s";
        if (hour == 0) return value;
        if (hour % 24l != 0) value = String.valueOf(hour%24l)+hours+" "+value;
        long day = (hour/24l);
        String days = " Day";
        if (day != 1l) days = days+"s";
        if (day == 0) return value;
        return String.valueOf(day)+days+" "+value;
    }
}
