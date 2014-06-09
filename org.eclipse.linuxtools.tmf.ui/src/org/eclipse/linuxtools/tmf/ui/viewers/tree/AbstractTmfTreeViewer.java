/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.tree;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.viewers.TmfTimeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Abstract class for viewers who will display data using a TreeViewer. It
 * automatically synchronizes with time information of the UI. It also
 * implements some common functionalities for all tree viewer, such as managing
 * the column data, content initialization and update. The viewer implementing
 * this does not have to worry about whether some code runs in the UI thread or
 * not.
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public abstract class AbstractTmfTreeViewer extends TmfTimeViewer {

    private final TreeViewer fTreeViewer;

    // ------------------------------------------------------------------------
    // Internal classes
    // ------------------------------------------------------------------------

    /* The elements of the tree viewer are of type ITmfTreeViewerEntry */
    private class TreeContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof ITmfTreeViewerEntry) {
                return ((ITmfTreeViewerEntry) inputElement).getChildren().toArray(new ITmfTreeViewerEntry[0]);
            }
            return new ITmfTreeViewerEntry[0];
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            ITmfTreeViewerEntry entry = (ITmfTreeViewerEntry) parentElement;
            List<? extends ITmfTreeViewerEntry> children = entry.getChildren();
            return children.toArray(new ITmfTreeViewerEntry[children.size()]);
        }

        @Override
        public Object getParent(Object element) {
            ITmfTreeViewerEntry entry = (ITmfTreeViewerEntry) element;
            return entry.getParent();
        }

        @Override
        public boolean hasChildren(Object element) {
            ITmfTreeViewerEntry entry = (ITmfTreeViewerEntry) element;
            return entry.hasChildren();
        }

    }

    /**
     * Base class to provide the labels for the tree viewer. Views extending
     * this class typically need to override the getColumnText method if they
     * have more than one column to display. It also allows to change the font
     * and colors of the cells.
     */
    protected static class TreeLabelProvider implements ITableLabelProvider, ITableFontProvider, ITableColorProvider {

        @Override
        public void addListener(ILabelProviderListener listener) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if ((element instanceof ITmfTreeViewerEntry) && (columnIndex == 0)) {
                ITmfTreeViewerEntry entry = (ITmfTreeViewerEntry) element;
                return entry.getName();
            }
            return new String();
        }

        @Override
        public Color getForeground(Object element, int columnIndex) {
            return Display.getCurrent().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
        }

        @Override
        public Color getBackground(Object element, int columnIndex) {
            return Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
        }

        @Override
        public Font getFont(Object element, int columnIndex) {
            return null;
        }

    }

    // ------------------------------------------------------------------------
    // Constructors and initialization methods
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite that holds this viewer
     * @param allowMultiSelect
     *            Whether multiple selections are allowed
     */
    public AbstractTmfTreeViewer(Composite parent, boolean allowMultiSelect) {
        super(parent);

        int flags = SWT.FULL_SELECTION | SWT.H_SCROLL;
        if (allowMultiSelect) {
            flags |= SWT.MULTI;
        }

        /* Build the tree viewer part of the view */
        fTreeViewer = new TreeViewer(parent, flags);
        fTreeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        final Tree tree = fTreeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        fTreeViewer.setContentProvider(new TreeContentProvider());
        fTreeViewer.setLabelProvider(new TreeLabelProvider());
        List<TmfTreeColumnData> columns = getColumnDataProvider().getColumnData();
        this.setTreeColumns(columns);
    }

    /**
     * Get the column data provider that will contain the list of columns to be
     * part of this viewer. It is called once during the constructor.
     *
     * @return The tree column data provider for this viewer.
     */
    protected abstract ITmfTreeColumnDataProvider getColumnDataProvider();

    /**
     * Sets the tree columns for this tree viewer
     *
     * @param columns
     *            The tree column data
     */
    public void setTreeColumns(final List<TmfTreeColumnData> columns) {
        boolean hasPercentProvider = false;
        for (final TmfTreeColumnData columnData : columns) {
            columnData.createColumn(fTreeViewer);
            hasPercentProvider |= (columnData.getPercentageProvider() != null);
        }

        if (hasPercentProvider) {
            /*
             * Handler that will draw bar charts in the cell using a percentage
             * value.
             */
            fTreeViewer.getTree().addListener(SWT.EraseItem, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    if (columns.get(event.index).getPercentageProvider() != null) {

                        double percentage = columns.get(event.index).getPercentageProvider().getPercentage(event.item.getData());
                        if (percentage == 0) { // No bar to draw
                            return;
                        }

                        if ((event.detail & SWT.SELECTED) > 0) {
                            /*
                             * The item is selected. Draw our own background to
                             * avoid overwriting the bar.
                             */
                            event.gc.fillRectangle(event.x, event.y, event.width, event.height);
                            event.detail &= ~SWT.SELECTED;
                        }

                        int barWidth = (int) ((fTreeViewer.getTree().getColumn(event.index).getWidth() - 8) * percentage);
                        int oldAlpha = event.gc.getAlpha();
                        Color oldForeground = event.gc.getForeground();
                        Color oldBackground = event.gc.getBackground();
                        /*
                         * Draws a transparent gradient rectangle from the color
                         * of foreground and background.
                         */
                        event.gc.setAlpha(64);
                        event.gc.setForeground(event.item.getDisplay().getSystemColor(SWT.COLOR_BLUE));
                        event.gc.setBackground(event.item.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
                        event.gc.fillGradientRectangle(event.x, event.y, barWidth, event.height, true);
                        event.gc.drawRectangle(event.x, event.y, barWidth, event.height);
                        /* Restores old values */
                        event.gc.setForeground(oldForeground);
                        event.gc.setBackground(oldBackground);
                        event.gc.setAlpha(oldAlpha);
                        event.detail &= ~SWT.BACKGROUND;
                    }
                }
            });
        }
    }

    /**
     * Set the label provider that will fill the columns of the tree viewer
     *
     * @param labelProvider
     *            The label provider to fill the columns
     */
    protected void setLabelProvider(IBaseLabelProvider labelProvider) {
        fTreeViewer.setLabelProvider(labelProvider);
    }

    /**
     * Get the tree viewer object
     *
     * @return The tree viewer object displayed by this viewer
     */
    protected TreeViewer getTreeViewer() {
        return fTreeViewer;
    }

    // ------------------------------------------------------------------------
    // ITmfViewer
    // ------------------------------------------------------------------------

    @Override
    public Control getControl() {
        return fTreeViewer.getControl();
    }

    @Override
    public void refresh() {
        Tree tree = fTreeViewer.getTree();
        tree.setRedraw(false);
        fTreeViewer.refresh();
        fTreeViewer.expandAll();
        tree.setRedraw(true);
    }

    @Override
    public void loadTrace(ITmfTrace trace) {
        super.loadTrace(trace);
        Thread thread = new Thread() {
            @Override
            public void run() {
                initializeDataSource();
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        clearContent();
                        updateContent(getWindowStartTime(), getWindowEndTime(), false);
                    }
                });
            }
        };
        thread.start();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Set the currently selected items in the treeviewer
     *
     * @param selection
     *            The list of selected items
     * @since 3.1
     */
    public void setSelection(@NonNull List<ITmfTreeViewerEntry> selection) {
        IStructuredSelection sel = new StructuredSelection(selection);
        fTreeViewer.setSelection(sel, true);
    }

    /**
     * Add a selection listener to the tree viewer. This will be called when the
     * selection changes and contain all the selected items.
     *
     * The selection change listener can be used like this:
     *
     * <pre>
     * getTreeViewer().addSelectionChangeListener(new ISelectionChangedListener() {
     *     &#064;Override
     *     public void selectionChanged(SelectionChangedEvent event) {
     *         if (event.getSelection() instanceof IStructuredSelection) {
     *             Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
     *             if (selection instanceof ITmfTreeViewerEntry) {
     *                 // Do something
     *             }
     *         }
     *     }
     * });
     * </pre>
     *
     * @param listener
     *            The {@link ISelectionChangedListener}
     */
    public void addSelectionChangeListener(ISelectionChangedListener listener) {
        fTreeViewer.addSelectionChangedListener(listener);
    }

    /**
     * Method called when the trace is loaded, to initialize any data once the
     * trace has been set, but before the first call to update the content of
     * the viewer.
     */
    protected void initializeDataSource() {

    }

    /**
     * Clears the current content of the viewer.
     */
    protected void clearContent() {
        fTreeViewer.setInput(null);
    }

    /**
     * Method called after the content has been updated and the new input has
     * been set on the tree.
     *
     * @param rootEntry
     *            The new input of this viewer, or null if none
     * @since 3.1
     */
    protected void contentChanged(ITmfTreeViewerEntry rootEntry) {

    }

    /**
     * Requests an update of the viewer's content in a given time range or
     * selection time range. An extra parameter defines whether these times
     * correspond to the selection or the visible range, as the viewer may
     * update differently in those cases.
     *
     * @param start
     *            The start time of the requested content
     * @param end
     *            The end time of the requested content
     * @param isSelection
     *            <code>true</code> if this time range is for a selection,
     *            <code>false</code> for the visible time range
     */
    protected void updateContent(final long start, final long end, final boolean isSelection) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                final ITmfTreeViewerEntry rootEntry = updateElements(start, end, isSelection);
                /* Set the input in main thread only if it didn't change */
                if (rootEntry != null) {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            if (rootEntry != fTreeViewer.getInput()) {
                                fTreeViewer.setInput(rootEntry);
                                contentChanged(rootEntry);
                            } else {
                                fTreeViewer.refresh();
                            }
                            // FIXME should add a bit of padding
                            for (TreeColumn column : fTreeViewer.getTree().getColumns()) {
                                column.pack();
                            }
                        }
                    });
                }
            }
        };
        thread.start();
    }

    /**
     * Update the entries to the given start/end time. An extra parameter
     * defines whether these times correspond to the selection or the visible
     * range, as the viewer may update differently in those cases. This methods
     * returns a root node that is not meant to be visible. The children of this
     * 'fake' root node are the first level of entries that will appear in the
     * tree. If no update is necessary, the method should return
     * <code>null</code>. To empty the tree, a root node containing an empty
     * list of children should be returned.
     *
     * This method is not called in the UI thread when using the default viewer
     * content update. Resource-intensive calculations here should not block the
     * UI.
     *
     * @param start
     *            The start time of the requested content
     * @param end
     *            The end time of the requested content
     * @param isSelection
     *            <code>true</code> if this time range is for a selection,
     *            <code>false</code> for the visible time range
     * @return The root entry of the list of entries to display or
     *         <code>null</code> if no update necessary
     */
    protected abstract ITmfTreeViewerEntry updateElements(long start, long end, boolean isSelection);

    /**
     * Get the current input displayed by the viewer
     *
     * @return The input of the tree viewer, the root entry
     */
    protected ITmfTreeViewerEntry getInput() {
        return (ITmfTreeViewerEntry) fTreeViewer.getInput();
    }

    // ------------------------------------------------------------------------
    // Signal Handler
    // ------------------------------------------------------------------------

    /**
     * Signal handler for handling of the time synch signal. The times
     * correspond to the selection by the user, not the visible time range.
     *
     * @param signal
     *            The time synch signal {@link TmfTimeSynchSignal}
     */
    @Override
    @TmfSignalHandler
    public void selectionRangeUpdated(TmfTimeSynchSignal signal) {
        super.selectionRangeUpdated(signal);
        if ((signal.getSource() != this) && (getTrace() != null)) {
            updateContent(this.getSelectionBeginTime(), this.getSelectionEndTime(), true);
        }
    }

    /**
     * Signal handler for handling of the time range synch signal. This time
     * range is the visible zone of the view.
     *
     * @param signal
     *            The time range synch signal {@link TmfRangeSynchSignal}
     */
    @Override
    @TmfSignalHandler
    public void timeRangeUpdated(TmfRangeSynchSignal signal) {
        super.timeRangeUpdated(signal);
        updateContent(this.getWindowStartTime(), this.getWindowEndTime(), false);
    }

    @Override
    public void reset() {
        super.reset();
        clearContent();
    }

}
