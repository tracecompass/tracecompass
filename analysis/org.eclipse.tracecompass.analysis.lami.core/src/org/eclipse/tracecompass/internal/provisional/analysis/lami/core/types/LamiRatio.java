/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
