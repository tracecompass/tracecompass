/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson and others.
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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.TmfProjectNature;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.ui.project.wizards.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.ui.dialogs.WizardResourceImportPage;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * A variant of the standard resource import wizard with the following changes:
 * <ul>
 * <li>A folder/file combined checkbox tree viewer to select traces
 * <li>Cherry-picking of traces in the file structure without re-creating the
 * file hierarchy
 * <li>A trace types dropbox for optional characterization
 * </ul>
 * For our purpose, a trace can either be a single file or a whole directory
 * sub-tree, whichever is reached first from the root directory.
 * <p>
 *
 * @version 1.0
 * @author Francois Chouinard
 * @since 2.0
 */
public class ImportTraceWizardPage extends WizardResourceImportPage {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    static private final String IMPORT_WIZARD_PAGE = "ImportTraceWizardPage"; //$NON-NLS-1$
    private static final String DEFAULT_TRACE_ICON_PATH = "icons/elcl16/trace.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Folder navigation start point (saved between invocations)
    private static String fRootDirectory = null;

    // Navigation folder content viewer and selector
    private CheckboxTreeViewer fFolderViewer;

    // Target import directory ('Traces' folder)
    private IFolder fTargetFolder;

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
            TmfTraceFolder tmfTraceFolder = (TmfTraceFolder) element;
            tmfTraceFolder.getProject().getResource();
            traceFolder = tmfTraceFolder.getResource();
        } else if (element instanceof IProject) {
            IProject project = (IProject) element;
            try {
                if (project.hasNature(TmfProjectNature.ID)) {
                    traceFolder = (IFolder) project.findMember(TmfTraceFolder.TRACE_FOLDER_NAME);
                }
            } catch (CoreException e) {
            }
        }

        // Set the target trace folder
        if (traceFolder != null) {
            fTargetFolder = traceFolder;
            String path = traceFolder.getFullPath().toOSString();
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
    protected void createFileSelectionGroup(Composite parent) {

        // This Composite is only used for widget alignment purposes
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        final int PREFERRED_LIST_HEIGHT = 150;

        fFolderViewer = new CheckboxTreeViewer(composite, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = PREFERRED_LIST_HEIGHT;
        fFolderViewer.getTree().setLayoutData(data);
        fFolderViewer.getTree().setFont(parent.getFont());

        fFolderViewer.setContentProvider(getFileProvider());
        fFolderViewer.setLabelProvider(new WorkbenchLabelProvider());
        fFolderViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                Object elem = event.getElement();
                if (elem instanceof FileSystemElement) {
                    FileSystemElement element = (FileSystemElement) elem;
                    if (fFolderViewer.getGrayed(element)) {
                        fFolderViewer.setSubtreeChecked(element, false);
                        fFolderViewer.setGrayed(element, false);
                    } else if (event.getChecked()) {
                        fFolderViewer.setSubtreeChecked(event.getElement(), true);
                    } else {
                        fFolderViewer.setParentsGrayed(element, true);
                        if (!element.isDirectory()) {
                            fFolderViewer.setGrayed(element, false);
                        }
                    }
                    updateWidgetEnablements();
                }
            }
        });
    }

    @Override
    protected ITreeContentProvider getFolderProvider() {
        return null;
    }

    @Override
    protected ITreeContentProvider getFileProvider() {
        return new WorkbenchContentProvider() {
            @Override
            public Object[] getChildren(Object o) {
                if (o instanceof FileSystemElement) {
                    FileSystemElement element = (FileSystemElement) o;
                    populateChildren(element);
                    // For our purpose, we need folders + files
                    Object[] folders = element.getFolders().getChildren();
                    Object[] files = element.getFiles().getChildren();

                    List<Object> result = new LinkedList<Object>();
                    for (Object folder : folders) {
                        result.add(folder);
                    }
                    for (Object file : files) {
                        result.add(file);
                    }

                    return result.toArray();
                }
                return new Object[0];
            }
        };
    }

    private static void populateChildren(FileSystemElement parent) {
        // Do not re-populate if the job was done already...
        FileSystemStructureProvider provider = FileSystemStructureProvider.INSTANCE;
        if (parent.getFolders().size() == 0 && parent.getFiles().size() == 0) {
            Object fileSystemObject = parent.getFileSystemObject();
            List<?> children = provider.getChildren(fileSystemObject);
            if (children != null) {
                Iterator<?> iterator = children.iterator();
                while (iterator.hasNext()) {
                    Object child = iterator.next();
                    String label = provider.getLabel(child);
                    FileSystemElement element = new FileSystemElement(label, parent, provider.isFolder(child));
                    element.setFileSystemObject(child);
                }
            }
        }
    }

    @Override
    protected List<FileSystemElement> getSelectedResources() {
        List<FileSystemElement> resources = new ArrayList<FileSystemElement>();
        Object[] checkedItems = fFolderViewer.getCheckedElements();
        for (Object item : checkedItems) {
            if (item instanceof FileSystemElement && !fFolderViewer.getGrayed(item)) {
                resources.add((FileSystemElement) item);
            }
        }
        return resources;
    }

    // ------------------------------------------------------------------------
    // Directory Selection Group (forked WizardFileSystemResourceImportPage1)
    // ------------------------------------------------------------------------

    /**
     * The directory name field
     */
    protected Combo directoryNameField;
    /**
     * The directory browse button.
     */
    protected Button directoryBrowseButton;

    private boolean entryChanged = false;

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
        super.handleEvent(event);
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
        FileSystemElement root = getFileSystemTree();
        populateListViewer(root);
    }

    private void populateListViewer(final Object treeElement) {
        fFolderViewer.setInput(treeElement);
    }

    private FileSystemElement getFileSystemTree() {
        File sourceDirectory = getSourceDirectory();
        if (sourceDirectory == null) {
            return null;
        }
        return selectFiles(sourceDirectory, FileSystemStructureProvider.INSTANCE);
    }

    private FileSystemElement selectFiles(final Object rootFileSystemObject, final IImportStructureProvider structureProvider) {
        final FileSystemElement[] results = new FileSystemElement[1];
        BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
            @Override
            public void run() {
                // Create the root element from the supplied file system object
                results[0] = createRootElement(rootFileSystemObject, structureProvider);
            }
        });
        return results[0];
    }

    private static FileSystemElement createRootElement(Object fileSystemObject,
            IImportStructureProvider provider) {

        boolean isContainer = provider.isFolder(fileSystemObject);
        String elementLabel = provider.getLabel(fileSystemObject);

        FileSystemElement dummyParent = new FileSystemElement("", null, true); //$NON-NLS-1$
        FileSystemElement element = new FileSystemElement(elementLabel, dummyParent, isContainer);
        element.setFileSystemObject(fileSystemObject);

        // Get the first level
        populateChildren(element);

        return dummyParent;
    }

    // ------------------------------------------------------------------------
    // Trace Type Group
    // ------------------------------------------------------------------------

    private Combo fTraceTypes;

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
        fTraceTypes = new Combo(composite, SWT.BORDER);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
        fTraceTypes.setLayoutData(data);
        fTraceTypes.setFont(parent.getFont());

        String[] availableTraceTypes = TmfTraceType.getInstance().getAvailableTraceTypes();
        fTraceTypes.setItems(availableTraceTypes);

        fTraceTypes.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateWidgetEnablements();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    // ------------------------------------------------------------------------
    // Options
    // ------------------------------------------------------------------------

    private Button overwriteExistingResourcesCheckbox;
    private Button createLinksInWorkspaceButton;

    @Override
    protected void createOptionsGroupButtons(Group optionsGroup) {

        // Overwrite checkbox
        overwriteExistingResourcesCheckbox = new Button(optionsGroup, SWT.CHECK);
        overwriteExistingResourcesCheckbox.setFont(optionsGroup.getFont());
        overwriteExistingResourcesCheckbox.setText(Messages.ImportTraceWizard_OverwriteExistingTrace);
        overwriteExistingResourcesCheckbox.setSelection(false);

        // Create links checkbox
        createLinksInWorkspaceButton = new Button(optionsGroup, SWT.CHECK);
        createLinksInWorkspaceButton.setFont(optionsGroup.getFont());
        createLinksInWorkspaceButton.setText(Messages.ImportTraceWizard_CreateLinksInWorkspace);
        createLinksInWorkspaceButton.setSelection(true);

        createLinksInWorkspaceButton.addSelectionListener(new SelectionAdapter() {
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

        List<FileSystemElement> resourcesToImport = getSelectedResources();
        if (resourcesToImport.size() == 0) {
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
            if (createLinksInWorkspaceButton == null || !createLinksInWorkspaceButton.getSelection()) {
                setMessage(null);
                setErrorMessage(Messages.ImportTraceWizard_HaveToCreateLinksUnderAVirtualFolder);
                return false;
            }
        }

        // Perform trace validation
        String traceTypeName = fTraceTypes.getText();
        String tokens[] = traceTypeName.split(":", 2); //$NON-NLS-1$
        if (tokens.length >= 2) {
            String id = TmfTraceType.getInstance().getTraceTypeId(tokens[0], tokens[1]);
            if (!TmfTraceType.getInstance().validateTrace(id, getSelectedResources())) {
                setMessage(null);
                setErrorMessage(Messages.ImportTraceWizard_TraceValidationFailed);
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
     * @return <code>true</code> if successful else false
     */
    public boolean finish() {
        // Ensure source is valid
        File sourceDir = new File(getSourceDirectoryName());
        if (!sourceDir.isDirectory()) {
            setErrorMessage(Messages.ImportTraceWizard_InvalidTraceDirectory);
            return false;
        }

        try {
            sourceDir.getCanonicalPath();
        } catch (IOException e) {
            MessageDialog.openInformation(getContainer().getShell(), Messages.ImportTraceWizard_Information,
                    Messages.ImportTraceWizard_InvalidTraceDirectory);
            return false;
        }

        // Save directory for next import operation
        fRootDirectory = getSourceDirectoryName();

        List<FileSystemElement> selectedResources = getSelectedResources();
        Iterator<FileSystemElement> resources = selectedResources.iterator();

        // Use a map to end up with unique resources (getSelectedResources() can
        // return duplicates)
        Map<String, File> fileSystemObjects = new HashMap<String, File>();
        while (resources.hasNext()) {
            File file = (File) resources.next().getFileSystemObject();
            String key = file.getAbsolutePath();
            fileSystemObjects.put(key, file);
        }

        if (fileSystemObjects.size() > 0) {
            boolean ok = importResources(fileSystemObjects);
            String traceBundle = null;
            String traceTypeId = null;
            String traceIcon = null;
            String traceType = fTraceTypes.getText();
            boolean traceTypeOK = false;
            if (traceType.startsWith(TmfTraceType.CUSTOM_TXT_CATEGORY)) {
                for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                    if (traceType.equals(TmfTraceType.CUSTOM_TXT_CATEGORY + " : " + def.definitionName)) { //$NON-NLS-1$
                        traceTypeOK = true;
                        traceBundle = Activator.getDefault().getBundle().getSymbolicName();
                        traceTypeId = CustomTxtTrace.class.getCanonicalName() + ":" + def.definitionName; //$NON-NLS-1$
                        traceIcon = DEFAULT_TRACE_ICON_PATH;
                        break;
                    }
                }
            } else if (traceType.startsWith(TmfTraceType.CUSTOM_XML_CATEGORY)) {
                for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                    if (traceType.equals(TmfTraceType.CUSTOM_XML_CATEGORY + " : " + def.definitionName)) { //$NON-NLS-1$
                        traceTypeOK = true;
                        traceBundle = Activator.getDefault().getBundle().getSymbolicName();
                        traceTypeId = CustomXmlTrace.class.getCanonicalName() + ":" + def.definitionName; //$NON-NLS-1$
                        traceIcon = DEFAULT_TRACE_ICON_PATH;
                        break;
                    }
                }
            } else {
                IConfigurationElement ce = TmfTraceType.getInstance().getTraceAttributes(traceType);
                if (ce != null) {
                    traceTypeOK = true;
                    traceBundle = ce.getContributor().getName();
                    traceTypeId = ce.getAttribute(TmfTraceType.ID_ATTR);
                    traceIcon = ce.getAttribute(TmfTraceType.ICON_ATTR);
                }
            }
            if (ok && traceTypeOK && !traceType.equals("")) { //$NON-NLS-1$
                // Tag the selected traces with their type
                List<String> files = new ArrayList<String>(fileSystemObjects.keySet());
                Collections.sort(files, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        String v1 = o1 + File.separatorChar;
                        String v2 = o2 + File.separatorChar;
                        return v1.compareTo(v2);
                    }
                });
                // After sorting, traces correspond to the unique prefixes
                String prefix = null;
                for (int i = 0; i < files.size(); i++) {
                    File file = fileSystemObjects.get(files.get(i));
                    String name = file.getAbsolutePath() + File.separatorChar;
                    if (fTargetFolder != null && (prefix == null || !name.startsWith(prefix))) {
                        prefix = name; // new prefix
                        IResource resource = fTargetFolder.findMember(file.getName());
                        if (resource != null) {
                            try {
                                // Set the trace properties for this resource
                                resource.setPersistentProperty(TmfCommonConstants.TRACEBUNDLE, traceBundle);
                                resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, traceTypeId);
                                resource.setPersistentProperty(TmfCommonConstants.TRACEICON, traceIcon);
                                TmfProjectElement tmfProject = TmfProjectRegistry.getProject(resource.getProject());
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
                }
            }
            return ok;
        }

        MessageDialog.openInformation(getContainer().getShell(), Messages.ImportTraceWizard_Information,
                Messages.ImportTraceWizard_SelectTraceNoneSelected);
        return false;
    }

    private boolean importResources(Map<String, File> fileSystemObjects) {

        // Determine the sorted canonical list of items to import
        List<File> fileList = new ArrayList<File>();
        for (Entry<String, File> entry : fileSystemObjects.entrySet()) {
            fileList.add(entry.getValue());
        }
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String v1 = o1.getAbsolutePath() + File.separatorChar;
                String v2 = o2.getAbsolutePath() + File.separatorChar;
                return v1.compareTo(v2);
            }
        });

        // Perform a distinct import operation for everything that has the same
        // prefix
        // (distinct prefixes correspond to traces - we don't want to re-create
        // parent structures)
        boolean ok = true;
        boolean isLinked = createLinksInWorkspaceButton.getSelection();
        for (int i = 0; i < fileList.size(); i++) {
            File resource = fileList.get(i);
            File parentFolder = new File(resource.getParent());

            List<File> subList = new ArrayList<File>();
            subList.add(resource);
            if (resource.isDirectory()) {
                String prefix = resource.getAbsolutePath() + File.separatorChar;
                boolean hasSamePrefix = true;
                for (int j = i + 1; j < fileList.size() && hasSamePrefix; j++) {
                    File res = fileList.get(j);
                    hasSamePrefix = res.getAbsolutePath().startsWith(prefix);
                    if (hasSamePrefix) {
                        // Import children individually if not linked
                        if (!isLinked) {
                            subList.add(res);
                        }
                        i = j;
                    }
                }
            }

            // Perform the import operation for this subset
            FileSystemStructureProvider fileSystemStructureProvider = FileSystemStructureProvider.INSTANCE;
            ImportOperation operation = new ImportOperation(getContainerFullPath(), parentFolder, fileSystemStructureProvider, this,
                    subList);
            operation.setContext(getShell());
            ok = executeImportOperation(operation);
        }

        return ok;
    }

    private boolean executeImportOperation(ImportOperation op) {
        initializeOperation(op);

        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            displayErrorDialog(e.getTargetException());
            return false;
        }

        IStatus status = op.getStatus();
        if (!status.isOK()) {
            ErrorDialog.openError(getContainer().getShell(), Messages.ImportTraceWizard_ImportProblem, null, status);
            return false;
        }

        return true;
    }

    private void initializeOperation(ImportOperation op) {
        op.setCreateContainerStructure(false);
        op.setOverwriteResources(overwriteExistingResourcesCheckbox.getSelection());
        op.setCreateLinks(createLinksInWorkspaceButton.getSelection());
        op.setVirtualFolders(false);
    }

}
