/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.btf.core.trace;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Column names
 *
 * @author Matthew Khouzam
 */
public enum BtfColumnNames {

    /**
     * The event timestamp
     */
    TIMESTAMP("Timestamp"), //$NON-NLS-1$
    /**
     * The source
     */
    SOURCE("Source"), //$NON-NLS-1$
    /**
     * The source instance
     */
    SOURCE_INSTANCE("Source instance"), //$NON-NLS-1$
    /**
     * The event field name
     */
    EVENT_TYPE("Event type"), //$NON-NLS-1$
    /**
     * The target
     */
    TARGET("Target"), //$NON-NLS-1$
    /**
     * The target instance
     */
    TARGET_INSTANCE("Target instance"), //$NON-NLS-1$
    /**
     * The event
     */
    EVENT("Event"), //$NON-NLS-1$
    /**
     * Notes
     */
    NOTES("Notes"); //$NON-NLS-1$

    private final @NonNull String fField;

    private BtfColumnNames(@NonNull String field) {
        fField = field;
    }

    @Override
    public @NonNull String toString() {
        return fField;
    }
}
