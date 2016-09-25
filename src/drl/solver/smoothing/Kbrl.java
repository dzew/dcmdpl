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

package drl.solver.smoothing;

import java.util.List;

import drl.math.MathUtils;
import drl.math.geom.Vector;
import drl.math.tfs.DistanceFunction;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.State;
import drl.mdp.utils.Transition;

import org.ejml.simple.SimpleMatrix;

/**
 * Static utility class containing implementations of kernel based reinforcement
 * learning and kernel based stochastic factorization.
 * 
 * @author Dawit
 * 
 */
public class Kbrl {

    private Kbrl() {
    }

    /**
     * Solve and MDP using KBSF
     * 
     * @param instance
     *            The MDP to be solved.
     * @param repStates
     *            The representative states.
     * @param samples
     *            The sample transitions.
     * @param adf
     *            The action dependent metric.
     * @param bandwidth
     *            The bandwidth to use.
     * @param steps
     *            The number of rounds of value iteration to perform.
     * @return The resulting Q-values.
     */
    public static <S extends State, A extends Action> KernelQValue<S, A> solveByKbsf(
            MDP<S, A> instance, List<S> repStates, SampleTransitions<S, A> samples,
            ActionDistanceFn<A> adf, double bandwidth, int steps) {
        System.out.println("Starting KBSF");
        List<Transition<S, A>> tList = samples.get(instance.getActions()[0]);
        Vector[] xs = new Vector[tList.size()];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = tList.get(i).getStartVector();
            adf.memoize(xs[i]);
        }
        Vector[] rxs = new Vector[repStates.size()];
        for (int i = 0; i < repStates.size(); i++) {
            rxs[i] = instance.vectorFromState(repStates.get(i));
            adf.memoize(rxs[i]);
        }
        Vector[][] ys = new Vector[instance.getActions().length][xs.length];
        double[][] rewards = new double[ys.length][xs.length];
        SimpleMatrix[] repRewards = new SimpleMatrix[ys.length];
        for (A action : instance.getActions()) {
            int a = action.ordinal();
            List<Transition<S, A>> ts = samples.get(action);
            DistanceFunction df = adf.get(action);
            for (int i = 0; i < xs.length; i++) {
                ys[a][i] = ts.get(i).getEndVector();
                df.memoize(ys[a][i]);
                rewards[a][i] = ts.get(i).getReward();
            }
        }

        SimpleMatrix[] dMatrix = new SimpleMatrix[instance.getActions().length];
        SimpleMatrix[] pMatrix = new SimpleMatrix[instance.getActions().length];

        boolean uniqueDf = true;
        for (A action : instance.getActions()) {
            if (adf.get(action) != adf.get(instance.getActions()[0])) {
                uniqueDf = false;
                break;
            }
        }

        SimpleMatrix kMat = null;
        for (A action : instance.getActions()) {
            DistanceFunction df = adf.get(action);
            if (!uniqueDf || kMat == null) {
                kMat = KbUtils.makeK(xs, rxs, df, bandwidth);
            }
            System.out.println("Preprocessing action " + action);
            int a = action.ordinal();
            double[][] dMat = new double[xs.length][repStates.size()];
            for (int i = 0; i < xs.length; i++) {
                for (int j = 0; j < repStates.size(); j++) {
                    dMat[i][j] = KbUtils.gaussian(df.distance(ys[a][i], rxs[j]), bandwidth);
                }
            }
            for (int i = 0; i < xs.length; i++) {
                double sum = MathUtils.sum(dMat[i]);
                if (sum == 0.0) {
                    System.out.println("All-zero row in transition matrix. Bandwidth too small?");
                    int indexOfClosest = -1;
                    double closest = Double.POSITIVE_INFINITY;
                    for (int j = 0; j < repStates.size(); j++) {
                        double dist = df.distance(ys[a][i], rxs[j]);
                        if (dist < closest) {
                            closest = dist;
                            indexOfClosest = j;
                        }
                    }
                    dMat[i][indexOfClosest] = 1.0;
                } else {
                    for (int j = 0; j < repStates.size(); j++) {
                        dMat[i][j] /= sum;
                    }
                }
            }
            double[] vals = new double[repStates.size()];
            for (int j = 0; j < repStates.size(); j++) {
                for (int i = 0; i < xs.length; i++) {
                    vals[j] += kMat.get(j, i) * rewards[action.ordinal()][i];
                }
            }
            repRewards[action.ordinal()] = new SimpleMatrix(vals.length, 1, false, vals);
            dMatrix[action.ordinal()] = new SimpleMatrix(dMat);
            pMatrix[action.ordinal()] = kMat.mult(dMatrix[action.ordinal()]);
        }

