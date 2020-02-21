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
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterRootNode;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the {@link TmfFilterRootNode} class.
 *
 * @author Patrick Tasse
 */
@SuppressWarnings("javadoc")
public class TmfFilterRootNodeTest extends TmfFilterTreeNodeTestBase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private ITmfEventField fContent = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, null);
    private TmfEvent fEvent = new TmfEvent(TRACE, 0, TmfTimestamp.fromNanos(1), EVENT_TYPE, fContent);
    private TmfFilterRootNode fFilter;

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    @Before
    public void createFilter() {
        fFilter = new TmfFilterRootNode();
        fFilterNode = fFilter;
    }

    @Test
    public void testMatches() {
        fFilter.addChild(TRUE_NODE);
        fFilter.addChild(TRUE_NODE);
        assertTrue(fFilter.matches(fEvent));

        fFilter.replaceChild(0, FALSE_NODE);
        assertFalse(fFilter.matches(fEvent));
    }

    @Test
    public void testGetName() {
        assertEquals("getName()", "ROOT", fFilter.getNodeName());
    }

    @Test
    public void testGetValidChildren() {
        assertArrayEquals("getValidChildren()", new String[] { TmfFilterNode.NODE_NAME }, fFilter.getValidChildren().toArray());
    }
}
