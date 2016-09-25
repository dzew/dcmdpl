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

import drl.math.geom.Cell;
import drl.math.geom.Interval;
import drl.math.geom.Vector;
import drl.mdp.api.MDP;

/**
 * A two dimensional version of the PinBall domain. Used for testing and for
 * efficiently exploring the reachable state space.
 * 
 * @author Dawit
 * 
 */
public class PinBall2D implements MDP<PinBallState, PinBallAction> {

    private final PinBallState startState;
    private final Cell domain;
    private final PinBallParams params;

    public PinBall2D(PinBallParams params) {
        this.params = params;
        this.domain = Cell.of(new Interval(0, 1), new Interval(0, 1));
        this.startState = new PinBallState(params.startX, params.startY, 0, 0);

    }

    @Override
    public PinBallAction[] getActions() {
        return PinBallAction.values();
    }

    @Override
    public double getDiscountFactor() {
        return .999;
    }

    @Override
    public double getReward(PinBallState state, PinBallAction action, PinBallState end) {
        return isTerminal(end) ? 0. : -1.;
    }

    @Override
    public PinBallState getStartState() {
        return startState;
    }

    @Override
    public int getStateDimensions() {
        return 2;
    }

    @Override
    public Cell getStateSpace() {
        return domain;
    }

    @Override
    public boolean isTerminal(PinBallState s) {
        double dx = s.x - params.targetX;
        double dy = s.y - params.targetY;
        return params.targetRadius * params.targetRadius > dx * dx + dy * dy;
    }

    @Override
    public PinBallState simulate(PinBallState state, PinBallAction action) {
        // if (isTerminal(state)) {
        // return state;
        // }
        double angle = Math.PI * 2 * action.ordinal() / 5.;
        double dx = Math.cos(angle) * params.ballRadius / 20.;
        double dy = Math.sin(angle) * params.ballRadius / 20.;
        double x = state.x;
        double y = state.y;

        for (int i = 0; i < 20; i++) {
            x += dx;
            y += dy;
            // double errx = x - params.targetX;
            // double erry = y - params.targetY;
            // if (params.targetRadius * params.targetRadius > errx * errx +
            // erry * erry) {
            // break;
            // }

            boolean collision = false;
            Ball ball = new Ball(x, y, dx, dy, params.ballRadius);
            for (Obstacle obstacle : params.obstacles) {
                Point result = obstacle.checkCollision(ball);
                if (result != null) {
                    double noise = (Math.random() - .5) * .25;
                    if (collision) {
                        dx = -ball.getXDot() * Math.cos(noise) + ball.getYDot() * Math.sin(noise);
                        dy = -ball.getXDot() * Math.sin(noise) - ball.getYDot() * Math.cos(noise);
                        break;
                    }
                    dx = result.x;
                    dy = result.y;
                    collision = true;
                }
            }
        }
        return new PinBallState(x, y, 0, 0);
    }

    @Override
    public PinBallState stateFromVector(Vector v) {
        return new PinBallState(v.get(0), v.get(1), 0, 0);
    }

    @Override
    public Vector vectorFromState(PinBallState s) {
        return Vector.asVector(s.x, s.y);
    }

}
