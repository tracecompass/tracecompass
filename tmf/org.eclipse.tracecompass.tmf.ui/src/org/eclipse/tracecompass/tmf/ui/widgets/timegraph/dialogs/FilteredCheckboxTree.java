/*******************************************************************************
 * Copyright (c) 2014 Inria
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Generoso Pagano, Inria - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A <code>FilteredTree</code> wrapping a <code>CheckboxTreeViewer</code>.
 *
 * This tree keeps the check state of the nodes in sync, regardless of the fact
 * that a node is filtered or not. This way, even if an node is filtered (not
 * visible), the caller can get and set the check state.
 *
 * Note that all the "uncheck" operations act only on what is not filtered and
 * what is child of something not filtered (even if such a child is filtered).
 * On the contrary, all the "check" operations act only on what is not filtered.
 *
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * @author "Mikael Ferland <mikael.ferland@ericsson.com>"
 */
public class FilteredCheckboxTree extends FilteredTree implements ICheckable {

    /**
     * Set containing only the tree items that are checked
     * @since 3.1
     */
    protected Set<Object> fCheckedObjects = new HashSet<>();

    /**
     * Handle to the tree viewer
     */
    private CheckboxTreeViewer fCheckboxTreeViewer;

    /**
     * Create a new instance of the receiver.
     *
     * @param parent
     *            the parent <code>Composite</code>
     * @param treeStyle
     *            the style bits for the <code>Tree</code>
     * @param filter
     *            the filter to be used
     * @param useNewLook
     *            <code>true</code> if the new <code>FilteredTree</code> look
     *            should be used
     * @deprecated use {@link #FilteredCheckboxTree(Composite, int, PatternFilter, boolean, boolean)}
     *             Will only be removed after Eclipse 2019-06 (4.12) support is dropped.
     */
    @Deprecated
    public FilteredCheckboxTree(Composite parent, int treeStyle, PatternFilter filter,
            boolean useNewLook) {
        this(parent, treeStyle, filter, useNewLook, false);
    }

    /**
     * Create a new instance of the receiver.
     *
     * @param parent
     *            the parent <code>Composite</code>
     * @param treeStyle
     *            the style bits for the <code>Tree</code>
     * @param filter
     *            the filter to be used
     * @param useNewLook
     *            <code>true</code> if the new <code>FilteredTree</code> look
     *            should be used
     * @param useFastHashLookup true, if tree should use fast hash lookup, else false
     * @since 5.3
     */
    public FilteredCheckboxTree(Composite parent, int treeStyle, PatternFilter filter,
            boolean useNewLook, boolean useFastHashLookup) {
        /*
         *  Keep call to deprecated super constructor until support
         *  for Eclipse 2019-06 (4.12) is dropped.
         */
        super(parent, treeStyle, filter, useNewLook);
        TreeViewer viewer = getViewer();
        if (viewer != null) {
            viewer.setUseHashlookup(useFastHashLookup);
        }
    }

    @Override
    protected TreeViewer doCreateTreeViewer(Composite parentComposite, int style) {
        fCheckboxTreeViewer = new CheckboxTreeViewer(parentComposite, style);
        fCheckboxTreeViewer.setUseHashlookup(true);
        fCheckboxTreeViewer.addCheckStateListener(event -> {
            if (event.getChecked()) {
                fCheckedObjects.add(event.getElement());
            } else {
                fCheckedObjects.remove(event.getElement());
            }
        });
        return fCheckboxTreeViewer;
    }

    @Override
    protected WorkbenchJob doCreateRefreshJob() {
        WorkbenchJob job = super.doCreateRefreshJob();
        return new WorkbenchJob("Refresh Filter") {//$NON-NLS-1$
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (fCheckboxTreeViewer.getTree().isDisposed()) {
                    return Status.CANCEL_STATUS;
                }
                // Save expanded elements before and after the filtering
                Set<Object> expandedElements = new HashSet<>(Arrays.asList(fCheckboxTreeViewer.getExpandedElements()));
                job.runInUIThread(monitor);
                expandedElements.addAll(Arrays.asList(fCheckboxTreeViewer.getExpandedElements()));
                fCheckboxTreeViewer.getTree().setRedraw(false);
                // Expand all to be able to store all checked elements
                fCheckboxTreeViewer.expandAll();
                fCheckboxTreeViewer.setCheckedElements(getCheckedElements());
                // Collapse all can change selection
                ISelection selection = fCheckboxTreeViewer.getSelection();
                fCheckboxTreeViewer.collapseAll();
                fCheckboxTreeViewer.getTree().setRedraw(true);
                // Expand tree according to the saved expanded elements
                fCheckboxTreeViewer.setExpandedElements(expandedElements.toArray());
                // Restore the selection
                fCheckboxTreeViewer.setSelection(selection);
                return Status.OK_STATUS;
            }
        };
    }

    @Override
    public boolean getChecked(Object element) {
        return fCheckedObjects.contains(element);
    }

    @Override
    public boolean setChecked(Object element, boolean state) {
        boolean checkable = fCheckboxTreeViewer.setChecked(element, state);
        if (!state) {
            fCheckedObjects.remove(element);
        } else if (checkable) {
            fCheckedObjects.add(element);
        }
        return checkable;
    }

    @Override
    public void addCheckStateListener(ICheckStateListener listener) {
        fCheckboxTreeViewer.addCheckStateListener(listener);
    }

    @Override
    public void removeCheckStateListener(ICheckStateListener listener) {
        fCheckboxTreeViewer.addCheckStateListener(listener);
    }

    /**
     * @return the handle to the tree viewer
     * @since 3.1
     */
    public CheckboxTreeViewer getCheckboxTreeViewer() {
        return fCheckboxTreeViewer;
    }

    /**
     * Returns all the checked elements of this tree, either visible or not.
     *
     * @return an array containing all the checked elements
     */
    public Object[] getCheckedElements() {
        return fCheckedObjects.toArray();
    }

    /**
     * Checks all the passed elements and unchecks all the other.
     *
     * @param elements
     *            the elements to check
     */
    public void setCheckedElements(Object[] elements) {
        fCheckedObjects = new HashSet<>();
        for (Object element : elements) {
            fCheckedObjects.add(element);
        }
        fCheckboxTreeViewer.setCheckedElements(elements);
    }

    /**
     * Sets the check state for the given element and its children in this
     * viewer. The unchecked state is always set, while the checked state is set
     * only on visible elements.
     *
     * @param element
     *            the element
     * @param state
     *            the check state to set
     * @return <code>true</code> if the check state could be set, and
     *         <code>false</code> otherwise
     */
    public boolean setSubtreeChecked(Object element, boolean state) {
        checkSubtree(element, state);
        return fCheckboxTreeViewer.setSubtreeChecked(element, state);
    }

    /**
     * Recursively sets the check state on an element and its children, using
     * the politic specified in {@link #setSubtreeChecked(Object, boolean)}
     * documentation.
     *
     * @param element
     *            the element
     * @param state
     *            the check state to set
     * @since 3.1
     */
    protected void checkSubtree(Object element, boolean state) {
        if (!state || (fCheckboxTreeViewer.testFindItem(element) != null)) {
            if (state) {
                fCheckedObjects.add(element);
            } else {
                fCheckedObjects.remove(element);
            }
            for (Object o : ((ITreeContentProvider) fCheckboxTreeViewer.getContentProvider()).getChildren(element)) {
                checkSubtree(o, state);
            }
        }
    }

}
