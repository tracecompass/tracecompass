/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.core.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.counters.core.CounterAnalysis;
import org.eclipse.tracecompass.analysis.counters.core.aspects.CounterAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfContentFieldAspect;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStubNs;
import org.junit.After;
import org.junit.Test;

/**
 * Test for the <code>CounterAnalysis</code> class.
 *
 * @author Mikael Ferland
 */
public class CounterAnalysisTest {

    private final @NonNull CounterAnalysis fModule = new CounterAnalysis();
    private final @NonNull TmfXmlTraceStubNs fTrace = new TmfXmlTraceStubNs();

    /**
     * Dispose the counter analysis module and the trace.
     */
    @After
    public void teardown() {
        fModule.dispose();
        fTrace.dispose();
    }

    /**
     * Test if the analysis applies exclusively to traces which contain counter
     * aspects.
     */
    @Test
    public void testCanExecute() {
        // Initial trace only contains basic aspects (available to all trace types)
        assertFalse(fModule.canExecute(fTrace));
        fTrace.addEventAspect(new TmfContentFieldAspect("aspectName"));
        assertFalse(fModule.canExecute(fTrace));
        fTrace.addEventAspect(new CounterAspect("fieldName", "label"));
        assertTrue(fModule.canExecute(fTrace));
    }

}
