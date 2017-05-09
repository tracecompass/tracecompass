/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Delisle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ArchiveUtil;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.DownloadTraceHttpHelper;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.FileSystemObjectImportStructureProvider;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.IFileSystemObject;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizardPage;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.TraceDownloadStatus;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.TraceFileSystemElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.TraceValidateAndImportOperation;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;

/**
 * An operation which downloads and imports traces from a http/https source.
 *
 * @author Simon Delisle
 * @since 3.0
 *
 */
public class HttpTraceImportOperation extends WorkspaceModifyOperation {

    private static final String TRACE_HTTP_IMPORT_TEMP_FOLDER = ".traceHttpImport"; //$NON-NLS-1$

    private final Collection<String> fSourceUrl;
    private final TmfTraceFolder fDestinationFolder;

    /**
     * Constructor
     *
     * @param sourceUrl
     *            HTTP url for the trace you want to import
     * @param destFolder
     *            The destination folder
     */
    public HttpTraceImportOperation(String sourceUrl, TmfTraceFolder destFolder) {
        fSourceUrl = Collections.singletonList(sourceUrl);
        fDestinationFolder = destFolder;
    }

    /**
     * Constructor
     *
     * @param sourceUrl
     *            List of HTTP url for the traces you want to import
     * @param destFolder
     *            The destination folder
     */
    public HttpTraceImportOperation(Collection<String> sourceUrl, TmfTraceFolder destFolder) {
        fSourceUrl = sourceUrl;
        fDestinationFolder = destFolder;
    }

    @Override
    protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
        int importOptionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES |
                ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE;

        // Temporary directory to contain any downloaded files
        IFolder tempDestination = fDestinationFolder.getProject().getResource().getFolder(TRACE_HTTP_IMPORT_TEMP_FOLDER);
        String tempDestinationFolderPath = tempDestination.getLocation().toOSString();
        if (tempDestination.exists()) {
            tempDestination.delete(true, monitor);
        }
        tempDestination.create(IResource.HIDDEN, true, monitor);

        // Download trace/traces
        List<File> downloadedTraceList = new ArrayList<>();
        TraceDownloadStatus status = DownloadTraceHttpHelper.downloadTraces(fSourceUrl, tempDestinationFolderPath);
        if (status.isOk()) {
            List<TraceDownloadStatus> children = status.getChildren();
            for (TraceDownloadStatus traceDownloadStatus : children) {
                downloadedTraceList.add(traceDownloadStatus.getDownloadedFile());
            }
        } else if (status.isTimeout()) {
            if (tempDestination.exists()) {
                tempDestination.delete(true, monitor);
            }
            throw new InterruptedException();
        }

        boolean isArchive = false;
        if (!downloadedTraceList.isEmpty()) {
            isArchive = ArchiveUtil.isArchiveFile(downloadedTraceList.get(0));
        }

        FileSystemObjectImportStructureProvider provider = null;
        IFileSystemObject object = null;

        if (isArchive) {
            // If it's an archive there is only 1 element in this list
            Pair<IFileSystemObject, FileSystemObjectImportStructureProvider> rootObjectAndProvider = ArchiveUtil.getRootObjectAndProvider(downloadedTraceList.get(0), null);
            provider = rootObjectAndProvider.getSecond();
            object = rootObjectAndProvider.getFirst();
        } else {
            provider = new FileSystemObjectImportStructureProvider(FileSystemStructureProvider.INSTANCE, null);
            object = provider.getIFileSystemObject(new File(tempDestinationFolderPath));
        }

        TraceFileSystemElement root = TraceFileSystemElement.createRootTraceFileElement(object, provider);

        List<TraceFileSystemElement> fileSystemElements = new ArrayList<>();
        root.getAllChildren(fileSystemElements);

        IPath sourceContainerPath = new Path(tempDestinationFolderPath);
        IPath destinationContainerPath = fDestinationFolder.getPath();

        TraceValidateAndImportOperation validateAndImportOperation = new TraceValidateAndImportOperation(null, fileSystemElements, null, sourceContainerPath, destinationContainerPath, isArchive, importOptionFlags, fDestinationFolder);
        validateAndImportOperation.run(monitor);

        // Clean the temporary directory
        if (tempDestination.exists()) {
            tempDestination.delete(true, monitor);
        }
    }

}
