/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.tracecompass.internal.tmf.remote.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.remote.ui.messages.RemoteMessages;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceCoreUtils;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.remote.core.proxy.RemoteSystemProxy;

/**
 * An operation that generates the manifest based on a profile and the content
 * of a remote node.
 *
 * @author Bernd Hufmann
 */
public class RemoteGenerateManifestOperation extends AbstractGenerateManifestOperation {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private RemoteImportProfileElement fProfile;
    private Set<IPath> fDirectoryTraces = new HashSet<>();

    // ------------------------------------------------------------------------
    // Constructor(s)
    // ------------------------------------------------------------------------
    /**
     * Constructs a new import operation for generating the manifest
     *
     * @param profile
     *            the input profile element
     */
    public RemoteGenerateManifestOperation(RemoteImportProfileElement profile) {
        super(profile.getText());
        fProfile = profile;
    }

    // ------------------------------------------------------------------------
    // Operation(s)
    // ------------------------------------------------------------------------
    @Override
    public void run(IProgressMonitor monitor) {
        try {
            monitor.worked(1);
            String root = null;
            List<TracePackageElement> resultElementList = new ArrayList<>();
            SubMonitor subMonitor = SubMonitor.convert(monitor, fProfile.getChildren().length * 2);

            List<RemoteImportConnectionNodeElement> connectionNodes = fProfile.getConnectionNodeElements();
            for (RemoteImportConnectionNodeElement connectionNode : connectionNodes) {
                RemoteSystemProxy proxy = connectionNode.getRemoteSystemProxy();
                // create new element to decouple from input element
                RemoteImportConnectionNodeElement outputConnectionNode =
                        new RemoteImportConnectionNodeElement(null, connectionNode.getName(), connectionNode.getURI());
                resultElementList.add(outputConnectionNode);
                for (TracePackageElement element : connectionNode.getChildren()) {
                    if (element instanceof RemoteImportTraceGroupElement) {
                        ModalContext.checkCanceled(monitor);
                        RemoteImportTraceGroupElement traceGroup = (RemoteImportTraceGroupElement) element;
                        root = traceGroup.getRootImportPath();
                        TracePackageElement[] traceElements = traceGroup.getChildren();
                        fTemplatePatternsToTraceElements = generatePatterns(traceElements);
                        IRemoteFileService fs = proxy.getRemoteConnection().getService(IRemoteFileService.class);
                        if (fs == null) {
                            continue;
                        }

                        final IFileStore remoteFolder = fs.getResource(root);

                        // make sure that remote directory is read and not cached
                        int recursionLevel = 0;
                        // create new element to decouple from input element
                        RemoteImportTraceGroupElement outputTraceGroup =
                                new RemoteImportTraceGroupElement(outputConnectionNode, traceGroup.getRootImportPath());
                        outputTraceGroup.setRecursive(traceGroup.isRecursive());
                        generateElementsFromArchive(outputTraceGroup, outputTraceGroup, remoteFolder, recursionLevel, subMonitor.newChild(1));
                        filterElements(outputTraceGroup);
                    }
                }
            }
            setResultElements(resultElementList.toArray(new TracePackageElement[0]));
            setStatus(Status.OK_STATUS);
        } catch (InterruptedException e) {
            setStatus(Status.CANCEL_STATUS);
        } catch (Exception e) {
            setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind(RemoteMessages.RemoteGenerateManifest_GenerateProfileManifestError, fProfile.getText()), e));
        }
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    /**
     * Scan traceFolder for files that match the patterns specified in the
     * template file. When there is a match, the trace package element is used
     * to determine the trace name and trace type.
     *
     * @param traceGroup
     *                The parent trace group element
     * @param parentElement
     *                The immediate parent trace group or folder element
     * @param traceFolder
     *                The folder to scan
     * @param recursionLevel
     *                The recursion level (needed to find directory traces under the traceFolder
     * @param monitor
     *                The progress monitor
     * @throws CoreException
     *                Thrown by the file system implementation
     * @throws InterruptedException
     *                Thrown if operation was cancelled
     */
    private void generateElementsFromArchive(
            final RemoteImportTraceGroupElement traceGroup,
            final TracePackageElement parentElement,
            final IFileStore traceFolder,
            final int recursionLevel,
            IProgressMonitor monitor)
                    throws CoreException, InterruptedException {

        int localRecursionLevel = recursionLevel + 1;
        IFileStore[] sources = traceFolder.childStores(EFS.NONE, monitor);

        for (int i = 0; i < sources.length; i++) {
            ModalContext.checkCanceled(monitor);
            SubMonitor subMonitor = SubMonitor.convert(monitor, sources.length);

            IFileStore fileStore = sources[i];
            IPath fullArchivePath = TmfTraceCoreUtils.newSafePath(fileStore.toURI().getPath());

            IFileInfo sourceInfo = fileStore.fetchInfo();
            if (!sourceInfo.isDirectory()) {

                String rootPathString = traceGroup.getRootImportPath();
                IPath rootPath = TmfTraceCoreUtils.newSafePath(rootPathString);
                IPath relativeTracePath = Path.EMPTY;
                if (rootPath.isPrefixOf(fullArchivePath)) {
                    relativeTracePath = fullArchivePath.makeRelativeTo(rootPath);
                }
                Entry<Pattern, TracePackageTraceElement> matchingTemplateEntry = getMatchingTemplateElement(relativeTracePath);
                if (matchingTemplateEntry != null) {
                    TracePackageTraceElement matchingTemplateElement = matchingTemplateEntry.getValue();
                    String traceType = matchingTemplateElement.getTraceType();

                    // If a single file is part of a directory trace, use the parent directory instead
                    TracePackageElement parent = parentElement;
                    if (matchesDirectoryTrace(relativeTracePath, matchingTemplateEntry)) {
                        fullArchivePath = fullArchivePath.removeLastSegments(1);
                        fDirectoryTraces.add(fullArchivePath);
                        fileStore = fileStore.getParent();
                        sourceInfo = fileStore.fetchInfo();
                        parent = parentElement.getParent();
                        // Let the auto-detection choose the best trace type
                        traceType = null;
                    } else if ((localRecursionLevel > 1) && (!traceGroup.isRecursive())) {
                        // Don't consider file traces on level 2 if it's not recursive
                        continue;
                    }

                    if (sourceInfo.getLength() > 0 || sourceInfo.isDirectory()) {
                        // Only add non-empty files
                        String traceName = fullArchivePath.lastSegment();
                        String fileName = fileStore.getName();
                        // create new elements to decouple from input elements
                        TracePackageTraceElement traceElement = new TracePackageTraceElement(parent, traceName, traceType);
                        RemoteImportTraceFilesElement tracePackageFilesElement = new RemoteImportTraceFilesElement(traceElement, fileName, fileStore);
                        tracePackageFilesElement.setVisible(false);
                    }
                }
            } else {
                if (traceGroup.isRecursive() || localRecursionLevel < 2) {
                    RemoteImportFolderElement folder = new RemoteImportFolderElement(parentElement, fileStore.getName());
                    generateElementsFromArchive(traceGroup, folder, fileStore, localRecursionLevel, subMonitor);
                }
            }
        }
    }

    /*
     * Filter elements to avoid having files of directory traces listed as
     * separate traces.
     */
    private void filterElements(TracePackageElement parentElement) {
        for (TracePackageElement childElement : parentElement.getChildren()) {
            filterElements(childElement);
            if (childElement instanceof TracePackageTraceElement) {
                // no need to do length check here
                RemoteImportTraceFilesElement filesElement = (RemoteImportTraceFilesElement) childElement.getChildren()[0];
                IFileStore parentFile = filesElement.getRemoteFile().getParent();
                if (fDirectoryTraces.contains(TmfTraceCoreUtils.newSafePath(parentFile.toURI().getPath()))) {
                    removeChild(childElement, parentElement);
                    continue;
                }
                IFileStore grandParentFile = parentFile.getParent();
                if (grandParentFile != null && fDirectoryTraces.contains(TmfTraceCoreUtils.newSafePath(grandParentFile.toURI().getPath()))) {
                    // ignore file if grandparent is a directory trace
                    // for example: file is a index file of a LTTng kernel trace
                    parentElement.removeChild(childElement);
                    if (parentElement.getChildren().length == 0) {
                        TracePackageElement grandParentElement = parentElement.getParent();
                        removeChild(parentElement, grandParentElement);
                    }
                    continue;
                }
            } else if (childElement instanceof RemoteImportFolderElement) {
                if (childElement.getChildren().length == 0) {
                    parentElement.removeChild(childElement);
                }
            }
        }
    }

    private static void removeChild(TracePackageElement childElement, TracePackageElement parentElement) {
        parentElement.removeChild(childElement);
        if (parentElement.getChildren().length == 0) {
            if (parentElement.getParent() != null) {
                parentElement.getParent().removeChild(parentElement);
            }
        }
    }

    /**
     * This method takes the auto-detection case into consideration.
     *
     * {@inheritDoc}
     */
    @Override
    protected Entry<Pattern, TracePackageTraceElement> getMatchingTemplateElement(IPath fullArchivePath) {
        for (Entry<Pattern, TracePackageTraceElement> entry : fTemplatePatternsToTraceElements.entrySet()) {
            // Check for CTF trace (metadata)
            String traceType = entry.getValue().getTraceType();
            // empty string is for auto-detection
            if ((traceType.isEmpty() || TmfTraceType.isDirectoryTraceType(traceType)) &&
                (matchesDirectoryTrace(fullArchivePath, entry))) {
                return entry;
            } else if (entry.getKey().matcher(TmfTraceCoreUtils.safePathToString(fullArchivePath.toString())).matches()) {
                return entry;
            }
        }

        return null;
    }

    /**
     * This method takes the auto-detection case into consideration.
     *
     * {@inheritDoc}
     */
    @Override
    protected boolean matchesDirectoryTrace(IPath archivePath, Entry<Pattern, TracePackageTraceElement> entry) {
        if (METADATA_FILE_NAME.equals(archivePath.lastSegment())) {
            IPath archiveParentPath = archivePath.removeLastSegments(1);
            String parentPathString = TmfTraceCoreUtils.safePathToString(archiveParentPath.toString());
            if (entry.getKey().matcher(parentPathString).matches()) {
                String traceType = entry.getValue().getTraceType();
                // empty string is for auto-detection
                if (traceType.isEmpty() || TmfTraceType.isDirectoryTraceType(traceType)) {
                    return true;
                }
            }
        }
        return false;
    }
}
