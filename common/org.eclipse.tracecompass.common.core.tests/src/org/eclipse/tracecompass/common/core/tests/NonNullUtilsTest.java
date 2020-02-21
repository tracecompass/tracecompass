/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNullContents;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.junit.Test;

/**
 * Tests for {@link NonNullUtils}.
 */
public class NonNullUtilsTest {

    /**
     * Test code to ensure the null-annotations on
     * {@link NonNullUtils#checkNotNullContents(Stream)} work correctly.
     *
     * Not mean to be run as a {@link Test}, the code should simply not produce
     * any compilation errors or warnings.
     */
    public void testAnnotationsCheckNotNullContentsStream() {
        Stream<String> a                     = (new ArrayList<String>()).stream();
        Stream<@Nullable String> b           = (new ArrayList<@Nullable String>()).stream();
        @Nullable Stream<String> c           = (new ArrayList<String>()).stream();
        @Nullable Stream<@Nullable String> d = (new ArrayList<@Nullable String>()).stream();
        Stream<@Nullable String> e           = (new ArrayList<@Nullable String>()).stream();
        @NonNull Stream<String> f            = checkNotNull((new ArrayList<String>()).stream());
        @NonNull Stream<@NonNull String> g   = checkNotNull((new ArrayList<@NonNull String>()).stream());

        @NonNull Stream<@NonNull String> checkedA = checkNotNullContents(a);
        @NonNull Stream<@NonNull String> checkedB = checkNotNullContents(b);
        @NonNull Stream<@NonNull String> checkedC = checkNotNullContents(c);
        @NonNull Stream<@NonNull String> checkedD = checkNotNullContents(d);
        @NonNull Stream<@NonNull String> checkedE = checkNotNullContents(e);
        @NonNull Stream<@NonNull String> checkedF = checkNotNullContents(f);
        @NonNull Stream<@NonNull String> checkedG = checkNotNullContents(g);

        assertNotNull(checkedA);
        assertNotNull(checkedB);
        assertNotNull(checkedC);
        assertNotNull(checkedD);
        assertNotNull(checkedE);
        assertNotNull(checkedF);
        assertNotNull(checkedG);
    }

    /**
     * Test code to ensure the null-annotations on
     * {@link NonNullUtils#checkNotNullContents(Object[])} work correctly.
     *
     * Not mean to be run as a {@link Test}, the code should simply not produce
     * any compilation errors or warnings.
     */
    public void testAnnotationsCheckNotNullContentsArray() {
        String[] a                      = new String[] {};
        @Nullable String[] b            = new @Nullable String[] {};
        String @Nullable [] c           = new String[] {};
        @Nullable String @Nullable [] d = new @Nullable String[] {};
        @NonNull String[] e             = new @NonNull String[] {};
        String @NonNull [] f            = new String[] {};
        @NonNull String @NonNull [] g   = new @NonNull String[] {};

        @NonNull String @NonNull [] checkedA = checkNotNullContents(a);
        @NonNull String @NonNull [] checkedB = checkNotNullContents(b);
        @NonNull String @NonNull [] checkedC = checkNotNullContents(c);
        @NonNull String @NonNull [] checkedD = checkNotNullContents(d);
        @NonNull String @NonNull [] checkedE = checkNotNullContents(e);
        @NonNull String @NonNull [] checkedF = checkNotNullContents(f);
        @NonNull String @NonNull [] checkedG = checkNotNullContents(g);

        assertNotNull(checkedA);
        assertNotNull(checkedB);
        assertNotNull(checkedC);
        assertNotNull(checkedD);
        assertNotNull(checkedE);
        assertNotNull(checkedF);
        assertNotNull(checkedG);
    }

    /**
     * Test {@link NonNullUtils#checkNotNullContents(Stream)} for a stream
     * containing no null elements.
     */
    @Test
    public void testCheckContentsStream() {
        List<@Nullable String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");

        List<@NonNull String> out = checkNotNullContents(list.stream()).collect(Collectors.toList());
        assertEquals(list, out);
    }

    /**
     * Test {@link NonNullUtils#checkNotNullContents(Stream)} with a null
     * reference (should fail immediately).
     */
    @Test(expected = NullPointerException.class)
    public void testCheckContentsStreamNullRef() {
        checkNotNullContents((Stream<@Nullable ?>) null);
    }

    /**
     * Test {@link NonNullUtils#checkNotNullContents(Stream)} with a stream
     * containing a null value.
     */
    @Test(expected = NullPointerException.class)
    public void testCheckContentsStreamNullElement() {
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

    /**
     * Test {@link NonNullUtils#checkNotNullContents(Object[])} for an array
     * containing no null elements.
     */
    @Test
    public void testCheckContentsArray() {
        @Nullable String[] array = new @Nullable String[3];
        array[0] = "a";
        array[1] = "b";
        array[2] = "c";

        @NonNull String[] out = checkNotNullContents(array);
        assertArrayEquals(array, out);
    }

    /**
     * Test {@link NonNullUtils#checkNotNullContents(Object[])} with a null
     * reference (should fail immediately).
     */
    @Test(expected = NullPointerException.class)
    public void testCheckContentsArrayNullRef() {
        checkNotNullContents((@Nullable Object[]) null);
    }

    /**
     * Test {@link NonNullUtils#checkNotNullContents(Object[])} with an array
     * containing a null value.
     */
    @Test(expected = NullPointerException.class)
    public void testCheckContentsArrayNullElement() {
        @Nullable String[] array = new @Nullable String[3];
        array[0] = "a";
        array[1] = null;
        array[2] = "c";

        /* Should fail */
        checkNotNullContents(array);
    }
}
