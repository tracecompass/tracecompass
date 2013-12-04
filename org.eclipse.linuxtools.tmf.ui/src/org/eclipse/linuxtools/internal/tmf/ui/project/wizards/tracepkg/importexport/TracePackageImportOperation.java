/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.importexport;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.project.model.TmfTraceImportException;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.AbstractTracePackageOperation;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageBookmarkElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageSupplFileElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageSupplFilesElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfNavigatorContentProvider;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceTypeHelper;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * An operation that imports a trace package from an archive
 *
 * @author Marc-Andre Laperle
 */
@SuppressWarnings("restriction")
public class TracePackageImportOperation extends AbstractTracePackageOperation implements IOverwriteQuery {

    private final TracePackageElement[] fImportTraceElements;
    private final TmfTraceFolder fTmfTraceFolder;

    /**
     * Constructs a new import operation
     *
     * @param importTraceElements
     *            the trace element to be imported
     * @param fileName
     *            the output file name
     * @param tmfTraceFolder
     *            the destination folder
     */
    public TracePackageImportOperation(String fileName, TracePackageElement[] importTraceElements, TmfTraceFolder tmfTraceFolder) {
        super(fileName);
        fImportTraceElements = importTraceElements;
        fTmfTraceFolder = tmfTraceFolder;
    }

    private class ImportProvider implements IImportStructureProvider {

        private Exception fException;

        @Override
        public List getChildren(Object element) {
            return null;
        }

        @Override
        public InputStream getContents(Object element) {
            InputStream inputStream = null;
            // We can add throws
            try {
                inputStream = ((ArchiveProviderElement) element).getContents();
            } catch (IOException e) {
                fException = e;
            } catch (TarException e) {
                fException = e;
            }
            return inputStream;
        }

        @Override
        public String getFullPath(Object element) {
            return ((ArchiveProviderElement) element).getFullPath();
        }

        @Override
        public String getLabel(Object element) {
            return ((ArchiveProviderElement) element).getLabel();
        }

        @Override
        public boolean isFolder(Object element) {
            return ((ArchiveProviderElement) element).isFolder();
        }

        public Exception getException() {
            return fException;
        }
    }

    private class ArchiveProviderElement {

        private final String fPath;
        private final String fLabel;

        private ArchiveFile fArchiveFile;
        private ArchiveEntry fEntry;

        public ArchiveProviderElement(String destinationPath, String label, ArchiveFile archiveFile, ArchiveEntry entry) {
            fPath = destinationPath;
            fLabel = label;
            this.fArchiveFile = archiveFile;
            this.fEntry = entry;
        }

        public InputStream getContents() throws TarException, IOException {
            return fArchiveFile.getInputStream(fEntry);
        }

        public String getFullPath() {
            return fPath;
        }

        public String getLabel() {
            return fLabel;
        }

        public boolean isFolder() {
            return false;
        }
    }

    /**
     * Run the operation. The status (result) of the operation can be obtained
     * with {@link #getStatus}
     *
     * @param progressMonitor
     *            the progress monitor to use to display progress and receive
     *            requests for cancellation
     */
    @Override
    public void run(IProgressMonitor progressMonitor) {
        int totalWork = getNbCheckedElements(fImportTraceElements) * 2;
        progressMonitor.beginTask(Messages.TracePackageImportOperation_ImportingPackage, totalWork);
        doRun(progressMonitor);
        progressMonitor.done();
    }

