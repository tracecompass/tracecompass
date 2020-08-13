/*******************************************************************************
 * Copyright (c) 2015, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.TmfWorkspaceModifyOperation;
import org.eclipse.tracecompass.internal.util.ByteBufferTracker;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceTypeUIUtils;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * An operation that performs validation and importing of traces. Its primary
 * inputs are a collection of TraceFileSystemElement and several flags that
 * control
 *
 */
public class TraceValidateAndImportOperation extends TmfWorkspaceModifyOperation {

    private static final String TRACE_IMPORT_TEMP_FOLDER = ".traceImport"; //$NON-NLS-1$

    /**
     * Folder where we extract the archive when we want to keep it.
     */
    private static final String EXTRACTED_ARCHIVE_FOLDER = ".extractedArchive"; //$NON-NLS-1$
    private static final @NonNull String DEFAULT_ARCHIVE_FOLDER_NAME = "extractedTrace"; //$NON-NLS-1$

    private String fTraceType;
    private IPath fDestinationContainerPath;
    private IPath fBaseSourceContainerPath;
    private boolean fImportFromArchive;
    private int fImportOptionFlags;
    private Shell fShell;
    private TmfTraceFolder fTraceFolderElement;
    private List<TraceFileSystemElement> fSelectedFileSystemElements;

    private ITmfTimestamp fStartTimeRange;
    private ITmfTimestamp fEndTimeRange;

    private @NonNull String fFolderName;
    private boolean fKeepArchive;

    private IStatus fStatus;
    private ImportConflictHandler fConflictHandler;
    private String fCurrentPath;

    private List<IResource> fImportedResources;

    /**
     * Constructs a new validate and import operation.
     *
     * @param shell
     *            the parent shell to use for possible error messages
     * @param traceFileSystemElements
     *            the trace file elements to import
     * @param traceId
     *            the trace type to import the traces as (can be set to null for
     *            automatic detection)
     * @param baseSourceContainerPath
     *            the path to the container of the source. This is used as a "base"
     *            to generate the folder structure for the "preserve folder
     *            structure" option.
     * @param destinationContainerPath
     *            the destination path of the import operation, typically a trace
     *            folder path.
     * @param importFromArchive
     *            whether or not the source is an archive
     * @param importOptionFlags
     *            bit-wise 'or' of import option flag constants (
     *            {@link ImportTraceWizardPage#OPTION_PRESERVE_FOLDER_STRUCTURE} ,
     *            {@link ImportTraceWizardPage#OPTION_CREATE_LINKS_IN_WORKSPACE} ,
     *            {@link ImportTraceWizardPage#OPTION_IMPORT_UNRECOGNIZED_TRACES} ,
     *            {@link ImportTraceWizardPage#OPTION_OVERWRITE_EXISTING_RESOURCES},
     *            and
     *            {@link ImportTraceWizardPage#OPTION_RENAME_ALL}
     *            )
     * @param traceFolderElement
     *            the destination trace folder of the import operation.
     * @param startTimeRange
     *            Timestamp representing the start time of the range
     * @param endTimeRange
     *            Timestamp representing the end time of the range
     * @param folderName
     *            Archive folder name
     * @param keepArchive
     *            whether or not the extracted archive is kept
     */
    public TraceValidateAndImportOperation(Shell shell, List<TraceFileSystemElement> traceFileSystemElements, String traceId, IPath baseSourceContainerPath, IPath destinationContainerPath, boolean importFromArchive, int importOptionFlags,
            TmfTraceFolder traceFolderElement, ITmfTimestamp startTimeRange, ITmfTimestamp endTimeRange, String folderName, boolean keepArchive) {
        fTraceType = traceId;
        fBaseSourceContainerPath = baseSourceContainerPath;
        fDestinationContainerPath = destinationContainerPath;
        fImportOptionFlags = importOptionFlags;
        fImportFromArchive = importFromArchive;
        fShell = shell;
        fTraceFolderElement = traceFolderElement;
        fStartTimeRange = startTimeRange;
        fEndTimeRange = endTimeRange;
        fFolderName = folderName != null ? folderName : DEFAULT_ARCHIVE_FOLDER_NAME;
        fKeepArchive = keepArchive;

        boolean overwriteExistingResources = (importOptionFlags & ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES) != 0;
        if (overwriteExistingResources) {
            setConflictHandler(new ImportConflictHandler(fShell, fTraceFolderElement, ImportConfirmation.OVERWRITE_ALL));
        } else {
            boolean renameAll = (importOptionFlags & ImportTraceWizardPage.OPTION_RENAME_ALL) != 0;
            if (renameAll) {
                setConflictHandler(new ImportConflictHandler(fShell, fTraceFolderElement, ImportConfirmation.RENAME_ALL));
            } else {
                setConflictHandler(new ImportConflictHandler(fShell, fTraceFolderElement, ImportConfirmation.SKIP));
            }
        }
        fImportedResources = new ArrayList<>();
        fSelectedFileSystemElements = traceFileSystemElements;
    }

