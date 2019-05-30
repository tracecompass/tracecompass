/*******************************************************************************
 * Copyright (c) 2013, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.importexport;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.AbstractTracePackageOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.AbstractTracePackageWizardPage;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageExperimentElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Wizard page for the import trace package wizard
 *
 * @author Marc-Andre Laperle
 */
public class ImportTracePackageWizardPage extends AbstractTracePackageWizardPage {

    private static final String ICON_PATH = "icons/wizban/trace_import_wiz.png"; //$NON-NLS-1$
    private static final String PAGE_NAME = "ImportTracePackagePage"; //$NON-NLS-1$
    private static final String STORE_PROJECT_NAME_ID = PAGE_NAME + ".STORE_PROJECT_NAME_ID"; //$NON-NLS-1$

    private String fValidatedFilePath;
    private TmfTraceFolder fTmfTraceFolder;
    private Text fProjectText;
    private List<IProject> fOpenedTmfProjects;

    /**
     * Constructor for the import trace package wizard page
     *
     * @param selection
     *            the current object selection
     */
    public ImportTracePackageWizardPage(IStructuredSelection selection) {
        super(PAGE_NAME, Messages.ImportTracePackageWizardPage_Title, Activator.getDefault().getImageDescripterFromPath(ICON_PATH), selection);

        if (getSelection().getFirstElement() instanceof TmfTraceFolder) {
            fTmfTraceFolder = (TmfTraceFolder) getSelection().getFirstElement();
        }
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());

        createFilePathGroup(composite, Messages.ImportTracePackageWizardPage_FromArchive, SWT.OPEN);
        createElementViewer(composite);
        createButtonsGroup(composite);
        if (fTmfTraceFolder == null) {
            createProjectSelectionGroup(composite);
        }

        restoreWidgetValues();
        setMessage(Messages.ImportTracePackageWizardPage_Message);
        updatePageCompletion();

