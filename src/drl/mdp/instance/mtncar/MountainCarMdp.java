/*
 * Copyright 2014 Dawit Zewdie (dawit at alum dot mit dot edu)
 *  Adapted from code by Richard Sutton, Adam White (<adamwhite.ca>),
 *  and Brian Tanner (btanner at rl-community dot org).
 *  The adaptation includes modifications. 
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
 */package drl.mdp.instance.mtncar;

import drl.math.MathUtils;
import drl.math.geom.Cell;
import drl.math.geom.Interval;
import drl.math.geom.Vector;
import drl.mdp.api.MDP;

/**
 * The MountainCar domain. Adapted from code by George Konidaris, which was
 * adapted from code written by various authors (including Richard Sutton, Adam
 * White, and Brian Tanner) and available on RL-Glue.
 * 
 * @author Dawit
 * 
 */
public class MountainCarMdp implements MDP<CarState, CarAction> {

    private final MtnCarParams theta;
    private final Cell domain;
    private final CarState startState;

    public MountainCarMdp(MtnCarParams theta) {
        this.theta = theta;
        this.domain = Cell.of(new Interval(theta.minPos, theta.maxPos - theta.minPos),
                new Interval(theta.minVel, theta.maxVel - theta.minVel));
        this.startState = new CarState(-0.5, 0);
    }

    /**
     * Static constructor for MountainCarMdp
     * 
     * @return An instance of MountainCarMdp with default settings for all
     *         parameters.
     */
    public static MountainCarMdp defaultParams() {
        return new MountainCarMdp(MtnCarParams.defaultMtnCar());
    }

    @Override
    public Cell getStateSpace() {
        return domain;
    }

    @Override
    public CarAction[] getActions() {
        return CarAction.values();
    }

    @Override
    public double getDiscountFactor() {
        return .999;
    }

    @Override
    public double getReward(CarState start, CarAction action, CarState end) {
        if (isTerminal(end)) {
            return 0;
        }
        return -1;
    }

    @Override
    public CarState getStartState() {
        return startState;
    }

    @Override
    public int getStateDimensions() {
        return 2;
    }

    @Override
    public CarState simulate(CarState state, CarAction action) {
        if (isTerminal(state)) {
            return state;
        }
        double x = state.x;
        double xDot = state.xDot;

        xDot += action.force * 0.001 + Math.cos(3 * x) * (-0.0025);
        xDot = MathUtils.clip(xDot, theta.minVel, theta.maxVel);
        x += xDot;

        if (x < theta.minPos) {
            x = theta.minPos;
            xDot = 0;
        }
        x = Math.min(x, theta.maxPos);
        return new CarState(x, xDot);
    }

    @Override
    public CarState stateFromVector(Vector v) {
        return new CarState(v);
    }

    @Override
    public Vector vectorFromState(CarState s) {
        return s.asVector;
    }

    @Override
    public boolean isTerminal(CarState s) {
        // Note, this implementation is different from the standard
        // implementation of Mountain-Car.
        // In the terminal state, the car must have positive velocity.
        // This change has no effect on the optimal policy for reachable states
        // but makes the value function nicer to look at.
        return s.x > theta.goalPos && s.xDot > 0;
    }

}
