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

package knotjob.homology.slthree.foam;

import java.util.ArrayList;

/**
 *
 * @author Dirk
 */
public class GlueFacet {
    
    private final ArrayList<Facet> facets;
    private final ArrayList<Integer> endpoints;
    private final ArrayList<Integer> gluepoints;
    
    public GlueFacet(Facet facet) {
        facets = new ArrayList<Facet>();
        facets.add(facet);
        
        if (facet.isClosed()) System.out.println("unexp "+facet);
        
        endpoints = facet.endPoints();
        gluepoints = new ArrayList<Integer>();
    }
    
    public GlueFacet(GlueFacet oFacet, GlueFacet tFacet) {
        facets = new ArrayList<Facet>();
        endpoints = new ArrayList<Integer>();
        gluepoints = new ArrayList<Integer>();
        for (Facet fac : oFacet.facets) facets.add(fac);
        for (Facet fac : tFacet.facets) facets.add(fac);
        for (Integer ep : oFacet.endpoints) endpoints.add(ep);
        for (Integer ep : tFacet.endpoints) {
            if (endpoints.contains(ep)) {
                endpoints.remove(ep);
                gluepoints.add(ep);
            }
            else endpoints.add(ep);
        }
        for (Integer gp : oFacet.gluepoints) gluepoints.add(gp);
        for (Integer gp : tFacet.gluepoints) gluepoints.add(gp);
    }
    
    public boolean combinesWith(GlueFacet gFacet) {
        for (int ep : endpoints) if (gFacet.endpoints.contains(ep)) return true;
        return false;
    }
    
    public Facet getFacet(Web domain, Web cdmain) {
        int dts = 0;
        int eul = 0;
        ArrayList<Edge> bot = new ArrayList<Edge>();
        ArrayList<Edge> top = new ArrayList<Edge>();
        for (Facet fac : facets) {
            dts = dts+fac.getDots();
            eul = eul+fac.getEuler();
            for (Edge ed : fac.getDomainEdges()) {
                Edge ned = domain.getEdgeIncluding(ed);
                if (!bot.contains(ned)) bot.add(ned);
            }
            for (Edge ed : fac.getCodomainEdges()) {
                Edge ned = cdmain.getEdgeIncluding(ed);
                if (!top.contains(ned)) top.add(ned);
            }
        }
        eul = eul - gluepoints.size();
        if (eul >= 2) {
            System.out.println(bot);
            System.out.println(top);
            for (Facet fac : facets) System.out.println("Facet "+fac);
            System.out.println(gluepoints);
        }
        Facet facet = new Facet(dts, eul);
        facet.addEdges(bot, top);
        return facet;
    }
    
    public boolean contains(Facet fac) {
        return facets.contains(fac);
    }
    
}
