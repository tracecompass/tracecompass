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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the TmfTraceElement class.
 *
 * @author Geneviève Bastien
 */
public class ProjectModelTraceTest {

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

    /**
     * Test the getTrace() and trace opening
     */
    @Test
    public void testOpenTrace() {
        assertNotNull(fixture);

        final TmfTraceElement traceElement = fixture.getTracesFolder().getTraces().get(0);

        /*
         * Get the trace from the element, it is not opened yet, should be null
         */
        ITmfTrace trace = traceElement.getTrace();
        assertNull(trace);

        TmfOpenTraceHelper.openTraceFromElement(traceElement);

        /* Give the trace a chance to open */
        ProjectModelTestData.delayThread(500);

        trace = traceElement.getTrace();
        assertNotNull(trace);

        /*
         * Open the trace from project, then get from element, both should be
         * the exact same element as the active trace
         */
        TmfOpenTraceHelper.openTraceFromElement(traceElement);
        ProjectModelTestData.delayThread(500);

        ITmfTrace trace2 = TmfTraceManager.getInstance().getActiveTrace();

        /* The trace was reopened, it should be the same as before */
        assertTrue(trace2 == trace);

        /* Here, the getTrace() should return the same as active trace */
        trace = traceElement.getTrace();
        assertTrue(trace2 == trace);
    }

}
