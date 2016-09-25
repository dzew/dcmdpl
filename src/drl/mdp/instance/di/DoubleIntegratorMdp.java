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

package drl.mdp.instance.di;

import java.util.Random;

import drl.math.geom.Cell;
import drl.math.geom.Vector;
import drl.mdp.api.MDP;

/**
 * A simple MDP modeling an agent with velocity and position trying to stop at
 * some location.
 * 
 * @author Dawit
 * 
 */
public class DoubleIntegratorMdp implements MDP<DiState, DiAction> {

    private final DiParametrization theta;
    private final DiState startState;
    private final Random random;

    public DoubleIntegratorMdp(DiParametrization theta) {
        this.theta = theta;
        this.startState = new DiState(theta.initialVelocity, theta.initialPosition);
        this.random = new Random();
    }

    /**
     * @return a DoubleIntegrator with all parameters set to default values.
     */
    public static DoubleIntegratorMdp defaultDi() {
        return new DoubleIntegratorMdp(DiParametrization.defaultParametrization());
    }

    @Override
    public DiAction[] getActions() {
        return DiAction.values();
    }

    @Override
    public Cell getStateSpace() {
        return theta.domain;
    }

    @Override
    public double getDiscountFactor() {
        return theta.discountFactor;
    }

    @Override
    public double getReward(DiState start, DiAction action, DiState end) {
        return isTerminal(end) ? 0 : -Math.abs(end.position);
    }

    @Override
    public DiState getStartState() {
        return startState;
    }

    @Override
    public int getStateDimensions() {
        return 2;
    }

    @Override
    public DiState simulate(DiState state, DiAction action) {
        if (isTerminal(state)) {
            return state;
        }
        double pos = state.position + state.velocity;
        double vel = expectedSpeed(state.velocity, action) + simulatedNoise(state.velocity, action);
        return new DiState(vel, pos);
    }

    private double expectedSpeed(double velocity, DiAction action) {
        return velocity * theta.friction + action.impulse * theta.inverseInertia;
    }

    private double simulatedNoise(double velocity, DiAction action) {
        return action.sqrtImpulse * random.nextGaussian() * theta.div;
    }

    @Override
    public DiState stateFromVector(Vector v) {
        return new DiState(v);
    }

    @Override
    public Vector vectorFromState(DiState s) {
        return s.stateVector;
    }

    @Override
    public boolean isTerminal(DiState s) {
        return false;
    }

}
