/*******************************************************************************
 * Copyright (c) 2013, 2018 Ericsson
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

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.importexport;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.NewExperimentOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.AbstractTracePackageOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageBookmarkElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageExperimentElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageSupplFileElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageSupplFilesElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.eclipse.tracecompass.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfCommonProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceTypeUIUtils;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * An operation that imports a trace package from an archive
 *
 * @author Marc-Andre Laperle
 */
public class TracePackageImportOperation extends AbstractTracePackageOperation implements IOverwriteQuery {

    private final TracePackageElement[] fImportTraceElements;
    private final TmfTraceFolder fTmfTraceFolder;
    private final @NonNull TmfExperimentFolder fTmfExperimentFolder;

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
        fTmfExperimentFolder = checkNotNull(tmfTraceFolder.getProject().getExperimentsFolder());
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

        public InputStream getContents() throws IOException {
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
            if (!getStatus().isOK()) {
                return;
            }

            List<TracePackageExperimentElement> experimentPackageElements = new ArrayList<>();
            for (TracePackageElement packageElement : fImportTraceElements) {

                TracePackageTraceElement traceElement = (TracePackageTraceElement) packageElement;
                if (!isFilesChecked(packageElement)) {
                    continue;
                }
                if (packageElement instanceof TracePackageExperimentElement) {
                    experimentPackageElements.add((TracePackageExperimentElement) packageElement);
                    continue;
                }
                TracePackageElement[] children = traceElement.getChildren();
                for (TracePackageElement element : children) {
                    ModalContext.checkCanceled(progressMonitor);

                    if (element instanceof TracePackageFilesElement) {
                        TracePackageFilesElement traceFilesElement = (TracePackageFilesElement) element;
                        setStatus(importTraceFiles(traceFilesElement, traceElement, progressMonitor));
                    }

                    if (!getStatus().isOK()) {
                        return;
                    }
                }
                setStatus(importSupplFiles(traceElement, progressMonitor));
                if (!getStatus().isOK()) {
                    return;
                }
            }
            for (TracePackageExperimentElement experimentPackageElement : experimentPackageElements) {
                ModalContext.checkCanceled(progressMonitor);
                importExperiment(experimentPackageElement, progressMonitor);
                if (!getStatus().isOK()) {
                    return;
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
    private TmfCommonProjectElement getMatchingProjectElement(TracePackageTraceElement tracePackageElement) {
        if (tracePackageElement instanceof TracePackageExperimentElement) {
            for (TmfExperimentElement experiment : fTmfExperimentFolder.getExperiments()) {
                if (experiment.getName().equals(tracePackageElement.getImportName())) {
                    return experiment;
                }
            }
        } else {
            IPath tracePath = fTmfTraceFolder.getPath().append(tracePackageElement.getDestinationElementPath());
            List<TmfTraceElement> traces = fTmfTraceFolder.getTraces();
            for (TmfTraceElement t : traces) {
                if (t.getPath().equals(tracePath)) {
                    return t;
                }
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

            TmfCommonProjectElement projectElement = getMatchingProjectElement(traceElement);
            try {
                if (projectElement instanceof TmfExperimentElement) {
                    Display.getDefault().syncExec(() -> projectElement.closeEditors());
                    projectElement.deleteSupplementaryFolder();
                    projectElement.getResource().delete(true, SubMonitor.convert(progressMonitor));
                } else if (projectElement instanceof TmfTraceElement) {
                    ((TmfTraceElement) projectElement).delete(SubMonitor.convert(progressMonitor), true);
                }
            } catch (CoreException e) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_ErrorOperation, e);
            }
        }

        return Status.OK_STATUS;
    }

    private void importBookmarks(IResource traceRes, TracePackageTraceElement traceElement, IProgressMonitor monitor) {
        for (TracePackageElement o : traceElement.getChildren()) {
            if (o instanceof TracePackageBookmarkElement && o.isChecked()) {

                // Get element
                IFile bookmarksFile = null;
                TmfCommonProjectElement projectElement = getMatchingProjectElement(traceElement);
                if (projectElement != null) {
                    try {
                        bookmarksFile = projectElement.createBookmarksFile(monitor);

                        // Make sure that if a bookmark is double-clicked first
                        // before opening the trace, it opens the right editor

                        // Get the editor id from the extension point
                        String traceEditorId = projectElement.getEditorId();
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
                            for (Entry<String, String> entry : attrs.entrySet()) {
                                String key = entry.getKey();
                                String value = entry.getValue();
                                if (key.equals(IMarker.LOCATION)) {
                                    try {
                                        /* try location as an integer for backward compatibility */
                                        createMarker.setAttribute(IMarker.LOCATION, Integer.parseInt(value));
                                    } catch (NumberFormatException e) {
                                        createMarker.setAttribute(IMarker.LOCATION, value);
                                    }
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

        String sourceName = checkNotNull(traceFilesElement.getFileName());
        String destinationName = checkNotNull(traceElement.getImportName());

        fileNameAndLabelPairs.add(new Pair<>(sourceName, destinationName));

        IPath containerPath = fTmfTraceFolder.getPath();
        IStatus status = importFiles(getSpecifiedArchiveFile(), fileNameAndLabelPairs, containerPath, Path.EMPTY, Collections.emptyMap(), monitor);
        if (!getStatus().isOK()) {
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

    private IStatus importSupplFiles(TracePackageTraceElement packageElement, IProgressMonitor monitor) {
        List<Pair<String, String>> fileNameAndLabelPairs = new ArrayList<>();
        Map<String, String> suppFilesRename = new HashMap<>();
        for (TracePackageElement element : packageElement.getChildren()) {
            if (element instanceof TracePackageSupplFilesElement) {
                for (TracePackageElement child : element.getChildren()) {
                    if (child.isChecked()) {
                        TracePackageSupplFileElement supplFile = (TracePackageSupplFileElement) child;
                        if (packageElement instanceof TracePackageExperimentElement) {
                            String oldExpName = getOldExpName(supplFile);
                            if (!packageElement.getImportName().equals(oldExpName)) {
                                suppFilesRename.put(supplFile.getText(), supplFile.getText().replace(oldExpName, packageElement.getImportName()));
                            }
                        }
                        fileNameAndLabelPairs.add(new Pair<>(checkNotNull(supplFile.getText()), checkNotNull(new Path(supplFile.getText()).lastSegment())));
                    }
                }
            }
        }

        if (!fileNameAndLabelPairs.isEmpty()) {
            TmfCommonProjectElement projectElement = getMatchingProjectElement(packageElement);
            if (projectElement != null) {
                ArchiveFile archiveFile = getSpecifiedArchiveFile();
                projectElement.refreshSupplementaryFolder();
                IPath destinationContainerPath;
                if (packageElement instanceof TracePackageExperimentElement) {
                    destinationContainerPath = projectElement.getProject().getSupplementaryFolder().getFullPath();
                } else {
                    // Project/Traces/A/B -> A/B
                    final TmfTraceFolder tracesFolder = fTmfTraceFolder.getProject().getTracesFolder();
                    if (tracesFolder == null) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_ErrorOperation);
                    }
                    IPath traceFolderRelativePath = fTmfTraceFolder.getPath().makeRelativeTo(tracesFolder.getPath());
                    // Project/.tracing/A/B/
                    IFolder traceSupplementaryFolder = fTmfTraceFolder.getTraceSupplementaryFolder(traceFolderRelativePath.toString());
                    destinationContainerPath = traceSupplementaryFolder.getFullPath();
                }
                // Remove the .tracing segment at the beginning so that a file
                // in folder .tracing/A/B/ imports to destinationContainerPath/A/B/
                Path baseSourcePath = new Path(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER_NAME);
                return importFiles(archiveFile, fileNameAndLabelPairs, destinationContainerPath, baseSourcePath, suppFilesRename, monitor);
            }
        }

        return Status.OK_STATUS;
    }

    private static String getOldExpName(TracePackageSupplFileElement supplFile) {
        String[] split = supplFile.getText().split("/"); //$NON-NLS-1$
        String oldExpName = null;
        for (int i = 0; i < split.length; i++) {
            // After .tracing is the experiment name
            if (split[i].equals(".tracing")) { //$NON-NLS-1$
                oldExpName = split[i + 1];
                // Remove suffix "_exp"
                oldExpName = oldExpName.substring(0, oldExpName.length() - 4);
            }
        }
        return oldExpName;
    }

    private IStatus importFiles(ArchiveFile archiveFile, List<Pair<String, String>> fileNameAndLabelPairs, IPath destinationContainerPath, IPath baseSourcePath, @NonNull Map<String, String> filesRenameMap, IProgressMonitor monitor) {
        List<ArchiveProviderElement> objects = new ArrayList<>();
        Enumeration<@NonNull ?> entries = archiveFile.entries();
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
                    String newFilePath = filesRenameMap.get(entryName);
                    if (newFilePath != null) {
                        fullArchivePath = new Path(newFilePath);
                    }
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
            operation.run(SubMonitor.convert(monitor));
            archiveFile.close();
        } catch (InvocationTargetException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_ErrorOperation, e);
        } catch (InterruptedException e) {
            return Status.CANCEL_STATUS;
        } catch (IOException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_ErrorOperation, e);
        }

        if (provider.getException() != null) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_ErrorOperation, provider.getException());
        }

        return operation.getStatus();
    }

    private void importExperiment(TracePackageExperimentElement experimentPackageElement, IProgressMonitor progressMonitor) {
        SubMonitor subMonitor = SubMonitor.convert(progressMonitor, 3);
        String name = checkNotNull(experimentPackageElement.getImportName());
        String experimentType = experimentPackageElement.getTraceType();
        List<TmfTraceElement> traces = new ArrayList<>();
        List<TmfTraceElement> traceElements = fTmfTraceFolder.getTraces();
        boolean allFound = true;
        for (String expTrace : experimentPackageElement.getExpTraces()) {
            boolean found = false;
            for (TmfTraceElement traceElement : traceElements) {
                if (traceElement.getPath().makeRelativeTo(fTmfTraceFolder.getPath()).toString().equals(expTrace)) {
                    traces.add(traceElement);
                    found = true;
                    break;
                }
            }
            allFound &= found;
        }
        NewExperimentOperation newExperimentOperation = new NewExperimentOperation(fTmfExperimentFolder, name, experimentType, traces);
        newExperimentOperation.run(subMonitor.split(1));
        setStatus(newExperimentOperation.getStatus());
        if (!getStatus().isOK()) {
            return;
        }
        if (allFound) {
            importBookmarks(newExperimentOperation.getExperimentFolder(), experimentPackageElement, subMonitor.split(1));
            setStatus(importSupplFiles(experimentPackageElement, subMonitor.split(1)));
        }
        return;
    }

    @Override
    public String queryOverwrite(String pathString) {
        // We always overwrite once we reach this point
        return null;
    }
}
