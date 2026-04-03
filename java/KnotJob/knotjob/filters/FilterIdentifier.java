/*

Copyright (C) 2024 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.filters;

import java.util.ArrayList;

/**
 *
 * @author Dirk
 */
public class FilterIdentifier {

    private final ArrayList<String> commands;
    private final ArrayList<Filter> filters;
    
    public FilterIdentifier(String cmmnd, ArrayList<Filter> fltrs) {
        commands = getCommands(cmmnd);
        filters = fltrs;
    }
    
    public Filter getFilter() {
        if (commands.isEmpty()) return null;
        String first = commands.get(0);
        if ("1".equals(first)) return bltFilter();
        if ("2".equals(first)) return khovFilter();
        return null;
    }

    private ArrayList<String> getCommands(String cmmnd) {
        ArrayList<String> cmds = new ArrayList<String>();
        while (cmmnd.contains(".")) {
            int u = cmmnd.indexOf(".");
            cmds.add(cmmnd.substring(0, u));
            cmmnd = cmmnd.substring(u+1);
        }
        return cmds;
    }
    
    private BLTFilter bltFilter() {
        int[] coms = getCommands(commands.size());
        if (coms == null) return null;
        if (coms.length == 5) {
            int typ = 0;
            if (coms[0] == 1) typ = 1;
            boolean bdd = coms[4] == 1;
            return new BLTFilter("blt", typ, String.valueOf(coms[1]), coms[2], coms[3], bdd);
        }
        if (coms.length == 1) {
            int typ = 0;
            if (coms[0] == 1) typ = 1;
            return new BLTFilter("blt", typ);
        }
        return null;
    }
    
    private KhovFilter khovFilter() {
        int[] coms = getCommands(commands.size());
        if (coms == null) return null;
        if (coms.length != 7) return null;
        if (coms[0] == 3) {
            //boolean odd = coms[1] == 1;
            boolean rat = coms[2] == 1;
            boolean red = coms[3] == 1;
            return new KhovFilter("khov", coms[1], rat, red, new ArrayList<Integer>());
        }
        if (coms[0] == 4 || coms[0] < 2) {
            //boolean odd = coms[1] == 1;
            boolean red = coms[2] == 1;
            boolean bdd = coms[3] == 1;
            if (coms[0] == 4) return new KhovFilter("khov", new ArrayList<Integer>(), 
                    coms[1], red, bdd, coms[4], coms[5], coms[6]);
            if (coms[0] == 0) return new KhovFilter("khov", coms[1], red, bdd, coms[4], 
                    coms[5], new ArrayList<Integer>());
            if (coms[0] == 1) return new KhovFilter("khov", new ArrayList<Integer>(),
                    coms[1], red, bdd, coms[4], coms[5]);
        } 
        return null;
    }
    
    private int[] getCommands(int size) {
        int[] coms = new int[size-1];
        try {
            for (int i = 1; i < size; i++) coms[i-1] = Integer.parseInt(commands.get(i));
        }
        catch (NumberFormatException ne) {
            return null;
        }
        return coms;
    }
    
}
