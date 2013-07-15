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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfAnalysisElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfAnalysisOutputElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.tests.stubs.analysis.TestAnalysisUi;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the {@link TmfAnalysisOutputElement} class.
 *
 * @author Geneviève Bastien
 */
public class ProjectModelOutputTest {

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

    private TmfAnalysisElement getTestAnalysisUi() {
        TmfTraceElement trace = getTraceElement();

        /* Make sure the analysis list is not empty */
        List<TmfAnalysisElement> analysisList = trace.getAvailableAnalysis();
        assertFalse(analysisList.isEmpty());

        /* Make sure TestAnalysisUi is there */
        TmfAnalysisElement analysis = null;
        for (TmfAnalysisElement analysisElement : analysisList) {
            if (analysisElement.getAnalysisId().equals(ProjectModelAnalysisTest.MODULE_UI)) {
                analysis = analysisElement;
            }
        }
        assertNotNull(analysis);
        return analysis;
    }

    /**
     * Test the getAvailableOutputs() method
     */
    @Test
    public void testListOutputs() {
        TmfAnalysisElement analysis = getTestAnalysisUi();

        /* To get the list of outputs the trace needs to be opened */
        analysis.activateParent();
        ProjectModelTestData.delayThread(500);

        /* Make sure the output list is not empty */
        List<TmfAnalysisOutputElement> outputList = analysis.getAvailableOutputs();
        assertFalse(outputList.isEmpty());
        boolean found = false;
        for (ITmfProjectModelElement element : outputList) {
            if (element instanceof TmfAnalysisOutputElement) {
                TmfAnalysisOutputElement outputElement = (TmfAnalysisOutputElement) element;
                if (outputElement.getName().equals("Test Analysis View")) {
                    found = true;
                }
            }
        }
        assertTrue(found);
    }

    /**
     * Test the outputAnalysis method for a view
     */
    @Test
    public void testOpenView() {
        TmfAnalysisElement analysis = getTestAnalysisUi();

        analysis.activateParent();
        ProjectModelTestData.delayThread(1000);

        List<TmfAnalysisOutputElement> outputList = analysis.getAvailableOutputs();
        assertFalse(outputList.isEmpty());

        final IWorkbench wb = PlatformUI.getWorkbench();
        final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();

        IViewPart view = activePage.findView(TestAnalysisUi.VIEW_ID);
        if (view != null) {
            activePage.hideView(view);
        }

        TmfAnalysisOutputElement outputElement = null;
        for (ITmfProjectModelElement element : outputList) {
            if (element instanceof TmfAnalysisOutputElement) {
                TmfAnalysisOutputElement el = (TmfAnalysisOutputElement) element;
                if (el.getName().equals("Test Analysis View")) {
                    outputElement = el;
                }
            }
        }
        assertNotNull(outputElement);

        outputElement.outputAnalysis();
        ProjectModelTestData.delayThread(1000);
        view = activePage.findView(TestAnalysisUi.VIEW_ID);
        assertNotNull(view);
    }
}
