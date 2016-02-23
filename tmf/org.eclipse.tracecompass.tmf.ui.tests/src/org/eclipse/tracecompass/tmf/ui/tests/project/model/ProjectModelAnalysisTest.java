/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.project.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfAnalysisElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfViewsElement;
import org.eclipse.tracecompass.tmf.ui.tests.shared.ProjectModelTestData;
import org.eclipse.tracecompass.tmf.ui.tests.stubs.analysis.TestAnalysisUi;
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

        /* Make sure the list of views is there and not empty */
        Optional<TmfViewsElement> possibleViewsElem = trace.getChildren()
                .stream()
                .filter(child -> child instanceof TmfViewsElement)
                .map(elem -> (TmfViewsElement) elem)
                .findFirst();
        assertTrue(possibleViewsElem.isPresent());
        TmfViewsElement viewsElem = possibleViewsElem.get();

        /* Make sure TestAnalysisUi is there */
        Optional<TmfAnalysisElement> possibleAnalysisElem = viewsElem.getChildren().stream()
                .filter(child -> child instanceof TmfAnalysisElement)
                .map(elem -> (TmfAnalysisElement) elem)
                .filter(analysisElem -> analysisElem.getAnalysisId().equals(MODULE_UI))
                .findFirst();
        assertTrue(possibleAnalysisElem.isPresent());
        assertEquals("Test analysis in UI", possibleAnalysisElem.get().getName());
    }

    /**
     * Test the instantiateAnalysis method
     */
    @Test
    public void testInstantiate() {
        TmfTraceElement traceElement = getTraceElement();

        TmfAnalysisElement analysis = traceElement.getAvailableAnalysis().stream()
                .filter(availableAnalysis -> availableAnalysis.getAnalysisId().equals(MODULE_UI))
                .findFirst().get();

        /* Instantiate an analysis on a trace that is closed */
        traceElement.closeEditors();
        analysis.activateParentTrace();

        try {
            ProjectModelTestData.delayUntilTraceOpened(traceElement);
        } catch (TimeoutException e) {
            fail("The analysis parent did not open in a reasonable time");
        }
        ITmfTrace trace = traceElement.getTrace();

        assertNotNull(trace);
        TestAnalysisUi module = (TestAnalysisUi) trace.getAnalysisModule(analysis.getAnalysisId());
        assertNotNull(module);

    }
}
