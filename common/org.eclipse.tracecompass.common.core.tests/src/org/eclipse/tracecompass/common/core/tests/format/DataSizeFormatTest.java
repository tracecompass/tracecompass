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
import java.text.ParseException;
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
    public DataSizeFormatTest(@NonNull Number numValue, @NonNull String stringValue, @NonNull Number parseValue) {
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
                { 0, "0 B", 0L },
                { 3, "3 B", 3L },
                { 975, "975 B", 975L },
                { 1024, "1 KB", 1024L },
                { 1024 * 1024, "1 MB", 1024 * 1024L },
                { 1024 * 1024 * 1024, "1 GB", 1024 * 1024 * 1024L },
                { 1024L * 1024L * 1024L * 1024L, "1 TB", 1024 * 1024 * 1024 * 1024L },
                { 4096, "4 KB", 4096L },
                { -4096, "-4 KB", -4096L },
                { 4096L, "4 KB", 4096L },
                { 4096.0, "4 KB", 4096L },
                { 12345678, "11.774 MB", 12345933.824 },
                { Integer.MAX_VALUE, "2 GB", 2147483648L },
                { Integer.MIN_VALUE, "-2 GB", -2147483648L },
                { Long.MAX_VALUE, "8388608 TB", 9.223372036854775808E18 },
                { 98765432.123456, "94.19 MB", 98765373.44 },
                { -98765432.123456, "-94.19 MB", -98765373.44 },
                { 555555555555L, "517.401 GB", 555555093479.424 },
                { 555555555555555L, "505.275 TB", 555555737724518.4 }
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
        assertEquals("format value", fStringValue, getFormatter().format(fNumValue));
    }

    /**
     * Test the {@link Format#parseObject(String)} method
     *
     * @throws ParseException
     *             if the string cannot be parsed
     */
    @Test
    public void testParseObject() throws ParseException {
        assertEquals("parseObject value", fParseValue, getFormatter().parseObject(fStringValue));
    }
}
