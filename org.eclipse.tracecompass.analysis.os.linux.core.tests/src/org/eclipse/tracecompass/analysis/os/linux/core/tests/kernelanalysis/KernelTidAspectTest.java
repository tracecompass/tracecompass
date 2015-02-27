/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.kernelanalysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.ThreadPriorityAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.Activator;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Test the {@link KernelTidAspect} class
 *
 * @author Geneviève Bastien
 */
public class KernelTidAspectTest {

    private static final @NonNull String LTTNG_KERNEL_FILE = "testfiles/lttng_kernel_analysis.xml";

    // ------------------------------------------------------------------------
    // Test trace class definition
    // ------------------------------------------------------------------------

    private static class TmfXmlTraceStubWithTidAspects extends TmfXmlTraceStub {

        public TmfXmlTraceStubWithTidAspects() {
            super();
        }

        @Override
        public Iterable<ITmfEventAspect> getEventAspects() {
            ImmutableSet.Builder<ITmfEventAspect> builder = ImmutableSet.builder();
            builder.addAll(super.getEventAspects());
            builder.add(KernelTidAspect.INSTANCE);
            builder.add(ThreadPriorityAspect.INSTANCE);
            return NonNullUtils.checkNotNull(builder.build());
        }

    }

    private ITmfTrace fTrace;

    private static void deleteSuppFiles(ITmfTrace trace) {
        /* Remove supplementary files */
        File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
        for (File file : suppDir.listFiles()) {
            file.delete();
        }
    }

    /**
     * Test setup
     */
    @Before
    public void setUp() {
        ITmfTrace trace = new TmfXmlTraceStubWithTidAspects();
        IPath filePath = Activator.getAbsoluteFilePath(LTTNG_KERNEL_FILE);
        IStatus status = trace.validate(null, filePath.toOSString());
        if (!status.isOK()) {
            fail(status.getException().getMessage());
        }
        try {
            trace.initTrace(null, filePath.toOSString(), TmfEvent.class);
        } catch (TmfTraceException e) {
            fail(e.getMessage());
        }
        deleteSuppFiles(trace);
        /* Make sure the Kernel analysis has run */
        ((TmfTrace) trace).traceOpened(new TmfTraceOpenedSignal(this, trace, null));
        IAnalysisModule module = null;
        for (IAnalysisModule mod : TmfTraceUtils.getAnalysisModulesOfClass(trace, KernelAnalysis.class)) {
            module = mod;
        }
        assertNotNull(module);
        module.schedule();
        module.waitForCompletion();
        fTrace = trace;
    }

    /**
     * Test clean up
     */
    @After
    public void tearDown() {
        fTrace.dispose();
    }

    private void resolveNextEvent(ITmfContext context, Integer tid) {
        ITmfTrace trace = fTrace;
        ITmfEvent event = trace.getNext(context);
        assertNotNull(event);

        Object tidObj = TmfTraceUtils.resolveEventAspectOfClassForEvent(trace, KernelTidAspect.class, event);
        if (tid == null) {
            assertNull(tidObj);
        } else {
            assertNotNull(tidObj);
            assertEquals(tid, tidObj);
        }
    }

    /**
     * Test the {@link KernelTidAspect#resolve(ITmfEvent)} method method
     */
    @Test
    public void testResolveTidAspect() {

        ITmfContext context = fTrace.seekEvent(0L);
        resolveNextEvent(context, null);
        resolveNextEvent(context, null);
        resolveNextEvent(context, null);
        resolveNextEvent(context, 11);
        resolveNextEvent(context, null);
        resolveNextEvent(context, null);
        resolveNextEvent(context, 20);
        resolveNextEvent(context, 20);
        resolveNextEvent(context, 21);
        resolveNextEvent(context, 11);
        resolveNextEvent(context, 30);
        resolveNextEvent(context, 21);
        resolveNextEvent(context, 20);
    }

}
