/*
 * Copyright 2014 Dawit Zewdie (dawit at alum dot mit dot edu)
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package drl.math.geom;

import java.util.Arrays;

/**
 * A representation of a k-cell.
 */
public class Cell {

    private final Interval[] intervals;

    private Cell(Interval[] intervals) {
        this.intervals = intervals;
    }

    /**
     * A static constructor
     * 
     * @param intervals
     *            The intervals that make up this k-cell.
     * @return
     */
    public static Cell of(Interval... intervals) {
        return new Cell(Arrays.copyOf(intervals, intervals.length));
    }

    public Interval getInterval(int i) {
        return intervals[i];
    }

    public int getDimensions() {
        return intervals.length;
    }

    public boolean contains(Vector v) {
        for (int i = 0; i < intervals.length; i++) {
            if (!intervals[i].contains(v.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(intervals.length + "-Cell<" + intervals[0]);
        for (int i = 1; i < intervals.length; i++) {
            sb.append("x" + intervals[i]);
        }
        sb.append(">");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(intervals);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Cell other = (Cell) obj;
        if (!Arrays.equals(intervals, other.intervals)) return false;
        return true;
    }

}
