/*******************************************************************************
 * Copyright (c) 2009, 2016 Ericsson and others.
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
 *   Marc-Andre Laperle - Preserve folder structure on import
 *   Marc-Andre Laperle - Extract archives during import
 *   Marc-Andre Laperle - Add support for Gzip (non-Tar)
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.NewExperimentOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.SelectTracesOperation;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.TmfProjectNature;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTracesFolder;
import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.dialogs.WizardResourceImportPage;
import org.eclipse.ui.ide.dialogs.IElementFilter;
import org.eclipse.ui.ide.dialogs.ResourceTreeAndListGroup;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerComparator;

/**
 * A variant of the standard resource import wizard for importing traces to
 * given tracing project. If no project or tracing project was selected the
 * wizard imports it to the default tracing project which is created if
 * necessary.
 *
 * In our case traces could be files or a directory structure. This wizard
 * supports both cases. It imports traces for a selected trace type or, if no
 * trace type is selected, it tries to detect the trace type automatically.
 * However, the automatic detection is a best-effort and cannot guarantee that
 * the detection is successful. The reason for this is that there might be
 * multiple trace types that can be assigned to a single trace.
 *
 *
 * @author Francois Chouinard
 */
@SuppressWarnings("restriction")
public class ImportTraceWizardPage extends WizardResourceImportPage {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String IMPORT_WIZARD_PAGE_NAME = "ImportTraceWizardPage"; //$NON-NLS-1$
    private static final String IMPORT_WIZARD_ROOT_DIRECTORY_ID = ".import_root_directory_id"; //$NON-NLS-1$ ;
    private static final String IMPORT_WIZARD_ARCHIVE_FILE_NAME_ID = ".import_archive_file_name_id"; //$NON-NLS-1$
    private static final String IMPORT_WIZARD_IMPORT_UNRECOGNIZED_ID = ".import_unrecognized_traces_id"; //$NON-NLS-1$
    private static final String IMPORT_WIZARD_PRESERVE_FOLDERS_ID = ".import_preserve_folders_id"; //$NON-NLS-1$
    private static final String IMPORT_WIZARD_IMPORT_FROM_DIRECTORY_ID = ".import_from_directory"; //$NON-NLS-1$
    private static final String IMPORT_WIZARD_CREATE_EXPERIMENT_ID = ".create_experiment"; //$NON-NLS-1$

