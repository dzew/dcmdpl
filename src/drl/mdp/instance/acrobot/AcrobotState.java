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

package drl.mdp.instance.acrobot;

import drl.math.geom.Vector;
import drl.mdp.api.State;

/**
 * A representation of the state of Acrobot.
 * 
 * @author Dawit
 * 
 */
public class AcrobotState implements State {

    public final double theta1;
    public final double theta2;
    public final double theta1Dot;
    public final double theta2Dot;

    final Vector stateVector;

    /**
     * 
     * @param t1
     *            Top link angular position.
     * @param t2
     *            Bottom link angular position.
     * @param t1d
     *            Top link angular velocity.
     * @param t2d
     *            Bottom link angular velocity.
     */
    public AcrobotState(double t1, double t2, double t1d, double t2d) {
        this.theta1 = t1;
        this.theta2 = t2;
        this.theta1Dot = t1d;
        this.theta2Dot = t2d;
        this.stateVector = Vector.asVector(t1, t2, t1d, t2d);
    }

    AcrobotState(Vector v) {
        this.theta1 = v.get(0);
        this.theta2 = v.get(1);
        this.theta1Dot = v.get(2);
        this.theta2Dot = v.get(3);
        this.stateVector = v;
    }

    @Override
    public String toString() {
        return "AcrobotState" + stateVector;
    }
}
