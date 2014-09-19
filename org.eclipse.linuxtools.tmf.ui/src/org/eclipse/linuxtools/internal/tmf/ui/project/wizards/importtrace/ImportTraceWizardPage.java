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
 *   Marc-Andre Laperle - Preserve folder structure on import
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

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
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
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
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTracesFolder;
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
import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.dialogs.WizardResourceImportPage;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.dialogs.IElementFilter;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileManipulations;
import org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.TarEntry;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.internal.wizards.datatransfer.TarFile;
import org.eclipse.ui.internal.wizards.datatransfer.TarLeveledStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerComparator;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

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
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class ImportTraceWizardPage extends WizardResourceImportPage {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String IMPORT_WIZARD_PAGE_NAME = "ImportTraceWizardPage"; //$NON-NLS-1$
    private static final String IMPORT_WIZARD_ROOT_DIRECTORY_ID = ".import_root_directory_id"; //$NON-NLS-1$;
    private static final String IMPORT_WIZARD_ARCHIVE_FILE_NAME_ID = ".import_archive_file_name_id"; //$NON-NLS-1$
    private static final String IMPORT_WIZARD_IMPORT_UNRECOGNIZED_ID = ".import_unrecognized_traces_id"; //$NON-NLS-1$
    private static final String IMPORT_WIZARD_PRESERVE_FOLDERS_ID = ".import_preserve_folders_id"; //$NON-NLS-1$
    private static final String IMPORT_WIZARD_IMPORT_FROM_DIRECTORY_ID = ".import_from_directory"; //$NON-NLS-1$
    private static final String SEPARATOR = ":"; //$NON-NLS-1$

    // constant from WizardArchiveFileResourceImportPage1
    private static final String[] FILE_IMPORT_MASK = { "*.jar;*.zip;*.tar;*.tar.gz;*.tgz", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
    private static final String TRACE_IMPORT_TEMP_FOLDER = ".traceImport"; //$NON-NLS-1$

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

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

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
    // Button to preserve folder structure
    private Button fPreserveFolderStructureButton;
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

    /**
     * ResourceTreeAndListGroup was internal in Kepler and we referenced it. It
     * is now removed in Luna. To keep our builds compatible with Kepler, we
     * need to have our own version of this class. Once we stop supporting
     * Kepler, we can delete this class and use the public one from the
     * platform.
     */
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
                new FileSystemElement("Dummy", null, true),//$NON-NLS-1$
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
        fImportFromDirectoryRadio
                .setText(Messages.ImportTraceWizard_DirectoryLocation);

        // import location entry combo
        directoryNameField = createPathSelectionCombo(sourceGroup);
        createDirectoryBrowseButton(sourceGroup);

        // import from archive radio button
        fImportFromArchiveRadio = new Button(sourceGroup, SWT.RADIO);
        fImportFromArchiveRadio
                .setText(Messages.ImportTraceWizard_ArchiveLocation);

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
        FileDialog dialog = new FileDialog(fArchiveNameField.getShell(), SWT.SHEET);
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
    }

    private void disposeSelectionGroupRoot() {
        if (fSelectionGroupRoot != null && fSelectionGroupRoot.getProvider() != null) {
            FileSystemObjectImportStructureProvider provider = fSelectionGroupRoot.getProvider();
            provider.dispose();
            fSelectionGroupRoot = null;
        }
    }

    private TraceFileSystemElement getFileSystemTree() {
        IFileSystemObject rootElement = null;
        FileSystemObjectImportStructureProvider importStructureProvider = null;

        // Import from directory
        if (isImportFromDirectory()) {
            importStructureProvider = new FileSystemObjectImportStructureProvider(FileSystemStructureProvider.INSTANCE, null);
            File sourceDirectory = getSourceDirectory();
            if (sourceDirectory == null) {
                return null;
            }
            rootElement = importStructureProvider.getIFileSystemObject(sourceDirectory);
        } else {
            // Import from archive
            FileSystemObjectLeveledImportStructureProvider leveledImportStructureProvider = null;
            String archivePath = getSourceArchiveFile() != null ? getSourceArchiveFile().getAbsolutePath() : ""; //$NON-NLS-1$
            if (ArchiveFileManipulations.isTarFile(archivePath)) {
                if (ensureTarSourceIsValid(archivePath)) {
                    // We close the file when we dispose the import provider,
                    // see disposeSelectionGroupRoot
                    TarFile tarFile = getSpecifiedTarSourceFile(archivePath);
                    leveledImportStructureProvider = new FileSystemObjectLeveledImportStructureProvider(new TarLeveledStructureProvider(tarFile), archivePath);
                }
            } else if (ensureZipSourceIsValid(archivePath)) {
                // We close the file when we dispose the import provider, see
                // disposeSelectionGroupRoot
                @SuppressWarnings("resource")
                ZipFile zipFile = getSpecifiedZipSourceFile(archivePath);
                leveledImportStructureProvider = new FileSystemObjectLeveledImportStructureProvider(new ZipLeveledStructureProvider(zipFile), archivePath);
            }
            if (leveledImportStructureProvider == null) {
                return null;
            }
            rootElement = leveledImportStructureProvider.getRoot();
            importStructureProvider = leveledImportStructureProvider;
        }

        if (rootElement == null) {
            return null;
        }

        return selectFiles(rootElement, importStructureProvider);
    }

    /**
     * An import provider that makes use of the IFileSystemObject abstraction
     * instead of using plain file system objects (File, TarEntry, ZipEntry)
     */
    private static class FileSystemObjectImportStructureProvider implements IImportStructureProvider {

        private IImportStructureProvider fImportProvider;
        private String fArchivePath;

        private FileSystemObjectImportStructureProvider(IImportStructureProvider importStructureProvider, String archivePath) {
            fImportProvider = importStructureProvider;
            fArchivePath = archivePath;
        }

        @Override
        public List<IFileSystemObject> getChildren(Object element) {
            @SuppressWarnings("rawtypes")
            List children = fImportProvider.getChildren(((IFileSystemObject) element).getRawFileSystemObject());
            List<IFileSystemObject> adapted = new ArrayList<>(children.size());
            for (Object o : children) {
                adapted.add(getIFileSystemObject(o));
            }
            return adapted;
        }

        public IFileSystemObject getIFileSystemObject(Object o) {
            if (o == null) {
                return null;
            }

            if (o instanceof File) {
                return new FileFileSystemObject((File) o);
            } else if (o instanceof TarEntry) {
                return new TarFileSystemObject((TarEntry) o, fArchivePath);
            } else if (o instanceof ZipEntry) {
                return new ZipFileSystemObject((ZipEntry) o, fArchivePath);
            }

            throw new IllegalArgumentException("Object type not handled"); //$NON-NLS-1$
        }

        @Override
        public InputStream getContents(Object element) {
            return fImportProvider.getContents(((IFileSystemObject) element).getRawFileSystemObject());
        }

        @Override
        public String getFullPath(Object element) {
            return fImportProvider.getFullPath(((IFileSystemObject) element).getRawFileSystemObject());
        }

        @Override
        public String getLabel(Object element) {
            return fImportProvider.getLabel(((IFileSystemObject) element).getRawFileSystemObject());
        }

        @Override
        public boolean isFolder(Object element) {
            return fImportProvider.isFolder(((IFileSystemObject) element).getRawFileSystemObject());
        }

        /**
         * Disposes of the resources associated with the provider.
         */
        public void dispose() {
        }
    }

    /**
     * An import provider that both supports using IFileSystemObject and adds
     * "archive functionality" by delegating to a leveled import provider
     * (TarLeveledStructureProvider, ZipLeveledStructureProvider)
     */
    private static class FileSystemObjectLeveledImportStructureProvider extends FileSystemObjectImportStructureProvider implements ILeveledImportStructureProvider {

        private ILeveledImportStructureProvider fLeveledImportProvider;

        private FileSystemObjectLeveledImportStructureProvider(ILeveledImportStructureProvider importStructureProvider, String archivePath) {
            super(importStructureProvider, archivePath);
            fLeveledImportProvider = importStructureProvider;
        }

        @Override
        public IFileSystemObject getRoot() {
            return getIFileSystemObject(fLeveledImportProvider.getRoot());
        }

        @Override
        public void setStrip(int level) {
            fLeveledImportProvider.setStrip(level);
        }

        @Override
        public int getStrip() {
            return fLeveledImportProvider.getStrip();
        }

        @Override
        public boolean closeArchive() {
            return fLeveledImportProvider.closeArchive();
        }
    }

    @SuppressWarnings("resource")
    private boolean ensureZipSourceIsValid(String archivePath) {
        ZipFile specifiedFile = getSpecifiedZipSourceFile(archivePath);
        if (specifiedFile == null) {
            return false;
        }
        return ArchiveFileManipulations.closeZipFile(specifiedFile, getShell());
    }

    private boolean ensureTarSourceIsValid(String archivePath) {
        TarFile specifiedFile = getSpecifiedTarSourceFile(archivePath);
        if (specifiedFile == null) {
            return false;
        }
        return ArchiveFileManipulations.closeTarFile(specifiedFile, getShell());
    }

    private static ZipFile getSpecifiedZipSourceFile(String fileName) {
        if (fileName.length() == 0) {
            return null;
        }

        try {
            return new ZipFile(fileName);
        } catch (ZipException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        }

        return null;
    }

    private static TarFile getSpecifiedTarSourceFile(String fileName) {
        if (fileName.length() == 0) {
            return null;
        }

        try {
            return new TarFile(fileName);
        } catch (TarException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        }

        return null;
    }

    private TraceFileSystemElement selectFiles(final IFileSystemObject rootFileSystemObject,
            final FileSystemObjectImportStructureProvider structureProvider) {
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

    private static TraceFileSystemElement createRootElement(IFileSystemObject element,
            FileSystemObjectImportStructureProvider provider) {
        boolean isContainer = provider.isFolder(element);
        String elementLabel = provider.getLabel(element);

        // Use an empty label so that display of the element's full name
        // doesn't include a confusing label
        TraceFileSystemElement dummyParent = new TraceFileSystemElement("", null, true, provider);//$NON-NLS-1$
        Object dummyParentFileSystemObject = element;
        Object rawFileSystemObject = element.getRawFileSystemObject();
        if (rawFileSystemObject instanceof File) {
            dummyParentFileSystemObject = provider.getIFileSystemObject(((File) rawFileSystemObject).getParentFile());
        }
        dummyParent.setFileSystemObject(dummyParentFileSystemObject);
        dummyParent.setPopulated();
        TraceFileSystemElement result = new TraceFileSystemElement(
                elementLabel, dummyParent, isContainer, provider);
        result.setFileSystemObject(element);

        // Get the files for the element so as to build the first level
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

        String[] availableTraceTypes = TmfTraceType.getAvailableTraceTypes();
        String[] traceTypeList = new String[availableTraceTypes.length + 1];
        traceTypeList[0] = TRACE_TYPE_AUTO_DETECT;
        for (int i = 0; i < availableTraceTypes.length; i++) {
            traceTypeList[i + 1] = availableTraceTypes[i];
        }
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

        updateWidgetEnablements();
    }

    // ------------------------------------------------------------------------
    // Determine if the finish button can be enabled
    // ------------------------------------------------------------------------
    @Override
    public boolean validateSourceGroup() {

        File source = isImportFromDirectory() ? getSourceDirectory() : getSourceArchiveFile();
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

        if (!isImportFromDirectory()) {
            if (!ensureTarSourceIsValid(source.getAbsolutePath()) && !ensureZipSourceIsValid(source.getAbsolutePath())) {
                setMessage(null);
                setErrorMessage(Messages.ImportTraceWizard_BadArchiveFormat);
                return false;
            }
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
        String traceTypeName = getImportTraceTypeId();
        String traceId = null;
        if (!TRACE_TYPE_AUTO_DETECT.equals(traceTypeName)) {
            String tokens[] = traceTypeName.split(SEPARATOR, 2);
            if (tokens.length < 2) {
                return false;
            }
            traceId = TmfTraceType.getTraceTypeId(tokens[0], tokens[1]);
        }

        // Save dialog settings
        saveWidgetValues();

        IPath baseSourceContainerPath = new Path(getSourceContainerPath());
        boolean importFromArchive = getSourceArchiveFile() != null;
        int importOptionFlags = getImportOptionFlags();

        final TraceValidateAndImportOperation operation = new TraceValidateAndImportOperation(traceId, baseSourceContainerPath, getContainerFullPath(), importFromArchive,
                importOptionFlags);

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
        }
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
        return flags;
    }

    @Override
    public void dispose() {
        super.dispose();
        disposeSelectionGroupRoot();
    }

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private class TraceValidateAndImportOperation {
        private IStatus fStatus;
        private String fTraceType;
        private IPath fDestinationContainerPath;
        private IPath fBaseSourceContainerPath;
        private boolean fImportFromArchive;
        private int fImportOptionFlags;
        private ImportConfirmation fConfirmationMode = ImportConfirmation.SKIP;

        private TraceValidateAndImportOperation(String traceId, IPath baseSourceContainerPath, IPath destinationContainerPath, boolean importFromArchive, int importOptionFlags) {
            fTraceType = traceId;
            fBaseSourceContainerPath = baseSourceContainerPath;
            fDestinationContainerPath = destinationContainerPath;
            fImportOptionFlags = importOptionFlags;
            fImportFromArchive = importFromArchive;

            boolean overwriteExistingResources = (importOptionFlags & OPTION_OVERWRITE_EXISTING_RESOURCES) != 0;
            if (overwriteExistingResources) {
                fConfirmationMode = ImportConfirmation.OVERWRITE_ALL;
            }
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
                            fileSystemElements.add((TraceFileSystemElement) elements[i]);
                        }
                    }
                };

                // List fileSystemElements will be filled using the
                // passThroughFilter
                SubMonitor subMonitor = SubMonitor.convert(progressMonitor, 1);
                fSelectionGroup.getAllCheckedListItems(passThroughFilter, subMonitor);

                // Check if operation was cancelled.
                ModalContext.checkCanceled(subMonitor);

                Iterator<TraceFileSystemElement> fileSystemElementsIter = fileSystemElements.iterator();
                IFolder destTempFolder = null;
                subMonitor = SubMonitor.convert(progressMonitor, fileSystemElements.size());
                if (fImportFromArchive) {
                    // When importing from archive, we first extract the
                    // *selected* files to a temporary folder then create a new
                    // Iterator<TraceFileSystemElement> that points to the
                    // extracted files. This way, the import operator can
                    // continue as it normally would.

                    subMonitor = SubMonitor.convert(progressMonitor, fileSystemElements.size() * 2);
                    destTempFolder = fTargetFolder.getProject().getFolder(TRACE_IMPORT_TEMP_FOLDER);
                    if (destTempFolder.exists()) {
                        SubProgressMonitor monitor = new SubProgressMonitor(subMonitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
                        destTempFolder.delete(true, monitor);
                    }
                    SubProgressMonitor monitor = new SubProgressMonitor(subMonitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
                    destTempFolder.create(IResource.HIDDEN, true, monitor);

                    fileSystemElementsIter = extractSelectedFiles(fileSystemElementsIter, destTempFolder, subMonitor);
                    // We need to update the source container path because the
                    // "preserve folder structure" option would create the
                    // wrong folders otherwise.
                    fBaseSourceContainerPath = destTempFolder.getLocation();
                }

                while (fileSystemElementsIter.hasNext()) {
                    ModalContext.checkCanceled(progressMonitor);
                    currentPath = null;
                    TraceFileSystemElement element = fileSystemElementsIter.next();
                    IFileSystemObject fileSystemObject = element.getFileSystemObject();
                    String resourcePath = element.getFileSystemObject().getAbsolutePath(fBaseSourceContainerPath.toOSString());
                    element.setDestinationContainerPath(computeDestinationContainerPath(new Path(resourcePath)));

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
                        TraceFileSystemElement parentElement = (TraceFileSystemElement) element.getParent();
                        String parentPath = parentElement.getFileSystemObject().getAbsolutePath(fBaseSourceContainerPath.toOSString());
                        parentElement.setDestinationContainerPath(computeDestinationContainerPath(new Path(parentPath)));
                        currentPath = parentPath;
                        if (!folderElements.containsKey(parentPath)) {
                            if (isDirectoryTrace(parentElement)) {
                                folderElements.put(parentPath, parentElement);
                                validateAndImportTrace(parentElement, sub);
                            } else {
                                if (fileSystemObject.exists()) {
                                    validateAndImportTrace(element, sub);
                                }
                            }
                        }
                    }
                }

                if (destTempFolder != null && destTempFolder.exists()) {
                    destTempFolder.delete(true, progressMonitor);
                }

                setStatus(Status.OK_STATUS);
            } catch (InterruptedException e) {
                setStatus(Status.CANCEL_STATUS);
            } catch (Exception e) {
                String errorMessage = Messages.ImportTraceWizard_ImportProblem + ": " + //$NON-NLS-1$
                        (currentPath != null ? currentPath : ""); //$NON-NLS-1$
                Activator.getDefault().logError(errorMessage, e);
                setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, errorMessage, e));
            }
        }

        private Iterator<TraceFileSystemElement> extractSelectedFiles(Iterator<TraceFileSystemElement> fileSystemElementsIter, IFolder tempFolder, IProgressMonitor progressMonitor) throws InterruptedException,
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

            // Find a sensible root element
            TraceFileSystemElement root = subList.get(0);
            while (root.getParent() != null) {
                root = (TraceFileSystemElement) root.getParent();
            }

            ImportProvider fileSystemStructureProvider = new ImportProvider();

            IOverwriteQuery myQueryImpl = new IOverwriteQuery() {
                @Override
                public String queryOverwrite(String file) {
                    return IOverwriteQuery.NO_ALL;
                }
            };

            progressMonitor.setTaskName(Messages.ImportTraceWizard_ExtractImportOperationTaskName);
            IPath containerPath = tempFolder.getFullPath();
            ImportOperation operation = new ImportOperation(containerPath, root, fileSystemStructureProvider, myQueryImpl, subList);
            operation.setContext(getShell());

            operation.setCreateContainerStructure(true);
            operation.setOverwriteResources(false);
            operation.setVirtualFolders(false);

            operation.run(new SubProgressMonitor(progressMonitor, subList.size(), SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

            // Create the new import provider and root element based on the
            // extracted temp folder
            FileSystemObjectImportStructureProvider importStructureProvider = new FileSystemObjectImportStructureProvider(FileSystemStructureProvider.INSTANCE, null);
            IFileSystemObject rootElement = importStructureProvider.getIFileSystemObject(new File(tempFolder.getLocation().toOSString()));
            TraceFileSystemElement createRootElement = createRootElement(rootElement, importStructureProvider);
            List<TraceFileSystemElement> list = new ArrayList<>();
            getAllChildren(list, createRootElement);
            Iterator<TraceFileSystemElement> extractedElementsIter = list.iterator();
            return extractedElementsIter;
        }

        /**
         * Get all the TraceFileSystemElements recursively.
         *
         * @param result
         *            the list accumulating the result
         * @param rootElement
         *            the root element of the file system to be imported
         */
        private void getAllChildren(List<TraceFileSystemElement> result, TraceFileSystemElement rootElement) {
            AdaptableList files = rootElement.getFiles();
            for (Object file : files.getChildren()) {
                result.add((TraceFileSystemElement) file);
            }

            AdaptableList folders = rootElement.getFolders();
            for (Object folder : folders.getChildren()) {
                getAllChildren(result, (TraceFileSystemElement) folder);
            }
        }

        private IPath computeDestinationContainerPath(Path resourcePath) {
            IPath destinationContainerPath = fDestinationContainerPath;

            // We need to figure out the new destination path relative to the
            // selected "base" source directory.
            // Here for example, the selected source directory is /home/user
            if ((fImportOptionFlags & OPTION_PRESERVE_FOLDER_STRUCTURE) != 0) {
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

        private void validateAndImportTrace(TraceFileSystemElement fileSystemElement, IProgressMonitor monitor)
                throws TmfTraceImportException, CoreException, InvocationTargetException, InterruptedException {
            String parentContainerPath = fBaseSourceContainerPath.toOSString();
            String path = fileSystemElement.getFileSystemObject().getAbsolutePath(parentContainerPath);
            TraceTypeHelper traceTypeHelper = null;

            if (fTraceType == null) {
                // Auto Detection
                try {
                    traceTypeHelper = TmfTraceTypeUIUtils.selectTraceType(path, null, null);
                } catch (TmfTraceImportException e) {
                    // the trace did not match any trace type
                }
                if (traceTypeHelper == null) {
                    if ((fImportOptionFlags & OPTION_IMPORT_UNRECOGNIZED_TRACES) != 0) {
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
            IResource importedResource = importResource(fileSystemElement, monitor);
            if (importedResource != null) {
                TmfTraceTypeUIUtils.setTraceType(importedResource, traceTypeHelper);
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
                return null;
            }

            List<TraceFileSystemElement> subList = new ArrayList<>();

            FileSystemElement parentFolder = fileSystemElement.getParent();

            IPath containerPath = fileSystemElement.getDestinationContainerPath();
            IPath tracePath = containerPath.addTrailingSeparator().append(fileSystemElement.getLabel());
            boolean createLinksInWorkspace = (fImportOptionFlags & OPTION_CREATE_LINKS_IN_WORKSPACE) != 0;
            if (fileSystemElement.isDirectory() && !createLinksInWorkspace) {
                containerPath = tracePath;

                Object[] array = fileSystemElement.getFiles().getChildren();
                for (int i = 0; i < array.length; i++) {
                    subList.add((TraceFileSystemElement) array[i]);
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

            monitor.setTaskName(Messages.ImportTraceWizard_ImportOperationTaskName + " " + fileSystemElement.getFileSystemObject().getAbsolutePath(fBaseSourceContainerPath.toOSString())); //$NON-NLS-1$
            ImportOperation operation = new ImportOperation(containerPath, parentFolder, fileSystemStructureProvider, myQueryImpl, subList);
            operation.setContext(getShell());

            operation.setCreateContainerStructure(false);
            operation.setOverwriteResources(false);
            operation.setCreateLinks(createLinksInWorkspace);
            operation.setVirtualFolders(false);

            operation.run(new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
            String sourceLocation = fileSystemElement.getFileSystemObject().getSourceLocation();
            IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(tracePath);
            if (sourceLocation != null) {
                resource.setPersistentProperty(TmfCommonConstants.SOURCE_LOCATION, sourceLocation);
            }

            return resource;
        }

        private boolean isDirectoryTrace(TraceFileSystemElement fileSystemElement) {
            String path = fileSystemElement.getFileSystemObject().getAbsolutePath(fBaseSourceContainerPath.toOSString());
            if (TmfTraceType.isDirectoryTrace(path)) {
                return true;
            }
            return false;
        }

        private ImportConfirmation checkForNameClashes(TraceFileSystemElement fileSystemElement) throws InterruptedException {
            IPath tracePath = getInitialDestinationPath(fileSystemElement);

            // handle rename
            if (getExistingTrace(tracePath) != null) {
                if ((fConfirmationMode == ImportConfirmation.RENAME_ALL) ||
                        (fConfirmationMode == ImportConfirmation.OVERWRITE_ALL) ||
                        (fConfirmationMode == ImportConfirmation.SKIP_ALL)) {
                    return fConfirmationMode;
                }

                int returnCode = promptForOverwrite(tracePath);
                if (returnCode < 0) {
                    // Cancel
                    throw new InterruptedException();
                }
                fConfirmationMode = ImportConfirmation.values()[returnCode];
                return fConfirmationMode;
            }
            return ImportConfirmation.CONTINUE;
        }

        private int promptForOverwrite(IPath tracePath) {
            final MessageDialog dialog = new MessageDialog(getContainer()
                    .getShell(), null, null, NLS.bind(Messages.ImportTraceWizard_TraceAlreadyExists, tracePath.makeRelativeTo(fTraceFolderElement.getProject().getPath())),
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

        /**
         * @return the initial destination path, before rename, if any
         */
        private IPath getInitialDestinationPath(TraceFileSystemElement fileSystemElement) {
            IPath traceFolderPath = fileSystemElement.getDestinationContainerPath();
            return traceFolderPath.append(fileSystemElement.getFileSystemObject().getLabel());
        }

        private void rename(TraceFileSystemElement fileSystemElement) {
            IPath tracePath = getInitialDestinationPath(fileSystemElement);
            TmfTraceElement trace = getExistingTrace(tracePath);
            if (trace == null) {
                return;
            }

            // Not using IFolder on purpose to leave the door open to import
            // directly into an IProject
            IContainer folder = (IContainer) trace.getParent().getResource();
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
            IPath tracePath = getInitialDestinationPath(fileSystemElement);
            TmfTraceElement trace = getExistingTrace(tracePath);
            if (trace == null) {
                return;
            }

            trace.delete(monitor);
        }

        private TmfTraceElement getExistingTrace(IPath tracePath) {
            List<TmfTraceElement> traces = fTraceFolderElement.getTraces();
            for (TmfTraceElement t : traces) {
                if (t.getPath().equals(tracePath)) {
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
     * The <code>TraceFileSystemElement</code> is a
     * <code>FileSystemElement</code> that knows if it has been populated or
     * not.
     */
    private static class TraceFileSystemElement extends FileSystemElement {

        private boolean fIsPopulated = false;
        private String fLabel = null;
        private IPath fDestinationContainerPath;
        private FileSystemObjectImportStructureProvider fProvider;

        public TraceFileSystemElement(String name, FileSystemElement parent, boolean isDirectory, FileSystemObjectImportStructureProvider provider) {
            super(name, parent, isDirectory);
            fProvider = provider;
        }

        public void setDestinationContainerPath(IPath destinationContainerPath) {
            fDestinationContainerPath = destinationContainerPath;
        }

        public void setPopulated() {
            fIsPopulated = true;
        }

        public boolean isPopulated() {
            return fIsPopulated;
        }

        @Override
        public AdaptableList getFiles() {
            if (!fIsPopulated) {
                populateElementChildren();
            }
            return super.getFiles();
        }

        @Override
        public AdaptableList getFolders() {
            if (!fIsPopulated) {
                populateElementChildren();
            }
            return super.getFolders();
        }

        /**
         * Sets the label for the trace to be used when importing at trace.
         *
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
                return getFileSystemObject().getLabel();
            }
            return fLabel;
        }

        /**
         * The full path to the container that will contain the trace
         *
         * @return the destination container path
         */
        public IPath getDestinationContainerPath() {
            return fDestinationContainerPath;
        }

        /**
         * Populates the children of the specified parent
         * <code>FileSystemElement</code>
         */
        private void populateElementChildren() {
            List<IFileSystemObject> allchildren = fProvider.getChildren(this.getFileSystemObject());
            Object child = null;
            TraceFileSystemElement newelement = null;
            Iterator<IFileSystemObject> iter = allchildren.iterator();
            while (iter.hasNext()) {
                child = iter.next();
                newelement = new TraceFileSystemElement(fProvider.getLabel(child), this, fProvider.isFolder(child), fProvider);
                newelement.setFileSystemObject(child);
            }
            setPopulated();
        }

        public FileSystemObjectImportStructureProvider getProvider() {
            return fProvider;
        }

        @Override
        public IFileSystemObject getFileSystemObject() {
            Object fileSystemObject = super.getFileSystemObject();
            return (IFileSystemObject) fileSystemObject;
        }
    }

    /**
     * This interface abstracts the differences between different kinds of
     * FileSystemObjects such as File, TarEntry and ZipEntry. This allows
     * clients (TraceFileSystemElement, TraceValidateAndImportOperation) to
     * handle all the types transparently.
     */
    private interface IFileSystemObject {
        String getLabel();

        String getAbsolutePath(String parentContainerPath);

        String getSourceLocation();

        Object getRawFileSystemObject();

        boolean exists();
    }

    /**
     * The "File" implementation of an IFileSystemObject
     */
    private static class FileFileSystemObject implements IFileSystemObject {

        private File fFileSystemObject;

        private FileFileSystemObject(File fileSystemObject) {
            fFileSystemObject = fileSystemObject;
        }

        @Override
        public String getLabel() {
            String name = fFileSystemObject.getName();
            if (name.length() == 0) {
                return fFileSystemObject.getPath();
            }
            return name;
        }

        @Override
        public String getAbsolutePath(String parentContainerPath) {
            return fFileSystemObject.getAbsolutePath();
        }

        @Override
        public boolean exists() {
            return fFileSystemObject.exists();
        }

        @Override
        public String getSourceLocation() {
            IResource sourceResource;
            String sourceLocation = null;
            if (fFileSystemObject.isDirectory()) {
                sourceResource = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(Path.fromOSString(fFileSystemObject.getAbsolutePath()));
            } else {
                sourceResource = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(Path.fromOSString(fFileSystemObject.getAbsolutePath()));
            }
            if (sourceResource != null && sourceResource.exists()) {
                try {
                    sourceLocation = sourceResource.getPersistentProperty(TmfCommonConstants.SOURCE_LOCATION);
                } catch (CoreException e) {
                    // Something went wrong with the already existing resource.
                    // This is not a problem, we'll assign a new location below.
                }
            }
            if (sourceLocation == null) {
                sourceLocation = URIUtil.toUnencodedString(fFileSystemObject.toURI());
            }
            return sourceLocation;
        }

        @Override
        public Object getRawFileSystemObject() {
            return fFileSystemObject;
        }
    }

    /**
     * The "Tar" implementation of an IFileSystemObject
     */
    private static class TarFileSystemObject implements IFileSystemObject {

        private TarEntry fFileSystemObject;
        private String fArchivePath;

        private TarFileSystemObject(TarEntry fileSystemObject, String archivePath) {
            fFileSystemObject = fileSystemObject;
            fArchivePath = archivePath;
        }

        @Override
        public String getLabel() {
            return new Path(fFileSystemObject.getName()).lastSegment();
        }

        @Override
        public String getAbsolutePath(String parentContainerPath) {
            return new Path(parentContainerPath).append(fFileSystemObject.getName()).toOSString();
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public String getSourceLocation() {
            URI uri = new File(fArchivePath).toURI();
            IPath entryPath = new Path(fFileSystemObject.getName());
            return URIUtil.toUnencodedString(URIUtil.toJarURI(uri, entryPath));
        }

        @Override
        public Object getRawFileSystemObject() {
            return fFileSystemObject;
        }
    }

    /**
     * The "Zip" implementation of an IFileSystemObject
     */
    private static class ZipFileSystemObject implements IFileSystemObject {

        private ZipEntry fFileSystemObject;
        private String fArchivePath;

        private ZipFileSystemObject(ZipEntry fileSystemObject, String archivePath) {
            fFileSystemObject = fileSystemObject;
            fArchivePath = archivePath;
        }

        @Override
        public String getLabel() {
            return new Path(fFileSystemObject.getName()).lastSegment();
        }

        @Override
        public String getAbsolutePath(String parentContainerPath) {
            return new Path(parentContainerPath).append(fFileSystemObject.getName()).toOSString();
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public String getSourceLocation() {
            URI uri = new File(fArchivePath).toURI();
            IPath entryPath = new Path(fFileSystemObject.getName());
            return URIUtil.toUnencodedString(URIUtil.toJarURI(uri, entryPath));
        }

        @Override
        public Object getRawFileSystemObject() {
            return fFileSystemObject;
        }
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
            Object[] array = resource.getFiles().getChildren();
            List<Object> list = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                list.add(array[i]);
            }
            return list;
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
         *
         * @param name
         *            the name of state
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
