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

package drl.tutorial;

import java.util.ArrayList;
import java.util.List;

import drl.data.print.PrintUtils;
import drl.data.vis.DisplayFrame;
import drl.data.vis.InteractiveFrame;
import drl.math.geom.Vector;
import drl.math.vfa.LinearFAs;
import drl.mdp.api.QValue;
import drl.mdp.utils.MdpUtils;
import drl.solver.KbrlCaller;
import drl.solver.MdpSolver;
import drl.solver.VfaBuilder;
import drl.solver.leastsquares.LvfApproximator;

public class OrbiterSolver {

    public static void main(String[] args) {
        testVisualizer();
    }

    public static void testVisualizer() {
        OrbiterMdp mdp = new OrbiterMdp();
        OrbiterVisualizer vis = new OrbiterVisualizer(mdp);
        InteractiveFrame.of(mdp, vis);
    }

    public static void testLstd() {
        OrbiterMdp mdp = new OrbiterMdp();
        OrbiterVisualizer vis = new OrbiterVisualizer(mdp);

        // Configure the linear value function approximator.
        LvfApproximator<OrbiterState, OrbiterAction> approximator = VfaBuilder.of(mdp)
                .useSupportStates(randomSample(mdp, 5000))
                .setBasisFunction(LinearFAs.polynomialBasisFactory(mdp.getStateSpace(), 4))
                .build();

        // Solve the MDP.
        QValue<OrbiterState, OrbiterAction> solution = null;
        solution = MdpSolver.solveByPolicyIteration(mdp, approximator, null, .0001, 6);

        // Roll out the solution
        MdpUtils.visualize(mdp, DisplayFrame.of(mdp, vis), solution, null, 300, 40);

        // Print a cross section of the value function.
        // As written, the line below varies xDot and yDot (dimension 2 and 3)
        // holding fixed x = .8 and y = .1
        PrintUtils.printValueCrossSection(mdp, solution, 2, 3, Vector.asVector(.8, .1, 0, 0));
    }

    public static void testKbrl() {
        OrbiterMdp mdp = new OrbiterMdp();
        OrbiterVisualizer vis = new OrbiterVisualizer(mdp);

        // Configure the settings
        // Currently this uses KBSF.
        // To use KBRL uncomment the line containing setRepresentativeStates
        KbrlCaller<OrbiterState, OrbiterAction> caller = KbrlCaller.of(mdp)
                .setStates(randomSample(mdp, 10000))
                .setRepresentativeStates(randomSample(mdp, 600))
                .makeMultithreaded(6)
                .setSteps(50)
                .setBandwidth(.09);

        // Solve the MDP.
        QValue<OrbiterState, OrbiterAction> solution = caller.solve();

        // Roll out the solution
        MdpUtils.visualize(mdp, DisplayFrame.of(mdp, vis), solution, null, 300, 40);

        // Print a cross section of the value function.
        PrintUtils.printValueCrossSection(mdp, solution, 2, 3, Vector.asVector(.8, .1, 0, 0));
    }

    /**
     * Utility method that samples the desired number of valid states u.a.r.
     */
    public static List<OrbiterState> randomSample(OrbiterMdp mdp, int numSamples) {
        List<OrbiterState> states = new ArrayList<OrbiterState>();
        while (states.size() < numSamples) {
            OrbiterState state = MdpUtils.sampleState(mdp);
            if (mdp.isValid(state)) {
                states.add(state);
            }
        }
        return states;
    }

    public static void testRadianTransform() {
        OrbiterMdp mdp = new OrbiterMdp();
        RadianTransform rtf = new RadianTransform(mdp);

        System.out.println("These three should be equal");
        System.out.println(Vector.asVector(1, 2, 3));
        System.out.println(rtf.transform(Vector.asVector(1, 0, 2, 3)));
        System.out.println(rtf.transform(Vector.asVector(0, 1, -3, 2)));

        System.out.println("\n and these four should be equal");
        System.out.println(Vector.asVector(1, 2, 0));
        System.out.println(rtf.transform(Vector.asVector(1, 0, 2, 0)));
        System.out.println(rtf.transform(Vector.asVector(0, 1, 0, 2)));
        System.out.println(rtf.transform(Vector.asVector(.707, .707, 1.414, 1.414)));
    }

}
