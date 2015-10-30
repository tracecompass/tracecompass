/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNullContents;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.junit.Test;

/**
 * Tests for {@link NonNullUtils}.
 */
public class NonNullUtilsTest {

    /**
     * Test {@link NonNullUtils#checkNotNullContents} for a stream containing no
     * null elements.
     */
    @Test
    public void testCheckContents() {
        List<@Nullable String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");

        List<@NonNull String> out = checkNotNullContents(list.stream()).collect(Collectors.toList());
        assertEquals(list, out);
    }

    /**
     * Test {@link NonNullUtils#checkNotNullContents} with a null reference
     * (should fail immediately).
     */
    @Test(expected = NullPointerException.class)
    public void testCheckContentsNullRef() {
        checkNotNullContents(null);
    }

    /**
     * Test {@link NonNullUtils#checkNotNullContents} with a stream containing a
     * null value.
     */
    @Test(expected = NullPointerException.class)
    public void testCheckContentsNullElement() {
        List<@Nullable String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add(null);
        list.add("d");

        /*
         * Should fail.
         *
         * Don't forget we need a terminal operation to process the contents of
         * the stream!
         */
        checkNotNullContents(list.stream()).collect(Collectors.toList());
    }
}
