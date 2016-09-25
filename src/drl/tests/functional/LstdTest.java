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

package drl.tests.functional;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import drl.data.print.PrintUtils;
import drl.data.vis.DisplayFrame;
import drl.math.vfa.LinearFAs;
import drl.mdp.api.Policy;
import drl.mdp.api.QValue;
import drl.mdp.instance.mtncar.CarAction;
import drl.mdp.instance.mtncar.CarState;
import drl.mdp.instance.mtncar.MountainCarMdp;
import drl.mdp.instance.mtncar.MtnCarParams;
import drl.mdp.utils.Generic2DVisualizer;
import drl.mdp.utils.MdpUtils;
import drl.mdp.utils.StaticPolicy;
import drl.solver.MdpSolver;
import drl.solver.SamplingStrategy;
import drl.solver.VfaBuilder;
import drl.solver.VfaBuilder.Approximator;
import drl.solver.leastsquares.LvfApproximator;

public class LstdTest {

    /**
     * This method shows how one can solve an MDP using a linear value function
     * approximator.
     */
    public static void main(String[] args) {
        // Create an instance of the MDP to be solved.
        MountainCarMdp mdp = new MountainCarMdp(MtnCarParams.defaultMtnCar());

        // We need to provide an executor to use the multithreaded
        // implementation.
        // Don't forget to shut it down at the end!
        ExecutorService exec = Executors.newFixedThreadPool(5);
        int numCores = 4;

        // Configure Value Function Approximator for the MDP.
        LvfApproximator<CarState, CarAction> approximator = VfaBuilder.of(mdp)
                .setApproximator(Approximator.LSTDQ)
                .setBasisFunction(LinearFAs.polynomialBasisFactory(mdp.getStateSpace(), 4))
                .setNumberSamples(1600)
                .makeMultithreaded(exec, numCores)
                .setSamplingStrategy(SamplingStrategy.TILING)
                .build();

        // Generate an initial policy.
        Policy<CarState, CarAction> initialPolicy = StaticPolicy.of(CarAction.NOOP);

        // Do policy iteration.
        System.out.println("Starting Policy iteration.");
        QValue<CarState, CarAction> solution = MdpSolver.solveByPolicyIteration(mdp,
                approximator,
                initialPolicy,
                .00001,
                15);

        // Print the value function.
        // You can view the printout using surfaceView() in vfplotter.py
        PrintUtils.printValueFunction(mdp, solution);
        // Print how many steps each state is from a terminal state.
        PrintUtils.printStepsToGo(mdp, solution);

        // Simulate the policy.
        Generic2DVisualizer<CarState> vis = Generic2DVisualizer.of(mdp, true);
        DisplayFrame<CarState> frame = DisplayFrame.of(mdp, vis);
        MdpUtils.visualize(mdp, frame, solution, mdp.getStartState(), 300, 50);

        // Shut down the executor.
        exec.shutdown();
    }

}
