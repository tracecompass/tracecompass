/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Added handling of streamed traces
 *   Marc-Andre Laperle - Use common method to get opened tmf projects
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.remote.IRemoteSystemProxy;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceUtils;
import org.eclipse.rse.core.subsystems.RemoteChildrenContentsType;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * <p>
 * Dialog box for collecting trace import information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class ImportDialog extends Dialog implements IImportDialog {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /** The icon file for this dialog box. */
    public static final String IMPORT_ICON_FILE = "icons/elcl16/import_trace.gif"; //$NON-NLS-1$

    /** Parent directory for UST traces */
    public static final String UST_PARENT_DIRECTORY = "ust"; //$NON-NLS-1$

    /** Name of metadata file of trace */
    public static final String METADATA_FILE_NAME = "metadata"; //$NON-NLS-1$

    /** Default name of kernel traces */
    public static final String DEFAULT_KERNEL_TRACE_NAME = "kernel"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The dialog composite.
     */
    private Composite fDialogComposite = null;
    /**
     * The checkbox tree viewer for selecting available traces
     */
    private CheckboxTreeViewer fFolderViewer;
    /**
     * The combo box for selecting a project.
     */
    private CCombo fCombo;
    /**
     * The overwrite button
     */
    private Button fOverwriteButton;
    /**
     * List of available LTTng 2.0 projects
     */
    private List<IProject> fProjects;
    /**
     * The parent where the new node should be added.
     */
    private TraceSessionComponent fSession = null;

    /**
     * The name of the default project name
     */
    private String fDefaultProjectName = null;
    /**
     * List of traces to import
     */
    private final List<ImportFileInfo> fTraces = new ArrayList<ImportFileInfo>();
    /**
     * Selection index in project combo box.
     */
    private int fProjectIndex;
    /**
     * Flag to indicate that something went wrong when creating the dialog box.
     */
    private boolean fIsError = false;
    /**
     * Children of the remote folder (can be null)
     */
    private Object[] fFolderChildren = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param shell - a shell for the display of the dialog
     */
    public ImportDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.RESIZE | getShellStyle());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public List<ImportFileInfo> getTracePathes() {
        List<ImportFileInfo> retList = new ArrayList<ImportFileInfo>();
        retList.addAll(fTraces);
        return retList;
    }

    @Override
    public IProject getProject() {
        return fProjects.get(fProjectIndex);
    }

    @Override
    public void setSession(TraceSessionComponent session) {
        fSession = session;
    }

    @Override
    public void setDefaultProject(String defaultProject) {
        fDefaultProjectName = defaultProject;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TraceControl_ImportDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(IMPORT_ICON_FILE));
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        // Main dialog panel
        fDialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        fDialogComposite.setLayout(layout);
        fDialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        try {
            createRemoteComposite();
        } catch (SystemMessageException e) {
            createErrorComposite(parent, e.fillInStackTrace());
            return fDialogComposite;
        }
        return fDialogComposite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button selectAllButton = createButton(parent, IDialogConstants.SELECT_ALL_ID, Messages.TraceControl_ImportDialog_SelectAll, true);
        selectAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setFolderChildrenChecked(true);
            }
        });

        Button deselectAllButton = createButton(parent, IDialogConstants.DESELECT_ALL_ID, Messages.TraceControl_ImportDialog_DeselectAll, true);
        deselectAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setFolderChildrenChecked(false);
            }
        });
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        updateOKButtonEnablement();
    }

    @Override
    protected void okPressed() {
        if (!fIsError) {

            // Validate input data
            fTraces.clear();

            fProjectIndex = fCombo.getSelectionIndex();

            if (fProjectIndex < 0) {
                MessageDialog.openError(getShell(),
                        Messages.TraceControl_ImportDialogTitle,
                        Messages.TraceControl_ImportDialogNoProjectSelectedError);
                return;
            }

            IProject project = fProjects.get(fProjectIndex);
            IFolder traceFolder = project.getFolder(TmfTraceFolder.TRACE_FOLDER_NAME);

            if (!traceFolder.exists()) {
                // Invalid LTTng 2.0 project
                MessageDialog.openError(getShell(),
                        Messages.TraceControl_ImportDialogTitle,
                        Messages.TraceControl_ImportDialogInvalidTracingProject + " (" + TmfTraceFolder.TRACE_FOLDER_NAME + ")");  //$NON-NLS-1$//$NON-NLS-2$
                return;
            }

            boolean overwriteAll = fOverwriteButton.getSelection();

            Object[] checked = fFolderViewer.getCheckedElements();
            for (int i = 0; i < checked.length; i++) {
                IRemoteFile file = (IRemoteFile) checked[i];
                if (!file.isDirectory() && file.getName().equals(METADATA_FILE_NAME)) {
                    IRemoteFile trace = file.getParentRemoteFile();
                    IRemoteFile parent = trace.getParentRemoteFile();
                    boolean isKernel = false;
                    if (trace.getName().equals(DEFAULT_KERNEL_TRACE_NAME)) {
                        isKernel = true;
                    }
                    StringBuffer traceName = new StringBuffer();
                    traceName.append(trace.getName());
                    traceName.insert(0, '-');

                    String path = fSession.isSnapshotSession() ? fSession.getSnapshotInfo().getSnapshotPath() : fSession.getSessionPath();

                    while (!parent.getAbsolutePath().equals(path)) {
                        traceName.insert(0, parent.getName());
                        traceName.insert(0, '-');
                        parent = parent.getParentRemoteFile();
                    }
                    traceName.insert(0, parent.getName());

                    ImportFileInfo info = new ImportFileInfo(trace, traceName.toString(), overwriteAll, isKernel);
                    IFolder folder = traceFolder.getFolder(traceName.toString());

                    // Verify if trace directory already exists (and not overwrite)
                    if (folder.exists() && !overwriteAll) {

                        // Ask user for overwrite or new name
                        IImportConfirmationDialog conf = TraceControlDialogFactory.getInstance().getImportConfirmationDialog();
                        conf.setTraceName(traceName.toString());

                        // Don't add trace to list if dialog was cancelled.
                        if (conf.open() == Window.OK) {
                            info.setOverwrite(conf.isOverwrite());
                            if (!conf.isOverwrite()) {
                                info.setLocalTraceName(conf.getNewTraceName());
                            }
                            fTraces.add(info);
                        }
                    } else {
                        fTraces.add(info);
                    }
                }
            }

            if (fTraces.isEmpty()) {
                MessageDialog.openError(getShell(),
                        Messages.TraceControl_ImportDialogTitle,
                        Messages.TraceControl_ImportDialogNoTraceSelectedError);
                return;
            }
        }

        // validation successful -> call super.okPressed()
        super.okPressed();
    }

    // ------------------------------------------------------------------------
    // Helper methods and classes
    // ------------------------------------------------------------------------

    /**
     * Helper class for the contents of a folder in a tracing project
     *
     * @author Bernd Hufmann
     */
    public static class FolderContentProvider extends WorkbenchContentProvider {
        @Override
        public Object[] getChildren(Object o) {
            if (o instanceof IRemoteFile) {
                IRemoteFile element = (IRemoteFile) o;
                // For our purpose, we need folders + files
                if (!element.isDirectory()) {
                    return new Object[0];
                }
            }
            return super.getChildren(o);
        }
    }

    /**
     * Creates a dialog composite with an error message which can be used
     * when an exception occurred during creation time of the dialog box.
     * @param parent - a parent composite
     * @param e - a error causing exception
     */
    private void createErrorComposite(Composite parent, Throwable e) {
        fIsError = true;
        fDialogComposite.dispose();

        fDialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        fDialogComposite.setLayout(layout);
        fDialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Text errorText = new Text(fDialogComposite, SWT.MULTI);
        StringBuffer error = new StringBuffer();
        error.append(Messages.TraceControl_ImportDialogCreationError);
        error.append(System.getProperty("line.separator")); //$NON-NLS-1$
        error.append(System.getProperty("line.separator")); //$NON-NLS-1$
        error.append(e.toString());
        errorText.setText(error.toString());
        errorText.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    private void createRemoteComposite() throws SystemMessageException{
        Group contextGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        contextGroup.setText(Messages.TraceControl_ImportDialogTracesGroupName);
        GridLayout layout = new GridLayout(1, true);
        contextGroup.setLayout(layout);
        contextGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        IRemoteSystemProxy proxy = fSession.getTargetNode().getRemoteSystemProxy();

        IFileServiceSubSystem fsss = proxy.getFileServiceSubSystem();

        final String path = fSession.isSnapshotSession() ? fSession.getSnapshotInfo().getSnapshotPath() : fSession.getSessionPath();
        final IRemoteFile remoteFolder = fsss.getRemoteFileObject(path, new NullProgressMonitor());
        // make sure that remote directory is read and not cached
        remoteFolder.markStale(true, true);

        fFolderViewer = new CheckboxTreeViewer(contextGroup, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData data = new GridData(GridData.FILL_BOTH);
        Tree tree = fFolderViewer.getTree();
        tree.setLayoutData(data);
        tree.setFont(fDialogComposite.getFont());
        tree.setToolTipText(Messages.TraceControl_ImportDialogTracesTooltip);

        fFolderViewer.setContentProvider(new FolderContentProvider());
        fFolderViewer.setLabelProvider(new WorkbenchLabelProvider());

        fFolderViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                Object elem = event.getElement();
                if (elem instanceof IRemoteFile) {
                    IRemoteFile element = (IRemoteFile) elem;
                    if (!element.isDirectory()) {
                        // A trick to keep selection of a file in sync with the directory
                        boolean p = fFolderViewer.getChecked((element.getParentRemoteFile()));
                        fFolderViewer.setChecked(element, p);
                    } else {
                        fFolderViewer.setSubtreeChecked(event.getElement(), event.getChecked());
                        if (!event.getChecked()) {
                            fFolderViewer.setChecked(element.getParentRemoteFile(), false);
                        }
                    }
                    updateOKButtonEnablement();
                }
            }
        });
        fFolderViewer.setInput(remoteFolder);

        fFolderChildren = remoteFolder.getContents(RemoteChildrenContentsType.getInstance());
        // children can be null if there the path doesn't exist. This happens when a trace
        // session hadn't been started and no output was created.
        setFolderChildrenChecked(true);

        Group projectGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        projectGroup.setText(Messages.TraceControl_ImportDialogProjectsGroupName);
        layout = new GridLayout(1, true);
        projectGroup.setLayout(layout);
        projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fProjects = new ArrayList<IProject>();
        List<String> projectNames = new ArrayList<String>();

        for (IProject project : TraceUtils.getOpenedTmfProjects()) {
            fProjects.add(project);
            projectNames.add(project.getName());
        }

        fCombo = new CCombo(projectGroup, SWT.READ_ONLY);
        fCombo.setToolTipText(Messages.TraceControl_ImportDialogProjectsTooltip);
        fCombo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
        fCombo.setItems(projectNames.toArray(new String[projectNames.size()]));

        if (fDefaultProjectName != null) {
            int select = projectNames.indexOf(fDefaultProjectName);
            fCombo.select(select);
        }

        Group overrideGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        layout = new GridLayout(1, true);
        overrideGroup.setLayout(layout);
        overrideGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fOverwriteButton = new Button(overrideGroup, SWT.CHECK);
        fOverwriteButton.setText(Messages.TraceControl_ImportDialogOverwriteButtonText);
        getShell().setMinimumSize(new Point(500, 400));
    }

    private void setFolderChildrenChecked(boolean isChecked) {
        if (fFolderChildren != null) {
            for (Object child : fFolderChildren) {
                fFolderViewer.setSubtreeChecked(child, isChecked);
            }
        }
        updateOKButtonEnablement();
    }

    private void updateOKButtonEnablement() {
        Object[] checked = fFolderViewer.getCheckedElements();
        Button okButton = getButton(IDialogConstants.OK_ID);
        if (okButton != null) {
            okButton.setEnabled(checked.length > 0);
        }
    }
 }
