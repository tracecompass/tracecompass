/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.project.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfAnalysisElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfNavigatorContentProvider;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.tests.shared.ProjectModelTestData;
import org.eclipse.linuxtools.tmf.ui.tests.stubs.analysis.TestAnalysisUi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the {@link TmfAnalysisElement} class.
 *
 * @author Geneviève Bastien
 */
public class ProjectModelAnalysisTest {

    /** ID of analysis module in UI */
    public static final String MODULE_UI = "org.eclipse.linuxtools.tmf.ui.tests.test";
    private TmfProjectElement fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        assumeTrue(CtfTmfTestTrace.KERNEL.exists());
        try {
            fixture = ProjectModelTestData.getFilledProject();
        } catch (CoreException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Cleans up the project after tests have been executed
     */
    @After
    public void cleanUp() {
        ProjectModelTestData.deleteProject(fixture);
    }

    private TmfTraceElement getTraceElement() {
        TmfTraceElement trace = null;
        for (ITmfProjectModelElement element : fixture.getTracesFolder().getChildren()) {
            if (element instanceof TmfTraceElement) {
                TmfTraceElement traceElement = (TmfTraceElement) element;
                if (traceElement.getName().equals(ProjectModelTestData.getTraceName())) {
                    trace = traceElement;
                }
            }
        }
        assertNotNull(trace);
        return trace;
    }

    /**
     * Test the getAvailableAnalysis() method
     */
    @Test
    public void testListAnalysis() {
        TmfTraceElement trace = getTraceElement();

        /* Make sure the analysis list is not empty */
        List<TmfAnalysisElement> analysisList = trace.getAvailableAnalysis();
        assertFalse(analysisList.isEmpty());

        /* Make sure TestAnalysisUi is there */
        TmfAnalysisElement analysis = null;
        for (TmfAnalysisElement analysisElement : analysisList) {
            if (analysisElement.getAnalysisId().equals(MODULE_UI)) {
                analysis = analysisElement;
            }
        }
        assertNotNull(analysis);

        assertEquals("Test analysis in UI", analysis.getName());
    }

    /**
     * Test if the list of available analysis is correctly populated by the
     * content provider
     */
    @Test
    public void testPopulate() {
        TmfTraceElement trace = getTraceElement();

        final TmfNavigatorContentProvider ncp = new TmfNavigatorContentProvider();
        // force the model to be populated
        ncp.getChildren(fixture);

        /* Make sure the analysis list is not empty */
        List<ITmfProjectModelElement> analysisList = trace.getChildren();
        assertFalse(analysisList.isEmpty());

        /* Make sure TestAnalysisUi is there */
        TmfAnalysisElement analysis = null;
        for (ITmfProjectModelElement element : analysisList) {
            if (element instanceof TmfAnalysisElement) {
                TmfAnalysisElement analysisElement = (TmfAnalysisElement) element;
                if (analysisElement.getAnalysisId().equals(MODULE_UI)) {
                    analysis = analysisElement;
                }
            }
        }
        assertNotNull(analysis);

        assertEquals("Test analysis in UI", analysis.getName());
    }

    /**
     * Test the instantiateAnalysis method
     */
    @Test
    public void testInstantiate() {
        TmfTraceElement traceElement = getTraceElement();

        TmfAnalysisElement analysis = null;
        for (TmfAnalysisElement analysisElement : traceElement.getAvailableAnalysis()) {
            if (analysisElement.getAnalysisId().equals(MODULE_UI)) {
                analysis = analysisElement;
            }
        }
        assertNotNull(analysis);

        /* Instantiate an analysis on a trace that is closed */
        traceElement.closeEditors();
        analysis.activateParent();

        ITmfTrace trace = null;
        int cnt = 0;

        /* Give some time to the trace to open */
        while ((trace == null) && (cnt++ < 10)) {

            ProjectModelTestData.delayThread(500);

            /* Get the analysis module associated with the element */
            trace = traceElement.getTrace();
        }

        assertNotNull(trace);
        TestAnalysisUi module = (TestAnalysisUi) trace.getAnalysisModule(analysis.getAnalysisId());
        assertNotNull(module);

    }
}
