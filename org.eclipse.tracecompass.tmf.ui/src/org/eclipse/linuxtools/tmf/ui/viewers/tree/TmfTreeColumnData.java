/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis - Initial Implementation and API (in TmfBaseColumnData of
 *                            statistics framework)
 *   Bernd Hufmann - Added Annotations
 *   Geneviève Bastien - Moved TmfBaseColumnData to this class and adapted
 *                            it for the abstract tree viewer
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.tree;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Tree;

/**
 * Represents a column in an abstract tree viewer. It allows to define the
 * column's characteristics: text, width, alignment, tooltip, comparators,
 * percent providers, whether the column is movable, etc.
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfTreeColumnData {
    /** Name of the column. */
    private final String fText;
    /** Width of the column. */
    private int fWidth = -1;
    /** Alignment of the column. */
    private int fAlignment = SWT.LEAD;
    /** Tooltip of the column. */
    private String fTooltip = null;
    /** Used to sort elements of this column. If null, column is not sortable. */
    private ViewerComparator fComparator = null;
    /** Whether the column is movable */
    private boolean fMovable = false;
    /** Used to draw bar charts in this column. Can be null. */
    private ITmfColumnPercentageProvider fPercentageProvider = null;

    /** Used to draw bar charts in columns. */
    public interface ITmfColumnPercentageProvider {

        /**
         * Percentage provider. Returns a percentage (between 0 and 100) from
         * the given object. The object is usually an entry (a line of the tree
         * viewer).
         *
         * @param data
         *            The data object corresponding to a line in the tree.
         * @return The value as a percentage (between 0 and 100)
         */
        public double getPercentage(Object data);
    }

    /**
     * Constructor with parameters
     *
     * @param text
     *            Text of the column. The name will be shown at the top of the
     *            column.
     */
    public TmfTreeColumnData(String text) {
        fText = text;
    }

    /**
     * Get the header text of a column
     *
     * @return The header text
     */
    public String getText() {
        return fText;
    }

    /**
     * Get the width of the column
     *
     * @return The column width
     */
    public int getWidth() {
        return fWidth;
    }

    /**
     * Get the alignment of the column
     *
     * @return The alignment (for example SWT.LEAD, SWT.RIGHT, etc)
     */
    public int getAlignment() {
        return fAlignment;
    }

    /**
     * Get the tooltip text to go with this column
     *
     * @return The tooltip text
     */
    public String getTooltip() {
        return fTooltip;
    }

    /**
     * Get the comparator used to sort columns. If <code>null</code>, then the
     * column is not sortable
     *
     * @return The column comparator
     */
    public ViewerComparator getComparator() {
        return fComparator;
    }

    /**
     * Get the percentage provider for this column. This will allow to draw a
     * bar chart inside the cells of this columns
     *
     * @return The percentage provider
     */
    public ITmfColumnPercentageProvider getPercentageProvider() {
        return fPercentageProvider;
    }

    /**
     * Return whether the column is movable or not
     *
     * @return True if column can be moved, false otherwise.
     */
    public boolean isMovable() {
        return fMovable;
    }

    /**
     * Set the width of the column. If not set, -1 is used.
     *
     * @param width
     *            Width of the column. Use -1 for tree viewer's default
     *            behavior.
     */
    public void setWidth(int width) {
        fWidth = width;
    }

    /**
     * Set the alignment of this column. If not set, default value is SWT.LEAD.
     *
     * @param alignment
     *            Alignment of the column. For example, SWT.LEAD, SWT.RIGHT,
     *            SWT.LEFT
     */
    public void setAlignment(int alignment) {
        fAlignment = alignment;
    }

    /**
     * Set the tooltip associated with this column
     *
     * @param tooltip
     *            the tooltip text
     */
    public void setTooltip(String tooltip) {
        fTooltip = tooltip;
    }

    /**
     * Set the comparator used to sort the column
     *
     * @param comparator
     *            The comparator. Use <code>null</code> to not sort the column.
     */
    public void setComparator(ViewerComparator comparator) {
        fComparator = comparator;
    }

    /**
     * Set the percentage provider that will provide a percentage value to draw
     * a bar chart inside the cells of this column
     *
     * @param percentProvider
     *            The percentage provider
     */
    public void setPercentageProvider(ITmfColumnPercentageProvider percentProvider) {
        fPercentageProvider = percentProvider;
    }

    /**
     * Set whether the column can be moved in the tree viewer. Default is false.
     *
     * @param movable
     *            true if the column can be moved, false otherwise
     */
    public void setMovable(boolean movable) {
        fMovable = movable;
    }

    /**
     * Create a TreeColumn with this column's data and adds it to a {@link Tree}
     *
     * @param treeViewer
     *            The {@link TreeViewer} object to add the column to
     * @return The newly created {@link TreeViewerColumn}
     */
    @NonNull
    public TreeViewerColumn createColumn(final TreeViewer treeViewer) {
        final TreeViewerColumn column = new TreeViewerColumn(treeViewer, getAlignment());
        final TmfTreeColumnData columnData = this;
        column.getColumn().setText(getText());
        if (getWidth() != -1) {
            column.getColumn().setWidth(getWidth());
        }
        if (getTooltip() != null) {
            column.getColumn().setToolTipText(getTooltip());
        }
        column.getColumn().setMoveable(isMovable());

        /* Add the comparator to sort the column */
        if (getComparator() != null) {
            column.getColumn().addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {

                    if (treeViewer.getTree().getSortDirection() == SWT.UP || treeViewer.getTree().getSortColumn() != column.getColumn()) {
                        /*
                         * Puts the descendant order if the old order was up
                         * or if the selected column has changed.
                         */
                        treeViewer.setComparator(columnData.getComparator());
                        treeViewer.getTree().setSortDirection(SWT.DOWN);
                    } else {
                        ViewerComparator reverseComparator;
                        /* Initializes the reverse comparator. */
                        reverseComparator = new ViewerComparator() {
                            @Override
                            public int compare(Viewer viewer, Object e1, Object
                                    e2) {
                                return -1 * columnData.getComparator().compare(viewer, e1, e2);
                            }
                        };

                        /*
                         * Puts the ascendant ordering if the selected
                         * column hasn't changed.
                         */
                        treeViewer.setComparator(reverseComparator);
                        treeViewer.getTree().setSortDirection(SWT.UP);
                    }
                    treeViewer.getTree().setSortColumn(column.getColumn());
                }
            });
        }

        return column;
    }
}
