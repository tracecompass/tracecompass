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
import static org.junit.Assert.assertNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * Test suite for the {@link TmfFilterTreeNodeTestBase} class.
 *
 * @author Patrick Tasse
 */
@SuppressWarnings("javadoc")
public abstract class TmfFilterTreeNodeTestBase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    protected static final @NonNull ITmfTrace TRACE = new TmfTraceStub();
    protected static final @NonNull String FIELD = "field";
    protected static final ITmfFilterTreeNode TRUE_NODE = new TmfFilterNode(null) {
        @Override
        public boolean matches(ITmfEvent event) {
            return true;
        }
    };
    protected static final ITmfFilterTreeNode FALSE_NODE = new TmfFilterNode(null) {
        @Override
        public boolean matches(ITmfEvent event) {
            return false;
        }
    };
    protected static final ITmfEventType EVENT_TYPE = new TmfEventType("Type", TmfEventField.makeRoot(new String[] { FIELD }));
    protected ITmfFilterTreeNode fFilterNode;

    @AfterClass
    public static void disposeTrace() {
        TRACE.dispose();
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    @Test
    public void testDefaults() {
        assertNull("getParent()", fFilterNode.getParent());
        assertEquals("hasChildren()", false, fFilterNode.hasChildren());
        assertEquals("getChildrenCount()", 0, fFilterNode.getChildrenCount());
        assertEquals("getChildren()", 0, fFilterNode.getChildren().length);
    }

    @Test
    public void testClone() {
        ITmfFilterTreeNode clone = fFilterNode.clone();
        assertFalse("clone().equals()", fFilterNode.equals(clone));
        assertFalse("clone() ==", fFilterNode == clone);
        assertEquals("clone().toString.equals()", fFilterNode.toString(), clone.toString());
        assertNull("clone().getParent()", clone.getParent());
    }

    @Test
    public void testAddChild() {
        ITmfFilterTreeNode child = fFilterNode.clone();
        assertEquals("addChild()", 0, fFilterNode.addChild(child));
        assertEquals("hasChildren()", true, fFilterNode.hasChildren());
        assertEquals("removeChild()", child, fFilterNode.removeChild(child));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetChild() {
        fFilterNode.getChild(0);
    }

    @Test
    public void testRemove() {
        ITmfFilterTreeNode child = fFilterNode.clone();
        assertEquals("addChild()", 0, fFilterNode.addChild(child));
        assertEquals("remove()", child, child.remove());
        assertEquals("hasChildren()", false, fFilterNode.hasChildren());
    }

    @Test
    public void testRemoveChild() {
        ITmfFilterTreeNode child = fFilterNode.clone();
        assertEquals("addChild()", 0, fFilterNode.addChild(child));
        assertEquals("removeChild()", child, fFilterNode.removeChild(child));
        assertEquals("hasChildren()", false, fFilterNode.hasChildren());
    }

    @Test
    public void testReplaceChild() {
        ITmfFilterTreeNode child1 = fFilterNode.clone();
        ITmfFilterTreeNode child2 = fFilterNode.clone();
        child1.addChild(child1.clone());
        assertEquals("addChild()", 0, fFilterNode.addChild(child1));
        assertEquals("getChild()", child1, fFilterNode.getChild(0));
        assertEquals("replaceChild()", child1, fFilterNode.replaceChild(0, child2));
        assertEquals("getChildrenCount()", 1, fFilterNode.getChildrenCount());
        assertEquals("getChild()", child2, fFilterNode.getChild(0));
        assertEquals("removeChild()", child2, fFilterNode.removeChild(child2));
    }
}
