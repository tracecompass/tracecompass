/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlAnalysisModuleSource;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.Activator;
import org.eclipse.tracecompass.tmf.ui.dialog.DirectoryDialogFactory;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfCommonProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectModelElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * This class implements a preference page for XML analyses
 *
 * @author Jean-Christian Kouame
 * @author Christophe Bourque Bedard
 *
 */
public class XMLAnalysesManagerPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private static final int BUTTON_CHECK_SELECTED_ID = IDialogConstants.CLIENT_ID;
    private static final int BUTTON_UNCHECK_SELECTED_ID = IDialogConstants.CLIENT_ID + 1;

    private static final String XML_FILTER_EXTENSION = "*.xml"; //$NON-NLS-1$
    private Table fAnalysesTable;
    private Button fDeleteButton;
    private Button fExportButton;
    private Button fEditButton;
    private Label fStatusLabel;

    private static final String LINE_SEP = System.getProperty("line.separator"); //$NON-NLS-1$
    private static final String ELEMENT_SEP = "-\t"; //$NON-NLS-1$

    private static final IPropertyListener SAVE_EDITOR_LISTENER = (source, propId) -> {
        if (source instanceof IEditorPart) {
            IEditorPart editorPart = (IEditorPart) source;
            if (ISaveablePart.PROP_DIRTY == propId && !editorPart.isDirty()) {
                // Editor is not dirty anymore, i.e. it was saved
                if (editorPart.getEditorInput() instanceof IURIEditorInput) {
                    File file = URIUtil.toFile(((IURIEditorInput) editorPart.getEditorInput()).getURI());
                    boolean success = loadXmlFile(file, false);
                    if (success) {
                        enableAndDisableAnalyses(Collections.singletonList(file.getName()), Collections.emptyList());
                    } else {
                        enableAndDisableAnalyses(Collections.emptyList(), Collections.singletonList(file.getName()));
                    }
                }
            }
        }
    };

    @Override
    public void init(IWorkbench workbench) {
        // Do nothing
    }

    @Override
    protected Control createContents(Composite parent) {
        getShell().setText(Messages.ManageXMLAnalysisDialog_ManageXmlAnalysesFiles);

        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayout(new GridLayout(2, false));

        Composite tableContainer = new Composite(mainComposite, SWT.NONE);
        tableContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayoutFactory.fillDefaults().applyTo(tableContainer);

        // Create sub-contents
        createTable(tableContainer);
        createLabels(tableContainer);
        createImportButtons(mainComposite);
        createSelectionButtons(mainComposite);

        fillAnalysesTable();

        getShell().setMinimumSize(300, 275);

        return mainComposite;
    }

    @Override
    public boolean performOk() {
        handleChecks();
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
    }

    /**
     * Create the analyses table.
     *
     * @param composite
     *            the parent composite
     */
    private void createTable(Composite composite) {
        fAnalysesTable = new Table(composite, SWT.CHECK | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        fAnalysesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fAnalysesTable.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Do nothing
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fAnalysesTable.getSelectionCount() == 0) {
                    setButtonsEnabled(false);
                } else {
                    setButtonsEnabled(true);
                    handleSelection(fAnalysesTable.getSelection());
                }
            }
        });
    }

    /**
     * Create the file labels.
     *
     * @param composite
     *            the parent composite
     */
    private void createLabels(Composite composite) {
        fStatusLabel = new Label(composite, SWT.NONE);
        fStatusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    }

    /**
     * Add the import-related buttons.
     *
     * @param composite
     *            the parent composite
     */
    private void createImportButtons(Composite composite) {
        Composite buttonContainer = new Composite(composite, SWT.NULL);
        buttonContainer.setLayout(new GridLayout());
        buttonContainer.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

        Button importButton = new Button(buttonContainer, SWT.PUSH);
        importButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        importButton.setText(Messages.ManageXMLAnalysisDialog_Import);
        importButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                importAnalysis();
            }
        });

        fExportButton = new Button(buttonContainer, SWT.PUSH);
        fExportButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        fExportButton.setText(Messages.ManageXMLAnalysisDialog_Export);
        fExportButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                exportAnalysis();
            }
        });

        fEditButton = new Button(buttonContainer, SWT.PUSH);
        fEditButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        fEditButton.setText(Messages.ManageXMLAnalysisDialog_Edit);
        fEditButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editAnalysis();
            }
        });

        fDeleteButton = new Button(buttonContainer, SWT.PUSH);
        fDeleteButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        fDeleteButton.setText(Messages.ManageXMLAnalysisDialog_Delete);
        fDeleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteAnalyses();
            }
        });

        setButtonsEnabled(false);
    }

    /**
     * Add the selection and deselection buttons to the dialog.
     *
     * @param composite
     *            the parent composite
     */
    protected void createSelectionButtons(Composite composite) {
        Composite buttonComposite = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout(2, true);
        layout.marginWidth = 0;
        buttonComposite.setLayout(layout);
        buttonComposite.setFont(composite.getFont());
        GridData data = new GridData(SWT.RIGHT, SWT.TOP, false, false);
        buttonComposite.setLayoutData(data);

        /* Create the buttons in the good order to place them as we want */
        Button checkSelectedButton = createButton(buttonComposite,
                BUTTON_CHECK_SELECTED_ID, Messages.ManageXMLAnalysisDialog_CHECK_SELECTED);
        Button checkAllButton = createButton(buttonComposite,
                IDialogConstants.SELECT_ALL_ID, Messages.ManageXMLAnalysisDialog_CHECK_ALL);
        Button uncheckSelectedButton = createButton(buttonComposite,
                BUTTON_UNCHECK_SELECTED_ID, Messages.ManageXMLAnalysisDialog_UNCHECK_SELECTED);
        Button uncheckAllButton = createButton(buttonComposite,
                IDialogConstants.DESELECT_ALL_ID, Messages.ManageXMLAnalysisDialog_UNCHECK_ALL);

        /* Add a listener to each button */
        checkSelectedButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (TableItem selectedItem : fAnalysesTable.getSelection()) {
                    selectedItem.setChecked(true);
                }
                handleSelection(fAnalysesTable.getSelection());
            }
        });

        checkAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (TableItem selectedItem : fAnalysesTable.getItems()) {
                    selectedItem.setChecked(true);
                }
                handleSelection(fAnalysesTable.getSelection());
            }
        });

        uncheckSelectedButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (TableItem selectedItem : fAnalysesTable.getSelection()) {
                    selectedItem.setChecked(false);
                }
            }
        });

        uncheckAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (TableItem selectedItem : fAnalysesTable.getItems()) {
                    selectedItem.setChecked(false);
                }
            }
        });
    }

    /**
     * Helper method for creating a button.
     *
     * @param parent
     *            the parent composite
     * @param id
     *            the id
     * @param label
     *            the label to display
     * @return the resulting button
     */
    private static Button createButton(Composite parent, int id, String label) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        button.setFont(JFaceResources.getDialogFont());
        button.setData(Integer.valueOf(id));
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, true);
        button.setLayoutData(data);
        return button;
    }

    private void setButtonsEnabled(boolean enable) {
        fDeleteButton.setEnabled(enable);
        fExportButton.setEnabled(enable);
        fEditButton.setEnabled(enable);
    }

    /**
     * Handle the current table selection by validating the associated file.
     *
     * @param selection
     *            the selected table item
     */
    private void handleSelection(TableItem[] selection) {
        for (TableItem selectedItem : selection) {
            String xmlName = XmlUtils.createXmlFileString(selectedItem.getText());
            if (isFileValid(xmlName)) {
                if (selection.length == 1) {
                    if (XmlUtils.isAnalysisEnabled(xmlName)) {
                        fStatusLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
                        fStatusLabel.setText(Messages.ManageXMLAnalysisDialog_FileEnabled);
                    } else {
                        fStatusLabel.setText(""); //$NON-NLS-1$
                    }
                }
            } else {
                if (selection.length == 1) {
                    fStatusLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
                    fStatusLabel.setText(Messages.ManageXMLAnalysisDialog_FileValidationError);
                }
                if (XmlUtils.isAnalysisEnabled(xmlName)) {
                    enableAndDisableAnalyses(
                            Collections.emptyList(),
                            ImmutableList.of(Objects.requireNonNull(xmlName)));
                }
                selectedItem.setChecked(false);
            }
        }
        if (selection.length != 1) {
            fStatusLabel.setText(""); //$NON-NLS-1$
        }
    }

    /**
     * Fill the table with the imported files.
     */
    private void fillAnalysesTable() {
        fAnalysesTable.removeAll();
        Map<String, File> files = XmlUtils.listFiles();
        for (String file : files.keySet()) {
            // Remove the extension from the file path to display.
            IPath path = new Path(file);

            // Create item and add to table
            TableItem item = new TableItem(fAnalysesTable, SWT.NONE);
            item.setText(path.removeFileExtension().toString());
            item.setChecked(XmlUtils.isAnalysisEnabled(path.toString()));
        }
        setButtonsEnabled(false);
    }

    /**
     * Apply change to items according to their checkboxes.
     */
    private void handleChecks() {
        Map<@NonNull String, @NonNull File> listFiles = XmlUtils.listFiles();
        Collection<String> filesToEnable = Lists.newArrayList();
        Collection<String> filesToDisable = Lists.newArrayList();

        for (TableItem item : fAnalysesTable.getItems()) {
            String xmlName = XmlUtils.createXmlFileString(item.getText());
            // Only enable/disable if the checkbox status has changed
            if (item.getChecked() && !XmlUtils.isAnalysisEnabled(xmlName)) {
                // Do not enable an invalid file
                if (isFileValid(xmlName, listFiles)) {
                    filesToEnable.add(xmlName);
                } else {
                    item.setChecked(false);
                }
            } else if (!item.getChecked() && XmlUtils.isAnalysisEnabled(xmlName)) {
                filesToDisable.add(xmlName);
            }
        }

        // Apply changes
        if (!(filesToEnable.isEmpty() && filesToDisable.isEmpty())) {
            enableAndDisableAnalyses(filesToEnable, filesToDisable);
        }

        // Force update for selection handling
        handleSelection(fAnalysesTable.getSelection());
    }

    /**
     * Enable and disable analyses all at once.
     *
     * @param toEnable
     *            the list of xml file names (with extension) to enable
     * @param toDisable
     *            the list of xml file names (with extension) to disable
     */
    private static void enableAndDisableAnalyses(Collection<String> toEnable, Collection<String> toDisable) {
        Collection<String> toEnableOrDisable = new ArrayList<>(toEnable.size() + toDisable.size());
        toEnableOrDisable.addAll(toEnable);
        toEnableOrDisable.addAll(toDisable);
        Collection<TmfCommonProjectElement> elements = deleteSupplementaryFiles(toEnableOrDisable);
        XmlUtils.enableFiles(toEnable);
        XmlUtils.disableFiles(toDisable);
        XmlAnalysisModuleSource.notifyModuleChange();
        refreshProject(elements);
    }

    /**
     * Import new analysis.
     */
    private void importAnalysis() {
        FileDialog dialog = TmfFileDialogFactory.create(Display.getCurrent().getActiveShell(), SWT.OPEN | SWT.MULTI);
        dialog.setText(Messages.ManageXMLAnalysisDialog_SelectFilesImport);
        dialog.setFilterNames(new String[] { Messages.ManageXMLAnalysisDialog_ImportXmlFile + " (" + XML_FILTER_EXTENSION + ")" }); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.setFilterExtensions(new String[] { XML_FILTER_EXTENSION });
        dialog.open();
        String directoryPath = dialog.getFilterPath();
        if (!directoryPath.isEmpty()) {
            File directory = new File(directoryPath);
            String[] files = dialog.getFileNames();
            Collection<String> filesToProcess = Lists.newArrayList();
            for (String fileName : files) {
                File file = new File(directory, fileName);
                if (loadXmlFile(file, true)) {
                    filesToProcess.add(file.getName());
                }
            }
            if (!filesToProcess.isEmpty()) {
                fillAnalysesTable();
                Collection<TmfCommonProjectElement> elements = deleteSupplementaryFiles(filesToProcess);
                XmlAnalysisModuleSource.notifyModuleChange();
                refreshProject(elements);
            }
        }
    }

    /**
     * Load an XML analysis file.
     *
     * @param file
     *            the file
     * @param addFile
     *            true if the file should be added/copied, false otherwise
     * @return true if loading was successful, false otherwise
     */
    private static boolean loadXmlFile(File file, boolean addFile) {
        IStatus status = XmlUtils.xmlValidate(file);
        if (status.isOK()) {
            if (addFile) {
                status = XmlUtils.addXmlFile(file);
            } else {
                XmlUtils.updateXmlFile(file);
            }
            if (status.isOK()) {
                return true;
            }

            Activator.logError(Messages.ManageXMLAnalysisDialog_ImportFileFailed);
            TraceUtils.displayErrorMsg(Messages.ManageXMLAnalysisDialog_ImportFileFailed, status.getMessage());
        } else {
            Activator.logError(Messages.ManageXMLAnalysisDialog_ImportFileFailed);
            TraceUtils.displayErrorMsg(Messages.ManageXMLAnalysisDialog_ImportFileFailed, status.getMessage());
        }
        return false;
    }

    /**
     * Export analysis to new file.
     */
    private void exportAnalysis() {
        TableItem[] selection = fAnalysesTable.getSelection();
        DirectoryDialog dialog = DirectoryDialogFactory.create(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setText(NLS.bind(Messages.ManageXMLAnalysisDialog_SelectDirectoryExport, selection.length));
        String directoryPath = dialog.open();
        if (directoryPath != null) {
            File directory = new File(directoryPath);
            for (TableItem item : selection) {
                String fileName = item.getText();
                String fileNameXml = XmlUtils.createXmlFileString(fileName);
                String path = new File(directory, fileNameXml).getAbsolutePath();
                if (!XmlUtils.exportXmlFile(fileNameXml, path).isOK()) {
                    Activator.logError(NLS.bind(Messages.ManageXMLAnalysisDialog_FailedToExport, fileNameXml));
                }
            }
        }
    }

    /**
     * Edit analysis file(s) with built-in editor.
     */
    private void editAnalysis() {
        Map<@NonNull String, @NonNull File> listFiles = XmlUtils.listFiles();
        for (TableItem item : fAnalysesTable.getSelection()) {
            String selection = XmlUtils.createXmlFileString(item.getText());
            @Nullable File file = listFiles.get(selection);
            if (file == null) {
                Activator.logError(NLS.bind(Messages.ManageXMLAnalysisDialog_FailedToEdit, selection));
                TraceUtils.displayErrorMsg(NLS.bind(Messages.ManageXMLAnalysisDialog_FailedToEdit, selection), NLS.bind(Messages.ManageXMLAnalysisDialog_FailedToEdit, selection));
                return;
            }
            try {
                IEditorPart editorPart = IDE.openEditorOnFileStore(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), EFS.getStore(file.toURI()));
                // Remove listener first in case the editor was already opened
                editorPart.removePropertyListener(SAVE_EDITOR_LISTENER);
                editorPart.addPropertyListener(SAVE_EDITOR_LISTENER);
            } catch (CoreException e) {
                Activator.logError(NLS.bind(Messages.ManageXMLAnalysisDialog_FailedToEdit, selection));
                TraceUtils.displayErrorMsg(NLS.bind(Messages.ManageXMLAnalysisDialog_FailedToEdit, selection), e.getMessage());
            }
        }
    }

    /**
     * Delete analyses, remove the corresponding files, and close the editors, if
     * opened.
     */
    private void deleteAnalyses() {
        // Get list of files
        TableItem[] selection = fAnalysesTable.getSelection();
        List<String> filesToDeleteList = Lists.newArrayList();
        for (TableItem item : selection) {
            filesToDeleteList.add(ELEMENT_SEP + item.getText());
        }
        final String filesToDelete = Joiner.on(LINE_SEP).join(filesToDeleteList);

        boolean confirm = MessageDialog.openQuestion(
                getShell(),
                Messages.ManageXMLAnalysisDialog_DeleteFile,
                Messages.ManageXMLAnalysisDialog_DeleteConfirmation + StringUtils.repeat(LINE_SEP, 2) + filesToDelete);
        if (confirm) {
            Set<IEditorReference> editorReferences = getEditorReferences();
            Collection<String> toDeleteSupFiles = Lists.newArrayList();
            Collection<String> toDeleteFiles = Lists.newArrayList();
            for (TableItem item : selection) {
                String itemTitle = XmlUtils.createXmlFileString(item.getText());
                // If opened, close the editor before deleting the file
                editorReferences.forEach(editorReference -> {
                    if (editorReference.getTitle().equals(itemTitle)) {
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(editorReference.getEditor(false), false);
                    }
                });
                // We do not need to re-open the elements of an analysis that was already
                // disabled
                if (XmlUtils.isAnalysisEnabled(itemTitle)) {
                    toDeleteSupFiles.add(itemTitle);
                }
                toDeleteFiles.add(itemTitle);
            }
            Collection<TmfCommonProjectElement> elements = deleteSupplementaryFiles(toDeleteSupFiles);
            XmlUtils.deleteFiles(toDeleteFiles);
            fillAnalysesTable();
            handleSelection(fAnalysesTable.getSelection());
            XmlAnalysisModuleSource.notifyModuleChange();
            refreshProject(elements);
        }
    }

    /**
     * Delete the supplementary files associated with XML analysis files.
     *
     * @param xmlFiles
     *            the xml analysis files (with extension)
     * @return the list of elements that should be re-opened
     */
    private static Collection<TmfCommonProjectElement> deleteSupplementaryFiles(Collection<String> xmlFiles) {
        // 1. Look for all traces that have these analyses
        // 2. Close them if they are opened, but remember them
        // 3. Delete the related supplementary files
        Collection<TmfCommonProjectElement> toReopen = new ArrayList<>();
        List<IResource> resourceToDelete = new ArrayList<>();
        Set<String> ids = Sets.newHashSet();
        xmlFiles.forEach(xmlFile -> ids.addAll(XmlUtils.getAnalysisIdsFromFile(xmlFile)));
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects(0);
        for (IProject project : projects) {
            TmfProjectElement pElement = TmfProjectRegistry.getProject(project);
            if (pElement != null) {
                List<TmfCommonProjectElement> tElements = new ArrayList<>();
                TmfTraceFolder tracesFolder = pElement.getTracesFolder();
                if (tracesFolder != null) {
                    tElements.addAll(tracesFolder.getTraces());
                }
                TmfExperimentFolder experimentsFolder = pElement.getExperimentsFolder();
                if (experimentsFolder != null) {
                    tElements.addAll(experimentsFolder.getExperiments());
                }

                Set<IEditorReference> editorReferences = getEditorReferences();

                for (TmfCommonProjectElement tElement : tElements) {
                    for (IResource resource : tElement.getSupplementaryResources()) {
                        for (String id : ids) {
                            if (resource.getName().startsWith(id)) {
                                resourceToDelete.add(resource);
                            }
                        }
                    }
                    IFile file = tElement.getBookmarksFile();
                    FileEditorInput input = new FileEditorInput(file);
                    boolean open = Iterables.any(editorReferences, editorReference -> {
                        try {
                            return editorReference.getEditorInput().equals(input);
                        } catch (PartInitException e) {
                            Activator.logError("Failed to test the " + tElement.getName() + " editor", e); //$NON-NLS-1$ //$NON-NLS-2$
                            return false;
                        }
                    });
                    if (open) {
                        toReopen.add(tElement);
                    }
                    tElement.closeEditors();
                }
            }
        }
        for (IResource resource : resourceToDelete) {
            try {
                resource.delete(false, null);
            } catch (CoreException e) {
                Activator.logError(NLS.bind(Messages.ManageXMLAnalysisDialog_DeleteFileError, resource.getName()));
            }
        }
        return toReopen;
    }

    /**
     * Refresh the selected project elements. This is useful after XML files
     * importing/deletion/enabling/disabling.
     *
     * @param elements
     *            the elements to re-open
     */
    private static void refreshProject(Collection<TmfCommonProjectElement> elements) {
        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return;
        }

        // Get the selection
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return;
        }
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return;
        }
        ISelection selection = selectionProvider.getSelection();

        if (selection instanceof TreeSelection) {
            TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            Object element = sel.getFirstElement();
            if (element instanceof TmfProjectModelElement) {
                ((TmfProjectModelElement) element).getProject().refresh();
            }
        }

        // Re-open given elements
        elements.forEach(TmfOpenTraceHelper::openFromElement);
    }

    /**
     * Validate an XML file.
     *
     * @param xmlName
     *            the xml file, with extension
     * @return true if valid, false otherwise
     */
    private static boolean isFileValid(String xmlName) {
        return isFileValid(xmlName, XmlUtils.listFiles());
    }

    /**
     * Validate an XML file. This version is intended to be used when validating
     * multiple files (i.e. when this is called multiple times).
     *
     * @param xmlName
     *            the xml file, with extension
     * @param listFiles
     *            the XML files
     * @return
     */
    private static boolean isFileValid(String xmlName, Map<@NonNull String, @NonNull File> listFiles) {
        File file = listFiles.get(xmlName);
        return (file != null && XmlUtils.xmlValidate(file).isOK());
    }

    /**
     * Get editor references.
     *
     * @return the set of editor references
     */
    private static Set<IEditorReference> getEditorReferences() {
        Set<IEditorReference> editorReferences = new HashSet<>();
        IWorkbench wb = PlatformUI.getWorkbench();
        for (IWorkbenchWindow wbWindow : wb.getWorkbenchWindows()) {
            for (IWorkbenchPage wbPage : wbWindow.getPages()) {
                editorReferences.addAll(Arrays.asList(wbPage.getEditorReferences()));
            }
        }
        return editorReferences;
    }
}