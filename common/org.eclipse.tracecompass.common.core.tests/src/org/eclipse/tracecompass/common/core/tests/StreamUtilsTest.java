/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.eclipse.tracecompass.common.core.StreamUtils;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Tests for {@link StreamUtils} methods.
 *
 * @author Alexandre Montplaisir
 */
@Deprecated
public class StreamUtilsTest {

    /**
     * Test {@link StreamUtils#getStream(Iterator)}.
     */
    @Test
    public void testGetStreamIterator() {
        List<Integer> list = ImmutableList.of(1, 2, 3, 4, 5);

        /* Test short-circuiting terminal operation */
        Iterator<Integer> iter = list.iterator();
        boolean test = StreamUtils.getStream(iter)
                .anyMatch(value -> value == 3);
        assertTrue(test);

        /*
         * Test that the short-circuiting operation stopped its iteration where
         * it needed to.
         */
        assertTrue(iter.hasNext());
        assertEquals(4, iter.next().intValue());

        /* Test fully-consuming terminal operation */
        iter = list.iterator();
        int sum = StreamUtils.getStream(iter)
                .mapToInt(Integer::intValue)
                .sum();
        assertEquals(15, sum);
        assertFalse(iter.hasNext());
    }
}
