/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.filter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfContentFieldAspect;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterEqualsNode;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the {@link TmfFilterEqualsNode} class.
 *
 * @author Patrick Tasse
 */
@SuppressWarnings("javadoc")
public class TmfFilterEqualsNodeTest extends TmfFilterTreeNodeTestBase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private ITmfEventField[] fFields1 = new ITmfEventField[] { new TmfEventField(FIELD, "value 1", null) };
    private ITmfEventField[] fFields2 = new ITmfEventField[] { new TmfEventField(FIELD, "value 2", null) };
    private ITmfEventField fContent1 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, fFields1);
    private ITmfEventField fContent2 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, fFields2);
    private TmfEvent fEvent1 = new TmfEvent(TRACE, 0, TmfTimestamp.fromNanos(1), EVENT_TYPE, fContent1);
    private TmfEvent fEvent2 = new TmfEvent(TRACE, 1, TmfTimestamp.fromNanos(2), EVENT_TYPE, fContent2);
    private TmfFilterEqualsNode fFilter;

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    @Before
    public void createFilter() {
        fFilter = new TmfFilterEqualsNode(null);
        fFilterNode = fFilter;
    }

    @Test
    public void testMatches() {
        fFilter.setEventAspect(new TmfContentFieldAspect(FIELD, FIELD));

        fFilter.setValue("value");
        assertFalse(fFilter.matches(fEvent1));
        assertFalse(fFilter.matches(fEvent2));

        fFilter.setValue("value 1");
        assertTrue(fFilter.matches(fEvent1));
        assertFalse(fFilter.matches(fEvent2));

        fFilter.setValue("VALUE 1");
        assertFalse(fFilter.matches(fEvent1));
        assertFalse(fFilter.matches(fEvent2));

        fFilter.setIgnoreCase(true);
        assertTrue(fFilter.matches(fEvent1));
        assertFalse(fFilter.matches(fEvent2));

        fFilter.setNot(true);
        assertFalse(fFilter.matches(fEvent1));
        assertTrue(fFilter.matches(fEvent2));
    }

    @Test
    public void testGetName() {
        assertEquals("getName()", "EQUALS", fFilter.getNodeName());
    }

    @Test
    public void testGetValidChildren() {
        assertArrayEquals("getValidChildren()", new String[] {}, fFilter.getValidChildren().toArray());
    }
}
