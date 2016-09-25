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

package drl.mdp.utils;

import java.util.ArrayList;
import java.util.List;

import drl.data.vis.DisplayFrame;
import drl.math.MathUtils;
import drl.math.geom.Vector;
import drl.math.tfs.DistanceFunction;
import drl.math.tfs.Normalizer;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.Policy;
import drl.mdp.api.QValue;
import drl.mdp.api.State;

/**
 * A static utility class containing methods to simplify some common operations
 * involving MDPs.
 * 
 * @author Dawit
 * 
 */
public class MdpUtils {

    private MdpUtils() {
    }

    /**
     * Returns a measure of the distance between two Q-Values defined over the
     * same MDP.
     * 
     * @return a non-negative number whose magnitude is the difference between
     *         qvf1 and qvf2.
     */
    public static <S extends State, A extends Action> double getDifference(MDP<S, A> mdp,
            QValue<S, A> qvf1, QValue<S, A> qvf2) {
        double diff = 0;
        for (A action : mdp.getActions()) {
            diff += qvf1.getValue(action).difference(qvf2.getValue(action));
        }
        return diff;
    }

    /**
     * 
     * @param mdp
     * @param domain
     * @return A u.a.r. sample from the given k-cell represented as a state of
     *         {@code mdp}. Note that there is no check to ensure that the
     *         returned state is valid or reachable.
     */
    public static <S extends State, A extends Action> S sampleState(MDP<S, A> mdp) {
        return mdp.stateFromVector(MathUtils.sampleUniformly(mdp.getStateSpace()));
    }

    /**
     * Produces a non-terminal state u.a.r. from the state space of {@code mdp}
     * by rejection sampling. Note that there is no check to ensure that the
     * returned state is valid or reachable.
     * 
     * @param mdp
     * @param domain
     * @param tries
     *            the number of samples to try before giving up and throwing a
     *            RuntimeException. Pass in -1 to try until a successful draw is
     *            made.
     * @return A non-terminal state of {@code mdp} drawn u.a.r. from the given
     *         domain.
     */
    public static <S extends State, A extends Action> S sampleNonTermainalState(MDP<S, A> mdp,
            int tries) {
        for (int i = 0; i != tries; i++) {
            S state = sampleState(mdp);
            if (!mdp.isTerminal(state)) {
                return state;
            }
        }
        throw new RuntimeException("Took too many tries to sample non-treminal state");
    }

    /**
     * A utility method for visualizing a rollout in an MDP.
     * 
     * @param mdp
     *            The MDP on which to perform the rollout.
     * @param frame
     *            The DisplayFrame where the visualization should appear.
     * @param policy
     *            The policy to run.
     * @param start
     *            The state from which to start the rollout. Set to {@code null}
     *            to use the MDP's start state.
     * @param steps
     *            The number of steps to simulate.
     * @param timeout
     *            The amount of time to pause between steps.
     */
    public static <S extends State, A extends Action> void visualize(MDP<S, A> mdp,
            DisplayFrame<S> frame, Policy<S, A> policy, S start, int steps, long timeout) {
        if (start == null) {
            start = mdp.getStartState();
        }
        for (int i = 0; i < steps; i++) {
            if (frame != null)
                frame.setState(start);
            if (mdp.isTerminal(start)) {
                System.out.println("Success! Steps taken: " + i);
                break;
            }
            A action = policy.getAction(start);
            System.out.println(i + "  " + mdp.vectorFromState(start) + "  " + action);
            start = mdp.simulate(start, action);
            sleep(timeout);
        }
    }

    /**
     * @param vecs
     * @param mdp
     * @return [mdp.stateFromVector(v) for v in vecs]
     */
    public static <S extends State> List<S> toStates(List<Vector> vecs, MDP<S, ?> mdp) {
        List<S> ret = new ArrayList<S>(vecs.size());
        for (Vector v : vecs) {
            ret.add(mdp.stateFromVector(v));
        }
        return ret;
    }

    /**
     * @param vecs
     * @param mdp
     * @return [mdp.vectorFromState(s) for s in states]
     */
    public static <S extends State> List<Vector> toVectors(List<S> states, MDP<S, ?> mdp) {
        List<Vector> ret = new ArrayList<Vector>(states.size());
        for (S state : states) {
            ret.add(mdp.vectorFromState(state));
        }
        return ret;
    }

