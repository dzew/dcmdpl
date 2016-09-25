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

/**
 * A representation of a compact interval on the real line.
 * 
 * @author Dawit
 * 
 */
public class Interval {
    private final double start;
    private final double width;
    private final double end;

    /**
     * 
     * @param start
     *            The start of the interval.
     * @param width
     *            The width of the interval.
     */
    public Interval(double start, double width) {
        this.start = start;
        this.width = Math.abs(width);
        this.end = start + this.width;
    }

    public double getStart() {
        return start;
    }

    public double getWidth() {
        return width;
    }

    public double getEnd() {
        return end;
    }

    public boolean contains(double d) {
        return (d >= start && d <= end);
    }

    @Override
    public String toString() {
        return String.format("[%.2f, %.2f]", start, end);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(end);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(start);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(width);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Interval other = (Interval) obj;
        if (Double.doubleToLongBits(end) != Double.doubleToLongBits(other.end))
            return false;
        if (Double.doubleToLongBits(start) != Double.doubleToLongBits(other.start))
            return false;
        if (Double.doubleToLongBits(width) != Double.doubleToLongBits(other.width))
            return false;
        return true;
    }
}
