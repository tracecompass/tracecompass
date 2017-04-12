/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Delisle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.tests.actions;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.tracecompass.tmf.ui.actions.HttpTraceImportOperation;
import org.eclipse.tracecompass.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the {@link HttpTraceImportOperation} class
 *
 * @author Simon Delisle
 */
public class HttpTraceImportOperationTest {

    private static TmfTraceFolder fDestFolder;
    private static String fTestTrace1Url = "http://archive.eclipse.org/tracecompass/test-traces/tmf/syslog";
    private static String fTestTrace2Url = "http://archive.eclipse.org/tracecompass/test-traces/tmf/syslog_collapse";
    private static String fTraceArchiveUrl = "http://archive.eclipse.org/tracecompass/test-traces/tmf/syslogs.zip";
    private static List<String> fImportedTraceNameList;

    /**
     * Setup class
     *
     * @throws Exception
     *             if an exception occurs
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        // Create the destination folder/project
        IProject project = TmfProjectRegistry.createProject("Test Project", null, null);
        final TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
        TmfTraceFolder tracesFolder = checkNotNull(projectElement.getTracesFolder());
        tracesFolder.getResource().getFolder("Folder").create(false, true, null);
        fDestFolder = (TmfTraceFolder) tracesFolder.getChildren().stream()
                .filter(element -> element.getName().equals("Folder")).findFirst().get();

        fImportedTraceNameList = new ArrayList<>();
        fImportedTraceNameList.add("syslog");
        fImportedTraceNameList.add("syslog_collapse");
    }

    /**
     * Cleanup class
     *
     * @throws CoreException
     *             if an exception occurs
     */
    @AfterClass
    public static void afterClass() throws CoreException {
        if (fDestFolder != null) {
            fDestFolder.getProject().getResource().delete(true, null);
        }
    }

    /**
     * Test the download and import operation for a trace file
     *
     * @throws Exception
     *             if an exception occurs
     */
    @Test
    public void testTraceImport() throws Exception {
        WorkspaceModifyOperation operation = new HttpTraceImportOperation(fTestTrace1Url, fDestFolder);
        try {
            PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
        } catch (InterruptedException e) {
            assumeTrue(false);
        }
        validateImport(Collections.singletonList("syslog"));
    }

    /**
     * Test the download and import operation for multiple traces
     *
     * @throws Exception
     *             if an exception occurs
     */
    @Test
    public void testMultipleTracesImport() throws Exception {
        List<String> tracesUrl = new ArrayList<>();
        tracesUrl.add(fTestTrace1Url);
        tracesUrl.add(fTestTrace2Url);
        WorkspaceModifyOperation operation = new HttpTraceImportOperation(tracesUrl, fDestFolder);
        try {
            PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
        } catch (InterruptedException e) {
            assumeTrue(false);
        }
        validateImport(fImportedTraceNameList);
    }

    /**
     * Test the download and import operation for an archive
     *
     * @throws Exception
     *             if an exception occurs
     */
    @Test
    public void testArchiveImport() throws Exception {
        WorkspaceModifyOperation operation = new HttpTraceImportOperation(fTraceArchiveUrl, fDestFolder);
        try {
            PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
        } catch (InterruptedException e) {
            assumeTrue(false);
        }
        validateImport(fImportedTraceNameList);
    }

    private static void validateImport(List<String> expectedImportFiles) {
        List<ITmfProjectModelElement> destFolderChildren = fDestFolder.getChildren();
        assertEquals(expectedImportFiles.size(), destFolderChildren.size());
        for (ITmfProjectModelElement element : destFolderChildren) {
            assertTrue(expectedImportFiles.contains(element.getName()));
        }
    }

}
