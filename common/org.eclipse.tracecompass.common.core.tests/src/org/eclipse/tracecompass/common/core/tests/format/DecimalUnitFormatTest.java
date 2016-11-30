/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Michael Jeanson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests.format;

import static org.junit.Assert.assertEquals;

import java.text.Format;
import java.text.ParseException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
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
public class DecimalUnitFormatTest {

    private static final @NonNull Format FORMATTER = new DecimalUnitFormat();

    private final @NonNull Number fNumValue;
    private final @NonNull String fStringValue;
    private final @NonNull Number fParseValue;

    /**
     * Constructor
     *
     * @param numValue
     *            The numeric value
     * @param stringValue
     *            The string value
     * @param parseValue
     *            The parse value of the string value
     */
    public DecimalUnitFormatTest(@NonNull Number numValue, @NonNull String stringValue, @NonNull Number parseValue) {
        fNumValue = numValue;
        fStringValue = stringValue;
        fParseValue = parseValue;
    }

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { 3, "3", 3L },
                { 5.6, "5.6", 5.6 },
                { 1.234567, "1.2", 1.2 },
                { 1.01, "1", 1L },
                { 975, "975", 975L },
                { 1000, "1 k", 1000L },
                { 4000, "4 k", 4000L },
                { -4000, "-4 k", -4000L },
                { 4000L, "4 k", 4000L },
                { 4000.0, "4 k", 4000L },
                { 12345678, "12.3 M", 12300000L },
                { Integer.MAX_VALUE, "2.1 G", 2100000000L },
                { Integer.MIN_VALUE, "-2.1 G", -2100000000L },
                { Long.MAX_VALUE, "9223.4 P", 9.2234E18 },
                { 98765432.123456, "98.8 M", 98800000L },
                { -98765432.123456, "-98.8 M", -98800000L },
                { 555555555555L, "555.6 G", 555600000000L },
                { 555555555555555L, "555.6 T", 555600000000000L },
                { 100100000, "100.1 M", 100100000L },
                { 0.1, "100 m", 0.1 },
                { 0.001, "1 m", 0.001 },
                { 0.000001, "1 µ", 0.000001 },
                { 0.000000001, "1 n", 0.000000001 },
                { 0.000000000001, "1 p", 0.000000000001 },
                { 0.0000000000001, "0", 0L },
                { -0.04, "-40 m", -0.04 },
                { 0.002, "2 m", 0.002 },
                { 0.0555, "55.5 m", 0.0555 },
                { 0.0004928373928, "492.8 µ", 0.0004928 },
                { 0.000000251, "251 n", 0.000000251 },
                { 0.000000000043, "43 p", 0.000000000043 },
                { 0.000000045643, "45.6 n", 0.0000000456 },
                { Double.MAX_VALUE, "1.7976931348623157E308", 1.7976931348623157E308 },
                { Double.POSITIVE_INFINITY, "∞", Double.POSITIVE_INFINITY },
                { Double.MIN_NORMAL, "0", 0L },
                { Double.NEGATIVE_INFINITY, "-∞", Double.NEGATIVE_INFINITY },
                { Double.NaN, "�", Double.NaN }
        });
    }

    /**
     * Test the {@link Format#format(Object)} method
     */
    @Test
    public void testFormat() {
        assertEquals("format value", fStringValue, FORMATTER.format(fNumValue));
    }

    /**
     * Test the {@link Format#parseObject(String)} method
     *
     * @throws ParseException
     *             if the string cannot be parsed
     */
    @Test
    public void testParseObject() throws ParseException {
        assertEquals("parseObject value", fParseValue, FORMATTER.parseObject(fStringValue));
    }
}
