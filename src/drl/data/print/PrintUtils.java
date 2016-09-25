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

package drl.data.print;

import java.util.HashMap;
import java.util.List;

import drl.math.geom.Cell;
import drl.math.geom.Interval;
import drl.math.geom.Vector;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.Policy;
import drl.mdp.api.QValue;
import drl.mdp.api.State;
import drl.mdp.utils.MdpUtils;
import drl.mdp.utils.Transition;

/**
 * Static utility class providing methods for printing out representations of
 * various MDP related objects.
 * 
 * @author Dawit
 * 
 */
public class PrintUtils {

    private static final int CELLS = 40;

    private PrintUtils() {
    }

    /**
     * Prints a representation of a given Policy for a given MDP.
     * 
     * @param mdp
     *            the MDP on which the policy is to be run.
     * @param policy
     *            the Policy to run.
     */
    public static <S extends State, A extends Action> void printPolicy(MDP<S, A> mdp,
            Policy<S, A> policy) {
        Cell domain = mdp.getStateSpace();
        if (domain.getDimensions() == 1) {
            printPolicy1D(mdp, policy);
        } else if (domain.getDimensions() == 2) {
            printPolicy2D(mdp, policy);
        } else {
            printPolicyND(mdp, policy);
        }
    }

    /**
     * Prints a representation of a given value function over a given MDP.
     * 
     * @param mdp
     *            the MDP on which the policy is to be run.
     * @param vf
     *            the value function to represent.
     */
    public static <S extends State, A extends Action> void printValueFunction(MDP<S, A> mdp,
            QValue<S, A> vf) {
        Cell domain = mdp.getStateSpace();
        if (domain.getDimensions() == 1) {
            printValueFunction1D(mdp, vf);
        } else if (domain.getDimensions() == 2) {
            printValueFunction2D(mdp, vf);
        } else {
            printValueFunctionND(mdp, vf);
        }
    }

    /**
     * Prints the difference between two value functions for a given MDP.
     * 
     * @param mdp
     *            the MDP on which the policy is to be run.
     * @param vf1
     *            one of the value functions.
     * @param vf2
     *            the other value function.
     */
    public static <S extends State, A extends Action> void printDifference(MDP<S, A> mdp,
            QValue<S, A> vf1, QValue<S, A> vf2) {
        Cell domain = mdp.getStateSpace();
        if (domain.getDimensions() == 1) {
            printDifference1D(mdp, vf1, vf2);
        } else if (domain.getDimensions() == 2) {
            printDifference2D(mdp, vf1, vf2);
        } else {
            throw new IllegalArgumentException("Cannot represent " + domain.getDimensions()
                    + " dimensional MDPs");
        }
    }

    /**
     * Displays what the cost to go for the mdp would be if there were no
     * discounting and every non-terminal state had a reward of -1.
     * 
     * @param mdp
     *            the MDP on which the policy is to be run.
     * @param policy
     *            the Policy to run.
     */
    public static <S extends State, A extends Action> void printStepsToGo(MDP<S, A> mdp,
            Policy<S, A> policy) {
        Cell domain = mdp.getStateSpace();
        if (domain.getDimensions() == 1) {
            printStepsToGo1D(mdp, policy);
        } else if (domain.getDimensions() == 2) {
            printStepsToGo2D(mdp, policy);
        } else {
            printGroundTruthND(mdp, policy);
        }
    }

    /**
     * Prints an estimate of the value of a policy. This method is for producing
     * ground truth values.
     * 
     * @param mdp
     *            the MDP on which the policy is to be run.
     * @param policy
     *            the Policy to evaluate.
     */
    public static <S extends State, A extends Action> void printTrueValue(MDP<S, A> mdp,
            Policy<S, A> policy) {
        Cell domain = mdp.getStateSpace();
        if (domain.getDimensions() == 1) {
            printTrueValue1D(mdp, policy);
        } else if (domain.getDimensions() == 2) {
            printTrueValue2D(mdp, policy);
        } else {
            printGroundTruthND(mdp, policy);
        }
    }

    private static <S extends State, A extends Action> void printPolicy1D(MDP<S, A> mdp,
            Policy<S, A> policy) {
        Interval domain = mdp.getStateSpace().getInterval(0);
        for (double d = domain.getStart(); d < domain.getEnd(); d += domain.getWidth() / 20) {
            System.out.print(policy.getAction(mdp.stateFromVector(Vector.asVector(d))));
        }
        System.out.println();
    }

    private static <S extends State, A extends Action> void printValueFunction1D(MDP<S, A> mdp,
            QValue<S, A> vf) {
        Interval domain = mdp.getStateSpace().getInterval(0);
        for (double d = domain.getStart(); d < domain.getEnd(); d += domain.getWidth() / 20) {
            System.out.print(String.format("%.3f ",
                    vf.getValue(mdp.stateFromVector(Vector.asVector(d)))));
        }
        System.out.println();
    }

