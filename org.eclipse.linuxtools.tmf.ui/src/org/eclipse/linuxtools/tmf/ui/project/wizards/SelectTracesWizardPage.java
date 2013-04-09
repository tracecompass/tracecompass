/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Geneviève Bastien - Moved the add and remove code to the experiment class
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceFolderContentProvider;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceFolderLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

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
    private final TmfExperimentElement fExperiment;
    private Map<String, TmfTraceElement> fPreviousTraces;
    private CheckboxTableViewer fCheckboxTableViewer;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param project The project model element.
     * @param experiment The experiment model experiment.
     */
    protected SelectTracesWizardPage(TmfProjectElement project, TmfExperimentElement experiment) {
        super(""); //$NON-NLS-1$
        setTitle(Messages.SelectTracesWizardPage_WindowTitle);
        setDescription(Messages.SelectTracesWizardPage_Description);
        fProject = project;
        fExperiment = experiment;
    }

    // ------------------------------------------------------------------------
    // Dialog
    // ------------------------------------------------------------------------

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new FormLayout());
        setControl(container);

        fCheckboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
        fCheckboxTableViewer.setContentProvider(new TraceFolderContentProvider());
        fCheckboxTableViewer.setLabelProvider(new TraceFolderLabelProvider());

        final Table table = fCheckboxTableViewer.getTable();
        final FormData formData = new FormData();
        formData.bottom = new FormAttachment(100, 0);
        formData.right = new FormAttachment(100, 0);
        formData.top = new FormAttachment(0, 0);
        formData.left = new FormAttachment(0, 0);
        table.setLayoutData(formData);
        table.setHeaderVisible(true);

        final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        tableColumn.setWidth(200);
        tableColumn.setText(Messages.SelectTracesWizardPage_TraceColumnHeader);

        // Get the list of traces already part of the experiment
        fPreviousTraces = new HashMap<String, TmfTraceElement>();
        for (ITmfProjectModelElement child : fExperiment.getChildren()) {
            if (child instanceof TmfTraceElement) {
                TmfTraceElement trace = (TmfTraceElement) child;
                String name = trace.getResource().getName();
                fPreviousTraces.put(name, trace);
            }
        }

        // Populate the list of traces to choose from
        Set<String> keys = fPreviousTraces.keySet();
        TmfTraceFolder traceFolder = fProject.getTracesFolder();
        fCheckboxTableViewer.setInput(traceFolder);

        // Set the checkbox for the traces already included
        int index = 0;
        Object element = fCheckboxTableViewer.getElementAt(index++);
        while (element != null) {
            if (element instanceof TmfTraceElement) {
                TmfTraceElement trace = (TmfTraceElement) element;
                if (keys.contains(trace.getResource().getName())) {
                    fCheckboxTableViewer.setChecked(element, true);
                }
            }
            element = fCheckboxTableViewer.getElementAt(index++);
        }
    }

    /**
     * Method to finalize the select operation.
     * @return <code>true</code> if successful else <code>false</code>
     */
    public boolean performFinish() {

        IFolder experiment = fExperiment.getResource();

        // Add the selected traces to the experiment
        Set<String> keys = fPreviousTraces.keySet();
        TmfTraceElement[] traces = getSelection();
        for (TmfTraceElement trace : traces) {
            String name = trace.getResource().getName();
            if (keys.contains(name)) {
                fPreviousTraces.remove(name);
            } else {
                fExperiment.addTrace(trace);
            }
        }

        // Remove traces that were unchecked (thus left in fPreviousTraces)
        keys = fPreviousTraces.keySet();
        for (String key : keys) {
            try {
                fExperiment.removeTrace(fPreviousTraces.get(key));
            } catch (CoreException e) {
                Activator.getDefault().logError("Error selecting traces for experiment " + experiment.getName(), e); //$NON-NLS-1$
            }
        }
        fProject.refresh();

        return true;
    }

    /**
     * Get the list of selected traces
     */
    private TmfTraceElement[] getSelection() {
        Vector<TmfTraceElement> traces = new Vector<TmfTraceElement>();
        Object[] selection = fCheckboxTableViewer.getCheckedElements();
        for (Object sel : selection) {
            if (sel instanceof TmfTraceElement) {
                traces.add((TmfTraceElement) sel);
            }
        }
        TmfTraceElement[] result = new TmfTraceElement[traces.size()];
        traces.toArray(result);
        return result;
    }

}
