/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.event.lookup.aspect;

import static org.junit.Assert.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.MultiAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Simple test for MultiAspect
 *
 * @author Matthew Khouzam
 */
public class MultiAspectTest {

    /**
     * Dummy event, never used during resolution.
     */
    private static final @NonNull TmfEvent DUMMY_EVENT = new TmfEvent(null, -1, TmfTimestamp.BIG_BANG, null, null);

    private final @NonNull TmfCpuAspect fAspectNull = new TmfCpuAspect() {
        @Override
        public @Nullable Integer resolve(@NonNull ITmfEvent event) {
            return null;
        }
    };

    private final @NonNull TmfCpuAspect fAspect0 = new TmfCpuAspect() {
        @Override
        public @Nullable Integer resolve(@NonNull ITmfEvent event) {
            return 0;
        }
    };

    private final @NonNull TmfCpuAspect fAspect1 = new TmfCpuAspect() {
        @Override
        public @Nullable Integer resolve(@NonNull ITmfEvent event) {
            return 1;
        }
    };

    /**
     * Test the resolve of aspects.
     */
    @Test
    public void testResolve() {

        ITmfEventAspect<?> fMultiAspect0 = MultiAspect.create(ImmutableList.of(fAspectNull), TmfCpuAspect.class);
        ITmfEventAspect<?> fMultiAspect1 = MultiAspect.create(ImmutableList.of(fAspect0), TmfCpuAspect.class);
        ITmfEventAspect<?> fMultiAspect2 = MultiAspect.create(ImmutableList.of(fAspect1), TmfCpuAspect.class);
        ITmfEventAspect<?> fMultiAspect3 = MultiAspect.create(ImmutableList.of(fAspectNull, fAspect0), TmfCpuAspect.class);
        ITmfEventAspect<?> fMultiAspect4 = MultiAspect.create(ImmutableList.of(fAspectNull, fAspect1), TmfCpuAspect.class);
        ITmfEventAspect<?> fMultiAspect5 = MultiAspect.create(ImmutableList.of(fAspect0, fAspect1), TmfCpuAspect.class);
        ITmfEventAspect<?> fMultiAspect6 = MultiAspect.create(ImmutableList.of(fAspectNull, fAspect0, fAspect1), TmfCpuAspect.class);
        ITmfEventAspect<?> fMultiAspect7 = MultiAspect.create(ImmutableList.of(fAspectNull, fAspect1, fAspect0), TmfCpuAspect.class);
        ITmfEventAspect<?> fMultiAspect8 = MultiAspect.create(ImmutableList.of(fAspect0, fAspectNull), TmfCpuAspect.class);
        ITmfEventAspect<?> fMultiAspect9 = MultiAspect.create(ImmutableList.of(fAspect1, fAspectNull), TmfCpuAspect.class);
        ITmfEventAspect<?> fMultiAspect10 = MultiAspect.create(ImmutableList.of(fAspect1, fAspect0), TmfCpuAspect.class);
        TmfEvent event = DUMMY_EVENT;
        assertNotNull(fMultiAspect0);
        assertNotNull(fMultiAspect1);
        assertNotNull(fMultiAspect2);
        assertNotNull(fMultiAspect3);
        assertNotNull(fMultiAspect4);
        assertNotNull(fMultiAspect5);
        assertNotNull(fMultiAspect6);
        assertNotNull(fMultiAspect7);
        assertNotNull(fMultiAspect8);
        assertNotNull(fMultiAspect9);
        assertNotNull(fMultiAspect10);
        assertEquals(null, fMultiAspect0.resolve(event));
        assertEquals(Integer.valueOf(0), fMultiAspect1.resolve(event));
        assertEquals(Integer.valueOf(1), fMultiAspect2.resolve(event));
        assertEquals(Integer.valueOf(0), fMultiAspect3.resolve(event));
        assertEquals(Integer.valueOf(1), fMultiAspect4.resolve(event));
        assertEquals(Integer.valueOf(0), fMultiAspect5.resolve(event));
        assertEquals(Integer.valueOf(0), fMultiAspect6.resolve(event));
        assertEquals(Integer.valueOf(1), fMultiAspect7.resolve(event));
        assertEquals(Integer.valueOf(0), fMultiAspect8.resolve(event));
        assertEquals(Integer.valueOf(1), fMultiAspect9.resolve(event));
        assertEquals(Integer.valueOf(1), fMultiAspect10.resolve(event));
    }

    /**
     * Simple Foo aspect
     */
    interface FooAspect extends ITmfEventAspect<Long> {
    }

    private long fFooImplRet = 0;
    private long fBarImplRet = 0;

    /**
     * Simple Bar aspect
     */
    interface BarAspect extends FooAspect {
    }

    class FooImpl implements FooAspect {
        @Override
        public @NonNull String getName() {
            return "Foo";
        }

        @Override
        public @NonNull String getHelpText() {
            return "Foo help";
        }

        @Override
        public @Nullable Long resolve(@NonNull ITmfEvent event) {
            return fFooImplRet;
        }
    }

    class BarImpl implements BarAspect {
        @Override
        public @NonNull String getName() {
            return "Bar";
        }

        @Override
        public @NonNull String getHelpText() {
            return "Bar help";
        }

        @Override
        public @Nullable Long resolve(@NonNull ITmfEvent event) {
            return fBarImplRet;
        }
    }

    /**
     * Happy path testing for heterogenous multi-aspect creation
     */
    @Test
    public void testCreateHappy() {
        ITmfEventAspect<?> aspect = MultiAspect.create(ImmutableList.of(new FooImpl(), new BarImpl()), FooAspect.class);
        assertNotNull(aspect);
        assertEquals(fFooImplRet, aspect.resolve(DUMMY_EVENT));
        long orig = fFooImplRet;
        fFooImplRet = 25;
        assertEquals(fFooImplRet, aspect.resolve(DUMMY_EVENT));
        fFooImplRet = orig;
        assertEquals(fFooImplRet, aspect.resolve(DUMMY_EVENT));
    }

    /**
     * What happens when heterogenous multi-aspect creation is given impossible
     * arguments
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateIllegal() {
        // this should cause a problem since foo is not a bar
        MultiAspect.create(ImmutableList.of(new FooImpl(), new BarImpl()), BarAspect.class);
    }
}