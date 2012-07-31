/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics;

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.ITmfColumnDataProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnData;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnDataProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeRootFactory;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfTreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * A basic viewer to display statistics in the statistics view.
 *
 * @author Mathieu Denis
 * @version 2.0
 * @since 2.0
 */
public class TmfStatisticsViewer extends TmfComponent {

    /**
     * Refresh frequency
     */
    protected static final Long STATS_INPUT_CHANGED_REFRESH = 5000L;

    /**
     * The actual tree viewer to display
     */
    protected TreeViewer fTreeViewer;

    /**
     * View instance counter (for multiple statistic views)
     */
    private static int fCountInstance = 0;

    /**
     * Number of this instance. Used as an instance ID.
     */
    private int fInstanceNb;

    /**
     * Object to store the cursor while waiting for the experiment to load
     */
    private Cursor fWaitCursor = null;

    /**
     * Default constructor
     *
     * @param parent
     *            The parent of this viewer
     */
    public TmfStatisticsViewer(Composite parent) {
        // Increment a counter to make sure the tree ID is unique.
        fCountInstance++;
        fInstanceNb = fCountInstance;

        final List<TmfBaseColumnData> columnDataList = getColumnDataProvider().getColumnData();
        parent.setLayout(new FillLayout());

        fTreeViewer = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        fTreeViewer.setContentProvider(new TmfTreeContentProvider());
        fTreeViewer.getTree().setHeaderVisible(true);
        fTreeViewer.setUseHashlookup(true);

        // Creates the columns defined by the column data provider
        for (final TmfBaseColumnData columnData : columnDataList) {
            final TreeViewerColumn treeColumn = new TreeViewerColumn(fTreeViewer, columnData.getAlignment());
            treeColumn.getColumn().setText(columnData.getHeader());
            treeColumn.getColumn().setWidth(columnData.getWidth());
            treeColumn.getColumn().setToolTipText(columnData.getTooltip());

            if (columnData.getComparator() != null) { // A comparator is defined.
                // Adds a listener on the columns header for sorting purpose.
                treeColumn.getColumn().addSelectionListener(new SelectionAdapter() {

                    private ViewerComparator reverseComparator;

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        // Initializes the reverse comparator once.
                        if (reverseComparator == null) {
                            reverseComparator = new ViewerComparator() {
                                @Override
                                public int compare(Viewer viewer, Object e1, Object e2) {
                                    return -1 * columnData.getComparator().compare(viewer, e1, e2);
                                }
                            };
                        }

                        if (fTreeViewer.getTree().getSortDirection() == SWT.UP
                                || fTreeViewer.getTree().getSortColumn() != treeColumn.getColumn()) {
                            /*
                             * Puts the descendant order if the old order was
                             * up or if the selected column has changed.
                             */
                            fTreeViewer.setComparator(columnData.getComparator());
                            fTreeViewer.getTree().setSortDirection(SWT.DOWN);
                        } else {
                            /*
                             * Puts the ascendant ordering if the selected
                             * column hasn't changed.
                             */
                            fTreeViewer.setComparator(reverseComparator);
                            fTreeViewer.getTree().setSortDirection(SWT.UP);
                        }
                        fTreeViewer.getTree().setSortColumn(treeColumn.getColumn());
                    }
                });
            }
            treeColumn.setLabelProvider(columnData.getLabelProvider());
        }

