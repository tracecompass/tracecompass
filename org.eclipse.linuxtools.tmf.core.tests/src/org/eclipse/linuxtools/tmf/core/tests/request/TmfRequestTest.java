/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.request.ITmfRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfRequest.TmfRequestPriority;
import org.eclipse.linuxtools.tmf.core.request.ITmfRequest.TmfRequestState;
import org.eclipse.linuxtools.tmf.core.request.TmfBlockFilter;
import org.eclipse.linuxtools.tmf.core.request.TmfRangeFilter;
import org.eclipse.linuxtools.tmf.tests.stubs.request.TmfRequestStub;
import org.junit.Test;

/**
 * <b><u>TmfRequestTest</u></b>
 * <p>
 * Test suite for the TmfRequest class.
 */
@SuppressWarnings({"nls","javadoc"})
public class TmfRequestTest extends TestCase {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final TmfRequestPriority NORMAL = TmfRequestPriority.NORMAL;
    private static final TmfRequestPriority HIGH = TmfRequestPriority.HIGH;

    private static final long ALL_EVENTS = ITmfRequest.ALL_EVENTS;

    private static final TmfTimeRange ETERNITY = TmfTimeRange.ETERNITY;
    private static final TmfTimeRange EPOCH = new TmfTimeRange(TmfTimestamp.ZERO, TmfTimestamp.BIG_BANG);

    private static final TmfRequestState PENDING = TmfRequestState.PENDING;
    private static final TmfRequestState RUNNING = TmfRequestState.RUNNING;
    private static final TmfRequestState COMPLETED = TmfRequestState.COMPLETED;

    // ------------------------------------------------------------------------
	// Variables
	// ------------------------------------------------------------------------

	private ITmfRequest fRequest1;
    private ITmfRequest fRequest1b;
    private ITmfRequest fRequest1c;

    private ITmfRequest fRequest2;

	private static int fLastRequestId;

    // ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

    /**
     * @param name the test name
     */
	public TmfRequestTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	    fRequest1  = new TmfRequestStub();
	    fRequest1b = new TmfRequestStub();
	    fRequest1c = new TmfRequestStub();
	    fRequest2 = new TmfRequestStub(HIGH);
	    fLastRequestId = fRequest2.getRequestId();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    private static ITmfRequest setupDummyRequest(final boolean[] flags) {

        ITmfRequest request = new TmfRequestStub(10, 100) {
            @Override
            public synchronized void handleCompleted() {
                super.handleCompleted();
                flags[0] = true;
            }

            @Override
            public void handleSuccess() {
                super.handleSuccess();
                flags[1] = true;
            }

            @Override
            public void handleFailure() {
                super.handleFailure();
                flags[2] = true;
            }

            @Override
            public void handleCancel() {
                super.handleCancel();
                flags[3] = true;
            }
        };
        fLastRequestId = request.getRequestId();
        return request;
    }

    // ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	@Test
	public void testTmfRequest() {
        ITmfRequest request = new TmfRequestStub();
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());
        assertEquals("getEventFilters", TmfBlockFilter.ALL_EVENTS, request.getEventFilter(TmfBlockFilter.class));
        assertEquals("getEventFilters", TmfRangeFilter.ALL_EVENTS, request.getEventFilter(TmfRangeFilter.class));
        assertNull("getParent", request.getParent());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());
	}

    @Test
    public void testTmfRequestPriority() {

        // 1. Normal priority
        ITmfRequest request = new TmfRequestStub(TmfRequestPriority.NORMAL);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        // 2. High priority
        request = new TmfRequestStub(TmfRequestPriority.HIGH);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", HIGH, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());
    }

    @Test
    public void testTmfRequestTimeRange() {

        // 1. Eternity
        ITmfRequest request = new TmfRequestStub(ETERNITY);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        // 2. Since the epoch
        request = new TmfRequestStub(EPOCH);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", EPOCH, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());
    }

    @Test
    public void testTmfRequestBlock() {

        // 1. All events
        ITmfRequest request = new TmfRequestStub(0, ALL_EVENTS);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        // 2. For an event count
        long nbRequested = 10000;
        request = new TmfRequestStub(0, nbRequested);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", nbRequested, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        // 3. From a given index
        long index = 100;
        request = new TmfRequestStub(index, ALL_EVENTS);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", index, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        // 4. From a given index, for an event count
        request = new TmfRequestStub(index, nbRequested);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", nbRequested, request.getNbRequested());
        assertEquals("getStartIndex", index, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());
    }

    @Test
    public void testTmfRequestRangeAndBlock() {

        // 1. All events since beginning of time
        ITmfRequest request = new TmfRequestStub(ETERNITY, 0, ALL_EVENTS);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        // 2. All events since the epoch
        request = new TmfRequestStub(EPOCH, 0, ALL_EVENTS);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", EPOCH, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        // 3. A block of events since the beginning of time
        long nbRequested = 10000;
        request = new TmfRequestStub(ETERNITY, 0, nbRequested);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", nbRequested, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        // 4. All events from a given index since the beginning of time
        long index = 100;
        request = new TmfRequestStub(ETERNITY, index, ALL_EVENTS);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", index, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        // 4. Some events from a given index since the epoch
        request = new TmfRequestStub(EPOCH, index, nbRequested);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", EPOCH, request.getTimeRange());
        assertEquals("getNbRequested", nbRequested, request.getNbRequested());
        assertEquals("getStartIndex", index, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());
    }

    @Test
    public void testTmfRequestRangeAndBlockWithPriority() {

        // 1. All events since beginning of time
        ITmfRequest request = new TmfRequestStub(ETERNITY, 0, ALL_EVENTS, NORMAL);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        // 2. All events since beginning of time, high priority
        request = new TmfRequestStub(ETERNITY, 0, ALL_EVENTS, HIGH);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", HIGH, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        // 3. A block of events since the beginning of time
        long nbRequested = 10000;
        long index = 100;
        request = new TmfRequestStub(EPOCH, index, nbRequested, NORMAL);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", EPOCH, request.getTimeRange());
        assertEquals("getNbRequested", nbRequested, request.getNbRequested());
        assertEquals("getStartIndex", index, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        // 4. A block of events since the beginning of time, high priority
        request = new TmfRequestStub(EPOCH, index, nbRequested, HIGH);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", HIGH, request.getRequestPriority());
        assertEquals("getTimeRange", EPOCH, request.getTimeRange());
        assertEquals("getNbRequested", nbRequested, request.getNbRequested());
        assertEquals("getStartIndex", index, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());
    }

    @Test
    public void testTmfRequestCopy() {
        TmfRequestStub other = new TmfRequestStub();
        ITmfRequest request = new TmfRequestStub(other);
        fLastRequestId += 2;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());
    }

    // ------------------------------------------------------------------------
	// equals
	// ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        assertEquals("equals", fRequest1, fRequest1);
        assertEquals("equals", fRequest2, fRequest2);

        assertFalse("equals", fRequest1.equals(fRequest2));
        assertFalse("equals", fRequest2.equals(fRequest1));
    }

    @Test
    public void testEqualsSymmetry() {
        assertEquals("equals", fRequest1, fRequest1b);
        assertEquals("equals", fRequest1b, fRequest1);

        assertFalse("equals", fRequest1.equals(fRequest2));
        assertFalse("equals", fRequest2.equals(fRequest1));
    }

    @Test
    public void testEqualsTransivity() {
        assertEquals("equals", fRequest1, fRequest1b);
        assertEquals("equals", fRequest1b, fRequest1c);
        assertEquals("equals", fRequest1c, fRequest1);
    }

    @Test
    public void testEqualsNull() {
        assertFalse("equals", fRequest1.equals(null));
        assertFalse("equals", fRequest2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        assertTrue("hashCode", fRequest1.hashCode() == fRequest1.hashCode());
        assertTrue("hashCode", fRequest2.hashCode() == fRequest2.hashCode());
        assertTrue("hashCode", fRequest1.hashCode() != fRequest2.hashCode());
    }

	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

    @Test
    public void testToString() {
        String expected1 = "TmfRequest [fRequestId=" + fRequest1.getRequestId() + "]";
        String expected2 = "TmfRequest [fRequestId=" + fRequest2.getRequestId() + "]";

        assertEquals("toString", expected1, fRequest1.toString());
        assertEquals("toString", expected2, fRequest2.toString());
    }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

    @Test
    public void testSetTimeRange() {

        TmfRequestStub request = new TmfRequestStub(ETERNITY);
        request.setTimeRange(EPOCH);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", EPOCH, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());
    }

    @Test
    public void testSetNbRequested() {
        long nbRequested = 10000;

        TmfRequestStub request = new TmfRequestStub(ETERNITY, 0, ALL_EVENTS);
        request.setNbRequested(nbRequested);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", nbRequested, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());
    }

    @Test
    public void testSetStartIndex() {
        long index = 100;

        TmfRequestStub request = new TmfRequestStub(ETERNITY, 0, ALL_EVENTS);
        request.setStartIndex(index);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", index, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());
    }

    // ------------------------------------------------------------------------
    // setEventFilters, addEventFilter
    // ------------------------------------------------------------------------

    @Test
    public void testSetEventFilters() {
        TmfRequestStub request = new TmfRequestStub();
        Collection<ITmfFilter> filters = new ArrayList<ITmfFilter>();
        request.setEventFilters(filters);
        assertEquals("setEventFilters", TmfBlockFilter.ALL_EVENTS, request.getEventFilter(TmfBlockFilter.class));
        assertEquals("setEventFilters", TmfRangeFilter.ALL_EVENTS, request.getEventFilter(TmfRangeFilter.class));

        TmfBlockFilter blockFilter = new TmfBlockFilter(10, 1000);
        filters.add(blockFilter);
        request.setEventFilters(filters);
        assertEquals("setEventFilters", blockFilter, request.getEventFilter(TmfBlockFilter.class));
        assertEquals("setEventFilters", TmfRangeFilter.ALL_EVENTS, request.getEventFilter(TmfRangeFilter.class));

        TmfRangeFilter rangeFilter = new TmfRangeFilter(EPOCH);
        filters.add(rangeFilter);
        request.setEventFilters(filters);
        assertEquals("setEventFilters", blockFilter, request.getEventFilter(TmfBlockFilter.class));
        assertEquals("setEventFilters", rangeFilter, request.getEventFilter(TmfRangeFilter.class));
    }

    @Test
    public void testAddEventFilters() {
        TmfRequestStub request = new TmfRequestStub();
        Collection<ITmfFilter> filters = new ArrayList<ITmfFilter>();
        request.setEventFilters(filters);
        assertEquals("setEventFilters", TmfBlockFilter.ALL_EVENTS, request.getEventFilter(TmfBlockFilter.class));
        assertEquals("setEventFilters", TmfRangeFilter.ALL_EVENTS, request.getEventFilter(TmfRangeFilter.class));

        TmfBlockFilter blockFilter = new TmfBlockFilter(10, 1000);
        request.addEventFilter(blockFilter);
        assertEquals("setEventFilters", blockFilter, request.getEventFilter(TmfBlockFilter.class));
        assertEquals("setEventFilters", TmfRangeFilter.ALL_EVENTS, request.getEventFilter(TmfRangeFilter.class));

        TmfRangeFilter rangeFilter = new TmfRangeFilter(EPOCH);
        request.addEventFilter(rangeFilter);
        assertEquals("setEventFilters", blockFilter, request.getEventFilter(TmfBlockFilter.class));
        assertEquals("setEventFilters", rangeFilter, request.getEventFilter(TmfRangeFilter.class));
    }

    // ------------------------------------------------------------------------
    // setParent, notifyParent
    // ------------------------------------------------------------------------

    @Test
    public void testSetParent() {
        TmfRequestStub request1 = new TmfRequestStub();
        TmfRequestStub request2 = new TmfRequestStub();

        assertNull("getParent", request2.getParent());
        request2.setParent(request1);
        assertEquals("getParent", request1, request2.getParent());
        request2.setParent(null);
        assertNull("getParent", request2.getParent());
    }

    @Test
    public void testNotifyParent() {
        final Boolean[] notifications = new Boolean[2];
        notifications[0] = notifications[1] = false;

        TmfRequestStub request1 = new TmfRequestStub() {
            @Override
            public void notifyParent(ITmfRequest child) {
                notifications[0] = true;
                super.notifyParent(this);
            }
        };
        TmfRequestStub request2 = new TmfRequestStub() {
            @Override
            public void notifyParent(ITmfRequest child) {
                notifications[1] = true;
                super.notifyParent(this);
            }
        };

        request2.setParent(request1);
        assertFalse("notifyParent", notifications[0]);
        assertFalse("notifyParent", notifications[1]);

        request2.notifyParent(null);
        assertTrue("notifyParent", notifications[0]);
        assertTrue("notifyParent", notifications[1]);
    }

    // ------------------------------------------------------------------------
    // start
    // ------------------------------------------------------------------------

    @Test
    public void testStart() {

        final boolean[] flags = new boolean[4];
        ITmfRequest request = setupDummyRequest(flags);
        request.start();

        assertTrue("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());
        assertEquals("getState", RUNNING, request.getState());

        assertNull("getStatus", request.getStatus());

        assertFalse("handleCompleted", flags[0]);
        assertFalse("handleSuccess",   flags[1]);
        assertFalse("handleFailure",   flags[2]);
        assertFalse("handleCancel",    flags[3]);
    }

	// ------------------------------------------------------------------------
	// done
	// ------------------------------------------------------------------------

    @Test
    public void testDone() {

        final boolean[] flags = new boolean[4];
        ITmfRequest request = setupDummyRequest(flags);
        request.done();

        assertFalse("isRunning", request.isRunning());
        assertTrue("isCompleted", request.isCompleted());
        assertEquals("getState", COMPLETED, request.getState());

        assertEquals("getStatus", IStatus.OK, request.getStatus().getSeverity());
        assertTrue("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertTrue("handleCompleted", flags[0]);
        assertTrue("handleSuccess",   flags[1]);
        assertFalse("handleFailure",  flags[2]);
        assertFalse("handleCancel",   flags[3]);
    }

	// ------------------------------------------------------------------------
	// fail
	// ------------------------------------------------------------------------

    @Test
    public void testFail() {

        final boolean[] flags = new boolean[4];
        ITmfRequest request = setupDummyRequest(flags);
        request.fail();

        assertFalse("isRunning", request.isRunning());
        assertTrue("isCompleted", request.isCompleted());
        assertEquals("getState", COMPLETED, request.getState());

        assertEquals("getStatus", IStatus.ERROR, request.getStatus().getSeverity());
        assertFalse("isOK", request.isOK());
        assertTrue("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertTrue("handleCompleted", flags[0]);
        assertFalse("handleSuccess",  flags[1]);
        assertTrue("handleFailure",   flags[2]);
        assertFalse("handleCancel",   flags[3]);
    }

	// ------------------------------------------------------------------------
	// cancel
	// ------------------------------------------------------------------------

    @Test
    public void testCancel() {

        final boolean[] flags = new boolean[4];
        ITmfRequest request = setupDummyRequest(flags);
        request.cancel();

        assertFalse("isRunning", request.isRunning());
        assertTrue("isCompleted", request.isCompleted());
        assertEquals("getState", COMPLETED, request.getState());

        assertEquals("getStatus", IStatus.CANCEL, request.getStatus().getSeverity());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertTrue("isCancelled", request.isCancelled());

        assertTrue("handleCompleted", flags[0]);
        assertFalse("handleSuccess",  flags[1]);
        assertFalse("handleFailure",  flags[2]);
        assertTrue("handleCancel",    flags[3]);
    }

}
