/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.remote.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.remote.ui.messages.RemoteMessages;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.TmfWorkspaceModifyOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ArchiveUtil;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.FileSystemObjectImportStructureProvider;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.IFileSystemObject;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportConfirmation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportConflictHandler;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizardPage;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.TraceFileSystemElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.TraceValidateAndImportOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceCoreUtils;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceTypeUIUtils;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;

/**
 * Operation to import a set of traces from a remote node into a tracing
 * project.
 *
 * @author Bernd Hufmann
 */
public class RemoteImportTracesOperation extends TmfWorkspaceModifyOperation {

    private static final String TRACE_IMPORT = ".traceRemoteImport"; //$NON-NLS-1$
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final int BUFFER_IN_KB = 16;
    private static final int BYTES_PER_KB = 1024;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private IStatus fStatus;
    private final Shell fShell;
    private final TmfTraceFolder fDestination;
    private final Object[] fTraceElements;
    private final ImportConflictHandler fConflictHandler;
    private final List<IResource> fImportedResources = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Constructor(s)
    // ------------------------------------------------------------------------
    /**
     * Operation to import a set of traces from a remote node into a tracing
     * project.
     *
     * @param shell
     *            shell to display confirmation dialog
     * @param destination
     *            The destination traces folder
     * @param elements
     *            The trace model elements describing the traces to import
     * @param overwriteAll
     *            Flag to indicate to overwrite all existing traces
     */
    public RemoteImportTracesOperation(Shell shell, TmfTraceFolder destination, Object[] elements, boolean overwriteAll) {
        super();
        fShell = shell;
        fDestination = destination;
        fTraceElements = Arrays.copyOf(elements, elements.length);
        if (overwriteAll) {
            fConflictHandler = new ImportConflictHandler(fShell, destination, ImportConfirmation.OVERWRITE_ALL);
        } else {
            fConflictHandler = new ImportConflictHandler(fShell, destination, ImportConfirmation.SKIP);
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    @Override
    protected void execute(IProgressMonitor monitor) throws CoreException,
            InvocationTargetException, InterruptedException {

        try {
            doRun(monitor);
            setStatus(Status.OK_STATUS);
        } catch (InterruptedException | OperationCanceledException e) {
            setStatus(Status.CANCEL_STATUS);
            throw e;
        } catch (Exception e) {
            setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, RemoteMessages.RemoteImportTracesOperation_ImportFailure, e));
            throw new InvocationTargetException(e);
        }
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    private void doRun(IProgressMonitor monitor) throws ExecutionException, CoreException, IOException, InterruptedException {

        IFolder destinationFolder = fDestination.getResource();
        if (!destinationFolder.exists()) {
            throw new ExecutionException(RemoteMessages.RemoteImportTracesOperation_ImportDialogInvalidTracingProject + " (" + destinationFolder + ")"); //$NON-NLS-1$//$NON-NLS-2$
        }

        SubMonitor subMonitor = SubMonitor.convert(monitor, fTraceElements.length * 4);
        subMonitor.beginTask(RemoteMessages.RemoteImportTracesOperation_DownloadTask, fTraceElements.length * 4);

        for (Object packageElement : fTraceElements) {
            if (!(packageElement instanceof TracePackageTraceElement)) {
                continue;
            }
            TracePackageTraceElement traceElement = (TracePackageTraceElement) packageElement;
            TracePackageElement parentElement = traceElement.getParent();
            while (parentElement != null) {
                if (parentElement instanceof RemoteImportTraceGroupElement) {
                    break;
                }
                parentElement = parentElement.getParent();
            }

            if (parentElement == null) {
                continue;
            }

            RemoteImportTraceGroupElement traceGroup = (RemoteImportTraceGroupElement) parentElement;
            String rootPath = traceGroup.getRootImportPath();

            // Create folder with node name in destination folder
            RemoteImportConnectionNodeElement nodeElement = (RemoteImportConnectionNodeElement) traceGroup.getParent();
            String nodeName = nodeElement.getName();
            IFolder nodeFolder = destinationFolder.getFolder(nodeName);

            TracePackageElement[] children = traceElement.getChildren();
            SubMonitor childMonitor = subMonitor.newChild(1);
            TraceUtils.createFolder(nodeFolder, childMonitor);

            for (TracePackageElement element : children) {
                ModalContext.checkCanceled(monitor);

                if (element instanceof RemoteImportTraceFilesElement) {
                    RemoteImportTraceFilesElement traceFilesElement = (RemoteImportTraceFilesElement) element;

                    IFileStore remoteFile = traceFilesElement.getRemoteFile();

                    // Preserve folder structure
                    IPath sessionParentPath = TmfTraceCoreUtils.newSafePath(rootPath).removeLastSegments(1);
                    IPath traceParentPath = TmfTraceCoreUtils.newSafePath(remoteFile.getParent().toURI().getPath());
                    IPath relativeTracePath = Path.EMPTY;
                    if (sessionParentPath.isPrefixOf(traceParentPath)) {
                        relativeTracePath = traceParentPath.makeRelativeTo(sessionParentPath);
                    }

                    String[] segments = relativeTracePath.segments();
                    for (int i = 0; i < segments.length; i++) {
                        String segment = TmfTraceCoreUtils.validateName(TmfTraceCoreUtils.safePathToString(segments[i]));
                        if (i == 0) {
                            relativeTracePath = new Path(segment);
                        } else {
                            relativeTracePath = relativeTracePath.append(segment);
                        }
                    }

                    IFolder traceFolder = nodeFolder.getFolder(new Path(relativeTracePath.toOSString()));
                    childMonitor = subMonitor.newChild(1);
                    TraceUtils.createFolder(traceFolder, childMonitor);
                    childMonitor.done();

                    // Import trace
                    IResource traceRes = null;
                    IFileInfo info = remoteFile.fetchInfo();
                    if (info.isDirectory()) {
                        traceRes = downloadDirectoryTrace(remoteFile, traceFolder, subMonitor.newChild(1));
                    } else {
                        traceRes = downloadFileTrace(remoteFile, traceFolder, subMonitor.newChild(1));
                    }

                    String traceName = traceElement.getText();
                    if (traceRes == null || !traceRes.exists()) {
                        continue;
                    }

                    // Select trace type
                    TraceTypeHelper traceTypeHelper = null;
                    String traceTypeStr = traceElement.getTraceType();
                    if (traceTypeStr != null) {
                        traceTypeHelper = TmfTraceType.getTraceType(traceTypeStr);
                    }

                    // no specific trace type found
                    if (traceTypeHelper == null) {
                        try {
                            // Try to auto-detect the trace typ
                            childMonitor = subMonitor.newChild(1);
                            childMonitor.setTaskName(NLS.bind(RemoteMessages.RemoteImportTracesOperation_DetectingTraceType, traceName));
                            childMonitor.done();
                            traceTypeHelper = TmfTraceTypeUIUtils.selectTraceType(traceRes.getLocation().toOSString(), null, null);
                        } catch (TmfTraceImportException e) {
                            // Could not figure out the type
                        }
                    }

                    if (traceTypeHelper != null) {
                        TmfTraceTypeUIUtils.setTraceType(traceRes, traceTypeHelper);
                        fImportedResources.add(traceRes);
                    }

                    // Set source location
                    URI uri = remoteFile.toURI();
                    String sourceLocation = URIUtil.toUnencodedString(uri);
                    traceRes.setPersistentProperty(TmfCommonConstants.SOURCE_LOCATION, sourceLocation);
                }
            }
        }
    }

    // Download a directory trace
    private IResource downloadDirectoryTrace(IFileStore trace, IFolder traceFolder, IProgressMonitor monitor) throws CoreException, IOException, InterruptedException {

        IFileStore[] sources = trace.childStores(EFS.NONE, monitor);

        // Don't import just the metadata file
        if (sources.length > 1) {
            String traceName = trace.getName();

            traceName = TmfTraceCoreUtils.validateName(traceName);

            IFolder folder = traceFolder.getFolder(traceName);
            String newName = fConflictHandler.checkAndHandleNameClash(folder.getFullPath(), monitor);
            if (newName == null) {
                return null;
            }

            folder = traceFolder.getFolder(newName);
            folder.create(true, true, null);

            SubMonitor subMonitor = SubMonitor.convert(monitor, sources.length);
            subMonitor.beginTask(RemoteMessages.RemoteImportTracesOperation_DownloadTask, sources.length);

            for (IFileStore source : sources) {
                if (subMonitor.isCanceled()) {
                    throw new InterruptedException();
                }

                IPath destination = folder.getLocation().addTrailingSeparator().append(source.getName());
                IFileInfo info = source.fetchInfo();
                // TODO allow for downloading index directory and files
                if (!info.isDirectory()) {
                    SubMonitor childMonitor = subMonitor.newChild(1);
                    childMonitor.setTaskName(RemoteMessages.RemoteImportTracesOperation_DownloadTask + ' ' + trace.getName() + '/' + source.getName());
                    try (InputStream in = source.openInputStream(EFS.NONE, new NullProgressMonitor())) {
                        copy(in, folder, destination, childMonitor, info.getLength());
                    }
                }
            }
            folder.refreshLocal(IResource.DEPTH_INFINITE, null);
            return folder;
        }
        return null;
    }

    // Download file trace
    private IResource downloadFileTrace(IFileStore trace, IFolder traceFolder, IProgressMonitor monitor) throws CoreException, IOException, InterruptedException {

        IFolder folder = traceFolder;
        String traceName = trace.getName();

        traceName = TmfTraceCoreUtils.validateName(traceName);

        IResource resource = folder.findMember(traceName);
        if ((resource != null) && resource.exists()) {
            String newName = fConflictHandler.checkAndHandleNameClash(resource.getFullPath(), monitor);
            if (newName == null) {
                return null;
            }
            traceName = newName;
        }
        SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
        subMonitor.beginTask(RemoteMessages.RemoteImportTracesOperation_DownloadTask, 1);

        IPath destination = folder.getLocation().addTrailingSeparator().append(traceName);
        IFileInfo info = trace.fetchInfo();
        subMonitor.setTaskName(RemoteMessages.RemoteImportTracesOperation_DownloadTask + ' ' + trace.getName() + '/' + trace.getName());
        try (InputStream in = trace.openInputStream(EFS.NONE, new NullProgressMonitor())) {
            copy(in, folder, destination, subMonitor, info.getLength());
        }
        folder.refreshLocal(IResource.DEPTH_INFINITE, null);
        return folder.findMember(traceName);
    }

    private void copy(InputStream in, IFolder destFolder, IPath destination, SubMonitor monitor, long length) throws IOException {
        IFolder intermediateTempFolder = null;
        IFile tempFile = null;
        File intermediateFile = null;
        try {
            intermediateTempFolder = fDestination.getProject().getResource().getFolder(TRACE_IMPORT);
            if (intermediateTempFolder.exists()) {
                intermediateTempFolder.delete(true, SubMonitor.convert(monitor));
            }
            intermediateTempFolder.create(true, true, SubMonitor.convert(monitor));
            tempFile = intermediateTempFolder.getFile(destination.lastSegment());
            tempFile.create(null, true, SubMonitor.convert(monitor));
            intermediateFile = tempFile.getLocation().toFile();
            intermediateFile.createNewFile();
            copy(in, intermediateFile, length, monitor);
            if (ArchiveUtil.isArchiveFile(intermediateFile)) {
                // Select all the elements in the archive
                FileSystemObjectImportStructureProvider importProvider = new FileSystemObjectImportStructureProvider(FileSystemStructureProvider.INSTANCE, null);
                IFileSystemObject fileSystemObject = importProvider.getIFileSystemObject(intermediateFile);
                TraceFileSystemElement rootTraceFileElement = TraceFileSystemElement.createRootTraceFileElement(fileSystemObject, importProvider);

                // Select all the elements in the archive
                List<TraceFileSystemElement> list = new ArrayList<>();
                rootTraceFileElement.getAllChildren(list);
                if (!destFolder.exists()) {
                    destFolder.create(true, true, SubMonitor.convert(monitor));
                }
                final IFolder folder = destFolder.getFolder(destination.lastSegment());
                if (!folder.exists()) {
                    folder.create(true, true, SubMonitor.convert(monitor));
                }

                final TraceValidateAndImportOperation operation = new TraceValidateAndImportOperation(
                        fShell, list, null, intermediateTempFolder.getLocation(), destFolder.getFullPath(), false,
                        ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE | ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES,
                        fDestination, null, null);
                operation.setConflictHandler(fConflictHandler);
                operation.run(SubMonitor.convert(monitor));
                monitor.done();
            } else {
                // should be lightning fast unless someone maps different files
                // to different physical disks. In windows and linux, moves are
                // super fast on the same drive
                Files.move(intermediateFile.toPath(), destination.toFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (CoreException | InvocationTargetException | InterruptedException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        } finally {
            if (intermediateFile != null && intermediateFile.exists()) {
                intermediateFile.delete();
            }
            try {
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete(true, SubMonitor.convert(monitor));
                }
                if (intermediateTempFolder != null && intermediateTempFolder.exists()) {
                    intermediateTempFolder.delete(true, SubMonitor.convert(monitor));
                }
            } catch (CoreException e) {
                Activator.getDefault().logError(e.getMessage(), e);
            }
        }
    }

    private static void copy(InputStream in, File intermediateFile, long length, SubMonitor monitor) throws IOException, FileNotFoundException {
        try (OutputStream out = new FileOutputStream(intermediateFile)) {
            monitor.setWorkRemaining((int) (length / BYTES_PER_KB));
            byte[] buf = new byte[BYTES_PER_KB * BUFFER_IN_KB];
            int counter = 0;
            for (;;) {
                int n = in.read(buf);
                if (n <= 0) {
                    break;
                }
                out.write(buf, 0, n);
                counter = (counter % BYTES_PER_KB) + n;
                monitor.worked(counter / BYTES_PER_KB);
            }
        }
    }

    /**
     * Set the result status for this operation
     *
     * @param status
     *            the status
     */
    protected void setStatus(IStatus status) {
        fStatus = status;
    }

    /**
     * Gets the result of the operation.
     *
     * @return result status of operation
     */
    public IStatus getStatus() {
        return fStatus;
    }

    /**
     * Get the list of resources that were imported by this operation. An
     * example use case would be to use this to open traces that were imported
     * by this operation.
     *
     * Note this includes only valid traces and doesn'tinclude unrecognized
     * files.
     *
     * @return the trace resources that were imported
     */
    public List<IResource> getImportedResources() {
        return fImportedResources;
    }
}
