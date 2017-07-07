/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core.interval.json;

/**
 * GSON State Interval Strings
 *
 * @author Matthew Khouzam
 */
final class TmfIntervalStrings {
    public static final String START = "start"; //$NON-NLS-1$
    public static final String END = "end"; //$NON-NLS-1$
    public static final String QUARK = "quark"; //$NON-NLS-1$
    public static final String TYPE = "type"; //$NON-NLS-1$
    public static final String NULL = String.valueOf((Object)null);
    public static final String VALUE = "value"; //$NON-NLS-1$

    /**
     * Constructor
     */
    private TmfIntervalStrings() {
        // Do nothing
    }
}