    /**
     * A list of states reached when performing a random walk through the MDP,
     * starting from the MDP's start state.
     * 
     * @param mdp
     *            The MDP to be simulated.
     * @param frame
     *            A DisplayFrame for observing the terminal states reached. Set
     *            this to null for no visualization.
     * @param iters
     *            The number of steps to take in the random walk (restarting
     *            every time a terminal state is reached).
     * @param restart
     *            The number of steps to take without reaching a terminal state
     *            before restarting.
     * @return The list of states reached.
     */
    public static <S extends State, A extends Action> List<S> randomWalk(MDP<S, A> mdp,
            DisplayFrame<S> frame, int iters, int restart) {
        List<S> states = new ArrayList<S>(iters);
        S state = mdp.getStartState();
        states.add(state);
        int lastStart = 0;
        for (int i = 0; states.size() < iters; i++) {
            state = mdp.simulate(state, MathUtils.sample(mdp.getActions()));
            states.add(state);
            if (mdp.isTerminal(state)) {
                System.out.println("Reached end " + state + " in " + (i - lastStart));
                if (frame != null) {
                    frame.setState(state);
                }
                state = mdp.getStartState();
                lastStart = i;
            } else if (i - lastStart >= restart) {
                System.out.println("Gave up at " + state + " in " + (i - lastStart));
                if (frame != null) {
                    frame.setState(state);
                }
                state = mdp.getStartState();
                lastStart = i;
            }
        }
        return states;
    }

    /**
     * A utility method that wraps {@code Thread.sleep(millis)} in a try/catch
     * block.
     * 
     * @param millis
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a set of transitions where each action is held until the state
     * changes by some amount or some amount of time has passed.
     * 
     * @param mdp
     *            The MDP.
     * @param states
     *            The set of start states.
     * @param action
     *            The action to take
     * @param stepLimit
     *            The cap on the number of timesteps to hold the action.
     * @param eps
     *            The minimum desired change in state, measured with the
     * @param df
     *            The metric to use. Set this to {@code null} to use the
     *            normalized state space.
     * @return
     */
    public static <S extends State, A extends Action> List<Transition<S, A>> sustainedActionTransitions(
            MDP<S, A> mdp, List<S> states, A action, int stepLimit, double eps, DistanceFunction df) {
        df = df == null ? Normalizer.df(mdp.getStateSpace()) : df;
        List<Transition<S, A>> transitions = new ArrayList<Transition<S, A>>(states.size());
        double gamma = mdp.getDiscountFactor();
        for (S start : states) {
            S state = start;
            Vector startVec = mdp.vectorFromState(start);
            double reward = 0;
            for (int i = 0; i < stepLimit; i++) {
                S temp = mdp.simulate(state, action);
                reward += Math.pow(gamma, i) * mdp.getReward(state, action, temp);
                state = temp;
                Vector end = mdp.vectorFromState(temp);
                if (df.distance(startVec, end) >= eps) {
                    break;
                }
            }
            transitions.add(new Transition<S, A>(start,
                    action,
                    state,
                    startVec,
                    mdp.vectorFromState(state),
                    reward));
        }
        return transitions;
    }

    /**
     * Run an MDP using the given policy.
     * 
     * @param mdp
     *            The MDP to be simulated.
     * @param start
     *            The start state. Set this to {@code null}to use {@code
     *            mdp.getStartState()}
     * @param steps
     *            The number of steps to take. If a terminal state is reached
     *            before the step limit is reached, the simulation is stopped
     *            early.
     * @param policy
     *            The policy to use.
     * @return The list of Transitions that occur, in the order that they occur.
     */
    public static <S extends State, A extends Action> List<Transition<S, A>> rollout(MDP<S, A> mdp,
            S start, int steps, Policy<S, A> policy) {
        List<Transition<S, A>> trajectory = new ArrayList<Transition<S, A>>();
        policy = policy == null ? RandomPolicy.of(mdp) : policy;
        start = start == null ? mdp.getStartState() : start;

        for (int i = 0; i < steps; i++) {
            if (mdp.isTerminal(start)) {
                break;
            }
            A action = policy.getAction(start);
            S end = mdp.simulate(start, action);
            trajectory.add(Transition.of(start, action, end, mdp));
            start = end;
        }
        return trajectory;
    }

    /**
     * Calculates the value of a trajectory for the given discount rate.
     */
    public static <S extends State, A extends Action> double getValue(
            List<Transition<S, A>> trajectory, double gamma) {
        double value = 0.;
        double g = 1.;
        for (Transition<S, A> t : trajectory) {
            value += g * t.getReward();
            g *= gamma;
        }
        return value;
    }

}
