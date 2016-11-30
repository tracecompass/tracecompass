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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.format.DecimalUnitFormat;
import org.junit.Test;

/**
 * Test the {@link DecimalUnitFormat} class
 *
 * @author Michael Jeanson
 */
public class DecimalUnitFormatErrorTest {

    private static final @NonNull Format FORMATTER = new DecimalUnitFormat();

    /**
     * Test format with an illegal argument
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFormatIllegalArgument() {
        FORMATTER.format("Toto");
    }

    /**
     * Test parsing a string that is not a number
     * @throws ParseException if the string cannot be parsed
     */
    @Test(expected = ParseException.class)
    public void testParseNotANumber() throws ParseException {
        FORMATTER.parseObject("Toto");
    }

    /**
     * Test parsing a number with a unit
     * @throws ParseException if the string cannot be parsed
     */
    @Test
    public void testParseWithUnit() throws ParseException {
        FORMATTER.parseObject("1.2 s");
    }

    /**
     * Test parsing a number with a prefix and a unit
     * @throws ParseException if the string cannot be parsed
     */
    @Test
    public void testParsePrefixWithUnitAndPrefix() throws ParseException {
        assertEquals(0.0012, FORMATTER.parseObject("1.2 ms"));
    }

    /**
     * Test parsing a special Double number with a prefix
     * @throws ParseException if the string cannot be parsed
     */
    @Test
    public void testParseSpecialWithPrefix() throws ParseException {
        assertEquals(Double.POSITIVE_INFINITY, FORMATTER.parseObject("∞ k"));
        assertEquals(Double.NEGATIVE_INFINITY, FORMATTER.parseObject("-∞ p"));
        assertEquals(Double.NaN, FORMATTER.parseObject("�M"));
    }
}
