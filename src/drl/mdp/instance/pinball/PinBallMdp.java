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

import drl.math.MathUtils;
import drl.math.geom.Cell;
import drl.math.geom.Interval;
import drl.math.geom.Vector;
import drl.mdp.api.MDP;

/**
 * Implementation of the PinBall domain adapted from code by George Konidaris.
 * 
 * @author Dawit
 * 
 */
public class PinBallMdp implements MDP<PinBallState, PinBallAction> {

    private final PinBallState startState;
    private final Cell domain;
    private final PinBallParams params;
    public static int wallHits = 0;

    public PinBallMdp(PinBallParams params) {
        this.params = params;
        this.domain = Cell.of(new Interval(0, 1.),
                new Interval(0, 1.),
                new Interval(-.5, 1.),
                new Interval(-.5, 1.));
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
    public double getReward(PinBallState start, PinBallAction action, PinBallState end) {
        return isTerminal(end) ? 0. : (action == PinBallAction.NOOP) ? -1. : -5.;
    }

    @Override
    public PinBallState getStartState() {
        return startState;
    }

    @Override
    public int getStateDimensions() {
        return 4;
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
        if (isTerminal(state)) {
            return state;
        }
        double xDot = MathUtils.clip(state.xdot + action.dx, -1, 1);
        double yDot = MathUtils.clip(state.ydot + action.dy, -1, 1);
        double x = state.x;
        double y = state.y;

        for (int i = 0; i < 20 * 3; i++) {
            boolean collision = false;
            x += xDot * params.ballRadius / 20.;
            y += yDot * params.ballRadius / 20.;

            double dx = x - params.targetX;
            double dy = y - params.targetY;
            if (params.targetRadius * params.targetRadius > dx * dx + dy * dy) {
                break;
            }

            Ball ball = new Ball(x, y, xDot, yDot, params.ballRadius);
            for (Obstacle obstacle : params.obstacles) {
                Point result = obstacle.checkCollision(ball);
                if (result != null) {
                    if (collision) {
                        xDot = -ball.getXDot();
                        yDot = -ball.getYDot();
                        break;
                    }
                    xDot = result.x;
                    yDot = result.y;
                    collision = true;
                    wallHits++;
                }
            }
        }
        // x = MathUtils.clip(x, .05, .95);
        // y = MathUtils.clip(y, .05, .95);
        return new PinBallState(x, y, params.drag * xDot, params.drag * yDot);
    }

    @Override
    public PinBallState stateFromVector(Vector v) {
        return new PinBallState(v);
    }

    @Override
    public Vector vectorFromState(PinBallState s) {
        return s.asVector();
    }

}
