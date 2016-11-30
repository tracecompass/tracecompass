/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.analysis.Messages;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot stress test for opening and closing of traces.
 *
 * @author Bernd Hufmann
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class OpenTraceStressTest {

    private static final String TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    private static final String KERNEL_PERSPECTIVE_ID = "org.eclipse.linuxtools.lttng2.kernel.ui.perspective";
    private static final @NonNull CtfTestTrace CTF_TRACE = CtfTestTrace.SYNC_DEST;
    private static final String TRACE_PROJECT_NAME = "test";

    private static SWTWorkbenchBot workbenchbot;

    /**
     * Test Class setup
     */
    @BeforeClass
    public static void init() {
        SWTBotUtils.initialize();

        /* Set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */

        workbenchbot = new SWTWorkbenchBot();

        /* Close welcome view */
        SWTBotUtils.closeView("Welcome", workbenchbot);

        /* Switch perspectives */
        SWTBotUtils.switchToPerspective(KERNEL_PERSPECTIVE_ID);

        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Main test case to test opening and closing of traces concurrently.
     */
    @Test
    public void testOpenAndCloseConcurrency() {
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);

        File fTestFile = new File(CtfTmfTestTraceUtils.getTrace(CTF_TRACE).getPath());
        CtfTmfTestTraceUtils.dispose(CTF_TRACE);

        String path = fTestFile.getAbsolutePath();

        assertNotNull(fTestFile);
        assumeTrue(fTestFile.exists());

        /*
         *  This opening and closing of traces will trigger several threads for analysis which
         *  will be closed concurrently. There used to be a concurrency bug (447434) which should
         *  be fixed by now and this test should run without any exceptions.
         *
         *  Since the failure depends on timing it only happened sometimes before the bug fix
         *  using this test.
         */
        final MultiStatus status = new MultiStatus("lttn2.kernel.ui.swtbot.tests", IStatus.OK, null, null);
        IJobManager mgr = Job.getJobManager();
        JobChangeAdapter changeListener = new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                Job job = event.getJob();
                // Check for analysis failure
                String jobNamePrefix = NLS.bind(Messages.TmfAbstractAnalysisModule_RunningAnalysis, "");
                if ((job.getName().startsWith(jobNamePrefix)) && (job.getResult().getSeverity() == IStatus.ERROR)) {
                    status.add(job.getResult());
                }
            }
        };
        mgr.addJobChangeListener(changeListener);
        for (int i = 0; i < 10; i++) {
            SWTBotUtils.openTrace(TRACE_PROJECT_NAME, path, TRACE_TYPE, false);
            SWTBotUtils.openTrace(TRACE_PROJECT_NAME, path, TRACE_TYPE, false);
            SWTBotUtils.openTrace(TRACE_PROJECT_NAME, path, TRACE_TYPE, false);
            SWTBotUtils.openTrace(TRACE_PROJECT_NAME, path, TRACE_TYPE, false);
            SWTBotUtils.openTrace(TRACE_PROJECT_NAME, path, TRACE_TYPE, false);
            // Add little delay so that treads have a chance to start
            SWTBotUtils.delay(1000);
            workbenchbot.closeAllEditors();

            if (!status.isOK()) {
                SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, workbenchbot);
                fail(handleErrorStatus(status));
            }
        }
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, workbenchbot);
    }

    private static String handleErrorStatus(MultiStatus status) {

        // Build a string with all the children status messages, exception
        // messages and stack traces
        StringBuilder sb = new StringBuilder();
        for (IStatus childStatus : status.getChildren()) {
            StringBuilder childSb = new StringBuilder();
            if (!childStatus.getMessage().isEmpty()) {
                childSb.append(childStatus.getMessage() + '\n');
            }

            Throwable childException = childStatus.getException();
            if (childException != null) {
                String reason = childException.getMessage();
                // Some system exceptions have no message
                if (reason == null) {
                    reason = childException.toString();
                }

                String stackMessage = getExceptionStackMessage(childException);
                if (stackMessage == null) {
                    stackMessage = reason;
                }

                childSb.append(stackMessage);
            }

            if (childSb.length() > 0) {
                childSb.insert(0, '\n');
                sb.append(childSb.toString());
            }
        }
        return sb.toString();
    }

    private static String getExceptionStackMessage(Throwable exception) {
        String stackMessage = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        exception.printStackTrace(ps);
        ps.flush();
        try {
            baos.flush();
            stackMessage = baos.toString();
        } catch (IOException e) {
        }

        return stackMessage;
    }
}