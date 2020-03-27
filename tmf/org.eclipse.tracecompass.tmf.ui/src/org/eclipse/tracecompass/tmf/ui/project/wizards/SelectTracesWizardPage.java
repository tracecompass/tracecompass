/*******************************************************************************
 * Copyright (c) 2009, 2015, 2017 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Geneviève Bastien - Moved the add and remove code to the experiment class
 *   Patrick Tasse - Add support for folder elements
 *   Marc-Andre Laperle - Convert to tree structure and add select/deselect all
 *   Simon Delisle - Add time range selection support
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.wizards;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.SelectTracesOperation;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfNavigatorContentProvider;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfNavigatorLabelProvider;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Implementation of a wizard page for selecting trace for an experiment.
 * <p>
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class SelectTracesWizardPage extends WizardPage {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final TmfProjectElement fProject;
    private final @NonNull TmfExperimentElement fExperiment;
    private @NonNull Map<String, TmfTraceElement> fPreviousTraces;
    private CheckboxTreeViewer fCheckboxTreeViewer;
    private TmfNavigatorContentProvider fContentProvider;
    private TmfNavigatorLabelProvider fLabelProvider;

    private Button fTimeRangeSelectionButton;
    private Text fStartTimeRange;
    private Text fEndTimeRange;
    private ITmfTimestamp fStartTimestamp;
    private ITmfTimestamp fEndTimestamp;
    private static final TmfTimestampFormat TIMESTAMP_FORMAT = new TmfTimestampFormat("yyyy-MM-dd HH:mm:ss.SSS SSS SSS"); //$NON-NLS-1$

    private static final int COLUMN_WIDTH = 200;
    private static final int BUTTON_SPACING = 4;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param project
     *            The project model element.
     * @param experiment
     *            The experiment model experiment.
     */
    protected SelectTracesWizardPage(TmfProjectElement project, @NonNull TmfExperimentElement experiment) {
        super(""); //$NON-NLS-1$
        setTitle(Messages.SelectTracesWizardPage_WindowTitle);
        setDescription(Messages.SelectTracesWizardPage_Description);
        fProject = project;
        fExperiment = experiment;
        fPreviousTraces = new HashMap<>();
    }

    // ------------------------------------------------------------------------
    // Dialog
    // ------------------------------------------------------------------------

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout(2, false));
        setControl(container);

        new FilteredTree(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(), true) {
            @Override
            protected TreeViewer doCreateTreeViewer(Composite aparent, int style) {
                return SelectTracesWizardPage.this.doCreateTreeViewer(aparent);
            }
        };

        Composite buttonComposite = new Composite(container, SWT.NONE);
        FillLayout layout = new FillLayout(SWT.VERTICAL);
        layout.spacing = BUTTON_SPACING;
        buttonComposite.setLayout(layout);
        GridData gd = new GridData();
        gd.verticalAlignment = SWT.CENTER;
        buttonComposite.setLayoutData(gd);

        Button selectAllButton = new Button(buttonComposite, SWT.PUSH);
        selectAllButton.setText(org.eclipse.tracecompass.internal.tmf.ui.project.dialogs.Messages.Dialog_SelectAll);
        selectAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setAllChecked(true);
            }
        });

        Button deselectAllButton = new Button(buttonComposite, SWT.PUSH);
        deselectAllButton.setText(org.eclipse.tracecompass.internal.tmf.ui.project.dialogs.Messages.Dialog_DeselectAll);
        deselectAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setAllChecked(false);
            }
        });

        createTimeRangeSelection(container);
    }

    private void createTimeRangeSelection(Composite parent) {
        Composite timeRangeSelectionComposite = new Composite(parent, SWT.NONE);
        GridLayout compositeLayout = new GridLayout(2, false);
        compositeLayout.marginWidth = 0;
        timeRangeSelectionComposite.setLayout(compositeLayout);
        timeRangeSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        fTimeRangeSelectionButton = new Button(timeRangeSelectionComposite, SWT.CHECK);
        fTimeRangeSelectionButton.setText(Messages.SelectTracesWizardPage_TimeRangeOptionButton + " (" + TIMESTAMP_FORMAT.toPattern() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        fTimeRangeSelectionButton.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());

        Label startTimeLabel = new Label(timeRangeSelectionComposite, SWT.NONE);
        startTimeLabel.setText(Messages.SelectTracesWizardPage_StartTime);
        startTimeLabel.setEnabled(false);
        fStartTimeRange = new Text(timeRangeSelectionComposite, SWT.BORDER);
        fStartTimeRange.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        fStartTimeRange.setEnabled(false);

        Label endTimeLabel = new Label(timeRangeSelectionComposite, SWT.NONE);
        endTimeLabel.setText(Messages.SelectTracesWizardPage_EndTime);
        endTimeLabel.setEnabled(false);
        fEndTimeRange = new Text(timeRangeSelectionComposite, SWT.BORDER);
        fEndTimeRange.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        fEndTimeRange.setEnabled(false);

        fTimeRangeSelectionButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                startTimeLabel.setEnabled(fTimeRangeSelectionButton.getSelection());
                fStartTimeRange.setEnabled(fTimeRangeSelectionButton.getSelection());
                endTimeLabel.setEnabled(fTimeRangeSelectionButton.getSelection());
                fEndTimeRange.setEnabled(fTimeRangeSelectionButton.getSelection());
            }
        });

        FocusListener focusListener = new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                validateTimeRange();
            }

            @Override
            public void focusGained(FocusEvent e) {
                setErrorMessage(null);
                setPageComplete(true);
            }
        };
        fStartTimeRange.addFocusListener(focusListener);
        fEndTimeRange.addFocusListener(focusListener);
    }

    private TreeViewer doCreateTreeViewer(Composite parent) {
        fCheckboxTreeViewer = new CheckboxTreeViewer(parent, SWT.BORDER);
        fContentProvider = new TmfNavigatorContentProvider() {

            @Override
            public Object[] getElements(Object inputElement) {
                return getChildren(inputElement);
            }

            @Override
            public synchronized Object[] getChildren(Object parentElement) {
                // We only care about the content of trace folders
                if (parentElement instanceof TmfTraceFolder) {
                    Object[] children = super.getChildren(parentElement);
                    List<ITmfProjectModelElement> filteredChildren = new ArrayList<>();
                    for (Object child : children) {
                        if (child instanceof TmfTraceElement) {
                            TmfTraceElement traceElement = (TmfTraceElement) child;
                            String traceType = traceElement.getTraceType();
                            if (traceType != null && TmfTraceType.getTraceType(traceType) != null) {
                                filteredChildren.add(traceElement);
                            }
                        } else if (child instanceof TmfTraceFolder) {
                            filteredChildren.add((TmfTraceFolder) child);
                        }
                    }
                    return filteredChildren.toArray();
                }
                return null;
            }

            @Override
            public boolean hasChildren(Object element) {
                Object[] children = getChildren(element);
                return children != null && children.length > 0;
            }
        };
        fCheckboxTreeViewer.setContentProvider(fContentProvider);
        fLabelProvider = new TmfNavigatorLabelProvider();
        fCheckboxTreeViewer.setLabelProvider(fLabelProvider);
        fCheckboxTreeViewer.setComparator(new ViewerComparator());

        final Tree tree = fCheckboxTreeViewer.getTree();
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        tree.setLayoutData(gd);
        tree.setHeaderVisible(true);

        final TreeViewerColumn column = new TreeViewerColumn(fCheckboxTreeViewer, SWT.NONE);
        column.getColumn().setWidth(COLUMN_WIDTH);
        column.getColumn().setText(Messages.SelectTracesWizardPage_TraceColumnHeader);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return fLabelProvider.getText(element);
            }

            @Override
            public Image getImage(Object element) {
                return fLabelProvider.getImage(element);
            }
        });

        // Get the list of traces already part of the experiment
        fPreviousTraces.clear();
        for (ITmfProjectModelElement child : fExperiment.getChildren()) {
            if (child instanceof TmfTraceElement) {
                TmfTraceElement trace = (TmfTraceElement) child;
                String name = trace.getElementPath();
                fPreviousTraces.put(name, trace);
            }
        }

        // Populate the list of traces to choose from
        Set<String> keys = fPreviousTraces.keySet();
        TmfTraceFolder traceFolder = fProject.getTracesFolder();
        fCheckboxTreeViewer.setInput(traceFolder);

        // Set the checkbox for the traces already included
        setCheckedAlreadyIncludedTraces(keys, fContentProvider.getElements(fCheckboxTreeViewer.getInput()));

        fCheckboxTreeViewer.addCheckStateListener(event -> {
            Object element = event.getElement();
            setSubtreeChecked(element, event.getChecked());
            maintainCheckIntegrity(element);
        });
        fCheckboxTreeViewer.setUseHashlookup(true);
        return fCheckboxTreeViewer;
    }

    private void maintainCheckIntegrity(final Object element) {
        Object parentElement = fContentProvider.getParent(element);
        boolean allChecked = true;
        boolean oneChecked = false;
        boolean oneGrayed = false;
        if (parentElement != null) {
            if (fContentProvider.getChildren(parentElement) != null) {
                for (Object child : fContentProvider.getChildren(parentElement)) {
                    boolean checked = fCheckboxTreeViewer.getChecked(child);
                    oneChecked |= checked;
                    allChecked &= checked;
                    oneGrayed |= fCheckboxTreeViewer.getGrayed(child);
                }
            }
            if (oneGrayed || oneChecked && !allChecked) {
                fCheckboxTreeViewer.setGrayChecked(parentElement, true);
            } else {
                fCheckboxTreeViewer.setGrayed(parentElement, false);
                fCheckboxTreeViewer.setChecked(parentElement, allChecked);
            }
            maintainCheckIntegrity(parentElement);
        }
    }

    private void setCheckedAlreadyIncludedTraces(Set<String> alreadyIncludedTraces, Object[] elements) {
        for (Object element : elements) {
            if (element instanceof TmfTraceElement) {
                TmfTraceElement trace = (TmfTraceElement) element;
                String elementPath = trace.getElementPath();
                if (alreadyIncludedTraces.contains(elementPath)) {
                    fCheckboxTreeViewer.setChecked(element, true);
                    maintainCheckIntegrity(element);
                }
            }
            Object[] children = fContentProvider.getChildren(element);
            if (children != null) {
                setCheckedAlreadyIncludedTraces(alreadyIncludedTraces, children);
            }
        }
    }

    /**
     * Sets all items in the element viewer to be checked or unchecked
     *
     * @param checked
     *            whether or not items should be checked
     */
    private void setAllChecked(boolean checked) {
        for (Object element : fContentProvider.getChildren(fCheckboxTreeViewer.getInput())) {
            setSubtreeChecked(element, checked);
        }
    }

    /**
     * A version of setSubtreeChecked that also handles the grayed state
     *
     * @param element
     *            the element
     * @param checked
     *            true if the item should be checked, and false if it should be
     *            unchecked
     */
    private void setSubtreeChecked(Object element, boolean checked) {
        fCheckboxTreeViewer.setChecked(element, checked);
        if (checked) {
            fCheckboxTreeViewer.setGrayed(element, false);
        }
        Object[] children = fContentProvider.getChildren(element);
        if (children != null) {
            for (Object child : children) {
                setSubtreeChecked(child, checked);
            }
        }
    }

    /**
     * Method to finalize the select operation.
     *
     * @return <code>true</code> if successful else <code>false</code>
     */
    public boolean performFinish() {
        SelectTracesOperation operation;
        if (fTimeRangeSelectionButton.getSelection() && validateTimeRange()) {
            operation = new SelectTracesOperation(fExperiment, getSelection(), fPreviousTraces, fStartTimestamp, fEndTimestamp);
        } else {
            operation = new SelectTracesOperation(fExperiment, getSelection(), fPreviousTraces);
        }

        IStatus status = Status.OK_STATUS;
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    // Wrapper to have only one resource changed event at the
                    // end of the operation.
                    IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable() {
                        @Override
                        public void run(IProgressMonitor pm) throws CoreException {
                            operation.run(pm);
                        }
                    };

                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    try {
                        workspace.run(workspaceRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, monitor);
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.done();
                    }
                }
            });

            status = operation.getStatus();
        } catch (InvocationTargetException e) {
            status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SelectTracesWizardPage_SelectionError, e);
        } catch (InterruptedException e) {
            status = Status.CANCEL_STATUS;
            Thread.currentThread().interrupt();
        }
        if (!status.isOK()) {
            if (status.getSeverity() == IStatus.CANCEL) {
                setMessage(Messages.SelectTracesWizardPage_SelectionOperationCancelled);
                setErrorMessage(null);
            } else {
                if (status.getException() != null) {
                    MessageDialog.open(MessageDialog.ERROR, getContainer().getShell(),
                            Messages.SelectTracesWizardPage_InternalErrorTitle, status.getMessage() + ": " + status.getException(), SWT.SHEET); //$NON-NLS-1$
                }
                setMessage(null);
                setErrorMessage(Messages.SelectTracesWizardPage_SelectionError);
            }
            return false;
        }
        setErrorMessage(null);

        return true;
    }

    /**
     * Validate time range and set wizard completion accordingly
     *
     * @return True if the time range is valid
     */
    private boolean validateTimeRange() {
        boolean validTimeRange = parseTimeRange() && fStartTimestamp.compareTo(fEndTimestamp) <= 0;

        if (validTimeRange) {
            setErrorMessage(null);
        } else {
            setErrorMessage(Messages.SelectTracesWizardPage_TimeRangeErrorMessage);
        }

        setPageComplete(validTimeRange);
        return validTimeRange;
    }

    /**
     * Parse the user inputs and create timestamps.
     *
     * @return True if we are able to parse the inputs, false otherwise
     */
    private boolean parseTimeRange() {
        try {
            long startTimeValue = TIMESTAMP_FORMAT.parseValue(fStartTimeRange.getText());
            long endTimeValue = TIMESTAMP_FORMAT.parseValue(fEndTimeRange.getText());
            fStartTimestamp = TmfTimestamp.fromNanos(startTimeValue);
            fEndTimestamp = TmfTimestamp.fromNanos(endTimeValue);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Get the list of selected traces
     */
    private @NonNull TmfTraceElement[] getSelection() {
        List<TmfTraceElement> traces = new ArrayList<>();
        Object[] selection = fCheckboxTreeViewer.getCheckedElements();
        for (Object sel : selection) {
            if (sel instanceof TmfTraceElement) {
                traces.add((TmfTraceElement) sel);
            }
        }
        return traces.toArray(new @NonNull TmfTraceElement[0]);
    }

}
