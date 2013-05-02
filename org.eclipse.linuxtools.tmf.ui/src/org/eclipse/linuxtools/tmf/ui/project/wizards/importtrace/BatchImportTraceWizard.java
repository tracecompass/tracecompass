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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceValidationHelper;
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
public class BatchImportTraceWizard extends ImportTraceWizard {

    private static final int TOTALWORK = 65536;
    // -----------------
    // Constants
    // -----------------

    private static final int MAX_FILES = TOTALWORK - 1;
    private static final String DEFAULT_TRACE_ICON_PATH = "icons/elcl16/trace.gif"; //$NON-NLS-1$
    private static final String BATCH_IMPORT_WIZARD = "BatchImportTraceWizard"; //$NON-NLS-1$

    // ------------------
    // Fields
    // ------------------

    private IWizardPage fSelectDirectoriesPage;
    private IWizardPage fScanPage;
    private IWizardPage fSelectTypePage;
    private NonModalWizardDialog fNonModalWizard = null;

    private final List<String> fTraceTypesToScan = new ArrayList<String>();
    private final Set<String> fParentFilesToScan = new TreeSet<String>();

    private ImportTraceContentProvider fScannedTraces = new ImportTraceContentProvider();

    private final Map<TraceValidationHelper, Boolean> fResults = new HashMap<TraceValidationHelper, Boolean>();
    private boolean fOverwrite = true;
    private boolean fLinked = true;

