/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.tests.trace;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxTidAspect;
import org.eclipse.tracecompass.lttng2.ust.core.tests.shared.LttngUstTestTraceUtils;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link LttngUstTrace} class
 *
 * @author Geneviève Bastien
 */
public class LttngUstTraceTest {

    private static final @NonNull CtfTestTrace TEST_TRACE = CtfTestTrace.CYG_PROFILE;

    private ITmfTrace fTrace;

    /**
     * Perform pre-class initialization.
     */
    @Before
    public void setUp() {
        fTrace = LttngUstTestTraceUtils.getTrace(TEST_TRACE);

    }

    /** Empty and delete a directory */
    private static void deleteDirectory(File dir) {
        /* Assuming the dir only contains file or empty directories */
        for (File file : dir.listFiles()) {
            file.delete();
        }
        dir.delete();
    }

    /**
     * Perform post-class clean-up.
     */
    @After
    public void tearDown() {
        ITmfTrace trace = fTrace;
        if (trace != null) {
            LttngUstTestTraceUtils.dispose(TEST_TRACE);
            File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
            deleteDirectory(suppDir);
        }
    }

    private static class TestEventRequest extends TmfEventRequest {

        private String errString = null;

        public TestEventRequest() {
            super(ITmfEvent.class, TmfTimeRange.ETERNITY, 0, 2, ExecutionType.FOREGROUND);
        }

        @Override
        public void handleData(@NonNull ITmfEvent event) {
            super.handleData(event);
            Integer tid = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), LinuxTidAspect.class, event);
            if (tid == null) {
                errString = "No TID for event " + event;
                cancel();
                return;
            }
            if (tid.intValue() != 16073) {
                errString = "Wrong tid: " + tid + " for event";
                cancel();
                return;
            }
        }

        public String getErrString() {
            return errString;
        }

    }

    /**
     * Test the LinuxTidAspect for this trace
     *
     * @throws InterruptedException
     *             exception thrown in the request
     */
    @Test
    public void testTidAspect() throws InterruptedException {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        TestEventRequest request = new TestEventRequest();
        trace.sendRequest(request);
        request.waitForCompletion();
        assertNull(request.getErrString());
    }
}
