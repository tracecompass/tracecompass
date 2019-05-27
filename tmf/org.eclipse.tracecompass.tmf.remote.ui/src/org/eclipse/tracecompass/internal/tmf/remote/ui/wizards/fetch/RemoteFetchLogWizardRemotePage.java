/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.tracecompass.internal.tmf.remote.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.remote.ui.messages.RemoteMessages;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteGenerateManifestOperation;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportConnectionNodeElement;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportProfileElement;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportTracesOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.NewExperimentOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.SelectTracesOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.AbstractTracePackageOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.AbstractTracePackageWizardPage;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageLabelProvider;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.importexport.Messages;
import org.eclipse.tracecompass.tmf.core.TmfProjectNature;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Wizard page that connects to a remote node, lists all traces according to
 * a given remote profile and imports the selected traces.

 * @author Marc-Andre Laperle
 * @author Bernd Hufmann
 */
public class RemoteFetchLogWizardRemotePage extends AbstractTracePackageWizardPage {

    // ------------------------------------------------------------------------
    // Constant(s)
    // ------------------------------------------------------------------------
    /* The page name */
    private static final String PAGE_NAME = "org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch"; //$NON-NLS-1$
    private static final String ICON_PATH = "icons/elcl16/fetch_log_wiz.gif"; //$NON-NLS-1$
    private static final Image COLLAPSE_ALL_IMAGE = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_COLLAPSEALL);
    private static final Image EXPAND_ALL_IMAGE = Activator.getDefault().getImageFromPath("icons/elcl16/expandall.png"); //$NON-NLS-1$
    /** Name of default project to import traces to */
    public static final String DEFAULT_REMOTE_PROJECT_NAME = "Remote"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes(s)
    // ------------------------------------------------------------------------
    private TmfTraceFolder fTmfTraceFolder;

    private RemoteImportProfileElement fProfile;
    private final Set<RemoteImportConnectionNodeElement> fRemoteHosts = new HashSet<>();
    private boolean fOverwriteAll;
    private boolean fIsVisible = false;
    private String fDefaultProjectName = null;
    private CCombo fCombo;
    private Button fCreateExperimentCheckbox;
    private Text fExperimentNameText;
    private List<IProject> fProjects;
    private @Nullable TmfExperimentFolder fExperimentFolderElement;
    private @Nullable String fExperimentName;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param title
     *          The Wizard title
     * @param selection
     *          The current selection (trace folder element)
     * @param profile
     *          A profile to use or null
     * @param experimentName
     *          A name of an experiment to create and add traces to, or null
     *          for no experiment
     */
    protected RemoteFetchLogWizardRemotePage(String title, IStructuredSelection selection, @Nullable RemoteImportProfileElement profile, @Nullable String experimentName) {
        super(PAGE_NAME, title, null, selection);
        setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, ICON_PATH));

        if (selection.getFirstElement() instanceof TmfTraceFolder) {
            fTmfTraceFolder = (TmfTraceFolder) selection.getFirstElement();
        }

        if (fTmfTraceFolder == null) {
            // create default project
            TmfProjectRegistry.createProject(DEFAULT_REMOTE_PROJECT_NAME, null, null);
            fDefaultProjectName = DEFAULT_REMOTE_PROJECT_NAME;
        }

        fProfile = profile;
        fExperimentName = experimentName;
        setDescription(RemoteMessages.RemoteFetchLogWizardRemotePage_Description);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());

        createToolBar(composite);
        createElementViewer(composite);

        createButtonsGroup(composite);

        createProjectGroup(composite);

        createOptionGroup(composite);

        restoreWidgetValues();
        updatePageCompletion();

        setControl(composite);
    }

    @Override
    protected boolean determinePageCompletion() {
         if (getElementViewer().getCheckedElements().length <= 0) {
             return false;
         }

         validateProject();

         if ((fExperimentName != null) && (fCreateExperimentCheckbox.getSelection())) {
             String name = fExperimentNameText.getText().trim();
             // verify if experiment name is empty
             if (name.isEmpty()) {
                 setMessage(null);
                 setErrorMessage(RemoteMessages.RemoteFetchLogWizardRemotePage_ErrorEmptyExperimentName);
                 return false;
             }

             // verify that name is a valid resource name
             IWorkspace workspace = ResourcesPlugin.getWorkspace();
             if ((workspace != null) && (!workspace.validateName(name, IResource.FILE).isOK())) {
                 setMessage(null);
                 setErrorMessage(NLS.bind(RemoteMessages.RemoteFetchLogWizardRemotePage_ErrorExperimentNameInvalid, name));
                 return false;
             }

             TmfExperimentFolder experimentFolder = fExperimentFolderElement;
             if (experimentFolder != null) {
                 TmfExperimentElement element =
                         experimentFolder.getExperiment(name);
                 // verify if experiment already exists
                 if (element != null) {
                     setMessage(null);
                     setErrorMessage(NLS.bind(RemoteMessages.RemoteFetchLogWizardRemotePage_ErrorExperimentAlreadyExists, name));
                     return false;
                 }

                 // verify if a resource already exists in the experiment folder
                 IFolder expResource = experimentFolder.getResource();
                 IResource res = expResource.findMember(name);
                 if (res != null) {
                     setMessage(null);
                     setErrorMessage(NLS.bind(RemoteMessages.RemoteFetchLogWizardRemotePage_ErrorResourceAlreadyExists, name));
                     return false;
                 }
             }
         }

         return true;

    }

    @Override
    protected void updateWithFilePathSelection() {
        // Do nothing
    }

    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);

        fIsVisible = visible;

        if (visible) {
            getContainer().getShell().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    updateViewer();
                }
            });
        } else {
            getElementViewer().setInput(null);
        }
    }

    private void createToolBar(Composite parent) {
        ToolBar toolBar = new ToolBar(parent, SWT.HORIZONTAL);
        toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        ToolItem expandAll = new ToolItem(toolBar, SWT.PUSH);
        expandAll.setImage(EXPAND_ALL_IMAGE);
        expandAll.setToolTipText(RemoteMessages.RemoteFetchLogWizardRemotePage_ExpandAll);
        expandAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                getElementViewer().expandAll();
            }
        });

        ToolItem collapseAll = new ToolItem(toolBar, SWT.PUSH);
        collapseAll.setImage(COLLAPSE_ALL_IMAGE);
        collapseAll.setToolTipText(RemoteMessages.RemoteFetchLogWizardRemotePage_CollapseAll);
        collapseAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                getElementViewer().collapseAll();
            }
        });
    }

    @Override
    protected void createElementViewer(Composite compositeParent) {
        super.createElementViewer(compositeParent);

        CheckboxTreeViewer elementViewer = getElementViewer();
        elementViewer.setLabelProvider(new TracePackageLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof TracePackageTraceElement) {
                    for (TracePackageElement files : ((TracePackageTraceElement) element).getChildren()) {
                        if (files instanceof TracePackageFilesElement) {
                            return ((TracePackageFilesElement) files).getFileName();
                        }
                    }
                }
                return super.getText(element);
            }
            @Override
            public Image getImage(Object element) {
                if (element instanceof TracePackageTraceElement) {
                    for (TracePackageElement files : ((TracePackageTraceElement) element).getChildren()) {
                        return files.getImage();
                    }
                }
                return super.getImage(element);
            }
        });
        elementViewer.setComparator(new ViewerComparator() {
            @Override
            public int category(Object element) {
                if (element instanceof TracePackageTraceElement) {
                    return 1;
                }
                return 0;
            }
        });
    }

    private void createProjectGroup(Composite parent) {
        if (fDefaultProjectName != null) {
            Group projectGroup = new Group(parent, SWT.SHADOW_NONE);
            projectGroup.setText(RemoteMessages.RemoteFetchLogWizardRemotePage_ImportDialogProjectsGroupName);
            GridLayout layout = new GridLayout(1, true);
            projectGroup.setLayout(layout);
            projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            fProjects = new ArrayList<>();
            List<String> projectNames = new ArrayList<>();

            for (IProject project : TraceUtils.getOpenedTmfProjects()) {
                fProjects.add(project);
                projectNames.add(project.getName());
            }

            fCombo = new CCombo(projectGroup, SWT.READ_ONLY);
            fCombo.setToolTipText(RemoteMessages.RemoteFetchLogWizardRemotePage_ImportDialogProjectsGroupName);
            fCombo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
            fCombo.setItems(projectNames.toArray(new String[projectNames.size()]));
            int select = projectNames.indexOf(fDefaultProjectName);
            fCombo.select(select);
        }
    }

    private void createOptionGroup(Composite parent) {
        if (fExperimentName != null) {
            Group comp = new Group(parent, SWT.SHADOW_NONE);
            comp.setText(RemoteMessages.RemoteFetchLogWizardRemotePage_OptionsGroupName);
            GridLayout layout = new GridLayout(2, false);
            comp.setLayout(layout);
            GridData data = new GridData(GridData.FILL, GridData.CENTER, true, false);
            comp.setLayoutData(data);

            fCreateExperimentCheckbox = new Button(comp, SWT.CHECK);
            fCreateExperimentCheckbox.setFont(comp.getFont());
            fCreateExperimentCheckbox.setText(RemoteMessages.RemoteFetchLogWizardRemotePage_CreateExperimentName);
            fCreateExperimentCheckbox.setSelection(false);
            data = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
            fCreateExperimentCheckbox.setLayoutData(data);

            fExperimentNameText = new Text(comp, SWT.BORDER);
            data = new GridData(GridData.FILL, GridData.CENTER, true, false);
            fExperimentNameText.setLayoutData(data);
            fExperimentNameText.setText(fExperimentName);
            fExperimentNameText.setEnabled(fCreateExperimentCheckbox.getSelection());

            fExperimentNameText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updatePageCompletion();
                }
            });

            fCreateExperimentCheckbox.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    fExperimentNameText.setEnabled(fCreateExperimentCheckbox.getSelection());
                    updatePageCompletion();
                }
            });
        }
    }

    @Override
    protected Object createElementViewerInput() {
        if (fProfile == null) {
            return null;
        }
        final List<RemoteImportConnectionNodeElement> remoteHosts = fProfile.getConnectionNodeElements();

        if (remoteHosts.isEmpty()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, RemoteMessages.RemoteFetchLogWizardRemotePage_MissingConnectionInformation);
        }

        final IStatus status[] = new IStatus[1];
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    SubMonitor subMonitor = SubMonitor.convert(monitor, remoteHosts.size());
                    for (final RemoteImportConnectionNodeElement remoteHost : remoteHosts) {
                        SubMonitor child = subMonitor.newChild(1);
                        child.setTaskName(MessageFormat.format(RemoteMessages.RemoteFetchLogWizardRemotePage_OpeningConnectionTo, remoteHost.getURI()));
                        status[0] = remoteHost.connect(checkNotNull(subMonitor.newChild(1)));
                        if (!status[0].isOK()) {
                            monitor.done();
                            return;
                        }
                        // cache remote host
                        fRemoteHosts.add(remoteHost);
                    }
                    monitor.done();
                }
            });
        } catch (InvocationTargetException e) {
            handleError(
                    MessageFormat.format(RemoteMessages.RemoteFetchLogWizardRemotePage_ConnectionError, fProfile.getText()),
                    e);
            return null;
        } catch (InterruptedException e) {
            // Cancelled
            status[0] = Status.CANCEL_STATUS;
        }

        if (!status[0].isOK()) {
            handleErrorStatus(status[0]);
            return null;
        }

        try {
            final AbstractTracePackageOperation generateManifestOperation = new RemoteGenerateManifestOperation(fProfile);

            getContainer().run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(
                            Messages.ImportTracePackageWizardPage_ReadingPackage,
                            10);
                    generateManifestOperation.run(monitor);
                    monitor.done();
                }
            });

            status[0] = generateManifestOperation.getStatus();
            if (status[0].getSeverity() == IStatus.ERROR) {
                handleErrorStatus(status[0]);
                return null;
            }
            return generateManifestOperation.getResultElements();
        } catch (InvocationTargetException e1) {
            handleError(
                    Messages.TracePackageExtractManifestOperation_ErrorReadingManifest,
                    e1);
        } catch (InterruptedException e1) {
            // Canceled
        }
        return null;
    }

    /**
     * Method to set input data for this wizard
     *
     * @param profile
     *              The remote profile
     * @param overwriteAll
     *              Overwrite existing traces without confirmation
     */
    public void setPageData(RemoteImportProfileElement profile, boolean overwriteAll) {
        fProfile = profile;
        fOverwriteAll = overwriteAll;
    }

    /**
     * Finishes the wizard page.
     *
     * @return <code>true</code> if successful else <code>false</code>
     */
    public boolean finish() {

        boolean result;
        if (!fIsVisible) {
            result = updateViewer();
            if (!result) {
                return false;
            }
        }

        result = validateProject();
        if (!result) {
            return false;
        }

        Object[] elements = getElementViewer().getCheckedElements();
        final RemoteImportTracesOperation importOperation = new RemoteImportTracesOperation(getContainer().getShell(), fTmfTraceFolder, elements, fOverwriteAll);

        final List<IResource> traceResources = new ArrayList<>();
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    importOperation.run(monitor);
                    traceResources.addAll(importOperation.getImportedResources());
                    monitor.done();
                }
            });

            if ((fExperimentName != null) && fCreateExperimentCheckbox.getSelection() && (!traceResources.isEmpty())) {
                final IFolder[] experimentFolders = new IFolder[1];
                final TmfExperimentFolder root = fExperimentFolderElement;
                final IStatus[] operationStatus = new IStatus[1];
                final String experimentName = fExperimentNameText.getText().trim();

                // just safety guards
                if ((root == null) || (experimentName.isEmpty())) {
                    return true;
                }
                getContainer().run(true, true, new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        operationStatus[0] = null;
                        final NewExperimentOperation operation = new NewExperimentOperation(root, experimentName);
                        operation.run(monitor);
                        monitor.done();
                        operationStatus[0] = operation.getStatus();
                        experimentFolders[0] = operation.getExperimentFolder();
                    }
                });

                final IFolder expFolder = experimentFolders[0];
                final TmfTraceFolder parentTraceFolder = fTmfTraceFolder;
                // just safety guards
                if ((expFolder == null) || (parentTraceFolder == null)) {
                    return true;
                }

                if ((operationStatus[0] != null) && (operationStatus[0].isOK())) {
                    getContainer().run(true, true, new IRunnableWithProgress() {
                        @Override
                        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                            operationStatus[0] = null;
                            final SelectTracesOperation operation = new SelectTracesOperation(root, expFolder, parentTraceFolder, traceResources);
                            operation.run(monitor);
                            monitor.done();
                            operationStatus[0] = operation.getStatus();
                        }
                    });
                }
            }
        } catch (InvocationTargetException e) {
            handleError(
                    Messages.TracePackageExtractManifestOperation_ErrorReadingManifest,
                    e);
            return false;
        } catch (InterruptedException e) {
            // Cancelled
            return false;
        }

        IStatus status = importOperation.getStatus();
        if (status.getSeverity() == IStatus.ERROR) {
            handleErrorStatus(status);
            return false;
        }

        disconnectAllRemoteHosts();
        return true;
    }

    /**
     * Cancels the the wizard and disconnects all open connections.
     */
    public void cancel() {
        disconnectAllRemoteHosts();
    }

    // ------------------------------------------------------------------------
    // Helper Methods
    // ------------------------------------------------------------------------
    private void disconnectAllRemoteHosts() {
        for (RemoteImportConnectionNodeElement remoteHost : fRemoteHosts) {
            remoteHost.disconnect();
        }
    }

    private boolean updateViewer() {
        Object elementViewerInput = createElementViewerInput();
        if (elementViewerInput == null) {
            return false;
        }

        CheckboxTreeViewer elementViewer = getElementViewer();
        elementViewer.setInput(elementViewerInput);
        elementViewer.expandToLevel(3);
        setAllChecked(elementViewer, false, true);
        updatePageCompletion();

        return true;
    }

    private boolean validateProject() {
        if (fCombo != null) {
            int fProjectIndex = fCombo.getSelectionIndex();
            if (fProjectIndex < 0) {
                handleError(RemoteMessages.RemoteFetchLogWizardRemotePage_NoProjectSelectedError, null);
                return false;
            }

            IProject project = fProjects.get(fProjectIndex);
            TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
            TmfTraceFolder tracesFolderElement = projectElement.getTracesFolder();

            if (tracesFolderElement == null) {
                handleError(RemoteMessages.RemoteFetchLogWizardRemotePage_InvalidTracingProject, null);
                return false;
            }

            IFolder traceFolder = tracesFolderElement.getResource();

            if (!traceFolder.exists()) {
                handleError(RemoteMessages.RemoteFetchLogWizardRemotePage_InvalidTracingProject, null);
                return false;
            }

            try {
                if (project.hasNature(TmfProjectNature.ID)) {
                    fTmfTraceFolder = projectElement.getTracesFolder();
                    fExperimentFolderElement = projectElement.getExperimentsFolder();
                }
            } catch (CoreException ex) {
                handleError(RemoteMessages.RemoteFetchLogWizardRemotePage_InvalidTracingProject, ex);
                return false;
            }
        }
        return true;
    }

}
