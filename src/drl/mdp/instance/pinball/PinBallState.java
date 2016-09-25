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

package drl.mdp.instance.pinball;

import drl.math.geom.Vector;
import drl.mdp.api.State;

/**
 * State in the PinBall domain.
 * 
 * @author Dawit
 * 
 */
public class PinBallState implements State {
    public final double x;
    public final double y;
    public final double xdot;
    public final double ydot;
    private final Vector vector;

    /**
     * State constructor.
     * 
     * @param xx
     *            X coordinate of the ball
     * @param yy
     *            Y coordinate of the ball
     * @param xxdot
     *            X velocity of the ball
     * @param yydot
     *            Y velocity of the ball
     */
    public PinBallState(double xx, double yy, double xxdot, double yydot) {
        x = xx;
        y = yy;
        xdot = xxdot;
        ydot = yydot;
        vector = Vector.asVector(x, y, xdot / 2., ydot / 2.);
    }

    public PinBallState(Vector v) {
        x = v.get(0);
        y = v.get(1);
        xdot = v.get(2) * 2.;
        ydot = v.get(3) * 2.;
        vector = v;
    }

    public Vector asVector() {
        return vector;
    }

    @Override
    public String toString() {
        return "PinBallState" + vector;
    }

}
