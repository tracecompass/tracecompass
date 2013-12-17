/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Marc-Andre Laperle - Log some exceptions
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.project.model.TmfImportHelper;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceValidationHelper;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * Batch Import trace wizard.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class BatchImportTraceWizard extends Wizard implements IImportWizard {

    private static final int WIN_HEIGHT = 400;
    private static final int WIN_WIDTH = 800;
    private static final Status CANCEL_STATUS = new Status(IStatus.CANCEL, Activator.PLUGIN_ID, ""); //$NON-NLS-1$
    private static final int TOTALWORK = 65536;
    // -----------------
    // Constants
    // -----------------

    private static final int MAX_FILES = TOTALWORK - 1;
    private static final String BATCH_IMPORT_WIZARD = "BatchImportTraceWizard"; //$NON-NLS-1$

    // ------------------
    // Fields
    // ------------------

    private IWizardPage fSelectDirectoriesPage;
    private ImportTraceWizardScanPage fScanPage;
    private IWizardPage fSelectTypePage;
    private IWizardPage fOptions;

    private final List<String> fTraceTypesToScan = new ArrayList<>();
    private final Set<String> fParentFilesToScan = new HashSet<>();

    private ImportTraceContentProvider fScannedTraces = new ImportTraceContentProvider(fTraceTypesToScan, fParentFilesToScan);

    private final Map<TraceValidationHelper, Boolean> fResults = new HashMap<>();
    private boolean fOverwrite = true;
    private boolean fLinked = true;

    private BlockingQueue<TraceValidationHelper> fTracesToScan;
    private final Set<FileAndName> fTraces = new TreeSet<>();

    private Map<String, Set<String>> fParentFiles = new HashMap<>();

    // Target import directory ('Traces' folder)
    private IFolder fTargetFolder;

    /**
     * Returns the ScannedTraces model
     *
     * @return the ScannedTraces model
     */
    public ImportTraceContentProvider getScannedTraces() {
        return fScannedTraces;
    }

    /**
     * Constructor
     */
    public BatchImportTraceWizard() {
        IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection(BATCH_IMPORT_WIZARD);
        if (section == null) {
            section = workbenchSettings.addNewSection(BATCH_IMPORT_WIZARD);
        }
        setDialogSettings(section);
        setNeedsProgressMonitor(true);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {

        fSelectDirectoriesPage = new ImportTraceWizardSelectDirectoriesPage(workbench, selection);
        fScanPage = new ImportTraceWizardScanPage(workbench, selection);
        fSelectTypePage = new ImportTraceWizardSelectTraceTypePage(workbench, selection);
        fOptions = new ImportTraceWizardPageOptions(workbench, selection);
        // keep in case it's called later
        Iterator<?> iter = selection.iterator();
        while (iter.hasNext()) {
            Object selected = iter.next();
            if (selected instanceof TmfTraceFolder) {
                fTargetFolder = ((TmfTraceFolder) selected).getResource();
                break;
            }
        }
        fResults.clear();
    }

    @Override
    public void addPages() {
        addPage(fSelectTypePage);
        addPage(fSelectDirectoriesPage);
        addPage(fScanPage);
        addPage(fOptions);
        final WizardDialog container = (WizardDialog) getContainer();
        if (container != null) {
            container.setPageSize(WIN_WIDTH, WIN_HEIGHT);
        }
    }

    /**
     * Add a file to scan
     *
     * @param fileName
     *            the file to scan
     */
    public void addFileToScan(final String fileName) {
        String absolutePath = new File(fileName).getAbsolutePath();
        if (!fParentFiles.containsKey(absolutePath)) {
            fParentFiles.put(absolutePath, new HashSet<String>());
            startUpdateTask(Messages.BatchImportTraceWizardAdd + ' ' + absolutePath, absolutePath);

        }

    }

    /**
     * Remove files from selection
     *
     * @param fileName
     *            the name of the file to remove
     */
    public void removeFile(final String fileName) {
        fParentFiles.remove(fileName);
        fParentFilesToScan.remove(fileName);
        startUpdateTask(Messages.BatchImportTraceWizardRemove + ' ' + fileName, null);
    }

    private void startUpdateTask(final String taskName, final String fileAbsolutePath) {
        try {
            this.getContainer().run(true, true, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    synchronized (BatchImportTraceWizard.this) { // this should
                                                                 // only run one
                                                                 // at a time
                        SubMonitor sm;
                        sm = SubMonitor.convert(monitor);
                        sm.setTaskName(taskName);
                        sm.setWorkRemaining(TOTALWORK);
                        updateFiles(sm, fileAbsolutePath);
                        sm.done();
                    }
                }
            });
        } catch (InvocationTargetException e) {
            Activator.getDefault().logError(Messages.ImportTraceWizardImportProblem, e);
        } catch (InterruptedException e) {
        }
    }

    /**
     * The set of names of the selected files
     *
     * @return the set of names of the selected files
     */
    public Set<String> getFileNames() {
        return fParentFilesToScan;
    }

    /**
     * Reset the trace list to import
     */
    public void clearTraces() {
        fTraces.clear();
    }

    @Override
    public boolean performFinish() {
        if (fTraces.isEmpty()) {
            return false;
        }
        // if this turns out to be too slow, put in a progress monitor. Does not
        // appear to be slow for the moment.
        boolean success = importTraces();
        return success;
    }

    private boolean importTraces() {
        boolean success = false;
        IOverwriteQuery overwriteQuery = new IOverwriteQuery() {
            @Override
            public String queryOverwrite(String file) {
                return fOverwrite ? IOverwriteQuery.ALL : IOverwriteQuery.NO_ALL;
            }
        };
        FileSystemStructureProvider fileSystemStructureProvider = FileSystemStructureProvider.INSTANCE;

        for (FileAndName traceToImport : fTraces) {
            try {
                if (fLinked) {
                    if (TmfImportHelper.createLink(fTargetFolder, Path.fromOSString(traceToImport.getFile().getAbsolutePath()), traceToImport.getName()) == null) {
                        success = false;
                    }
                    else {
                        success = setTraceType(traceToImport).isOK();
                    }
                }
                else {
                    List<File> subList = new ArrayList<>();
                    IPath path = fTargetFolder.getFullPath();
                    File parentFile = traceToImport.getFile();
                    final boolean isFile = parentFile.isFile();
                    if (isFile) {
                        IFile resource = ResourcesPlugin.getWorkspace().getRoot().getFile(path.append(traceToImport.getName()));
                        if (fOverwrite || !resource.exists()) {
                            subList.add(parentFile);
                            parentFile = parentFile.getParentFile();
                            try (final FileInputStream source = new FileInputStream(traceToImport.getFile());) {
                                if (resource.exists()) {
                                    resource.delete(IResource.FORCE, new NullProgressMonitor());
                                }
                                resource.create(source, true, new NullProgressMonitor());
                            }
                            setTraceType(traceToImport);
                            success = true;
                        }
                    } else {
                        // Add trace directory
                        subList.add(traceToImport.getFile());
                        // Add all files in trace directory
                        File[] fileList = traceToImport.getFile().listFiles();
                        for (File child : fileList) {
                            subList.add(child);
                        }

                        Collections.sort(subList, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
                            }
                        });
                        ImportOperation operation = new ImportOperation(
                                path,
                                parentFile.getParentFile(),
                                fileSystemStructureProvider,
                                overwriteQuery,
                                subList);
                        operation.setContext(getShell());
                        operation.setCreateContainerStructure(false);
                        if (executeImportOperation(operation)) {
                            setTraceType(traceToImport);
                            success = true;
                        }
                    }

                }
            } catch (Exception e) {
            }
        }
        return success;
    }

    private IStatus setTraceType(FileAndName traceToImport) {
        IStatus validate = Status.OK_STATUS;
        IPath path = fTargetFolder.getFullPath().append(traceToImport.getName());
        IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        if (resource != null) {
            try {
                // Set the trace type for this resource
                String traceTypeId = traceToImport.getTraceTypeId();
                TraceTypeHelper traceType = TmfTraceType.getInstance().getTraceType(traceTypeId);
                if (traceType != null) {
                    TmfTraceType.setTraceType(path, traceType);
                }

                TmfProjectElement tmfProject =
                        TmfProjectRegistry.getProject(resource.getProject());
                if (tmfProject != null) {
                    final TmfTraceFolder tracesFolder = tmfProject.getTracesFolder();
                    tracesFolder.refresh();

                    List<TmfTraceElement> traces = tracesFolder.getTraces();
                    boolean found = false;
                    for (TmfTraceElement traceElement : traces) {
                        if (traceElement.getName().equals(resource.getName())) {
                            traceElement.refreshTraceType();
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        TmfTraceElement te = new TmfTraceElement(traceToImport.getName(), resource, tracesFolder);
                        te.refreshTraceType();
                        traces = tracesFolder.getTraces();
                        for (TmfTraceElement traceElement : traces) {
                            if (traceElement.getName().equals(resource.getName())) {
                                traceElement.refreshTraceType();
                                ITmfTrace tmfTrace = null;
                                try {
                                    tmfTrace = traceElement.instantiateTrace();
                                    if (tmfTrace != null) {
                                        validate = tmfTrace.validate(tmfProject.getResource(), traceElement.getLocation().getPath());
                                    } else {
                                        return new Status(IStatus.ERROR, traceElement.getName(), "File does not exist : " + traceElement.getLocation().getPath()); //$NON-NLS-1$
                                    }
                                } finally {
                                    if (tmfTrace != null) {
                                        tmfTrace.dispose();
                                    }
                                }
                                break;
                            }
                        }

                    }

                }
            } catch (CoreException e) {
                Activator.getDefault().logError(Messages.BatchImportTraceWizardErrorImportingTraceResource
                        + ' ' + resource.getName(), e);
            }
        }
        return validate;
    }

    @Override
    public boolean canFinish() {
        return super.canFinish() && hasTracesToImport() && !hasConflicts() && (fTargetFolder != null);
    }

    /**
     * Returns if a trace to import is selected
     *
     * @return if there are traces to import
     */
    public boolean hasTracesToImport() {
        return fTraces.size() > 0;
    }

    /**
     * Reset the files to scan
     */
    public void clearFilesToScan() {
        fTracesToScan.clear();
    }

    /**
     * Set the trace types to scan
     *
     * @param tracesToScan
     *            a list of trace types to scan for
     */
    public void setTraceTypesToScan(List<String> tracesToScan) {
        // intersection to know if there's a diff.
        // if there's a diff, we need to re-enque everything
        List<String> added = new ArrayList<>();
        for (String traceLoc : tracesToScan) {
            if (!fTraceTypesToScan.contains(traceLoc)) {
                added.add(traceLoc);
            }
        }
        fTraceTypesToScan.clear();
        fTraceTypesToScan.addAll(tracesToScan);
        updateTracesToScan(added);
    }

    /**
     * Get the trace types to scan
     *
     * @return a list of traces to Scan for
     */
    public List<String> getTraceTypesToScan() {
        return fTraceTypesToScan;
    }

    /**
     * Add files to Import
     *
     * @param element
     *            add the file and tracetype to import
     */
    public void addFileToImport(FileAndName element) {
        fTraces.add(element);
        updateConflicts();
    }

    /**
     * Remove the file to scan
     *
     * @param element
     *            the element to remove
     */
    public void removeFileToImport(FileAndName element) {
        fTraces.remove(element);
        element.setConflictingName(false);
        updateConflicts();
    }

    /**
     * Updates the trace to see if there are conflicts.
     */
    public void updateConflicts() {
        final FileAndName[] fChildren = fTraces.toArray(new FileAndName[0]);
        for (int i = 0; i < fChildren.length; i++) {
            fChildren[i].setConflictingName(false);
        }
        for (int i = 1; i < fChildren.length; i++) {
            for (int j = 0; j < i; j++) {
                if (fChildren[i].getName().equals(fChildren[j].getName())) {
                    fChildren[i].setConflictingName(true);
                    fChildren[j].setConflictingName(true);
                }
            }
        }
        getContainer().updateButtons();
    }

    /**
     * Is there a name conflict
     */
    boolean hasConflicts() {
        boolean conflict = false;
        for (FileAndName child : fTraces) {
            conflict |= child.isConflictingName();
        }
        return conflict;
    }

    private boolean executeImportOperation(ImportOperation op) {
        initializeOperation(op);

        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Activator.getDefault().logError(Messages.ImportTraceWizardImportProblem, e);
            return false;
        }

        IStatus status = op.getStatus();
        if (!status.isOK()) {
            ErrorDialog.openError(getContainer().getShell(), Messages.ImportTraceWizardImportProblem, null, status);
            return false;
        }

        return true;
    }

    private static void initializeOperation(ImportOperation op) {
        op.setCreateContainerStructure(false);
        op.setOverwriteResources(false);
        op.setVirtualFolders(false);
    }

    /**
     * Override existing resources
     *
     * @param selection
     *            true or false
     */
    public void setOverwrite(boolean selection) {
        fOverwrite = selection;
    }

    /**
     * Is the trace linked?
     *
     * @param isLink
     *            true or false
     */
    public void setLinked(boolean isLink) {
        fLinked = isLink;
    }

    /**
     * @param tracesToScan
     *            sets the common traces to scan
     */
    public void setTracesToScan(BlockingQueue<TraceValidationHelper> tracesToScan) {
        fTracesToScan = tracesToScan;
    }

    /**
     * @param traceToScan
     *            The trace to scan
     * @return if the trace has been scanned yet or not
     */
    public boolean hasScanned(TraceValidationHelper traceToScan) {
        return fResults.containsKey(traceToScan);
    }

    /**
     * Add a result to a cache
     *
     * @param traceToScan
     *            The trace that has been scanned
     * @param validate
     *            if the trace is valid
     */
    public void addResult(TraceValidationHelper traceToScan, boolean validate) {
        fResults.put(traceToScan, validate);
    }

    /**
     * Gets if the trace has been scanned or not
     *
     * @param traceToScan
     *            the scanned trace
     * @return whether it passes or not
     */
    public Boolean getResult(TraceValidationHelper traceToScan) {
        return fResults.get(traceToScan);
    }

    /**
     * Returns the amount of files scanned
     *
     * @return the amount of files scanned
     */
    public int getNumberOfResults() {
        return fResults.size();
    }

    private void updateTracesToScan(final List<String> added) {
        // Treeset is used instead of a hashset since the traces should be read
        // in the order they were added.
        final Set<String> filesToScan = new TreeSet<>();
        for (String name : fParentFiles.keySet()) {
            filesToScan.addAll(fParentFiles.get(name));
        }
        IProgressMonitor pm = new NullProgressMonitor();
        try {
            updateScanQueue(pm, filesToScan, added);
        } catch (InterruptedException e) {
        }

    }

    /*
     * I am a job. Make me work
     */
    private synchronized IStatus updateFiles(IProgressMonitor monitor, String traceToScanAbsPath) {
        final Set<String> filesToScan = new TreeSet<>();

        int workToDo = 1;
        for (String name : fParentFiles.keySet()) {

            final File file = new File(name);
            final File[] listFiles = file.listFiles();
            if (listFiles != null) {
                workToDo += listFiles.length;
            }
        }
        int step = TOTALWORK / workToDo;
        try {
            for (String name : fParentFiles.keySet()) {
                final File fileToAdd = new File(name);
                final Set<String> parentFilesToScan = fParentFiles.get(fileToAdd.getAbsolutePath());
                recurse(parentFilesToScan, fileToAdd, monitor, step);
                if (monitor.isCanceled()) {
                    fParentFilesToScan.remove(traceToScanAbsPath);
                    fParentFiles.remove(traceToScanAbsPath);
                    return CANCEL_STATUS;
                }
            }
            filesToScan.clear();
            for (String name : fParentFiles.keySet()) {
                filesToScan.addAll(fParentFiles.get(name));
                fParentFilesToScan.add(name);
            }
            IStatus cancelled = updateScanQueue(monitor, filesToScan, fTraceTypesToScan);
            if (cancelled.matches(IStatus.CANCEL)) {
                fParentFilesToScan.remove(traceToScanAbsPath);
                fParentFiles.remove(traceToScanAbsPath);
            }
        } catch (InterruptedException e) {
            monitor.done();
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
        }

        monitor.done();
        return Status.OK_STATUS;
    }

    private IStatus updateScanQueue(IProgressMonitor monitor, final Set<String> filesToScan, final List<String> traceTypes) throws InterruptedException {
        for (String fileToScan : filesToScan) {
            for (String traceCat : traceTypes) {
                TraceValidationHelper tv = new TraceValidationHelper(fileToScan, traceCat);
                // for thread safety, keep checks in this order.
                if (!fResults.containsKey(tv)) {
                    if (!fTracesToScan.contains(tv)) {
                        fTracesToScan.put(tv);
                        monitor.subTask(tv.getTraceToScan());
                        if (monitor.isCanceled()) {
                            fScanPage.refresh();
                            return CANCEL_STATUS;
                        }
                    }
                }
            }
        }
        fScanPage.refresh();
        return Status.OK_STATUS;
    }

    private IStatus recurse(Set<String> filesToScan, File fileToAdd, IProgressMonitor monitor, int step) {
        final String absolutePath = fileToAdd.getAbsolutePath();
        if (!filesToScan.contains(absolutePath) && (filesToScan.size() < MAX_FILES)) {
            filesToScan.add(absolutePath);
            final File[] listFiles = fileToAdd.listFiles();
            if (null != listFiles) {
                for (File child : listFiles) {
                    monitor.subTask(child.getName());
                    if (monitor.isCanceled()) {
                        return CANCEL_STATUS;
                    }
                    IStatus retVal = recurse(filesToScan, child, monitor);
                    if (retVal.matches(IStatus.CANCEL)) {
                        return retVal;
                    }
                    monitor.worked(step);
                }
            }
        }
        return Status.OK_STATUS;
    }

    private IStatus recurse(Set<String> filesToScan, File fileToAdd, IProgressMonitor monitor) {
        final String absolutePath = fileToAdd.getAbsolutePath();
        if (!filesToScan.contains(absolutePath) && (filesToScan.size() < MAX_FILES)) {
            filesToScan.add(absolutePath);
            final File[] listFiles = fileToAdd.listFiles();
            if (null != listFiles) {
                for (File child : listFiles) {
                    if (monitor.isCanceled()) {
                        return CANCEL_STATUS;
                    }
                    IStatus retVal = recurse(filesToScan, child, monitor);
                    if (retVal.matches(IStatus.CANCEL)) {
                        return retVal;
                    }
                }
            }
        }
        return Status.OK_STATUS;
    }

    /**
     * Gets the folder in the resource (project)
     *
     * @param targetFolder
     *            the folder to import to
     */
    public void setTraceFolder(IFolder targetFolder) {
        fTargetFolder = targetFolder;
        if (this.getContainer() != null && this.getContainer().getCurrentPage() != null) {
            this.getContainer().updateButtons();
        }
    }

    /**
     * Gets the target folder
     *
     * @return the target folder
     */
    public IFolder getTargetFolder() {
        return fTargetFolder;
    }

}
