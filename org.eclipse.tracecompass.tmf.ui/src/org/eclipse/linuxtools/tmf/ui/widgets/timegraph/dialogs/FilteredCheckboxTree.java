/*******************************************************************************
 * Copyright (c) 2014 Inria
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Generoso Pagano, Inria - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.dialogs;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
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
 * @since 3.2
 */
public class FilteredCheckboxTree extends FilteredTree implements ICheckable {

    /**
     * Set containing only the tree items that are checked
     */
    private Set<Object> fObjects = new HashSet<>();

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
     */
    public FilteredCheckboxTree(Composite parent, int treeStyle, PatternFilter filter,
            boolean useNewLook) {
        super(parent, treeStyle, filter, useNewLook);
    }

    @Override
    protected TreeViewer doCreateTreeViewer(Composite parentComposite, int style) {
        fCheckboxTreeViewer = new CheckboxTreeViewer(parentComposite, style);
        fCheckboxTreeViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (event.getChecked()) {
                    fObjects.add(event.getElement());
                } else {
                    fObjects.remove(event.getElement());
                }
            }
        });
        return fCheckboxTreeViewer;
    }

    @Override
    protected WorkbenchJob doCreateRefreshJob() {
        WorkbenchJob job = super.doCreateRefreshJob();
        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                fCheckboxTreeViewer.expandAll();
                fCheckboxTreeViewer.setCheckedElements(getCheckedElements());
            }
        });
        return job;
    }

    @Override
    public boolean getChecked(Object element) {
        return fObjects.contains(element);
    }

    @Override
    public boolean setChecked(Object element, boolean state) {
        boolean checkable = fCheckboxTreeViewer.setChecked(element, state);
        if (!state) {
            fObjects.remove(element);
        } else if (checkable) {
            fObjects.add(element);
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
     * Returns all the checked elements of this tree, either visible or not.
     *
     * @return an array containing all the checked elements
     */
    public Object[] getCheckedElements() {
        return fObjects.toArray();
    }

    /**
     * Checks all the passed elements and unchecks all the other.
     *
     * @param elements
     *            the elements to check
     */
    public void setCheckedElements(Object[] elements) {
        fObjects = new HashSet<>();
        for (Object element : elements) {
            fObjects.add(element);
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
     */
    private void checkSubtree(Object element, boolean state) {
        if (!state || (fCheckboxTreeViewer.testFindItem(element) != null)) {
            if (state) {
                fObjects.add(element);
            } else {
                fObjects.remove(element);
            }
            for (Object o : ((ITreeContentProvider) fCheckboxTreeViewer.getContentProvider()).getChildren(element)) {
                checkSubtree(o, state);
            }
        }
    }

}
