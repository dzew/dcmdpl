/*
 * Copyright 2014 Dawit Zewdie (dawit at alum dot mit dot edu)
 * Adapted (with modifications) from code by George Konidaris
 * (gdk at csail dot mit dot edu).
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

package drl.mdp.instance.pinball;

public class Ball {
    private final double x;
    private final double y;
    private final double xDot;
    private final double yDot;
    private final double radius;
    private final Point center;

    public Ball(double x, double y, double xDot, double yDot, double radius) {
        this.x = x;
        this.y = y;
        this.xDot = xDot;
        this.yDot = yDot;
        this.radius = radius;
        this.center = new Point(x, y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getXDot() {
        return xDot;
    }

    public double getYDot() {
        return yDot;
    }

    public double getRadius() {
        return radius;
    }

    public Point getCenter() {
        return center;
    }

    public double getVelocity() {
        return Math.sqrt(xDot * xDot + yDot * yDot);
    }

}
