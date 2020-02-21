/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.filter.TmfCollapseFilter;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.event.collapse.ITmfCollapsibleEvent;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.After;
import org.junit.Test;

/**
 * Test suite for the {@link TmfCollpaseFilter} class.
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings("javadoc")
public class TmfCollapseFilterTest {

    private static final TmfTestTrace STUB_TRACE = TmfTestTrace.A_TEST_10K;

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private CollapsibleEvent fCollapsibleEvent1 = new CollapsibleEvent(true);
    private CollapsibleEvent fCollapsibleEvent2 = new CollapsibleEvent(true);
    private CollapsibleEvent fCollapsibleEvent3 = new CollapsibleEvent(false);
    private NonCollapsibleEvent fNonCollapsibleEvent1 = new NonCollapsibleEvent();
    private TmfCollapseFilter fFilter = new TmfCollapseFilter();
    private @NonNull ITmfTrace fTrace = STUB_TRACE.getTrace();

    // ------------------------------------------------------------------------
    // matches
    // ------------------------------------------------------------------------

    @After
    public void disposeTrace() {
        fTrace.dispose();
    }

    @Test
    public void testMatches() {

        TmfCollapseFilter filter = new TmfCollapseFilter();

        assertTrue(filter.matches(fCollapsibleEvent1));
        assertFalse(filter.matches(fCollapsibleEvent2));
        assertFalse(filter.matches(fCollapsibleEvent1));
        assertFalse(filter.matches(fCollapsibleEvent2));
        assertTrue(filter.matches(fNonCollapsibleEvent1));
        assertTrue(filter.matches(fNonCollapsibleEvent1));
        assertTrue(filter.matches(fCollapsibleEvent1));
        assertFalse(filter.matches(fCollapsibleEvent2));
        assertTrue(filter.matches(fCollapsibleEvent3));
    }

    @Test
    public void testInterfaces() {
        assertNull("getParent()", fFilter.getParent());
        assertEquals("getName()", "Collapse", fFilter.getNodeName());
        assertEquals("hasChildren()", false, fFilter.hasChildren());
        assertEquals("getChildrenCount()", 0, fFilter.getChildrenCount());
        assertEquals("getChildren()", 0, fFilter.getChildren().length);
    }

    @Test
    public void testClone() {
        assertNotEquals("clone()", fFilter, fFilter.clone());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetChild() {
        fFilter.getChild(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        fFilter.remove();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveChild() {
        fFilter.removeChild(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddChild() {
        fFilter.addChild(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testReplaceChild() {
        fFilter.replaceChild(0, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValidChildren() {
        fFilter.getValidChildren();
    }

    // ------------------------------------------------------------------------
    // Helper Classes
    // ------------------------------------------------------------------------

    private class CollapsibleEvent extends TmfEvent implements ITmfCollapsibleEvent {

        private final boolean fIsCollapsible;

        CollapsibleEvent(boolean isCollapsible) {
            super(fTrace, ITmfContext.UNKNOWN_RANK, null, null, null);
            fIsCollapsible = isCollapsible;
        }

        @Override
        public boolean isCollapsibleWith(ITmfEvent otherEvent) {
            return ((CollapsibleEvent) otherEvent).fIsCollapsible;
        }
    }

    private class NonCollapsibleEvent extends PlatformObject implements ITmfEvent {

        @Override
        public ITmfTrace getTrace() {
            return fTrace;
        }

        @Override
        public long getRank() {
            return 0;
        }

        @Override
        public ITmfTimestamp getTimestamp() {
            return TmfTimestamp.fromNanos(100);
        }

        @Override
        public ITmfEventType getType() {
            return new TmfEventType();
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public ITmfEventField getContent() {
            return new TmfEventField("testField", "test", null);
        }
    }
}
