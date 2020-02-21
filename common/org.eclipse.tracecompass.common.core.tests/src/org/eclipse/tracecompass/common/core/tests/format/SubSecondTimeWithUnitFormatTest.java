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

package org.eclipse.tracecompass.common.core.tests.format;

import java.text.Format;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.format.DataSpeedWithUnitFormat;
import org.eclipse.tracecompass.common.core.format.SubSecondTimeWithUnitFormat;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the {@link DataSpeedWithUnitFormat} class
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class SubSecondTimeWithUnitFormatTest extends FormatTestBase {

    private static final @NonNull Format FORMAT = SubSecondTimeWithUnitFormat.getInstance();

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0} - {1}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { 0, "0", 0L, -1 },
                { 3, "3 ns", 3L, -1 },
                { 1975, "1.975 \u00B5s", 1975L, -1 },
                { 200000000, "200 ms", 200000000L, -1 },
                { -200000000, "-200 ms", -200000000L, -1 },
                { 200000000L, "200 ms", 200000000L, -1 },
                { 200000000.0, "200 ms", 200000000L, -1 },
                { 2100000000, "2.1 s", 2100000000L, -1 },
                { 314159264, "314.159 ms", 314159000L, -1 },
                { 200001000L, "200.001 ms", 200001000L, -1 },
                { 200000100L, "200 ms", 200000000L, -1 },
                // No space between number and units
                { null, "200ms", 200000000L, -1 },
                // Trailing spaces after units
                { null, "200ms  ", 200000000L, 5 },
                // Lots of spaces between number and units
                { null, "200     ms", 200000000L, -1 },
                { null, "1.5ms", 1500000L, -1 },
                { null, "1.5us", 1500L, -1 },
                { null, "1.5\u00B5s", 1500L, -1 },
                // Trailing data, it should still parse the string
                { null, "1.5usdt", 1500L, 5 },
                // Invalid units, but should parse the value
                { null, "1.5hello", 1.5, 3 },
                { null, "15", 15L, -1 },
                // Invalid units
                { null, "hello", null, -1 },
                // A lot of decimals
                { null, "0.000001s", 1000L, -1 },
                { null, ".000001s", 1000L, -1 },
        });
    }

    /**
     * Constructor
     *
     * @param numValue
     *            The numeric value. Use <code>null</code> to not test the
     *            number to string conversion for this test case.
     * @param stringValue
     *            The string value
     * @param parseValue
     *            The parse value of the string value, can be <code>null</code>
     *            if the string is supposed to either throw an exception of
     *            return a null value.
     * @param parseIndex
     *            The expected index of end of string. Putting <code>-1</code>
     *            means the whole string has been parsed. If the parseValue is
     *            null, this value will be ignored.
     */
    public SubSecondTimeWithUnitFormatTest(@Nullable Number numValue, @NonNull String stringValue, @Nullable Number parseValue, int parseIndex) {
        super(numValue, stringValue, parseValue, parseIndex);
    }

    @Override
    protected Format getFormatter() {
        return FORMAT;
    }

}
