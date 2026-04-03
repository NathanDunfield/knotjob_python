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

package knotjob.polynomial.moy;

import java.util.ArrayList;
import knotjob.polynomial.Polynomial;

/**
 *
 * @author Dirk
 */
public class MoyWebPolynomial {
    
    private final Polynomial polynomial;
    private final int web;
    private final int labels;
    
    public MoyWebPolynomial(Polynomial poly, int w, int l) {
        polynomial = poly;
        web = w;
        labels = l;
    }

    public void output() {
        System.out.println(polynomial+" ("+web+", "+labels+")");
    }

    public ArrayList<Edge> getWeb(MoyCache cache) {
        return cache.getEdgesFromWeb(web);
    }
    
    public ArrayList<Triple<Edge>> getTriples(MoyCache cache) {
        return cache.getTriplesFromWeb(web);
    }
    
    public ArrayList<Integer> getLabels(MoyCache cache) {
        return cache.getLabelsFromWeb(web, labels);
    }
    
    public Polynomial getPolynomial() {
        return polynomial;
    }
    
}