    private static <S extends State, A extends Action> void printDifference1D(MDP<S, A> mdp,
            QValue<S, A> vf1, QValue<S, A> vf2) {
        Interval domain = mdp.getStateSpace().getInterval(0);
        for (double d = domain.getStart(); d < domain.getEnd(); d += domain.getWidth() / 20) {
            S state = mdp.stateFromVector(Vector.asVector(d));
            System.out.print(String.format("%.3f ", vf1.getValue(state) - vf2.getValue(state)));
        }
        System.out.println();
    }

    private static <S extends State, A extends Action> void printStepsToGo1D(MDP<S, A> mdp,
            Policy<S, A> policy) {
        Interval domain = mdp.getStateSpace().getInterval(0);
        for (double d = domain.getStart(); d < domain.getEnd(); d += domain.getWidth() / 20) {
            S state = mdp.stateFromVector(Vector.asVector(d));
            boolean nonTerminal = true;
            for (int i = 0; i < 100; i++) {
                if (mdp.isTerminal(state)) {
                    System.out.print(String.format(i + " "));
                    nonTerminal = false;
                    break;
                }
                state = mdp.simulate(state, policy.getAction(state));
            }
            if (nonTerminal) {
                System.out.println("110 ");
            }
        }
    }

    private static <S extends State, A extends Action> void printTrueValue1D(MDP<S, A> mdp,
            Policy<S, A> policy) {
        Interval domain = mdp.getStateSpace().getInterval(0);
        for (double d = domain.getStart(); d < domain.getEnd(); d += domain.getWidth() / 20) {
            S state = mdp.stateFromVector(Vector.asVector(d));
            List<Transition<S, A>> trajectory = MdpUtils.rollout(mdp, state, 100, policy);
            double val = MdpUtils.getValue(trajectory, mdp.getDiscountFactor());
            System.out.print(String.format("%.3f ", val));
        }
    }

    private static <S extends State, A extends Action> void printPolicy2D(MDP<S, A> mdp,
            Policy<S, A> policy) {
        Cell domain = mdp.getStateSpace();
        Interval i1 = domain.getInterval(0);
        Interval i2 = domain.getInterval(1);
        System.out.println();
        System.out.println();
        // for (double vel = i2.getStart(); vel < i2.getEnd(); vel +=
        // i2.getWidth() / 20) {
        // System.out.print(String.format("%.2f ", vel));
        // }
        System.out.println();
        for (double pos = i1.getStart(); pos < i1.getEnd(); pos += i1.getWidth() / CELLS) {
            // System.out.print(String.format("%.2f ", pos));
            for (double vel = i2.getStart(); vel < i2.getEnd(); vel += i2.getWidth() / CELLS) {
                System.out.print(policy.getAction(mdp.stateFromVector(Vector.asVector(pos, vel)))
                        .ordinal()
                        + " ");
            }
            System.out.println();
        }
    }

    private static <S extends State, A extends Action> void printValueFunction2D(MDP<S, A> mdp,
            QValue<S, A> vf) {
        printValueCrossSection(mdp, vf, 0, 1, Vector.asVector(0, 0));
    }

    /**
     * Prints a two dimensional slice of a given value function.
     * 
     * @param mdp
     *            The MDP under consideration.
     * @param vf
     *            A value function for the MDP.
     * @param d1
     *            The first dimension to use in the cross section.
     * @param d2
     *            The second dimension to use in the cross section.
     * @param v
     *            A vector specifying what values to use for the non-free
     *            dimensions.
     */
    public static <S extends State, A extends Action> void printValueCrossSection(MDP<S, A> mdp,
            QValue<S, A> vf, int d1, int d2, Vector v) {
        Cell domain = mdp.getStateSpace();
        Interval i1 = domain.getInterval(d1);
        Interval i2 = domain.getInterval(d2);
        double[] d = new double[v.getDimensions()];
        for (int i = 0; i < d.length; i++) {
            d[i] = v.get(i);
        }
        System.out.println();
        System.out.println();
        for (double pos = i1.getStart(); pos < i1.getEnd(); pos += i1.getWidth() / CELLS) {
            for (double vel = i2.getStart(); vel < i2.getEnd(); vel += i2.getWidth() / CELLS) {
                d[d1] = pos;
                d[d2] = vel;
                System.out.print(String.format("%.2f ",
                        vf.getValue(mdp.stateFromVector(Vector.asVector(d)))));
            }
            System.out.println();
        }
        System.out.println();
    }

