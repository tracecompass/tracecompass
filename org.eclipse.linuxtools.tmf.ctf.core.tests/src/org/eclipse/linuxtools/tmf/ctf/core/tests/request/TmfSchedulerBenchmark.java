/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Simon Delisle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ctf.core.tests.request;

import java.io.PrintWriter;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.tests.shared.CtfTmfTestTrace;

/**
 * Benchmark for the request scheduler
 *
 * The benchmark has three tests. The first one is the latency (time between the
 * creation of the request and the beginning of its execution). The second one
 * is the average waiting time for a request. The last one is the total
 * completion time.
 */
public class TmfSchedulerBenchmark {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final int NUM_LOOPS = 10;
    private static final int NANOSECONDS_IN_MILLISECONDS = 1000000;
    private static final int NANOSECONDS_IN_SECONDS = 1000000000;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static CtfTmfTrace trace = CtfTmfTestTrace.KERNEL.getTrace();
    private static ForegroundRequest lastForegroundRequest = null;
    private static BackgroundRequest lastBackgroundRequest = null;

    private static PrintWriter pw = new PrintWriter(System.out, true);

    /**
     * Start the benchmark
     *
     * @param args
     *            The command-line arguments
     */
    public static void main(final String[] args) {
        trace.indexTrace(true);
        pw.println("---------- Benchmark started ----------");
        latencyBenchmark();
        averageWaitingTime();
        completedTime();
        benchmarkResults();
        trace.dispose();
    }

