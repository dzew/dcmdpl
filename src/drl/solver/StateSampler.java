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

package drl.solver;

import java.util.ArrayList;
import java.util.List;

import drl.math.MathUtils;
import drl.math.algs.GridFilter;
import drl.math.geom.Vector;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.State;
import drl.mdp.utils.MdpUtils;

/**
 * A static utility class providing methods for sampling states of an MDP.
 * 
 * @author Dawit
 * 
 */
public class StateSampler {

    private StateSampler() {
    }

    /**
     * @param mdp
     *            The MDP from which to sample states.
     * @param strategy
     *            The sampling strategy to use.
     * @param numSamples
     *            The number of samples to generate.
     */
    public static <S extends State, A extends Action> List<S> sample(MDP<S, A> mdp,
            SamplingStrategy strategy, int numSamples) {
        switch (strategy) {
        case TILING:
            return tilingSample(mdp, numSamples);
        case RANDOM:
            return randomSample(mdp, numSamples);
        case REACHABLE:
            return reachabilitySample(mdp, numSamples);
        default:
            throw new IllegalArgumentException("Unsupported sampling strategy: " + strategy);
        }
    }

    private static int tiles(int s, MDP<?, ?> mdp) {
        return 1 + (int) Math.pow(s - 1, 1. / mdp.getStateDimensions());
    }

    /**
     * Generate states that tile the bounding cell of the MDP's state space.
     * 
     * @param mdp
     *            The MDP to be sampled.
     * @param samples
     *            The number of states to sample.
     * @return A list of states whose length is {@code >= samples}. The length
     *         is chosen to be the smallest such number that allows for even
     *         tiling of the state space.
     */
    public static <S extends State, A extends Action> List<S> tilingSample(MDP<S, A> mdp,
            int samples) {
        int pointsPerDimension = tiles(samples, mdp);
        List<Vector> vectors = MathUtils.tilingSample(pointsPerDimension, mdp.getStateSpace());
        List<S> ret = new ArrayList<S>(vectors.size());
        for (Vector v : vectors) {
            ret.add(mdp.stateFromVector(v));
        }
        return ret;
    }

    /**
     * Sample states independently and u.a.r. from the bounding cell of the
     * MDP's state space.
     */
    public static <S extends State, A extends Action> List<S> randomSample(MDP<S, A> mdp,
            int samples) {
        List<S> ret = new ArrayList<S>(samples);
        for (int i = 0; i < samples; i++) {
            ret.add(mdp.stateFromVector(MathUtils.sampleUniformly(mdp.getStateSpace())));
        }
        return ret;
    }

    /**
     * Return a list of states that cover the reachable state space of the MDP.
     * 
     */
    public static <S extends State, A extends Action> List<S> reachabilitySample(MDP<S, A> mdp,
            int samples) {
        int cells = samples < 20000 ? tiles(samples, mdp) - 1 : tiles(20000, mdp);
        GridFilter filter = new GridFilter(mdp.getStateSpace(), cells);
        System.out.println("Performing random walk.");
        List<S> ret = MdpUtils.randomWalk(mdp, null, 1500000, 300000);
        for (S state : ret) {
            filter.add(mdp.vectorFromState(state));
        }
        if (filter.binsReached() * 4 < samples) {
            System.out.println("Re-filtering " + filter.binsReached());
            int factor = (int) (1 + Math.pow(1. * samples / filter.binsReached(),
                    1. / mdp.getStateDimensions()));
            cells = cells * factor;
            filter = new GridFilter(mdp.getStateSpace(), cells);
            for (S state : ret) {
                filter.add(mdp.vectorFromState(state));
            }
        }
        System.out.println(String.format("Fraction of state space reached: %d / %d",
                filter.binsReached(),
                MathUtils.raise(cells, mdp.getStateDimensions())));
        List<Vector> vecs = filter.subsample(samples);
        ret = new ArrayList<S>(vecs.size());
        for (Vector v : vecs) {
            ret.add(mdp.stateFromVector(v));
        }
        return ret;
    }
}
