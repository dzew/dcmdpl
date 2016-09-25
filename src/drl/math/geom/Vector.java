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
 * An immutable view onto an array of doubles.
 * 
 * @author Dawit
 * 
 */
public class Vector {

    private final double[] points;
    private final int dimensions;

    private Vector(double[] points) {
        this.points = points;
        this.dimensions = this.points.length;
    }

    /**
     * Creates a Vector from a given array of doubles. This method copies the
     * given array.
     */
    public static Vector asVector(double... points) {
        return new Vector(Arrays.copyOf(points, points.length));
    }

    public static Vector indicator(int index, int dimensions) {
        double[] d = new double[dimensions];
        d[index] = 1;
        return new Vector(d);
    }

    public double get(int i) {
        return points[i];
    }

    public int getDimensions() {
        return dimensions;
    }

    /**
     * @return A string representation of the vector without rounding.
     */
    public String untruncated() {
        return Arrays.toString(points);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%.3f", points[0]));
        for (int i = 1; i < dimensions; i++) {
            sb.append(String.format(", %.3f", points[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(points);
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
        Vector other = (Vector) obj;
        if (!Arrays.equals(points, other.points))
            return false;
        return true;
    }

}
