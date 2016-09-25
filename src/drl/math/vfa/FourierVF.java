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

package drl.math.vfa;

import drl.math.geom.Cell;
import drl.math.geom.Vector;

/**
 * A value function represented as a Fourier Series.
 * 
 * @author Dawit
 * 
 */
public class FourierVF implements ValueFunction {

    private final CosineVF cosVf;
    private final SineVF sinVf;

    private FourierVF(CosineVF cosVf, SineVF sinVf) {
        this.cosVf = cosVf;
        this.sinVf = sinVf;
    }

    /**
     * @param cosCoeffs
     *            The coefficients of the cosine terms.
     * @param sinCoeffs
     *            The coefficients of the sine terms
     * @param domain
     *            The domain of the approximation.
     * @param terms
     *            The number of harmonics.
     * @return
     */
    public static FourierVF of(Vector cosCoeffs, Vector sinCoeffs, Cell domain, int terms) {
        return new FourierVF(CosineVF.of(cosCoeffs, domain, terms), SineVF.of(sinCoeffs,
                domain,
                terms));
    }

    @Override
    public double value(Vector v) {
        return cosVf.value(v) + sinVf.value(v);
    }

    @Override
    public String toString() {
        return "FourierVF<" + cosVf + " + " + sinVf + ">";
    }

    @Override
    public double difference(ValueFunction vf) {
        if (!(vf instanceof FourierVF)) {
            return Double.POSITIVE_INFINITY;
        }
        FourierVF fvf = (FourierVF) vf;
        return cosVf.difference(fvf.cosVf) + sinVf.difference(fvf.sinVf);
    }

}
