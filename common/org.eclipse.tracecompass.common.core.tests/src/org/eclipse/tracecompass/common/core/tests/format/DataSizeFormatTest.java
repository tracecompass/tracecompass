/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests.format;

import static org.junit.Assert.assertEquals;

import java.text.Format;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.format.DataSizeWithUnitFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the {@link DataSizeWithUnitFormat} class
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class DataSizeFormatTest {

    private static final @NonNull Format FORMAT = DataSizeWithUnitFormat.getInstance();

    private final @NonNull Number fNumValue;
    private final @NonNull String fExpected;

    /**
     * Constructor
     *
     * @param value
     *            The numeric value to format
     * @param expected
     *            The expected formatted result
     */
    public DataSizeFormatTest(@NonNull Number value, @NonNull String expected) {
        fNumValue = value;
        fExpected = expected;
    }

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { 0, "0" },
                { 3, "3 B" },
                { 975, "975 B" },
                { 1024, "1 KB" },
                { 1024 * 1024, "1 MB" },
                { 1024 * 1024 * 1024, "1 GB" },
                { 1024L * 1024L * 1024L * 1024L, "1 TB" },
                { 4096, "4 KB" },
                { -4096, "-4 KB" },
                { 4096L, "4 KB" },
                { 4096.0, "4 KB" },
                { 12345678, "11.774 MB" },
                { Integer.MAX_VALUE, "2 GB" },
                { Integer.MIN_VALUE, "-2 GB" },
                { Long.MAX_VALUE, "8388608 TB" },
                { 98765432.123456, "94.19 MB" },
                { -98765432.123456, "-94.19 MB" },
                { 555555555555L, "517.401 GB" },
                { 555555555555555L, "505.275 TB" }
        });
    }

    /**
     * Get the formatted to use for the unit test
     *
     * @return The formatter to use for the unit test
     */
    protected Format getFormatter() {
        return FORMAT;
    }

    /**
     * Test the {@link Format#format(Object)} method
     */
    @Test
    public void testFormat() {
        assertEquals("format value", fExpected, getFormatter().format(fNumValue));
    }
}
