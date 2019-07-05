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
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.format.DataSpeedWithUnitFormat;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the {@link DataSpeedWithUnitFormat} class
 *
 * @author Geneviève Bastien
 */
public class DataSpeedFormatTest extends DataSizeFormatTest {

    private static final @NonNull Format FORMAT = DataSpeedWithUnitFormat.getInstance();
    private static final String PER_SECOND = "/s";

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0} - {1}")
    public static Iterable<Object[]> getParameters() {
        List<Object[]> parameters = new ArrayList<>(DataSizeFormatTest.getCommonParameters());
        // Add the suffix to the strings
        for (Object[] objects : parameters) {
            objects[1] += PER_SECOND;
        }

        List<Object[]> newParameters = Arrays.asList(new Object[][] {
                // Wrong units, just take the number as bytes
                { null, "1234 TPotato", 1234L, 4 },
                // Double number for entry
                { null, ".0001 MB" + PER_SECOND, 104.8576, -1 },
                // No space between number and units
                { null, "1KB" + PER_SECOND, 1024L, -1 },
                // Trailing spaces at the end
                { null, "1KB" + PER_SECOND + "   ", 1024L, 5 },
                // Trailing text
                { null, "1KB" + PER_SECOND + "  potato", 1024L, 5 },
                // No per_second suffix
                { null, "1 KB", 1L, 1 },
        });
        parameters.addAll(newParameters);
        return parameters;
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
    public DataSpeedFormatTest(@NonNull Number numValue, @NonNull String stringValue, @NonNull Number parseValue, int parseIndex) {
        super(numValue, stringValue, parseValue, parseIndex);
    }

    @Override
    protected Format getFormatter() {
        return FORMAT;
    }
}
