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

import org.junit.Test;

import drl.math.MathUtils;
import drl.math.geom.Cell;
import drl.math.geom.Interval;
import drl.math.geom.Vector;
import drl.math.tfs.DistanceFunction;
import drl.math.tfs.Transform;
import drl.math.tfs.TransformDF;
import drl.math.tfs.ValueSmoothingDF;
import drl.math.vfa.ValueFunction;

public class ValueSmoothingDfTest {

    @Test
    public void testDavr() {
        double alpha = .8;
        Cell domain = Cell.of(new Interval(-1, 3), new Interval(9, .6), new Interval(2, 2));
        ValueFunction func = new ValueFunction() {

            @Override
            public double difference(ValueFunction vf) {
                throw new UnsupportedOperationException();
            }

            @Override
            public double value(Vector v) {
                return Math.cos(v.get(0) * sq(v.get(1)) + 3 * Math.sqrt(v.get(2)));
            }
        };
        double imu = 2 / MathUtils.diameterOf(domain);

        DistanceFunction d0 = new TransformDF(new Transform() {

            @Override
            public Vector transform(Vector v) {
                double x = v.get(0), y = v.get(1), z = v.get(2);
                return Vector.asVector(Math.sqrt(x * x + y * y + z * z),
                        Math.atan2(y, x),
                        Math.atan2(x * x + y * y, z));
            }
        });
        DistanceFunction d1 = ValueSmoothingDF.of(d0, func, imu, alpha, true);

        for (int i = 0; i < 100; i++) {
            Vector v1 = MathUtils.sampleUniformly(domain);
            Vector v2 = MathUtils.sampleUniformly(domain);
            double error = d1.distance(v1, v2) - davr(d0, func, alpha, 1. / imu, v1, v2);
            System.out.println(error);
            assertTrue(Math.abs(error) < .0000001);
        }

    }

    private static double sq(double a) {
        return a * a;
    }

    private static double davr(DistanceFunction df, ValueFunction f, double a, double mu,
            Vector v1, Vector v2) {
        return Math.sqrt(sq(df.distance(v1, v2)) + sq(a * mu * (f.value(v1) - f.value(v2))))
                / Math.sqrt(1 + a * a);
    }
}
