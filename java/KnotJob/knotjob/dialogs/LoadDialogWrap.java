/*

Copyright (C) 2021 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

/**
 *
 * @author Dirk
 */
public class LoadDialogWrap extends DialogWrap {
    
    private final LoadDialog frame;
    
    public LoadDialogWrap(LoadDialog frm) {
        super(null, null);
        frame = frm;
    }
    
    @Override
    public void setText(String substring) {
        frame.setText(substring);
    }

    @Override
    public void dispose() {
        delay(200);
        frame.dispose();
    }
    
}