    /**
     * This orders by files first then the folders. Then by lexical order.
     * This comparator handles full paths.
     * Example of ordering:
     *
     * /trace.txt
     * /folderA/trace.txt
     * /folderA/folderB/trace.txt
     * /folderZ/trace.txt
     */
    private final class FileObjectPathComparator implements Comparator<TraceFileSystemElement> {
        @Override
        public int compare(TraceFileSystemElement e1, TraceFileSystemElement e2) {
            IFileSystemObject o1 = e1.getFileSystemObject();
            IFileSystemObject o2 = e2.getFileSystemObject();
            IPath p1 = new Path(e1.getProvider().getFullPath(o1));
            IPath p2 = new Path(e2.getProvider().getFullPath(o2));
            int segmentCount1 = p1.segmentCount();
            int segmentCount2 = p2.segmentCount();

            int commonParentSegmentCount = Math.min(segmentCount1, segmentCount2) - 1;
            // Compare parents that are common (in terms of segment number).
            // If one of them is different, we do not need to worry about any
            // children, we already know in which order they are going to be.
            for (int i = 0; i < commonParentSegmentCount; i++) {
                int compare = p1.segment(i).compareToIgnoreCase(p2.segment(i));
                if (compare != 0) {
                    return compare;
                }
            }

            // At this point, we know all the common parent folders are the same.
            // Either:
            // - One of them is shorter which means it should be processed first because files are processed before sub-folders.
            // or
            // - They are the same level so only the name matters.
            if (segmentCount1 != segmentCount2) {
                return Integer.compare(segmentCount1, segmentCount2);
            }

            //
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    @Override
    protected void execute(IProgressMonitor progressMonitor) throws CoreException, InvocationTargetException, InterruptedException {
        try {
            final int ARCHIVE_OR_DIRECTORY_PROGRESS = 45;
            final int EXTRA_IMPORT_OPERATION_PROGRESS = 45;
            final int DELETE_PROGRESS = 10;
            int TOTAL_PROGRESS = ARCHIVE_OR_DIRECTORY_PROGRESS +
                    EXTRA_IMPORT_OPERATION_PROGRESS + DELETE_PROGRESS;

            final List<TraceFileSystemElement> selectedFileSystemElements = fSelectedFileSystemElements;

            // List fileSystemElements will be filled using the
            // passThroughFilter
            SubMonitor subMonitor = SubMonitor.convert(progressMonitor, TOTAL_PROGRESS);

            // Check if operation was cancelled.
            ModalContext.checkCanceled(subMonitor);

            // Temporary directory to contain any extracted files
            IFolder destTempFolder = fTraceFolderElement.getProject().getResource().getFolder(TRACE_IMPORT_TEMP_FOLDER);
            if (destTempFolder.exists()) {
                SubMonitor monitor = subMonitor.newChild(1);
                destTempFolder.delete(true, monitor);
            }
            SubMonitor monitor = subMonitor.newChild(1);
            destTempFolder.create(IResource.HIDDEN, true, monitor);

            String baseSourceLocation = null;
            if (fImportFromArchive) {
                // When importing from archive, we first extract the
                // *selected* files to a temporary folder then create new
                // TraceFileSystemElements

                SubMonitor archiveMonitor = SubMonitor.convert(subMonitor.newChild(ARCHIVE_OR_DIRECTORY_PROGRESS), 2);

                if (fKeepArchive) {
                    fImportOptionFlags |= ImportTraceWizardPage.OPTION_KEEP_EXTRACTED_ARCHIVE;
                    fImportOptionFlags |= ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
                    IFolder extractedFolder = fTraceFolderElement.getProject().getResource().getFolder(EXTRACTED_ARCHIVE_FOLDER);
                    if(!extractedFolder.exists()) {
                        monitor = archiveMonitor.newChild(1);
                        extractedFolder.create(IResource.FOLDER, true, monitor);
                    }
                    destTempFolder = extractedFolder.getFolder(fFolderName);
                    if (!destTempFolder.exists()) {
                        monitor = archiveMonitor.newChild(1);
                        destTempFolder.create(IResource.FOLDER, true, monitor);
                    }
                }

                // Extract selected files from source archive to temporary
                // folder
                extractArchiveContent(selectedFileSystemElements.iterator(), destTempFolder, archiveMonitor.newChild(1));
                archiveMonitor.subTask(""); //$NON-NLS-1$

                if (!selectedFileSystemElements.isEmpty()) {
                    // Even if the files were extracted to temporary folder, they
                    // have to look like they originate from the source archive
                    baseSourceLocation = getRootElement(selectedFileSystemElements.get(0)).getSourceLocation();
                    if ((fImportOptionFlags & ImportTraceWizardPage.OPTION_SKIP_ARCHIVE_EXTRACTION) == 0) {
                        // Extract additional archives contained in the extracted files
                        // (archives in archives)
                        List<TraceFileSystemElement> tempFolderFileSystemElements = createElementsForFolder(destTempFolder);
                        extractAllArchiveFiles(tempFolderFileSystemElements, destTempFolder, destTempFolder.getLocation(), archiveMonitor.newChild(1));
                    }
                }
            } else {
                SubMonitor directoryMonitor = SubMonitor.convert(subMonitor.newChild(ARCHIVE_OR_DIRECTORY_PROGRESS), 2);
                // Import selected files, excluding archives (done in a later step)
                importFileSystemElements(directoryMonitor.newChild(1), selectedFileSystemElements);

                if ((fImportOptionFlags & ImportTraceWizardPage.OPTION_SKIP_ARCHIVE_EXTRACTION) == 0) {
                    // Extract archives in selected files (if any) to temporary folder
                    extractAllArchiveFiles(selectedFileSystemElements, destTempFolder, fBaseSourceContainerPath, directoryMonitor.newChild(1));
                }
                // Even if the files were extracted to temporary folder, they
                // have to look like they originate from the source folder
                baseSourceLocation = URIUtil.toUnencodedString(fBaseSourceContainerPath.toFile().getCanonicalFile().toURI());
            }

            /*
             * Import extracted files that are now in the temporary folder, if any
             */

            // We need to update the source container path because the
            // "preserve folder structure" option would create the
            // wrong trace folders otherwise.
            fBaseSourceContainerPath = destTempFolder.getLocation();
            List<TraceFileSystemElement> tempFolderFileSystemElements = createElementsForFolder(destTempFolder);
            boolean keepArchive = (fImportOptionFlags & ImportTraceWizardPage.OPTION_KEEP_EXTRACTED_ARCHIVE) != 0;
            if (!tempFolderFileSystemElements.isEmpty()) {
                calculateSourceLocations(tempFolderFileSystemElements, baseSourceLocation);
                // Never import extracted files as links, they would link to the
                // temporary directory that will be deleted
                if (!keepArchive) {
                    fImportOptionFlags = fImportOptionFlags & ~ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
                }
                SubMonitor importTempMonitor = subMonitor.newChild(EXTRA_IMPORT_OPERATION_PROGRESS);
                importFileSystemElements(importTempMonitor.newChild(1), tempFolderFileSystemElements);
            }

            if (destTempFolder.exists() && !keepArchive) {
                // Do a gc if the tracker got marked during trace validation
                if (ByteBufferTracker.getAndReset()) {
                    System.gc();
                }
                destTempFolder.delete(true, subMonitor.newChild(TOTAL_PROGRESS));
            }

            setStatus(Status.OK_STATUS);
        } catch (InterruptedException e) {
            setStatus(Status.CANCEL_STATUS);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            String errorMessage = Messages.ImportTraceWizard_ImportProblem + ": " + //$NON-NLS-1$
                    (fCurrentPath != null ? fCurrentPath : ""); //$NON-NLS-1$
            Activator.getDefault().logError(errorMessage, e);
            setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, errorMessage, e));
        }
    }