    private static <S extends State, A extends Action> void printDifference2D(MDP<S, A> mdp,
            QValue<S, A> vf1, QValue<S, A> vf2) {
        Cell domain = mdp.getStateSpace();
        Interval i1 = domain.getInterval(0);
        Interval i2 = domain.getInterval(1);
        System.out.println();
        System.out.println();
        for (double pos = i1.getStart(); pos < i1.getEnd(); pos += i1.getWidth() / CELLS) {
            for (double vel = i2.getStart(); vel < i2.getEnd(); vel += i2.getWidth() / CELLS) {
                S state = mdp.stateFromVector(Vector.asVector(pos, vel));
                System.out.print(String.format("%.2f ", vf1.getValue(state) - vf2.getValue(state)));
            }
            System.out.println();
        }
        System.out.println();
    }

    private static <S extends State, A extends Action> void printStepsToGo2D(MDP<S, A> mdp,
            Policy<S, A> policy) {
        Cell domain = mdp.getStateSpace();
        Interval i1 = domain.getInterval(0);
        Interval i2 = domain.getInterval(1);
        System.out.println();
        System.out.println();
        for (double pos = i1.getStart(); pos < i1.getEnd(); pos += i1.getWidth() / CELLS) {
            for (double vel = i2.getStart(); vel < i2.getEnd(); vel += i2.getWidth() / CELLS) {
                S state = mdp.stateFromVector(Vector.asVector(pos, vel));
                boolean nonTerminal = true;
                for (int i = 0; i < 300; i++) {
                    if (mdp.isTerminal(state)) {
                        System.out.print(String.format(i + " "));
                        nonTerminal = false;
                        break;
                    }
                    state = mdp.simulate(state, policy.getAction(state));
                }
                if (nonTerminal) {
                    // Note that a reading of 300 means that a terminal state
                    // was not reached in 299 steps. It does not necessarily
                    // mean that a terminal state was reached on the 300th step.
                    System.out.print("300 ");
                }
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
    }

    private static <S extends State, A extends Action> void printTrueValue2D(MDP<S, A> mdp,
            Policy<S, A> policy) {
        Cell domain = mdp.getStateSpace();
        Interval i1 = domain.getInterval(0);
        Interval i2 = domain.getInterval(1);
        System.out.println();
        System.out.println();
        for (double pos = i1.getStart(); pos < i1.getEnd(); pos += i1.getWidth() / CELLS) {
            for (double vel = i2.getStart(); vel < i2.getEnd(); vel += i2.getWidth() / CELLS) {
                S state = mdp.stateFromVector(Vector.asVector(pos, vel));
                List<Transition<S, A>> trajectory = MdpUtils.rollout(mdp, state, 100, policy);
                double val = MdpUtils.getValue(trajectory, mdp.getDiscountFactor());
                System.out.print(String.format("%.3f ", val));
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
    }

    private static <S extends State, A extends Action> void printPolicyND(MDP<S, A> mdp,
            Policy<S, A> policy) {
        HashMap<A, Integer> hist = new HashMap<A, Integer>();
        for (A a : mdp.getActions()) {
            hist.put(a, 0);
        }
        System.out.println("Histogram of action selection frequencies:");
        for (int i = 0; i < 100 * hist.size(); i++) {
            A a = policy.getAction(MdpUtils.sampleNonTermainalState(mdp, 100));
            hist.put(a, hist.get(a) + 1);
        }
        System.out.println(hist);
    }

    private static <S extends State, A extends Action> void printValueFunctionND(MDP<S, A> mdp,
            QValue<S, A> vf) {
        double sum = 0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        int n = 100;

        System.out.println(vf);
        System.out.println("Expected min = max_reward(mdp) * "
                + (1. / (1. - mdp.getDiscountFactor())));
        System.out.println("Out of " + n
                + " randomly sampled non-terminal points in the state space");
        for (int i = 0; i < n; i++) {
            double val = vf.getValue(MdpUtils.sampleNonTermainalState(mdp, 100));
            sum += val;
            min = Math.min(min, val);
            max = Math.max(max, val);
        }
        System.out.println("Mean value: " + sum / 100);
        System.out.println("Min value: " + min);
        System.out.println("Max value: " + max);
    }

    private static <S extends State, A extends Action> void printGroundTruthND(MDP<S, A> mdp,
            Policy<S, A> policy) {
        int terminated = 0;
        int steps = 0;
        double rewards = 0;

        int n = 200;
        int cap = 350;

        System.out.println("Out of " + n
                + " randomly sampled non-terminal points in the state space");
        for (int i = 0; i < n; i++) {
            S state = MdpUtils.sampleNonTermainalState(mdp, 100);
            List<Transition<S, A>> trajectory = MdpUtils.rollout(mdp, state, cap, policy);
            rewards += MdpUtils.getValue(trajectory, mdp.getDiscountFactor());
            if (mdp.isTerminal(trajectory.get(trajectory.size() - 1).getEndState())) {
                terminated++;
                steps += trajectory.size();
            }
        }
        System.out.println(terminated + " runs converged.");
        System.out.println("The runs that converged did so in " + (1.0 * steps / terminated)
                + " steps on average.");
        System.out.println("The average reward over all runs was " + (rewards / n));
    }

}
