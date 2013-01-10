/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.request;

import junit.framework.TestCase;

import org.eclipse.linuxtools.internal.tmf.core.component.TmfEventThread;
import org.eclipse.linuxtools.internal.tmf.core.request.TmfRequestExecutor;
import org.eclipse.linuxtools.tmf.core.component.TmfDataProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.ITmfRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;

/**
 * Test suite for the TmfRequestExecutor class.
 */
@SuppressWarnings("nls")
public class TmfRequestExecutorTest extends TestCase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private TmfRequestExecutor fExecutor;

	// ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

	/**
	 * @param name the test name
	 */
	public TmfRequestExecutorTest(String name) {
		super(name);
	}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fExecutor = new TmfRequestExecutor();

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        fExecutor.stop();
    }

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * Test method for {@link org.eclipse.linuxtools.internal.tmf.core.request.TmfRequestExecutor#TmfRequestExecutor()}.
	 */
	public void testTmfRequestExecutor() {
		TmfRequestExecutor executor = new TmfRequestExecutor();
		assertFalse("isShutdown",   executor.isShutdown());
		assertFalse("isTerminated", executor.isTerminated());
	}

	/**
	 * Test method for {@link org.eclipse.linuxtools.internal.tmf.core.request.TmfRequestExecutor#stop()}.
	 */
	public void testStop() {
		TmfRequestExecutor executor = new TmfRequestExecutor();
		executor.stop();
		assertTrue("isShutdown",   executor.isShutdown());
		assertTrue("isTerminated", executor.isTerminated());
	}

	// ------------------------------------------------------------------------
	// execute with pre-emption
	// ------------------------------------------------------------------------

	// Dummy context
	private static class MyContext implements ITmfContext, Cloneable {
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
        public void sendRequest(ITmfRequest request) {
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
        public ITmfContext armRequest(ITmfRequest request) {
            return new MyContext(request.getNbRequested());
        }
	}

	// Dummy request
    private static class MyRequest extends TmfRequest {
        private final Object monitor;
        public MyRequest(TmfRequestPriority priority, int requested, Object mon) {
            super(TmfTimeRange.ETERNITY, 0, requested, priority);
            monitor = mon;
        }
        @Override
        public synchronized void done() {
            super.done();
            synchronized (monitor) {
                monitor.notifyAll();
            }
        }
        @Override
        public synchronized void handleStarted() {
            super.handleStarted();
            synchronized (monitor) {
                monitor.notifyAll();
            }
        }
    }

    // Dummy thread
    private static class MyThread extends TmfEventThread {
        private final Object monitor;
        public MyThread(TmfDataProvider provider, ITmfRequest request, Object mon) {
            super(provider, request);
            monitor = mon;
        }
        @Override
        public synchronized void resume() {
            super.resume();
            synchronized (monitor) {
                monitor.notifyAll();
            }
        }
    }

    private final static Object reqmon1 = new Object();
    private final static Object reqmon2 = new Object();
    private final static Object reqmon3 = new Object();

    private final static Object thrmon1 = new Object();
    private final static Object thrmon2 = new Object();
    private final static Object thrmon3 = new Object();

    /**
	 * Test method for {@link org.eclipse.linuxtools.internal.tmf.core.request.TmfRequestExecutor#execute(java.lang.Runnable)}.
	 */
	public void testExecute() {
	    final long TIMEOUT = 100;
	    final long ONE_MINUTE = 60 * 1000;

        MyProvider provider = new MyProvider();
        MyRequest  request1 = new MyRequest(ITmfRequest.TmfRequestPriority.NORMAL, Integer.MAX_VALUE /  10, reqmon1);
        MyThread   thread1  = new MyThread(provider, request1, thrmon1);
        MyRequest  request2 = new MyRequest(ITmfRequest.TmfRequestPriority.HIGH,   Integer.MAX_VALUE / 100, reqmon2);
        MyThread   thread2  = new MyThread(provider, request2, thrmon2);
        MyRequest  request3 = new MyRequest(ITmfRequest.TmfRequestPriority.HIGH,   Integer.MAX_VALUE / 100, reqmon3);
        MyThread   thread3  = new MyThread(provider, request3, thrmon3);

        // Start thread1
        synchronized (reqmon1) {
            try {
                fExecutor.execute(thread1);
                reqmon1.wait(ONE_MINUTE);
                assertTrue("isRunning", thread1.isRunning());
            } catch (InterruptedException e) {
            }
        }

        // Start higher priority thread2
        synchronized (reqmon2) {
            try {
                fExecutor.execute(thread2);
                reqmon2.wait(ONE_MINUTE);
                assertFalse("isRunning", thread1.isRunning());
                assertTrue("isRunning", thread2.isRunning());
            } catch (InterruptedException e) {
            }
        }

        // Wait for end of thread2
        try {
            synchronized (reqmon2) {
                reqmon2.wait(ONE_MINUTE);
                assertTrue("isCompleted", thread2.isCompleted());
            }
            synchronized (thrmon1) {
                thrmon1.wait(TIMEOUT); // Use a timeout in case we already missed the signal
                assertTrue("isRunning", thread1.isRunning());
            }
        } catch (InterruptedException e) {
        }

        // Start higher priority thread3
        synchronized (reqmon3) {
            try {
                fExecutor.execute(thread3);
                reqmon3.wait(ONE_MINUTE);
                assertFalse("isRunning", thread1.isRunning());
                assertTrue("isRunning", thread3.isRunning());
            } catch (InterruptedException e) {
            }
        }

        // Wait for end of thread3
        try {
            synchronized (reqmon3) {
                reqmon3.wait(ONE_MINUTE);
                assertTrue("isCompleted", thread3.isCompleted());
            }
            synchronized (thrmon1) {
                thrmon1.wait(TIMEOUT); // Use a timeout in case we already missed the signal
                assertTrue("isRunning", thread1.isRunning());
            }
        } catch (InterruptedException e) {
        }

        // Wait for thread1 completion
        try {
            synchronized (reqmon1) {
                reqmon1.wait(ONE_MINUTE);
                assertTrue("isCompleted", thread1.isCompleted());
            }
        } catch (InterruptedException e) {
        }
    }

	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

	/**
	 * Test method for {@link org.eclipse.linuxtools.internal.tmf.core.request.TmfRequestExecutor#toString()}.
	 */
	public void testToString() {
        TmfRequestExecutor executor = new TmfRequestExecutor();
        String expected = "[TmfRequestExecutor(ThreadPoolExecutor)]";
        assertEquals("toString", expected, executor.toString());
	}

}
