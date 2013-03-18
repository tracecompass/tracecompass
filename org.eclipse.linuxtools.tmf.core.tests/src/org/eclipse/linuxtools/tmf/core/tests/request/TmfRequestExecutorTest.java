/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.internal.tmf.core.component.TmfEventThread;
import org.eclipse.linuxtools.internal.tmf.core.request.TmfRequestExecutor;
import org.eclipse.linuxtools.tmf.core.component.TmfDataProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the TmfRequestExecutor class.
 */
public class TmfRequestExecutorTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private TmfRequestExecutor fExecutor;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Setup
     */
    @Before
    public void setUp() {
        fExecutor = new TmfRequestExecutor();
    }

    /**
     * Cleanup
     */
    @After
    public void tearDown() {
        fExecutor.stop();
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Test method for
     * {@link org.eclipse.linuxtools.internal.tmf.core.request.TmfRequestExecutor#TmfRequestExecutor()}
     */
    @Test
    public void testTmfRequestExecutor() {
        TmfRequestExecutor executor = new TmfRequestExecutor();
        assertFalse("isShutdown", executor.isShutdown());
        assertFalse("isTerminated", executor.isTerminated());
    }

    /**
     * Test method for
     * {@link org.eclipse.linuxtools.internal.tmf.core.request.TmfRequestExecutor#stop()}
     */
    @Test
    public void testStop() {
        TmfRequestExecutor executor = new TmfRequestExecutor();
        executor.stop();
        assertTrue("isShutdown", executor.isShutdown());
        assertTrue("isTerminated", executor.isTerminated());
    }

    // ------------------------------------------------------------------------
    // execute
    // ------------------------------------------------------------------------

    // Dummy context
    private static class MyContext implements ITmfContext {
        private long fNbRequested;
        private long fRank;

        public MyContext(long requested) {
            fNbRequested = requested;
            fRank = 0;
        }

        @Override
        public long getRank() {
            return (fRank <= fNbRequested) ? fRank : -1;
        }

        @Override
        public ITmfLocation getLocation() {
            return null;
        }

        @Override
        public boolean hasValidRank() {
            return true;
        }

        @Override
        public void setLocation(ITmfLocation location) {
        }

        @Override
        public void setRank(long rank) {
            fRank = rank;
        }

        @Override
        public void increaseRank() {
            fRank++;
        }

        @Override
        public void dispose() {
        }

        @Override
        public MyContext clone() {
            return this;
        }
    }

    // Dummy provider
    private static class MyProvider extends TmfDataProvider {
        private ITmfEvent fEvent = new TmfEvent();

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void dispose() {
        }

        @Override
        public void broadcast(TmfSignal signal) {
        }

        @Override
        public void sendRequest(ITmfDataRequest request) {
        }

        @Override
        public void fireRequest() {
        }

        @Override
        public void notifyPendingRequest(boolean isIncrement) {
        }

        @Override
        public ITmfEvent getNext(ITmfContext context) {
            context.increaseRank();
            return context.getRank() >= 0 ? fEvent : null;
        }

        @Override
        public ITmfContext armRequest(ITmfDataRequest request) {
            return new MyContext(request.getNbRequested());
        }
    }

    // Dummy request
    private static class MyRequest extends TmfDataRequest {
        public MyRequest(ExecutionType priority, int requested) {
            super(ITmfEvent.class, 0, requested, priority);
        }

        @Override
        public void done() {
            synchronized (monitor) {
                monitor.notifyAll();
            }
        }
    }

    // Dummy thread
    private static class MyThread extends TmfEventThread {
        public MyThread(TmfDataProvider provider, ITmfDataRequest request) {
            super(provider, request);
        }
    }

    private final static Object monitor = new Object();

    /**
     * Test method for
     * {@link org.eclipse.linuxtools.internal.tmf.core.request.TmfRequestExecutor#execute(java.lang.Runnable)}
     */
    @Test
    public void testExecute() {
        MyProvider provider = new MyProvider();
        MyRequest request1 = new MyRequest(ExecutionType.BACKGROUND, Integer.MAX_VALUE / 5);
        MyThread thread1 = new MyThread(provider, request1);
        MyRequest request2 = new MyRequest(ExecutionType.FOREGROUND, Integer.MAX_VALUE / 10);
        MyThread thread2 = new MyThread(provider, request2);
        MyRequest request3 = new MyRequest(ExecutionType.FOREGROUND, Integer.MAX_VALUE / 10);
        MyThread thread3 = new MyThread(provider, request3);

        // Start thread1
        fExecutor.execute(thread1);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        assertTrue("isRunning", thread1.isRunning());

        // Start higher priority thread2
        fExecutor.execute(thread2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        assertFalse("isRunning", thread1.isRunning());
        assertTrue("isRunning", thread2.isRunning());

        // Wait for end of thread2
        try {
            synchronized (monitor) {
                monitor.wait();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
        }
        assertTrue("isCompleted", thread2.isCompleted());
        assertTrue("isRunning", thread1.isRunning());

        // Start higher priority thread3
        fExecutor.execute(thread3);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        assertFalse("isRunning", thread1.isRunning());
        assertTrue("isRunning", thread3.isRunning());

        // Wait for end of thread3
        try {
            synchronized (monitor) {
                monitor.wait();
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
        }
        assertTrue("isCompleted", thread3.isCompleted());
        assertTrue("isRunning", thread1.isRunning());

        // Wait for thread1 completion
        try {
            synchronized (monitor) {
                monitor.wait();
            }
        } catch (InterruptedException e) {
        }
        assertTrue("isCompleted", thread1.isCompleted());
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    /**
     * Test method for
     * {@link org.eclipse.linuxtools.internal.tmf.core.request.TmfRequestExecutor#toString()}
     */
    @Test
    public void testToString() {
        TmfRequestExecutor executor = new TmfRequestExecutor();
        String expected = "[TmfRequestExecutor(ThreadPoolExecutor)]";
        assertEquals("toString", expected, executor.toString());
    }

}
