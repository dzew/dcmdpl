package drl.experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import drl.data.print.Serializer;
import drl.math.tfs.Normalizer;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.State;
import drl.mdp.instance.acrobot.AcrobotAction;
import drl.mdp.instance.acrobot.AcrobotMdp;
import drl.mdp.instance.acrobot.AcrobotState;
import drl.mdp.utils.MdpUtils;
import drl.mdp.utils.Transition;
import drl.solver.ConcurrencyUtils;
import drl.solver.StateSampler;
import drl.solver.smoothing.ActionDistanceFn;
import drl.solver.smoothing.KbUtils;
import drl.solver.smoothing.KernelQValue;
import drl.solver.smoothing.MultithreadedKbrl;
import drl.solver.smoothing.SampleTransitions;

public class AcrobotKbrl {

    private static final double bandwidth = .07;
    private static final int rounds = 3;
    private static final double alpha = .5;

    private static <S extends State, A extends Action> ComputationResult computeResultsKbrl(
            MDP<S, A> mdp, SampleTransitions<S, A> samples, List<S> tests, ExecutorService exec,
            double bandwidth) {
        int iters = 200;
        int subsequentIters = 50;

        ComputationResult result = new ComputationResult();
        ActionDistanceFn<A> adf = ActionDistanceFn.of(mdp.getActions(),
                Normalizer.df(mdp.getStateSpace()));
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

    public static void main(String[] args) {
        System.out.println("Running AcrobotKbrl!");
        AcrobotMdp mdp = AcrobotMdp.defaultAcrobot();

        List<AcrobotState> rps = StateSampler.reachabilitySample(mdp, 1000);
        List<AcrobotState> tts = StateSampler.reachabilitySample(mdp, 260);
        List<AcrobotState> t2 = new ArrayList<AcrobotState>();
        for (AcrobotState state : tts) {
            if (!mdp.isTerminal(state)) {
                t2.add(state);
            }
        }
        System.out.println("Number of non-terminal states: " + t2.size());
        Serializer.writeToFile("data/acrobotReps", MdpUtils.toVectors(rps, mdp));
        Serializer.writeToFile("data/acrobotTests", MdpUtils.toVectors(t2, mdp));
        System.exit(0);

        List<AcrobotState> states = MdpUtils.toStates(Serializer.vectorsFromFile("data/acrobotReps"),
                mdp);
        List<AcrobotState> tests = MdpUtils.toStates(Serializer.vectorsFromFile("data/acrobotTests"),
                mdp);
        System.out.println("Starting experiment with " + tests.size() + " and " + states.size());
        SampleTransitions<AcrobotState, AcrobotAction> samples = KbUtils.generateTransitions(mdp,
                states);
        ExecutorService exec = Executors.newFixedThreadPool(16);
        System.out.println(computeResultsKbrl(mdp, samples, tests, exec, bandwidth));
        exec.shutdown();
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