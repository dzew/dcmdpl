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
import java.util.concurrent.Executors;

import drl.math.MathUtils;
import drl.math.algs.GridFilter;
import drl.math.geom.Vector;
import drl.math.tfs.DistanceFunction;
import drl.math.tfs.EuclideanDF;
import drl.mdp.instance.pinball.PinBall2D;
import drl.mdp.instance.pinball.PinBallAction;
import drl.mdp.instance.pinball.PinBallMdp;
import drl.mdp.instance.pinball.PinBallParams;
import drl.mdp.instance.pinball.PinBallState;
import drl.mdp.utils.MdpUtils;
import drl.mdp.utils.Transition;
import drl.solver.ConcurrencyUtils;
import drl.solver.KbrlCaller;
import drl.solver.smoothing.ActionDistanceFn;
import drl.solver.smoothing.KbUtils;
import drl.solver.smoothing.KernelQValue;
import drl.solver.smoothing.SampleTransitions;

/**
 * Code to test KBSF on PinBall (table 5.1 in my thesis).
 * 
 * @author Dawit
 * 
 */
public class PinBallKbsf {

    private static final int samples = 30000;

    private static List<Vector> generateSamples(PinBallParams params) {
        PinBall2D mdp = new PinBall2D(params);
        GridFilter f2 = new GridFilter(mdp.getStateSpace(), 46);
        List<Vector> allData = MdpUtils.toVectors(MdpUtils.randomWalk(mdp, null, 2000000, 130000),
                mdp);
        f2.addAll(allData);
        List<Vector> pos = f2.subsample(samples);
        System.out.println("Points: " + pos.size());
        List<Vector> alls = new ArrayList<Vector>(pos.size());
        for (Vector v : pos) {
            alls.add(Vector.asVector(v.get(0), v.get(1), Math.random() - .5, Math.random() - .5));
        }
        return alls;
    }

    public static List<Vector> generateReps(PinBallParams params) {
        PinBall2D mdp = new PinBall2D(params);
        List<Vector> repVels = MathUtils.tilingSample(8, MathUtils.regularCell(2, -.5, .5));
        Collections.shuffle(repVels);
        GridFilter filter = new GridFilter(mdp.getStateSpace(), 90);
        filter.addAll(MdpUtils.toVectors(MdpUtils.randomWalk(mdp, null, 5000000, 1000000), mdp));
        System.out.println("Vectors to be created number " + filter.binsReached());
        List<Vector> repPos = filter.subsample(filter.binsReached());
        List<Vector> reps = new ArrayList<Vector>();
        for (int i = 0; i < repPos.size(); i++) {
            Vector v = repPos.get(i);
            Vector v2 = repVels.get(i % repVels.size());
            reps.add(Vector.asVector(v.get(0), v.get(1), v2.get(0), v2.get(1)));
        }
        return reps;
    }

    public static void main(String[] args) {
        PinBallParams params = PinBallParams.fromFile("data/pinball-easy.cfg");
        PinBallMdp mdp = new PinBallMdp(params);

        System.out.println("Generating states");
        List<PinBallState> states = MdpUtils.toStates(generateSamples(params), mdp);
        System.out.println("Generating representative states");
        List<PinBallState> reps = MdpUtils.toStates(generateReps(params), mdp);

        System.out.println("Num states and reps: " + states.size() + " " + reps.size());
        DistanceFunction mydf = EuclideanDF.instance;
        SampleTransitions<PinBallState, PinBallAction> samples = KbUtils.generateSustainedTransitions(mdp,
                states,
                mydf,
                30,
                .12);

        ExecutorService exec = Executors.newFixedThreadPool(6);

        KbrlCaller<PinBallState, PinBallAction> caller = KbrlCaller.of(mdp)
                .useTransitions(samples)
                .setRepresentativeStates(reps)
                .setDistanceFunction(mydf)
                // .makeMultithreaded(exec, 15)
                .setBandwidth(.03)
                .setSteps(800);

        KernelQValue<PinBallState, PinBallAction> fvfEuc = caller.solve();

        List<Transition<PinBallState, PinBallAction>> try1 = MdpUtils.rollout(mdp,
                null,
                1000,
                fvfEuc);
        System.out.println("Finished first try: " + try1.size() + " "
                + MdpUtils.getValue(try1, mdp.getDiscountFactor()));
        printActionSequence(try1);
        ActionDistanceFn<PinBallAction> adf = ActionDistanceFn.of(mdp.getActions(),
                EuclideanDF.instance);
        caller.setActionDistanceFn(ConcurrencyUtils.parallelMakeAdfn(mdp,
                fvfEuc,
                adf,
                samples,
                reps,
                exec,
                .5,
                false));

        fvfEuc = caller.solve();
        List<Transition<PinBallState, PinBallAction>> try2 = MdpUtils.rollout(mdp,
                null,
                1000,
                fvfEuc);
        System.out.println("Finished second try: " + try2.size() + " "
                + MdpUtils.getValue(try2, mdp.getDiscountFactor()));
        printActionSequence(try2);

        /*
         * caller.setActionDistanceFn(ExperimentUtils.parallelMakeAdfn(mdp,
         * fvfEuc, adf , samples, reps, exec, .2)); fvfEuc = caller.solve();
         * 
         * List<Transition<PinBallState, PinBallAction>> try3 =
         * MdpUtils.rollout(mdp, null, 500, fvfEuc);
         * System.out.println("Finished third try: " + try3.size() + " " +
         * MdpUtils.getValue(try3, mdp.getDiscountFactor()));
         */
        exec.shutdown();

        System.out.println("Try 1 " + try1.size()
                + MdpUtils.getValue(try1, mdp.getDiscountFactor()));
        printActionSequence(try1);
        System.out.println("Try 2 " + try2.size()
                + MdpUtils.getValue(try2, mdp.getDiscountFactor()));
        printActionSequence(try2);
        // System.out.println("Try 3 " + try3.size() + MdpUtils.getValue(try3,
        // mdp.getDiscountFactor()));
        // printActionSequence(try3);
    }

    private static void printActionSequence(List<Transition<PinBallState, PinBallAction>> traj) {
        for (Transition<?, ?> trans : traj) {
            System.out.print(trans.getAction());
        }
        System.out.println();
    }

}