    private void doRun(IProgressMonitor progressMonitor) {
        try {
            setStatus(deleteExistingTraces(progressMonitor));
            if (getStatus().getSeverity() != IStatus.OK) {
                return;
            }

            for (TracePackageElement packageElement : fImportTraceElements) {
                TracePackageTraceElement traceElement = (TracePackageTraceElement) packageElement;
                if (!isFilesChecked(packageElement)) {
                    continue;
                }

                TracePackageElement[] children = traceElement.getChildren();
                for (TracePackageElement element : children) {
                    ModalContext.checkCanceled(progressMonitor);

                    if (element instanceof TracePackageFilesElement) {
                        TracePackageFilesElement traceFilesElement = (TracePackageFilesElement) element;
                        setStatus(importTraceFiles(traceFilesElement, progressMonitor));

                    } else if (element instanceof TracePackageSupplFilesElement) {
                        TracePackageSupplFilesElement suppFilesElement = (TracePackageSupplFilesElement) element;
                        setStatus(importSupplFiles(suppFilesElement, traceElement, progressMonitor));
                    }

                    if (getStatus().getSeverity() != IStatus.OK) {
                        return;
                    }
                }

                String traceName = traceElement.getText();
                IResource traceRes = fTmfTraceFolder.getResource().findMember(traceName);
                if (traceRes == null || !traceRes.exists()) {
                    setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, MessageFormat.format(Messages.ImportTracePackageWizardPage_ErrorFindingImportedTrace, traceName)));
                    return;
                }

                TraceTypeHelper traceType = null;
                String traceTypeStr = traceElement.getTraceType();
                if (traceTypeStr != null) {
                    traceType = TmfTraceType.getInstance().getTraceType(traceTypeStr);
                    if (traceType == null) {
                        setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, MessageFormat.format(Messages.ImportTracePackageWizardPage_ErrorSettingTraceType, traceElement.getTraceType(), traceName)));
                        return;
                    }
                } else {
                    try {
                        progressMonitor.subTask(MessageFormat.format(Messages.TracePackageImportOperation_DetectingTraceType, traceName));
                        traceType = TmfTraceType.getInstance().selectTraceType(traceRes.getLocation().toOSString(), null, null);
                    } catch (TmfTraceImportException e) {
                        // Could not figure out the type
                    }
                }

                if (traceType != null) {
                    try {
                        TmfTraceType.setTraceType(traceRes.getFullPath(), traceType);
                    } catch (CoreException e) {
                        setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, MessageFormat.format(Messages.ImportTracePackageWizardPage_ErrorSettingTraceType, traceElement.getTraceType(), traceName), e));
                    }
                }

                importBookmarks(traceRes, traceElement, progressMonitor);
            }



        } catch (InterruptedException e) {
            setStatus(Status.CANCEL_STATUS);
        }
    }

    private IStatus deleteExistingTraces(IProgressMonitor progressMonitor) {
        List<TmfTraceElement> traces = fTmfTraceFolder.getTraces();

        for (TracePackageElement packageElement : fImportTraceElements) {
            TracePackageTraceElement traceElement = (TracePackageTraceElement) packageElement;
            if (!isFilesChecked(traceElement)) {
                continue;
            }

            TmfTraceElement existingTrace = null;
            for (TmfTraceElement t : traces) {
                if (t.getName().equals(traceElement.getText())) {
                    existingTrace = t;
                    break;
                }
            }

            if (existingTrace != null) {
                try {
                    existingTrace.delete(new SubProgressMonitor(progressMonitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
                } catch (CoreException e) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_ErrorOperation, e);
                }
            }
        }

        return Status.OK_STATUS;
    }

    private void importBookmarks(IResource traceRes, TracePackageTraceElement traceElement, IProgressMonitor monitor) {
        for (TracePackageElement o : traceElement.getChildren()) {
            if (o instanceof TracePackageBookmarkElement && o.isChecked()) {

                // Get element
                IFile bookmarksFile = null;
                List<TmfTraceElement> traces = fTmfTraceFolder.getTraces();
                for (TmfTraceElement t : traces) {
                    if (t.getName().equals(traceRes.getName())) {
                        try {
                            bookmarksFile = t.createBookmarksFile();

                            // Make sure that if a bookmark is double-clicked first
                            // before opening the trace, it opens the right editor

                            // Get the editor id from the extension point
                            String traceEditorId = t.getEditorId();
                            final String editorId = (traceEditorId != null) ? traceEditorId : TmfEventsEditor.ID;
                            IDE.setDefaultEditor(bookmarksFile, editorId);

                        } catch (CoreException e) {
                            Activator.getDefault().logError(MessageFormat.format(Messages.TracePackageImportOperation_ErrorCreatingBookmarkFile, traceRes.getName()), e);
                        }
                        break;
                    }
                }

                if (bookmarksFile == null) {
                    break;
                }

                TracePackageBookmarkElement bookmarkElement = (TracePackageBookmarkElement) o;

                List<Map<String, String>> bookmarks = bookmarkElement.getBookmarks();
                for (Map<String, String> attrs : bookmarks) {
                    IMarker createMarker = null;
                    try {
                        createMarker = bookmarksFile.createMarker(IMarker.BOOKMARK);
                    } catch (CoreException e) {
                        Activator.getDefault().logError(MessageFormat.format(Messages.TracePackageImportOperation_ErrorCreatingBookmark, traceRes.getName()), e);
                    }
                    if (createMarker != null && createMarker.exists()) {
                        try {
                            for (String key : attrs.keySet()) {
                                String value = attrs.get(key);
                                if (key.equals(IMarker.LOCATION)) {
                                    createMarker.setAttribute(IMarker.LOCATION, Integer.valueOf(value).intValue());
                                } else {
                                    createMarker.setAttribute(key, value);
                                }
                            }
                        } catch (CoreException e) {
                            Activator.getDefault().logError(MessageFormat.format(Messages.TracePackageImportOperation_ErrorCreatingBookmark, traceRes.getName()), e);
                        }
                    }
                }
            }
        }

        monitor.worked(1);
    }

    private static boolean fileNameMatches(String fileName, String entryName) {
        boolean fileMatch = entryName.equalsIgnoreCase(fileName);
        boolean folderMatch = entryName.startsWith(fileName + "/"); //$NON-NLS-1$
        return fileMatch || folderMatch;
    }

    private IStatus importTraceFiles(TracePackageFilesElement traceFilesElement, IProgressMonitor monitor) {
        List<String> fileNames = new ArrayList<String>();
        fileNames.add(traceFilesElement.getFileName());
        IPath containerPath = fTmfTraceFolder.getPath();
        IStatus status = importFiles(getSpecifiedArchiveFile(), fileNames, containerPath, monitor);
        if (status.isOK()) {
            new TmfNavigatorContentProvider().getChildren(fTmfTraceFolder);
        }
        return status;
    }

    private IStatus importSupplFiles(TracePackageSupplFilesElement suppFilesElement, TracePackageTraceElement traceElement, IProgressMonitor monitor) {
        List<String> fileNames = new ArrayList<String>();
        for (TracePackageElement child : suppFilesElement.getChildren()) {
            TracePackageSupplFileElement supplFile = (TracePackageSupplFileElement) child;
            fileNames.add(supplFile.getText());
        }

        if (!fileNames.isEmpty()) {
            List<TmfTraceElement> traces = fTmfTraceFolder.getTraces();
            TmfTraceElement tmfTraceElement = null;
            for (TmfTraceElement t : traces) {
                if (t.getName().equals(traceElement.getText())) {
                    tmfTraceElement = t;
                    break;
                }
            }

            if (tmfTraceElement != null) {
                ArchiveFile archiveFile = getSpecifiedArchiveFile();
                tmfTraceElement.refreshSupplementaryFolder();
                String traceName = tmfTraceElement.getResource().getName();
                // Project/.tracing/tracename
                IPath destinationContainerPath = tmfTraceElement.getTraceSupplementaryFolder(traceName).getFullPath();
                return importFiles(archiveFile, fileNames, destinationContainerPath, monitor);
            }
        }

        return Status.OK_STATUS;
    }

    private IStatus importFiles(ArchiveFile archiveFile, List<String> fileNames, IPath destinationContainerPath, IProgressMonitor monitor) {
        List<ArchiveProviderElement> objects = new ArrayList<ArchiveProviderElement>();
        Enumeration<?> entries = archiveFile.entries();
        while (entries.hasMoreElements()) {
            ArchiveEntry entry = (ArchiveEntry) entries.nextElement();
            String entryName = entry.getName();
            IPath fullArchivePath = new Path(entryName);
            if (fullArchivePath.hasTrailingSeparator()) {
                // We only care about file entries as the folders will get created by the ImportOperation
                continue;
            }

            for (String fileName : fileNames) {

                // Examples: Traces/kernel/     .tracing/testtexttrace.txt/statistics.ht
                IPath searchedArchivePath = new Path(fileName);

                // Check if this archive entry matches the searched file name at this archive location
                if (fileNameMatches(searchedArchivePath.toString(), entryName)) {
                    // Traces/     .tracing/testtexttrace.txt/
                    IPath searchedArchivePathContainer = searchedArchivePath.removeLastSegments(1);

                    // Traces/kernel/metadata -> kernel/metadata   .tracing/testtexttrace.txt/statistics.ht -> statistics.ht
                    // Note: The ImportOperation will take care of creating the kernel folder
                    IPath destinationPath = fullArchivePath.makeRelativeTo(searchedArchivePathContainer);
                    // metadata    statistics.ht
                    String resourceLabel = fullArchivePath.lastSegment();

                    ArchiveProviderElement pe = new ArchiveProviderElement(destinationPath.toString(), resourceLabel, archiveFile, entry);
                    objects.add(pe);
                    break;
                }
            }
        }

        ImportProvider provider = new ImportProvider();

        ImportOperation operation = new ImportOperation(destinationContainerPath,
                null, provider, this,
                objects);
        operation.setCreateContainerStructure(true);
        operation.setOverwriteResources(true);

        try {
            operation.run(new SubProgressMonitor(monitor, fileNames.size(), SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
            archiveFile.close();
        } catch (InvocationTargetException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_ErrorOperation, e);
        } catch (InterruptedException e) {
            return Status.CANCEL_STATUS;
        } catch (IOException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_ErrorOperation, e);
        }

        if (provider.getException() != null) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_ErrorOperation, provider.getException());
        }

        return operation.getStatus();
    }

    @Override
    public String queryOverwrite(String pathString) {
        // We always overwrite once we reach this point
        return null;
    }
}
