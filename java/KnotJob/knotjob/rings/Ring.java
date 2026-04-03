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

package knotjob.rings;

/**
 *
 * @author Dirk
 * @param <R>
 */
public interface Ring<R extends Ring> {
    
    public R add(R r);
    public R div (R r);
    public R getZero();
    public R invert();
    public boolean divides(R r);
    public boolean isBigger(R r);
    public boolean isInvertible();
    public boolean isZero();
    public R multiply(R r);
    public R negate();
    public R abs(int i);
}
