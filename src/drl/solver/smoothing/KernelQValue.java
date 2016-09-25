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

package drl.solver.smoothing;

import java.util.Arrays;
import java.util.List;

import drl.math.geom.Vector;
import drl.math.tfs.DistanceFunction;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.State;
import drl.mdp.utils.Transition;

/**
 * Implementation of a Q-Value implemented by kernel regression.
 * 
 * @author Dawit
 * 
 */
public class KernelQValue<S extends State, A extends Action> extends AbstractKernelQValue<S, A> {

    private final SampleTransitions<S, A> samples;
    private final double bandwidth;
    private final ActionDistanceFn<A> adf;
    private final MDP<S, A> mdp;
    private final double[][] endStateValues;

    private KernelQValue(SampleTransitions<S, A> samples, MDP<S, A> mdp, ActionDistanceFn<A> df,
            double bandwidth, double[][] endStateValues) {
        super(mdp);
        this.samples = samples;
        this.adf = df;
        this.bandwidth = bandwidth;
        this.mdp = mdp;
        this.endStateValues = endStateValues;
    }

    /**
     * Static constructor.
     * 
     * @param mdp
     * @param samples
     * @param df
     * @param bandwidth
     * @return
     */
    public static <S extends State, A extends Action> KernelQValue<S, A> of(MDP<S, A> mdp,
            SampleTransitions<S, A> samples, ActionDistanceFn<A> df, double bandwidth) {
        double[][] values = new double[mdp.getActions().length][];
        for (int i = 0; i < values.length; i++) {
            values[i] = new double[samples.get(mdp.getActions()[i]).size()];
        }
        return new KernelQValue<S, A>(samples, mdp, df, bandwidth, values);
    }

    public double getMaxValue(Action a) {
        double max = Double.NEGATIVE_INFINITY;
        for (double d : endStateValues[a.ordinal()]) {
            max = Math.max(max, d);
        }
        return max;
    }

    public double getMinValue(Action a) {
        double min = Double.POSITIVE_INFINITY;
        for (double d : endStateValues[a.ordinal()]) {
            min = Math.min(min, d);
        }
        return min;
    }

    @Override
    protected double getValue(Vector x, A action) {
        double sum = 0;
        double value = 0;
        double gamma = mdp.getDiscountFactor();
        double minDist = Double.POSITIVE_INFINITY;
        double minDistVal = Double.NaN;

        List<Transition<S, A>> transitions = samples.get(action);
        DistanceFunction df = adf.get(action);
        int i = 0;
        double[] values = endStateValues[action.ordinal()];
        for (Transition<S, A> datum : transitions) {
            double dist = df.distance(x, datum.getStartVector());
            double val = datum.getReward() + gamma * values[i++];
            if (dist < minDist) {
                minDist = dist;
                minDistVal = val;
            }
            double k = KbUtils.gaussian(dist, bandwidth);
            sum += k;
            value += k * val;
        }
        if (Double.isNaN(value / sum)) {
            return minDistVal;
        }
        return value / sum;
    }

    protected KernelQValue<S, A> update(double[][] values) {
        double[][] newVals = new double[values.length][];
        for (int i = 0; i < newVals.length; i++) {
            newVals[i] = Arrays.copyOf(values[i], values[i].length);
        }
        return new KernelQValue<S, A>(samples, mdp, adf, bandwidth, newVals);
    }

    @Override
    public KernelQValue<S, A> withBandwidth(double b) {
        return new KernelQValue<S, A>(samples, mdp, adf, b, endStateValues);
    }

    public KernelQValue<S, A> withDistanceFunction(ActionDistanceFn<A> f) {
        return new KernelQValue<S, A>(samples, mdp, f, bandwidth, endStateValues);
    }

    protected double difference(KernelQValue<S, A> other) {
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        for (int a = 0; a < endStateValues.length; a++) {
            for (int i = 0; i < endStateValues[a].length; i++) {
                max = Math.max(max, endStateValues[a][i] - other.endStateValues[a][i]);
                min = Math.min(min, endStateValues[a][i] - other.endStateValues[a][i]);
            }
        }
        return max - min;
    }

}