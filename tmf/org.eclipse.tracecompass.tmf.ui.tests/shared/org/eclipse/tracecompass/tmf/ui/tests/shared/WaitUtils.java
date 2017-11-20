/*******************************************************************************
 * Copyright (c) 2016, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.shared;

import java.util.function.Predicate;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

/**
 * A utility class for methods related to waiting until certain conditions are met.
 */
public final class WaitUtils {

    private WaitUtils() {
    }

    private static final long SLEEP_INTERVAL_MS = 100;
    private static final long UI_THREAD_SLEEP_INTERVAL_MS = 10;
    private static final long DEFAULT_MAX_WAIT_TIME_MS = 300000;

    /**
     * Waits for all Eclipse jobs to finish.
     *
     * @throws WaitTimeoutException
     *             once the waiting time passes the maximum value
     */
    public static void waitForJobs() {
        waitUntil(new IWaitCondition() {
            @Override
            public boolean test() throws Exception {
                return Job.getJobManager().isIdle();
            }

            @Override
            public String getFailureMessage() {
                printJobs();
                return "Timed out waiting for jobs to finish.";
            }
        });
    }

    /**
     * Prints the state of all the jobs on stderr.
     */
    public static void printJobs() {
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

    /**
     * Waits for a certain condition to be met.
     *
     * @param condition
     *            the condition to be met
     *
     * @throws WaitTimeoutException
     *             once the waiting time passes the maximum value
     */
    public static void waitUntil(IWaitCondition condition) {
        waitUntil(condition, DEFAULT_MAX_WAIT_TIME_MS);
    }

    /**
     * Wait for a predicate to succeed.
     * <p>
     * Note: When testing with SWTBot, use SWTBotUtils.waitUntil() to use SWTBot's
     * timeout preference value.
     *
     * @param predicate
     *            The predicate
     * @param argument
     *            The argument used by the predicate for match
     * @param failureMessage
     *            The failure message
     * @throws WaitTimeoutException
     *             once the waiting time passes the maximum value
     */
    public static <E> void waitUntil(final Predicate<E> predicate, final E argument, final String failureMessage) {
        IWaitCondition condition = new IWaitCondition() {

            @Override
            public boolean test() throws Exception {
                return predicate.test(argument);
            }

            @Override
            public String getFailureMessage() {
                return failureMessage;
            }
        };
        waitUntil(condition);
    }

    /**
     * Waits for a certain condition to be met.
     *
     * @param condition
     *            the condition to be met
     * @param maxWait
     *            the maximum time to wait, in milliseconds. Once the waiting time
     *            passes the maximum value, a WaitTimeoutException is thrown
     * @throws WaitTimeoutException
     *             once the waiting time passes the maximum value
     */
    public static void waitUntil(IWaitCondition condition, long maxWait) {
        long waitStart = System.currentTimeMillis();
        Display display = Display.getCurrent();
        try {
            while (!condition.test()) {
                if (System.currentTimeMillis() - waitStart > maxWait) {
                    throw new WaitTimeoutException(condition.getFailureMessage()); //$NON-NLS-1$
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
        } catch (Exception e) {
            throw new WaitTimeoutException(condition.getFailureMessage() + "\n" + e);
        }
    }

    /**
     * Wait for a predicate to succeed.
     *
     * @param predicate
     *            The predicate
     * @param argument
     *            The argument used by the predicate for match
     * @param failureMessage
     *            The failure message
     * @param maxWait
     *            the maximum time to wait, in milliseconds. Once the waiting time
     *            passes the maximum value, a WaitTimeoutException is thrown
     * @throws WaitTimeoutException
     *             once the waiting time passes the maximum value
     */
    public static <E> void waitUntil(final Predicate<E> predicate, final E argument, final String failureMessage, long maxWait) {
        IWaitCondition condition = new IWaitCondition() {

            @Override
            public boolean test() throws Exception {
                return predicate.test(argument);
            }

            @Override
            public String getFailureMessage() {
                return failureMessage;
            }
        };
        waitUntil(condition, maxWait);
    }
}
