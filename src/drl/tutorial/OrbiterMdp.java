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

import drl.math.geom.Cell;
import drl.math.geom.Vector;
import drl.mdp.api.MDP;

public class OrbiterMdp implements MDP<OrbiterState, OrbiterAction> {

    final double dt = .1; // time discretization.
    final double g = .25; // gravity.
    final int steps = 20; // trajectory smoothness.
    final double drag = .9995; // coefficient of friction.

    final double maxRadius = 1.; // radius of celestial sphere.
    final double minRadius = .1; // radius of planet.
    final double targetRadius = .85; // desired orbit radius.
    final double maxSpeed = 2.0; // maximum attainable speed.
    final double targetSpeed = .4; // speed needed to stay in desired orbit.
    final double tolerance = .08; // allowable margin of error.

    // The start state.
    private final OrbiterState startState = new OrbiterState(.5, 0., 0., -.7);

    @Override
    public OrbiterAction[] getActions() {
        // TODO return an array of actions, ordered by ordinal value.
        throw new RuntimeException("This method has not been implemented yet.");
    }

    @Override
    public double getDiscountFactor() {
        return .999;
    }

    @Override
    public double getReward(OrbiterState start, OrbiterAction action, OrbiterState end) {
        // TODO Return the reward for the transition (start, action, end).
        // if end is terminal, the reward should be 0, otherwise it should be
        // -1.
        throw new RuntimeException("This method has not been implemented yet.");
    }

    @Override
    public OrbiterState getStartState() {
        return startState;
    }

    @Override
    public int getStateDimensions() {
        return 4;
    }

    @Override
    public Cell getStateSpace() {
        // TODO return the bounding box of the state space.
        // Hint, a state is [x, y, xDot, yDot] and:
        // |x| <= 1, |y| <= 1, |xDot| < 2, |yDot| < 2.
        throw new RuntimeException("This method has not been implemented yet.");
    }

    @Override
    public boolean isTerminal(OrbiterState s) {
        double rad = Math.sqrt(s.x * s.x + s.y * s.y);
        double vel = Math.sqrt(s.xDot * s.xDot + s.yDot * s.yDot);
        double angle = Math.atan2(s.yDot, s.xDot) - Math.atan2(s.y, s.x);

        return Math.abs(targetRadius - rad) < tolerance && Math.abs(targetSpeed - vel) < tolerance
                && Math.cos(angle) < 2 * tolerance;
    }

    @Override
    public OrbiterState simulate(OrbiterState state, OrbiterAction action) {
        if (isTerminal(state)) {
            return state;
        }
        double x = state.x, y = state.y, xDot = state.xDot, yDot = state.yDot;

        for (int i = 0; i < steps; i++) {
            double r = x * x + y * y;
            double theta = Math.atan2(y, x);

            double dx = action.v_tangent * Math.sin(theta) + action.v_norm * Math.cos(theta);
            double dy = action.v_tangent * Math.cos(theta) + action.v_norm * Math.sin(theta);

            if (r > minRadius * minRadius) {
                dx -= g * Math.cos(theta) / r;
                dy -= g * Math.sin(theta) / r;
            }

            xDot += dx * dt / steps;
            yDot += dy * dt / steps;

            xDot *= Math.pow(drag, 1.0 / steps);
            yDot *= Math.pow(drag, 1.0 / steps);

            x += xDot * dt / steps;
            y += yDot * dt / steps;

            r = Math.sqrt(x * x + y * y);
            if (r > maxRadius) {
                x *= maxRadius / r;
                y *= maxRadius / r;

                double rot = theta - Math.atan2(yDot, xDot) + Math.PI;

                dx = Math.cos(rot) * (xDot) + Math.sin(rot) * (yDot);
                yDot = Math.sin(rot) * (xDot) - Math.sin(rot) * (yDot);
                xDot = dx;
            }
            double v = Math.sqrt(xDot * xDot + yDot * yDot);
            if (v > maxSpeed) {
                xDot *= maxSpeed / v;
                yDot *= maxSpeed / v;
            }
        }

        return new OrbiterState(x, y, xDot, yDot);
    }

    public boolean isValid(OrbiterState state) {
        return Math.sqrt(state.x * state.x + state.y * state.y) <= maxRadius
                && Math.sqrt(state.xDot * state.xDot + state.yDot * state.yDot) <= maxSpeed;
    }

    @Override
    public OrbiterState stateFromVector(Vector v) {
        return new OrbiterState(v);
    }

    @Override
    public Vector vectorFromState(OrbiterState s) {
        return s.asVector;
    }

}
