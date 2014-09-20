/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.internal.tmf.core.filter.TmfCollapseFilter;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.collapse.ITmfCollapsibleEvent;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.junit.Test;

/**
 * Test suite for the {@link TmfCollpaseFilter} class.
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings("javadoc")
public class TmfCollapseFilterTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private CollapsibleEvent fCollapsibleEvent1 = new CollapsibleEvent(true);
    private CollapsibleEvent fCollapsibleEvent2 = new CollapsibleEvent(true);
    private CollapsibleEvent fCollapsibleEvent3 = new CollapsibleEvent(false);
    private NonCollapsibleEvent fNonCollapsibleEvent1 = new NonCollapsibleEvent();
    private TmfCollapseFilter fFilter = new TmfCollapseFilter();

    // ------------------------------------------------------------------------
    // matches
    // ------------------------------------------------------------------------

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

    @Test(expected=UnsupportedOperationException.class)
    public void testGetChild() {
        fFilter.getChild(0);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testRemove() {
        fFilter.remove();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testRemoveChild() {
        fFilter.removeChild(null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testAddChild() {
        fFilter.addChild(null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testReplaceChild() {
        fFilter.replaceChild(0, null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testGetValidChildren() {
        fFilter.getValidChildren();
    }

    // ------------------------------------------------------------------------
    // Helper Classes
    // ------------------------------------------------------------------------

    private class CollapsibleEvent extends TmfEvent implements ITmfCollapsibleEvent {

        private final boolean fIsCollapsible;
        CollapsibleEvent(boolean isCollapsible) {
            super();
            fIsCollapsible = isCollapsible;
        }
        @Override
        public boolean isCollapsibleWith(ITmfEvent otherEvent) {
            return ((CollapsibleEvent)otherEvent).fIsCollapsible;
        }
    }

    private class NonCollapsibleEvent implements ITmfEvent {

        @Override
        public Object getAdapter(Class adapter) {
            return null;
        }
        @Override
        public ITmfTrace getTrace() {
            return null;
        }
        @Override
        public long getRank() {
            return 0;
        }
        @Override
        public ITmfTimestamp getTimestamp() {
            return new TmfNanoTimestamp(100);
        }
        @Override
        public String getSource() {
            return "";
        }
        @Override
        public ITmfEventType getType() {
            return new TmfEventType();
        }
        @Override
        public ITmfEventField getContent() {
            return new TmfEventField("testField", "test", null);
        }
        @Override
        public String getReference() {
            return "remote";
        }
    }
}
