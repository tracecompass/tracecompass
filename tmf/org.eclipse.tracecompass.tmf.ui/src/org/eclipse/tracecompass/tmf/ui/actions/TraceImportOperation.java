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

package org.eclipse.tracecompass.tmf.ui.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.FileSystemObjectImportStructureProvider;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.IFileSystemObject;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizardPage;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.TraceFileSystemElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.TraceValidateAndImportOperation;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;

/**
 * An operation which imports traces from a source folder to a target folder.
 * <p>
 * By default, the operation will import all traces from the source folder
 * recursively, using trace type auto-detection, preserving the folder structure
 * and creating links for each imported trace in the target folder, overwriting
 * any existing trace without warning.
 *
 * @since 3.0
 */
public class TraceImportOperation extends WorkspaceModifyOperation {

    private final String fSourcePath;
    private final TmfTraceFolder fDestFolder;

    private boolean fSkipArchiveExtraction = false;

    /**
     * Constructor
     *
     * @param sourcePath
     *            the source path
     * @param destFolder
     *            the destination folder
     */
    public TraceImportOperation(String sourcePath, TmfTraceFolder destFolder) {
        fSourcePath = sourcePath;
        fDestFolder = destFolder;
    }

    /**
     * Sets the skip archive extraction option. When set to true, archive files
     * will be imported directly without being detected or extracted.
     *
     * @param skipArchiveExtraction
     *            true to skip archive extraction
     */
    public void setSkipArchiveExtraction(boolean skipArchiveExtraction) {
        fSkipArchiveExtraction = skipArchiveExtraction;
    }

    @Override
    protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
        int importOptionFlags =
                ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE |
                ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES |
                ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE;
        if (fSkipArchiveExtraction) {
            importOptionFlags |= ImportTraceWizardPage.OPTION_SKIP_ARCHIVE_EXTRACTION;
        }
        IPath baseSourceContainerPath = new Path(fSourcePath);
        IPath destinationContainerPath = fDestFolder.getPath();
        FileSystemObjectImportStructureProvider provider = new FileSystemObjectImportStructureProvider(FileSystemStructureProvider.INSTANCE, null);
        IFileSystemObject object = provider.getIFileSystemObject(new File(fSourcePath));
        TraceFileSystemElement root = TraceFileSystemElement.createRootTraceFileElement(object, provider);
        List<TraceFileSystemElement> fileSystemElements = new ArrayList<>();
        root.getAllChildren(fileSystemElements);
        final TraceValidateAndImportOperation importOperation = new TraceValidateAndImportOperation(
                null, fileSystemElements, null,
                baseSourceContainerPath, destinationContainerPath, false,
                importOptionFlags, fDestFolder);
        try {
            importOperation.run(monitor);
        } catch (InvocationTargetException e) {
            Activator.getDefault().logError("Error running trace import operation", e); //$NON-NLS-1$
        } catch (InterruptedException e) {
        }
        monitor.done();
    }

}
