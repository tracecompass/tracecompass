/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.kernelanalysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.Activator;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.TmfXmlKernelTraceStub;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.KernelStateProvider;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link KernelStateProvider}
 *
 * @author Alexandre Montplaisir
 */
public class KernelStateProviderTest {

    private static final @NonNull String LTTNG_KERNEL_FILE = "testfiles/lttng_kernel_analysis.xml";

    private IKernelTrace fTrace;
    private ITmfStateProvider fInput;

    /**
     * Set-up.
     */
    @Before
    public void initialize() {
        IKernelTrace thetrace = new TmfXmlKernelTraceStub();
        IPath filePath = Activator.getAbsoluteFilePath(LTTNG_KERNEL_FILE);
        IStatus status = thetrace.validate(null, filePath.toOSString());
        if (!status.isOK()) {
            fail(status.getException().getMessage());
        }
        try {
            thetrace.initTrace(null, filePath.toOSString(), TmfEvent.class);
        } catch (TmfTraceException e) {
            fail(e.getMessage());
        }

        fTrace = thetrace;
        fInput = new KernelStateProvider(thetrace, thetrace.getKernelEventLayout());
    }

    /**
     * Class teardown
     */
    @After
    public void classTeardown() {
        if (fTrace != null) {
            fTrace.dispose();
        }
    }

    /**
     * Test loading the state provider.
     */
    @Test
    public void testOpening() {
        long testStartTime;
        testStartTime = fInput.getStartTime();
        /* Expected start time of the trace */
        assertEquals(testStartTime, 1L);
    }

}
