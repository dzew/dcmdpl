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

import java.util.HashSet;
import java.util.List;

import drl.math.MathUtils;
import drl.math.geom.Cell;
import drl.math.geom.Interval;
import drl.math.geom.Vector;

import org.junit.Test;

public class MathUtilsTest {

    @Test
    public void testIncrement1d() {
        int radix = 10;
        int[] counter = new int[1];
        MathUtils.increment(counter, radix);
        assertThat(counter[0], equalTo(1));
        for (int i = 0; i < 5; i++) {
            MathUtils.increment(counter, radix);
        }
        assertThat(counter[0], equalTo(6));
    }

    @Test
    public void testIncrementBase3() {
        int radix = 3;
        int[] counter = new int[3];
        int[] expected = new int[3];
        MathUtils.increment(counter, radix);
        expected[0] = 1;
        assertThat(counter, equalTo(expected));
        MathUtils.increment(counter, radix);
        expected[0] = 2;
        assertThat(counter, equalTo(expected));
        MathUtils.increment(counter, radix);
        expected[0] = 0;
        expected[1] = 1;
        assertThat(counter, equalTo(expected));
        for (int i = 0; i < 18; i++) {
            MathUtils.increment(counter, radix);
        }
        expected[2] = 2;
        assertThat(counter, equalTo(expected));
    }

    @Test(expected = IllegalStateException.class)
    public void testIncrementOverflow1d() {
        int[] counter = new int[1];
        for (int i = 0; i < 10; i++) {
            MathUtils.increment(counter, 10);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testIncrementOverflow3d() {
        int radix = 3;
        int[] counter = new int[3];
        for (int i = 0; i < 27; i++) {
            MathUtils.increment(counter, radix);
        }
    }

    @Test
    public void testSumCappedIncrement() {
        int dimensions = 6;
        int degree = 4;
        HashSet<Integer> hashes = new HashSet<Integer>();

        int[] counter = new int[dimensions];
        int expected = MathUtils.taylorExpansionTerms(dimensions, degree);

        hashes.add(0);
        int items = 1;
        boolean done;
        do {
            done = MathUtils.incrementWithSumCap(counter, degree);
            int hash = counter[0];
            for (int i = 1; i < dimensions; i++) {
                hash += MathUtils.raise(degree + 1, i) * counter[i];
            }
            hashes.add(hash);
            items += 1;
        } while (!done);
        assertThat(items, equalTo(expected));
        assertThat(hashes.size(), equalTo(expected));
    }

    @Test
    public void testExponent() {
        int[] powers = new int[] { 4, 2, 0, 0, 1, 0, 3, 1 };
        double[] terms = new double[] { 3, 0, 4, .6, .1, .9, 7., 4. };
        double expected = 1;
        for (int i = 0; i < terms.length; i++) {
            expected *= Math.pow(terms[i], (double) powers[i]);
        }
        assertTrue(Math.abs(MathUtils.raise(powers, terms) - expected) < .0001);
    }

    @Test
    public void testEvenSampling() {
        Interval[] intervals = new Interval[] { new Interval(15, 20), new Interval(-3, 4),
                new Interval(5, 2), };
        List<Vector> sampled = MathUtils.tilingSample(4, Cell.of(intervals));
        assertThat(sampled.size(), equalTo(MathUtils.raise(4, 3)));
    }
}
