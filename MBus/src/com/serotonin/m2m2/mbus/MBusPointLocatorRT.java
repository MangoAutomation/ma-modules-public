/*
 *   Mango - Open Source M2M - http://mango.serotoninsoftware.com
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.m2m2.mbus;

import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;

public class MBusPointLocatorRT extends PointLocatorRT<MBusPointLocatorVO> {

    private int effectiveExponent;
    private double effectiveCorrectionFactor = 1;
    boolean needCheckDifAndVif = true;

    public MBusPointLocatorRT(MBusPointLocatorVO vo) {
    	super(vo);
    }

    @Override
    public boolean isSettable() {
        return false;
    }

    public double calcCorrectedValue(final double value, final int exponent, final double correctionConstant) {
        if (exponent != effectiveExponent) {
            effectiveExponent = exponent;
            effectiveCorrectionFactor = Math.pow(10, exponent);
        }
        if (effectiveExponent == 0) {
            if (Double.isNaN(correctionConstant)) {
                return value;
            } else {
                return value + correctionConstant;
            }
        } else if (Double.isNaN(correctionConstant)) {
            return value * effectiveCorrectionFactor;
        } else {
            return value * effectiveCorrectionFactor + correctionConstant;
        }
    }

    
}
