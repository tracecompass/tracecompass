/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.actions;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.ui.actions.TraceImportOperation;
import org.eclipse.tracecompass.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the {@link TraceImportOperation} class
 */
public class TraceImportOperationTest {

    private static String fSourcePath;
    private static TmfTraceFolder fDestFolder;

    /**
     * Setup class
     *
     * @throws Exception if an exception occurs
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        URL resource = TmfCoreTestPlugin.getDefault().getBundle().getResource("testfiles");
        fSourcePath = FileLocator.toFileURL(resource).toURI().getPath();
        IProject project = TmfProjectRegistry.createProject("Test Project", null, null);
        final TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
        TmfTraceFolder tracesFolder = checkNotNull(projectElement.getTracesFolder());
        tracesFolder.getResource().getFolder("Folder").create(false, true, null);
        fDestFolder = (TmfTraceFolder) tracesFolder.getChildren().stream()
                .filter(element -> element.getName().equals("Folder")).findFirst().get();
    }

    /**
     * Cleanup class
     *
     * @throws CoreException if an exception occurs
     */
    @AfterClass
    public static void afterClass() throws CoreException {
        if (fDestFolder != null) {
            fDestFolder.getProject().getResource().delete(true, null);
        }
    }

    /**
     * Test the operation
     *
     * @throws Exception if an exception occurs
     */
    @Test
    public void test() throws Exception {
        WorkspaceModifyOperation operation = new TraceImportOperation(fSourcePath, fDestFolder);
        PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
        validateImport(fSourcePath, fDestFolder);
    }

    private static void validateImport(String sourcePath, TmfTraceFolder destFolder) {
        Map<String, ITmfProjectModelElement> map = new HashMap<>();
        destFolder.getChildren().forEach(element -> map.put(element.getName(), element));
        File source = new File(sourcePath);
        File[] files = source.listFiles();
        for (File file : files) {
            ITmfProjectModelElement element = map.get(file.getName());
            if (element == null) {
                continue;
            }
            if (file.isDirectory()) {
                assertTrue(file.toString(), element instanceof TmfTraceFolder);
                validateImport(file.getAbsolutePath(), (TmfTraceFolder) element);
            } else {
                assertTrue(file.toString(), element instanceof TmfTraceElement);
                TmfTraceElement traceElement = (TmfTraceElement) element;
                assertNotNull(file.toString(), traceElement.getTraceType());
                assertTrue(file.toString(), traceElement.getResource().isLinked());
            }
        }
    }
}
