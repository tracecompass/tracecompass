/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types;

import org.eclipse.jdt.annotation.Nullable;

class LamiNumber extends LamiData {

    private final double fValue;

    public LamiNumber(double value) {
        fValue = value;
    }

    public double getValue() {
        return fValue;
    }

    @Override
    public @Nullable String toString() {
        return Double.toString(fValue);
    }
}
