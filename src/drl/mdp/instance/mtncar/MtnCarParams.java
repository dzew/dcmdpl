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

package drl.mdp.instance.mtncar;

/**
 * A container for parameters of the Mountain-Car MDP.
 * 
 * @author Dawit
 * 
 */
public class MtnCarParams {

    final double minPos;
    final double maxPos;
    final double maxVel;
    final double minVel;
    final double goalPos;

    public MtnCarParams(double minPos, double maxPos, double minVel, double maxVel, double goalPos) {
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.minVel = minVel;
        this.maxVel = maxVel;
        this.goalPos = goalPos;
    }

    public static MtnCarParams defaultMtnCar() {
        return new Builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        double minPos = -1.2;
        double maxPos = .6;
        double minVel = -.07;
        double maxVel = .07;
        double goalPos = .5;

        public Builder setMinPos(double minPos) {
            this.minPos = minPos;
            return this;
        }

        public Builder setMaxPos(double maxPos) {
            this.maxPos = maxPos;
            return this;
        }

        public Builder setMinVel(double minVel) {
            this.minVel = minVel;
            return this;
        }

        public Builder setMaxVel(double maxVel) {
            this.maxVel = maxVel;
            return this;
        }

        public Builder setGoalPos(double goalPos) {
            this.goalPos = goalPos;
            return this;
        }

        public MtnCarParams build() {
            return new MtnCarParams(minPos, maxPos, minVel, maxVel, goalPos);
        }
    }

}
