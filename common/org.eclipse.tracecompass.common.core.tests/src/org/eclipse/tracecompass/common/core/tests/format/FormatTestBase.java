/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;

/**
 * Base formatter test that tests the formatting to String, as well as string
 * parsing for different values.
 *
 * @author Geneviève Bastien
 */
public abstract class FormatTestBase {

    private final @Nullable Number fNumValue;
    private final @NonNull String fStringValue;
    private final @Nullable Number fParseValue;
    private final int fParseIndex;

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
    public FormatTestBase(@Nullable Number numValue, @NonNull String stringValue, @Nullable Number parseValue, int parseIndex) {
        fNumValue = numValue;
        fStringValue = stringValue;
        fParseValue = parseValue;
        fParseIndex = parseIndex;
    }

    /**
     * Get the formatter to test
     *
     * @return The formatter to test
     */
    protected abstract Format getFormatter();

    /**
     * Test the {@link Format#format(Object)} method
     */
    @Test
    public void testFormat() {
        // If the value is null, don't test formatting
        if (fNumValue != null) {
            assertEquals("format value", fStringValue, getFormatter().format(fNumValue));
        }
    }

    /**
     * Test the {@link Format#parseObject(String)} method
     *
     * @throws ParseException
     *             if the string cannot be parsed
     */
    @Test
    public void testParseObject() throws ParseException {
        if (fParseValue != null) {
            assertEquals("parseObject value", fParseValue, getFormatter().parseObject(fStringValue));
        } else {
            // String should throw an error
            ParseException exception = null;
            try {
                getFormatter().parseObject(fStringValue);
            } catch (ParseException e) {
                exception = e;
            }
            assertNotNull(exception);
        }
    }

    /**
     * Test the {@link Format#parseObject(String, ParsePosition)} method
     */
    @Test
    public void testParseObject2() {
        ParsePosition pos = new ParsePosition(0);
        if (fParseValue != null) {
            assertEquals("parseObject with pos value", fParseValue, getFormatter().parseObject(fStringValue, pos));
            if (fParseIndex < 0) {
                // The whole length of the string should have been parsed
                assertEquals("String parsed final position", fStringValue.length(), pos.getIndex());
                assertEquals("String parsed error index position", -1, pos.getErrorIndex());
            } else {
                // There's trailing string after the parsing
                assertEquals("String parsed final position", fParseIndex, pos.getIndex());
            }
        } else {
            // String should return null
            assertNull(getFormatter().parseObject(fStringValue, pos));
            assertNotEquals("String parsed final position", fStringValue.length(), pos.getIndex());
        }
    }

    /**
     * Test format with an illegal argument
     */
    @Test
    public void testFormatIllegalArgument() {
        assertEquals("Illegal argument", "", getFormatter().format("Toto"));
    }

}
