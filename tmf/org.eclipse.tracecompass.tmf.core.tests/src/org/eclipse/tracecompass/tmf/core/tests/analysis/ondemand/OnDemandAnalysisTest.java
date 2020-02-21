/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.analysis.ondemand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.eclipse.tracecompass.tmf.core.analysis.ondemand.IOnDemandAnalysis;
import org.eclipse.tracecompass.tmf.core.analysis.ondemand.OnDemandAnalysisManager;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.ondemand.OnDemandAnalysisStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub2;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub3;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

/**
 * Basic tests for {@link IOnDemandAnalysis}, {@link OnDemandAnalysisManager}
 * and the related extension point.
 *
 * @author Alexandre Montplaisir
 */
public class OnDemandAnalysisTest {

    private IOnDemandAnalysis analysis = new OnDemandAnalysisStub();

    private ITmfTrace fTraceThatApplies;
    private ITmfTrace fTraceThatDoesntApply;

    /**
     * Test setup
     */
    @Before
    public void setup() {
        fTraceThatApplies = new TmfTraceStub2();
        fTraceThatDoesntApply = new TmfTraceStub3();
    }

    /**
     * Test cleanup
     */
    @After
    public void teardown() {
        if (fTraceThatApplies != null) {
            fTraceThatApplies.dispose();
            fTraceThatApplies = null;
        }
        if (fTraceThatDoesntApply != null) {
            fTraceThatDoesntApply.dispose();
            fTraceThatDoesntApply = null;
        }
    }

    /**
     * Test our stub analysis with a trace type on which it applies.
     */
    @Test
    public void testApplies() {
        ITmfTrace trace = fTraceThatApplies;
        assertNotNull(trace);

        assertTrue(analysis.appliesTo(trace));
    }

    /**
     * Test our stub analysis with a trace type on which it does not apply.
     */
    @Test
    public void testDoesNotApply() {
        ITmfTrace trace = fTraceThatDoesntApply;
        assertNotNull(trace);

        assertFalse(analysis.appliesTo(trace));
    }

    /**
     * Test getting an analysis via the manager. The expected way of doing
     * things.
     */
    @Test
    public void testGetAnalysisFromTrace() {
        ITmfTrace trace = fTraceThatApplies;
        assertNotNull(trace);

        Set<IOnDemandAnalysis> set1 = OnDemandAnalysisManager.getInstance().getOndemandAnalyses(trace);

        assertEquals(1, set1.size());
        assertTrue(Iterables.getOnlyElement(set1) instanceof OnDemandAnalysisStub);

        /* Make sure it is still true on a subsequent call */
        Set<IOnDemandAnalysis> set2 = OnDemandAnalysisManager.getInstance().getOndemandAnalyses(trace);

        assertTrue(set1.equals(set2));
    }

    /**
     * Test querying the manager for analyses, but for a trace that does not
     * support any.
     */
    @Test
    public void testGetNoAnalysisFromTrace() {
        ITmfTrace trace = fTraceThatDoesntApply;
        assertNotNull(trace);

        Set<IOnDemandAnalysis> set = OnDemandAnalysisManager.getInstance().getOndemandAnalyses(trace);
        assertTrue(set.isEmpty());
    }
}
