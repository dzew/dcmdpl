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

package drl.tests.unit;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import drl.math.geom.Vector;
import drl.math.tfs.DistanceFunction;
import drl.math.tfs.Normalizer;
import drl.mdp.instance.mtncar.CarAction;
import drl.mdp.instance.mtncar.CarState;
import drl.mdp.instance.mtncar.MountainCarMdp;
import drl.mdp.utils.Transition;
import drl.solver.StateSampler;
import drl.solver.smoothing.KbUtils;
import drl.solver.smoothing.SampleTransitions;

public class SustainedActionsTest {

    @Test
    public void test() {
        double eps = .1;
        int reps = 50;

        MountainCarMdp mdp = MountainCarMdp.defaultParams();
        List<CarState> states = StateSampler.tilingSample(mdp, 400);
        DistanceFunction df = Normalizer.df(mdp.getStateSpace());

        SampleTransitions<CarState, CarAction> samples = KbUtils.generateSustainedTransitions(mdp,
                states,
                null,
                reps,
                eps);

        double minReward = Math.pow(mdp.getDiscountFactor(), reps) - 1;
        minReward /= 1 - mdp.getDiscountFactor();

        for (CarAction a : CarAction.values()) {
            Map<Double, Integer> histogram = new HashMap<Double, Integer>();
            System.out.println("Processing action: " + a);
            for (Transition<CarState, CarAction> transition : samples.get(a)) {
                if (!histogram.containsKey(transition.getReward()))
                    histogram.put(transition.getReward(), 0);
                histogram.put(transition.getReward(), 1 + histogram.get(transition.getReward()));
                Vector v1 = transition.getStartVector();
                Vector v2 = transition.getEndVector();
                assertTrue(mdp.isTerminal(transition.getEndState()) || df.distance(v1, v2) >= eps
                        || Math.abs(transition.getReward() - minReward) < .0001);
                System.out.print(String.format("((%.3f, %.3f),(%.3f, %.3f)),",
                        v1.get(0),
                        v2.get(0),
                        v1.get(1),
                        v2.get(1)));
            }
            System.out.println();
            System.out.println(histogram);
        }
    }

}
