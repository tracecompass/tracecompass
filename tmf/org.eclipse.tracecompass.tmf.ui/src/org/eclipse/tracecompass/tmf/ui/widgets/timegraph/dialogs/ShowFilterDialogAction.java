/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * Action to show the time graph filter dialog and add a filter to hide the
 * unselected elements in the time graph viewer. The filter is removed if all
 * elements are selected.
 *
 * @since 1.2
 */
public class ShowFilterDialogAction extends Action {

    /** The filter dialog */
    private final TimeGraphFilterDialog fFilterDialog;

    /** The time graph viewer */
    private final TimeGraphViewer fTimeGraphViewer;

    /**
     * This filter simply keeps a list of elements that should be filtered out.
     * All the other elements will be shown.
     * By default and when the list is set to null, all elements are shown.
     */
    private class RawViewerFilter extends ViewerFilter {

        private List<? extends ITimeGraphEntry> fFiltered = null;

        public RawViewerFilter(List<? extends ITimeGraphEntry> filteredElements) {
            fFiltered = filteredElements;
        }

        public void setFiltered(List<? extends ITimeGraphEntry> objects) {
            fFiltered = objects;
        }

        public List<? extends ITimeGraphEntry> getFiltered() {
            return fFiltered;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (fFiltered == null) {
                return true;
            }
            return !fFiltered.contains(element);
        }
    }

    /**
     * Constructor
     *
     * @param timeGraphViewer
     *            the time graph viewer
     */
    public ShowFilterDialogAction(TimeGraphViewer timeGraphViewer) {
        fFilterDialog = new TimeGraphFilterDialog(timeGraphViewer.getControl().getShell());
        fTimeGraphViewer = timeGraphViewer;
        setText(Messages.ShowFilterDialogAction_FilterActionNameText);
        setToolTipText(Messages.ShowFilterDialogAction_FilterActionToolTipText);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_FILTERS));
    }

    @Override
    public void run() {
        ITimeGraphEntry[] topInput = fTimeGraphViewer.getTimeGraphContentProvider().getElements(fTimeGraphViewer.getInput());
        if (topInput != null) {
            List<? extends ITimeGraphEntry> allElements = listAllInputs(Arrays.asList(topInput));
            fFilterDialog.setInput(fTimeGraphViewer.getInput());
            fFilterDialog.setTitle(Messages.TmfTimeFilterDialog_WINDOW_TITLE);
            fFilterDialog.setMessage(Messages.TmfTimeFilterDialog_MESSAGE);
            fFilterDialog.setExpandedElements(allElements.toArray());
            RawViewerFilter rawViewerFilter = null;
            for (ViewerFilter filter : fTimeGraphViewer.getFilters()) {
                if (filter instanceof RawViewerFilter) {
                    rawViewerFilter = (RawViewerFilter) filter;
                }
            }
            if (rawViewerFilter != null && rawViewerFilter.getFiltered() != null) {
                ArrayList<? extends ITimeGraphEntry> nonFilteredElements = new ArrayList<>(allElements);
                nonFilteredElements.removeAll(rawViewerFilter.getFiltered());
                fFilterDialog.setInitialElementSelections(nonFilteredElements);
            } else {
                fFilterDialog.setInitialElementSelections(allElements);
            }
            fFilterDialog.open();
            if (fFilterDialog.getResult() != null) {
                if (fFilterDialog.getResult().length != allElements.size()) {
                    List<? extends ITimeGraphEntry> filteredElements = new ArrayList<>(allElements);
                    filteredElements.removeAll(Arrays.asList(fFilterDialog.getResult()));
                    if (rawViewerFilter == null) {
                        rawViewerFilter = new RawViewerFilter(filteredElements);
                        addFilter(rawViewerFilter);
                    } else {
                        rawViewerFilter.setFiltered(filteredElements);
                        changeFilter(rawViewerFilter);
                    }
                } else if (rawViewerFilter != null) {
                    removeFilter(rawViewerFilter);
                }
            }
        }
    }

    /**
     * Get the filter dialog.
     *
     * @return the filter dialog
     */
    public TimeGraphFilterDialog getFilterDialog() {
        return fFilterDialog;
    }

    /**
     * Add a viewer filter.
     *
     * @param filter
     *            The filter object to be added to the viewer
     */
    protected void addFilter(@NonNull ViewerFilter filter) {
        fTimeGraphViewer.addFilter(filter);
    }

    /**
     * Update a viewer filter.
     *
     * @param filter
     *            The updated filter object
     * @since 3.2
     */
    protected void changeFilter(@NonNull ViewerFilter filter) {
        fTimeGraphViewer.changeFilter(filter);
    }

    /**
     * Remove a viewer filter.
     *
     * @param filter
     *            The filter object to be removed from the viewer
     */
    protected void removeFilter(@NonNull ViewerFilter filter) {
        fTimeGraphViewer.removeFilter(filter);
    }

    /**
     * Refresh the viewer.
     */
    protected void refresh() {
        fTimeGraphViewer.refresh();
    }

    /**
     * Explores the list of top-level inputs and returns all the inputs.
     *
     * @param inputs
     *            The top-level inputs
     * @return All the inputs
     */
    private List<? extends ITimeGraphEntry> listAllInputs(List<? extends ITimeGraphEntry> inputs) {
        ArrayList<ITimeGraphEntry> items = new ArrayList<>();
        for (ITimeGraphEntry entry : inputs) {
            items.add(entry);
            if (entry != null && entry.hasChildren()) {
                items.addAll(listAllInputs(entry.getChildren()));
            }
        }
        return items;
    }
}
