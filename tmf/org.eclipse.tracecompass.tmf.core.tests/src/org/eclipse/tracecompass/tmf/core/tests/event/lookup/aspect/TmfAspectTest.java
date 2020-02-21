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
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.junit.Test;

/**
 * Simple test for TmfAspect
 *
 * @author Matthew Khouzam
 */
public class TmfAspectTest {

    ITmfEventAspect<Integer> fAspect0 = new TmfCpuAspect() {
        @Override
        public @Nullable Integer resolve(@NonNull ITmfEvent event) {
            return 0;
        }
    };

    ITmfEventAspect<Integer> fAspect1 = new TmfCpuAspect() {
        @Override
        public @Nullable Integer resolve(@NonNull ITmfEvent event) {
            return 1;
        }
    };

    /**
     * Test getname of aspects
     */
    @Test
    public void testGetName() {
        assertEquals("CPU", fAspect0.getName());
        assertEquals("CPU", fAspect1.getName());
    }

    /**
     * Test the resolve of aspects.
     */
    @Test
    public void testResolve() {
        TmfEvent event = new TmfEvent(null, -1, TmfTimestamp.BIG_BANG, null, null);
        assertEquals(Integer.valueOf(0), fAspect0.resolve(event));
        assertEquals(Integer.valueOf(1), fAspect1.resolve(event));
    }
}
