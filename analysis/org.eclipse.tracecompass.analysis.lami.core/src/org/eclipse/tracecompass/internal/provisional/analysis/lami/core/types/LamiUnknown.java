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

/**
 * Lami 'unknown' value.
 *
 * The special unknown object represents an unknown value. It is typically used
 * in result table cells where a given computation cannot produce a result for
 * some reason.
 *
 * @author Alexandre Montplaisir
 */
class LamiUnknown extends LamiData {

    public static final LamiUnknown INSTANCE = new LamiUnknown();

    private LamiUnknown() {}

    @Override
    public @Nullable String toString() {
        return null;
    }
}
