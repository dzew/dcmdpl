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

package drl.mdp.instance.acrobot;

/**
 * Settings for the various parameters of acrobot. Construct instances of this
 * class using the builder.
 * 
 * @author Dawit
 * 
 */
public class AcrobotParams {

    public final double m1;
    public final double m2;
    public final double l1;
    public final double l2;
    public final double acrobotGoalPosition;

    private AcrobotParams(double m1, double m2, double l1, double l2, double gp) {
        this.m1 = m1;
        this.m2 = m2;
        this.l1 = l1;
        this.l2 = l2;
        this.acrobotGoalPosition = gp;
    }

    public static AcrobotParams defaultParams() {
        return new Builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * A utility class for configuring AcrobotParams. Unspecified parameter
     * values default to the values in the code from RL-Glue.
     * 
     * @author Dawit
     * 
     */
    public static class Builder {
        private double mass1 = 1;
        private double mass2 = 1;
        private double len1 = 1;
        private double len2 = 1;
        private double goal = 1;

        /**
         * Set the mass of the top link.
         */
        public Builder setMass1(double val) {
            this.mass1 = val;
            return this;
        }

        /**
         * Set the mass of the bottom link.
         */
        public Builder setMass2(double val) {
            this.mass2 = val;
            return this;
        }

        /**
         * Set the length of the top link.
         */
        public Builder setLen1(double val) {
            this.len1 = val;
            return this;
        }

        /**
         * Set the length of the bottom link.
         */
        public Builder setLen2(double val) {
            this.len2 = val;
            return this;
        }

        /**
         * Set the height of the goal location.
         */
        public Builder setGoalHeight(double val) {
            this.goal = val;
            return this;
        }

        /**
         * @return An instance of AcrobotParams with the specified settings.
         */
        public AcrobotParams build() {
            return new AcrobotParams(mass1, mass2, len1, len2, goal);
        }
    }

}