    // constant from WizardArchiveFileResourceImportPage1
    private static final String[] FILE_IMPORT_MASK = { "*.jar;*.zip;*.tar;*.tar.gz;*.tgz;*.gz", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * A special trace type value to communicate that automatic trace type
     * detection will occur instead of setting a specific trace type when
     * importing the traces.
     */
    public static final String TRACE_TYPE_AUTO_DETECT = Messages.ImportTraceWizard_AutoDetection;

    /**
     * Preserve the folder structure of the import traces.
     */
    public static final int OPTION_PRESERVE_FOLDER_STRUCTURE = 1 << 1;
    /**
     * Create links to the trace files instead of copies.
     */
    public static final int OPTION_CREATE_LINKS_IN_WORKSPACE = 1 << 2;
    /**
     * Import files that were not recognized as the selected trace type.
     */
    public static final int OPTION_IMPORT_UNRECOGNIZED_TRACES = 1 << 3;
    /**
     * Overwrite existing resources without prompting.
     */
    public static final int OPTION_OVERWRITE_EXISTING_RESOURCES = 1 << 4;
    /**
     * Create an experiment with imported traces.
     */
    public static final int OPTION_CREATE_EXPERIMENT = 1 << 5;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Target import directory ('Traces' folder)
    private IFolder fTargetFolder;
    // Target Trace folder element
    private TmfTraceFolder fTraceFolderElement;
    // The workspace experiment folder
    private TmfExperimentFolder fExperimentFolderElement;
    private  String fPreviousSource;
    // Flag to handle destination folder change event
    private Boolean fIsDestinationChanged = false;
    private final Object fSyncObject = new Object();
    // Combo box containing trace types
    private Combo fTraceTypes;
    // Button to ignore unrecognized traces or not
    private Button fImportUnrecognizedButton;
    // Button to overwrite existing resources or not
    private Button fOverwriteExistingResourcesCheckbox;
    // Button to link or copy traces to workspace
    private Button fCreateLinksInWorkspaceButton;
    // Button to preserve folder structure
    private Button fPreserveFolderStructureButton;
    // Button to create an experiment
    private Button fCreateExperimentCheckbox;
    // Text box for experiment name
    private Text fExperimentNameText;
    private boolean entryChanged = false;
    // The import from directory radio button
    private Button fImportFromDirectoryRadio;
    // The import from archive radio button
    private Button fImportFromArchiveRadio;
    // Flag to remember the "create links" checkbox when it gets disabled by
    // the import from archive radio button
    private Boolean fPreviousCreateLinksValue = true;

    /** The archive name field */
    protected Combo fArchiveNameField;
    /** The archive browse button. */
    protected Button fArchiveBrowseButton;
    /** The directory name field */
    protected Combo directoryNameField;
    /** The directory browse button. */
    protected Button directoryBrowseButton;

    private ResourceTreeAndListGroup fSelectionGroup;

    // Keep trace of the selection root so that we can dispose its related
    // resources
    private TraceFileSystemElement fSelectionGroupRoot;

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
                    traceFolder = project.getFolder(TmfTracesFolder.TRACES_FOLDER_NAME);
                }
            } catch (CoreException e) {
            }
        }

        // If no tracing project was selected or trace folder doesn't exist use
        // default tracing project
        if (traceFolder == null) {
            IProject project = TmfProjectRegistry.createProject(
                    TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME, null, new NullProgressMonitor());
            TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
            fTraceFolderElement = projectElement.getTracesFolder();
            traceFolder = project.getFolder(TmfTracesFolder.TRACES_FOLDER_NAME);
        }

        // Set the target trace folder
        if (traceFolder != null) {
            fTargetFolder = traceFolder;
            String path = traceFolder.getFullPath().toString();
            setContainerFieldValue(path);
        }

        TmfProjectElement project = fTraceFolderElement.getProject();
        fExperimentFolderElement = project.getExperimentsFolder();
    }

    /**
     * Constructor
     *
     * @param selection
     *            The current selection
     */
    public ImportTraceWizardPage(IStructuredSelection selection) {
        this(IMPORT_WIZARD_PAGE_NAME, selection);
    }

    /**
     * Create the import source selection widget. (Copied from
     * WizardResourceImportPage but instead always uses the internal
     * ResourceTreeAndListGroup to keep compatibility with Kepler)
     */
    @Override
    protected void createFileSelectionGroup(Composite parent) {

        // Just create with a dummy root.
        fSelectionGroup = new ResourceTreeAndListGroup(parent,
                new FileSystemElement("Dummy", null, true), //$NON-NLS-1$
                getFolderProvider(), new WorkbenchLabelProvider(),
                getFileProvider(), new WorkbenchLabelProvider(), SWT.NONE,
                DialogUtil.inRegularFontMode(parent));

        ICheckStateListener listener = new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                updateWidgetEnablements();
            }
        };

        WorkbenchViewerComparator comparator = new WorkbenchViewerComparator();
        fSelectionGroup.setTreeComparator(comparator);
        fSelectionGroup.setListComparator(comparator);
        fSelectionGroup.addCheckStateListener(listener);

    }

    // ------------------------------------------------------------------------
    // WizardResourceImportPage
    // ------------------------------------------------------------------------

    @Override
    protected void createSourceGroup(Composite parent) {
        createSourceSelectionGroup(parent);
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
                    // If we have not populated then wait until asked
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
     * creates the source selection group.
     *
     * @param parent
     *            the parent composite
     */
    protected void createSourceSelectionGroup(Composite parent) {

        Composite sourceGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        sourceGroup.setLayout(layout);
        sourceGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // import from directory radio button
        fImportFromDirectoryRadio = new Button(sourceGroup, SWT.RADIO);
        fImportFromDirectoryRadio.setText(Messages.ImportTraceWizard_DirectoryLocation);

        // import location entry combo
        directoryNameField = createPathSelectionCombo(sourceGroup);
        createDirectoryBrowseButton(sourceGroup);

        // import from archive radio button
        fImportFromArchiveRadio = new Button(sourceGroup, SWT.RADIO);
        fImportFromArchiveRadio.setText(Messages.ImportTraceWizard_ArchiveLocation);

        // import location entry combo
        fArchiveNameField = createPathSelectionCombo(sourceGroup);
        createArchiveBrowseButton(sourceGroup);

        fImportFromDirectoryRadio.setSelection(true);
        fArchiveNameField.setEnabled(false);
        fArchiveBrowseButton.setEnabled(false);

        fImportFromDirectoryRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                directoryRadioSelected();
            }
        });

        fImportFromArchiveRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                archiveRadioSelected();
            }
        });
    }

    /**
     * Select or deselect all files in the file selection group
     *
     * @param checked
     *            whether or not the files should be checked
     */
    protected void setFileSelectionGroupChecked(boolean checked) {
        if (fSelectionGroup != null) {
            fSelectionGroup.setAllSelections(checked);
        }
    }

    /**
     * Create a combo that will be used to select a path to specify the source
     * of the import. The parent is assumed to have a GridLayout.
     *
     * @param parent
     *            the parent composite
     * @return the created path selection combo
     */
    protected Combo createPathSelectionCombo(Composite parent) {
        Combo pathSelectionCombo = new Combo(parent, SWT.BORDER);

        GridData layoutData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        layoutData.widthHint = new PixelConverter(pathSelectionCombo).convertWidthInCharsToPixels(25);
        pathSelectionCombo.setLayoutData(layoutData);

        TraverseListener traverseListener = new TraverseListener() {
            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                    entryChanged = false;
                    updateFromSourceField();
                }
            }
        };

        FocusAdapter focusAdapter = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Clear the flag to prevent constant update
                if (entryChanged) {
                    entryChanged = false;
                    updateFromSourceField();
                }
            }
        };

        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                entryChanged = false;
                updateFromSourceField();
            }
        };

        ModifyListener modifyListner = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                entryChanged = true;
            }
        };

        pathSelectionCombo.addModifyListener(modifyListner);
        pathSelectionCombo.addTraverseListener(traverseListener);
        pathSelectionCombo.addFocusListener(focusAdapter);
        pathSelectionCombo.addSelectionListener(selectionAdapter);

        return pathSelectionCombo;
    }

    /**
     * Create the directory browse button.
     *
     * @param parent
     *            the parent composite
     */
    protected void createDirectoryBrowseButton(Composite parent) {
        directoryBrowseButton = createPathSelectionBrowseButton(parent);
        directoryBrowseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleSourceDirectoryBrowseButtonPressed();
            }
        });
    }

    /**
     * Create the archive browse button.
     *
     * @param parent
     *            the parent composite
     */
    protected void createArchiveBrowseButton(Composite parent) {
        fArchiveBrowseButton = createPathSelectionBrowseButton(parent);
        fArchiveBrowseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleArchiveBrowseButtonPressed(FILE_IMPORT_MASK);
            }
        });
    }

    /**
     * Create a browse button that will be used to browse for a path to specify
     * the source of the import. The parent is assumed to have a GridLayout.
     *
     * @param parent
     *            the parent composite
     * @return the created path selection combo
     */
    protected Button createPathSelectionBrowseButton(Composite parent) {
        Button pathSelectionBrowseButton = new Button(parent, SWT.PUSH);
        pathSelectionBrowseButton.setText(Messages.ImportTraceWizard_BrowseButton);
        setButtonLayoutData(pathSelectionBrowseButton);

        return pathSelectionBrowseButton;
    }

    private void archiveRadioSelected() {
        if (!isImportFromDirectory()) {
            directoryNameField.setEnabled(false);
            directoryBrowseButton.setEnabled(false);
            fArchiveNameField.setEnabled(true);
            fArchiveBrowseButton.setEnabled(true);
            updateFromSourceField();
            fArchiveNameField.setFocus();
            if (fCreateLinksInWorkspaceButton != null) {
                fPreviousCreateLinksValue = fCreateLinksInWorkspaceButton.getSelection();
                fCreateLinksInWorkspaceButton.setSelection(false);
                fCreateLinksInWorkspaceButton.setEnabled(false);
            }
        }
    }

    private void directoryRadioSelected() {
        if (isImportFromDirectory()) {
            directoryNameField.setEnabled(true);
            directoryBrowseButton.setEnabled(true);
            fArchiveNameField.setEnabled(false);
            fArchiveBrowseButton.setEnabled(false);
            updateFromSourceField();
            directoryNameField.setFocus();
            if (fCreateLinksInWorkspaceButton != null) {
                fCreateLinksInWorkspaceButton.setSelection(fPreviousCreateLinksValue);
                fCreateLinksInWorkspaceButton.setEnabled(true);
            }
        }
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
        synchronized (fSyncObject) {
            if (!fIsDestinationChanged) {
                event.display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (fSyncObject) {
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
            setSourcePath(selectedDirectory);
        }
    }

    /**
     * Handle the button pressed event
     *
     * @param extensions
     *            file extensions used to filter files shown to the user
     */
    protected void handleArchiveBrowseButtonPressed(String[] extensions) {
        FileDialog dialog = TmfFileDialogFactory.create(fArchiveNameField.getShell(), SWT.SHEET);
        dialog.setFilterExtensions(extensions);
        dialog.setText(Messages.ImportTraceWizard_SelectTraceArchiveTitle);
        String fileName = fArchiveNameField.getText().trim();
        if (!fileName.isEmpty()) {
            File path = new File(fileName).getParentFile();
            if (path != null && path.exists()) {
                dialog.setFilterPath(path.toString());
            }
        }

        String selectedArchive = dialog.open();
        if (selectedArchive != null) {
            setErrorMessage(null);
            setSourcePath(selectedArchive);
            updateWidgetEnablements();
        }
    }

    private File getSourceDirectory() {
        if (directoryNameField == null) {
            return null;
        }
        return getSourceDirectory(directoryNameField.getText());
    }

    private File getSourceArchiveFile() {
        if (fArchiveNameField == null) {
            return null;
        }

        return getSourceArchiveFile(fArchiveNameField.getText());
    }

    private String getSourceContainerPath() {
        if (isImportFromDirectory()) {
            File sourceDirectory = getSourceDirectory();
            if (sourceDirectory != null) {
                return sourceDirectory.getAbsolutePath();
            }
        }
        File sourceArchiveFile = getSourceArchiveFile();
        if (sourceArchiveFile != null) {
            return sourceArchiveFile.getParent();
        }
        return null;
    }

    private static File getSourceDirectory(String path) {
        File sourceDirectory = new File(getSourceDirectoryName(path));
        if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            return null;
        }

        return sourceDirectory;
    }

    private static File getSourceArchiveFile(String path) {
        File sourceArchiveFile = new File(path);
        if (!sourceArchiveFile.exists() || sourceArchiveFile.isDirectory()) {
            return null;
        }

        return sourceArchiveFile;
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

    private void updateFromSourceField() {
        setSourcePath(getSourceField().getText());
        updateWidgetEnablements();
    }

    private Combo getSourceField() {
        if (directoryNameField == null) {
            return fArchiveNameField;
        }

        return directoryNameField.isEnabled() ? directoryNameField : fArchiveNameField;
    }

    /**
     * Set the source path that was selected by the user by various input
     * methods (Browse button, typing, etc).
     *
     * Clients can also call this to set the path programmatically (hard-coded
     * initial path) and this can also be overridden to be notified when the
     * source path changes.
     *
     * @param path
     *            the source path
     */
    protected void setSourcePath(String path) {
        Combo sourceField = getSourceField();
        if (sourceField == null) {
            return;
        }

        if (path.length() > 0) {
            String[] currentItems = sourceField.getItems();
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
                sourceField.setItems(newItems);
                selectionIndex = oldLength;
            }
            sourceField.select(selectionIndex);
        }
        resetSelection();
    }

    // ------------------------------------------------------------------------
    // File Selection Group (forked WizardFileSystemResourceImportPage1)
    // ------------------------------------------------------------------------
    private void resetSelection() {

        if (fSelectionGroupRoot != null) {
            disposeSelectionGroupRoot();
        }
        fSelectionGroupRoot = getFileSystemTree();
        fSelectionGroup.setRoot(fSelectionGroupRoot);

        if (fCreateExperimentCheckbox != null) {
            File file = getSourceFile();
            if (file != null) {
                String previousName = fExperimentNameText.getText().trim();
                if (((fPreviousSource != null) && (previousName.equals(fPreviousSource))) || previousName.isEmpty()) {
                    fExperimentNameText.setText(file.getName());
                }
                fPreviousSource = file.getName();
            }
        }
    }

    private void disposeSelectionGroupRoot() {
        if (fSelectionGroupRoot != null && fSelectionGroupRoot.getProvider() != null) {
            FileSystemObjectImportStructureProvider provider = fSelectionGroupRoot.getProvider();
            provider.dispose();
            fSelectionGroupRoot = null;
        }
    }

    private TraceFileSystemElement getFileSystemTree() {
        Pair<IFileSystemObject, FileSystemObjectImportStructureProvider> rootObjectAndProvider = ArchiveUtil.getRootObjectAndProvider(getSourceFile(), getContainer().getShell());
        if (rootObjectAndProvider == null) {
            return null;
        }
        return selectFiles(rootObjectAndProvider.getFirst(), rootObjectAndProvider.getSecond());
    }

    private TraceFileSystemElement selectFiles(final IFileSystemObject rootFileSystemObject,
            final FileSystemObjectImportStructureProvider structureProvider) {
        final TraceFileSystemElement[] results = new TraceFileSystemElement[1];
        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
            @Override
            public void run() {
                // Create the root element from the supplied file system object
                results[0] = TraceFileSystemElement.createRootTraceFileElement(rootFileSystemObject, structureProvider);
            }
        });
        return results[0];
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

        String[] availableTraceTypes = TmfTraceType.getAvailableTraceTypes();
        String[] traceTypeList = new String[availableTraceTypes.length + 1];
        traceTypeList[0] = TRACE_TYPE_AUTO_DETECT;
        System.arraycopy(availableTraceTypes, 0, traceTypeList, 1, availableTraceTypes.length);
        fTraceTypes.setItems(traceTypeList);
        fTraceTypes.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateWidgetEnablements();
                boolean enabled = fTraceTypes.getText().equals(TRACE_TYPE_AUTO_DETECT);
                fImportUnrecognizedButton.setEnabled(enabled);
            }
        });
        fTraceTypes.select(0);

        // Unrecognized checkbox
        fImportUnrecognizedButton = new Button(composite, SWT.CHECK);
        fImportUnrecognizedButton.setSelection(true);
        fImportUnrecognizedButton.setText(Messages.ImportTraceWizard_ImportUnrecognized);
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

        fPreserveFolderStructureButton = new Button(optionsGroup, SWT.CHECK);
        fPreserveFolderStructureButton.setFont(optionsGroup.getFont());
        fPreserveFolderStructureButton.setText(Messages.ImportTraceWizard_PreserveFolderStructure);
        fPreserveFolderStructureButton.setSelection(true);

        Composite comp = new Composite(optionsGroup, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginWidth = 0;
        comp.setLayout(layout);
        GridData data = new GridData(GridData.FILL, GridData.CENTER, true, false);
        comp.setLayoutData(data);

        fCreateExperimentCheckbox = new Button(comp, SWT.CHECK);
        fCreateExperimentCheckbox.setFont(comp.getFont());
        fCreateExperimentCheckbox.setText(Messages.ImportTraceWizard_CreateExperiment);
        fCreateExperimentCheckbox.setSelection(false);
        data = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
        fCreateExperimentCheckbox.setLayoutData(data);

        fExperimentNameText = new Text(comp, SWT.BORDER);
        data = new GridData(GridData.FILL, GridData.CENTER, true, false);
        fExperimentNameText.setLayoutData(data);

        fExperimentNameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateWidgetEnablements();
            }
        });

        fCreateExperimentCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fExperimentNameText.setEnabled(fCreateExperimentCheckbox.getSelection());
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

        File source = getSourceFile();
        if (source == null) {
            setMessage(Messages.ImportTraceWizard_SelectTraceSourceEmpty);
            setErrorMessage(null);
            return false;
        }

        if (sourceConflictsWithDestination(new Path(source.getPath()))) {
            setMessage(null);
            setErrorMessage(getSourceConflictMessage());
            return false;
        }

        if (!isImportFromDirectory() && !ArchiveUtil.ensureTarSourceIsValid(source.getAbsolutePath()) && !ArchiveUtil.ensureZipSourceIsValid(source.getAbsolutePath())
                && !ArchiveUtil.ensureGzipSourceIsValid(source.getAbsolutePath())) {
            setMessage(null);
            setErrorMessage(Messages.ImportTraceWizard_BadArchiveFormat);
            return false;
        }

        if (fSelectionGroup.getCheckedElementCount() == 0) {
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

    @Override
    protected boolean validateOptionsGroup() {
        if (fCreateExperimentCheckbox != null && fCreateExperimentCheckbox.getSelection()) {
            String name = fExperimentNameText.getText().trim();
            // verify if experiment name is empty
            if (name.isEmpty()) {
                setMessage(null);
                setErrorMessage(Messages.ImportTraceWizard_ErrorEmptyExperimentName);
                return false;
            }
            // verify that name is a valid resource name
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            if ((workspace != null) && (!workspace.validateName(name, IResource.FILE).isOK())) {
                setMessage(null);
                setErrorMessage(NLS.bind(Messages.ImportTraceWizard_ErrorExperimentNameInvalid, name));
                return false;
            }
            // verify if experiment already exists
            if (fExperimentFolderElement != null) {
                TmfExperimentElement element = fExperimentFolderElement.getExperiment(name);
                if (element != null) {
                    setMessage(null);
                    setErrorMessage(NLS.bind(Messages.ImportTraceWizard_ErrorExperimentAlreadyExists, name));
                    return false;
                }
                IFolder expResource = fExperimentFolderElement.getResource();
                IResource res = expResource.findMember(name);
                if (res != null) {
                    setMessage(null);
                    setErrorMessage(NLS.bind(Messages.ImportTraceWizard_ErrorResourceAlreadyExists, name));
                    return false;
                }
            }
        }
        setErrorMessage(null);
        return true;
    }

    private File getSourceFile() {
        return isImportFromDirectory() ? getSourceDirectory() : getSourceArchiveFile();
    }

    private boolean isImportFromDirectory() {
        return fImportFromDirectoryRadio != null && fImportFromDirectoryRadio.getSelection();
    }

    @Override
    protected void restoreWidgetValues() {
        super.restoreWidgetValues();

        IDialogSettings settings = getDialogSettings();
        boolean value;
        if (fImportUnrecognizedButton != null) {
            if (settings.get(getPageStoreKey(IMPORT_WIZARD_IMPORT_UNRECOGNIZED_ID)) == null) {
                value = true;
            } else {
                value = settings.getBoolean(getPageStoreKey(IMPORT_WIZARD_IMPORT_UNRECOGNIZED_ID));
            }
            fImportUnrecognizedButton.setSelection(value);
        }

        if (fPreserveFolderStructureButton != null) {
            if (settings.get(getPageStoreKey(IMPORT_WIZARD_PRESERVE_FOLDERS_ID)) == null) {
                value = true;
            } else {
                value = settings.getBoolean(getPageStoreKey(IMPORT_WIZARD_PRESERVE_FOLDERS_ID));
            }
            fPreserveFolderStructureButton.setSelection(value);
        }

        if (fCreateExperimentCheckbox != null) {
            if (settings.get(getPageStoreKey(IMPORT_WIZARD_CREATE_EXPERIMENT_ID)) == null) {
                value = false;
            } else {
                value = settings.getBoolean(getPageStoreKey(IMPORT_WIZARD_CREATE_EXPERIMENT_ID));
            }
            fCreateExperimentCheckbox.setSelection(value);
            fExperimentNameText.setEnabled(fCreateExperimentCheckbox.getSelection());
        }

        if (settings.get(getPageStoreKey(IMPORT_WIZARD_IMPORT_FROM_DIRECTORY_ID)) == null) {
            value = true;
        } else {
            value = settings.getBoolean(getPageStoreKey(IMPORT_WIZARD_IMPORT_FROM_DIRECTORY_ID));
        }

        if (directoryNameField != null) {
            restoreComboValues(directoryNameField, settings, getPageStoreKey(IMPORT_WIZARD_ROOT_DIRECTORY_ID));
        }
        if (fArchiveNameField != null) {
            restoreComboValues(fArchiveNameField, settings, getPageStoreKey(IMPORT_WIZARD_ARCHIVE_FILE_NAME_ID));
        }

        if (fImportFromDirectoryRadio != null) {
            fImportFromDirectoryRadio.setSelection(value);
            if (value) {
                directoryRadioSelected();
            }
        }
        if (fImportFromArchiveRadio != null) {
            fImportFromArchiveRadio.setSelection(!value);
            if (!value) {
                archiveRadioSelected();
            }
        }
    }

    @Override
    protected void saveWidgetValues() {
        // Persist dialog settings
        IDialogSettings settings = getDialogSettings();
        if (fImportUnrecognizedButton != null) {
            settings.put(getPageStoreKey(IMPORT_WIZARD_IMPORT_UNRECOGNIZED_ID), fImportUnrecognizedButton.getSelection());
        }
        if (fPreserveFolderStructureButton != null) {
            settings.put(getPageStoreKey(IMPORT_WIZARD_PRESERVE_FOLDERS_ID), fPreserveFolderStructureButton.getSelection());
        }

        if (fCreateExperimentCheckbox != null) {
            settings.put(getPageStoreKey(IMPORT_WIZARD_CREATE_EXPERIMENT_ID), fCreateExperimentCheckbox.getSelection());
        }

        settings.put(getPageStoreKey(IMPORT_WIZARD_IMPORT_FROM_DIRECTORY_ID), isImportFromDirectory());

        if (directoryNameField != null) {
            saveComboValues(directoryNameField, settings, getPageStoreKey(IMPORT_WIZARD_ROOT_DIRECTORY_ID));
        }
        if (fArchiveNameField != null) {
            saveComboValues(fArchiveNameField, settings, getPageStoreKey(IMPORT_WIZARD_ARCHIVE_FILE_NAME_ID));
        }
    }

    private String getPageStoreKey(String key) {
        return getName() + key;
    }

    private static void restoreComboValues(Combo combo, IDialogSettings settings, String key) {
        String[] directoryNames = settings.getArray(key);
        if ((directoryNames != null) && (directoryNames.length != 0)) {
            for (int i = 0; i < directoryNames.length; i++) {
                combo.add(directoryNames[i]);
            }
        }
    }

    private void saveComboValues(Combo combo, IDialogSettings settings, String key) {
        // update names history
        String[] directoryNames = settings.getArray(key);
        if (directoryNames == null) {
            directoryNames = new String[0];
        }

        String items[] = combo.getItems();
        for (int i = 0; i < items.length; i++) {
            directoryNames = addToHistory(directoryNames, items[i]);
        }
        settings.put(key, directoryNames);
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
        String traceTypeLabel = getImportTraceTypeId();
        final String traceId = !TRACE_TYPE_AUTO_DETECT.equals(traceTypeLabel) ? TmfTraceType.getTraceTypeId(traceTypeLabel) : null;

        // Save dialog settings
        saveWidgetValues();

        final IPath baseSourceContainerPath = new Path(getSourceContainerPath());
        final boolean importFromArchive = getSourceArchiveFile() != null;
        final int importOptionFlags = getImportOptionFlags();
        final IPath destinationContainerPath = getContainerFullPath();

        final IStatus[] operationStatus = new IStatus[1];
        operationStatus[0] = Status.OK_STATUS;
        final List<IResource> traceResources = new ArrayList<>();
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

                    final List<TraceFileSystemElement> selectedFileSystemElements = new LinkedList<>();
                    IElementFilter passThroughFilter = new IElementFilter() {

                        @Override
                        public void filterElements(Collection elements, IProgressMonitor m) {
                            selectedFileSystemElements.addAll(elements);
                        }

                        @Override
                        public void filterElements(Object[] elements, IProgressMonitor m) {
                            for (int i = 0; i < elements.length; i++) {
                                selectedFileSystemElements.add((TraceFileSystemElement) elements[i]);
                            }
                        }
                    };

                    // List fileSystemElements will be filled using the
                    // passThroughFilter
                    SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
                    fSelectionGroup.getAllCheckedListItems(passThroughFilter, subMonitor);

                    final TraceValidateAndImportOperation operation = new TraceValidateAndImportOperation(getContainer().getShell(), selectedFileSystemElements, traceId, baseSourceContainerPath, destinationContainerPath, importFromArchive,
                            importOptionFlags, fTraceFolderElement);
                    operation.run(monitor);
                    monitor.done();
                    operationStatus[0] = operation.getStatus();
                    traceResources.addAll(operation.getImportedResources());
                }
            });

            // Only create experiment when option is selected and
            // if there has been at least one trace imported
            if (((importOptionFlags & OPTION_CREATE_EXPERIMENT) != 0) && (traceResources.size() > 0)) {
                final IFolder[] experimentFolders = new IFolder[1];
                final TmfExperimentFolder root = fExperimentFolderElement;
                final String experimentName = fExperimentNameText.getText().trim();
                // just safety guards
                if ((root == null) || (experimentName == null)) {
                    return true;
                }
                if ((operationStatus[0] != null) && (operationStatus[0].isOK())) {
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
                    final TmfTraceFolder parentTraceFolder = fTraceFolderElement;
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
            }
        } catch (InvocationTargetException e) {
            operationStatus[0] = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.ImportTraceWizard_ImportProblem, e.getTargetException());
        } catch (InterruptedException e) {
            operationStatus[0] = Status.CANCEL_STATUS;
        }
        if (!operationStatus[0].isOK()) {
            if (operationStatus[0].getSeverity() == IStatus.CANCEL) {
                setMessage(Messages.ImportTraceWizard_ImportOperationCancelled);
                setErrorMessage(null);
            } else {
                Throwable exception = operationStatus[0].getException();
                if (exception != null) {
                    Activator.getDefault().logError(exception.getMessage(), exception);
                    displayErrorDialog(operationStatus[0].getMessage() + ": " + exception); //$NON-NLS-1$
                }
                setMessage(null);
                setErrorMessage(Messages.ImportTraceWizard_ImportProblem);
            }
            return false;
        }
        setErrorMessage(null);
        return true;
    }

    /**
     * Get the trace type id to import as. This can also return
     * {@link #TRACE_TYPE_AUTO_DETECT} to communicate that automatic trace type
     * detection will occur instead of setting a specific trace type when
     * importing the traces.
     *
     * @return the trace type id or {@link #TRACE_TYPE_AUTO_DETECT}
     */
    protected String getImportTraceTypeId() {
        return fTraceTypes.getText();
    }

    /**
     * Get import options in the form of flags (bits).
     *
     * @return the import flags.
     * @see #OPTION_CREATE_LINKS_IN_WORKSPACE
     * @see #OPTION_IMPORT_UNRECOGNIZED_TRACES
     * @see #OPTION_OVERWRITE_EXISTING_RESOURCES
     * @see #OPTION_PRESERVE_FOLDER_STRUCTURE
     * @see #OPTION_CREATE_EXPERIMENT
     */
    protected int getImportOptionFlags() {
        int flags = 0;
        if (fCreateLinksInWorkspaceButton != null && fCreateLinksInWorkspaceButton.getSelection()) {
            flags |= OPTION_CREATE_LINKS_IN_WORKSPACE;
        }
        if (fImportUnrecognizedButton != null && fImportUnrecognizedButton.getSelection()) {
            flags |= OPTION_IMPORT_UNRECOGNIZED_TRACES;
        }
        if (fOverwriteExistingResourcesCheckbox != null && fOverwriteExistingResourcesCheckbox.getSelection()) {
            flags |= OPTION_OVERWRITE_EXISTING_RESOURCES;
        }
        if (fPreserveFolderStructureButton != null && fPreserveFolderStructureButton.getSelection()) {
            flags |= OPTION_PRESERVE_FOLDER_STRUCTURE;
        }
        if (fCreateExperimentCheckbox != null && fCreateExperimentCheckbox.getSelection()) {
            flags |= OPTION_CREATE_EXPERIMENT;
        }
        return flags;
    }

    @Override
    public void dispose() {
        super.dispose();
        disposeSelectionGroupRoot();
    }
}