    private static void latencyBenchmark() {
        long averageLatency = 0;

        pw.println("----- Latency -----");
        for (int i = 0; i < NUM_LOOPS; i++) {
            try {
                ForegroundRequest foreground1 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                trace.sendRequest(foreground1);
                foreground1.waitForCompletion();
                averageLatency += foreground1.getLatency();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        pw.println((averageLatency / NUM_LOOPS) / NANOSECONDS_IN_MILLISECONDS + " ms");
    }

    private static void averageWaitingTime() {
        long averageWaitingBackground = 0;
        long averageWaitingForeground1 = 0;
        long averageWaitingForeground2 = 0;

        pw.println("----- Average waiting time with 3 requests -----");
        for (int i = 0; i < NUM_LOOPS; i++) {
            ForegroundRequest foreground1 = new ForegroundRequest(TmfTimeRange.ETERNITY);
            ForegroundRequest foreground2 = new ForegroundRequest(TmfTimeRange.ETERNITY);
            BackgroundRequest background1 = new BackgroundRequest(TmfTimeRange.ETERNITY);
            trace.sendRequest(background1);
            trace.sendRequest(foreground1);
            trace.sendRequest(foreground2);
            try {
                foreground1.waitForCompletion();
                foreground2.waitForCompletion();
                background1.waitForCompletion();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            averageWaitingBackground += background1.getAverageWaitingTime();
            averageWaitingForeground1 += foreground1.getAverageWaitingTime();
            averageWaitingForeground2 += foreground2.getAverageWaitingTime();
        }
        pw.print("-- Background : ");
        pw.println((averageWaitingBackground / NUM_LOOPS) / NANOSECONDS_IN_MILLISECONDS + " ms");

        pw.print("-- First foreground : ");
        pw.println((averageWaitingForeground1 / NUM_LOOPS) / NANOSECONDS_IN_MILLISECONDS + " ms");

        pw.print("-- Second foreground : ");
        pw.println((averageWaitingForeground2 / NUM_LOOPS) / NANOSECONDS_IN_MILLISECONDS + " ms");
    }

    private static void completedTime() {
        long averageCompletedTime1 = 0;
        long averageCompletedTime2 = 0;
        long averageCompletedTime3 = 0;
        long averageCompletedTime4 = 0;
        long averageCompletedTime5 = 0;
        long averageCompletedTime6 = 0;

        pw.println("----- Time to complete request -----");
        for (int i = 0; i < NUM_LOOPS; i++) {
            try {
                ForegroundRequest foreground1 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                trace.sendRequest(foreground1);
                foreground1.waitForCompletion();
                averageCompletedTime1 += foreground1.getCompletedTime();

                ForegroundRequest foreground2 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                ForegroundRequest foreground3 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                trace.sendRequest(foreground2);
                trace.sendRequest(foreground3);
                foreground2.waitForCompletion();
                foreground3.waitForCompletion();
                averageCompletedTime2 += (foreground2.getCompletedTime() + foreground3.getCompletedTime());

                ForegroundRequest foreground4 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                BackgroundRequest background1 = new BackgroundRequest(TmfTimeRange.ETERNITY);
                trace.sendRequest(foreground4);
                trace.sendRequest(background1);
                foreground4.waitForCompletion();
                background1.waitForCompletion();
                averageCompletedTime3 += (foreground4.getCompletedTime() + background1.getCompletedTime());

                ForegroundRequest foreground5 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                ForegroundRequest foreground6 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                BackgroundRequest background2 = new BackgroundRequest(TmfTimeRange.ETERNITY);
                trace.sendRequest(foreground5);
                trace.sendRequest(foreground6);
                trace.sendRequest(background2);
                foreground5.waitForCompletion();
                foreground6.waitForCompletion();
                background2.waitForCompletion();
                averageCompletedTime4 += (foreground5.getCompletedTime() + foreground6.getCompletedTime() + background2.getCompletedTime());

                ForegroundRequest foreground7 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                ForegroundRequest foreground8 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                ForegroundRequest foreground9 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                BackgroundRequest background3 = new BackgroundRequest(TmfTimeRange.ETERNITY);
                trace.sendRequest(foreground7);
                trace.sendRequest(foreground8);
                trace.sendRequest(foreground9);
                trace.sendRequest(background3);
                foreground7.waitForCompletion();
                foreground8.waitForCompletion();
                foreground9.waitForCompletion();
                background3.waitForCompletion();
                averageCompletedTime5 += (foreground7.getCompletedTime() + foreground8.getCompletedTime() + foreground9.getCompletedTime() + background3.getCompletedTime());

                ForegroundRequest foreground10 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                ForegroundRequest foreground11 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                ForegroundRequest foreground12 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                ForegroundRequest foreground13 = new ForegroundRequest(TmfTimeRange.ETERNITY);
                BackgroundRequest background4 = new BackgroundRequest(TmfTimeRange.ETERNITY);
                trace.sendRequest(foreground10);
                trace.sendRequest(foreground11);
                trace.sendRequest(foreground12);
                trace.sendRequest(foreground13);
                trace.sendRequest(background4);
                foreground10.waitForCompletion();
                foreground11.waitForCompletion();
                foreground12.waitForCompletion();
                foreground13.waitForCompletion();
                background4.waitForCompletion();
                averageCompletedTime6 += (foreground10.getCompletedTime() + foreground11.getCompletedTime() + foreground12.getCompletedTime() + foreground13.getCompletedTime() + background4.getCompletedTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        pw.print("-- Time to complete one request : ");
        pw.println((averageCompletedTime1 / NUM_LOOPS) / NANOSECONDS_IN_SECONDS + " s");

        pw.print("-- Time to complete 2 requests (2 foreground) : ");
        pw.println((averageCompletedTime2 / NUM_LOOPS) / NANOSECONDS_IN_SECONDS + " s");

        pw.print("-- Time to complete 2 requests (1 foreground, 1 background) : ");
        pw.println((averageCompletedTime3 / NUM_LOOPS) / NANOSECONDS_IN_SECONDS + " s");

        pw.print("-- Time to complete 3 requests (2 foreground, 1 background) : ");
        pw.println((averageCompletedTime4 / NUM_LOOPS) / NANOSECONDS_IN_SECONDS + " s");

        pw.print("-- Time to complete 4 requests (3 foreground, 1 background) : ");
        pw.println((averageCompletedTime5 / NUM_LOOPS) / NANOSECONDS_IN_SECONDS + " s");

        pw.print("-- Time to complete 5 requests (4 foreground, 1 background) : ");
        pw.println((averageCompletedTime6 / NUM_LOOPS) / NANOSECONDS_IN_SECONDS + " s");
    }

    /**
     * The benchmark results
     */
    public static void benchmarkResults() {
        pw.println("---------- Benchmark completed ----------");
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    private static class BackgroundRequest extends TmfEventRequest {
        private long startTime;
        private long endTimeLatency = -1;
        private long completedTime = 0;
        private long waitingTimeStart = 0;
        private long waitingTimeEnd = 0;
        private long waitingTime = 0;
        private int waitingCounter = 0;
        private boolean isWaiting = false;

        BackgroundRequest(TmfTimeRange timeRange) {
            super(trace.getEventType(),
                    timeRange,
                    0,
                    ITmfEventRequest.ALL_DATA,
                    ExecutionType.BACKGROUND);
            startTime = System.nanoTime();
        }

        @Override
        public void handleData(final ITmfEvent event) {
            if (endTimeLatency == -1) {
                endTimeLatency = System.nanoTime();
            }
            super.handleData(event);
            if (lastForegroundRequest == null && lastBackgroundRequest == null) {
                lastBackgroundRequest = this;
            }
            if (isWaiting) {
                waitingTimeEnd = System.nanoTime();
                waitingTime += waitingTimeEnd - waitingTimeStart;
                ++waitingCounter;
                isWaiting = false;
            }
            if (lastForegroundRequest != null) {
                lastForegroundRequest.waitingTimeStart = System.nanoTime();
                lastForegroundRequest.isWaiting = true;
                lastForegroundRequest = null;
                lastBackgroundRequest = this;
            }
            if (lastBackgroundRequest != this) {
                lastBackgroundRequest.waitingTimeStart = System.nanoTime();
                lastBackgroundRequest.isWaiting = true;
                lastBackgroundRequest = this;
            }
        }

        @Override
        public void handleCompleted() {
            completedTime = System.nanoTime();
            super.handleCompleted();
        }

        public long getCompletedTime() {
            return completedTime - startTime;
        }

        public long getAverageWaitingTime() {
            if (waitingCounter == 0) {
                return 0;
            }
            return waitingTime / waitingCounter;
        }
    }

    private static class ForegroundRequest extends TmfEventRequest {
        private long startTime = 0;
        private long endTimeLatency = -1;
        private long completedTime = 0;
        private long waitingTimeStart = 0;
        private long waitingTimeEnd = 0;
        private long waitingTime = 0;
        private int waitingCounter = 0;
        private boolean isWaiting = false;

        ForegroundRequest(TmfTimeRange timeRange) {
            super(trace.getEventType(),
                    timeRange,
                    0,
                    ITmfEventRequest.ALL_DATA,
                    ExecutionType.FOREGROUND);
            startTime = System.nanoTime();
        }

        @Override
        public void handleData(final ITmfEvent event) {
            if (endTimeLatency == -1) {
                endTimeLatency = System.nanoTime();
            }
            super.handleData(event);
            if (lastBackgroundRequest == null && lastForegroundRequest == null) {
                lastForegroundRequest = this;
            }
            if (isWaiting) {
                waitingTimeEnd = System.nanoTime();
                waitingTime += waitingTimeEnd - waitingTimeStart;
                ++waitingCounter;
                isWaiting = false;
            }
            if (lastBackgroundRequest != null) {
                lastBackgroundRequest.waitingTimeStart = System.nanoTime();
                lastBackgroundRequest.isWaiting = true;
                lastBackgroundRequest = null;
                lastForegroundRequest = this;
            }
            if (lastForegroundRequest != this) {
                lastForegroundRequest.waitingTimeStart = System.nanoTime();
                lastForegroundRequest.isWaiting = true;
                lastForegroundRequest = this;
            }
        }

        @Override
        public void handleCompleted() {
            completedTime = System.nanoTime();
            super.handleCompleted();
        }

        public long getLatency() {
            return endTimeLatency - startTime;
        }

        public long getCompletedTime() {
            return completedTime - startTime;
        }

        public long getAverageWaitingTime() {
            if (waitingCounter == 0) {
                return 0;
            }
            return waitingTime / waitingCounter;
        }
    }
}
