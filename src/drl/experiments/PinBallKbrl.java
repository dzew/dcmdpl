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

package drl.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import drl.data.vis.DisplayFrame;
import drl.math.algs.GridFilter;
import drl.math.geom.Vector;
import drl.math.tfs.EuclideanDF;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.State;
import drl.mdp.instance.pinball.PinBall2D;
import drl.mdp.instance.pinball.PinBallMdp;
import drl.mdp.instance.pinball.PinBallParams;
import drl.mdp.instance.pinball.PinBallState;
import drl.mdp.utils.MdpUtils;
import drl.mdp.utils.Transition;
import drl.solver.ConcurrencyUtils;
import drl.solver.smoothing.ActionDistanceFn;
import drl.solver.smoothing.KernelQValue;
import drl.solver.smoothing.MultithreadedKbrl;
import drl.solver.smoothing.SampleTransitions;

/**
 * Code to test DKBRL on PinBall (Table 5.2 in my thesis and the figures in
 * Dawit and Konidaris 2014).
 * 
 * @author Dawit
 * 
 */
public class PinBallKbrl {

    // private static final double bandwidth = .07;
    // private static final int numSamples = 20000;
    private static final int rounds = 3;
    private static final double alpha = .5;
    private static final String world = "data/pinball-easy.cfg";

    public static <S extends State, A extends Action> ComputationResult computeResultsKbrl(
            DisplayFrame<S> frame, MDP<S, A> mdp, SampleTransitions<S, A> samples, List<S> tests,
            ExecutorService exec, double bandwidth) {
        int iters = 200;
        int subsequentIters = 50;

        ComputationResult result = new ComputationResult();
        ActionDistanceFn<A> adf = ActionDistanceFn.of(mdp.getActions(), EuclideanDF.instance);
        KernelQValue<S, A> qvf = KernelQValue.of(mdp, samples, adf, bandwidth);
        adf = null;
        for (int i = 0; i < rounds; i++) {
            adf = ConcurrencyUtils.parallelMakeAdfn(mdp,
                    qvf,
                    adf,
                    samples,
                    new ArrayList<S>(),
                    exec,
                    alpha,
                    false);
            qvf = MultithreadedKbrl.solve(qvf.withDistanceFunction(adf),
                    mdp,
                    samples,
                    exec,
                    14,
                    iters);
            // PrintUtils.printValueFunction(mdp, qvf);
            // PrintUtils.printPolicy(mdp, QValuePolicy.of(qvf));
            List<Transition<S, A>> traj = MdpUtils.rollout(mdp, null, 500, qvf);
            result.steps.add(traj.size());
            result.averageCosts.add(MdpUtils.getValue(traj, mdp.getDiscountFactor()));
            System.out.println("Start test: " + traj.size() + "  "
                    + result.averageCosts.get(result.averageCosts.size() - 1));
            for (Transition<S, A> t : traj) {
                // frame.setState(t.getStartState());
                System.out.print(t.getAction());
                // MdpUtils.sleep(70);
            }
            System.out.println();
            System.out.println();
            int num = 0;
            for (S state : tests) {
                traj = MdpUtils.rollout(mdp, state, 500, qvf);
                System.out.println("XTest: " + (num++) + " steps " + traj.size() + " rewards "
                        + MdpUtils.getValue(traj, mdp.getDiscountFactor()) + " start "
                        + mdp.vectorFromState(state).untruncated());
                for (Transition<S, A> t : traj) {
                    // frame.setState(t.getStartState());
                    System.out.print(t.getAction());
                    // MdpUtils.sleep(70);
                }
                System.out.println();
            }
            System.out.println();

            System.out.println();
            // MappingTF.deduceTransform(MdpUtils.toVectors(tests, mdp),
            // adf.get(mdp.getActions()[1]), 4);
            System.out.println("Results: " + result);
            iters = subsequentIters;
        }
        // System.out.println(MdpUtils.toVectors(tests, mdp));
        // for(S state : tests) {
        // System.out.print(String.format("%.3f, ", qvf.getValue(state,
        // mdp.getActions()[1])));
        // }
        // System.out.println();
        return result;
    }

    public static List<Vector> generateReps(PinBallParams params) {
        PinBall2D mdp = new PinBall2D(params);
        // List<Vector> repVels = MathUtils.tilingSample(8,
        // MathUtils.regularCell(2, -.5, .5));
        // Collections.shuffle(repVels);
        GridFilter filter = new GridFilter(mdp.getStateSpace(), 90);
        filter.addAll(MdpUtils.toVectors(MdpUtils.randomWalk(mdp, null, 500000, 100000), mdp));
        System.out.println("Vectors to be created number " + filter.binsReached());
        List<Vector> repPos = filter.subsample(filter.binsReached());
        List<Vector> reps = new ArrayList<Vector>();
        for (int i = 0; i < repPos.size(); i++) {
            Vector v = repPos.get(i);
            // Vector v2 = repVels.get(i % repVels.size());
            reps.add(Vector.asVector(v.get(0), v.get(1), Math.random() - .5, Math.random() - .5));
        }
        return reps;
    }

    public static List<Vector> generateTests(PinBallParams params) {
        PinBall2D mdp = new PinBall2D(params);
        GridFilter filter = new GridFilter(mdp.getStateSpace(), 28);
        filter.addAll(MdpUtils.toVectors(MdpUtils.randomWalk(mdp, null, 1000000, 1000000), mdp));
        System.out.println("Test vectors to be created number " + filter.binsReached());
        List<Vector> repPos = filter.subsample(filter.binsReached());
        List<Vector> reps = new ArrayList<Vector>();
        for (int i = 0; i < repPos.size(); i++) {
            Vector v = repPos.get(i);
            reps.add(Vector.asVector(v.get(0), v.get(1), Math.random() - .5, Math.random() - .5));
        }
        Collections.shuffle(reps);
        return reps;
    }

    public static void main(String[] args) {
        System.err.println("Running PB2 ");
        PinBallParams params = PinBallParams.fromFile(world);
        PinBallMdp mdp = new PinBallMdp(params);
        List<PinBallState> states = MdpUtils.toStates(generateReps(params), mdp);
        // Collections.shuffle(states);
        // List<PinBallState> tests = new ArrayList<PinBallState>(500);
        /*
         * for (PinBallState state : MdpUtils.toStates(generateTests(params),
         * mdp)) { if (mdp.isTerminal(state)) { continue; } tests.add(state); if
         * (tests.size() == 500) { break; } }
         */
        System.out.println("Starting experiment with " + 0 + " and " + states.size());
        PinBallMdp.wallHits = 0;
        // SampleTransitions<PinBallState, PinBallAction> samples =
        // KbUtils.generateTransitions(mdp,
        // states);
        System.out.println("Times wall was hit: " + PinBallMdp.wallHits);
        // DisplayFrame<PinBallState, PinBallAction> frame =
        // DisplayFrame.of(mdp, PinBallVisualizer.of(params));
        // ExecutorService exec = Executors.newFixedThreadPool(16);
        // System.out.println(computeResultsKbrl(null, mdp, samples, tests,
        // exec, bandwidth));
        // exec.shutdown();
    }

    private static class ComputationResult {
        private List<Integer> steps = new ArrayList<Integer>();
        private List<Double> averageCosts = new ArrayList<Double>();

        @Override
        public String toString() {
            return steps + " " + averageCosts;
        }
    }

}
