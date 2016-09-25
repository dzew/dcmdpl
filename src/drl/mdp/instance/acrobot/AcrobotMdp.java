/*
 * Copyright 2014 Dawit Zewdie (dawit at alum dot mit dot edu)
 *  Adapted from code by Adam White (<adamwhite.ca>) and Brian Tanner
 *  (btanner at rl-community dot org). The adaptation includes modifications. 
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

import drl.math.geom.Cell;
import drl.math.geom.Interval;
import drl.math.geom.Vector;
import drl.mdp.api.MDP;

/**
 * 
 * @author dawithz
 * 
 *         Adapted from the VariableMassAcrobot code written by George Konidaris
 *         which was adapted from the Acrobot Java code available on the RLGlue
 *         RL Library agents repository. That code was written by Brian Tanner,
 *         adapted from previous code by Adam White. The Acrobot domain itself
 *         was described by Sutton and Barto in Reinforcement Learning, an
 *         Introduction.
 * 
 */
public class AcrobotMdp implements MDP<AcrobotState, AcrobotAction> {

    // More parameters.
    // It might make sense to move some of these to AcrobotParams.
    private static final double dt = 0.05;
    private static final double g = 9.8;

    private static final double lc1 = 0.5;
    private static final double lc2 = 0.5;
    private static final double I1 = 1.0;
    private static final double I2 = 1.0;

    private static final double maxTheta_1 = Math.PI;
    private static final double maxTheta_2 = Math.PI;
    private static final double maxTheta1Dot = 4.0 * Math.PI;
    private static final double maxTheta2Dot = 9.0 * Math.PI;

    private final AcrobotParams params;
    private final AcrobotState startState;
    private final Cell stateSpace;

    public AcrobotMdp(AcrobotParams params) {
        this.params = params;
        this.startState = new AcrobotState(0, 0, 0, 0);
        stateSpace = Cell.of(new Interval(-maxTheta_1, 2 * maxTheta_1),
                new Interval(-maxTheta_2, 2 * maxTheta_2),
                new Interval(-maxTheta1Dot, 2 * maxTheta1Dot),
                new Interval(-maxTheta2Dot, 2 * maxTheta2Dot));
    }

    /**
     * @return an Acrobot instance with all parameters set to their default
     *         values.
     */
    public static AcrobotMdp defaultAcrobot() {
        return new AcrobotMdp(AcrobotParams.defaultParams());
    }

    @Override
    public AcrobotAction[] getActions() {
        return AcrobotAction.values();
    }

    @Override
    public double getDiscountFactor() {
        return .9999;
    }

    @Override
    public double getReward(AcrobotState start, AcrobotAction act, AcrobotState end) {
        return isTerminal(end) ? 0 : -1;
    }

    @Override
    public AcrobotState getStartState() {
        return startState;
    }

    @Override
    public int getStateDimensions() {
        return 4;
    }

    @Override
    public AcrobotState simulate(AcrobotState state, AcrobotAction action) {
        double d1, d2;

        double phi_1, phi_2;

        double theta1_ddot;
        double theta2_ddot;
        double theta1 = state.theta1, theta2 = state.theta2, theta1Dot = state.theta1Dot, theta2Dot = state.theta2Dot;

        // torque is in [-1,1]
        // We'll make noise equal to at most +/- 1
        // double
        // theNoise=transitionNoise*2.0d*(ourRandomNumber.nextDouble()-.5d);
        //
        // torque+=theNoise;

        for (int count = 0; count < 4; count++) {
            if (isTerminal(state)) {
                return state;
            }
            d1 = params.m1
                    * Math.pow(lc1, 2)
                    + params.m2
                    * (Math.pow(params.l1, 2) + Math.pow(lc2, 2) + 2 * params.l1 * lc2
                            * Math.cos(theta2)) + I1 + I2;
            d2 = params.m2 * (Math.pow(lc2, 2) + params.l1 * lc2 * Math.cos(theta2)) + I2;

            phi_2 = params.m2 * lc2 * g * Math.cos(theta1 + theta2 - Math.PI / 2.0);
            phi_1 = -(params.m2 * params.l1 * lc2 * Math.pow(theta2Dot, 2) * Math.sin(theta2) - 2
                    * params.m2 * params.l1 * lc2 * theta1Dot * theta2Dot * Math.sin(theta2))
                    + (params.m1 * lc1 + params.m2 * params.l1)
                    * g
                    * Math.cos(theta1 - Math.PI / 2.0) + phi_2;

            theta2_ddot = (action.torque + (d2 / d1) * phi_1 - params.m2 * params.l1 * lc2
                    * Math.pow(theta1Dot, 2) * Math.sin(theta2) - phi_2)
                    / (params.m2 * Math.pow(lc2, 2) + I2 - Math.pow(d2, 2) / d1);
            theta1_ddot = -(d2 * theta2_ddot + phi_1) / d1;

            theta1Dot += theta1_ddot * dt;
            theta2Dot += theta2_ddot * dt;

            theta1 += theta1Dot * dt;
            theta2 += theta2Dot * dt;
        }

        if (Math.abs(theta1Dot) > maxTheta1Dot) {
            theta1Dot = Math.signum(theta1Dot) * maxTheta1Dot;
        }

        if (Math.abs(theta2Dot) > maxTheta2Dot) {
            theta2Dot = Math.signum(theta2Dot) * maxTheta2Dot;
        }
        /*
         * Put a hard constraint on the Acrobot physics, thetas MUST be in
         * [-PI,+PI] if they reach a top then angular velocity becomes zero
         */
        if (Math.abs(theta2) > maxTheta_2) {
            theta2 = Math.signum(theta2) * maxTheta_2;
            theta2Dot = 0;
        }
        if (Math.abs(theta1) > maxTheta_1) {
            theta1 = Math.signum(theta1) * maxTheta_1;
            theta1Dot = 0;
        }
        return new AcrobotState(theta1, theta2, theta1Dot, theta2Dot);
    }

    @Override
    public Vector vectorFromState(AcrobotState s) {
        return s.stateVector;
    }

    @Override
    public AcrobotState stateFromVector(Vector v) {
        return new AcrobotState(v);
    }

    @Override
    public Cell getStateSpace() {
        return stateSpace;
    }

    /**
     * 
     * @param s
     * @return The height of the tip of the bottom link when the Acrobot is in
     *         state {@code s} with the parameter settings in this instance.
     */
    public double getTip(AcrobotState s) {
        double firstJointEndHeight = params.l1 * Math.cos(s.theta1);
        double secondJointEndHeight = params.l2 * Math.sin(Math.PI / 2 - s.theta1 - s.theta2);

        return -(firstJointEndHeight + secondJointEndHeight);
    }

    @Override
    public boolean isTerminal(AcrobotState s) {
        return getTip(s) >= params.acrobotGoalPosition;
    }

}
