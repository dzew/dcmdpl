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

package drl.mdp.instance.di;

import drl.math.geom.Cell;
import drl.math.geom.Interval;

/**
 * A container for parameters of DoubleIntegratorMdp.
 * 
 * @author Dawit
 * 
 */
public class DiParametrization {
    final double inverseInertia;
    final double friction;
    final double div;
    final double variance;
    final double discountFactor;
    final double initialVelocity;
    final double initialPosition;
    final Cell domain;

    private DiParametrization(double inverseInertia, double friction, double stddiv,
            double discountFactor, double initialVel, double position, double maxVel, double maxPos) {
        this.inverseInertia = inverseInertia;
        this.friction = friction;
        this.div = stddiv;
        this.variance = stddiv * stddiv;
        this.discountFactor = discountFactor;
        this.initialVelocity = initialVel;
        this.initialPosition = position;
        this.domain = Cell.of(new Interval(-maxVel, maxVel * 2), new Interval(-maxPos, maxPos * 2));
    }

    public static DiParametrization defaultParametrization() {
        return (new Builder()).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        double invInert = 1.;
        double mu = .95;
        double noiseDiv = .4;
        double gamma = .9;
        double initVel = 0;
        double initPos = 30;
        double maxVel = 50;
        double maxPos = 50;

        public Builder setInverseInertia(double val) {
            this.invInert = val;
            return this;
        }

        public Builder setFrictionCoeff(double val) {
            this.mu = val;
            return this;
        }

        public Builder setActionNoise(double val) {
            this.noiseDiv = val;
            return this;
        }

        public Builder setDiscountValue(double val) {
            this.gamma = val;
            return this;
        }

        public Builder setStartVel(double val) {
            this.initVel = val;
            return this;
        }

        public Builder setStartPos(double val) {
            this.initPos = val;
            return this;
        }

        public Builder setMaxVel(double val) {
            this.maxVel = val;
            return this;
        }

        public Builder setMaxPos(double val) {
            this.maxPos = val;
            return this;
        }

        public DiParametrization build() {
            return new DiParametrization(invInert,
                    mu,
                    noiseDiv,
                    gamma,
                    initVel,
                    initPos,
                    maxVel,
                    maxPos);
        }
    }
}