    private TraceFileSystemElement filterSelectedFileSystemElement(TraceFileSystemElement selectedFileSystemElements, IProgressMonitor monitor) {
        if ((fImportOptionFlags & ImportTraceWizardPage.OPTION_FILTER_TIMERANGE) != 0 && fStartTimeRange != null && fEndTimeRange != null) {
            return filterByTimerange(selectedFileSystemElements, monitor);
        }

        return selectedFileSystemElements;
    }

    private TraceFileSystemElement filterByTimerange(TraceFileSystemElement selectedFileSystemElements, IProgressMonitor monitor) {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
        subMonitor.subTask(Messages.ImportTraceWizard_FilteringOperationTaskName);

        String tracePath = selectedFileSystemElements.getFileSystemObject().getAbsolutePath();
        ITmfTrace trace = null;
        try {
            TraceTypeHelper traceTypeHelper = TmfTraceTypeUIUtils.selectTraceType(tracePath, null, null);
            if (traceTypeHelper == null) {
                return null;
            }

            IConfigurationElement ce = TmfTraceType.getTraceAttributes(traceTypeHelper.getTraceTypeId());
            if (ce == null) {
                return null;
            }
            trace = (ITmfTrace) ce.createExecutableExtension(TmfTraceType.TRACE_TYPE_ATTR);
            ITmfEvent event = (ITmfEvent) ce.createExecutableExtension(TmfTraceType.EVENT_TYPE_ATTR);

            trace.initTrace(null, tracePath, event.getClass());

            if (isInTimerange(trace)) {
                return selectedFileSystemElements;
            }

        } catch (TmfTraceImportException | CoreException | TmfTraceException e) {
            String errorMessage = Messages.ImportTraceWizard_ImportProblem + ": " + tracePath; //$NON-NLS-1$
            Activator.getDefault().logError(errorMessage, e);
        } finally {
            if (trace != null) {
                trace.dispose();
                TmfTraceManager.deleteSupplementaryFolder(trace);
            }
            subMonitor.worked(1);
        }

        return null;
    }

