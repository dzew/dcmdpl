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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import drl.math.MathUtils;
import drl.math.geom.Cell;
import drl.math.geom.Interval;
import drl.math.geom.Vector;
import drl.math.vfa.CosineVfFactory;
import drl.math.vfa.LvfFactory;
import drl.math.vfa.PolynomialVfFactory;

public class LvfaFactoryTest {

    @Test
    public void testPolynomial1D() {
        Cell domain = Cell.of(new Interval(100, 20));
        LvfFactory factory = new PolynomialVfFactory(domain, 5);
        double[] vals = new double[] { 110, 103, 117, 95, 125 };
        for (double d : vals) {
            Vector vect = Vector.asVector(d);
            double prod = 1;
            double[] computed = factory.generateBases(vect);
            assertThat(computed.length, equalTo(6));
            for (int i = 0; i < 6; i++) {
                assertTrue(Math.abs(prod - computed[i]) < .0000001);
                prod *= d / 10 - 11;
            }
        }
    }

    @Test
    public void testFourier1D() {
        Cell domain = Cell.of(new Interval(100, 20));
        LvfFactory factory = new CosineVfFactory(domain, 5);
        double[] vals = new double[] { 110, 103, 117, 95, 125 };
        for (double d : vals) {
            Vector vect = Vector.asVector(d);
            double[] computed = factory.generateBases(vect);
            assertThat(computed.length, equalTo(5));
            for (int i = 0; i < 5; i++) {
                double prod = Math.cos(Math.PI * i * (d / 20 - 5));
                assertTrue(Math.abs(prod - computed[i]) < .0000001);
            }
        }
    }

    @Test
    public void testPolynomial3D() {
        Cell domain = Cell.of(new Interval(100, 20), new Interval(-61, 2), new Interval(-.5, 1));
        LvfFactory factory = new PolynomialVfFactory(domain, 4);
        double[] xvals = new double[] { 104, 113, 122, 106, 110 };
        double[] yvals = new double[] { -60.3, -59.2, -61.3, -60, -59.9 };
        double[] zvals = new double[] { .3, 0, -.9, .8, -.5 };

        for (double x : xvals) {
            for (double y : yvals) {
                for (double z : zvals) {
                    int[] pows = new int[] { 0, 0, 0 };
                    Vector vect = Vector.asVector(x, y, z);
                    double[] computed = factory.generateBases(vect);
                    for (int i = 1; !MathUtils.incrementWithSumCap(pows, 4); i++) {
                        double prod = Math.pow(x / 10 - 11, pows[0]) * Math.pow(y + 60, pows[1])
                                * Math.pow(z * 2, pows[2]);
                        assertTrue(Math.abs(prod - computed[i]) < .000001);
                    }
                    assertThat(pows, equalTo(new int[] { 0, 0, 4 }));
                    assertThat(computed[0], equalTo(1.0));
                    assertTrue(Math.abs(Math.pow(z * 2, 4) - computed[computed.length - 1]) < .000001);
                }
            }
        }
    }

    @Test
    public void testFourier3D() {
        Cell domain = Cell.of(new Interval(100, 20), new Interval(-61, 2), new Interval(-.5, 1));
        LvfFactory factory = new CosineVfFactory(domain, 4);
        double[] xvals = new double[] { 104, 113, 122, 106, 110 };
        double[] yvals = new double[] { -60.3, -59.2, -61.3, -60, -59.9 };
        double[] zvals = new double[] { .3, 0, -.9, .8, -.5 };

        int terms = MathUtils.fourierExpansionTerms(3, 4);
        for (double x : xvals) {
            for (double y : yvals) {
                for (double z : zvals) {
                    int[] pows = new int[] { 0, 0, 0 };
                    Vector vect = Vector.asVector(x, y, z);
                    double[] computed = factory.generateBases(vect);
                    assertThat(computed.length, equalTo(terms));
                    for (int i = 1; i < terms; i++) {
                        MathUtils.increment(pows, 4);
                        double prod = Math.cos(Math.PI
                                * ((x / 20 - 5) * pows[0] + (y + 61) / 2 * pows[1] + (z + .5)
                                        * pows[2]));
                        assertTrue(Math.abs(prod - computed[i]) < .000001);
                    }
                    assertThat(pows, equalTo(new int[] { 3, 3, 3 }));
                    assertThat(computed[0], equalTo(1.0));
                }
            }
        }
    }

}
