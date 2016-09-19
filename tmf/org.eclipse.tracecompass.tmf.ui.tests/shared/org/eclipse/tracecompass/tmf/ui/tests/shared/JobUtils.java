/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.tests.shared;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

/**
 * A utility class for Job related things.
 */
public final class JobUtils {

    private JobUtils() {
    }

    private static final long SLEEP_INTERVAL_MS = 100;
    private static final long UI_THREAD_SLEEP_INTERVAL_MS = 10;
    private static final long DEFAULT_MAX_JOBS_WAIT_TIME_MS = 300000;

    /**
     * Waits for all Eclipse jobs to finish. Times out after
     * RuntimeUtils#MAX_JOBS_WAIT_TIME by default.
     *
     * @throws RuntimeException
     *             once the waiting time passes the default maximum value
     */
    public static void waitForJobs() {
        waitForJobs(DEFAULT_MAX_JOBS_WAIT_TIME_MS);
    }

    /**
     * Waits for all Eclipse jobs to finish
     *
     * @param maxWait
     *            the maximum time to wait, in milliseconds. Once the waiting
     *            time passes the maximum value, a TimeoutException is thrown
     * @throws RuntimeException
     *             once the waiting time passes the maximum value
     */
    public static void waitForJobs(long maxWait) {
        long waitStart = System.currentTimeMillis();
        Display display = Display.getCurrent();
        while (!Job.getJobManager().isIdle()) {
            if (System.currentTimeMillis() - waitStart > maxWait) {
                printJobs();
                throw new RuntimeException("Timed out waiting for jobs to finish."); //$NON-NLS-1$
            }

            if (display != null) {
                if (!display.readAndDispatch()) {
                    // We do not use Display.sleep because it might never wake up
                    // if there is no user interaction
                    try {
                        Thread.sleep(UI_THREAD_SLEEP_INTERVAL_MS);
                    } catch (final InterruptedException e) {
                        // Ignored
                    }
                }
                display.update();
            } else {
                try {
                    Thread.sleep(SLEEP_INTERVAL_MS);
                } catch (final InterruptedException e) {
                    // Ignored
                }
            }
        }
    }

    private static void printJobs() {
        Job[] jobs = Job.getJobManager().find(null);
        for (Job job : jobs) {
            System.err.println(job.toString() + " state: " + jobStateToString(job.getState())); //$NON-NLS-1$
            Thread thread = job.getThread();
            if (thread != null) {
                for (StackTraceElement stractTraceElement : thread.getStackTrace()) {
                    System.err.println("  " + stractTraceElement); //$NON-NLS-1$
                }
            }
            System.err.println();
        }
    }

    private static String jobStateToString(int jobState) {
        switch (jobState) {
        case Job.RUNNING:
            return "RUNNING"; //$NON-NLS-1$
        case Job.WAITING:
            return "WAITING"; //$NON-NLS-1$
        case Job.SLEEPING:
            return "SLEEPING"; //$NON-NLS-1$
        case Job.NONE:
            return "NONE"; //$NON-NLS-1$
        default:
            return "UNKNOWN"; //$NON-NLS-1$
        }
    }
}
