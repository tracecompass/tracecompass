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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFolder;
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
    private static TmfTraceFolder fTracesFolder;
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
        fTracesFolder = checkNotNull(projectElement.getTracesFolder());
        fTracesFolder.getResource().getFolder("Folder").create(false, true, null);
        fDestFolder = (TmfTraceFolder) fTracesFolder.getChild("Folder");
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
        assertEquals(9, fDestFolder.getTraces().size());
        validateImport(fSourcePath, fDestFolder, true);
    }

    /**
     * Test the operation skip archive extraction flag
     *
     * @throws Exception if an exception occurs
     */
    @Test
    public void testSkipArchiveExtraction() throws Exception {
        IFolder sourceFolder = fDestFolder.getProject().getResource().getFolder("testfiles");
        sourceFolder.create(false, true, null);
        File archive = new File(sourceFolder.getFile("testfiles.zip").getLocation().toOSString());
        createArchive(fSourcePath, archive);

        fTracesFolder.getResource().getFolder("skipFalse").create(false, true, null);
        TmfTraceFolder destFolder = (TmfTraceFolder) fTracesFolder.getChild("skipFalse");
        TraceImportOperation operation = new TraceImportOperation(sourceFolder.getLocation().toOSString(), destFolder);
        operation.setSkipArchiveExtraction(false);
        PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
        assertEquals(1, destFolder.getChildren().size());
        assertTrue(destFolder.getChildren().get(0) instanceof TmfTraceFolder);
        destFolder = (TmfTraceFolder) destFolder.getChildren().get(0);
        assertEquals("testfiles.zip", destFolder.getName());
        assertEquals(9, destFolder.getTraces().size());
        validateImport(fSourcePath, destFolder, false);

        fTracesFolder.getResource().getFolder("skipTrue").create(false, true, null);
        destFolder = (TmfTraceFolder) fTracesFolder.getChild("skipTrue");
        operation = new TraceImportOperation(sourceFolder.getLocation().toOSString(), destFolder);
        operation.setSkipArchiveExtraction(true);
        PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
        assertEquals(0, destFolder.getTraces().size());
        validateImport(sourceFolder.getLocation().toOSString(), destFolder, true);
    }

    private static void validateImport(String sourcePath, TmfTraceFolder destFolder, boolean isLinked) {
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
                validateImport(file.getAbsolutePath(), (TmfTraceFolder) element, isLinked);
            } else {
                assertTrue(file.toString(), element instanceof TmfTraceElement);
                TmfTraceElement traceElement = (TmfTraceElement) element;
                assertNotNull(file.toString(), traceElement.getTraceType());
                assertEquals(file.toString(), isLinked, traceElement.getResource().isLinked());
            }
        }
    }

    private static void createArchive(String sourcePath, File archive) throws FileNotFoundException, IOException, ArchiveException {
        try (OutputStream out = new FileOutputStream(archive);
                ArchiveOutputStream archiveOutputStream = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, out)) {
            for (File file : FileUtils.listFiles(new File(sourcePath), null, true)) {
                String name = file.getAbsolutePath().substring(sourcePath.length());
                archiveOutputStream.putArchiveEntry(new ZipArchiveEntry(name));
                try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                    IOUtils.copy(in, archiveOutputStream);
                }
                archiveOutputStream.closeArchiveEntry();
            }
            archiveOutputStream.finish();
        }
    }
}
