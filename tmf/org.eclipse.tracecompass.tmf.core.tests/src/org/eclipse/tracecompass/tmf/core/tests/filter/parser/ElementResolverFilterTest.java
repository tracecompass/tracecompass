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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.function.Predicate;

import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser.FilterCu;
import org.junit.Test;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * Test suite for the {@link FilterCu}
 *
 * @author Jean-Christian
 */
@SuppressWarnings("javadoc")
public class ElementResolverFilterTest {

    private static final ElementResolverStub ELEMENT = new ElementResolverStub(ImmutableMultimap.of("label", "elementLabel", "key0", "value0", "key0", "some other", "key 2", "value2", "key3", "10"));

    @Test
    public void testRegex() {
        //Test a constant string
        FilterCu cu = FilterCu.compile("Label");
        assertNotNull(cu);
        Predicate<Multimap<String, Object>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        //Test an unmatched constant string
        cu = FilterCu.compile("fail");
        assertNotNull(cu);
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.getMetadata()));

        //Test a regex
        cu = FilterCu.compile("0$");
        assertNotNull(cu);
        predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        //Test a regex
        cu = FilterCu.compile("a.*l");
        assertNotNull(cu);
        predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        //Test an unmatched regex
        cu = FilterCu.compile("y$");
        assertNotNull(cu);
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.getMetadata()));
    }

    @Test
    public void testLogicalOperator() {
        FilterCu cu = FilterCu.compile("Label && value");
        assertNotNull(cu);
        Predicate<Multimap<String, Object>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        cu = FilterCu.compile("label && fail");
        assertNotNull(cu);
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.getMetadata()));

        cu = FilterCu.compile("Label || absent");
        assertNotNull(cu);
        predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        cu = FilterCu.compile("absent || fail");
        assertNotNull(cu);
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.getMetadata()));
    }

    @Test
    public void testEqualsOperator() {
        FilterCu cu = FilterCu.compile("label == elementLabel");
        assertNotNull(cu);
        Predicate<Multimap<String, Object>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        cu = FilterCu.compile("label == fail");
        assertNotNull(cu);
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.getMetadata()));
    }

    @Test
    public void testNotEqualsOperator() {
        FilterCu cu = FilterCu.compile("label != fail");
        assertNotNull(cu);
        Predicate<Multimap<String, Object>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        cu = FilterCu.compile("label != elementLabel");
        assertNotNull(cu);
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.getMetadata()));
    }

    @Test
    public void testNotOperator() {
        FilterCu cu = FilterCu.compile("!fail");
        assertNotNull(cu);
        Predicate<Multimap<String, Object>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        cu = FilterCu.compile("!elementLabel");
        assertNotNull(cu);
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.getMetadata()));
    }

    @Test
    public void testContainsOperator() {
        FilterCu cu = FilterCu.compile("label contains element");
        assertNotNull(cu);
        Predicate<Multimap<String, Object>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        cu = FilterCu.compile("label contains value");
        assertNotNull(cu);
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.getMetadata()));
    }

    @Test
    public void testPresentOperator() {
        FilterCu cu = FilterCu.compile("key0 present");
        assertNotNull(cu);
        Predicate<Multimap<String, Object>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        cu = FilterCu.compile("invalidKey present");
        assertNotNull(cu);
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.getMetadata()));
    }

    @Test
    public void testMatchesOperator() {
        FilterCu cu = FilterCu.compile("key0 matches v.*ue");
        assertNotNull(cu);
        Predicate<Multimap<String, Object>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        // Test with a second value of key0
        cu = FilterCu.compile("key0 matches o.*er");
        assertNotNull(cu);
        predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        cu = FilterCu.compile("key0 matches v.*ue$");
        assertNotNull(cu);
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.getMetadata()));

        cu = FilterCu.compile(" \"key 2\" matches value2");
        assertNotNull(cu);
        predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));
    }

    @Test
    public void testGreaterThanOperator() {
        FilterCu cu = FilterCu.compile("key3 > 9");
        assertNotNull(cu);
        Predicate<Multimap<String, Object>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        cu = FilterCu.compile("key3 > 10");
        assertNotNull(cu);
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.getMetadata()));
    }

    @Test
    public void testLessThanOperator() {
        FilterCu cu = FilterCu.compile("key3 < 11");
        assertNotNull(cu);
        Predicate<Multimap<String, Object>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        cu = FilterCu.compile("key3 < 10");
        assertNotNull(cu);
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.getMetadata()));
    }

    @Test
    public void testComplexFilter() {
        FilterCu cu = FilterCu.compile("(key0 matches v.*ue) && Label");
        assertNotNull(cu);
        Predicate<Multimap<String, Object>> predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        cu = FilterCu.compile("!(key0 matches v.*ue) && Label");
        assertNotNull(cu);
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.getMetadata()));

        // Filter with multiple keys
        cu = FilterCu.compile("(key0 matches v.*ue) && (key0 matches \"some other\")");
        assertNotNull(cu);
        predicate = cu.generate();
        assertTrue(predicate.test(ELEMENT.getMetadata()));

        cu = FilterCu.compile("(key0 matches v.*ue) && (key0 matches \"invalid\")");
        assertNotNull(cu);
        predicate = cu.generate();
        assertFalse(predicate.test(ELEMENT.getMetadata()));
    }

    @Test
    public void testInvalid() {
        FilterCu cu = FilterCu.compile("label = elementLabel");
        assertNull(cu);
    }
}
