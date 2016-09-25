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

package drl.solver;

import java.util.HashMap;
import java.util.Map;

import drl.math.MathUtils;
import drl.math.tfs.DistanceFunction;
import drl.math.tfs.ValueSmoothingDF;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.Policy;
import drl.mdp.api.QValue;
import drl.mdp.api.State;
import drl.mdp.utils.MdpUtils;
import drl.mdp.utils.StaticPolicy;
import drl.solver.leastsquares.LvfApproximator;
import drl.solver.smoothing.ActionDistanceFn;
import drl.solver.smoothing.KernelQValue;

/**
 * A static utility class with providing methods for solving MDPs.
 * 
 * @author Dawit
 * 
 */
public class MdpSolver {

    private MdpSolver() {
    }

    /**
     * Solves an MDP by using policy iteration.
     * 
     * @param mdp
     *            The MDP to be solved.
     * @param approximator
     *            The value function approximator for the mdp.
     * @param initialPolicy
     *            The initial policy to use or null to use arbitrary initial
     *            policy.
     * @param eps
     *            The epsilon used to determine convergence.
     * @param maxIterations
     *            The maximum number of iterations to do before giving up.
     * @return The final Q-values found, regardless of whether or not the
     *         process converged.
     */
    public static <S extends State, A extends Action> QValue<S, A> solveByPolicyIteration(
            MDP<S, A> mdp, LvfApproximator<S, A> approximator, Policy<S, A> initialPolicy,
            double eps, int maxIterations) {
        QValue<S, A> vf = null;
        if (initialPolicy == null) {
            initialPolicy = StaticPolicy.of(mdp.getActions()[0]);
        }
        double diff = Double.POSITIVE_INFINITY;
        for (int i = 0; i < maxIterations; i++) {
            QValue<S, A> oldVf = vf;
            vf = approximator.approximateQValueOf(initialPolicy);
            if (oldVf != null) {
                diff = MdpUtils.getDifference(mdp, vf, oldVf);
            }
            initialPolicy = vf;
            System.out.println("Finished iteration " + i + ", Q-Values changed by " + diff);
            if (diff < eps) {
                System.out.println("Successful convergence!");
                return vf;
            }
        }
        System.out.println("Did not converge.");
        return vf;
    }

    /**
     * A very limited implementation of DKBRL. For a more versatile
     * implementation, copy and modify the code in the experiments.
     * 
     * @param mdp
     *            The MDP to be solved.
     * @param caller
     *            The KbrlCaller to use. Note that the caller may be modified by
     *            this method. The caller can be configured to use KBRL or KBSF.
     * @param iterations
     *            The number of iterations of DKBRL to perform.
     * @param initialAdfn
     *            The initial ActionDistanceFn to use. This parameter should
     *            match the setting in {@code caller}.
     * @param alpha
     *            The relaxation rate.
     * @return The computed Q-values after the final iteration without any
     *         policy evaluation.
     */
    public static <S extends State, A extends Action> QValue<S, A> solveByDkbrl(MDP<S, A> mdp,
            KbrlCaller<S, A> caller, int iterations, ActionDistanceFn<A> initialAdfn, double alpha) {
        System.out.println("Starting DKBRL");
        caller.setActionDistanceFn(initialAdfn);
        KernelQValue<S, A> qvf = caller.solve();
        System.out.println("Finished iteration 0 of KBRL");
        ActionDistanceFn<A> adf = initialAdfn;
        for (int i = 1; i < iterations; i++) {
            Map<A, DistanceFunction> map = new HashMap<A, DistanceFunction>();
            for (A action : mdp.getActions()) {
                double slope = (qvf.getMaxValue(action) - qvf.getMinValue(action))
                        / MathUtils.diameterOf(MathUtils.unitCell(mdp.getStateDimensions()));
                map.put(action, ValueSmoothingDF.of(adf.get(action),
                        qvf.getValue(action),
                        slope,
                        alpha,
                        true));
            }
            adf = ActionDistanceFn.of(map);
            caller.setActionDistanceFn(adf);
            System.out.println("Starting next iteration.");
            qvf = caller.solve();
            System.out.println("Finished iteration " + i + " of KBRL");
        }
        caller.setActionDistanceFn(initialAdfn);
        return qvf;
    }
}
