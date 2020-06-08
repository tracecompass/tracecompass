/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.format;

import java.text.Format;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Provides a formatter for data sizes along with the unit of size (KB, MB, GB
 * ou TB). It receives a size in bytes and it formats a number in the closest
 * thousand's unit, with at most 3 decimals.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class DataSizeWithUnitFormat extends DecimalWithUnitPrefixFormat {

    private static final @NonNull Format INSTANCE = new DataSizeWithUnitFormat();

    private static final long serialVersionUID = 3934127385682676804L;
    private static final String BYTES = "B"; //$NON-NLS-1$
    private static final long KILO = 1024;
    private static final String SUFFIX = ""; //$NON-NLS-1$

    /**
     * Protected constructor
     */
    protected DataSizeWithUnitFormat() {
        this(SUFFIX);
    }

    /**
     * Constructor with suffix
     *
     * @param suffix
     *            The suffix to append to the units for this formatter
     * @since 4.1
     */
    protected DataSizeWithUnitFormat(String suffix) {
        super(BYTES + suffix, KILO);
    }

    /**
     * Returns the instance of this formatter
     *
     * @return The instance of this formatter
     */
    public static @NonNull Format getInstance() {
        return INSTANCE;
    }

}
