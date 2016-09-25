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

import java.util.Collection;

import drl.math.MathUtils;
import drl.math.algs.GridFilter;
import drl.math.geom.Cell;
import drl.math.geom.Vector;
import drl.mdp.instance.mtncar.CarState;
import drl.mdp.instance.mtncar.MountainCarMdp;
import drl.mdp.instance.mtncar.MtnCarParams;

public class FilterFuncTest {

    /**
     * A test of the type of samples GridFilter produces. This test performs
     * several rollouts on MountainCar then filters the states reached for
     * coverage.
     * 
     * @param args
     */
    public static void main(String[] args) {
        MountainCarMdp mdp = new MountainCarMdp(MtnCarParams.defaultMtnCar());
        System.out.println(mdp.getStateSpace());
        GridFilter filter = new GridFilter(mdp.getStateSpace(), 14);
        System.out.println(filter.histogram());
        CarState state = mdp.getStartState();
        filter.add(mdp.vectorFromState(state));

        for (int i = 0; i < 150000; i++) {
            state = mdp.simulate(state, MathUtils.sample(mdp.getActions()));
            filter.add(mdp.vectorFromState(state));
            if (mdp.isTerminal(state)) {
                System.out.println("Terminated at " + i);
                state = mdp.getStartState();
            }
        }

        System.out.println(filter.histogram());
        System.out.println("Bins reached:  " + filter.binsReached());
        Collection<Vector> subsamp = filter.subsample(180);
        System.out.print(subsamp.size() + " sampled.");
        addCorners(subsamp, mdp.getStateSpace());
        System.out.println();

        System.out.print("The sampled states plus the corners of the state space:\n[");
        System.out.println("Plot this list of (x,y) points.");
        for (Vector v : subsamp) {
            System.out.print(String.format("(%.3f,%.3f), ", v.get(0), v.get(1)));
        }
        System.out.println("]");

        System.out.println("The bins from which the samples came:");
        for (Vector v : subsamp) {
            System.out.print(filter.getIndex(v) + " ");
        }
    }

    private static void addCorners(Collection<Vector> v, Cell domain) {
        double[] start = new double[domain.getDimensions()];
        double[] end = new double[domain.getDimensions()];
        for (int i = 0; i < start.length; i++) {
            start[i] = domain.getInterval(i).getStart();
            end[i] = domain.getInterval(i).getEnd();
        }
        v.add(Vector.asVector(start));
        v.add(Vector.asVector(end));
    }

}
