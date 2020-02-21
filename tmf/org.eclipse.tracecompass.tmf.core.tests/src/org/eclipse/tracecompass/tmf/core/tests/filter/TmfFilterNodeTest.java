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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterAndNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterCompareNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterContainsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterEqualsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterOrNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterTraceTypeNode;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the {@link TmfFilterNode} class.
 *
 * @author Patrick Tasse
 */
@SuppressWarnings("javadoc")
public class TmfFilterNodeTest extends TmfFilterTreeNodeTestBase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private static final String FILTER = "filter";
    private ITmfEventField fContent = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, null);
    private TmfEvent fEvent = new TmfEvent(TRACE, 0, TmfTimestamp.fromNanos(1), EVENT_TYPE, fContent);
    private TmfFilterNode fFilter;

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    @Before
    public void createFilter() {
        fFilter = new TmfFilterNode(null, FILTER);
        fFilterNode = fFilter;
    }

    @Test
    public void testMatches() {
        fFilter.addChild(TRUE_NODE);
        assertTrue(fFilter.matches(fEvent));

        fFilter.replaceChild(0, FALSE_NODE);
        assertFalse(fFilter.matches(fEvent));
    }

    @Test
    public void testGetFilterName() {
        assertEquals("getFilterName()", FILTER, fFilter.getFilterName());
    }

    @Test
    public void testGetName() {
        assertEquals("getName()", "FILTER", fFilter.getNodeName());
    }

    @Test
    public void testGetValidChildren() {
        Set<String> validChildren = new HashSet<>(Arrays.asList(
                TmfFilterTraceTypeNode.NODE_NAME,
                TmfFilterAndNode.NODE_NAME,
                TmfFilterOrNode.NODE_NAME,
                TmfFilterContainsNode.NODE_NAME,
                TmfFilterEqualsNode.NODE_NAME,
                TmfFilterMatchesNode.NODE_NAME,
                TmfFilterCompareNode.NODE_NAME));
        assertEquals("getValidChildren()", validChildren, new HashSet<>(fFilter.getValidChildren()));
    }
}
