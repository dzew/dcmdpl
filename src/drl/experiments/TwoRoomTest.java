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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import drl.data.print.PrintUtils;
import drl.data.vis.DisplayFrame;
import drl.math.geom.Vector;
import drl.math.tfs.DistanceFunction;
import drl.math.tfs.EuclideanDF;
import drl.math.tfs.MappingTF;
import drl.math.tfs.Transform;
import drl.mdp.api.QValue;
import drl.mdp.instance.pinball.PinBall2D;
import drl.mdp.instance.pinball.PinBallAction;
import drl.mdp.instance.pinball.PinBallParams;
import drl.mdp.instance.pinball.PinBallState;
import drl.mdp.utils.Generic2DVisualizer;
import drl.mdp.utils.MdpUtils;
import drl.solver.ConcurrencyUtils;
import drl.solver.KbrlCaller;
import drl.solver.StateSampler;
import drl.solver.smoothing.ActionDistanceFn;
import drl.solver.smoothing.KbUtils;
import drl.solver.smoothing.KernelQValue;
import drl.solver.smoothing.MultithreadedKbrl;
import drl.solver.smoothing.SampleTransitions;

/**
 * The code for the TWO-ROOM examples in the thesis and paper.
 * 
 * @author Dawit
 * 
 */
public class TwoRoomTest {

    /**
     * A transform that opens up the wall (as described in the thesis).
     */
    static final Transform twoRoom = new Transform() {

        @Override
        public Vector transform(Vector v) {
            if (v.get(1) < .5) {
                return v;
            }
            double x = v.get(0);
            double y = v.get(1);
            return Vector.asVector(y + .4, 1.5 - x);
        }

    };

    private static final Vector pivot = Vector.asVector(.8, .5);

    /**
     * The distance function that corresponds to shortest paths in TWO-ROOM
     */
    static final DistanceFunction trdf = new DistanceFunction() {

        @Override
        public void memoize(Vector v) {
        }

        @Override
        public double distance(Vector v1, Vector v2) {
            double x1 = v1.get(0), y1 = v1.get(1);
            double x2 = v2.get(0), y2 = v2.get(1);
            if ((y2 - .5) * (y1 - .5) > 0) {
                return EuclideanDF.instance.distance(v1, v2);
            }
            if (x1 + (x2 - x1) * (y1 - .5) / (y1 - y2) > .8) {
                return EuclideanDF.instance.distance(v1, v2);
            }
            return EuclideanDF.instance.distance(pivot, v2)
                    + EuclideanDF.instance.distance(v1, pivot);
        }
    };

    public static void testKbrl(PinBall2D mdp, List<PinBallState> states) {
        System.out.println(".01");
        KbrlCaller<PinBallState, PinBallAction> caller = KbrlCaller.of(mdp)
                .setStates(states)
                .setBandwidth(.01)
                .makeMultithreaded(15)
                .setSteps(200);
        Generic2DVisualizer<PinBallState> v = Generic2DVisualizer.of(mdp, true);
        DisplayFrame<PinBallState> frame = DisplayFrame.of(mdp, v);
        QValue<PinBallState, PinBallAction> qvf = caller.solve();
        MdpUtils.visualize(mdp, frame, qvf, null, 200, 60);
        PrintUtils.printValueFunction(mdp, qvf);

        caller.setBandwidth(.06);
        System.out.println(".06");
        qvf = caller.solve();
        PrintUtils.printValueFunction(mdp, qvf);
        MdpUtils.visualize(mdp, frame, qvf, null, 200, 60);

        caller.setDistanceFunction(trdf);// new TransformDF(twoRoom));
        System.out.println("d_{TR}");
        qvf = caller.solve();
        PrintUtils.printValueFunction(mdp, qvf);
        MdpUtils.visualize(mdp, frame, qvf, null, 200, 60);
    }

    public static void testDkbrl(PinBall2D mdp, List<PinBallState> states) {
        List<Vector> stateVecs = MdpUtils.toVectors(states, mdp);
        System.out.println(stateVecs);
        SampleTransitions<PinBallState, PinBallAction> trans = KbUtils.generateTransitions(mdp,
                states);

        KbrlCaller<PinBallState, PinBallAction> caller = KbrlCaller.of(mdp)
                .useTransitions(trans)
                .setBandwidth(.09)
                .makeMultithreaded(15)
                .setSteps(200);
        KernelQValue<PinBallState, PinBallAction> qvf = caller.solve();
        ActionDistanceFn<PinBallAction> adf = ActionDistanceFn.of(mdp.getActions(),
                EuclideanDF.instance);
        PrintUtils.printValueFunction(mdp, qvf);

        ExecutorService exec = Executors.newFixedThreadPool(16);
        Generic2DVisualizer<PinBallState> v = Generic2DVisualizer.of(mdp, true);
        DisplayFrame<PinBallState> frame = DisplayFrame.of(mdp, v);
        for (int i = 0; i < 3; i++) {
            MdpUtils.visualize(mdp, frame, qvf, null, 200, 40);
            System.out.println("Finished 2Room DKBRL iteration " + i);
            adf = ConcurrencyUtils.parallelMakeAdfn(mdp, qvf, adf, trans, states, exec, .5, false);
            MappingTF.deduceTransform(stateVecs, adf.get(PinBallAction.UP), 4);
            for (PinBallState s : states) {
                System.out.print(String.format("%.3f, ", qvf.getValue(s, PinBallAction.UP)));
            }
            qvf = MultithreadedKbrl.solve(qvf.withDistanceFunction(adf), mdp, trans, exec, 15, 200);
            PrintUtils.printValueFunction(mdp, qvf);
            System.out.println("\nsolving\n");
        }
        MdpUtils.visualize(mdp, frame, qvf, null, 200, 40);
        adf = ConcurrencyUtils.parallelMakeAdfn(mdp, qvf, adf, trans, states, exec, .5, false);
        MappingTF.deduceTransform(stateVecs, adf.get(PinBallAction.UP), 4);
        for (PinBallState s : states) {
            System.out.print(String.format("%.3f ", qvf.getValue(s, PinBallAction.UP)));
        }
        PrintUtils.printValueFunction(mdp, qvf);
        exec.shutdown();
    }

    public static void main(String[] args) {
        PinBallParams params = PinBallParams.fromFile("data/pinball-tworoom.cfg");
        PinBall2D mdp = new PinBall2D(params);
        List<PinBallState> states = StateSampler.reachabilitySample(mdp, 850);
        testKbrl(mdp, states);
    }

}