    private BlockingQueue<TraceValidationHelper> fTracesToScan;
    private final Set<FileAndName> fTraces = new TreeSet<FileAndName>();
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
        // keep in case it's called later
        fResults.clear();
    }

    @Override
    public void addPages() {
        addPage(fSelectTypePage);
        addPage(fSelectDirectoriesPage);
        addPage(fScanPage);
        final WizardDialog container = (WizardDialog)getContainer();
        container.setPageSize(800, 400);
        container.updateSize();
    }

    /**
     * A non-modal wizard container
     *
     * @return a non-modal wizard container
     */
    public NonModalWizardDialog getNMContainer() {
        if (!(super.getContainer() instanceof NonModalWizardDialog)) {
            if (fNonModalWizard == null) {
                WizardDialog dlg = (WizardDialog) super.getContainer();
                fNonModalWizard = new NonModalWizardDialog(dlg);
            }
            return fNonModalWizard;
        }
        return (NonModalWizardDialog) super.getContainer();
    }

    /**
     * Add a file to scan
     *
     * @param fileName
     *            the file to scan
     */
    public void addFileToScan(final String fileName) {
        fParentFilesToScan.add(fileName);
        startUpdateTask(Messages.BatchImportTraceWizard_add + " " + fileName); //$NON-NLS-1$
    }

    /**
     * Remove files from selection
     *
     * @param fileName
     *            the name of the file to remove
     */
    public void removeFile(final String fileName) {
        fParentFilesToScan.remove(fileName);
        startUpdateTask(Messages.BatchImportTraceWizard_remove + " " + fileName);//$NON-NLS-1$
    }

    private void startUpdateTask(final String taskName) {
        try {
            this.getContainer().run(true, false, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    synchronized (BatchImportTraceWizard.this) { // this should
                                                                 // only run one
                                                                 // at a time
                        SubMonitor sm;
                        sm = SubMonitor.convert(monitor);
                        sm.setTaskName(taskName);
                        sm.setWorkRemaining(TOTALWORK);
                        updateFiles(sm);
                        sm.done();
                    }
                }
            });
        } catch (InvocationTargetException e) {
        } catch (InterruptedException e) {
        } finally {

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
                    createLink(fTargetFolder, Path.fromOSString(traceToImport.getFile().getAbsolutePath()), traceToImport.getName());
                    setTraceType(traceToImport);
                    success = true;
                }
                else {
                    List<File> subList = new ArrayList<File>();
                    subList.add(traceToImport.getFile());
                    IPath path = fTargetFolder.getFullPath().append(traceToImport.getName());
                    final File parentFile = traceToImport.getFile().getParentFile();
                    ImportOperation operation = new ImportOperation(path,
                            parentFile, fileSystemStructureProvider, overwriteQuery,
                            subList);
                    operation.setContext(getShell());
                    if (executeImportOperation(operation)) {
                        setTraceType(traceToImport);
                        success = true;
                    }
                }
            } catch (Exception e) {
            }
        }
        return success;
    }

    private static void createLink(IFolder parentFolder, IPath location, String targetName) {
        File source = new File(location.toString());
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {

            if (source.isDirectory()) {
                IFolder folder = parentFolder.getFolder(targetName);
                IStatus result = workspace.validateLinkLocation(folder, location);
                if (result.isOK()) {
                    folder.createLink(location, IResource.REPLACE, null);
                } else {
                    Activator.getDefault().logError(result.getMessage());
                }
            } else {
                IFile file = parentFolder.getFile(targetName);
                IStatus result = workspace.validateLinkLocation(file, location);
                if (result.isOK()) {
                    file.createLink(location, IResource.REPLACE, null);
                } else {
                    Activator.getDefault().logError(result.getMessage());
                }
            }
        } catch (CoreException e) {

        }
    }

    private void setTraceType(FileAndName traceToImport) {
        IPath path = fTargetFolder.getFullPath().append(traceToImport.getName());
        IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        if (resource != null) {
            try {
                // Set the trace properties for this resource
                boolean traceTypeOK = false;
                String traceBundle = null, traceTypeId = null, traceIcon = null;
                traceTypeId = traceToImport.getTraceTypeId();
                IConfigurationElement ce = TmfTraceType.getInstance().getTraceAttributes(traceTypeId);
                if ((ce != null) && (ce.getContributor() != null)) {
                    traceTypeOK = true;
                    traceBundle = ce.getContributor().getName();
                    traceTypeId = ce.getAttribute(TmfTraceType.ID_ATTR);
                    traceIcon = ce.getAttribute(TmfTraceType.ICON_ATTR);
                }
                final String traceType = traceTypeId;
                if (!traceTypeOK &&
                        (traceType.startsWith(TmfTraceType.CUSTOM_TXT_CATEGORY) ||
                        traceType.startsWith(TmfTraceType.CUSTOM_XML_CATEGORY))) {
                    // do custom trace stuff here
                    traceTypeOK = true;
                    traceBundle =
                            Activator.getDefault().getBundle().getSymbolicName();

                    traceTypeId = CustomTxtTrace.class.getCanonicalName() + ":" + traceType; //$NON-NLS-1$
                    traceIcon = DEFAULT_TRACE_ICON_PATH;
                }
                if (traceTypeOK) {
                    resource.setPersistentProperty(TmfCommonConstants.TRACEBUNDLE,
                            traceBundle);
                    resource.setPersistentProperty(TmfCommonConstants.TRACETYPE,
                            traceTypeId);
                    resource.setPersistentProperty(TmfCommonConstants.TRACEICON,
                            traceIcon);
                }
                TmfProjectElement tmfProject =
                        TmfProjectRegistry.getProject(resource.getProject());
                if (tmfProject != null) {
                    for (TmfTraceElement traceElement : tmfProject.getTracesFolder().getTraces()) {
                        if (traceElement.getName().equals(resource.getName())) {
                            traceElement.refreshTraceType();
                            break;
                        }
                    }
                }
            } catch (CoreException e) {
                Activator.getDefault().logError("Error importing trace resource " + resource.getName(), e); //$NON-NLS-1$
            }
        }
    }

    @Override
    public boolean canFinish() {
        return super.canFinish() && hasTracesToImport() && !hasConflicts();
    }

    /**
     * Returns if a trace to import is selected
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
        fTraceTypesToScan.clear();
        fTraceTypesToScan.addAll(tracesToScan);
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
            System.out.println(e.getTargetException());
            return false;
        }

        IStatus status = op.getStatus();
        if (!status.isOK()) {
            ErrorDialog.openError(getContainer().getShell(), Messages.ImportTraceWizard_ImportProblem, null, status);
            return false;
        }

        return true;
    }

    private static void initializeOperation(ImportOperation op) {
        op.setCreateContainerStructure(false);
        op.setOverwriteResources(false);
        op.setCreateLinks(true);
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

    /*
     * I am a job. Make me work
     */
    private synchronized IStatus updateFiles(IProgressMonitor monitor) {
        final Set<String> filesToScan = new TreeSet<String>();
        final String[] parentFiles = fParentFilesToScan.toArray(new String[0]);
        final String[] traceTypes = fTraceTypesToScan.toArray(new String[0]);
        int workToDo = 1;
        for (String name : parentFiles) {

            final File file = new File(name);
            final File[] listFiles = file.listFiles();
            if (listFiles != null) {
                workToDo += listFiles.length;
            }
        }
        int step = TOTALWORK / workToDo;
        try {
            for (String name : parentFiles) {
                final File fileToAdd = new File(name);
                recurse(filesToScan, fileToAdd, monitor, step);
            }

            for (String fileToScan : filesToScan) {
                for (String traceCat : traceTypes) {
                    TraceValidationHelper tv = new TraceValidationHelper(fileToScan, traceCat);
                    // for thread safety, keep checks in this order.
                    if (!fResults.containsKey(tv)) {
                        if (!fTracesToScan.contains(tv)) {
                            fTracesToScan.put(tv);
                            monitor.subTask(tv.getTraceToScan());
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            monitor.done();
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
        }

        monitor.done();
        return Status.OK_STATUS;
    }

    private void recurse(Set<String> filesToScan, File fileToAdd, IProgressMonitor monitor, int step) {
        final String absolutePath = fileToAdd.getAbsolutePath();
        if (!filesToScan.contains(absolutePath) && (filesToScan.size() < MAX_FILES)) {
            filesToScan.add(absolutePath);
            final File[] listFiles = fileToAdd.listFiles();
            if (null != listFiles) {
                for (File child : listFiles) {
                    monitor.subTask(child.getName());
                    recurse(filesToScan, child);
                    monitor.worked(step);
                }
            }
        }
    }

    private void recurse(Set<String> filesToScan, File fileToAdd) {
        final String absolutePath = fileToAdd.getAbsolutePath();
        if (!filesToScan.contains(absolutePath) && (filesToScan.size() < MAX_FILES)) {
            filesToScan.add(absolutePath);
            final File[] listFiles = fileToAdd.listFiles();
            if (null != listFiles) {
                for (File child : listFiles) {
                    recurse(filesToScan, child);
                }
            }
        }
    }

    /**
     * Gets the folder in the resource (project)
     *
     * @param targetFolder
     *            the folder to import to
     */
    public void setTraceFolder(IFolder targetFolder) {
        fTargetFolder = targetFolder;
    }

}
