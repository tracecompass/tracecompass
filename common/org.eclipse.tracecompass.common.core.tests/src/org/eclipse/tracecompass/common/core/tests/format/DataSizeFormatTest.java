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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.format.DataSizeWithUnitFormat;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the {@link DataSizeWithUnitFormat} class
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class DataSizeFormatTest extends FormatTestBase {

    private static final @NonNull Format FORMAT = DataSizeWithUnitFormat.getInstance();

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
    public DataSizeFormatTest(@Nullable Number numValue, @NonNull String stringValue, @Nullable Number parseValue, int parseIndex) {
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
                { 0, "0 B", 0L, -1 },
                { 3, "3 B", 3L, -1 },
                { 975, "975 B", 975L, -1 },
                { 1024, "1 KB", 1024L, -1 },
                { 1024 * 1024, "1 MB", 1024 * 1024L, -1 },
                { 1024 * 1024 * 1024, "1 GB", 1024 * 1024 * 1024L, -1 },
                { 1024L * 1024L * 1024L * 1024L, "1 TB", 1024 * 1024 * 1024 * 1024L, -1 },
                { 4096, "4 KB", 4096L, -1 },
                { -4096, "-4 KB", -4096L, -1 },
                { 4096L, "4 KB", 4096L, -1 },
                { 4096.0, "4 KB", 4096L, -1 },
                { 12345678, "11.774 MB", 12345933.824, -1 },
                { Integer.MAX_VALUE, "2 GB", 2147483648L, -1 },
                { Integer.MIN_VALUE, "-2 GB", -2147483648L, -1 },
                { Long.MAX_VALUE, "8388608 TB", 9.223372036854775808E18, -1 },
                { 98765432.123456, "94.19 MB", 98765373.44, -1 },
                { -98765432.123456, "-94.19 MB", -98765373.44, -1 },
                { 555555555555L, "517.401 GB", 555555093479.424, -1 },
                { 555555555555555L, "505.275 TB", 555555737724518.4, -1 },
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
                { null, ".0001 MB", 104.8576, -1 },
                // No space between number and units
                { null, "1KB", 1024L, -1 },
                // Trailing spaces at the end
                { null, "1KB   ", 1024L, 3 },
                // Trailing text
                { null, "1KB  potato", 1024L, 3 },
        });
        parameters.addAll(newParameters);
        return parameters;
    }

    @Override
    protected Format getFormatter() {
        return FORMAT;
    }
}
