/*******************************************************************************
 * Copyright (c) 2019, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.callsite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.Activator;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.TmfXmlKernelTraceStub;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadStatusDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.analysis.callsite.CallsiteAnalysis;
import org.eclipse.tracecompass.tmf.core.analysis.callsite.ITmfCallsiteIterator;
import org.eclipse.tracecompass.tmf.core.analysis.callsite.TimeCallsite;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link CallsiteAnalysis} test
 *
 * @author Matthew Khouzam
 */
public class CallsiteTest {

    private static final String KERNEL_ANALYSIS = "testfiles/kernel_analysis/lttng_kernel_analysis.xml";
    private static final List<@NonNull ITmfCallsite> EXPECTED_40 = Arrays.asList(new TmfCallsite("fs/open.c", 0L));
    private static final List<@NonNull ITmfCallsite> EXPECTED_71 = Arrays.asList(new TmfCallsite("fs/read_write.c", 0L));
    private TmfXmlKernelTraceStub fTrace;

    /**
     * Init
     *
     * @throws TmfTraceException
     *             If we couldn't open the trace
     */
    @Before
    public void init() throws TmfTraceException {
        IPath filePath = Activator.getAbsoluteFilePath(KERNEL_ANALYSIS);
        fTrace = new TmfXmlKernelTraceStub();
        fTrace.initTrace(null, filePath.toOSString(), TmfEvent.class);
        fTrace.traceOpened(new TmfTraceOpenedSignal(this, fTrace, null));
    }

    /**
     * Tear down
     */
    @After
    public void teardown() {
        fTrace.dispose();
    }

    /**
     * Test the {@link ThreadStatusDataProvider} for the XML kernel test trace
     */
    @Test
    public void testCallsiteAnalysis() {
        TmfXmlKernelTraceStub trace = fTrace;
        assertNotNull(trace);
        CallsiteAnalysis module = TmfTraceUtils.getAnalysisModuleOfClass(trace, CallsiteAnalysis.class, CallsiteAnalysis.ID);
        assertNotNull(module);
        assertTrue(module.schedule().isOK());
        assertTrue(module.waitForCompletion());
        String uuid = String.valueOf(fTrace.getUUID());
        String cpu = "cpu";
        assertEquals(Collections.emptyList(), module.getCallsites(uuid, cpu, "0", 39));
        assertEquals(EXPECTED_40, module.getCallsites(uuid, cpu, "0", 40));
        assertEquals(EXPECTED_40, module.getCallsites(uuid, cpu, "0", 41));
        assertEquals(EXPECTED_40, module.getCallsites(uuid, cpu, "0", 70));
        assertEquals(EXPECTED_71, module.getCallsites(uuid, cpu, "0", 71));
        assertEquals(EXPECTED_71, module.getCallsites(uuid, cpu, "0", 72));
        assertEquals(Collections.emptyList(), module.getCallsites("Hello", cpu, "0", 42));
        assertEquals(Collections.emptyList(), module.getCallsites(uuid, cpu, "1", 42));
        assertEquals(Collections.emptyList(), module.getCallsites(uuid, cpu, "0", 12));
        assertEquals(Collections.emptyList(), module.getCallsites(uuid, cpu, "..", 42));
    }

    /**
     * Test callsite iterator
     */
    @Test
    public void testIterator() {
        TmfXmlKernelTraceStub trace = fTrace;
        assertNotNull(trace);
        CallsiteAnalysis module = TmfTraceUtils.getAnalysisModuleOfClass(trace, CallsiteAnalysis.class, CallsiteAnalysis.ID);
        assertNotNull(module);
        assertTrue(module.schedule().isOK());
        assertTrue(module.waitForCompletion());
        UUID uuid = trace.getUUID();
        assertNotNull(uuid);
        String cpu = "cpu";

        ITmfCallsiteIterator iter = module.iterator(uuid.toString(), cpu, "0", 1);
        evaluateNextIterator(iter, 40, new TmfCallsite("fs/open.c", 0L));
        evaluateNextIterator(iter, 71, new TmfCallsite("fs/read_write.c", 0L));
        evaluateEmptyNextIterator(iter);
        evaluateEmptyNextIterator(iter);

        iter = module.iterator(uuid.toString(), cpu, "0", 40);
        evaluateNextIterator(iter, 40, new TmfCallsite("fs/open.c", 0L));
        iter = module.iterator(uuid.toString(), cpu, "0", 71);
        evaluateNextIterator(iter, 71, new TmfCallsite("fs/read_write.c", 0L));
        iter = module.iterator(uuid.toString(), cpu, "0", 72);
        evaluateEmptyNextIterator(iter);

        iter = module.iterator(uuid.toString(), cpu, "0", 99);
        evaluatePrevIterator(iter, 71, new TmfCallsite("fs/read_write.c", 0L));
        evaluatePrevIterator(iter, 40, new TmfCallsite("fs/open.c", 0L));
        evaluateEmptyPrevIterator(iter);
        evaluateEmptyPrevIterator(iter);

        iter = module.iterator(uuid.toString(), cpu, "0", 71);
        evaluatePrevIterator(iter, 71, new TmfCallsite("fs/read_write.c", 0L));
        iter = module.iterator(uuid.toString(), cpu, "0", 40);
        evaluatePrevIterator(iter, 40, new TmfCallsite("fs/open.c", 0L));
        iter = module.iterator(uuid.toString(), cpu, "0", 39);
        evaluateEmptyPrevIterator(iter);

        iter = module.iterator(uuid.toString(), cpu, "0", 42);
        for (int i = 0; i < 10; i++) {
            evaluateNextIterator(iter, 71, new TmfCallsite("fs/read_write.c", 0L));
            evaluateEmptyNextIterator(iter);
            evaluatePrevIterator(iter, 40, new TmfCallsite("fs/open.c", 0L));
            evaluateEmptyPrevIterator(iter);
        }

        iter = module.iterator(uuid.toString(), cpu, "0", 42);
        for (int i = 0; i < 10; i++) {
            evaluatePrevIterator(iter, 40, new TmfCallsite("fs/open.c", 0L));
            evaluateEmptyPrevIterator(iter);
            evaluateNextIterator(iter, 71, new TmfCallsite("fs/read_write.c", 0L));
            evaluateEmptyNextIterator(iter);
        }

        iter = module.iterator("", cpu, "0", 42);
        evaluateEmptyNextIterator(iter);
        evaluateEmptyPrevIterator(iter);

        iter = module.iterator(uuid.toString(), cpu, "elephant", 42);
        evaluateEmptyNextIterator(iter);
        evaluateEmptyPrevIterator(iter);
    }

    private static void evaluateEmptyNextIterator(Iterator<@NonNull TimeCallsite> iter) {
        assertFalse(iter.hasNext());
    }

    private static void evaluateEmptyPrevIterator(ITmfCallsiteIterator iter) {
        assertFalse(iter.hasPrevious());
    }

    private static void evaluateNextIterator(Iterator<@NonNull TimeCallsite> iter, long time, TmfCallsite callsite) {
        assertTrue(iter.hasNext());
        TimeCallsite next = iter.next();
        assertEquals(time, next.getTime());
        assertEquals(callsite, next.getCallsite());
    }

    private static void evaluatePrevIterator(ITmfCallsiteIterator iter, long time, TmfCallsite callsite) {
        assertTrue(iter.hasPrevious());
        TimeCallsite previous = iter.previous();
        assertEquals(time, previous.getTime());
        assertEquals(callsite, previous.getCallsite());
    }

}