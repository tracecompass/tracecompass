/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfExperimentStub;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.tests.experiment.type.TmfEventsEditorStub;
import org.eclipse.linuxtools.tmf.ui.tests.experiment.type.TmfEventsTableExperimentStub;
import org.eclipse.linuxtools.tmf.ui.tests.shared.ProjectModelTestData;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Some unit tests for trace types and experiment types
 *
 * @author Geneviève Bastien
 */
public class TraceAndExperimentTypeTest {

    /** Test experiment type id */
    public final static String TEST_EXPERIMENT_TYPE = "org.eclipse.linuxtools.tmf.core.tests.experimenttype";

    private TmfProjectElement fixture;
    private TmfExperimentElement fExperiment;
    private final String EXPERIMENT_NAME = "exp_test";

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        assumeTrue(CtfTmfTestTrace.KERNEL.exists());
        try {
            fixture = ProjectModelTestData.getFilledProject();
            fExperiment = ProjectModelTestData.addExperiment(fixture, EXPERIMENT_NAME);
            assertNotNull(fExperiment);
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

    /**
     * Test whether a newly created experiment has the default experiment type,
     * even though none was specified
     */
    @Test
    public void testDefaultExperimentType() {
        TmfExperimentElement experimentElement = ProjectModelTestData.addExperiment(fixture, "testDefaultExpType");
        assertNotNull(experimentElement);
        TmfExperiment experiment = experimentElement.instantiateTrace();
        assertNotNull(experiment);
        assertEquals(TmfTraceType.DEFAULT_EXPERIMENT_TYPE, experimentElement.getTraceType());
    }

    /**
     * Test that the experiment opened is of the right class
     */
    @Test
    public void testExperimentType() {

        IResource resource = fExperiment.getResource();
        try {
            resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, TEST_EXPERIMENT_TYPE);
            fExperiment.refreshTraceType();
        } catch (CoreException e) {
            fail(e.getMessage());
        }

        TmfOpenTraceHelper.openTraceFromElement(fExperiment);
        try {
            ProjectModelTestData.delayUntilTraceOpened(fExperiment);
        } catch (TimeoutException e1) {
            fail (e1.getMessage());
        }

        ITmfTrace trace = fExperiment.getTrace();
        assertTrue(trace instanceof TmfExperimentStub);
    }

    /**
     * Test that event editor, event table and statistics viewer are the default
     * ones for a generic experiment
     */
    @Test
    public void testNoExperimentTypeChildren() {
        TmfOpenTraceHelper.openTraceFromElement(fExperiment);

        try {
            ProjectModelTestData.delayUntilTraceOpened(fExperiment);
        } catch (TimeoutException e1) {
            fail (e1.getMessage());
        }

        final IWorkbench wb = PlatformUI.getWorkbench();
        final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
        IEditorPart editor = activePage.getActiveEditor();

        /* Test the editor class. Cannot test table class since it is unexposed */
        assertNotNull(editor);
        assertTrue(editor.getClass().equals(TmfEventsEditor.class));
    }

    /**
     * Test that event editor, event table and statistics viewer are built
     * correctly when specified
     */
    @Test
    public void testExperimentTypeChildren() {

        /* Set the trace type of the experiment */
        IResource resource = fExperiment.getResource();
        try {
            resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, TEST_EXPERIMENT_TYPE);
            fExperiment.refreshTraceType();
        } catch (CoreException e) {
            fail(e.getMessage());
        }

        TmfOpenTraceHelper.openTraceFromElement(fExperiment);

        ProjectModelTestData.delayThread(500);

        /* Test the editor class */
        final IWorkbench wb = PlatformUI.getWorkbench();
        final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
        IEditorPart editor = activePage.getActiveEditor();

        assertNotNull(editor);
        assertTrue(editor.getClass().equals(TmfEventsEditorStub.class));

        /* Test the event table class */
        TmfEventsEditorStub editorStub = (TmfEventsEditorStub) editor;
        TmfEventsTable table = editorStub.getNewEventsTable();

        assertNotNull(table);
        assertTrue(table.getClass().equals(TmfEventsTableExperimentStub.class));

    }

    /**
     * Test that the analysis get populated under an experiment of the proper type
     */
    @Test
    public void testExperimentTypeAnalysis() {

        /* Set the trace type of the experiment */
        IResource resource = fExperiment.getResource();
        try {
            resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, TEST_EXPERIMENT_TYPE);
            fExperiment.refreshTraceType();
        } catch (CoreException e) {
            fail(e.getMessage());
        }

        /* Force the refresh of the experiment */
        fExperiment.getParent().refresh();
        assertFalse(fExperiment.getAvailableAnalysis().isEmpty());
    }

}
