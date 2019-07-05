/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Michael Jeanson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests.format;

import java.text.Format;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.format.DecimalUnitFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the {@link DecimalUnitFormat} class
 *
 * @author Michael Jeanson
 */
@RunWith(Parameterized.class)
public class DecimalUnitFormatTest extends FormatTestBase {

    private static final @NonNull Format FORMATTER = new DecimalUnitFormat();

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
    public DecimalUnitFormatTest(@Nullable Number numValue, @NonNull String stringValue, @Nullable Number parseValue, int parseIndex) {
        super(numValue, stringValue, parseValue, parseIndex);
    }

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0} - {1}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { 3, "3", 3L, -1 },
                { 5.6, "5.6", 5.6, -1 },
                { 1.234567, "1.2", 1.2, -1 },
                { 1.01, "1", 1L, -1 },
                { 975, "975", 975L, -1 },
                { 1000, "1 k", 1000L, -1 },
                { 4000, "4 k", 4000L, -1 },
                { -4000, "-4 k", -4000L, -1 },
                { 4000L, "4 k", 4000L, -1 },
                { 4000.0, "4 k", 4000L, -1 },
                { 12345678, "12.3 M", 12300000L, -1 },
                { Integer.MAX_VALUE, "2.1 G", 2100000000L, -1 },
                { Integer.MIN_VALUE, "-2.1 G", -2100000000L, -1 },
                { Long.MAX_VALUE, "9223.4 P", 9.2234E18, -1 },
                { 98765432.123456, "98.8 M", 98800000L, -1 },
                { -98765432.123456, "-98.8 M", -98800000L, -1 },
                { 555555555555L, "555.6 G", 555600000000L, -1 },
                { 555555555555555L, "555.6 T", 555600000000000L, -1 },
                { 100100000, "100.1 M", 100100000L, -1 },
                { 0.1, "100 m", 0.1, -1 },
                { 0.001, "1 m", 0.001, -1 },
                { 0.000001, "1 µ", 0.000001, -1 },
                { 0.000000001, "1 n", 0.000000001, -1 },
                { 0.000000000001, "1 p", 0.000000000001, -1 },
                { 0.0000000000001, "0", 0L, -1 },
                { -0.04, "-40 m", -0.04, -1 },
                { 0.002, "2 m", 0.002, -1 },
                { 0.0555, "55.5 m", 0.0555, -1 },
                { 0.0004928373928, "492.8 µ", 0.0004928, -1 },
                { 0.000000251, "251 n", 0.000000251, -1 },
                { 0.000000000043, "43 p", 0.000000000043, -1 },
                { 0.000000045643, "45.6 n", 0.0000000456, -1 },
                { Double.MAX_VALUE, "1.7976931348623157E308", 1.7976931348623157E308, -1 },
                { Double.POSITIVE_INFINITY, "∞", Double.POSITIVE_INFINITY, -1 },
                { Double.MIN_NORMAL, "0", 0L, -1 },
                { Double.NEGATIVE_INFINITY, "-∞", Double.NEGATIVE_INFINITY, -1 },
                { Double.NaN, "�", Double.NaN, -1 },
                // Illegal argument
                { null, "Toto", null, -1 },
                // A unit that is not one of the expected ones, should parse the value, but not the whole string
                { null, "1.2 s", 1.2, 3 },
                // a number with a prefix and a unit
                { null, "1.2 ms", 0.0012, 5 },
                // Special double numbers with prefix
                { null, "∞ k", Double.POSITIVE_INFINITY, -1 },
                { null, "-∞ p", Double.NEGATIVE_INFINITY, -1 },
                { null, "�M", Double.NaN, -1 },
                // Trailing spaces
                { null, "1.2 m  ", 0.0012, 5 },
        });
    }

    @Override
    protected Format getFormatter() {
        return FORMATTER;
    }

    /**
     * Test format with an illegal argument
     */
    @Override
    @Test(expected = IllegalArgumentException.class)
    public void testFormatIllegalArgument() {
        FORMATTER.format("Toto");
    }

}