        SimpleMatrix[] qValues = new SimpleMatrix[repRewards.length];
        for (int i = 0; i < qValues.length; i++) {
            qValues[i] = repRewards[i];
        }
        SimpleMatrix values = new SimpleMatrix(repStates.size(), 1);
        for (int i = 0; i < steps; i++) {
            // SimpleMatrix[] newVals = new SimpleMatrix[ys.length];
            for (int a = 0; a < ys.length; a++) {
                qValues[a] = repRewards[a].plus(instance.getDiscountFactor(),
                        pMatrix[a].mult(values));
            }
            double[] vals = new double[repStates.size()];
            for (int j = 0; j < vals.length; j++) {
                double val = Double.NEGATIVE_INFINITY;
                for (int a = 0; a < qValues.length; a++) {
                    val = Math.max(val, qValues[a].get(j));
                }
                vals[j] = val;
            }
            SimpleMatrix newVals = new SimpleMatrix(vals.length, 1, false, vals);
            SimpleMatrix diff = newVals.minus(values);
            double max = Double.NEGATIVE_INFINITY;
            double min = Double.POSITIVE_INFINITY;
            for (int w = 0; w < diff.getNumElements(); w++) {
                max = Math.max(max, diff.get(w));
                min = Math.min(min, diff.get(w));
            }
            values = newVals;
            if (max - min < .00001) {
                System.out.println("Computation converged in " + i + " iterations.");
                break;
            }
        }
        for (int a = 0; a < ys.length; a++) {
            qValues[a] = dMatrix[a].mult(qValues[a]);
        }
        double[][] qValArray = new double[qValues.length][xs.length];
        for (int a = 0; a < ys.length; a++) {
            for (int i = 0; i < qValArray[a].length; i++) {
                qValArray[a][i] = qValues[a].get(i);
            }
        }

        KernelQValue<S, A> qval = KernelQValue.of(instance, samples, adf, bandwidth);
        return qval.update(qValArray);
    }

    public static <S extends State, A extends Action> KernelQValue<S, A> solve(
            KernelQValue<S, A> qval, MDP<S, A> mdp, SampleTransitions<S, A> samples, int steps) {
        System.out.println("Starting KBRL.");
        A[] actions = mdp.getActions();
        double[][] news = new double[actions.length][];
        for (int i = 0; i < news.length; i++) {
            news[i] = new double[samples.get(actions[i]).size()];
        }

        for (int trial = 0; trial < steps; trial++) {
            for (A action : actions) {
                int a = action.ordinal();
                for (int i = 0; i < news[a].length; i++) {
                    double val = Double.NEGATIVE_INFINITY;
                    List<Transition<S, A>> data = samples.get(action);
                    if (mdp.isTerminal(data.get(i).getEndState())) {
                        val = 0;
                    } else {
                        for (A a2 : actions) {
                            val = Math.max(val, qval.getValue(data.get(i).getEndVector(), a2));
                        }
                    }
                    news[a][i] = val;
                }
            }
            KernelQValue<S, A> newQval = qval.update(news);
            double diff = qval.difference(newQval);
            System.out.println("#" + diff + " on iteration" + trial);
            qval = newQval;
            if (diff < .0001) {
                System.out.println("Converged after " + trial + " rounds of value iteration.");
                break;
            }
        }
        return qval;
    }

}
