/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.core.tests.filter.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.function.Predicate;

import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser.FilterCu;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * Test suite for the {@link FilterCu}
 *
 * @author Jean-Christian
 */
@SuppressWarnings("javadoc")
public class ElementResolverFilterTest {

    private static final ElementResolverStub ELEMENT = new ElementResolverStub(ImmutableMap.of("label", "elementLabel", "key0", "value0", "key1", "value1", "key 2", "value2", "key3", "10"));

    @Test
    public void testRegex() {
        //Test a constant string
        FilterCu cu = FilterCu.compile("Label");
        Predicate<Map<String, String>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.computeData()));

        //Test an unmatched constant string
        cu = FilterCu.compile("fail");
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.computeData()));

        //Test a regex
        cu = FilterCu.compile("0$");
        predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.computeData()));

        //Test a regex
        cu = FilterCu.compile("a.*l");
        predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.computeData()));

        //Test an unmatched regex
        cu = FilterCu.compile("y$");
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.computeData()));
    }

    @Test
    public void testLogicalOperator() {
        FilterCu cu = FilterCu.compile("Label && value");
        Predicate<Map<String, String>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.computeData()));

        cu = FilterCu.compile("label && fail");
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.computeData()));

        cu = FilterCu.compile("Label || absent");
        predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.computeData()));

        cu = FilterCu.compile("absent || fail");
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.computeData()));
    }

    @Test
    public void testEqualsOperator() {
        FilterCu cu = FilterCu.compile("label == elementLabel");
        Predicate<Map<String, String>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.computeData()));

        cu = FilterCu.compile("label == fail");
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.computeData()));
    }

    @Test
    public void testNotEqualsOperator() {
        FilterCu cu = FilterCu.compile("label != fail");
        Predicate<Map<String, String>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.computeData()));

        cu = FilterCu.compile("label != elementLabel");
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.computeData()));
    }

    @Test
    public void testNotOperator() {
        FilterCu cu = FilterCu.compile("!fail");
        Predicate<Map<String, String>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.computeData()));

        cu = FilterCu.compile("!elementLabel");
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.computeData()));
    }

    @Test
    public void testContainsOperator() {
        FilterCu cu = FilterCu.compile("label contains element");
        Predicate<Map<String, String>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.computeData()));

        cu = FilterCu.compile("label contains value");
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.computeData()));
    }

    @Test
    public void testPresentOperator() {
        FilterCu cu = FilterCu.compile("key1 present");
        Predicate<Map<String, String>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.computeData()));

        cu = FilterCu.compile("invalidKey present");
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.computeData()));
    }

    @Test
    public void testMatchesOperator() {
        FilterCu cu = FilterCu.compile("key1 matches v.*ue");
        Predicate<Map<String, String>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.computeData()));

        cu = FilterCu.compile("key1 matches v.*ue$");
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.computeData()));

        cu = FilterCu.compile(" \"key 2\" matches value2");
        predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.computeData()));
    }

    @Test
    public void testGreaterThanOperator() {
        FilterCu cu = FilterCu.compile("key3 > 9");
        Predicate<Map<String, String>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.computeData()));

        cu = FilterCu.compile("key3 > 10");
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.computeData()));
    }

    @Test
    public void testLessThanOperator() {
        FilterCu cu = FilterCu.compile("key3 < 11");
        Predicate<Map<String, String>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.computeData()));

        cu = FilterCu.compile("key3 < 10");
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.computeData()));
    }

    @Test
    public void testComplexFilter() {
        FilterCu cu = FilterCu.compile("(key1 matches v.*ue) && Label");
        Predicate<Map<String, String>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.computeData()));

        cu = FilterCu.compile("!(key1 matches v.*ue) && Label");
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.computeData()));
    }
}
