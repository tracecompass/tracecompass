/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types;

import org.eclipse.jdt.annotation.Nullable;

class LamiRatio extends LamiDoubleNumber {

    public LamiRatio(double value) {
        super(value);
    }

    public LamiRatio(@Nullable Double low, @Nullable Double value, @Nullable Double high) {
        super(low, value, high);
    }

    @Override
    public @Nullable String toString() {
        // TODO: The string should probably include the low and
        //       high limits here.
        Number value = getValue();

        if (value != null) {
            return String.format("%.2f", value.doubleValue() * 100); //$NON-NLS-1$
        }

        return null;
    }
}
