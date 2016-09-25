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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import drl.data.vis.DisplayFrame;
import drl.math.tfs.EuclideanDF;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.State;
import drl.mdp.instance.mtncar.MountainCarMdp;
import drl.mdp.instance.mtncar.MtnCarParams;
import drl.mdp.utils.MdpUtils;
import drl.mdp.utils.Transition;
import drl.solver.ConcurrencyUtils;
import drl.solver.StateSampler;
import drl.solver.smoothing.ActionDistanceFn;
import drl.solver.smoothing.KbUtils;
import drl.solver.smoothing.KernelQValue;
import drl.solver.smoothing.MultithreadedKbrl;
import drl.solver.smoothing.SampleTransitions;

/**
 * Code to calculate sample size dependence for DKBRL (Figures 5-5, 5-6, 5-7,
 * and 5-11 in my thesis). To duplicate the results, run this 20 times and plot
 * averages.
 * 
 * The code is currently configured to run on Mountain-Car. To change it to
 * Acrobot or some other MDP, make the change in the main method. You may also
 * want to adjust the parameters to suit Acrobot.
 * 
 * @author Dawit
 * 
 */
public class SampleSizeSensitivity {

    // The bandwidth.
    private static final double bandwidth = .03;
    // The sample sizes to test
    private static final int[] numSamples = new int[] { 200, 250, 300, 350, 400, 450, 500, 550,
            600, 650, 700, 750, 800 };
    // Iterations of representation adjustment to perform.
    private static final int rounds = 4;
    // The relaxation rate.
    private static final double alpha = 1.;

    private static <S extends State, A extends Action> ComputationResult computeResultsKbrl(
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
            System.out.println();
            // MappingTF.deduceTransform(MdpUtils.toVectors(tests, mdp),
            // adf.get(mdp.getActions()[1]), 4);
            System.out.println("Results: " + result);
            iters = subsequentIters;
        }
        // System.out.println(MdpUtils.toVectors(tests, mdp));
        // for (S state : tests) {
        // System.out.print(String.format("%.3f, ", qvf.getValue(state,
        // mdp.getActions()[1])));
        // }
        return result;
    }

    private static <S extends State, A extends Action> void collectData(DisplayFrame<S> frame,
            MDP<S, A> mdp) {
        List<ComputationResult> results = new ArrayList<ComputationResult>();
        // List<S> tests = new ArrayList<S>();
        // for (int i = 0; i < 50; i++) {
        // tests.add(MdpUtils.sampleNonTermainalState(mdp, 1000));
        // }
        List<S> tests = StateSampler.tilingSample(mdp, 23 * 23);
        // System.out.println(MdpUtils.toVectors(tests, mdp));

        // SampleTransitions<S, A> samples = KbUtils.generateTransitions(mdp,
        // states, EuclideanDF.instance);
        ExecutorService exec = Executors.newFixedThreadPool(14);
        for (int num : numSamples) {
            List<S> states = StateSampler.reachabilitySample(mdp, num);
            SampleTransitions<S, A> samples = KbUtils.generateSustainedTransitions(mdp,
                    states,
                    EuclideanDF.instance,
                    20,
                    .1);
            results.add(computeResultsKbrl(frame, mdp, samples, tests, exec, bandwidth));
            System.out.println("\n\nProcessed bandwidth " + num);
            for (int i = 0; i < results.size(); i++) {
                System.out.println(numSamples[i] + " " + results.get(i));
            }
        }
        System.out.println("End computation. KBRL");
        exec.shutdown();
    }

    public static void main(String[] args) {
        MountainCarMdp mdp = new MountainCarMdp(MtnCarParams.defaultMtnCar());
        // AcrobotMDP mdp = AcrobotMDP.defaultAcrobot();
        collectData(null, mdp);
    }

    private static class ComputationResult {
        private List<Integer> steps = new ArrayList<Integer>();

        @Override
        public String toString() {
            return steps + "";// + averageCosts;
        }
    }

}