        // Handler that will draw the bar charts.
        fTreeViewer.getTree().addListener(SWT.EraseItem, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (columnDataList.get(event.index).getPercentageProvider() != null) {
                    TmfStatisticsTreeNode node = (TmfStatisticsTreeNode) event.item.getData();

                    double percentage = columnDataList.get(event.index).getPercentageProvider().getPercentage(node);
                    if (percentage == 0) {  // No bar to draw
                        return;
                    }

                    if ((event.detail & SWT.SELECTED) > 0) {    // The item is selected.
                        // Draws our own background to avoid overwritten the bar.
                        event.gc.fillRectangle(event.x, event.y, event.width, event.height);
                        event.detail &= ~SWT.SELECTED;
                    }

                    int barWidth = (int) ((fTreeViewer.getTree().getColumn(event.index).getWidth() - 8) * percentage);
                    int oldAlpha = event.gc.getAlpha();
                    Color oldForeground = event.gc.getForeground();
                    Color oldBackground = event.gc.getBackground();
                    /*
                     * Draws a transparent gradient rectangle from the color of
                     * foreground and background.
                     */
                    event.gc.setAlpha(64);
                    event.gc.setForeground(event.item.getDisplay().getSystemColor(SWT.COLOR_BLUE));
                    event.gc.setBackground(event.item.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
                    event.gc.fillGradientRectangle(event.x, event.y, barWidth, event.height, true);
                    event.gc.drawRectangle(event.x, event.y, barWidth, event.height);
                    // Restores old values
                    event.gc.setForeground(oldForeground);
                    event.gc.setBackground(oldBackground);
                    event.gc.setAlpha(oldAlpha);
                    event.detail &= ~SWT.BACKGROUND;
                }
            }
        });

        // Initializes the comparator parameters
        fTreeViewer.setComparator(columnDataList.get(0).getComparator());
        fTreeViewer.getTree().setSortColumn(fTreeViewer.getTree().getColumn(0));
        fTreeViewer.getTree().setSortDirection(SWT.DOWN);
    }

    /**
     * Refreshes this viewer completely with information freshly obtained from
     * this viewer's model.
     */
    public void refresh() {
        fTreeViewer.refresh();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.core.component.TmfComponent#dispose()
     */
    @Override
    public void dispose() {
        if (fWaitCursor != null) {
            fWaitCursor.dispose();
        }
        fTreeViewer.getControl().dispose();
        super.dispose();
        // clean the model
        TmfStatisticsTreeRootFactory.removeAll();
    }

    /**
     * Focus on the statistics tree of the viewer
     */
    public void setFocus() {
        fTreeViewer.getTree().setFocus();
    }

    /**
     * Sets or clears the input for this viewer.
     *
     * @param input
     *            The input of this viewer, or <code>null</code> if none
     */
    public void setInput(TmfStatisticsTreeNode input) {
        fTreeViewer.setInput(input);
    }

    /**
     * Returns the quantity of data to retrieve before a refresh of the view is
     * performed
     *
     * @return the quantity of data to retrieve before a refresh of the view is
     *         performed.
     */
    public long getInputChangedRefresh() {
        return STATS_INPUT_CHANGED_REFRESH;
    }

    /**
     * This method can be overridden to implement another way to represent the
     * statistics data and to retrieve the information for display.
     *
     * @return a TmfStatisticsData object.
     */
    public AbsTmfStatisticsTree getStatisticData() {
        return new TmfBaseStatisticsTree();
    }

    /**
     * Get the input of the viewer
     *
     * @return an object representing the input of the statistics viewer
     */
    public Object getInput() {
        return fTreeViewer.getInput();
    }

    /**
     * Returns the primary control associated with this viewer.
     *
     * @return the SWT control which displays this viewer's content
     */
    public Control getControl() {
        return fTreeViewer.getControl();
    }

    /**
     * When the experiment is loading the cursor will be different so the user
     * know the processing is not finished yet.
     *
     * @param waitInd
     *            Indicates if we need to show the waiting cursor, or the
     *            default one
     */
    public void waitCursor(final boolean waitInd) {
        if ((fTreeViewer == null) || (fTreeViewer.getTree().isDisposed())) {
            return;
        }

        Display display = fTreeViewer.getControl().getDisplay();
        if (fWaitCursor == null) {
            fWaitCursor = new Cursor(display, SWT.CURSOR_WAIT);
        }

        // Perform the updates on the UI thread
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                if ((fTreeViewer != null)
                        && (!fTreeViewer.getTree().isDisposed())) {
                    Cursor cursor = null; /* indicates default */
                    if (waitInd) {
                        cursor = fWaitCursor;
                    }
                    fTreeViewer.getControl().setCursor(cursor);
                }
            }
        });
    }

    /**
     * Constructs the ID based on the experiment name and the instance number
     *
     * @param name
     *            The name of the trace to show in the view
     * @return a view ID
     */
    public String getTreeID(String name) {
        return name + fInstanceNb;
    }

    /**
     * This method can be overridden to change the representation of the data in
     * the columns.
     *
     * @return an object implementing ITmfBaseColumnDataProvider.
     */
    protected ITmfColumnDataProvider getColumnDataProvider() {
        return new TmfBaseColumnDataProvider();
    }
}
