/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *     Patrick Tasse - Add support for source location
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.importexport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.AbstractTracePackageOperation;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageBookmarkElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageSupplFileElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageSupplFilesElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.core.util.Pair;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceTypeUIUtils;
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

            TracePackageFilesElement traceFilesElement = null;
            for (TracePackageElement packageElement : fImportTraceElements) {
                TracePackageTraceElement traceElement = (TracePackageTraceElement) packageElement;
                if (!isFilesChecked(packageElement)) {
                    continue;
                }

                TracePackageElement[] children = traceElement.getChildren();
                for (TracePackageElement element : children) {
                    ModalContext.checkCanceled(progressMonitor);

                    if (element instanceof TracePackageFilesElement) {
                        traceFilesElement = (TracePackageFilesElement) element;
                        setStatus(importTraceFiles(traceFilesElement, traceElement, progressMonitor));

                    } else if (element instanceof TracePackageSupplFilesElement) {
                        TracePackageSupplFilesElement suppFilesElement = (TracePackageSupplFilesElement) element;
                        setStatus(importSupplFiles(suppFilesElement, traceElement, progressMonitor));
                    }

                    if (getStatus().getSeverity() != IStatus.OK) {
                        return;
                    }
                }
            }

        } catch (InterruptedException e) {
            setStatus(Status.CANCEL_STATUS);
        }
    }

    /**
     * Returns whether or not the Files element is checked under the given trace
     * package element
     *
     * @param tracePackageElement
     *            the trace package element
     * @return whether or not the Files element is checked under the given trace
     *         package element
     */
    public static boolean isFilesChecked(TracePackageElement tracePackageElement) {
        for (TracePackageElement element : tracePackageElement.getChildren()) {
            if (element instanceof TracePackageFilesElement) {
                return element.isChecked();
            }
        }

        return false;
    }

    /**
     * Return the matching TmfTraceElement for a given trace element.
     */
    private TmfTraceElement getMatchingTraceElement(TracePackageTraceElement tracePackageElement) {
        IPath tracePath = fTmfTraceFolder.getPath().append(tracePackageElement.getDestinationElementPath());
        List<TmfTraceElement> traces = fTmfTraceFolder.getTraces();
        for (TmfTraceElement t : traces) {
            if (t.getPath().equals(tracePath)) {
                return t;
            }
        }

        return null;
    }

    private IStatus deleteExistingTraces(IProgressMonitor progressMonitor) {
        for (TracePackageElement packageElement : fImportTraceElements) {
            TracePackageTraceElement traceElement = (TracePackageTraceElement) packageElement;
            if (!isFilesChecked(traceElement)) {
                continue;
            }

            TmfTraceElement existingTrace = getMatchingTraceElement(traceElement);
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
                TmfTraceElement tmfTraceElement = getMatchingTraceElement(traceElement);
                if (tmfTraceElement != null) {
                    try {
                        bookmarksFile = tmfTraceElement.createBookmarksFile();

                        // Make sure that if a bookmark is double-clicked first
                        // before opening the trace, it opens the right editor

                        // Get the editor id from the extension point
                        String traceEditorId = tmfTraceElement.getEditorId();
                        final String editorId = (traceEditorId != null) ? traceEditorId : TmfEventsEditor.ID;
                        IDE.setDefaultEditor(bookmarksFile, editorId);

                    } catch (CoreException e) {
                        Activator.getDefault().logError(MessageFormat.format(Messages.TracePackageImportOperation_ErrorCreatingBookmarkFile, traceRes.getName()), e);
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

    private IStatus importTraceFiles(TracePackageFilesElement traceFilesElement, TracePackageTraceElement traceElement, IProgressMonitor monitor) {
        List<Pair<String, String>> fileNameAndLabelPairs = new ArrayList<>();

        String sourceName = traceFilesElement.getFileName();
        String destinationName = traceElement.getImportName();

        fileNameAndLabelPairs.add(new Pair<>(sourceName, destinationName));

        IPath containerPath = fTmfTraceFolder.getPath();
        IStatus status = importFiles(getSpecifiedArchiveFile(), fileNameAndLabelPairs, containerPath, Path.EMPTY, monitor);
        if (getStatus().getSeverity() != IStatus.OK) {
            return status;
        }

        // We need to set the trace type before importing the supplementary files so we do it here
        IResource traceRes = fTmfTraceFolder.getResource().findMember(traceElement.getDestinationElementPath());
        if (traceRes == null || !traceRes.exists()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, MessageFormat.format(Messages.ImportTracePackageWizardPage_ErrorFindingImportedTrace, destinationName));
        }

        TraceTypeHelper traceType = null;
        String traceTypeStr = traceElement.getTraceType();
        if (traceTypeStr != null) {
            traceType = TmfTraceType.getTraceType(traceTypeStr);
            if (traceType == null) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, MessageFormat.format(Messages.ImportTracePackageWizardPage_ErrorSettingTraceType, traceElement.getTraceType(), destinationName));
            }
        } else {
            try {
                monitor.subTask(MessageFormat.format(Messages.TracePackageImportOperation_DetectingTraceType, destinationName));
                traceType = TmfTraceTypeUIUtils.selectTraceType(traceRes.getLocation().toOSString(), null, null);
            } catch (TmfTraceImportException e) {
                // Could not figure out the type
            }
        }

        if (traceType != null) {
            try {
                TmfTraceTypeUIUtils.setTraceType(traceRes, traceType);
            } catch (CoreException e) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, MessageFormat.format(Messages.ImportTracePackageWizardPage_ErrorSettingTraceType, traceElement.getTraceType(), destinationName), e);
            }
        }

        importBookmarks(traceRes, traceElement, monitor);

        try {
            URI uri = new File(getFileName()).toURI();
            IPath entryPath = new Path(traceFilesElement.getFileName());
            if (traceRes instanceof IFolder) {
                entryPath = entryPath.addTrailingSeparator();
            }
            String sourceLocation = URIUtil.toUnencodedString(URIUtil.toJarURI(uri, entryPath));
            traceRes.setPersistentProperty(TmfCommonConstants.SOURCE_LOCATION, sourceLocation);
        } catch (CoreException e) {
        }

        return status;
    }

    private IStatus importSupplFiles(TracePackageSupplFilesElement suppFilesElement, TracePackageTraceElement traceElement, IProgressMonitor monitor) {
        List<Pair<String, String>> fileNameAndLabelPairs = new ArrayList<>();
        for (TracePackageElement child : suppFilesElement.getChildren()) {
            TracePackageSupplFileElement supplFile = (TracePackageSupplFileElement) child;
            fileNameAndLabelPairs.add(new Pair<>(supplFile.getText(), new Path(supplFile.getText()).lastSegment()));
        }

        if (!fileNameAndLabelPairs.isEmpty()) {
            TmfTraceElement existingTrace = getMatchingTraceElement(traceElement);
            if (existingTrace != null) {
                ArchiveFile archiveFile = getSpecifiedArchiveFile();
                existingTrace.refreshSupplementaryFolder();
                // Project/Traces/A/B -> A/B
                IPath traceFolderRelativePath = fTmfTraceFolder.getPath().makeRelativeTo(fTmfTraceFolder.getProject().getTracesFolder().getPath());
                // Project/.tracing/A/B/
                IFolder traceSupplementaryFolder = fTmfTraceFolder.getTraceSupplementaryFolder(traceFolderRelativePath.toString());
                IPath destinationContainerPath = traceSupplementaryFolder.getFullPath();
                // Remove the .tracing segment at the beginnin so that a file in folder .tracing/A/B/ imports destinationContainerPath/A/B/
                Path baseSourcePath = new Path(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER_NAME);
                return importFiles(archiveFile, fileNameAndLabelPairs, destinationContainerPath, baseSourcePath, monitor);
            }
        }

        return Status.OK_STATUS;
    }

    private IStatus importFiles(ArchiveFile archiveFile, List<Pair<String, String>> fileNameAndLabelPairs, IPath destinationContainerPath, IPath baseSourcePath, IProgressMonitor monitor) {
        List<ArchiveProviderElement> objects = new ArrayList<>();
        Enumeration<?> entries = archiveFile.entries();
        while (entries.hasMoreElements()) {
            ArchiveEntry entry = (ArchiveEntry) entries.nextElement();
            String entryName = entry.getName();
            IPath fullArchivePath = new Path(entryName);
            if (fullArchivePath.hasTrailingSeparator()) {
                // We only care about file entries as the folders will get created by the ImportOperation
                continue;
            }

            for (Pair<String, String> fileNameAndLabel : fileNameAndLabelPairs) {

                // Examples: Traces/aaa/kernel/     .tracing/aaa/testtexttrace.txt/statistics.ht
                IPath searchedArchivePath = new Path(fileNameAndLabel.getFirst());

                // Check if this archive entry matches the searched file name at this archive location
                boolean fileMatch = entryName.equalsIgnoreCase(searchedArchivePath.toString());
                // For example Traces/aaa/kernel/metadata matches Traces/aaa/kernel/
                boolean folderMatch = entryName.startsWith(searchedArchivePath + "/"); //$NON-NLS-1$

                if (fileMatch || folderMatch) {
                    // .tracing/aaa/testtexttrace.txt/statistics.ht -> aaa/testtexttrace.txt/statistics.ht
                    IPath destinationPath = fullArchivePath.makeRelativeTo(baseSourcePath);

                    // metadata    statistics.ht
                    // We don't use the label when the entry is a folder match because the labels for individual files
                    // under the folder are not specified in the manifest so just use the last segment.
                    String resourceLabel = folderMatch ? fullArchivePath.lastSegment() : fileNameAndLabel.getSecond();

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
            operation.run(new SubProgressMonitor(monitor, fileNameAndLabelPairs.size(), SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
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
