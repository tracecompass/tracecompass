/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests.format;

import java.text.Format;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.format.DataSpeedWithUnitFormat;

/**
 * Test the {@link DataSpeedWithUnitFormat} class
 *
 * @author Geneviève Bastien
 */
public class DataSpeedFormatTest extends DataSizeFormatTest {

    private static final @NonNull Format FORMAT = new DataSpeedWithUnitFormat();
    private static final String PER_SECOND = "/s";

    /**
     * Constructor
     *
     * @param value
     *            The numeric value to format
     * @param expected
     *            The expected formatted result
     */
    public DataSpeedFormatTest(@NonNull Number value, @NonNull String expected) {
        super(value, expected + PER_SECOND);
    }

    @Override
    protected Format getFormatter() {
        return FORMAT;
    }
}