        setControl(composite);
    }

    private void createProjectSelectionGroup(Composite parent) {

        Composite projectSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        projectSelectionGroup.setLayout(layout);
        projectSelectionGroup.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

        Label projectLabel = new Label(projectSelectionGroup, SWT.NONE);
        projectLabel.setText(Messages.ImportTracePackageWizardPage_Project);

        fProjectText = new Text(projectSelectionGroup, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL);
        data.grabExcessHorizontalSpace = true;
        fProjectText.setLayoutData(data);

        fOpenedTmfProjects = TraceUtils.getOpenedTmfProjects();

        // No project to import to, create a default project if it doesn't exist
        if (fOpenedTmfProjects.isEmpty()) {
            IProject defaultProject = ResourcesPlugin.getWorkspace().getRoot().getProject(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME);
            if (!defaultProject.exists()) {
                IProject project = TmfProjectRegistry.createProject(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME, null, null);
                fOpenedTmfProjects.add(project);
            }
        }

        if (!fOpenedTmfProjects.isEmpty()) {
            selectProject(fOpenedTmfProjects.get(0));
        }

        Button button = new Button(projectSelectionGroup,
                SWT.PUSH);
        button.setText(Messages.ImportTracePackageWizardPage_SelectProjectButton);
        button.addListener(SWT.Selection, event -> {
            ElementListSelectionDialog d = new ElementListSelectionDialog(getContainer().getShell(), new WorkbenchLabelProvider());

            d.setBlockOnOpen(true);
            d.setTitle(Messages.ImportTracePackageWizardPage_SelectProjectDialogTitle);

            d.setElements(fOpenedTmfProjects.toArray(new IProject[] {}));

            d.open();
            if (d.getFirstResult() != null) {
                IProject project = (IProject) d.getFirstResult();
                selectProject(project);
            }
        });
        setButtonLayoutData(button);
    }

    @Override
    protected void restoreWidgetValues() {
        super.restoreWidgetValues();
        IDialogSettings settings = getDialogSettings();
        if (settings != null && fProjectText != null) {

            // Restore last selected project
            String projectName = settings.get(STORE_PROJECT_NAME_ID);
            if (projectName != null && !projectName.isEmpty()) {
                for (IProject project : fOpenedTmfProjects) {
                    if (project.getName().equals(projectName)) {
                        selectProject(project);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void saveWidgetValues() {
        super.saveWidgetValues();

        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            settings.put(STORE_PROJECT_NAME_ID, fTmfTraceFolder.getProject().getResource().getName());
        }
    }

    private void selectProject(IProject project) {
        fProjectText.setText(project.getName());
        fTmfTraceFolder = TmfProjectRegistry.getProject(project, true).getTracesFolder();
        updatePageCompletion();
    }

    @Override
    protected boolean determinePageCompletion() {
        return super.determinePageCompletion() && fTmfTraceFolder != null;
    }

    /**
     * Create the operation that will be responsible of creating the manifest
     * based on the file name.
     *
     * @param fileName the file name to generate the manifest from
     *
     * @return the operation that will extract the manifest
     */
    protected AbstractTracePackageOperation createExtractManifestOperation(String fileName) {
        return new TracePackageExtractManifestOperation(fileName);
    }

    @Override
    protected Object createElementViewerInput() {

        final AbstractTracePackageOperation op = createExtractManifestOperation(getFilePathValue());

        try {
            getContainer().run(true, true, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.ImportTracePackageWizardPage_ReadingPackage, 10);
                    op.run(monitor);
                    monitor.done();
                }

            });

            IStatus status = op.getStatus();
            if (status.getSeverity() == IStatus.ERROR) {
                handleErrorStatus(status);
            }
        } catch (InvocationTargetException e1) {
            handleError(Messages.TracePackageExtractManifestOperation_ErrorReadingManifest, e1);
        } catch (InterruptedException e1) {
            // Canceled
        }

        TracePackageElement[] resultElements = op.getResultElements();
        if (resultElements == null || resultElements.length == 0) {
            return null;
        }

        return resultElements;
    }

    @Override
    protected void createFilePathGroup(Composite parent, String label, int fileDialogStyle) {
        super.createFilePathGroup(parent, label, fileDialogStyle);

        Combo filePathCombo = getFilePathCombo();
        filePathCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateWithFilePathSelection();
            }
        });

        // User can type-in path and press return to validate
        filePathCombo.addTraverseListener(e -> {
            if (e.detail == SWT.TRAVERSE_RETURN) {
                e.doit = false;
                updateWithFilePathSelection();
            }
        });
    }

    @Override
    protected void updateWithFilePathSelection() {
        if (!isFilePathValid()) {
            setErrorMessage(Messages.ImportTracePackageWizardPage_ErrorFileNotFound);
            getElementViewer().setInput(null);
            return;
        }
        setErrorMessage(null);

        getContainer().getShell().getDisplay().asyncExec(() -> {
            CheckboxTreeViewer elementViewer = getElementViewer();
            Object elementViewerInput = createElementViewerInput();
            elementViewer.setInput(elementViewerInput);
            if (elementViewerInput != null) {
                elementViewer.expandToLevel(2);
                setAllChecked(elementViewer, false, true);
                fValidatedFilePath = getFilePathValue();
            }

            updatePageCompletion();
        });
    }

    private boolean isFilePathValid() {
        return new File(getFilePathValue()).exists();
    }

    /**
     * Finish the wizard page
     *
     * @return true on success
     */
    public boolean finish() {
        if (!checkForConflict()) {
            return false;
        }

        saveWidgetValues();

        Object input = getElementViewer().getInput();
        TracePackageElement[] traceElements = (TracePackageElement[]) input;
        final TracePackageImportOperation importOperation = new TracePackageImportOperation(fValidatedFilePath, traceElements, fTmfTraceFolder);

        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    importOperation.run(monitor);
                }
            });

            IStatus status = importOperation.getStatus();
            if (status.getSeverity() == IStatus.ERROR) {
                handleErrorStatus(status);
            }

        } catch (InvocationTargetException e) {
            handleError(Messages.ImportTracePackageWizardPage_ErrorOperation, e);
        } catch (InterruptedException e) {
        }

        return importOperation.getStatus().getSeverity() == IStatus.OK;
    }

    private boolean checkForConflict() {
        TracePackageElement[] packageElements = (TracePackageElement[]) getElementViewer().getInput();
        List<TracePackageExperimentElement> experimentElements = new ArrayList<>();
        List<TracePackageTraceElement> traceElements = new ArrayList<>();
        Map<TracePackageExperimentElement, List<TracePackageTraceElement>> experimentTracesMap = new HashMap<>();
        // List of experiment to rename
        List<TracePackageExperimentElement> experimentToRename = new ArrayList<>();
        // Conflict traces that are not in an experiment
        List<TracePackageTraceElement> unhandledTraces = new ArrayList<>();

        // Process package element to separate experiments and traces
        for (TracePackageElement packageElement : packageElements) {
            TracePackageTraceElement traceElement = (TracePackageTraceElement) packageElement;
            if (AbstractTracePackageOperation.isFilesChecked(traceElement)) {
                if (traceElement instanceof TracePackageExperimentElement) {
                    TracePackageExperimentElement experimentElement = (TracePackageExperimentElement) traceElement;
                    experimentElements.add(experimentElement);
                    if (experimentExists(experimentElement)) {
                        experimentToRename.add(experimentElement);
                    }
                } else {
                    traceElements.add(traceElement);
                }
            }
        }

        for (TracePackageTraceElement traceElement : traceElements) {
            processTrace(experimentElements, traceElement, experimentTracesMap, unhandledTraces);
        }

        boolean result = true;
        if (!experimentTracesMap.isEmpty() || !experimentToRename.isEmpty()) {
            result &= handleExperimentConflict(experimentTracesMap, experimentToRename);
        }

        if (!unhandledTraces.isEmpty()) {
            result &= handleTracesConflict(unhandledTraces);
        }

        return result;
    }

    private void processTrace(List<TracePackageExperimentElement> experimentElements, TracePackageTraceElement traceElement, Map<TracePackageExperimentElement, List<TracePackageTraceElement>> experimentTracesMap,
            List<TracePackageTraceElement> unhandledTraces) {
        boolean isAlone = traceExists(traceElement);
        for (TracePackageExperimentElement experimentElement : experimentElements) {
            List<String> tracesPath = experimentElement.getExpTraces();
            if (traceExists(traceElement) && tracesPath.contains(traceElement.getDestinationElementPath())) {
                isAlone = false;
                List<TracePackageTraceElement> traces = experimentTracesMap.get(experimentElement);
                if (traces == null) {
                    traces = new ArrayList<>();
                }
                traces.add(traceElement);
                experimentTracesMap.put(experimentElement, traces);
            }
        }

        if (isAlone) {
            unhandledTraces.add(traceElement);
        }
    }

    private boolean handleExperimentConflict(Map<TracePackageExperimentElement, List<TracePackageTraceElement>> experimentTracesMap, List<TracePackageExperimentElement> experimentToRename) {
        if (!experimentToRename.isEmpty() && experimentTracesMap.isEmpty()) {
            for (TracePackageExperimentElement experimentElement : experimentToRename) {
                int returnCode = promptForExperimentRename(experimentElement);
                // The return code is an index to a button in the dialog but the
                // 'X' button in the window corner is not considered a button
                // therefore it returns -1 and unfortunately, there is no
                // constant for that.
                if (returnCode < 0) {
                    return false;
                }
                final String[] response = new String[] { IDialogConstants.NO_LABEL, IDialogConstants.YES_LABEL };
                if (response[returnCode].equals(IDialogConstants.NO_LABEL)) {
                    uncheckExperimentElement(experimentElement, Collections.emptyList());
                } else if (response[returnCode].equals(IDialogConstants.YES_LABEL)) {
                    changeExperimentName(experimentElement);
                }
            }
        } else {
            for (Entry<TracePackageExperimentElement, List<TracePackageTraceElement>> experimentEntry : experimentTracesMap.entrySet()) {
                int returnCode = promptForExperimentOverwrite(experimentEntry, experimentToRename);
                // The return code is an index to a button in the dialog but the
                // 'X' button in the window corner is not considered a button
                // therefore it returns -1 and unfortunately, there is no
                // constant for that.
                if (returnCode < 0) {
                    return false;
                }
                final String[] response = new String[] { IDialogConstants.NO_LABEL, IDialogConstants.YES_LABEL };
                if (response[returnCode].equals(IDialogConstants.NO_LABEL)) {
                    uncheckExperimentElement(experimentEntry.getKey(), experimentEntry.getValue());
                }
            }
        }
        return true;
    }

    private void changeExperimentName(TracePackageExperimentElement experimentElement) {
        String currentName = experimentElement.getImportName();
        int nameSuffixIndex = 2;
        while (experimentExists(experimentElement)) {
            String newName = currentName + " (" + nameSuffixIndex + ')'; //$NON-NLS-1$
            experimentElement.setImportName(newName);
            nameSuffixIndex++;
        }
    }

    private boolean handleTracesConflict(List<TracePackageTraceElement> traceElements) {
        boolean noToAll = false;
        for (TracePackageTraceElement traceElement : traceElements) {
            if (noToAll) {
                uncheckTraceElement(traceElement);
            } else {
                int returnCode = promptForTraceOverwrite(traceElement);
                // The return code is an index to a button in the dialog but the
                // 'X' button in the window corner is not considered a button
                // therefore it returns -1 and unfortunately, there is no
                // constant for that.
                if (returnCode < 0) {
                    return false;
                }

                final String[] response = new String[] { IDialogConstants.NO_TO_ALL_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.YES_TO_ALL_LABEL, IDialogConstants.YES_LABEL };
                if (response[returnCode].equals(IDialogConstants.NO_TO_ALL_LABEL)) {
                    noToAll = true;
                    uncheckTraceElement(traceElement);
                } else if (response[returnCode].equals(IDialogConstants.NO_LABEL)) {
                    uncheckTraceElement(traceElement);
                }
            }
        }

        return true;
    }

    private static void uncheckTraceElement(TracePackageTraceElement traceElement) {
        for (TracePackageElement e : traceElement.getChildren()) {
            if (e instanceof TracePackageFilesElement) {
                ((TracePackageFilesElement) e).setChecked(false);
            }
        }
    }

    private static void uncheckExperimentElement(TracePackageExperimentElement experimentElement, List<TracePackageTraceElement> expTraceElements) {
        for (TracePackageElement e : experimentElement.getChildren()) {
            if (e instanceof TracePackageFilesElement) {
                ((TracePackageFilesElement) e).setChecked(false);
            }
        }

        for (TracePackageTraceElement traceElement : expTraceElements) {
            uncheckTraceElement(traceElement);
        }
    }

    private boolean experimentExists(TracePackageExperimentElement experimentElement) {
        TmfExperimentFolder experimentsFolder = fTmfTraceFolder.getProject().getExperimentsFolder();
        return experimentsFolder != null && experimentsFolder.getChild(experimentElement.getImportName()) != null;
    }

    private boolean traceExists(TracePackageTraceElement traceElement) {
        IResource traceRes = fTmfTraceFolder.getResource().findMember(traceElement.getDestinationElementPath());
        return traceRes != null;
    }

    private int promptForTraceOverwrite(TracePackageTraceElement packageElement) {
        String name = packageElement.getDestinationElementPath();
        final MessageDialog dialog = new MessageDialog(getContainer().getShell(),
                Messages.ImportTracePackageWizardPage_AlreadyExistsTitle,
                null,
                MessageFormat.format(Messages.ImportTracePackageWizardPage_TraceAlreadyExists, name),
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.NO_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.YES_LABEL },
                3) {
            @Override
            protected int getShellStyle() {
                return super.getShellStyle() | SWT.SHEET;
            }
        };
        return dialog.open();
    }

    private int promptForExperimentRename(TracePackageExperimentElement experimentElement) {
        String dialogMessage = MessageFormat.format(Messages.ImportTracePackageWizardPage_ExperimentAlreadyExists, experimentElement.getImportName());
        final MessageDialog dialog = new MessageDialog(getContainer().getShell(),
                Messages.ImportTracePackageWizardPage_AlreadyExistsTitle,
                null,
                dialogMessage,
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.YES_LABEL },
                3) {
            @Override
            protected int getShellStyle() {
                return super.getShellStyle() | SWT.SHEET;
            }
        };
        return dialog.open();
    }

    private int promptForExperimentOverwrite(Entry<TracePackageExperimentElement, List<TracePackageTraceElement>> experimentEntry, List<TracePackageExperimentElement> experimentToRename) {
        List<TracePackageTraceElement> traceElements = experimentEntry.getValue();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < traceElements.size(); i++) {
            builder.append(traceElements.get(i).getImportName());
            if (i < traceElements.size() - 1) {
                builder.append(", "); //$NON-NLS-1$
            }

        }
        String dialogMessage;
        if (experimentToRename.contains(experimentEntry.getKey())) {
            dialogMessage = MessageFormat.format(Messages.ImportTracePackageWizardPage_ExperimentAndTraceAlreadyExist, experimentEntry.getKey().getImportName(), builder.toString());
        } else {
            dialogMessage = MessageFormat.format(Messages.ImportTracePackageWizardPage_TraceFromExperimentAlreadyExist, experimentEntry.getKey().getImportName(), builder.toString());
        }
        final MessageDialog dialog = new MessageDialog(getContainer().getShell(),
                Messages.ImportTracePackageWizardPage_AlreadyExistsTitle,
                null,
                dialogMessage,
                MessageDialog.QUESTION, new String[] {
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.YES_LABEL },
                3) {
            @Override
            protected int getShellStyle() {
                return super.getShellStyle() | SWT.SHEET;
            }
        };
        return dialog.open();
    }
}
