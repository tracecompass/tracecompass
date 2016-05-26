/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterAndNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterCompareNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterContainsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterEqualsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterOrNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterTraceTypeNode;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtEvent;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtEventType;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlEvent;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlEventType;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlInputElement;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the {@link TmfFilterTraceTypeNode} class.
 *
 * @author Patrick Tasse
 */
@SuppressWarnings("javadoc")
public class TmfFilterTraceTypeNodeTest extends TmfFilterTreeNodeTestBase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private static final String CATEGORY_TXT = "txt";
    private static final String CATEGORY_XML = "xml";
    private static final @NonNull String DEFINITION_NAME_TXT = "name txt";
    private static final @NonNull String DEFINITION_NAME_XML = "name xml";
    private static final String SEP = ":";
    private static final String CUSTOM_TXT_TRACE_TYPE_PREFIX = "custom.txt.trace" + SEP;
    private static final String CUSTOM_XML_TRACE_TYPE_PREFIX = "custom.xml.trace" + SEP;
    private static CustomTxtTraceDefinition fCustomTxtDefinition = new CustomTxtTraceDefinition();
    private static CustomXmlTraceDefinition fCustomXmlDefinition = new CustomXmlTraceDefinition();
    static {
        fCustomTxtDefinition.categoryName = CATEGORY_TXT;
        fCustomTxtDefinition.definitionName = DEFINITION_NAME_TXT;
        fCustomXmlDefinition.categoryName = CATEGORY_XML;
        fCustomXmlDefinition.definitionName = DEFINITION_NAME_XML;
        fCustomXmlDefinition.rootInputElement = new CustomXmlInputElement();
    }
    private static CustomTxtTrace fCustomTxtTrace = new CustomTxtTrace(fCustomTxtDefinition);
    private static CustomXmlTrace fCustomXmlTrace = new CustomXmlTrace(fCustomXmlDefinition);
    private static TmfEventType fCustomTxtEventType = new CustomTxtEventType(DEFINITION_NAME_TXT, null);
    private static TmfEventType fCustomXmlEventType = new CustomXmlEventType(DEFINITION_NAME_XML, null);
    private ITmfEventField fContent = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, null);
    private TmfEvent fEvent1 = new TmfEvent(TRACE, 0, TmfTimestamp.fromNanos(1), EVENT_TYPE, fContent);
    private TmfEvent fEvent2 = new CustomTxtEvent(fCustomTxtDefinition, fCustomTxtTrace, TmfTimestamp.fromNanos(2), fCustomTxtEventType);
    private TmfEvent fEvent3 = new CustomXmlEvent(fCustomXmlDefinition, fCustomXmlTrace, TmfTimestamp.fromNanos(3), fCustomXmlEventType);
    private TmfFilterTraceTypeNode fFilter;

    @AfterClass
    public static void disposeCustomTraces() {
        fCustomTxtTrace.dispose();
        fCustomXmlTrace.dispose();
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    @Before
    public void createFilter() {
        fFilter = new TmfFilterTraceTypeNode(null);
        fFilterNode = fFilter;
    }

    @Test
    public void testMatches() {
        fFilter.setTraceClass(TmfTraceStub.class);
        assertTrue(fFilter.matches(fEvent1));
        assertFalse(fFilter.matches(fEvent2));
        assertFalse(fFilter.matches(fEvent3));

        fFilter.setTraceClass(CustomTxtTrace.class);
        fFilter.setTraceTypeId(CUSTOM_TXT_TRACE_TYPE_PREFIX + CATEGORY_TXT + SEP + DEFINITION_NAME_TXT);
        assertFalse(fFilter.matches(fEvent1));
        assertTrue(fFilter.matches(fEvent2));
        assertFalse(fFilter.matches(fEvent3));

        fFilter.setTraceClass(CustomXmlTrace.class);
        fFilter.setTraceTypeId(CUSTOM_XML_TRACE_TYPE_PREFIX + CATEGORY_XML + SEP + DEFINITION_NAME_XML);
        assertFalse(fFilter.matches(fEvent1));
        assertFalse(fFilter.matches(fEvent2));
        assertTrue(fFilter.matches(fEvent3));

        fFilter.setTraceClass(CustomTxtTrace.class);
        fFilter.setTraceTypeId(CUSTOM_TXT_TRACE_TYPE_PREFIX + CATEGORY_XML + SEP + DEFINITION_NAME_XML);
        assertFalse(fFilter.matches(fEvent1));
        assertFalse(fFilter.matches(fEvent2));
        assertFalse(fFilter.matches(fEvent3));
    }

    @Test
    public void testGetName() {
        assertEquals("getName()", "TRACETYPE", fFilter.getNodeName());
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