    private boolean isInTimerange(ITmfTrace trace) {
        ITmfTimestamp startTimestamp = trace.readStart();
        ITmfTimestamp endTimestamp = trace.readEnd();

        return (startTimestamp != null && endTimestamp != null) && (startTimestamp.compareTo(fEndTimeRange) <= 0 && endTimestamp.compareTo(fStartTimeRange) >= 0);
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

    /**
     * Import a collection of file system elements into the workspace.
     */
    private void importFileSystemElements(IProgressMonitor monitor, List<TraceFileSystemElement> fileSystemElements)
            throws InterruptedException, TmfTraceImportException, CoreException, InvocationTargetException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, fileSystemElements.size());
        // Sort the elements in a sensible order to make it more predictable to
        // the user when there can be name clashes. Otherwise, the order can
        // seem pretty random depending on the OS/Filesystem.
        fileSystemElements.sort(new FileObjectPathComparator());

        ListIterator<TraceFileSystemElement> fileSystemElementsIter = fileSystemElements.listIterator();

        // Map to remember already imported directory traces
        final Map<String, TraceFileSystemElement> directoryTraces = new HashMap<>();
        while (fileSystemElementsIter.hasNext()) {
            ModalContext.checkCanceled(monitor);
            fCurrentPath = null;
            TraceFileSystemElement element = fileSystemElementsIter.next();
            IFileSystemObject fileSystemObject = element.getFileSystemObject();
            String resourcePath = element.getFileSystemObject().getAbsolutePath();
            element.setDestinationContainerPath(computeDestinationContainerPath(new Path(resourcePath)));

            fCurrentPath = resourcePath;
            SubMonitor sub = subMonitor.split(1, SubMonitor.SUPPRESS_BEGINTASK | SubMonitor.SUPPRESS_SUBTASK);
            if (element.isDirectory()) {
                if (!directoryTraces.containsKey(resourcePath) && isDirectoryTrace(element)) {
                    directoryTraces.put(resourcePath, element);
                    validateAndImportTrace(element, sub);
                }
            } else {
                TraceFileSystemElement parentElement = (TraceFileSystemElement) element.getParent();
                String parentPath = parentElement.getFileSystemObject().getAbsolutePath();
                parentElement.setDestinationContainerPath(computeDestinationContainerPath(new Path(parentPath)));
                fCurrentPath = parentPath;
                if (!directoryTraces.containsKey(parentPath)) {
                    if (isDirectoryTrace(parentElement)) {
                        directoryTraces.put(parentPath, parentElement);
                        validateAndImportTrace(parentElement, sub);
                    } else {
                        boolean validateFile = true;
                        TraceFileSystemElement grandParentElement = (TraceFileSystemElement) parentElement.getParent();
                        // Special case for LTTng trace that may contain index
                        // directory and files
                        if (grandParentElement != null) {
                            String grandParentPath = grandParentElement.getFileSystemObject().getAbsolutePath();
                            grandParentElement.setDestinationContainerPath(computeDestinationContainerPath(new Path(parentPath)));
                            fCurrentPath = grandParentPath;
                            if (directoryTraces.containsKey(grandParentPath)) {
                                validateFile = false;
                            } else if (isDirectoryTrace(grandParentElement)) {
                                directoryTraces.put(grandParentPath, grandParentElement);
                                validateAndImportTrace(grandParentElement, sub);
                                validateFile = false;
                            }
                        }
                        if (validateFile && (fileSystemObject.exists())) {
                            validateAndImportTrace(element, sub);
                        }
                    }
                }
            }
        }
    }

    /**
     * Generate a new list of file system elements for the specified folder.
     */
    private static List<TraceFileSystemElement> createElementsForFolder(IFolder folder) {
        // Create the new import provider and root element based on the
        // specified folder
        FileSystemObjectImportStructureProvider importStructureProvider = new FileSystemObjectImportStructureProvider(FileSystemStructureProvider.INSTANCE, null);
        IFileSystemObject rootElement = importStructureProvider.getIFileSystemObject(new File(folder.getLocation().toOSString()));
        TraceFileSystemElement createRootElement = TraceFileSystemElement.createRootTraceFileElement(rootElement, importStructureProvider);
        List<TraceFileSystemElement> list = new LinkedList<>();
        createRootElement.getAllChildren(list);
        return list;
    }

    /**
     * Extract all file system elements (File) to destination folder (typically
     * workspace/TraceProject/.traceImport)
     */
    private void extractAllArchiveFiles(List<TraceFileSystemElement> fileSystemElements, IFolder destFolder, IPath baseSourceContainerPath, IProgressMonitor progressMonitor) throws InterruptedException, CoreException, InvocationTargetException {
        SubMonitor subMonitor = SubMonitor.convert(progressMonitor, fileSystemElements.size());
        ListIterator<TraceFileSystemElement> fileSystemElementsIter = fileSystemElements.listIterator();
        while (fileSystemElementsIter.hasNext()) {
            ModalContext.checkCanceled(subMonitor);

            SubMonitor elementProgress = subMonitor.newChild(1);
            TraceFileSystemElement element = fileSystemElementsIter.next();
            elementProgress.setTaskName(Messages.ImportTraceWizard_ExamineOperationTaskName + " " + element.getFileSystemObject().getAbsolutePath()); //$NON-NLS-1$
            File archiveFile = (File) element.getFileSystemObject().getRawFileSystemObject();
            boolean isArchiveFileElement = element.getFileSystemObject() instanceof FileFileSystemObject && ArchiveUtil.isArchiveFile(archiveFile);
            if (isArchiveFileElement) {
                elementProgress = SubMonitor.convert(elementProgress, 4);
                IPath makeAbsolute = baseSourceContainerPath.makeAbsolute();
                IPath relativeToSourceContainer = new Path(element.getFileSystemObject().getAbsolutePath()).makeRelativeTo(makeAbsolute);
                IFolder folder = safeCreateExtractedFolder(destFolder, relativeToSourceContainer, elementProgress.newChild(1));
                extractArchiveToFolder(archiveFile, folder, elementProgress.newChild(1));

                // Delete original archive, we don't want to import this, just
                // the extracted content
                IFile fileRes = destFolder.getFile(relativeToSourceContainer);
                fileRes.delete(true, elementProgress.newChild(1));
                IPath newPath = destFolder.getFullPath().append(relativeToSourceContainer);
                // Rename extracted folder (.extract) to original archive name
                folder.move(newPath, true, elementProgress.newChild(1));
                folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(newPath);
                elementProgress.subTask(""); //$NON-NLS-1$

                // Create the new import provider and root element based on
                // the newly extracted temporary folder
                FileSystemObjectImportStructureProvider importStructureProvider = new FileSystemObjectImportStructureProvider(FileSystemStructureProvider.INSTANCE, null);
                IFileSystemObject rootElement = importStructureProvider.getIFileSystemObject(new File(folder.getLocation().toOSString()));
                TraceFileSystemElement newElement = TraceFileSystemElement.createRootTraceFileElement(rootElement, importStructureProvider);
                List<TraceFileSystemElement> extractedChildren = new ArrayList<>();
                newElement.getAllChildren(extractedChildren);
                extractAllArchiveFiles(extractedChildren, folder, folder.getLocation(), progressMonitor);
            }
        }
    }

    /**
     * Extract a file (File) to a destination folder
     */
    private void extractArchiveToFolder(File sourceFile, IFolder destinationFolder, IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
        Pair<IFileSystemObject, FileSystemObjectImportStructureProvider> rootObjectAndProvider = ArchiveUtil.getRootObjectAndProvider(sourceFile, fShell);
        TraceFileSystemElement rootElement = TraceFileSystemElement.createRootTraceFileElement(rootObjectAndProvider.getFirst(), rootObjectAndProvider.getSecond());
        List<TraceFileSystemElement> fileSystemElements = new ArrayList<>();
        rootElement.getAllChildren(fileSystemElements);
        extractArchiveContent(fileSystemElements.listIterator(), destinationFolder, progressMonitor);
        rootObjectAndProvider.getSecond().dispose();
    }

    /**
     * Safely create a folder meant to receive extracted content by making sure
     * there is no name clash.
     */
    private static IFolder safeCreateExtractedFolder(IFolder destinationFolder, IPath relativeContainerRelativePath, IProgressMonitor monitor) throws CoreException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
        IFolder extractedFolder;
        String suffix = ""; //$NON-NLS-1$
        int i = 2;
        while (true) {
            IPath fullPath = destinationFolder.getFullPath().append(relativeContainerRelativePath + ".extract" + suffix); //$NON-NLS-1$
            IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(fullPath);
            if (!folder.exists()) {
                extractedFolder = folder;
                break;
            }
            suffix = "(" + i + ")"; //$NON-NLS-1$//$NON-NLS-2$
            i++;
        }
        subMonitor.worked(1);

        TraceUtils.createFolder(extractedFolder, subMonitor.newChild(1));
        return extractedFolder;
    }

    private void calculateSourceLocations(List<TraceFileSystemElement> fileSystemElements, String baseSourceLocation) {
        for (TraceFileSystemElement element : fileSystemElements) {
            IPath tempRelative = new Path(element.getFileSystemObject().getAbsolutePath()).makeRelativeTo(fBaseSourceContainerPath);
            String sourceLocation = baseSourceLocation + tempRelative;
            element.setSourceLocation(sourceLocation);

            TraceFileSystemElement parentElement = (TraceFileSystemElement) element.getParent();
            tempRelative = new Path(parentElement.getFileSystemObject().getAbsolutePath()).makeRelativeTo(fBaseSourceContainerPath);
            sourceLocation = baseSourceLocation + tempRelative + '/';
            parentElement.setSourceLocation(sourceLocation);
        }
    }

    /**
     * Extract all file system elements (Tar, Zip elements) to destination
     * folder (typically workspace/TraceProject/.traceImport or a subfolder of
     * it)
     */
    private void extractArchiveContent(Iterator<TraceFileSystemElement> fileSystemElementsIter, IFolder tempFolder, IProgressMonitor progressMonitor) throws InterruptedException,
            InvocationTargetException {
        List<TraceFileSystemElement> subList = new ArrayList<>();
        // Collect all the elements
        while (fileSystemElementsIter.hasNext()) {
            ModalContext.checkCanceled(progressMonitor);
            TraceFileSystemElement element = fileSystemElementsIter.next();
            if (element.isDirectory()) {
                Object[] array = element.getFiles().getChildren();
                for (int i = 0; i < array.length; i++) {
                    subList.add((TraceFileSystemElement) array[i]);
                }
            }
            subList.add(element);
        }

        if (subList.isEmpty()) {
            return;
        }

        TraceFileSystemElement root = getRootElement(subList.get(0));

        ImportProvider fileSystemStructureProvider = new ImportProvider();

        IOverwriteQuery myQueryImpl = file -> IOverwriteQuery.NO_ALL;

        progressMonitor.setTaskName(Messages.ImportTraceWizard_ExtractImportOperationTaskName);
        IPath containerPath = tempFolder.getFullPath();
        ImportOperation operation = new ImportOperation(containerPath, root, fileSystemStructureProvider, myQueryImpl, subList);
        operation.setContext(fShell);

        operation.setCreateContainerStructure(true);
        operation.setOverwriteResources(false);
        operation.setVirtualFolders(false);

        operation.run(SubMonitor.convert(progressMonitor).newChild(subList.size()));
    }

    private static TraceFileSystemElement getRootElement(TraceFileSystemElement element) {
        TraceFileSystemElement root = element;
        while (root.getParent() != null) {
            root = (TraceFileSystemElement) root.getParent();
        }
        return root;
    }

    private IPath computeDestinationContainerPath(Path resourcePath) {
        IPath destinationContainerPath = fDestinationContainerPath;

        // We need to figure out the new destination path relative to the
        // selected "base" source directory.
        // Here for example, the selected source directory is /home/user
        if ((fImportOptionFlags & ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE) != 0) {
            // /home/user/bar/foo/trace -> /home/user/bar/foo
            IPath sourceContainerPath = resourcePath.removeLastSegments(1);
            if (fBaseSourceContainerPath.equals(resourcePath)) {
                // Use resourcePath directory if fBaseSourceContainerPath
                // points to a directory trace
                sourceContainerPath = resourcePath;
            }
            // /home/user/bar/foo, /home/user -> bar/foo
            IPath relativeContainerPath = sourceContainerPath.makeRelativeTo(fBaseSourceContainerPath);
            // project/Traces + bar/foo -> project/Traces/bar/foo
            destinationContainerPath = fDestinationContainerPath.append(relativeContainerPath);
        }
        return destinationContainerPath;
    }

    /**
     * Import a single file system element into the workspace.
     */
    private void validateAndImportTrace(TraceFileSystemElement fileSystemElement, IProgressMonitor monitor)
            throws TmfTraceImportException, CoreException, InvocationTargetException, InterruptedException {
        String path = fileSystemElement.getFileSystemObject().getAbsolutePath();
        TraceTypeHelper traceTypeHelper = null;
        monitor.setTaskName(Messages.ImportTraceWizard_ExamineOperationTaskName + " " + fileSystemElement.getFileSystemObject().getAbsolutePath()); //$NON-NLS-1$

        if ((fImportOptionFlags & ImportTraceWizardPage.OPTION_SKIP_ARCHIVE_EXTRACTION) == 0) {
            File file = (File) fileSystemElement.getFileSystemObject().getRawFileSystemObject();
            boolean isArchiveFileElement = fileSystemElement.getFileSystemObject() instanceof FileFileSystemObject && ArchiveUtil.isArchiveFile(file);
            if (isArchiveFileElement) {
                // We'll be extracting this later, do not import as a trace
                return;
            }
        }

        if (fTraceType == null) {
            // Auto Detection
            try {
                traceTypeHelper = TmfTraceTypeUIUtils.selectTraceType(path, null, null);
            } catch (TmfTraceImportException e) {
                // the trace did not match any trace type
            }
            if (traceTypeHelper == null) {
                if ((fImportOptionFlags & ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES) != 0) {
                    importResource(fileSystemElement, monitor);
                }
                return;
            }
        } else {
            boolean isDirectoryTraceType = TmfTraceType.isDirectoryTraceType(fTraceType);
            if (fileSystemElement.isDirectory() != isDirectoryTraceType) {
                return;
            }
            traceTypeHelper = TmfTraceType.getTraceType(fTraceType);

            if (traceTypeHelper == null) {
                // Trace type not found
                throw new TmfTraceImportException(Messages.ImportTraceWizard_TraceTypeNotFound);
            }

            if (!traceTypeHelper.validate(path).isOK()) {
                // Trace type exist but doesn't validate for given trace.
                return;
            }
        }

        // Finally import trace
        TraceFileSystemElement resourceToImport = filterSelectedFileSystemElement(fileSystemElement, monitor);
        if(resourceToImport == null) {
            return;
        }
        IResource importedResource = importResource(resourceToImport, monitor);
        if (importedResource != null) {
            TmfTraceTypeUIUtils.setTraceType(importedResource, traceTypeHelper, false);
            fImportedResources.add(importedResource);
        }

    }

    /**
     * Imports a trace resource to project. In case of name collision the user
     * will be asked to confirm overwriting the existing trace, overwriting or
     * skipping the trace to be imported.
     *
     * @param fileSystemElement
     *            trace file system object to import
     * @param monitor
     *            a progress monitor
     * @return the imported resource or null if no resource was imported
     *
     * @throws InvocationTargetException
     *             if problems during import operation
     * @throws InterruptedException
     *             if cancelled
     * @throws CoreException
     *             if problems with workspace
     */
    private IResource importResource(TraceFileSystemElement fileSystemElement, IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException, CoreException {

        IPath tracePath = getInitialDestinationPath(fileSystemElement);
        String newName = fConflictHandler.checkAndHandleNameClash(tracePath, monitor);

        if (newName == null) {
            return null;
        }
        fileSystemElement.setLabel(newName);

        List<TraceFileSystemElement> subList = new ArrayList<>();

        FileSystemElement parentFolder = fileSystemElement.getParent();

        IPath containerPath = fileSystemElement.getDestinationContainerPath();
        tracePath = containerPath.addTrailingSeparator().append(fileSystemElement.getLabel());
        boolean createLinksInWorkspace = (fImportOptionFlags & ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE) != 0;
        if (fileSystemElement.isDirectory() && !createLinksInWorkspace) {
            containerPath = tracePath;

            Object[] array = fileSystemElement.getFiles().getChildren();
            for (int i = 0; i < array.length; i++) {
                subList.add((TraceFileSystemElement) array[i]);
            }
            parentFolder = fileSystemElement;

        } else {
            if (!fileSystemElement.isDirectory()) {
                // File traces
                IFileInfo info = EFS.getStore(new File(fileSystemElement.getFileSystemObject().getAbsolutePath()).toURI()).fetchInfo();
                if (info.getLength() == 0) {
                    // Don't import empty traces
                    return null;
                }
            }
            subList.add(fileSystemElement);
        }

        ImportProvider fileSystemStructureProvider = new ImportProvider();

        IOverwriteQuery myQueryImpl = file -> IOverwriteQuery.NO_ALL;

        monitor.setTaskName(Messages.ImportTraceWizard_ImportOperationTaskName + " " + fileSystemElement.getFileSystemObject().getAbsolutePath()); //$NON-NLS-1$
        ImportOperation operation = new ImportOperation(containerPath, parentFolder, fileSystemStructureProvider, myQueryImpl, subList);
        operation.setContext(fShell);

        operation.setCreateContainerStructure(false);
        operation.setOverwriteResources(false);
        operation.setCreateLinks(createLinksInWorkspace);
        operation.setVirtualFolders(false);

        operation.run(SubMonitor.convert(monitor).newChild(1));
        String sourceLocation = fileSystemElement.getSourceLocation();
        IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(tracePath);
        if ((sourceLocation != null) && (resource != null)) {
            resource.setPersistentProperty(TmfCommonConstants.SOURCE_LOCATION, sourceLocation);
        }

        return resource;
    }

    private static boolean isDirectoryTrace(TraceFileSystemElement fileSystemElement) {
        String path = fileSystemElement.getFileSystemObject().getAbsolutePath();
        return (TmfTraceType.isDirectoryTrace(path));
    }

    /**
     * @return the initial destination path, before rename, if any
     */
    private static IPath getInitialDestinationPath(TraceFileSystemElement fileSystemElement) {
        IPath traceFolderPath = fileSystemElement.getDestinationContainerPath();
        return traceFolderPath.append(fileSystemElement.getFileSystemObject().getName());
    }

    /**
     * Set the status for this operation
     *
     * @param status
     *            the status
     */
    private void setStatus(IStatus status) {
        fStatus = status;
    }

    /**
     * Get the resulting status of this operation. Clients can use this for
     * error reporting, etc.
     *
     * @return the resulting status
     */
    public IStatus getStatus() {
        return fStatus;
    }

    private class ImportProvider implements IImportStructureProvider {

        ImportProvider() {
        }

        @Override
        public String getLabel(Object element) {
            TraceFileSystemElement resource = (TraceFileSystemElement) element;
            return resource.getLabel();
        }

        @Override
        public List getChildren(Object element) {
            TraceFileSystemElement resource = (TraceFileSystemElement) element;
            return Arrays.asList(resource.getFiles().getChildren());
        }

        @Override
        public InputStream getContents(Object element) {
            TraceFileSystemElement resource = (TraceFileSystemElement) element;
            return resource.getProvider().getContents(resource.getFileSystemObject());
        }

        @Override
        public String getFullPath(Object element) {
            TraceFileSystemElement resource = (TraceFileSystemElement) element;
            return resource.getProvider().getFullPath(resource.getFileSystemObject());
        }

        @Override
        public boolean isFolder(Object element) {
            TraceFileSystemElement resource = (TraceFileSystemElement) element;
            return resource.isDirectory();
        }
    }

    /**
     * Sets the conflict handler
     *
     * @param conflictHandler
     *            the conflict handler
     */
    public void setConflictHandler(ImportConflictHandler conflictHandler) {
        fConflictHandler = conflictHandler;
    }
}