/*******************************************************************************
 * Copyright (c) 2016, 2018 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests.perf;

import java.io.IOException;
import java.util.EnumSet;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.tracecompass.analysis.os.linux.ui.swtbot.tests.perf.views.UiResponseTest;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfBenchmarkTrace;
import org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.PerspectiveFactory;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the responsiveness of Control Flow View and Resources View for different
 * traces and scenarios. Ideally, when running this test, JUL logging should be
 * enabled using a logger.properties file. LTTng JUL handler is advised since it
 * works better with multi-threaded applications than other log handlers
 *
 * @author Geneviève Bastien
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class LttngUiResponseBenchmark extends UiResponseTest {

    private static final String TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";

    @Override
    protected void prepareWorkspace() {
        /* Switch to kernel perspective */
        SWTBotUtils.switchToPerspective(PerspectiveFactory.ID);
    }

    /**
     * Test with the django trace
     *
     * @throws SecurityException
     *             If a security manager is present and any the wrong class is
     *             loaded or the class loader is not the same as its ancestor's
     *             loader.
     * @throws IllegalArgumentException
     *             the object is not the correct class type
     * @throws IOException
     *             Exceptions with the trace file
     */
    @Test
    public void testWithDjango() throws SecurityException, IllegalArgumentException, IOException {
        runTestWithTrace(FileUtils.toFile(FileLocator.toFileURL(CtfTestTrace.DJANGO_CLIENT.getTraceURL())).getAbsolutePath(), TRACE_TYPE, EnumSet.allOf(OsLinuxViews.class));
    }

    /**
     * Test with the many-threads trace
     *
     * @throws SecurityException
     *             If a security manager is present and any the wrong class is
     *             loaded or the class loader is not the same as its ancestor's
     *             loader.
     * @throws IllegalArgumentException
     *             the object is not the correct class type
     * @throws IOException
     *             Exceptions with the trace file
     *
     */
    @Test
    public void testWithManyThreads() throws SecurityException, IllegalArgumentException, IOException {
        runTestWithTrace(FileUtils.toFile(FileLocator.toFileURL(CtfTestTrace.MANY_THREADS.getTraceURL())).getAbsolutePath(), TRACE_TYPE, EnumSet.of(OsLinuxViews.CONTROL_FLOW, OsLinuxViews.RESOURCES, OsLinuxViews.CPU_USAGE, OsLinuxViews.DISK_IO_ACTIVITY));
    }

    /**
     * Test with the os-events benchmark trace
     *
     * @throws SecurityException
     *             If a security manager is present and any the wrong class is
     *             loaded or the class loader is not the same as its ancestor's
     *             loader.
     * @throws IllegalArgumentException
     *             the object is not the correct class type
     */
    @Test
    public void testWithOsEvents() throws SecurityException, IllegalArgumentException {
        runTestWithTrace(CtfBenchmarkTrace.ALL_OS_ANALYSES.getTracePath().toString(), TRACE_TYPE, EnumSet.of(OsLinuxViews.CONTROL_FLOW, OsLinuxViews.RESOURCES, OsLinuxViews.CPU_USAGE, OsLinuxViews.DISK_IO_ACTIVITY));
    }

}
