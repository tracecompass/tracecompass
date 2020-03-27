/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Cédric Biancheri - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfNavigatorContentProvider;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfNavigatorLabelProvider;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Implementation of a wizard page for selecting a root node for
 * synchronization.
 *
 * @author Cedric Biancheri
 * @since 2.0
 *
 */
public class SelectRootNodeWizardPage extends WizardPage {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final TmfExperimentElement fExperiment;
    private CheckboxTreeViewer fCheckboxTreeViewer;
    private TmfNavigatorContentProvider fContentProvider;
    private TmfNavigatorLabelProvider fLabelProvider;
    private TmfTraceElement rootNode;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * @param experiment
     *            The experiment where the synchronization will be made.
     */
    protected SelectRootNodeWizardPage(TmfExperimentElement experiment) {
        super(""); //$NON-NLS-1$
        fExperiment = experiment;
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout(2, false));
        setControl(container);
        setTitle(Messages.SelectRootNodeWizardPage_WindowTitle);
        setDescription(Messages.SelectRootNodeWizardPage_Description);

        new FilteredTree(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(), true) {
            @Override
            protected TreeViewer doCreateTreeViewer(Composite aparent, int style) {
                return SelectRootNodeWizardPage.this.doCreateTreeViewer(aparent);
            }
        };
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
                if (parentElement instanceof TmfExperimentElement) {
                    return ((TmfExperimentElement) parentElement).getTraces().toArray();
                }
                return null;
            }

            @Override
            public boolean hasChildren(Object element) {
                if (element instanceof TmfExperimentElement) {
                    return !(((TmfExperimentElement) element).getTraces().isEmpty());
                }
                return false;
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
        column.getColumn().setText(Messages.SelectRootNodeWizardPage_TraceColumnHeader);
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

        // Populate the list of traces from the experiment
        fCheckboxTreeViewer.setInput(fExperiment);
        column.getColumn().pack();

        fCheckboxTreeViewer.addCheckStateListener(event -> {
            Object element = event.getElement();
            // Uncheck all elements
            for (Object checkedElement : fCheckboxTreeViewer.getCheckedElements()) {
                fCheckboxTreeViewer.setChecked(checkedElement, false);
            }
            fCheckboxTreeViewer.setChecked(element, event.getChecked());
            setPageComplete(event.getChecked());
        });

        setPageComplete(true);
        // Checks the first element by default
        fCheckboxTreeViewer.getTree().getItem(0).setChecked(true);
        fCheckboxTreeViewer.setUseHashlookup(true);
        return fCheckboxTreeViewer;
    }

    /**
     * Method to finalize the select operation.
     *
     * @return <code>true</code> if successful. Should always be successful.
     */
    public boolean performFinish() {

        TmfTraceElement[] selection = getSelection();

        if (selection.length > 0 && selection[0] != null) {
            setRootNode(selection[0]);
        }

        return true;
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

    /**
     * Gets the root node.
     *
     * @return The root node
     */
    public TmfTraceElement getRootNode() {
        return rootNode;
    }

    /**
     * Sets the root node
     *
     * @param rootNode
     *            The root node
     */
    private void setRootNode(TmfTraceElement rootNode) {
        this.rootNode = rootNode;
    }

}
