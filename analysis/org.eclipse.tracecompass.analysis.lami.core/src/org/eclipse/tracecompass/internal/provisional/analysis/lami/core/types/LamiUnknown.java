/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
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

/**
 * Lami 'unknown' value.
 *
 * The special unknown object represents an unknown value. It is typically used
 * in result table cells where a given computation cannot produce a result for
 * some reason.
 *
 * @author Alexandre Montplaisir
 */
final class LamiUnknown extends LamiData {

    public static final LamiUnknown INSTANCE = new LamiUnknown();

    private LamiUnknown() {}

    @Override
    public @Nullable String toString() {
        return null;
    }
}
