/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Got rid of dependency on internal platform class
 *   Francois Chouinard - Complete re-design
 *   Anna Dushistova(Montavista) - [383047] NPE while importing a CFT trace
 *   Matthew Khouzam - Moved out some common functions
 *   Patrick Tasse - Add sorting of file system elements
 *   Bernd Hufmann - Re-design of trace selection and trace validation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.TmfProjectNature;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceTypeUIUtils;
import org.eclipse.linuxtools.tmf.ui.project.wizards.Messages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.dialogs.WizardResourceImportPage;
import org.eclipse.ui.internal.ide.dialogs.IElementFilter;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * A variant of the standard resource import wizard for importing traces
 * to given tracing project. If no project or tracing project was selected
 * the wizard imports it to the default tracing project which is created
 * if necessary.
 *
 * In our case traces could be files or a directory structure. This wizard
 * supports both cases. It imports traces for a selected trace type or, if
 * no trace type is selected, it tries to detect the trace type automatically.
 * However, the automatic detection is a best-effort and cannot guarantee
 * that the detection is successful. The reason for this is that there might
 * be multiple trace types that can be assigned to a single trace.
 *
 *
 * @author Francois Chouinard
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class ImportTraceWizardPage extends WizardResourceImportPage {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String IMPORT_WIZARD_PAGE = "ImportTraceWizardPage"; //$NON-NLS-1$
    private static final String IMPORT_WIZARD_IMPORT_UNRECOGNIZED_ID = IMPORT_WIZARD_PAGE + ".import_unrecognized_traces_id"; //$NON-NLS-1$
    private static final String SEPARATOR = ":"; //$NON-NLS-1$
    private static final String AUTO_DETECT = Messages.ImportTraceWizard_AutoDetection;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Folder navigation start point (saved between invocations)
    private static String fRootDirectory = null;
    // Target import directory ('Traces' folder)
    private IFolder fTargetFolder;
    // Target Trace folder element
    private TmfTraceFolder fTraceFolderElement;
    // Flag to handle destination folder change event
    private Boolean fIsDestinationChanged = false;
    // Combo box containing trace types
    private Combo fTraceTypes;
    // Button to ignore unrecognized traces or not
    private Button fImportUnrecognizedButton;
    // Button to overwrite existing resources or not
    private Button fOverwriteExistingResourcesCheckbox;
    // Button to link or copy traces to workspace
    private Button fCreateLinksInWorkspaceButton;
    private boolean entryChanged = false;
    /** The directory name field */
    protected Combo directoryNameField;
    /** The directory browse button. */
    protected Button directoryBrowseButton;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor. Creates the trace wizard page.
     *
     * @param name
     *            The name of the page.
     * @param selection
     *            The current selection
     */
    protected ImportTraceWizardPage(String name, IStructuredSelection selection) {
        super(name, selection);
    }

    /**
     * Constructor
     *
     * @param workbench
     *            The workbench reference.
     * @param selection
     *            The current selection
     */
    public ImportTraceWizardPage(IWorkbench workbench, IStructuredSelection selection) {
        this(IMPORT_WIZARD_PAGE, selection);
        setTitle(Messages.ImportTraceWizard_FileSystemTitle);
        setDescription(Messages.ImportTraceWizard_ImportTrace);

        // Locate the target trace folder
        IFolder traceFolder = null;
        Object element = selection.getFirstElement();

        if (element instanceof TmfTraceFolder) {
            fTraceFolderElement = (TmfTraceFolder) element;
            traceFolder = fTraceFolderElement.getResource();
        } else if (element instanceof IProject) {
            IProject project = (IProject) element;
            try {
                if (project.hasNature(TmfProjectNature.ID)) {
                    TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
                    fTraceFolderElement = projectElement.getTracesFolder();
                    traceFolder = project.getFolder(TmfTraceFolder.TRACE_FOLDER_NAME);
                }
            } catch (CoreException e) {
            }
        }

         //  If no tracing project was selected or trace folder doesn't exist use
         //  default tracing project
        if (traceFolder == null) {
            IProject project = TmfProjectRegistry.createProject(
                    TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME, null, new NullProgressMonitor());
            TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
            fTraceFolderElement = projectElement.getTracesFolder();
            traceFolder = project.getFolder(TmfTraceFolder.TRACE_FOLDER_NAME);
        }

        // Set the target trace folder
        if (traceFolder != null) {
            fTargetFolder = traceFolder;
            String path = traceFolder.getFullPath().toString();
            setContainerFieldValue(path);
        }
    }

    // ------------------------------------------------------------------------
    // WizardResourceImportPage
    // ------------------------------------------------------------------------

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        // Restore last directory if applicable
        if (fRootDirectory != null) {
            directoryNameField.setText(fRootDirectory);
            updateFromSourceField();
        }
    }

    @Override
    protected void createSourceGroup(Composite parent) {
        createDirectorySelectionGroup(parent);
        createFileSelectionGroup(parent);
        createTraceTypeGroup(parent);
        validateSourceGroup();
    }

    @Override
    protected ITreeContentProvider getFileProvider() {
        return new WorkbenchContentProvider() {
            @Override
            public Object[] getChildren(Object object) {
                if (object instanceof TraceFileSystemElement) {
                    TraceFileSystemElement element = (TraceFileSystemElement) object;
                    return element.getFiles().getChildren(element);
                }
                return new Object[0];
            }
        };
    }

    @Override
    protected ITreeContentProvider getFolderProvider() {
        return new WorkbenchContentProvider() {
            @Override
            public Object[] getChildren(Object o) {
                if (o instanceof TraceFileSystemElement) {
                    TraceFileSystemElement element = (TraceFileSystemElement) o;
                    return element.getFolders().getChildren();
                }
                return new Object[0];
            }

            @Override
            public boolean hasChildren(Object o) {
                if (o instanceof TraceFileSystemElement) {
                    TraceFileSystemElement element = (TraceFileSystemElement) o;
                    if (element.isPopulated()) {
                        return getChildren(element).length > 0;
                    }
                    //If we have not populated then wait until asked
                    return true;
                }
                return false;
            }
        };
    }

    // ------------------------------------------------------------------------
    // Directory Selection Group (forked WizardFileSystemResourceImportPage1)
    // ------------------------------------------------------------------------

    /**
     * creates the directory selection group.
     *
     * @param parent
     *            the parent composite
     */
    protected void createDirectorySelectionGroup(Composite parent) {

        Composite directoryContainerGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        directoryContainerGroup.setLayout(layout);
        directoryContainerGroup.setFont(parent.getFont());
        directoryContainerGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        // Label ("Trace directory:")
        Label groupLabel = new Label(directoryContainerGroup, SWT.NONE);
        groupLabel.setText(Messages.ImportTraceWizard_DirectoryLocation);
        groupLabel.setFont(parent.getFont());

        // Directory name entry field
        directoryNameField = new Combo(directoryContainerGroup, SWT.BORDER);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        directoryNameField.setLayoutData(data);
        directoryNameField.setFont(parent.getFont());

        directoryNameField.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateFromSourceField();
            }
        });

        directoryNameField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                // If there has been a key pressed then mark as dirty
                entryChanged = true;
                if (e.character == SWT.CR) { // Windows...
                    entryChanged = false;
                    updateFromSourceField();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        directoryNameField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // Do nothing when getting focus
            }
            @Override
            public void focusLost(FocusEvent e) {
                // Clear the flag to prevent constant update
                if (entryChanged) {
                    entryChanged = false;
                    updateFromSourceField();
                }
            }
        });

        // Browse button
        directoryBrowseButton = new Button(directoryContainerGroup, SWT.PUSH);
        directoryBrowseButton.setText(Messages.ImportTraceWizard_BrowseButton);
        directoryBrowseButton.addListener(SWT.Selection, this);
        directoryBrowseButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        directoryBrowseButton.setFont(parent.getFont());
        setButtonLayoutData(directoryBrowseButton);
    }

    // ------------------------------------------------------------------------
    // Browse for the source directory
    // ------------------------------------------------------------------------

    @Override
    public void handleEvent(Event event) {
        if (event.widget == directoryBrowseButton) {
            handleSourceDirectoryBrowseButtonPressed();
        }

        // Avoid overwriting destination path without repeatedly trigger
        // call of handleEvent();
        synchronized (fIsDestinationChanged) {
            if (fIsDestinationChanged == false) {
                event.display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (fIsDestinationChanged) {
                            fIsDestinationChanged = true;
                            String path = fTargetFolder.getFullPath().toString();
                            setContainerFieldValue(path);
                        }
                    }
                });
            } else {
                fIsDestinationChanged = false;
            }
        }
        super.handleEvent(event);
    }

    @Override
    protected void handleContainerBrowseButtonPressed() {
        // Do nothing so that destination directory cannot be changed.
    }

    /**
     * Handle the button pressed event
     */
    protected void handleSourceDirectoryBrowseButtonPressed() {
        String currentSource = directoryNameField.getText();
        DirectoryDialog dialog = new DirectoryDialog(directoryNameField.getShell(), SWT.SAVE | SWT.SHEET);
        dialog.setText(Messages.ImportTraceWizard_SelectTraceDirectoryTitle);
        dialog.setMessage(Messages.ImportTraceWizard_SelectTraceDirectoryMessage);
        dialog.setFilterPath(getSourceDirectoryName(currentSource));

        String selectedDirectory = dialog.open();
        if (selectedDirectory != null) {
            // Just quit if the directory is not valid
            if ((getSourceDirectory(selectedDirectory) == null) || selectedDirectory.equals(currentSource)) {
                return;
            }
            // If it is valid then proceed to populate
            setErrorMessage(null);
            setSourceName(selectedDirectory);
        }
    }

    private File getSourceDirectory() {
        return getSourceDirectory(directoryNameField.getText());
    }

    private static File getSourceDirectory(String path) {
        File sourceDirectory = new File(getSourceDirectoryName(path));
        if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            return null;
        }

        return sourceDirectory;
    }

    private static String getSourceDirectoryName(String sourceName) {
        IPath result = new Path(sourceName.trim());
        if (result.getDevice() != null && result.segmentCount() == 0) {
            result = result.addTrailingSeparator();
        } else {
            result = result.removeTrailingSeparator();
        }
        return result.toOSString();
    }

    private String getSourceDirectoryName() {
        return getSourceDirectoryName(directoryNameField.getText());
    }

    private void updateFromSourceField() {
        setSourceName(directoryNameField.getText());
        updateWidgetEnablements();
    }

    private void setSourceName(String path) {
        if (path.length() > 0) {
            String[] currentItems = directoryNameField.getItems();
            int selectionIndex = -1;
            for (int i = 0; i < currentItems.length; i++) {
                if (currentItems[i].equals(path)) {
                    selectionIndex = i;
                }
            }
            if (selectionIndex < 0) {
                int oldLength = currentItems.length;
                String[] newItems = new String[oldLength + 1];
                System.arraycopy(currentItems, 0, newItems, 0, oldLength);
                newItems[oldLength] = path;
                directoryNameField.setItems(newItems);
                selectionIndex = oldLength;
            }
            directoryNameField.select(selectionIndex);
        }
        resetSelection();
    }

    // ------------------------------------------------------------------------
    // File Selection Group (forked WizardFileSystemResourceImportPage1)
    // ------------------------------------------------------------------------
    private void resetSelection() {
        TraceFileSystemElement root = getFileSystemTree();
        selectionGroup.setRoot(root);
    }

    private TraceFileSystemElement getFileSystemTree() {
        File sourceDirectory = getSourceDirectory();
        if (sourceDirectory == null) {
            return null;
        }
        return selectFiles(sourceDirectory, FileSystemStructureProvider.INSTANCE);
    }

    private TraceFileSystemElement selectFiles(final Object rootFileSystemObject,
            final IImportStructureProvider structureProvider) {
        final TraceFileSystemElement[] results = new TraceFileSystemElement[1];
        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
            @Override
            public void run() {
                // Create the root element from the supplied file system object
                results[0] = createRootElement(rootFileSystemObject, structureProvider);
            }
        });
        return results[0];
    }

    private static TraceFileSystemElement createRootElement(Object fileSystemObject,
            IImportStructureProvider provider) {

        boolean isContainer = provider.isFolder(fileSystemObject);
        String elementLabel = provider.getLabel(fileSystemObject);

        // Use an empty label so that display of the element's full name
        // doesn't include a confusing label
        TraceFileSystemElement dummyParent = new TraceFileSystemElement("", null, true);//$NON-NLS-1$
        dummyParent.setFileSystemObject(((File)fileSystemObject).getParentFile());
        dummyParent.setPopulated();
        TraceFileSystemElement result = new TraceFileSystemElement(
                elementLabel, dummyParent, isContainer);
        result.setFileSystemObject(fileSystemObject);

        //Get the files for the element so as to build the first level
        result.getFiles();

        return dummyParent;
    }

    // ------------------------------------------------------------------------
    // Trace Type Group
    // ------------------------------------------------------------------------
    private final void createTraceTypeGroup(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = false;
        composite.setLayout(layout);
        composite.setFont(parent.getFont());
        GridData buttonData = new GridData(SWT.FILL, SWT.FILL, true, false);
        composite.setLayoutData(buttonData);

        // Trace type label ("Trace Type:")
        Label typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText(Messages.ImportTraceWizard_TraceType);
        typeLabel.setFont(parent.getFont());

        // Trace type combo
        fTraceTypes = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
        fTraceTypes.setLayoutData(data);
        fTraceTypes.setFont(parent.getFont());

        String[] availableTraceTypes = TmfTraceType.getInstance().getAvailableTraceTypes();
        String[] traceTypeList = new String[availableTraceTypes.length + 1];
        traceTypeList[0] = AUTO_DETECT;
        for (int i = 0; i < availableTraceTypes.length; i++) {
            traceTypeList[i + 1] = availableTraceTypes[i];
        }
        fTraceTypes.setItems(traceTypeList);
        fTraceTypes.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateWidgetEnablements();
                boolean enabled = fTraceTypes.getText().equals(AUTO_DETECT);
                fImportUnrecognizedButton.setEnabled(enabled);
            }
        });
        fTraceTypes.select(0);

        // Unrecognized checkbox
        fImportUnrecognizedButton = new Button(composite, SWT.CHECK);
        fImportUnrecognizedButton.setSelection(true);
        fImportUnrecognizedButton.setText(Messages.ImportTraceWizard_ImportUnrecognized);

        IDialogSettings settings = getDialogSettings();
        boolean value;
        if (settings.get(IMPORT_WIZARD_IMPORT_UNRECOGNIZED_ID) == null) {
            value = true;
        } else {
            value = settings.getBoolean(IMPORT_WIZARD_IMPORT_UNRECOGNIZED_ID);
        }
        fImportUnrecognizedButton.setSelection(value);
    }

    // ------------------------------------------------------------------------
    // Options
    // ------------------------------------------------------------------------

    @Override
    protected void createOptionsGroupButtons(Group optionsGroup) {

        // Overwrite checkbox
        fOverwriteExistingResourcesCheckbox = new Button(optionsGroup, SWT.CHECK);
        fOverwriteExistingResourcesCheckbox.setFont(optionsGroup.getFont());
        fOverwriteExistingResourcesCheckbox.setText(Messages.ImportTraceWizard_OverwriteExistingTrace);
        fOverwriteExistingResourcesCheckbox.setSelection(false);

        // Create links checkbox
        fCreateLinksInWorkspaceButton = new Button(optionsGroup, SWT.CHECK);
        fCreateLinksInWorkspaceButton.setFont(optionsGroup.getFont());
        fCreateLinksInWorkspaceButton.setText(Messages.ImportTraceWizard_CreateLinksInWorkspace);
        fCreateLinksInWorkspaceButton.setSelection(true);

        fCreateLinksInWorkspaceButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateWidgetEnablements();
            }
        });

        updateWidgetEnablements();
    }

    // ------------------------------------------------------------------------
    // Determine if the finish button can be enabled
    // ------------------------------------------------------------------------
    @Override
    public boolean validateSourceGroup() {

        File sourceDirectory = getSourceDirectory();
        if (sourceDirectory == null) {
            setMessage(Messages.ImportTraceWizard_SelectTraceSourceEmpty);
            return false;
        }

        if (sourceConflictsWithDestination(new Path(sourceDirectory.getPath()))) {
            setMessage(null);
            setErrorMessage(getSourceConflictMessage());
            return false;
        }

        if (selectionGroup.getCheckedElementCount() == 0) {
            setMessage(null);
            setErrorMessage(Messages.ImportTraceWizard_SelectTraceNoneSelected);
            return false;
        }

        IContainer container = getSpecifiedContainer();
        if (container != null && container.isVirtual()) {
            if (Platform.getPreferencesService().getBoolean(Activator.PLUGIN_ID, ResourcesPlugin.PREF_DISABLE_LINKING, false, null)) {
                setMessage(null);
                setErrorMessage(Messages.ImportTraceWizard_CannotImportFilesUnderAVirtualFolder);
                return false;
            }
            if (fCreateLinksInWorkspaceButton == null || !fCreateLinksInWorkspaceButton.getSelection()) {
                setMessage(null);
                setErrorMessage(Messages.ImportTraceWizard_HaveToCreateLinksUnderAVirtualFolder);
                return false;
            }
        }

        setErrorMessage(null);
        return true;
    }

    // ------------------------------------------------------------------------
    // Import the trace(s)
    // ------------------------------------------------------------------------

    /**
     * Finish the import.
     *
     * @return <code>true</code> if successful else <code>false</code>
     */
    public boolean finish() {
        IDialogSettings settings = getDialogSettings();
        settings.put(IMPORT_WIZARD_IMPORT_UNRECOGNIZED_ID, fImportUnrecognizedButton.getSelection());

        String traceTypeName = fTraceTypes.getText();
        String traceId = null;
        if (!AUTO_DETECT.equals(traceTypeName)) {
            String tokens[] = traceTypeName.split(SEPARATOR, 2);
            if (tokens.length < 2) {
                return false;
            }
            traceId = TmfTraceType.getInstance().getTraceTypeId(tokens[0], tokens[1]);
        }

        // Save directory for next import operation
        fRootDirectory = getSourceDirectoryName();

        final TraceValidateAndImportOperation operation = new TraceValidateAndImportOperation(traceId, getContainerFullPath(),
                fImportUnrecognizedButton.getSelection(), fOverwriteExistingResourcesCheckbox.getSelection(), fCreateLinksInWorkspaceButton.getSelection());

        IStatus status = Status.OK_STATUS;
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    operation.run(monitor);
                    monitor.done();
                }
            });

            status = operation.getStatus();
        } catch (InvocationTargetException e) {
            status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.ImportTraceWizard_ImportProblem, e);
        } catch (InterruptedException e) {
            status = Status.CANCEL_STATUS;
        } finally {
            if (!status.isOK()) {
                if (status.getSeverity() == IStatus.CANCEL) {
                    setMessage(Messages.ImportTraceWizard_ImportOperationCancelled);
                    setErrorMessage(null);
                } else {
                    if (status.getException() != null) {
                        displayErrorDialog(status.getMessage() + ": " + status.getException()); //$NON-NLS-1$
                    }
                    setMessage(null);
                    setErrorMessage(Messages.ImportTraceWizard_ImportProblem);
                }
                return false;
            }
        }
        setErrorMessage(null);
        return true;
    }


    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private class TraceValidateAndImportOperation {
        private IStatus fStatus;
        private String fTraceType;
        private IPath fContainerPath;
        private boolean fImportUnrecognizedTraces;
        private boolean fLink;
        private ImportConfirmation fConfirmationMode = ImportConfirmation.SKIP;

        private TraceValidateAndImportOperation(String traceId, IPath containerPath, boolean doImport, boolean overwrite, boolean link) {
            fTraceType = traceId;
            fContainerPath = containerPath;
            fImportUnrecognizedTraces = doImport;
            if (overwrite) {
                fConfirmationMode = ImportConfirmation.OVERWRITE_ALL;
            }
            fLink = link;
        }

        public void run(IProgressMonitor progressMonitor) {
            String currentPath = null;
            final Map<String, TraceFileSystemElement> folderElements = new HashMap<>();
            try {

                final ArrayList<TraceFileSystemElement> fileSystemElements = new ArrayList<>();
                IElementFilter passThroughFilter = new IElementFilter() {

                    @Override
                    public void filterElements(Collection elements, IProgressMonitor monitor) {
                        fileSystemElements.addAll(elements);
                    }
                    @Override
                    public void filterElements(Object[] elements, IProgressMonitor monitor) {
                        for (int i = 0; i < elements.length; i++) {
                            fileSystemElements.add((TraceFileSystemElement)elements[i]);
                        }
                    }
                };

                // List fileSystemElements will be filled using the passThroughFilter
                SubMonitor subMonitor = SubMonitor.convert(progressMonitor, 1);
                getSelectedResources(passThroughFilter, subMonitor);

                // Check if operation was cancelled.
                ModalContext.checkCanceled(subMonitor);

                Iterator<TraceFileSystemElement> fileSystemElementsIter = fileSystemElements.iterator();
                subMonitor = SubMonitor.convert(progressMonitor, fileSystemElements.size());

                while (fileSystemElementsIter.hasNext()) {
                    ModalContext.checkCanceled(progressMonitor);
                    currentPath = null;
                    TraceFileSystemElement element = fileSystemElementsIter.next();
                    File fileResource = (File) element.getFileSystemObject();
                    String resourcePath = fileResource.getAbsolutePath();
                    currentPath = resourcePath;
                    SubMonitor sub = subMonitor.newChild(1);
                    if (element.isDirectory()) {
                        if (!folderElements.containsKey(resourcePath)) {
                            if (isDirectoryTrace(element)) {
                                folderElements.put(resourcePath, element);
                                validateAndImportTrace(element, sub);
                            }
                        }
                    } else {
                        TraceFileSystemElement parentElement = (TraceFileSystemElement)element.getParent();
                        File parentFile = (File) parentElement.getFileSystemObject();
                        String parentPath = parentFile.getAbsolutePath();
                        currentPath = parentPath;
                        if (!folderElements.containsKey(parentPath)) {
                            if (isDirectoryTrace(parentElement)) {
                                folderElements.put(parentPath, parentElement);
                                validateAndImportTrace(parentElement, sub);
                            } else {
                                if (fileResource.exists()) {
                                    validateAndImportTrace(element, sub);
                                }
                            }
                        }
                    }
                }
                setStatus(Status.OK_STATUS);
            } catch (InterruptedException e) {
                setStatus(Status.CANCEL_STATUS);
            } catch (Exception e) {
                setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.ImportTraceWizard_ImportProblem + ": " + //$NON-NLS-1$
                        (currentPath != null ? currentPath : "") , e)); //$NON-NLS-1$
            }
        }

        private void validateAndImportTrace(TraceFileSystemElement fileSystemElement, IProgressMonitor monitor)
                throws TmfTraceImportException, CoreException, InvocationTargetException, InterruptedException {
            File file = (File) fileSystemElement.getFileSystemObject();
            String path = file.getAbsolutePath();
            TraceTypeHelper traceTypeHelper = null;

            if (fTraceType == null) {
                // Auto Detection
                try {
                    traceTypeHelper = TmfTraceTypeUIUtils.selectTraceType(path, null, null);
                } catch (TmfTraceImportException e) {
                    // the trace did not match any trace type
                }
                if (traceTypeHelper == null) {
                    if (fImportUnrecognizedTraces) {
                        importResource(fileSystemElement, monitor);
                    }
                    return;
                }
            } else {
                boolean isDirectoryTraceType = TmfTraceType.getInstance().isDirectoryTraceType(fTraceType);
                if (fileSystemElement.isDirectory() != isDirectoryTraceType) {
                    return;
                }
                traceTypeHelper = TmfTraceType.getInstance().getTraceType(fTraceType);

                if (traceTypeHelper == null) {
                    // Trace type not found
                    throw new TmfTraceImportException(Messages.ImportTraceWizard_TraceTypeNotFound);
                }

                if (!traceTypeHelper.validate(path)) {
                    // Trace type exist but doesn't validate for given trace.
                    return;
                }
            }

            // Finally import trace
            if (importResource(fileSystemElement, monitor)) {
                IResource resource = fTargetFolder.findMember(fileSystemElement.getLabel());
                TmfTraceTypeUIUtils.setTraceType(resource, traceTypeHelper);
            }

        }

        /**
         * Imports a trace resource to project. In case of name collision the
         * user will be asked to confirm overwriting the existing trace,
         * overwriting or skipping the trace to be imported.
         *
         * @param fileSystemElement
         *            trace file system object to import
         * @param monitor
         *            a progress monitor
         * @return true if trace was imported else false
         *
         * @throws InvocationTargetException
         *             if problems during import operation
         * @throws InterruptedException
         *             if cancelled
         * @throws CoreException
         *             if problems with workspace
         */
        private boolean importResource(TraceFileSystemElement fileSystemElement, IProgressMonitor monitor)
                throws InvocationTargetException, InterruptedException, CoreException {
            ImportConfirmation mode = checkForNameClashes(fileSystemElement);
            switch (mode) {
            case RENAME:
            case RENAME_ALL:
                rename(fileSystemElement);
                break;
            case OVERWRITE:
            case OVERWRITE_ALL:
                delete(fileSystemElement, monitor);
                break;
            case CONTINUE:
                break;
            case SKIP:
            case SKIP_ALL:
            default:
                return false;
            }

            List<TraceFileSystemElement> subList = new ArrayList<>();

            IPath containerPath = fContainerPath;
            FileSystemElement parentFolder = fileSystemElement.getParent();

            if (fileSystemElement.isDirectory() && (!fLink)) {
                containerPath = containerPath.addTrailingSeparator().append(fileSystemElement.getLabel());

                Object[] array = fileSystemElement.getFiles().getChildren();
                for (int i = 0; i < array.length; i++) {
                    subList.add((TraceFileSystemElement)array[i]);
                }
                parentFolder = fileSystemElement;

            } else {
                subList.add(fileSystemElement);
            }


            ImportProvider fileSystemStructureProvider = new ImportProvider();

            IOverwriteQuery myQueryImpl = new IOverwriteQuery() {
                @Override
                public String queryOverwrite(String file) {
                    return IOverwriteQuery.NO_ALL;
                }
            };

            monitor.setTaskName(Messages.ImportTraceWizard_ImportOperationTaskName + " " + ((File)fileSystemElement.getFileSystemObject()).getAbsolutePath()); //$NON-NLS-1$
            ImportOperation operation = new ImportOperation(containerPath, parentFolder, fileSystemStructureProvider, myQueryImpl, subList);
            operation.setContext(getShell());

            operation.setCreateContainerStructure(false);
            operation.setOverwriteResources(false);
            operation.setCreateLinks(fLink);
            operation.setVirtualFolders(false);

            operation.run(new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
            return true;
        }

        private boolean isDirectoryTrace(FileSystemElement fileSystemElement) {
            File file = (File) fileSystemElement.getFileSystemObject();
            String path = file.getAbsolutePath();
            if (TmfTraceType.getInstance().isDirectoryTrace(path)) {
                return true;
            }
            return false;
        }

        private ImportConfirmation checkForNameClashes(TraceFileSystemElement fileSystemElement) throws InterruptedException {

            String traceName = ((File)fileSystemElement.getFileSystemObject()).getName();

            // handle rename
            if (getExistingTrace(traceName) != null) {
                if ((fConfirmationMode == ImportConfirmation.RENAME_ALL) ||
                    (fConfirmationMode == ImportConfirmation.OVERWRITE_ALL) ||
                    (fConfirmationMode == ImportConfirmation.SKIP_ALL)) {
                    return fConfirmationMode;
                }

                int returnCode = promptForOverwrite(traceName);
                if (returnCode < 0) {
                    // Cancel
                    throw new InterruptedException();
                }
                fConfirmationMode = ImportConfirmation.values()[returnCode];
                return fConfirmationMode;
            }
            return ImportConfirmation.CONTINUE;
        }

        private int promptForOverwrite(String traceName) {
            final MessageDialog dialog = new MessageDialog(getContainer()
                    .getShell(), null, null, NLS.bind(Messages.ImportTraceWizard_TraceAlreadyExists, traceName),
                    MessageDialog.QUESTION, new String[] {
                        ImportConfirmation.RENAME.getInName(),
                        ImportConfirmation.RENAME_ALL.getInName(),
                        ImportConfirmation.OVERWRITE.getInName(),
                        ImportConfirmation.OVERWRITE_ALL.getInName(),
                        ImportConfirmation.SKIP.getInName(),
                        ImportConfirmation.SKIP_ALL.getInName(),
                    }, 4) {
                @Override
                protected int getShellStyle() {
                    return super.getShellStyle() | SWT.SHEET;
                }
            };

            final int[] returnValue = new int[1];
            getShell().getDisplay().syncExec(new Runnable() {

                @Override
                public void run() {
                    returnValue[0] = dialog.open();
                }
            });
            return returnValue[0];
        }

        private void rename(TraceFileSystemElement fileSystemElement) {
            String traceName = ((File)fileSystemElement.getFileSystemObject()).getName();
            TmfTraceElement trace = getExistingTrace(traceName);
            if (trace == null) {
                return;
            }

            IFolder folder = trace.getProject().getTracesFolder().getResource();
            int i = 2;
            while (true) {
                String name = trace.getName() + '(' + Integer.toString(i++) + ')';
                IResource resource = folder.findMember(name);
                if (resource == null) {
                    fileSystemElement.setLabel(name);
                    return;
                }
            }
        }

        private void delete(TraceFileSystemElement fileSystemElement, IProgressMonitor monitor) throws CoreException {
            String traceName = ((File)fileSystemElement.getFileSystemObject()).getName();
            TmfTraceElement trace = getExistingTrace(traceName);
            if (trace == null) {
                return;
            }

            trace.delete(monitor);
        }

        private TmfTraceElement getExistingTrace(String traceName) {
            List<TmfTraceElement> traces = fTraceFolderElement.getTraces();
            for (TmfTraceElement t : traces) {
                if (t.getName().equals(traceName)) {
                    return t;
                }
            }
            return null;
        }

        /**
         * Set the status for this operation
         *
         * @param status
         *            the status
         */
        protected void setStatus(IStatus status) {
            fStatus = status;
        }

        public IStatus getStatus() {
            return fStatus;
        }
    }

    /**
     * The <code>TraceFileSystemElement</code> is a <code>FileSystemElement</code> that knows
     * if it has been populated or not.
     */
    private static class TraceFileSystemElement extends FileSystemElement {

        private boolean fIsPopulated = false;
        private String fLabel = null;

        public TraceFileSystemElement(String name, FileSystemElement parent, boolean isDirectory) {
            super(name, parent, isDirectory);
        }

        public void setPopulated() {
            fIsPopulated = true;
        }

        public boolean isPopulated() {
            return fIsPopulated;
        }

        @Override
        public AdaptableList getFiles() {
            if(!fIsPopulated) {
                populateElementChildren();
            }
            return super.getFiles();
        }

        @Override
        public AdaptableList getFolders() {
            if(!fIsPopulated) {
                populateElementChildren();
            }
            return super.getFolders();
        }

        /**
         * Sets the label for the trace to be used when importing at trace.
         * @param name
         *            the label for the trace
         */
        public void setLabel(String name) {
            fLabel = name;
        }

        /**
         * Returns the label for the trace to be used when importing at trace.
         *
         * @return the label of trace resource
         */
        public String getLabel() {
            if (fLabel == null) {
                //Get the name - if it is empty then return the path as it is a file root
                File file = (File) getFileSystemObject();
                String name = file.getName();
                if (name.length() == 0) {
                    return file.getPath();
                }
                return name;
            }
            return fLabel;
        }

        /**
         * Populates the children of the specified parent <code>FileSystemElement</code>
         */
        private void populateElementChildren() {
            FileSystemStructureProvider provider = FileSystemStructureProvider.INSTANCE;
            List<File> allchildren = provider.getChildren(this.getFileSystemObject());
            File child = null;
            TraceFileSystemElement newelement = null;
            Iterator<File> iter = allchildren.iterator();
            while(iter.hasNext()) {
                child = iter.next();
                newelement = new TraceFileSystemElement(provider.getLabel(child), this, provider.isFolder(child));
                newelement.setFileSystemObject(child);
            }
            setPopulated();
        }
    }

    private class ImportProvider implements IImportStructureProvider {

        private FileSystemStructureProvider provider = FileSystemStructureProvider.INSTANCE;

        ImportProvider() {
        }

        @Override
        public String getLabel(Object element) {
            TraceFileSystemElement resource = (TraceFileSystemElement)element;
            return resource.getLabel();
        }

        @Override
        public List getChildren(Object element) {
            TraceFileSystemElement resource = (TraceFileSystemElement)element;
            Object[] array = resource.getFiles().getChildren();
                    List<Object> list = new ArrayList<>();
                    for (int i = 0; i < array.length; i++) {
                        list.add(array[i]);
                    }
            return list;
        }

        @Override
        public InputStream getContents(Object element) {
            TraceFileSystemElement resource = (TraceFileSystemElement)element;
            return provider.getContents(resource.getFileSystemObject());
        }

        @Override
        public String getFullPath(Object element) {
            TraceFileSystemElement resource = (TraceFileSystemElement)element;
            return provider.getFullPath(resource.getFileSystemObject());
        }

        @Override
        public boolean isFolder(Object element) {
            TraceFileSystemElement resource = (TraceFileSystemElement)element;
            return resource.isDirectory();
        }
    }

    private enum ImportConfirmation {
        // ------------------------------------------------------------------------
        // Enum definition
        // ------------------------------------------------------------------------
        RENAME(Messages.ImportTraceWizard_ImportConfigurationRename),
        RENAME_ALL(Messages.ImportTraceWizard_ImportConfigurationRenameAll),
        OVERWRITE(Messages.ImportTraceWizard_ImportConfigurationOverwrite),
        OVERWRITE_ALL(Messages.ImportTraceWizard_ImportConfigurationOverwriteAll),
        SKIP(Messages.ImportTraceWizard_ImportConfigurationSkip),
        SKIP_ALL(Messages.ImportTraceWizard_ImportConfigurationSkipAll),
        CONTINUE("CONTINUE"); //$NON-NLS-1$

        // ------------------------------------------------------------------------
        // Attributes
        // ------------------------------------------------------------------------
        /**
         * Name of enum
         */
        private final String fInName;

        // ------------------------------------------------------------------------
        // Constuctors
        // ------------------------------------------------------------------------

        /**
         * Private constructor
         * @param name the name of state
         */
        private ImportConfirmation(String name) {
            fInName = name;
        }

        // ------------------------------------------------------------------------
        // Accessors
        // ------------------------------------------------------------------------
        /**
         * @return state name
         */
        public String getInName() {
            return fInName;
        }
    }
}
