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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.format.DataSizeWithUnitFormat;
import org.eclipse.tracecompass.common.core.format.DecimalWithUnitPrefixFormat;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the {@link DataSizeWithUnitFormat} class
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class DecimalWithUnitPrefixFormatTest extends FormatTestBase {

    private static final @NonNull String UNITS = "rabbit";
    private static final @NonNull Format FORMAT = new DecimalWithUnitPrefixFormat(UNITS);

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
    public DecimalWithUnitPrefixFormatTest(@Nullable Number numValue, @NonNull String stringValue, @Nullable Number parseValue, int parseIndex) {
        super(numValue, stringValue, parseValue, parseIndex);
    }

    /**
     * Get parameters that the base class and implementing formatter classes
     * should support. The string value of those parameters can just be appended
     * the suffix of the implementing class.
     *
     * @return The common parameters for base and implementing formatter classes
     */
    protected static Collection<Object[]> getCommonParameters() {
        return Arrays.asList(new Object[][] {
                { 0, "0 rabbit", 0L, -1 },
                { 3, "3 rabbit", 3L, -1 },
                { 975, "975 rabbit", 975L, -1 },
                { 1000, "1 krabbit", 1000L, -1 },
                { 1000 * 1000, "1 Mrabbit", 1000 * 1000L, -1 },
                { 1000 * 1000 * 1000L, "1 Grabbit", 1000 * 1000 * 1000L, -1 },
                { 1000 * 1000 * 1000 * 1000L, "1 Trabbit", 1000 * 1000 * 1000 * 1000L, -1 },
                { 4000, "4 krabbit", 4000L, -1 },
                { -4000, "-4 krabbit", -4000L, -1 },
                { 4000L, "4 krabbit", 4000L, -1 },
                { 4000.0, "4 krabbit", 4000L, -1 },
                { 12345678, "12.346 Mrabbit", 1.2346E7, -1 },
                { Integer.MAX_VALUE, "2.147 Grabbit", 2.147E9, -1 },
                { Integer.MIN_VALUE, "-2.147 Grabbit", -2.147E9, -1 },
                { Long.MAX_VALUE, "9223372.037 Trabbit", 9.223372037E18, -1 },
                { 98765432.123456, "98.765 Mrabbit", 9.8765E7, -1 },
                { -98765432.123456, "-98.765 Mrabbit", -9.8765E7, -1 },
                { 555555555555L, "555.556 Grabbit", 5.55556E11, -1 },
                { 555555555555555L, "555.556 Trabbit", 5.55556E14, -1 },
                { 100100000, "100.1 Mrabbit", 1.001E8, -1 },
                { 0.1, "100 mrabbit", 0.1, -1 },
                { 0.001, "1 mrabbit", 0.001, -1 },
                { 0.000001, "1 µrabbit", 0.000001, -1 },
                { 0.000000001, "1 nrabbit", 0.000000001, -1 },
                { 0.000000000001, "1 prabbit", 0.000000000001, -1 },
                { -0.04, "-40 mrabbit", -0.04, -1 },
                { 0.002, "2 mrabbit", 0.002, -1 },
                { 0.0555, "55.5 mrabbit", 0.0555, -1 },
                { 0.0004928373928, "492.837 µrabbit", 0.000492837, -1 },
                { 0.000000251, "251 nrabbit", 0.000000251, -1 },
                { 0.000000000043, "43 prabbit", 0.000000000043, -1 },
                { 0.000000045643, "45.643 nrabbit", 0.000000045643, -1 },
        });
    }

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0} - {1}")
    public static Iterable<Object[]> getParameters() {
        List<Object[]> parameters = new ArrayList<>(getCommonParameters());
        List<Object[]> newParameters = Arrays.asList(new Object[][] {
                // Wrong units, just take the number as bytes
                { null, "1234 TPotato", 1234L, 4 },
                // Double number for entry
                { null, ".0001 Mrabbit", 100.0, -1 },
                // No space between number and units
                { null, "1krabbit", 1000L, -1 },
                // Trailing spaces at the end
                { null, "1krabbit   ", 1000L, 8 },
                // Using the binary kilo prefix
                { null, "1 Krabbit   ", 1000L, 9 },
                // Trailing text
                { null, "1krabbit  potato", 1000L, 8 },
        });
        parameters.addAll(newParameters);
        return parameters;
    }

    @Override
    protected Format getFormatter() {
        return FORMAT;
    }
}
