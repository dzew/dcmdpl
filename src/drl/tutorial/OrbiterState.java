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

package drl.tutorial;

import drl.math.geom.Vector;
import drl.mdp.api.State;

/**
 * A representation of a state in the Orbiter domain.
 * 
 * @author Dawit
 * 
 */
public class OrbiterState implements State {

    final double x; // The x-position.
    final double y; // The y-position.
    final double xDot; // The x-velocity.
    final double yDot; // The y-velocity.
    final Vector asVector; // The vector representation of this state.

    public OrbiterState(double x, double y, double xDot, double yDot) {
        this.x = x;
        this.y = y;
        this.xDot = xDot;
        this.yDot = yDot;
        this.asVector = Vector.asVector(x, y, xDot, yDot);
    }

    public OrbiterState(Vector v) {
        this.asVector = v;
        this.x = v.get(0);
        this.y = v.get(1);
        this.xDot = v.get(2);
        this.yDot = v.get(3);
    }

}
