/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
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
 * Provides a formatter for data speeds in (XB/s). It receives a size in bytes
 * and it will return a string precise at the closest thousand's with at most 3
 * decimals.
 *
 * @author Geneviève Bastien
 * @since 2.0
 */
public class DataSpeedWithUnitFormat extends DataSizeWithUnitFormat {

    private static final @NonNull Format INSTANCE = new DataSpeedWithUnitFormat();
    private static final long serialVersionUID = -3603301320242441850L;
    private static final String PER_SECOND = "/s"; //$NON-NLS-1$

    /**
     * Protected constructor
     */
    protected DataSpeedWithUnitFormat() {
        super(PER_SECOND);
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
